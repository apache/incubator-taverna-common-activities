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

import java.io.IOException;
import java.util.Map;

import javax.wsdl.WSDLException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.SOAPException;

import net.sf.taverna.wsdl.parser.UnknownOperationException;

import org.apache.axis.message.SOAPBodyElement;
import org.xml.sax.SAXException;

/**
 * Interface to a class that is responsible for creating the SOAP body elements from the provided inputs
 * for invoking a SOAP based Web-service.
 * 
 * @author Stuart Owen
 */
@SuppressWarnings("unchecked")
public interface BodyBuilder {
	
	public SOAPBodyElement build(Map inputMap)
			throws WSDLException, ParserConfigurationException, SOAPException,
			IOException, SAXException, UnknownOperationException;
	
}

