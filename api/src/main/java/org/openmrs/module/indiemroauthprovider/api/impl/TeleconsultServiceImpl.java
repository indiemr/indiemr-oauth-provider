package org.openmrs.module.indiemroauthprovider.api.impl;

import java.util.Calendar;
import java.util.Date;

import org.openmrs.Provider;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.indiemroauthprovider.api.TeleconsultService;
import org.openmrs.module.indiemroauthprovider.crypto.CryptoService;
import org.openmrs.module.indiemroauthprovider.dao.ExternalResourceMappingDao;
import org.openmrs.module.indiemroauthprovider.dao.OAuthAccountDao;
import org.openmrs.module.indiemroauthprovider.dao.TeleconsultLinkDao;
import org.openmrs.module.indiemroauthprovider.dto.CreateCalendarEventRequest;
import org.openmrs.module.indiemroauthprovider.dto.CreateCalendarEventResponse;
import org.openmrs.module.indiemroauthprovider.dto.MintLinkRequest;
import org.openmrs.module.indiemroauthprovider.dto.MintLinkResponse;
import org.openmrs.module.indiemroauthprovider.dto.ResolveResult;
import org.openmrs.module.indiemroauthprovider.exception.TeleconsultException;
import org.openmrs.module.indiemroauthprovider.model.ExternalResourceMapping;
import org.openmrs.module.indiemroauthprovider.model.OAuthAccount;
import org.openmrs.module.indiemroauthprovider.model.TeleconsultLink;
import org.openmrs.module.indiemroauthprovider.provider.MeetingProviderAdapter;
import org.openmrs.module.indiemroauthprovider.provider.dto.CalendarEventRequest;
import org.openmrs.module.indiemroauthprovider.provider.dto.CalendarEventResult;
import org.openmrs.module.indiemroauthprovider.provider.dto.MeetingRequest;
import org.openmrs.module.indiemroauthprovider.provider.dto.MeetingResult;
import org.openmrs.module.indiemroauthprovider.provider.registry.CalendarProviderRegistry;
import org.openmrs.module.indiemroauthprovider.provider.registry.MeetingProviderRegistry;
import org.openmrs.module.indiemroauthprovider.util.ModuleConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("indiemroauthprovider.TeleconsultService")
@Transactional
public class TeleconsultServiceImpl extends BaseOpenmrsService implements TeleconsultService {
	
	private static final long LINK_TTL_HOURS = 6;
	
	private static final long MEET_WINDOW_SECONDS = 3600;
	
	@Autowired
	@Qualifier("indiemroauthprovider.MeetingProviderRegistry")
	private MeetingProviderRegistry meetingRegistry;
	
	@Autowired
	@Qualifier("indiemroauthprovider.OAuthAccountDao")
	private OAuthAccountDao oauthAccountDao;
	
	@Autowired
	@Qualifier("indiemroauthprovider.ExternalResourceMappingDao")
	private ExternalResourceMappingDao externalResourceMappingDao;
	
	@Autowired
	@Qualifier("indiemroauthprovider.TeleconsultLinkDao")
	private TeleconsultLinkDao teleconsultLinkDao;
	
	@Autowired
	@Qualifier("indiemroauthprovider.CryptoService")
	private CryptoService crypto;
	
	@Autowired
	@Qualifier("indiemroauthprovider.ModuleConfig")
	private ModuleConfig moduleConfig;
	
	@Autowired
	@Qualifier("indiemroauthprovider.CalendarProviderRegistry")
	private CalendarProviderRegistry calendarRegistry;
	
	@Override
	public MintLinkResponse mintLink(Provider provider, MintLinkRequest request) throws Exception {
		if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
			throw new IllegalArgumentException("title is required and must be provided by the caller");
		}
		if (request.getResourceType() == null || request.getResourceType().trim().isEmpty()) {
			throw new IllegalArgumentException("resourceType is required");
		}
		if (request.getResourceUuid() == null || request.getResourceUuid().trim().isEmpty()) {
			throw new IllegalArgumentException("resourceUuid is required");
		}
		
		String oauthProviderCode = request.getOauthProviderCode() != null ? request.getOauthProviderCode() : "GOOGLE";
		OAuthAccount account = oauthAccountDao.findByProviderAndProviderCode(provider, oauthProviderCode);
		if (account == null) {
			throw new IllegalStateException("No connected " + oauthProviderCode
			        + " account for provider. Run connect-url first.");
		}
		
		String refreshToken = crypto.decrypt(account.getRefreshTokenEnc());
		Date now = new Date();
		Date end = new Date(now.getTime() + MEET_WINDOW_SECONDS * 1000L);
		
		MeetingProviderAdapter meetingProvider = meetingRegistry.require(oauthProviderCode);
		MeetingResult meeting = meetingProvider.createMeeting(account, refreshToken, new MeetingRequest(request.getTitle(),
		        now, end, "UTC"));
		
