/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester   
 * 
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *    
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *    
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
package net.sf.taverna.wsdl.soap;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.taverna.wsdl.parser.WSDLParser;
import net.sf.taverna.wsdl.testutils.LocationConstants;
import net.sf.taverna.wsdl.testutils.WSDLTestHelper;

import org.apache.axis.message.SOAPBodyElement;
import org.junit.Test;


public class EncodedBodyBuilderTest implements LocationConstants {
	
	private String wsdlResourcePath(String wsdlName) throws Exception {
		return WSDLTestHelper.wsdlResourcePath(wsdlName);
	}

	@Test
	public void testSimpleCase() throws Exception {
		Map<String,Object> inputMap = new HashMap<String, Object>();
		
		BodyBuilder builder = createBuilder(wsdlResourcePath("TestServices-rpcencoded.wsdl"), "countString");
		
		assertTrue("Wrong type of builder created",builder instanceof EncodedBodyBuilder);
		
		inputMap.put("str", "Roger Ramjet");
		SOAPBodyElement body = builder.build(inputMap);
		
		String xml = body.getAsString();
		
		assertTrue("Contents of body are not as expected: actual body:"+xml,xml.contains("<str xsi:type=\"xsd:string\">Roger Ramjet</str>"));
	}
	
	@Test
	public void testStringArray() throws Exception {
		Map<String,Object> inputMap = new HashMap<String, Object>();
		
		BodyBuilder builder = createBuilder(wsdlResourcePath("TestServices-rpcencoded.wsdl"), "countStringArray");
		
		assertTrue("Wrong type of builder created",builder instanceof EncodedBodyBuilder);
		List<String> array=new ArrayList<String>();
		array.add("one");
		array.add("two");
		array.add("three");
		inputMap.put("array", array);
		SOAPBodyElement body = builder.build(inputMap);
		
		String xml = body.getAsString();
		
		assertTrue("Contents of body are not as expected: actual body:"+xml,xml.contains("<string>one</string><string>two</string><string>three</string>"));
	}
	
	@Test
	public void testComplexType() throws Exception {
		BodyBuilder builder = createBuilder(wsdlResourcePath("TestServices-rpcencoded.wsdl"), "personToString");
		
		assertTrue("Wrong type of builder created",builder instanceof EncodedBodyBuilder);
		
		String p = "<Person xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"><name xsi:type=\"xsd:string\">bob</name><age xsi:type=\"xsd:int\">12</age></Person>";
		
		Map<String,Object> inputMap = new HashMap<String, Object>();
		
		inputMap.put("p",p);
		SOAPBodyElement body = builder.build(inputMap);
		
		String xml = body.getAsString();
		
		System.out.println(xml);
		
		assertTrue("Type definition of Person is missing",xml.contains("<p xsi:type=\"ns1:Person\">"));
		assertFalse("There shouldn't be ns2 declaration",xml.contains("xmlns:ns2"));
		assertTrue("Missing data content",xml.contains("<name xsi:type=\"xsd:string\">bob</name><age xsi:type=\"xsd:int\">12</age>"));
		
	}
	
	protected BodyBuilder createBuilder(String wsdl, String operation) throws Exception {
		WSDLParser parser = new WSDLParser(wsdl);
		
		return BodyBuilderFactory.instance().create(parser, operation, parser.getOperationInputParameters(operation));
	}
}
