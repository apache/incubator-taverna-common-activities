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

package org.apache.taverna.activities.wsdl;

import java.net.URI;

import org.apache.taverna.workflowmodel.processor.config.ConfigurationBean;
import org.apache.taverna.workflowmodel.processor.config.ConfigurationProperty;

/**
 * Configuration for a WSDL operation.
 *
 * @author David Withers
 */
@ConfigurationBean(uri = WSDLActivity.URI + "/operation")
public class WSDLOperationConfigurationBean {

	private URI wsdl;
	private String operationName;

	public URI getWsdl() {
		return wsdl;
	}

	@ConfigurationProperty(name = "wsdl", label = "WSDL URL", description = "The location of the WSDL definition for the web service")
	public void setWsdl(URI wsdl) {
		this.wsdl = wsdl;
	}

	public String getOperationName() {
		return operationName;
	}

	@ConfigurationProperty(name = "name", label = "Operation Name", description = "The name of the WSDL operation")
	public void setOperationName(String operationName) {
		this.operationName = operationName;
	}

}
