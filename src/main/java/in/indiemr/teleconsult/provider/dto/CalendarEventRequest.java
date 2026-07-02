package in.indiemr.teleconsult.provider.dto;

import java.time.Instant;

public record CalendarEventRequest(
    String summary,
    String description,
    Instant start,
    Instant end,
    String timeZone
) {}