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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class XMLSplitterConfigurationBeanBuilderTest {

	

	@Test
	public void testBuildBeanForInput() throws Exception  {
		
		String xml = "<s:extensions xmlns:s=\"http://org.embl.ebi.escience/xscufl/0.1alpha\"><s:complextype optional=\"false\" unbounded=\"false\" typename=\"personToString\" name=\"parameters\" qname=\"{http://xfire.codehaus.org/BookService}personToString\"><s:elements><s:complextype optional=\"false\" unbounded=\"false\" typename=\"Person\" name=\"person\" qname=\"{http://xfire.codehaus.org/BookService}&gt;personToString&gt;person\"><s:elements><s:complextype optional=\"true\" unbounded=\"false\" typename=\"Address\" name=\"address\" qname=\"{http://complex.pojo.axis2.menagerie.googlecode}Person&gt;address\"><s:elements><s:basetype optional=\"true\" unbounded=\"false\" typename=\"string\" name=\"city\" qname=\"{http://complex.pojo.axis2.menagerie.googlecode}Address&gt;city\" /><s:basetype optional=\"true\" unbounded=\"false\" typename=\"string\" name=\"road\" qname=\"{http://complex.pojo.axis2.menagerie.googlecode}Address&gt;road\" /><s:basetype optional=\"true\" unbounded=\"false\" typename=\"int\" name=\"roadNumber\" qname=\"{http://complex.pojo.axis2.menagerie.googlecode}Address&gt;roadNumber\" /></s:elements></s:complextype><s:basetype optional=\"true\" unbounded=\"false\" typename=\"int\" name=\"age\" qname=\"{http://complex.pojo.axis2.menagerie.googlecode}Person&gt;age\" /><s:basetype optional=\"true\" unbounded=\"false\" typename=\"string\" name=\"firstName\" qname=\"{http://complex.pojo.axis2.menagerie.googlecode}Person&gt;firstName\" /><s:basetype optional=\"true\" unbounded=\"false\" typename=\"string\" name=\"lastName\" qname=\"{http://complex.pojo.axis2.menagerie.googlecode}Person&gt;lastName\" /></s:elements></s:complextype></s:elements></s:complextype></s:extensions>";
		XMLSplitterConfigurationBean bean = XMLSplitterConfigurationBeanBuilder.buildBeanForInput(xml);
		assertNotNull("The bean should not be null",bean);
		
		assertEquals("There should be 1 input",1,bean.getInputPortDefinitions().size());
		assertEquals("There should be 1 output",1,bean.getOutputPortDefinitions().size());
		
		assertEquals("The input should be named person","person",bean.getInputPortDefinitions().get(0).getName());
		assertEquals("The output should be named output","output",bean.getOutputPortDefinitions().get(0).getName());
		
		assertEquals("The type xml definition should match",xml,bean.getWrappedTypeXML());
	}
	
	@Test
	public void testBuildBeanForInput2() throws Exception {
		String xml = "<s:extensions xmlns:s=\"http://org.embl.ebi.escience/xscufl/0.1alpha\"><s:complextype optional=\"false\" unbounded=\"false\" typename=\"Person\" name=\"person\" qname=\"{http://xfire.codehaus.org/BookService}&gt;personToString&gt;person\"><s:elements><s:complextype optional=\"true\" unbounded=\"false\" typename=\"Address\" name=\"address\" qname=\"{http://complex.pojo.axis2.menagerie.googlecode}Person&gt;address\"><s:elements><s:basetype optional=\"true\" unbounded=\"false\" typename=\"string\" name=\"city\" qname=\"{http://complex.pojo.axis2.menagerie.googlecode}Address&gt;city\" /><s:basetype optional=\"true\" unbounded=\"false\" typename=\"string\" name=\"road\" qname=\"{http://complex.pojo.axis2.menagerie.googlecode}Address&gt;road\" /><s:basetype optional=\"true\" unbounded=\"false\" typename=\"int\" name=\"roadNumber\" qname=\"{http://complex.pojo.axis2.menagerie.googlecode}Address&gt;roadNumber\" /></s:elements></s:complextype><s:basetype optional=\"true\" unbounded=\"false\" typename=\"int\" name=\"age\" qname=\"{http://complex.pojo.axis2.menagerie.googlecode}Person&gt;age\" /><s:basetype optional=\"true\" unbounded=\"false\" typename=\"string\" name=\"firstName\" qname=\"{http://complex.pojo.axis2.menagerie.googlecode}Person&gt;firstName\" /><s:basetype optional=\"true\" unbounded=\"false\" typename=\"string\" name=\"lastName\" qname=\"{http://complex.pojo.axis2.menagerie.googlecode}Person&gt;lastName\" /></s:elements></s:complextype></s:extensions>";
		
		XMLSplitterConfigurationBean bean = XMLSplitterConfigurationBeanBuilder.buildBeanForInput(xml);
		assertNotNull("The bean should not be null",bean);
		
		assertEquals("There should be 4 inputs",4,bean.getInputPortDefinitions().size());
		assertEquals("There should be 1 output",1,bean.getOutputPortDefinitions().size());
		
		assertEquals("The first input should be named address","address",bean.getInputPortDefinitions().get(0).getName());
		assertEquals("The output should be named output","output",bean.getOutputPortDefinitions().get(0).getName());
		
		assertEquals("The type xml definition should match",xml,bean.getWrappedTypeXML());
	}

	@Test
	public void testBuildBeanForOutput() throws Exception {
		String xml = "<s:extensions xmlns:s=\"http://org.embl.ebi.escience/xscufl/0.1alpha\"><s:complextype optional=\"false\" unbounded=\"false\" typename=\"getPersonResponse\" name=\"parameters\" qname=\"{http://testing.org}getPersonResponse\"><s:elements><s:complextype optional=\"false\" unbounded=\"false\" typename=\"Person\" name=\"getPersonReturn\" qname=\"{http://testing.org}&gt;getPersonResponse&gt;getPersonReturn\"><s:elements><s:complextype optional=\"false\" unbounded=\"false\" typename=\"Address\" name=\"address\" qname=\"{http://testing.org}Person&gt;address\"><s:elements><s:basetype optional=\"false\" unbounded=\"false\" typename=\"string\" name=\"city\" qname=\"{http://testing.org}Address&gt;city\" /><s:basetype optional=\"false\" unbounded=\"false\" typename=\"int\" name=\"number\" qname=\"{http://testing.org}Address&gt;number\" /><s:basetype optional=\"false\" unbounded=\"false\" typename=\"string\" name=\"road\" qname=\"{http://testing.org}Address&gt;road\" /></s:elements></s:complextype><s:basetype optional=\"false\" unbounded=\"false\" typename=\"int\" name=\"age\" qname=\"{http://testing.org}Person&gt;age\" /><s:basetype optional=\"false\" unbounded=\"false\" typename=\"string\" name=\"name\" qname=\"{http://testing.org}Person&gt;name\" /></s:elements></s:complextype></s:elements></s:complextype></s:extensions>";
		XMLSplitterConfigurationBean bean = XMLSplitterConfigurationBeanBuilder.buildBeanForOutput(xml);
		
		assertNotNull("The bean should not be null",bean);
		
		assertEquals("There should be 1 input",1,bean.getInputPortDefinitions().size());
		assertEquals("There should be 1 output",1,bean.getOutputPortDefinitions().size());
		
		assertEquals("The input should be named input","input",bean.getInputPortDefinitions().get(0).getName());
		assertEquals("The output shouldbe named getPersonResponse","getPersonReturn",bean.getOutputPortDefinitions().get(0).getName());
		
		assertEquals("The type xml definition should match",xml,bean.getWrappedTypeXML());
	}

}
