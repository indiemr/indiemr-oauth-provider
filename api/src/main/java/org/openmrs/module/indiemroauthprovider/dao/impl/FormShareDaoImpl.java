package org.openmrs.module.indiemroauthprovider.dao.impl;

import java.util.Date;
import java.util.List;

import org.openmrs.api.db.hibernate.DbSessionFactory;
import org.openmrs.module.indiemroauthprovider.dao.FormShareDao;
import org.openmrs.module.indiemroauthprovider.model.ClinicForm;
import org.openmrs.module.indiemroauthprovider.model.FormShare;

public class FormShareDaoImpl implements FormShareDao {
	
	private DbSessionFactory sessionFactory;
	
	public void setSessionFactory(DbSessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	@Override
	public FormShare save(FormShare share) {
		if (share.getCreatedAt() == null) {
			share.setCreatedAt(new Date());
		}
		if (share.getId() == null) {
			sessionFactory.getCurrentSession().save(share);
			return share;
		}
		return (FormShare) sessionFactory.getCurrentSession().merge(share);
	}
	
	@Override
	public FormShare findByToken(String token) {
		return (FormShare) sessionFactory.getCurrentSession().createQuery("from FormShare s where s.token = :token")
		        .setParameter("token", token).uniqueResult();
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<FormShare> findByPatient(String patientUuid) {
		return sessionFactory.getCurrentSession()
		        .createQuery("from FormShare s where s.patientUuid = :patientUuid order by s.createdAt desc")
		        .setParameter("patientUuid", patientUuid).list();
	}
	
	@Override
	public boolean hasPendingShare(ClinicForm clinicForm, String patientUuid) {
		Number count = (Number) sessionFactory
		        .getCurrentSession()
		        .createQuery(
		            "select count(*) from FormShare s where s.clinicForm.id = :formId "
		                    + "and s.patientUuid = :patientUuid and s.status = :status")
		        .setParameter("formId", clinicForm.getId()).setParameter("patientUuid", patientUuid)
		        .setParameter("status", FormShare.STATUS_SHARED).uniqueResult();
		return count != null && count.longValue() > 0;
	}
	
	@Override
	public int closePendingShares(Long clinicFormId) {
		return sessionFactory
		        .getCurrentSession()
		        .createQuery(
		            "update FormShare s set s.status = :closed where s.clinicForm.id = :formId and s.status = :shared")
		        .setParameter("closed", FormShare.STATUS_CLOSED).setParameter("formId", clinicFormId)
		        .setParameter("shared", FormShare.STATUS_SHARED).executeUpdate();
	}
}
