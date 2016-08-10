/*******************************************************************************
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *     contributor license agreements.  See the NOTICE file distributed with
 *     this work for additional information regarding copyright ownership.
 *     The ASF licenses this file to You under the Apache License, Version 2.0
 *     (the "License"); you may not use this file except in compliance with
 *     the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *******************************************************************************/
package org.apache.taverna.cwl;

import java.net.URI;
import java.util.Map;
import org.apache.taverna.reference.T2Reference;
import org.apache.taverna.workflowmodel.processor.activity.AbstractAsynchronousActivity;
import org.apache.taverna.workflowmodel.processor.activity.ActivityConfigurationException;
import org.apache.taverna.workflowmodel.processor.activity.AsynchronousActivityCallback;
import com.fasterxml.jackson.databind.JsonNode;

public class CwlDummyActivity extends AbstractAsynchronousActivity<JsonNode>{
	private JsonNode conf;	
	public static final URI ACTIVITY_TYPE = URI.create("https://taverna.apache.org/ns/2016/activity/cwl");
	@Override
	public void configure(JsonNode conf) throws ActivityConfigurationException {
		this.conf=conf;
	}
	@Override
	public JsonNode getConfiguration() {
		return conf;
	}
	@Override
	public void executeAsynch(Map<String, T2Reference> data, AsynchronousActivityCallback callback) {
		
	}
	
	
}
