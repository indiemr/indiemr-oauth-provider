package org.openmrs.module.indiemroauthprovider.provider.google;

import org.openmrs.module.indiemroauthprovider.model.OAuthAccount;
import org.openmrs.module.indiemroauthprovider.provider.CalendarProviderAdapter;
import org.openmrs.module.indiemroauthprovider.provider.MeetingProviderAdapter;
import org.openmrs.module.indiemroauthprovider.provider.dto.CalendarEventRequest;
import org.openmrs.module.indiemroauthprovider.provider.dto.CalendarEventResult;
import org.openmrs.module.indiemroauthprovider.provider.dto.CalendarEventUpdate;
import org.openmrs.module.indiemroauthprovider.provider.dto.MeetingRequest;
import org.openmrs.module.indiemroauthprovider.provider.dto.MeetingResult;
import org.openmrs.module.indiemroauthprovider.util.ModuleConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.ClientParametersAuthentication;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.ConferenceData;
import com.google.api.services.calendar.model.ConferenceSolutionKey;
import com.google.api.services.calendar.model.CreateConferenceRequest;
import com.google.api.services.calendar.model.EntryPoint;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;

@Component("indiemroauthprovider.GoogleCalendarMeetingAdapter")
public class GoogleCalendarMeetingAdapter implements CalendarProviderAdapter, MeetingProviderAdapter {
	
	@Autowired
	@Qualifier("indiemroauthprovider.ModuleConfig")
	private ModuleConfig moduleConfig;
	
	@Override
	public String getProviderCode() {
		return GoogleOAuthProviderAdapter.CODE;
	}
	
	@Override
	public CalendarEventResult createEvent(OAuthAccount account, String decryptedRefreshToken, CalendarEventRequest request)
	        throws Exception {
		Event createdEvent = insertEvent(decryptedRefreshToken, request, false);
		return new CalendarEventResult(createdEvent.getId(), createdEvent.getHtmlLink());
	}
	
	@Override
	public MeetingResult createMeeting(OAuthAccount account, String decryptedRefreshToken, MeetingRequest request)
	        throws Exception {
		CalendarEventRequest calReq = new CalendarEventRequest(request.getSummary(), null, request.getStart(),
		        request.getEnd(), request.getTimeZone());
		Event createdEvent = insertEvent(decryptedRefreshToken, calReq, true);
		
		String meetUrl = createdEvent.getHangoutLink();
		if (meetUrl == null && createdEvent.getConferenceData() != null) {
			meetUrl = null;
			for (EntryPoint entryPoint : createdEvent.getConferenceData().getEntryPoints()) {
				if ("video".equals(entryPoint.getEntryPointType())) {
					meetUrl = entryPoint.getUri();
					break;
				}
			}
			if (meetUrl == null) {
				throw new IllegalStateException("Google did not return a Meet link");
			}
		}
		return new MeetingResult(createdEvent.getId(), meetUrl, createdEvent.getId(), createdEvent.getHtmlLink());
	}
	
	@Override
	public CalendarEventResult updateEvent(OAuthAccount account, String decryptedRefreshToken, String externalEventId,
	        CalendarEventUpdate update) throws Exception {
		Calendar client = calendarClient(decryptedRefreshToken);
		Event event = client.events().get("primary", externalEventId).execute();
		
		if (update.getTitle() != null) {
			event.setSummary(update.getTitle());
		}
		if (update.getDescription() != null) {
			event.setDescription(update.getDescription());
		}
		if (update.getStart() != null) {
			EventDateTime start = event.getStart() != null ? event.getStart() : new EventDateTime();
			start.setDateTime(new DateTime(update.getStart().getTime()));
			if (update.getTimeZone() != null) {
				start.setTimeZone(update.getTimeZone());
			}
			event.setStart(start);
		}
		if (update.getEnd() != null) {
			EventDateTime end = event.getEnd() != null ? event.getEnd() : new EventDateTime();
			end.setDateTime(new DateTime(update.getEnd().getTime()));
			if (update.getTimeZone() != null) {
				end.setTimeZone(update.getTimeZone());
			}
			event.setEnd(end);
		}
		
		Event updated = client.events().patch("primary", externalEventId, event).execute();
		return new CalendarEventResult(updated.getId(), updated.getHtmlLink());
	}
	
	@Override
	public void deleteEvent(OAuthAccount account, String decryptedRefreshToken, String externalEventId) throws Exception {
		calendarClient(decryptedRefreshToken).events().delete("primary", externalEventId).execute();
	}
	
	@Override
	public void deleteMeeting(OAuthAccount account, String decryptedRefreshToken, String meetingId) throws Exception {
		deleteEvent(account, decryptedRefreshToken, meetingId);
	}
	
	private Event insertEvent(String refreshToken, CalendarEventRequest req, boolean withMeet) throws Exception {
		String requestId = "indiemr-" + System.currentTimeMillis();
		Event event = new Event().setSummary(req.getSummary())
		        .setStart(new EventDateTime().setDateTime(new DateTime(req.getStart().getTime())))
		        .setEnd(new EventDateTime().setDateTime(new DateTime(req.getEnd().getTime())));
		
		if (withMeet) {
			event.setConferenceData(new ConferenceData().setCreateRequest(new CreateConferenceRequest().setRequestId(
			    requestId).setConferenceSolutionKey(new ConferenceSolutionKey().setType("hangoutsMeet"))));
		}
		
		Calendar.Events.Insert insert = calendarClient(refreshToken).events().insert("primary", event);
		if (withMeet) {
			insert.setConferenceDataVersion(1);
		}
		return insert.execute();
	}
	
	private Calendar calendarClient(String refreshToken) throws Exception {
		HttpTransport transport = GoogleNetHttpTransport.newTrustedTransport();
		GsonFactory json = GsonFactory.getDefaultInstance();
		Credential cred = new Credential.Builder(BearerToken.authorizationHeaderAccessMethod())
		        .setTransport(transport)
		        .setJsonFactory(json)
		        .setTokenServerUrl(new GenericUrl("https://oauth2.googleapis.com/token"))
		        .setClientAuthentication(
		            new ClientParametersAuthentication(moduleConfig.getGoogleClientId(), moduleConfig
		                    .getGoogleClientSecret())).build().setRefreshToken(refreshToken);
		return new Calendar.Builder(transport, json, cred).setApplicationName("IndiEMR Teleconsult").build();
	}
}
