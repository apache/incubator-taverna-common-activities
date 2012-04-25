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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPElement;

import net.sf.taverna.wsdl.parser.WSDLParser;
import net.sf.taverna.wsdl.testutils.LocationConstants;
import net.sf.taverna.wsdl.testutils.WSDLTestHelper;

import org.junit.Test;
import org.w3c.dom.Node;

public class LiteralBodyBuilderTest implements LocationConstants{
	
	private String wsdlResourcePath(String wsdlName) throws Exception {
		return WSDLTestHelper.wsdlResourcePath(wsdlName);
	}

	@Test
	public void testUnqualifiedNamespaces() throws Exception {
		BodyBuilder builder = createBuilder(wsdlResourcePath("whatizit.wsdl"), "queryPmid");
		
		assertTrue("Is is the wrong type, it should be LiteralBodyBuilder",builder instanceof LiteralBodyBuilder);
		
		String parameters = "<parameters xmlns=\"http://www.ebi.ac.uk/webservices/whatizit/ws\"><pipelineName xmlns=\"\">swissProt</pipelineName><pmid xmlns=\"\">1234</pmid></parameters>";
		Map<String,Object> inputMap = new HashMap<String, Object>();
		inputMap.put("parameters", parameters);
		
		SOAPElement body = builder.build(inputMap);
	
                assertTrue("Wrong wrapping element name", "queryPmid".equals(body.getLocalName()) && "http://www.ebi.ac.uk/webservices/whatizit/ws".equals(body.getNamespaceURI()));

                Iterator<Node> pipelineNames = body.getChildElements(new QName("","pipelineName"));
                
                assertTrue("No pipelineName defined", pipelineNames.hasNext());
                assertTrue("Wrong pipelineName value (must be 'swissProt')", "swissProt".equals(pipelineNames.next().getTextContent()));
                
                Iterator<Node> pmids = body.getChildElements(new QName("","pmid"));

                assertTrue("No pmid defined", pmids.hasNext());
                assertTrue("Wrong pmid value (must be '1234')", "1234".equals(pmids.next().getTextContent()));
	}
	
	@Test
	public void testQualifiedUnwrapped() throws Exception {
		BodyBuilder builder = createBuilder(wsdlResourcePath("TestServices-unwrapped.wsdl"), "countString");
		
		assertTrue("Is is the wrong type, it should be LiteralBodyBuilder",builder instanceof LiteralBodyBuilder);
		Map<String,Object>inputMap = new HashMap<String, Object>();
		inputMap.put("str", "bob");
		
                SOAPElement body = builder.build(inputMap);
		
                assertEquals("Wrong localName","str", body.getLocalName());
                assertEquals("XML should containe qualifed namespace for str","http://testing.org", body.getNamespaceURI());
	}
	
	@Test
	public void testUnwrappedSimple() throws Exception {
		BodyBuilder builder = createBuilder(wsdlResourcePath("TestServices-unwrapped.wsdl"), "countString");
		
		assertTrue("Wrong type of builder, it should be Literal based",builder instanceof LiteralBodyBuilder);
		
		Map<String,Object> inputMap = new HashMap<String, Object>();
		inputMap.put("str", "12345");
		
		SOAPElement body = builder.build(inputMap);
		
                assertTrue("Input element should be named {http://testing.org}str ", "str".equals(body.getLocalName()) && "http://testing.org".equals(body.getNamespaceURI()));

		assertEquals("Value should be 12345:","12345",body.getFirstChild().getNodeValue());
	}
	
	@Test
	public void testUnwrappedArray() throws Exception {
		BodyBuilder builder = createBuilder(wsdlResourcePath("TestServices-unwrapped.wsdl"), "countStringArray");
		
		assertTrue("Wrong type of builder, it should be Literal based",builder instanceof LiteralBodyBuilder);
		
		Map<String,Object> inputMap = new HashMap<String, Object>();
		inputMap.put("array", "<array><item>1</item><item>2</item><item>3</item></array>");
		
		SOAPElement body = builder.build(inputMap);
		
                assertTrue("Outer element should be named {http://testing.org}array ", "array".equals(body.getLocalName()) && "http://testing.org".equals(body.getNamespaceURI()));
                
                assertTrue("There must be three child nodes in array", body.getChildNodes().getLength() == 3);

                Iterator<Node> items = body.getChildElements(new QName("", "item"));
                assertTrue("Array element should be named item", items.hasNext());
                
                assertTrue("First Array element should have the value '1'", "1".equals(items.next().getTextContent()));
	}
	
	@Test 
	public void testOperationElementNameEUtils() throws Exception {
		BodyBuilder builder = createBuilder(wsdlResourcePath("eutils/eutils_lite.wsdl"), "run_eInfo");

		assertTrue("Wrong type of builder, it should be Literal based",builder instanceof LiteralBodyBuilder);
		Map<String,Object> inputMap = new HashMap<String, Object>();
		inputMap.put("parameters",
		// Note: Don't use xmlns="" as it would also affect <parameters>
				// - which should not affect the namespace of the soap body
				// element. The element qname of the SOAPBodyElement should be
				// determined by the schema only
				"<parameters xmlns:e='http://www.ncbi.nlm.nih.gov/soap/eutils/einfo'>"
						+ "<e:db>database</e:db>" + "<e:tool>myTool</e:tool>"
						+ "<e:email>nobody@nowhere.net</e:email>"
						+ "</parameters>");
		SOAPElement body = builder.build(inputMap);
		assertEquals("QName of SOAP body's element did not match expected qname ", 
				new QName("http://www.ncbi.nlm.nih.gov/soap/eutils/einfo", "eInfoRequest"), 
				body.getElementQName());
	}
	
	@Test 
	public void testOperationElementNameTAV744() throws Exception {
		URL tav744Url = getClass().getResource(
				"/net/sf/taverna/wsdl/parser/TAV-744/InstrumentService__.wsdl");
		
		BodyBuilder builder = createBuilder(tav744Url.toExternalForm(), "getList");

		assertTrue("Wrong type of builder, it should be Literal based",builder instanceof LiteralBodyBuilder);
		Map<String,Object> inputMap = new HashMap<String, Object>();
		// No inputs
		SOAPElement body = builder.build(inputMap);
		assertEquals("QName of SOAP body's element did not match expected qname ", 
				new QName("http://InstrumentService.uniparthenope.it/InstrumentService", "GetListRequest"), 
				body.getElementQName());
	}
	
	@Test
	public void testRPCLiteral() throws Exception {
		BodyBuilder builder = createBuilder(wsdlResourcePath("MyService-rpc-literal.wsdl"), "countString");
		
		assertTrue("Wrong type of builder, it should be Literal based",builder instanceof LiteralBodyBuilder);
		
		Map<String,Object> inputMap = new HashMap<String, Object>();
		inputMap.put("str", "abcdef");
		
		SOAPElement body = builder.build(inputMap);
		
		assertTrue("Outer element should be named {http://testing.org}countString","countString".equals(body.getLocalName()) && "http://testing.org".equals(body.getNamespaceURI()));
                
		Node strNode = body.getFirstChild();
		assertEquals("Inner element should be called 'str'","str",strNode.getNodeName());
		assertEquals("str content should be abcdef","abcdef",strNode.getFirstChild().getNodeValue());
	}
	
	protected BodyBuilder createBuilder(String wsdl, String operation) throws Exception {
		WSDLParser parser = new WSDLParser(wsdl);
		return BodyBuilderFactory.instance().create(parser, operation, parser.getOperationInputParameters(operation));
	}
}
