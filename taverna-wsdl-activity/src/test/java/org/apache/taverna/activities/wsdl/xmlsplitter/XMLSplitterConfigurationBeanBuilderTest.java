package org.apache.taverna.activities.wsdl.xmlsplitter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.StringReader;

import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;

public class XMLSplitterConfigurationBeanBuilderTest {

	@Test
	public void testBuildBeanForInput() throws Exception  {
		String xml = "<s:extensions xmlns:s=\"http://org.embl.ebi.escience/xscufl/0.1alpha\"><s:complextype optional=\"false\" unbounded=\"false\" typename=\"personToString\" name=\"parameters\" qname=\"{http://xfire.codehaus.org/BookService}personToString\"><s:elements><s:complextype optional=\"false\" unbounded=\"false\" typename=\"Person\" name=\"person\" qname=\"{http://xfire.codehaus.org/BookService}&gt;personToString&gt;person\"><s:elements><s:complextype optional=\"true\" unbounded=\"false\" typename=\"Address\" name=\"address\" qname=\"{http://complex.pojo.axis2.menagerie.googlecode}Person&gt;address\"><s:elements><s:basetype optional=\"true\" unbounded=\"false\" typename=\"string\" name=\"city\" qname=\"{http://complex.pojo.axis2.menagerie.googlecode}Address&gt;city\" /><s:basetype optional=\"true\" unbounded=\"false\" typename=\"string\" name=\"road\" qname=\"{http://complex.pojo.axis2.menagerie.googlecode}Address&gt;road\" /><s:basetype optional=\"true\" unbounded=\"false\" typename=\"int\" name=\"roadNumber\" qname=\"{http://complex.pojo.axis2.menagerie.googlecode}Address&gt;roadNumber\" /></s:elements></s:complextype><s:basetype optional=\"true\" unbounded=\"false\" typename=\"int\" name=\"age\" qname=\"{http://complex.pojo.axis2.menagerie.googlecode}Person&gt;age\" /><s:basetype optional=\"true\" unbounded=\"false\" typename=\"string\" name=\"firstName\" qname=\"{http://complex.pojo.axis2.menagerie.googlecode}Person&gt;firstName\" /><s:basetype optional=\"true\" unbounded=\"false\" typename=\"string\" name=\"lastName\" qname=\"{http://complex.pojo.axis2.menagerie.googlecode}Person&gt;lastName\" /></s:elements></s:complextype></s:elements></s:complextype></s:extensions>";
		Element element = new SAXBuilder().build(new StringReader(xml)).getRootElement();
		JsonNode bean = XMLSplitterConfigurationBeanBuilder.buildBeanForInput(element);
		assertNotNull("The bean should not be null",bean);

		assertEquals("There should be 1 input",1,bean.get("inputPorts").size());
		assertEquals("There should be 1 output",1,bean.get("outputPorts").size());

		assertEquals("The input should be named person","person",bean.get("inputPorts").get(0).get("name").textValue());
		assertEquals("The output should be named output","output",bean.get("outputPorts").get(0).get("name").textValue());

		assertEquals("The type xml definition should match",xml,bean.get("wrappedType").textValue());
	}

