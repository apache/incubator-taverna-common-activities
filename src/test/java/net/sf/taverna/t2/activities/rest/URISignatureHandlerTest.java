package net.sf.taverna.t2.activities.rest;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.taverna.t2.activities.rest.URISignatureHandler.URISignatureParsingException;
import net.sf.taverna.t2.activities.rest.URISignatureHandler.URIGenerationFromSignatureException;

import org.junit.*;

import static org.junit.Assert.*;

public class URISignatureHandlerTest
{
  //==========================================================================
  //                               TEST URIs
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
  
  
  
  // ==========================================================================
  //                      TEST URI SIGNATURE BOOLEAN VALIDATION
  // ==========================================================================
  
  // success cases
  
  @Test
  public void isValid_validURI_NoPlaceholders() {
    assertTrue(URISignatureHandler.isValid(validURI_NoPlaceholders));
  }
  
  @Test
  public void isValid_validURI_PlaceholdersInMainPartOfURIOnly() {
    assertTrue(URISignatureHandler.isValid(validURI_PlaceholdersInMainPartOfURIOnly));
  }
  
  @Test
  public void isValid_validURI_PlaceholdersInQueryStringOnly() {
    assertTrue(URISignatureHandler.isValid(validURI_PlaceholdersInQueryStringOnly));
  }
  
  @Test
  public void isValid_validURI_MixedPlaceholders() {
    assertTrue(URISignatureHandler.isValid(validURI_3MixedPlaceholders));
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
    assertFalse(URISignatureHandler.isValid(badURI_SingleOpeningSymbolNoClosingSymbol));
  }
  
  @Test
  public void isValid_badURI_SingleClosingSymbolNoOpeningSymbol() {
    assertFalse(URISignatureHandler.isValid(badURI_SingleClosingSymbolNoOpeningSymbol));
  }
  
  @Test
  public void isValid_badURI_DoubleOpeningSymbolsNoClosingSymbol() {
    assertFalse(URISignatureHandler.isValid(badURI_DoubleOpeningSymbolsNoClosingSymbol));
  }
  
  @Test
  public void isValid_badURI_DoubleOpeningSymbols() {
    assertFalse(URISignatureHandler.isValid(badURI_DoubleOpeningSymbols));
  }
  
  @Test
  public void isValid_badURI_DoubleOpeningSymbolsSpaced() {
    assertFalse(URISignatureHandler.isValid(badURI_DoubleOpeningSymbolsSpaced));
  }
  
  @Test
  public void isValid_badURI_DoubleClosingSymbols() {
    assertFalse(URISignatureHandler.isValid(badURI_DoubleClosingSymbols));
  }
  
  @Test
  public void isValid_badURI_DoubleClosingSymbolsSpaced() {
    assertFalse(URISignatureHandler.isValid(badURI_DoubleClosingSymbolsSpaced));
  }
  
  @Test
  public void isValid_badURI_NestedPlaceholders() {
    assertFalse(URISignatureHandler.isValid(badURI_NestedPlaceholders));
  }
  
  @Test
  public void isValid_badURI_NestedPlaceholdersSpaced() {
    assertFalse(URISignatureHandler.isValid(badURI_NestedPlaceholdersSpaced));
  }
  
  @Test
  public void isValid_badURI_DuplicatePlaceholders() {
    assertFalse(URISignatureHandler.isValid(badURI_DuplicatePlaceholders));
  }
  
  @Test
  public void isValid_badURI_DuplicatePlaceholdersWithOthers() {
    assertFalse(URISignatureHandler.isValid(badURI_DuplicatePlaceholdersWithOthers));
  }
  
  
  
  // ==========================================================================
  //                    TEST URI SIGNATURE DETAILED VALIDATION
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
  
  
  // failure cases
  
  @Test(expected=URISignatureHandler.URISignatureParsingException.class)
  public void validate_badURI_nullURI() {
    URISignatureHandler.validate(badURI_nullURI);
  }
  
