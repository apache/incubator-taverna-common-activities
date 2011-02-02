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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.wsdl.WSDLException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.SOAPException;

import net.sf.taverna.wsdl.parser.ArrayTypeDescriptor;
import net.sf.taverna.wsdl.parser.BaseTypeDescriptor;
import net.sf.taverna.wsdl.parser.TypeDescriptor;
import net.sf.taverna.wsdl.parser.UnknownOperationException;
import net.sf.taverna.wsdl.parser.WSDLParser;

import org.apache.axis.message.SOAPBodyElement;
import org.apache.axis.utils.XMLUtils;
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
	public SOAPBodyElement build(Map inputMap) throws WSDLException,
			ParserConfigurationException, SOAPException, IOException,
			SAXException, UnknownOperationException {

		SOAPBodyElement result = super.build(inputMap);
		for (Iterator iterator = namespaceMappings.keySet().iterator(); iterator
				.hasNext();) {
			String namespaceURI = (String) iterator.next();
			String ns = namespaceMappings.get(namespaceURI);
			result.addNamespaceDeclaration(ns, namespaceURI);
		}
		result.setAttribute("soapenv:encodingStyle",
				"http://schemas.xmlsoap.org/soap/encoding/");
		return result;
	}

	@Override
	protected Element createSkeletonElementForSingleItem(
			Map<String, String> namespaceMappings, TypeDescriptor descriptor,
			String inputName, String typeName) {
		Element el = XMLUtils.StringToElement("", inputName, "");

		String ns = namespaceMappings.get(descriptor.getNamespaceURI());
		if (ns != null) {
			el.setAttribute("xsi:type", ns + ":" + descriptor.getType());
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

		Element el;
		ArrayTypeDescriptor arrayDescriptor = (ArrayTypeDescriptor) descriptor;
		TypeDescriptor elementType = arrayDescriptor.getElementType();
		int size = 0;

		el = XMLUtils.StringToElement("", inputName, "");

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
				populateElementWithObjectData(mimeType, item, dataValue);
				el.appendChild(item);
			}

		}

		String ns = namespaceMappings.get(elementType.getNamespaceURI());
		if (ns != null) {
			String elementNS = ns + ":" + elementType.getType() + "[" + size
					+ "]";
			el.setAttribute("soapenc:arrayType", elementNS);
			el.setAttribute("xmlns:soapenc",
					"http://schemas.xmlsoap.org/soap/encoding/");
		}

		el.setAttribute("xsi:type", "soapenc:Array");

		return el;
	}

	@Override
	protected SOAPBodyElement addElementToBody(String operationNamespace, SOAPBodyElement body, Element el) throws SOAPException {
		body.addChildElement(new SOAPBodyElement(el));
		return body;
	}

	
}
