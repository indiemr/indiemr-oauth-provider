package org.openmrs.module.indiemroauthprovider.api;

import org.openmrs.Provider;
import org.openmrs.annotation.Authorized;
import org.openmrs.api.OpenmrsService;
import org.openmrs.module.indiemroauthprovider.PrivilegeConstants;
import org.openmrs.module.indiemroauthprovider.model.InternalResourceType;
import org.springframework.transaction.annotation.Transactional;

public interface ExternalResourceService extends OpenmrsService {
	
	@Transactional
	@Authorized({ PrivilegeConstants.MANAGE_EVENTS })
	void voidInternalResources(InternalResourceType resourceType, String resourceUuid) throws Exception;
	
	@Transactional
	@Authorized({ PrivilegeConstants.MANAGE_EVENTS })
	void cancelResources(Provider provider, String internalResourceType, String internalResourceUuid) throws Exception;
	
	@Transactional
	@Authorized({ PrivilegeConstants.MANAGE_EVENTS })
	void cancelAppointmentResources(String appointmentUuid) throws Exception;
}
