package net.sf.taverna.t2.activities.rest;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.*;

import static org.junit.Assert.*;

public class URISignatureHandlerTest {
	// ==========================================================================
	// TEST URIs
	// ==========================================================================

	final String validURI_NoPlaceholders = "http://sysmo-db.org/sops/";
	final String validURI_PlaceholdersInMainPartOfURIOnly = "http://sysmo-db.org/sops/{sop_id}/experimental_conditions/{cond_id}";
	final String validURI_PlaceholdersInQueryStringOnly = "http://sandbox.myexperiment.org/user.xml?id={user_id}&verbose=true";
	final String validURI_3MixedPlaceholders = "http://sysmo-db.org/sops/{sop_id}/experimental_conditions/{cond_id}?condition_unit={unit}";

	final String badURI_nullURI = null;
	final String badURI_emptyURI = "";
	final String badURI_SingleOpeningSymbolNoClosingSymbol = "http://sysmo-db.org/sops/{sop_id/experimental_conditions";
	final String badURI_SingleClosingSymbolNoOpeningSymbol = "http://sysmo-db.org/sops/sop_id}/experimental_conditions";
	final String badURI_DoubleOpeningSymbolsNoClosingSymbol = "http://sysmo-db.org/sops/{{sop_id/experimental_conditions";
	final String badURI_DoubleOpeningSymbols = "http://sysmo-db.org/sops/{{sop_id}/experimental_conditions";
	final String badURI_DoubleOpeningSymbolsSpaced = "http://sysmo-db.org/sops/{sop_{id}/experimental_conditions";
	final String badURI_DoubleClosingSymbols = "http://sysmo-db.org/sops/{sop_id}}/experimental_conditions";
	final String badURI_DoubleClosingSymbolsSpaced = "http://sysmo-db.org/sops/{sop}_id}/experimental_conditions";
	final String badURI_NestedPlaceholders = "http://sandbox.myexperiment.org/user.xml?id={user_{id}}&verbose=true";
	final String badURI_NestedPlaceholdersSpaced = "http://sandbox.myexperiment.org/user.xml?id={us{er}_id}&verbose=true";
	final String badURI_DuplicatePlaceholders = "http://sandbox.myexperiment.org/user.xml?id={user_id}&verbose={user_id}";
	final String badURI_DuplicatePlaceholdersWithOthers = "http://sysmo-db.org/sops/{unit}/experimental_conditions/{cond_id}?condition_unit={unit}";
	
     final String validURI_MultipleQueryString =
            "http://dr-site.esrin.esa.int/{catalogue}/genesi/ASA_IMS_1P/rdf/?count={count?}&startPage={startPage?}&startIndex={startIndex?}&q={searchTerms?}"; 	

	// ==========================================================================
	// TEST URI SIGNATURE BOOLEAN VALIDATION
	// ==========================================================================

	// success cases

	@Test
	public void isValid_validURI_NoPlaceholders() {
		assertTrue(URISignatureHandler.isValid(validURI_NoPlaceholders));
	}

	@Test
	public void isValid_validURI_PlaceholdersInMainPartOfURIOnly() {
		assertTrue(URISignatureHandler
				.isValid(validURI_PlaceholdersInMainPartOfURIOnly));
	}

	@Test
	public void isValid_validURI_PlaceholdersInQueryStringOnly() {
		assertTrue(URISignatureHandler
				.isValid(validURI_PlaceholdersInQueryStringOnly));
	}

	@Test
	public void isValid_validURI_MixedPlaceholders() {
		assertTrue(URISignatureHandler.isValid(validURI_3MixedPlaceholders));
	}

 	@Test
 	public void isValid_validURI_MultipleQueryString() {
 		assertTrue(URISignatureHandler.isValid(validURI_MultipleQueryString));
 	}
 	
	// failure cases

	@Test
	public void isValid_badURI_nullURI() {
		assertFalse(URISignatureHandler.isValid(badURI_nullURI));
	}

	@Test
	public void isValid_badURI_emptyURI() {
		assertFalse(URISignatureHandler.isValid(badURI_emptyURI));
	}

