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

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;

import org.apache.taverna.cwl.CwlActivityConfigurationBean;
import org.apache.taverna.cwl.CwlDumyActivity;

import net.sf.taverna.t2.servicedescriptions.ServiceDescription;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

public class CwlServiceDesc extends ServiceDescription<CwlActivityConfigurationBean> {

	private Map cwlConfiguration;
	private String toolName;
	@Override
	public Class<? extends Activity<CwlActivityConfigurationBean>> getActivityClass() {
		return (Class<? extends Activity<CwlActivityConfigurationBean>>) CwlDumyActivity.class;
	}

	@Override
	public CwlActivityConfigurationBean getActivityConfiguration() {
		//Creating the CWL configuration bean
		CwlActivityConfigurationBean configurationBean = new CwlActivityConfigurationBean();
		configurationBean.setCwlConfigurations(cwlConfiguration);
		return configurationBean;
	}

	public Map getCwlConfiguration() {
		return cwlConfiguration;
	}

	public void setCwlConfiguration(Map cwlConfiguration) {
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
	public List<? extends Comparable> getPath() {
		return null;
	}

	@Override
	protected List<? extends Object> getIdentifyingData() {
		return Arrays.asList("CWL Services " + toolName);
	}

	public String getToolName() {
		return toolName;
	}

	public void setToolName(String toolName) {
		this.toolName = toolName;
	}

}
