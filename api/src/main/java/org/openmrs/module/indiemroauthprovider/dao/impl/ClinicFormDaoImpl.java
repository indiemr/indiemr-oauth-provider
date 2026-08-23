package org.openmrs.module.indiemroauthprovider.dao.impl;

import java.util.Date;
import java.util.List;

import org.openmrs.api.context.Context;
import org.openmrs.api.db.hibernate.DbSessionFactory;
import org.openmrs.module.indiemroauthprovider.dao.ClinicFormDao;
import org.openmrs.module.indiemroauthprovider.model.ClinicForm;

public class ClinicFormDaoImpl implements ClinicFormDao {
	
	private DbSessionFactory sessionFactory;
	
	public void setSessionFactory(DbSessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	@Override
	public ClinicForm getById(Long id) {
		return (ClinicForm) sessionFactory.getCurrentSession().get(ClinicForm.class, id);
	}
	
	@Override
	public ClinicForm findByLocationAndType(String locationUuid, String formType) {
		return (ClinicForm) sessionFactory.getCurrentSession()
		        .createQuery("from ClinicForm f where f.locationUuid = :locationUuid and f.formType = :formType")
		        .setParameter("locationUuid", locationUuid).setParameter("formType", formType).uniqueResult();
	}
	
	@Override
	public ClinicForm findActiveByGoogleFormId(String googleFormId) {
		return (ClinicForm) sessionFactory.getCurrentSession()
		        .createQuery("from ClinicForm f where f.activeFormId = :googleFormId")
		        .setParameter("googleFormId", googleFormId).uniqueResult();
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<ClinicForm> findAllByLocation(String locationUuid) {
		return sessionFactory.getCurrentSession()
		        .createQuery("from ClinicForm f where f.locationUuid = :locationUuid order by f.formType")
		        .setParameter("locationUuid", locationUuid).list();
	}
	
	@Override
	public ClinicForm save(ClinicForm clinicForm) {
		Date now = new Date();
		Integer userId = currentUserId();
		if (clinicForm.getCreatedAt() == null) {
			clinicForm.setCreatedAt(now);
			clinicForm.setCreatedBy(userId);
		}
		clinicForm.setUpdatedAt(now);
		clinicForm.setUpdatedBy(userId);
		clinicForm.setActiveFormId(ClinicForm.STATUS_DISABLED.equals(clinicForm.getStatus()) ? null : clinicForm
		        .getGoogleFormId());
		
		if (clinicForm.getId() == null) {
			sessionFactory.getCurrentSession().save(clinicForm);
			return clinicForm;
		}
		return (ClinicForm) sessionFactory.getCurrentSession().merge(clinicForm);
	}
	
	private static Integer currentUserId() {
		return Context.getAuthenticatedUser() != null ? Context.getAuthenticatedUser().getUserId() : null;
	}
}