	@Test
	public void isValid_badURI_SingleOpeningSymbolNoClosingSymbol() {
		assertFalse(URISignatureHandler
				.isValid(badURI_SingleOpeningSymbolNoClosingSymbol));
	}

	@Test
	public void isValid_badURI_SingleClosingSymbolNoOpeningSymbol() {
		assertFalse(URISignatureHandler
				.isValid(badURI_SingleClosingSymbolNoOpeningSymbol));
	}

	@Test
	public void isValid_badURI_DoubleOpeningSymbolsNoClosingSymbol() {
		assertFalse(URISignatureHandler
				.isValid(badURI_DoubleOpeningSymbolsNoClosingSymbol));
	}

	@Test
	public void isValid_badURI_DoubleOpeningSymbols() {
		assertFalse(URISignatureHandler.isValid(badURI_DoubleOpeningSymbols));
	}

	@Test
	public void isValid_badURI_DoubleOpeningSymbolsSpaced() {
		assertFalse(URISignatureHandler
				.isValid(badURI_DoubleOpeningSymbolsSpaced));
	}

	@Test
	public void isValid_badURI_DoubleClosingSymbols() {
		assertFalse(URISignatureHandler.isValid(badURI_DoubleClosingSymbols));
	}

	@Test
	public void isValid_badURI_DoubleClosingSymbolsSpaced() {
		assertFalse(URISignatureHandler
				.isValid(badURI_DoubleClosingSymbolsSpaced));
	}

	@Test
	public void isValid_badURI_NestedPlaceholders() {
		assertFalse(URISignatureHandler.isValid(badURI_NestedPlaceholders));
	}

	@Test
	public void isValid_badURI_NestedPlaceholdersSpaced() {
		assertFalse(URISignatureHandler
				.isValid(badURI_NestedPlaceholdersSpaced));
	}

	@Test
	public void isValid_badURI_DuplicatePlaceholders() {
		assertFalse(URISignatureHandler.isValid(badURI_DuplicatePlaceholders));
	}

	@Test
	public void isValid_badURI_DuplicatePlaceholdersWithOthers() {
		assertFalse(URISignatureHandler
				.isValid(badURI_DuplicatePlaceholdersWithOthers));
	}

	// ==========================================================================
	// TEST URI SIGNATURE DETAILED VALIDATION
	// ==========================================================================

	// success cases

	@Test
	public void validate_validURI_NoPlaceholders() {
		// nothing should happen when this is executed if validation succeeds
		URISignatureHandler.validate(validURI_NoPlaceholders);
	}

	@Test
	public void validate_validURI_PlaceholdersInMainPartOfURIOnly() {
		// nothing should happen when this is executed if validation succeeds
		URISignatureHandler.validate(validURI_PlaceholdersInMainPartOfURIOnly);
	}

	@Test
	public void validate_validURI_PlaceholdersInQueryStringOnly() {
		// nothing should happen when this is executed if validation succeeds
		URISignatureHandler.validate(validURI_PlaceholdersInQueryStringOnly);
	}

	@Test
	public void validate_validURI_MixedPlaceholders() {
		// nothing should happen when this is executed if validation succeeds
		URISignatureHandler.validate(validURI_3MixedPlaceholders);
	}

 	@Test
 	public void validate_validURI_validURI_Multiple() {
 		URISignatureHandler.validate(validURI_MultipleQueryString);
 	}
	
	// failure cases

	@Test(expected = URISignatureHandler.URISignatureParsingException.class)
	public void validate_badURI_nullURI() {
		URISignatureHandler.validate(badURI_nullURI);
	}

	@Test(expected = URISignatureHandler.URISignatureParsingException.class)
	public void validate_badURI_emptyURI() {
		URISignatureHandler.validate(badURI_emptyURI);
	}

	@Test(expected = URISignatureHandler.URISignatureParsingException.class)
	public void validate_badURI_SingleOpeningSymbolNoClosingSymbol() {
		URISignatureHandler.validate(badURI_SingleOpeningSymbolNoClosingSymbol);
	}

	@Test(expected = URISignatureHandler.URISignatureParsingException.class)
	public void validate_badURI_SingleClosingSymbolNoOpeningSymbol() {
		URISignatureHandler.validate(badURI_SingleClosingSymbolNoOpeningSymbol);
	}

