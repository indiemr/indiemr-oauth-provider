package org.openmrs.module.indiemroauthprovider.dto;

public class MintLinkRequest {
	
	private String patientName;
	
	private String patientPhone;
	
	private String oauthProviderCode = "GOOGLE";
	
	private String appointmentUuid;
	
	public String getOauthProviderCode() {
		return oauthProviderCode;
	}
	
	public void setOauthProviderCode(String oauthProviderCode) {
		this.oauthProviderCode = oauthProviderCode;
	}
	
	public String getAppointmentUuid() {
		return appointmentUuid;
	}
	
	public void setAppointmentUuid(String appointmentUuid) {
		this.appointmentUuid = appointmentUuid;
	}
	
	public String getPatientName() {
		return patientName;
	}
	
	public void setPatientName(String patientName) {
		this.patientName = patientName;
	}
	
	public String getPatientPhone() {
		return patientPhone;
	}
	
	public void setPatientPhone(String patientPhone) {
		this.patientPhone = patientPhone;
	}
}
