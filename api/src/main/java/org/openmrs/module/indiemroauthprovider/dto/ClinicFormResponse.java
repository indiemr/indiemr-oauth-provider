package org.openmrs.module.indiemroauthprovider.dto;

import java.util.Date;

import org.openmrs.module.indiemroauthprovider.model.ClinicForm;

/** Registry row as the Integrations page sees it. Carries no token of any kind. */
public class ClinicFormResponse {
	
	private String formType;
	
	private String formId;
	
	private String status;
	
	private String ownerEmail;
	
	private boolean prefillReady;
	
	private String lastError;
	
	private Date lastPolledAt;
	
	public static ClinicFormResponse from(ClinicForm form) {
		ClinicFormResponse response = new ClinicFormResponse();
		response.formType = form.getFormType();
		response.formId = form.getGoogleFormId();
		response.status = form.getStatus();
		response.ownerEmail = form.getOwnerEmail();
		response.prefillReady = form.getPrefillBase() != null && form.getEntryToken() != null;
		response.lastError = form.getLastError();
		response.lastPolledAt = form.getLastPolledAt();
		return response;
	}
	
	public String getFormType() {
		return formType;
	}
	
	public String getFormId() {
		return formId;
	}
	
	public String getStatus() {
		return status;
	}
	
	public String getOwnerEmail() {
		return ownerEmail;
	}
	
	public boolean isPrefillReady() {
		return prefillReady;
	}
	
	public String getLastError() {
		return lastError;
	}
	
	public Date getLastPolledAt() {
		return lastPolledAt;
	}
}
