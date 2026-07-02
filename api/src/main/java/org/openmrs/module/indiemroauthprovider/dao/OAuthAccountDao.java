package org.openmrs.module.indiemroauthprovider.dao;

import java.util.List;

import org.openmrs.Provider;
import org.openmrs.module.indiemroauthprovider.model.OAuthAccount;

public interface OAuthAccountDao {
	
	OAuthAccount getById(Long id);
	
	OAuthAccount findByProviderAndProviderCode(Provider provider, String oauthProviderCode);
	
	List<OAuthAccount> findAllByProvider(Provider provider);
	
	OAuthAccount save(OAuthAccount account);
	
	OAuthAccount saveWithCapabilities(OAuthAccount account, List<String> capabilityCodes);
	
	void markRevoked(Long accountId);
}
