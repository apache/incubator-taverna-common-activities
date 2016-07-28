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

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPanel;

import org.apache.taverna.cwl.ui.serviceprovider.CwlServiceProvider;
import org.apache.taverna.cwl.utilities.CwlContextualUtil;
import org.apache.taverna.scufl2.api.activity.Activity;
import org.apache.taverna.scufl2.api.configurations.Configuration;
import org.apache.taverna.workbench.configuration.colour.ColourManager;
import org.apache.taverna.workbench.ui.actions.activity.HTMLBasedActivityContextualView;

import com.fasterxml.jackson.databind.JsonNode;

/*
 * This class is responsible for producing service detail panel for each tool
 * 
 * */
public class CwlContextualView extends HTMLBasedActivityContextualView {

	private static final String DESCRIPTION = "description";
	private static final String LABEL = "label";
	// private static final String TABLE_COLOR = "59A9CB";// this is color in
	// RGB
	// hex value
	private static final String TABLE_BORDER = "2";
	private static final String TABLE_WIDTH = "100%";
	private static final String TABLE_CELL_PADDING = "5%";

	private static ColourManager colourManager;

	public static ColourManager getColourManager() {
		return colourManager;
	}

	public static void setColourManager(ColourManager colourManager) {
		CwlContextualView.colourManager = colourManager;
	}

	private final Configuration configurationNode;
	private final Activity activity;
	private JsonNode cwlActivityConfiguration;
	private JsonNode cwlToolConfiguration;//This is output of the YAML parser
	private CwlContextualUtil cwlutil;

	public CwlContextualView(Activity activity, ColourManager colourManager) {
		super(activity, colourManager);
		this.activity = activity;
		this.configurationNode = activity.getConfiguration();
		cwlActivityConfiguration = configurationNode.getJson();

		cwlToolConfiguration = cwlActivityConfiguration.get(CwlServiceProvider.CWL_CONF);
		setUpCwlContextualUtil();
		super.initView();
	}

	public void setUpCwlContextualUtil() {
		cwlutil = new CwlContextualUtil(cwlToolConfiguration);
	}

	@Override
	public void initView() {
	}

	@Override
	public JComponent getMainFrame() {
		final JComponent mainFrame = super.getMainFrame();
		JPanel flowPanel = new JPanel(new FlowLayout());

		mainFrame.add(flowPanel, BorderLayout.SOUTH);
		return mainFrame;
	}

	@Override
	public String getViewTitle() {
		return cwlActivityConfiguration.get(CwlServiceProvider.TOOL_NAME).asText();
	}

	/**
	 * Typically called when the activity configuration has changed.
	 */
	@Override
	public void refreshView() {
	}

	/**
	 * View position hint
	 */
	@Override
	public int getPreferredPosition() {
		return 100;
	}

	@Override
	public Action getConfigureAction(final Frame owner) {
		return null;
	}

	@Override
	protected String getRawTableRowsHtml() {
		String summary = "<table border=\"" + TABLE_BORDER + "\" style=\"width:" + TABLE_WIDTH + "\" cellpadding=\""
				+ TABLE_CELL_PADDING + "\" >";

		String description = "";
		// Get the CWL tool Description
		if (cwlToolConfiguration.has(DESCRIPTION)) {
			description = cwlToolConfiguration.get(DESCRIPTION).asText();
			summary = cwlutil.paragraphToHtml(summary, description);

		}
		// Get the CWL tool Label
		if (cwlToolConfiguration.has(LABEL)) {
			summary += "<tr><th colspan='2' align='left'>Label</th></tr>";
			summary += "<tr><td colspan='2' align='left'>" + cwlToolConfiguration.get(LABEL).asText() + "</td></tr>";
		}
		summary += "<tr><th colspan='2' align='left'>Inputs</th></tr>";

		summary = cwlutil.setUpInputDetails(summary);

		summary += "<tr><th colspan='2' align='left'>Outputs</th></tr>";

		summary = cwlutil.setUpOutputDetails(summary);
		summary += "</table>";
		return summary;
	}

}
