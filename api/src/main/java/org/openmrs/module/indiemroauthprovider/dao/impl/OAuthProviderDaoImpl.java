package org.openmrs.module.indiemroauthprovider.dao.impl;

import org.openmrs.api.db.hibernate.DbSessionFactory;
import org.openmrs.module.indiemroauthprovider.dao.OAuthProviderDao;
import org.openmrs.module.indiemroauthprovider.model.OAuthProvider;

public class OAuthProviderDaoImpl implements OAuthProviderDao {
	
	private DbSessionFactory sessionFactory;
	
	public void setSessionFactory(DbSessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	@Override
	public OAuthProvider findEnabledByCode(String code) {
		return (OAuthProvider) sessionFactory.getCurrentSession()
		        .createQuery("from OAuthProvider p where p.code = :code and p.enabled = true").setParameter("code", code)
		        .uniqueResult();
	}
	
	@Override
	public OAuthProvider findByCode(String code) {
		return (OAuthProvider) sessionFactory.getCurrentSession().createQuery("from OAuthProvider p where p.code = :code")
		        .setParameter("code", code).uniqueResult();
	}
}
