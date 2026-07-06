package org.openmrs.module.indiemroauthprovider.provider.dto;

public class OAuthUser {
	
	private final String externalAccountId;
	
	private final String email;
	
	private final String displayName;
	
	public OAuthUser(String externalAccountId, String email, String displayName) {
		this.externalAccountId = externalAccountId;
		this.email = email;
		this.displayName = displayName;
	}
	
	public String getExternalAccountId() {
		return externalAccountId;
	}
	
	public String getEmail() {
		return email;
	}
	
	public String getDisplayName() {
		return displayName;
	}
}
