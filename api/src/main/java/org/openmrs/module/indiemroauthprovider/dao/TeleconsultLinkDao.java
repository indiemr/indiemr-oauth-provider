package org.openmrs.module.indiemroauthprovider.dao;

import org.openmrs.module.indiemroauthprovider.model.TeleconsultLink;

public interface TeleconsultLinkDao {
	
	TeleconsultLink save(TeleconsultLink link);
	
	TeleconsultLink findByToken(String token);
	
	void markStatus(String token, String status);
	
	void voidByAppointmentUuid(String appointmentUuid);
}
