package in.indiemr.teleconsult.service;

import in.indiemr.teleconsult.config.TeleconsultProperties;
import in.indiemr.teleconsult.crypto.CryptoService;
import in.indiemr.teleconsult.dao.ExternalResourceMappingDao;
import in.indiemr.teleconsult.dao.OAuthAccountDao;
import in.indiemr.teleconsult.dao.TeleconsultLinkDao;
import in.indiemr.teleconsult.dto.MintLinkRequest;
import in.indiemr.teleconsult.dto.MintLinkResponse;
import in.indiemr.teleconsult.exception.TeleconsultException;
import in.indiemr.teleconsult.model.ExternalResourceMapping;
import in.indiemr.teleconsult.model.OAuthAccount;
import in.indiemr.teleconsult.model.TeleconsultLink;
import in.indiemr.teleconsult.provider.MeetingProviderAdapter;
import in.indiemr.teleconsult.provider.dto.MeetingRequest;
import in.indiemr.teleconsult.provider.dto.MeetingResult;
import in.indiemr.teleconsult.provider.registry.MeetingProviderRegistry;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import org.springframework.stereotype.Service;
import in.indiemr.teleconsult.dto.CreateCalendarEventRequest;
import in.indiemr.teleconsult.dto.CreateCalendarEventResponse;
import in.indiemr.teleconsult.provider.dto.CalendarEventRequest;
import in.indiemr.teleconsult.provider.dto.CalendarEventResult;
import in.indiemr.teleconsult.provider.registry.CalendarProviderRegistry;

@Service
public class TeleconsultService {
    private static final long LINK_TTL_HOURS = 6;
    private static final long MEET_WINDOW_SECONDS = 3600;

    private final MeetingProviderRegistry meetingRegistry;
    private final OAuthAccountDao oauthAccountDao;
    private final ExternalResourceMappingDao externalResourceMappingDao;
    private final TeleconsultLinkDao teleconsultLinkDao;
    private final CryptoService crypto;
    private final TeleconsultProperties props;
    private final CalendarProviderRegistry calendarRegistry;

    public TeleconsultService(MeetingProviderRegistry meetingRegistry,
                              OAuthAccountDao oauthAccountDao,
                              ExternalResourceMappingDao externalResourceMappingDao,
                              TeleconsultLinkDao teleconsultLinkDao,
                              CryptoService crypto,
                              TeleconsultProperties props,
                              CalendarProviderRegistry calendarRegistry) {
        this.meetingRegistry = meetingRegistry;
        this.oauthAccountDao = oauthAccountDao;
        this.externalResourceMappingDao = externalResourceMappingDao;
        this.teleconsultLinkDao = teleconsultLinkDao;
        this.crypto = crypto;
        this.props = props;
        this.calendarRegistry = calendarRegistry;
    }

