package org.openmrs.module.indiemroauthprovider.provider.dto;

public class MeetingResult {
	
	private final String calendarEventId;
	
	private final String joinUrl;
	
	private final String meetingId;
	
	public MeetingResult(String calendarEventId, String joinUrl, String meetingId) {
		this.calendarEventId = calendarEventId;
		this.joinUrl = joinUrl;
		this.meetingId = meetingId;
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
}
