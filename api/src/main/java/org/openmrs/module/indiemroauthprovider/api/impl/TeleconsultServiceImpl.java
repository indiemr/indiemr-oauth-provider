package org.openmrs.module.indiemroauthprovider.api.impl;

import java.util.Calendar;
import java.util.Date;

import org.openmrs.Provider;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.indiemroauthprovider.api.ExternalResourceService;
import org.openmrs.module.indiemroauthprovider.api.TeleconsultService;
import org.openmrs.module.indiemroauthprovider.crypto.CryptoService;
import org.openmrs.module.indiemroauthprovider.dao.ExternalEventDao;
import org.openmrs.module.indiemroauthprovider.dao.MeetingDao;
import org.openmrs.module.indiemroauthprovider.dao.OAuthAccountDao;
import org.openmrs.module.indiemroauthprovider.dao.ResourceEventMappingDao;
import org.openmrs.module.indiemroauthprovider.dto.CancelCalendarEventRequest;
import org.openmrs.module.indiemroauthprovider.dto.CancelCalendarEventResponse;
import org.openmrs.module.indiemroauthprovider.dto.CreateCalendarEventRequest;
import org.openmrs.module.indiemroauthprovider.dto.CreateCalendarEventResponse;
import org.openmrs.module.indiemroauthprovider.dto.ResolveResult;
import org.openmrs.module.indiemroauthprovider.dto.UpdateCalendarEventRequest;
import org.openmrs.module.indiemroauthprovider.exception.TeleconsultException;
import org.openmrs.module.indiemroauthprovider.model.ExternalEvent;
import org.openmrs.module.indiemroauthprovider.model.ExternalResourceType;
import org.openmrs.module.indiemroauthprovider.model.Meeting;
import org.openmrs.module.indiemroauthprovider.model.OAuthAccount;
import org.openmrs.module.indiemroauthprovider.model.ResourceEventMapping;
import org.openmrs.module.indiemroauthprovider.provider.MeetingProviderAdapter;
import org.openmrs.module.indiemroauthprovider.provider.dto.CalendarEventRequest;
import org.openmrs.module.indiemroauthprovider.provider.dto.CalendarEventResult;
import org.openmrs.module.indiemroauthprovider.provider.dto.CalendarEventUpdate;
import org.openmrs.module.indiemroauthprovider.provider.dto.MeetingRequest;
import org.openmrs.module.indiemroauthprovider.provider.dto.MeetingResult;
import org.openmrs.module.indiemroauthprovider.provider.registry.CalendarProviderRegistry;
import org.openmrs.module.indiemroauthprovider.provider.registry.MeetingProviderRegistry;
import org.openmrs.module.indiemroauthprovider.util.ModuleConfigLoader;

public class TeleconsultServiceImpl extends BaseOpenmrsService implements TeleconsultService {
	
	private static final long LINK_TTL_HOURS = 6;
	
	private MeetingProviderRegistry meetingRegistry;
	
	private OAuthAccountDao oauthAccountDao;
	
	private ExternalEventDao externalEventDao;
	
	private ResourceEventMappingDao resourceEventMappingDao;
	
	private MeetingDao meetingDao;
	
	private CryptoService crypto;
	
	private ModuleConfigLoader moduleConfigLoader;
	
	private CalendarProviderRegistry calendarRegistry;
	
	private ExternalResourceService externalResourceService;
	
	public void setMeetingRegistry(MeetingProviderRegistry meetingRegistry) {
		this.meetingRegistry = meetingRegistry;
	}
	
	public void setOauthAccountDao(OAuthAccountDao oauthAccountDao) {
		this.oauthAccountDao = oauthAccountDao;
	}
	
	public void setExternalEventDao(ExternalEventDao externalEventDao) {
		this.externalEventDao = externalEventDao;
	}
	
	public void setResourceEventMappingDao(ResourceEventMappingDao resourceEventMappingDao) {
		this.resourceEventMappingDao = resourceEventMappingDao;
	}
	
	public void setMeetingDao(MeetingDao meetingDao) {
		this.meetingDao = meetingDao;
	}
	
	public void setCrypto(CryptoService crypto) {
		this.crypto = crypto;
	}
	
	public void setModuleConfigLoader(ModuleConfigLoader moduleConfigLoader) {
		this.moduleConfigLoader = moduleConfigLoader;
	}
	
	public void setCalendarRegistry(CalendarProviderRegistry calendarRegistry) {
		this.calendarRegistry = calendarRegistry;
	}
	
	public void setExternalResourceService(ExternalResourceService externalResourceService) {
		this.externalResourceService = externalResourceService;
	}
	
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
			saveEventAndMapping(account, request.getResourceType(), request.getResourceUuid(),
			    ExternalResourceType.CALENDAR_EVENT.getCode(), calendarEventId);
			
			ExternalEvent meetEvent = saveEventAndMapping(account, request.getResourceType(), request.getResourceUuid(),
			    ExternalResourceType.VIDEO_MEETING.getCode(), meeting.getMeetingId());
			
			response.setExternalEventId(calendarEventId);
			response.setHtmlLink(meeting.getHtmlLink());
			response.setMeetingUrl(meeting.getJoinUrl());
			
