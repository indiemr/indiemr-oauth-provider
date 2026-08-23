package org.openmrs.module.indiemroauthprovider.model;

/**
 * Form types a clinic can link. INTAKE is the only one v1 ships; the rest are the dropdown the
 * design reserves — added here so an unknown client value is rejected instead of stored.
 */
public enum ClinicFormType {
	
	INTAKE;
	
	public static ClinicFormType fromCode(String code) {
		if (code == null) {
			throw new IllegalArgumentException("formType is required");
		}
		for (ClinicFormType type : values()) {
			if (type.name().equalsIgnoreCase(code.trim())) {
				return type;
			}
		}
		throw new IllegalArgumentException("Unknown formType: " + code);
	}
}
