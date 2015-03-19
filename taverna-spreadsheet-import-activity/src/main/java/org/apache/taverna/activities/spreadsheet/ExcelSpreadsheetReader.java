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

import java.io.IOException;
import java.io.InputStream;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

/**
 * Reads Excel '97 (.xls) or Excel '07 (.xlsx) spreadsheet files.
 * 
 * @author David Withers
 */
public class ExcelSpreadsheetReader implements SpreadsheetReader {

	private static Logger logger = Logger.getLogger(ExcelSpreadsheetReader.class);

	public void read(InputStream inputStream, Range rowRange, Range columnRange, boolean ignoreBlankRows, SpreadsheetRowProcessor rowProcessor)
			throws SpreadsheetReadException {
		Workbook workbook;
		try {
			workbook = WorkbookFactory.create(inputStream);
		} catch (InvalidFormatException e) {
			throw new SpreadsheetReadException(
					"The file does not have a compatible spreadsheet format", e);
		} catch (IOException e) {
			throw new SpreadsheetReadException("The spreadsheet stream could not be read", e);
		} catch (IllegalArgumentException e) {
			throw new SpreadsheetReadException("The spreadsheet stream could not be read", e);
		}

		DataFormatter dataFormatter = new DataFormatter();
		
		workbook.setMissingCellPolicy(Row.CREATE_NULL_AS_BLANK);
		Sheet sheet = workbook.getSheetAt(0);

		if (rowRange.getEnd() < 0) {
			rowRange.setEnd(sheet.getLastRowNum());
			logger.debug("No end of row range specified, setting to " + rowRange.getEnd());
		}

		SortedMap<Integer, String> currentDataRow = new TreeMap<Integer, String>();

		for (int rowIndex = rowRange.getStart(); rowIndex <= rowRange.getEnd(); rowIndex++) {
			boolean blankRow = true;
			if (rowRange.contains(rowIndex)) {
				Row row = sheet.getRow(rowIndex);
				for (int columnIndex = columnRange.getStart(); columnIndex <= columnRange.getEnd(); columnIndex++) {
					if (columnRange.contains(columnIndex)) {
						String value = null;
						if (row != null) {
							Cell cell = row.getCell(columnIndex);
							if (cell != null) {
								value = getCellValue(cell, dataFormatter);
							}
						}
						if (value != null) {
							blankRow = false;
						}
						currentDataRow.put(columnIndex, value);
						if (columnIndex == columnRange.getEnd()) {
							if (!ignoreBlankRows || !blankRow) {
								rowProcessor.processRow(rowIndex, currentDataRow);
							}
							currentDataRow = new TreeMap<Integer, String>();
						}
					}
				}
			}
		}

	}

