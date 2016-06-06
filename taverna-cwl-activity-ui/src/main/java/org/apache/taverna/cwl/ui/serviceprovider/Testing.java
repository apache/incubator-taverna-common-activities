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
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.taverna.cwl.CwlActivityConfigurationBean;
import org.yaml.snakeyaml.Yaml;

public class Testing {
	private static final File cwlFilesLocation = new File("CWLFiles");
	private static final String INPUTS = "inputs";
	private static final String ID = "id";
	private static final String TYPE = "type";
	private static final String ARRAY = "array";
	private static final String ITEMS = "items";
	static int i = 0;

	
	private static final int DEPTH_0 = 0;
	private static final int DEPTH_1 = 1;
	private static final int DEPTH_2 = 2;

//	public static void main(String[] args) {
//		File[] cwlFiles = getCwlFiles();
//
//		for (File file : cwlFiles) {
//
//			Map cwlFile = null;
//			// Load the CWL file using SnakeYaml lib
//			Yaml cwlReader = new Yaml();
//			try {
//				cwlFile = (Map) cwlReader.load(new FileInputStream(file));
//				System.out.println(file.getName().split("\\.")[0]);
//				HashMap<String, Integer> map =processInputs(cwlFile);
//				for (String s : map.keySet()) {
//					//System.out.println(map.get(s));
//				}
//			} catch (FileNotFoundException e) {
//				e.printStackTrace();
//			}
//			if (cwlFile != null) {
//				// Creating the CWL configuration bean
//				CwlActivityConfigurationBean configurationBean = new CwlActivityConfigurationBean();
//				configurationBean.setCwlConfigurations(cwlFile);
//			
//				
//			}
//
//		}
//		
//	
//	}

	private static HashMap<String, Integer> processInputs(Map cwlFile) {
		
		HashMap<String, Integer> result = new HashMap<>();
		
		// Get all input objects in
		Object inputs = cwlFile.get(INPUTS);
		if(inputs.getClass()==ArrayList.class){
			
			
			for (Map input :( ArrayList<Map>)inputs) {
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
						if (inputType.equals(ARRAY)){
							result.put(currentInputId, DEPTH_1);
						}
					}

				} catch (ClassCastException e) {

					System.out.println("Class cast exception !!!");
				}

			}
		}else if(inputs.getClass()==LinkedHashMap.class){
			
			for (Object parameter : ((Map) inputs).keySet()) {
				if(parameter.toString().startsWith("$")) System.out.println("Exception");
			}
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
