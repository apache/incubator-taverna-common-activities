package org.apache.taverna.cwl.ui.serviceprovider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

public class Testing {
	private static final File cwlFilesLocation = new File("CWLFiles");
	private static final String INPUTS = "inputs";
	private static final String ID = "id";
	private static final String TYPE = "type";
	private static final String ARRAY = "array";
	private static final String FILE = "File";

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
				getInputs(configurationBean);
			}

		}
	}

	private static void getInputs(CwlActivityConfigurationBean configurationBean) {

		Map cwlFile = configurationBean.getCwlConfigurations();

		for (Object mainKey : cwlFile.keySet()) {

			if (mainKey.equals(INPUTS)) {
				ArrayList<Map> arrayList = (ArrayList<Map>) cwlFile.get(mainKey);
				processInputs(arrayList);
			}
		}

	}

	private static HashMap<String, String> processInputs(ArrayList<Map> inputs) {

		HashMap<String, String> result = new HashMap<>();
		for (Map input : inputs) {
			String currentInputId = (String) input.get(ID);
			System.out.println(currentInputId);
			Map typeConfigurations;
			String type;
			try {
				typeConfigurations = (Map) input.get(TYPE);
				type = (String) typeConfigurations.get(TYPE);
				System.out.println(type);
			} catch (ClassCastException e) {
				// This exception means type is described as single argument ex:
				// type : File
				type = (String) input.get(TYPE);
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
