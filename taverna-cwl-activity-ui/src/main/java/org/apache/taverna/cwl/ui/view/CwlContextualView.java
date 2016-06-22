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
import org.apache.taverna.cwl.CwlActivityConfigurationBean;
import org.apache.taverna.cwl.CwlDumyActivity;
import org.apache.taverna.cwl.utilities.PortDetail;
import org.apache.taverna.cwl.utilities.CWLUtil;

import net.sf.taverna.t2.workbench.ui.actions.activity.HTMLBasedActivityContextualView;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

/*
 * This class is responsible for producing service detail panel for each tool
 * 
 * */
public class CwlContextualView extends HTMLBasedActivityContextualView<CwlActivityConfigurationBean> {

	private static final String DESCRIPTION = "description";
	private static final String LABEL = "label";
	private static final String TABLE_COLOR = "59A9CB";// this is color in RGB
														// hex value
	private static final String TABLE_BORDER = "2";
	private static final String TABLE_WIDTH = "100%";
	private static final String TABLE_CELL_PADDING = "5%";

	private final CwlActivityConfigurationBean configurationBean;
	private final CwlDumyActivity activity;

	private CWLUtil cwlutil;
	public CwlContextualView(CwlDumyActivity activity) {
		super((Activity) activity);
		this.activity = activity;
		this.configurationBean = activity.getConfiguration();
		cwlutil = new CWLUtil(configurationBean.getCwlConfigurations());
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
		return configurationBean.getToolName();
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

		for (String line : paragraph.split("[\n|\r]"))
			summery += "<p>" + line + "</p>";

		summery += "</td></tr>";

		return summery;
	}

	@Override
	protected String getRawTableRowsHtml() {
		String summery = "<table border=\"" + TABLE_BORDER + "\" style=\"width:" + TABLE_WIDTH + "\" bgcolor=\""
				+ TABLE_COLOR + "\" cellpadding=\"" + TABLE_CELL_PADDING + "\" >";

		Map cwlFile = configurationBean.getCwlConfigurations();
		String description = "";

		if (cwlFile.containsKey("description")) {
			description = (String) cwlFile.get("description");
			summery = paragraphToHtml(summery, description);

		}
		if (cwlFile.containsKey(LABEL)) {
			summery += "<tr><th colspan='2' align='left'>Label</th></tr>";
			summery += "<tr><td colspan='2' align='left'>" + (String) cwlFile.get(LABEL) + "</td></tr>";
		}
		summery += "<tr><th colspan='2' align='left'>Inputs</th></tr>";

		HashMap<String, PortDetail> inputs = cwlutil.processInputDetails();
		HashMap<String, Integer> inputDepths = cwlutil.processInputDepths();
		
		if ((inputs != null && !inputs.isEmpty())&&(inputDepths != null && !inputDepths.isEmpty()))
			for (String id : inputs.keySet()) {
				PortDetail detail = inputs.get(id);
				if(inputDepths.containsKey(id))
				summery = extractSummery(summery, id, detail,inputDepths.get(id));
			}

		summery += "<tr><th colspan='2' align='left'>Outputs</th></tr>";

		HashMap<String, PortDetail> outPuts = cwlutil.processOutputDetails();
		HashMap<String, Integer> outputDepths = cwlutil.processOutputDepths();
		
		if ((outPuts != null && !outPuts.isEmpty())&&(outputDepths != null && !outputDepths.isEmpty()))
			for (String id : outPuts.keySet()) {
				PortDetail detail = outPuts.get(id);
				if(outputDepths.containsKey(id))
					summery = extractSummery(summery, id, detail,outputDepths.get(id));
			}
		summery += "</table>";
		return summery;
	}

	private String extractSummery(String summery, String id, PortDetail detail,int depth) {
		summery += "<tr align='left'><td> ID: " + id + " </td><td>Depth: " + depth+ "</td></tr>";
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
}
