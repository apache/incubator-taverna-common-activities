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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.wsdl.WSDLException;
import javax.xml.bind.DatatypeConverter;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import org.apache.taverna.wsdl.parser.ArrayTypeDescriptor;
import org.apache.taverna.wsdl.parser.BaseTypeDescriptor;
import org.apache.taverna.wsdl.parser.ComplexTypeDescriptor;
import org.apache.taverna.wsdl.parser.TypeDescriptor;
import org.apache.taverna.wsdl.parser.UnknownOperationException;
import org.apache.taverna.wsdl.parser.WSDLParser;
import org.apache.log4j.Logger;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

@SuppressWarnings("unchecked")
public abstract class AbstractBodyBuilder implements BodyBuilder {
	
	private static Logger logger = Logger.getLogger(AbstractBodyBuilder.class);

	private String style;
	private WSDLParser parser;
	private String operationName;

	protected enum Style {
		DOCUMENT, RPC
	};

	protected enum Use {
		LITERAL, ENCODED
	};

	protected Map<String, String> namespaceMappings;
	protected List<TypeDescriptor> inputDescriptors;

	public AbstractBodyBuilder(String style, WSDLParser parser,
			String operationName, List<TypeDescriptor> inputDescriptors) {
		this.style = style;
		this.parser = parser;
		this.operationName = operationName;
		this.inputDescriptors = inputDescriptors;
	}

	protected Style getStyle() {
		Style result = Style.DOCUMENT;
		if (style.equalsIgnoreCase("rpc")) {
			result = Style.RPC;
		} else if (style.equalsIgnoreCase("document")) {
			result = Style.DOCUMENT;
		}
		return result;
	}

	protected abstract Use getUse();

	/**
	 * 
	 * @return the namespace for the operation
	 */
	private String getOperationNamespace() throws UnknownOperationException {
		return parser.getOperationNamespaceURI(operationName);
	}
	
	private QName getOperationQname() throws UnknownOperationException {
		return parser.getOperationQname(operationName);	
	}

    @Override
	public SOAPElement build(Map inputMap) throws WSDLException,
			ParserConfigurationException, SOAPException, IOException,
			SAXException, UnknownOperationException {

		List<TypeDescriptor> inputs = parser.getOperationInputParameters(operationName);

		namespaceMappings = generateNamespaceMappings(inputs);

		QName operationQname = getOperationQname();
		
                String ns = namespaceMappings.get(operationQname.getNamespaceURI());
                
		SOAPElement body = ns == null ? SOAPFactory.newInstance().createElement(operationQname) :
                                                SOAPFactory.newInstance().createElement(operationQname.getLocalPart(), ns, operationQname.getNamespaceURI());

		// its important to preserve the order of the inputs!
		for (Iterator<TypeDescriptor> iterator = inputs.iterator(); iterator.hasNext();) {
			TypeDescriptor descriptor = iterator.next();
			String inputName = descriptor.getName();
			Object dataValue = inputMap.get(inputName);

			body = createBodyElementForData(operationName, namespaceMappings,
					operationQname.getNamespaceURI(), body, descriptor, inputName, dataValue);
		}

		return body;
	}



	protected SOAPElement createBodyElementForData(String operationName,
			Map<String, String> namespaceMappings, String operationNamespace,
			SOAPElement body, TypeDescriptor descriptor, String inputName,
			Object dataValue) throws ParserConfigurationException,
			SAXException, IOException, UnknownOperationException, SOAPException {
		if (dataValue != null) {
			String mimeType = getMimeTypeForInputName(inputName);
			String typeName = descriptor.getType();

			Element el;
			if (descriptor instanceof ArrayTypeDescriptor) {
				el = createElementForArrayType(namespaceMappings, inputName,
						dataValue, descriptor, mimeType, typeName);

			} else {
				el = createSkeletonElementForSingleItem(namespaceMappings,
						descriptor, inputName, typeName);
				populateElementWithObjectData(mimeType, el, dataValue, descriptor);
			}

			body = addElementToBody(operationNamespace, body, el);
		}
		return body;
	}

	protected abstract SOAPElement addElementToBody(
			String operationNamespace, SOAPElement body, Element el)
			throws SOAPException;

	protected abstract Element createSkeletonElementForSingleItem(
			Map<String, String> namespaceMappings, TypeDescriptor descriptor,
			String inputName, String typeName);

