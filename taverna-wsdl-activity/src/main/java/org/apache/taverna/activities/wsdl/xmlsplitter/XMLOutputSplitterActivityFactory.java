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
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Iterator;

import net.sf.taverna.t2.workflowmodel.Edits;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityFactory;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityInputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityOutputPort;

/**
 * An {@link ActivityFactory} for creating <code>XMLOutputSplitterActivity</code>.
 *
 * @author David Withers
 */
public class XMLOutputSplitterActivityFactory implements ActivityFactory {

	private Edits edits;

	@Override
	public XMLOutputSplitterActivity createActivity() {
		return new XMLOutputSplitterActivity();
	}

	@Override
	public URI getActivityType() {
		return URI.create(XMLOutputSplitterActivity.URI);
	}

	@Override
	public JsonNode getActivityConfigurationSchema() {
		ObjectMapper objectMapper = new ObjectMapper();
		try {
 			return objectMapper.readTree(getClass().getResource("/xml-splitter.schema.json"));
		} catch (IOException e) {
			return objectMapper.createObjectNode();
		}
	}

	@Override
	public Set<ActivityInputPort> getInputPorts(JsonNode configuration)
			throws ActivityConfigurationException {
		Set<ActivityInputPort> inputPorts = new HashSet<>();
		if (configuration.has("inputPorts")) {
//			for (JsonNode inputPort : configuration.get("inputPorts")) {
                        for (Iterator<JsonNode> iter = configuration.get("inputPorts").iterator();iter.hasNext();) {
                                JsonNode inputPort = iter.next();
				inputPorts.add(edits.createActivityInputPort(inputPort.get("name").textValue(),
						inputPort.get("depth").intValue(), false, null, String.class));
			}
		}
		return inputPorts;
	}

	@Override
	public Set<ActivityOutputPort> getOutputPorts(JsonNode configuration)
			throws ActivityConfigurationException {
		Set<ActivityOutputPort> outputPorts = new HashSet<>();
		if (configuration.has("outputPorts")) {
//			for (JsonNode outputPort : configuration.get("outputPorts")) {
                        for (Iterator<JsonNode> iter = configuration.get("outputPorts").iterator();iter.hasNext();) {
                                JsonNode outputPort = iter.next();
				outputPorts.add(edits.createActivityOutputPort(outputPort.get("name").textValue(),
                                outputPort.get("depth").intValue(), outputPort.get("granularDepth").intValue()));
			}
		}
		return outputPorts;
	}

	public void setEdits(Edits edits) {
		this.edits = edits;
	}

}
