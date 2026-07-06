package org.openmrs.module.indiemroauthprovider.dao;

import java.util.Date;

import org.openmrs.module.indiemroauthprovider.model.TeleconsultLink;

public interface TeleconsultLinkDao {
	
	TeleconsultLink save(TeleconsultLink link);
	
	TeleconsultLink findByToken(String token);
	
	void markStatus(String token, String status);
	
	TeleconsultLink findActiveByExternalResourceMappingId(Long externalResourceMappingId);
	
	void extendExpiryByExternalResourceMappingId(Long externalResourceMappingId, Date newExpiresAt);
	
	void voidActiveByExternalResourceMappingId(Long externalResourceMappingId);
}
