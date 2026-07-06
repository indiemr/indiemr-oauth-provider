package org.openmrs.module.indiemroauthprovider.model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "indiemr_oauth_account_capability")
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
