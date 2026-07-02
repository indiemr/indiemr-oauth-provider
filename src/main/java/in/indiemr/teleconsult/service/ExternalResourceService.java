package in.indiemr.teleconsult.service;

import in.indiemr.teleconsult.crypto.CryptoService;
import in.indiemr.teleconsult.dao.ExternalResourceMappingDao;
import in.indiemr.teleconsult.dao.TeleconsultLinkDao;
import in.indiemr.teleconsult.model.ExternalResourceMapping;
import in.indiemr.teleconsult.model.OAuthAccount;
import in.indiemr.teleconsult.provider.registry.CalendarProviderRegistry;
import in.indiemr.teleconsult.provider.registry.MeetingProviderRegistry;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

@Service
public class ExternalResourceService {

    private final ExternalResourceMappingDao mappingDao;
    private final CalendarProviderRegistry calendarRegistry;
    private final MeetingProviderRegistry meetingRegistry;
    private final TeleconsultLinkDao teleconsultLinkDao;
    private final CryptoService crypto;

    public ExternalResourceService(ExternalResourceMappingDao mappingDao,
                                   CalendarProviderRegistry calendarRegistry,
                                   MeetingProviderRegistry meetingRegistry,
                                   CryptoService crypto,
                                   TeleconsultLinkDao teleconsultLinkDao) {
        this.mappingDao = mappingDao;
        this.calendarRegistry = calendarRegistry;
        this.meetingRegistry = meetingRegistry;
        this.crypto = crypto;
        this.teleconsultLinkDao = teleconsultLinkDao;
    }

    public void cancelAppointmentResources(String appointmentUuid) throws Exception {
        List<ExternalResourceMapping> mappings = mappingDao.findByInternalResource(
            ExternalResourceMapping.INTERNAL_APPOINTMENT,
            appointmentUuid
        );


        Set<String> deletedExternalIds = new HashSet<>();

        for (ExternalResourceMapping mapping : mappings) {
            String externalId = mapping.getExternalResourceId();
            if (!deletedExternalIds.add(externalId)) {
                continue; // same Google event — already deleted
            }
            OAuthAccount account = mapping.getOauthAccount();
            String providerCode = account.getOauthProvider().getCode();
            String refreshToken = crypto.decrypt(account.getRefreshTokenEnc());

            switch (mapping.getExternalResourceType()) {
                case ExternalResourceMapping.EXTERNAL_CALENDAR_EVENT ->
                    calendarRegistry.require(providerCode)
                        .deleteEvent(account, refreshToken, mapping.getExternalResourceId());
                case ExternalResourceMapping.EXTERNAL_VIDEO_MEETING ->
                    meetingRegistry.require(providerCode)
                        .deleteMeeting(account, refreshToken, mapping.getExternalResourceId());
            }
        }

        mappingDao.voidByInternalResource(
            ExternalResourceMapping.INTERNAL_APPOINTMENT,
            appointmentUuid
        );
        teleconsultLinkDao.voidByAppointmentUuid(appointmentUuid);
    }
}