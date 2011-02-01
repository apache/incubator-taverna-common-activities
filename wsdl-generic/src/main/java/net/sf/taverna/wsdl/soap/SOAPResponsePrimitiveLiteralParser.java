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
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.sf.taverna.wsdl.parser.TypeDescriptor;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * A response parser specifically for literal use services that return primative types.
 * It extends the SOAPReponseLiteralParser, but unwraps the result from the enclosing XML
 * to expose the primitive result.
 * 
 * This is specially designed for unwrapped/literal type services, and RPC/literal services (untested). 
 * @author Stuart
 *
 */
@SuppressWarnings("unchecked")
public class SOAPResponsePrimitiveLiteralParser extends
		SOAPResponseLiteralParser {

	public SOAPResponsePrimitiveLiteralParser(List<TypeDescriptor> outputDescriptors) {
		super(outputDescriptors);
	}

	@Override
	public Map parse(List response) throws Exception {
		Map result = super.parse(response);
		Object dataValue = result.get(getOutputName());
		if (dataValue!=null) {
			String xml = dataValue.toString();
			
			DocumentBuilder builder = DocumentBuilderFactory.newInstance()
			.newDocumentBuilder();
			Document doc = builder.parse(new ByteArrayInputStream(xml.getBytes()));
		
			Node node = doc.getFirstChild();
			result.put(getOutputName(), node.getFirstChild().getNodeValue());
		}
		return result;
	}
	
	
}

	
