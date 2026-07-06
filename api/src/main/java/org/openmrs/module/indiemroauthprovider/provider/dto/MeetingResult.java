package org.openmrs.module.indiemroauthprovider.provider.dto;

public class MeetingResult {
	
	private final String calendarEventId;
	
	private final String joinUrl;
	
	private final String meetingId;
	
	private final String htmlLink;
	
	public MeetingResult(String calendarEventId, String joinUrl, String meetingId) {
		this(calendarEventId, joinUrl, meetingId, null);
	}
	
	public MeetingResult(String calendarEventId, String joinUrl, String meetingId, String htmlLink) {
		this.calendarEventId = calendarEventId;
		this.joinUrl = joinUrl;
		this.meetingId = meetingId;
		this.htmlLink = htmlLink;
	}
	
	public String getCalendarEventId() {
		return calendarEventId;
	}
	
	public String getJoinUrl() {
		return joinUrl;
	}
	
	public String getMeetingId() {
		return meetingId;
	}
	
	public String getHtmlLink() {
		return htmlLink;
	}
}
