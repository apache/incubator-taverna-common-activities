package org.apache.taverna.cwl.utilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

public class CWLUtil {

	private static final String INPUTS = "inputs";
	private static final String OUTPUTS = "outputs";
	private static final String ID = "id";
	private static final String TYPE = "type";
	private static final String ARRAY = "array";
	private static final String DESCRIPTION = "description";
	private static final int DEPTH_0 = 0;
	private static final int DEPTH_1 = 1;
	private static final int DEPTH_2 = 2;

	private static final String FLOAT = "float";
	private static final String NULL = "null";
	private static final String BOOLEAN = "boolean";
	private static final String INT = "int";
	private static final String DOUBLE = "double";
	private static final String STRING = "string";
	private static final String LABEL = "label";
	private static final String FILE = "file";
	private static final String FORMAT = "format";
	private JsonNode nameSpace;
	private JsonNode cwlFile;

	public CWLUtil(JsonNode cwlFile) {
		this.cwlFile = cwlFile;
		processNameSpace();
	}

	public JsonNode getNameSpace() {
		return nameSpace;
	}

	public void processNameSpace() {

		if (cwlFile.has("$namespaces")) {
			nameSpace = cwlFile.path("$namespaces");
		}

	}

	public HashMap<String, Integer> processInputDepths() {
		return process(cwlFile.get(INPUTS));
	}

	public HashMap<String, Integer> processOutputDepths() {
		return process(cwlFile.get(OUTPUTS));
	}

	public HashMap<String, PortDetail> processInputDetails() {
		return processdetails(cwlFile.get(INPUTS));
	}

	public HashMap<String, PortDetail> processOutputDetails() {
		return processdetails(cwlFile.get(OUTPUTS));
	}

	public HashMap<String, Integer> process(JsonNode inputs) {

		HashMap<String, Integer> result = new HashMap<>();

		if (inputs.getClass() == ArrayNode.class) {
			Iterator<JsonNode> iterator = inputs.iterator();

			while (iterator.hasNext()) {
				JsonNode input = iterator.next();
				String currentInputId =  input.get(ID).asText();

				JsonNode typeConfigurations;
				try {

					typeConfigurations = input.get(TYPE);
					// if type :single argument
					if (typeConfigurations.getClass() == TextNode.class) {

						result.put(currentInputId, DEPTH_0);
						// type : defined as another map which contains type:
					} else if (typeConfigurations.getClass() == ObjectNode.class) {
						String inputType = typeConfigurations.get(TYPE).asText();
						if (inputType.equals(ARRAY)) {
							result.put(currentInputId, DEPTH_1);

						}
					} else if (typeConfigurations.getClass() == ArrayNode.class) {
						if (isValidDataType(typeConfigurations)) {
							result.put(currentInputId, DEPTH_0);
						}

					}

				} catch (ClassCastException e) {

					System.out.println("Class cast exception !!!");
				}

			}
		} else if (inputs.getClass() == ObjectNode.class) {
			for (JsonNode parameter :inputs) {
				if (parameter.asText().startsWith("$"))
					System.out.println("Exception");
			}
			
			
			
		}
		return result;
	}

	private HashMap<String, PortDetail> processdetails(JsonNode inputs) {

		HashMap<String, PortDetail> result = new HashMap<>();

		if (inputs.getClass() == ArrayNode.class) {

			for (JsonNode input :inputs) {
				PortDetail detail = new PortDetail();
				String currentInputId = input.get(ID).asText();

				extractDescription(input, detail);

				extractFormat(input, detail);

				extractLabel(input, detail);
				result.put(currentInputId, detail);

			}
		} else if (inputs.getClass() == ObjectNode.class) {
			for (JsonNode parameter :inputs) {
				if (parameter.asText().startsWith("$"))
					System.out.println("Exception");
			}
		}
		return result;
	}

	public void extractLabel(JsonNode input, PortDetail detail) {
		if (input != null)
			if (input.has(LABEL)) {
				detail.setLabel(input.get(LABEL).asText());
			} else {
				detail.setLabel(null);
			}
	}

	public void extractDescription(JsonNode input, PortDetail detail) {
		if (input != null)
			if (input.has(DESCRIPTION)) {
				detail.setDescription(input.get(DESCRIPTION).asText());
			} else {
				detail.setDescription(null);
			}
	}

	public void extractFormat(JsonNode input, PortDetail detail) {
		if (input != null)
			if (input.has(FORMAT)) {

				JsonNode formatInfo = input.get(FORMAT);

				ArrayList<String> format = new ArrayList<>();
				detail.setFormat(format);

				if (formatInfo.getClass() == TextNode.class) {

					figureOutFormats(formatInfo.asText(), detail);
				} else if (formatInfo.getClass() == ArrayNode.class) {
					for (JsonNode eachFormat : formatInfo) {
						figureOutFormats(eachFormat.asText(), detail);
					}
				}

			}
	}

	public void figureOutFormats(String formatInfoString, PortDetail detail) {
		if (formatInfoString.startsWith("$")) {

			detail.addFormat(formatInfoString);
		} else if (formatInfoString.contains(":")) {
			String format[] = formatInfoString.split(":");
			String namespaceKey = format[0];
			String urlAppednd = format[1];
		
				if (nameSpace.has(namespaceKey))
					detail.addFormat(nameSpace.get(namespaceKey).asText() + urlAppednd);
				else
					// can't figure out the format
					detail.addFormat(formatInfoString);
		
		} else {
			// can't figure out the format
			detail.addFormat(formatInfoString);
		}
	}

	public boolean isValidDataType(JsonNode typeConfigurations) {
		for (JsonNode  type : typeConfigurations) {
			if (!( type.asText().equals(FLOAT) ||  type.asText().equals(NULL) ||  type.asText().equals(BOOLEAN)
					||  type.asText().equals(INT) ||  type.asText().equals(STRING) ||  type.asText().equals(DOUBLE)
					||  type.asText().equals(FILE)))
				return false;
		}
		return true;
	}
}
