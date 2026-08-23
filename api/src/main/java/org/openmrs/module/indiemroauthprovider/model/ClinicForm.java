package org.openmrs.module.indiemroauthprovider.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Registry row: one linked Google Form per workspace root per form type. Mutable config — a removal
 * flips the status to DISABLED and a re-registration updates the same row, so the (location_uuid,
 * form_type) unique key can stay hard.
 */
@Entity
@Table(name = "indiemr_clinic_form")
public class ClinicForm {
	
	public static final String STATUS_PENDING_SHARE = "PENDING_SHARE";
	
	public static final String STATUS_ACTIVE = "ACTIVE";
	
	public static final String STATUS_BROKEN = "BROKEN";
	
	public static final String STATUS_DISABLED = "DISABLED";
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name = "location_uuid", nullable = false, length = 38)
	private String locationUuid;
	
	@Column(name = "form_type", nullable = false, length = 32)
	private String formType;
	
	@Column(name = "google_form_id", nullable = false, length = 191)
	private String googleFormId;
	
	/** Mirror of googleFormId while the row is usable, NULL once DISABLED — see the unique index. */
	@Column(name = "active_form_id", length = 191)
	private String activeFormId;
	
	@Column(name = "prefill_base", length = 2048)
	private String prefillBase;
	
	@Column(name = "entry_token", length = 64)
	private String entryToken;
	
	@Column(name = "entry_name", length = 64)
	private String entryName;
	
	@Column(name = "entry_age", length = 64)
	private String entryAge;
	
	/** Google questionId of the reference-code field. Idempotency key — never the title. */
	@Column(name = "reference_question_id", length = 64)
	private String referenceQuestionId;
	
	@Column(name = "owner_email")
	private String ownerEmail;
	
	@Column(nullable = false, length = 32)
	private String status = STATUS_PENDING_SHARE;
	
	@Column(name = "last_error", length = 1024)
	private String lastError;
	
	@Column(name = "last_polled_at")
	private Date lastPolledAt;
	
	@Column(name = "created_by")
	private Integer createdBy;
	
	@Column(name = "created_at", nullable = false)
	private Date createdAt;
	
	@Column(name = "updated_by")
	private Integer updatedBy;
	
	@Column(name = "updated_at", nullable = false)
	private Date updatedAt;
	
	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	public String getLocationUuid() {
		return locationUuid;
	}
	
	public void setLocationUuid(String locationUuid) {
		this.locationUuid = locationUuid;
	}
	
	public String getFormType() {
		return formType;
	}
	
	public void setFormType(String formType) {
		this.formType = formType;
	}
	
	public String getGoogleFormId() {
		return googleFormId;
	}
	
	public void setGoogleFormId(String googleFormId) {
		this.googleFormId = googleFormId;
	}
	
	public String getActiveFormId() {
		return activeFormId;
	}
	
	public void setActiveFormId(String activeFormId) {
		this.activeFormId = activeFormId;
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
	
	public String getOwnerEmail() {
		return ownerEmail;
	}
	
	public void setOwnerEmail(String ownerEmail) {
		this.ownerEmail = ownerEmail;
	}
	
	public String getStatus() {
		return status;
	}
	
	public void setStatus(String status) {
		this.status = status;
	}
	
	public String getLastError() {
		return lastError;
	}
	
	public void setLastError(String lastError) {
		this.lastError = lastError;
	}
	
	public Date getLastPolledAt() {
		return lastPolledAt;
	}
	
	public void setLastPolledAt(Date lastPolledAt) {
		this.lastPolledAt = lastPolledAt;
	}
	
	public Integer getCreatedBy() {
		return createdBy;
	}
	
	public void setCreatedBy(Integer createdBy) {
		this.createdBy = createdBy;
	}
	
	public Date getCreatedAt() {
		return createdAt;
	}
	
	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}
	
	public Integer getUpdatedBy() {
		return updatedBy;
	}
	
	public void setUpdatedBy(Integer updatedBy) {
		this.updatedBy = updatedBy;
	}
	
	public Date getUpdatedAt() {
		return updatedAt;
	}
	
	public void setUpdatedAt(Date updatedAt) {
		this.updatedAt = updatedAt;
	}
}
