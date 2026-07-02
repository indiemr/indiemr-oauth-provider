package org.openmrs.module.indiemroauthprovider.dto;

import java.util.Date;

/**
 * Partial update for an existing calendar event linked to an OpenMRS resource. Only non-null fields
 * are applied; at least one of title, description, start, end must be set.
 */
public class UpdateCalendarEventRequest {
	
	private String oauthProviderCode = "GOOGLE";
	
	private String resourceType;
	
	private String resourceUuid;
	
	private String title;
	
	private String description;
	
	private Date start;
	
	private Date end;
	
	private String timeZone;
	
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
	
	public String getTitle() {
		return title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public Date getStart() {
		return start;
	}
	
	public void setStart(Date start) {
		this.start = start;
	}
	
	public Date getEnd() {
		return end;
	}
	
	public void setEnd(Date end) {
		this.end = end;
	}
	
	public String getTimeZone() {
		return timeZone;
	}
	
	public void setTimeZone(String timeZone) {
		this.timeZone = timeZone;
	}
}
