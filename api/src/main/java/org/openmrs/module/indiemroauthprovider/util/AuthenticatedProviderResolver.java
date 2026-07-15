package org.openmrs.module.indiemroauthprovider.util;

import java.util.Collection;

import org.openmrs.Provider;
import org.openmrs.User;
import org.openmrs.api.APIAuthenticationException;
import org.openmrs.api.context.Context;

public final class AuthenticatedProviderResolver {
	
	private AuthenticatedProviderResolver() {
	}
	
	public static Provider requireAuthenticatedProvider() {
		User user = Context.getAuthenticatedUser();
		if (user == null || user.getPerson() == null) {
			throw new APIAuthenticationException("Authentication required");
		}
		Collection<Provider> providers = Context.getProviderService().getProvidersByPerson(user.getPerson(), false);
		if (providers == null || providers.isEmpty()) {
			throw new APIAuthenticationException("No provider found for authenticated user");
		}
		return providers.iterator().next();
	}
}
