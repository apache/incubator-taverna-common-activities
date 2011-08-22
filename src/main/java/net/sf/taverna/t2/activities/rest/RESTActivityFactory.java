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

import org.apache.http.client.CredentialsProvider;

import net.sf.taverna.t2.security.credentialmanager.CredentialManager;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityFactory;

/**
 * An {@link ActivityFactory} for creating <code>RESTActivity</code>.
 *
 * @author David Withers
 */
public class RESTActivityFactory implements ActivityFactory {

	private CredentialsProvider credentialsProvider;

	@Override
	public RESTActivity createActivity() {
		return new RESTActivity(credentialsProvider);
	}

	@Override
	public URI getActivityURI() {
		return URI.create(RESTActivity.URI);
	}

	@Override
	public Object createActivityConfiguration() {
		return new RESTActivityConfigurationBean();
	}

	public void setCredentialManager(CredentialManager credentialManager) {
		credentialsProvider = new RESTActivityCredentialsProvider(credentialManager);
	}

}
