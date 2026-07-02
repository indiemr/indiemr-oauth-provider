package in.indiemr.teleconsult.provider;

import in.indiemr.teleconsult.model.OAuthAccount;
import in.indiemr.teleconsult.provider.dto.MeetingRequest;
import in.indiemr.teleconsult.provider.dto.MeetingResult;

public interface MeetingProviderAdapter {
    String getProviderCode();    

    MeetingResult createMeeting(
        OAuthAccount account,
        String decryptedRefreshToken,
        MeetingRequest request
    ) throws Exception;

    void deleteMeeting(
        OAuthAccount account,
        String decryptedRefreshToken,
        String meetingId
    ) throws Exception;
}
