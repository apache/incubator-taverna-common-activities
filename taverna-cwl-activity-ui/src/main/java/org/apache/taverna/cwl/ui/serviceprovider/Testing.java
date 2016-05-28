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
	private static final String FILE = "File";
	private static final String ITEMS = "items";
	static int i=0;
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
		
		System.out.println(i);
		i++;
		Map cwlFile = configurationBean.getCwlConfigurations();

		for (Object mainKey : cwlFile.keySet()) {

			if (mainKey.equals(INPUTS)) {
				ArrayList<Map> arrayList = (ArrayList<Map>) cwlFile.get(mainKey);
				
				HashMap<String, Type> map = processInputs(arrayList);
				for (String s:map.keySet()  ) {
					if(map.get(s).getType().equals(FILE)) System.out.println("ID: "+s+" type : File");
					
					if(map.get(s).getType().equals(ARRAY)) System.out.println("ID :"+s+" type: Array items: "+map.get(s).getItems());
				}
			}
		}

	}

	private static HashMap<String, Type> processInputs(ArrayList<Map> inputs) {

		HashMap<String, Type> result = new HashMap<>();
		for (Map input : inputs) {
			String currentInputId = (String) input.get(ID);
			Map typeConfigurations;
			Type type = new Type();
			try {
				typeConfigurations = (Map) input.get(TYPE);
				type.setType((String) typeConfigurations.get(TYPE));
				type.setItems((String) typeConfigurations.get(ITEMS));
			} catch (ClassCastException e) {
				// This exception means type is described as single argument ex:
				// type : File
				type.setType((String)input.get(TYPE));
				type.setItems(null);
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
