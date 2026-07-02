package in.indiemr.teleconsult.dto;

import java.time.Instant;

public class CreateCalendarEventRequest {
    private String providerUuid;
    private String oauthProviderCode = "GOOGLE";
    private String appointmentUuid;
    private String summary;
    private String description;
    private Instant start;
    private Instant end;
    private String timeZone = "UTC";

    public String getProviderUuid() { return providerUuid; }
    public void setProviderUuid(String providerUuid) { this.providerUuid = providerUuid; }
    public String getOauthProviderCode() { return oauthProviderCode; }
    public void setOauthProviderCode(String oauthProviderCode) { this.oauthProviderCode = oauthProviderCode; }
    public String getAppointmentUuid() { return appointmentUuid; }
    public void setAppointmentUuid(String appointmentUuid) { this.appointmentUuid = appointmentUuid; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Instant getStart() { return start; }
    public void setStart(Instant start) { this.start = start; }
    public Instant getEnd() { return end; }
    public void setEnd(Instant end) { this.end = end; }
    public String getTimeZone() { return timeZone; }
    public void setTimeZone(String timeZone) { this.timeZone = timeZone; }
}