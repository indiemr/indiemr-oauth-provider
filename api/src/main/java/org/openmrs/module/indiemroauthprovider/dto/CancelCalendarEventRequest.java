package org.openmrs.module.indiemroauthprovider.dto;

/**
 * Cancel calendar/meet resources linked to an OpenMRS resource. Identifies the event by
 * resourceType + resourceUuid (same keys as create/update).
 */
public class CancelCalendarEventRequest {
	
	private String oauthProviderCode = "GOOGLE";
	
	private String resourceType;
	
	private String resourceUuid;
	
	public String getOauthProviderCode() {
		return oauthProviderCode;
	}
	
	public void setOauthProviderCode(String oauthProviderCode) {
		this.oauthProviderCode = oauthProviderCode;
	}
	
	public String getResourceType() {
		return resourceType;
	}
	
	public void setResourceType(String resourceType) {
		this.resourceType = resourceType;
	}
	
	public String getResourceUuid() {
		return resourceUuid;
	}
	
	public void setResourceUuid(String resourceUuid) {
		this.resourceUuid = resourceUuid;
	}
}
