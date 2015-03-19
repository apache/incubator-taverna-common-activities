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

package org.apache.taverna.activities.interaction;

import java.util.List;

import net.sf.taverna.t2.visit.VisitReport;
import net.sf.taverna.t2.workflowmodel.health.HealthChecker;
import net.sf.taverna.t2.workflowmodel.health.RemoteHealthChecker;

/**
 * Example health checker
 * 
 */
public class InteractionActivityHealthChecker implements
		HealthChecker<InteractionActivity> {

	@Override
	public boolean canVisit(final Object o) {
		return o instanceof InteractionActivity;
	}

	@Override
	public boolean isTimeConsuming() {
		return true;
	}

	@Override
	public VisitReport visit(final InteractionActivity activity,
			final List<Object> ancestry) {

		if (activity.getInteractionActivityType().equals(
				InteractionActivityType.LocallyPresentedHtml)) {
			return RemoteHealthChecker.contactEndpoint(activity,
					activity.getPresentationOrigin());
		}

		return null;
	}

}
