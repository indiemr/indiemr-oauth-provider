package org.openmrs.module.indiemroauthprovider.model;

public enum ExternalResourceType {
	
	CALENDAR_EVENT("CALENDAR_EVENT"), VIDEO_MEETING("VIDEO_MEETING");
	
	private final String code;
	
	ExternalResourceType(String code) {
		this.code = code;
	}
	
	public String getCode() {
		return code;
	}
}