	@Test(expected = URISignatureHandler.URISignatureParsingException.class)
	public void validate_badURI_DoubleOpeningSymbolsNoClosingSymbol() {
		URISignatureHandler
				.validate(badURI_DoubleOpeningSymbolsNoClosingSymbol);
	}

	@Test(expected = URISignatureHandler.URISignatureParsingException.class)
	public void validate_badURI_DoubleOpeningSymbols() {
		URISignatureHandler.validate(badURI_DoubleOpeningSymbols);
	}

	@Test(expected = URISignatureHandler.URISignatureParsingException.class)
	public void validate_badURI_DoubleOpeningSymbolsSpaced() {
		URISignatureHandler.validate(badURI_DoubleOpeningSymbolsSpaced);
	}

	@Test(expected = URISignatureHandler.URISignatureParsingException.class)
	public void validate_badURI_DoubleClosingSymbols() {
		URISignatureHandler.validate(badURI_DoubleClosingSymbols);
	}

	@Test(expected = URISignatureHandler.URISignatureParsingException.class)
	public void validate_badURI_DoubleClosingSymbolsSpaced() {
		URISignatureHandler.validate(badURI_DoubleClosingSymbolsSpaced);
	}

	@Test(expected = URISignatureHandler.URISignatureParsingException.class)
	public void validate_badURI_NestedPlaceholders() {
		URISignatureHandler.validate(badURI_NestedPlaceholders);
	}

	@Test(expected = URISignatureHandler.URISignatureParsingException.class)
	public void validate_badURI_NestedPlaceholdersSpaced() {
		URISignatureHandler.validate(badURI_NestedPlaceholdersSpaced);
	}

	@Test(expected = URISignatureHandler.URISignatureParsingException.class)
	public void validate_badURI_DuplicatePlaceholders() {
		URISignatureHandler.validate(badURI_DuplicatePlaceholders);
	}

	@Test(expected = URISignatureHandler.URISignatureParsingException.class)
	public void validate_badURI_DuplicatePlaceholdersWithOthers() {
		URISignatureHandler.validate(badURI_DuplicatePlaceholdersWithOthers);
	}

	// ==========================================================================
	// TEST PLACEHOLDER EXTRACTION FROM URI SIGNATURE
	// ==========================================================================

	// success cases

	@Test
	public void extractPlaceholders_validURI_NoPlaceholders() {
		List<String> placeholders = URISignatureHandler
				.extractPlaceholders(validURI_NoPlaceholders);
		assertNotNull(placeholders);
		assertEquals(0, placeholders.size());
	}

	@Test
	public void extractPlaceholders_validURI_PlaceholdersInMainPartOfURIOnly() {
		List<String> placeholders = URISignatureHandler
				.extractPlaceholders(validURI_PlaceholdersInMainPartOfURIOnly);
		assertNotNull(placeholders);
		assertEquals(2, placeholders.size());
		assertEquals("Wrong first placeholder", "sop_id", placeholders.get(0));
		assertEquals("Wrong second placeholder", "cond_id", placeholders.get(1));
	}

	@Test
	public void extractPlaceholders_validURI_PlaceholdersInQueryStringOnly() {
		List<String> placeholders = URISignatureHandler
				.extractPlaceholders(validURI_PlaceholdersInQueryStringOnly);
		assertNotNull(placeholders);
		assertEquals(1, placeholders.size());
		assertEquals("Wrong first placeholder", "user_id", placeholders.get(0));
	}

	@Test
	public void extractPlaceholders_validURI_MixedPlaceholders() {
		List<String> placeholders = URISignatureHandler
				.extractPlaceholders(validURI_3MixedPlaceholders);
		assertNotNull(placeholders);
		assertEquals("Wrong number of placeholders extracted", 3, placeholders
				.size());
		assertEquals("Wrong first placeholder", "sop_id", placeholders.get(0));
		assertEquals("Wrong second placeholder", "cond_id", placeholders.get(1));
		assertEquals("Wrong third placeholder", "unit", placeholders.get(2));
	}

