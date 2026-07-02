package org.openmrs.module.indiemroauthprovider.dto;

public class AccountStatusResponse {
	
	private String status;
	
	private String oauthProvider;
	
	private String email;
	
	private String scope;
	
	public AccountStatusResponse() {
	}
	
	public AccountStatusResponse(String status, String oauthProvider, String email, String scope) {
		this.status = status;
		this.oauthProvider = oauthProvider;
		this.email = email;
		this.scope = scope;
	}
	
	public String getStatus() {
		return status;
	}
	
	public void setStatus(String status) {
		this.status = status;
	}
	
	public String getOauthProvider() {
		return oauthProvider;
	}
	
	public void setOauthProvider(String oauthProvider) {
		this.oauthProvider = oauthProvider;
	}
	
	public String getEmail() {
		return email;
	}
	
	public void setEmail(String email) {
		this.email = email;
	}
	
	public String getScope() {
		return scope;
	}
	
	public void setScope(String scope) {
		this.scope = scope;
	}
}
