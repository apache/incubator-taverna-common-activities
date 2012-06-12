/*******************************************************************************
 * Copyright (C) 2010 The University of Manchester
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
package net.sf.taverna.t2.activities.beanshell;

import java.net.URI;

import uk.org.taverna.configuration.app.ApplicationConfiguration;

import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityFactory;

/**
 * An {@link ActivityFactory} for creating <code>BeanshellActivity</code>.
 *
 * @author David Withers
 */
public class BeanshellActivityFactory implements ActivityFactory {

	private ApplicationConfiguration applicationConfiguration;

	@Override
	public BeanshellActivity createActivity() {
		return new BeanshellActivity(applicationConfiguration);
	}

	@Override
	public URI getActivityURI() {
		return URI.create(BeanshellActivity.URI);
	}

	@Override
	public Object createActivityConfiguration() {
		return new BeanshellActivityConfigurationBean();
	}

	public void setApplicationConfiguration(ApplicationConfiguration applicationConfiguration) {
		this.applicationConfiguration = applicationConfiguration;
	}

}
