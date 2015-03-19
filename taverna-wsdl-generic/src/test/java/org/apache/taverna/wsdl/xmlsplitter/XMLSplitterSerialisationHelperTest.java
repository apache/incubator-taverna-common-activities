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

package org.apache.taverna.wsdl.xmlsplitter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.StringReader;
import java.util.List;

import org.apache.taverna.wsdl.parser.ComplexTypeDescriptor;
import org.apache.taverna.wsdl.parser.TypeDescriptor;

//import org.embl.ebi.escience.scufl.XScufl;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;
import org.junit.Test;

public class XMLSplitterSerialisationHelperTest {

//	@Test
//	public void testScuflNS() throws Exception {
//		assertEquals("namespace should be equal",XScufl.XScuflNS,XMLSplitterSerialisationHelper.XScuflNS);
//	}

    @Test
	public void testCyclicToElement() throws Exception {
		ComplexTypeDescriptor a = new ComplexTypeDescriptor();
		a.setName("a");
		a.setType("typename");
		a.setQnameFromString("{namespace}typename");

		ComplexTypeDescriptor b = new ComplexTypeDescriptor();
		b.setName("b");
		b.setType("typename2");
		b.setQnameFromString("{namespace}typename2");

		a.getElements().add(b);

		b.getElements().add(a);

		Element el = XMLSplitterSerialisationHelper
				.typeDescriptorToExtensionXML(a);

		String xml = new XMLOutputter().outputString(el);

		assertEquals(
				"unexpected xml",
				"<s:extensions xmlns:s=\"http://org.embl.ebi.escience/xscufl/0.1alpha\"><s:complextype optional=\"false\" unbounded=\"false\" typename=\"typename\" name=\"a\" qname=\"{namespace}typename\"><s:elements><s:complextype optional=\"false\" unbounded=\"false\" typename=\"typename2\" name=\"b\" qname=\"{namespace}typename2\"><s:elements><s:complextype id=\"{namespace}typename\" optional=\"false\" unbounded=\"false\" typename=\"typename\" name=\"a\" /></s:elements></s:complextype></s:elements></s:complextype></s:extensions>",
				xml);

	}

    @Test
	public void testCyclicToElement2() throws Exception {
		ComplexTypeDescriptor a = new ComplexTypeDescriptor();
		a.setName("a");
		a.setType("typename");
		a.setQnameFromString("{namespace}typename");

		a.getElements().add(a);

		Element el = XMLSplitterSerialisationHelper
				.typeDescriptorToExtensionXML(a);

		String xml = new XMLOutputter().outputString(el);

		assertEquals(
				"unexpected xml",
				"<s:extensions xmlns:s=\"http://org.embl.ebi.escience/xscufl/0.1alpha\"><s:complextype optional=\"false\" unbounded=\"false\" typename=\"typename\" name=\"a\" qname=\"{namespace}typename\"><s:elements><s:complextype id=\"{namespace}typename\" optional=\"false\" unbounded=\"false\" typename=\"typename\" name=\"a\" /></s:elements></s:complextype></s:extensions>",
				xml);
	}

    @Test
	public void testCyclicFromElement() throws Exception {
		String xml = "<s:extensions xmlns:s=\"http://org.embl.ebi.escience/xscufl/0.1alpha\"><s:complextype optional=\"false\" unbounded=\"false\" typename=\"typename\" name=\"a\" qname=\"{namespace}typename\"><s:elements><s:complextype id=\"{namespace}typename\" /></s:elements></s:complextype></s:extensions>";
		Element el = new SAXBuilder().build(new StringReader(xml))
				.getRootElement();

		TypeDescriptor a = XMLSplitterSerialisationHelper
				.extensionXMLToTypeDescriptor(el);

		assertTrue("wrong type", a instanceof ComplexTypeDescriptor);
		assertEquals("wrong name", "a", a.getName());

		List<TypeDescriptor> a_elements = ((ComplexTypeDescriptor) a).getElements();

		assertEquals("should be only 1 element", 1, a_elements.size());

		TypeDescriptor b = a_elements.get(0);

		assertTrue("wrong type", b instanceof ComplexTypeDescriptor);

		List<TypeDescriptor> b_elements = ((ComplexTypeDescriptor) b).getElements();

		assertEquals("should be only 1 element", 1, b_elements.size());

		assertEquals("b should contain a reference to a", a.toString(),
				b_elements.get(0).toString());
	}

	/**
	 * Tests the QName is constructed with the correct URI and LocalPart
	 * 
	 * @throws Exception
	 */
    @Test
	public void testCorrectQName() throws Exception {
		TypeDescriptor desc = XMLSplitterSerialisationHelper
				.extensionXMLToTypeDescriptor(new SAXBuilder().build(
						new StringReader(eInfoXML())).getRootElement());
		assertEquals("NamespaceURI is incorrect",
				"http://www.ncbi.nlm.nih.gov/soap/eutils/espell", desc
						.getQname().getNamespaceURI());
		assertEquals("Localpart is incorrect", "eSpellRequest", desc.getQname()
				.getLocalPart());
	}

	private String eInfoXML() {
		return "<s:extensions xmlns:s=\"http://org.embl.ebi.escience/xscufl/0.1alpha\"><s:complextype optional=\"false\" unbounded=\"false\" typename=\"eSpellRequest\" name=\"parameters\" qname=\"{http://www.ncbi.nlm.nih.gov/soap/eutils/espell}eSpellRequest\"><s:elements><s:basetype optional=\"true\" unbounded=\"false\" typename=\"string\" name=\"db\" qname=\"{http://www.w3.org/2001/XMLSchema}string\" /><s:basetype optional=\"true\" unbounded=\"false\" typename=\"string\" name=\"term\" qname=\"{http://www.w3.org/2001/XMLSchema}string\" /><s:basetype optional=\"true\" unbounded=\"false\" typename=\"string\" name=\"tool\" qname=\"{http://www.w3.org/2001/XMLSchema}string\" /><s:basetype optional=\"true\" unbounded=\"false\" typename=\"string\" name=\"email\" qname=\"{http://www.w3.org/2001/XMLSchema}string\" /></s:elements></s:complextype></s:extensions>";

	}
}
