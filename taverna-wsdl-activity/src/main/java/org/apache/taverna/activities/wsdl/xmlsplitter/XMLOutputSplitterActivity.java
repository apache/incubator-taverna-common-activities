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

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.taverna.activities.wsdl.OutputPortTypeDescriptorActivity;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.ReferenceServiceException;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.workflowmodel.InputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.AbstractAsynchronousActivity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivityCallback;
import org.apache.taverna.wsdl.parser.ArrayTypeDescriptor;
import org.apache.taverna.wsdl.parser.ComplexTypeDescriptor;
import org.apache.taverna.wsdl.parser.TypeDescriptor;
import org.apache.taverna.wsdl.parser.UnknownOperationException;
import org.apache.taverna.wsdl.xmlsplitter.XMLOutputSplitter;
import org.apache.taverna.wsdl.xmlsplitter.XMLSplitterSerialisationHelper;

import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Iterator;

/**
 * An activity that replicates the behaviour of the Taverna 1 XMLOutputSplitter.
 *
 * @author Stuart Owen
 *
 */
public class XMLOutputSplitterActivity extends
		AbstractAsynchronousActivity<JsonNode> implements
		OutputPortTypeDescriptorActivity {

	public static final String URI = "http://ns.taverna.org.uk/2010/activity/xml-splitter/out";

	JsonNode configBean;
	TypeDescriptor typeDescriptor;

	@Override
	public void configure(JsonNode configBean)
			throws ActivityConfigurationException {
		this.configBean = configBean;

		try {
			String wrappedType = configBean.get("wrappedType").textValue();
			Element element = new SAXBuilder().build(new StringReader(wrappedType)).getRootElement();
			typeDescriptor = XMLSplitterSerialisationHelper.extensionXMLToTypeDescriptor(element);
		} catch (JDOMException | IOException e) {
			throw new ActivityConfigurationException(e);
		}
	}

	@Override
	public void executeAsynch(final Map<String, T2Reference> data,
			final AsynchronousActivityCallback callback) {
		callback.requestRun(new Runnable() {

			public void run() {
				try {
					ReferenceService referenceService = callback.getContext()
							.getReferenceService();
					XMLOutputSplitter splitter = createSplitter();
					Map<String, String> inputMap = buildInputMap(data,
							referenceService);
					Map<String, Object> outputMap = splitter.execute(inputMap);
					callback.receiveResult(createOutputData(outputMap,
							referenceService), new int[0]);
				} catch (Exception e) {
					callback.fail("Error in XMLInputSplitterActivity", e);
				}
			}

			private Map<String, T2Reference> createOutputData(
					Map<String, Object> outputMap,
					ReferenceService referenceService)
					throws ReferenceServiceException {
				Map<String, T2Reference> result = new HashMap<String, T2Reference>();
				for (String outputName : outputMap.keySet()) {
					Object output = outputMap.get(outputName);
					// TODO check if the output can be anything other than
					// String or List
					if (output instanceof List) {
						result.put(outputName, referenceService.register(
								output, 1, true, callback.getContext()));
					} else {
						result.put(outputName, referenceService.register(
								output, 0, true, callback.getContext()));
					}
				}
				return result;
			}

			private XMLOutputSplitter createSplitter() {
				List<String> inputNames = new ArrayList<String>();
				List<String> outputTypes = new ArrayList<String>();
				List<String> outputNames = new ArrayList<String>();

				// FIXME: need to use the definition beans for now to get the
				// mimetype. Need to use the actual InputPort once the mimetype
				// becomes available again.
				if (configBean.has("outputPorts")) {
//					for (JsonNode outputPort : configBean.get("outputPorts")) {
                                        for (Iterator<JsonNode> iter = configBean.get("outputPorts").iterator();iter.hasNext();) {
                                                JsonNode outputPort = iter.next();
						outputNames.add(outputPort.get("name").textValue());
						outputTypes.add(outputPort.get("mimeType").textValue());
					}
				}

				for (InputPort outputPorts : getInputPorts()) {
					inputNames.add(outputPorts.getName());
				}

				return new XMLOutputSplitter(typeDescriptor, outputNames
						.toArray(new String[] {}), outputTypes
						.toArray(new String[] {}), inputNames
						.toArray(new String[] {}));
			}

			private Map<String, String> buildInputMap(
					Map<String, T2Reference> data,
					ReferenceService referenceService)
					throws ReferenceServiceException {
				Map<String, String> result = new HashMap<String, String>();
				for (String inputName : data.keySet()) {
					T2Reference id = data.get(inputName);
					result.put(inputName, (String) referenceService
							.renderIdentifier(id, String.class, callback
									.getContext()));

				}
				return result;
			}
		});
	}

	@Override
	public JsonNode getConfiguration() {
		return configBean;
	}

	public TypeDescriptor getTypeDescriptorForOutputPort(String portName) {
		TypeDescriptor result = null;
		if (typeDescriptor instanceof ComplexTypeDescriptor) {
			for (TypeDescriptor desc : ((ComplexTypeDescriptor) typeDescriptor)
					.getElements()) {
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
        @Override
	public Map<String, TypeDescriptor> getTypeDescriptorsForOutputPorts()
			throws UnknownOperationException, IOException {
		Map<String, TypeDescriptor> descriptors = new HashMap<String, TypeDescriptor>();
		if (typeDescriptor instanceof ComplexTypeDescriptor) {
			for (TypeDescriptor desc : ((ComplexTypeDescriptor) typeDescriptor)
					.getElements()) {
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
