package org.openmrs.module.indiemroauthprovider.api;

import org.openmrs.api.OpenmrsService;
import org.openmrs.module.indiemroauthprovider.model.InternalResourceType;

public interface ExternalResourceService extends OpenmrsService {
	
	void voidInternalResources(InternalResourceType resourceType, String resourceUuid) throws Exception;
	
	void cancelAppointmentResources(String appointmentUuid) throws Exception;
}
