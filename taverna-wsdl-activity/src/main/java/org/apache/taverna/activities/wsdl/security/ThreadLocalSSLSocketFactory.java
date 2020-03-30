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
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.log4j.Logger;

/**
 * An implementation of SSLSocketFactory which delegates to one of two internal
 * SSLSocketFactory implementations. The implementation delegated to is
 * determined by a thread local property and is either the default SSL socket or
 * a customized one with a trust manager that allows the use of unsigned server
 * certificates.
 * <p>
 * To set this as the default call install() prior to making any HTTPS
 * connections, then bracket code which needs to trust unsigned certificates in
 * the startTrustingEverything() and stopTrustingEverything() methods.
 * 
 * @author Tom Oinn
 * 
 */
public class ThreadLocalSSLSocketFactory extends SSLSocketFactory {

	private static Logger logger = Logger
	.getLogger(ThreadLocalSSLSocketFactory.class);

	/**
	 * Calls to open HTTPS connections will trust unsigned certificates afer
	 * this call is made, this is scoped to the current thread only.
	 */
	public static void startTrustingEverything() {
		threadLocalFactory.set(createAlwaysTrustingFactory());
	}

	/**
	 * Stop trusting unsigned certificates, reverting to the default behaviour
	 * for the current thread.
	 */
	public static void stopTrustingEverything() {
		threadLocalFactory.set(null);
	}

	/**
	 * Set this as the default global socket factory for HTTPS connections
	 */
	public static void install() {
		HttpsURLConnection
				.setDefaultSSLSocketFactory(new ThreadLocalSSLSocketFactory());
	}

	/**
	 * Determine whether the current thread will trust unsigned certificates
	 */
	public static boolean isTrustingEverything() {
		return (threadLocalFactory.get() != null);
	}

	/**
	 * Never construct manually
	 */
	private ThreadLocalSSLSocketFactory() {
		super();
	}

	private static ThreadLocal<SSLSocketFactory> threadLocalFactory = new ThreadLocal<SSLSocketFactory>();

	private static SSLSocketFactory createAlwaysTrustingFactory() {
		SSLContext sc = null;
		try {
			/** SSL is not secure */
			sc = SSLContext.getInstance("SSL");
		} catch (NoSuchAlgorithmException e1) {

			logger.error("No SSL algorithm", e1);
		}
		TrustManager overlyTrusting = new X509TrustManager() {

			public void checkClientTrusted(X509Certificate[] arg0, String arg1)
					throws CertificateException {

			}

			public void checkServerTrusted(X509Certificate[] arg0, String arg1)
					throws CertificateException {

			}

			public X509Certificate[] getAcceptedIssuers() {
				return null;
			}

		};
		try {
			sc.init(null, new TrustManager[] { overlyTrusting },
					new SecureRandom());
		} catch (KeyManagementException e) {
			logger.error("Unable to initialize SSLContext", e);
		}
		return sc.getSocketFactory();

	}

	private SSLSocketFactory getFactory() {
		if (threadLocalFactory.get() == null) {
			return (SSLSocketFactory) SSLSocketFactory.getDefault();
		} else {
			return threadLocalFactory.get();
		}
	}

	@Override
	public Socket createSocket(Socket arg0, String arg1, int arg2, boolean arg3)
			throws IOException {
		return getFactory().createSocket(arg0, arg1, arg2, arg3);
	}

	@Override
	public String[] getDefaultCipherSuites() {
		return getFactory().getDefaultCipherSuites();
	}

	@Override
	public String[] getSupportedCipherSuites() {
		return getFactory().getSupportedCipherSuites();
	}

	@Override
	public Socket createSocket() throws IOException {
		return getFactory().createSocket();
	}

	@Override
	public Socket createSocket(String arg0, int arg1) throws IOException,
			UnknownHostException {
		return getFactory().createSocket(arg0, arg1);
	}

	@Override
	public Socket createSocket(InetAddress arg0, int arg1) throws IOException {
		return getFactory().createSocket(arg0, arg1);
	}

	@Override
	public Socket createSocket(String arg0, int arg1, InetAddress arg2, int arg3)
			throws IOException, UnknownHostException {
		return getFactory().createSocket(arg0, arg1, arg2, arg3);
	}

	@Override
	public Socket createSocket(InetAddress arg0, int arg1, InetAddress arg2,
			int arg3) throws IOException {
		return getFactory().createSocket(arg0, arg1, arg2, arg3);
	}
}
