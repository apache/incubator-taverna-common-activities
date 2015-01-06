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
package net.sf.taverna.t2.activities.wsdl;

import java.net.URI;

import net.sf.taverna.t2.workflowmodel.processor.config.ConfigurationBean;
import net.sf.taverna.t2.workflowmodel.processor.config.ConfigurationProperty;

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
