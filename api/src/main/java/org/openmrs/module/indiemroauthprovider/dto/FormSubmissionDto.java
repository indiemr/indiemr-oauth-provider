package org.openmrs.module.indiemroauthprovider.dto;

import java.util.Date;
import java.util.List;

public class FormSubmissionDto {
	
	private String responseId;
	
	private Date submittedAt;
	
	private List<FormAnswerDto> answers;
	
	public String getResponseId() {
		return responseId;
	}
	
	public void setResponseId(String responseId) {
		this.responseId = responseId;
	}
	
	public Date getSubmittedAt() {
		return submittedAt;
	}
	
	public void setSubmittedAt(Date submittedAt) {
		this.submittedAt = submittedAt;
	}
	
	public List<FormAnswerDto> getAnswers() {
		return answers;
	}
	
	public void setAnswers(List<FormAnswerDto> answers) {
		this.answers = answers;
	}
}
