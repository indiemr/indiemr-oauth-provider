package org.openmrs.module.indiemroauthprovider.web.controller;

import java.util.HashMap;
import java.util.Map;

import org.openmrs.module.indiemroauthprovider.api.ExternalResourceService;
import org.openmrs.module.indiemroauthprovider.api.TeleconsultService;
import org.openmrs.module.indiemroauthprovider.dto.CreateCalendarEventRequest;
import org.openmrs.module.indiemroauthprovider.dto.UpdateCalendarEventRequest;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.api.context.Context;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/rest/" + RestConstants.VERSION_1 + "/teleconsult")
public class TeleconsultController extends BaseTeleconsultController {
	
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
	
	@RequestMapping(value = "/events", method = RequestMethod.PUT)
	@ResponseBody
	public ResponseEntity<?> updateEvent(@RequestBody UpdateCalendarEventRequest req) {
		ResponseEntity<Map<String, Object>> authError = requireAuthenticatedProvider();
		if (authError != null) {
			return authError;
		}
		try {
			TeleconsultService service = Context.getService(TeleconsultService.class);
			return new ResponseEntity<Object>(service.updateCalendarEvent(getAuthenticatedProvider(), req), HttpStatus.OK);
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
}
