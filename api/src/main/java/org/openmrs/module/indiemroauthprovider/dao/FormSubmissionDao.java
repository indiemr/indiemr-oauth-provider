package org.openmrs.module.indiemroauthprovider.dao;

import java.util.List;

import org.openmrs.module.indiemroauthprovider.model.FormSubmission;

public interface FormSubmissionDao {
	
	FormSubmission findByGoogleResponseId(String googleResponseId);
	
	FormSubmission save(FormSubmission submission);
	
	List<FormSubmission> findByPatientAndForm(String patientUuid, Long clinicFormId);
	
	List<FormSubmission> findUnresolved(Long clinicFormId);
}
