package org.openmrs.module.indiemroauthprovider.dao;

import java.util.List;

import org.openmrs.module.indiemroauthprovider.model.ClinicForm;

public interface ClinicFormDao {
	
	ClinicForm getById(Long id);
	
	ClinicForm findByLocationAndType(String locationUuid, String formType);
	
	/** Any non-DISABLED row holding this Google form, in ANY workspace — the collision check. */
	ClinicForm findActiveByGoogleFormId(String googleFormId);
	
	List<ClinicForm> findAllByLocation(String locationUuid);
	
	ClinicForm save(ClinicForm clinicForm);
}