	@Test
 	public void extractPlaceholders_validURI_MultipleQueryString() {
 		List<String> placeholders = URISignatureHandler
 				.extractPlaceholders(validURI_MultipleQueryString);
 		assertNotNull(placeholders);
 		assertEquals(5, placeholders.size());
 		assertEquals("Wrong first placeholder", "catalogue", placeholders.get(0));
 		assertEquals("Wrong second placeholder", "count?", placeholders.get(1));
 		assertEquals("Wrong third placeholder", "startPage?", placeholders.get(2));
 		assertEquals("Wrong fourth placeholder", "startIndex?", placeholders.get(3));
 		assertEquals("Wrong fifth placeholder", "searchTerms?", placeholders.get(4));
 	}
	
	// failure cases

	/*
	 * These tests are all meant to generate an exception - therefore, no need
	 * to evaluate generated values, as there will be none returned.
	 */

	@Test(expected = URISignatureHandler.URISignatureParsingException.class)
	public void extractPlaceholders_badURI_nullURI() {
		URISignatureHandler.extractPlaceholders(badURI_nullURI);
	}

	@Test(expected = URISignatureHandler.URISignatureParsingException.class)
	public void extractPlaceholders_badURI_emptyURI() {
		URISignatureHandler.extractPlaceholders(badURI_emptyURI);
	}

	@Test(expected = URISignatureHandler.URISignatureParsingException.class)
	public void extractPlaceholders_badURI_SingleOpeningSymbolNoClosingSymbol() {
		URISignatureHandler
				.extractPlaceholders(badURI_SingleOpeningSymbolNoClosingSymbol);
	}

	@Test(expected = URISignatureHandler.URISignatureParsingException.class)
	public void extractPlaceholders_badURI_SingleClosingSymbolNoOpeningSymbol() {
		URISignatureHandler
				.extractPlaceholders(badURI_SingleClosingSymbolNoOpeningSymbol);
	}

	@Test(expected = URISignatureHandler.URISignatureParsingException.class)
	public void extractPlaceholders_badURI_DoubleOpeningSymbolsNoClosingSymbol() {
		URISignatureHandler
				.extractPlaceholders(badURI_DoubleOpeningSymbolsNoClosingSymbol);
	}

	@Test(expected = URISignatureHandler.URISignatureParsingException.class)
	public void extractPlaceholders_badURI_DoubleOpeningSymbols() {
		URISignatureHandler.extractPlaceholders(badURI_DoubleOpeningSymbols);
	}

	@Test(expected = URISignatureHandler.URISignatureParsingException.class)
	public void extractPlaceholders_badURI_DoubleOpeningSymbolsSpaced() {
		URISignatureHandler
				.extractPlaceholders(badURI_DoubleOpeningSymbolsSpaced);
	}

	@Test(expected = URISignatureHandler.URISignatureParsingException.class)
	public void extractPlaceholders_badURI_DoubleClosingSymbols() {
		URISignatureHandler.extractPlaceholders(badURI_DoubleClosingSymbols);
	}

	@Test(expected = URISignatureHandler.URISignatureParsingException.class)
	public void extractPlaceholders_badURI_DoubleClosingSymbolsSpaced() {
		URISignatureHandler
				.extractPlaceholders(badURI_DoubleClosingSymbolsSpaced);
	}

	@Test(expected = URISignatureHandler.URISignatureParsingException.class)
	public void extractPlaceholders_badURI_NestedPlaceholders() {
		URISignatureHandler.extractPlaceholders(badURI_NestedPlaceholders);
	}

	@Test(expected = URISignatureHandler.URISignatureParsingException.class)
	public void extractPlaceholders_badURI_NestedPlaceholdersSpaced() {
		URISignatureHandler
				.extractPlaceholders(badURI_NestedPlaceholdersSpaced);
	}

	@Test(expected = URISignatureHandler.URISignatureParsingException.class)
	public void extractPlaceholders_badURI_DuplicatePlaceholders() {
		URISignatureHandler.extractPlaceholders(badURI_DuplicatePlaceholders);
	}

	@Test(expected = URISignatureHandler.URISignatureParsingException.class)
	public void extractPlaceholders_badURI_DuplicatePlaceholdersWithOthers() {
		URISignatureHandler
				.extractPlaceholders(badURI_DuplicatePlaceholdersWithOthers);
	}

