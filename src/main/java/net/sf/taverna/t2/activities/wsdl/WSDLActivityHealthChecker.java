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
package net.sf.taverna.t2.activities.wsdl;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
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
import net.sf.taverna.wsdl.parser.UnknownOperationException;
import net.sf.taverna.wsdl.parser.WSDLParser;

import org.xml.sax.SAXException;

public class WSDLActivityHealthChecker extends RemoteHealthChecker {
	
	private Activity activity;

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

	public VisitReport visit(Object o, List<Object> ancestors) {
		List<VisitReport> reports = new ArrayList<VisitReport>();
		activity = (Activity) o;
		
		WSDLParser parser;
		try {
			WSDLActivityConfigurationBean configuration = null;
			if (activity instanceof WSDLActivity) {
				configuration = (WSDLActivityConfigurationBean) activity.getConfiguration();
			} else if (activity instanceof DisabledActivity) {
				configuration = (WSDLActivityConfigurationBean) ((DisabledActivity) activity).getActivityConfiguration();
			}
			VisitReport wsdlEndpointReport = RemoteHealthChecker.contactEndpoint(activity, configuration.getWsdl());
			reports.add(wsdlEndpointReport);
			if (!wsdlEndpointReport.getStatus().equals(Status.SEVERE)) {
			parser = new WSDLParser(configuration.getWsdl());

			reports.add(testEndpoint(parser, configuration
					.getOperation()));
			reports.add(testStyleAndUse(parser, configuration
					.getOperation()));
			}

		} catch (ParserConfigurationException e) {
			VisitReport vr = new VisitReport(HealthCheck.getInstance(), activity, "Bad WSDL", HealthCheck.BAD_WSDL, Status.SEVERE);
			vr.setProperty("exception", e);
			reports.add(vr);
		} catch (WSDLException e) {
			VisitReport vr = new VisitReport(HealthCheck.getInstance(), activity, "Bad WSDL", HealthCheck.BAD_WSDL, Status.SEVERE);
			vr.setProperty("exception", e);
			reports.add(vr);
		} catch (IOException e) {
			VisitReport vr = new VisitReport(HealthCheck.getInstance(), activity, "IO problem", HealthCheck.IO_PROBLEM, Status.SEVERE);
			vr.setProperty("exception", e);
			reports.add(vr);
		} catch (SAXException e) {
			VisitReport vr = new VisitReport(HealthCheck.getInstance(), activity, "Bad WSDL", HealthCheck.BAD_WSDL, Status.SEVERE);
			vr.setProperty("exception", e);
			reports.add(vr);
		}

		Status status = VisitReport.getWorstStatus(reports);
		VisitReport report = new VisitReport(HealthCheck.getInstance(), activity, "WSDL Activity report", HealthCheck.NO_PROBLEM,
				status, reports);

		return report;
	}

	private VisitReport testStyleAndUse(WSDLParser parser, String operationName) {
		VisitReport report;
		String style = parser.getStyle().toLowerCase();
		String use = "?";
		try {
			use = parser.getUse(operationName).toLowerCase();
			if (use.equals("literal") && style.equals("rpc")) {
				report = new VisitReport(HealthCheck.getInstance(), activity,
						"Unsupported style", HealthCheck.UNSUPPORTED_STYLE, 
						Status.SEVERE);
				report.setProperty("use", use);
				report.setProperty("style", style);
			} else {
				report = new VisitReport(HealthCheck.getInstance(), activity, style + "/"
						+ use + " is OK", HealthCheck.NO_PROBLEM, Status.OK);
			}
		} catch (UnknownOperationException e) {
			report = new VisitReport(HealthCheck.getInstance(), activity,
					"Unable to find use for operation:" + operationName, HealthCheck.UNKNOWN_OPERATION,
					Status.SEVERE);
		}
		return report;
	}

	private VisitReport testEndpoint(WSDLParser parser, String operationName) {
		List<VisitReport> reports = new ArrayList<VisitReport>();
		List<String> endpoints = parser
				.getOperationEndpointLocations(operationName);
		for (String endpoint : endpoints) {
			reports.add(RemoteHealthChecker.contactEndpoint(activity, endpoint));
		}

		Status status = VisitReport.getWorstStatus(reports);
		if (reports.size()==1) {
			return reports.get(0);
		}
		else if (reports.size()==0) {
			return new VisitReport(HealthCheck.getInstance(), activity, "No service endpoint could be determined from the WSDL", HealthCheck.NO_ENDPOINTS, Status.SEVERE);
		}
		else {
			return new VisitReport(HealthCheck.getInstance(), activity, "Endpoint tests",  HealthCheck.NO_PROBLEM, status, reports);
		}
	}

}
