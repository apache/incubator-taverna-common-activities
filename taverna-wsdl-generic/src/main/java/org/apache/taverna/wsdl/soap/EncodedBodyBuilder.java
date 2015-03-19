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
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.wsdl.WSDLException;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import org.apache.taverna.wsdl.parser.ArrayTypeDescriptor;
import org.apache.taverna.wsdl.parser.BaseTypeDescriptor;
import org.apache.taverna.wsdl.parser.TypeDescriptor;
import org.apache.taverna.wsdl.parser.UnknownOperationException;
import org.apache.taverna.wsdl.parser.WSDLParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

@SuppressWarnings("unchecked")
public class EncodedBodyBuilder extends AbstractBodyBuilder {
	public EncodedBodyBuilder(String style, WSDLParser parser, String operationName, List<TypeDescriptor> inputDescriptors) {
		super(style, parser,operationName,inputDescriptors);
	}

	@Override
	protected Use getUse() {
		return Use.ENCODED;
	}

	@Override
	public SOAPElement build(Map inputMap) throws WSDLException,
			ParserConfigurationException, SOAPException, IOException,
			SAXException, UnknownOperationException {

		SOAPElement result = super.build(inputMap);
                
		for (Iterator iterator = namespaceMappings.keySet().iterator(); iterator.hasNext();) {
			String namespaceURI = (String) iterator.next();
			String ns = namespaceMappings.get(namespaceURI);
			result.addNamespaceDeclaration(ns, namespaceURI);
		}
                
                result.setAttributeNS(SOAPConstants.URI_NS_SOAP_1_1_ENVELOPE, "soapenv:encodingStyle", SOAPConstants.URI_NS_SOAP_ENCODING);

		return result;
	}

	@Override
	protected Element createSkeletonElementForSingleItem(
			Map<String, String> namespaceMappings, TypeDescriptor descriptor,
			String inputName, String typeName) {
            
                Element el = createElementNS(null, inputName);

                String ns = namespaceMappings.get(descriptor.getNamespaceURI());
                if (ns != null) {
                        el.setAttributeNS(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, "xsi:type", ns + ":" + descriptor.getType());
                }
                return el;
	}

	@Override
	protected Element createElementForArrayType(
			Map<String, String> namespaceMappings, String inputName,
			Object dataValue, TypeDescriptor descriptor, String mimeType,
			String typeName) throws ParserConfigurationException, SAXException,
			IOException, UnknownOperationException {
		DocumentBuilderFactory builderFactory = DocumentBuilderFactory
				.newInstance();
		builderFactory.setNamespaceAware(true);
		DocumentBuilder docBuilder = builderFactory.newDocumentBuilder();

		ArrayTypeDescriptor arrayDescriptor = (ArrayTypeDescriptor) descriptor;
		TypeDescriptor elementType = arrayDescriptor.getElementType();
		int size = 0;

                Element el = createElementNS(null, inputName);
                
		if (dataValue instanceof List) {
			List dataValues = (List) dataValue;
			size = dataValues.size();
			populateElementWithList(mimeType, el, dataValues, elementType);
		} else {
			// if mime type is text/xml then the data is an array in xml form,
			// else its just a single primitive element
			if (mimeType.equals("'text/xml'")) {

				Document doc = docBuilder.parse(new ByteArrayInputStream(
						dataValue.toString().getBytes()));
				Node child = doc.getDocumentElement().getFirstChild();

				while (child != null) {
					size++;
					el.appendChild(el.getOwnerDocument()
							.importNode(child, true));
					child = child.getNextSibling();
				}
			} else {
				String tag = "item";
				if (elementType instanceof BaseTypeDescriptor) {
					tag = elementType.getType();
				} else {
					tag = elementType.getName();
				}
				Element item = el.getOwnerDocument().createElement(tag);
				populateElementWithObjectData(mimeType, item, dataValue, descriptor);
				el.appendChild(item);
			}

		}

		String ns = namespaceMappings.get(elementType.getNamespaceURI());
		if (ns != null) {
			String elementNS = ns + ":" + elementType.getType() + "[" + size + "]";
			el.setAttributeNS(SOAPConstants.URI_NS_SOAP_ENCODING, "soapenc:arrayType", elementNS);
		}

		el.setAttributeNS(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, "xsi:type", "soapenc:Array");

		return el;
	}

	@Override
	protected SOAPElement addElementToBody(String operationNamespace, SOAPElement body, Element el) throws SOAPException {
            SOAPElement child = SOAPFactory.newInstance().createElement(el);
            body.addChildElement(child);
            return body;
	}

	
}
