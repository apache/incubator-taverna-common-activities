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

import net.sf.taverna.t2.workflowmodel.health.HealthChecker;
import net.sf.taverna.t2.workflowmodel.health.HealthReport;
import net.sf.taverna.t2.workflowmodel.health.HealthReport.Status;
import net.sf.taverna.wsdl.parser.UnknownOperationException;
import net.sf.taverna.wsdl.parser.WSDLParser;

import org.xml.sax.SAXException;

public class WSDLActivityHealthChecker implements HealthChecker<WSDLActivity> {

	public boolean canHandle(Object subject) {
		return subject != null && subject instanceof WSDLActivity;
	}

	public HealthReport checkHealth(WSDLActivity activity) {
		List<HealthReport> reports = new ArrayList<HealthReport>();
		

		WSDLParser parser;
		try {
			parser = new WSDLParser(activity.getConfiguration().getWsdl());
			reports.add(testWSDL(activity.getConfiguration().getWsdl()));
			reports.add(testEndpoint(parser, activity.getConfiguration()
					.getOperation()));
			reports.add(testStyleAndUse(parser, activity.getConfiguration()
					.getOperation()));

		} catch (ParserConfigurationException e) {
			reports.add(new HealthReport("WSDL Activity","Error whilst parsing the WSDL:"+e.getMessage(),Status.SEVERE));
		} catch (WSDLException e) {
			reports.add(new HealthReport("WSDL Activity","Error whilst parsing the WSDL:"+e.getMessage(),Status.SEVERE));
		} catch (IOException e) {
			reports.add(new HealthReport("WSDL Activity","Communication error whilst parsing the WSDL:"+e.getMessage(),Status.SEVERE));
		} catch (SAXException e) {
			reports.add(new HealthReport("WSDL Activity","XML error whilst parsing the WSDL:"+e.getMessage(),Status.SEVERE));
		}

		Status status = highestStatus(reports);
		HealthReport report = new HealthReport("WSDL Activity", "",
				status, reports);

		return report;
	}

	private int pingURL(HttpURLConnection httpConnection, int timeout)
			throws IOException {
		httpConnection.setRequestMethod("HEAD");
		httpConnection.connect();
		httpConnection.setReadTimeout(timeout);
		return httpConnection.getResponseCode();
	}

	private HealthReport testWSDL(String wsdl) {
		HealthReport report;
		try {
			URL url = new URL(wsdl);
			URLConnection connection = url.openConnection();
			if (connection instanceof HttpURLConnection) {
				int code = pingURL((HttpURLConnection) connection, 15000);
				if (code != 200) {
					report = new HealthReport("WSDL Test",
							"Pinging the WSDL did not responded with " + code
									+ " rather than 200", Status.WARNING);
				} else {
					report = new HealthReport("WSDL Test", "The WSDL ["
							+ wsdl + "] responded OK", Status.OK);
				}
			}
			else {
				report = new HealthReport("WSDL Test","The WSDL is not HTTP based which may affect workflow portability",Status.WARNING);
			}
		} catch (MalformedURLException e) {
			report = new HealthReport("WSDL Test",
					"There was a problem with the WSDL URL:" + e.getMessage(),
					Status.SEVERE);
		} catch (SocketTimeoutException e) {
			report = new HealthReport(
							"WSDL Test",
							"Reading the WSDL tool longer than 15 seconds to get a response",
							Status.WARNING);
		} catch (IOException e) {
			report = new HealthReport("WSDL Test",
					"There was an error opening the WSDL:" + e.getMessage(),
					Status.WARNING);
		}
		return report;
	}

	private Status highestStatus(List<HealthReport> reports) {
		Status status = Status.OK;
		for (HealthReport report : reports) {
			if (report.getStatus().equals(Status.WARNING)
					&& status.equals(Status.OK))
				status = report.getStatus();
			if (report.getStatus().equals(Status.SEVERE))
				status = Status.SEVERE;
		}
		return status;
	}

	private HealthReport testStyleAndUse(WSDLParser parser, String operationName) {
		HealthReport report;
		String style = parser.getStyle().toLowerCase();
		String use = "?";
		try {
			use = parser.getUse(operationName).toLowerCase();
			if (use.equals("literal") && style.equals("rpc")) {
				report = new HealthReport("Style and Use",
						"RPC/Literal is not officially supported by Taverna",
						Status.SEVERE);
			} else {
				report = new HealthReport("Style and Use", style + "/"
						+ use + " is OK", Status.OK);
			}
		} catch (UnknownOperationException e) {
			report = new HealthReport("Style and Use",
					"Unable to find use for operation:" + operationName,
					Status.SEVERE);
		}
		return report;
	}

	private HealthReport testEndpoint(WSDLParser parser, String operationName) {
		List<HealthReport> reports = new ArrayList<HealthReport>();
		List<String> endpoints = parser
				.getOperationEndpointLocations(operationName);
		for (String endpoint : endpoints) {
			URL url;
			try {
				url = new URL(endpoint);

				URLConnection connection = url.openConnection();
				if (connection instanceof HttpURLConnection) {
					int code = pingURL((HttpURLConnection) connection, 15000);
					if (code == 404) {
						reports
								.add(new HealthReport(
										"Endpoint test",
										"The endpoint ["
												+ endpoint
												+ "] responded, but a response code of "
												+ code, Status.WARNING));
					} else {
						reports
								.add(new HealthReport(
										"Endpoint test",
										"The endpoint ["
												+ endpoint
												+ "] responded, with a response code of "
												+ code, Status.OK));
					}

				}
			} catch (MalformedURLException e) {
				reports.add(new HealthReport("Endpoint test",
						"There was a problem with the endpoint[" + endpoint
								+ "] URL:" + e.getMessage(), Status.SEVERE));
			} catch (SocketTimeoutException e) {
				reports.add(new HealthReport("Endpoint test",
						"The endpoint[" + endpoint
								+ "] took more than 15 seconds to respond",
						Status.SEVERE));
			} catch (IOException e) {
				reports.add(new HealthReport("Endpoint test",
						"There was an error contacting the endpoint["
								+ endpoint + "]:" + e.getMessage(),
						Status.SEVERE));
			}
		}

		Status status = highestStatus(reports);
		if (reports.size()==1) {
			return reports.get(0);
		}
		else if (reports.size()==0) {
			return new HealthReport("Enpoint test","No service endpoint could be determined from the WSDL",Status.SEVERE);
		}
		else {
			return new HealthReport("Endpoint tests", "", status, reports);
		}
	}

}
