package org.openmrs.module.indiemroauthprovider.provider.google;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import org.junit.Test;
import org.openmrs.module.indiemroauthprovider.exception.ClinicFormException;

/**
 * Covers the one algorithmic piece of the Clinic Form Relay that depends on Google's UNDOCUMENTED
 * public-page format: pulling prefill {@code entry.<id>} values out of the
 * {@code FB_PUBLIC_LOAD_DATA_} array on a form's viewform page. Pure static methods — no OpenMRS
 * context, no Spring, no mocks.
 * <p>
 * <b>Which fixture is real.</b> {@code viewform-live-capture.html} is a VERBATIM capture of a live
 * Google viewform page, fetched anonymously on 2026-08-24 during the first end-to-end {@code /link}
 * run (a throwaway intake form owned by the clinic test account, registered through the real
 * Picker/Drive/Forms chain). It settles what the earlier synthetic fixture could only assume: the
 * item list really does live at {@code root[1][1]}, an item's prefill id really does sit at
 * {@code item[4][0][0]}, and a non-question item really does hold null there. The ids asserted
 * below were cross-checked against the registry row the live run wrote, so the fixture and the
 * production path agree on the same five slots.
 * <p>
 * <b>Which fixture is synthetic, and why it stays.</b> {@code viewform-hazards.html} is hand-built.
 * Real captures are clean — the live page carries no bracket inside question text and no escaped
 * quote anywhere in the load-data array — so a live capture alone cannot exercise the bracket
 * balancer or the string-literal handling at all. The hazard fixture carries two DELIBERATELY
 * unmatched brackets, one in a plain string and one between escaped quotes. They are load-bearing:
 * with every bracket balanced, those tests pass even against a parser that ignores string literals
 * entirely. Confirmed by mutation — do not "fix" them.
 * <p>
 * The loud-failure fixtures stay synthetic for the same reason: a form that is not publicly
 * fillable serves a redirect stub, and registering such a form silently would mint prefill links
 * that can never work.
 */
public class GoogleFormsRelayClientParseTest {
	
	/**
	 * The live form's five items in order: Reference code (added by /link at index 0), Age, a
	 * title-and-description item, Chief complaint, Name.
	 */
	private static final List<String> EXPECTED_LIVE_ENTRY_IDS = Arrays.asList("471888886", "2010899856", null, "1032911868",
	    "2144456462");
	
	/** Item order in the synthetic hazard fixture, which exercises every parser branch. */
	private static final List<String> EXPECTED_HAZARD_ENTRY_IDS = Arrays.asList("2000001", "2000002", null, "2000003",
	    "2000004", null);
	
	// ---------------------------------------------------------------- happy path (real page)
	
	@Test
	public void parseEntryIds_returnsOneEntryPerItemInFormOrder() {
		List<String> entryIds = GoogleFormsRelayClient.parseEntryIds(fixture("viewform-live-capture.html"));
		assertEquals("one slot per form item, in item order", EXPECTED_LIVE_ENTRY_IDS, entryIds);
	}
	
	@Test
	public void parseEntryIds_findsTheReferenceQuestionFirstOnALiveLinkedForm() {
		List<String> entryIds = GoogleFormsRelayClient.parseEntryIds(fixture("viewform-live-capture.html"));
		// /link inserts the reference question at index 0, and the share URL is built from whatever
		// id lands in that slot — if this moves, every prefilled token goes into the wrong field.
		assertEquals("471888886", entryIds.get(0));
	}
	
	@Test
	public void parseEntryIds_keepsIndexAlignmentAcrossNonQuestionItems() {
		List<String> entryIds = GoogleFormsRelayClient.parseEntryIds(fixture("viewform-live-capture.html"));
		// The list is index-correlated against the Forms API item list, so a title-and-description
		// item must still occupy its slot — collapsing it would shift every id after it.
		assertEquals("length must equal the item count, not the question count", 5, entryIds.size());
		assertNull("title-and-description item carries no prefill id", entryIds.get(2));
		assertNotNull("the question after it keeps its own id", entryIds.get(3));
		assertEquals("1032911868", entryIds.get(3));
	}
	
	// ---------------------------------------------------------------- string-literal hazards
	
	@Test
	public void parseEntryIds_isNotTruncatedByBracketsInsideQuestionText() {
		List<String> entryIds = GoogleFormsRelayClient.parseEntryIds(fixture("viewform-hazards.html"));
		// Three items carry '[' or ']' in their own title/description. A naive scan that counted
		// brackets inside string literals would close the array early and lose the later items.
		assertEquals("parser reached the final item despite brackets in question text", 6, entryIds.size());
		assertEquals("2000002", entryIds.get(1));
		assertEquals("2000004", entryIds.get(4));
	}
	
	@Test
	public void parseEntryIds_treatsEscapedQuotesAsPartOfTheString() {
		List<String> entryIds = GoogleFormsRelayClient.parseEntryIds(fixture("viewform-hazards.html"));
		// Item 1's title contains \" and item 4's options contain \" — mishandling the escape would
		// flip the in-string flag and corrupt bracket depth for everything that follows.
		assertEquals("2000002", entryIds.get(1));
		assertEquals("2000004", entryIds.get(4));
		assertEquals(EXPECTED_HAZARD_ENTRY_IDS, entryIds);
	}
	
	@Test
	public void parseEntryIds_keepsIndexAlignmentAcrossEveryNonQuestionItemType() {
		List<String> entryIds = GoogleFormsRelayClient.parseEntryIds(fixture("viewform-hazards.html"));
		// The live capture only contains a title-and-description item; images and section breaks
		// have to come from the synthetic fixture.
		assertEquals(EXPECTED_HAZARD_ENTRY_IDS, entryIds);
		assertNull("section header carries no prefill id", entryIds.get(2));
		assertNull("image item carries no prefill id", entryIds.get(5));
	}
	
	// ---------------------------------------------------------------- loud failures
	
	@Test
	public void parseEntryIds_failsLoudlyWhenThePageHasNoLoadData() {
		// A form that is not publicly fillable serves a sign-in page instead. Silence here would
		// register a form whose links can never be prefilled.
		assertLoudFailure("viewform-no-marker.html", "prefill data");
	}
	
	@Test
	public void parseEntryIds_failsLoudlyWhenTheArrayIsTruncated() {
		assertLoudFailure("viewform-truncated.html", "ended mid-way");
	}
	
	@Test
	public void parseEntryIds_failsLoudlyWhenTheItemSlotIsNotAList() {
		assertLoudFailure("viewform-items-not-array.html", "no question list");
	}
	
	private static void assertLoudFailure(String fixtureName, String expectedInMessage) {
		try {
			GoogleFormsRelayClient.parseEntryIds(fixture(fixtureName));
			fail(fixtureName + " should not have parsed");
		}
		catch (ClinicFormException e) {
			assertTrue("message should say what went wrong and point at the manual fallback, was: " + e.getMessage(), e
			        .getMessage().contains(expectedInMessage));
		}
	}
	
	// ---------------------------------------------------------------- helpers
	
	private static String fixture(String name) {
		String path = "/clinicform/" + name;
		try (InputStream in = GoogleFormsRelayClientParseTest.class.getResourceAsStream(path)) {
			assertNotNull("missing test fixture " + path, in);
			try (Scanner scanner = new Scanner(in, "UTF-8").useDelimiter("\\A")) {
				return scanner.hasNext() ? scanner.next() : "";
			}
		}
		catch (Exception e) {
			throw new IllegalStateException("Could not read fixture " + path, e);
		}
	}
}
