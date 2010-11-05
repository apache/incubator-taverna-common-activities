package net.sf.taverna.t2.activities.rest;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


/**
 * This class deals with URI signatures - essentially, strings that
 * represent some resource's URI with zero or more placeholders.
 * URI signatures are known at workflow definition time and represent
 * the pattern of the complete URIs that will be used at workflow run time.
 * 
 * An example of the URI signature is:
 * http://sysmo-db.org/sops/{sop_id}/experimental_conditions/{cond_id}?condition_unit={unit}
 * 
 * Placeholders "{sop_id}", "{cond_id}" and "{unit}" will be replaced by the real values
 * prior to using the URI for a real request.
 * 
 * This class is concerned with validation of URI signatures, extraction of
 * placeholders and substituting placeholders to generate complete URIs.
 * 
 * @author Sergejs Aleksejevs
 */
public class URISignatureHandler
{
  public static final char PLACEHOLDER_START_SYMBOL = '{';
  public static final char PLACEHOLDER_END_SYMBOL = '}';
  
  
  /**
   * Extracts placeholders of the given URI signature with their
   * positions in the signature in the order of their occurrence.
   * 
   * Extraction is done in a robust way with signature validity
   * checks being carried out simultaneously. This makes sure
   * that even with no explicit validation (see {@link URISignatureHandler#isValid(String)})
   * no unexpected faults occur.
   * 
   * @param uriSignature The URI signature to process.
   * @return A map of placeholders as they are encountered (from start to end) 
   *         in the URI signature and their start positions. Keys of the map
   *         are the "titles" of the placeholders without opening and closing
   *         placeholder symbols; values are the URI signature string indices,
   *         where the title of the corresponding placeholder starts in the string.
   */
  public static LinkedHashMap<String,Integer> extractPlaceholdersWithPositions(String uriSignature)
  {
    // no signature - nothing to process
    if (uriSignature == null || uriSignature.length() == 0) {
      throw new URISignatureParsingException("URI signature is null or empty - nothing to process.");
    }
    
    
    LinkedHashMap<String,Integer> foundPlaceholdersWithPositions = new LinkedHashMap<String,Integer>();
    
    int nestingLevel = 0;
    int startSymbolIdx = -1;
    
    // go through the signature character by character trying to extract placeholders
    for (int i = 0; i < uriSignature.length(); i++)
    {
      switch (uriSignature.charAt(i)) {
        case PLACEHOLDER_START_SYMBOL:
          nestingLevel++;
          if (nestingLevel == 1) {
            startSymbolIdx = i;
          }
          else /* if (nestingLevel > 0) */ {
            throw new URISignatureParsingException("Malformed URI signature: at least two " + 
                "placeholder opening symbols follow each other without being closed " +
                "appropriately (possibly, the signature contains nested placeholders)");
          }
          break;
        
        case PLACEHOLDER_END_SYMBOL:
          nestingLevel--;
          if (nestingLevel == 0) {
            // correctly opened and closed placeholder found; check if it is a "fresh" one
            String placeholderCandidate = uriSignature.substring(startSymbolIdx + 1, i);
            if (!foundPlaceholdersWithPositions.containsKey(placeholderCandidate)) {
              foundPlaceholdersWithPositions.put(placeholderCandidate, startSymbolIdx + 1);
            }
            else {
              throw new URISignatureParsingException("Malformed URI signature: duplicate placeholder \"" + placeholderCandidate + "\" found");
            }
          }
          else /* if (nestingLevel < 0) */ {
            throw new URISignatureParsingException("Malformed URI signature: placeholder closing symbol found before the opening one");
          }
          break;
        
        default: continue;
      }
    }
    
    
    // the final check - make sure that after traversing the string, we are
    // not "inside" one of the placeholders (e.g. this could happen if a placeholder
    // opening symbol was found, but the closing one never occurred after that)
    if (nestingLevel > 0) {
      throw new URISignatureParsingException("Malformed URI signature: placeholder opening symbol found, " +
      		"but the closing one has not been encountered");
    }
    
    
    return (foundPlaceholdersWithPositions);
  }
  
  
  /**
   * Works identical to {@link URISignatureHandler#extractPlaceholdersWithPositions(String)}
   * except for returning only the list of placeholder titles - without positions.
   * 
   * @param uriSignature The URI signature to process.
   * @return List of the placeholder titles in the order of their occurrence in
   *         the provided URI signature.
   */
  public static List<String> extractPlaceholders(String uriSignature) {
    return (new ArrayList<String>(extractPlaceholdersWithPositions(uriSignature).keySet()));
  }
  
  
  /**
   * This method performs explicit validation of the URI signature.
   * If the validation succeeds, the method terminates quietly;
   * in case of any identified problems a {@link URISignatureParsingException}
   * is thrown.
   * 
   * @param uriSignature The URI signature to validate.
   * @throws URISignatureParsingException
   */
  public static void validate(String uriSignature) throws URISignatureParsingException
  {
    // this method essentially needs to do exactly the same thing
    // as the method to extract the placeholders with their corresponding
    // positions; all necessary validation is already performed there -
    // hence the trick is simply to call that method (discarding its output),
    // while keeping track of any exceptions that may be generated by the
    // called method;
    //
    // for this simply call the placeholder extraction method - any exceptions
    // will be forwarded up the method call stack; in case of success, the method
    // will terminate quietly
    extractPlaceholdersWithPositions(uriSignature);
  }
  
  
  /**
   * Tests whether the provided URI signature is valid or not.
   * 
   * @param uriSignature URI signature to check for validity.
   * @return <code>true</code> if the URI signature is valid; <code>false</code> otherwise.
   */
  public static boolean isValid(String uriSignature) {
    try {
      // no exceptions are generated by validate(), the validation has succeeded
      validate(uriSignature);
      return (true);
    }
    catch (URISignatureParsingException e) {
      return false;
    }
  }
  
  
  /**
   * Substitutes real values for all placeholders encountered in the
   * URI signature and produces a complete URI that can be used directly.
   * 
   * @param uriSignature The URI signature to use as a basis.
   * @param parameters Map of {name,value} pairs for all placeholders in the
   *                   signature. These values will be used to replace the
   *                   placeholders in the signature.
   * @return A complete URI with all placeholders replaced by the provided values.
   * @throws URISignatureParsingException Thrown if there is a problem with the
   *                   provided URI signature (e.g. null, empty, ill-formed, etc).
   * @throws URIGenerationFromSignatureException Thrown if there is a problem with
   *                   the provided parameter map (e.g. null, empty, not containing
   *                   enough values for some of the placeholders found in <code>uriSignature</code>. 
   */
  public static String generateCompleteURI(String uriSignature, Map<String,String> parameters)
    throws URISignatureParsingException, URIGenerationFromSignatureException
  {
    StringBuilder completeURI = new StringBuilder(uriSignature);
    
    // no need to make any checks on the uriSignature - it is 
    // already handled by extractPlaceholdersWithPositions() --
    // if something goes wrong a runtime exception will be thrown
    // during placeholder extraction
    LinkedHashMap<String,Integer> placeholdersWithPositions = extractPlaceholdersWithPositions(uriSignature);
    
    // check that the URI signature contains some placeholders
    if (placeholdersWithPositions.keySet().size() > 0)
    {
      // some work will actually have to be done to replace placeholders with real values;
      // check that the parameter map contains some values
      if (parameters == null || parameters.isEmpty()) {
        throw new URIGenerationFromSignatureException("Parameter map is null or empty");
      }
      
      // the 'placeholders' linked list is guaranteed to be in the order of occurrence of placeholders in the URI signature;
      // this will allow to traverse the URI signature and replace the placeholders in the reverse order --
      // this way it is possible to use the indices of placeholders that were already found during their extraction to
      // improve performance
      LinkedList<String> placeholders = new LinkedList<String>(placeholdersWithPositions.keySet());
      Collections.reverse(placeholders);
      Iterator<String> placeholdersIterator = placeholders.iterator();
      
      while (placeholdersIterator.hasNext()) {
        String placeholder = placeholdersIterator.next();
        if (parameters.containsKey(placeholder)) {
          int placeholderStartPos = placeholdersWithPositions.get(placeholder) - 1;
          int placeholderEndPos = placeholderStartPos + placeholder.length() + 2;
          completeURI.replace(placeholderStartPos, placeholderEndPos, urlEncodeQuery(parameters.get(placeholder)));
        }
        else {
          throw new URIGenerationFromSignatureException("Parameter map does not contain a key/value for \"" + placeholder + "\" placeholder");
        }
      }
    }
    /* else { NO PLACEHOLDERS, SO NOTHING TO REPLACE WITH REAL VALUES - JUST RETURN THE ORIGINAL 'uriSignature' } */
    
    return (completeURI.toString());
  }
  
  
  /**
   * Exceptions of this type may be thrown when errors occur
   * during URI signature parsing - these will often indicate
   * the reason for failure (e.g. missing URI signature, nested
   * placeholders, ill-formed signature, etc). 
   * 
   * @author Sergejs Aleksejevs
   */
  public static class URISignatureParsingException extends IllegalArgumentException
  {
    public URISignatureParsingException() { }
    public URISignatureParsingException(String message) { super(message); }
  }
  
  
  /**
   * Exceptions of this type may be thrown during generation of
   * a complete URI from the provided signature and parameter hash.
   * These may occur because of wrong parameters, etc.
   * 
   * @author Sergejs Aleksejevs
   */
  public static class URIGenerationFromSignatureException extends RuntimeException
  {
    public URIGenerationFromSignatureException() { }
    public URIGenerationFromSignatureException(String message) { super(message); }
  }
  
  
  /**
   * Prepares the string to serve as a part of url query to the server.
   * @param query The string that needs URL encoding.
   * @return URL encoded string that can be inserted into the request URL.
   */
  public static String urlEncodeQuery(String query)
  {
    // "fast exit" - if null supplied, just return an empty string;
    // this is because in the URLs we have "q=", rather than "q=null" - this will cater for such cases
    if (query == null) return ("");
    
    // encode the query
    String strRes = "";
    try {
      strRes = URLEncoder.encode(query, "UTF-8");
    }
    catch (UnsupportedEncodingException e) {
      // do nothing - UTF-8 must be supported...
    }
    
    return (strRes);
  }
}
