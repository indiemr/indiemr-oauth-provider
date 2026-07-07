package org.openmrs.module.indiemroauthprovider.dao.impl;

import org.openmrs.api.db.hibernate.DbSessionFactory;
import org.openmrs.module.indiemroauthprovider.dao.ExternalEventDao;
import org.openmrs.module.indiemroauthprovider.model.ExternalEvent;
import org.openmrs.module.indiemroauthprovider.model.OAuthAccount;
import org.openmrs.module.indiemroauthprovider.util.OpenmrsDataUtils;

public class ExternalEventDaoImpl implements ExternalEventDao {
	
	private DbSessionFactory sessionFactory;
	
	public void setSessionFactory(DbSessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	@Override
	public ExternalEvent save(ExternalEvent event) {
		OpenmrsDataUtils.setCreated(event);
		OAuthAccount account = (OAuthAccount) sessionFactory.getCurrentSession().get(OAuthAccount.class,
		    event.getOauthAccount().getId());
		event.setOauthAccount(account);
		sessionFactory.getCurrentSession().saveOrUpdate(event);
		return event;
	}
	
	@Override
	public void voidEvent(ExternalEvent event, String reason) {
		OpenmrsDataUtils.voidWithReason(event, reason);
		OpenmrsDataUtils.setChanged(event);
		event.setStatus(ExternalEvent.STATUS_CANCELLED);
		sessionFactory.getCurrentSession().merge(event);
	}
}
