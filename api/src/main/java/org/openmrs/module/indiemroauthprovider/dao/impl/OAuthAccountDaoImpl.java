package org.openmrs.module.indiemroauthprovider.dao.impl;

import java.util.Date;
import java.util.List;

import org.openmrs.Provider;
import org.openmrs.api.db.hibernate.DbSessionFactory;
import org.openmrs.module.indiemroauthprovider.dao.OAuthAccountDao;
import org.openmrs.module.indiemroauthprovider.model.OAuthAccount;
import org.openmrs.module.indiemroauthprovider.model.OAuthAccountCapability;
import org.openmrs.module.indiemroauthprovider.model.OAuthCapability;
import org.openmrs.module.indiemroauthprovider.model.OAuthProvider;

public class OAuthAccountDaoImpl implements OAuthAccountDao {
	
	private DbSessionFactory sessionFactory;
	
	public void setSessionFactory(DbSessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	@Override
	public OAuthAccount getById(Long id) {
		return (OAuthAccount) sessionFactory.getCurrentSession()
		        .createQuery("from OAuthAccount a join fetch a.oauthProvider where a.id = :id and a.voided = false")
		        .setParameter("id", id).uniqueResult();
	}
	
	@Override
	public OAuthAccount findByProviderAndProviderCode(Provider provider, String oauthProviderCode) {
		return (OAuthAccount) sessionFactory
		        .getCurrentSession()
		        .createQuery(
		            "from OAuthAccount a join fetch a.oauthProvider p "
		                    + "where a.provider.uuid = :providerUuid and p.code = :providerCode and a.voided = false")
		        .setParameter("providerUuid", provider.getUuid()).setParameter("providerCode", oauthProviderCode)
		        .uniqueResult();
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<OAuthAccount> findAllByProvider(Provider provider) {
		return sessionFactory
		        .getCurrentSession()
		        .createQuery(
		            "from OAuthAccount a join fetch a.oauthProvider p "
		                    + "where a.provider.uuid = :providerUuid and a.voided = false")
		        .setParameter("providerUuid", provider.getUuid()).list();
	}
	
	@Override
	public OAuthAccount save(OAuthAccount account) {
		return persistOrMerge(account);
	}
	
	@Override
	public OAuthAccount saveWithCapabilities(OAuthAccount account, List<String> capabilityCodes) {
		OAuthAccount managed = persistOrMerge(account);
		
		sessionFactory.getCurrentSession()
		        .createQuery("delete from OAuthAccountCapability c where c.oauthAccount.id = :accountId")
		        .setParameter("accountId", managed.getId()).executeUpdate();
		
		managed.getCapabilities().clear();
		
		for (String code : capabilityCodes) {
			OAuthCapability cap = (OAuthCapability) sessionFactory.getCurrentSession()
			        .createQuery("from OAuthCapability c where c.code = :code").setParameter("code", code).uniqueResult();
			if (cap == null) {
				throw new IllegalArgumentException("Unknown capability: " + code);
			}
			
			OAuthAccountCapability link = new OAuthAccountCapability();
			link.setOauthAccount(managed);
			link.setCapability(cap);
			managed.getCapabilities().add(link);
			sessionFactory.getCurrentSession().save(link);
		}
		
		sessionFactory.getCurrentSession().merge(managed);
		return managed;
	}
	
	@Override
	public void markRevoked(Long accountId) {
		OAuthAccount account = (OAuthAccount) sessionFactory.getCurrentSession().get(OAuthAccount.class, accountId);
		if (account != null) {
			account.setStatus(OAuthAccount.STATUS_REVOKED);
			account.setUpdatedAt(new Date());
			sessionFactory.getCurrentSession().merge(account);
		}
	}
	
	private OAuthAccount persistOrMerge(OAuthAccount account) {
		Date now = new Date();
		if (account.getCreatedAt() == null) {
			account.setCreatedAt(now);
		}
		account.setUpdatedAt(now);
		if (account.getStatus() == null) {
			account.setStatus(OAuthAccount.STATUS_ACTIVE);
		}
		
		OAuthProvider oauthProvider = (OAuthProvider) sessionFactory.getCurrentSession()
		        .createQuery("from OAuthProvider p where p.id = :id").setParameter("id", account.getOauthProvider().getId())
		        .uniqueResult();
		account.setOauthProvider(oauthProvider);
		
		if (account.getProvider() != null && account.getProvider().getProviderId() != null) {
			Provider provider = (Provider) sessionFactory.getCurrentSession().get(Provider.class,
			    account.getProvider().getProviderId());
			account.setProvider(provider);
		}
		
		if (account.getId() == null) {
			sessionFactory.getCurrentSession().save(account);
			return account;
		}
		return (OAuthAccount) sessionFactory.getCurrentSession().merge(account);
	}
}
