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

@Entity
@Table(name = "indiemr_external_resource_mapping")
public class ExternalResourceMapping {
	
	public static final String INTERNAL_APPOINTMENT = "APPOINTMENT";
	
	public static final String EXTERNAL_CALENDAR_EVENT = "CALENDAR_EVENT";
	
	public static final String EXTERNAL_VIDEO_MEETING = "VIDEO_MEETING";
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "oauth_account_id", nullable = false)
	private OAuthAccount oauthAccount;
	
	@Column(name = "internal_resource_type", nullable = false, length = 64)
	private String internalResourceType;
	
	@Column(name = "internal_resource_uuid", nullable = false, length = 64)
	private String internalResourceUuid;
	
	@Column(name = "external_resource_type", nullable = false, length = 64)
	private String externalResourceType;
	
	@Column(name = "external_resource_id", nullable = false, length = 512)
	private String externalResourceId;
	
	@Column(name = "metadata_json", columnDefinition = "TEXT")
	private String metadataJson;
	
	@Column(nullable = false)
	private boolean voided = false;
	
	@Column(name = "created_at", nullable = false)
	private Date createdAt;
	
	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	public OAuthAccount getOauthAccount() {
		return oauthAccount;
	}
	
	public void setOauthAccount(OAuthAccount oauthAccount) {
		this.oauthAccount = oauthAccount;
	}
	
	public String getInternalResourceType() {
		return internalResourceType;
	}
	
	public void setInternalResourceType(String internalResourceType) {
		this.internalResourceType = internalResourceType;
	}
	
	public String getInternalResourceUuid() {
		return internalResourceUuid;
	}
	
	public void setInternalResourceUuid(String internalResourceUuid) {
		this.internalResourceUuid = internalResourceUuid;
	}
	
	public String getExternalResourceType() {
		return externalResourceType;
	}
	
	public void setExternalResourceType(String externalResourceType) {
		this.externalResourceType = externalResourceType;
	}
	
	public String getExternalResourceId() {
		return externalResourceId;
	}
	
	public void setExternalResourceId(String externalResourceId) {
		this.externalResourceId = externalResourceId;
	}
	
	public String getMetadataJson() {
		return metadataJson;
	}
	
	public void setMetadataJson(String metadataJson) {
		this.metadataJson = metadataJson;
	}
	
	public boolean isVoided() {
		return voided;
	}
	
	public void setVoided(boolean voided) {
		this.voided = voided;
	}
	
	public Date getCreatedAt() {
		return createdAt;
	}
	
	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}
}