  @Test(expected=URISignatureHandler.URISignatureParsingException.class)
  public void validate_badURI_emptyURI() {
    URISignatureHandler.validate(badURI_emptyURI);
  }
  
  @Test(expected=URISignatureHandler.URISignatureParsingException.class)
  public void validate_badURI_SingleOpeningSymbolNoClosingSymbol() {
    URISignatureHandler.validate(badURI_SingleOpeningSymbolNoClosingSymbol);
  }
  
  @Test(expected=URISignatureHandler.URISignatureParsingException.class)
  public void validate_badURI_SingleClosingSymbolNoOpeningSymbol() {
    URISignatureHandler.validate(badURI_SingleClosingSymbolNoOpeningSymbol);
  }
  
  @Test(expected=URISignatureHandler.URISignatureParsingException.class)
  public void validate_badURI_DoubleOpeningSymbolsNoClosingSymbol() {
    URISignatureHandler.validate(badURI_DoubleOpeningSymbolsNoClosingSymbol);
  }
  
  @Test(expected=URISignatureHandler.URISignatureParsingException.class)
  public void validate_badURI_DoubleOpeningSymbols() {
    URISignatureHandler.validate(badURI_DoubleOpeningSymbols);
  }
  
  @Test(expected=URISignatureHandler.URISignatureParsingException.class)
  public void validate_badURI_DoubleOpeningSymbolsSpaced() {
    URISignatureHandler.validate(badURI_DoubleOpeningSymbolsSpaced);
  }
  
  @Test(expected=URISignatureHandler.URISignatureParsingException.class)
  public void validate_badURI_DoubleClosingSymbols() {
    URISignatureHandler.validate(badURI_DoubleClosingSymbols);
  }
  
  @Test(expected=URISignatureHandler.URISignatureParsingException.class)
  public void validate_badURI_DoubleClosingSymbolsSpaced() {
    URISignatureHandler.validate(badURI_DoubleClosingSymbolsSpaced);
  }
  
  @Test(expected=URISignatureHandler.URISignatureParsingException.class)
  public void validate_badURI_NestedPlaceholders() {
    URISignatureHandler.validate(badURI_NestedPlaceholders);
  }
  
  @Test(expected=URISignatureHandler.URISignatureParsingException.class)
  public void validate_badURI_NestedPlaceholdersSpaced() {
    URISignatureHandler.validate(badURI_NestedPlaceholdersSpaced);
  }
  
  @Test(expected=URISignatureHandler.URISignatureParsingException.class)
  public void validate_badURI_DuplicatePlaceholders() {
    URISignatureHandler.validate(badURI_DuplicatePlaceholders);
  }
  
  @Test(expected=URISignatureHandler.URISignatureParsingException.class)
  public void validate_badURI_DuplicatePlaceholdersWithOthers() {
    URISignatureHandler.validate(badURI_DuplicatePlaceholdersWithOthers);
  }
  
  
  // ==========================================================================
  //          TEST PLACEHOLDER EXTRACTION FROM URI SIGNATURE
  // ==========================================================================
  
  // success cases
  
  @Test
  public void extractPlaceholders_validURI_NoPlaceholders() {
    List<String> placeholders = URISignatureHandler.extractPlaceholders(validURI_NoPlaceholders);
    assertNotNull(placeholders);
    assertEquals(0, placeholders.size());
  }
  
  @Test
  public void extractPlaceholders_validURI_PlaceholdersInMainPartOfURIOnly() {
    List<String> placeholders = URISignatureHandler.extractPlaceholders(validURI_PlaceholdersInMainPartOfURIOnly);
    assertNotNull(placeholders);
    assertEquals(2, placeholders.size());
    assertEquals("Wrong first placeholder", "sop_id", placeholders.get(0));
    assertEquals("Wrong second placeholder", "cond_id", placeholders.get(1));
  }
  
  @Test
  public void extractPlaceholders_validURI_PlaceholdersInQueryStringOnly() {
    List<String> placeholders = URISignatureHandler.extractPlaceholders(validURI_PlaceholdersInQueryStringOnly);
    assertNotNull(placeholders);
    assertEquals(1, placeholders.size());
    assertEquals("Wrong first placeholder", "user_id", placeholders.get(0));
  }
  
