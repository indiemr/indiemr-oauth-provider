package org.openmrs.module.indiemroauthprovider.model;

public enum OAuthVendorCode {
	
	GOOGLE, MICROSOFT, ZOOM, WEBEX;
	
	public String getCode() {
		return name();
	}
	
	public static OAuthVendorCode fromCode(String code) {
		if (code == null || code.trim().isEmpty()) {
			throw new IllegalArgumentException("OAuth vendor code is required");
		}
		return valueOf(code.trim().toUpperCase());
	}
}
