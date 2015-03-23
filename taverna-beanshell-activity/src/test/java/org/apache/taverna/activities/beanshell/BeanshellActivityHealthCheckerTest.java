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

package org.apache.taverna.activities.beanshell;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.taverna.activities.testutils.ActivityInvoker;
import org.apache.taverna.visit.VisitReport;
import org.apache.taverna.visit.VisitReport.Status;
import org.apache.taverna.workflowmodel.Edits;
import org.apache.taverna.workflowmodel.impl.EditsImpl;

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
