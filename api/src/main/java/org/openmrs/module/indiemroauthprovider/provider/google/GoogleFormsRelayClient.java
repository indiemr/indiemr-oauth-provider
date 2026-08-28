package org.openmrs.module.indiemroauthprovider.provider.google;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.indiemroauthprovider.exception.ClinicFormException;
import org.openmrs.module.indiemroauthprovider.util.ModuleConfigLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.ClientParametersAuthentication;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.gson.GsonFactory;

/**
 * Every Google call the Clinic Form Relay makes, in one place.
 * <p>
 * Two identities, never mixed up:
 * <ul>
 * <li><b>platform</b> — one refresh token ({@code indiemr1}) that is Editor on every linked form
 * and does all runtime reads/edits. N clinics, 1 token.</li>
 * <li><b>clinic grant</b> — the short-lived Picker access token, passed in per call, used for
 * exactly one operation (adding the platform account as Editor) and then dropped. It is never
 * persisted and never logged (R8).</li>
 * </ul>
 * Deliberately built on the raw google-http-client already on the module's classpath rather than
 * the generated Forms/Drive clients: six endpoints do not justify two more jars inside the omod.
 * {@link Credential} still does the token refresh, exactly as the teleconsult Calendar client does.
 */
@Component("indiemroauthprovider.GoogleFormsRelayClient")
public class GoogleFormsRelayClient {
	
	private static final Log log = LogFactory.getLog(GoogleFormsRelayClient.class);
	
	private static final String FORMS_BASE = "https://forms.googleapis.com/v1/forms/";
	
	private static final String DRIVE_FILES_BASE = "https://www.googleapis.com/drive/v3/files/";
	
	private static final String TOKEN_SERVER_URL = "https://oauth2.googleapis.com/token";
	
	private static final String JSON_CONTENT_TYPE = "application/json; charset=UTF-8";
	
	private static final String FB_LOAD_DATA_MARKER = "FB_PUBLIC_LOAD_DATA_";
	
	private static final ObjectMapper MAPPER = new ObjectMapper();
	
	@Autowired
	@Qualifier("indiemroauthprovider.ModuleConfigLoader")
	private ModuleConfigLoader moduleConfigLoader;
	
	private volatile HttpTransport transport;
	
	public void setModuleConfigLoader(ModuleConfigLoader moduleConfigLoader) {
		this.moduleConfigLoader = moduleConfigLoader;
	}
	
	// ---------------------------------------------------------------- Drive (clinic grant)
	
	/**
	 * Adds the platform account as Editor on the clinic's form, using the one-time Picker grant.
	 * Idempotent: an existing writer/owner grant is left alone, so a resumed /link does not pile up
	 * permissions.
	 * 
	 * @return true when a new permission was created
	 */
	public boolean ensurePlatformEditor(String clinicAccessToken, String formId) {
		String platformEmail = requireConfig(moduleConfigLoader.getFormsPlatformAccountEmail(),
		    "forms-relay.platform-account-email");
		HttpRequestFactory factory = requestFactory(grantCredential(clinicAccessToken));
		
		String listUrl = DRIVE_FILES_BASE + encodePathSegment(formId)
		        + "/permissions?fields=permissions(id,emailAddress,role)&pageSize=100";
		JsonNode permissions = readJson(factory, listUrl, "list form permissions").path("permissions");
		for (JsonNode permission : permissions) {
			String email = permission.path("emailAddress").asText("");
			String role = permission.path("role").asText("");
			if (platformEmail.equalsIgnoreCase(email) && ("writer".equals(role) || "owner".equals(role))) {
				return false;
			}
		}
		
		Map<String, Object> body = new LinkedHashMap<String, Object>();
		body.put("role", "writer");
		body.put("type", "user");
		body.put("emailAddress", platformEmail);
		String createUrl = DRIVE_FILES_BASE + encodePathSegment(formId) + "/permissions?sendNotificationEmail=false";
		postJson(factory, createUrl, body, "add the platform account as an editor");
		return true;
	}
	
