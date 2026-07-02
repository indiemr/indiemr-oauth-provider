package org.openmrs.module.indiemroauthprovider.provider.registry;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openmrs.module.indiemroauthprovider.provider.OAuthProviderAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("indiemroauthprovider.OAuthProviderRegistry")
public class OAuthProviderRegistry {
	
	private final Map<String, OAuthProviderAdapter> providers = new HashMap<String, OAuthProviderAdapter>();
	
	@Autowired
	public OAuthProviderRegistry(List<OAuthProviderAdapter> implementations) {
		for (OAuthProviderAdapter adapter : implementations) {
			if (providers.containsKey(adapter.getProviderCode())) {
				throw new IllegalStateException("Duplicate OAuth provider: " + adapter.getProviderCode());
			}
			providers.put(adapter.getProviderCode(), adapter);
		}
	}
	
	public OAuthProviderAdapter require(String providerCode) {
		OAuthProviderAdapter adapter = providers.get(providerCode);
		if (adapter == null) {
			throw new IllegalArgumentException("Unsupported OAuth provider: " + providerCode);
		}
		return adapter;
	}
}
