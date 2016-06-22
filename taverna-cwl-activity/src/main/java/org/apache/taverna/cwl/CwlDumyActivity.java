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

import org.apache.taverna.cwl.utilities.CWLUtil;
import org.apache.taverna.reference.T2Reference;
import org.apache.taverna.workflowmodel.processor.activity.AbstractAsynchronousActivity;
import org.apache.taverna.workflowmodel.processor.activity.ActivityConfigurationException;
import org.apache.taverna.workflowmodel.processor.activity.AsynchronousActivity;
import org.apache.taverna.workflowmodel.processor.activity.AsynchronousActivityCallback;



public class CwlDumyActivity extends AbstractAsynchronousActivity<CwlActivityConfigurationBean>
		implements AsynchronousActivity<CwlActivityConfigurationBean> {

	private static final int DEPTH_0 = 0;
	private static final int DEPTH_1 = 1;
	private static final int DEPTH_2 = 2;

	//all processes are done here
	private CWLUtil cwlUtil;
	


	@Override
	public void configure(CwlActivityConfigurationBean configurationBean) throws ActivityConfigurationException {
		removeInputs();
		removeOutputs();
		
		 
		 
		Map cwlFile = configurationBean.getCwlConfigurations();
		cwlUtil = new CWLUtil(cwlFile);
		

		if (cwlFile != null) {
			//get the processed data
			HashMap<String, Integer>  processedInputs= cwlUtil.processInputDepths();
			for (String inputId : processedInputs.keySet()) {
				int depth = processedInputs.get(inputId);
				if (depth == DEPTH_0)
					addInput(inputId, DEPTH_0, true, null, String.class);
				else if (depth == DEPTH_1)
					addInput(inputId, DEPTH_1, true, null, byte[].class);

			}
			//get the processed data
			HashMap<String, Integer>  processedOutputs = cwlUtil.processOutputDepths();
			for (String inputId : processedOutputs.keySet()) {
				int depth = processedOutputs.get(inputId);
				if (depth == DEPTH_0)
					addOutput(inputId, DEPTH_0);
				else if (depth == DEPTH_1)
					addOutput(inputId, DEPTH_1);

			}
		}

	}

	@Override
	public void executeAsynch(Map<String, T2Reference> arg0, AsynchronousActivityCallback arg1) {
	}

	@Override
	public CwlActivityConfigurationBean getConfiguration() {
		return null;
	}

}