	/** Owner of the linked file, for display on the Integrations page. Uses the clinic grant. */
	public String fetchOwnerEmail(String clinicAccessToken, String formId) {
		HttpRequestFactory factory = requestFactory(grantCredential(clinicAccessToken));
		String url = DRIVE_FILES_BASE + encodePathSegment(formId) + "?fields=owners(emailAddress)";
		JsonNode owners = readJson(factory, url, "read form ownership").path("owners");
		if (owners.isArray() && owners.size() > 0) {
			String email = owners.get(0).path("emailAddress").asText(null);
			return email != null && !email.isEmpty() ? email : null;
		}
		return null;
	}
	
	// ---------------------------------------------------------------- Forms (platform token)
	
	public JsonNode getForm(String formId) {
		return readJson(platformFactory(), FORMS_BASE + encodePathSegment(formId), "read the linked form");
	}
	
	/**
	 * Best-effort nudge of the form into "published, accepting responses". Not fatal on failure:
	 * the prefill scrape that follows is the real test of public fillability, and it fails loud
	 * (R9).
	 */
	public void tryEnsurePublished(String formId) {
		Map<String, Object> publishState = new LinkedHashMap<String, Object>();
		publishState.put("isPublished", Boolean.TRUE);
		publishState.put("isAcceptingResponses", Boolean.TRUE);
		Map<String, Object> publishSettings = new LinkedHashMap<String, Object>();
		publishSettings.put("publishState", publishState);
		Map<String, Object> body = new LinkedHashMap<String, Object>();
		body.put("publishSettings", publishSettings);
		body.put("updateMask", "publishState.isPublished,publishState.isAcceptingResponses");
		try {
			postJson(platformFactory(), FORMS_BASE + encodePathSegment(formId) + ":setPublishSettings", body,
			    "update publish settings");
		}
		catch (ClinicFormException e) {
			log.warn("Clinic form " + formId + ": could not set publish settings (" + e.getMessage()
			        + ") — continuing; the prefill scrape decides whether the form is actually fillable");
		}
	}
	
	/** Adds the reference-code question at the top of the form and returns its questionId. */
	public String addReferenceQuestion(String formId, String title, String description) {
		Map<String, Object> textQuestion = new LinkedHashMap<String, Object>();
		textQuestion.put("paragraph", Boolean.FALSE);
		Map<String, Object> question = new LinkedHashMap<String, Object>();
		question.put("required", Boolean.TRUE);
		question.put("textQuestion", textQuestion);
		Map<String, Object> questionItem = new LinkedHashMap<String, Object>();
		questionItem.put("question", question);
		Map<String, Object> item = new LinkedHashMap<String, Object>();
		item.put("title", title);
		item.put("description", description);
		item.put("questionItem", questionItem);
		Map<String, Object> location = new LinkedHashMap<String, Object>();
		location.put("index", Integer.valueOf(0));
		Map<String, Object> createItem = new LinkedHashMap<String, Object>();
		createItem.put("item", item);
		createItem.put("location", location);
		Map<String, Object> request = new LinkedHashMap<String, Object>();
		request.put("createItem", createItem);
		List<Object> requests = new ArrayList<Object>();
		requests.add(request);
		Map<String, Object> body = new LinkedHashMap<String, Object>();
		body.put("requests", requests);
		
		JsonNode response = postJson(platformFactory(), FORMS_BASE + encodePathSegment(formId) + ":batchUpdate", body,
		    "add the reference-code field");
		JsonNode questionIds = response.path("replies").path(0).path("createItem").path("questionId");
		String questionId = questionIds.isArray() && questionIds.size() > 0 ? questionIds.get(0).asText(null) : null;
		if (questionId == null || questionId.isEmpty()) {
			throw new ClinicFormException("Google accepted the reference-code field but returned no questionId", 502);
		}
		return questionId;
	}
	
	/** Every response on the form. Volumes are small and dedup is by responseId, so no checkpoint. */
	public List<JsonNode> listResponses(String formId) {
		List<JsonNode> responses = new ArrayList<JsonNode>();
		HttpRequestFactory factory = platformFactory();
		String pageToken = null;
		do {
			String url = FORMS_BASE + encodePathSegment(formId) + "/responses?pageSize=100";
			if (pageToken != null) {
				url += "&pageToken=" + encodeQueryValue(pageToken);
			}
			JsonNode page = readJson(factory, url, "read form responses");
			for (JsonNode response : page.path("responses")) {
				responses.add(response);
			}
			pageToken = page.path("nextPageToken").asText(null);
		} while (pageToken != null && !pageToken.isEmpty());
		return responses;
	}
	
