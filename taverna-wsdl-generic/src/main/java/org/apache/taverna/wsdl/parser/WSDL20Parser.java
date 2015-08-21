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

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.xml.namespace.QName;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.woden.WSDLFactory;
import org.apache.woden.WSDLReader;
import org.apache.woden.XMLElement;
import org.apache.woden.types.NCName;
import org.apache.woden.wsdl20.Binding;
import org.apache.woden.wsdl20.BindingOperation;
import org.apache.woden.wsdl20.Description;
import org.apache.woden.wsdl20.ElementDeclaration;
import org.apache.woden.wsdl20.Endpoint;
import org.apache.woden.wsdl20.Interface;
import org.apache.woden.wsdl20.InterfaceMessageReference;
import org.apache.woden.wsdl20.InterfaceOperation;
import org.apache.woden.wsdl20.Service;
import org.apache.woden.wsdl20.enumeration.Direction;
import org.apache.woden.wsdl20.extensions.ExtensionProperty;
import org.apache.woden.wsdl20.extensions.soap.SOAPConstants;
import org.apache.woden.wsdl20.xml.DescriptionElement;
import org.apache.woden.wsdl20.xml.DocumentationElement;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.apache.ws.commons.schema.utils.XmlSchemaNamed;
import org.w3c.dom.Element;

/**
 * @author Dmitry Repchevsky
 */

public class WSDL20Parser implements GenericWSDLParser {
    private static Map<String, WSDL20Parser> parsers;
    
    private final Description description;
    private final XmlSchemaCollection schemas;

    private WSDL20Parser(Description description) throws IOException {
        this.description = description;
        
        schemas = new XmlSchemaCollection();
    }

    @Override
    public String getDocumentBaseURI() {
        DescriptionElement element = description.toElement();
        URI documentBaseURI = element.getDocumentBaseURI();
        return documentBaseURI == null ? null : documentBaseURI.toString();
    }

    @Override
    public List<QName> getServices() {
        List<QName> services = new ArrayList();
        for (Service service : description.getServices()) {
            services.add(service.getName());
        }
        return services;
    }

    @Override
    public List<String> getPorts(QName serviceName) {
        List<String> interfaces = new ArrayList();
        for (Interface _interface : description.getInterfaces()) {
            interfaces.add(_interface.getName().getLocalPart());
        }
        return interfaces;
    }

    @Override
    public List<String> getOperations(String portName) {
        List<String> operations = new ArrayList();
        
        for (Service service : description.getServices()) {
            for (Endpoint endpoint : service.getEndpoints()) {
                final NCName endpointName = endpoint.getName();
                if (endpointName != null && endpointName.toString().equals(portName)) {
                    Binding binding = endpoint.getBinding();
                    if (binding != null) {
                        for (BindingOperation bindingOperation : binding.getBindingOperations()) {
                            InterfaceOperation interfaceOperation = bindingOperation.getInterfaceOperation();
                            if (interfaceOperation != null) {
                                operations.add(interfaceOperation.getName().getLocalPart());
                            }
                        }
                    }
                }
            }
        }
        return operations;
    }

    @Override
    public WSRF_Version isWSRFPort(String portName) {
        return null;
    }

    @Override
    public String getOperationStyle(String portName, String operationName) throws UnknownOperationException {
        return "document";
    }

    @Override
    public String getSOAPActionURI(String portName, String operationName) throws UnknownOperationException {
        BindingOperation bindingOperation = getBindingOperation(portName, operationName);
        InterfaceOperation interfaceOperation = bindingOperation.getInterfaceOperation();

        ExtensionProperty property = interfaceOperation.getExtensionProperty(SOAPConstants.NS_URI_SOAP, SOAPConstants.ATTR_ACTION);
        return property == null ? null : property.getContent().toString();
    }

    @Override
    public QName getRPCRequestMethodName(String portName, String operationName) throws UnknownOperationException {
        // NO RPC SUPPORT YET
        return new QName(operationName);    }

    @Override
    public QName getRPCResponseMethodName(String portName, String operationName) throws UnknownOperationException {
        // NO RPC SUPPORT YET
        return new QName(operationName);
    }

    @Override
    public String getSoapInputUse(String portName, String operationName) throws UnknownOperationException {
        return "literal";
    }

    @Override
    public String getSoapOutputUse(String portName, String operationName) throws UnknownOperationException {
        return "literal";
    }

    @Override
    public String getSoapAddress(String portName) {
        for (Service service : description.getServices()) {
            for (Endpoint endpoint : service.getEndpoints()) {
                final NCName endpointName = endpoint.getName();
                if (endpointName != null && endpointName.toString().equals(portName)) {
                    URI address = endpoint.getAddress();
                    return address == null ? null : address.toString();
                }
            }    
        }
        return null;
    }

