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

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.taverna.activities.rest.RESTActivity.HTTP_METHOD;
import net.sf.taverna.t2.workflowmodel.processor.config.ConfigurationBean;
import net.sf.taverna.t2.workflowmodel.processor.config.ConfigurationProperty;

/**
 * HTTP Request configuration bean.
 *
 * @author David Withers
 */
@ConfigurationBean(uri = RESTActivity.URI + "#Request")
public class HTTPRequest {

	private HTTP_METHOD method;

	private String absoluteURITemplate;

	private List<HTTPRequestHeader> headers = new ArrayList<HTTPRequestHeader>();

	public HTTP_METHOD getMethod() {
		return method;
	}

	@ConfigurationProperty(name = "mthd", label = "HTTP Method", uri="http://www.w3.org/2011/http#mthd")
	public void setMethod(URI method) {
		setMethod(HTTP_METHOD.valueOf(method.getFragment()));
	}

	public void setMethod(HTTP_METHOD method) {
		this.method = method;
	}

	public String getAbsoluteURITemplate() {
		return absoluteURITemplate;
	}

	@ConfigurationProperty(name = "absoluteURITemplate", label = "URL Template")
	public void setAbsoluteURITemplate(String absoluteURITemplate) {
		this.absoluteURITemplate = absoluteURITemplate;
	}

	public List<HTTPRequestHeader> getHeaders() {
		return headers;
	}

	@ConfigurationProperty(name = "headers", label = "HTTP Request Headers", uri="http://www.w3.org/2011/http#headers")
	public void setHeaders(List<HTTPRequestHeader> headers) {
		this.headers = headers;
	}

	public HTTPRequestHeader getHeader(String name) {
		for (HTTPRequestHeader httpRequestHeader : headers) {
			if (httpRequestHeader.getFieldName().equals(name)) {
				return httpRequestHeader;
			}
		}
		return null;
	}

	public void setHeader(String name, String value) {
		HTTPRequestHeader httpRequestHeader = getHeader(name);
		if (httpRequestHeader == null) {
			httpRequestHeader = new HTTPRequestHeader();
			httpRequestHeader.setFieldName(name);
			headers.add(httpRequestHeader);
		}
		httpRequestHeader.setFieldValue(value);
	}

	public void setHeader(String name, boolean use100Continue) {
		HTTPRequestHeader httpRequestHeader = getHeader(name);
		if (httpRequestHeader == null) {
			httpRequestHeader = new HTTPRequestHeader();
			httpRequestHeader.setFieldName(name);
			headers.add(httpRequestHeader);
		}
		httpRequestHeader.setUse100Continue(use100Continue);
	}

}
