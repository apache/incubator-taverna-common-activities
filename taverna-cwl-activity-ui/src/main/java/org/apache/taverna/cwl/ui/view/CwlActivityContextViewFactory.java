/*******************************************************************************
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *     contributor license agreements.  See the NOTICE file distributed with
 *     this work for additional information regarding copyright ownership.
 *     The ASF licenses this file to You under the Apache License, Version 2.0
 *     (the "License"); you may not use this file except in compliance with
 *     the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *******************************************************************************/
package org.apache.taverna.cwl.ui.view;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

import org.apache.taverna.scufl2.api.activity.Activity;
import org.apache.taverna.workbench.configuration.colour.ColourManager;
import org.apache.taverna.workbench.ui.views.contextualviews.ContextualView;
import org.apache.taverna.workbench.ui.views.contextualviews.activity.ContextualViewFactory;



public class CwlActivityContextViewFactory implements ContextualViewFactory<Activity> {
	public static final URI ACTIVITY_TYPE = URI.create("https://taverna.apache.org/ns/2016/activity/cwl");
	private  ColourManager colourManager;

	public ColourManager getColourManager() {
		return colourManager;
	}


	public void setColourManager(ColourManager colourManager) {
		this.colourManager = colourManager;
	}


	@Override
	public List<ContextualView> getViews(Activity selection) {
		return Arrays.<ContextualView> asList(new CwlContextualView(selection,colourManager));
	}


	@Override
	public boolean canHandle(Object selection) {
		return selection instanceof Activity && ((Activity) selection).getType().equals(ACTIVITY_TYPE);
	}
}
