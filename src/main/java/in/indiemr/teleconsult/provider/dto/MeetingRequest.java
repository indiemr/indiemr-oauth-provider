package in.indiemr.teleconsult.provider.dto;

import java.time.Instant;

public record MeetingRequest(
    String summary,
    Instant start,
    Instant end,
    String timeZone
) {}