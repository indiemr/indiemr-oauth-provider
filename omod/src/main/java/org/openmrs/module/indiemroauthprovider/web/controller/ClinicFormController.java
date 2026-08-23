package org.openmrs.module.indiemroauthprovider.web.controller;

import java.util.List;

import org.openmrs.api.context.Context;
import org.openmrs.module.indiemroauthprovider.api.ClinicFormService;
import org.openmrs.module.indiemroauthprovider.dto.ClinicFormResponse;
import org.openmrs.module.indiemroauthprovider.dto.FormResultsResponse;
import org.openmrs.module.indiemroauthprovider.dto.LinkClinicFormRequest;
import org.openmrs.module.indiemroauthprovider.dto.RegisterClinicFormRequest;
import org.openmrs.module.indiemroauthprovider.dto.ShareFormRequest;
import org.openmrs.module.indiemroauthprovider.dto.ShareFormResponse;
import org.openmrs.module.indiemroauthprovider.exception.ClinicFormException;
import org.openmrs.module.indiemroauthprovider.model.ClinicFormType;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.RestUtil;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Clinic Form Relay endpoints.
 * <p>
 * Note what is NOT here: a try/catch around each handler. A hand-rolled controller under /ws/rest
 * gets no automatic authentication — the AuthorizationFilter lets unauthenticated requests through
 * and only an escaping APIAuthenticationException becomes a 401/403. Catching everything and
 * answering 400 (as the module's own /connect handler does) would turn every unauthenticated call
 * into a 400 and hide that the endpoint is open. So authorisation lives on the service's
 * {@code @Authorized} methods and its exception is deliberately left to propagate (R1).
 * <p>
 * The workspace is never taken from the request either — it is always derived from the session.
 */
@Controller
@RequestMapping("/rest/" + RestConstants.VERSION_1 + "/clinicform")
public class ClinicFormController extends BaseRestController {
	
	@RequestMapping(method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<List<ClinicFormResponse>> listForms() {
		return new ResponseEntity<List<ClinicFormResponse>>(service().getFormsForCurrentWorkspace(), HttpStatus.OK);
	}
	
	@RequestMapping(method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<ClinicFormResponse> registerForm(@RequestBody RegisterClinicFormRequest request) {
		return new ResponseEntity<ClinicFormResponse>(service().registerForm(request), HttpStatus.OK);
	}
	
	@RequestMapping(value = "/link", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<ClinicFormResponse> linkForm(@RequestBody LinkClinicFormRequest request) {
		return new ResponseEntity<ClinicFormResponse>(service().linkForm(request), HttpStatus.OK);
	}
	
	@RequestMapping(value = "/share", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<ShareFormResponse> shareForm(@RequestBody ShareFormRequest request) {
		if (request != null && request.getFormType() == null) {
			request.setFormType(ClinicFormType.INTAKE.name());
		}
		return new ResponseEntity<ShareFormResponse>(service().shareForm(request), HttpStatus.OK);
	}
	
	@RequestMapping(value = "/results", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<FormResultsResponse> results(@RequestParam(value = "patientUuid") String patientUuid,
	        @RequestParam(value = "formType", required = false) String formType) {
		String type = formType != null ? formType : ClinicFormType.INTAKE.name();
		return new ResponseEntity<FormResultsResponse>(service().getResults(patientUuid, type), HttpStatus.OK);
	}
	
	/** Bad input from the client — never an authorisation outcome, which is handled a layer up. */
	@ExceptionHandler(IllegalArgumentException.class)
	@ResponseBody
	public ResponseEntity<Object> handleBadRequest(IllegalArgumentException e) {
		return new ResponseEntity<Object>(RestUtil.wrapErrorResponse(e, e.getMessage()), HttpStatus.BAD_REQUEST);
	}
	
	@ExceptionHandler(ClinicFormException.class)
	@ResponseBody
	public ResponseEntity<Object> handleClinicFormException(ClinicFormException e) {
		return new ResponseEntity<Object>(RestUtil.wrapErrorResponse(e, e.getMessage()), statusOf(e));
	}
	
	private static HttpStatus statusOf(ClinicFormException e) {
		try {
			return HttpStatus.valueOf(e.getStatus());
		}
		catch (IllegalArgumentException unknown) {
			return HttpStatus.INTERNAL_SERVER_ERROR;
		}
	}
	
	private static ClinicFormService service() {
		return Context.getService(ClinicFormService.class);
	}
}
