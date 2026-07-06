package org.openmrs.module.indiemroauthprovider.dto;

public class ResolveResult {
	
	private final String meetUrl;
	
	private final String providerDisplay;
	
	public ResolveResult(String meetUrl, String providerDisplay) {
		this.meetUrl = meetUrl;
		this.providerDisplay = providerDisplay;
	}
	
	public String getMeetUrl() {
		return meetUrl;
	}
	
	public String getProviderDisplay() {
		return providerDisplay;
	}
}
