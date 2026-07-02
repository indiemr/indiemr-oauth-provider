package in.indiemr.teleconsult.web;

import in.indiemr.teleconsult.dto.MintLinkRequest;
import in.indiemr.teleconsult.service.ExternalResourceService;
import in.indiemr.teleconsult.service.OAuthConnectService;
import in.indiemr.teleconsult.service.TeleconsultService;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import in.indiemr.teleconsult.dto.CreateCalendarEventRequest;


@RestController
@RequestMapping("/api/v1/teleconsult")
public class TeleconsultAdminController {

    private final OAuthConnectService oauthConnectService;
    private final TeleconsultService teleconsultService;
    private final ExternalResourceService externalResourceService;


    public TeleconsultAdminController(OAuthConnectService oauthConnectService,
                                      TeleconsultService teleconsultService,
                                      ExternalResourceService externalResourceService) {
        this.oauthConnectService = oauthConnectService;
        this.teleconsultService = teleconsultService;
        this.externalResourceService = externalResourceService;
    }

    @GetMapping("/connect-url")
    public ResponseEntity<Map<String, String>> connectUrl(
            @RequestParam String providerUuid,
            @RequestParam String providerDisplay,
            @RequestParam(defaultValue = "GOOGLE") String oauthProvider) throws Exception {
        return ResponseEntity.ok(Map.of(
            "url", oauthConnectService.buildConnectUrl(providerUuid, providerDisplay, oauthProvider)));
    }

    @GetMapping("/check-token")
    public ResponseEntity<?> checkToken(
            @RequestParam String providerUuid,
            @RequestParam(defaultValue = "GOOGLE") String oauthProvider) {
        return oauthConnectService.getAccountStatus(providerUuid, oauthProvider)
            .<ResponseEntity<?>>map(ResponseEntity::ok)
            .orElse(ResponseEntity.ok(Map.of(
                "status", "NO TOKEN stored yet. Run connect-url first."
            )));
    }

    @PostMapping("/mint")
    public ResponseEntity<?> mint(@RequestBody MintLinkRequest req) throws Exception {
        return ResponseEntity.ok(teleconsultService.mintLink(req));
    }

    @DeleteMapping("/appointments/{appointmentUuid}/resources")
    public ResponseEntity<Map<String, String>> cancelAppointment(
            @PathVariable String appointmentUuid) throws Exception {
        externalResourceService.cancelAppointmentResources(appointmentUuid);
        return ResponseEntity.ok(Map.of(
            "status", "CANCELLED",
            "appointmentUuid", appointmentUuid
        ));
    }

    @PostMapping("/events")
    public ResponseEntity<?> createEvent(@RequestBody CreateCalendarEventRequest req) throws Exception {
        return ResponseEntity.ok(teleconsultService.createCalendarEvent(req));
    }
}