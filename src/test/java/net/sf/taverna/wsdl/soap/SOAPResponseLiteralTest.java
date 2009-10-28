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
 * Filename           $RCSfile: SOAPResponseLiteralTest.java,v $
 * Revision           $Revision: 1.2 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007/11/30 12:13:37 $
 *               by   $Author: sowen70 $
 * Created on 11-May-2006
 *****************************************************************/
package net.sf.taverna.wsdl.soap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.sf.taverna.wsdl.parser.BaseTypeDescriptor;
import net.sf.taverna.wsdl.parser.ComplexTypeDescriptor;
import net.sf.taverna.wsdl.parser.TypeDescriptor;

import org.apache.axis.message.SOAPBodyElement;
import org.junit.Test;
import org.w3c.dom.Document;

public class SOAPResponseLiteralTest{

	@SuppressWarnings("unchecked")
	@Test
	public void testLiteralParserResultInTextBlock() throws Exception {
		List response = new ArrayList();
		String xml = "<testResponse><out>&lt;data name=&quot;a&quot;&gt;some data&lt;/data&gt;&lt;data name=&quot;b&quot;&gt;some more data&lt;/data&gt;</out></testResponse>";
		DocumentBuilder builder = DocumentBuilderFactory.newInstance()
				.newDocumentBuilder();
		Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes()));

		response.add(new SOAPBodyElement(doc.getDocumentElement()));

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
				"<testResponse><out>&lt;data name=&quot;a&quot;&gt;some data&lt;/data&gt;&lt;data name=&quot;b&quot;&gt;some more data&lt;/data&gt;</out></testResponse>",
				testResponse.toString());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testLiteralParser() throws Exception {
		List response = new ArrayList();
		String xml = "<testResponse><out><data name=\"a\">some data</data><data name=\"b\">some more data</data></out></testResponse>";
		DocumentBuilder builder = DocumentBuilderFactory.newInstance()
				.newDocumentBuilder();
		Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes()));

		response.add(new SOAPBodyElement(doc.getDocumentElement()));

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
		DocumentBuilder builder = DocumentBuilderFactory.newInstance()
		.newDocumentBuilder();
		Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes()));

		response.add(new SOAPBodyElement(doc.getDocumentElement()));

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

