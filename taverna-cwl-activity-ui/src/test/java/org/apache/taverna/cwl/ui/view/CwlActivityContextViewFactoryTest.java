package org.apache.taverna.cwl.ui.view;

import static org.junit.Assert.*;

import org.apache.taverna.cwl.CwlDumyActivity;
import org.apache.taverna.scufl2.api.activity.Activity;
import org.apache.taverna.workbench.configuration.colour.ColourManager;
import org.junit.Before;
import org.junit.Test;

public class CwlActivityContextViewFactoryTest {

	@Before
	public void setUp() throws Exception {
		ColourManager  colourManager=null;
		CwlDumyActivity activity =new  CwlDumyActivity();
		//CwlContextualView contextualView = new CwlContextualView((Activity)activity,colourManager);
	}

	@Test
	public void test() {
		fail("Not yet implemented");
	}

}
