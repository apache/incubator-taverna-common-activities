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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.URL;

import org.junit.Before;
import org.junit.Test;

/**
 * Check that WSDLParser can detect WSRF.
 * Tests {@link WSDLParser#checkWSRF()}
 * 
 * @author Stian Soiland-Reyes
 *
 */
public class WSRFParserTest {
	
	private URL counterServiceWSDL;
	private WSDLParser wsdlParser;

	@Before
	public void findWSDL() {
		String path = "wsrf/counterService/CounterService_.wsdl";
		counterServiceWSDL = getClass().getResource(path);	
		assertNotNull("Coult not find test WSDL " + path, counterServiceWSDL);
	}
	
	@Test
	public void isWSRF() throws Exception {
		wsdlParser = new WSDLParser(counterServiceWSDL.toExternalForm());
		assertTrue("Not recognized as WSRF service", wsdlParser.isWsrfService());
	}

	
}
