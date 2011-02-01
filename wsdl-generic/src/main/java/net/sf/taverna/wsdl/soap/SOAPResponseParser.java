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
 * Filename           $RCSfile: SOAPResponseParser.java,v $
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007/11/28 16:05:45 $
 *               by   $Author: sowen70 $
 * Created on 05-May-2006
 *****************************************************************/
package net.sf.taverna.wsdl.soap;

import java.util.List;
import java.util.Map;

/**
 * Inteface that defines all parsers responsible for parsing SOAP responses from
 * calling SOAP based webservices.
 * 
 * @author sowen
 * 
 */
@SuppressWarnings("unchecked")
public interface SOAPResponseParser {

	/**
	 * All SOAPResponseParsers take a list of SOAPBodyElement's, resulting from
	 * invoking the service, and convert these into a suitable map of output
	 * DataThings.
	 * 
	 * @param response -
	 *            List of SOAPBodyElements
	 * @return Map of output DataThing's mapped to their output name
	 * @throws Exception
	 */
	public Map parse(List response) throws Exception;

}
