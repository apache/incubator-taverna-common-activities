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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.soap.SOAPElement;

import org.apache.taverna.wsdl.parser.TypeDescriptor;

/**
 * Responsible for parsing the SOAP response from calling a Literal based
 * service.
 * 
 * @author sowen
 * 
 */
@SuppressWarnings("unchecked")
public class SOAPResponseLiteralParser extends AbstractSOAPResponseParser {

	List<TypeDescriptor>outputDescriptors;

	public SOAPResponseLiteralParser(List<TypeDescriptor> outputDescriptors) {
		this.outputDescriptors = outputDescriptors;
	}

	/**
	 * Expects a list containing a single SOAPBodyElement, the contents of which
	 * are transferred directly to the output, converted to a String, and placed
	 * into the outputMaP which is returned
	 * 
	 * @return Map of the outputs
	 */
    @Override
	public Map parse(List response) throws Exception {
		Map result = new HashMap();

		if (response.size() > 0) {
			SOAPElement rpcElement = (SOAPElement) response.get(0);
	
			String outputName = getOutputName();
			String xml = toString(rpcElement);
	
			result.put(outputName, xml);
		}

		return result;
	}

	protected String getOutputName() {
		String result = "";
		for (TypeDescriptor descriptor : outputDescriptors) {
			String name=descriptor.getName();
			if (!name.equals("attachmentList")) {
				result = name;
				break;
			}
		}
		return result;
	}
}
