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
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPFactory;
import org.apache.taverna.wsdl.parser.WSDLParser;
import org.apache.taverna.wsdl.testutils.LocationConstants;
import org.apache.taverna.wsdl.testutils.WSDLTestHelper;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

public class SOAPResponseEncodedTest  implements LocationConstants {
	private String wsdlResourcePath(String wsdlName) throws Exception {
		return WSDLTestHelper.wsdlResourcePath(wsdlName);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testSimpleRPC() throws Exception {
		
		WSDLParser wsdlParser = new WSDLParser(wsdlResourcePath("ma.wsdl"));

		String xml1 = "<ns1:whatGeneInStageResponse soapenv:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\" xmlns:ns1=\"urn:hgu.webservice.services\" xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"><whatGeneInStageReturn soapenc:arrayType=\"ns2:GeneExpressedQueryShortDetails[0]\" xsi:type=\"soapenc:Array\" xmlns:ns2=\"http://SubmissionQuery.WSDLGenerated.hgu\" xmlns:soapenc=\"http://schemas.xmlsoap.org/soap/encoding/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><agene xsi:type=\"string\">a gene</agene></whatGeneInStageReturn></ns1:whatGeneInStageResponse>";

		List<SOAPElement> response = new ArrayList<SOAPElement>();

                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                factory.setNamespaceAware(true);
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.parse(new InputSource(new StringReader(xml1)));
		response.add(SOAPFactory.newInstance().createElement(doc.getDocumentElement()));

		SOAPResponseEncodedParser parser = new SOAPResponseEncodedParser(wsdlParser.getOperationOutputParameters("whatGeneInStage"));
		parser.setStripAttributes(true);

		Map outputMap = parser.parse(response);

		assertNotNull("no output map returned", outputMap);

		assertEquals("map should contain 1 element", 1, outputMap.size());

		Object result = outputMap.get("whatGeneInStageReturn");

		assertNotNull(
				"output map should have contained entry for 'whatGeneInStageReturn'",
				result);

		assertEquals("output data should be a string", String.class, result.getClass());

		assertEquals(
				"incorrect xml content in output",
				"<whatGeneInStageReturn><agene>a gene</agene></whatGeneInStageReturn>",
				result.toString());
	}
}
