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

package org.apache.taverna.activities.xpath;

import static org.dom4j.DocumentHelper.createXPath;

import org.dom4j.InvalidXPathException;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Utility methods for validating xpath expressions.
 * 
 * @author David Withers
 */
public class XPathUtils {

	public static final int XPATH_VALID = 1;
	public static final int XPATH_EMPTY = 0;
	public static final int XPATH_INVALID = -1;

	/**
	 * Validates an XPath expression.
	 * 
	 * @return {@link XPathActivityConfigurationBean#XPATH_VALID XPATH_VALID} -
	 *         if the expression is valid;<br/>
	 *         {@link XPathActivityConfigurationBean#XPATH_EMPTY XPATH_EMPTY} -
	 *         if expression is empty;<br/>
	 *         {@link XPathActivityConfigurationBean#XPATH_INVALID
	 *         XPATH_INVALID} - if the expression is invalid / ill-formed.<br/>
	 */
	public static int validateXPath(String xpathExpressionToValidate) {
		// no XPath expression
		if (xpathExpressionToValidate == null
				|| xpathExpressionToValidate.trim().isEmpty()) {
			return XPATH_EMPTY;
		}

		try {
			// try to parse the XPath expression...
			createXPath(xpathExpressionToValidate.trim());
			// ...success
			return XPATH_VALID;
		} catch (InvalidXPathException e) {
			// ...failed to parse the XPath expression: notify of the error
			return XPATH_INVALID;
		}
	}

	/**
	 * Tests validity of the configuration held.
	 * 
	 * @return <code>true</code> if the configuration in the bean is valid;
	 *         <code>false</code> otherwise.
	 */
	public static boolean isValid(JsonNode json) {
		return (json.has("xpathExpression")
				&& validateXPath(json.get("xpathExpression").textValue()) == XPATH_VALID && json
					.has("xpathNamespaceMap"));
	}
}
