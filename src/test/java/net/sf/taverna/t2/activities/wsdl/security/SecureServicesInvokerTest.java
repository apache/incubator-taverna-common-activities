package net.sf.taverna.t2.activities.wsdl.security;

import static org.junit.Assert.*;

import java.util.Hashtable;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.xml.namespace.QName;

import org.apache.axis.MessageContext;
import org.apache.axis.SimpleChain;
import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.axis.client.Transport;
import org.apache.axis.configuration.BasicClientConfig;
import org.apache.axis.configuration.SimpleProvider;
import org.apache.axis.configuration.XMLStringProvider;
import org.apache.axis.encoding.Base64;
import org.apache.axis.transport.http.HTTPConstants;
import org.apache.axis.transport.http.HTTPSender;
import org.apache.axis.transport.http.HTTPTransport;
import org.apache.ws.axis.security.WSDoAllSender;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.handler.WSHandlerConstants;
import org.junit.Test;

public class SecureServicesInvokerTest {

	@Test
	public void testCreteCallAndInvoke() { 
		try { 
			

			String endpoint = "http://www.mygrid.org.uk/axis/services/UsernameHelloService"; // test server
			//endpoint = "http://rpc212.cs.man.ac.uk/taverna/";
			
			HostnameVerifier hv = new HostnameVerifier() {
				public boolean verify(String hostName, SSLSession session) {
					return true;
				}
			};
			HttpsURLConnection.setDefaultHostnameVerifier(hv);
			//endpoint = "https://rpc103.cs.man.ac.uk:8443/wsrf/services/cagrid/SecureHelloWorld?wsdl";
			
			// Set the system property "javax.net.ssl.Truststore" to use Taverna's truststore 
			//System.setProperty("javax.net.ssl.trustStore", "/Users/alex/Desktop/t2truststore.jks");
			//System.setProperty("javax.net.ssl.trustStorePassword", "raehiekooshe0eghiPhi");
			//System.clearProperty("javax.net.ssl.trustStoreType");
			//System.clearProperty("javax.net.ssl.trustStoreProvider");
			
			//String endpoint = "http://www.mygrid.org.uk/axis/services/UsernameTimestampHelloService"; // test server
			//String endpoint = "http://www.mygrid.org.uk/axis/services/UsernameDigestHelloService"; // test server
			//String endpoint = "http://www.mygrid.org.uk/axis/services/UsernameDigestTimestampHelloService"; // test server
			
			//String endpoint = "http://localhost:8080/axis/services/UsernameHelloService" ; // local server
			//String endpoint = "http://localhost:8080/axis/services/UsernameTimestampHelloService" ; 
			//String endpoint = "http://localhost:8080/axis/services/UsernameDigestHelloService" ; 
			//String endpoint = "http://localhost:8080/axis/services/UsernameDigestTimestampHelloService" ; 
			
			//Service service = new org.apache.axis.client.Service() ; 
			
			//String wssEngineConfigurationString = WSSecurityProfiles.wssUTTimestampProfile;
			//String wssEngineConfigurationString = WSSecurityProfiles.wssUTDigestProfile;
			//String wssEngineConfigurationString = WSSecurityProfiles.wssUTDigestTimestampProfile;
		
			XMLStringProvider wssEngineConfiguration = new XMLStringProvider("<deployment xmlns=\"http://xml.apache.org/axis/wsdd/\" " +
					"xmlns:java=\"http://xml.apache.org/axis/wsdd/providers/java\">" +
					"<globalConfiguration>" +
					"<requestFlow>" +
					"<handler type=\"java:org.apache.ws.axis.security.WSDoAllSender\">" + 
					"<parameter name=\"action\" value=\"UsernameToken\"/>" +
					"<parameter name=\"passwordType\" value=\"PasswordDigest\"/>" +
					"</handler>"+
					"</requestFlow>" + 
					"</globalConfiguration>" + 
					"<transport name=\"http\" pivot=\"java:org.apache.axis.transport.http.HTTPSender\"/>"+
					"</deployment>");
			
			Service service = new Service(wssEngineConfiguration);
		
			Call call = new Call(service);			
			
			//call.setTransport(new HTTPTransport());
			call.setTargetEndpointAddress(endpoint) ; 
			call.setOperationName(new QName("hello")) ;
			
			String username = "testuser";
			String password = "testpasswd";
			
			//WSS4J WSDoAllSender's invoke() method expects username not to be empty before the agent takes over 
			// to set it so we set it to the WSDL location here (that is used as keystore alias) and later on overwrite it 
			call.setProperty(Call.USERNAME_PROPERTY, "testuser");
			call.setProperty(Call.PASSWORD_PROPERTY, "testpasswd");
					
			
			// Set HTTP Basic AuthN
//			MessageContext context = call.getMessageContext();
//			Hashtable headers = (Hashtable) context
//					.getProperty(HTTPConstants.REQUEST_HEADERS);
//			if (headers == null) {
//				headers = new Hashtable();
//				context.setProperty(HTTPConstants.REQUEST_HEADERS, headers);
//			}
////			String authorization = Base64.encode(("hudsonadmin" + ":"
////					+ "ch33se").getBytes());
//			String authorization = Base64.encode((username + ":"
//					+ password).getBytes());
//			headers.put("Authorization", "Basic " + authorization);
//			System.out.println("HTTP Authorization header: "
//					+ headers.get("Authorization"));
//			HTTPSender http = new HTTPSender();
//			
//			Transport transport = call.getTransportForProtocol("https");
//			System.out.println(transport.getClass());
//			
			
			
		
			String nickName = "Beauty" ; 
			System.out.println("Sent: '" + nickName + "'") ; 
			String ret = (String) call.invoke(new Object[] {nickName}) ;
			System.out.println("Got: '" + ret + "'") ; 
			assertEquals(ret, "Hello Beauty!");
		} 
		catch (Exception e) { 
			e.printStackTrace() ; 
		} 
		System.exit(0) ; 
	} 

}
