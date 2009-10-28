package net.sf.taverna.t2.activities.wsdl.security;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.cert.Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLHandshakeException;

import org.junit.Assert;
import org.junit.Test;

public class HTTPSConnectionsTest {
	
	@Test()
	public void testHTTPSConnections(){
		
		String serviceURL = "https://rpc103.cs.man.ac.uk:8443/wsrf/services";
		
			URL url;
			try {
				url = new URL(serviceURL);
				SSLUtilities.trustAllHttpsCertificates();
				HttpsURLConnection httpsConnection = (HttpsURLConnection) url.openConnection();
				httpsConnection.connect();
				Certificate[] certificates = httpsConnection.getServerCertificates();
				Assert.assertFalse(certificates.length == 0);
				
				SSLUtilities.stopTrustingAllHttpsCertificates();
				HttpsURLConnection httpsConnection2 = (HttpsURLConnection) url.openConnection();
				httpsConnection2.connect(); // This should now throw an SSLHandshakeException which is a subclass of IOException

			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			
			} catch(SSLHandshakeException e){
				// This is what we are expecting
				e.printStackTrace();
			}
			catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}

}
