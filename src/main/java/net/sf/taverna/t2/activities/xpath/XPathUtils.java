/*******************************************************************************
 * Copyright (C) 2013 The University of Manchester
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
package net.sf.taverna.t2.activities.xpath;

import org.dom4j.DocumentHelper;
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
		if (xpathExpressionToValidate == null || xpathExpressionToValidate.trim().length() == 0) {
			return (0);
		}

		try {
			// try to parse the XPath expression...
			DocumentHelper.createXPath(xpathExpressionToValidate.trim());
			// ...success
			return (1);
		} catch (InvalidXPathException e) {
			// ...failed to parse the XPath expression: notify of the error
			return (-1);
		}
	}

	/**
	 * Tests validity of the configuration held.
	 *
	 * @return <code>true</code> if the configuration in the bean is valid; <code>false</code>
	 *         otherwise.
	 */
	public static boolean isValid(JsonNode json) {
		return (json.has("xpathExpression")
				&& validateXPath(json.get("xpathExpression").textValue()) == XPATH_VALID
				&& json.has("xpathNamespaceMap"));
	}

}
