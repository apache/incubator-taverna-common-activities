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
import java.util.List;
import java.util.Map;

import javax.swing.Icon;

import org.apache.taverna.cwl.Type;
import org.yaml.snakeyaml.Yaml;

import net.sf.taverna.t2.servicedescriptions.ServiceDescriptionProvider;

public class CwlServiceProvider implements ServiceDescriptionProvider {

	private static final File cwlFilesLocation = new File("CWLFiles");
	// CWLTYPES
	private static final String FILE = "File";
	private static final String INTEGER = "int";
	private static final String DOUBLE = "double";
	private static final String FLOAT = "float";
	private static final String STRING = "string";

	private static final String ITEMS = "items";
	private static final String TYPE = "type";
	private static final String ID = "id";
	private static final String INPUTS = "inputs";
	private static final String ARRAY = "array";

	@Override
	public void findServiceDescriptionsAsync(FindServiceDescriptionsCallBack callBack) {

		// This is holding the CWL configuration beans
		List<CwlServiceDesc> result = new ArrayList<CwlServiceDesc>();

		File[] cwlFiles = getCwlFiles();

		// Load the CWL file using SnakeYaml lib
		Yaml cwlReader = new Yaml();

		for (File file : cwlFiles) {
			Map cwlFile = null;

			try {
				cwlFile = (Map) cwlReader.load(new FileInputStream(file));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			if (cwlFile != null) {
				// Creating CWl service Description
				CwlServiceDesc cwlServiceDesc = new CwlServiceDesc();
				cwlServiceDesc.setCwlConfiguration(cwlFile);
				// add to the result
				result.add(cwlServiceDesc);
				// return the service description
				callBack.partialResults(result);
			}

		}
		callBack.finished();

	}

	private HashMap<String, Type> processInputs(ArrayList<Map> inputs) {

		HashMap<String, Type> result = new HashMap<>();

		for (Map input : inputs) {

			String Id = (String) input.get(ID);
			// This require for nested type definitions
			Map typeConfigurations;
			// this object holds the type and if it's an array then type of the
			// elements in the array
			Type type = null;
			try {
				/*
				 * This part will go through nested type definitions
				 * 
				 * type : type : array items : boolean
				 * 
				 */

				typeConfigurations = (Map) input.get(TYPE);

				// Check type is defined or not
				if (typeConfigurations != null) {
					type = new Type();
					type.setType((String) typeConfigurations.get(TYPE));
					type.setItems((String) typeConfigurations.get(ITEMS));

				}

			} catch (ClassCastException e) {
				/*
				 * This exception means type is described as single argument ex:
				 * type : File
				 */
				type = new Type();
				type.setType((String) input.get(TYPE));
				type.setItems(null);
			}
			//when processing the inputs from the HashMap  type should be checked for is it null or not 
				result.put(Id, type);
		}
		return result;
	}

	public boolean isTavernaCompatible(Map cwlFile) {

	
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

	@Override
	public Icon getIcon() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	private File[] getCwlFiles() {
		// Get the .cwl files in the directory using the FileName Filter
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
