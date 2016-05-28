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
	public void configure(CwlActivityConfigurationBean arg0) throws ActivityConfigurationException {
		removeInputs();
		removeOutputs();

	}
	
	

	private void getInputs(CwlActivityConfigurationBean configurationBean) {

		Map cwlFile = configurationBean.getCwlConfigurations();

		// Get all input objects in
		ArrayList<Map> inputs = (ArrayList<Map>) cwlFile.get(INPUTS);

		HashMap<String, Type> processedInputs;

		if (inputs != null) {
			processedInputs = processInputs(inputs);

			for (String inputId : processedInputs.keySet()) {
				if (processedInputs.get(inputId).getType().equals(FILE))
					System.out.println("ID: " + inputId + " type : File");
				

				if (processedInputs.get(inputId).getType().equals(ARRAY))
					System.out.println(
							"ID :" + inputId + " type: Array items: " + processedInputs.get(inputId).getItems());
			}

		}
	}

	private HashMap<String, Type> processInputs(ArrayList<Map> inputs) {

		HashMap<String, Type> result = new HashMap<>();

		for (Map input : inputs) {

			String Id = (String) input.get(ID);
			// This require for nested type definitions
			Map typeConfigurations;
			// this object holds the type and if it's an array then type of the
			// elements in the array
			Type type = new Type();
			try {
				/*
				 * This part will go through nested type definitions
				 * 
				 * type :
				 *   type : array
				 *   items : boolean
				 *  
				 */
				
				typeConfigurations = (Map) input.get(TYPE);
				type.setType((String) typeConfigurations.get(TYPE));
				type.setItems((String) typeConfigurations.get(ITEMS));
			} catch (ClassCastException e) {
				/*This exception means type is described as single argument ex:
				* type : File
				*/
				type.setType((String) input.get(TYPE));
				type.setItems(null);
			}
			result.put(Id, type);
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
