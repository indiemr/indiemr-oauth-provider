package org.openmrs.module.indiemroauthprovider.util;

import java.util.Date;
import java.util.UUID;

import org.openmrs.BaseChangeableOpenmrsData;
import org.openmrs.OpenmrsData;
import org.openmrs.User;
import org.openmrs.api.context.Context;

public final class OpenmrsDataUtils {
	
	private OpenmrsDataUtils() {
	}
	
	public static void setCreated(OpenmrsData data) {
		if (data.getUuid() == null) {
			data.setUuid(UUID.randomUUID().toString());
		}
		if (data.getCreator() == null) {
			data.setCreator(getAuthenticatedUser());
		}
		if (data.getDateCreated() == null) {
			data.setDateCreated(new Date());
		}
		if (data.getVoided() == null) {
			data.setVoided(false);
		}
	}
	
	public static void setChanged(BaseChangeableOpenmrsData data) {
		User user = Context.getAuthenticatedUser();
		if (user != null) {
			data.setChangedBy(user);
		}
		data.setDateChanged(new Date());
	}
	
	public static void voidWithReason(OpenmrsData data, String reason) {
		data.setVoided(true);
		data.setVoidedBy(getAuthenticatedUser());
		data.setDateVoided(new Date());
		data.setVoidReason(reason);
	}
	
	private static User getAuthenticatedUser() {
		User user = Context.getAuthenticatedUser();
		if (user == null) {
			throw new IllegalStateException("No authenticated user in context");
		}
		return user;
	}
}
