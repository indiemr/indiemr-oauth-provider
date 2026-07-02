package org.openmrs.module.indiemroauthprovider.util;

import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.indiemroauthprovider.ModuleConstants;
import org.springframework.stereotype.Component;

@Component("indiemroauthprovider.ModuleConfig")
public class ModuleConfig {
	
	public String getPublicBaseUrl() {
		return getGlobalProperty(ModuleConstants.GP_PUBLIC_BASE_URL);
	}
	
	public String getEncKey() {
		return getGlobalProperty(ModuleConstants.GP_ENC_KEY);
	}
	
	public String getGoogleClientId() {
		return getGlobalProperty(ModuleConstants.GP_GOOGLE_CLIENT_ID);
	}
	
	public String getGoogleClientSecret() {
		return getGlobalProperty(ModuleConstants.GP_GOOGLE_CLIENT_SECRET);
	}
	
	public String getGoogleRedirectUri() {
		return getGlobalProperty(ModuleConstants.GP_GOOGLE_REDIRECT_URI);
	}
	
	private String getGlobalProperty(String property) {
		AdministrationService adminService = Context.getAdministrationService();
		return adminService.getGlobalProperty(property);
	}
}
