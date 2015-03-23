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

package org.apache.taverna.activities.externaltool;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.taverna.activities.externaltool.manager.MechanismCreator;
import org.apache.taverna.workflowmodel.Edits;
import org.apache.taverna.workflowmodel.processor.activity.ActivityConfigurationException;
import org.apache.taverna.workflowmodel.processor.activity.ActivityFactory;
import org.apache.taverna.workflowmodel.processor.activity.ActivityInputPort;
import org.apache.taverna.workflowmodel.processor.activity.ActivityOutputPort;

/**
 * An {@link ActivityFactory} for creating <code>ExternalToolActivity</code>.
 *
 * @author David Withers
 */
public class ExternalToolActivityFactory implements ActivityFactory {

	private List<InvocationCreator> invocationCreators;

	private List<MechanismCreator> mechanismCreators;

        private Edits edits;

	@Override
	public ExternalToolActivity createActivity() {
		ExternalToolActivity activity = new ExternalToolActivity();
		activity.setInvocationCreators(invocationCreators);
                activity.setEdits(edits);
		return activity;
	}

	@Override
	public URI getActivityType() {
		return URI.create(ExternalToolActivity.URI);
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

	public void setInvocationCreators(List<InvocationCreator> invocationCreators) {
		this.invocationCreators = invocationCreators;
	}

	public void setMechanismCreators(List<MechanismCreator> mechanismCreators) {
		this.mechanismCreators = mechanismCreators;
	}

	@Override
	public Set<ActivityInputPort> getInputPorts(JsonNode configuration)
			throws ActivityConfigurationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<ActivityOutputPort> getOutputPorts(JsonNode configuration)
			throws ActivityConfigurationException {
		// TODO Auto-generated method stub
		return null;
	}

        public void setEdits(Edits edits) {
		this.edits = edits;
	}

}
