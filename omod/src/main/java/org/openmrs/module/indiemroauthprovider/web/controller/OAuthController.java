package org.openmrs.module.indiemroauthprovider.web.controller;

import java.util.HashMap;
import java.util.Map;

import org.openmrs.Provider;
import org.openmrs.api.context.Context;
import org.openmrs.module.indiemroauthprovider.api.OAuthConnectService;
import org.openmrs.module.indiemroauthprovider.dto.AccountStatusResponse;
import org.openmrs.module.indiemroauthprovider.dto.ConnectResult;
import org.openmrs.module.indiemroauthprovider.model.OAuthVendorCode;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/rest/" + RestConstants.VERSION_1 + "/oauth")
public class OAuthController extends BaseTeleconsultController {
	
	@RequestMapping(value = "/connect-url", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<?> connectUrl(@RequestParam(value = "providerDisplay", required = false) String providerDisplay,
	        @RequestParam(value = "oauthProvider", defaultValue = "GOOGLE") String oauthProvider) {
		ResponseEntity<Map<String, Object>> authError = requireAuthenticatedProvider();
		if (authError != null) {
			return authError;
		}
		try {
			Provider provider = getAuthenticatedProvider();
			String display = providerDisplay != null ? providerDisplay : provider.getName();
			OAuthConnectService service = Context.getService(OAuthConnectService.class);
			Map<String, String> result = new HashMap<String, String>();
			result.put("url", service.buildConnectUrl(provider, display, OAuthVendorCode.fromCode(oauthProvider)));
			return new ResponseEntity<Map<String, String>>(result, HttpStatus.OK);
		}
		catch (Exception e) {
			return errorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
		}
	}
	
	@RequestMapping(value = "/check-token", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<?> checkToken(@RequestParam(value = "oauthProvider", defaultValue = "GOOGLE") String oauthProvider) {
		ResponseEntity<Map<String, Object>> authError = requireAuthenticatedProvider();
		if (authError != null) {
			return authError;
		}
		OAuthConnectService service = Context.getService(OAuthConnectService.class);
		AccountStatusResponse status = service.getAccountStatus(getAuthenticatedProvider(),
		    OAuthVendorCode.fromCode(oauthProvider));
		if (status != null) {
			return new ResponseEntity<AccountStatusResponse>(status, HttpStatus.OK);
		}
		Map<String, String> result = new HashMap<String, String>();
		result.put("status", "NO TOKEN stored yet. Run connect-url first.");
		return new ResponseEntity<Map<String, String>>(result, HttpStatus.OK);
	}
	
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
