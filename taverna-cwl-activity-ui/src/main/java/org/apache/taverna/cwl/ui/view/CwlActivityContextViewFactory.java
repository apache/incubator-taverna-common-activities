package org.apache.taverna.cwl.ui.view;

import java.util.Arrays;
import java.util.List;

import org.apache.taverna.cwl.CwlDumyActivity;

import net.sf.taverna.t2.workbench.ui.views.contextualviews.ContextualView;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ContextualViewFactory;

public class CwlActivityContextViewFactory implements ContextualViewFactory<CwlDumyActivity> {

	public boolean canHandle(Object selection) {
		return selection instanceof CwlDumyActivity;
	}

	public List<ContextualView> getViews(CwlDumyActivity selection) {
		return Arrays.<ContextualView> asList(new CwlContextualView(selection));
	}
}
