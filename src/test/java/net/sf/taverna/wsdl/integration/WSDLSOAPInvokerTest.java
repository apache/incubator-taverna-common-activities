/*
 * Copyright (C) 2003 The University of Manchester 
 *
 * Modifications to the initial code base are copyright of their
 * respective authors, or their employers as appropriate.  Authorship
 * of the modifications may be determined from the ChangeLog placed at
 * the end of this file.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA.
 *
 ****************************************************************
 * Source code information
 * -----------------------
 * Filename           $RCSfile: WSDLSOAPInvokerTest.java,v $
 * Revision           $Revision: 1.8 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2008/09/18 10:37:21 $
 *               by   $Author: stain $
 * Created on 04-May-2006
 *****************************************************************/
package net.sf.taverna.wsdl.integration;

import net.sf.taverna.wsdl.soap.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.taverna.wsdl.parser.TypeDescriptor;
import net.sf.taverna.wsdl.parser.WSDLParser;
import net.sf.taverna.wsdl.testutils.LocationConstants;

import org.junit.Ignore;
import org.junit.Test;

public class WSDLSOAPInvokerTest  implements LocationConstants {

	@SuppressWarnings("unchecked")
	@Test    
	public void testPrimitive() throws Exception {        
		WSDLParser wsdlParser = new WSDLParser(WSDL_TEST_BASE
				+ "KEGG.wsdl");
		List<String> outputNames = new ArrayList<String>();
		for (TypeDescriptor d : wsdlParser
				.getOperationOutputParameters("get_pathways_by_genes")) {
			outputNames.add(d.getName());
		}
		WSDLSOAPInvoker invoker = new WSDLSOAPInvoker(wsdlParser,
				"get_pathways_by_genes", outputNames);

		List<String> inputs = new ArrayList<String>();
		inputs.add("eco:b0077");
		inputs.add("eco:b0078");

		Map inputMap = new HashMap();
		inputMap.put("genes_id_list", inputs);

		Map outputMap = invoker.invoke(inputMap);

		assertEquals("should be 2 elements to output", 2, outputMap.size());

		Object outputThing = outputMap.get("return");

		assertNotNull("no return value with name 'return' found", outputThing);

		assertEquals("result should be an ArrayList", ArrayList.class,
				outputThing.getClass());

	}

	@SuppressWarnings("unchecked")
	@Test    
	public void testComplexDocStyle() throws Exception {

		WSDLParser wsdlParser = new WSDLParser(WSDL_TEST_BASE
				+ "eutils/eutils_lite.wsdl");
		List<String> outputNames = new ArrayList<String>();
		for (TypeDescriptor d : wsdlParser
				.getOperationOutputParameters("run_eInfo")) {
			outputNames.add(d.getName());
		}
		WSDLSOAPInvoker invoker = new WSDLSOAPInvoker(wsdlParser, "run_eInfo",
				outputNames);

		String input = "<eInfoRequest><db>pubmed</db></eInfoRequest>";

		Map inputMap = new HashMap();
		inputMap.put("parameters", input);

		Map outputMap = invoker.invoke(inputMap);

		assertEquals("should be 2 elements to output", 2, outputMap.size());

		Object outputThing = outputMap.get("parameters");

		assertNotNull("no return with name 'parameters' found", outputThing);

		assertEquals("result should be a string", String.class, outputThing
				.getClass());

		String xml = outputThing.toString();
		assertTrue("unexpected start to result", xml.startsWith("<eInfoResult"));
		assertTrue("unexpected end to result", xml.endsWith("/eInfoResult>"));

	}
	
