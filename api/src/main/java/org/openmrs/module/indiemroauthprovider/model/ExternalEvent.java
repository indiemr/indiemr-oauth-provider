package org.openmrs.module.indiemroauthprovider.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.openmrs.BaseChangeableOpenmrsData;

@Entity
@Table(name = "indiemr_external_event")
public class ExternalEvent extends BaseChangeableOpenmrsData {
	
	public static final String STATUS_ACTIVE = "active";
	
	public static final String STATUS_CANCELLED = "cancelled";
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "oauth_account_id", nullable = false)
	private OAuthAccount oauthAccount;
	
	@Column(name = "external_resource_type", nullable = false, length = 64)
	private String externalResourceType;
	
	@Column(name = "external_event_id", nullable = false, length = 512)
	private String externalEventId;
	
	@Column(nullable = false, length = 20)
	private String status = STATUS_ACTIVE;
	
	@Column(name = "metadata_json", columnDefinition = "TEXT")
	private String metadataJson;
	
	public Integer getId() {
		return id;
	}
	
	public void setId(Integer id) {
		this.id = id;
	}
	
	public OAuthAccount getOauthAccount() {
		return oauthAccount;
	}
	
	public void setOauthAccount(OAuthAccount oauthAccount) {
		this.oauthAccount = oauthAccount;
	}
	
	public String getExternalResourceType() {
		return externalResourceType;
	}
	
	public void setExternalResourceType(String externalResourceType) {
		this.externalResourceType = externalResourceType;
	}
	
	public String getExternalEventId() {
		return externalEventId;
	}
	
	public void setExternalEventId(String externalEventId) {
		this.externalEventId = externalEventId;
	}
	
	public String getStatus() {
		return status;
	}
	
	public void setStatus(String status) {
		this.status = status;
	}
	
	public String getMetadataJson() {
		return metadataJson;
	}
	
	public void setMetadataJson(String metadataJson) {
		this.metadataJson = metadataJson;
	}
}
