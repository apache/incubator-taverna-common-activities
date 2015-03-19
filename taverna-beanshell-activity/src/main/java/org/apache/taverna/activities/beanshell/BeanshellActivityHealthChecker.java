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

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;

import org.apache.taverna.activities.dependencyactivity.AbstractAsynchronousDependencyActivity.FileExtFilter;
import net.sf.taverna.t2.visit.VisitReport;
import net.sf.taverna.t2.visit.VisitReport.Status;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.health.HealthCheck;
import net.sf.taverna.t2.workflowmodel.health.HealthChecker;
import bsh.ParseException;
import bsh.Parser;

import com.fasterxml.jackson.databind.JsonNode;

public class BeanshellActivityHealthChecker implements HealthChecker<BeanshellActivity> {

	public boolean canVisit(Object subject) {
		return (subject!=null && subject instanceof BeanshellActivity);
	}

	public VisitReport visit(BeanshellActivity activity, List<Object> ancestors) {
		Object subject = (Processor) VisitReport.findAncestor(ancestors, Processor.class);
		if (subject == null) {
			// Fall back to using the activity itself as the subject of the reports
			subject = activity;
		}
		List<VisitReport> reports = new ArrayList<VisitReport>();

		String script = activity.getConfiguration().get("script").textValue();
		if (! script.trim().endsWith(";")) {
			/** Missing ; on last line is not allowed by Parser,
			 * but is allowed by Interpreter.eval() used at runtime
			 */
			script = script + ";";
		}
		Parser parser = new Parser(new StringReader(script));
		try {
			while (!parser.Line());
			reports.add(new VisitReport(HealthCheck.getInstance(), subject, "Script OK", HealthCheck.NO_PROBLEM, Status.OK));
		} catch (ParseException e) {
		    VisitReport report = new VisitReport(HealthCheck.getInstance(), subject ,e.getMessage(), HealthCheck.INVALID_SCRIPT, Status.SEVERE);
		    report.setProperty("exception", e);
		    reports.add(report);
		}

		// Check if we can find all the Beanshell's dependencies
		if (activity.getConfiguration().has("localDependency")) {
		LinkedHashSet<String> localDependencies = new LinkedHashSet<>();
		for (JsonNode localDependency : activity.getConfiguration().get("localDependency")) {
			localDependencies.add(localDependency.textValue());
		}

		String[] jarArray = activity.libDir.list(new FileExtFilter(".jar"));
		if (jarArray != null) {
		    List<String> jarFiles = Arrays.asList(jarArray); // URLs of all jars found in the lib directory
		    for (String jar : localDependencies) {
			if (jarFiles.contains(jar)){
			    localDependencies.remove(jar);
			}
		    }
		}
		if (localDependencies.isEmpty()){ // all dependencies found
			reports.add(new VisitReport(HealthCheck.getInstance(), subject, "Beanshell dependencies found", HealthCheck.NO_PROBLEM, Status.OK));
		}
		else{
			VisitReport vr = new VisitReport(HealthCheck.getInstance(), subject, "Beanshell dependencies missing", HealthCheck.MISSING_DEPENDENCY, Status.SEVERE);
			vr.setProperty("dependencies", localDependencies);
			vr.setProperty("directory", activity.libDir);
			reports.add(vr);
		}

		}
		Status status = VisitReport.getWorstStatus(reports);
		VisitReport report = new VisitReport(HealthCheck.getInstance(), subject, "Beanshell report", HealthCheck.NO_PROBLEM,
				status, reports);

		return report;

	}

	public boolean isTimeConsuming() {
		return false;
	}


}
