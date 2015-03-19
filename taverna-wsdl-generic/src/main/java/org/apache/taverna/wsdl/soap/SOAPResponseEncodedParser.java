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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.SOAPElement;

import org.apache.taverna.wsdl.parser.TypeDescriptor;

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
public class SOAPResponseEncodedParser extends AbstractSOAPResponseParser {

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
    @Override
    public Map parse(List<SOAPElement> response) throws Exception {

		Map result = new HashMap();
		SOAPElement mainBody = response.get(0);

		for (TypeDescriptor descriptor : outputDescriptors) {
			String outputName = descriptor.getName();

			Node outputNode = getOutputNode(mainBody, outputName);
			if (outputNode != null) {
				if (stripAttributes) {					
					stripAttributes(outputNode);
					outputNode = removeNamespace(outputName, (Element) outputNode);
				}
				
                                String xml = toString(outputNode);
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
            
            return (Element)element.getOwnerDocument().renameNode(element, null, element.getLocalName());

                // :-O
//		String xml;
//		String innerXML = XMLUtils.getInnerXMLString(element);
//		if (innerXML != null) {
//			xml = "<" + outputName + ">" + innerXML + "</" + outputName + ">";
//		} else {
//			xml = "<" + outputName + " />";
//		}
//		DocumentBuilder builder = DocumentBuilderFactory.newInstance()
//				.newDocumentBuilder();
//		Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes()));
//		return doc.getDocumentElement();
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
