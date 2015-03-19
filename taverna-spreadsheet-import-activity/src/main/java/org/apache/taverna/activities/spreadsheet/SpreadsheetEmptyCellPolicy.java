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
 * Enumeration of the policies for handling empty/missing cells in a spreadsheet.
 * <p>
 * <dl>
 * <dt>EMPTY_STRING</dt>
 * <dd>Use an empty string value ("")</dd>
 * <dt>USER_DEFINED</dt>
 * <dd>Use a value defined by the user</dd>
 * <dt>GENERATE_ERROR</dt>
 * <dd>Generate an ErrorDocument</dd>
 * </dl>
 * 
 * @author David Withers
 */
public enum SpreadsheetEmptyCellPolicy {
	EMPTY_STRING, USER_DEFINED, GENERATE_ERROR
}