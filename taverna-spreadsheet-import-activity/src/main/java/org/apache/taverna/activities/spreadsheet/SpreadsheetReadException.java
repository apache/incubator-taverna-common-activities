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
