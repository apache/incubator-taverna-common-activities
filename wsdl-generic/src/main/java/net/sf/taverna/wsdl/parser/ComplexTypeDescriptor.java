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
package net.sf.taverna.wsdl.parser;

import java.util.ArrayList;
import java.util.List;

/**
 * A TypeDescriptor that specifically describes a complex type
 * 
 */
public class ComplexTypeDescriptor extends TypeDescriptor {
	private List<TypeDescriptor> elements = new ArrayList<TypeDescriptor>();

	public List<TypeDescriptor> getElements() {
		return elements;
	}

	public void setElements(List<TypeDescriptor> elements) {
		this.elements = elements;
	}
	
	public TypeDescriptor elementForName(String name) {
		TypeDescriptor result=null;
		for (TypeDescriptor desc : getElements()) {
			if (desc.getName().equals(name)) {
				result=desc;
				break;
			}
		}
		return result;
	}
}
