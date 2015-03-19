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

import net.sf.taverna.t2.workflowmodel.processor.config.ConfigurationBean;
import net.sf.taverna.t2.workflowmodel.processor.config.ConfigurationProperty;

/**
 * A standard Java Bean that provides the details required to configure a WSDLActivity.
 * <p>
 * This contains details about the WSDL and the Operation that the WSDLActivity is intended to invoke.
 * </p>
 * @author Stuart Owen
 */
@ConfigurationBean(uri = WSDLActivity.URI + "#Config")
public class WSDLActivityConfigurationBean {
    private WSDLOperationConfigurationBean operation;
    private URI securityProfile;

    // In the case service requires username and password for authentication,
    // but do not serialise these variables to file
    //transient private String username;
    //transient private String password;

    /** Creates a new instance of WSDLActivityConfigurationBean */
    public WSDLActivityConfigurationBean() {
    }

    public WSDLOperationConfigurationBean getOperation() {
        return operation;
    }

	@ConfigurationProperty(name = "operation", label = "WSDL Operation", description = "The WSDL operation")
    public void setOperation(WSDLOperationConfigurationBean operation) {
        this.operation = operation;
    }

	public URI getSecurityProfile() {
		return securityProfile;
	}

	@ConfigurationProperty(name = "securityProfile", label = "Security Profile", description = "WS-Security settings required by the web service", required = false)
	public void setSecurityProfile(URI securityProfile) {
		this.securityProfile = securityProfile;
	}

//	public void setUsername(String username) {
//		this.username = username;
//	}
//
//	public String getUsername() {
//		return username;
//	}
//
//	public void setPassword(String password) {
//		this.password = password;
//	}
//
//	public String getPassword() {
//		return password;
//	}
}
