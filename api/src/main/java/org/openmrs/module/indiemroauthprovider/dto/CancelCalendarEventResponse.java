package org.openmrs.module.indiemroauthprovider.dto;

public class CancelCalendarEventResponse {
	
	private String status = "CANCELLED";
	
	private String resourceType;
	
	private String resourceUuid;
	
	public CancelCalendarEventResponse() {
	}
	
	public CancelCalendarEventResponse(String resourceType, String resourceUuid) {
		this.resourceType = resourceType;
		this.resourceUuid = resourceUuid;
	}
	
	public String getStatus() {
		return status;
	}
	
	public void setStatus(String status) {
		this.status = status;
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
