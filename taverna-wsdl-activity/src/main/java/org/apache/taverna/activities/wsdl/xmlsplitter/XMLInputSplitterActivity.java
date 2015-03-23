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

import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.taverna.activities.wsdl.InputPortTypeDescriptorActivity;
import org.apache.taverna.reference.ReferenceService;
import org.apache.taverna.reference.ReferenceServiceException;
import org.apache.taverna.reference.T2Reference;
import org.apache.taverna.workflowmodel.OutputPort;
import org.apache.taverna.workflowmodel.processor.activity.AbstractAsynchronousActivity;
import org.apache.taverna.workflowmodel.processor.activity.ActivityConfigurationException;
import org.apache.taverna.workflowmodel.processor.activity.AsynchronousActivityCallback;
import org.apache.taverna.wsdl.parser.ArrayTypeDescriptor;
import org.apache.taverna.wsdl.parser.ComplexTypeDescriptor;
import org.apache.taverna.wsdl.parser.TypeDescriptor;
import org.apache.taverna.wsdl.parser.UnknownOperationException;
import org.apache.taverna.wsdl.xmlsplitter.XMLInputSplitter;
import org.apache.taverna.wsdl.xmlsplitter.XMLSplitterSerialisationHelper;

import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

/**
 * An activity that replicates the behaviour of the Taverna 1 XMLInputSplitters.
 * 
 * @author Stuart Owen
 *
 */
public class XMLInputSplitterActivity extends AbstractAsynchronousActivity<JsonNode> implements InputPortTypeDescriptorActivity {

	public static final String URI = "http://ns.taverna.org.uk/2010/activity/xml-splitter/in";

	JsonNode configBean;
	TypeDescriptor typeDescriptor;
	
        @Override
        public void configure(JsonNode configBean) throws ActivityConfigurationException {
		this.configBean = configBean;

		String wrappedType = configBean.get("wrappedType").textValue();
		Element element;
		try {
			element = new SAXBuilder().build(new StringReader(wrappedType)).getRootElement();
		} catch (JDOMException e) {
			throw new ActivityConfigurationException("Error reading xml for XMLInputSplitter",e);
		} catch (IOException e) {
			throw new ActivityConfigurationException("Error reading xml for XMLInputSplitter",e);
		}
		typeDescriptor = XMLSplitterSerialisationHelper.extensionXMLToTypeDescriptor(element);
		
	}

        @Override
        public JsonNode getConfiguration() {
		return configBean;
	}

	@Override
	public void executeAsynch(final Map<String, T2Reference> data,
			final AsynchronousActivityCallback callback) {
		callback.requestRun(new Runnable() {

                        @Override
			public void run() {
				try {
					ReferenceService referenceService = callback.getContext().getReferenceService();
					XMLInputSplitter splitter = createSplitter();
					Map<String,Object> inputMap = buildInputMap(data,referenceService);
					Map<String,String> outputMap = splitter.execute(inputMap);
					callback.receiveResult(createOutputData(outputMap,referenceService), new int[0]);
				}
				catch(Exception e) {
					callback.fail("Error in XMLInputSplitterActivity",e);
				}
			}

			private Map<String, T2Reference> createOutputData(
					Map<String, String> outputMap,ReferenceService referenceService) throws ReferenceServiceException {
				Map<String,T2Reference> result = new HashMap<String, T2Reference>();
				for (String outputName : outputMap.keySet()) {
					String xmlOut = outputMap.get(outputName);
					result.put(outputName, referenceService.register(xmlOut, 0, true, callback.getContext()));
				}
				return result;
			}

			private XMLInputSplitter createSplitter() {
				List<String> inputNames = new ArrayList<String>();
				List<String> inputTypes = new ArrayList<String>();
				List<String> outputNames = new ArrayList<String>();
				
				//FIXME: need to use the definition beans for now to get the mimetype. Need to use the actual InputPort once the mimetype becomes available again.
				if (configBean.has("inputPorts")) {
//                                      for (JsonNode inputPort : configBean.get("inputPorts")) {
                                        for (Iterator<JsonNode> iter = configBean.get("inputPorts").iterator();iter.hasNext();) {
                                                JsonNode inputPort = iter.next();
						inputNames.add(inputPort.get("name").textValue());
						inputTypes.add(inputPort.get("mimeType").textValue());
                                        }
				}
				
				for (OutputPort outputPorts : getOutputPorts()) {
					outputNames.add(outputPorts.getName());
				}
				
				return new XMLInputSplitter(typeDescriptor,inputNames.toArray(new String[]{}),inputTypes.toArray(new String[]{}),outputNames.toArray(new String[]{}));
			}
			
			private Map<String,Object> buildInputMap(Map<String, T2Reference> data,ReferenceService referenceService) throws ReferenceServiceException {
				Map<String,Object> result = new HashMap<String, Object>();
				for (String inputName : data.keySet()) {
					T2Reference id = data.get(inputName);
					result.put(inputName, referenceService.renderIdentifier(id,String.class, callback.getContext()));
					
				}
				return result;
			}
		});
		
	}

	/**
	 * Returns a TypeDescriptor for the given port name. If the port cannot be found, or is not based upon a complex type, then null is returned.
	 * @param portName
	 * @return
	 */
        @Override
	public TypeDescriptor getTypeDescriptorForInputPort(String portName) {
		TypeDescriptor result = null;
		if (typeDescriptor instanceof ComplexTypeDescriptor) {
			for (TypeDescriptor desc : ((ComplexTypeDescriptor)typeDescriptor).getElements()) {
				if (desc.getName().equals(portName)) {
					result = desc;
					break;
				}
			}
		}
		else if (typeDescriptor instanceof ArrayTypeDescriptor) {
			TypeDescriptor desc = ((ArrayTypeDescriptor)typeDescriptor).getElementType();	
			
			if (typeDescriptor.getName().equals(portName)) {
				result = desc;
			}
		}
		return result;
	}

	/**
	 * {@inheritDoc}
	 * 
	 */
	public Map<String, TypeDescriptor> getTypeDescriptorsForInputPorts()
			throws UnknownOperationException, IOException {
		Map<String, TypeDescriptor> descriptors = new HashMap<String, TypeDescriptor>();
		if (typeDescriptor instanceof ComplexTypeDescriptor) {
			for (TypeDescriptor desc : ((ComplexTypeDescriptor)typeDescriptor).getElements()) {
				descriptors.put(desc.getName(), desc);
			}
		}
		else if (typeDescriptor instanceof ArrayTypeDescriptor) {
			TypeDescriptor desc = ((ArrayTypeDescriptor)typeDescriptor).getElementType();
			descriptors.put(typeDescriptor.getName(), desc);
		}
		return descriptors;
	}
}
