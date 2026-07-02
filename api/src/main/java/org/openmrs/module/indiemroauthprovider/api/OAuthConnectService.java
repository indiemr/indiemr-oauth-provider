package org.openmrs.module.indiemroauthprovider.api;

import org.openmrs.Provider;
import org.openmrs.api.OpenmrsService;
import org.openmrs.module.indiemroauthprovider.dto.AccountStatusResponse;
import org.openmrs.module.indiemroauthprovider.dto.ConnectResult;

public interface OAuthConnectService extends OpenmrsService {
	
	String buildConnectUrl(Provider provider, String providerDisplay, String oauthProviderCode) throws Exception;
	
	ConnectResult handleCallback(String code, String state) throws Exception;
	
	AccountStatusResponse getAccountStatus(Provider provider, String oauthProviderCode);
}