	// ==========================================================================
	// TEST COMPLETE URI GENERATION FROM URI SIGNATURE + PARAMETERS
	// ==========================================================================

	// success cases

	@SuppressWarnings("serial")
	@Test
	public void generateCompleteURI_successfulURIGeneration() {
		String uriSignature = "http://sysmo-db.org/sops/{sop_id}/experimental_conditions/{cond_id}?condition_unit={unit}";
		Map<String, String> parameters = new HashMap<String, String>() {
			{
				put("sop_id", "111");
				put("unit", "33");
				put("cond_id", "2222");
			}
		};

		String completeURI = URISignatureHandler.generateCompleteURI(
				uriSignature, parameters, true);
		assertEquals(
				"http://sysmo-db.org/sops/111/experimental_conditions/2222?condition_unit=33",
				completeURI);
	}

	@SuppressWarnings("serial")
	@Test
	public void generateCompleteURI_successfulURIGeneration_URLParameterEscaping() {
		String uriSignature = "http://sysmo-db.org/sops/{sop_id}/experimental_conditions/{cond_id}?condition_unit={unit}";
		Map<String, String> parameters = new HashMap<String, String>() {
			{
				put("sop_id", "1 11");
				put("unit", "3;3");
				put("cond_id", "2/2$2&2:");
			}
		};

		String completeURI = URISignatureHandler.generateCompleteURI(
				uriSignature, parameters, true);
		System.err.println(completeURI);
		assertEquals(
				"http://sysmo-db.org/sops/1%2011/experimental_conditions/2%2F2%242%262%3A?condition_unit=3%3B3",
				completeURI);
	}

	@SuppressWarnings("serial")
	@Test
	public void generateCompleteURI_successfulURIGeneration_noURLParameterEscaping() {
		String uriSignature = "http://sysmo-db.org/sops/{sop_id}/experimental_conditions/{cond_id}?condition_unit={unit}";
		Map<String, String> parameters = new HashMap<String, String>() {
			{
				put("sop_id", "1 11");
				put("unit", "3;3");
				put("cond_id", "2/2$2&2:");
			}
		};

		String completeURI = URISignatureHandler.generateCompleteURI(
				uriSignature, parameters, false);
		assertEquals(
				"http://sysmo-db.org/sops/1 11/experimental_conditions/2/2$2&2:?condition_unit=3;3",
				completeURI);
	}
	
 	@SuppressWarnings("serial")
 	@Test
 	public void generateCompleteURI_successfulURIGeneration_optionalParams() {
 		String uriSignature = "http://dr-site.esrin.esa.int/{catalogue}/genesi/ASA_IMS_1P/rdf/?count={count?}&startPage={startPage?}&startIndex={startIndex?}&q={searchTerms?}";
 		Map<String, String> allParameters = new HashMap<String, String>() {
 			{
 				put("catalogue", "catalogue");
 				put("count?", "10");
 				put("startPage?", "1");
 				put("startIndex?", "1");
 				put("searchTerms?", "term1");
 			}
 		};
 
 		Map<String, String> parametersMissingOptional = new HashMap<String, String>() {
 			{
 				put("catalogue", "catalogue");
 				put("count?", "10");
 				put("searchTerms?", "term1");
 			}
 		};
 
 		Map<String, String> parametersMissingFirstOptional = new HashMap<String, String>() {
 			{
 				put("catalogue", "catalogue");
 				put("startPage?", "1");
 				put("startIndex?", "1");
 				put("searchTerms?", "term1");
 			}
 		};
 
 		String completeURI1 = URISignatureHandler.generateCompleteURI(
 				uriSignature, allParameters, false);
 		assertEquals(
				"http://dr-site.esrin.esa.int/catalogue/genesi/ASA_IMS_1P/rdf/?count=10&startPage=1&startIndex=1&q=term1",
 				completeURI1);
 
 		String completeURI2 = URISignatureHandler.generateCompleteURI(
 				uriSignature, parametersMissingOptional, false);
 		assertEquals(
 				"http://dr-site.esrin.esa.int/catalogue/genesi/ASA_IMS_1P/rdf/?count=10&q=term1",
 				completeURI2);
 
 		String completeURI3 = URISignatureHandler.generateCompleteURI(
 				uriSignature, parametersMissingFirstOptional, false);
 		assertEquals(
 				"http://dr-site.esrin.esa.int/catalogue/genesi/ASA_IMS_1P/rdf/?startPage=1&startIndex=1&q=term1",
 				completeURI3);
 	}
 