			if (request.isMintJoinLink()) {
				Meeting link = mintMeeting(meetEvent, meeting.getJoinUrl(), request.getEnd());
				response.setJoinToken(link.getToken());
				response.setResolverUrl(buildResolverUrl(link.getToken()));
			}
		} else {
			CalendarEventResult event = calendarRegistry.require(oauthProviderCode).createEvent(account, refreshToken,
			    calReq);
			saveEventAndMapping(account, request.getResourceType(), request.getResourceUuid(),
			    ExternalResourceType.CALENDAR_EVENT.getCode(), event.getExternalEventId());
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
		
		ExternalEvent calendarEvent = resourceEventMappingDao.findActiveEventByProviderAndInternalResource(
		    provider.getUuid(), request.getResourceType(), request.getResourceUuid(),
		    ExternalResourceType.CALENDAR_EVENT.getCode());
		if (calendarEvent == null) {
			throw new IllegalStateException("No calendar event found for " + request.getResourceType() + ":"
			        + request.getResourceUuid());
		}
		
		String refreshToken = crypto.decrypt(account.getRefreshTokenEnc());
		CalendarEventUpdate update = toCalendarEventUpdate(request);
		CalendarEventResult updated = calendarRegistry.require(oauthProviderCode).updateEvent(account, refreshToken,
		    calendarEvent.getExternalEventId(), update);
		
		ExternalEvent meetEvent = resourceEventMappingDao.findActiveEventByProviderAndInternalResource(provider.getUuid(),
		    request.getResourceType(), request.getResourceUuid(), ExternalResourceType.VIDEO_MEETING.getCode());
		
		if (request.getEnd() != null && meetEvent != null) {
			Meeting meeting = meetingDao.findActiveByExternalEventId(meetEvent.getId());
			if (meeting != null) {
				meetingDao
				        .extendExpiryByExternalEventId(meetEvent.getId(), addHours(request.getEnd(), (int) LINK_TTL_HOURS));
			}
		}
		
		CreateCalendarEventResponse response = new CreateCalendarEventResponse();
		response.setResourceUuid(request.getResourceUuid());
		response.setExternalEventId(updated.getExternalEventId());
		response.setHtmlLink(updated.getHtmlLink());
		return response;
	}
	
	@Override
	public CancelCalendarEventResponse cancelCalendarEvent(Provider provider, CancelCalendarEventRequest request)
	        throws Exception {
		validateCancelRequest(request);
		
		externalResourceService.cancelResources(provider, request.getResourceType(), request.getResourceUuid());
		
		return new CancelCalendarEventResponse(request.getResourceType(), request.getResourceUuid());
		
	}
	
	private void validateCancelRequest(CancelCalendarEventRequest request) {
		if (request.getResourceType() == null || request.getResourceType().trim().isEmpty()) {
			throw new IllegalArgumentException("resourceType is required");
		}
		if (request.getResourceUuid() == null || request.getResourceUuid().trim().isEmpty()) {
			throw new IllegalArgumentException("resourceUuid is required");
		}
	}
	
	@Override
	public ResolveResult resolveLink(String token) {
		Meeting meeting = meetingDao.findByToken(token);
		if (meeting == null) {
			throw new TeleconsultException("Invalid or unknown link", 404);
		}
		
		if (meeting.getExpiresAt().before(new Date())) {
			meetingDao.markStatus(token, Meeting.STATUS_EXPIRED);
			throw new TeleconsultException("This consultation link has expired", 410);
		}
		
		meetingDao.markStatus(token, Meeting.STATUS_JOINED);
		
		OAuthAccount account = meeting.getExternalEvent().getOauthAccount();
		String providerDisplay = account.getDisplayName() != null ? account.getDisplayName() : account.getExternalEmail();
		
		return new ResolveResult(meeting.getMeetingUrl(), providerDisplay);
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
	
	private CalendarEventUpdate toCalendarEventUpdate(UpdateCalendarEventRequest request) {
		CalendarEventUpdate update = new CalendarEventUpdate();
		update.setTitle(request.getTitle());
		update.setDescription(request.getDescription());
		update.setStart(request.getStart());
		update.setEnd(request.getEnd());
		update.setTimeZone(request.getTimeZone());
		return update;
	}
	
	private ExternalEvent saveEventAndMapping(OAuthAccount account, String resourceType, String resourceUuid,
	        String externalResourceType, String externalEventId) {
		ExternalEvent event = new ExternalEvent();
		event.setOauthAccount(account);
		event.setExternalResourceType(externalResourceType);
		event.setExternalEventId(externalEventId);
		externalEventDao.save(event);
		
		ResourceEventMapping mapping = new ResourceEventMapping();
		mapping.setExternalEvent(event);
		mapping.setInternalResourceType(resourceType);
		mapping.setInternalResourceUuid(resourceUuid);
		resourceEventMappingDao.save(mapping);
		return event;
	}
	
	private Meeting mintMeeting(ExternalEvent meetEvent, String meetingUrl, Date eventEnd) {
		Meeting meeting = new Meeting();
		meeting.setExternalEvent(meetEvent);
		meeting.setToken(crypto.randomToken(18));
		meeting.setMeetingUrl(meetingUrl);
		meeting.setExpiresAt(addHours(eventEnd, (int) LINK_TTL_HOURS));
		meetingDao.save(meeting);
		return meeting;
	}
	
	private String buildResolverUrl(String token) {
		return moduleConfigLoader.getPublicBaseUrl() + "/openmrs/ws/rest/v1/teleconsult/link/" + token;
	}
	
	private static Date addHours(Date date, int hours) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.HOUR_OF_DAY, hours);
		return cal.getTime();
	}
}
