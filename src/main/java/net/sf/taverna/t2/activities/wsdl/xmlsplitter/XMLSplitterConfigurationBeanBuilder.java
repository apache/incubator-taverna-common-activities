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
package net.sf.taverna.t2.activities.wsdl.xmlsplitter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import net.sf.taverna.wsdl.parser.ArrayTypeDescriptor;
import net.sf.taverna.wsdl.parser.BaseTypeDescriptor;
import net.sf.taverna.wsdl.parser.ComplexTypeDescriptor;
import net.sf.taverna.wsdl.parser.TypeDescriptor;
import net.sf.taverna.wsdl.xmlsplitter.XMLSplitterSerialisationHelper;

import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.output.XMLOutputter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * A helper class to facilitate in building an XMLSplitter configuration bean
 * from the type descriptor XML, including setting up the ports.
 *
 * @author Stuart Owen
 *
 */
public class XMLSplitterConfigurationBeanBuilder {

	private static final JsonNodeFactory JSON_NODE_FACTORY = JsonNodeFactory.instance;

	public static JsonNode buildBeanForInput(TypeDescriptor descriptor) throws JDOMException, IOException {
		Element element = XMLSplitterSerialisationHelper.typeDescriptorToExtensionXML(descriptor);
		return buildBeanForInput(element);
	}

	public static JsonNode buildBeanForOutput(TypeDescriptor descriptor) throws JDOMException, IOException {
		Element element = XMLSplitterSerialisationHelper.typeDescriptorToExtensionXML(descriptor);
		return buildBeanForOutput(element);
	}

	public static JsonNode buildBeanForInput(Element element) throws JDOMException, IOException {
		ObjectNode bean = JSON_NODE_FACTORY.objectNode();
		ArrayNode inputDefinitions = bean.arrayNode();
		bean.put("inputPorts", inputDefinitions);
		ArrayNode outputDefinitions = bean.arrayNode();
		bean.put("outputPorts", outputDefinitions);

		TypeDescriptor descriptor = XMLSplitterSerialisationHelper
				.extensionXMLToTypeDescriptor(element);
		ObjectNode outBean = outputDefinitions.addObject();
		outBean.put("name", "output");
		outBean.put("mimeType", "'text/xml'");
		outBean.put("depth", 0);
		outBean.put("granularDepth", 0);

		if (descriptor instanceof ComplexTypeDescriptor) {
			List<TypeDescriptor> elements = ((ComplexTypeDescriptor) descriptor).getElements();
			String[] names = new String[elements.size()];
			Class<?>[] types = new Class<?>[elements.size()];
			TypeDescriptor.retrieveSignature(elements, names, types);
			for (int i = 0; i < names.length; i++) {
				ObjectNode portBean = inputDefinitions.addObject();
				portBean.put("name", names[i]);
				portBean.put("mimeType", TypeDescriptor.translateJavaType(types[i]));
				portBean.put("depth", depthForDescriptor(elements.get(i)));
			}

			List<TypeDescriptor> attributes = ((ComplexTypeDescriptor) descriptor).getAttributes();
			String[] elementNames = Arrays.copyOf(names, names.length);
			Arrays.sort(elementNames);
			String[] attributeNames = new String[attributes.size()];
			Class<?>[] attributeTypes = new Class<?>[attributes.size()];
			TypeDescriptor.retrieveSignature(attributes, attributeNames, attributeTypes);
			for (int i = 0; i < attributeNames.length; i++) {
				ObjectNode portBean = inputDefinitions.addObject();
				if (Arrays.binarySearch(elementNames, attributeNames[i]) < 0) {
					portBean.put("name", attributeNames[i]);
				} else {
					portBean.put("name", "1" + attributeNames[i]);
				}
				portBean.put("mimeType", TypeDescriptor.translateJavaType(attributeTypes[i]));
				portBean.put("depth", depthForDescriptor(attributes.get(i)));
			}
		} else if (descriptor instanceof ArrayTypeDescriptor) {
			ObjectNode portBean = inputDefinitions.addObject();
			portBean.put("name", descriptor.getName());

			if (((ArrayTypeDescriptor) descriptor).getElementType() instanceof BaseTypeDescriptor) {
				portBean.put("mimeType", "l('text/plain')");
			} else {
				portBean.put("mimeType", "l('text/xml')");
			}
			portBean.put("depth", 1);
		}

		String wrappedType = new XMLOutputter().outputString(element);
		bean.put("wrappedType", wrappedType);

		return bean;
	}


