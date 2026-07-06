package org.openmrs.module.indiemroauthprovider.dao;

import java.util.List;

import org.openmrs.module.indiemroauthprovider.model.ExternalResourceMapping;

public interface ExternalResourceMappingDao {
	
	ExternalResourceMapping save(ExternalResourceMapping mapping);
	
	List<ExternalResourceMapping> findByInternalResource(String internalResourceType, String internalResourceUuid);
	
	List<ExternalResourceMapping> findByProviderAndInternalResource(String providerUuid, String internalResourceType,
	        String internalResourceUuid);
	
	void voidByInternalResource(String internalResourceType, String internalResourceUuid);
	
	void voidByProviderAndInternalResource(String providerUuid, String internalResourceType, String internalResourceUuid);
	
	ExternalResourceMapping findActiveMeetingMapping(String providerUuid, String internalResourceType,
	        String internalResourceUuid);
}
