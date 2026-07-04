package org.openmrs.module.indiemroauthprovider.api;

import org.openmrs.Provider;
import org.openmrs.api.OpenmrsService;
import org.openmrs.module.indiemroauthprovider.model.InternalResourceType;

public interface ExternalResourceService extends OpenmrsService {
	
	void voidInternalResources(InternalResourceType resourceType, String resourceUuid) throws Exception;
	
	void cancelResources(Provider provider, String internalResourceType, String internalResourceUuid) throws Exception;
	
	void cancelAppointmentResources(String appointmentUuid) throws Exception;
}
