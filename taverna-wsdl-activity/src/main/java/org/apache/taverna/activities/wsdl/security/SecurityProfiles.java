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

import java.net.URI;

import org.apache.taverna.activities.wsdl.WSDLActivity;

/**
 * Various security profiles for Web services.
 *
 * @author Alex Nenadic
 *
 */
public class SecurityProfiles {

	public static URI SECURITY = URI.create(WSDLActivity.URI).resolve("wsdl/security");

	/**
	  * Security profile for Web services that require
	 * UsernameToken authentication with plaintext password.
	 * Such services should typically be invoked over HTTPS.
	 */
	public static final URI WSSECURITY_USERNAMETOKEN_PLAINTEXTPASSWORD = SECURITY.resolve("#WSSecurityUsernameTokenPlainTextPassword");
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
	public static final URI WSSECURITY_USERNAMETOKEN_DIGESTPASSWORD = SECURITY.resolve("#WSSecurityUsernameTokenDigestPassword");
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
	public static final URI WSSECURITY_TIMESTAMP_USERNAMETOKEN_PLAINTEXTPASSWORD = SECURITY.resolve("#WSSecurityTimestampUsernameTokenPlainTextPassword");
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
	public static final URI WSSECURITY_TIMESTAMP_USERNAMETOKEN_DIGESTPASSWORD = SECURITY.resolve("#WSSecurityTimestampUsernameTokenDigestPassword");
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
	public static final URI HTTP_BASIC_AUTHN = SECURITY.resolve("#HTTPBasicAuthNPlainTextPassword");

	/**
	 * Security profile for Web services that require HTTP Digest Authentication.
	 * There is no WS-Security involved.
	 * Such services would typically be invoked over HTTPS.
	 */
	public static final URI HTTP_DIGEST_AUTHN = SECURITY.resolve("#HTTPDigestAuthN");


}