	// ---------------------------------------------------------------- Public viewform scrape
	
	/**
	 * Fetches the PUBLIC responder page. No credentials at all — this doubles as the health check:
	 * a form that is not publicly fillable cannot be scraped, and the caller fails loud (R9).
	 */
	public String fetchViewformHtml(String responderUri) {
		try {
			HttpRequest request = requestFactory(null).buildGetRequest(new GenericUrl(responderUri));
			request.getHeaders().setUserAgent("IndiEMR-ClinicFormRelay/1.0");
			return request.execute().parseAsString();
		}
		catch (HttpResponseException e) {
			throw new ClinicFormException("Could not open the form's public page (HTTP " + e.getStatusCode()
			        + ") — check that the form accepts responses from anyone", 502, e);
		}
		catch (IOException e) {
			throw new ClinicFormException("Could not open the form's public page: " + e.getMessage(), 502, e);
		}
	}
	
	/**
	 * Prefill {@code entry.<id>} values in form-item order, with a null wherever an item carries no
	 * answer (page breaks, section headers, images). Positional so it can be index-correlated with
	 * the Forms API item list — entry ids are NOT questionIds and no API returns them.
	 */
	public static List<String> parseEntryIds(String viewformHtml) {
		String arrayText = extractLoadDataArray(viewformHtml);
		JsonNode root;
		try {
			root = MAPPER.readTree(arrayText);
		}
		catch (IOException e) {
			throw new ClinicFormException("Could not parse the form's public page — Google may have changed its layout; "
			        + "register the entry id manually", 502, e);
		}
		JsonNode items = root.path(1).path(1);
		if (!items.isArray()) {
			throw new ClinicFormException(
			        "The form's public page carried no question list — register the entry id manually", 502);
		}
		List<String> entryIds = new ArrayList<String>();
		for (JsonNode item : items) {
			JsonNode answers = item.path(4);
			if (answers.isArray() && answers.size() > 0 && answers.get(0).isArray() && answers.get(0).size() > 0
			        && answers.get(0).get(0).isNumber()) {
				entryIds.add(answers.get(0).get(0).asText());
			} else {
				entryIds.add(null);
			}
		}
		return entryIds;
	}
	
	/**
	 * Pulls the {@code FB_PUBLIC_LOAD_DATA_} array out of the page by balancing brackets, so a
	 * bracket inside a question's own text cannot truncate it.
	 */
	private static String extractLoadDataArray(String html) {
		int marker = html != null ? html.indexOf(FB_LOAD_DATA_MARKER) : -1;
		int start = marker >= 0 ? html.indexOf('[', marker) : -1;
		if (start < 0) {
			throw new ClinicFormException("The form's public page did not contain the expected prefill data — the form "
			        + "may not be publicly fillable, or Google changed its layout; register the entry id manually", 502);
		}
		int depth = 0;
		boolean inString = false;
		boolean escaped = false;
		for (int i = start; i < html.length(); i++) {
			char c = html.charAt(i);
			if (inString) {
				if (escaped) {
					escaped = false;
				} else if (c == '\\') {
					escaped = true;
				} else if (c == '"') {
					inString = false;
				}
				continue;
			}
			if (c == '"') {
				inString = true;
			} else if (c == '[') {
				depth++;
			} else if (c == ']' && --depth == 0) {
				return html.substring(start, i + 1);
			}
		}
		throw new ClinicFormException("The form's public page ended mid-way through its prefill data — register the "
		        + "entry id manually", 502);
	}
	
	// ---------------------------------------------------------------- plumbing
	
	private HttpRequestFactory platformFactory() {
		return requestFactory(platformCredential());
	}
	
	private Credential platformCredential() {
		String clientId = requireConfig(moduleConfigLoader.getFormsClientId(), "forms-relay.client-id");
		String clientSecret = requireConfig(moduleConfigLoader.getFormsClientSecret(), "forms-relay.client-secret");
		String refreshToken = requireConfig(moduleConfigLoader.getFormsPlatformRefreshToken(),
		    "forms-relay.platform-refresh-token");
		return new Credential.Builder(BearerToken.authorizationHeaderAccessMethod()).setTransport(transport())
		        .setJsonFactory(GsonFactory.getDefaultInstance()).setTokenServerUrl(new GenericUrl(TOKEN_SERVER_URL))
		        .setClientAuthentication(new ClientParametersAuthentication(clientId, clientSecret)).build()
		        .setRefreshToken(refreshToken);
	}
	
