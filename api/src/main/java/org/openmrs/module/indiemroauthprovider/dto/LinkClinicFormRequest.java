package org.openmrs.module.indiemroauthprovider.dto;

/**
 * Result of the Google Picker, posted by the wizard. {@code accessToken} is the clinic's one-time
 * grant: used for a single Drive call and then dropped — never stored, never logged (R8). It is
 * optional so a link that failed after the permission step can be resumed without re-picking (R7).
 */
public class LinkClinicFormRequest {
	
	private String formType;
	
	private String formId;
	
	private String accessToken;
	
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
	
	public String getAccessToken() {
		return accessToken;
	}
	
	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}
	
	/** Never let the grant reach a log line or an error message. */
	@Override
	public String toString() {
		return "LinkClinicFormRequest{formType=" + formType + ", formId=" + formId + ", accessToken=***}";
	}
}
