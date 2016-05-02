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

package org.apache.taverna.wsdl.xmlsplitter;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.apache.taverna.wsdl.parser.TypeDescriptor;
import org.apache.taverna.wsdl.parser.WSDLParser;
import org.apache.taverna.wsdl.testutils.LocationConstants;
import org.apache.taverna.wsdl.testutils.WSDLTestHelper;

import org.junit.Test;

public class XMLInputSplitterTest extends WSDLTestHelper {

	@Test
	public void testExecute() throws Exception {
		WSDLParser parser = new WSDLParser(wsdlResourcePath("eutils/eutils_lite.wsdl"));
		TypeDescriptor descriptor = parser.getOperationInputParameters("run_eInfo").get(0);
		XMLInputSplitter splitter = new XMLInputSplitter(descriptor,new String[]{"db","tool","email"},new String[]{"text/plain","text/plain","text/plain"},new String[]{"output"});
		Map<String,Object> inputMap = new HashMap<String, Object>();
		inputMap.put("db", "pubmed");
		inputMap.put("email", "bob.monkhouse@itv.com");
		Map<String,String> outputMap = splitter.execute(inputMap);
		assertNotNull("there should be an output named 'output'",outputMap.containsKey("output"));
		String xml = outputMap.get("output");
		assertTrue(xml.startsWith("<parameters xmlns=\"http://www.ncbi.nlm.nih.gov/soap/eutils/einfo\">"));
		assertTrue(xml.contains("<db>pubmed</db>"));
		assertTrue(! xml.contains("<tool"));
		assertTrue(xml.contains("<email>bob.monkhouse@itv.com</email>"));
	} 
	
	
	@Test
	public void testOptional() throws Exception {
		WSDLParser parser = new WSDLParser(wsdlResourcePath("VSOi.wsdl"));
		TypeDescriptor descriptor = parser.getOperationInputParameters("Query").get(0);
		XMLInputSplitter splitter = new XMLInputSplitter(descriptor,new String[]{"version","block"},new String[]{"text/plain","text/plain"},new String[]{"output"});
		Map<String,Object> inputMap = new HashMap<String, Object>();
		// connect none of the inputs
		Map<String,String> outputMap = splitter.execute(inputMap);
		assertNotNull("there should be an output named 'output'",outputMap.containsKey("output"));
		String xml = outputMap.get("output");
		// empty string as <block> as it is not nillable
		assertTrue(xml.contains("<block xmlns=\"\"></block>"));
		// minOccurs=0 - so it should not be there
		assertTrue(! xml.contains("<version>"));
	} 
	
	
	@Test
	public void testNillable() throws Exception {
		WSDLParser parser = new WSDLParser(wsdlResourcePath("VSOi.wsdl"));
		TypeDescriptor descriptor = parser.getOperationInputParameters("Query").get(0);
		XMLInputSplitter splitter = new XMLInputSplitter(descriptor,new String[]{"version","block"},new String[]{"text/plain","text/plain"},new String[]{"output"});
		Map<String,Object> inputMap = new HashMap<String, Object>();
		// Magic string meaning insert xsi:nil=true
		inputMap.put("version", "xsi:nil");
		Map<String,String> outputMap = splitter.execute(inputMap);
		assertNotNull("there should be an output named 'output'",outputMap.containsKey("output"));
		String xml = outputMap.get("output");
		System.out.println(xml);
		// empty string as <block> as it is not nillable
		assertTrue(xml.contains("<block xmlns=\"\"></block>"));
		// FIXME: Should not really allow nil=true here, as version is not nillable! 
		assertTrue(xml.contains("<version xmlns=\"\" " +
				"xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
				"xsi:nil=\"true\" />"));
	} 
	
	
}
