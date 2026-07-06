package org.openmrs.module.indiemroauthprovider.web.controller;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.openmrs.Provider;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public abstract class BaseTeleconsultController {
	
	protected Provider getAuthenticatedProvider() {
		User user = Context.getAuthenticatedUser();
		if (user == null || user.getPerson() == null) {
			return null;
		}
		Collection<Provider> providers = Context.getProviderService().getProvidersByPerson(user.getPerson(), false);
		if (providers == null || providers.isEmpty()) {
			return null;
		}
		return providers.iterator().next();
	}
	
	protected ResponseEntity<Map<String, Object>> requireAuthenticatedProvider() {
		if (Context.getAuthenticatedUser() == null) {
			return errorResponse(HttpStatus.UNAUTHORIZED, "Authentication required");
		}
		if (getAuthenticatedProvider() == null) {
			return errorResponse(HttpStatus.BAD_REQUEST, "No provider found for authenticated user");
		}
		return null;
	}
	
	protected ResponseEntity<Map<String, Object>> errorResponse(HttpStatus status, String message) {
		Map<String, Object> error = new HashMap<String, Object>();
		error.put("message", message);
		Map<String, Object> body = new HashMap<String, Object>();
		body.put("error", error);
		return new ResponseEntity<Map<String, Object>>(body, status);
	}
}