    public MintLinkResponse mintLink(MintLinkRequest request) throws Exception {
        String oauthProviderCode = request.getOauthProviderCode() != null
                            ? request.getOauthProviderCode()
                            : "GOOGLE";
        OAuthAccount account = oauthAccountDao
                            .findByProviderUuidAndProviderCode(request.getProviderUuid(), oauthProviderCode)
                            .orElseThrow(() -> new IllegalStateException(
                                "No connected " + oauthProviderCode + " account for provider. Run connect-url first."));

        String refreshToken = crypto.decrypt(account.getRefreshTokenEnc());
        Instant now = Instant.now();
        Instant end = now.plusSeconds(MEET_WINDOW_SECONDS);

        String summary = "Teleconsultation" + (request.getPatientName() != null ? " - " + request.getPatientName() : "");
        MeetingProviderAdapter meetingProvider = meetingRegistry.require(oauthProviderCode);
        MeetingResult meeting = meetingProvider.createMeeting(account, refreshToken, 
            new MeetingRequest(summary, now, end, "UTC")
        );

        String appointmentUuid = request.getAppointmentUuid() != null
            ? request.getAppointmentUuid()
            : "apt-" + UUID.randomUUID();

        ExternalResourceMapping calendarMapping = new ExternalResourceMapping();
        calendarMapping.setOauthAccount(account);
        calendarMapping.setInternalResourceType(ExternalResourceMapping.INTERNAL_APPOINTMENT);
        calendarMapping.setInternalResourceUuid(appointmentUuid);
        calendarMapping.setExternalResourceType(ExternalResourceMapping.EXTERNAL_CALENDAR_EVENT);
        calendarMapping.setExternalResourceId(
            meeting.calendarEventId() != null ? meeting.calendarEventId() : meeting.meetingId());
        externalResourceMappingDao.save(calendarMapping);

         // Video meeting mapping
         ExternalResourceMapping meetMapping = new ExternalResourceMapping();
         meetMapping.setOauthAccount(account);
         meetMapping.setInternalResourceType(ExternalResourceMapping.INTERNAL_APPOINTMENT);
         meetMapping.setInternalResourceUuid(appointmentUuid);
         meetMapping.setExternalResourceType(ExternalResourceMapping.EXTERNAL_VIDEO_MEETING);
         meetMapping.setExternalResourceId(meeting.meetingId());
         externalResourceMappingDao.save(meetMapping);

         // Patient resolver link
        TeleconsultLink link = new TeleconsultLink();
        link.setOauthAccount(account);
        link.setExternalResourceMapping(meetMapping);
        link.setToken(crypto.randomToken(18));
        link.setMeetingUrl(meeting.joinUrl());
        link.setMeetingId(meeting.meetingId());
        link.setMeetingProvider(oauthProviderCode);
        link.setPatientName(request.getPatientName());
        link.setPatientPhone(request.getPatientPhone());
        link.setExpiresAt(LocalDateTime.now(ZoneOffset.UTC).plusHours(LINK_TTL_HOURS));
        link.setVoided(false);
        teleconsultLinkDao.save(link);

        String resolverUrl = props.getPublicBaseUrl() + "/c/" + link.getToken();
        return new MintLinkResponse(link.getToken(), link.getMeetingUrl(), resolverUrl);
    }

    public ResolveResult resolveLink(String token) {
        TeleconsultLink link = teleconsultLinkDao.findByToken(token)
            .orElseThrow(() -> new TeleconsultException("Invalid or unknown link", 404));

        if (link.getExpiresAt().isBefore(LocalDateTime.now(ZoneOffset.UTC))) {
            teleconsultLinkDao.markStatus(token, TeleconsultLink.STATUS_EXPIRED);
            throw new TeleconsultException("This consultation link has expired", 410);
        }

        teleconsultLinkDao.markStatus(token, TeleconsultLink.STATUS_JOINED);

        OAuthAccount account = link.getOauthAccount();
        String providerDisplay = account.getDisplayName() != null
            ? account.getDisplayName()
            : account.getExternalEmail();

        return new ResolveResult(link.getMeetingUrl(), providerDisplay);
    }

    public CreateCalendarEventResponse createCalendarEvent(CreateCalendarEventRequest request) throws Exception {
        String oauthProviderCode = request.getOauthProviderCode() != null
            ? request.getOauthProviderCode()
            : "GOOGLE";
    
        OAuthAccount account = oauthAccountDao
            .findByProviderUuidAndProviderCode(request.getProviderUuid(), oauthProviderCode)
            .orElseThrow(() -> new IllegalStateException(
                "No connected " + oauthProviderCode + " account for provider. Run connect-url first."));
    
        String refreshToken = crypto.decrypt(account.getRefreshTokenEnc());
    
        CalendarEventResult event = calendarRegistry.require(oauthProviderCode).createEvent(
            account,
            refreshToken,
            new CalendarEventRequest(
                request.getSummary(),
                request.getDescription(),
                request.getStart(),
                request.getEnd(),
                request.getTimeZone()
            )
        );
    
        String appointmentUuid = request.getAppointmentUuid() != null
            ? request.getAppointmentUuid()
            : "apt-" + UUID.randomUUID();
    
        ExternalResourceMapping calendarMapping = new ExternalResourceMapping();
        calendarMapping.setOauthAccount(account);
        calendarMapping.setInternalResourceType(ExternalResourceMapping.INTERNAL_APPOINTMENT);
        calendarMapping.setInternalResourceUuid(appointmentUuid);
        calendarMapping.setExternalResourceType(ExternalResourceMapping.EXTERNAL_CALENDAR_EVENT);
        calendarMapping.setExternalResourceId(event.externalEventId());
        externalResourceMappingDao.save(calendarMapping);
    
        return new CreateCalendarEventResponse(
            appointmentUuid,
            event.externalEventId(),
            event.htmlLink()
        );
    }

    public record ResolveResult(String meetUrl, String providerDisplay) {}
}


