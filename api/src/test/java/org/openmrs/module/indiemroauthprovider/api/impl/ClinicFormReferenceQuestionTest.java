package org.openmrs.module.indiemroauthprovider.api.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.openmrs.module.indiemroauthprovider.api.impl.ClinicFormServiceImpl.ReferenceQuestion;
import org.openmrs.module.indiemroauthprovider.provider.google.GoogleFormsRelayClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Covers the one decision in the relay that can write to a CLINIC'S OWN Google form: whether a
 * reference question has to be added at all. Adding it is not rollbackable, so a {@code /link} that
 * fails after the add (the prefill scrape throws) rolls the registry row back and leaves the
 * question stranded on the form. Without adopt-by-title the next run — link or drift self-heal —
 * sees no stored id, adds a SECOND "Reference code" question, and the form drifts further every
 * time.
 * <p>
 * Pure decision logic: the service's other collaborators are never touched, so this needs no
 * OpenMRS context and no Spring — same shape as {@code GoogleFormsRelayClientParseTest}. The relay
 * client is a recording stub rather than a mock, so "was the clinic's form written to?" is asserted
 * on a real call count.
 */
public class ClinicFormReferenceQuestionTest {
	
	private static final ObjectMapper MAPPER = new ObjectMapper();
	
	/** Must match ClinicFormServiceImpl.REFERENCE_FIELD_TITLE — that constant is the contract. */
	private static final String REFERENCE_TITLE = "Reference code";
	
	private static final String ADDED_QUESTION_ID = "q_added_by_google";
	
	// ---------------------------------------------------------------- (a) adopt
	
	@Test
	public void resolveReferenceQuestion_adoptsAnExistingQuestionWithOurTitleWithoutWriting() {
		RecordingRelayClient relay = new RecordingRelayClient();
		ClinicFormServiceImpl service = serviceWith(relay);
		JsonNode formJson = form(item("Name", "q_name"), item(REFERENCE_TITLE, "q_orphan"), item("Age", "q_age"));
		
		ReferenceQuestion reference = service.resolveReferenceQuestion("form-1", formJson, null);
		
		assertEquals("q_orphan", reference.questionId);
		assertFalse("adoption must not write to the clinic's form", reference.added);
		assertFalse("an adopted id is new to us — the prefill entry must be re-captured", reference.unchanged);
		assertEquals(0, relay.addCalls);
	}
	
	/** The rollback case itself: a stored id survives in the registry but is no longer on the form. */
	@Test
	public void resolveReferenceQuestion_adoptsByTitleWhenTheStoredIdIsStale() {
		RecordingRelayClient relay = new RecordingRelayClient();
		ClinicFormServiceImpl service = serviceWith(relay);
		JsonNode formJson = form(item("Name", "q_name"), item(REFERENCE_TITLE, "q_recreated"));
		
		ReferenceQuestion reference = service.resolveReferenceQuestion("form-1", formJson, "q_gone");
		
		assertEquals("q_recreated", reference.questionId);
		assertFalse(reference.added);
		assertFalse(reference.unchanged);
		assertEquals(0, relay.addCalls);
	}
	
	// ---------------------------------------------------------------- (b) add
	
	@Test
	public void resolveReferenceQuestion_addsTheQuestionWhenNoTitleMatches() {
		RecordingRelayClient relay = new RecordingRelayClient();
		ClinicFormServiceImpl service = serviceWith(relay);
		JsonNode formJson = form(item("Name", "q_name"), item("Age", "q_age"));
		
		ReferenceQuestion reference = service.resolveReferenceQuestion("form-1", formJson, null);
		
		assertEquals(ADDED_QUESTION_ID, reference.questionId);
		assertTrue("the caller must re-read the form json after a write", reference.added);
		assertFalse(reference.unchanged);
		assertEquals(1, relay.addCalls);
		assertEquals(REFERENCE_TITLE, relay.lastTitle);
	}
	
