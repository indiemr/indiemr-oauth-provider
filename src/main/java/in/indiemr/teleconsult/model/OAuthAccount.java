package in.indiemr.teleconsult.model;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "oauth_account")
public class OAuthAccount {
    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_REVOKED = "REVOKED";
    public static final String STATUS_EXPIRED = "EXPIRED";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "provider_uuid", nullable = false, length = 64)
    private String providerUuid;

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
    private LocalDateTime expiresAt;

    @Column(nullable = false, length = 32)
    private String status = STATUS_ACTIVE;

    @Column(name = "metadata_json", columnDefinition = "TEXT")
    private String metadataJson;

    @Column(nullable = false)
    private boolean voided = false;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "oauthAccount", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<OAuthAccountCapability> capabilities = new HashSet<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProviderUuid() {
        return providerUuid;
    }

    public void setProviderUuid(String providerUuid) {
        this.providerUuid = providerUuid;
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

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Set<OAuthAccountCapability> getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(Set<OAuthAccountCapability> capabilities) {
        this.capabilities = capabilities;
    }
}
