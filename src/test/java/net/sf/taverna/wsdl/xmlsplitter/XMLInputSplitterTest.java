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
package net.sf.taverna.wsdl.xmlsplitter;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import net.sf.taverna.wsdl.parser.TypeDescriptor;
import net.sf.taverna.wsdl.parser.WSDLParser;
import net.sf.taverna.wsdl.testutils.LocationConstants;
import net.sf.taverna.wsdl.testutils.WSDLTestHelper;

import org.junit.Test;

public class XMLInputSplitterTest implements LocationConstants {

	@Test
	public void testExecute() throws Exception {
		WSDLParser parser = new WSDLParser(WSDLTestHelper.wsdlResourcePath("eutils/eutils_lite.wsdl"));
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
		assertTrue(xml.contains("<tool></tool>"));
		assertTrue(xml.contains("<email>bob.monkhouse@itv.com</email>"));
	} 
	
}
