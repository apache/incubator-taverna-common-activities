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

import java.util.List;

import javax.swing.Icon;

import org.apache.taverna.cwl.CwlDumyActivity;

import com.fasterxml.jackson.databind.JsonNode;

import net.sf.taverna.t2.servicedescriptions.ServiceDescription;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

public class CwlServiceDesc extends ServiceDescription<JsonNode > {

	private static final String DESCRIPTION = "description";

	@Override
	public String getDescription() {
	
		//see  whether description is too long
		if (cwlConfiguration.has(DESCRIPTION)){
			String description = cwlConfiguration.path(DESCRIPTION).asText();
				if((description.length()<40))return description;
				else return "";
		}
		else
			return "";
	}

	private JsonNode cwlConfiguration;

	public void setCwlConfiguration(JsonNode cwlConfiguration) {
		//set yaml parse CWL tool content
		this.cwlConfiguration = cwlConfiguration;
	}

	private String toolName;

	@Override
	public Class<? extends Activity<JsonNode>> getActivityClass() {
		//should fix this
		return null;
	}
	@Override
	public JsonNode getActivityConfiguration() {
		return cwlConfiguration;
	}
	@Override
	public Icon getIcon() {
		return  CwlServiceIcon.getIcon();
	}
	@Override
	public String getName() {
		return toolName;
	}
	@Override
	public List<? extends Comparable> getPath() {
		return null;
	}
	@Override
	protected List<? extends Object> getIdentifyingData() {
		return null;
	}

	public void setToolName(String toolName) {
		this.toolName = toolName;
	}

}
