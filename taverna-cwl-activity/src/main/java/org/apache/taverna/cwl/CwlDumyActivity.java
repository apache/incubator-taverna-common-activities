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
	private static final String OUTPUTS = "outputs";
	private static final String ID = "id";
	private static final String TYPE = "type";
	private static final String ARRAY = "array";
	private static final String DESCRIPTION = "description";
	private static final int DEPTH_0 = 0;
	private static final int DEPTH_1 = 1;
	private static final int DEPTH_2 = 2;
	private static final String LABEL = "label";

	private static final String NAMESPACES = "$namespaces";
	// datatypes
	private static final String FLOAT = "float";
	private static final String NULL = "null";
	private static final String BOOLEAN = "boolean";
	private static final String INT = "int";
	private static final String DOUBLE = "double";
	private static final String STRING = "string";
	private static final String FILE = "file";
	private static final String FORMAT = "format";

	private HashMap<String, PortDetail> processedInputs;
	private HashMap<String, PortDetail> processedOutputs;
	private LinkedHashMap nameSpace;

	public HashMap<String, PortDetail> getProcessedInputs() {
		return processedInputs;
	}

	public void setProcessedInputs(HashMap<String, PortDetail> processedInputs) {
		this.processedInputs = processedInputs;
	}

	public HashMap<String, PortDetail> getProcessedOutputs() {
		return processedOutputs;
	}

	public void setProcessedOutputs(HashMap<String, PortDetail> processedOutputs) {
		this.processedOutputs = processedOutputs;
	}

	public void processNameSpace(Map cwlFile) {

		if (cwlFile.containsKey(NAMESPACES)) {
			nameSpace = (LinkedHashMap) cwlFile.get(NAMESPACES);
		}

	}

	@Override
	public void configure(CwlActivityConfigurationBean configurationBean) throws ActivityConfigurationException {
		removeInputs();
		removeOutputs();
		Map cwlFile = configurationBean.getCwlConfigurations();

		processNameSpace(cwlFile);

		if (cwlFile != null) {
			processedInputs = processInputs(cwlFile);

			for (String inputId : processedInputs.keySet()) {
				int depth = processedInputs.get(inputId).getDepth();
				if (depth == DEPTH_0)
					addInput(inputId, DEPTH_0, true, null, String.class);
				else if (depth == DEPTH_1)
					addInput(inputId, DEPTH_1, true, null, byte[].class);

			}
			processedOutputs = processOutputs(cwlFile);
			for (String inputId : processedOutputs.keySet()) {
				int depth = processedOutputs.get(inputId).getDepth();
				if (depth == DEPTH_0)
					addOutput(inputId, DEPTH_0);
				else if (depth == DEPTH_1)
					addOutput(inputId, DEPTH_1);

			}
		}

	}

	private HashMap<String, PortDetail> processOutputs(Map cwlFile) {
		return process(cwlFile.get(OUTPUTS));
	}

	private HashMap<String, PortDetail> processInputs(Map cwlFile) {
		return process(cwlFile.get(INPUTS));
	}

	public boolean isValidDataType(ArrayList typeConfigurations) {
		for (Object type : typeConfigurations) {
			if (!(((String) type).equals(FLOAT) || ((String) type).equals(NULL)) || (((String) type).equals(BOOLEAN))
					|| (((String) type).equals(INT) || (((String) type).equals(DOUBLE)))
					|| (((String) type).equals(STRING)) || (((String) type).equals(FILE)))
				return false;
		}
		return true;
	}

	private HashMap<String, PortDetail> process(Object inputs) {

		HashMap<String, PortDetail> result = new HashMap<>();

		if (inputs.getClass() == ArrayList.class) {

			for (Map input : (ArrayList<Map>) inputs) {
				PortDetail detail = new PortDetail();

				String currentInputId = (String) input.get(ID);
				Object typeConfigurations;
				// get the parameter description
				if (input.containsKey(DESCRIPTION)) {
					detail.setDescription((String) input.get(DESCRIPTION));
				} else {
					detail.setDescription(null);
				}
				// get the parameter label
				if (input.containsKey(LABEL)) {
					detail.setLabel((String) input.get(LABEL));
				} else {
					detail.setLabel(null);
				}
				// getting the format info
				if (input.containsKey(FORMAT)) {
					
					String format[] = input.get(FORMAT).toString().split(":");
					String namespaceKey = format[0];
					String urlAppednd = format[1];
					if (!nameSpace.isEmpty()) {
						detail.setFormat(nameSpace.get(namespaceKey) + urlAppednd);
					} else {
						detail.setFormat(null);
					}
				}
				try {

					typeConfigurations = input.get(TYPE);
					// if type :single argument
					if (typeConfigurations.getClass() == String.class) {
						detail.setDepth(DEPTH_0);

						result.put(currentInputId, detail);
						// type : defined as another map which contains type:
					} else if (typeConfigurations.getClass() == LinkedHashMap.class) {
						String inputType = (String) ((Map) typeConfigurations).get(TYPE);
						if (inputType.equals(ARRAY)) {
							detail.setDepth(DEPTH_1);
							result.put(currentInputId, detail);
						}
					} else if (typeConfigurations.getClass() == ArrayList.class) {
						if (isValidDataType((ArrayList) typeConfigurations)) {
							detail.setDepth(DEPTH_0);
							result.put(currentInputId, detail);
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

	@Override
	public void executeAsynch(Map<String, T2Reference> arg0, AsynchronousActivityCallback arg1) {
	}

	@Override
	public CwlActivityConfigurationBean getConfiguration() {
		return null;
	}

}
