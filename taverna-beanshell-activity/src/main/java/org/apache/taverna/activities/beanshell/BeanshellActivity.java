/*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.apache.taverna.activities.beanshell;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.taverna.activities.dependencyactivity.AbstractAsynchronousDependencyActivity;
import org.apache.taverna.reference.ErrorDocumentService;
import org.apache.taverna.reference.ReferenceService;
import org.apache.taverna.reference.ReferenceServiceException;
import org.apache.taverna.reference.T2Reference;
import org.apache.taverna.workflowmodel.OutputPort;
import org.apache.taverna.workflowmodel.processor.activity.ActivityInputPort;
import org.apache.taverna.workflowmodel.processor.activity.AsynchronousActivityCallback;

import org.apache.log4j.Logger;

import uk.org.taverna.configuration.app.ApplicationConfiguration;
import bsh.EvalError;
import bsh.Interpreter;
import bsh.TargetError;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * An Activity providing Beanshell functionality.
 * 
 * @author David Withers
 * @author Stuart Owen
 * @author Alex Nenadic
 */
public class BeanshellActivity extends AbstractAsynchronousDependencyActivity {

	public static final String URI = "http://ns.taverna.org.uk/2010/activity/beanshell";

	private static Logger logger = Logger.getLogger(BeanshellActivity.class);

	private Interpreter interpreter;

	private static String CLEAR_COMMAND = "clear();";

	private JsonNode json;

	public BeanshellActivity(ApplicationConfiguration applicationConfiguration) {
		super(applicationConfiguration);
		createInterpreter();
	}

	@Override
	public JsonNode getConfiguration() {
		return json;
	}

	@Override
	public void configure(JsonNode json) {
		this.json = json;
		checkGranularDepths();
	}

	/**
	 * Creates the interpreter required to run the beanshell script, and assigns
	 * the correct classloader setting according to the
	 */
	private void createInterpreter() {
		interpreter = new Interpreter();
	}

	/**
	 * As the Beanshell activity currently only can output values at the
	 * specified depth, the granular depths should always be equal to the actual
	 * depth.
	 * <p>
	 * Workflow definitions created with Taverna 2.0b1 would not honour this and
	 * always set the granular depth to 0.
	 * <p>
	 * This method modifies the granular depths to be equal to the depths.
	 */
	protected void checkGranularDepths() {
		for (OutputPort outputPort : getOutputPorts()) {
			if (outputPort.getGranularDepth() != outputPort.getDepth()) {
				logger.warn("Replacing granular depth of port "
						+ outputPort.getName());
				// outputPort.setGranularDepth(outputPort.getDepth());
			}
		}
	}

	public ActivityInputPort getInputPort(String name) {
		for (ActivityInputPort port : getInputPorts()) {
			if (port.getName().equals(name)) {
				return port;
			}
		}
		return null;
	}

	private void clearInterpreter() {
		try {
			interpreter.eval(CLEAR_COMMAND);
		} catch (EvalError e) {
			logger.error("Could not clear the interpreter", e);
		}
	}

	@Override
	public void executeAsynch(final Map<String, T2Reference> data,
			final AsynchronousActivityCallback callback) {
		callback.requestRun(new Runnable() {

			public void run() {

				// Workflow run identifier (needed when classloader sharing is
				// set to 'workflow').
				String procID = callback.getParentProcessIdentifier();
				String workflowRunID;
				if (procID.contains(":")) {
					workflowRunID = procID.substring(0, procID.indexOf(':'));
				} else {
					workflowRunID = procID; // for tests, will be an empty
											// string
				}

				synchronized (interpreter) {

					// Configure the classloader for executing the Beanshell
					if (classLoader == null) {
						try {
							classLoader = findClassLoader(json, workflowRunID);
							interpreter.setClassLoader(classLoader);
						} catch (RuntimeException rex) {
							String message = "Unable to obtain the classloader for Beanshell service";
							callback.fail(message, rex);
							return;
						}
					}

					ReferenceService referenceService = callback.getContext()
							.getReferenceService();

					Map<String, T2Reference> outputData = new HashMap<String, T2Reference>();

					clearInterpreter();
					try {
						// set inputs
						for (String inputName : data.keySet()) {
							ActivityInputPort inputPort = getInputPort(inputName);
							Object input = referenceService.renderIdentifier(
									data.get(inputName),
									inputPort.getTranslatedElementClass(),
									callback.getContext());
							inputName = sanatisePortName(inputName);
							interpreter.set(inputName, input);
						}
						// run
						interpreter.eval(json.get("script").asText());
						// get outputs
						for (OutputPort outputPort : getOutputPorts()) {
							String name = outputPort.getName();
							Object value = interpreter.get(name);
							if (value == null) {
								ErrorDocumentService errorDocService = referenceService
										.getErrorDocumentService();
								value = errorDocService.registerError(
										"No value produced for output variable "
												+ name, outputPort.getDepth(),
										callback.getContext());
							}
							outputData.put(name, referenceService.register(
									value, outputPort.getDepth(), true,
									callback.getContext()));
						}
						callback.receiveResult(outputData, new int[0]);
					} catch (EvalError e) {
						logger.error(e);
						try {
							int lineNumber = e.getErrorLineNumber();

							callback.fail("Line " + lineNumber + ": "
									+ determineMessage(e));
						} catch (NullPointerException e2) {
							callback.fail(determineMessage(e));
						}
					} catch (ReferenceServiceException e) {
						callback.fail(
								"Error accessing beanshell input/output data for "
										+ this, e);
					}
					clearInterpreter();
				}
			}

			/**
			 * Removes any invalid characters from the port name. For example,
			 * xml-text would become xmltext.
			 * 
			 * @param name
			 * @return
			 */
			private String sanatisePortName(String name) {
				String result = name;
				if (Pattern.matches("\\w++", name) == false) {
					result = "";
					for (char c : name.toCharArray()) {
						if (Character.isLetterOrDigit(c) || c == '_') {
							result += c;
						}
					}
				}
				return result;
			}
		});

	}

	private static String determineMessage(Throwable e) {
		if (e instanceof TargetError) {
			Throwable t = ((TargetError) e).getTarget();
			if (t != null) {
				return t.getClass().getCanonicalName() + ": "
						+ determineMessage(t);
			}
		}
		Throwable cause = e.getCause();
		if (cause != null) {
			return determineMessage(cause);
		}
		return e.getMessage();
	}
}
