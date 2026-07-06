package org.openmrs.module.indiemroauthprovider.web.controller;

import org.openmrs.module.indiemroauthprovider.api.TeleconsultService;
import org.openmrs.module.indiemroauthprovider.dto.ResolveResult;
import org.openmrs.module.indiemroauthprovider.exception.TeleconsultException;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.api.context.Context;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/rest/" + RestConstants.VERSION_1 + "/teleconsult")
public class PatientLinkController {
	
	@RequestMapping(value = "/link/{token}", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<String> resolve(@PathVariable("token") String token) {
		try {
			TeleconsultService service = Context.getService(TeleconsultService.class);
			ResolveResult result = service.resolveLink(token);
			return new ResponseEntity<String>(landingPage(result.getMeetUrl(), result.getProviderDisplay()), HttpStatus.OK);
		}
		catch (TeleconsultException e) {
			return new ResponseEntity<String>(e.getMessage(), HttpStatus.valueOf(e.getStatus()));
		}
		catch (Exception e) {
			return new ResponseEntity<String>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	private String landingPage(String meetUrl, String providerDisplay) {
		String providerLine = providerDisplay != null ? "<p>with " + escape(providerDisplay) + "</p>" : "";
		return "<!doctype html>\n"
		        + "<html>\n"
		        + "  <head>\n"
		        + "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\" />\n"
		        + "    <title>IndiEMR Teleconsultation</title>\n"
		        + "  </head>\n"
		        + "  <body style=\"font-family:sans-serif;text-align:center;padding:48px 20px;\">\n"
		        + "    <h2>Video Consultation</h2>\n"
		        + "    "
		        + providerLine
		        + "\n"
		        + "    <a href=\""
		        + escape(meetUrl)
		        + "\"\n"
		        + "       style=\"display:inline-block;margin-top:24px;padding:14px 28px;background:#0a7d49;color:#fff;border-radius:8px;text-decoration:none;font-size:18px;\">\n"
		        + "      Join video call\n" + "    </a>\n"
		        + "    <p style=\"margin-top:24px;color:#666;font-size:14px;\">Your doctor will admit you when ready.</p>\n"
		        + "  </body>\n" + "</html>";
	}
	
	private String escape(String s) {
		return s.replace("&", "&amp;").replace("<", "&lt;").replace("\"", "&quot;");
	}
}
