package in.indiemr.teleconsult.provider.dto;

public record MeetingResult(
    String meetingId,
    String joinUrl,
    String calendarEventId   // optional — Google creates both together
) {}