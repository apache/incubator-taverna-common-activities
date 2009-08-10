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

import java.io.InputStream;

/**
 * Reads a spreadsheet from a stream.
 * 
 * @author David Withers
 */
public interface SpreadsheetReader {

	/**
	 * Reads an InputStream and passes spreadsheet cell data values, row by row, to the
	 * rowProcessor.
	 * 
	 * @param inputStream
	 *            the stream to read
	 * @param rowProcessor
	 *            the rowProcessor to write rows of data values to
	 * @param rowRange
	 *            the rows to read
	 * @param columnRange
	 *            the columns to read
	 * @param ignoreBlankRows
	 *            whether to ignore blank rows
	 * @throws SpreadsheetReadException
	 *             if there's an error reading the stream or the stream is not a valid spreadsheet
	 */
	public void read(InputStream inputStream, Range rowRange, Range columnRange, boolean ignoreBlankRows,
			SpreadsheetRowProcessor rowProcessor) throws SpreadsheetReadException;

}