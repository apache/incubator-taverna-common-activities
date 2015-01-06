/*******************************************************************************
 * Copyright (C) 2010 The University of Manchester
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

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.wsdl.WSDLException;
import javax.xml.parsers.ParserConfigurationException;

import net.sf.taverna.t2.security.credentialmanager.CredentialManager;
import net.sf.taverna.t2.workflowmodel.Edits;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityFactory;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityInputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityOutputPort;
import net.sf.taverna.wsdl.parser.TypeDescriptor;
import net.sf.taverna.wsdl.parser.UnknownOperationException;
import net.sf.taverna.wsdl.parser.WSDLParser;

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
