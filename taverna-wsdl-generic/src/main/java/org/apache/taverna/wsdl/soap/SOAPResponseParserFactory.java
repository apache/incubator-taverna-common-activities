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

package org.apache.taverna.wsdl.soap;

import java.util.List;

import org.apache.taverna.wsdl.parser.TypeDescriptor;

/**
 * A factory class that selects the correct type of SOAPResponseParser according
 * to the service type , the types output of that service, and the response from
 * invoking that service.
 * 
 * @author sowen
 * 
 */
@SuppressWarnings("unchecked")
public class SOAPResponseParserFactory {

	private static SOAPResponseParserFactory instance = new SOAPResponseParserFactory();

	public static SOAPResponseParserFactory instance() {
		return instance;
	}

	/**
	 * returns an instance of the appropriate type of SOAPResponseParser
	 * 
	 * @param response -
	 *            List of SOAPBodyElement's resulting from the service
	 *            invokation.
	 * @param use -
	 *            the type of the service - 'literal' or 'encoded'
	 * @param style -
	 *            the style of the service - 'document' or 'rpc'
	 * @param outputDescriptors -
	 *            the List of {@link TypeDescriptor}'s describing the service outputs
	 * @return
	 * @see SOAPResponseParser
	 */
	public SOAPResponseParser create(List response, String use, String style,
			List<TypeDescriptor> outputDescriptors) {

		SOAPResponseParser result = null;
		
		if (outputIsPrimitive(outputDescriptors)) {
			if (use.equalsIgnoreCase("literal")) {
				result = new SOAPResponsePrimitiveLiteralParser(outputDescriptors);
			}
			else {
				result = new SOAPResponsePrimitiveParser(outputDescriptors);
			}
		} else if (use.equals("literal")) {
			result = new SOAPResponseLiteralParser(outputDescriptors);
		} else {
			if (response.size() > 1) {
				result = new SOAPResponseEncodedMultiRefParser(outputDescriptors);
			} else {
				result = new SOAPResponseEncodedParser(outputDescriptors);
			}
		}

		return result;
	}

	private boolean outputIsPrimitive(List<TypeDescriptor> outputDescriptors) {
		boolean result = true;
		for (TypeDescriptor d : outputDescriptors) {
			if (d.getMimeType().equals("'text/xml'")) {
				result = false;
				break;
			}
		}
		return result;
	}

}
