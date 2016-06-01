package org.apache.taverna.cwl.ui.serviceprovider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.taverna.cwl.CwlActivityConfigurationBean;
import org.apache.taverna.cwl.Type;
import org.yaml.snakeyaml.Yaml;

public class Testing {
	private static final File cwlFilesLocation = new File("CWLFiles");
	private static final String INPUTS = "inputs";
	private static final String ID = "id";
	private static final String TYPE = "type";
	private static final String ARRAY = "array";
	private static final String ITEMS = "items";
	static int i = 0;

	// CWLTYPES
	private static final String FILE = "File";
	private static final String INTEGER = "int";
	private static final String DOUBLE = "double";
	private static final String FLOAT = "float";
	private static final String STRING = "string";

	public static void main(String[] args) {
		File[] cwlFiles = getCwlFiles();

		for (File file : cwlFiles) {

			Map cwlFile = null;
			// Load the CWL file using SnakeYaml lib
			Yaml cwlReader = new Yaml();
			try {
				cwlFile = (Map) cwlReader.load(new FileInputStream(file));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			if (cwlFile != null) {
				// Creating the CWL configuration bean
				CwlActivityConfigurationBean configurationBean = new CwlActivityConfigurationBean();
				configurationBean.setCwlConfigurations(cwlFile);
				// getInputs(configurationBean);
				if (isCompatible(cwlFile))
					System.out.println(file.getName().split("\\.")[0]);
			}

		}
	}

	public static boolean isCompatible(Map cwlFile) {

		/*
		 * in this method cwl tool is verified whether it's compatible with
		 * Taverna or not
		 */
		ArrayList<Map> inputs = (ArrayList<Map>) cwlFile.get(INPUTS);

		if (inputs != null) {

			HashMap<String, Type> processedinputs = processInputs(inputs);

			for (String inputId : processedinputs.keySet()) {
				Type type = processedinputs.get(inputId);
				if (type != null)
					if (type.getItems() == null) {
						String inputType = type.getType();
						if (!(inputType.equals(DOUBLE) || inputType.equals(FILE) || inputType.equals(FLOAT)
								|| inputType.equals(INTEGER) || inputType.equals(STRING)))
							return false;

					} else {
						if (type.getType().equals(ARRAY)) {
							String inputType = type.getItems();
							if (!(inputType.equals(DOUBLE) || inputType.equals(FILE) || inputType.equals(FLOAT)
									|| inputType.equals(INTEGER) || inputType.equals(STRING)))
								return false;

						}
					}
				else
					return false;
			}
		}
		return true;

	}

	private static HashMap<String, Type> processInputs(ArrayList<Map> inputs) {

		HashMap<String, Type> result = new HashMap<>();
		for (Map input : inputs) {
			String currentInputId = (String) input.get(ID);
			Object typeConfigurations;
			Type type = null;

			try {

				typeConfigurations = input.get(TYPE);
				//if type :single argument
				if (typeConfigurations.getClass() == String.class) {
					type = new Type();
					type.setType((String) typeConfigurations);
					type.setItems(null);
					
					//type : defined as another map which contains type: 
				} else if (typeConfigurations.getClass() == LinkedHashMap.class) {
					
					type = new Type();
					type.setType((String) ((Map) typeConfigurations).get(TYPE));
					type.setItems((String) ((Map) typeConfigurations).get(ITEMS));
				}

			} catch (ClassCastException e) {

				System.out.println("Class cast exception !!!");
			}
			if (type != null)//see whether type is defined or not
				result.put(currentInputId, type);

		}
		return result;
	}

	private static File[] getCwlFiles() {
		// Get the cwl files in the directory using the FileName Filter
		FilenameFilter fileNameFilter = new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				if (name.lastIndexOf('.') > 0) {
					// get last index for '.' char
					int lastIndex = name.lastIndexOf('.');

					// get extension
					String str = name.substring(lastIndex);

					// match path name extension
					if (str.equals(".cwl")) {
						return true;
					}
				}
				return false;
			}
		};

		return cwlFilesLocation.listFiles(fileNameFilter);
	}
}
