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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.yaml.snakeyaml.Yaml;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CWLUtilTest {
	JsonNode cwlFile;
	CWLUtil cwlUtil;
	JsonNode input;

	@Before
	public void setUp() throws Exception {
		Yaml reader = new Yaml();
		ObjectMapper mapper = new  ObjectMapper();
		cwlFile = mapper.valueToTree(reader.load(new FileInputStream(CWLUtilTest.class.getResource("/CWLFiles/customtool1.cwl").getPath()))); 

		cwlUtil = new CWLUtil(cwlFile);
		input =  cwlFile.get("inputs").get(0);
	}

	@Test
	public void processNameSpaceTest() {
		JsonNode nameSpace = cwlUtil.getNameSpace();

		assertEquals(1, nameSpace.size());
		assertTrue(nameSpace.has("edam"));
		assertEquals("http://edamontology.org/", nameSpace.get("edam").asText());
	}

	@Test
	public void extractLabelTest() {
		PortDetail detail = new PortDetail();

		cwlUtil.extractLabel(null, detail);
		assertEquals(null, detail.getLabel());

		cwlUtil.extractLabel(input, detail);
		assertEquals("input 1 testing label", detail.getLabel());

	}

	@Test
	public void extractDescriptionTest() {
		PortDetail detail = new PortDetail();

		cwlUtil.extractDescription(null, detail);
		assertEquals(null, detail.getDescription());

		cwlUtil.extractDescription(input, detail);
		assertEquals("this is a short description of input 1", detail.getDescription());

	}

	@Test
	public void figureOutFormatsTest() {
		PortDetail detail = new PortDetail();
		detail.setFormat(new ArrayList<String>());
		cwlUtil.figureOutFormats("edam:1245", detail);
		assertEquals("http://edamontology.org/1245", detail.getFormat().get(0));

		cwlUtil.figureOutFormats("$formatExpression", detail);
		assertEquals("$formatExpression", detail.getFormat().get(1));

		// format that doesn't defined in the name space

		cwlUtil.figureOutFormats("formatkey: not Defined", detail);
		assertEquals("formatkey: not Defined", detail.getFormat().get(2));
	}

	@Test
	public void processTest() {

		Map<String, Integer> actual = cwlUtil.process( cwlFile.get("inputs"));

		HashMap<String, Integer> expected = new HashMap<>();
		expected.put("input_1", 0);
		expected.put("input_2", 1);
		expected.put("input_3", 0);
		for (Map.Entry<String, Integer> input : expected.entrySet()) {
			assertEquals(input.getValue(), actual.get(input.getKey()));
		}
		
		actual = cwlUtil.process((cwlFile.get("outputs")));
		expected = new HashMap<>();
		expected.put("output_1", 0);
		expected.put("output_2", 0);
		for (Map.Entry<String, Integer> input : expected.entrySet()) {
			assertEquals(input.getValue(), actual.get(input.getKey()));
		}
	}
}
