package org.openmrs.module.indiemroauthprovider.api.impl;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.openmrs.Provider;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.indiemroauthprovider.api.TeleconsultService;
import org.openmrs.module.indiemroauthprovider.crypto.CryptoService;
import org.openmrs.module.indiemroauthprovider.dao.ExternalResourceMappingDao;
import org.openmrs.module.indiemroauthprovider.dao.OAuthAccountDao;
import org.openmrs.module.indiemroauthprovider.dao.TeleconsultLinkDao;
import org.openmrs.module.indiemroauthprovider.dto.CreateCalendarEventRequest;
import org.openmrs.module.indiemroauthprovider.dto.CreateCalendarEventResponse;
import org.openmrs.module.indiemroauthprovider.dto.ResolveResult;
import org.openmrs.module.indiemroauthprovider.dto.UpdateCalendarEventRequest;
import org.openmrs.module.indiemroauthprovider.exception.TeleconsultException;
import org.openmrs.module.indiemroauthprovider.model.ExternalResourceMapping;
import org.openmrs.module.indiemroauthprovider.model.OAuthAccount;
import org.openmrs.module.indiemroauthprovider.model.TeleconsultLink;
import org.openmrs.module.indiemroauthprovider.provider.MeetingProviderAdapter;
import org.openmrs.module.indiemroauthprovider.provider.dto.CalendarEventRequest;
import org.openmrs.module.indiemroauthprovider.provider.dto.CalendarEventResult;
import org.openmrs.module.indiemroauthprovider.provider.dto.CalendarEventUpdate;
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
	public CreateCalendarEventResponse createCalendarEvent(Provider provider, CreateCalendarEventRequest request)
	        throws Exception {
		validateRequest(request);
		
		String oauthProviderCode = request.getOauthProviderCode() != null ? request.getOauthProviderCode() : "GOOGLE";
		OAuthAccount account = oauthAccountDao.findByProviderAndProviderCode(provider, oauthProviderCode);
		if (account == null) {
			throw new IllegalStateException("No connected " + oauthProviderCode
			        + " account for provider. Run connect-url first.");
		}
		
		String refreshToken = crypto.decrypt(account.getRefreshTokenEnc());
		CalendarEventRequest calReq = new CalendarEventRequest(request.getTitle(), request.getDescription(),
		        request.getStart(), request.getEnd(), request.getTimeZone());
		
		CreateCalendarEventResponse response = new CreateCalendarEventResponse();
		response.setResourceUuid(request.getResourceUuid());
		
		if (request.isCreateMeet()) {
			MeetingProviderAdapter meetingProvider = meetingRegistry.require(oauthProviderCode);
			MeetingResult meeting = meetingProvider.createMeeting(account, refreshToken,
			    new MeetingRequest(request.getTitle(), request.getStart(), request.getEnd(), request.getTimeZone()));
			
			String calendarEventId = meeting.getCalendarEventId() != null ? meeting.getCalendarEventId() : meeting
			        .getMeetingId();
			saveCalendarMapping(account, provider, request.getResourceType(), request.getResourceUuid(), calendarEventId);
			
			ExternalResourceMapping meetMapping = saveMeetingMapping(account, provider, request.getResourceType(),
			    request.getResourceUuid(), meeting.getMeetingId());
			
			response.setExternalEventId(calendarEventId);
			response.setHtmlLink(meeting.getHtmlLink());
			response.setMeetingUrl(meeting.getJoinUrl());
			
			if (request.isMintJoinLink()) {
				TeleconsultLink link = mintTeleconsultLink(account, meetMapping, meeting, oauthProviderCode,
				    request.getEnd());
				response.setJoinToken(link.getToken());
				response.setResolverUrl(buildResolverUrl(link.getToken()));
			}
		} else {
			CalendarEventResult event = calendarRegistry.require(oauthProviderCode).createEvent(account, refreshToken,
			    calReq);
			saveCalendarMapping(account, provider, request.getResourceType(), request.getResourceUuid(),
			    event.getExternalEventId());
			response.setExternalEventId(event.getExternalEventId());
			response.setHtmlLink(event.getHtmlLink());
		}
		
		return response;
	}
	
	@Override
	public CreateCalendarEventResponse updateCalendarEvent(Provider provider, UpdateCalendarEventRequest request)
	        throws Exception {
		validateUpdateRequest(request);
		
		String oauthProviderCode = request.getOauthProviderCode() != null ? request.getOauthProviderCode() : "GOOGLE";
		OAuthAccount account = oauthAccountDao.findByProviderAndProviderCode(provider, oauthProviderCode);
		if (account == null) {
			throw new IllegalStateException("No connected " + oauthProviderCode
			        + " account for provider. Run connect-url first.");
		}
		
		ExternalResourceMapping calendarMapping = findActiveCalendarMapping(provider, request.getResourceType(),
		    request.getResourceUuid());
		if (calendarMapping == null) {
			throw new IllegalStateException("No calendar event found for " + request.getResourceType() + ":"
			        + request.getResourceUuid());
		}
		
		String refreshToken = crypto.decrypt(account.getRefreshTokenEnc());
		CalendarEventUpdate update = toCalendarEventUpdate(request);
		CalendarEventResult updated = calendarRegistry.require(oauthProviderCode).updateEvent(account, refreshToken,
		    calendarMapping.getExternalResourceId(), update);
		
		if (request.getEnd() != null) {
			teleconsultLinkDao.extendLinkExpiryForResource(request.getResourceType(), request.getResourceUuid(),
			    addHours(request.getEnd(), (int) LINK_TTL_HOURS));
		}
		
		CreateCalendarEventResponse response = new CreateCalendarEventResponse();
		response.setResourceUuid(request.getResourceUuid());
		response.setExternalEventId(updated.getExternalEventId());
		response.setHtmlLink(updated.getHtmlLink());
		return response;
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
	
	private void validateRequest(CreateCalendarEventRequest request) {
		if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
			throw new IllegalArgumentException("title is required and must be provided by the caller");
		}
		if (request.getResourceType() == null || request.getResourceType().trim().isEmpty()) {
			throw new IllegalArgumentException("resourceType is required");
		}
		if (request.getResourceUuid() == null || request.getResourceUuid().trim().isEmpty()) {
			throw new IllegalArgumentException("resourceUuid is required");
		}
		if (request.getStart() == null) {
			throw new IllegalArgumentException("start is required");
		}
		if (request.getEnd() == null) {
			throw new IllegalArgumentException("end is required");
		}
		if (request.isMintJoinLink() && !request.isCreateMeet()) {
			throw new IllegalArgumentException("mintJoinLink requires createMeet=true");
		}
	}
	
	private void validateUpdateRequest(UpdateCalendarEventRequest request) {
		if (request.getResourceType() == null || request.getResourceType().trim().isEmpty()) {
			throw new IllegalArgumentException("resourceType is required");
		}
		if (request.getResourceUuid() == null || request.getResourceUuid().trim().isEmpty()) {
			throw new IllegalArgumentException("resourceUuid is required");
		}
		boolean hasUpdate = request.getTitle() != null || request.getDescription() != null || request.getStart() != null
		        || request.getEnd() != null || request.getTimeZone() != null;
		if (!hasUpdate) {
			throw new IllegalArgumentException("at least one of title, description, start, end, or timeZone is required");
		}
	}
	
	private ExternalResourceMapping findActiveCalendarMapping(Provider provider, String resourceType, String resourceUuid) {
		List<ExternalResourceMapping> mappings = externalResourceMappingDao.findByProviderAndInternalResource(
		    provider.getUuid(), resourceType, resourceUuid);
		for (ExternalResourceMapping mapping : mappings) {
			if (!mapping.isVoided()
			        && ExternalResourceMapping.EXTERNAL_CALENDAR_EVENT.equals(mapping.getExternalResourceType())) {
				return mapping;
			}
		}
		return null;
	}
	
	private CalendarEventUpdate toCalendarEventUpdate(UpdateCalendarEventRequest request) {
		CalendarEventUpdate update = new CalendarEventUpdate();
		update.setTitle(request.getTitle());
		update.setDescription(request.getDescription());
		update.setStart(request.getStart());
		update.setEnd(request.getEnd());
		update.setTimeZone(request.getTimeZone());
		return update;
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
	
	private ExternalResourceMapping saveMeetingMapping(OAuthAccount account, Provider provider, String resourceType,
	        String resourceUuid, String meetingId) {
		ExternalResourceMapping meetMapping = new ExternalResourceMapping();
		meetMapping.setOauthAccount(account);
		meetMapping.setProvider(provider);
		meetMapping.setInternalResourceType(resourceType);
		meetMapping.setInternalResourceUuid(resourceUuid);
		meetMapping.setExternalResourceType(ExternalResourceMapping.EXTERNAL_VIDEO_MEETING);
		meetMapping.setExternalResourceId(meetingId);
		externalResourceMappingDao.save(meetMapping);
		return meetMapping;
	}
	
	private TeleconsultLink mintTeleconsultLink(OAuthAccount account, ExternalResourceMapping meetMapping,
	        MeetingResult meeting, String oauthProviderCode, Date eventEnd) {
		TeleconsultLink link = new TeleconsultLink();
		link.setOauthAccount(account);
		link.setExternalResourceMapping(meetMapping);
		link.setToken(crypto.randomToken(18));
		link.setMeetingUrl(meeting.getJoinUrl());
		link.setMeetingId(meeting.getMeetingId());
		link.setMeetingProvider(oauthProviderCode);
		link.setExpiresAt(addHours(eventEnd, (int) LINK_TTL_HOURS));
		link.setVoided(false);
		teleconsultLinkDao.save(link);
		return link;
	}
	
	private String buildResolverUrl(String token) {
		return moduleConfig.getPublicBaseUrl() + "/openmrs/ws/rest/v1/teleconsult/link/" + token;
	}
	
	private static Date addHours(Date date, int hours) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.HOUR_OF_DAY, hours);
		return cal.getTime();
	}
}
