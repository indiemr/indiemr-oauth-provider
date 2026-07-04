package org.openmrs.module.indiemroauthprovider.dao.impl;

import java.util.Date;

import org.openmrs.api.db.hibernate.DbSessionFactory;
import org.openmrs.module.indiemroauthprovider.dao.TeleconsultLinkDao;
import org.openmrs.module.indiemroauthprovider.model.ExternalResourceMapping;
import org.openmrs.module.indiemroauthprovider.model.TeleconsultLink;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

@Repository("indiemroauthprovider.TeleconsultLinkDao")
public class TeleconsultLinkDaoImpl implements TeleconsultLinkDao {
	
	@Autowired
	@Qualifier("dbSessionFactory")
	private DbSessionFactory sessionFactory;
	
	@Override
	public TeleconsultLink save(TeleconsultLink link) {
		Date now = new Date();
		link.setCreatedAt(now);
		link.setUpdatedAt(now);
		if (link.getStatus() == null) {
			link.setStatus(TeleconsultLink.STATUS_CREATED);
		}
		sessionFactory.getCurrentSession().save(link);
		return link;
	}
	
	@Override
	public TeleconsultLink findByToken(String token) {
		return (TeleconsultLink) sessionFactory
		        .getCurrentSession()
		        .createQuery(
		            "from TeleconsultLink l join fetch l.oauthAccount a where l.token = :token and l.voided = false")
		        .setParameter("token", token).uniqueResult();
	}
	
	@Override
	public void markStatus(String token, String status) {
		TeleconsultLink link = (TeleconsultLink) sessionFactory.getCurrentSession()
		        .createQuery("from TeleconsultLink l where l.token = :token").setParameter("token", token).uniqueResult();
		if (link != null) {
			link.setStatus(status);
			link.setUpdatedAt(new Date());
			sessionFactory.getCurrentSession().merge(link);
		}
	}
	
	@Override
	public void voidByAppointmentUuid(String appointmentUuid) {
		voidByInternalResource(ExternalResourceMapping.INTERNAL_APPOINTMENT, appointmentUuid);
	}
	
	@Override
	public void extendLinkExpiryForResource(String resourceType, String resourceUuid, Date newExpiresAt) {
		sessionFactory
		        .getCurrentSession()
		        .createQuery(
		            "update TeleconsultLink l set l.expiresAt = :expiresAt, l.updatedAt = :now " 
					        + "where l.voided = false "
		                    + "and l.externalResourceMapping.internalResourceType = :type "
		                    + "and l.externalResourceMapping.internalResourceUuid = :uuid")
		        .setParameter("expiresAt", newExpiresAt).
				setParameter("now", new Date())
				.setParameter("type", resourceType)
		        .setParameter("uuid", resourceUuid)
				.executeUpdate();
	}
	
	@Override
	public void voidByInternalResource(String resourceType, String resourceUuid) {
		sessionFactory
		        .getCurrentSession()
		        .createQuery(
		            "update TeleconsultLink l set l.voided = true, l.status = :status, l.updatedAt = :now"
		                    + "where l.voided = false " + "and l.externalResourceMapping.internalResourceType = :type"
		                    + "and l.externalResourceMapping.internalResourceUuid = :uuid")
		        .setParameter("status", TeleconsultLink.STATUS_EXPIRED).setParameter("now", new Date())
		        .setParameter("type", resourceType).setParameter("uuid", resourceUuid).executeUpdate();
	}
}
