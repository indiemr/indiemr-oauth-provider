package org.openmrs.module.indiemroauthprovider.api;

import org.openmrs.Provider;
import org.openmrs.api.OpenmrsService;
import org.openmrs.module.indiemroauthprovider.dto.CancelCalendarEventRequest;
import org.openmrs.module.indiemroauthprovider.dto.CancelCalendarEventResponse;
import org.openmrs.module.indiemroauthprovider.dto.CreateCalendarEventRequest;
import org.openmrs.module.indiemroauthprovider.dto.CreateCalendarEventResponse;
import org.openmrs.module.indiemroauthprovider.dto.ResolveResult;
import org.openmrs.module.indiemroauthprovider.dto.UpdateCalendarEventRequest;

public interface TeleconsultService extends OpenmrsService {
	
	CreateCalendarEventResponse createCalendarEvent(Provider provider, CreateCalendarEventRequest request) throws Exception;
	
	CreateCalendarEventResponse updateCalendarEvent(Provider provider, UpdateCalendarEventRequest request) throws Exception;
	
	CancelCalendarEventResponse cancelCalendarEvent(Provider provider, CancelCalendarEventRequest request) throws Exception;
	
	ResolveResult resolveLink(String token);
}
