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

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.apache.taverna.wsdl.parser.WSDLParser;
import org.apache.taverna.wsdl.testutils.LocationConstants;
import org.apache.taverna.wsdl.testutils.WSDLTestHelper;

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
