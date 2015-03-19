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
import java.net.Socket;
import java.util.Hashtable;

import javax.net.ssl.SSLContext;

public class TavernaAxisCustomSSLSocketFactory
{
    
}        
//import org.apache.axis.components.net.BooleanHolder;
//import org.apache.axis.components.net.JSSESocketFactory;
//
//public class TavernaAxisCustomSSLSocketFactory extends JSSESocketFactory {
//
//
//	public TavernaAxisCustomSSLSocketFactory(Hashtable attributes) {
//		super(attributes);
//	}
//
//	@Override
//	public Socket create(String host, int port, StringBuffer otherHeaders,
//			BooleanHolder useFullURL) throws Exception {
//		// Make sure we always pick up the default socket factory from SSLContext, which is based on 
//		// Taverna's Keystore and Truststore and gets updated when they get updated (it may have
//		// been updated in the menatime so just refresh it here just in case).
//		initFactory();
//		return super.create(host, port, otherHeaders, useFullURL);
//	}
//	
//	@Override
//	protected void initFactory() throws IOException {
//		try{
//			// Set it to the default one from the SSLContext which is set to use Taverna's Keystore and Truststore
//			sslFactory = SSLContext.getDefault().getSocketFactory();
//		}
//		catch (Exception e) {
//			throw new IOException("Could not get the Taverna's default SSLSocketFactory from SSLContext",e);
//		}
//	}
//	
//}
