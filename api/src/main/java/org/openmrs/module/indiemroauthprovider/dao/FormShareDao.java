package org.openmrs.module.indiemroauthprovider.dao;

import java.util.List;

import org.openmrs.module.indiemroauthprovider.model.ClinicForm;
import org.openmrs.module.indiemroauthprovider.model.FormShare;

public interface FormShareDao {
	
	FormShare save(FormShare share);
	
	FormShare findByToken(String token);
	
	List<FormShare> findByPatient(String patientUuid);
	
	/** True while the patient is still waiting on a fill — what gates the poll (throttle aside). */
	boolean hasPendingShare(ClinicForm clinicForm, String patientUuid);
	
	/** R10: a re-link retires every still-open token minted against the old form. */
	int closePendingShares(Long clinicFormId);
}