	private static int depthForDescriptor(TypeDescriptor desc) {
		if (desc instanceof ArrayTypeDescriptor && (!((ArrayTypeDescriptor)desc).isWrapped() || ((ArrayTypeDescriptor)desc).getElementType() instanceof BaseTypeDescriptor)) {
			return 1;
		}
		else {
			return 0;
		}
	}

	public static JsonNode buildBeanForOutput(Element element)
			throws JDOMException, IOException {
		ObjectNode bean = JSON_NODE_FACTORY.objectNode();
		ArrayNode inputDefinitions = bean.arrayNode();
		bean.put("inputPorts", inputDefinitions);
		ArrayNode outputDefinitions = bean.arrayNode();
		bean.put("outputPorts", outputDefinitions);

		TypeDescriptor descriptor = XMLSplitterSerialisationHelper
				.extensionXMLToTypeDescriptor(element);

		ObjectNode inBean = inputDefinitions.addObject();
		inBean.put("name", "input");
		inBean.put("mimeType", "'text/xml'");
		inBean.put("depth", 0);

		if (descriptor instanceof ComplexTypeDescriptor) {
			List<TypeDescriptor> elements = ((ComplexTypeDescriptor) descriptor).getElements();
			String[] names = new String[elements.size()];
			Class<?>[] types = new Class<?>[elements.size()];
			TypeDescriptor.retrieveSignature(elements, names, types);
			for (int i = 0; i < names.length; i++) {
				ObjectNode portBean = outputDefinitions.addObject();
				portBean.put("name", names[i]);
				portBean.put("mimeType", TypeDescriptor.translateJavaType(types[i]));
				int depth = depthForDescriptor(elements.get(i));
				portBean.put("depth", depth);
				portBean.put("granularDepth", depth);
			}


			List<TypeDescriptor> attributes = ((ComplexTypeDescriptor) descriptor).getAttributes();
			String[] elementNames = Arrays.copyOf(names, names.length);
			Arrays.sort(elementNames);
			String[] attributeNames = new String[attributes.size()];
			Class<?>[] attributeTypes = new Class<?>[attributes.size()];
			TypeDescriptor.retrieveSignature(attributes, attributeNames, attributeTypes);
			for (int i = 0; i < attributeNames.length; i++) {
				ObjectNode portBean = outputDefinitions.addObject();
				if (Arrays.binarySearch(elementNames, attributeNames[i]) < 0) {
					portBean.put("name", attributeNames[i]);
				} else {
					portBean.put("name", "1" + attributeNames[i]);
				}
				portBean.put("mimeType", TypeDescriptor
						.translateJavaType(attributeTypes[i]));
				int depth = depthForDescriptor(attributes.get(i));
				portBean.put("depth", depth);
				portBean.put("granularDepth", depth);
			}
		} else if (descriptor instanceof ArrayTypeDescriptor) {
			ObjectNode portBean = outputDefinitions.addObject();
			String name=descriptor.getName();
			portBean.put("name", name);
			portBean.put("depth", 1);
			portBean.put("granularDepth", 1);
			if (((ArrayTypeDescriptor) descriptor).getElementType() instanceof BaseTypeDescriptor) {
				portBean.put("mimeType", "l('text/plain')");
			} else {
				portBean.put("mimeType", "l('text/xml')");
			}
		}


		String wrappedType = new XMLOutputter().outputString(element);
		bean.put("wrappedType", wrappedType);

		return bean;
	}

}