	/** A section header carrying the title is not a question — there is no id to adopt. */
	@Test
	public void resolveReferenceQuestion_ignoresANonQuestionItemWithOurTitle() {
		RecordingRelayClient relay = new RecordingRelayClient();
		ClinicFormServiceImpl service = serviceWith(relay);
		JsonNode formJson = form(item(REFERENCE_TITLE, null), item("Name", "q_name"));
		
		ReferenceQuestion reference = service.resolveReferenceQuestion("form-1", formJson, null);
		
		assertEquals(ADDED_QUESTION_ID, reference.questionId);
		assertEquals(1, relay.addCalls);
	}
	
	// ---------------------------------------------------------------- (c) duplicates
	
	@Test
	public void resolveReferenceQuestion_adoptsTheFirstOfSeveralDuplicatesAndLeavesTheRestAlone() {
		RecordingRelayClient relay = new RecordingRelayClient();
		ClinicFormServiceImpl service = serviceWith(relay);
		JsonNode formJson = form(item(REFERENCE_TITLE, "q_first"), item("Name", "q_name"),
		    item(REFERENCE_TITLE, "q_second"), item(REFERENCE_TITLE, "q_third"));
		
		ReferenceQuestion reference = service.resolveReferenceQuestion("form-1", formJson, null);
		
		assertEquals("q_first", reference.questionId);
		assertFalse(reference.added);
		assertEquals(0, relay.addCalls);
	}
	
	// ---------------------------------------------------------------- stored id still there
	
	@Test
	public void resolveReferenceQuestion_keepsTheStoredIdWhenItIsStillOnTheForm() {
		RecordingRelayClient relay = new RecordingRelayClient();
		ClinicFormServiceImpl service = serviceWith(relay);
		JsonNode formJson = form(item("Name", "q_name"), item(REFERENCE_TITLE, "q_stored"));
		
		ReferenceQuestion reference = service.resolveReferenceQuestion("form-1", formJson, "q_stored");
		
		assertEquals("q_stored", reference.questionId);
		assertTrue("nothing changed — the stored prefill entry id still holds", reference.unchanged);
		assertFalse(reference.added);
		assertEquals(0, relay.addCalls);
	}
	
	// ---------------------------------------------------------------- fixtures
	
	private static ClinicFormServiceImpl serviceWith(GoogleFormsRelayClient relay) {
		ClinicFormServiceImpl service = new ClinicFormServiceImpl();
		service.setGoogleFormsRelayClient(relay);
		return service;
	}
	
	/** A questionId of null builds a non-question item, the way Google reports a section header. */
	private static String item(String title, String questionId) {
		String question = questionId == null ? "" : ",\"questionItem\":{\"question\":{\"questionId\":\"" + questionId
		        + "\"}}";
		return "{\"title\":\"" + title + "\"" + question + "}";
	}
	
	private static JsonNode form(String... items) {
		List<String> parts = new ArrayList<String>();
		for (String item : items) {
			parts.add(item);
		}
		String json = "{\"formId\":\"form-1\",\"items\":[" + join(parts) + "]}";
		try {
			return MAPPER.readTree(json);
		}
		catch (IOException e) {
			throw new IllegalStateException("bad fixture: " + json, e);
		}
	}
	
	private static String join(List<String> parts) {
		StringBuilder sb = new StringBuilder();
		for (String part : parts) {
			if (sb.length() > 0) {
				sb.append(',');
			}
			sb.append(part);
		}
		return sb.toString();
	}
	
	/** Counts the one call that reaches the clinic's live form. Every other method stays unused. */
	private static class RecordingRelayClient extends GoogleFormsRelayClient {
		
		int addCalls;
		
		String lastTitle;
		
		@Override
		public String addReferenceQuestion(String formId, String title, String description) {
			addCalls++;
			lastTitle = title;
			return ADDED_QUESTION_ID;
		}
	}
}
