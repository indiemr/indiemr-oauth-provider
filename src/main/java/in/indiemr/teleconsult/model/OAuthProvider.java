package in.indiemr.teleconsult.model;


import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "oauth_provider")
public class OAuthProvider {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "authorization_url", length = 2048)
    private String authorizationUrl;

    @Column(name = "display_name", nullable = false)
    private String displayName;

    @Column(name = "token_url", length = 2048)
    private String tokenUrl;

    @Column(name = "userinfo_url", length = 2048)
    private String userInfoUrl;

    @Column(name = "revocation_url", length = 2048)
    private String revocationUrl;

    @Column(name = "supports_refresh_token", length = 2048)
    private boolean supportsRefreshToken = true;

    @Column(name = "supports_pkce", nullable = false)
    private boolean supportsPkce = true;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(name = "metadata_json", columnDefinition = "TEXT")
    private String metadataJson;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getAuthorizationUrl() {
        return authorizationUrl;
    }

    public void setAuthorizationUrl(String authorizationUrl) {
        this.authorizationUrl = authorizationUrl;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getTokenUrl() {
        return tokenUrl;
    }

    public void setTokenUrl(String tokenUrl) {
        this.tokenUrl = tokenUrl;
    }

    public String getUserInfoUrl() {
        return userInfoUrl;
    }

    public void setUserInfoUrl(String userInfoUrl) {
        this.userInfoUrl = userInfoUrl;
    }

    public String getRevocationUrl() {
        return revocationUrl;
    }

    public void setRevocationUrl(String revocationUrl) {
        this.revocationUrl = revocationUrl;
    }

    public boolean isSupportsRefreshToken() {
        return supportsRefreshToken;
    }

    public void setSupportsRefreshToken(boolean supportsRefreshToken) {
        this.supportsRefreshToken = supportsRefreshToken;
    }

    public boolean isSupportsPkce() {
        return supportsPkce;
    }

    public void setSupportsPkce(boolean supportsPkce) {
        this.supportsPkce = supportsPkce;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getMetadataJson() {
        return metadataJson;
    }

    public void setMetadataJson(String metadataJson) {
        this.metadataJson = metadataJson;
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
}
