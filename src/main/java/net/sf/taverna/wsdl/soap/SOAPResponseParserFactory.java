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
 * Filename           $RCSfile: SOAPResponseParserFactory.java,v $
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007/11/28 16:05:45 $
 *               by   $Author: sowen70 $
 * Created on 05-May-2006
 *****************************************************************/
package net.sf.taverna.wsdl.soap;

import java.util.List;

import net.sf.taverna.wsdl.parser.TypeDescriptor;

/**
 * A factory class that selects the correct type of SOAPResponseParser according
 * to the service type , the types output of that service, and the response from
 * invoking that service.
 * 
 * @author sowen
 * 
 */
@SuppressWarnings("unchecked")
public class SOAPResponseParserFactory {

	private static SOAPResponseParserFactory instance = new SOAPResponseParserFactory();

	public static SOAPResponseParserFactory instance() {
		return instance;
	}

	/**
	 * returns an instance of the appropriate type of SOAPResponseParser
	 * 
	 * @param response -
	 *            List of SOAPBodyElement's resulting from the service
	 *            invokation.
	 * @param use -
	 *            the type of the service - 'literal' or 'encoded'
	 * @param style -
	 *            the style of the service - 'document' or 'rpc'
	 * @param outputDescriptors -
	 *            the List of {@link TypeDescriptor}'s describing the service outputs
	 * @return
	 * @see SOAPResponseParser
	 */
	public SOAPResponseParser create(List response, String use, String style,
			List<TypeDescriptor> outputDescriptors) {

		SOAPResponseParser result = null;
		
		if (outputIsPrimitive(outputDescriptors)) {
			if (use.equalsIgnoreCase("literal")) {
				result = new SOAPResponsePrimitiveLiteralParser(outputDescriptors);
			}
			else {
				result = new SOAPResponsePrimitiveParser(outputDescriptors);
			}
		} else if (use.equals("literal")) {
			result = new SOAPResponseLiteralParser(outputDescriptors);
		} else {
			if (response.size() > 1) {
				result = new SOAPResponseEncodedMultiRefParser(outputDescriptors);
			} else {
				result = new SOAPResponseEncodedParser(outputDescriptors);
			}
		}

		return result;
	}

	private boolean outputIsPrimitive(List<TypeDescriptor> outputDescriptors) {
		boolean result = true;
		for (TypeDescriptor d : outputDescriptors) {
			if (d.getMimeType().equals("'text/xml'")) {
				result = false;
				break;
			}
		}
		return result;
	}

}
