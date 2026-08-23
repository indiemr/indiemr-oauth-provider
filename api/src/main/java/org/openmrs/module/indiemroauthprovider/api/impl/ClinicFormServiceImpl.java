package org.openmrs.module.indiemroauthprovider.api.impl;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.indiemroauthprovider.api.ClinicFormService;
import org.openmrs.module.indiemroauthprovider.dao.ClinicFormDao;
import org.openmrs.module.indiemroauthprovider.dao.FormShareDao;
import org.openmrs.module.indiemroauthprovider.dao.FormSubmissionDao;
import org.openmrs.module.indiemroauthprovider.dto.ClinicFormResponse;
import org.openmrs.module.indiemroauthprovider.dto.FormAnswerDto;
import org.openmrs.module.indiemroauthprovider.dto.FormResultsResponse;
import org.openmrs.module.indiemroauthprovider.dto.FormSubmissionDto;
import org.openmrs.module.indiemroauthprovider.dto.LinkClinicFormRequest;
import org.openmrs.module.indiemroauthprovider.dto.RegisterClinicFormRequest;
import org.openmrs.module.indiemroauthprovider.dto.ShareFormRequest;
import org.openmrs.module.indiemroauthprovider.dto.ShareFormResponse;
import org.openmrs.module.indiemroauthprovider.exception.ClinicFormException;
import org.openmrs.module.indiemroauthprovider.model.ClinicForm;
import org.openmrs.module.indiemroauthprovider.model.ClinicFormType;
import org.openmrs.module.indiemroauthprovider.model.FormShare;
import org.openmrs.module.indiemroauthprovider.model.FormSubmission;
import org.openmrs.module.indiemroauthprovider.provider.google.GoogleFormsRelayClient;
import org.openmrs.module.indiemroauthprovider.util.WorkspaceLocationUtil;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ClinicFormServiceImpl extends BaseOpenmrsService implements ClinicFormService {
	
	private static final Log log = LogFactory.getLog(ClinicFormServiceImpl.class);
	
	private static final String REFERENCE_FIELD_TITLE = "Reference code";
	
	private static final String REFERENCE_FIELD_DESCRIPTION = "Filled in automatically by your clinic. "
	        + "Please leave it exactly as it is.";
	
	private static final String TOKEN_PREFIX = "intk_";
	
	/** Same shape the July PoC minted, so its proven attribution regex still matches. */
	private static final Pattern TOKEN_PATTERN = Pattern.compile(TOKEN_PREFIX + "[a-f0-9]{24,}");
	
	private static final SecureRandom RANDOM = new SecureRandom();
	
	private static final ObjectMapper MAPPER = new ObjectMapper();
	
	/** Auto-check hits this on every dashboard load; one poll per patient per 30s is plenty. */
	private static final long POLL_THROTTLE_MS = 30_000L;
	
	private ClinicFormDao clinicFormDao;
	
	private FormShareDao formShareDao;
	
	private FormSubmissionDao formSubmissionDao;
	
	private GoogleFormsRelayClient googleFormsRelayClient;
	
	public void setClinicFormDao(ClinicFormDao clinicFormDao) {
		this.clinicFormDao = clinicFormDao;
	}
	
	public void setFormShareDao(FormShareDao formShareDao) {
		this.formShareDao = formShareDao;
	}
	
	public void setFormSubmissionDao(FormSubmissionDao formSubmissionDao) {
		this.formSubmissionDao = formSubmissionDao;
	}
	
	public void setGoogleFormsRelayClient(GoogleFormsRelayClient googleFormsRelayClient) {
		this.googleFormsRelayClient = googleFormsRelayClient;
	}
	
	// ---------------------------------------------------------------- registry
	
	@Override
	public List<ClinicFormResponse> getFormsForCurrentWorkspace() {
		Location root = WorkspaceLocationUtil.currentWorkspaceRoot();
		List<ClinicFormResponse> responses = new ArrayList<ClinicFormResponse>();
		for (ClinicForm form : clinicFormDao.findAllByLocation(root.getUuid())) {
			responses.add(ClinicFormResponse.from(form));
		}
		return responses;
	}
	
	@Override
	public ClinicFormResponse registerForm(RegisterClinicFormRequest request) {
		if (request == null) {
			throw new ClinicFormException("A form registration payload is required", 400);
		}
		String formType = ClinicFormType.fromCode(request.getFormType()).name();
		String formId = require(request.getFormId(), "formId");
		Location root = WorkspaceLocationUtil.currentWorkspaceRoot();
		
		ClinicForm form = loadOrCreate(root.getUuid(), formType);
		boolean formReplaced = isFormReplaced(form, formId);
		assertFormFreeForWorkspace(formId, root.getUuid(), formType);
		if (formReplaced) {
			clearFormSpecificState(form);
		}
		form.setGoogleFormId(formId);
		
		applyIfPresent(request, form);
		form.setStatus(resolveRequestedStatus(request.getStatus(), form));
		form.setLastError(null);
		form = clinicFormDao.save(form);
		if (formReplaced) {
			closePendingShares(form);
		}
		return ClinicFormResponse.from(form);
	}
	
	@Override
	public ClinicFormResponse linkForm(LinkClinicFormRequest request) {
		if (request == null) {
			throw new ClinicFormException("A link payload is required", 400);
		}
		String formType = ClinicFormType.fromCode(request.getFormType()).name();
		String formId = require(request.getFormId(), "formId");
		Location root = WorkspaceLocationUtil.currentWorkspaceRoot();
		
		ClinicForm form = loadOrCreate(root.getUuid(), formType);
		boolean formReplaced = isFormReplaced(form, formId);
		assertFormFreeForWorkspace(formId, root.getUuid(), formType);
		if (formReplaced) {
			clearFormSpecificState(form);
		}
		form.setGoogleFormId(formId);
		form.setStatus(ClinicForm.STATUS_PENDING_SHARE);
		form.setLastError(null);
		form = clinicFormDao.save(form);
		if (formReplaced) {
			closePendingShares(form);
		}
		
		// R7 — the clinic's Picker grant is needed for exactly this step and nothing else. Doing it
		// first is what makes a failed link resumable: on retry the platform token already has
		// access, so the wizard does not have to send the admin back to the Picker.
		if (request.getAccessToken() != null && !request.getAccessToken().trim().isEmpty()) {
			googleFormsRelayClient.ensurePlatformEditor(request.getAccessToken(), formId);
			form.setOwnerEmail(googleFormsRelayClient.fetchOwnerEmail(request.getAccessToken(), formId));
		}
		
		// Everything below runs on the platform token.
		googleFormsRelayClient.tryEnsurePublished(formId);
		JsonNode formJson = googleFormsRelayClient.getForm(formId);
		
		String questionId = findQuestionId(formJson, form.getReferenceQuestionId());
		if (questionId == null) {
			questionId = googleFormsRelayClient.addReferenceQuestion(formId, REFERENCE_FIELD_TITLE,
			    REFERENCE_FIELD_DESCRIPTION);
			formJson = googleFormsRelayClient.getForm(formId);
		}
		form.setReferenceQuestionId(questionId);
		capturePrefillEntries(form, formJson);
		
		form.setStatus(ClinicForm.STATUS_ACTIVE);
		form = clinicFormDao.save(form);
		return ClinicFormResponse.from(form);
	}
	
	// ---------------------------------------------------------------- share
	
	@Override
	public ShareFormResponse shareForm(ShareFormRequest request) {
		if (request == null) {
			throw new ClinicFormException("A share payload is required", 400);
		}
		String formType = ClinicFormType.fromCode(request.getFormType()).name();
		Location root = WorkspaceLocationUtil.currentWorkspaceRoot();
		Patient patient = requirePatient(request.getPatientUuid());
		ClinicForm form = requireShareableForm(root.getUuid(), formType);
		
		FormShare share = new FormShare();
		share.setClinicForm(form);
		share.setPatientUuid(patient.getUuid());
		share.setToken(mintToken());
		share.setSharedBy(requireAuthenticatedUser());
		share.setStatus(FormShare.STATUS_SHARED);
		share.setCreatedAt(new Date());
		share = formShareDao.save(share);
		
		String url = buildPrefilledUrl(form, share.getToken(), patient);
		return new ShareFormResponse(form.getFormType(), patient.getUuid(), share.getToken(), url, share.getCreatedAt());
	}
	
	// ---------------------------------------------------------------- results
	
	@Override
	public FormResultsResponse getResults(String patientUuid, String formType) {
		String type = ClinicFormType.fromCode(formType).name();
		Location root = WorkspaceLocationUtil.currentWorkspaceRoot();
		Patient patient = requirePatient(patientUuid);
		ClinicForm form = clinicFormDao.findByLocationAndType(root.getUuid(), type);
		
		FormResultsResponse response = new FormResultsResponse();
		response.setFormType(type);
		response.setPatientUuid(patient.getUuid());
		if (form == null) {
			response.setError("No " + type.toLowerCase() + " form is linked for this clinic");
			return response;
		}
		response.setFormStatus(form.getStatus());
		
		boolean pending = formShareDao.hasPendingShare(form, patient.getUuid());
		if (pending && isPollable(form) && !isThrottled(form)) {
			try {
				pollForm(form);
				response.setPolled(true);
			}
			catch (ClinicFormException e) {
				// R6 — surfaced, never swallowed. Kept out of the exception path on purpose: throwing
				// here would roll the transaction back and lose the BROKEN flag we just wrote.
				markBroken(form, e);
				response.setError(e.getMessage());
			}
			form = clinicFormDao.save(form);
			response.setFormStatus(form.getStatus());
		}
		
		response.setSubmissions(toSubmissionDtos(formSubmissionDao.findByPatientAndForm(patient.getUuid(), form.getId())));
		response.setAwaitingResponse(formShareDao.hasPendingShare(form, patient.getUuid()));
		if (response.getError() == null && form.getLastError() != null) {
			response.setError(form.getLastError());
		}
		return response;
	}
	
	// ---------------------------------------------------------------- polling
	
	private void pollForm(ClinicForm form) {
		String formId = form.getGoogleFormId();
		JsonNode formJson = googleFormsRelayClient.getForm(formId);
		
		// Form-drift self-heal: clinics stay free to edit their own questions, so the reference field
		// can simply vanish. We are Editor — put it back and re-capture the prefill id rather than
		// locking the clinic's form. Shares minted during the gap land unresolved, which is safe.
		if (findQuestionId(formJson, form.getReferenceQuestionId()) == null) {
			log.warn("Clinic form " + formId + ": reference field is missing — re-adding it");
			form.setReferenceQuestionId(googleFormsRelayClient.addReferenceQuestion(formId, REFERENCE_FIELD_TITLE,
			    REFERENCE_FIELD_DESCRIPTION));
			formJson = googleFormsRelayClient.getForm(formId);
			capturePrefillEntries(form, formJson);
			form.setLastError("The reference field was removed from the form and has been restored. Forms shared in "
			        + "the meantime may need to be sent again.");
		} else {
			form.setLastError(null);
		}
		if (ClinicForm.STATUS_BROKEN.equals(form.getStatus())) {
			form.setStatus(ClinicForm.STATUS_ACTIVE);
		}
		
		Map<String, String> questionTitles = questionTitlesById(formJson);
		int resolved = 0;
		int unresolved = 0;
		for (JsonNode response : googleFormsRelayClient.listResponses(formId)) {
			String responseId = response.path("responseId").asText(null);
			if (responseId == null || responseId.isEmpty()) {
				continue;
			}
			// The unique index on google_response_id is the real guarantee; this is the cheap check
			// that keeps re-polls from doing pointless work.
			if (formSubmissionDao.findByGoogleResponseId(responseId) != null) {
				continue;
			}
			FormShare share = resolveShare(form, response);
			FormSubmission submission = new FormSubmission();
			submission.setClinicForm(form);
			submission.setFormShare(share);
			submission.setGoogleResponseId(responseId);
			submission.setAnswersJson(serialiseAnswers(response, questionTitles));
			submission.setSubmittedAt(parseTimestamp(response.path("lastSubmittedTime").asText(null)));
			submission.setFetchedAt(new Date());
			formSubmissionDao.save(submission);
			
			if (share != null) {
				share.setStatus(FormShare.STATUS_SUBMITTED);
				formShareDao.save(share);
				resolved++;
			} else {
				unresolved++;
			}
		}
		form.setLastPolledAt(new Date());
		if (resolved > 0 || unresolved > 0) {
			log.info("Clinic form " + formId + ": stored " + resolved + " attributed and " + unresolved
			        + " unresolved response(s)");
		}
	}
	
	/**
	 * Token match, question-agnostic — we scan every text answer rather than trusting a question
	 * position, because the clinic can reorder its own form at will. A token that belongs to another
	 * form, or that a re-link has closed, resolves to nobody: an unattributed response is always
	 * preferable to a wrong-patient one.
	 */
	private FormShare resolveShare(ClinicForm form, JsonNode response) {
		String token = extractToken(response);
		if (token == null) {
			return null;
		}
		FormShare share = formShareDao.findByToken(token);
		if (share == null) {
			return null;
		}
		if (share.getClinicForm() == null || !form.getId().equals(share.getClinicForm().getId())) {
			log.warn("Clinic form " + form.getGoogleFormId() + ": response carried a token minted for a different form "
			        + "— leaving it unresolved");
			return null;
		}
		if (FormShare.STATUS_CLOSED.equals(share.getStatus())) {
			return null;
		}
		return share;
	}
	
	private static String extractToken(JsonNode response) {
		JsonNode answers = response.path("answers");
		Iterator<String> questionIds = answers.fieldNames();
		while (questionIds.hasNext()) {
			for (JsonNode answer : answers.path(questionIds.next()).path("textAnswers").path("answers")) {
				String value = answer.path("value").asText(null);
				if (value == null) {
					continue;
				}
				Matcher matcher = TOKEN_PATTERN.matcher(value);
				if (matcher.find()) {
					return matcher.group();
				}
			}
		}
		return null;
	}
	
	private static String serialiseAnswers(JsonNode response, Map<String, String> questionTitles) {
		List<FormAnswerDto> flattened = new ArrayList<FormAnswerDto>();
		JsonNode answers = response.path("answers");
		Iterator<String> questionIds = answers.fieldNames();
		while (questionIds.hasNext()) {
			String questionId = questionIds.next();
			List<String> values = new ArrayList<String>();
			for (JsonNode answer : answers.path(questionId).path("textAnswers").path("answers")) {
				values.add(answer.path("value").asText(""));
			}
			flattened.add(new FormAnswerDto(questionId, questionTitles.get(questionId), values));
		}
		try {
			return MAPPER.writeValueAsString(flattened);
		}
		catch (IOException e) {
			throw new ClinicFormException("Could not store the form answers: " + e.getMessage(), 500, e);
		}
	}
	
	private static List<FormSubmissionDto> toSubmissionDtos(List<FormSubmission> submissions) {
		List<FormSubmissionDto> dtos = new ArrayList<FormSubmissionDto>();
		for (FormSubmission submission : submissions) {
			FormSubmissionDto dto = new FormSubmissionDto();
			dto.setResponseId(submission.getGoogleResponseId());
			dto.setSubmittedAt(submission.getSubmittedAt());
			dto.setAnswers(deserialiseAnswers(submission.getAnswersJson()));
			dtos.add(dto);
		}
		return dtos;
	}
	
	private static List<FormAnswerDto> deserialiseAnswers(String answersJson) {
		if (answersJson == null || answersJson.trim().isEmpty()) {
			return new ArrayList<FormAnswerDto>();
		}
		try {
			return MAPPER.readValue(answersJson,
			    MAPPER.getTypeFactory().constructCollectionType(ArrayList.class, FormAnswerDto.class));
		}
		catch (IOException e) {
			log.warn("Unreadable stored answers — returning an empty answer list", e);
			return new ArrayList<FormAnswerDto>();
		}
	}
	
	// ---------------------------------------------------------------- prefill capture
	
	/**
	 * Prefill {@code entry.<id>}s are not exposed by any Google API — the only non-manual route is
	 * the public page, and there the ids arrive positionally. So: read the item order from the API,
	 * read the entry order from the page, and correlate by index. A length mismatch means the two
	 * views disagree, and we refuse rather than guess (R9).
	 */
	private void capturePrefillEntries(ClinicForm form, JsonNode formJson) {
		String responderUri = formJson.path("responderUri").asText(null);
		if (responderUri == null || responderUri.isEmpty()) {
			throw new ClinicFormException("Google did not report a public link for this form — publish the form so "
			        + "patients can fill it, then link it again", 502);
		}
		form.setPrefillBase(responderUri);
		
		List<String> entryIds = GoogleFormsRelayClient.parseEntryIds(googleFormsRelayClient
		        .fetchViewformHtml(responderUri));
		List<String> questionIds = questionIdsInOrder(formJson);
		List<String> titles = titlesInOrder(formJson);
		if (entryIds.size() != questionIds.size()) {
			throw new ClinicFormException("The form's public page and the Google Forms API disagree about this form's "
			        + "questions — enter the reference entry id manually", 502);
		}
		
		int referenceIndex = questionIds.indexOf(form.getReferenceQuestionId());
		if (referenceIndex < 0 || entryIds.get(referenceIndex) == null) {
			throw new ClinicFormException("Could not read the prefill id of the reference field — enter it manually", 502);
		}
		form.setEntryToken(entryIds.get(referenceIndex));
		// Name and age are prefilled purely so a shared family phone knows who the form is for.
		// Best effort by title: absent means "do not prefill", never a failure.
		form.setEntryName(matchEntryByTitle(titles, entryIds, referenceIndex, "name"));
		form.setEntryAge(matchEntryByTitle(titles, entryIds, referenceIndex, "age"));
	}
	
	private static String matchEntryByTitle(List<String> titles, List<String> entryIds, int skipIndex, String needle) {
		for (int i = 0; i < titles.size(); i++) {
			if (i == skipIndex || entryIds.get(i) == null || titles.get(i) == null) {
				continue;
			}
			if (titles.get(i).toLowerCase().contains(needle)) {
				return entryIds.get(i);
			}
		}
		return null;
	}
	
	private static List<String> questionIdsInOrder(JsonNode formJson) {
		List<String> questionIds = new ArrayList<String>();
		for (JsonNode item : formJson.path("items")) {
			questionIds.add(item.path("questionItem").path("question").path("questionId").asText(null));
		}
		return questionIds;
	}
	
	private static List<String> titlesInOrder(JsonNode formJson) {
		List<String> titles = new ArrayList<String>();
		for (JsonNode item : formJson.path("items")) {
			titles.add(item.path("title").asText(null));
		}
		return titles;
	}
	
	private static Map<String, String> questionTitlesById(JsonNode formJson) {
		Map<String, String> titles = new HashMap<String, String>();
		for (JsonNode item : formJson.path("items")) {
			String itemTitle = item.path("title").asText(null);
			String questionId = item.path("questionItem").path("question").path("questionId").asText(null);
			if (questionId != null) {
				titles.put(questionId, itemTitle);
			}
			for (JsonNode grouped : item.path("questionGroupItem").path("questions")) {
				String groupedId = grouped.path("questionId").asText(null);
				if (groupedId != null) {
					String rowTitle = grouped.path("rowQuestion").path("title").asText(null);
					titles.put(groupedId, rowTitle == null ? itemTitle : itemTitle + " — " + rowTitle);
				}
			}
		}
		return titles;
	}
	
	/** @return the stored questionId when it is still on the form, otherwise null */
	private static String findQuestionId(JsonNode formJson, String questionId) {
		if (questionId == null || questionId.isEmpty()) {
			return null;
		}
		return questionIdsInOrder(formJson).contains(questionId) ? questionId : null;
	}
	
	// ---------------------------------------------------------------- helpers
	
	private ClinicForm loadOrCreate(String locationUuid, String formType) {
		ClinicForm form = clinicFormDao.findByLocationAndType(locationUuid, formType);
		if (form != null) {
			return form;
		}
		form = new ClinicForm();
		form.setLocationUuid(locationUuid);
		form.setFormType(formType);
		return form;
	}
	
	private static boolean isFormReplaced(ClinicForm form, String formId) {
		return form.getId() != null && form.getGoogleFormId() != null && !formId.equals(form.getGoogleFormId());
	}
	
	private static void clearFormSpecificState(ClinicForm form) {
		form.setPrefillBase(null);
		form.setEntryToken(null);
		form.setEntryName(null);
		form.setEntryAge(null);
		form.setReferenceQuestionId(null);
		form.setOwnerEmail(null);
		form.setLastPolledAt(null);
	}
	
	/** R10: tokens minted against the form we are replacing can never be attributed again. */
	private void closePendingShares(ClinicForm form) {
		int closed = formShareDao.closePendingShares(form.getId());
		if (closed > 0) {
			log.info("Clinic form " + form.getFormType() + " re-linked — closed " + closed + " pending share(s)");
		}
	}
	
	/**
	 * A Google form may back exactly one registry row anywhere in the system. Without this, two
	 * workspaces could link the same form and each would see the other's unresolved responses.
	 */
	private void assertFormFreeForWorkspace(String formId, String locationUuid, String formType) {
		ClinicForm existing = clinicFormDao.findActiveByGoogleFormId(formId);
		if (existing == null) {
			return;
		}
		if (!locationUuid.equals(existing.getLocationUuid())) {
			throw new ClinicFormException("This Google form is already linked to another clinic", 409);
		}
		if (!formType.equals(existing.getFormType())) {
			throw new ClinicFormException("This Google form is already linked here as " + existing.getFormType(), 409);
		}
	}
	
	private ClinicForm requireShareableForm(String locationUuid, String formType) {
		ClinicForm form = clinicFormDao.findByLocationAndType(locationUuid, formType);
		if (form == null || ClinicForm.STATUS_DISABLED.equals(form.getStatus())) {
			throw new ClinicFormException("No " + formType.toLowerCase() + " form is linked for this clinic", 404);
		}
		if (ClinicForm.STATUS_PENDING_SHARE.equals(form.getStatus()) || form.getPrefillBase() == null
		        || form.getEntryToken() == null) {
			throw new ClinicFormException("This clinic's " + formType.toLowerCase()
			        + " form is not finished being set up yet", 409);
		}
		return form;
	}
	
	/**
	 * R3 — the only tenancy check that matters. Custom tables are never covered by DataFilter, so the
	 * patient is resolved through the platform under the caller's own session: a patient in another
	 * workspace comes back null and never yields a uuid we would store or query with.
	 */
	private static Patient requirePatient(String patientUuid) {
		String uuid = require(patientUuid, "patientUuid");
		Patient patient = Context.getPatientService().getPatientByUuid(uuid);
		if (patient == null) {
			throw new ClinicFormException("Patient not found", 404);
		}
		return patient;
	}
	
	/** R2 — identity is the authenticated User. Never a Provider: admins may not have one. */
	private static User requireAuthenticatedUser() {
		User user = Context.getAuthenticatedUser();
		if (user == null) {
			throw new ClinicFormException("No authenticated user", 401);
		}
		return user;
	}
	
	private String mintToken() {
		String token = randomToken();
		if (formShareDao.findByToken(token) != null) {
			token = randomToken();
		}
		return token;
	}
	
	private static String randomToken() {
		byte[] bytes = new byte[16];
		RANDOM.nextBytes(bytes);
		StringBuilder hex = new StringBuilder(TOKEN_PREFIX);
		for (byte b : bytes) {
			hex.append(String.format("%02x", Byte.valueOf(b)));
		}
		return hex.toString();
	}
	
	private static String buildPrefilledUrl(ClinicForm form, String token, Patient patient) {
		StringBuilder url = new StringBuilder(form.getPrefillBase()).append("?usp=pp_url");
		appendEntry(url, form.getEntryToken(), token);
		if (form.getEntryName() != null && patient.getPersonName() != null) {
			appendEntry(url, form.getEntryName(), patient.getPersonName().getFullName());
		}
		if (form.getEntryAge() != null && patient.getAge() != null) {
			appendEntry(url, form.getEntryAge(), String.valueOf(patient.getAge()));
		}
		return url.toString();
	}
	
	private static void appendEntry(StringBuilder url, String entryId, String value) {
		if (entryId == null || value == null || value.isEmpty()) {
			return;
		}
		try {
			url.append("&entry.").append(entryId).append('=')
			        .append(URLEncoder.encode(value, StandardCharsets.UTF_8.name()));
		}
		catch (IOException e) {
			throw new ClinicFormException("Could not build the prefilled link: " + e.getMessage(), 500, e);
		}
	}
	
	private static boolean isPollable(ClinicForm form) {
		return !ClinicForm.STATUS_DISABLED.equals(form.getStatus())
		        && !ClinicForm.STATUS_PENDING_SHARE.equals(form.getStatus());
	}
	
	private static boolean isThrottled(ClinicForm form) {
		return form.getLastPolledAt() != null
		        && System.currentTimeMillis() - form.getLastPolledAt().getTime() < POLL_THROTTLE_MS;
	}
	
	private static void markBroken(ClinicForm form, ClinicFormException failure) {
		log.error("Clinic form " + form.getGoogleFormId() + " poll failed: " + failure.getMessage(), failure);
		if (failure.getStatus() == 403 || failure.getStatus() == 404) {
			form.setStatus(ClinicForm.STATUS_BROKEN);
		}
		form.setLastError(failure.getMessage());
		form.setLastPolledAt(new Date());
	}
	
	private void applyIfPresent(RegisterClinicFormRequest request, ClinicForm form) {
		if (request.getPrefillBase() != null) {
			form.setPrefillBase(trimToNull(request.getPrefillBase()));
		}
		if (request.getEntryToken() != null) {
			form.setEntryToken(trimToNull(request.getEntryToken()));
		}
		if (request.getEntryName() != null) {
			form.setEntryName(trimToNull(request.getEntryName()));
		}
		if (request.getEntryAge() != null) {
			form.setEntryAge(trimToNull(request.getEntryAge()));
		}
		if (request.getReferenceQuestionId() != null) {
			form.setReferenceQuestionId(trimToNull(request.getReferenceQuestionId()));
		}
	}
	
	private static String resolveRequestedStatus(String requested, ClinicForm form) {
		if (requested == null || requested.trim().isEmpty()) {
			return form.getPrefillBase() != null && form.getEntryToken() != null ? ClinicForm.STATUS_ACTIVE
			        : ClinicForm.STATUS_PENDING_SHARE;
		}
		String status = requested.trim().toUpperCase();
		if (ClinicForm.STATUS_ACTIVE.equals(status) || ClinicForm.STATUS_DISABLED.equals(status)
		        || ClinicForm.STATUS_PENDING_SHARE.equals(status)) {
			return status;
		}
		throw new ClinicFormException("Unsupported status: " + requested, 400);
	}
	
	private static Date parseTimestamp(String rfc3339) {
		if (rfc3339 == null || rfc3339.isEmpty()) {
			return null;
		}
		try {
			return Date.from(Instant.parse(rfc3339));
		}
		catch (RuntimeException e) {
			log.warn("Unparseable Google timestamp '" + rfc3339 + "' — storing without a submission time");
			return null;
		}
	}
	
	private static String require(String value, String field) {
		String trimmed = trimToNull(value);
		if (trimmed == null) {
			throw new ClinicFormException(field + " is required", 400);
		}
		return trimmed;
	}
	
	private static String trimToNull(String value) {
		if (value == null) {
			return null;
		}
		String trimmed = value.trim();
		return trimmed.isEmpty() ? null : trimmed;
	}
}
