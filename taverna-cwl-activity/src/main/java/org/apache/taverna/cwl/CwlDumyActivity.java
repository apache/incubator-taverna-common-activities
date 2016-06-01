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
package org.apache.taverna.cwl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.taverna.reference.T2Reference;
import org.apache.taverna.workflowmodel.processor.activity.AbstractAsynchronousActivity;
import org.apache.taverna.workflowmodel.processor.activity.ActivityConfigurationException;
import org.apache.taverna.workflowmodel.processor.activity.AsynchronousActivity;
import org.apache.taverna.workflowmodel.processor.activity.AsynchronousActivityCallback;


public class CwlDumyActivity extends AbstractAsynchronousActivity<CwlActivityConfigurationBean>
		implements AsynchronousActivity<CwlActivityConfigurationBean> {

	private static final String INPUTS = "inputs";
	private static final String ID = "id";
	private static final String TYPE = "type";
	private static final String ARRAY = "array";

	private static final String ITEMS = "items";
	
	//CWLTYPES
	private static final String FILE = "File";
	private static final String INTEGER="int";
	private static final String DOUBLE="double";
	private static final String FLOAT="float";
	private static final String STRING="string";
	
	@Override
	public void configure(CwlActivityConfigurationBean configurationBean) throws ActivityConfigurationException {
		removeInputs();
		removeOutputs();
		Map cwlFile = configurationBean.getCwlConfigurations();

		// Get all input objects in
		ArrayList<Map> inputs = (ArrayList<Map>) cwlFile.get(INPUTS);

		HashMap<String, Type> processedInputs;

		if (inputs != null) {
			processedInputs = processInputs(inputs);

			for (String inputId : processedInputs.keySet()) {
				if (processedInputs.get(inputId).getType().equals(FILE))
					addInput(inputId, 0, true, null, String.class);
				if (processedInputs.get(inputId).getType().equals(ARRAY))
					addInput(inputId, 1, true, null, byte[].class);
			}

		}
	}
	
	

	private HashMap<String, Type> processInputs(ArrayList<Map> inputs) {

		HashMap<String, Type> result = new HashMap<>();
		for (Map input : inputs) {
			String currentInputId = (String) input.get(ID);
			Object typeConfigurations;
			Type type = null;// this object holds the type of the input/output
								// or if it's an array then the type of the
								// elements in the array

			try {

				typeConfigurations = input.get(TYPE);
				// if type :single argument
				if (typeConfigurations.getClass() == String.class) {
					type = new Type();
					type.setType((String) typeConfigurations);
					type.setItems(null);// set it to null so that later we can
										// figure out this a single argument
										// type

					// type : defined as another map which contains type:
				} else if (typeConfigurations.getClass() == LinkedHashMap.class) {

					type = new Type();
					type.setType((String) ((Map) typeConfigurations).get(TYPE));
					type.setItems((String) ((Map) typeConfigurations).get(ITEMS));
				}

			} catch (ClassCastException e) {

				System.out.println("Class cast exception !!!");
			}
			if (type != null)// see whether type is defined or not
				result.put(currentInputId, type);

		}
		return result;
	}
	@Override
	public void executeAsynch(Map<String, T2Reference> arg0, AsynchronousActivityCallback arg1) {
	}

	@Override
	public CwlActivityConfigurationBean getConfiguration() {
		return null;
	}

}