	@SuppressWarnings("unchecked")
	@Test
    @Ignore("Throws out of memory error. Needs to use menagerie webservice")
	//TODO: set up an equivalent test on Phoebus - this service is unreliable.
	public void testComplexMultiRef() throws Exception {

		WSDLParser wsdlParser = new WSDLParser(WSDL_TEST_BASE
				+ "ma.wsdl");
		List<String> outputNames = new ArrayList<String>();
		for (TypeDescriptor d : wsdlParser
				.getOperationOutputParameters("whatGeneInStage")) {
			outputNames.add(d.getName());
		}
		WSDLSOAPInvoker invoker = new WSDLSOAPInvoker(wsdlParser,
				"whatGeneInStage", outputNames);

		Map inputs = new HashMap();
		inputs.put("in0", "13");
		inputs.put("in1", "14");
		inputs.put("in2", "1");

		Map outputs = invoker.invoke(inputs);
		assertEquals("outputs should have 2 entries", 2, outputs.size());

		Object thing = outputs.get("whatGeneInStageReturn");

		assertNotNull(
				"output should contain an entry with key 'whatGeneInStageReturn'",
				thing);

		assertEquals("output type should be of type String", String.class,
				thing.getClass());

	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testMultirefWithOutputNamespaced() throws Exception {
		WSDLParser wsdlParser = new WSDLParser(WSDL_TEST_BASE
				+ "Annotation.wsdl");
		List<String> outputNames = new ArrayList<String>();
		for (TypeDescriptor d : wsdlParser
				.getOperationOutputParameters("getDatabasesWithDetails")) {
			outputNames.add(d.getName());
		}
		WSDLSOAPInvoker invoker = new WSDLSOAPInvoker(wsdlParser,
				"getDatabasesWithDetails", outputNames);

		Map output = invoker.invoke(new HashMap());

		assertNotNull("no result returned", output
				.get("getDatabasesWithDetailsReturn"));
	}

	@SuppressWarnings("unchecked")
	@Test    
	public void testSOAPEncoded() throws Exception {
		WSDLParser wsdlParser = new WSDLParser(
				"http://www.mygrid.org.uk/menagerie/xfire/Primatives-re?wsdl");
		List<String> outputNames = new ArrayList<String>();
		for (TypeDescriptor d : wsdlParser
				.getOperationOutputParameters("echo")) {
			outputNames.add(d.getName());
		}
		WSDLSOAPInvoker invoker = new WSDLSOAPInvoker(wsdlParser,
				"echo", outputNames);
		Map inputs = new HashMap();
		inputs.put("msg","fred");
		Map output = invoker.invoke(inputs);
		
		assertEquals("should be 2 elements", 2, output.size());
		Object outputThing = output.get("out");
		assertNotNull("there should be a result of name 'out'", outputThing);
		assertEquals("output data should be String", String.class,
				outputThing.getClass());
		assertEquals("The output should be fred","fred",outputThing);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testDocumentNamespace() throws Exception {

		WSDLParser wsdlParser = new WSDLParser(WSDL_TEST_BASE
				+ "CountryInfoService.wsdl");
		List<String> outputNames = new ArrayList<String>();
		for (TypeDescriptor d : wsdlParser
				.getOperationOutputParameters("CapitalCity")) {
			outputNames.add(d.getName());
		}
		WSDLSOAPInvoker invoker = new WSDLSOAPInvoker(wsdlParser,
				"CapitalCity", outputNames);

		Map inputMap = new HashMap();
		inputMap
				.put("parameters",
						"<parameters><sCountryISOCode>FR</sCountryISOCode></parameters>");

		invoker.invoke(inputMap);
	}

	// can't always assume the return will be nested in a tag named the same as
	// the output message part.
	@SuppressWarnings("unchecked")
	@Test    
	public void testEncodedDifferentOutputName() throws Exception {
		System.setProperty("taverna.wsdl.timeout", "1");
		WSDLParser wsdlParser = new WSDLParser(
				"http://biowulf.bu.edu/zlab/promoser/promoser.wsdl");
		List<String> outputNames = new ArrayList<String>();
		for (TypeDescriptor d : wsdlParser.getOperationOutputParameters("help")) {
			outputNames.add(d.getName());
		}
		WSDLSOAPInvoker invoker = new WSDLSOAPInvoker(wsdlParser, "help",
				outputNames);
		Map output = invoker.invoke(new HashMap());

		assertNotNull("missing output", output.get("helpResponseSoapMsg"));

		Object thing = output.get("helpResponseSoapMsg");

		assertTrue("unexpected output contents", thing.toString().startsWith(
				"Usage:"));
	}
}
