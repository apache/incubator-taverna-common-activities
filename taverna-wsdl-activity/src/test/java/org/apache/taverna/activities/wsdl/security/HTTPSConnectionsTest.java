/*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.apache.taverna.activities.wsdl.security;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.cert.Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLHandshakeException;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class HTTPSConnectionsTest {

	@Test()
	@Ignore("https://rpc103.cs.man.ac.uk:8443/wsrf/services no available")
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
