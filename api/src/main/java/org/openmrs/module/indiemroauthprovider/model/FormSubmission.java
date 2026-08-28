package org.openmrs.module.indiemroauthprovider.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * A response pulled back from Google. A null formShare is the unresolved bucket — the reference
 * token was missing or mangled, so the response is kept but attributed to nobody.
 */
@Entity
@Table(name = "indiemr_form_submission")
public class FormSubmission {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "clinic_form_id", nullable = false)
	private ClinicForm clinicForm;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "form_share_id")
	private FormShare formShare;
	
	@Column(name = "google_response_id", nullable = false, unique = true, length = 191)
	private String googleResponseId;
	
	@Column(name = "answers_json", columnDefinition = "TEXT")
	private String answersJson;
	
	@Column(name = "submitted_at")
	private Date submittedAt;
	
	@Column(name = "fetched_at", nullable = false)
	private Date fetchedAt;
	
	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	public ClinicForm getClinicForm() {
		return clinicForm;
	}
	
	public void setClinicForm(ClinicForm clinicForm) {
		this.clinicForm = clinicForm;
	}
	
	public FormShare getFormShare() {
		return formShare;
	}
	
	public void setFormShare(FormShare formShare) {
		this.formShare = formShare;
	}
	
	public String getGoogleResponseId() {
		return googleResponseId;
	}
	
	public void setGoogleResponseId(String googleResponseId) {
		this.googleResponseId = googleResponseId;
	}
	
	public String getAnswersJson() {
		return answersJson;
	}
	
	public void setAnswersJson(String answersJson) {
		this.answersJson = answersJson;
	}
	
	public Date getSubmittedAt() {
		return submittedAt;
	}
	
	public void setSubmittedAt(Date submittedAt) {
		this.submittedAt = submittedAt;
	}
	
	public Date getFetchedAt() {
		return fetchedAt;
	}
	
	public void setFetchedAt(Date fetchedAt) {
		this.fetchedAt = fetchedAt;
	}
}
