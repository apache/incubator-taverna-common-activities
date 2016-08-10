/*******************************************************************************
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *     contributor license agreements.  See the NOTICE file distributed with
 *     this work for additional information regarding copyright ownership.
 *     The ASF licenses this file to You under the Apache License, Version 2.0
 *     (the "License"); you may not use this file except in compliance with
 *     the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *******************************************************************************/
package org.apache.taverna.cwl.utilities;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.yaml.snakeyaml.Yaml;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CwlContextualUtilTest {
	JsonNode cwlFile;
	CwlContextualUtil cwlContextualUtil;
	JsonNode input;

	@Before
	public void setUp() throws Exception {
		Yaml reader = new Yaml();
		ObjectMapper mapper = new ObjectMapper();
		cwlFile = mapper.valueToTree(reader.load(getClass().getResourceAsStream("/customtool1.cwl")));
		cwlContextualUtil = new CwlContextualUtil(cwlFile);
	}

	@Test
	public void paragraphToHtmlTest() {
		String summary = "";
		String para = "This is for just testing purposese******************* ****************************\n"
				+ "********************************** *5**************************** ******** ******\n";
		String expected = "<tr><td colspan='2' align='left'><p>This is for just testing purposese******************* ****************************</p><p>********************************** *5**************************** ******** ******</p></td></tr>";
		assertEquals(expected, cwlContextualUtil.paragraphToHtml(summary, para));
	}

	@Test
	public void extractSummaryTest() {
		String summary = "";
		String id = "parameter_1";
		int depth = 0;
		PortDetail detail = new PortDetail();
		detail.setLabel("Test Label");
		detail.setDescription("Test Description");
		String expected ="<tr align='left'><td> ID: parameter_1 </td><td>Depth: 0</td></tr><tr><td  align ='left' colspan ='2'>Label: Test Label</td></tr><tr><td colspan='2' align='left'><p>Test Description</p></td></tr><tr></tr>";
		assertEquals(expected, cwlContextualUtil.extractSummary(summary, id, detail, depth));
	}
	
}
