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

import org.apache.axis.message.MessageElement;
import org.apache.axis.message.SOAPBodyElement;
import org.apache.axis.utils.XMLUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * An implementation of BodyBuilder that supports creating the SOAP body for
 * Webservices based upon a WSDL with Literal style.
 * 
 * @author Stuart Owen
 * @author Stian Soiland-Reyes
 * 
 */
@SuppressWarnings("unchecked")
public class LiteralBodyBuilder extends AbstractBodyBuilder {

	private static Logger logger = Logger.getLogger(LiteralBodyBuilder.class);

	private static final String TYPE = "type";
	private static final String NS_XSI = "http://www.w3.org/2001/XMLSchema-instance";

	public LiteralBodyBuilder(String style, WSDLParser parser, String operationName, List<TypeDescriptor> inputDescriptors) {
		super(style, parser, operationName,inputDescriptors);
	}

	@Override
	protected Use getUse() {
		return Use.LITERAL;
	}

	@Override
	public SOAPBodyElement build(Map inputMap) throws WSDLException,
			ParserConfigurationException, SOAPException, IOException,
			SAXException, UnknownOperationException {

		SOAPBodyElement body = super.build(inputMap);

		if (getStyle() == Style.DOCUMENT) {
			fixTypeAttributes(body);
		}

		return body;
	}

	@Override
	protected Element createSkeletonElementForSingleItem(
			Map<String, String> namespaceMappings, TypeDescriptor descriptor,
			String inputName, String typeName) {
		if (getStyle()==Style.DOCUMENT) {
			return XMLUtils.StringToElement("", descriptor.getQname().getLocalPart(), "");
		}
		else {
			return XMLUtils.StringToElement("", inputName, "");
		}
	}
	
		private void fixTypeAttributes(Node parent) {
		if (parent.getNodeType() == Node.ELEMENT_NODE) {
			Element el = (Element) parent;
			if (parent.hasAttributes()) {
				NamedNodeMap attributes = parent.getAttributes();
				List<Node> attributeNodesForRemoval = new ArrayList<Node>();
				for (int i = 0; i < attributes.getLength(); i++) {
					Node node = attributes.item(i);
					
					if (NS_XSI.equals(node.getNamespaceURI()) && TYPE.equals(node.getLocalName())) {
						// TAV-712 - don't just strip out xsi:type - let's fix the
						// name prefixes instead
						
						String xsiType = node.getTextContent();
						// Resolve prefix of xsi type
						String[] xsiTypeSplitted = xsiType.split(":", 2);
						String xsiTypePrefix = "";
						String xsiTypeName;
						if (xsiTypeSplitted.length == 1) {
							// No prefix
							xsiTypeName = xsiTypeSplitted[0];
						} else {
							xsiTypePrefix = xsiTypeSplitted[0];
							xsiTypeName = xsiTypeSplitted[1];
						}
						
						String xsiTypeNS;
						if (parent instanceof MessageElement) {
							xsiTypeNS = ((MessageElement)parent).getNamespaceURI(xsiTypePrefix);
						} else {
							xsiTypeNS = node
									.lookupNamespaceURI(xsiTypePrefix);
						}
						// Use global namespace prefixes						
						String newPrefix = namespaceMappings.get(xsiTypeNS);
						if (newPrefix == null) {
							logger.warn("Can't find prefix for xsi:type namespace " + xsiTypeNS + " - keeping old " + xsiType);
						} else {
							String newXsiType = newPrefix + ":" + xsiTypeName;
							node.setTextContent(newXsiType);	
							logger.info("Replacing " + xsiType + " with " + newXsiType);
						}
					}
				}
				for (Node node : attributeNodesForRemoval) {
					el.removeAttributeNS(node.getNamespaceURI(), node
							.getLocalName());
				}
			}
		}
		for (int i = 0; i < parent.getChildNodes().getLength(); i++) {
			fixTypeAttributes(parent.getChildNodes().item(i));
		}
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

		el = XMLUtils.StringToElement("", typeName, "");

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

		return el;
	}

	@Override
	protected SOAPBodyElement addElementToBody(String operationNamespace, SOAPBodyElement body, Element el) throws SOAPException {
		if (getStyle()==Style.DOCUMENT) {
			body = new SOAPBodyElement(el);
			body.setNamespaceURI(operationNamespace);
		}
		else {
			body.addChildElement(new SOAPBodyElement(el));
		}
		return body;
	}
	
	

}
