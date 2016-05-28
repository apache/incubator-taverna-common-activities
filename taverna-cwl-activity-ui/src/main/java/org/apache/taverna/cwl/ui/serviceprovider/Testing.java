package org.apache.taverna.cwl.ui.serviceprovider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;
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
				if (isTavernaCompatible(cwlFile))
					System.out.println(file.getName().split("\\.")[0]);
			}

		}
	}

	public static boolean isTavernaCompatible(Map cwlFile) {

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
				else return false;
			}
		}
		return true;

	}

	private static void getInputs(CwlActivityConfigurationBean configurationBean) {

		System.out.println(i);
		i++;
		Map cwlFile = configurationBean.getCwlConfigurations();

		for (Object mainKey : cwlFile.keySet()) {

			if (mainKey.equals(INPUTS)) {
				ArrayList<Map> arrayList = (ArrayList<Map>) cwlFile.get(mainKey);

				HashMap<String, Type> map = processInputs(arrayList);
				for (String s : map.keySet()) {
					if (map.get(s).getType().equals(FILE))
						System.out.println("ID: " + s + " type : File");

					if (map.get(s).getType().equals(ARRAY))
						System.out.println("ID :" + s + " type: Array items: " + map.get(s).getItems());
				}
			}
		}

	}

	private static HashMap<String, Type> processInputs(ArrayList<Map> inputs) {

		HashMap<String, Type> result = new HashMap<>();
		for (Map input : inputs) {
			String currentInputId = (String) input.get(ID);
			Map typeConfigurations;
			Type type = null;

			try {

				typeConfigurations = (Map) input.get(TYPE);

				if (typeConfigurations != null) {
					type = new Type();
					type.setType((String) typeConfigurations.get(TYPE));
					type.setItems((String) typeConfigurations.get(ITEMS));
				}
			} catch (ClassCastException e) {
				// This exception means type is described as single argument ex:
				// type : File

				type = new Type();
				type.setType((String) input.get(TYPE));
				type.setItems(null);
			} catch (NullPointerException e) {

				System.out.println("No type is defined");
			}

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
