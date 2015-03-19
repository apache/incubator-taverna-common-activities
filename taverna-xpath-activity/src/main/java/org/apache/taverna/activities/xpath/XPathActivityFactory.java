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

package org.apache.taverna.activities.xpath;

import static org.apache.taverna.activities.xpath.XPathActivity.IN_XML;
import static org.apache.taverna.activities.xpath.XPathActivity.OUT_TEXT;
import static org.apache.taverna.activities.xpath.XPathActivity.OUT_XML;

import java.io.IOException;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import net.sf.taverna.t2.workflowmodel.Edits;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityFactory;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityInputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityOutputPort;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * An {@link ActivityFactory} for creating <code>XPathActivity</code>.
 *
 * @author David Withers
 */
public class XPathActivityFactory implements ActivityFactory {

	private Edits edits;

	@Override
	public XPathActivity createActivity() {
		return new XPathActivity();
	}

	@Override
	public URI getActivityType() {
		return URI.create(XPathActivity.URI);
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

	@Override
	public Set<ActivityInputPort> getInputPorts(JsonNode configuration)
			throws ActivityConfigurationException {
		Set<ActivityInputPort> outputPorts = new HashSet<>();
		outputPorts.add(edits.createActivityInputPort(IN_XML, 0, true, null,
				String.class));
		return outputPorts;
	}

	@Override
	public Set<ActivityOutputPort> getOutputPorts(JsonNode configuration)
			throws ActivityConfigurationException {
		Set<ActivityOutputPort> outputPorts = new HashSet<>();
		outputPorts.add(edits.createActivityOutputPort(OUT_TEXT, 1, 1));
		outputPorts.add(edits.createActivityOutputPort(OUT_XML, 1, 1));
		return outputPorts;
	}

	public void setEdits(Edits edits) {
		this.edits = edits;
	}

}