    @Override
    public String getOperationEndpointLocation(String operationName) throws UnknownOperationException {
        for (Service service : description.getServices()) {
            for (Endpoint endpoint : service.getEndpoints()) {
                Binding binding = endpoint.getBinding();
                if (binding != null) {
                    for (BindingOperation o : binding.getBindingOperations()) {
                        InterfaceOperation operation = o.getInterfaceOperation();
                        if (operation != null && operationName.equals(operation.getName().getLocalPart())) {
                            URI address = endpoint.getAddress();
                            return address == null ? null : address.toString();                        
                        }
                    }
                }
            }    
        }
        return null;

    }

    @Override
    public String getOperationDocumentation(String portName, String operationName) throws UnknownOperationException {
        Writer writer = new StringWriter();
        
        BindingOperation bindingOperation = getBindingOperation(portName, operationName);
        InterfaceOperation interfaceOperation = bindingOperation.getInterfaceOperation();

        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            for (DocumentationElement documentation : interfaceOperation.toElement().getDocumentationElements()) {
                XMLElement xml = documentation.getContent();
                Object source = xml.getSource();
                if (source instanceof Element) {
                    Element element = (Element)source;
                    try {
                        transformer.transform(new DOMSource(element), new StreamResult(writer));
                    } catch (TransformerException ex) {}
                }
            }
        } catch (TransformerFactoryConfigurationError | TransformerConfigurationException ex) {}
        
        return writer.toString();
    }

    @Override
    public XmlSchemaCollection getXmlSchemas() {
        return schemas;
    }
    
    @Override
    public LinkedHashMap<String, XmlSchemaNamed> getInputParameters(String portName, String operationName) throws UnknownOperationException {
        LinkedHashMap<String, XmlSchemaNamed> parameters = new LinkedHashMap();

        BindingOperation bindingOperation = getBindingOperation(portName, operationName);
        InterfaceOperation interfaceOperation = bindingOperation.getInterfaceOperation();

        // NOTE THAT THIS CODE IS NOT VALID FOR "RPC" STYLE
        for (InterfaceMessageReference input : interfaceOperation.getInterfaceMessageReferences()) {
            if (Direction.IN == input.getDirection()) {
                ElementDeclaration element = input.getElementDeclaration();
                parameters.put(element.getName().getLocalPart(), (XmlSchemaNamed)element.getContent());
            }
        }
        return parameters;
    }

    @Override
    public LinkedHashMap<String, XmlSchemaNamed> getOutputParameters(String portName, String operationName) throws UnknownOperationException {
        LinkedHashMap<String, XmlSchemaNamed> parameters = new LinkedHashMap();
        
        BindingOperation bindingOperation = getBindingOperation(portName, operationName);
        InterfaceOperation interfaceOperation = bindingOperation.getInterfaceOperation();
        
        // NOTE THAT THIS CODE IS NOT VALID FOR "RPC" STYLE
        for (InterfaceMessageReference output : interfaceOperation.getInterfaceMessageReferences()) {
            if (Direction.OUT == output.getDirection()) {
                // TODO!!!
                ElementDeclaration element = output.getElementDeclaration();
                parameters.put(element.getName().getLocalPart(), (XmlSchemaNamed)element.getContent());
            }
        }
        return parameters;
    }
    
    private BindingOperation getBindingOperation(String portName, String operationName) throws UnknownOperationException {
        
        Binding[] bindings = null;
        if (portName == null) {
            bindings = description.getBindings();
        } else {
            loop:
            for (Service service : description.getServices()) {
                for (Endpoint endpoint : service.getEndpoints()) {
                    final NCName endpointName = endpoint.getName();
                    if (endpointName != null && endpointName.toString().equals(portName)) {
                        Binding binding = endpoint.getBinding();
                        if (binding != null) {
                            bindings = new Binding[] {binding};
                            break loop;
                        }
                    }
                }
            }
            if (bindings == null) {
                bindings = description.getBindings();
            }
        }

        if (bindings != null) {
            for (Binding binding : bindings) {
                for (BindingOperation bindingOperation : binding.getBindingOperations()) {
                    InterfaceOperation interfaceOperation = bindingOperation.getInterfaceOperation();
                    if (interfaceOperation != null && operationName.equals(interfaceOperation.getName().getLocalPart())) {
                        return bindingOperation;
                    }
                }
            }
        }

        throw new UnknownOperationException("Unknown operation: " + operationName);
    }

    public static synchronized WSDL20Parser getWSDL20Parser(String wsdlLocation) throws Exception {
        WSDL20Parser parser = null;
        
        if (parsers == null) {
            parsers = new TreeMap();
        } else {
            parser = parsers.get(wsdlLocation);
        }
        
        if (parser == null) {
            WSDLFactory factory = WSDLFactory.newInstance();
            WSDLReader reader = factory.newWSDLReader();
            reader.setFeature(WSDLReader.FEATURE_VALIDATION, true);
            Description description = reader.readWSDL(wsdlLocation);

            parser = new WSDL20Parser(description);
            
            parsers.put(wsdlLocation, parser);
        }
        
        return parser;
    }
}
