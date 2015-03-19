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

package org.apache.taverna.wsdl.parser;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;


/**
 * Base class for all descriptors for type
 * 
 */
public class TypeDescriptor {
	private String name;

	private String type;

	private boolean optional;

	private boolean unbounded;

	private QName qname;

	private boolean nillable = false;
	
	private String documentation;

	public boolean isNillable() {
		return nillable;
	}

	public QName getQname() {
		if (qname != null)
			return qname;
		else if (type != null) {
			return new QName("", type);
		}
                return new QName("", "");
	}

	public void setQnameFromString(String qname) {
		String[] split = qname.split("}");
		if (split.length == 1) {
			this.qname = new QName("", qname);
		} else {
			String uri = split[0];
			uri = uri.replaceAll("\\{", "");
			uri = uri.replaceAll("\\}", "");
			this.qname = new QName(uri, split[1]);
		}
	}

	public String getMimeType() {
		return translateJavaType(determineClassType(this));
	}
	
	public void setQname(QName qname) {
		this.qname = qname;
	}

	public String getNamespaceURI() {
		return getQname().getNamespaceURI();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		int i;
		if ((i = name.lastIndexOf('>')) != -1) {
			this.name = name.substring(i + 1);
		} else {
			this.name = name;
		}
	}
	
	/**
     * @return the depth determined from the syntactic mime type of the original
     *         port. i.e text/plain = 0, l('text/plain') = 1, l(l('text/plain')) =
     *         2, ... etc.
     */
    public int getDepth() {
    	String syntacticType=getMimeType();
        if (syntacticType == null) {
                return 0;
        } else {
                return syntacticType.split("l\\(").length - 1;
        }
    }

	public boolean isOptional() {
		return optional;
	}

	public void setOptional(boolean optional) {
		this.optional = optional;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public boolean isUnbounded() {
		return unbounded;
	}

	public void setUnbounded(boolean unbounded) {
		this.unbounded = unbounded;
	}

	@Override
	public String toString() {
		return name + ":" + type;
	}

	/**
	 * Translate a java type into a taverna type string
	 */
	public static String translateJavaType(Class<?> type) {
		if (type.equals(String[].class)) {
			return "l('text/plain')";
		} else if (type.equals(org.w3c.dom.Element.class)) {
			return "'text/xml'";
		}

		else if (type.equals(org.w3c.dom.Element[].class)) {
			return "l('text/xml')";
		} else if (type.equals(byte[].class)) {
			return "'application/octet-stream'";
		} else {
			return "'text/plain'";
		}
	}

	public static void retrieveSignature(List<TypeDescriptor> params, String[] names,
			Class<?>[] types) {
		for (int i = 0; i < names.length; i++) {
			TypeDescriptor descriptor = params.get(i);
			names[i] = descriptor.getName();
			
			types[i]=determineClassType(descriptor);
		}
	}

	private static Class<?> determineClassType(TypeDescriptor descriptor) {
		String s = descriptor.getType().toLowerCase();
		Class<?> type;
		if (descriptor instanceof ArrayTypeDescriptor) {
			if (((ArrayTypeDescriptor) descriptor).getElementType() instanceof BaseTypeDescriptor) {
				type = String[].class;
			} else if (((ArrayTypeDescriptor) descriptor).isUnbounded()) {
				type = org.w3c.dom.Element[].class;
			}
			else {
				type = org.w3c.dom.Element.class;
			}
		} else {
			if ("string".equals(s)) {
				type = String.class;
			} else if ("double".equals(s) || "decimal".equals(s)) {
				type = Double.TYPE;
			} else if ("float".equals(s)) {
				type = Float.TYPE;
			} else if ("int".equals(s) || "integer".equals(s)) {
				type = Integer.TYPE;
			} else if ("boolean".equals(s)) {
				type = Boolean.TYPE;
			} else if ("base64binary".equals(s)) {
				type = byte[].class;
			} else {
				//treat any other basetype as a String.
				if (descriptor instanceof BaseTypeDescriptor) {
					type=String.class;
				}
				else {
					type = org.w3c.dom.Element.class;
				}
			}
		}
		return type;
	}

	/**
	 * Determines whether the descriptor describes a data structure that is
	 * cyclic, i.e. contains inner elements that contain references to outer
	 * elements, leading to a state of infinate recursion.
	 * 
	 * @param descriptor
	 * @return
	 */
	public static boolean isCyclic(TypeDescriptor descriptor) {
		boolean result = false;
		if (!(descriptor instanceof BaseTypeDescriptor)) {
			if (descriptor instanceof ComplexTypeDescriptor) {
				result = testForCyclic((ComplexTypeDescriptor) descriptor,
						new ArrayList<String>());
			} else {
				result = testForCyclic((ArrayTypeDescriptor) descriptor,
						new ArrayList<String>());
			}
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	private static boolean testForCyclic(ComplexTypeDescriptor descriptor,
			List<String> parents) {
		boolean result = false;
		String descKey = descriptor.getQname().toString();
		if (parents.contains(descKey))
			result = true;
		else {
			parents.add(descKey);
			List elements = descriptor.getElements();
			for (Iterator iterator = elements.iterator(); iterator.hasNext();) {
				TypeDescriptor elementDescriptor = (TypeDescriptor) iterator
						.next();
				if (elementDescriptor instanceof ComplexTypeDescriptor) {
					result = testForCyclic(
							(ComplexTypeDescriptor) elementDescriptor, parents);
				} else if (elementDescriptor instanceof ArrayTypeDescriptor) {
					result = testForCyclic(
							(ArrayTypeDescriptor) elementDescriptor, parents);
				}

				if (result)
					break;
			}

			parents.remove(descKey);
		}
		return result;
	}

	private static boolean testForCyclic(ArrayTypeDescriptor descriptor,
			List<String> parents) {
		boolean result = false;
		String descKey = descriptor.getQname().toString();
		if (parents.contains(descKey))
			result = true;
		else {
			parents.add(descKey);

			TypeDescriptor elementDescriptor = descriptor
					.getElementType();
			if (elementDescriptor instanceof ComplexTypeDescriptor) {
				result = testForCyclic(
						(ComplexTypeDescriptor) elementDescriptor, parents);
			} else if (elementDescriptor instanceof ArrayTypeDescriptor) {
				result = testForCyclic((ArrayTypeDescriptor) elementDescriptor,
						parents);
			}

			parents.remove(descKey);
		}
		return result;
	}

	public void setNillable(boolean nillable) {
		this.nillable  = nillable;
		
		
	}

	public String getDocumentation() {
		return documentation;
	}

	public void setDocumentation(String documentation) {
		this.documentation = documentation;
	}
}
