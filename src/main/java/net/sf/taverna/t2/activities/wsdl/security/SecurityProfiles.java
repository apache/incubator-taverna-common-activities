package net.sf.taverna.t2.activities.wsdl.security;

/**
 * Various security profiles for Web services.
 * 
 * @author Alex Nenadic
 *
 */
public class SecurityProfiles {
	
	 /** 
	  * Security profile for Web services that require
	 * UsernameToken authentication with plaintext password. 
	 * Such services should typically be invoked over HTTPS.
	 */
	public static final String WSSECURITY_USERNAMETOKEN_PLAINTEXTPASSWORD = "WSSecurityUsernameTokenPlainTextPassword";
	/**
	 * XML string for configuring Axis engine with wss4j handlers to handle setting security 
	 * headers on the SOAP message for profile WSSECURITY_USERNAMETOKEN_PLAINTEXTPASSWORD.
	 */
	public static final String WSSECURITY_USERNAMETOKEN_PLAINTEXTPASSWORD_CONFIG = "<deployment xmlns=\"http://xml.apache.org/axis/wsdd/\" xmlns:java=\"http://xml.apache.org/axis/wsdd/providers/java\">" +
	"<globalConfiguration>" +
	"<requestFlow>" +
	"<handler type=\"java:org.apache.ws.axis.security.WSDoAllSender\">" + 
	"<parameter name=\"action\" value=\"UsernameToken\"/>" +
	"<parameter name=\"passwordType\" value=\"PasswordText\"/>" +
	"</handler>"+
	"</requestFlow>" + 
	"</globalConfiguration>" + 
	"<transport name=\"http\" pivot=\"java:org.apache.axis.transport.http.HTTPSender\"/>"+
	"</deployment>";

	 /** 
	  * Security profile for Web services that require
	 * UsernameToken authentication with digest password. 
	 * Such services would typically be invoked over HTTPS.
	 */
	public static final String WSSECURITY_USERNAMETOKEN_DIGESTPASSWORD = "WSSecurityUsernameTokenDigestPassword";
	/**
	 * XML string for configuring Axis engine with wss4j handlers to handle setting security 
	 * headers on the SOAP message for profile WSSECURITY_USERNAMETOKEN_DIGESTPASSWORD.
	 */
	public static final String WSSECURITY_USERNAMETOKEN_DIGESTPASSWORD_CONFIG = "<deployment xmlns=\"http://xml.apache.org/axis/wsdd/\" xmlns:java=\"http://xml.apache.org/axis/wsdd/providers/java\">" +
	"<globalConfiguration>" +
	"<requestFlow>" +
	"<handler type=\"java:org.apache.ws.axis.security.WSDoAllSender\">" + 
	"<parameter name=\"action\" value=\"UsernameToken\"/>" +
	"<parameter name=\"passwordType\" value=\"PasswordDigest\"/>" +
	"</handler>"+
	"</requestFlow>" + 
	"</globalConfiguration>" + 
	"<transport name=\"http\" pivot=\"java:org.apache.axis.transport.http.HTTPSender\"/>"+
	"</deployment>";
	
	 /** 
	  * Security profile for Web services that require a timestamp SOAP header
	  * to be sent in addition to UsernameToken authentication with plaintext password. 
	 * Such services should typically be invoked over HTTPS.
	 */
	public static final String WSSECURITY_TIMESTAMP_USERNAMETOKEN_PLAINTEXTPASSWORD = "WSSecurityTimestampUsernameTokenPlainTextPassword";
	/**
	 * XML string for configuring Axis engine with wss4j handlers to handle setting security 
	 * headers on the SOAP message for profile WSSECURITY_TIMESTAMP_USERNAMETOKEN_DIGESTPASSWORD.
	 */
	public static final String WSSECURITY_TIMESTAMP_USERNAMETOKEN_PLAINTETPASSWORD_CONFIG = "<deployment xmlns=\"http://xml.apache.org/axis/wsdd/\" xmlns:java=\"http://xml.apache.org/axis/wsdd/providers/java\">" +
	"<globalConfiguration>" +
	"<requestFlow>" +
	"<handler type=\"java:org.apache.ws.axis.security.WSDoAllSender\">" + 
	"<parameter name=\"action\" value=\"Timestamp UsernameToken\"/>" +
	"<parameter name=\"passwordType\" value=\"PasswordText\"/>" +
	"</handler>"+
	"</requestFlow>" + 
	"<responseFlow>" +
	"<handler type=\"java:org.apache.ws.axis.security.WSDoAllReceiver\">" + 
	"<parameter name=\"action\" value=\"Timestamp\"/>" +
	"</handler>"+
	"</responseFlow>" + 	
	"</globalConfiguration>" + 
	"<transport name=\"http\" pivot=\"java:org.apache.axis.transport.http.HTTPSender\"/>"+
	"</deployment>";
	
	 /** 
	  * Security profile for Web services that require a timestamp SOAP header
	  * to be sent in addition to UsernameToken authentication with digest password. 
	 * Such services would typically be invoked over HTTPS.
	 */
	public static final String WSSECURITY_TIMESTAMP_USERNAMETOKEN_DIGESTPASSWORD = "WSSecurityTimestampUsernameTokenDigestPassword";
	/**
	 * XML string for configuring Axis engine with wss4j handlers to handle setting security 
	 * headers on the SOAP message for profile WSSECURITY_TIMESTAMP_USERNAMETOKEN_DIGESTPASSWORD.
	 */
	public static final String WSSECURITY_TIMESTAMP_USERNAMETOKEN_DIGESTPASSWORD_CONFIG = "<deployment xmlns=\"http://xml.apache.org/axis/wsdd/\" xmlns:java=\"http://xml.apache.org/axis/wsdd/providers/java\">" +
	"<globalConfiguration>" +
	"<requestFlow>" +
	"<handler type=\"java:org.apache.ws.axis.security.WSDoAllSender\">" + 
	"<parameter name=\"action\" value=\"Timestamp UsernameToken\"/>" +
	"<parameter name=\"passwordType\" value=\"PasswordDigest\"/>" +
	"</handler>"+
	"</requestFlow>" + 
	"<responseFlow>" +
	"<handler type=\"java:org.apache.ws.axis.security.WSDoAllReceiver\">" + 
	"<parameter name=\"action\" value=\"Timestamp\"/>" +
	"</handler>"+
	"</responseFlow>" + 
	"</globalConfiguration>" + 
	"<transport name=\"http\" pivot=\"java:org.apache.axis.transport.http.HTTPSender\"/>"+
	"</deployment>";
	
	/**
	 * Security profile for Web services that require HTTP Basic Authentication. 
	 * There is no WS-Security involved.
	 * Such services should typically be invoked over HTTPS.
	 */
	public static final String HTTP_BASIC_AUTHN = "HTTPBasicAuthNPlainTextPassword";
	
	/**
	 * Security profile for Web services that require HTTP Digest Authentication. 
	 * There is no WS-Security involved.
	 * Such services would typically be invoked over HTTPS.
	 */
	public static final String HTTP_DIGEST_AUTHN = "HTTPDigestAuthN";


}
