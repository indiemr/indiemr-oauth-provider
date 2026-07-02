package org.openmrs.module.indiemroauthprovider.api;

import org.openmrs.Provider;
import org.openmrs.api.OpenmrsService;
import org.openmrs.module.indiemroauthprovider.dto.CreateCalendarEventRequest;
import org.openmrs.module.indiemroauthprovider.dto.CreateCalendarEventResponse;
import org.openmrs.module.indiemroauthprovider.dto.MintLinkRequest;
import org.openmrs.module.indiemroauthprovider.dto.MintLinkResponse;
import org.openmrs.module.indiemroauthprovider.dto.ResolveResult;

public interface TeleconsultService extends OpenmrsService {
	
	MintLinkResponse mintLink(Provider provider, MintLinkRequest request) throws Exception;
	
	ResolveResult resolveLink(String token);
	
	CreateCalendarEventResponse createCalendarEvent(Provider provider, CreateCalendarEventRequest request) throws Exception;
}
