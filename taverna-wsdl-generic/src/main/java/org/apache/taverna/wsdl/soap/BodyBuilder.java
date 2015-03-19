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

package org.apache.taverna.wsdl.soap;

import java.io.IOException;
import java.util.Map;
import javax.wsdl.WSDLException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import org.apache.taverna.wsdl.parser.UnknownOperationException;
import org.xml.sax.SAXException;

/**
 * Interface to a class that is responsible for creating the SOAP body elements from the provided inputs
 * for invoking a SOAP based Web-service.
 * 
 * @author Stuart Owen
 */
@SuppressWarnings("unchecked")
public interface BodyBuilder {
	
	public SOAPElement build(Map inputMap)
			throws WSDLException, ParserConfigurationException, SOAPException,
			IOException, SAXException, UnknownOperationException;
	
}

