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

package org.apache.taverna.activities.spreadsheet;

import java.io.IOException;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.taverna.workflowmodel.Edits;
import org.apache.taverna.workflowmodel.processor.activity.ActivityConfigurationException;
import org.apache.taverna.workflowmodel.processor.activity.ActivityFactory;
import org.apache.taverna.workflowmodel.processor.activity.ActivityInputPort;
import org.apache.taverna.workflowmodel.processor.activity.ActivityOutputPort;

/**
 * An {@link ActivityFactory} for creating <code>SpreadsheetImportActivity</code>.
 *
 * @author David Withers
 */
public class SpreadsheetImportActivityFactory implements ActivityFactory {

	private Edits edits;

	@Override
	public SpreadsheetImportActivity createActivity() {
		return new SpreadsheetImportActivity();
	}

	@Override
	public URI getActivityType() {
		return URI.create(SpreadsheetImportActivity.URI);
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
		Set<ActivityInputPort> inputPorts = new HashSet<>();
		inputPorts.add(edits.createActivityInputPort(SpreadsheetImportActivity.INPUT_PORT_NAME, 0, false, null, null));
		return inputPorts;
	}

	@Override
	public Set<ActivityOutputPort> getOutputPorts(JsonNode configuration)
			throws ActivityConfigurationException {
		Set<ActivityOutputPort> outputPorts = new HashSet<>();
		if ("PORT_PER_COLUMN".equals(configuration.get("outputFormat").textValue())) {
			Range columnRange = SpreadsheetUtils.getRange(configuration.get("columnRange"));
			for (int column = columnRange.getStart(); column <= columnRange.getEnd(); column++) {
				if (columnRange.contains(column)) {
					outputPorts.add(edits.createActivityOutputPort(SpreadsheetUtils.getPortName(column, configuration), 1, 1));
				}
			}
		} else {
			outputPorts.add(edits.createActivityOutputPort(SpreadsheetImportActivity.OUTPUT_PORT_NAME, 0, 0));
		}
		return outputPorts;
	}

	public void setEdits(Edits edits) {
		this.edits = edits;
	}

}