	/**
	 * generates an XML DOM Element for an array
	 * 
	 * @param namespaceMappings
	 * @param inputName
	 * @param dataValue
	 * @param descriptor
	 * @param mimeType
	 * @param typeName
	 * @return
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	protected abstract Element createElementForArrayType(
			Map<String, String> namespaceMappings, String inputName,
			Object dataValue, TypeDescriptor descriptor, String mimeType,
			String typeName) throws ParserConfigurationException, SAXException,
			IOException, UnknownOperationException;

	/**
	 * Populates a DOM XML Element with the contents of a List of dataValues
	 * 
	 * @param mimeType -
	 *            the mime type of the data
	 * @param element -
	 *            the Element to be populated
	 * @param dataValues -
	 *            the List of Objects containing the data
	 * @param elementType -
	 *            the TypeDescriptor for the element being populated
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	protected void populateElementWithList(String mimeType, Element element,
			List dataValues, TypeDescriptor elementType)
			throws ParserConfigurationException, SAXException, IOException {
		for (Iterator dataIterator = dataValues.iterator(); dataIterator
				.hasNext();) {
			Object dataItem = dataIterator.next();
			String tag;
			if (elementType instanceof BaseTypeDescriptor) {
				tag = elementType.getType();
			} else {
				tag = elementType.getName();
			}

			Element item = element.getOwnerDocument().createElement(tag);
			populateElementWithObjectData(mimeType, item, dataItem, elementType);
			element.appendChild(item);
		}
	}

	/**
	 * Populates a DOM XML Element with dataValue according to its mimetype
	 * 
	 * @param mimeType
	 * @param element
	 * @param dataValue
	 * @param descriptor 
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	protected void populateElementWithObjectData(String mimeType,
			Element element, Object dataValue, TypeDescriptor descriptor)
			throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		DocumentBuilder builder = factory.newDocumentBuilder();

		if (mimeType.equals("'text/xml'")) {
			Document doc = builder.parse(new ByteArrayInputStream(dataValue
					.toString().getBytes()));
			Element documentElement = doc.getDocumentElement();
			
			if (descriptor instanceof ComplexTypeDescriptor) {
				ComplexTypeDescriptor complexType = (ComplexTypeDescriptor) descriptor;
				NamedNodeMap attributes = documentElement.getAttributes();
				if (attributes != null) {
					for (int i = 0; i < attributes.getLength(); i++) {
						Node attributeNode = attributes.item(i);
						if (attributeNode instanceof Attr) {
							Attr attribute = (Attr) attributeNode;
							TypeDescriptor typeDescriptor = complexType.attributeForName(attribute.getName());
							if (typeDescriptor != null) {
								element.setAttributeNS(typeDescriptor.getNamespaceURI(), typeDescriptor.getName(), attribute.getValue());
							}
						}
					}
				}
			}
			
			Node child = documentElement.getFirstChild();

			while (child != null) {
				element.appendChild(element.getOwnerDocument().importNode(
						child, true));
				child = child.getNextSibling();
			}
		} else if (mimeType.equals("'application/octet-stream'")
				&& dataValue instanceof byte[]) {
                        String encoded = DatatypeConverter.printBase64Binary((byte[]) dataValue);
			element.appendChild(element.getOwnerDocument().createTextNode(
					encoded));
		} else {
			element.appendChild(element.getOwnerDocument().createTextNode(
					dataValue.toString()));
		}
	}

	/**
	 * Provides the mime type for a given input
	 * 
	 * @param inputName
	 * @return
	 */
	protected String getMimeTypeForInputName(String inputName) {
		for (TypeDescriptor desc : inputDescriptors) {
			if (desc.getName().equals(inputName))
				return desc.getMimeType();
		}
		return "";
	}

	/**
	 * Generates a map of all the namespaces for the operation and all of the
	 * types required to call the operation. Namesspace prefixes (the key) start
	 * with ns1 representing the operation, and continue incrementally for all
	 * additional namespaces (ns2, ns3 ... etc).
	 * 
	 * @return
	 * @param inputs -
	 *            List of input TypeDescriptor's
	 * @throws UnknownOperationException
	 * @throws IOException
	 */
	protected Map<String, String> generateNamespaceMappings(List inputs)
			throws UnknownOperationException, IOException {
		Map<String, String> result = new HashMap<String, String>();
		int nsCount = 2;

		result.put(getOperationNamespace(), "ns1");
		result.put("http://www.w3.org/2001/XMLSchema", "xsd");
		result.put("http://www.w3.org/2001/XMLSchema-instance", "xsi");

		for (Iterator iterator = inputs.iterator(); iterator.hasNext();) {
			TypeDescriptor descriptor = (TypeDescriptor) iterator.next();
			nsCount = mapNamespace(descriptor, new ArrayList<TypeDescriptor>(),result, nsCount);

		}

		return result;
	}

	/**
	 * creates a namespace prefix and adds the namespace to the namespaceMap for
	 * a TypeDescriptor. Further recursive calls are made if this type contains
	 * addition inner elements that are not already mapped.
	 * 
	 * @param descriptor
	 * @param namespaceMap
	 * @param nsCount
	 * @return
	 */
	protected int mapNamespace(TypeDescriptor descriptor, List<TypeDescriptor> visitedDescriptors,
			Map<String, String> namespaceMap, int nsCount) {
		if (!visitedDescriptors.contains(descriptor)) {
			visitedDescriptors.add(descriptor);
			String namespace = descriptor.getNamespaceURI();
			if (namespace != null && namespace.length() > 0
					&& !namespaceMap.containsKey(namespace)) {
				namespaceMap.put(namespace, "ns" + nsCount);
				nsCount++;
			}
	
			if (descriptor instanceof ArrayTypeDescriptor) {
				nsCount = mapNamespace(((ArrayTypeDescriptor) descriptor)
						.getElementType(),visitedDescriptors, namespaceMap, nsCount);
			} else if (descriptor instanceof ComplexTypeDescriptor) {
				List elements = ((ComplexTypeDescriptor) descriptor).getElements();
				for (Iterator iterator = elements.iterator(); iterator.hasNext();) {
					nsCount = mapNamespace((TypeDescriptor) iterator.next(),visitedDescriptors,
							namespaceMap, nsCount);
				}
			}
		}
		else {
			logger.error("The descriptor: "+descriptor+" is appears to be part of a cyclic schema. Bailing out of mapping namespace.");
		}

		return nsCount;
	}
        
        protected Element createElementNS(String namespace, String name) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            try {
                DocumentBuilder builder = factory.newDocumentBuilder();
                return builder.newDocument().createElementNS(namespace, name);
            } catch (ParserConfigurationException e) {
                return null;
            }
        }
}
