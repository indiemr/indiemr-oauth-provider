package org.openmrs.module.indiemroauthprovider.provider.google;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.openmrs.module.indiemroauthprovider.model.OAuthAccount;
import org.openmrs.module.indiemroauthprovider.provider.OAuthProviderAdapter;
import org.openmrs.module.indiemroauthprovider.provider.dto.OAuthToken;
import org.openmrs.module.indiemroauthprovider.provider.dto.OAuthUser;
import org.openmrs.module.indiemroauthprovider.model.OAuthVendorCode;
import org.openmrs.module.indiemroauthprovider.util.ModuleConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.Base64;

@Component("indiemroauthprovider.GoogleOAuthProviderAdapter")
public class GoogleOAuthProviderAdapter implements OAuthProviderAdapter {
	
	public static final String CODE = OAuthVendorCode.GOOGLE.getCode();
	
	private static final List<String> SCOPES = Arrays.asList("openid", "email",
	    "https://www.googleapis.com/auth/calendar.events");
	
	@Autowired
	@Qualifier("indiemroauthprovider.ModuleConfig")
	private ModuleConfig moduleConfig;
	
	@Override
	public String getProviderCode() {
		return CODE;
	}
	
	@Override
	public String buildAuthorizationUrl(String state) throws Exception {
		return flow().newAuthorizationUrl().setRedirectUri(moduleConfig.getGoogleRedirectUri()).setState(state).build();
	}
	
	@Override
	public OAuthToken exchangeAuthorizationCode(String code) throws Exception {
		TokenResponse response = flow().newTokenRequest(code).setRedirectUri(moduleConfig.getGoogleRedirectUri()).execute();
		if (response.getRefreshToken() == null) {
			throw new IllegalStateException("Google did not return a refresh token — revoke prior access and reconnect.");
		}
		Date expires = response.getExpiresInSeconds() != null ? new Date(System.currentTimeMillis()
		        + response.getExpiresInSeconds() * 1000L) : null;
		String idToken = response.get("id_token") != null ? String.valueOf(response.get("id_token")) : null;
		return new OAuthToken(response.getAccessToken(), response.getRefreshToken(), idToken, response.getScope(), expires);
	}
	
	@Override
	public OAuthUser getCurrentUser(OAuthAccount account, String decryptedAccessToken) {
		return new OAuthUser(account.getExternalAccountId(), account.getExternalEmail(), account.getDisplayName());
	}
	
	@Override
	public void revoke(OAuthAccount account, String decryptedRefreshToken) {
		// optional: call https://oauth2.googleapis.com/revoke
	}
	
	private GoogleAuthorizationCodeFlow flow() throws Exception {
		GoogleClientSecrets.Details web = new GoogleClientSecrets.Details();
		web.setClientId(moduleConfig.getGoogleClientId());
		web.setClientSecret(moduleConfig.getGoogleClientSecret());
		return new GoogleAuthorizationCodeFlow.Builder(GoogleNetHttpTransport.newTrustedTransport(),
		        GsonFactory.getDefaultInstance(), new GoogleClientSecrets().setWeb(web), SCOPES).setAccessType("offline")
		        .setApprovalPrompt("force").build();
	}
	
	public static String emailFromIdToken(String idToken) {
		try {
			String payload = idToken.split("\\.")[1];
			String json = new String(Base64.decodeBase64(payload), StandardCharsets.UTF_8);
			int idx = json.indexOf("\"email\"");
			if (idx < 0) {
				return null;
			}
			int start = json.indexOf('"', idx + 8) + 1;
			int end = json.indexOf('"', start);
			return json.substring(start, end);
		}
		catch (Exception e) {
			return null;
		}
	}
}
