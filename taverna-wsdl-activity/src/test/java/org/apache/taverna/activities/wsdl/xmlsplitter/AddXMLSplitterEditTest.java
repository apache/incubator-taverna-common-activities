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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import net.sf.taverna.t2.activities.testutils.LocationConstants;
import org.apache.taverna.activities.wsdl.WSDLActivity;
import net.sf.taverna.t2.workflowmodel.Edits;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.impl.DataflowImpl;
import net.sf.taverna.t2.workflowmodel.impl.EditsImpl;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class AddXMLSplitterEditTest implements LocationConstants {

	private static final JsonNodeFactory JSON_NODE_FACTORY = JsonNodeFactory.instance;

	private WSDLActivity activity;
	private DataflowImpl dataflow;
	private ObjectNode configBean;
	private static ObjectNode operationConfigBean;
	private String wsdlLocation = WSDL_TEST_BASE
	+ "eutils/eutils_lite.wsdl";
	private Edits edits;

	@Ignore("Integration test")
	@Before
	public void setUp() throws Exception {
		activity = new WSDLActivity(null);
		configBean = JSON_NODE_FACTORY.objectNode();
		operationConfigBean = configBean.objectNode();
		configBean.put("operation", operationConfigBean);
		operationConfigBean.put("name", "run_eInfo");
		operationConfigBean.put("wsdl", wsdlLocation);
		activity.configure(configBean);
		edits = new EditsImpl();

		dataflow = (DataflowImpl)edits.createDataflow();
		Processor p=edits.createProcessor("run_eInfo");
		edits.getDefaultDispatchStackEdit(p).doEdit();
		edits.getAddActivityEdit(p, activity).doEdit();
		edits.getMapProcessorPortsForActivityEdit(p).doEdit();
		edits.getAddProcessorEdit(dataflow, p).doEdit();

	}

	@Ignore("Integration test")
	@Test
	public void testAddOutputSplitterToWSDLActivity() throws Exception {
		AddXMLSplitterEdit edit = new AddXMLSplitterEdit(dataflow,activity,"parameters",false, new EditsImpl());
		edit.doEdit();
		assertEquals("The workflow should now contain 2 services",2,dataflow.getProcessors().size());
		Processor processor=null;
		for (Processor p : dataflow.getProcessors()) {
			if (p.getLocalName().equals("parametersXML")) {
				processor = p;
				break;
			}
		}
		assertNotNull("There should be a processor named parametersXML",processor);
		assertEquals("There should be 1 activity",1,processor.getActivityList().size());

		assertEquals("The processor should have 3 output ports",3,processor.getOutputPorts().size());
		assertEquals("The processor should have 1 input port",1,processor.getInputPorts().size());

		Activity<?>a = processor.getActivityList().get(0);
		assertEquals("The activity should have 3 output ports",3,a.getOutputPorts().size());
		assertEquals("The activity should have 1 input port",1,a.getInputPorts().size());

		assertEquals("There should be 1 datalink",1,dataflow.getLinks().size());
	}

	@Ignore("Integration test")
	@Test
	public void testUndo() throws Exception {
		AddXMLSplitterEdit edit = new AddXMLSplitterEdit(dataflow,activity,"parameters",false, new EditsImpl());
		edit.doEdit();
		edit.undo();
		assertEquals("There should be only 1 processor",1,dataflow.getProcessors().size());
		assertEquals("The processor should be called run_eInfo","run_eInfo",dataflow.getProcessors().get(0).getLocalName());
		assertEquals("There should be no datalinks",0,dataflow.getLinks().size());
	}

	@Ignore("Integration test")
	@Test
	public void testAddInputSplitterToWSDLActivity() throws Exception {
		AddXMLSplitterEdit edit = new AddXMLSplitterEdit(dataflow,activity,"parameters",true, new EditsImpl());
		edit.doEdit();
		assertEquals("The workflow should now contain 2 services",2,dataflow.getProcessors().size());
		Processor processor=null;
		for (Processor p : dataflow.getProcessors()) {
			if (p.getLocalName().equals("parametersXML")) {
				processor = p;
				break;
			}
		}
		assertNotNull("There should be a processor named parametersXML",processor);
		assertEquals("There should be 1 activity",1,processor.getActivityList().size());
		assertEquals("THe processor should have 3 input ports",3,processor.getInputPorts().size());
		assertEquals("THe processor should have 1 output port",1,processor.getOutputPorts().size());

		Activity<?>a = processor.getActivityList().get(0);

		assertEquals("The activity should have 3 input ports",3,a.getInputPorts().size());
		assertEquals("The activity 1 output port",1,a.getOutputPorts().size());

		assertEquals("There should be 1 datalink",1,dataflow.getLinks().size());
	}

}
