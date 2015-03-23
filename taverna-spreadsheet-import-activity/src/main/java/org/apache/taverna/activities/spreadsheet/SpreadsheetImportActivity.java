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

package org.apache.taverna.activities.spreadsheet;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;

import org.apache.taverna.invocation.InvocationContext;
import org.apache.taverna.reference.ExternalReferenceSPI;
import org.apache.taverna.reference.Identified;
import org.apache.taverna.reference.ReferenceService;
import org.apache.taverna.reference.ReferenceServiceException;
import org.apache.taverna.reference.ReferenceSet;
import org.apache.taverna.reference.T2Reference;
import org.apache.taverna.reference.ValueCarryingExternalReference;
import org.apache.taverna.workflowmodel.OutputPort;
import org.apache.taverna.workflowmodel.Port;
import org.apache.taverna.workflowmodel.processor.activity.AbstractAsynchronousActivity;
import org.apache.taverna.workflowmodel.processor.activity.ActivityConfigurationException;
import org.apache.taverna.workflowmodel.processor.activity.AsynchronousActivityCallback;

import org.apache.log4j.Logger;

import com.csvreader.CsvWriter;
import com.fasterxml.jackson.databind.JsonNode;


/**
 * An {@link net.sf.taverna.t2.workflowmodel.processor.activity.Activity} that reads spreadsheet
 * files.
 *
 * @author David Withers
 */
public class SpreadsheetImportActivity extends AbstractAsynchronousActivity<JsonNode> {

	public static final String URI = "http://ns.taverna.org.uk/2010/activity/spreadsheet-import";

	public static final String INPUT_PORT_NAME = "fileurl";

	public static final String OUTPUT_PORT_NAME = "output";

	private static Logger logger = Logger.getLogger(SpreadsheetImportActivity.class);

	private JsonNode configurationBean;

	private Range rowRange, columnRange;

	private boolean ignoreBlankRows;

	private String missingCellValue;

	private SpreadsheetEmptyCellPolicy emptyCellPolicy;

	private SpreadsheetOutputFormat outputFormat;

	private String csvDelimiter;

	/**
	 * Constructs a SpreadsheetImport activity.
	 */
	public SpreadsheetImportActivity() {
	}

	@Override
	public void configure(JsonNode configurationBean)
			throws ActivityConfigurationException {
		this.configurationBean = configurationBean;
		rowRange = SpreadsheetUtils.getRange(configurationBean.get("rowRange"));
		logger.debug("Setting row range to " + rowRange);
		columnRange = SpreadsheetUtils.getRange(configurationBean.get("columnRange"));
		logger.debug("Setting column range to " + columnRange);
		ignoreBlankRows = configurationBean.get("ignoreBlankRows").booleanValue();
		missingCellValue = configurationBean.get("emptyCellValue").textValue();
		logger.debug("Setting empty cell value to '" + missingCellValue + "'");
		emptyCellPolicy = SpreadsheetEmptyCellPolicy.valueOf(configurationBean.get("emptyCellPolicy").textValue());
		logger.debug("Setting empty cell policy to " + emptyCellPolicy);
		outputFormat = SpreadsheetOutputFormat.valueOf(configurationBean.get("outputFormat").textValue());
		logger.debug("Setting output format to " + outputFormat);
		csvDelimiter = configurationBean.get("csvDelimiter").textValue();
		logger.debug("Setting csv delimiter to '" + csvDelimiter + "'");
//		configurePorts();
	}

	private void configurePorts() {
		removeInputs();
		addInput(INPUT_PORT_NAME, 0, false, null, null);

		removeOutputs();
		if (outputFormat.equals(SpreadsheetOutputFormat.PORT_PER_COLUMN)) {
			for (int column = columnRange.getStart(); column <= columnRange.getEnd(); column++) {
				if (columnRange.contains(column)) {
					addOutput(SpreadsheetUtils.getPortName(column, configurationBean), 1, 1);
				}
			}
		} else {
			addOutput(OUTPUT_PORT_NAME, 0, 0);
		}
	}

