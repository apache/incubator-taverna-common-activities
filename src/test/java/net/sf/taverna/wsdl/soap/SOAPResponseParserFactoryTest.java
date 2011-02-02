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

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import net.sf.taverna.wsdl.parser.WSDLParser;
import net.sf.taverna.wsdl.testutils.LocationConstants;
import net.sf.taverna.wsdl.testutils.WSDLTestHelper;

import org.junit.Test;

public class SOAPResponseParserFactoryTest  implements LocationConstants {
	
	private String wsdlResourcePath(String wsdlName) throws Exception {
		return WSDLTestHelper.wsdlResourcePath(wsdlName);
	}

	//tests that the factory always returns a SOAPResponseLiteralParser regardless of the 
	//output mime type, if the use is set to 'literal' (unwrapped/literal)
	@Test
	public void testLiteralUnwrappedParserForNonXMLOutput() throws Exception {
		SOAPResponseParserFactory factory = SOAPResponseParserFactory.instance();
		List<String> response = new ArrayList<String>();
		WSDLParser wsdlParser = new WSDLParser(wsdlResourcePath("TestServices-unwrapped.wsdl"));
		
		SOAPResponseParser parser = factory.create(response, "literal", "document", wsdlParser.getOperationOutputParameters("getString"));
		
		assertTrue("The parser is the wrong type, it was:"+parser.getClass().getSimpleName(),parser instanceof SOAPResponsePrimitiveLiteralParser);
	}
	
	//an additional test using another unwrapped/literal wsdl that returns a primative type
	@Test
	public void testLiteralUnwrappedAlternativeWSDL() throws Exception {
		SOAPResponseParserFactory factory = SOAPResponseParserFactory.instance();
		List<String> response = new ArrayList<String>();
		WSDLParser wsdlParser = new WSDLParser(wsdlResourcePath("prodoric.wsdl"));
		
		SOAPResponseParser parser = factory.create(response, "literal", "document", wsdlParser.getOperationOutputParameters("hello"));
		
		assertTrue("The parser is the wrong type, it was:"+parser.getClass().getSimpleName(),parser instanceof SOAPResponsePrimitiveLiteralParser);
	}
}
