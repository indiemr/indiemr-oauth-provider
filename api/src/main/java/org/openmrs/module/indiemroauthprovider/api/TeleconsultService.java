package org.openmrs.module.indiemroauthprovider.api;

import org.openmrs.Provider;
import org.openmrs.annotation.Authorized;
import org.openmrs.api.OpenmrsService;
import org.openmrs.module.indiemroauthprovider.PrivilegeConstants;
import org.openmrs.module.indiemroauthprovider.dto.CancelCalendarEventRequest;
import org.openmrs.module.indiemroauthprovider.dto.CancelCalendarEventResponse;
import org.openmrs.module.indiemroauthprovider.dto.CreateCalendarEventRequest;
import org.openmrs.module.indiemroauthprovider.dto.CreateCalendarEventResponse;
import org.openmrs.module.indiemroauthprovider.dto.ResolveResult;
import org.openmrs.module.indiemroauthprovider.dto.UpdateCalendarEventRequest;
import org.openmrs.module.indiemroauthprovider.model.ExternalEvent;
import org.springframework.transaction.annotation.Transactional;

public interface TeleconsultService extends OpenmrsService {
	
	@Transactional(readOnly = true)
	@Authorized({ PrivilegeConstants.VIEW_EVENTS, PrivilegeConstants.MANAGE_EVENTS })
	ExternalEvent findActiveCalendarEvent(Provider provider, String oauthProviderCode, String resourceType, String resourceUuid);

	@Transactional(readOnly = true)
	@Authorized({ PrivilegeConstants.VIEW_EVENTS, PrivilegeConstants.MANAGE_EVENTS })
	boolean hasActiveCalendarEvent(Provider provider, String oauthProviderCode, String resourceType, String resourceUuid);

	@Transactional
	@Authorized({ PrivilegeConstants.MANAGE_EVENTS })
	CreateCalendarEventResponse createCalendarEvent(Provider provider, CreateCalendarEventRequest request) throws Exception;
	
	@Transactional
	@Authorized({ PrivilegeConstants.MANAGE_EVENTS })
	CreateCalendarEventResponse updateCalendarEvent(Provider provider, UpdateCalendarEventRequest request) throws Exception;
	
	@Transactional
	@Authorized({ PrivilegeConstants.MANAGE_EVENTS })
	CancelCalendarEventResponse cancelCalendarEvent(Provider provider, CancelCalendarEventRequest request) throws Exception;
	
	@Transactional(readOnly = true)
	ResolveResult resolveLink(String token);
}
