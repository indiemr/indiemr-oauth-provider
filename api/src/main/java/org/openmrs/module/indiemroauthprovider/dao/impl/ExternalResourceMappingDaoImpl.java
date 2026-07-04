package org.openmrs.module.indiemroauthprovider.dao.impl;

import java.util.Date;
import java.util.List;

import org.openmrs.api.db.hibernate.DbSessionFactory;
import org.openmrs.module.indiemroauthprovider.dao.ExternalResourceMappingDao;
import org.openmrs.module.indiemroauthprovider.model.ExternalResourceMapping;
import org.openmrs.module.indiemroauthprovider.model.OAuthAccount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

@Repository("indiemroauthprovider.ExternalResourceMappingDao")
public class ExternalResourceMappingDaoImpl implements ExternalResourceMappingDao {
	
	@Autowired
	@Qualifier("dbSessionFactory")
	private DbSessionFactory sessionFactory;
	
	@Override
	public ExternalResourceMapping save(ExternalResourceMapping mapping) {
		if (mapping.getCreatedAt() == null) {
			mapping.setCreatedAt(new Date());
		}
		OAuthAccount account = (OAuthAccount) sessionFactory.getCurrentSession().get(OAuthAccount.class,
		    mapping.getOauthAccount().getId());
		mapping.setOauthAccount(account);
		sessionFactory.getCurrentSession().save(mapping);
		return mapping;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<ExternalResourceMapping> findByInternalResource(String internalResourceType, String internalResourceUuid) {
		return sessionFactory
		        .getCurrentSession()
		        .createQuery(
		            "from ExternalResourceMapping m " + "join fetch m.oauthAccount a join fetch a.oauthProvider p "
		                    + "where m.internalResourceType = :type and m.internalResourceUuid = :uuid and m.voided = false")
		        .setParameter("type", internalResourceType).setParameter("uuid", internalResourceUuid).list();
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<ExternalResourceMapping> findByProviderAndInternalResource(String providerUuid, String internalResourceType,
	        String internalResourceUuid) {
		return sessionFactory
		        .getCurrentSession()
		        .createQuery(
		            "from ExternalResourceMapping m " + "join fetch m.oauthAccount a join fetch a.oauthProvider p "
		                    + "where m.provider.uuid = :providerUuid and m.internalResourceType = :type "
		                    + "and m.internalResourceUuid = :uuid and m.voided = false")
		        .setParameter("providerUuid", providerUuid).setParameter("type", internalResourceType)
		        .setParameter("uuid", internalResourceUuid).list();
	}
	
	@Override
	public void voidByInternalResource(String internalResourceType, String internalResourceUuid) {
		sessionFactory
		        .getCurrentSession()
		        .createQuery(
		            "update ExternalResourceMapping m set m.voided = true "
		                    + "where m.internalResourceType = :type and m.internalResourceUuid = :uuid and m.voided = false")
		        .setParameter("type", internalResourceType).setParameter("uuid", internalResourceUuid).executeUpdate();
	}
	
	@Override
	public void voidByProviderAndInternalResource(String providerUuid, String internalResourceType,
	        String internalResourceUuid) {
		sessionFactory
		        .getCurrentSession()
		        .createQuery(
		            "update ExternalResourceMapping m set m.voided = true" + "where m.provider.uuid = :providerUuid "
		                    + "and m.internalResourceType = :internalResourceType "
		                    + "and m.internalResourceUuid = :internalResourceUuid" + "and m.voided = false")
		        .setParameter("providerUuid", providerUuid).setParameter("internalResourceType", internalResourceType)
		        .setParameter("internalResourceUuid", internalResourceUuid).executeUpdate();
	}
}
