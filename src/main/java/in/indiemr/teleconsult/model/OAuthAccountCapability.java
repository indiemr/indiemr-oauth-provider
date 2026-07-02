package in.indiemr.teleconsult.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "oauth_account_capability")
public class OAuthAccountCapability {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "oauth_account_id", nullable = false)
    private OAuthAccount oauthAccount;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "capability_id", nullable = false)
    private OAuthCapability capability;

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

    public OAuthCapability getCapability() {
        return capability;
    }

    public void setCapability(OAuthCapability capability) {
        this.capability = capability;
    }
}
