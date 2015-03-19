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

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.taverna.wsdl.parser.TypeDescriptor;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * A response parser specifically for literal use services that return primative types.
 * It extends the SOAPReponseLiteralParser, but unwraps the result from the enclosing XML
 * to expose the primitive result.
 * 
 * This is specially designed for unwrapped/literal type services, and RPC/literal services (untested). 
 * @author Stuart
 *
 */
@SuppressWarnings("unchecked")
public class SOAPResponsePrimitiveLiteralParser extends
		SOAPResponseLiteralParser {

	public SOAPResponsePrimitiveLiteralParser(List<TypeDescriptor> outputDescriptors) {
		super(outputDescriptors);
	}

	@Override
	public Map parse(List response) throws Exception {
		Map result = super.parse(response);
		Object dataValue = result.get(getOutputName());
		if (dataValue!=null) {
			String xml = dataValue.toString();
			
			DocumentBuilder builder = DocumentBuilderFactory.newInstance()
			.newDocumentBuilder();
			Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes()));
		
			Node node = doc.getFirstChild();
			result.put(getOutputName(), node.getFirstChild().getNodeValue());
		}
		return result;
	}
	
	
}

	
