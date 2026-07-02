package in.indiemr.teleconsult.provider;

import in.indiemr.teleconsult.model.OAuthAccount;
import in.indiemr.teleconsult.provider.dto.CalendarEventRequest;
import in.indiemr.teleconsult.provider.dto.CalendarEventResult;

public interface CalendarProviderAdapter {
    String getProviderCode();

    CalendarEventResult createEvent(
        OAuthAccount account,
        String decryptedRefreshToken,
        CalendarEventRequest request
    ) throws Exception;

    void deleteEvent(
        OAuthAccount account,
        String decryptedRefreshToken,
        String externalEventId
    ) throws Exception;
}
