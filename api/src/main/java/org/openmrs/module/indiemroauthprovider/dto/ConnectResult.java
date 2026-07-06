package org.openmrs.module.indiemroauthprovider.dto;

public class ConnectResult {
	
	private final String providerUuid;
	
	private final String oauthProviderCode;
	
	private final String email;
	
	public ConnectResult(String providerUuid, String oauthProviderCode, String email) {
		this.providerUuid = providerUuid;
		this.oauthProviderCode = oauthProviderCode;
		this.email = email;
	}
	
	public String getProviderUuid() {
		return providerUuid;
	}
	
	public String getOauthProviderCode() {
		return oauthProviderCode;
	}
	
	public String getEmail() {
		return email;
	}
}
