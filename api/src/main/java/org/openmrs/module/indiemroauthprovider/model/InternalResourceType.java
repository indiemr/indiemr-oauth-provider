package org.openmrs.module.indiemroauthprovider.model;

public enum InternalResourceType {
	
	PATIENT("PATIENT"),
	
	APPOINTMENT("APPOINTMENT");
	
	private final String code;
	
	InternalResourceType(String code) {
		this.code = code;
	}
	
	public String getCode() {
		return code;
	}
	
	public static InternalResourceType fromCode(String code) {
		for (InternalResourceType type : values()) {
			if (type.code.equals(code)) {
				return type;
			}
		}
		throw new IllegalArgumentException("Unknown internal resource type: " + code);
	}
}
