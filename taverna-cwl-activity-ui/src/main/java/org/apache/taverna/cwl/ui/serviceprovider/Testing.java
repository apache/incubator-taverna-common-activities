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
package org.apache.taverna.cwl.ui.serviceprovider;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.jena.atlas.iterator.Iter;
import org.springframework.util.SystemPropertyUtils;
import org.yaml.snakeyaml.Yaml;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class Testing {
	private static final String INPUTS = "inputs";
	private static final String ID = "id";
	private static final String TYPE = "type";
	private static final String ARRAY = "array";
	private static final String ITEMS = "items";
	static int i = 0;

	private static final int DEPTH_0 = 0;
	private static final int DEPTH_1 = 1;
	private static final int DEPTH_2 = 2;
	private static final String LABEL = "label";

	public static void main(String[] args) {
		
		Yaml reader = new Yaml();
		Map cwlFile = null;
		try {
			cwlFile = (Map) reader.load(new FileInputStream(new File(Testing.class.getResource("/CWLFiles/customtool1.cwl").getPath())));
			
		} catch (IOException e) {
			
			
		}
	
		ObjectMapper mapper = new ObjectMapper();
		JsonNode node = mapper.createObjectNode();
		JsonNode node2 =mapper.valueToTree(cwlFile);
		System.out.println(node2.get("class"));
//
//		((ObjectNode )node).put("toolName","asdsa");((ObjectNode )node).put("asda", node2);
////		System.out.println(((ObjectNode )node).path("asda"));
//			Iterator<JsonNode> i=	node.get("asda").get(INPUTS).iterator();
//			
//			
////		System.out.println(node.get("asda").get(INPUTS).get(2).get("type"));
//		
//		for(JsonNode nodse :node.get("asda").get(INPUTS).get(2).get("type") )System.out.println(nodse.asText());
//		
	}
}
