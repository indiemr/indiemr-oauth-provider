package org.openmrs.module.indiemroauthprovider.provider.dto;

import java.util.Date;

public class MeetingRequest {
	
	private final String summary;
	
	private final Date start;
	
	private final Date end;
	
	private final String timeZone;
	
	public MeetingRequest(String summary, Date start, Date end, String timeZone) {
		this.summary = summary;
		this.start = start;
		this.end = end;
		this.timeZone = timeZone;
	}
	
	public String getSummary() {
		return summary;
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
