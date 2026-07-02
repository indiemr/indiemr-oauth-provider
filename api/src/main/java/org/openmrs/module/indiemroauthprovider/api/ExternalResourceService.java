package org.openmrs.module.indiemroauthprovider.api;

import org.openmrs.api.OpenmrsService;

public interface ExternalResourceService extends OpenmrsService {
	
	void cancelAppointmentResources(String appointmentUuid) throws Exception;
}
