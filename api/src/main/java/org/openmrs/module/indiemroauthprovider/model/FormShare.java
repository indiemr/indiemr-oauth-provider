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

import org.openmrs.User;

/**
 * One row per "Share" click: the opaque token that rides the prefilled link and comes back in the
 * patient's answers. Anchored on the authenticated User, never on a Provider (R2).
 */
@Entity
@Table(name = "indiemr_form_share")
public class FormShare {
	
	public static final String STATUS_SHARED = "SHARED";
	
	public static final String STATUS_SUBMITTED = "SUBMITTED";
	
	/** The linked form was replaced — this token can never be attributed again (R10). */
	public static final String STATUS_CLOSED = "CLOSED";
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "clinic_form_id", nullable = false)
	private ClinicForm clinicForm;
	
	@Column(name = "patient_uuid", nullable = false, length = 38)
	private String patientUuid;
	
	@Column(nullable = false, unique = true, length = 64)
	private String token;
	
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "shared_by", nullable = false)
	private User sharedBy;
	
	@Column(nullable = false, length = 20)
	private String status = STATUS_SHARED;
	
	@Column(name = "created_at", nullable = false)
	private Date createdAt;
	
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
	
	public String getPatientUuid() {
		return patientUuid;
	}
	
	public void setPatientUuid(String patientUuid) {
		this.patientUuid = patientUuid;
	}
	
	public String getToken() {
		return token;
	}
	
	public void setToken(String token) {
		this.token = token;
	}
	
	public User getSharedBy() {
		return sharedBy;
	}
	
	public void setSharedBy(User sharedBy) {
		this.sharedBy = sharedBy;
	}
	
	public String getStatus() {
		return status;
	}
	
	public void setStatus(String status) {
		this.status = status;
	}
	
	public Date getCreatedAt() {
		return createdAt;
	}
	
	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}
}
