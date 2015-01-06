/*******************************************************************************
 * Copyright (C) 2011 The University of Manchester
 *
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
package net.sf.taverna.t2.activities.rest;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import net.sf.taverna.t2.activities.rest.RESTActivity.HTTP_METHOD;
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
