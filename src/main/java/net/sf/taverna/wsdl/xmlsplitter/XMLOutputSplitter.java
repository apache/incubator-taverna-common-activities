/*
 * Copyright (C) 2003 The University of Manchester 
 *
 * Modifications to the initial code base are copyright of their
 * respective authors, or their employers as appropriate.  Authorship
 * of the modifications may be determined from the ChangeLog placed at
 * the end of this file.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA.
 *
 ****************************************************************
 * Source code information
 * -----------------------
 * Filename           $RCSfile: XMLOutputSplitter.java,v $
 * Revision           $Revision: 1.2 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2008/08/08 10:28:07 $
 *               by   $Author: stain $
 * Created on 16-May-2006
 *****************************************************************/
package net.sf.taverna.wsdl.xmlsplitter;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.taverna.wsdl.parser.ArrayTypeDescriptor;
import net.sf.taverna.wsdl.parser.BaseTypeDescriptor;
import net.sf.taverna.wsdl.parser.ComplexTypeDescriptor;
import net.sf.taverna.wsdl.parser.TypeDescriptor;

import org.apache.axis.encoding.Base64;
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
				executeForComplexType(result, outputNameList, children);
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
			List<String> outputNameList, List<Element> children)
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
						
						byte[] data = Base64.decode(child
								.getText());
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
