package org.openmrs.module.indiemroauthprovider.dto;

import java.util.Date;

/** What staff needs to send the patient. The token is inside the url — that is its only home. */
public class ShareFormResponse {
	
	private final String formType;
	
	private final String patientUuid;
	
	private final String token;
	
	private final String url;
	
	private final Date sharedAt;
	
	public ShareFormResponse(String formType, String patientUuid, String token, String url, Date sharedAt) {
		this.formType = formType;
		this.patientUuid = patientUuid;
		this.token = token;
		this.url = url;
		this.sharedAt = sharedAt;
	}
	
	public String getFormType() {
		return formType;
	}
	
	public String getPatientUuid() {
		return patientUuid;
	}
	
	public String getToken() {
		return token;
	}
	
	public String getUrl() {
		return url;
	}
	
	public Date getSharedAt() {
		return sharedAt;
	}
}
