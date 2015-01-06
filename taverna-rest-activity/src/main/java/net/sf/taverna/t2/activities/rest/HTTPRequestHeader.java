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