	@Override
	public JsonNode getConfiguration() {
		return configurationBean;
	}

	@Override
	public void executeAsynch(final Map<String, T2Reference> data,
			final AsynchronousActivityCallback callback) {
		callback.requestRun(new Runnable() {

			public void run() {

				Map<String, T2Reference> outputData = new HashMap<String, T2Reference>();

				InvocationContext context = callback.getContext();
				ReferenceService referenceService = context.getReferenceService();

				try {
					T2Reference inputRef = data.get(INPUT_PORT_NAME);

					SpreadsheetRowProcessor spreadsheetRowProcessor = null;
					Map<String, List<T2Reference>> outputLists = null;
					StringWriter output = null;

					if (outputFormat.equals(SpreadsheetOutputFormat.PORT_PER_COLUMN)) {
						outputLists = new HashMap<String, List<T2Reference>>();
						for (Port port : getOutputPorts()) {
							outputLists.put(port.getName(), new ArrayList<T2Reference>());
						}
						spreadsheetRowProcessor = new MultiplePortRowProcessor(referenceService, outputLists, context);
					} else {
						output = new StringWriter();
						char csvDelimiterCharacter = ',';
						if (csvDelimiter != null && csvDelimiter.length() > 0) {
							csvDelimiterCharacter = csvDelimiter.charAt(0);
						}
						CsvWriter csvWriter = new CsvWriter(output, csvDelimiterCharacter);
						csvWriter.setEscapeMode(CsvWriter.ESCAPE_MODE_DOUBLED);
						csvWriter.setTextQualifier('"');
						csvWriter.setUseTextQualifier(true);
						spreadsheetRowProcessor = new SingleOutputRowProcessor(csvWriter);
					}

					InputStream inputStream = getInputStream(context, referenceService, inputRef);
					if (inputStream == null) {
						logger.warn("Input is not a file reference or a file name");
						callback.fail("Input is not a file reference or a file name");
						return;
					}
					try {
						try {
							new ExcelSpreadsheetReader().read(inputStream, new Range(rowRange),
									new Range(columnRange), ignoreBlankRows, spreadsheetRowProcessor);
						} catch (SpreadsheetReadException e) {
							inputStream.close();
							inputStream = getInputStream(context, referenceService, inputRef);
							try {
								new ODFSpreadsheetReader().read(inputStream, new Range(rowRange),
										new Range(columnRange), ignoreBlankRows, spreadsheetRowProcessor);
							} catch (SpreadsheetReadException e2) {
								inputStream.close();
								inputStream = getInputStream(context, referenceService, inputRef);
								new CSVSpreadsheetReader().read(inputStream, new Range(rowRange),
										new Range(columnRange), ignoreBlankRows, spreadsheetRowProcessor);
							}
						} finally {
							inputStream.close();
						}
					} catch (IOException e1) {
						logger.warn("Failed to close spereadsheet stream", e1);
					}

					// get outputs
					if (outputFormat.equals(SpreadsheetOutputFormat.PORT_PER_COLUMN)) {
						for (OutputPort outputPort : getOutputPorts()) {
							String name = outputPort.getName();
							Object value = outputLists.get(name);
							T2Reference id = referenceService.register(value, outputPort.getDepth(),
									true, context);
							outputData.put(name, id);
						}
					} else {
						T2Reference id = referenceService.register(output.toString(), 0, true, context);
						outputData.put(OUTPUT_PORT_NAME, id);
					}
					callback.receiveResult(outputData, new int[0]);
				} catch (ReferenceServiceException e) {
					logger.warn("Error accessing spreadsheet input/output data", e);
					callback.fail("Error accessing spreadsheet input/output data", e);
				} catch (SpreadsheetReadException e) {
					logger.warn("Spreadsheet input cannot be read", e);
					callback.fail("Spreadsheet input cannot be read", e);
				} catch (FileNotFoundException e) {
					logger.warn("Input spreadsheet file does not exist", e);
					callback.fail("Input spreadsheet file does not exist", e);
				} catch (IOException e) {
					logger.warn("Error reading spreadsheet", e);
					callback.fail("Error reading spreadsheet", e);
				}
			}


		});
	}

