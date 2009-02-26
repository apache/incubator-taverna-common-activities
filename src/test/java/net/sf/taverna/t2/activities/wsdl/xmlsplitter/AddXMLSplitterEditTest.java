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
import net.sf.taverna.t2.activities.testutils.LocationConstants;
import net.sf.taverna.t2.activities.wsdl.WSDLActivity;
import net.sf.taverna.t2.activities.wsdl.WSDLActivityConfigurationBean;
import net.sf.taverna.t2.workflowmodel.Edits;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.impl.DataflowImpl;
import net.sf.taverna.t2.workflowmodel.impl.EditsImpl;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class AddXMLSplitterEditTest implements LocationConstants {
	
	private WSDLActivity activity;
	private DataflowImpl dataflow;
	private WSDLActivityConfigurationBean configBean;
	private String wsdlLocation = WSDL_TEST_BASE
	+ "eutils/eutils_lite.wsdl";
	private Edits edits;

	@Ignore("Integration test")
	@Before
	public void setUp() throws Exception {
		activity = new WSDLActivity();
		configBean = new WSDLActivityConfigurationBean();
		configBean.setOperation("run_eInfo");
		configBean.setWsdl(wsdlLocation);
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
		AddXMLSplitterEdit edit = new AddXMLSplitterEdit(dataflow,activity,"parameters",false);
		edit.doEdit();
		assertEquals("The dataflow should now contain 2 processors",2,dataflow.getProcessors().size());
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
		AddXMLSplitterEdit edit = new AddXMLSplitterEdit(dataflow,activity,"parameters",false);
		edit.doEdit();
		edit.undo();
		assertEquals("There should be only 1 processor",1,dataflow.getProcessors().size());
		assertEquals("The processor should be called run_eInfo","run_eInfo",dataflow.getProcessors().get(0).getLocalName());
		assertEquals("There should be no datalinks",0,dataflow.getLinks().size());
	}
	
	@Ignore("Integration test")
	@Test
	public void testAddInputSplitterToWSDLActivity() throws Exception {
		AddXMLSplitterEdit edit = new AddXMLSplitterEdit(dataflow,activity,"parameters",true);
		edit.doEdit();
		assertEquals("The dataflow should now contain 2 processors",2,dataflow.getProcessors().size());
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
