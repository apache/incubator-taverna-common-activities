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

package org.apache.taverna.activities.wsdl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.wsdl.WSDLException;
import javax.xml.parsers.ParserConfigurationException;

import net.sf.taverna.t2.visit.VisitReport;
import net.sf.taverna.t2.visit.VisitReport.Status;
import net.sf.taverna.t2.workflowmodel.health.HealthCheck;
import net.sf.taverna.t2.workflowmodel.health.RemoteHealthChecker;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.workflowmodel.processor.activity.DisabledActivity;
import org.apache.taverna.wsdl.parser.UnknownOperationException;
import org.apache.taverna.wsdl.parser.WSDLParser;

import org.xml.sax.SAXException;

import com.fasterxml.jackson.databind.JsonNode;

public class WSDLActivityHealthChecker extends RemoteHealthChecker {

	private Activity<?> activity;

        @Override
	public boolean canVisit(Object subject) {
		if (subject == null) {
			return false;
		}
		if (subject instanceof WSDLActivity) {
			return true;
		}
		if (subject instanceof DisabledActivity) {
			return (((DisabledActivity) subject).getActivity() instanceof WSDLActivity);
		}
		return false;
	}

        @Override
	public VisitReport visit(Object o, List<Object> ancestors) {
		List<VisitReport> reports = new ArrayList<VisitReport>();
		activity = (Activity<?>) o;
		String endpoint = null;

		WSDLParser parser;
		try {
			JsonNode configuration = null;
			if (activity instanceof WSDLActivity) {
				configuration = ((WSDLActivity)activity).getConfiguration();
			} else if (activity instanceof DisabledActivity) {
				configuration = (JsonNode) ((DisabledActivity) activity).getActivityConfiguration();
			}
			endpoint = configuration.get("operation").get("wsdl").asText();
			VisitReport wsdlEndpointReport = RemoteHealthChecker.contactEndpoint(activity, endpoint);
			reports.add(wsdlEndpointReport);
			if (!wsdlEndpointReport.getStatus().equals(Status.SEVERE)) {
			    parser = new WSDLParser(endpoint);

			    String operationName = configuration.get("operation").get("name").asText();
			    try {
                reports.add(testStyleAndUse(endpoint,
							    parser,
							    operationName));
				reports.add(testEndpoint(parser, operationName));
			    } catch (UnknownOperationException e) {
				VisitReport vr = new VisitReport(HealthCheck.getInstance(), activity,
							 "Operation not found", HealthCheck.UNKNOWN_OPERATION,
							 Status.SEVERE);
				vr.setProperty("operationName", operationName);
				vr.setProperty("endpoint", endpoint);
				reports.add(vr);
			    }
			}

		} catch (ParserConfigurationException e) {
			VisitReport vr = new VisitReport(HealthCheck.getInstance(), activity, "Invalid WSDL", HealthCheck.BAD_WSDL, Status.SEVERE);
			vr.setProperty("exception", e);
			vr.setProperty("endpoint", endpoint);
			reports.add(vr);
		} catch (WSDLException e) {
			VisitReport vr = new VisitReport(HealthCheck.getInstance(), activity, "Invalid WSDL", HealthCheck.BAD_WSDL, Status.SEVERE);
			vr.setProperty("exception", e);
			vr.setProperty("endpoint", endpoint);
			reports.add(vr);
		} catch (IOException e) {
			VisitReport vr = new VisitReport(HealthCheck.getInstance(), activity, "Read problem", HealthCheck.IO_PROBLEM, Status.SEVERE);
			vr.setProperty("exception", e);
			vr.setProperty("endpoint", endpoint);
			reports.add(vr);
		} catch (SAXException e) {
			VisitReport vr = new VisitReport(HealthCheck.getInstance(), activity, "Invalid WSDL", HealthCheck.BAD_WSDL, Status.SEVERE);
			vr.setProperty("exception", e);
			vr.setProperty("endpoint", endpoint);
			reports.add(vr);
		}

		Status status = VisitReport.getWorstStatus(reports);
		VisitReport report = new VisitReport(HealthCheck.getInstance(), activity, "WSDL Activity report", HealthCheck.NO_PROBLEM,
				status, reports);

		return report;
	}

	public static boolean checkStyleAndUse(String style, String use) {
		return !(style.equalsIgnoreCase("rpc") && use.equalsIgnoreCase("literal"));
	}

	private VisitReport testStyleAndUse(String endpoint, WSDLParser parser, String operationName) throws
                UnknownOperationException {
		VisitReport report;
		String style = parser.getStyle().toLowerCase();
		String use = "?";
		use = parser.getUse(operationName).toLowerCase();
		if (!checkStyleAndUse(style, use)) {
		    report = new VisitReport(HealthCheck.getInstance(), activity,
					     "Unsupported style", HealthCheck.UNSUPPORTED_STYLE,
					     Status.SEVERE);
		    report.setProperty("use", use);
		    report.setProperty("style", style);
		    report.setProperty("endpoint", endpoint);
		} else {
		    report = new VisitReport(HealthCheck.getInstance(), activity, style + "/"
					     + use + " is OK", HealthCheck.NO_PROBLEM, Status.OK);
		}
		return report;
	}

	private VisitReport testEndpoint(WSDLParser parser, String operationName) {
		List<VisitReport> reports = new ArrayList<VisitReport>();
                
                try {
                    List<String> endpoints = parser
                                    .getOperationEndpointLocations(operationName);
                    for (String endpoint : endpoints) {
                            reports.add(RemoteHealthChecker.contactEndpoint(activity, endpoint));
                    }
                } catch (UnknownOperationException ex) {
		    VisitReport report = new VisitReport(HealthCheck.getInstance(), activity, "Operation could not be located.", HealthCheck.UNKNOWN_OPERATION, Status.SEVERE);
		    report.setProperty("operationName", operationName);
		    return report;                    
                }

		Status status = VisitReport.getWorstStatus(reports);
		if (reports.isEmpty()) {
		    VisitReport report = new VisitReport(HealthCheck.getInstance(), activity, "Service could not be located.", HealthCheck.NO_ENDPOINTS, Status.SEVERE);
		    report.setProperty("operationName", operationName);
		    return report;
		} else if (reports.size()==1) {
			return reports.get(0);
		} else {
			return new VisitReport(HealthCheck.getInstance(), activity, "Endpoint tests",  HealthCheck.NO_PROBLEM, status, reports);
		}
        }
}