	/** Access token only — no refresh token, no client secret: it cannot outlive the request (R8). */
	private Credential grantCredential(String accessToken) {
		if (accessToken == null || accessToken.trim().isEmpty()) {
			throw new ClinicFormException("The Google authorisation for this form is missing — pick the form again", 400);
		}
		return new Credential(BearerToken.authorizationHeaderAccessMethod()).setAccessToken(accessToken.trim());
	}
	
	private HttpRequestFactory requestFactory(Credential credential) {
		return credential != null ? transport().createRequestFactory(credential) : transport().createRequestFactory();
	}
	
	private HttpTransport transport() {
		HttpTransport local = transport;
		if (local == null) {
			synchronized (this) {
				local = transport;
				if (local == null) {
					try {
						local = GoogleNetHttpTransport.newTrustedTransport();
					}
					catch (Exception e) {
						throw new ClinicFormException("Could not initialise the Google HTTP transport: " + e.getMessage(),
						        500, e);
					}
					transport = local;
				}
			}
		}
		return local;
	}
	
	private JsonNode readJson(HttpRequestFactory factory, String url, String what) {
		try {
			return parse(factory.buildGetRequest(new GenericUrl(url)).execute().parseAsString(), what);
		}
		catch (HttpResponseException e) {
			throw googleFailure(what, e);
		}
		catch (IOException e) {
			throw new ClinicFormException("Could not " + what + ": " + e.getMessage(), 502, e);
		}
	}
	
	private JsonNode postJson(HttpRequestFactory factory, String url, Map<String, Object> body, String what) {
		try {
			ByteArrayContent content = new ByteArrayContent(JSON_CONTENT_TYPE, MAPPER.writeValueAsBytes(body));
			return parse(factory.buildPostRequest(new GenericUrl(url), content).execute().parseAsString(), what);
		}
		catch (HttpResponseException e) {
			throw googleFailure(what, e);
		}
		catch (IOException e) {
			throw new ClinicFormException("Could not " + what + ": " + e.getMessage(), 502, e);
		}
	}
	
	private static JsonNode parse(String body, String what) {
		if (body == null || body.trim().isEmpty()) {
			return MAPPER.createObjectNode();
		}
		try {
			return MAPPER.readTree(body);
		}
		catch (IOException e) {
			throw new ClinicFormException("Google returned an unreadable response when asked to " + what, 502, e);
		}
	}
	
	/**
	 * 403/404 from Google means we have lost access to the form — the caller flips the registry row
	 * to BROKEN rather than swallowing it (R6).
	 */
	private static ClinicFormException googleFailure(String what, HttpResponseException e) {
		int status = e.getStatusCode() == 403 || e.getStatusCode() == 404 ? e.getStatusCode() : 502;
		return new ClinicFormException("Google refused to " + what + " (HTTP " + e.getStatusCode() + "): "
		        + firstLine(e.getContent()), status, e);
	}
	
	private static String firstLine(String content) {
		if (content == null) {
			return "no detail";
		}
		String trimmed = content.trim().replaceAll("\\s+", " ");
		return trimmed.length() > 300 ? trimmed.substring(0, 300) + "…" : trimmed;
	}
	
	private static String requireConfig(String value, String key) {
		if (value == null || value.trim().isEmpty()) {
			throw new ClinicFormException("Clinic form relay is not configured on this server (missing " + key + ")", 500);
		}
		return value.trim();
	}
	
	private static String encodePathSegment(String value) {
		try {
			return URLEncoder.encode(value, StandardCharsets.UTF_8.name()).replace("+", "%20");
		}
		catch (Exception e) {
			throw new ClinicFormException("Unusable identifier: " + value, 400, e);
		}
	}
	
	private static String encodeQueryValue(String value) {
		try {
			return URLEncoder.encode(value, StandardCharsets.UTF_8.name());
		}
		catch (Exception e) {
			throw new ClinicFormException("Unusable page token", 502, e);
		}
	}
}
