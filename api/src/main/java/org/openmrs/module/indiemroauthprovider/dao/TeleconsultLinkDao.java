package org.openmrs.module.indiemroauthprovider.dao;

import java.util.Date;

import org.openmrs.module.indiemroauthprovider.model.TeleconsultLink;

public interface TeleconsultLinkDao {
	
	TeleconsultLink save(TeleconsultLink link);
	
	TeleconsultLink findByToken(String token);
	
	void markStatus(String token, String status);
	
	void voidByAppointmentUuid(String appointmentUuid);
	
	void voidByInternalResource(String resourceType, String resourceUuid);
	
	void extendLinkExpiryForResource(String resourceType, String resourceUuid, Date newExpiresAt);
}
