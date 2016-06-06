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

package org.apache.taverna.activities.externaltool.local;

import java.io.IOException;
import java.util.Map;

import org.apache.taverna.activities.externaltool.InvocationCreator;
import org.apache.taverna.activities.externaltool.desc.UseCaseDescription;
import org.apache.taverna.activities.externaltool.invocation.UseCaseInvocation;
import org.apache.taverna.activities.externaltool.manager.InvocationMechanism;
import org.apache.taverna.reference.ReferenceService;
import org.apache.taverna.reference.T2Reference;

import org.apache.log4j.Logger;

/**
 * @author alanrw
 *
 */
public final class LocalInvocationCreator implements
		InvocationCreator {
	
	private static Logger logger = Logger.getLogger(LocalInvocationCreator.class);

	public boolean equals(Object o) {
		return (o instanceof LocalInvocationCreator);
	}

	@Override
	public boolean canHandle(String mechanismType) {
		return mechanismType.equals(LocalUseCaseInvocation.LOCAL_USE_CASE_INVOCATION_TYPE);
	}

	@Override
	public UseCaseInvocation convert(InvocationMechanism m, UseCaseDescription description, Map<String, T2Reference> data, ReferenceService referenceService) {
	    ExternalToolLocalInvocationMechanism mechanism = (ExternalToolLocalInvocationMechanism) m;
		UseCaseInvocation result = null;
		try {
		    result = new LocalUseCaseInvocation(description, mechanism.isRetrieveData(), mechanism.getDirectory(), mechanism.getShellPrefix(), mechanism.getLinkCommand());
		} catch (IOException e) {
			logger.error(e);
		}
		return result;
	}
}
