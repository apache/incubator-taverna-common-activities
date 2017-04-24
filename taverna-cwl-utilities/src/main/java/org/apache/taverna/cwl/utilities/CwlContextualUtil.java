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

package org.apache.taverna.cwl.utilities;

import java.util.ArrayList;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;

public class CwlContextualUtil extends CWLUtil {
	

	public CwlContextualUtil(JsonNode cwlFile) {
		super(cwlFile);
	}

	/**
	 * This method creates HTML representation of the String paragraph
	 * 
	 * @param summary
	 * @param paragraph
	 * @return
	 */
	public String paragraphToHtml(String summary, String paragraph) {

		summary += "<tr><td colspan='2' align='left'>";
		
		for (String line : paragraph.split("[\n|\r]"))
			summary += "<p>" + line + "</p>";

		summary += "</td></tr>";

		return summary;
	}

	/**
	 * This method creates the HTML tags and details of each input/output for
	 * service detail panel
	 * 
	 * @param summary
	 *            current String summary
	 * @param id
	 *            input/output Id
	 * @param detail
	 *            PortDetail object of the input/output
	 * @param depth
	 *            depth of the input/output
	 * @return
	 */
	public String extractSummary(String summary, String id, PortDetail detail, int depth) {
		summary += "<tr align='left'><td> ID: " + id + " </td><td>Depth: " + depth + "</td></tr>";
		if (detail.getLabel() != null) {
			summary += "<tr><td  align ='left' colspan ='2'>Label: " + detail.getLabel() + "</td></tr>";
		}
		if (detail.getDescription() != null) {

			summary = paragraphToHtml(summary, detail.getDescription());

		}
		if (detail.getFormat() != null) {
			summary += "<tr><td  align ='left' colspan ='2'>Format: ";
			ArrayList<String> formats = detail.getFormat();

			int Size = formats.size();

			if (Size == 1) {
				summary += formats.get(0);
			} else {
				for (int i = 0; i < (Size - 1); i++) {
					summary += formats.get(i) + ", ";
				}
				summary += formats.get(Size - 1);
			}
			summary += "</td></tr>";
		}
		summary += "<tr></tr>";
		return summary;
	}

	

	public String setUpInputDetails(String summary) {

		Map<String, PortDetail> inputs = processInputDetails();
		Map<String, Integer> inputDepths = processInputDepths();
		return extracSummaries(summary, inputs, inputDepths);
	}

	public String setUpOutputDetails(String summary) {
		Map<String, PortDetail> outPuts = processOutputDetails();
		Map<String, Integer> outputDepths = processOutputDepths();
		return extracSummaries(summary, outPuts, outputDepths);
	}
/**
 * 
 * @param summary current String 
 * @param parameters  respective PortDetail Object which hold the label, description  
 * @param parameterDepths Map containing parameter Id and the corresponding depth
 * @return
 */
	public String extracSummaries(String summary, Map<String, PortDetail> parameters,
			Map<String, Integer> parameterDepths) {
		if ((parameters != null && !parameters.isEmpty()) && (parameterDepths != null && !parameterDepths.isEmpty()))
			for (String id : parameters.keySet()) {
				PortDetail detail = parameters.get(id);
				if (parameterDepths.containsKey(id))
					summary = extractSummary(summary, id, detail, parameterDepths.get(id));
			}
		return summary;
	}
}
