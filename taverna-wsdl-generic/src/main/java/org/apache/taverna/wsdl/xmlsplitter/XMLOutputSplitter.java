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

package org.apache.taverna.wsdl.xmlsplitter;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.taverna.wsdl.parser.ArrayTypeDescriptor;
import org.apache.taverna.wsdl.parser.BaseTypeDescriptor;
import org.apache.taverna.wsdl.parser.ComplexTypeDescriptor;
import org.apache.taverna.wsdl.parser.TypeDescriptor;

import javax.xml.bind.DatatypeConverter;

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

public class XMLOutputSplitter {

	private TypeDescriptor typeDescriptor;
	private String[] outputNames;
	private String[] inputNames;
	private String[] outputTypes;

	public XMLOutputSplitter(TypeDescriptor typeDescriptor,
			String[] outputNames, String[] outputTypes,String[] inputNames) {
		this.typeDescriptor = typeDescriptor;
		this.outputNames = outputNames;
		this.inputNames = inputNames;
		this.outputTypes = outputTypes;
	}

	@SuppressWarnings("unchecked")
	public Map<String, Object> execute(Map<String, String> inputMap)
			throws XMLSplitterExecutionException {

		Map<String, Object> result = new HashMap<String, Object>();
		List<String> outputNameList = Arrays.asList(outputNames);

		String xml = inputMap.get(inputNames[0]);
		try {
			Document doc = new SAXBuilder().build(new StringReader(xml));
			List<Element> children = doc.getRootElement().getChildren();
			if (typeDescriptor instanceof ArrayTypeDescriptor) {
				if (outputNames.length > 1)
					throw new XMLSplitterExecutionException(
							"Unexpected, multiple output names for ArrayType");
				executeForArrayType(result, children);
			} else {
				executeForComplexType(result, outputNameList, children, doc.getRootElement().getAttributes());
			}

			// populate missing outputs with empty strings for basic types,
			// empty elements for complex/array types.
			for (int i = 0; i < outputNames.length; i++) {
				if (result.get(outputNames[i]) == null) {
					if (outputTypes[i].equals("'text/xml'")) {
						result
								.put(outputNames[i], "<" + outputNames[i]
										+ " />");
					} else if (outputTypes[i].startsWith("l('")) {
						result.put(outputNames[i], new ArrayList<Object>());
					} else {
						result.put(outputNames[i], "");
					}

				}
			}
		} catch (JDOMException e) {
			throw new XMLSplitterExecutionException("Unable to parse XML: " + xml, e);
		} catch (IOException e) {
			throw new XMLSplitterExecutionException("IOException parsing XML: " + xml,
					e);
		}

		return result;
	}

	private void executeForArrayType(Map<String, Object> result,
			List<Element> children) {
		ArrayTypeDescriptor arrayDescriptor = (ArrayTypeDescriptor) typeDescriptor;
		List<String> values = new ArrayList<String>();
		XMLOutputter outputter = new XMLOutputter();

		boolean isInnerBaseType = arrayDescriptor.getElementType() instanceof BaseTypeDescriptor;
		if (isInnerBaseType) {
			values = extractBaseTypeArrayFromChildren(children);
		} else {
			for (Element child : children) {
				values.add(outputter.outputString(child));
			}
		}
		result.put(outputNames[0], values);
	}

	@SuppressWarnings({ "unchecked" })
	private void executeForComplexType(Map<String, Object> result,
			List<String> outputNameList, List<Element> children, List<Attribute> list)
			throws IOException {               

		XMLOutputter outputter = new XMLOutputter();
		for (Element child : children) {
			
			if (outputNameList.contains(child.getName())) {
				int i = outputNameList.indexOf(child.getName());
				TypeDescriptor descriptorForChild = ((ComplexTypeDescriptor) typeDescriptor)
						.elementForName(outputNames[i]);
				if (outputTypes[i].startsWith("l(")
						&& descriptorForChild instanceof ArrayTypeDescriptor
						&& !((ArrayTypeDescriptor) descriptorForChild)
								.isWrapped()) {
					boolean isXMLContent = outputTypes[i].contains("text/xml");
					result.put(child.getName(), extractDataListFromChildList(
							children, isXMLContent));
                    break;
				} else {
					if (outputTypes[i].equals("'text/xml'")
							|| outputTypes[i].equals("l('text/xml')")) {
						String xmlText = outputter.outputString(child);
						result.put(child.getName(), xmlText);
					} else if (outputTypes[i]
							.equals("'application/octet-stream'")) { // base64Binary
						
						byte[] data = DatatypeConverter.parseBase64Binary(child.getText());
						result.put(child.getName(), data);
					} else if (outputTypes[i].equals("l('text/plain')")) { // an
																			// inner
																			// element
																			// containing
																			// a
																			// list
						result.put(child.getName(),
								extractBaseTypeArrayFromChildren(child
										.getChildren()));
					} else {
						result.put(child.getName(), child.getText());
					}
				}
			}
		}
		for (Attribute attribute : list) {
			if (outputNameList.contains("1" + attribute.getName())) {
				result.put("1" + attribute.getName(), attribute.getValue());
			} else if (outputNameList.contains(attribute.getName())) {
				result.put(attribute.getName(), attribute.getValue());
			}
		}
	}

	private List<String> extractDataListFromChildList(List<Element> children,
			boolean isXMLContent) {
		List<String> result = new ArrayList<String>();
		XMLOutputter outputter = new XMLOutputter();
		for (Element child : children) {
			if (!isXMLContent) {
				result.add(child.getTextTrim());
			} else {
				result.add(outputter.outputString(child));
			}
		}
		return result;
	}

	private List<String> extractBaseTypeArrayFromChildren(List<Element> children) {
		List<String> result = new ArrayList<String>();
		for (Element child : children) {
			result.add(child.getTextTrim());
		}
		return result;
	}

}
