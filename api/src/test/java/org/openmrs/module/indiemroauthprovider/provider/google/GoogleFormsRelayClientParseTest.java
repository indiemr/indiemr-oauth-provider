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
 * <b>What these fixtures do and do not prove.</b> They encode the array shape the implementation
 * assumes — {@code root[1][1]} is the item list, an item's prefill id sits at {@code item[4][0][0]}
 * , and a non-question item holds null there. That shape comes from the format's common public
 * description and the intake PoC's report that scraping matched hand-captured ids; it was NOT
 * captured from a live Google page in this test's authorship. So these tests lock in the parser's
 * behaviour against regressions and prove the bracket balancer and the loud-failure paths work —
 * they do NOT independently verify Google's real layout. A live /link run remains the only proof of
 * that, and if the real shape differs the parser fails loud with a manual-entry fallback rather
 * than mis-mapping. Replace the happy-path fixture with a real captured page when one is available.
 * <p>
 * The happy fixture carries two DELIBERATELY unmatched brackets — one in a plain string, one
 * between escaped quotes. They are load-bearing: with every bracket balanced, these tests pass even
 * against a parser that ignores string literals entirely. Confirmed by mutation.
 */
public class GoogleFormsRelayClientParseTest {
	
	/** Item order in the happy fixture, mirroring a form that exercises every parser branch. */
	private static final List<String> EXPECTED_ENTRY_IDS = Arrays.asList("2000001", "2000002", null, "2000003", "2000004",
	    null);
	
	// ---------------------------------------------------------------- happy path
	
	@Test
	public void parseEntryIds_returnsOneEntryPerItemInFormOrder() {
		List<String> entryIds = GoogleFormsRelayClient.parseEntryIds(fixture("viewform-happy.html"));
		assertEquals("one slot per form item, in item order", EXPECTED_ENTRY_IDS, entryIds);
	}
	
	@Test
	public void parseEntryIds_keepsIndexAlignmentAcrossNonQuestionItems() {
		List<String> entryIds = GoogleFormsRelayClient.parseEntryIds(fixture("viewform-happy.html"));
		// The list is index-correlated against the Forms API item list, so a section header and an
		// image must still occupy their slot — collapsing them would shift every id after them.
		assertEquals("length must equal the item count, not the question count", 6, entryIds.size());
		assertNull("section header carries no prefill id", entryIds.get(2));
		assertNull("image item carries no prefill id", entryIds.get(5));
		assertNotNull("the question after the section header keeps its own id", entryIds.get(3));
		assertEquals("2000003", entryIds.get(3));
	}
	
	// ---------------------------------------------------------------- string-literal hazards
	
	@Test
	public void parseEntryIds_isNotTruncatedByBracketsInsideQuestionText() {
		List<String> entryIds = GoogleFormsRelayClient.parseEntryIds(fixture("viewform-happy.html"));
		// Three items carry '[' or ']' in their own title/description. A naive scan that counted
		// brackets inside string literals would close the array early and lose the later items.
		assertEquals("parser reached the final item despite brackets in question text", 6, entryIds.size());
		assertEquals("2000002", entryIds.get(1));
		assertEquals("2000004", entryIds.get(4));
	}
	
	@Test
	public void parseEntryIds_treatsEscapedQuotesAsPartOfTheString() {
		List<String> entryIds = GoogleFormsRelayClient.parseEntryIds(fixture("viewform-happy.html"));
		// Item 1's title contains \" and item 4's options contain \" — mishandling the escape would
		// flip the in-string flag and corrupt bracket depth for everything that follows.
		assertEquals("2000002", entryIds.get(1));
		assertEquals("2000004", entryIds.get(4));
		assertEquals(EXPECTED_ENTRY_IDS, entryIds);
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
