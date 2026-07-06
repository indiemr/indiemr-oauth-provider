package org.openmrs.module.indiemroauthprovider.api.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openmrs.Provider;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.indiemroauthprovider.api.ExternalResourceService;
import org.openmrs.module.indiemroauthprovider.crypto.CryptoService;
import org.openmrs.module.indiemroauthprovider.dao.ExternalResourceMappingDao;
import org.openmrs.module.indiemroauthprovider.dao.TeleconsultLinkDao;
import org.openmrs.module.indiemroauthprovider.model.ExternalResourceMapping;
import org.openmrs.module.indiemroauthprovider.model.ExternalResourceType;
import org.openmrs.module.indiemroauthprovider.model.InternalResourceType;
import org.openmrs.module.indiemroauthprovider.model.OAuthAccount;
import org.openmrs.module.indiemroauthprovider.provider.registry.CalendarProviderRegistry;
import org.openmrs.module.indiemroauthprovider.provider.registry.MeetingProviderRegistry;

public class ExternalResourceServiceImpl extends BaseOpenmrsService implements ExternalResourceService {
	
	private ExternalResourceMappingDao mappingDao;
	
	private CalendarProviderRegistry calendarRegistry;
	
	private MeetingProviderRegistry meetingRegistry;
	
	private TeleconsultLinkDao teleconsultLinkDao;
	
	private CryptoService crypto;
	
	public void setMappingDao(ExternalResourceMappingDao mappingDao) {
		this.mappingDao = mappingDao;
	}
	
	public void setCalendarRegistry(CalendarProviderRegistry calendarRegistry) {
		this.calendarRegistry = calendarRegistry;
	}
	
	public void setMeetingRegistry(MeetingProviderRegistry meetingRegistry) {
		this.meetingRegistry = meetingRegistry;
	}
	
	public void setTeleconsultLinkDao(TeleconsultLinkDao teleconsultLinkDao) {
		this.teleconsultLinkDao = teleconsultLinkDao;
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
		List<ExternalResourceMapping> mappings = mappingDao.findByInternalResource(resourceType, resourceUuid);
		deleteExternalResources(mappings);
		mappingDao.voidByInternalResource(resourceType, resourceUuid);
		voidActiveTeleconsultLinks(mappings);
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
		List<ExternalResourceMapping> mappings = mappingDao.findByProviderAndInternalResource(provider.getUuid(),
		    internalResourceType, internalResourceUuid);
		if (mappings == null || mappings.isEmpty()) {
			throw new IllegalStateException("No external resources found for " + internalResourceType + ":"
			        + internalResourceUuid);
		}
		deleteExternalResources(mappings);
		mappingDao.voidByProviderAndInternalResource(provider.getUuid(), internalResourceType, internalResourceUuid);
		voidActiveTeleconsultLinks(mappings);
	}
	
	private void deleteExternalResources(List<ExternalResourceMapping> mappings) throws Exception {
		Set<String> deletedExternalIds = new HashSet<String>();
		
		for (ExternalResourceMapping mapping : mappings) {
			String externalId = mapping.getExternalResourceId();
			if (!deletedExternalIds.add(externalId)) {
				continue;
			}
			OAuthAccount account = mapping.getOauthAccount();
			String providerCode = account.getOauthProvider().getCode();
			String refreshToken = crypto.decrypt(account.getRefreshTokenEnc());
			
			if (ExternalResourceType.CALENDAR_EVENT.getCode().equals(mapping.getExternalResourceType())) {
				calendarRegistry.require(providerCode).deleteEvent(account, refreshToken, mapping.getExternalResourceId());
			} else if (ExternalResourceType.VIDEO_MEETING.getCode().equals(mapping.getExternalResourceType())) {
				meetingRegistry.require(providerCode).deleteMeeting(account, refreshToken, mapping.getExternalResourceId());
			}
		}
	}
	
	private void voidActiveTeleconsultLinks(List<ExternalResourceMapping> mappings) {
		for (ExternalResourceMapping mapping : mappings) {
			if (ExternalResourceMapping.EXTERNAL_VIDEO_MEETING.equals(mapping.getExternalResourceType())) {
				teleconsultLinkDao.voidActiveByExternalResourceMappingId(mapping.getId());
			}
		}
	}
}
