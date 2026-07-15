package org.openmrs.module.indiemroauthprovider.dao;

import org.openmrs.module.indiemroauthprovider.model.ExternalEvent;

public interface ExternalEventDao {
	
	ExternalEvent save(ExternalEvent event);
	
	void voidEvent(ExternalEvent event, String reason);
}
