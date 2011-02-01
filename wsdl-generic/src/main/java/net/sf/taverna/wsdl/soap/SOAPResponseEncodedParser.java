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
 * Filename           $RCSfile: SOAPResponseEncodedParser.java,v $
 * Revision           $Revision: 1.2 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2008/08/08 10:28:09 $
 *               by   $Author: stain $
 * Created on 08-May-2006
 *****************************************************************/
package net.sf.taverna.wsdl.soap;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.sf.taverna.wsdl.parser.TypeDescriptor;

import org.apache.axis.message.SOAPBodyElement;
import org.apache.axis.utils.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * SOAPResponseParser responsible for parsing SOAP responses from RPC/encoded
 * based service, but that are not fragmented to multiref documents.
 * 
 * @author sowen
 * 
 */
@SuppressWarnings("unchecked")
public class SOAPResponseEncodedParser implements SOAPResponseParser {

	protected List<TypeDescriptor> outputDescriptors;

	private boolean stripAttributes = false;

	public SOAPResponseEncodedParser(List<TypeDescriptor> outputDescriptors) {
		this.outputDescriptors = outputDescriptors;
	}

	/**
	 * Parses the response into a single XML document, which is placed in the
	 * outputMap together with the given output name. Namespaces and other
	 * attributes are stripped out according to stripAttributes.
	 * 
	 * @param List
	 * @return Map
	 */
	public Map parse(List response) throws Exception {

		Map result = new HashMap();
		Element mainBody = ((SOAPBodyElement) response.get(0)).getAsDOM();

		for (TypeDescriptor descriptor : outputDescriptors) {
			String outputName = descriptor.getName();

			Node outputNode = getOutputNode(mainBody, outputName);
			if (outputNode != null) {
				String xml;				
				
				if (stripAttributes) {					
					stripAttributes(outputNode);
					outputNode = removeNamespace(outputName,
							(Element) outputNode);
				}
				
				xml = XMLUtils.ElementToString((Element) outputNode);
				result.put(outputName, xml);
			} 
		}

		return result;
	}

	protected Node getOutputNode(Element mainBody, String outputName) {
		// first try using body namespace ...
		Node outputNode = mainBody.getElementsByTagNameNS(
				mainBody.getNamespaceURI(), outputName).item(0);
		// ... and if that doesn't work, try without namespace
		if (outputNode == null) {
			outputNode = mainBody.getElementsByTagName(outputName).item(
					0);
		}
		if (outputNode == null) { // if still null, and there is only 1
			// output, take the first child
			if (outputDescriptors.size() == 1
					&& mainBody.getChildNodes().getLength() == 1) {
				outputNode = mainBody.getFirstChild();
			}
		}
		return outputNode;
	}

	/**
	 * Removes the namespace from the surrounding element that represents the
	 * outputName. E.g. converts <ns1:element xmlns:ns1="http://someurl">...</ns1:element>
	 * to <element>...</element>
	 * 
	 * @param outputName
	 * @param element
	 * @return
	 * @throws ParserConfigurationException
	 * @throws IOException
	 * @throws SAXException
	 */
	protected Element removeNamespace(String outputName, Element element)
			throws ParserConfigurationException, SAXException, IOException {
		String xml;
		String innerXML = XMLUtils.getInnerXMLString(element);
		if (innerXML != null) {
			xml = "<" + outputName + ">" + innerXML + "</" + outputName + ">";
		} else {
			xml = "<" + outputName + " />";
		}
		DocumentBuilder builder = DocumentBuilderFactory.newInstance()
				.newDocumentBuilder();
		Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes()));
		return doc.getDocumentElement();
	}

	protected void stripAttributes(Node node) {
		List names = new ArrayList();
		if (node.getAttributes() != null) {
			for (int i = 0; i < node.getAttributes().getLength(); i++) {
				names.add(node.getAttributes().item(i).getNodeName());
			}
		}

		for (Iterator iterator = names.iterator(); iterator.hasNext();) {
			node.getAttributes().removeNamedItem((String) iterator.next());
		}

		if (node.hasChildNodes()) {
			Node child = node.getFirstChild();
			while (child != null) {
				stripAttributes(child);
				child = child.getNextSibling();
			}
		}

	}

	/**
	 * determines whether attributes in the resulting XML should be stripped
	 * out, including namespace definitions, leading to XML that is much easier
	 * to read.
	 * 
	 * @param stripAttributes
	 */
	public void setStripAttributes(boolean stripAttributes) {
		this.stripAttributes = stripAttributes;
	}

	public boolean getStripAttributes() {
		return this.stripAttributes;
	}
}
