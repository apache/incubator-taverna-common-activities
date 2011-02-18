/*******************************************************************************
 * Copyright (C) 2009 Hajo Nils Krabbenhoeft, INB, University of Luebeck   
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

import java.util.List;

import de.uni_luebeck.inb.knowarc.gui.ProgressDisplayImpl;
import de.uni_luebeck.inb.knowarc.usecases.UseCaseDescription;
import de.uni_luebeck.inb.knowarc.usecases.UseCaseEnumeration;

/**
 * This class stores the repository URL and matching use case id, which identify
 * the use case to invoke.
 * 
 * @author Hajo Nils Krabbenhoeft
 */
public class RegisteredExternalToolActivityConfigurationBean extends ExternalToolActivityConfigurationBean {

	private String repositoryUrl;

	public String getRepositoryUrl() {
		return repositoryUrl;
	}

	public void setRepositoryUrl(String repositoryUrl) {
		this.repositoryUrl = repositoryUrl;
	}

	private String externaltoolid;

	public void setExternaltoolid(String externaltoolid) {
		this.externaltoolid = externaltoolid;
	}

	public String getExternaltoolid() {
		return externaltoolid;
	}
	
	private UseCaseDescription useCaseDescription = null;

	@Override
	public UseCaseDescription getUseCaseDescription() {
		// re-parse the use case XML file
		List<UseCaseDescription> usecases = UseCaseEnumeration.enumerateXmlFile(new ProgressDisplayImpl(KnowARCConfigurationFactory.getConfiguration()),
				this.getRepositoryUrl());
		// retrieve the UseCaseDescription for the given configuration bean
		// and store it into mydesc
		for (UseCaseDescription usecase : usecases) {
			if (!usecase.getUsecaseid().equalsIgnoreCase(this.getExternaltoolid()))
				continue;
			useCaseDescription = usecase;
			break;
		}

		return useCaseDescription;
	}

}