	private InputStream getInputStream(InvocationContext context,
			ReferenceService referenceService, T2Reference inputRef)
			throws IOException {
		InputStream inputStream = null;

		Identified identified = referenceService.resolveIdentifier(inputRef, null, context);
		if (identified instanceof ReferenceSet) {
			ReferenceSet referenceSet = (ReferenceSet) identified;
			Set<ExternalReferenceSPI> externalReferences = referenceSet
					.getExternalReferences();
			for (ExternalReferenceSPI externalReference : externalReferences) {
				if (externalReference instanceof ValueCarryingExternalReference<?>) {
					ValueCarryingExternalReference<?> vcer = (ValueCarryingExternalReference<?>) externalReference;
					if (String.class.isAssignableFrom(vcer.getValueType())) {
						String input = (String) vcer.getValue();
						try {
							URL url = new URL(input);
							inputStream = url.openStream();
							logger.debug("Input spreadsheet url is '" + input + "'");
						} catch (MalformedURLException e) {
							logger.debug("Input spreadsheet file name is '" + input + "'");
							inputStream = new FileInputStream(input);
						}
					}
					break;
				} else {
					inputStream = externalReference.openStream(context);
					break;
				}
			}
		}
		return inputStream;
	}

	/**
	 * SpreadsheetRowProcessor for handling a single output formatted as csv.
	 *
	 * @author David Withers
	 */
	private final class SingleOutputRowProcessor implements SpreadsheetRowProcessor {

		private final CsvWriter csvWriter;

		/**
		 * Constructs a new SingleOutputRowProcessor.
		 *
		 * @param csvWriter
		 */
		private SingleOutputRowProcessor(CsvWriter csvWriter) {
			this.csvWriter = csvWriter;
		}

		public void processRow(int rowIndex, SortedMap<Integer, String> row) {
			try {
				for (String value : row.values()) {
					if (value == null) {
						if (emptyCellPolicy.equals(SpreadsheetEmptyCellPolicy.GENERATE_ERROR)) {
							value = "ERROR";
						} else if (emptyCellPolicy.equals(SpreadsheetEmptyCellPolicy.EMPTY_STRING)) {
							value = "";
						} else {
							value = missingCellValue;
						}
					}
					csvWriter.write(value, true);
				}
				csvWriter.endRecord();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	/**
	 * SpreadsheetRowProcessor for handling multiple outputs.
	 *
	 * @author David Withers
	 */
	private final class MultiplePortRowProcessor implements SpreadsheetRowProcessor {

		private final ReferenceService referenceService;
		private final Map<String, List<T2Reference>> outputLists;
		private final InvocationContext context;

		/**
		 * Constructs a new MultiplePortRowProcessor.
		 *
		 * @param referenceService
		 * @param outputLists
		 * @param context
		 */
		private MultiplePortRowProcessor(ReferenceService referenceService,
				Map<String, List<T2Reference>> outputLists, InvocationContext context) {
			this.referenceService = referenceService;
			this.outputLists = outputLists;
			this.context = context;
		}

		public void processRow(int rowIndex, SortedMap<Integer, String> row) {
			for (Entry<Integer, String> entry : row.entrySet()) {
				String column = SpreadsheetUtils.getPortName(entry.getKey(),
						configurationBean);
				Object value = entry.getValue();
				if (value == null) {
					if (emptyCellPolicy
							.equals(SpreadsheetEmptyCellPolicy.GENERATE_ERROR)) {
						value = referenceService.getErrorDocumentService()
								.registerError(
										"Missing data for spreadsheet cell "
												+ column + row, 0, context);
					} else if (emptyCellPolicy
							.equals(SpreadsheetEmptyCellPolicy.EMPTY_STRING)) {
						value = "";
					} else {
						value = missingCellValue;
					}
				}
				T2Reference id = referenceService.register(value, 0, true, context);
				outputLists.get(column).add(id);
			}
		}
	}

}
