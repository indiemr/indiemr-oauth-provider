package org.openmrs.module.indiemroauthprovider.model;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.openmrs.Provider;

@Entity
@Table(name = "indiemr_oauth_account")
public class OAuthAccount {
	
	public static final String STATUS_ACTIVE = "ACTIVE";
	
	public static final String STATUS_REVOKED = "REVOKED";
	
	public static final String STATUS_EXPIRED = "EXPIRED";
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "provider_uuid", referencedColumnName = "uuid", nullable = false)
	private Provider provider;
	
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "oauth_provider_id", nullable = false)
	private OAuthProvider oauthProvider;
	
	@Column(name = "external_account_id")
	private String externalAccountId;
	
	@Column(name = "external_email")
	private String externalEmail;
	
	@Column(name = "display_name")
	private String displayName;
	
	@Column(name = "access_token_enc", columnDefinition = "TEXT")
	private String accessTokenEnc;
	
	@Column(name = "refresh_token_enc", columnDefinition = "TEXT")
	private String refreshTokenEnc;
	
	@Column(name = "id_token_enc", columnDefinition = "TEXT")
	private String idTokenEnc;
	
	private String scope;
	
	@Column(name = "expires_at")
	private Date expiresAt;
	
	@Column(nullable = false, length = 32)
	private String status = STATUS_ACTIVE;
	
	@Column(name = "metadata_json", columnDefinition = "TEXT")
	private String metadataJson;
	
	@Column(nullable = false)
	private boolean voided = false;
	
	@Column(name = "created_at", nullable = false)
	private Date createdAt;
	
	@Column(name = "updated_at", nullable = false)
	private Date updatedAt;
	
	@OneToMany(mappedBy = "oauthAccount", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<OAuthAccountCapability> capabilities = new HashSet<OAuthAccountCapability>();
	
	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	public Provider getProvider() {
		return provider;
	}
	
	public void setProvider(Provider provider) {
		this.provider = provider;
	}
	
	public OAuthProvider getOauthProvider() {
		return oauthProvider;
	}
	
	public void setOauthProvider(OAuthProvider oauthProvider) {
		this.oauthProvider = oauthProvider;
	}
	
	public String getExternalAccountId() {
		return externalAccountId;
	}
	
	public void setExternalAccountId(String externalAccountId) {
		this.externalAccountId = externalAccountId;
	}
	
	public String getExternalEmail() {
		return externalEmail;
	}
	
	public void setExternalEmail(String externalEmail) {
		this.externalEmail = externalEmail;
	}
	
	public String getDisplayName() {
		return displayName;
	}
	
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	
	public String getAccessTokenEnc() {
		return accessTokenEnc;
	}
	
	public void setAccessTokenEnc(String accessTokenEnc) {
		this.accessTokenEnc = accessTokenEnc;
	}
	
	public String getRefreshTokenEnc() {
		return refreshTokenEnc;
	}
	
	public void setRefreshTokenEnc(String refreshTokenEnc) {
		this.refreshTokenEnc = refreshTokenEnc;
	}
	
	public String getIdTokenEnc() {
		return idTokenEnc;
	}
	
	public void setIdTokenEnc(String idTokenEnc) {
		this.idTokenEnc = idTokenEnc;
	}
	
	public String getScope() {
		return scope;
	}
	
	public void setScope(String scope) {
		this.scope = scope;
	}
	
	public Date getExpiresAt() {
		return expiresAt;
	}
	
	public void setExpiresAt(Date expiresAt) {
		this.expiresAt = expiresAt;
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
	
	public Date getUpdatedAt() {
		return updatedAt;
	}
	
	public void setUpdatedAt(Date updatedAt) {
		this.updatedAt = updatedAt;
	}
	
	public Set<OAuthAccountCapability> getCapabilities() {
		return capabilities;
	}
	
	public void setCapabilities(Set<OAuthAccountCapability> capabilities) {
		this.capabilities = capabilities;
	}
}