	@Test
	public void generateCompleteURI_signatureWithNoPlaceholders_nullParameterMap() {
		String completeURI = URISignatureHandler.generateCompleteURI(
				validURI_NoPlaceholders, null, true);
		assertEquals(validURI_NoPlaceholders, completeURI);
	}

	@Test
	public void generateCompleteURI_signatureWithNoPlaceholders_emptyParameterMap() {
		String completeURI = URISignatureHandler.generateCompleteURI(
				validURI_NoPlaceholders, Collections
						.<String, String> emptyMap(), true);
		assertEquals(validURI_NoPlaceholders, completeURI);
	}

	// failure cases

	@Test(expected = URISignatureHandler.URIGenerationFromSignatureException.class)
	public void generateCompleteURI_signatureWithPlaceholders_nullParameterMap() {
		URISignatureHandler.generateCompleteURI(validURI_3MixedPlaceholders,
				null, true);
	}

	@Test(expected = URISignatureHandler.URIGenerationFromSignatureException.class)
	public void generateCompleteURI_signatureWithPlaceholders_emptyParameterMap() {
		URISignatureHandler.generateCompleteURI(validURI_3MixedPlaceholders,
				Collections.<String, String> emptyMap(), true);
	}

	@SuppressWarnings("serial")
	@Test
	public void generateCompleteURI_signatureWithPlaceholders_missingParameterURIGeneration_FailureNotExpected() {
		String uriSignature = "http://sysmo-db.org/sops/{sop_id}/experimental_conditions/{cond_id}?condition_unit={unit}";
		Map<String, String> parameters = new HashMap<String, String>() {
			{
				put("sop_id", "111");
				put("cond_id", "2222");
			}
		};

		String completeURI = URISignatureHandler.generateCompleteURI(
				uriSignature, parameters, true);

		assertEquals(
				"http://sysmo-db.org/sops/111/experimental_conditions/2222",
				completeURI);
	}

	// the following failure case is just to confirm the validation of the URI
	// signature by the validation mechanism, which is the same for
	// generateCompleteURI()
	// and extractPlaceholders()

	@SuppressWarnings("serial")
	@Test(expected = URISignatureHandler.URISignatureParsingException.class)
	public void generateCompleteURI_duplicatePlaceholderURIGeneration_FailureExpected() {
		String uriSignature = "http://sysmo-db.org/sops/{sop_id}/experimental_conditions/{cond_id}?condition_unit={sop_id}";
		Map<String, String> parameters = new HashMap<String, String>() {
			{
				put("sop_id", "111");
				put("unit", "33");
				put("cond_id", "2222");
			}
		};

		String completeURI = URISignatureHandler.generateCompleteURI(
				uriSignature, parameters, true);

		assertEquals(
				"http://sysmo-db.org/sops/111/experimental_conditions/2222?condition_unit=33",
				completeURI);
	}

	@SuppressWarnings("serial")
 	@Test(expected = URISignatureHandler.URIGenerationFromSignatureException.class)
 	public void generateCompleteURI_failureURIGeneration_optionalParams() {
 		String uriSignature = "http://dr-site.esrin.esa.int/{catalogue}/genesi/ASA_IMS_1P/rdf/?count={count?}&startPage={startPage?}&startIndex={startIndex?}&q={searchTerms?}";
 
 		Map<String, String> parametersMissingCompulsory = new HashMap<String, String>() {
 			{
 				put("count?", "10");
 				put("startPage?", "1");
 				put("startIndex?", "1");
 				put("searchTerms?", "term1");
 			}
 		};
 
 		String completeURI = URISignatureHandler.generateCompleteURI(
 				uriSignature, parametersMissingCompulsory, false);
 
 		assertEquals(
 				"http://dr-site.esrin.esa.int/catalogue/genesi/ASA_IMS_1P/rdf/?count={count?}&startPage={startPage?}&startIndex={startIndex?}&q={searchTerms?}",
 				completeURI);
 	}
}
