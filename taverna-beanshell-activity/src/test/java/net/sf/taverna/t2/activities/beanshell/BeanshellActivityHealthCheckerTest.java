/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester
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
package net.sf.taverna.t2.activities.beanshell;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import net.sf.taverna.t2.activities.testutils.ActivityInvoker;
import net.sf.taverna.t2.visit.VisitReport;
import net.sf.taverna.t2.visit.VisitReport.Status;
import net.sf.taverna.t2.workflowmodel.Edits;
import net.sf.taverna.t2.workflowmodel.impl.EditsImpl;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * BeanshellActivityHealthChecker tests
 *
 * @author Stian Soiland-Reyes
 *
 */
public class BeanshellActivityHealthCheckerTest {

	private Edits edits = new EditsImpl();

	private ObjectNode configuration;

	@Before
	public void setup() throws Exception {
		configuration = JsonNodeFactory.instance.objectNode();
		configuration.put("classLoaderSharing", "workflow");
	}

	@Test
	public void oneLinerNoSemicolon() throws Exception {
		BeanshellActivity activity = new BeanshellActivity(null);
		configuration.put("script", "a = 5+3");
		// Notice lack of ;
		activity.configure(configuration);

		Map<String,Object> inputs = new HashMap<String, Object>();
		Map<String, Class<?>> expectedOutputs = new HashMap<String, Class<?>>();
		ActivityInvoker.invokeAsyncActivity(activity, inputs, expectedOutputs);

		BeanshellActivityHealthChecker healthChecker = new BeanshellActivityHealthChecker();
		assertTrue(healthChecker.canVisit(activity));
		ArrayList<Object> ancestors = new ArrayList<Object>();

		ancestors.add(edits.createProcessor("beanie"));
		VisitReport visit = healthChecker.visit(activity, ancestors);
		assertEquals(Status.OK, visit.getStatus());
	}

	@Test
	public void oneLiner() throws Exception {
		BeanshellActivity activity = new BeanshellActivity(null);
		configuration.put("script", "System.out.println(\"Hello\");");
		activity.configure(configuration);

		Map<String,Object> inputs = new HashMap<String, Object>();
		Map<String, Class<?>> expectedOutputs = new HashMap<String, Class<?>>();
		ActivityInvoker.invokeAsyncActivity(activity, inputs, expectedOutputs);

		BeanshellActivityHealthChecker healthChecker = new BeanshellActivityHealthChecker();
		assertTrue(healthChecker.canVisit(activity));
		ArrayList<Object> ancestors = new ArrayList<Object>();

		ancestors.add(edits.createProcessor("beanie"));
		VisitReport visit = healthChecker.visit(activity, ancestors);
		assertEquals(Status.OK, visit.getStatus());
	}

	@Test
	public void threeLines() throws Exception {
		BeanshellActivity activity = new BeanshellActivity(null);
		configuration.put("script", "if (2>1) {\n" +
				"  new Integer(4);\n" +
				"}");
		activity.configure(configuration);

		Map<String,Object> inputs = new HashMap<String, Object>();
		Map<String, Class<?>> expectedOutputs = new HashMap<String, Class<?>>();
		ActivityInvoker.invokeAsyncActivity(activity, inputs, expectedOutputs);

		BeanshellActivityHealthChecker healthChecker = new BeanshellActivityHealthChecker();
		assertTrue(healthChecker.canVisit(activity));
		ArrayList<Object> ancestors = new ArrayList<Object>();

		ancestors.add(edits.createProcessor("beanie"));
		VisitReport visit = healthChecker.visit(activity, ancestors);
		assertEquals(Status.OK, visit.getStatus());



	}

	@Test
	public void invalidScript() throws Exception {
		BeanshellActivity activity = new BeanshellActivity(null);
		configuration.put("script", "invalid script 5 +");
		activity.configure(configuration);

		Map<String,Object> inputs = new HashMap<String, Object>();
		Map<String, Class<?>> expectedOutputs = new HashMap<String, Class<?>>();
		try {
			ActivityInvoker.invokeAsyncActivity(activity, inputs, expectedOutputs);
			fail("Script should not be valid");
		} catch (RuntimeException ex) {
			// expected to fail
		}


		BeanshellActivityHealthChecker healthChecker = new BeanshellActivityHealthChecker();
		assertTrue(healthChecker.canVisit(activity));
		ArrayList<Object> ancestors = new ArrayList<Object>();

		ancestors.add(edits.createProcessor("beanie"));
		VisitReport visit = healthChecker.visit(activity, ancestors);
		assertEquals(Status.SEVERE, visit.getStatus());
	}

	@Test
	public void strangeWhitespace() throws Exception {
		BeanshellActivity activity = new BeanshellActivity(null);
		configuration.put("script", "b = \"fish\";\n" +
				"a = 2+3\n" +
				"\n" +
				"\n" +
				"  +5   ");
		// Notice lots of whitespace, but still valid
		activity.configure(configuration);

		Map<String,Object> inputs = new HashMap<String, Object>();
		Map<String, Class<?>> expectedOutputs = new HashMap<String, Class<?>>();
		ActivityInvoker.invokeAsyncActivity(activity, inputs, expectedOutputs);

		BeanshellActivityHealthChecker healthChecker = new BeanshellActivityHealthChecker();
		assertTrue(healthChecker.canVisit(activity));
		ArrayList<Object> ancestors = new ArrayList<Object>();

		ancestors.add(edits.createProcessor("beanie"));
		VisitReport visit = healthChecker.visit(activity, ancestors);
		System.out.println(visit);
		assertEquals(Status.OK, visit.getStatus());
	}
}
