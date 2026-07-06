package org.openmrs.module.indiemroauthprovider.dao.impl;

import java.util.List;

import org.openmrs.api.db.hibernate.DbSessionFactory;
import org.openmrs.module.indiemroauthprovider.dao.OAuthCapabilityDao;
import org.openmrs.module.indiemroauthprovider.model.OAuthCapability;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

@Repository("indiemroauthprovider.OAuthCapabilityDao")
public class OAuthCapabilityDaoImpl implements OAuthCapabilityDao {
	
	@Autowired
	@Qualifier("dbSessionFactory")
	private DbSessionFactory sessionFactory;
	
	@Override
	public OAuthCapability findByCode(String code) {
		return (OAuthCapability) sessionFactory.getCurrentSession()
		        .createQuery("from OAuthCapability c where c.code = :code").setParameter("code", code).uniqueResult();
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<OAuthCapability> findByCodes(List<String> codes) {
		return sessionFactory.getCurrentSession().createQuery("from OAuthCapability c where c.code in (:codes)")
		        .setParameterList("codes", codes).list();
	}
}
