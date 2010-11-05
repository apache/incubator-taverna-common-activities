package net.sf.taverna.t2.activities.rest;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import net.sf.taverna.t2.activities.rest.RESTActivity.DATA_FORMAT;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;

/**
 * This class deals with the actual remote REST service invocation.
 * The main four HTTP methods (GET | POST | PUT | DELETE) are supported.
 * <br/><br/>
 * 
 * Configuration for request execution is obtained from the related
 * REST activity - encapsulated in a configuration bean.
 * 
 * @author Sergejs Aleksejevs
 */
public class HTTPRequestHandler
{
  private static final String CONTENT_TYPE_HEADER_NAME = "Content-Type";
  private static final String ACCEPT_HEADER_NAME = "Accept";
  
  
  /**
   * This method is the entry point to the invocation of a remote REST
   * service. It accepts a number of parameters from the related REST
   * activity and uses those to assemble, execute and fetch results of
   * a relevant HTTP request.
   * 
   * @param requestURL The URL for the request to be made. This cannot be
   *                   taken from the <code>configBean</code>, because this
   *                   should be the complete URL which may be directly used
   *                   to make the request (<code>configBean</code> would only
   *                   contain the URL signature associated with the REST activity). 
   * @param configBean Configuration of the associated REST activity is passed to
   *                   this class as a configuration bean. Settings such as HTTP method,
   *                   MIME types for "Content-Type" and "Accept" headers, etc are taken
   *                   from the bean.
   * @param inputMessageBody Body of the message to be sent to the server - only needed
   *                         for POST and PUT requests; for GET and DELETE it will be discarded.
   * @return
   */
  public static HTTPRequestResponse initiateHTTPRequest(String requestURL,
      RESTActivityConfigurationBean configBean, Object inputMessageBody)
  {
    switch (configBean.getHttpMethod()) {
      case GET:    return (doGET   (requestURL, configBean));
      case POST:   return (doPOST  (requestURL, configBean, inputMessageBody));
      case PUT:    return (doPUT   (requestURL, configBean, inputMessageBody));
      case DELETE: return (doDELETE(requestURL, configBean));
      default:     return (new HTTPRequestResponse(new Exception("Error: something went wrong; " +
      		                  "no failure has occurred, but but unexpected HTTP method (\"" +
      		                  configBean.getHttpMethod() + "\") encountered.")));
    }
  }
  
  
  private static HTTPRequestResponse doGET(String requestURL, RESTActivityConfigurationBean configBean) {
    HttpGet httpGet = new HttpGet(requestURL);
    return (performHTTPRequest(httpGet, configBean));
  }
  
  
  private static HTTPRequestResponse doPOST(String requestURL,
      RESTActivityConfigurationBean configBean, Object inputMessageBody)
  {
    HttpPost httpPost = new HttpPost(requestURL);
    
    // TODO - decide whether this is needed for PUT requests, too (or just here, for POST)
    // check whether to send the HTTP Expect header or not
    if (!configBean.getSendHTTPExpectRequestHeader()) {
      httpPost.getParams().setBooleanParameter("http.protocol.expect-continue", false );
    }
    
    httpPost.setHeader(CONTENT_TYPE_HEADER_NAME, configBean.getContentTypeForUpdates());
    try {
      HttpEntity entity = null;
      if (inputMessageBody == null) {
        entity = new StringEntity("");
      }
      else if (configBean.getOutgoingDataFormat() == DATA_FORMAT.String) {
        entity = new StringEntity((String)inputMessageBody);
      }
      else {
        entity = new ByteArrayEntity((byte[])inputMessageBody);
      }
      httpPost.setEntity(entity);
    }
    catch (UnsupportedEncodingException e) {
      return(new HTTPRequestResponse(new Exception("Error occurred while trying to " +
      		"attach a message body to the POST request. See attached cause of this " +
      		"exception for details.")));
    }
    return(performHTTPRequest(httpPost, configBean));
  }
  
  
  private static HTTPRequestResponse doPUT(String requestURL,
      RESTActivityConfigurationBean configBean, Object inputMessageBody)
  {
    HttpPut httpPut = new HttpPut(requestURL);
    httpPut.setHeader(CONTENT_TYPE_HEADER_NAME, configBean.getContentTypeForUpdates());
    try {
      HttpEntity entity = null;
      if (inputMessageBody == null) {
        entity = new StringEntity("");
      }
      else if (configBean.getOutgoingDataFormat() == DATA_FORMAT.String) {
        entity = new StringEntity((String)inputMessageBody);
      }
      else {
        entity = new ByteArrayEntity((byte[])inputMessageBody);
      }
      httpPut.setEntity(entity);
    }
    catch (UnsupportedEncodingException e) {
      return(new HTTPRequestResponse(new Exception("Error occurred while trying to " +
          "attach a message body to the PUT request. See attached cause of this " +
          "exception for details.")));
    }
    return (performHTTPRequest(httpPut, configBean));
  }
  
  
  private static HTTPRequestResponse doDELETE(String requestURL, RESTActivityConfigurationBean configBean) {
    HttpDelete httpDelete = new HttpDelete(requestURL);
    return (performHTTPRequest(httpDelete, configBean));
  }
  
  
  /**
   * TODO - may need to set PROXY from Taverna's / Java's settings...
   * TODO - REDIRECTION output:: if there was no redirection, should just show the actual initial URL?
   * 
   * @param httpRequest
   * @param acceptHeaderValue 
   */
  private static HTTPRequestResponse performHTTPRequest(HttpRequestBase httpRequest, RESTActivityConfigurationBean configBean)
  {
    // headers are set identically for all HTTP methods, therefore can do centrally - here
    httpRequest.setHeader(ACCEPT_HEADER_NAME, configBean.getAcceptsHeaderValue());
    
    
    HTTPRequestResponse requestResponse = new HTTPRequestResponse();
    
    try {
      HttpClient httpClient = new DefaultHttpClient();
      ((DefaultHttpClient)httpClient).setCredentialsProvider(RESTActivityCredentialsProvider.getInstance());
      HttpContext localContext = new BasicHttpContext();
      
      // execute the request
      HttpResponse response = httpClient.execute(httpRequest, localContext);
      
      // record response code
      requestResponse.setStatusCode(response.getStatusLine().getStatusCode());
      requestResponse.setReasonPhrase(response.getStatusLine().getReasonPhrase());
      
      // record header values for Content-Type of the response 
      requestResponse.setResponseContentTypes(response.getHeaders(CONTENT_TYPE_HEADER_NAME));
      
      
      // track where did the final redirect go to (if there was any)
      HttpHost targetHost = (HttpHost) localContext.getAttribute(ExecutionContext.HTTP_TARGET_HOST);
      HttpUriRequest targetRequest = (HttpUriRequest) localContext.getAttribute(ExecutionContext.HTTP_REQUEST);
      requestResponse.setRedirectionURL("" + targetHost + targetRequest.getURI());
      requestResponse.setRedirectionHTTPMethod(targetRequest.getMethod());
      
      // read and store response body
      // (check there is some content - negative length of content means unknown length;
      //  zero definitely means no content...)
      // TODO - make sure that this test is sufficient to determine if there is no response entity
      if (response.getEntity() != null && response.getEntity().getContentLength() != 0) {
        requestResponse.setResponseBody(readResponseBody(response.getEntity()));
      }
      
      // release resources (e.g. connection pool, etc)
      httpClient.getConnectionManager().shutdown();
     
    }
    catch (Exception ex) {
      requestResponse = new HTTPRequestResponse(ex);
    }
    
    return (requestResponse);
  }
  
  
  /**
   * Dispatcher method that decides on the method of reading
   * the server response data - either as a string or as binary
   * data.
   * 
   * @param entity
   * @return
   * @throws IOException
   */
  private static Object readResponseBody(HttpEntity entity) throws IOException
  {
    if (entity != null)
    {
      // test whether the data is binary or textual -
      // for binary data will read just as it is, for textual data
      // will attempt to perform charset conversion from the original
      // one into UTF-8
      
      String contentType = entity.getContentType().getValue().toLowerCase();
      if (contentType.startsWith("text") && contentType.contains("charset=")) {
        // read as text
        return (readResponseBodyAsString(entity));
      }
      else {
        // read as binary - enough to pass the input stream, not the whole entity
        return (readFromInputStreamAsBinary(entity.getContent()));
      }
    }
    else {
      // HTTP message did not contain body...
      return (null);
    }
  }
  
  
  /**
   * Worker method that extracts the content of the received
   * HTTP message as a string. It also makes use of the
   * charset that is specified in the Content-Type header
   * of the received data to read it appropriately.
   * 
   * @param entity
   * @return
   * @throws IOException
   */
  private static String readResponseBodyAsString(HttpEntity entity) throws IOException
  {
    // get charset name
    String charset = null;
    String contentType = entity.getContentType().getValue().toLowerCase();
    
    String[] contentTypeParts = contentType.split(";");
    for (String contentTypePart : contentTypeParts)
    {
      contentTypePart = contentTypePart.trim();
      if (contentTypePart.startsWith("charset=")) {
        charset = contentTypePart.substring("charset=".length());
      }
    }
    
    // read the data line by line
    StringBuilder responseBodyString = new StringBuilder();
    BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent(), charset));
    
    String str;
    while ((str = reader.readLine()) != null) {
      responseBodyString.append(str + "\n");
    }
    
    return (responseBodyString.toString());
  }
  
  
  /**
   * Worker method that extracts the content of the input stream as binary data.
   * 
   * @param inputStream
   * @return
   * @throws IOException
   */
  public static byte[] readFromInputStreamAsBinary(InputStream inputStream) throws IOException
  {
    // use BufferedInputStream for better performance
    BufferedInputStream in = new BufferedInputStream(inputStream);
    
    try
    {
      // this list is to hold all fetched data
      List<byte[]> data = new ArrayList<byte[]>();
      
      // set up buffers for reading the data
      int bufLength = 100 * 1024; // 100K
      byte[] buf = new byte[bufLength];
      byte[] currentPortionOfData = null;
      int currentlyReadByteCount = 0;
      
      // read the data portion by portion into a list
      while ((currentlyReadByteCount = in.read(buf, 0, bufLength)) != -1) {
        currentPortionOfData = new byte[currentlyReadByteCount];
        System.arraycopy(buf, 0, currentPortionOfData, 0, currentlyReadByteCount);
        data.add(currentPortionOfData);
      }
      
      // now check how much data was read and return that as a single byte array
      if (data.size() == 1)
      {
        // just a single block of data - return it as it is
        return (data.get(0));
      }
      else {
        // there is more than one block of data -- calculate total length of data
        bufLength = 0;
        for (byte[] portionOfData : data) bufLength += portionOfData.length;
        
        // allocate a single large byte array that could contain all data
        buf = new byte[bufLength];
        
        // fill this byte array with data from all fragments
        int lastFilledPositionInOutputArray = 0;
        for (byte[] portionOfData : data) {
          System.arraycopy(portionOfData, 0, buf, lastFilledPositionInOutputArray, portionOfData.length);
          lastFilledPositionInOutputArray += portionOfData.length;
        }
        
        return (buf);
      }
    }
    finally {
      // this method will still throw any IOExceptions that may occur, but
      // this block is used to close the input stream anyway
      if (in != null) {
        try { in.close(); }
        catch (Exception e) { /* do nothing on this failure - it was just an attempt to recover resources */ }
      }
    }
  }
  
  
  /**
   * All fields have public accessor, but private mutators. This
   * is because it should only be allowed to modify the HTTPRequestResponse
   * partially inside the HTTPRequestHandler class only. For users of this
   * class it will behave as immutable.
   * 
   * @author Sergejs Aleksejevs
   */
  public static class HTTPRequestResponse
  {
    private int statusCode;
    private String reasonPhrase;
    private String redirectionURL;
    private String redirectionHTTPMethod;
    private Header[] responseContentTypes;
    private Object responseBody;
    
    private Exception exception;
    
    
    /**
     * Private default constructor - will only be accessible from HTTPRequestHandler.
     * Values for the entity will then be set using the private mutator methods.
     */
    private HTTPRequestResponse()
    {
      /* 
       * do nothing here - values will need to be manually set later by using
       * private mutator methods
       */
    }
    
    /**
     * Standard public constructor for a regular case, where all values are known and
     * the request has succeeded.
     * 
     * @param statusCode
     * @param reasonPhrase
     * @param redirection
     * @param responseContentTypes
     * @param responseBody
     */
    public HTTPRequestResponse(int statusCode, String reasonPhrase, String redirectionURL,
        String redirectionHTTPMethod, Header[] responseContentTypes, String responseBody)
    {
      this.statusCode = statusCode;
      this.reasonPhrase = reasonPhrase;
      this.redirectionURL = redirectionURL;
      this.redirectionHTTPMethod = redirectionHTTPMethod;
      this.responseContentTypes = responseContentTypes;
      this.responseBody = responseBody;
    }
    
    /**
     * Standard public constructor for an error case, where an error has occurred
     * and request couldn't be executed because of an internal exception (rather
     * than an error received from the remote server).
     * 
     * @param exception
     */
    public HTTPRequestResponse(Exception exception)
    {
      this.exception = exception;
    }
    
    
    private void setStatusCode(int statusCode) {
      this.statusCode = statusCode;
    }
    public int getStatusCode() {
      return statusCode;
    }
    
    public String getReasonPhrase() {
      return reasonPhrase;
    }
    private void setReasonPhrase(String reasonPhrase) {
      this.reasonPhrase = reasonPhrase;
    }
    
    public String getRedirectionURL() {
      return redirectionURL;
    }
    private void setRedirectionURL(String redirectionURL) {
      this.redirectionURL = redirectionURL;
    }
    
    public String getRedirectionHTTPMethod() {
      return redirectionHTTPMethod;
    }
    private void setRedirectionHTTPMethod(String redirectionHTTPMethod) {
      this.redirectionHTTPMethod = redirectionHTTPMethod;
    }
    
    public Header[] getResponseContentTypes() {
      return responseContentTypes;
    }
    private void setResponseContentTypes(Header[] responseContentTypes) {
      this.responseContentTypes = responseContentTypes;
    }
    
    public Object getResponseBody() {
      return responseBody;
    }
    private void setResponseBody(Object outputBody) {
      this.responseBody = outputBody;
    }
    
    
    /**
     * @return <code>true</code> if an exception has occurred while the
     *         HTTP request was executed. (E.g. this doesn't indicate a
     *         server error - just that the request couldn't be successfully
     *         executed. It could have been a network timeout, etc).
     */
    public boolean hasException() {
      return (this.exception != null);
    }
    public Exception getException() {
      return exception;
    }
    private void setException(Exception exception) {
      this.exception = exception;
    }
    
    
    /**
     * @return <code>true</code> if HTTP code of server response is
     *         either 4xx or 5xx.
     */
    public boolean hasServerError() {
      return (statusCode >= 400 && statusCode < 600);
    }
  }
  
}
