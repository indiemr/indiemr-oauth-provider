package org.openmrs.module.indiemroauthprovider.api;

import java.util.List;

import org.openmrs.annotation.Authorized;
import org.openmrs.api.OpenmrsService;
import org.openmrs.module.indiemroauthprovider.PrivilegeConstants;
import org.openmrs.module.indiemroauthprovider.dto.ClinicFormResponse;
import org.openmrs.module.indiemroauthprovider.dto.FormResultsResponse;
import org.openmrs.module.indiemroauthprovider.dto.LinkClinicFormRequest;
import org.openmrs.module.indiemroauthprovider.dto.RegisterClinicFormRequest;
import org.openmrs.module.indiemroauthprovider.dto.ShareFormRequest;
import org.openmrs.module.indiemroauthprovider.dto.ShareFormResponse;
import org.springframework.transaction.annotation.Transactional;

/**
 * Clinic Form Relay. Every method is privilege-gated here rather than in the controller:
 * hand-rolled {@code @Controller}s under /ws/rest get no automatic authentication, so the service
 * layer is the only place the check cannot be forgotten (R1).
 * <p>
 * No method takes a location — the workspace is always derived from the caller's session (R3/R4).
 */
public interface ClinicFormService extends OpenmrsService {
	
	@Transactional(readOnly = true)
	@Authorized({ PrivilegeConstants.MANAGE_CLINIC_FORMS, PrivilegeConstants.SHARE_CLINIC_FORMS })
	List<ClinicFormResponse> getFormsForCurrentWorkspace();
	
	@Transactional
	@Authorized({ PrivilegeConstants.MANAGE_CLINIC_FORMS })
	ClinicFormResponse registerForm(RegisterClinicFormRequest request);
	
	@Transactional
	@Authorized({ PrivilegeConstants.MANAGE_CLINIC_FORMS })
	ClinicFormResponse linkForm(LinkClinicFormRequest request);
	
	@Transactional
	@Authorized({ PrivilegeConstants.SHARE_CLINIC_FORMS })
	ShareFormResponse shareForm(ShareFormRequest request);
	
	@Transactional
	@Authorized({ PrivilegeConstants.SHARE_CLINIC_FORMS })
	FormResultsResponse getResults(String patientUuid, String formType);
}
