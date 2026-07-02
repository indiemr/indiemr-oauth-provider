package org.openmrs.module.indiemroauthprovider.provider.dto;

public class CalendarEventResult {
	
	private final String externalEventId;
	
	private final String htmlLink;
	
	public CalendarEventResult(String externalEventId, String htmlLink) {
		this.externalEventId = externalEventId;
		this.htmlLink = htmlLink;
	}
	
	public String getExternalEventId() {
		return externalEventId;
	}
	
	public String getHtmlLink() {
		return htmlLink;
	}
}
