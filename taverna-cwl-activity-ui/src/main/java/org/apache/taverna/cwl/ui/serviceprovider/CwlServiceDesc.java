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
package org.apache.taverna.cwl.ui.serviceprovider;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

import javax.swing.Icon;

import org.apache.taverna.scufl2.api.configurations.Configuration;
import org.apache.taverna.servicedescriptions.ServiceDescription;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class CwlServiceDesc extends ServiceDescription {

	private static final String DESCRIPTION = "description";
	public static final String  CONFIG ="config";
	public static final URI ACTIVITY_TYPE = URI.create("https://taverna.apache.org/ns/2016/activity/cwl");

	@Override
	public Configuration getActivityConfiguration() {
		Configuration c = new Configuration();
		c.setType(ACTIVITY_TYPE.resolve("#Config"));	// FIXME ask what to do
		ObjectNode json = c.getJsonAsObjectNode();
		json.put(CONFIG, cwlConfiguration);
		return c;
	}

	@Override
	public String getDescription() {

		// see whether description is too long
		if (cwlConfiguration.has(DESCRIPTION)) {
			String description = cwlConfiguration.path(DESCRIPTION).asText();
			if ((description.length() < 40))
				return description;
			else
				return "";
		} else
			return "";
	}

	private JsonNode cwlConfiguration;

	public void setCwlConfiguration(JsonNode cwlConfiguration) {
		this.cwlConfiguration = cwlConfiguration;
	}

	

	@Override
	public Icon getIcon() {
		return CwlServiceIcon.getIcon();
	}

	@Override
	public String getName() {
		return toolName;
	}

	@Override
	protected List<? extends Object> getIdentifyingData() {	// FIXME ask what to do
		return Arrays.<Object>asList(toolName);
	}

	

	@Override
	public URI getActivityType() {
		return ACTIVITY_TYPE;
	}

	@Override
	public List<? extends Comparable<?>> getPath() {	// FIXME ask what to do
		return null;
	}
	private String toolName;
	
	public String getToolName() {
		return toolName;
	}

	public void setToolName(String toolName) {
		this.toolName = toolName;
	}
}
