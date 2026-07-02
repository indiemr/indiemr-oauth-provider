package in.indiemr.teleconsult.provider.google;

import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.ClientParametersAuthentication;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.*;
import in.indiemr.teleconsult.config.GoogleProperties;
import in.indiemr.teleconsult.model.OAuthAccount;
import in.indiemr.teleconsult.provider.CalendarProviderAdapter;
import in.indiemr.teleconsult.provider.MeetingProviderAdapter;
import in.indiemr.teleconsult.provider.dto.*;
import org.springframework.stereotype.Component;

@Component
public class GoogleCalendarMeetingAdapter implements CalendarProviderAdapter, MeetingProviderAdapter {

    private final GoogleProperties googleProperties;
    public GoogleCalendarMeetingAdapter(GoogleProperties googleProperties) {
        this.googleProperties = googleProperties;
    }
    @Override
    public String getProviderCode() {
        return GoogleOAuthProviderAdapter.CODE;
    }

    @Override
    public CalendarEventResult createEvent(OAuthAccount account, String decryptedRefreshToken,
            CalendarEventRequest request) throws Exception {
        Event createdEvent = insertEvent(decryptedRefreshToken, request, false);
        return new CalendarEventResult(createdEvent.getId(), createdEvent.getHtmlLink());
    }

    @Override
    public MeetingResult createMeeting(OAuthAccount account, String decryptedRefreshToken, MeetingRequest request)
            throws Exception {
        CalendarEventRequest calReq = new CalendarEventRequest(
            request.summary(), null, request.start(), request.end(), request.timeZone()
        );
        Event createdEvent = insertEvent(decryptedRefreshToken, calReq, true);

        String meetUrl = createdEvent.getHangoutLink();
        if (meetUrl == null && createdEvent.getConferenceData() != null) {
            meetUrl = createdEvent.getConferenceData().getEntryPoints().stream()
                .filter(e -> "video".equals(e.getEntryPointType()))
                .map(EntryPoint::getUri)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Google did not return a Meet link"));
        }
        return new MeetingResult(createdEvent.getId(), meetUrl, createdEvent.getId());
    }

    @Override
    public void deleteEvent(OAuthAccount account, String decryptedRefreshToken, String externalEventId)
            throws Exception {
                calendarClient(decryptedRefreshToken).events().delete("primary", externalEventId).execute();
    }

    @Override
    public void deleteMeeting(OAuthAccount account, String decryptedRefreshToken, String meetingId) throws Exception {
        deleteEvent(account, decryptedRefreshToken, meetingId); // Meet tied to calendar event
    }

    private Event insertEvent(String refreshToken, CalendarEventRequest req, boolean withMeet) throws Exception {
        String requestId = "poc-" + System.currentTimeMillis();
        Event event = new Event()
            .setSummary(req.summary())
            .setStart(new EventDateTime().setDateTime(new DateTime(req.start().toString())))
            .setEnd(new EventDateTime().setDateTime(new DateTime(req.end().toString())));

        if (withMeet) {
            event.setConferenceData(new ConferenceData().setCreateRequest(
                new CreateConferenceRequest()
                .setRequestId(requestId)
                .setConferenceSolutionKey(new ConferenceSolutionKey().setType("hangoutsMeet"))
            ));
        }

        Calendar.Events.Insert insert = calendarClient(refreshToken).events().insert("primary", event);
        if (withMeet) insert.setConferenceDataVersion(1);
        return insert.execute();
    }

    private Calendar calendarClient(String refreshToken) throws Exception {
        var transport = GoogleNetHttpTransport.newTrustedTransport();
        var json = GsonFactory.getDefaultInstance();
        Credential cred = new Credential.Builder(BearerToken.authorizationHeaderAccessMethod())
            .setTransport(transport).setJsonFactory(json)
            .setTokenServerUrl(new GenericUrl("https://oauth2.googleapis.com/token"))
            .setClientAuthentication(new ClientParametersAuthentication(
                googleProperties.getClientId(), googleProperties.getClientSecret()))
            .build()
            .setRefreshToken(refreshToken);
        return new Calendar.Builder(transport, json, cred)
            .setApplicationName("IndiEMR Teleconsult").build();
    }
}
