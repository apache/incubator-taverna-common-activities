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

package org.apache.taverna.activities.externaltool.manager;

import java.util.List;

import org.apache.taverna.activities.externaltool.ExternalToolActivity;
import net.sf.taverna.t2.workflowmodel.processor.config.ConfigurationBean;
import net.sf.taverna.t2.workflowmodel.processor.config.ConfigurationProperty;

import org.apache.log4j.Logger;

/**
 * @author alanrw
 *
 */
@ConfigurationBean(uri = ExternalToolActivity.URI + "#InvocationGroup")
public class InvocationGroup {

	private static Logger logger = Logger.getLogger(InvocationGroup.class);

	private String invocationGroupName;

	private String mechanismType;

	private String mechanismName;

	private String mechanismXML;

	private transient InvocationMechanism mechanism;

	private List<MechanismCreator> mechanismCreators;

	public InvocationGroup(List<MechanismCreator> mechanismCreators) {
		setMechanismCreators(mechanismCreators);
	}

	/**
	 * @return the invocationGroupName
	 */
	public String getName() {
		return invocationGroupName;
	}

	/**
	 * @param invocationGroupName the invocationGroupName to set
	 */
	@ConfigurationProperty(name = "name", label = "Invocation Group Name")
	public void setName(String invocationGroupName) {
		this.invocationGroupName = invocationGroupName;
	}

	/**
	 * @return the mechanismType
	 */
	public String getMechanismType() {
		return mechanismType;
	}

	/**
	 * @param mechanismType the mechanismType to set
	 */
	@ConfigurationProperty(name = "mechanismType", label = "Mechanism Type")
	public void setMechanismType(String mechanismType) {
		this.mechanismType = mechanismType;
	}

	/**
	 * @return the mechanismName
	 */
	public String getMechanismName() {
		return mechanismName;
	}

	/**
	 * @param mechanismName the mechanismName to set
	 */
	@ConfigurationProperty(name = "mechanismName", label = "Mechanism Name")
	public void setMechanismName(String mechanismName) {
		this.mechanismName = mechanismName;
	}

	/**
	 * @return the mechanismXML
	 */
	public String getMechanismXML() {
		return mechanismXML;
	}

	/**
	 * @param mechanismXML the mechanismXML to set
	 */
	@ConfigurationProperty(name = "mechanismXML", label = "Mechanism XML")
	public void setMechanismXML(String mechanismXML) {
		this.mechanismXML = mechanismXML;
	}

	/**
	 * @return the mechanism
	 */
	public InvocationMechanism getMechanism() {
		return mechanism;
	}

	/**
	 * Note this also sets the corresponding details
	 *
	 * @param mechanism the mechanism to set
	 */
	public void setMechanism(InvocationMechanism mechanism) {
		this.mechanism = mechanism;
		convertMechanismToDetails();
	}

	public String toString() {
		return getName();
	}

	public void convertMechanismToDetails() {
		this.setMechanismXML(mechanism.getXML());
		this.setMechanismName(mechanism.getName());
		this.setMechanismType(mechanism.getType());
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

	public void setMechanismCreators(List<MechanismCreator> mechanismCreators) {
		this.mechanismCreators = mechanismCreators;
	}

}
