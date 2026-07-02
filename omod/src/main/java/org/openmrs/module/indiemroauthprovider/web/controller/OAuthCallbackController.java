package org.openmrs.module.indiemroauthprovider.web.controller;

import org.openmrs.module.indiemroauthprovider.api.OAuthConnectService;
import org.openmrs.module.indiemroauthprovider.dto.ConnectResult;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.api.context.Context;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/rest/" + RestConstants.VERSION_1 + "/teleconsult")
public class OAuthCallbackController {
	
	@RequestMapping(value = "/connect/callback", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<String> callback(@RequestParam(value = "code", required = false) String code,
	        @RequestParam(value = "state", required = false) String state) {
		if (code == null || state == null) {
			return new ResponseEntity<String>("Missing code or state", HttpStatus.BAD_REQUEST);
		}
		try {
			OAuthConnectService service = Context.getService(OAuthConnectService.class);
			ConnectResult result = service.handleCallback(code, state);
			String email = result.getEmail() != null ? result.getEmail() : "your account";
			return new ResponseEntity<String>("Connected " + result.getOauthProviderCode() + " for " + email
			        + ". You can close this tab.", HttpStatus.OK);
		}
		catch (Exception e) {
			return new ResponseEntity<String>("Error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
