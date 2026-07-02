package in.indiemr.teleconsult.web;

import in.indiemr.teleconsult.exception.TeleconsultException;
import in.indiemr.teleconsult.service.TeleconsultService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PatientLinkController {

    private final TeleconsultService teleconsultService;

    public PatientLinkController(TeleconsultService teleconsultService) {
        this.teleconsultService = teleconsultService;
    }

    @GetMapping("/c/{token}")
    public ResponseEntity<String> resolve(@PathVariable String token) {
        try {
            var result = teleconsultService.resolveLink(token);
            return ResponseEntity.ok(landingPage(result.meetUrl(), result.providerDisplay()));
        } catch (TeleconsultException e) {
            return ResponseEntity.status(e.getStatus()).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    private String landingPage(String meetUrl, String providerDisplay) {
        String providerLine = providerDisplay != null
            ? "<p>with " + escape(providerDisplay) + "</p>" : "";
        return """
            <!doctype html>
            <html>
              <head>
                <meta name="viewport" content="width=device-width, initial-scale=1" />
                <title>IndiEMR Teleconsultation</title>
              </head>
              <body style="font-family:sans-serif;text-align:center;padding:48px 20px;">
                <h2>Video Consultation</h2>
                %s
                <a href="%s"
                   style="display:inline-block;margin-top:24px;padding:14px 28px;background:#0a7d49;color:#fff;border-radius:8px;text-decoration:none;font-size:18px;">
                  Join video call
                </a>
                <p style="margin-top:24px;color:#666;font-size:14px;">Your doctor will admit you when ready.</p>
              </body>
            </html>
            """.formatted(providerLine, escape(meetUrl));
    }

    private String escape(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace("\"", "&quot;");
    }
}