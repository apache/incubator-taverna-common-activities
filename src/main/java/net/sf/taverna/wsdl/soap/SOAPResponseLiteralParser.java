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
 * Filename           $RCSfile: SOAPResponseLiteralParser.java,v $
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007/11/28 16:05:45 $
 *               by   $Author: sowen70 $
 * Created on 05-May-2006
 *****************************************************************/
package net.sf.taverna.wsdl.soap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.taverna.wsdl.parser.TypeDescriptor;

import org.apache.axis.message.SOAPBodyElement;
import org.apache.axis.utils.XMLUtils;
import org.w3c.dom.Element;

/**
 * Responsible for parsing the SOAP response from calling a Literal based
 * service.
 * 
 * @author sowen
 * 
 */
@SuppressWarnings("unchecked")
public class SOAPResponseLiteralParser implements SOAPResponseParser {

	List<TypeDescriptor>outputDescriptors;

	public SOAPResponseLiteralParser(List<TypeDescriptor> outputDescriptors) {
		this.outputDescriptors = outputDescriptors;
	}

	/**
	 * Expects a list containing a single SOAPBodyElement, the contents of which
	 * are transferred directly to the output, converted to a String, and placed
	 * into the outputMaP which is returned
	 * 
	 * @return Map of the outputs
	 */
	public Map parse(List response) throws Exception {
		Map result = new HashMap();

		if (response.size()>0) {
			SOAPBodyElement rpcElement = (SOAPBodyElement) response.get(0);
	
			Element dom = rpcElement.getAsDOM();
	
			String outputName = getOutputName();
			String xml = XMLUtils.ElementToString(dom);
	
			result.put(outputName, xml);
		}

		return result;
	}

	protected String getOutputName() {
		String result = "";
		for (TypeDescriptor descriptor : outputDescriptors) {
			String name=descriptor.getName();
			if (!name.equals("attachmentList")) {
				result = name;
				break;
			}
		}
		return result;
	}
}