  @Test
  public void extractPlaceholders_validURI_MixedPlaceholders() {
    List<String> placeholders = URISignatureHandler.extractPlaceholders(validURI_3MixedPlaceholders);
    assertNotNull(placeholders);
    assertEquals("Wrong number of placeholders extracted", 3, placeholders.size());
    assertEquals("Wrong first placeholder", "sop_id", placeholders.get(0));
    assertEquals("Wrong second placeholder", "cond_id", placeholders.get(1));
    assertEquals("Wrong third placeholder", "unit", placeholders.get(2));
  }
  
  
  // failure cases
  
  /*
   * These tests are all meant to generate an exception - therefore,
   * no need to evaluate generated values, as there will be none returned.
   */
  
  @Test(expected=URISignatureHandler.URISignatureParsingException.class)
  public void extractPlaceholders_badURI_nullURI() {
    URISignatureHandler.extractPlaceholders(badURI_nullURI);
  }
  
  @Test(expected=URISignatureHandler.URISignatureParsingException.class)
  public void extractPlaceholders_badURI_emptyURI() {
    URISignatureHandler.extractPlaceholders(badURI_emptyURI);
  }
  
  @Test(expected=URISignatureHandler.URISignatureParsingException.class)
  public void extractPlaceholders_badURI_SingleOpeningSymbolNoClosingSymbol() {
    URISignatureHandler.extractPlaceholders(badURI_SingleOpeningSymbolNoClosingSymbol);
  }
  
  @Test(expected=URISignatureHandler.URISignatureParsingException.class)
  public void extractPlaceholders_badURI_SingleClosingSymbolNoOpeningSymbol() {
    URISignatureHandler.extractPlaceholders(badURI_SingleClosingSymbolNoOpeningSymbol);
  }
  
  @Test(expected=URISignatureHandler.URISignatureParsingException.class)
  public void extractPlaceholders_badURI_DoubleOpeningSymbolsNoClosingSymbol() {
    URISignatureHandler.extractPlaceholders(badURI_DoubleOpeningSymbolsNoClosingSymbol);
  }
  
  @Test(expected=URISignatureHandler.URISignatureParsingException.class)
  public void extractPlaceholders_badURI_DoubleOpeningSymbols() {
    URISignatureHandler.extractPlaceholders(badURI_DoubleOpeningSymbols);
  }
  
  @Test(expected=URISignatureHandler.URISignatureParsingException.class)
  public void extractPlaceholders_badURI_DoubleOpeningSymbolsSpaced() {
    URISignatureHandler.extractPlaceholders(badURI_DoubleOpeningSymbolsSpaced);
  }
  
  @Test(expected=URISignatureHandler.URISignatureParsingException.class)
  public void extractPlaceholders_badURI_DoubleClosingSymbols() {
    URISignatureHandler.extractPlaceholders(badURI_DoubleClosingSymbols);
  }
  
  @Test(expected=URISignatureHandler.URISignatureParsingException.class)
  public void extractPlaceholders_badURI_DoubleClosingSymbolsSpaced() {
    URISignatureHandler.extractPlaceholders(badURI_DoubleClosingSymbolsSpaced);
  }
  
  @Test(expected=URISignatureHandler.URISignatureParsingException.class)
  public void extractPlaceholders_badURI_NestedPlaceholders() {
    URISignatureHandler.extractPlaceholders(badURI_NestedPlaceholders);
  }
  
  @Test(expected=URISignatureHandler.URISignatureParsingException.class)
  public void extractPlaceholders_badURI_NestedPlaceholdersSpaced() {
    URISignatureHandler.extractPlaceholders(badURI_NestedPlaceholdersSpaced);
  }
  
  @Test(expected=URISignatureHandler.URISignatureParsingException.class)
  public void extractPlaceholders_badURI_DuplicatePlaceholders() {
    URISignatureHandler.extractPlaceholders(badURI_DuplicatePlaceholders);
  }
  
