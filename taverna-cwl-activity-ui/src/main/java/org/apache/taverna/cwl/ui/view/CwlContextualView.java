package org.apache.taverna.cwl.ui.view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.util.HashMap;
import java.util.Map;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.apache.taverna.cwl.CwlActivityConfigurationBean;
import org.apache.taverna.cwl.CwlDumyActivity;
import org.apache.taverna.cwl.PortDetail;
import net.sf.taverna.t2.workbench.ui.actions.activity.HTMLBasedActivityContextualView;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

public class CwlContextualView extends HTMLBasedActivityContextualView<CwlActivityConfigurationBean> {

	private static final String DESCRIPTION = "description";

	private final CwlActivityConfigurationBean configurationBean;
	private final CwlDumyActivity activity;

	public CwlContextualView(CwlDumyActivity activity) {
		super((Activity) activity);
		this.activity = activity;
		this.configurationBean = activity.getConfiguration();
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

	private String paragraphToHtml(String summery, String paragraph) {

		summery += "<tr><td colspan='2' align='left'>";

		for (String line : paragraph.split("[\n|\r]"))
			summery += "<p>" + line + "</p>";

		summery += "</td></tr>";

		return summery;
	}

	@Override
	protected String getRawTableRowsHtml() {
		String summery = "";

		Map cwlFile = configurationBean.getCwlConfigurations();
		String description = "";

		if (cwlFile.containsKey(DESCRIPTION)) {
			
			description = (String) cwlFile.get(DESCRIPTION);
			summery = paragraphToHtml(summery, description);

		}

		summery += "<tr><th colspan='2' align='left'>Inputs</th></tr>";

		HashMap<String, PortDetail> inputs = activity.getProcessedInputs();
		if (inputs != null)
			for (String id : inputs.keySet()) {
				PortDetail detail = inputs.get(id);
				summery += "<tr align='left'><td> ID: " + id + " </td><td>Depth: " + detail.getDepth() + "</td></tr>";

				if (detail.getDescription() != null) {

					summery = paragraphToHtml(summery, detail.getDescription());

				}
				summery += "<tr></tr>";
			}

		summery += "<tr><th colspan='2' align='left'>Outputs</th></tr>";

		HashMap<String, PortDetail> outPuts = activity.getProcessedOutputs();
		
		if (outPuts != null)
			for (String id : outPuts.keySet()) {
				PortDetail detail = outPuts.get(id);
				summery += "<tr align='left'><td> ID: " + id + " </td><td>Depth: " + detail.getDepth() + "</td></tr>";

				if (detail.getDescription() != null) {
					summery = paragraphToHtml(summery, detail.getDescription());
				}
				summery += "<tr></tr>";
			}

		return summery;
	}

}
