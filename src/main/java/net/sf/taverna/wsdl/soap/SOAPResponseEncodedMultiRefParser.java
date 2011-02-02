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
 * Filename           $RCSfile: SOAPResponseEncodedMultiRefParser.java,v $
 * Revision           $Revision: 1.2 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2008/08/08 10:28:09 $
 *               by   $Author: stain $
 * Created on 05-May-2006
 *****************************************************************/
package net.sf.taverna.wsdl.soap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sf.taverna.wsdl.parser.TypeDescriptor;

import org.apache.axis.message.SOAPBodyElement;
import org.apache.axis.utils.XMLUtils;
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
	public Map parse(List response) throws Exception, CyclicReferenceException {
		Map result = new HashMap();
		generateRefMap(response);
		expandRefMap();
		Element mainBody = ((SOAPBodyElement) response.get(0)).getAsDOM();

		for (TypeDescriptor descriptor : outputDescriptors) {
			String outputName = descriptor.getName();

			Node outputNode = getOutputNode(mainBody, outputName);
			if (outputNode != null) {
				expandNode(outputNode, new ArrayList());
				String xml;
				if (getStripAttributes()) {
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

	/**
	 * Generates a map of each multiref element, mapped to its ID.
	 * 
	 * @param response
	 * @throws Exception
	 */
	private void generateRefMap(List response) throws Exception {
		Map result = new HashMap();

		for (Iterator iterator = response.iterator(); iterator.hasNext();) {
			SOAPBodyElement bodyElement = (SOAPBodyElement) iterator.next();
			String id = bodyElement.getAttribute("id");
			if (id != null) {
				result.put("#" + id, bodyElement.getAsDOM());
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
