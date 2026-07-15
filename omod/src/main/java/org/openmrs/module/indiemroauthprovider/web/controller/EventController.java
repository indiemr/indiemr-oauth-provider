// package org.openmrs.module.indiemroauthprovider.web.controller;
// import org.openmrs.Provider;
// import org.openmrs.api.context.Context;
// import org.openmrs.module.indiemroauthprovider.api.TeleconsultService;
// import org.openmrs.module.indiemroauthprovider.dto.CreateCalendarEventRequest;
// import org.openmrs.module.indiemroauthprovider.dto.UpdateCalendarEventRequest;
// import org.openmrs.module.indiemroauthprovider.util.AuthenticatedProviderResolver;
// import org.openmrs.module.webservices.rest.web.RestConstants;
// import org.openmrs.module.webservices.rest.web.RestUtil;
// import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
// import org.springframework.http.HttpStatus;
// import org.springframework.http.ResponseEntity;
// import org.springframework.stereotype.Controller;
// import org.springframework.web.bind.annotation.RequestBody;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RequestMethod;
// import org.springframework.web.bind.annotation.ResponseBody;

// @Controller
// @RequestMapping("/rest/" + RestConstants.VERSION_1 + "/event")
// public class EventController extends BaseRestController {

// 	@RequestMapping(value = "/events", method = RequestMethod.POST)
// 	@ResponseBody
// 	public ResponseEntity<?> createEvent(@RequestBody CreateCalendarEventRequest req) {
// 		try {
// 			Provider provider = AuthenticatedProviderResolver.requireAuthenticatedProvider();
// 			TeleconsultService service = Context.getService(TeleconsultService.class);
// 			return new ResponseEntity<Object>(service.createCalendarEvent(provider, req), HttpStatus.OK);
// 		}
// 		catch (Exception e) {
// 			return new ResponseEntity<Object>(RestUtil.wrapErrorResponse(e, e.getMessage()), HttpStatus.BAD_REQUEST);
// 		}
// 	}

// 	@RequestMapping(value = "/events", method = RequestMethod.PUT)
// 	@ResponseBody
// 	public ResponseEntity<?> updateEvent(@RequestBody UpdateCalendarEventRequest req) {
// 		try {
// 			Provider provider = AuthenticatedProviderResolver.requireAuthenticatedProvider();
// 			TeleconsultService service = Context.getService(TeleconsultService.class);
// 			return new ResponseEntity<Object>(service.updateCalendarEvent(provider, req), HttpStatus.OK);
// 		}
// 		catch (Exception e) {
// 			return new ResponseEntity<Object>(RestUtil.wrapErrorResponse(e, e.getMessage()), HttpStatus.BAD_REQUEST);
// 		}
// 	}
// }
