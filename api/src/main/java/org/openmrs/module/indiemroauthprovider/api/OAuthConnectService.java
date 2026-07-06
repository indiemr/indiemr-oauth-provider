package org.openmrs.module.indiemroauthprovider.api;

import org.openmrs.Provider;
import org.openmrs.annotation.Authorized;
import org.openmrs.api.OpenmrsService;
import org.openmrs.module.indiemroauthprovider.PrivilegeConstants;
import org.openmrs.module.indiemroauthprovider.dto.AccountStatusResponse;
import org.openmrs.module.indiemroauthprovider.dto.ConnectResult;
import org.openmrs.module.indiemroauthprovider.model.OAuthVendorCode;
import org.springframework.transaction.annotation.Transactional;

public interface OAuthConnectService extends OpenmrsService {
	
	@Transactional
	@Authorized({ PrivilegeConstants.MANAGE_OAUTH_ACCOUNT })
	String buildConnectUrl(Provider provider, OAuthVendorCode oauthVendor) throws Exception;
	
	@Transactional
	ConnectResult handleCallback(String code, String state) throws Exception;
	
	@Transactional(readOnly = true)
	@Authorized({ PrivilegeConstants.VIEW_OAUTH_ACCOUNT, PrivilegeConstants.MANAGE_OAUTH_ACCOUNT })
	AccountStatusResponse getAccountStatus(Provider provider, OAuthVendorCode oauthVendor);
}
