package org.openmrs.module.indiemroauthprovider.api.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openmrs.Provider;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.indiemroauthprovider.api.ExternalResourceService;
import org.openmrs.module.indiemroauthprovider.crypto.CryptoService;
import org.openmrs.module.indiemroauthprovider.dao.ExternalEventDao;
import org.openmrs.module.indiemroauthprovider.dao.MeetingDao;
import org.openmrs.module.indiemroauthprovider.dao.ResourceEventMappingDao;
import org.openmrs.module.indiemroauthprovider.model.ExternalEvent;
import org.openmrs.module.indiemroauthprovider.model.ExternalResourceType;
import org.openmrs.module.indiemroauthprovider.model.InternalResourceType;
import org.openmrs.module.indiemroauthprovider.model.OAuthAccount;
import org.openmrs.module.indiemroauthprovider.model.ResourceEventMapping;
import org.openmrs.module.indiemroauthprovider.provider.registry.CalendarProviderRegistry;
import org.openmrs.module.indiemroauthprovider.provider.registry.MeetingProviderRegistry;

public class ExternalResourceServiceImpl extends BaseOpenmrsService implements ExternalResourceService {
	
	private static final String VOID_REASON = "External resource cancelled";
	
	private ResourceEventMappingDao mappingDao;
	
	private ExternalEventDao externalEventDao;
	
	private CalendarProviderRegistry calendarRegistry;
	
	private MeetingProviderRegistry meetingRegistry;
	
	private MeetingDao meetingDao;
	
	private CryptoService crypto;
	
	public void setMappingDao(ResourceEventMappingDao mappingDao) {
		this.mappingDao = mappingDao;
	}
	
	public void setExternalEventDao(ExternalEventDao externalEventDao) {
		this.externalEventDao = externalEventDao;
	}
	
	public void setCalendarRegistry(CalendarProviderRegistry calendarRegistry) {
		this.calendarRegistry = calendarRegistry;
	}
	
	public void setMeetingRegistry(MeetingProviderRegistry meetingRegistry) {
		this.meetingRegistry = meetingRegistry;
	}
	
	public void setMeetingDao(MeetingDao meetingDao) {
		this.meetingDao = meetingDao;
	}
	
	public void setCrypto(CryptoService crypto) {
		this.crypto = crypto;
	}
	
	@Override
	public void voidInternalResources(InternalResourceType resourceType, String resourceUuid) throws Exception {
		voidInternalResourcesByType(resourceType.getCode(), resourceUuid);
	}
	
	@Override
	public void cancelAppointmentResources(String appointmentUuid) throws Exception {
		voidInternalResourcesByType(InternalResourceType.APPOINTMENT.getCode(), appointmentUuid);
	}
	
	private void voidInternalResourcesByType(String resourceType, String resourceUuid) throws Exception {
		List<ResourceEventMapping> mappings = mappingDao.findByInternalResource(resourceType, resourceUuid);
		deleteExternalResources(mappings);
		mappingDao.voidByInternalResource(resourceType, resourceUuid, VOID_REASON);
		voidEventsAndMeetings(mappings);
	}
	
	@Override
	public void cancelResources(Provider provider, String internalResourceType, String internalResourceUuid)
	        throws Exception {
		if (internalResourceType == null || internalResourceType.trim().isEmpty()) {
			throw new IllegalArgumentException("internalResourceType is required");
		}
		if (internalResourceUuid == null || internalResourceUuid.trim().isEmpty()) {
			throw new IllegalArgumentException("internalResourceUuid is required");
		}
		if (provider == null) {
			throw new IllegalArgumentException("provider is required");
		}
		List<ResourceEventMapping> mappings = mappingDao.findByProviderAndInternalResource(provider.getUuid(),
		    internalResourceType, internalResourceUuid);
		if (mappings == null || mappings.isEmpty()) {
			throw new IllegalStateException("No external resources found for " + internalResourceType + ":"
			        + internalResourceUuid);
		}
		deleteExternalResources(mappings);
		mappingDao.voidByProviderAndInternalResource(provider.getUuid(), internalResourceType, internalResourceUuid,
		    VOID_REASON);
		voidEventsAndMeetings(mappings);
	}
	
	private void deleteExternalResources(List<ResourceEventMapping> mappings) throws Exception {
		Set<Integer> deletedEventIds = new HashSet<Integer>();
		
		for (ResourceEventMapping mapping : mappings) {
			ExternalEvent event = mapping.getExternalEvent();
			if (!deletedEventIds.add(event.getId())) {
				continue;
			}
			OAuthAccount account = event.getOauthAccount();
			String providerCode = account.getOauthProvider().getCode();
			String refreshToken = crypto.decrypt(account.getRefreshTokenEnc());
			
			if (ExternalResourceType.CALENDAR_EVENT.getCode().equals(event.getExternalResourceType())) {
				calendarRegistry.require(providerCode).deleteEvent(account, refreshToken, event.getExternalEventId());
			} else if (ExternalResourceType.VIDEO_MEETING.getCode().equals(event.getExternalResourceType())) {
				meetingRegistry.require(providerCode).deleteMeeting(account, refreshToken, event.getExternalEventId());
			}
		}
	}
	
	private void voidEventsAndMeetings(List<ResourceEventMapping> mappings) {
		Set<Integer> processedEventIds = new HashSet<Integer>();
		for (ResourceEventMapping mapping : mappings) {
			ExternalEvent event = mapping.getExternalEvent();
			if (!processedEventIds.add(event.getId())) {
				continue;
			}
			externalEventDao.voidEvent(event, VOID_REASON);
			if (ExternalResourceType.VIDEO_MEETING.getCode().equals(event.getExternalResourceType())) {
				meetingDao.voidActiveByExternalEventId(event.getId(), VOID_REASON);
			}
		}
	}
}
