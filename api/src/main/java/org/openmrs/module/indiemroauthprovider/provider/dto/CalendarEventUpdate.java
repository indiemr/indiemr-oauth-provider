package org.openmrs.module.indiemroauthprovider.provider.dto;

import java.util.Date;

public class CalendarEventUpdate {
	
	private String title;
	
	private String description;
	
	private Date start;
	
	private Date end;
	
	private String timeZone;
	
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
