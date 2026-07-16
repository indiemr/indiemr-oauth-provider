package org.openmrs.module.indiemroauthprovider.web.controller;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.openmrs.Provider;
import org.openmrs.api.context.Context;
import org.openmrs.module.indiemroauthprovider.api.OAuthConnectService;
import org.openmrs.module.indiemroauthprovider.dto.AccountStatusResponse;
import org.openmrs.module.indiemroauthprovider.model.OAuthVendorCode;
import org.openmrs.module.indiemroauthprovider.util.AuthenticatedProviderResolver;
import org.openmrs.module.indiemroauthprovider.util.ModuleConfigLoader;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.RestUtil;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/rest/" + RestConstants.VERSION_1 + "/oauth")
public class OAuthController extends BaseRestController {

	@Autowired
	@Qualifier("indiemroauthprovider.ModuleConfigLoader")
	private ModuleConfigLoader moduleConfigLoader;
	
	@RequestMapping(value = "/connect", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<?> connectUrl(@RequestParam(value = "oauthProvider", defaultValue = "GOOGLE") String oauthProvider) {
		try {
			Provider provider = AuthenticatedProviderResolver.requireAuthenticatedProvider();
			OAuthConnectService service = Context.getService(OAuthConnectService.class);
			Map<String, String> result = new HashMap<String, String>();
			result.put("url", service.buildConnectUrl(provider, OAuthVendorCode.fromCode(oauthProvider)));
			return new ResponseEntity<Map<String, String>>(result, HttpStatus.OK);
		}
		catch (Exception e) {
			return new ResponseEntity<Object>(RestUtil.wrapErrorResponse(e, e.getMessage()), HttpStatus.BAD_REQUEST);
		}
	}
	
	@RequestMapping(value = "/check-token", method = RequestMethod.GET)
	@ResponseBody
	public ResponseEntity<?> checkToken(@RequestParam(value = "oauthProvider", defaultValue = "GOOGLE") String oauthProvider) {
		OAuthConnectService service = Context.getService(OAuthConnectService.class);
		Provider provider = AuthenticatedProviderResolver.requireAuthenticatedProvider();
		AccountStatusResponse status = service.getAccountStatus(provider, OAuthVendorCode.fromCode(oauthProvider));
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
			service.handleCallback(code, state);
			String redirectUrl = moduleConfigLoader.getPublicBaseUrl() + "/admin/integrations";
			return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(redirectUrl))
			        .build();
		}
		catch (Exception e) {
			return new ResponseEntity<String>("Error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
