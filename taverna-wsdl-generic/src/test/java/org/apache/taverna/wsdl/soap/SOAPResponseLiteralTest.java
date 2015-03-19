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

package org.apache.taverna.wsdl.soap;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.soap.SOAPFactory;
import org.apache.taverna.wsdl.parser.BaseTypeDescriptor;
import org.apache.taverna.wsdl.parser.ComplexTypeDescriptor;
import org.apache.taverna.wsdl.parser.TypeDescriptor;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

public class SOAPResponseLiteralTest{

	@SuppressWarnings("unchecked")
	@Test
	public void testLiteralParserResultInTextBlock() throws Exception {
		List response = new ArrayList();
		String xml = "<testResponse><out>&lt;data name=&quot;a&quot;&gt;some data&lt;/data&gt;&lt;data name=&quot;b&quot;&gt;some more data&lt;/data&gt;</out></testResponse>";

                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                factory.setNamespaceAware(true);
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.parse(new InputSource(new StringReader(xml)));

		response.add(SOAPFactory.newInstance().createElement(doc.getDocumentElement()));

		TypeDescriptor descriptor = new ComplexTypeDescriptor();
		descriptor.setName("testResponse");

		SOAPResponseLiteralParser parser = new SOAPResponseLiteralParser(
				Collections.singletonList(descriptor));

		Map outputMap = parser.parse(response);

		assertNotNull("no output map returned", outputMap);
		assertEquals("map should contain 1 element", 1, outputMap.size());

		Object testResponse = outputMap.get("testResponse");

		assertNotNull("there should be an output named 'testReponse'",
				testResponse);
		assertEquals("output data should be a string", String.class,
				testResponse.getClass());

		assertEquals(
				"xml is wrong",
				"<testResponse><out>&lt;data name=\"a\"&gt;some data&lt;/data&gt;&lt;data name=\"b\"&gt;some more data&lt;/data&gt;</out></testResponse>",
				testResponse.toString());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testLiteralParser() throws Exception {
		List response = new ArrayList();
		String xml = "<testResponse><out><data name=\"a\">some data</data><data name=\"b\">some more data</data></out></testResponse>";
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                factory.setNamespaceAware(true);
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.parse(new InputSource(new StringReader(xml)));
                response.add(SOAPFactory.newInstance().createElement(doc.getDocumentElement()));

		TypeDescriptor descriptor = new ComplexTypeDescriptor();
		descriptor.setName("testResponse");

		SOAPResponseLiteralParser parser = new SOAPResponseLiteralParser(
				Collections.singletonList(descriptor));

		Map outputMap = parser.parse(response);

		assertNotNull("no output map returned", outputMap);
		assertEquals("map should contain 1 element", 1, outputMap.size());

		Object testResponse = outputMap.get("testResponse");

		assertNotNull("there should be an output named 'testReponse'",
				testResponse);
		assertEquals("output data should be a string", String.class,
				testResponse.getClass());

		assertEquals(
				"xml is wrong",
				"<testResponse><out><data name=\"a\">some data</data><data name=\"b\">some more data</data></out></testResponse>",
				testResponse.toString());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testUnwrappedLiteralResponseParsing() throws Exception {
		List response = new ArrayList();
		
		String xml = "<getStringReturn xmlns=\"http://testing.org\">a string</getStringReturn>";
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                factory.setNamespaceAware(true);
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.parse(new InputSource(new StringReader(xml)));

                response.add(SOAPFactory.newInstance().createElement(doc.getDocumentElement()));

		TypeDescriptor descriptor = new BaseTypeDescriptor();
		descriptor.setName("getStringReturn");
		
		SOAPResponseLiteralParser parser = new SOAPResponsePrimitiveLiteralParser(
				Collections.singletonList(descriptor));

		Map outputMap = parser.parse(response);

		assertNotNull("no output map returned", outputMap);
		assertEquals("map should contain 1 element", 1, outputMap.size());

		Object stringReturn = outputMap.get("getStringReturn");
		
		assertEquals("value of data returned is wrong","a string",stringReturn.toString());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testEmptyResponse() throws Exception {
		List response = new ArrayList();
		
		TypeDescriptor descriptor = new BaseTypeDescriptor();
		descriptor.setName("getStringReturn");
		
		SOAPResponseLiteralParser parser = new SOAPResponseLiteralParser(
				Collections.singletonList(descriptor));

		Map outputMap = parser.parse(response);

		assertNotNull("no output map returned", outputMap);
		assertEquals("map should contain 1 element", 0, outputMap.size());
	}

}

