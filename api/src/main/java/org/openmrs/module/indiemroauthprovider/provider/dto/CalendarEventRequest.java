package org.openmrs.module.indiemroauthprovider.provider.dto;

import java.util.Date;

public class CalendarEventRequest {
	
	private final String summary;
	
	private final String description;
	
	private final Date start;
	
	private final Date end;
	
	private final String timeZone;
	
	public CalendarEventRequest(String summary, String description, Date start, Date end, String timeZone) {
		this.summary = summary;
		this.description = description;
		this.start = start;
		this.end = end;
		this.timeZone = timeZone;
	}
	
	public String getSummary() {
		return summary;
	}
	
	public String getDescription() {
		return description;
	}
	
	public Date getStart() {
		return start;
	}
	
	public Date getEnd() {
		return end;
	}
	
	public String getTimeZone() {
		return timeZone;
	}
}
