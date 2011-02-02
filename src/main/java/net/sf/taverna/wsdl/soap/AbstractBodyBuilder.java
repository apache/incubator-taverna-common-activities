/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester   
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
package net.sf.taverna.wsdl.soap;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.wsdl.WSDLException;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.SOAPException;

import net.sf.taverna.wsdl.parser.ArrayTypeDescriptor;
import net.sf.taverna.wsdl.parser.BaseTypeDescriptor;
import net.sf.taverna.wsdl.parser.ComplexTypeDescriptor;
import net.sf.taverna.wsdl.parser.TypeDescriptor;
import net.sf.taverna.wsdl.parser.UnknownOperationException;
import net.sf.taverna.wsdl.parser.WSDLParser;

import org.apache.axis.encoding.Base64;
import org.apache.axis.message.SOAPBodyElement;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
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

	public SOAPBodyElement build(Map inputMap) throws WSDLException,
			ParserConfigurationException, SOAPException, IOException,
			SAXException, UnknownOperationException {

		List inputs = parser.getOperationInputParameters(operationName);

		namespaceMappings = generateNamespaceMappings(inputs);

		QName operationQname = getOperationQname();
		
		SOAPBodyElement body = new SOAPBodyElement(operationQname);

		// its important to preserve the order of the inputs!
		for (Iterator iterator = inputs.iterator(); iterator.hasNext();) {
			TypeDescriptor descriptor = (TypeDescriptor) iterator.next();
			String inputName = descriptor.getName();
			Object dataValue = inputMap.get(inputName);

			body = createBodyElementForData(operationName, namespaceMappings,
					operationQname.getNamespaceURI(), body, descriptor, inputName, dataValue);
		}

		return body;
	}



	protected SOAPBodyElement createBodyElementForData(String operationName,
			Map<String, String> namespaceMappings, String operationNamespace,
			SOAPBodyElement body, TypeDescriptor descriptor, String inputName,
			Object dataValue) throws ParserConfigurationException,
			SAXException, IOException, UnknownOperationException, SOAPException {
		if (dataValue != null) {
			String mimeType = getMimeTypeForInputName(inputName);
			String typeName = descriptor.getType();

			Element el = null;

			if (descriptor instanceof ArrayTypeDescriptor) {
				el = createElementForArrayType(namespaceMappings, inputName,
						dataValue, descriptor, mimeType, typeName);

			} else {
				el = createSkeletonElementForSingleItem(namespaceMappings,
						descriptor, inputName, typeName);
				populateElementWithObjectData(mimeType, el, dataValue);
			}

			body = addElementToBody(operationNamespace, body, el);
		}
		return body;
	}

	protected abstract SOAPBodyElement addElementToBody(
			String operationNamespace, SOAPBodyElement body, Element el)
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
			populateElementWithObjectData(mimeType, item, dataItem);
			element.appendChild(item);
		}
	}

	/**
	 * Populates a DOM XML Element with dataValue according to its mimetype
	 * 
	 * @param mimeType
	 * @param element
	 * @param dataValue
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	protected void populateElementWithObjectData(String mimeType,
			Element element, Object dataValue)
			throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		DocumentBuilder builder = factory.newDocumentBuilder();

		if (mimeType.equals("'text/xml'")) {
			Document doc = builder.parse(new ByteArrayInputStream(dataValue
					.toString().getBytes()));
			Node child = doc.getDocumentElement().getFirstChild();

			while (child != null) {
				element.appendChild(element.getOwnerDocument().importNode(
						child, true));
				child = child.getNextSibling();
			}
		} else if (mimeType.equals("'application/octet-stream'")
				&& dataValue instanceof byte[]) {
			String encoded = Base64.encode((byte[]) dataValue);
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
}