	private String getCellValue(Cell cell, DataFormatter dataFormatter) {
		String value = null;
		switch (cell.getCellType()) {
		case Cell.CELL_TYPE_BOOLEAN:
			value = Boolean.toString(cell.getBooleanCellValue());
			break;
		case Cell.CELL_TYPE_NUMERIC:
			if (DateUtil.isCellDateFormatted(cell)) {
//				value = cell.getDateCellValue().toString();
				value = dataFormatter.formatCellValue(cell);
			} else {
				value = Double.toString(cell.getNumericCellValue());
			}
			break;
		case Cell.CELL_TYPE_STRING:
			value = cell.getStringCellValue();
			break;
		case Cell.CELL_TYPE_FORMULA:
			switch (cell.getCachedFormulaResultType()) {
			case Cell.CELL_TYPE_BOOLEAN:
				value = Boolean.toString(cell.getBooleanCellValue());
				break;
			case Cell.CELL_TYPE_NUMERIC:
				if (DateUtil.isCellDateFormatted(cell)) {
					value = cell.getDateCellValue().toString();
				} else {
					value = Double.toString(cell.getNumericCellValue());
				}
				break;
			case Cell.CELL_TYPE_STRING:
				value = cell.getStringCellValue();
				break;
			default:
				break;
			}
		default:
			break;
		}
		// value = dataFormatter.formatCellValue(cell);
		// if ("".equals(value)) value = null;
		return value;
	}


//	/**
//	 * Reads data from an HSSF stream.
//	 * 
//	 * @param inputStream
//	 * @param spreradsheetRowProcessor
//	 * @throws IOException
//	 * @deprecated can't generalize for XSSF streams and not much advantage as all the (non
//	 *             duplicated) data is contained in one event so memory footprint isn't much smaller
//	 */
//	public void readHSSF(InputStream inputStream, SpreadsheetRowProcessor spreradsheetRowProcessor)
//			throws IOException {
//		POIFSFileSystem poifs = new POIFSFileSystem(inputStream);
//
//		// get the workbook part of the stream
//		InputStream documentInputStream = poifs.createDocumentInputStream("Workbook");
//
//		RecordProcessor recordProcessor = new RecordProcessor(spreradsheetRowProcessor);
//		MissingRecordAwareHSSFListener hssfListener = new MissingRecordAwareHSSFListener(
//				recordProcessor);
//
//		// listen for all records
//		HSSFRequest request = new HSSFRequest();
//		request.addListenerForAllRecords(hssfListener);
//
//		HSSFEventFactory factory = new HSSFEventFactory();
//		factory.processEvents(request, documentInputStream);
//
//		inputStream.close();
//		documentInputStream.close();
//	}
//
//	/**
//	 * Listener for processing events from an HSSF stream.
//	 * 
//	 * @author David Withers
//	 * @deprecated can't generalize for XSSF streams and not much advantage as all the (non
//	 *             duplicated) data is contained in one event so memory footprint isn't much smaller
//	 */
//	class RecordProcessor implements HSSFListener {
//
//		private SpreadsheetRowProcessor spreradsheetRowProcessor;
//
//		private SSTRecord sstrec;
//
//		private boolean worksheetOpen = false;
//
//		private int row, column;
//
//		private Map<Integer, String> currentDataRow = new HashMap<Integer, String>();
//
//		public RecordProcessor(SpreadsheetRowProcessor spreradsheetRowProcessor) {
//			this.spreradsheetRowProcessor = spreradsheetRowProcessor;
//		}
//
//		public void processRecord(Record record) {
//			switch (record.getSid()) {
//			// the BOFRecord can represent either the beginning of a sheet or
//			// the workbook
//			case BOFRecord.sid:
//				BOFRecord bof = (BOFRecord) record;
//				if (bof.getType() == BOFRecord.TYPE_WORKSHEET) {
//					worksheetOpen = true;
//				}
//				break;
//			case EOFRecord.sid:
//				if (worksheetOpen) {
//					while (row < rowRange.getEnd()) {
//						row++;
//						if (rowRange.contains(row)) {
//							for (column = columnRange.getStart(); column <= columnRange.getEnd(); column++) {
//								processCell(row, column, null);
//							}
//							spreradsheetRowProcessor.processRow(row, currentDataRow);
//						}
//						currentDataRow = new HashMap<Integer, String>();
//					}
//					worksheetOpen = false;
//				}
//				break;
//			// don't care about sheet name for now
//			// case BoundSheetRecord.sid:
//			// BoundSheetRecord bsr = (BoundSheetRecord) record;
//			// logger.info("New sheet named: " + bsr.getSheetname());
//			// break;
//			case RowRecord.sid:
//				// RowRecord rowRecord = (RowRecord) record;
//				// if (readAllRows) {
//				// int rowNumber = row.getRowNumber();
//				// if (rowNumber < minRow) {
//				// minRow = rowNumber;
//				// currentRow = rowNumber;
//				// }
//				// if (rowNumber > maxRow) {
//				// maxRow = rowNumber;
//				// }
//				// }
//				// if (readAllColumns) {
//				// int firstColumn = row.getFirstCol();
//				// int lastColumn = row.getLastCol() - 1;
//				// if (firstColumn < minColumn) {
//				// minColumn = firstColumn;
//				// currentColumn = firstColumn;
//				// }
//				// if (lastColumn > maxColumn) {
//				// maxColumn = lastColumn;
//				// }
//				// }
//
//				break;
//			case NumberRecord.sid:
//				NumberRecord number = (NumberRecord) record;
//				row = number.getRow();
//				column = number.getColumn();
//				processCell(row, column, String.valueOf(number.getValue()));
//				break;
//			case SSTRecord.sid:
//				// SSTRecords store a array of unique strings used in Excel.
//				sstrec = (SSTRecord) record;
//				break;
//			case LabelSSTRecord.sid:
//				LabelSSTRecord label = (LabelSSTRecord) record;
//				row = label.getRow();
//				column = label.getColumn();
//				processCell(row, column, sstrec.getString(label.getSSTIndex()).getString());
//				break;
//			case BlankRecord.sid:
//				BlankRecord blank = (BlankRecord) record;
//				row = blank.getRow();
//				column = blank.getColumn();
//				processCell(row, column, null);
//				break;
//			}
//
//			// Missing column
//			if (record instanceof MissingCellDummyRecord) {
//				MissingCellDummyRecord cell = (MissingCellDummyRecord) record;
//				row = cell.getRow();
//				column = cell.getColumn();
//				processCell(row, column, null);
//			}
//
//			// Missing row
//			if (record instanceof MissingRowDummyRecord) {
//				MissingRowDummyRecord missingRow = (MissingRowDummyRecord) record;
//				row = missingRow.getRowNumber();
//				if (rowRange.contains(row)) {
//					for (column = columnRange.getStart(); column <= columnRange.getEnd(); column++) {
//						processCell(row, column, null);
//					}
//					spreradsheetRowProcessor.processRow(row, currentDataRow);
//				}
//				currentDataRow = new HashMap<Integer, String>();
//			}
//
//			// End of row
//			if (record instanceof LastCellOfRowDummyRecord) {
//				LastCellOfRowDummyRecord lastCell = (LastCellOfRowDummyRecord) record;
//				row = lastCell.getRow();
//				if (rowRange.contains(row)) {
//					int lastColumn = lastCell.getLastColumnNumber();
//					for (column = lastColumn + 1; column <= columnRange.getEnd(); column++) {
//						processCell(row, column, null);
//					}
//					spreradsheetRowProcessor.processRow(row, currentDataRow);
//				}
//				currentDataRow = new HashMap<Integer, String>();
//			}
//		}
//
//		private void processCell(int row, int column, String value) {
//			if (rowRange.contains(row) && columnRange.contains(column)) {
//				currentDataRow.put(column, value);
//			}
//		}
//
//	}

}
