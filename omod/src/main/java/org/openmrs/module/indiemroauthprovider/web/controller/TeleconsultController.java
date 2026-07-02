package org.openmrs.module.indiemroauthprovider.web.controller;

import java.util.HashMap;
import java.util.Map;

import org.openmrs.Provider;
import org.openmrs.module.indiemroauthprovider.api.ExternalResourceService;
import org.openmrs.module.indiemroauthprovider.api.OAuthConnectService;
import org.openmrs.module.indiemroauthprovider.api.TeleconsultService;
import org.openmrs.module.indiemroauthprovider.dto.AccountStatusResponse;
import org.openmrs.module.indiemroauthprovider.dto.CreateCalendarEventRequest;
import org.openmrs.module.indiemroauthprovider.dto.MintLinkRequest;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.api.context.Context;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/rest/" + RestConstants.VERSION_1 + "/teleconsult")
public class TeleconsultController extends BaseTeleconsultController {
	
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
			result.put("url", service.buildConnectUrl(provider, display, oauthProvider));
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
		AccountStatusResponse status = service.getAccountStatus(getAuthenticatedProvider(), oauthProvider);
		if (status != null) {
			return new ResponseEntity<AccountStatusResponse>(status, HttpStatus.OK);
		}
		Map<String, String> result = new HashMap<String, String>();
		result.put("status", "NO TOKEN stored yet. Run connect-url first.");
		return new ResponseEntity<Map<String, String>>(result, HttpStatus.OK);
	}
	
	@RequestMapping(value = "/mint", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<?> mint(@RequestBody MintLinkRequest req) {
		ResponseEntity<Map<String, Object>> authError = requireAuthenticatedProvider();
		if (authError != null) {
			return authError;
		}
		try {
			TeleconsultService service = Context.getService(TeleconsultService.class);
			return new ResponseEntity<Object>(service.mintLink(getAuthenticatedProvider(), req), HttpStatus.OK);
		}
		catch (Exception e) {
			return errorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
		}
	}
	
	@RequestMapping(value = "/appointments/{appointmentUuid}/resources", method = RequestMethod.DELETE)
	@ResponseBody
	public ResponseEntity<?> cancelAppointment(@PathVariable("appointmentUuid") String appointmentUuid) {
		ResponseEntity<Map<String, Object>> authError = requireAuthenticatedProvider();
		if (authError != null) {
			return authError;
		}
		try {
			Context.getService(ExternalResourceService.class).cancelAppointmentResources(appointmentUuid);
			Map<String, String> result = new HashMap<String, String>();
			result.put("status", "CANCELLED");
			result.put("appointmentUuid", appointmentUuid);
			return new ResponseEntity<Map<String, String>>(result, HttpStatus.OK);
		}
		catch (Exception e) {
			return errorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
		}
	}
	
	@RequestMapping(value = "/events", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<?> createEvent(@RequestBody CreateCalendarEventRequest req) {
		ResponseEntity<Map<String, Object>> authError = requireAuthenticatedProvider();
		if (authError != null) {
			return authError;
		}
		try {
			TeleconsultService service = Context.getService(TeleconsultService.class);
			return new ResponseEntity<Object>(service.createCalendarEvent(getAuthenticatedProvider(), req), HttpStatus.OK);
		}
		catch (Exception e) {
			return errorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
		}
	}
}
