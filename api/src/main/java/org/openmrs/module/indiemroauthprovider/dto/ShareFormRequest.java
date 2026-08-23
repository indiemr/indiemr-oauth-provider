package org.openmrs.module.indiemroauthprovider.dto;

public class ShareFormRequest {
	
	private String patientUuid;
	
	private String formType;
	
	public String getPatientUuid() {
		return patientUuid;
	}
	
	public void setPatientUuid(String patientUuid) {
		this.patientUuid = patientUuid;
	}
	
	public String getFormType() {
		return formType;
	}
	
	public void setFormType(String formType) {
		this.formType = formType;
	}
}
