package org.apache.taverna.cwl.utilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

public class CWLUtilTestResource {
	private static JsonNode cwlFile;
	private static JsonNode cwlFile2;
	private static CWLUtil cwlUtil;
	private static CWLUtil cwlUtil2;
	private static JsonNode input1;
	private static JsonNode input2;

	private static void setup() {
		Yaml reader = new Yaml();
		ObjectMapper mapper = new ObjectMapper();
		cwlFile = mapper.valueToTree(reader.load(CWLUtilTestResource.class.getResourceAsStream("/customtool1.cwl")));
		cwlFile2 = mapper.valueToTree(reader.load(CWLUtilTestResource.class.getResourceAsStream("/customtool2.cwl")));

		cwlUtil = new CWLUtil(cwlFile);
		cwlUtil2 = new CWLUtil(cwlFile2);
		input1 = cwlFile.get("inputs").get(0);
		input2 = cwlFile2.get("inputs").get("input_2");
	}

	public static Object[] extractDescriptionResources() {
		setup();

		return new Object[] { new Object[] { cwlUtil, "this is a short description of input 1", input1 },
				new Object[] { cwlUtil, null, null },
				new Object[] { cwlUtil2, "this is a short description of input 1 cwl v1.0", input2 } };
	}

	public static Object[] processResources() {
		setup();
		HashMap<String, Integer> expected = new HashMap<>();
		expected.put("input_1", 0);
		expected.put("input_2", 1);
		expected.put("input_3", 0);
		Map<String, Integer> actual = cwlUtil.process(cwlFile.get("inputs"));

		HashMap<String, Integer> expected2 = new HashMap<>();
		expected2.put("output_1", 0);
		expected2.put("output_2", 0);
		Map<String, Integer> actual2 = cwlUtil.process((cwlFile.get("outputs")));

		HashMap<String, Integer> expected3 = new HashMap<>();
		expected3.put("input_1", 1);
		expected3.put("input_2", 1);
		expected3.put("input_3", 0);
		expected3.put("input_4", 1);
		expected3.put("input_5", 0);
		Map<String, Integer> actual3 = cwlUtil2.process((cwlFile2.get("inputs")));

		HashMap<String, Integer> expected4 = new HashMap<>();
		expected4.put("output_1", 0);
		Map<String, Integer> actual4 = cwlUtil2.process((cwlFile2.get("outputs")));
		return new Object[] { new Object[] { expected, actual }, new Object[] { expected2, actual2 },
				new Object[] { expected3, actual3 }, new Object[] { expected4, actual4 } };
	}

	public static Object[] isValidArrayTypeTrueResources() {
		setup();
		return new Object[] { new Object[] { cwlUtil, "int[]" } };
	}

	public static Object[] isValidArrayTypeFalseResources() {
		setup();
		return new Object[] { new Object[] { cwlUtil, "int []" }, new Object[] { cwlUtil, "blah[]" },
				new Object[] { cwlUtil, null } };
	}

	public static Object[] isValidDataTypeTrueResources() {
		setup();
		ObjectMapper mapper = new ObjectMapper();
		ArrayNode node = mapper.createArrayNode();
		node.add("int");
		node.add("null");
		node.add("float");
		node.add("string");
		node.add("double");
		node.add("int");
		node.add("file");
		node.add("boolean");
		node.add("directory");
		return new Object[] { cwlUtil, node };
	}

	public static Object[] isValidDataTypeFalseResources() {
		setup();
		ObjectMapper mapper = new ObjectMapper();
		ArrayNode node = mapper.createArrayNode();
		node.add("int");
		node.add("null");
		node.add("float");
		node.add("string");
		node.add("double");
		node.add("int");
		node.add("file");
		node.add("boolean");
		node.add("blah blah");
		return new Object[] { new Object[] { cwlUtil, node }, new Object[] { cwlUtil, null } };
	}

	public static Object[] processNameSpaceResources() {
		setup();

		return new Object[] { new Object[] { cwlUtil, "edam", "http://edamontology.org/", 1 },
				new Object[] { cwlUtil2, "edam2", "http://edamontologytest.org/", 2 } };
	}

	public static Object[] extractLabelResources() {
		setup();

		return new Object[] { new Object[] { cwlUtil, input1, "input 1 testing label" },
				new Object[] { cwlUtil, null, null },
				new Object[] { cwlUtil2, input2, "input 1 testing label cwl v1.0" } };

	}

	public static Object[] figureOutFormatsResources() {
		setup();
		PortDetail detail = new PortDetail();
		detail.setFormat(new ArrayList<String>());

		return new Object[] { new Object[] { cwlUtil, "edam:1245", detail, "http://edamontology.org/1245",0 },
				new Object[] { cwlUtil, "$formatExpression", detail, "$formatExpression",1 },
				new Object[] { cwlUtil2, "formatkey: not Defined", detail, "formatkey: not Defined",2 } };
	}
}