		ExternalResourceMapping calendarMapping = new ExternalResourceMapping();
		calendarMapping.setOauthAccount(account);
		calendarMapping.setProvider(provider);
		calendarMapping.setInternalResourceType(request.getResourceType());
		calendarMapping.setInternalResourceUuid(request.getResourceUuid());
		calendarMapping.setExternalResourceType(ExternalResourceMapping.EXTERNAL_CALENDAR_EVENT);
		calendarMapping.setExternalResourceId(meeting.getCalendarEventId() != null ? meeting.getCalendarEventId() : meeting
		        .getMeetingId());
		externalResourceMappingDao.save(calendarMapping);
		
		ExternalResourceMapping meetMapping = new ExternalResourceMapping();
		meetMapping.setOauthAccount(account);
		meetMapping.setProvider(provider);
		meetMapping.setInternalResourceType(request.getResourceType());
		meetMapping.setInternalResourceUuid(request.getResourceUuid());
		meetMapping.setExternalResourceType(ExternalResourceMapping.EXTERNAL_VIDEO_MEETING);
		meetMapping.setExternalResourceId(meeting.getMeetingId());
		externalResourceMappingDao.save(meetMapping);
		
		TeleconsultLink link = new TeleconsultLink();
		link.setOauthAccount(account);
		link.setExternalResourceMapping(meetMapping);
		link.setToken(crypto.randomToken(18));
		link.setMeetingUrl(meeting.getJoinUrl());
		link.setMeetingId(meeting.getMeetingId());
		link.setMeetingProvider(oauthProviderCode);
		link.setExpiresAt(addHours(new Date(), (int) LINK_TTL_HOURS));
		link.setVoided(false);
		teleconsultLinkDao.save(link);
		
		String baseUrl = moduleConfig.getPublicBaseUrl();
		String resolverUrl = baseUrl + "/openmrs/ws/rest/v1/teleconsult/link/" + link.getToken();
		return new MintLinkResponse(link.getToken(), link.getMeetingUrl(), resolverUrl);
	}
	
	@Override
	public ResolveResult resolveLink(String token) {
		TeleconsultLink link = teleconsultLinkDao.findByToken(token);
		if (link == null) {
			throw new TeleconsultException("Invalid or unknown link", 404);
		}
		
		if (link.getExpiresAt().before(new Date())) {
			teleconsultLinkDao.markStatus(token, TeleconsultLink.STATUS_EXPIRED);
			throw new TeleconsultException("This consultation link has expired", 410);
		}
		
		teleconsultLinkDao.markStatus(token, TeleconsultLink.STATUS_JOINED);
		
		OAuthAccount account = link.getOauthAccount();
		String providerDisplay = account.getDisplayName() != null ? account.getDisplayName() : account.getExternalEmail();
		
		return new ResolveResult(link.getMeetingUrl(), providerDisplay);
	}
	
	@Override
	public CreateCalendarEventResponse createCalendarEvent(Provider provider, CreateCalendarEventRequest request)
	        throws Exception {
		if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
			throw new IllegalArgumentException("title is required and must be provided by the caller");
		}
		if (request.getResourceType() == null || request.getResourceType().trim().isEmpty()) {
			throw new IllegalArgumentException("resourceType is required");
		}
		if (request.getResourceUuid() == null || request.getResourceUuid().trim().isEmpty()) {
			throw new IllegalArgumentException("resourceUuid is required");
		}
		
		String oauthProviderCode = request.getOauthProviderCode() != null ? request.getOauthProviderCode() : "GOOGLE";
		
		OAuthAccount account = oauthAccountDao.findByProviderAndProviderCode(provider, oauthProviderCode);
		if (account == null) {
			throw new IllegalStateException("No connected " + oauthProviderCode
			        + " account for provider. Run connect-url first.");
		}
		
		String refreshToken = crypto.decrypt(account.getRefreshTokenEnc());
		
		CalendarEventResult event = calendarRegistry.require(oauthProviderCode).createEvent(
		    account,
		    refreshToken,
		    new CalendarEventRequest(request.getTitle(), request.getDescription(), request.getStart(), request.getEnd(),
		            request.getTimeZone()));
		
		saveCalendarMapping(account, provider, request.getResourceType(), request.getResourceUuid(),
		    event.getExternalEventId());
		
		return new CreateCalendarEventResponse(request.getResourceUuid(), event.getExternalEventId(), event.getHtmlLink());
	}
	
	private void saveCalendarMapping(OAuthAccount account, Provider provider, String resourceType, String resourceUuid,
	        String externalEventId) {
		ExternalResourceMapping calendarMapping = new ExternalResourceMapping();
		calendarMapping.setOauthAccount(account);
		calendarMapping.setProvider(provider);
		calendarMapping.setInternalResourceType(resourceType);
		calendarMapping.setInternalResourceUuid(resourceUuid);
		calendarMapping.setExternalResourceType(ExternalResourceMapping.EXTERNAL_CALENDAR_EVENT);
		calendarMapping.setExternalResourceId(externalEventId);
		externalResourceMappingDao.save(calendarMapping);
	}
	
	private static Date addHours(Date date, int hours) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.HOUR_OF_DAY, hours);
		return cal.getTime();
	}
}
