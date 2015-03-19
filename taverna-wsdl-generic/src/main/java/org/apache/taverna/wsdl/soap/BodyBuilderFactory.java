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
import org.apache.taverna.wsdl.parser.UnknownOperationException;
import org.apache.taverna.wsdl.parser.WSDLParser;

/**
 * Factory that creates an appropriate BodyBuilder according to the provided WSDLProcessors style and use.
 * @author Stuart Owen
 *
 */
public class BodyBuilderFactory {
	
	private static BodyBuilderFactory instance = new BodyBuilderFactory();
	
	public static BodyBuilderFactory instance() {
		return instance;
	}
	
	public BodyBuilder create(WSDLParser parser, String operationName, List<TypeDescriptor> inputDescriptors) throws UnknownOperationException {
		String use = parser.getUse(operationName);
		String style = parser.getStyle(operationName);
		if (use.equals("encoded")) {
			return new EncodedBodyBuilder(style, parser,operationName, inputDescriptors);
		}
		else if (use.equals("literal")) {
			return new LiteralBodyBuilder(style,parser,operationName, inputDescriptors);
		}
		return new LiteralBodyBuilder(style,parser,operationName, inputDescriptors);
	}
}
