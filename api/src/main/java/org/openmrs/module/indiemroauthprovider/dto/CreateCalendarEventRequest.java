package org.openmrs.module.indiemroauthprovider.dto;

import java.util.Date;

/**
 * Request to create a provider calendar event linked to OpenMRS resources.
 * <p>
 * Preferred shape — caller sends the exact event title and the primary internal resource.
 * 
 * <pre>
 * {
 *   "oauthProviderCode": "GOOGLE",
 *   "title": "Appointment - John Doe - +91 xxxxx",
 *   "resourceType": "APPOINTMENT",
 *   "resourceUuid": "appointment-uuid",
 *   "start": "2026-07-02T10:00:00.000Z",
 *   "end": "2026-07-02T11:00:00.000Z",
 *   "timeZone": "UTC",
 *   "description": "Optional notes"
 * }
 * </pre>
 * The module uses the provided {@code title} as-is; title composition is owned by the caller.
 */

public class CreateCalendarEventRequest {
	
	private String oauthProviderCode = "GOOGLE";
	
	private String title;
	
	private String resourceType;
	
	private String resourceUuid;
	
	private String description;
	
	private Date start;
	
	private Date end;
	
	private String timeZone = "UTC";
	
	public String getOauthProviderCode() {
		return oauthProviderCode;
	}
	
	public void setOauthProviderCode(String oauthProviderCode) {
		this.oauthProviderCode = oauthProviderCode;
	}
	
	public String getTitle() {
		return title;
	}
	
	public void setTitle(String title) {
		this.title = title;
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
	
	/**
	 * Backward-compat alias for older clients using "summary".
	 */
	public String getSummary() {
		return title;
	}
	
	/**
	 * Backward-compat alias for older clients using "summary".
	 */
	public void setSummary(String summary) {
		this.title = summary;
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
