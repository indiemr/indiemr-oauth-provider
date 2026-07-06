package org.openmrs.module.indiemroauthprovider.provider;

import org.openmrs.module.indiemroauthprovider.model.OAuthAccount;
import org.openmrs.module.indiemroauthprovider.provider.dto.OAuthToken;
import org.openmrs.module.indiemroauthprovider.provider.dto.OAuthUser;

public interface OAuthProviderAdapter {
	
	String getProviderCode();
	
	String buildAuthorizationUrl(String state) throws Exception;
	
	OAuthToken exchangeAuthorizationCode(String code) throws Exception;
	
	OAuthUser getCurrentUser(OAuthAccount account, String decryptedAccessToken) throws Exception;
	
	void revoke(OAuthAccount account, String decryptedRefreshToken) throws Exception;
}
