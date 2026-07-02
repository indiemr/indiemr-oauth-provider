package org.openmrs.module.indiemroauthprovider.provider.dto;

import java.util.Date;

public class OAuthToken {
	
	private final String accessToken;
	
	private final String refreshToken;
	
	private final String idToken;
	
	private final String scope;
	
	private final Date expiresAt;
	
	public OAuthToken(String accessToken, String refreshToken, String idToken, String scope, Date expiresAt) {
		this.accessToken = accessToken;
		this.refreshToken = refreshToken;
		this.idToken = idToken;
		this.scope = scope;
		this.expiresAt = expiresAt;
	}
	
	public String getAccessToken() {
		return accessToken;
	}
	
	public String getRefreshToken() {
		return refreshToken;
	}
	
	public String getIdToken() {
		return idToken;
	}
	
	public String getScope() {
		return scope;
	}
	
	public Date getExpiresAt() {
		return expiresAt;
	}
}
