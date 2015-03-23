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

import org.apache.taverna.workflowmodel.impl.EditsImpl;
import org.apache.taverna.workflowmodel.processor.activity.ActivityInputPort;
import org.apache.taverna.wsdl.parser.BaseTypeDescriptor;
import org.apache.taverna.wsdl.parser.ComplexTypeDescriptor;

import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;

public class XMLInputSplitterActivityTest {

	@Test
	public void testGetTypeDescriptorForInputPort() throws Exception {
		String xml = "<s:extensions xmlns:s=\"http://org.embl.ebi.escience/xscufl/0.1alpha\"><s:complextype optional=\"false\" unbounded=\"false\" typename=\"personToString\" name=\"parameters\" qname=\"{http://xfire.codehaus.org/BookService}personToString\"><s:elements><s:complextype optional=\"false\" unbounded=\"false\" typename=\"Person\" name=\"person\" qname=\"{http://xfire.codehaus.org/BookService}&gt;personToString&gt;person\"><s:elements><s:complextype optional=\"true\" unbounded=\"false\" typename=\"Address\" name=\"address\" qname=\"{http://complex.pojo.axis2.menagerie.googlecode}Person&gt;address\"><s:elements><s:basetype optional=\"true\" unbounded=\"false\" typename=\"string\" name=\"city\" qname=\"{http://complex.pojo.axis2.menagerie.googlecode}Address&gt;city\" /><s:basetype optional=\"true\" unbounded=\"false\" typename=\"string\" name=\"road\" qname=\"{http://complex.pojo.axis2.menagerie.googlecode}Address&gt;road\" /><s:basetype optional=\"true\" unbounded=\"false\" typename=\"int\" name=\"roadNumber\" qname=\"{http://complex.pojo.axis2.menagerie.googlecode}Address&gt;roadNumber\" /></s:elements></s:complextype><s:basetype optional=\"true\" unbounded=\"false\" typename=\"int\" name=\"age\" qname=\"{http://complex.pojo.axis2.menagerie.googlecode}Person&gt;age\" /><s:basetype optional=\"true\" unbounded=\"false\" typename=\"string\" name=\"firstName\" qname=\"{http://complex.pojo.axis2.menagerie.googlecode}Person&gt;firstName\" /><s:basetype optional=\"true\" unbounded=\"false\" typename=\"string\" name=\"lastName\" qname=\"{http://complex.pojo.axis2.menagerie.googlecode}Person&gt;lastName\" /></s:elements></s:complextype></s:elements></s:complextype></s:extensions>";
		Element element = new SAXBuilder().build(new StringReader(xml)).getRootElement();
		JsonNode bean = XMLSplitterConfigurationBeanBuilder.buildBeanForInput(element);
		XMLInputSplitterActivity a = new XMLInputSplitterActivity();
		a.configure(bean);
		XMLInputSplitterActivityFactory af = new XMLInputSplitterActivityFactory();
		af.setEdits(new EditsImpl());

		boolean exists = false;
		for (ActivityInputPort p : af.getInputPorts(bean)) {
			if (p.getName().equals("person")) {
				exists=true;
				break;
			}
		}

		assertTrue("The input port named person should have been found",exists);

		assertNotNull("There should be a type descriptor for person",a.getTypeDescriptorForInputPort("person"));
		assertTrue("The descriptor should be complex",a.getTypeDescriptorForInputPort("person") instanceof ComplexTypeDescriptor);
	}

	@Test
	public void testGetTypeDescriptorForInputPort2() throws Exception {
		String xml = "<s:extensions xmlns:s=\"http://org.embl.ebi.escience/xscufl/0.1alpha\"><s:complextype optional=\"false\" unbounded=\"false\" typename=\"Person\" name=\"person\" qname=\"{http://xfire.codehaus.org/BookService}&gt;personToString&gt;person\"><s:elements><s:complextype optional=\"true\" unbounded=\"false\" typename=\"Address\" name=\"address\" qname=\"{http://complex.pojo.axis2.menagerie.googlecode}Person&gt;address\"><s:elements><s:basetype optional=\"true\" unbounded=\"false\" typename=\"string\" name=\"city\" qname=\"{http://complex.pojo.axis2.menagerie.googlecode}Address&gt;city\" /><s:basetype optional=\"true\" unbounded=\"false\" typename=\"string\" name=\"road\" qname=\"{http://complex.pojo.axis2.menagerie.googlecode}Address&gt;road\" /><s:basetype optional=\"true\" unbounded=\"false\" typename=\"int\" name=\"roadNumber\" qname=\"{http://complex.pojo.axis2.menagerie.googlecode}Address&gt;roadNumber\" /></s:elements></s:complextype><s:basetype optional=\"true\" unbounded=\"false\" typename=\"int\" name=\"age\" qname=\"{http://complex.pojo.axis2.menagerie.googlecode}Person&gt;age\" /><s:basetype optional=\"true\" unbounded=\"false\" typename=\"string\" name=\"firstName\" qname=\"{http://complex.pojo.axis2.menagerie.googlecode}Person&gt;firstName\" /><s:basetype optional=\"true\" unbounded=\"false\" typename=\"string\" name=\"lastName\" qname=\"{http://complex.pojo.axis2.menagerie.googlecode}Person&gt;lastName\" /></s:elements></s:complextype></s:extensions>";
		Element element = new SAXBuilder().build(new StringReader(xml)).getRootElement();
		JsonNode bean = XMLSplitterConfigurationBeanBuilder.buildBeanForInput(element);
		XMLInputSplitterActivity a = new XMLInputSplitterActivity();
		a.configure(bean);
		XMLInputSplitterActivityFactory af = new XMLInputSplitterActivityFactory();
		af.setEdits(new EditsImpl());

		boolean exists = false;
		for (ActivityInputPort p : af.getInputPorts(bean)) {
			if (p.getName().equals("firstName")) {
				exists=true;
				break;
			}
		}

		assertTrue("The input port named firstName should have been found",exists);

		assertNotNull("There should be a type descriptor for person",a.getTypeDescriptorForInputPort("firstName"));
		assertTrue("The descriptor should be base type",a.getTypeDescriptorForInputPort("firstName") instanceof BaseTypeDescriptor);
	}
}
