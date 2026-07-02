package org.openmrs.module.indiemroauthprovider.dto;

public class CreateCalendarEventResponse {
	
	private String resourceUuid;
	
	private String externalEventId;
	
	private String htmlLink;
	
	private String meetingUrl;
	
	private String joinToken;
	
	private String resolverUrl;
	
	public CreateCalendarEventResponse() {
	}
	
	public CreateCalendarEventResponse(String resourceUuid, String externalEventId, String htmlLink) {
		this.resourceUuid = resourceUuid;
		this.externalEventId = externalEventId;
		this.htmlLink = htmlLink;
	}
	
	public String getResourceUuid() {
		return resourceUuid;
	}
	
	public void setResourceUuid(String resourceUuid) {
		this.resourceUuid = resourceUuid;
	}
	
	public String getAppointmentUuid() {
		return resourceUuid;
	}
	
	public void setAppointmentUuid(String appointmentUuid) {
		this.resourceUuid = appointmentUuid;
	}
	
	public String getExternalEventId() {
		return externalEventId;
	}
	
	public void setExternalEventId(String externalEventId) {
		this.externalEventId = externalEventId;
	}
	
	public String getHtmlLink() {
		return htmlLink;
	}
	
	public void setHtmlLink(String htmlLink) {
		this.htmlLink = htmlLink;
	}
	
	public String getMeetingUrl() {
		return meetingUrl;
	}
	
	public void setMeetingUrl(String meetingUrl) {
		this.meetingUrl = meetingUrl;
	}
	
	public String getJoinToken() {
		return joinToken;
	}
	
	public void setJoinToken(String joinToken) {
		this.joinToken = joinToken;
	}
	
	public String getResolverUrl() {
		return resolverUrl;
	}
	
	public void setResolverUrl(String resolverUrl) {
		this.resolverUrl = resolverUrl;
	}
}
