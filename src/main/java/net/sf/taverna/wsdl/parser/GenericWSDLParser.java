/**
 * *****************************************************************************
 * Copyright (C) 2012 Spanish National Bioinformatics Institute (INB),
 * Barcelona Supercomputing Center and The University of Manchester
 *
 * Modifications to the initial code base are copyright of their respective
 * authors, or their employers as appropriate.
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307
 *****************************************************************************
 */

package net.sf.taverna.wsdl.parser;

import java.util.LinkedHashMap;
import java.util.List;
import javax.xml.namespace.QName;
import org.apache.ws.commons.schema.XmlSchemaObject;

/**
 * @author Dmitry Repchevsky
 */

public interface GenericWSDLParser {
    
    String getDocumentBaseURI();
    List<QName> getServices();
    List<String> getPorts(QName serviceName);
    List<String> getOperations(String portName);
    WSRF_Version isWSRFPort(String portName);
    
    String getOperationStyle(String portName, String operationName) throws UnknownOperationException;
    String getSOAPActionURI(String portName, String operationName) throws UnknownOperationException;
    QName getRPCRequestMethodName(String portName, String operationName) throws UnknownOperationException;
    QName getRPCResponseMethodName(String portName, String operationName) throws UnknownOperationException;    
    String getSoapInputUse(String portName, String operationName) throws UnknownOperationException;
    String getSoapOutputUse(String portName, String operationName) throws UnknownOperationException;
    
    String getSoapAddress(String portName);
    String getOperationEndpointLocation(String operationName) throws UnknownOperationException;
    String getOperationDocumentation(String portName, String operationName) throws UnknownOperationException;
    
    LinkedHashMap<String, XmlSchemaObject> getInputParameters(String portName, String operationName) throws UnknownOperationException;
    LinkedHashMap<String, XmlSchemaObject> getOutputParameters(String portName, String operationName) throws UnknownOperationException;
}
