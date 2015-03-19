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

package org.apache.taverna.activities.wsdl.xmlsplitter;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.StringReader;

import net.sf.taverna.t2.workflowmodel.impl.EditsImpl;
import org.apache.taverna.wsdl.parser.ComplexTypeDescriptor;

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
