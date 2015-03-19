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

package org.apache.taverna.wsdl.parser;

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
