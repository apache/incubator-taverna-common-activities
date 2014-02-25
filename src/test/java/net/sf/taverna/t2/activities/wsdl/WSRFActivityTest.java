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
package net.sf.taverna.t2.activities.wsdl;

import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import net.sf.taverna.wsdl.parser.WSDLParser;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

public class WSRFActivityTest {

	public class DummyInvoker extends T2WSDLSOAPInvoker {

		public DummyInvoker(String wsrfEndpoint) {
			super(wsdlParser, "add", Arrays.asList("attachmentList"),
					wsrfEndpoint, null);
		}

		@Override
		public SOAPMessage call(SOAPMessage message) throws Exception{
			requestXML = message;
                        MessageFactory factory = MessageFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL);
                        return factory.createMessage();
		}
	}

	private URL counterServiceWSDL;
	private WSDLParser wsdlParser;
	protected SOAPMessage requestXML;

	@Before
	public void makeWSDLParser() throws Exception {
		String path = "wsrf/counterService/CounterService_.wsdl";
		counterServiceWSDL = getClass().getResource(path);
		assertNotNull("Coult not find test WSDL " + path, counterServiceWSDL);
		wsdlParser = new WSDLParser(counterServiceWSDL.toExternalForm());
	}

	public void noHeaders() throws Exception {

	}

	@Test
	public void insertedEndpoint() throws Exception {
		// From T2-677 - support wsa:EndpointReference directly as well 
		String wsrfEndpoint = "" +
				"<wsa:EndpointReference "
				+ "xmlns:wsa='http://schemas.xmlsoap.org/ws/2004/03/addressing' "
				+ "xmlns:counter='http://counter.com'>"
				+ "  <wsa:Address>http://130.88.195.110:8080/wsrf/services/CounterService</wsa:Address>"
				+ "   <wsa:ReferenceProperties>"
				+ "     <counter:CounterKey>15063581</counter:CounterKey>"
				+ "      <difficult:one xmlns:difficult='http://difficult.com/' "
				+ "             difficult:attrib='something' attrib='else' >"
				+ "         <difficult:fish><counter:fish /></difficult:fish> "
				+ "      </difficult:one>" + "      <emptyNamespace>"
				+ "          <defaultNamespace xmlns='http://default/'>"
				+ "\n  default  \n " + "</defaultNamespace>"
				+ "          <stillEmpty />" + "      </emptyNamespace>"
				+ "  </wsa:ReferenceProperties>"
				+ "  <wsa:ReferenceParameters/>" + "</wsa:EndpointReference>"; 

		// Note: We'll subclass to avoid calling service
		// and request attachmentList to trigger TAV-617-code and avoid
		// parsing of the (missing) response
		T2WSDLSOAPInvoker invoker = new DummyInvoker(wsrfEndpoint);
		Map<String, Object> results = invoker.invoke(Collections.singletonMap("add", "10"));
		assertEquals(1, results.size());
		assertEquals("attachmentList", results.keySet().iterator().next());

                SOAPEnvelope envelope = requestXML.getSOAPPart().getEnvelope();
		SOAPHeader header = envelope.getHeader();
                
                checkHeader(header);
	}
	

	@Test
	public void insertedEndpointWrapped() throws Exception {
		// From T2-677 - support wsa:EndpointReference wrapped in <*> to avoid
		// unnecessary XML splitters and to support legacy workflows
		
		// Example from http://www.mygrid.org.uk/dev/issues/browse/TAV-23
		String wsrfEndpoint = "" +
				"<c:createCounterResponse xmlns:c='http://counter.com'>" +
				"<wsa:EndpointReference "
				+ "xmlns:wsa='http://schemas.xmlsoap.org/ws/2004/03/addressing' "
				+ "xmlns:counter='http://counter.com'>"
				+ "  <wsa:Address>http://130.88.195.110:8080/wsrf/services/CounterService</wsa:Address>"
				+ "   <wsa:ReferenceProperties>"
				+ "     <counter:CounterKey>15063581</counter:CounterKey>"
				+ "      <difficult:one xmlns:difficult='http://difficult.com/' "
				+ "             difficult:attrib='something' attrib='else' >"
				+ "         <difficult:fish><counter:fish /></difficult:fish> "
				+ "      </difficult:one>" + "      <emptyNamespace>"
				+ "          <defaultNamespace xmlns='http://default/'>"
				+ "\n  default  \n " + "</defaultNamespace>"
				+ "          <stillEmpty />" + "      </emptyNamespace>"
				+ "  </wsa:ReferenceProperties>"
				+ "  <wsa:ReferenceParameters/>" + "</wsa:EndpointReference>" + 
				"</c:createCounterResponse>";

		// Note: We'll subclass to avoid calling service
		// and request attachmentList to trigger TAV-617-code and avoid
		// parsing of the (missing) response
		T2WSDLSOAPInvoker invoker = new DummyInvoker(wsrfEndpoint);
		Map<String, Object> results = invoker.invoke(Collections.singletonMap(
				"add", "10"));
		assertEquals(1, results.size());
		assertEquals("attachmentList", results.keySet().iterator().next());

                SOAPEnvelope envelope = requestXML.getSOAPPart().getEnvelope();
		SOAPHeader header = envelope.getHeader();
                
                checkHeader(header);
	}
        
        private void checkHeader(SOAPHeader header)
        {
            Iterator<SOAPHeaderElement> headers = header.examineAllHeaderElements();
            assertTrue("no headers found", headers.hasNext());

            Map<QName, SOAPHeaderElement> elements = new HashMap<QName, SOAPHeaderElement>();
            while (headers.hasNext()) {
                SOAPHeaderElement element = headers.next();
                elements.put(new QName(element.getNamespaceURI(), element.getLocalName()), element);
            }

            SOAPHeaderElement counterKey = elements.get(new QName("http://counter.com", "CounterKey"));
            assertNotNull("Could not find {http://counter.com}CounterKey header", counterKey);
            assertEquals("15063581", counterKey.getTextContent());

            SOAPHeaderElement difficultChild = elements.get(new QName("http://difficult.com/", "one"));
            assertNotNull("Could not find {http://difficult.com/}one header", difficultChild);

            assertEquals("something", difficultChild.getAttributeNS("http://difficult.com/","attrib"));
            assertEquals("else", difficultChild.getAttributeNS(null, "attrib"));

            Iterator<SOAPElement> difficultFishes = difficultChild.getChildElements(new QName("http://difficult.com/", "fish"));
            assertTrue("no {http://difficult.com/}fish element found", difficultFishes.hasNext());
            SOAPElement difficultFish = difficultFishes.next();
            assertFalse("more than one {http://difficult.com/}fish element found", difficultFishes.hasNext());

            Iterator<SOAPElement> counterFishes = difficultFish.getChildElements(new QName("http://counter.com", "fish"));
            assertTrue("no {http://counter.com}fish element found", counterFishes.hasNext());
            SOAPElement counterFish = counterFishes.next();
            assertFalse("more than one {http://counter.com}fish element found", counterFishes.hasNext());

            SOAPHeaderElement emptyNamespace = elements.get(new QName("emptyNamespace"));
            assertNotNull("Could not find {}emptyNamespace header", emptyNamespace);

            Iterator<SOAPElement> defaultNamespaces = emptyNamespace.getChildElements(new QName("http://default/", "defaultNamespace"));
            assertTrue("no {http://default/}defaultNamespace element found", defaultNamespaces.hasNext());
            SOAPElement defaultNamespace = defaultNamespaces.next();
            assertFalse("more than one {http://default/}defaultNamespace element found", defaultNamespaces.hasNext());

            Iterator<SOAPElement> stillEmpties = emptyNamespace.getChildElements(new QName("stillEmpty"));
            assertTrue("no {}stillEmpty element found", stillEmpties.hasNext());
            SOAPElement stillEmpty = stillEmpties.next();
            assertFalse("more than one {}stillEmpty element found", stillEmpties.hasNext());
        }
}
