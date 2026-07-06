package org.openmrs.module.indiemroauthprovider.api;

import org.openmrs.Provider;
import org.openmrs.api.OpenmrsService;
import org.openmrs.module.indiemroauthprovider.dto.AccountStatusResponse;
import org.openmrs.module.indiemroauthprovider.dto.ConnectResult;
import org.openmrs.module.indiemroauthprovider.model.OAuthVendorCode;

public interface OAuthConnectService extends OpenmrsService {
	
	String buildConnectUrl(Provider provider, String providerDisplay, OAuthVendorCode oauthVendor) throws Exception;
	
	ConnectResult handleCallback(String code, String state) throws Exception;
	
	AccountStatusResponse getAccountStatus(Provider provider, OAuthVendorCode oauthVendor);
}
