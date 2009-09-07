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
package net.sf.taverna.t2.activities.beanshell;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import net.sf.taverna.t2.activities.testutils.ActivityInvoker;
import net.sf.taverna.t2.reference.ExternalReferenceSPI;
import net.sf.taverna.t2.workflowmodel.AbstractPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.config.ActivityInputPortDefinitionBean;
import net.sf.taverna.t2.workflowmodel.processor.activity.config.ActivityOutputPortDefinitionBean;

import org.junit.Test;

/**
 * Beanshell Activity Tests
 * @author Stuart Owen
 *
 */
public class BeanshellActivityTest {
	
	/**
	 * Tests a simple script (String output = input + "_returned") to ensure the script is invoked correctly.
	 * @throws Exception
	 */
	@Test
	public void simpleScript() throws Exception {
		return;
//		BeanshellActivity activity = new BeanshellActivity();
//		BeanshellActivityConfigurationBean bean = new BeanshellActivityConfigurationBean();
//		
//		ActivityInputPortDefinitionBean inputPortBean = new ActivityInputPortDefinitionBean();
//		inputPortBean.setDepth(0);
//		inputPortBean.setName("input");
//		inputPortBean.setMimeTypes(new ArrayList<String>());
//		inputPortBean.setHandledReferenceSchemes(new ArrayList<Class<? extends ExternalReferenceSPI>>());
//		inputPortBean.setTranslatedElementType(String.class);
//		inputPortBean.setAllowsLiteralValues(true);
//		bean.setInputPortDefinitions(Collections.singletonList(inputPortBean));
//		
//		ActivityOutputPortDefinitionBean outputPortBean = new ActivityOutputPortDefinitionBean();
//		outputPortBean.setDepth(0);
//		outputPortBean.setName("output");
//		outputPortBean.setMimeTypes(new ArrayList<String>());
//		bean.setOutputPortDefinitions(Collections.singletonList(outputPortBean));
//		bean.setScript("String output = input + \"_returned\";");
//		
//		activity.configure(bean);
//		assertEquals("There should be 1 input port",1,activity.getInputPorts().size());
//		assertEquals("There should be 1 output port",1,activity.getOutputPorts().size());
//		
//		assertEquals("The input should be called input", "input",((AbstractPort)activity.getInputPorts().toArray()[0]).getName());
//		assertEquals("The output should be called output", "output",((AbstractPort)activity.getOutputPorts().toArray()[0]).getName());
//		
//		Map<String,Object> inputs = new HashMap<String, Object>();
//		inputs.put("input", "aString");
//		Map<String, Class<?>> expectedOutputs = new HashMap<String, Class<?>>();
//		expectedOutputs.put("output", String.class);
//		
//		Map<String,Object> outputs = ActivityInvoker.invokeAsyncActivity(activity, inputs, expectedOutputs);
//		assertTrue("there should be an output named output",outputs.containsKey("output"));
//		assertEquals("output should have the value aString_returned","aString_returned",outputs.get("output"));
	}
}
