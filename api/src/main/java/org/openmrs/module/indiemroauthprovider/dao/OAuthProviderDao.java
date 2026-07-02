package org.openmrs.module.indiemroauthprovider.dao;

import org.openmrs.module.indiemroauthprovider.model.OAuthProvider;

public interface OAuthProviderDao {
	
	OAuthProvider findEnabledByCode(String code);
	
	OAuthProvider findByCode(String code);
}
