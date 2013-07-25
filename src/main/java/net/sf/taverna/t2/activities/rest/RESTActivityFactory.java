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

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.sf.taverna.t2.activities.rest.URISignatureHandler.URISignatureParsingException;
import net.sf.taverna.t2.security.credentialmanager.CredentialManager;
import net.sf.taverna.t2.workflowmodel.Edits;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityFactory;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityInputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityOutputPort;

import org.apache.http.client.CredentialsProvider;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * An {@link ActivityFactory} for creating <code>RESTActivity</code>.
 *
 * @author David Withers
 */
public class RESTActivityFactory implements ActivityFactory {

	private static Logger logger = Logger.getLogger(RESTActivityFactory.class);

	private CredentialsProvider credentialsProvider;
	private Edits edits;

	@Override
	public RESTActivity createActivity() {
		return new RESTActivity(credentialsProvider);
	}

	@Override
	public URI getActivityType() {
		return URI.create(RESTActivity.URI);
	}

	@Override
	public JsonNode getActivityConfigurationSchema() {
		ObjectMapper objectMapper = new ObjectMapper();
		try {
 			return objectMapper.readTree(getClass().getResource("/schema.json"));
		} catch (IOException e) {
			return objectMapper.createObjectNode();
		}
	}

	public void setCredentialManager(CredentialManager credentialManager) {
		credentialsProvider = new RESTActivityCredentialsProvider(credentialManager);
	}

	@Override
	public Set<ActivityInputPort> getInputPorts(JsonNode configuration)
			throws ActivityConfigurationException {
		Set<ActivityInputPort> activityInputPorts = new HashSet<>();
		RESTActivityConfigurationBean configBean = new RESTActivityConfigurationBean(configuration);
		// ---- CREATE INPUTS ----

		// all input ports are dynamic and depend on the configuration
		// of the particular instance of the REST activity

		// POST and PUT operations send data, so an input for the message body
		// is required
		if (RESTActivity.hasMessageBodyInputPort(configBean.getHttpMethod())) {
			// the input message will be just an XML string for now
			activityInputPorts.add(edits.createActivityInputPort(RESTActivity.IN_BODY, 0, true, null, configBean.getOutgoingDataFormat()
					.getDataFormat()));
		}

		// now process the URL signature - extract all placeholders and create
		// an input port for each
		List<String> placeholders = URISignatureHandler
				.extractPlaceholders(configBean.getUrlSignature());
		String acceptsHeaderValue = configBean.getAcceptsHeaderValue();
		if (acceptsHeaderValue != null && !acceptsHeaderValue.isEmpty()) {
			try {
			List<String> acceptsPlaceHolders = URISignatureHandler
				.extractPlaceholders(acceptsHeaderValue);
			acceptsPlaceHolders.removeAll(placeholders);
			placeholders.addAll(acceptsPlaceHolders);
			}
			catch (URISignatureParsingException e) {
				logger.error(e);
			}
		}
		for (ArrayList<String> httpHeaderNameValuePair : configBean.getOtherHTTPHeaders()) {
			try {
				List<String> headerPlaceHolders = URISignatureHandler
				.extractPlaceholders(httpHeaderNameValuePair.get(1));
				headerPlaceHolders.removeAll(placeholders);
				placeholders.addAll(headerPlaceHolders);
			}
			catch (URISignatureParsingException e) {
				logger.error(e);
			}
		}
		for (String placeholder : placeholders) {
			// these inputs will have a dynamic name each;
			// the data type is string as they are the values to be
			// substituted into the URL signature at the execution time
			activityInputPorts.add(edits.createActivityInputPort(placeholder, 0, true, null, String.class));
		}
		return activityInputPorts;
	}

	@Override
	public Set<ActivityOutputPort> getOutputPorts(JsonNode configuration)
			throws ActivityConfigurationException {
		Set<ActivityOutputPort> activityOutputPorts = new HashSet<>();
		RESTActivityConfigurationBean configBean = new RESTActivityConfigurationBean(configuration);
		// ---- CREATE OUTPUTS ----
		// all outputs are of depth 0 - i.e. just a single value on each;

		// output ports for Response Body and Status are static - they don't
		// depend on the configuration of the activity;
		activityOutputPorts.add(edits.createActivityOutputPort(RESTActivity.OUT_RESPONSE_BODY, 0, 0));
		activityOutputPorts.add(edits.createActivityOutputPort(RESTActivity.OUT_STATUS, 0, 0));
		if (configBean.getShowActualUrlPort()) {
			activityOutputPorts.add(edits.createActivityOutputPort(RESTActivity.OUT_COMPLETE_URL, 0, 0));
			}
			if (configBean.getShowResponseHeadersPort()) {
				activityOutputPorts.add(edits.createActivityOutputPort(RESTActivity.OUT_RESPONSE_HEADERS, 1, 1));
			}
		// Redirection port may be hidden/shown
		if (configBean.getShowRedirectionOutputPort()) {
			activityOutputPorts.add(edits.createActivityOutputPort(RESTActivity.OUT_REDIRECTION, 0, 0));
		}
		return activityOutputPorts;
	}

	public void setEdits(Edits edits) {
		this.edits = edits;
	}

}
