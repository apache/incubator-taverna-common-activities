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

package org.apache.taverna.activities.wsdl.xmlsplitter;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import net.sf.taverna.t2.visit.VisitReport;
import net.sf.taverna.t2.visit.VisitReport.Status;
import net.sf.taverna.t2.workflowmodel.health.HealthCheck;
import net.sf.taverna.t2.workflowmodel.health.HealthChecker;
import org.apache.taverna.wsdl.parser.TypeDescriptor;
import org.apache.taverna.wsdl.xmlsplitter.XMLSplitterSerialisationHelper;

import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

public class XMLInputSplitterHealthChecker implements HealthChecker<XMLInputSplitterActivity> {

	public boolean canVisit(Object subject) {
		return subject!=null && subject instanceof XMLInputSplitterActivity;
	}

	public VisitReport visit(XMLInputSplitterActivity activity, List<Object> ancestors) {
		Element element;
		try {
			String wrappedType = activity.getConfiguration().get("wrappedType").textValue();
			element = new SAXBuilder().build(new StringReader(wrappedType)).getRootElement();
		} catch (JDOMException | IOException e) {
			return new VisitReport(HealthCheck.getInstance(), activity, "Error reading wrapped type", HealthCheck.INVALID_CONFIGURATION, Status.SEVERE);
		}
		TypeDescriptor typeDescriptor = XMLSplitterSerialisationHelper.extensionXMLToTypeDescriptor(element);
		if (typeDescriptor==null) {
			return new VisitReport(HealthCheck.getInstance(), activity, "Unknown datatype for port", HealthCheck.NULL_DATATYPE, Status.SEVERE);
		}
		else {
			return new VisitReport(HealthCheck.getInstance(), activity, "Recognized datatype", HealthCheck.NO_PROBLEM, Status.OK);
		}
	}

	public boolean isTimeConsuming() {
		return false;
	}

}
