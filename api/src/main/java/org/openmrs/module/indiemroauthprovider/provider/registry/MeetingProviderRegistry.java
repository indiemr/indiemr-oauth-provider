package org.openmrs.module.indiemroauthprovider.provider.registry;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openmrs.module.indiemroauthprovider.provider.MeetingProviderAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("indiemroauthprovider.MeetingProviderRegistry")
public class MeetingProviderRegistry {
	
	private final Map<String, MeetingProviderAdapter> providers = new HashMap<String, MeetingProviderAdapter>();
	
	@Autowired
	public MeetingProviderRegistry(List<MeetingProviderAdapter> implementations) {
		for (MeetingProviderAdapter adapter : implementations) {
			if (providers.containsKey(adapter.getProviderCode())) {
				throw new IllegalStateException("Duplicate meeting provider: " + adapter.getProviderCode());
			}
			providers.put(adapter.getProviderCode(), adapter);
		}
	}
	
	public MeetingProviderAdapter require(String providerCode) {
		MeetingProviderAdapter adapter = providers.get(providerCode);
		if (adapter == null) {
			throw new IllegalArgumentException("Unsupported meeting provider: " + providerCode);
		}
		return adapter;
	}
}
