package org.openmrs.module.indiemroauthprovider.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Answer to "has this patient filled the form yet?" — serves both the dashboard auto-check and the
 * manual "Check now". {@code error} is how a poll failure reaches a human instead of being
 * swallowed (R6): the stored submissions still come back alongside it.
 */
public class FormResultsResponse {
	
	private String formType;
	
	private String patientUuid;
	
	private String formStatus;
	
	private boolean awaitingResponse;
	
	private boolean polled;
	
	private String error;
	
	private List<FormSubmissionDto> submissions = new ArrayList<FormSubmissionDto>();
	
	public String getFormType() {
		return formType;
	}
	
	public void setFormType(String formType) {
		this.formType = formType;
	}
	
	public String getPatientUuid() {
		return patientUuid;
	}
	
	public void setPatientUuid(String patientUuid) {
		this.patientUuid = patientUuid;
	}
	
	public String getFormStatus() {
		return formStatus;
	}
	
	public void setFormStatus(String formStatus) {
		this.formStatus = formStatus;
	}
	
	public boolean isAwaitingResponse() {
		return awaitingResponse;
	}
	
	public void setAwaitingResponse(boolean awaitingResponse) {
		this.awaitingResponse = awaitingResponse;
	}
	
	public boolean isPolled() {
		return polled;
	}
	
	public void setPolled(boolean polled) {
		this.polled = polled;
	}
	
	public String getError() {
		return error;
	}
	
	public void setError(String error) {
		this.error = error;
	}
	
	public List<FormSubmissionDto> getSubmissions() {
		return submissions;
	}
	
	public void setSubmissions(List<FormSubmissionDto> submissions) {
		this.submissions = submissions;
	}
}
