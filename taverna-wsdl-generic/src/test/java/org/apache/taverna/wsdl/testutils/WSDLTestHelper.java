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

package org.apache.taverna.wsdl.testutils;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;

public class WSDLTestHelper implements LocationConstants {


	
	// See https://github.com/taverna-extras/wsdl-generic-test-cases
	//public static String TEST_CASES_URL = "https://github.com/taverna-extras/wsdl-generic-test-cases/archive/2016-04-28.zip";
	public static String TEST_CASES_URL = "https://github.com/taverna-extras/wsdl-generic-test-cases/releases/download/2016-04-28/wsdl-generic-test-cases-2016-04-28.jar";
	//public static String TEST_CASES_URL = "file:///tmp/1/wsdl-generic-test-cases-2016-04-28.jar";

	// Folder within JAR - e.g. wsdl-generic-test-cases-2016-04-28/
	public static String PREFIX = "";
	
	private static final String README_MD = "README.md";
	
	private static URLClassLoader classLoader;
	
	@BeforeClass
	public static void makeClassLoader() throws MalformedURLException {
		if (classLoader == null) {
			URL[] urls = new URL[] { new URL(TEST_CASES_URL) } ;
			classLoader = URLClassLoader.newInstance(urls);
		}
		
		Assume.assumeNotNull(getResource(README_MD));
	}

	@Before
	public void setThreadClassLoader() {
		Thread.currentThread().setContextClassLoader(classLoader);
	}
	
//	@Test
//	public void getReadmeUrl() throws Exception {
//		URL u = getResource(README_MD);
//		assertNotNull(u);
//		System.out.println(u);
//		
//	}
//
//	@Test
//	public void getReadme() throws Exception {
//		InputStream s = getResourceAsStream(README_MD);
//		assertNotNull(s);
//		IOUtils.copy(s, System.out);
//	}
//	
//	@Test
//	public void getReadmeString() throws Exception {
//		String s = getResourceAsString(README_MD);
//		assertTrue(s.length() > 0);
//		System.out.println(s);
//	}
	
	
	public static URL getResource(String path) { 
		return classLoader.getResource(PREFIX + path);
	}
	public static InputStream getResourceAsStream(String path) { 
		return classLoader.getResourceAsStream(PREFIX + path);
	}
	public static String getResourceAsString(String path) throws IOException {
		return IOUtils.toString(getResource(path), StandardCharsets.UTF_8);
	}
	
	public static String wsdlResourcePath(String resourceName) throws Exception {
		makeClassLoader();
		return getResource(WSDL_RESOURCE_BASE+resourceName).toExternalForm();
	}

    

}