  @Test(expected=URISignatureHandler.URISignatureParsingException.class)
  public void extractPlaceholders_badURI_DuplicatePlaceholdersWithOthers() {
    URISignatureHandler.extractPlaceholders(badURI_DuplicatePlaceholdersWithOthers);
  }
  
  
  // ==========================================================================
  //          TEST COMPLETE URI GENERATION FROM URI SIGNATURE + PARAMETERS
  // ==========================================================================
  
  // success cases
  
  @SuppressWarnings("serial")
  @Test
  public void generateCompleteURI_successfulURIGeneration()
  {
    String uriSignature = "http://sysmo-db.org/sops/{sop_id}/experimental_conditions/{cond_id}?condition_unit={unit}";
    Map<String,String> parameters = new HashMap<String,String>(){{
      put("sop_id", "111");
      put("unit", "33");
      put("cond_id", "2222");
    }};
    
    String completeURI = URISignatureHandler.generateCompleteURI(uriSignature, parameters);
    assertEquals("http://sysmo-db.org/sops/111/experimental_conditions/2222?condition_unit=33",
                 completeURI);
  }
  
  @Test
  public void generateCompleteURI_signatureWithNoPlaceholders_nullParameterMap() {
    String completeURI = URISignatureHandler.generateCompleteURI(validURI_NoPlaceholders, null);
    assertEquals(validURI_NoPlaceholders, completeURI);
  }
  
  @Test
  public void generateCompleteURI_signatureWithNoPlaceholders_emptyParameterMap() {
    String completeURI = URISignatureHandler.generateCompleteURI(validURI_NoPlaceholders, Collections.<String,String>emptyMap());
    assertEquals(validURI_NoPlaceholders, completeURI);
  }
  
  
  // failure cases
  
  @Test(expected=URISignatureHandler.URIGenerationFromSignatureException.class)
  public void generateCompleteURI_signatureWithPlaceholders_nullParameterMap() {
    URISignatureHandler.generateCompleteURI(validURI_3MixedPlaceholders, null);
  }
  
  @Test(expected=URISignatureHandler.URIGenerationFromSignatureException.class)
  public void generateCompleteURI_signatureWithPlaceholders_emptyParameterMap() {
    URISignatureHandler.generateCompleteURI(validURI_3MixedPlaceholders, Collections.<String,String>emptyMap());
  }
  
  @Test(expected=URISignatureHandler.URIGenerationFromSignatureException.class)
  public void generateCompleteURI_signatureWithPlaceholders_missingParameterURIGeneration_FailureExpected()
  {
    String uriSignature = "http://sysmo-db.org/sops/{sop_id}/experimental_conditions/{cond_id}?condition_unit={unit}";
    Map<String,String> parameters = new HashMap<String,String>(){{
      put("sop_id", "111");
      put("cond_id", "2222");
    }};
    
    String completeURI = URISignatureHandler.generateCompleteURI(uriSignature, parameters);
    
    assertEquals("http://sysmo-db.org/sops/111/experimental_conditions/2222?condition_unit=33",
                 completeURI);
  }
  
     // the following failure case is just to confirm the validation of the URI
     // signature by the validation mechanism, which is the same for generateCompleteURI()
     // and extractPlaceholders()
  
  @Test(expected=URISignatureHandler.URISignatureParsingException.class)
  public void generateCompleteURI_duplicatePlaceholderURIGeneration_FailureExpected()
  {
    String uriSignature = "http://sysmo-db.org/sops/{sop_id}/experimental_conditions/{cond_id}?condition_unit={sop_id}";
    Map<String,String> parameters = new HashMap<String,String>(){{
      put("sop_id", "111");
      put("unit", "33");
      put("cond_id", "2222");
    }};
    
    String completeURI = URISignatureHandler.generateCompleteURI(uriSignature, parameters);
    
    assertEquals("http://sysmo-db.org/sops/111/experimental_conditions/2222?condition_unit=33",
                 completeURI);
  }
  
}
