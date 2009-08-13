/*******************************************************************************
 * Copyright (C) 2009 The University of Manchester   
 * 
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *    
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *    
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
package net.sf.taverna.t2.activities.spreadsheet;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.reference.ExternalReferenceSPI;
import net.sf.taverna.t2.reference.Identified;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.ReferenceServiceException;
import net.sf.taverna.t2.reference.ReferenceSet;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.reference.ValueCarryingExternalReference;
import net.sf.taverna.t2.workflowmodel.OutputPort;
import net.sf.taverna.t2.workflowmodel.Port;
import net.sf.taverna.t2.workflowmodel.processor.activity.AbstractAsynchronousActivity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivityCallback;

import org.apache.log4j.Logger;

/**
 * An {@link net.sf.taverna.t2.workflowmodel.processor.activity.Activity} that reads spreadsheet
 * files.
 * 
 * @author David Withers
 */
public class SpreadsheetImportActivity extends
		AbstractAsynchronousActivity<SpreadsheetImportConfiguration> {

	private static final String INPUT_PORT_NAME = "fileurl";

	private static Logger logger = Logger.getLogger(SpreadsheetImportActivity.class);

	private SpreadsheetImportConfiguration configurationBean;

	private Range rowRange, columnRange;

	private boolean ignoreBlankRows;

	private Map<String, String> columnNames;

	private String missingCellValue;

	private SpreadsheetEmptyCellPolicy emptyCellPolicy;

	/**
	 * Constructs a SpreadsheetImport activity.
	 */
	public SpreadsheetImportActivity() {
	}

	@Override
	public void configure(SpreadsheetImportConfiguration configurationBean)
			throws ActivityConfigurationException {
		this.configurationBean = configurationBean;
		rowRange = configurationBean.getRowRange();
		logger.debug("Setting row range to " + rowRange);
		columnRange = configurationBean.getColumnRange();
		logger.debug("Setting column range to " + columnRange);
		ignoreBlankRows = configurationBean.isIgnoreBlankRows();
		columnNames = configurationBean.getColumnNames();
		missingCellValue = configurationBean.getEmptyCellValue();
		logger.debug("Setting empty cell value to " + missingCellValue);
		emptyCellPolicy = configurationBean.getEmptyCellPolicy();
		logger.debug("Setting empty cell policy to " + emptyCellPolicy);
		configurePorts();
	}

	private void configurePorts() {
		removeInputs();
		addInput(INPUT_PORT_NAME, 0, false, null, null);

		removeOutputs();
		for (int column = columnRange.getStart(); column <= columnRange.getEnd(); column++) {
			if (columnRange.contains(column)) {
				addOutput(SpreadsheetUtils.getPortName(column, columnNames), 1, 1);
			}
		}
	}

	@Override
	public SpreadsheetImportConfiguration getConfiguration() {
		return configurationBean;
	}

	@Override
	public void executeAsynch(final Map<String, T2Reference> data,
			final AsynchronousActivityCallback callback) {
		callback.requestRun(new Runnable() {

			public void run() {

				Map<String, T2Reference> outputData = new HashMap<String, T2Reference>();

				final InvocationContext context = callback.getContext();
				final ReferenceService referenceService = context.getReferenceService();

				try {
					T2Reference inputRef = data.get(INPUT_PORT_NAME);

					final Map<String, List<T2Reference>> outputLists = new HashMap<String, List<T2Reference>>();
					for (Port port : getOutputPorts()) {
						outputLists.put(port.getName(), new ArrayList<T2Reference>());
					}

					SpreadsheetRowProcessor spreadsheetRowProcessor = new SpreadsheetRowProcessor() {

						public void processRow(int rowIndex, Map<Integer, String> row) {
							for (Entry<Integer, String> entry : row.entrySet()) {
								String column = SpreadsheetUtils.getPortName(entry.getKey(),
										columnNames);
								Object value = entry.getValue();
								if (value == null) {
									if (emptyCellPolicy
											.equals(SpreadsheetEmptyCellPolicy.GENERATE_ERROR)) {
										value = referenceService.getErrorDocumentService()
												.registerError(
														"Missing data for spreadsheet cell "
																+ column + row, 0);
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

					};

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
							inputStream.close();
						} catch (SpreadsheetReadException e) {
							inputStream.close();
							inputStream = getInputStream(context, referenceService, inputRef);
							new ODFSpreadsheetReader().read(inputStream, new Range(rowRange),
									new Range(columnRange), ignoreBlankRows, spreadsheetRowProcessor);
							inputStream.close();
						}
					} catch (IOException e1) {
						logger.warn("Failed to close spereadsheet stream", e1);
					}

					// get outputs
					for (OutputPort outputPort : getOutputPorts()) {
						String name = outputPort.getName();
						Object value = outputLists.get(name);
						T2Reference id = referenceService.register(value, outputPort.getDepth(),
								true, context);
						outputData.put(name, id);
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

}
