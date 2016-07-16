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
package org.apache.taverna.cwl;

import java.io.IOException;
import java.net.URI;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.taverna.cwl.utilities.CWLUtil;
import org.apache.taverna.workflowmodel.Edits;
import org.apache.taverna.workflowmodel.processor.activity.Activity;
import org.apache.taverna.workflowmodel.processor.activity.ActivityConfigurationException;
import org.apache.taverna.workflowmodel.processor.activity.ActivityFactory;
import org.apache.taverna.workflowmodel.processor.activity.ActivityInputPort;
import org.apache.taverna.workflowmodel.processor.activity.ActivityOutputPort;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CwlActivityFactory implements ActivityFactory {
	private static Logger logger = Logger.getLogger(CwlActivityFactory.class);
	private static final int DEPTH_0 = 0;
	private static final int DEPTH_1 = 1;
	private Edits edits;
	public static final String  CWL_CONF ="cwl_conf";
	@Override
	public Activity<?> createActivity() {
		CwlDumyActivity activity = new CwlDumyActivity();
		activity.setEdits(edits);
		return activity;
	}

	@Override
	public URI getActivityType() {
		return CwlDumyActivity.ACTIVITY_TYPE;
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
	public Set<ActivityInputPort> getInputPorts(JsonNode configuration) throws ActivityConfigurationException {
		CWLUtil cwlUtil = new CWLUtil(configuration.get(CWL_CONF));

		Set<ActivityInputPort> inputs = new HashSet<>();
			
	
			//get the processed data
		Map<String, Integer>  processedInputs= cwlUtil.processInputDepths();
			for (String inputId : processedInputs.keySet()) {
				int depth = processedInputs.get(inputId);
				if (depth == DEPTH_0)
					inputs.add(edits.createActivityInputPort(inputId, DEPTH_0, true, null, null));
				else if (depth == DEPTH_1)
					inputs.add(edits.createActivityInputPort(inputId, DEPTH_1, true, null, null));
			}
			
		return inputs;
	}

	@Override
	public Set<ActivityOutputPort> getOutputPorts(JsonNode configuration) throws ActivityConfigurationException {
		CWLUtil cwlUtil = new CWLUtil(configuration.get(CWL_CONF));

		Set<ActivityOutputPort> outputs = new HashSet<>();
		
		
		//get the processed data
		Map<String, Integer>  processedOutputs = cwlUtil.processOutputDepths();
		for (String inputId : processedOutputs.keySet()) {
			int depth = processedOutputs.get(inputId);
			if (depth == DEPTH_0)
				outputs.add(edits.createActivityOutputPort(inputId, DEPTH_0,0 ));
			else if (depth == DEPTH_1)
				outputs.add(edits.createActivityOutputPort(inputId, DEPTH_1, 0));
		}
		return outputs;
	}
	public void setEdits(Edits edits) {
		this.edits = edits;
	}

}
