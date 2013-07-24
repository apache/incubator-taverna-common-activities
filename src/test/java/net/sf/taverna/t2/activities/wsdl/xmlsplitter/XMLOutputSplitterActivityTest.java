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
package net.sf.taverna.t2.activities.wsdl.xmlsplitter;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.StringReader;

import net.sf.taverna.t2.workflowmodel.impl.EditsImpl;
import net.sf.taverna.wsdl.parser.ComplexTypeDescriptor;

import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;

public class XMLOutputSplitterActivityTest {

	@Test
	public void testGetTypeDescriptorForOutputPort() throws Exception {

		String xml = "<s:extensions xmlns:s=\"http://org.embl.ebi.escience/xscufl/0.1alpha\"><s:complextype optional=\"false\" unbounded=\"false\" typename=\"getPersonResponse\" name=\"parameters\" qname=\"{http://testing.org}getPersonResponse\"><s:elements><s:complextype optional=\"false\" unbounded=\"false\" typename=\"Person\" name=\"getPersonReturn\" qname=\"{http://testing.org}&gt;getPersonResponse&gt;getPersonReturn\"><s:elements><s:complextype optional=\"false\" unbounded=\"false\" typename=\"Address\" name=\"address\" qname=\"{http://testing.org}Person&gt;address\"><s:elements><s:basetype optional=\"false\" unbounded=\"false\" typename=\"string\" name=\"city\" qname=\"{http://testing.org}Address&gt;city\" /><s:basetype optional=\"false\" unbounded=\"false\" typename=\"int\" name=\"number\" qname=\"{http://testing.org}Address&gt;number\" /><s:basetype optional=\"false\" unbounded=\"false\" typename=\"string\" name=\"road\" qname=\"{http://testing.org}Address&gt;road\" /></s:elements></s:complextype><s:basetype optional=\"false\" unbounded=\"false\" typename=\"int\" name=\"age\" qname=\"{http://testing.org}Person&gt;age\" /><s:basetype optional=\"false\" unbounded=\"false\" typename=\"string\" name=\"name\" qname=\"{http://testing.org}Person&gt;name\" /></s:elements></s:complextype></s:elements></s:complextype></s:extensions>";
		Element element = new SAXBuilder().build(new StringReader(xml)).getRootElement();
		JsonNode bean = XMLSplitterConfigurationBeanBuilder.buildBeanForOutput(element);
		XMLOutputSplitterActivity a = new XMLOutputSplitterActivity();
		a.setEdits(new EditsImpl());
		a.configure(bean);

		assertNotNull("There should be a descriptor for the port getPersonReturn",a.getTypeDescriptorForOutputPort("getPersonReturn"));
		assertTrue("The descriptor should be complex",a.getTypeDescriptorForOutputPort("getPersonReturn") instanceof ComplexTypeDescriptor);

	}
}
