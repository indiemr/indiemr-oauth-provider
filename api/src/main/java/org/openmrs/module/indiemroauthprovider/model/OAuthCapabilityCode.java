package org.openmrs.module.indiemroauthprovider.model;

public enum OAuthCapabilityCode {
	
	CALENDAR, VIDEO_MEETING, EMAIL, CONTACTS;
	
	public String getCode() {
		return name();
	}
	
	public static OAuthCapabilityCode fromCode(String code) {
		return valueOf(code.trim().toUpperCase());
	}
}
