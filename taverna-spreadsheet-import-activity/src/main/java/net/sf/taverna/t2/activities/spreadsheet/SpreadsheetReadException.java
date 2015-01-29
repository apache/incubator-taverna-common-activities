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

/**
 * Exception thrown when a spreadsheet cannot be read due to an IO error or when the file format is
 * not supported.
 * 
 * @author David Withers
 */
public class SpreadsheetReadException extends Exception {

	private static final long serialVersionUID = -823966225836697180L;

	/**
	 * Constructs a new SpreadsheetReadException with null as its detail message.
	 */
	public SpreadsheetReadException() {
	}

	/**
	 * Constructs a new SpreadsheetReadException with the specified detail message.
	 * 
	 * @param message
	 */
	public SpreadsheetReadException(String message) {
		super(message);
	}

	/**
	 * Constructs a new SpreadsheetReadException with the specified cause and a detail message of
	 * (cause==null ? null : cause.toString()) (which typically contains the class and detail
	 * message of cause).
	 * 
	 * @param cause
	 */
	public SpreadsheetReadException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructs a new SpreadsheetReadException with the specified detail message and cause.
	 * 
	 * @param message
	 * @param cause
	 */
	public SpreadsheetReadException(String message, Throwable cause) {
		super(message, cause);
	}

}
