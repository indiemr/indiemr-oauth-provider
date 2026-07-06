package org.openmrs.module.indiemroauthprovider.dto;

import java.util.Date;

/**
 * Request to create a provider calendar event linked to an OpenMRS resource.
 * <p>
 * Use {@code createMeet=false} for calendar-only, or {@code createMeet=true} for calendar + video
 * meeting at the same {@code start}/{@code end}. Set {@code mintJoinLink=true} to also mint a
 * shareable teleconsult join link (only valid when {@code createMeet=true}).
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
 *   "description": "Optional notes",
 *   "createMeet": true,
 *   "mintJoinLink": true
 * }
 * </pre>
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
	
	private boolean createMeet;
	
	private boolean mintJoinLink;
	
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
	
	public String getSummary() {
		return title;
	}
	
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
	
	public boolean isCreateMeet() {
		return createMeet;
	}
	
	public void setCreateMeet(boolean createMeet) {
		this.createMeet = createMeet;
	}
	
	public boolean isMintJoinLink() {
		return mintJoinLink;
	}
	
	public void setMintJoinLink(boolean mintJoinLink) {
		this.mintJoinLink = mintJoinLink;
	}
}
