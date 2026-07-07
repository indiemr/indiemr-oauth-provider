package org.openmrs.module.indiemroauthprovider.dao;

import java.util.Date;

import org.openmrs.module.indiemroauthprovider.model.Meeting;

public interface MeetingDao {
	
	Meeting save(Meeting meeting);
	
	Meeting findByToken(String token);
	
	void markStatus(String token, String status);
	
	Meeting findActiveByExternalEventId(Integer externalEventId);
	
	void extendExpiryByExternalEventId(Integer externalEventId, Date newExpiresAt);
	
	void voidActiveByExternalEventId(Integer externalEventId, String reason);
}
