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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.StringReader;
import java.util.List;

import net.sf.taverna.wsdl.parser.ComplexTypeDescriptor;
import net.sf.taverna.wsdl.parser.TypeDescriptor;

import org.embl.ebi.escience.scufl.XScufl;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;
import org.junit.Test;

public class XMLSplitterSerialisationHelperTest {

	@Test
	public void testScuflNS() throws Exception {
		assertEquals("namespace should be equal",XScufl.XScuflNS,XMLSplitterSerialisationHelper.XScuflNS);
	}
	
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
