package org.openmrs.module.indiemroauthprovider.api.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.indiemroauthprovider.api.ExternalResourceService;
import org.openmrs.module.indiemroauthprovider.crypto.CryptoService;
import org.openmrs.module.indiemroauthprovider.dao.ExternalResourceMappingDao;
import org.openmrs.module.indiemroauthprovider.dao.TeleconsultLinkDao;
import org.openmrs.module.indiemroauthprovider.model.ExternalResourceMapping;
import org.openmrs.module.indiemroauthprovider.model.OAuthAccount;
import org.openmrs.module.indiemroauthprovider.provider.registry.CalendarProviderRegistry;
import org.openmrs.module.indiemroauthprovider.provider.registry.MeetingProviderRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("indiemroauthprovider.ExternalResourceService")
@Transactional
public class ExternalResourceServiceImpl extends BaseOpenmrsService implements ExternalResourceService {
	
	@Autowired
	@Qualifier("indiemroauthprovider.ExternalResourceMappingDao")
	private ExternalResourceMappingDao mappingDao;
	
	@Autowired
	@Qualifier("indiemroauthprovider.CalendarProviderRegistry")
	private CalendarProviderRegistry calendarRegistry;
	
	@Autowired
	@Qualifier("indiemroauthprovider.MeetingProviderRegistry")
	private MeetingProviderRegistry meetingRegistry;
	
	@Autowired
	@Qualifier("indiemroauthprovider.TeleconsultLinkDao")
	private TeleconsultLinkDao teleconsultLinkDao;
	
	@Autowired
	@Qualifier("indiemroauthprovider.CryptoService")
	private CryptoService crypto;
	
	@Override
	public void cancelAppointmentResources(String appointmentUuid) throws Exception {
		List<ExternalResourceMapping> mappings = mappingDao.findByInternalResource(
		    ExternalResourceMapping.INTERNAL_APPOINTMENT, appointmentUuid);
		
		Set<String> deletedExternalIds = new HashSet<String>();
		
		for (ExternalResourceMapping mapping : mappings) {
			String externalId = mapping.getExternalResourceId();
			if (!deletedExternalIds.add(externalId)) {
				continue;
			}
			OAuthAccount account = mapping.getOauthAccount();
			String providerCode = account.getOauthProvider().getCode();
			String refreshToken = crypto.decrypt(account.getRefreshTokenEnc());
			
			if (ExternalResourceMapping.EXTERNAL_CALENDAR_EVENT.equals(mapping.getExternalResourceType())) {
				calendarRegistry.require(providerCode).deleteEvent(account, refreshToken, mapping.getExternalResourceId());
			} else if (ExternalResourceMapping.EXTERNAL_VIDEO_MEETING.equals(mapping.getExternalResourceType())) {
				meetingRegistry.require(providerCode).deleteMeeting(account, refreshToken, mapping.getExternalResourceId());
			}
		}
		
		mappingDao.voidByInternalResource(ExternalResourceMapping.INTERNAL_APPOINTMENT, appointmentUuid);
		teleconsultLinkDao.voidByAppointmentUuid(appointmentUuid);
	}
}