	@Test
	public void testBuildBeanForInput2() throws Exception {
		String xml = "<s:extensions xmlns:s=\"http://org.embl.ebi.escience/xscufl/0.1alpha\"><s:complextype optional=\"false\" unbounded=\"false\" typename=\"Person\" name=\"person\" qname=\"{http://xfire.codehaus.org/BookService}&gt;personToString&gt;person\"><s:elements><s:complextype optional=\"true\" unbounded=\"false\" typename=\"Address\" name=\"address\" qname=\"{http://complex.pojo.axis2.menagerie.googlecode}Person&gt;address\"><s:elements><s:basetype optional=\"true\" unbounded=\"false\" typename=\"string\" name=\"city\" qname=\"{http://complex.pojo.axis2.menagerie.googlecode}Address&gt;city\" /><s:basetype optional=\"true\" unbounded=\"false\" typename=\"string\" name=\"road\" qname=\"{http://complex.pojo.axis2.menagerie.googlecode}Address&gt;road\" /><s:basetype optional=\"true\" unbounded=\"false\" typename=\"int\" name=\"roadNumber\" qname=\"{http://complex.pojo.axis2.menagerie.googlecode}Address&gt;roadNumber\" /></s:elements></s:complextype><s:basetype optional=\"true\" unbounded=\"false\" typename=\"int\" name=\"age\" qname=\"{http://complex.pojo.axis2.menagerie.googlecode}Person&gt;age\" /><s:basetype optional=\"true\" unbounded=\"false\" typename=\"string\" name=\"firstName\" qname=\"{http://complex.pojo.axis2.menagerie.googlecode}Person&gt;firstName\" /><s:basetype optional=\"true\" unbounded=\"false\" typename=\"string\" name=\"lastName\" qname=\"{http://complex.pojo.axis2.menagerie.googlecode}Person&gt;lastName\" /></s:elements></s:complextype></s:extensions>";
		Element element = new SAXBuilder().build(new StringReader(xml)).getRootElement();

		JsonNode bean = XMLSplitterConfigurationBeanBuilder.buildBeanForInput(element);
		assertNotNull("The bean should not be null",bean);

		assertEquals("There should be 4 inputs",4,bean.get("inputPorts").size());
		assertEquals("There should be 1 output",1,bean.get("outputPorts").size());

		assertEquals("The first input should be named address","address",bean.get("inputPorts").get(0).get("name").textValue());
		assertEquals("The output should be named output","output",bean.get("outputPorts").get(0).get("name").textValue());

		assertEquals("The type xml definition should match",xml,bean.get("wrappedType").textValue());
	}

	@Test
	public void testBuildBeanForOutput() throws Exception {
		String xml = "<s:extensions xmlns:s=\"http://org.embl.ebi.escience/xscufl/0.1alpha\"><s:complextype optional=\"false\" unbounded=\"false\" typename=\"getPersonResponse\" name=\"parameters\" qname=\"{http://testing.org}getPersonResponse\"><s:elements><s:complextype optional=\"false\" unbounded=\"false\" typename=\"Person\" name=\"getPersonReturn\" qname=\"{http://testing.org}&gt;getPersonResponse&gt;getPersonReturn\"><s:elements><s:complextype optional=\"false\" unbounded=\"false\" typename=\"Address\" name=\"address\" qname=\"{http://testing.org}Person&gt;address\"><s:elements><s:basetype optional=\"false\" unbounded=\"false\" typename=\"string\" name=\"city\" qname=\"{http://testing.org}Address&gt;city\" /><s:basetype optional=\"false\" unbounded=\"false\" typename=\"int\" name=\"number\" qname=\"{http://testing.org}Address&gt;number\" /><s:basetype optional=\"false\" unbounded=\"false\" typename=\"string\" name=\"road\" qname=\"{http://testing.org}Address&gt;road\" /></s:elements></s:complextype><s:basetype optional=\"false\" unbounded=\"false\" typename=\"int\" name=\"age\" qname=\"{http://testing.org}Person&gt;age\" /><s:basetype optional=\"false\" unbounded=\"false\" typename=\"string\" name=\"name\" qname=\"{http://testing.org}Person&gt;name\" /></s:elements></s:complextype></s:elements></s:complextype></s:extensions>";
		Element element = new SAXBuilder().build(new StringReader(xml)).getRootElement();
		JsonNode bean = XMLSplitterConfigurationBeanBuilder.buildBeanForOutput(element);

		assertNotNull("The bean should not be null",bean);

		assertEquals("There should be 1 input",1,bean.get("inputPorts").size());
		assertEquals("There should be 1 output",1,bean.get("outputPorts").size());

		assertEquals("The input should be named input","input",bean.get("inputPorts").get(0).get("name").textValue());
		assertEquals("The output shouldbe named getPersonResponse","getPersonReturn",bean.get("outputPorts").get(0).get("name").textValue());

		assertEquals("The type xml definition should match",xml,bean.get("wrappedType").textValue());
	}

}
