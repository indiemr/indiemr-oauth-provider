package org.openmrs.module.indiemroauthprovider.dao;

import java.util.List;

import org.openmrs.module.indiemroauthprovider.model.ExternalEvent;
import org.openmrs.module.indiemroauthprovider.model.ResourceEventMapping;

public interface ResourceEventMappingDao {
	
	ResourceEventMapping save(ResourceEventMapping mapping);
	
	List<ResourceEventMapping> findByInternalResource(String internalResourceType, String internalResourceUuid);
	
	List<ResourceEventMapping> findByProviderAndInternalResource(String providerUuid, String internalResourceType,
	        String internalResourceUuid);
	
	ExternalEvent findActiveEventByProviderAndInternalResource(String providerUuid, String internalResourceType,
	        String internalResourceUuid, String externalResourceType);
	
	void voidByInternalResource(String internalResourceType, String internalResourceUuid, String reason);
	
	void voidByProviderAndInternalResource(String providerUuid, String internalResourceType, String internalResourceUuid,
	        String reason);
}
