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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.xml.soap.SOAPElement;

import org.apache.taverna.wsdl.parser.TypeDescriptor;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * A SOAPResponseParser responsible for responses that are fragmented into
 * seperate referenced blocks of XML - Multiref format. It trys to resolve each
 * reference to the corresponding multiref element, eventually generating a
 * single XML document. Cyclic references are not allows, and lead to a
 * CyclicReferenceException
 * 
 * @author sowen
 * 
 */
@SuppressWarnings("unchecked")
public class SOAPResponseEncodedMultiRefParser extends
		SOAPResponseEncodedParser {

	private List resolvedReferences = new ArrayList();

	private Map referenceMap;

	public SOAPResponseEncodedMultiRefParser(List<TypeDescriptor> outputDescriptors) {
		super(outputDescriptors);
	}

	/**
	 * Expects a list of XML SOAPBodyElement fragements, with the first being
	 * the root, and transforms this into a single XML document. Cyclic
	 * references lead to a CyclicReferenceException being thrown. XML
	 * namespaces are removed, leading to easier to read XML.
	 * 
	 * @param response -
	 *            List of XML SOAPBodyElement fragments.
	 */
	@Override
	public Map parse(List<SOAPElement> response) throws Exception, CyclicReferenceException {
		Map result = new HashMap();
		generateRefMap(response);
		expandRefMap();
		SOAPElement mainBody = response.get(0);

		for (TypeDescriptor descriptor : outputDescriptors) {
			String outputName = descriptor.getName();

			Node outputNode = getOutputNode(mainBody, outputName);
			if (outputNode != null) {
				expandNode(outputNode, new ArrayList());
				String xml;
				if (getStripAttributes()) {
					stripAttributes(outputNode);
					outputNode = removeNamespace(outputName, (Element) outputNode);
				}
				xml = toString(outputNode);

				result.put(outputName, xml);
			} 

		}

		return result;
	}

	/**
	 * Generates a map of each multiref element, mapped to its ID.
	 * 
	 * @param response
	 * @throws Exception
	 */
	private void generateRefMap(List response) throws Exception {
		Map result = new HashMap();

		for (Iterator iterator = response.iterator(); iterator.hasNext();) {
			SOAPElement bodyElement = (SOAPElement) iterator.next();
			String id = bodyElement.getAttribute("id");
			if (id.length() > 0) {
				result.put("#" + id, bodyElement);
			}
		}

		referenceMap = result;
	}

	/**
	 * Expands any references to other fragments within each multiref fragment,
	 * resulting in all multiref fragments being fully expanded.
	 * 
	 * @throws CyclicReferenceException
	 */
	private void expandRefMap() throws CyclicReferenceException {
		for (Iterator iterator = referenceMap.keySet().iterator(); iterator
				.hasNext();) {
			String key = (String) iterator.next();
			if (!resolvedReferences.contains(key)) {
				expandMultirefElement(key, new ArrayList());
			}
		}
	}

	private void expandMultirefElement(String key, List parentKeys)
			throws CyclicReferenceException {
		if (parentKeys.contains(key))
			throw new CyclicReferenceException();
		parentKeys.add(key);
		Node node = (Node) referenceMap.get(key);
		expandNode(node, parentKeys);
		resolvedReferences.add(key);
		parentKeys.remove(key);
	}

	private void expandNode(Node node, List parentKeys)
			throws CyclicReferenceException {
		String href = getHrefForNode(node);
		if (href != null) {
			if (!resolvedReferences.contains(href)) {
				expandMultirefElement(href, parentKeys);
			}
			copyMultirefContentsToParent(node, href);
		}
		if (node.hasChildNodes()) {
			Node child = node.getFirstChild();
			while (child != null) {
				expandNode(child, parentKeys);
				child = child.getNextSibling();
			}
		}
	}

	private void copyMultirefContentsToParent(Node parent, String multirefKey) {
		Element multiRef = (Element) referenceMap.get(multirefKey);
		Node child = multiRef.getFirstChild();
		while (child != null) {
			parent.appendChild(parent.getOwnerDocument()
					.importNode(child, true));
			child = child.getNextSibling();
		}
		parent.getAttributes().removeNamedItem("href");
	}

	private String getHrefForNode(Node node) {
		String result = null;
		NamedNodeMap nodemap = node.getAttributes();
		if (nodemap != null) {
			Node attrNode = nodemap.getNamedItem("href");
			if (attrNode != null) {
				result = attrNode.getNodeValue();
			}
		}
		return result;
	}
}
