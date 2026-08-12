package org.openmrs.module.indiemroauthprovider.provider.dto;

public class CalendarEventResult {
	
	private final String externalEventId;
	
	private final String htmlLink;
	
	private final String meetingUrl;
	
	public CalendarEventResult(String externalEventId, String htmlLink, String meetingUrl) {
		this.externalEventId = externalEventId;
		this.htmlLink = htmlLink;
		this.meetingUrl = meetingUrl;
	}
	
	public String getExternalEventId() {
		return externalEventId;
	}
	
	public String getHtmlLink() {
		return htmlLink;
	}
	
	public String getMeetingUrl() {
		return meetingUrl;
	}
}
