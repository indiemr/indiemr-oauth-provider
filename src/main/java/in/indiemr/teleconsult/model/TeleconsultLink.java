package in.indiemr.teleconsult.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "teleconsult_link")
public class TeleconsultLink {

    public static final String STATUS_CREATED = "created";
    public static final String STATUS_SENT = "sent";
    public static final String STATUS_JOINED = "joined";
    public static final String STATUS_EXPIRED = "expired";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "oauth_account_id", nullable = false)
    private OAuthAccount oauthAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "external_resource_mapping_id")
    private ExternalResourceMapping externalResourceMapping;

    @Column(nullable = false, unique = true, length = 64)
    private String token;

    @Column(name = "meeting_url", nullable = false, length = 2048)
    private String meetingUrl;

    @Column(name = "meeting_id")
    private String meetingId;

    @Column(name = "meeting_provider", length = 50)
    private String meetingProvider;

    @Column(name = "patient_name")
    private String patientName;

    @Column(name = "patient_phone", length = 32)
    private String patientPhone;

    @Column(nullable = false, length = 20)
    private String status = STATUS_CREATED;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private boolean voided = false;

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

    public OAuthAccount getOauthAccount() {
        return oauthAccount;
    }

    public void setOauthAccount(OAuthAccount oauthAccount) {
        this.oauthAccount = oauthAccount;
    }

    public ExternalResourceMapping getExternalResourceMapping() {
        return externalResourceMapping;
    }

    public void setExternalResourceMapping(ExternalResourceMapping externalResourceMapping) {
        this.externalResourceMapping = externalResourceMapping;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getMeetingUrl() {
        return meetingUrl;
    }

    public void setMeetingUrl(String meetingUrl) {
        this.meetingUrl = meetingUrl;
    }

    public String getMeetingId() {
        return meetingId;
    }

    public void setMeetingId(String meetingId) {
        this.meetingId = meetingId;
    }

    public String getMeetingProvider() {
        return meetingProvider;
    }

    public void setMeetingProvider(String meetingProvider) {
        this.meetingProvider = meetingProvider;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getPatientPhone() {
        return patientPhone;
    }

    public void setPatientPhone(String patientPhone) {
        this.patientPhone = patientPhone;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
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
}