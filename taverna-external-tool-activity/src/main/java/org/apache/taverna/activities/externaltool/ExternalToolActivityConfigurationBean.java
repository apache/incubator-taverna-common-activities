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

import java.util.List;

import org.apache.taverna.activities.externaltool.desc.ToolDescription;
import org.apache.taverna.activities.externaltool.manager.InvocationGroup;
import org.apache.taverna.activities.externaltool.manager.InvocationMechanism;
import org.apache.taverna.activities.externaltool.manager.MechanismCreator;
import org.apache.taverna.workflowmodel.processor.config.ConfigurationBean;
import org.apache.taverna.workflowmodel.processor.config.ConfigurationProperty;

@ConfigurationBean(uri = ExternalToolActivity.URI + "#Config")
public final class ExternalToolActivityConfigurationBean {

	private InvocationGroup group;

	private String mechanismType;

	private String mechanismName;

	private String mechanismXML;

	private transient InvocationMechanism mechanism;

	protected String repositoryUrl;
	protected String externaltoolid;
	protected ToolDescription useCaseDescription = null;
	private boolean edited = false;

	private List<MechanismCreator> mechanismCreators;

    public boolean isEdited() {
		return edited;
	}

	public ExternalToolActivityConfigurationBean() {
	}

	public InvocationGroup getInvocationGroup() {
	    return group;
	}

	@ConfigurationProperty(name = "invocationGroup", label = "InvocationGroup", required=false)
	public void setInvocationGroup(
			InvocationGroup group) {
		this.group = group;
		clearMechanismInformation();
	}

	private void clearMechanismInformation() {
		this.mechanismType = null;
		this.mechanismName = null;
		this.mechanismXML = null;
		this.mechanism = null;
	}

	/**
	 * @return the repositoryUrl
	 */
	public String getRepositoryUrl() {
		return repositoryUrl;
	}

	/**
	 * @param repositoryUrl the repositoryUrl to set
	 */
	@ConfigurationProperty(name = "repositoryUrl", label = "Repository URL", required=false)
	public void setRepositoryUrl(String repositoryUrl) {
		this.repositoryUrl = repositoryUrl;
	}

	/**
	 * @return the externaltoolid
	 */
	public String getExternaltoolid() {
		return externaltoolid;
	}

	/**
	 * @param externaltoolid the externaltoolid to set
	 */
	@ConfigurationProperty(name = "toolId", label = "Tool ID")
	public void setExternaltoolid(String externaltoolid) {
		this.externaltoolid = externaltoolid;
	}

	/**
	 * @return the useCaseDescription
	 */
	public ToolDescription getUseCaseDescription() {
		return useCaseDescription;
	}

	/**
	 * @param useCaseDescription the useCaseDescription to set
	 */
	@ConfigurationProperty(name = "toolDescription", label = "Tool Description")
	public void setUseCaseDescription(ToolDescription useCaseDescription) {
		this.useCaseDescription = useCaseDescription;
	}

	@ConfigurationProperty(name = "edited", label = "Edited", required=false)
	public void setEdited(boolean b) {
		this.edited  = b;
	}

	/**
	 * Note this also sets the details
	 *
	 * @param mechanism the mechanism to set
	 */
	public void setMechanism(InvocationMechanism mechanism) {
		this.mechanism = mechanism;
		convertMechanismToDetails();
		this.group = null;
	}

	public void convertMechanismToDetails() {
		if (mechanism != null) {
			this.setMechanismXML(mechanism.getXML());
			this.setMechanismName(mechanism.getName());
			this.setMechanismType(mechanism.getType());
		}
	}

	/**
	 * @param mechanismType the mechanismType to set
	 */
	@ConfigurationProperty(name = "mechanismType", label = "Mechanism Type", required=false)
	public void setMechanismType(String mechanismType) {
		this.mechanismType = mechanismType;
	}

	/**
	 * @param mechanismName the mechanismName to set
	 */
	@ConfigurationProperty(name = "mechanismName", label = "Mechanism Name", required=false)
	public void setMechanismName(String mechanismName) {
		this.mechanismName = mechanismName;
	}

	/**
	 * @param mechanismXML the mechanismXML to set
	 */
	@ConfigurationProperty(name = "mechanismXML", label = "Mechanism XML", required=false)
	public void setMechanismXML(String mechanismXML) {
		this.mechanismXML = mechanismXML;
	}

	public void convertDetailsToMechanism() {
		if (mechanismXML != null) {
			for (MechanismCreator mc : mechanismCreators) {
				if (mc.canHandle(getMechanismType())) {
					mechanism = mc.convert(getMechanismXML(), getMechanismName());
					break;
				}
			}
		}
	}

	/**
	 * @return the mechanism
	 */
	public InvocationMechanism getMechanism() {

		return mechanism;
	}

	/**
	 * @return the mechanismType
	 */
	public String getMechanismType() {
		return mechanismType;
	}

	/**
	 * @return the mechanismName
	 */
	public String getMechanismName() {
		return mechanismName;
	}

	/**
	 * @return the mechanismXML
	 */
	public String getMechanismXML() {
		return mechanismXML;
	}

	public void setMechanismCreators(List<MechanismCreator> mechanismCreators) {
		this.mechanismCreators = mechanismCreators;
	}

}
