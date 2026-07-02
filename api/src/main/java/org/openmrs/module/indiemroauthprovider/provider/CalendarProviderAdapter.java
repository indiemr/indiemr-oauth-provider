package org.openmrs.module.indiemroauthprovider.provider;

import org.openmrs.module.indiemroauthprovider.model.OAuthAccount;
import org.openmrs.module.indiemroauthprovider.provider.dto.CalendarEventRequest;
import org.openmrs.module.indiemroauthprovider.provider.dto.CalendarEventResult;

public interface CalendarProviderAdapter {
	
	String getProviderCode();
	
	CalendarEventResult createEvent(OAuthAccount account, String decryptedRefreshToken, CalendarEventRequest request)
	        throws Exception;
	
	void deleteEvent(OAuthAccount account, String decryptedRefreshToken, String externalEventId) throws Exception;
}
