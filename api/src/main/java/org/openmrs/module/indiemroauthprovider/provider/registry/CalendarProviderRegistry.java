package org.openmrs.module.indiemroauthprovider.provider.registry;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openmrs.module.indiemroauthprovider.provider.CalendarProviderAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("indiemroauthprovider.CalendarProviderRegistry")
public class CalendarProviderRegistry {
	
	private final Map<String, CalendarProviderAdapter> providers = new HashMap<String, CalendarProviderAdapter>();
	
	@Autowired
	public CalendarProviderRegistry(List<CalendarProviderAdapter> implementations) {
		for (CalendarProviderAdapter adapter : implementations) {
			if (providers.containsKey(adapter.getProviderCode())) {
				throw new IllegalStateException("Duplicate calendar provider: " + adapter.getProviderCode());
			}
			providers.put(adapter.getProviderCode(), adapter);
		}
	}
	
	public CalendarProviderAdapter require(String providerCode) {
		CalendarProviderAdapter adapter = providers.get(providerCode);
		if (adapter == null) {
			throw new IllegalArgumentException("Unsupported calendar provider: " + providerCode);
		}
		return adapter;
	}
}
