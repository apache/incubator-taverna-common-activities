/*
 * Copyright (C) 2003 The University of Manchester 
 *
 * Modifications to the initial code base are copyright of their
 * respective authors, or their employers as appropriate.  Authorship
 * of the modifications may be determined from the ChangeLog placed at
 * the end of this file.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA.
 *
 ****************************************************************
 * Source code information
 * -----------------------
 * Filename           $RCSfile: SOAPResponseMultiRefTest.java,v $
 * Revision           $Revision: 1.4 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2008/08/29 16:15:54 $
 *               by   $Author: sowen70 $
 * Created on 08-May-2006
 *****************************************************************/
package net.sf.taverna.wsdl.soap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.sf.taverna.wsdl.parser.ComplexTypeDescriptor;
import net.sf.taverna.wsdl.parser.TypeDescriptor;
import net.sf.taverna.wsdl.parser.WSDLParser;
import net.sf.taverna.wsdl.testutils.LocationConstants;
import net.sf.taverna.wsdl.testutils.WSDLTestHelper;

import org.apache.axis.message.SOAPBodyElement;
import org.junit.Test;
import org.w3c.dom.Document;

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

		DocumentBuilder builder = DocumentBuilderFactory.newInstance()
				.newDocumentBuilder();
		Document doc = builder.parse(new ByteArrayInputStream(xml1.getBytes()));

		response.add(new SOAPBodyElement(doc.getDocumentElement()));

		doc = builder.parse(new ByteArrayInputStream(xml2.getBytes()));
		response.add(new SOAPBodyElement(doc.getDocumentElement()));

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

		DocumentBuilder builder = DocumentBuilderFactory.newInstance()
				.newDocumentBuilder();
		Document doc = builder.parse(new ByteArrayInputStream(xml1.getBytes()));

		response.add(new SOAPBodyElement(doc.getDocumentElement()));

		doc = builder.parse(new ByteArrayInputStream(xml2.getBytes()));
		response.add(new SOAPBodyElement(doc.getDocumentElement()));

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

		DocumentBuilder builder = DocumentBuilderFactory.newInstance()
				.newDocumentBuilder();
		Document doc = builder.parse(new ByteArrayInputStream(xml1.getBytes()));
		response.add(new SOAPBodyElement(doc.getDocumentElement()));

		doc = builder.parse(new ByteArrayInputStream(xml2.getBytes()));
		response.add(new SOAPBodyElement(doc.getDocumentElement()));

		doc = builder.parse(new ByteArrayInputStream(xml3.getBytes()));
		response.add(new SOAPBodyElement(doc.getDocumentElement()));

		doc = builder.parse(new ByteArrayInputStream(xml4.getBytes()));
		response.add(new SOAPBodyElement(doc.getDocumentElement()));

		
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

		DocumentBuilder builder = DocumentBuilderFactory.newInstance()
				.newDocumentBuilder();
		Document doc = builder.parse(new ByteArrayInputStream(xml1.getBytes()));
		response.add(new SOAPBodyElement(doc.getDocumentElement()));

		doc = builder.parse(new ByteArrayInputStream(xml2.getBytes()));
		response.add(new SOAPBodyElement(doc.getDocumentElement()));

		doc = builder.parse(new ByteArrayInputStream(xml3.getBytes()));
		response.add(new SOAPBodyElement(doc.getDocumentElement()));

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
