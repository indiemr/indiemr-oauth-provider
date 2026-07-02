package in.indiemr.teleconsult.web;

import in.indiemr.teleconsult.service.OAuthConnectService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OAuthCallbackController {

    private final OAuthConnectService oauthConnectService;

    public OAuthCallbackController(OAuthConnectService oauthConnectService) {
        this.oauthConnectService = oauthConnectService;
    }

    @GetMapping("/api/v1/teleconsult/connect/callback")
    public ResponseEntity<String> callback(@RequestParam(required = false) String code,
                                           @RequestParam(required = false) String state) {
        if (code == null || state == null) {
            return ResponseEntity.badRequest().body("Missing code or state");
        }
        try {
            var result = oauthConnectService.handleCallback(code, state);
            String email = result.email() != null ? result.email() : "your account";
            return ResponseEntity.ok(
                "Connected " + result.oauthProviderCode() + " for " + email + ". You can close this tab.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }
}