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

package org.apache.taverna.activities.rest;

import net.sf.taverna.t2.workflowmodel.processor.config.ConfigurationBean;
import net.sf.taverna.t2.workflowmodel.processor.config.ConfigurationProperty;

/**
 * HTTP Request Header configuration bean
 *
 * @author David Withers
 */
@ConfigurationBean(uri = "http://www.w3.org/2011/http#RequestHeader")
public class HTTPRequestHeader {

	private String fieldName, fieldValue;

	private boolean use100Continue;

	public String getFieldName() {
		return fieldName;
	}

	@ConfigurationProperty(name = "fieldName", label = "HTTP Header Name")
	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	public String getFieldValue() {
		return fieldValue;
	}

	@ConfigurationProperty(name = "fieldValue", label = "HTTP Header Value")
	public void setFieldValue(String fieldValue) {
		this.fieldValue = fieldValue;
	}

	public boolean isUse100Continue() {
		return use100Continue;
	}

	@ConfigurationProperty(name = "use100Continue", label = "Use 100 Continue", required = false, uri = RESTActivity.URI
			+ "#use100Continue")
	public void setUse100Continue(boolean use100Continue) {
		this.use100Continue = use100Continue;
	}

}
