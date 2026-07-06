package org.openmrs.module.indiemroauthprovider.util;

import org.springframework.stereotype.Component;

/**
 * Temporary hardcoded config for local testing. Replace with {@link ModuleConfigLoader} (env /
 * application.yml) before production deploy.
 */
@Component("indiemroauthprovider.ModuleConfig")
public class ModuleConfig {
	
	private static final String PUBLIC_BASE_URL = "https://localhost";
	
	private static final String ENC_KEY = "908698183a7bd9a855cd7fa308b85e776387f447dda348cf8c9cb9ac83911e74";
	
	private static final String GOOGLE_CLIENT_ID = "711590727381-8phvuojcelets9lt0oml339j959nchph.apps.googleusercontent.com";
	
	private static final String GOOGLE_CLIENT_SECRET = "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";
	
	private static final String GOOGLE_REDIRECT_URI = "https://localhost/openmrs/ws/rest/v1/oauth/connect/callback";
	
	public String getPublicBaseUrl() {
		return PUBLIC_BASE_URL;
	}
	
	public String getEncKey() {
		return ENC_KEY;
	}
	
	public String getGoogleClientId() {
		return GOOGLE_CLIENT_ID;
	}
	
	public String getGoogleClientSecret() {
		return GOOGLE_CLIENT_SECRET;
	}
	
	public String getGoogleRedirectUri() {
		return GOOGLE_REDIRECT_URI;
	}
}
