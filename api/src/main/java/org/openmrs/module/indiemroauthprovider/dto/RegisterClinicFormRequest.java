package org.openmrs.module.indiemroauthprovider.dto;

/**
 * Manual registry write — the escape hatch behind R9. If Google changes its page layout and the
 * prefill scrape stops working, an admin can still paste the form id and entry ids captured by hand
 * from "Get pre-filled link". Also the way a form is disabled (status = DISABLED).
 */
public class RegisterClinicFormRequest {
	
	private String formType;
	
	private String formId;
	
	private String prefillBase;
	
	private String entryToken;
	
	private String entryName;
	
	private String entryAge;
	
	private String referenceQuestionId;
	
	private String status;
	
	public String getFormType() {
		return formType;
	}
	
	public void setFormType(String formType) {
		this.formType = formType;
	}
	
	public String getFormId() {
		return formId;
	}
	
	public void setFormId(String formId) {
		this.formId = formId;
	}
	
	public String getPrefillBase() {
		return prefillBase;
	}
	
	public void setPrefillBase(String prefillBase) {
		this.prefillBase = prefillBase;
	}
	
	public String getEntryToken() {
		return entryToken;
	}
	
	public void setEntryToken(String entryToken) {
		this.entryToken = entryToken;
	}
	
	public String getEntryName() {
		return entryName;
	}
	
	public void setEntryName(String entryName) {
		this.entryName = entryName;
	}
	
	public String getEntryAge() {
		return entryAge;
	}
	
	public void setEntryAge(String entryAge) {
		this.entryAge = entryAge;
	}
	
	public String getReferenceQuestionId() {
		return referenceQuestionId;
	}
	
	public void setReferenceQuestionId(String referenceQuestionId) {
		this.referenceQuestionId = referenceQuestionId;
	}
	
	public String getStatus() {
		return status;
	}
	
	public void setStatus(String status) {
		this.status = status;
	}
}
