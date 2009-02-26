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

import static org.junit.Assert.*;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityInputPort;
import net.sf.taverna.wsdl.parser.BaseTypeDescriptor;
import net.sf.taverna.wsdl.parser.ComplexTypeDescriptor;

import org.junit.Test;

public class XMLInputSplitterActivityTest {

	@Test
	public void testGetTypeDescriptorForInputPort() throws Exception {
		String xml = "<s:extensions xmlns:s=\"http://org.embl.ebi.escience/xscufl/0.1alpha\"><s:complextype optional=\"false\" unbounded=\"false\" typename=\"personToString\" name=\"parameters\" qname=\"{http://xfire.codehaus.org/BookService}personToString\"><s:elements><s:complextype optional=\"false\" unbounded=\"false\" typename=\"Person\" name=\"person\" qname=\"{http://xfire.codehaus.org/BookService}&gt;personToString&gt;person\"><s:elements><s:complextype optional=\"true\" unbounded=\"false\" typename=\"Address\" name=\"address\" qname=\"{http://complex.pojo.axis2.menagerie.googlecode}Person&gt;address\"><s:elements><s:basetype optional=\"true\" unbounded=\"false\" typename=\"string\" name=\"city\" qname=\"{http://complex.pojo.axis2.menagerie.googlecode}Address&gt;city\" /><s:basetype optional=\"true\" unbounded=\"false\" typename=\"string\" name=\"road\" qname=\"{http://complex.pojo.axis2.menagerie.googlecode}Address&gt;road\" /><s:basetype optional=\"true\" unbounded=\"false\" typename=\"int\" name=\"roadNumber\" qname=\"{http://complex.pojo.axis2.menagerie.googlecode}Address&gt;roadNumber\" /></s:elements></s:complextype><s:basetype optional=\"true\" unbounded=\"false\" typename=\"int\" name=\"age\" qname=\"{http://complex.pojo.axis2.menagerie.googlecode}Person&gt;age\" /><s:basetype optional=\"true\" unbounded=\"false\" typename=\"string\" name=\"firstName\" qname=\"{http://complex.pojo.axis2.menagerie.googlecode}Person&gt;firstName\" /><s:basetype optional=\"true\" unbounded=\"false\" typename=\"string\" name=\"lastName\" qname=\"{http://complex.pojo.axis2.menagerie.googlecode}Person&gt;lastName\" /></s:elements></s:complextype></s:elements></s:complextype></s:extensions>";
		XMLSplitterConfigurationBean bean = XMLSplitterConfigurationBeanBuilder.buildBeanForInput(xml);
		XMLInputSplitterActivity a = new XMLInputSplitterActivity();
		a.configure(bean);
		
		boolean exists = false;
		for (ActivityInputPort p : a.getInputPorts()) {
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
		XMLSplitterConfigurationBean bean = XMLSplitterConfigurationBeanBuilder.buildBeanForInput(xml);
		XMLInputSplitterActivity a = new XMLInputSplitterActivity();
		a.configure(bean);
		
		boolean exists = false;
		for (ActivityInputPort p : a.getInputPorts()) {
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
