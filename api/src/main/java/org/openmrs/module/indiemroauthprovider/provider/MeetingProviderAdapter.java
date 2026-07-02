package org.openmrs.module.indiemroauthprovider.provider;

import org.openmrs.module.indiemroauthprovider.model.OAuthAccount;
import org.openmrs.module.indiemroauthprovider.provider.dto.MeetingRequest;
import org.openmrs.module.indiemroauthprovider.provider.dto.MeetingResult;

public interface MeetingProviderAdapter {
	
	String getProviderCode();
	
	MeetingResult createMeeting(OAuthAccount account, String decryptedRefreshToken, MeetingRequest request) throws Exception;
	
	void deleteMeeting(OAuthAccount account, String decryptedRefreshToken, String meetingId) throws Exception;
}
