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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.apache.taverna.cwl.CwlDumyActivity;
import org.apache.taverna.cwl.ui.serviceprovider.CwlServiceDesc;
import org.apache.taverna.cwl.ui.serviceprovider.CwlServiceProvider;
import org.apache.taverna.cwl.utilities.PortDetail;
import org.apache.taverna.scufl2.api.activity.Activity;
import org.apache.taverna.scufl2.api.configurations.Configuration;
import org.apache.taverna.workbench.configuration.colour.ColourManager;
import org.apache.taverna.workbench.ui.actions.activity.HTMLBasedActivityContextualView;

import com.fasterxml.jackson.databind.JsonNode;

import org.apache.taverna.cwl.utilities.CWLUtil;


/*
 * This class is responsible for producing service detail panel for each tool
 * 
 * */
public class CwlContextualView extends HTMLBasedActivityContextualView {

	private static final String DESCRIPTION = "description";
	private static final String LABEL = "label";
	private static final String TABLE_COLOR = "59A9CB";// this is color in RGB
														// hex value
	private static final String TABLE_BORDER = "2";
	private static final String TABLE_WIDTH = "100%";
	private static final String TABLE_CELL_PADDING = "5%";
	private static final String SPACE = " ";
	private static final String LINE_BREAK="\n";
	private static final int MAX_LINE_LENG = 80;
	private static ColourManager colourManager;
	
	
	public static ColourManager getColourManager() {
		return colourManager;
	}

	public static void setColourManager(ColourManager colourManager) {
		CwlContextualView.colourManager = colourManager;
	}

	private final Configuration configurationNode;
	private final Activity activity;
	private JsonNode CwlMap;
	private CWLUtil cwlutil;

	public CwlContextualView(Activity activity) {
		super( activity, colourManager);// FIXME ask colourManager
		this.activity = activity;
		this.configurationNode = activity.getConfiguration();
		CwlMap=configurationNode.getJsonAsObjectNode().get(CwlServiceDesc.CONFIG).get(CwlServiceProvider.MAP);
		cwlutil = new CWLUtil(CwlMap);
		super.initView();
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
		return CwlMap.get(CwlServiceProvider.TOOL_NAME).asText();
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

	// format long description using html <p> tags
	private String paragraphToHtml(String summery, String paragraph) {

		summery += "<tr><td colspan='2' align='left'>";
		paragraph = formatParagraph(paragraph);
		for (String line : paragraph.split("[\n|\r]"))
			summery += "<p>" + line + "</p>";

		summery += "</td></tr>";

		return summery;
	}

	@Override
	protected String getRawTableRowsHtml() {
		String summery = "<table border=\"" + TABLE_BORDER + "\" style=\"width:" + TABLE_WIDTH + "\" bgcolor=\""
				+ TABLE_COLOR + "\" cellpadding=\"" + TABLE_CELL_PADDING + "\" >";

		String description = "";

		if (CwlMap.has(DESCRIPTION)) {
			description = CwlMap.get(DESCRIPTION).asText();
			summery = paragraphToHtml(summery, description);

		}
		if (CwlMap.has(LABEL)) {
			summery += "<tr><th colspan='2' align='left'>Label</th></tr>";
			summery += "<tr><td colspan='2' align='left'>" +  CwlMap.get(LABEL).asText() + "</td></tr>";
		}
		summery += "<tr><th colspan='2' align='left'>Inputs</th></tr>";

		Map<String, PortDetail> inputs = cwlutil.processInputDetails();
		Map<String, Integer> inputDepths = cwlutil.processInputDepths();

		if ((inputs != null && !inputs.isEmpty()) && (inputDepths != null && !inputDepths.isEmpty()))
			for (String id : inputs.keySet()) {
				PortDetail detail = inputs.get(id);
				if (inputDepths.containsKey(id))
					summery = extractSummery(summery, id, detail, inputDepths.get(id));
			}

		summery += "<tr><th colspan='2' align='left'>Outputs</th></tr>";

		Map<String, PortDetail> outPuts = cwlutil.processOutputDetails();
		Map<String, Integer> outputDepths = cwlutil.processOutputDepths();

		if ((outPuts != null && !outPuts.isEmpty()) && (outputDepths != null && !outputDepths.isEmpty()))
			for (String id : outPuts.keySet()) {
				PortDetail detail = outPuts.get(id);
				if (outputDepths.containsKey(id))
					summery = extractSummery(summery, id, detail, outputDepths.get(id));
			}
		summery += "</table>";
		return summery;
	}

	private String extractSummery(String summery, String id, PortDetail detail, int depth) {
		summery += "<tr align='left'><td> ID: " + id + " </td><td>Depth: " + depth + "</td></tr>";
		if (detail.getLabel() != null) {
			summery += "<tr><td  align ='left' colspan ='2'>Label: " + detail.getLabel() + "</td></tr>";
		}
		if (detail.getDescription() != null) {

			summery = paragraphToHtml(summery, detail.getDescription());

		}
		if (detail.getFormat() != null) {
			summery += "<tr><td  align ='left' colspan ='2'>Format: ";
			ArrayList<String> formats = detail.getFormat();

			int Size = formats.size();

			if (Size == 1) {
				summery += formats.get(0);
			} else {
				for (int i = 0; i < (Size - 1); i++) {
					summery += formats.get(i) + ", ";
				}
				summery += formats.get(Size - 1);
			}
			summery += "</td></tr>";
		}
		summery += "<tr></tr>";
		return summery;
	}

	private String formatParagraph(String paragraph) {
		String result = "";
		for (String line : paragraph.split(LINE_BREAK)) {

			while (line.length() > MAX_LINE_LENG) {
				int lastSpaceIndex = line.lastIndexOf(SPACE, MAX_LINE_LENG);
				String firstHalf = line.substring(0, lastSpaceIndex) + LINE_BREAK;
				line = line.substring(lastSpaceIndex + 1);
				result += (firstHalf);
			}
			result += (line + LINE_BREAK);

		}
		return result;
	}

}
