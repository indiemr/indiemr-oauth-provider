package org.openmrs.module.indiemroauthprovider.dto;

public class CreateCalendarEventResponse {
	
	private String appointmentUuid;
	
	private String externalEventId;
	
	private String htmlLink;
	
	public CreateCalendarEventResponse() {
	}
	
	public CreateCalendarEventResponse(String appointmentUuid, String externalEventId, String htmlLink) {
		this.appointmentUuid = appointmentUuid;
		this.externalEventId = externalEventId;
		this.htmlLink = htmlLink;
	}
	
	public String getAppointmentUuid() {
		return appointmentUuid;
	}
	
	public void setAppointmentUuid(String appointmentUuid) {
		this.appointmentUuid = appointmentUuid;
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
}
