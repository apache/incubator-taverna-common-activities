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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.taverna.t2.activities.testutils.ActivityInvoker;
import net.sf.taverna.t2.activities.testutils.LocationConstants;
import net.sf.taverna.t2.workflowmodel.OutputPort;
import net.sf.taverna.wsdl.parser.ComplexTypeDescriptor;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

public class WSDLActivityTest implements LocationConstants {

	private static WSDLActivity activity;
	private static WSDLActivityConfigurationBean configBean;
	private static String wsdlLocation = WSDL_TEST_BASE
			+ "eutils/eutils_lite.wsdl";

	//@BeforeClass
    @Ignore("Integration test")
	public static void setUp() throws Exception {
		activity = new WSDLActivity();
		configBean = new WSDLActivityConfigurationBean();
		configBean.setOperation("run_eInfo");
		configBean.setWsdl(wsdlLocation);
		activity.configure(configBean);
	}

	@Test
    @Ignore("Integration test")
	public void testConfigureWSDLActivityConfigurationBean() throws Exception {
		assertEquals("There should be 1 input ports", 1, activity
				.getInputPorts().size());
		assertEquals("There should be 2 output ports", 2, activity
				.getOutputPorts().size());

		assertEquals("parameters", activity.getInputPorts().iterator().next()
				.getName());

		List<String> expectedOutputNames = new ArrayList<String>();
		expectedOutputNames.add("parameters");
		expectedOutputNames.add("attachmentList");
		for (OutputPort port : activity.getOutputPorts()) {
			assertTrue("Unexpected output name:" + port.getName(),
					expectedOutputNames.contains(port.getName()));
			expectedOutputNames.remove(port.getName());
		}
		assertEquals(
				"Not all of the expected outputs were found, those remainng are:"
						+ expectedOutputNames.toArray(), 0, expectedOutputNames
						.size());
	}

	@Test
    @Ignore("Integration test")
	public void testGetConfiguration() throws Exception {
		assertSame(configBean, activity.getConfiguration());
	}

	@Test
	@Ignore("Service is broken")
	public void testExecuteAsynchMapOfStringEntityIdentifierAsynchronousActivityCallback()
			throws Exception {
		Map<String, Object> inputMap = new HashMap<String, Object>();
		inputMap.put("parameters", "<parameters><db>pubmed</db></parameters>");

		Map<String, Class<?>> expectedOutputs = new HashMap<String, Class<?>>();
		expectedOutputs.put("parameters", String.class);

		Map<String, Object> outputMap = ActivityInvoker.invokeAsyncActivity(
				activity, inputMap, expectedOutputs);
		assertNotNull("there should be an output named parameters", outputMap
				.get("parameters"));
		String xml;
		if (outputMap.get("parameters") instanceof String) {
			xml = (String) outputMap.get("parameters");
		} else {
			byte[] bytes = (byte[]) outputMap.get("parameters");
			xml = new String(bytes);
		}

		assertTrue("the xml is not what was expected", xml
				.contains("<DbName>pubmed</DbName>"));
	}

    @Ignore("Integration test")
	@Test
	public void testGetTypeDescriptorForOutputPort() throws Exception {
		assertNotNull("The type for the port 'paremeters' could not be found",
				activity.getTypeDescriptorForOutputPort("parameters"));
		assertTrue(
				"The type for the port 'paremeters' shoule be complex",
				activity.getTypeDescriptorForOutputPort("parameters") instanceof ComplexTypeDescriptor);
		assertNull("There should be no type descriptor for 'fred' port",
				activity.getTypeDescriptorForOutputPort("fred"));
	}

    @Ignore("Integration test")
	@Test
	public void testGetTypeDescriptorForInputPort() throws Exception {
		assertNotNull("The type for the port 'parameters' could not be found",
				activity.getTypeDescriptorForInputPort("parameters"));
		assertTrue(
				"The type for the port 'parameters' shoule be complex",
				activity.getTypeDescriptorForInputPort("parameters") instanceof ComplexTypeDescriptor);
		assertNull("There should be no type descriptor for 'fred' port",
				activity.getTypeDescriptorForInputPort("fred"));
	}

}
