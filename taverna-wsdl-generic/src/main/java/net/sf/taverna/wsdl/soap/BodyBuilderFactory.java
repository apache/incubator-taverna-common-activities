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

import java.util.List;

import net.sf.taverna.wsdl.parser.TypeDescriptor;
import net.sf.taverna.wsdl.parser.UnknownOperationException;
import net.sf.taverna.wsdl.parser.WSDLParser;

/**
 * Factory that creates an appropriate BodyBuilder according to the provided WSDLProcessors style and use.
 * @author Stuart Owen
 *
 */
public class BodyBuilderFactory {
	
	private static BodyBuilderFactory instance = new BodyBuilderFactory();
	
	public static BodyBuilderFactory instance() {
		return instance;
	}
	
	public BodyBuilder create(WSDLParser parser, String operationName, List<TypeDescriptor> inputDescriptors) throws UnknownOperationException {
		String use = parser.getUse(operationName);
		String style = parser.getStyle();
		if (use.equals("encoded")) {
			return new EncodedBodyBuilder(style, parser,operationName, inputDescriptors);
		}
		else if (use.equals("literal")) {
			return new LiteralBodyBuilder(style,parser,operationName, inputDescriptors);
		}
		return new LiteralBodyBuilder(style,parser,operationName, inputDescriptors);
	}
}
