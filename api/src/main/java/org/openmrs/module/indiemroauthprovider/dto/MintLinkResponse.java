package org.openmrs.module.indiemroauthprovider.dto;

public class MintLinkResponse {
	
	private String token;
	
	private String meetUrl;
	
	private String resolverUrl;
	
	public MintLinkResponse() {
	}
	
	public MintLinkResponse(String token, String meetUrl, String resolverUrl) {
		this.token = token;
		this.meetUrl = meetUrl;
		this.resolverUrl = resolverUrl;
	}
	
	public String getToken() {
		return token;
	}
	
	public void setToken(String token) {
		this.token = token;
	}
	
	public String getMeetUrl() {
		return meetUrl;
	}
	
	public void setMeetUrl(String meetUrl) {
		this.meetUrl = meetUrl;
	}
	
	public String getResolverUrl() {
		return resolverUrl;
	}
	
	public void setResolverUrl(String resolverUrl) {
		this.resolverUrl = resolverUrl;
	}
}
