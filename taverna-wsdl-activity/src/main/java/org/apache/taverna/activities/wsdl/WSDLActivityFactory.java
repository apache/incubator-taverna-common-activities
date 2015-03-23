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

package org.apache.taverna.activities.wsdl;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.wsdl.WSDLException;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.taverna.security.credentialmanager.CredentialManager;
import org.apache.taverna.workflowmodel.Edits;
import org.apache.taverna.workflowmodel.processor.activity.ActivityFactory;
import org.apache.taverna.workflowmodel.processor.activity.ActivityInputPort;
import org.apache.taverna.workflowmodel.processor.activity.ActivityOutputPort;
import org.apache.taverna.wsdl.parser.TypeDescriptor;
import org.apache.taverna.wsdl.parser.UnknownOperationException;
import org.apache.taverna.wsdl.parser.WSDLParser;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * An {@link ActivityFactory} for creating <code>WSDLActivity</code>.
 *
 * @author David Withers
 */
public class WSDLActivityFactory implements ActivityFactory {

	private static Logger logger = Logger.getLogger(WSDLActivityFactory.class);

	private CredentialManager credentialManager;
	private Edits edits;

	@Override
	public WSDLActivity createActivity() {
		return new WSDLActivity(credentialManager);
	}

	@Override
	public URI getActivityType() {
		return URI.create(WSDLActivity.URI);
	}

	@Override
	public JsonNode getActivityConfigurationSchema() {
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			return objectMapper.readTree(getClass().getResource("/schema.json"));
		} catch (IOException e) {
			return objectMapper.createObjectNode();
		}
	}

	public void setCredentialManager(CredentialManager credentialManager) {
		this.credentialManager = credentialManager;
	}

	@Override
	public Set<ActivityInputPort> getInputPorts(JsonNode configuration) {
		Map<String, ActivityInputPort> inputPorts = new HashMap<>();
		try {
			WSDLParser parser = new WSDLParser(configuration.get("operation").get("wsdl").textValue());
			List<TypeDescriptor> inputDescriptors = parser.getOperationInputParameters(configuration
					.get("operation").get("name").textValue());
			for (TypeDescriptor descriptor : inputDescriptors) {
				inputPorts.put(descriptor.getName(), edits.createActivityInputPort(
						descriptor.getName(), descriptor.getDepth(), true, null, String.class));
			}
			if (parser.isWsrfService()) {
				// Make sure the port name is unique
				String endpointReferenceInputPortName = WSDLActivity.ENDPOINT_REFERENCE;
				int counter = 0;
				while (inputPorts.containsKey(endpointReferenceInputPortName)) {
					endpointReferenceInputPortName = WSDLActivity.ENDPOINT_REFERENCE + counter++;
				}
				inputPorts.put(endpointReferenceInputPortName, edits.createActivityInputPort(
						endpointReferenceInputPortName, 0, true, null, String.class));
			}
		} catch (ParserConfigurationException | WSDLException | IOException | SAXException | UnknownOperationException e) {
			logger.warn(
					"Unable to parse the WSDL " + configuration.get("operation").get("wsdl").textValue(), e);
		}

		return new HashSet<>(inputPorts.values());
	}

	@Override
	public Set<ActivityOutputPort> getOutputPorts(JsonNode configuration) {
		Set<ActivityOutputPort> outputPorts = new HashSet<>();
		try {
			WSDLParser parser = new WSDLParser(configuration.get("operation").get("wsdl")
					.textValue());
			List<TypeDescriptor> outputDescriptors = parser
					.getOperationOutputParameters(configuration.get("operation").get("name")
							.textValue());
			for (TypeDescriptor descriptor : outputDescriptors) {
				outputPorts.add(edits.createActivityOutputPort(descriptor.getName(),
						descriptor.getDepth(), descriptor.getDepth()));
			}
			// add output for attachment list
			outputPorts.add(edits.createActivityOutputPort("attachmentList", 1, 1));
		} catch (ParserConfigurationException | WSDLException | IOException | SAXException | UnknownOperationException e) {
			logger.warn(
					"Unable to parse the WSDL " + configuration.get("operation").get("wsdl").textValue(), e);
		}

		return outputPorts;
	}

	public void setEdits(Edits edits) {
		this.edits = edits;
	}

}
