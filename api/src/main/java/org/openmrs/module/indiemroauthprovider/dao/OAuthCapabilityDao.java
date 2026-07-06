package org.openmrs.module.indiemroauthprovider.dao;

import java.util.List;

import org.openmrs.module.indiemroauthprovider.model.OAuthCapability;

public interface OAuthCapabilityDao {
	
	OAuthCapability findByCode(String code);
	
	List<OAuthCapability> findByCodes(List<String> codes);
}
