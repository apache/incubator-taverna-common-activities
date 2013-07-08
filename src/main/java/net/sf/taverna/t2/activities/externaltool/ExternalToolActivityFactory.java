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
package net.sf.taverna.t2.activities.externaltool;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.sf.taverna.t2.activities.externaltool.manager.MechanismCreator;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityFactory;

/**
 * An {@link ActivityFactory} for creating <code>ExternalToolActivity</code>.
 *
 * @author David Withers
 */
public class ExternalToolActivityFactory implements ActivityFactory {

	private List<InvocationCreator> invocationCreators;

	private List<MechanismCreator> mechanismCreators;

	@Override
	public ExternalToolActivity createActivity() {
		ExternalToolActivity activity = new ExternalToolActivity();
		activity.setInvocationCreators(invocationCreators);
		return activity;
	}

	@Override
	public URI getActivityType() {
		return URI.create(ExternalToolActivity.URI);
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

	public void setInvocationCreators(List<InvocationCreator> invocationCreators) {
		this.invocationCreators = invocationCreators;
	}

	public void setMechanismCreators(List<MechanismCreator> mechanismCreators) {
		this.mechanismCreators = mechanismCreators;
	}

}
