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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.soap.SOAPFactory;

import org.apache.taverna.wsdl.parser.ComplexTypeDescriptor;
import org.apache.taverna.wsdl.parser.TypeDescriptor;
import org.apache.taverna.wsdl.parser.WSDLParser;
import org.apache.taverna.wsdl.testutils.LocationConstants;
import org.apache.taverna.wsdl.testutils.WSDLTestHelper;

import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

public class SOAPResponseMultiRefTest  implements LocationConstants {
	
	private String wsdlResourcePath(String wsdlName) throws Exception {
		return WSDLTestHelper.wsdlResourcePath(wsdlName);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testMultiRef() throws Exception {
		WSDLParser wsdlParser = new WSDLParser(wsdlResourcePath("TestServices-rpcencoded.wsdl"));

		List response = new ArrayList();

		String xml1 = "<ns1:getPersonResponse soapenv:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\" xmlns:ns1=\"urn:testing\" xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"><getPersonReturn href=\"#id0\"/></ns1:getPersonResponse>";
		String xml2 = "<multiRef id=\"id0\" soapenc:root=\"0\" soapenv:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\" xmlns:soapenc=\"http://schemas.xmlsoap.org/soap/encoding/\" xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><age xsi:type=\"soapenc:string\">5</age><name xsi:type=\"soapenc:string\">bob</name></multiRef>";

                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                factory.setNamespaceAware(true);
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.parse(new InputSource(new StringReader(xml1)));

                response.add(SOAPFactory.newInstance().createElement(doc.getDocumentElement()));

                doc = builder.parse(new InputSource(new StringReader(xml2)));
                response.add(SOAPFactory.newInstance().createElement(doc.getDocumentElement()));

		SOAPResponseEncodedMultiRefParser parser = new SOAPResponseEncodedMultiRefParser(
				wsdlParser.getOperationOutputParameters("getPerson"));
		parser.setStripAttributes(true);
		Map outputMap = parser.parse(response);

		assertNotNull("no output map returned", outputMap);

		assertEquals("map should contain 1 element", 1, outputMap.size());

		Object getPersonReturn =  outputMap
				.get("getPersonReturn");

		assertNotNull(
				"output map should have contained entry for 'getPersonReturn'",
				getPersonReturn);

		assertEquals("output data should be a string", String.class,
				getPersonReturn.getClass());

		assertEquals(
				"unexpected xml content in output",
				"<getPersonReturn><age>5</age><name>bob</name></getPersonReturn>",
				getPersonReturn.toString());

	}

	@SuppressWarnings("unchecked")
	@Test
	public void testMultiRefReturnNamespaced() throws Exception {
		WSDLParser wsdlParser = new WSDLParser(wsdlResourcePath("TestServices-rpcencoded.wsdl"));

		List response = new ArrayList();

		String xml1 = "<ns1:getPersonResponse soapenv:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\" xmlns:ns1=\"urn:testing\" xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"><ns1:getPersonReturn xmlns:ns1=\"urn:testing\" href=\"#id0\"/></ns1:getPersonResponse>";
		String xml2 = "<multiRef id=\"id0\" soapenc:root=\"0\" soapenv:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\" xmlns:soapenc=\"http://schemas.xmlsoap.org/soap/encoding/\" xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><age xsi:type=\"soapenc:string\">5</age><name xsi:type=\"soapenc:string\">bob</name></multiRef>";

                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                factory.setNamespaceAware(true);
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.parse(new InputSource(new StringReader(xml1)));

		response.add(SOAPFactory.newInstance().createElement(doc.getDocumentElement()));

                doc = builder.parse(new InputSource(new StringReader(xml2)));
		response.add(SOAPFactory.newInstance().createElement(doc.getDocumentElement()));

		SOAPResponseEncodedMultiRefParser parser = new SOAPResponseEncodedMultiRefParser(
				wsdlParser.getOperationOutputParameters("getPerson"));
		parser.setStripAttributes(true);
		Map outputMap = parser.parse(response);

		assertNotNull("no output map returned", outputMap);

		assertEquals("map should contain 1 element", 1, outputMap.size());

		Object getPersonReturn = outputMap
				.get("getPersonReturn");

		assertNotNull(
				"output map should have contained entry for 'getPersonReturn'",
				getPersonReturn);

		assertEquals("output data should be a string", String.class,
				getPersonReturn.getClass());

		assertEquals(
				"unexpected xml content in output",
				"<getPersonReturn><age>5</age><name>bob</name></getPersonReturn>",
				getPersonReturn.toString());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testNestedReferences() throws Exception {
	
		//only the name is important.
		TypeDescriptor descriptor = new ComplexTypeDescriptor();
		descriptor.setName("result");
		SOAPResponseEncodedMultiRefParser parser = new SOAPResponseEncodedMultiRefParser(
				Collections.singletonList(descriptor));
	

		String xml1 = "<response><result><creatures href=\"#id0\"/></result></response>";
		String xml2 = "<multiref id=\"id0\"><item href=\"#id1\"/><item href=\"#id2\"/></multiref>";
		String xml3 = "<multiref id=\"id1\"><creature>monkey</creature></multiref>";
		String xml4 = "<multiref id=\"id2\"><creature>frog</creature></multiref>";

		List response = new ArrayList();

                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                factory.setNamespaceAware(true);
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.parse(new InputSource(new StringReader(xml1)));

		response.add(SOAPFactory.newInstance().createElement(doc.getDocumentElement()));

                doc = builder.parse(new InputSource(new StringReader(xml2)));
		response.add(SOAPFactory.newInstance().createElement(doc.getDocumentElement()));

                doc = builder.parse(new InputSource(new StringReader(xml3)));
		response.add(SOAPFactory.newInstance().createElement(doc.getDocumentElement()));

                doc = builder.parse(new InputSource(new StringReader(xml4)));
		response.add(SOAPFactory.newInstance().createElement(doc.getDocumentElement()));

		
		parser.setStripAttributes(true);

		Map outputMap = parser.parse(response);

		assertNotNull("no output map returned", outputMap);

		assertEquals("map should contain 1 element", 1, outputMap.size());

		Object result = outputMap.get("result");

		assertNotNull("output map should have contained entry for 'result'",
				result);

		assertEquals("output data should be a string", String.class, result
				.getClass());

		assertEquals(
				"incorrect xml content in output",
				"<result><creatures><item><creature>monkey</creature></item><item><creature>frog</creature></item></creatures></result>",
				result.toString());

	}

	@SuppressWarnings("unchecked")
	@Test
	public void testFailOnCyclic() throws Exception {
		List outputNames = new ArrayList();
		outputNames.add("attachmentList");
		outputNames.add("result");

		String xml1 = "<response><result><item href=\"#id0\"/></result></response>";
		String xml2 = "<multiref id=\"id0\"><item href=\"#id1\"/></multiref>";
		String xml3 = "<multiref id=\"id1\"><item href=\"#id0\"/></multiref>";

		List response = new ArrayList();

                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                factory.setNamespaceAware(true);
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.parse(new InputSource(new StringReader(xml1)));
		response.add(SOAPFactory.newInstance().createElement(doc.getDocumentElement()));

		doc = builder.parse(new InputSource(new StringReader(xml2)));
		response.add(SOAPFactory.newInstance().createElement(doc.getDocumentElement()));

		doc = builder.parse(new InputSource(new StringReader(xml3)));
		response.add(SOAPFactory.newInstance().createElement(doc.getDocumentElement()));

		SOAPResponseEncodedMultiRefParser parser = new SOAPResponseEncodedMultiRefParser(
				outputNames);
		parser.setStripAttributes(true);

		try {
			parser.parse(response);
			fail("CyclicReferenceException should have been thrown");
		} catch (CyclicReferenceException e) {

		}
	}

}
