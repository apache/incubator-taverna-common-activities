package org.apache.taverna.cwl.utilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;


public class Utility {
	
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
	private static final String LABEL = "label";
	private static final String FORMAT = "format";
	private LinkedHashMap nameSpace;
	private Map cwlFile;
	
	
	public Utility(Map cwlFile) {
		this.cwlFile = cwlFile;
		processNameSpace();
	}
	public void processNameSpace() {

		if (cwlFile.containsKey("$namespaces")) {
			nameSpace = (LinkedHashMap) cwlFile.get("$namespaces");
		}

	}
	
	public HashMap<String, Integer> processInputDepths(){
		return process(cwlFile.get(INPUTS));
	}
	public HashMap<String, Integer> processOutputDepths(){
		return process(cwlFile.get(OUTPUTS));
	}
	public  HashMap<String, PortDetail> processInputDetails(){
		return processdetails(cwlFile.get(INPUTS));
	}
	public  HashMap<String, PortDetail> processOutputDetails(){
		return processdetails(cwlFile.get(OUTPUTS));
	}
	private HashMap<String, Integer> process(Object inputs) {

		HashMap<String, Integer> result = new HashMap<>();

		if (inputs.getClass() == ArrayList.class) {

			for (Map input : (ArrayList<Map>) inputs) {
				String currentInputId = (String) input.get(ID);


				Object typeConfigurations;
				try {

					typeConfigurations = input.get(TYPE);
					// if type :single argument
					if (typeConfigurations.getClass() == String.class) {
						

						result.put(currentInputId, DEPTH_0);
						// type : defined as another map which contains type:
					} else if (typeConfigurations.getClass() == LinkedHashMap.class) {
						String inputType = (String) ((Map) typeConfigurations).get(TYPE);
						if (inputType.equals(ARRAY)) {
							result.put(currentInputId, DEPTH_1);

						}
					} else if (typeConfigurations.getClass() == ArrayList.class) {
						if (isValidDataType((ArrayList) typeConfigurations)) {
							result.put(currentInputId, DEPTH_0);
						}

					}

				} catch (ClassCastException e) {

					System.out.println("Class cast exception !!!");
				}

			}
		} else if (inputs.getClass() == LinkedHashMap.class) {
			for (Object parameter : ((Map) inputs).keySet()) {
				if (parameter.toString().startsWith("$"))
					System.out.println("Exception");
			}
		}
		return result;
	}
	private HashMap<String, PortDetail> processdetails(Object inputs) {

		HashMap<String, PortDetail> result = new HashMap<>();

		if (inputs.getClass() == ArrayList.class) {

			for (Map input : (ArrayList<Map>) inputs) {
				PortDetail detail = new PortDetail();
				String currentInputId = (String) input.get(ID);

				extractDescription(input, detail);

				extractFormat(input, detail);

				extractLabel(input, detail);
				result.put(currentInputId, detail);
				

			}
		} else if (inputs.getClass() == LinkedHashMap.class) {
			for (Object parameter : ((Map) inputs).keySet()) {
				if (parameter.toString().startsWith("$"))
					System.out.println("Exception");
			}
		}
		return result;
	}
	private void extractLabel(Map input, PortDetail detail) {
		if (input != null)
			if (input.containsKey(LABEL)) {
				detail.setLabel((String) input.get(LABEL));
			} else {
				detail.setLabel(null);
			}
	}

	private void extractDescription(Map input, PortDetail detail) {
		if (input != null)
			if (input.containsKey(DESCRIPTION)) {
				detail.setDescription((String) input.get(DESCRIPTION));
			} else {
				detail.setDescription(null);
			}
	}

	private void extractFormat(Map input, PortDetail detail) {
		if (input != null)
			if (input.containsKey(FORMAT)) {

				Object formatInfo = input.get(FORMAT);

				ArrayList<String> format = new ArrayList<>();
				detail.setFormat(format);

				if (formatInfo.getClass() == String.class) {

					extractThisFormat(formatInfo.toString(), detail);
				} else if (formatInfo.getClass() == ArrayList.class) {
					for (Object eachFormat : (ArrayList) formatInfo) {
						extractThisFormat(eachFormat.toString(), detail);
					}
				}

			}
	}

	private void extractThisFormat(String formatInfoString, PortDetail detail) {
		if (formatInfoString.startsWith("$")) {

			 detail.addFormat(formatInfoString);
		} else if (formatInfoString.contains(":")) {
			String format[] = formatInfoString.split(":");
			String namespaceKey = format[0];
			String urlAppednd = format[1];
			if (!nameSpace.isEmpty()) {
				if (nameSpace.containsKey(namespaceKey))
					detail.addFormat(nameSpace.get(namespaceKey) + urlAppednd);
				else

					detail.addFormat(formatInfoString);
			} else {
				 detail.addFormat(formatInfoString);
			}
		} else {
			 detail.addFormat(formatInfoString);
		}
	}
	public boolean isValidDataType(ArrayList typeConfigurations) {
		for (Object type : typeConfigurations) {
			if (!(((String) type).equals(FLOAT) || ((String) type).equals(NULL)))
				return false;
		}
		return true;
	}
}
