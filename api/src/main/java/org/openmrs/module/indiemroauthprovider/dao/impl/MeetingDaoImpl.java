package org.openmrs.module.indiemroauthprovider.dao.impl;

import java.util.Date;

import org.openmrs.api.db.hibernate.DbSessionFactory;
import org.openmrs.module.indiemroauthprovider.dao.MeetingDao;
import org.openmrs.module.indiemroauthprovider.model.Meeting;
import org.openmrs.module.indiemroauthprovider.util.OpenmrsDataUtils;

public class MeetingDaoImpl implements MeetingDao {
	
	private DbSessionFactory sessionFactory;
	
	public void setSessionFactory(DbSessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	@Override
	public Meeting save(Meeting meeting) {
		OpenmrsDataUtils.setCreated(meeting);
		if (meeting.getStatus() == null) {
			meeting.setStatus(Meeting.STATUS_CREATED);
		}
		sessionFactory.getCurrentSession().saveOrUpdate(meeting);
		return meeting;
	}
	
	@Override
	public Meeting findByToken(String token) {
		return (Meeting) sessionFactory
		        .getCurrentSession()
		        .createQuery(
		            "from Meeting m join fetch m.externalEvent e join fetch e.oauthAccount a "
		                    + "where m.token = :token and m.voided = false").setParameter("token", token).uniqueResult();
	}
	
	@Override
	public void markStatus(String token, String status) {
		Meeting meeting = (Meeting) sessionFactory.getCurrentSession().createQuery("from Meeting m where m.token = :token")
		        .setParameter("token", token).uniqueResult();
		if (meeting != null) {
			meeting.setStatus(status);
			meeting.setDateChanged(new Date());
			sessionFactory.getCurrentSession().merge(meeting);
		}
	}
	
	@Override
	public Meeting findActiveByExternalEventId(Integer externalEventId) {
		if (externalEventId == null) {
			return null;
		}
		return (Meeting) sessionFactory.getCurrentSession()
		        .createQuery("from Meeting m where m.voided = false and m.externalEvent.id = :eventId")
		        .setParameter("eventId", externalEventId).uniqueResult();
	}
	
	@Override
	public void extendExpiryByExternalEventId(Integer externalEventId, Date newExpiresAt) {
		Meeting meeting = findActiveByExternalEventId(externalEventId);
		if (meeting != null) {
			meeting.setExpiresAt(newExpiresAt);
			meeting.setDateChanged(new Date());
			sessionFactory.getCurrentSession().merge(meeting);
		}
	}
	
	@Override
	public void voidActiveByExternalEventId(Integer externalEventId, String reason) {
		Meeting meeting = findActiveByExternalEventId(externalEventId);
		if (meeting != null) {
			OpenmrsDataUtils.voidWithReason(meeting, reason);
			meeting.setStatus(Meeting.STATUS_EXPIRED);
			meeting.setDateChanged(new Date());
			sessionFactory.getCurrentSession().merge(meeting);
		}
	}
}
