package in.indiemr.teleconsult.dto;

public record CreateCalendarEventResponse(
    String appointmentUuid,
    String externalEventId,
    String htmlLink
) {}