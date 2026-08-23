package org.openmrs.module.indiemroauthprovider.dao.impl;

import java.util.Date;
import java.util.List;

import org.openmrs.api.db.hibernate.DbSessionFactory;
import org.openmrs.module.indiemroauthprovider.dao.FormSubmissionDao;
import org.openmrs.module.indiemroauthprovider.model.FormSubmission;

public class FormSubmissionDaoImpl implements FormSubmissionDao {
	
	private DbSessionFactory sessionFactory;
	
	public void setSessionFactory(DbSessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	@Override
	public FormSubmission findByGoogleResponseId(String googleResponseId) {
		return (FormSubmission) sessionFactory.getCurrentSession()
		        .createQuery("from FormSubmission s where s.googleResponseId = :responseId")
		        .setParameter("responseId", googleResponseId).uniqueResult();
	}
	
	@Override
	public FormSubmission save(FormSubmission submission) {
		if (submission.getFetchedAt() == null) {
			submission.setFetchedAt(new Date());
		}
		if (submission.getId() == null) {
			sessionFactory.getCurrentSession().save(submission);
			return submission;
		}
		return (FormSubmission) sessionFactory.getCurrentSession().merge(submission);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<FormSubmission> findByPatientAndForm(String patientUuid, Long clinicFormId) {
		return sessionFactory
		        .getCurrentSession()
		        .createQuery(
		            "select s from FormSubmission s join s.formShare share "
		                    + "where share.patientUuid = :patientUuid and s.clinicForm.id = :formId "
		                    + "order by s.submittedAt desc").setParameter("patientUuid", patientUuid)
		        .setParameter("formId", clinicFormId).list();
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<FormSubmission> findUnresolved(Long clinicFormId) {
		return sessionFactory
		        .getCurrentSession()
		        .createQuery(
		            "from FormSubmission s where s.clinicForm.id = :formId and s.formShare is null "
		                    + "order by s.submittedAt desc").setParameter("formId", clinicFormId).list();
	}
}
