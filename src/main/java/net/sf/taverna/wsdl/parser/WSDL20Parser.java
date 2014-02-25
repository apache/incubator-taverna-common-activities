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
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.apache.ws.commons.schema.XmlSchemaObject;
import org.inb.bsc.wsdl20.Binding;
import org.inb.bsc.wsdl20.BindingOperation;
import org.inb.bsc.wsdl20.Description;
import org.inb.bsc.wsdl20.ElementDeclaration;
import org.inb.bsc.wsdl20.Endpoint;
import org.inb.bsc.wsdl20.Interface;
import org.inb.bsc.wsdl20.InterfaceMessageReference;
import org.inb.bsc.wsdl20.InterfaceOperation;
import org.inb.bsc.wsdl20.Service;
import static org.inb.bsc.wsdl20.extensions.soap.SOAPBindingOperationExtensions.SOAP_ACTION_ATTR;
import org.inb.bsc.wsdl20.factory.WSDL2Factory;
import org.inb.bsc.wsdl20.xml.WSDL2Reader;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

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
        URI documentBaseURI = description.getDocumentBaseURI();
        return documentBaseURI == null ? null : documentBaseURI.toString();
    }

    @Override
    public List<QName> getServices() {
        List<QName> services = new ArrayList<QName>();
        for (Service service : description.getAllServices()) {
            services.add(service.getName());
        }
        return services;
    }

    @Override
    public List<String> getPorts(QName serviceName) {
        List<String> interfaces = new ArrayList<String>();
        for (Interface _interface : description.getAllInterfaces()) {
            interfaces.add(_interface.getName().getLocalPart());
        }
        return interfaces;
    }

    @Override
    public List<String> getOperations(String portName) {
        List<String> operations = new ArrayList<String>();
        for (Interface _interface : description.getAllInterfaces()) {
            if (portName.equals(_interface.getName().getLocalPart())) {
                for (InterfaceOperation operation : _interface.getAllInterfaceOperations()) {
                    operations.add(operation.getName().getLocalPart());
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
        for (Binding binding : description.getAllBindings()) {
            if (portName != null) {
                Interface _interface = binding.getInterface();
                if (_interface == null || !portName.equals(_interface.getName().getLocalPart())) {
                    continue;
                }
            }    
        
            for (BindingOperation o : binding.getBindingOperations()) {
                InterfaceOperation operation = o.getInterfaceOperation();
                if (operation != null && operationName.equals(operation.getName().getLocalPart())) {
                    return operation.getExtensionAttribute(SOAP_ACTION_ATTR);
                }
            }
        }
        return null;
    }

    @Override
    public QName getRPCRequestMethodName(String portName, String operationName) throws UnknownOperationException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

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
        for (Service service : description.getAllServices()) {
            for (Endpoint endpoint : service.getEndpoints()) {
                Binding binding = endpoint.getBinding();
                if (binding != null) {
                    Interface _interface = binding.getInterface();
                    if (_interface != null && portName.equals(_interface.getName().getLocalPart())) {
                        URI address = endpoint.getAddress();
                        return address == null ? null : address.toString();
                    }
                }
            }    
        }
        return null;
    }

    @Override
    public String getOperationEndpointLocation(String operationName) throws UnknownOperationException {
        for (Service service : description.getAllServices()) {
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
        for (Interface _interface : description.getAllInterfaces()) {
            if (portName == null || portName.equals(_interface.getName().getLocalPart())) {
                for (InterfaceOperation operation : _interface.getAllInterfaceOperations()) {
                    if (operationName.equals(operation.getName().getLocalPart())) {
                        try {
                            Transformer transformer = TransformerFactory.newInstance().newTransformer();
                            for (Element element : operation.getDocumentationElements()) {
                                try {
                                    transformer.transform(new DOMSource(element), new StreamResult(writer));
                                } catch (TransformerException ex) {}
                            }
                        } catch (TransformerFactoryConfigurationError ex) {}
                        catch (TransformerConfigurationException ex) {}
                    }
                }
            }
        }
        return writer.toString();
    }

    @Override
    public LinkedHashMap<String, XmlSchemaObject> getInputParameters(String portName, String operationName) throws UnknownOperationException {
        LinkedHashMap<String, XmlSchemaObject> parameters = new LinkedHashMap<String, XmlSchemaObject>();

        // NOTE THAT THIS CODE IS NOT VALID FOR "RPC" STYLE
        for (Interface _interface : description.getAllInterfaces()) {
            if (portName == null || portName.equals(_interface.getName().getLocalPart())) {
                for (InterfaceOperation operation : _interface.getAllInterfaceOperations()) {
                    if (operationName.equals(operation.getName().getLocalPart())) {
                        for (InterfaceMessageReference input : operation.getInputs()) {
                            ElementDeclaration element = input.getElementDeclaration();
                            parameters.put(element.getName().getLocalPart(), (XmlSchemaObject)element.getContent());
                        }
                        return parameters;
                    }
                }
            }
        }
        return parameters;
    }

    @Override
    public LinkedHashMap<String, XmlSchemaObject> getOutputParameters(String portName, String operationName) throws UnknownOperationException {
        LinkedHashMap<String, XmlSchemaObject> parameters = new LinkedHashMap<String, XmlSchemaObject>();
        
        // NOTE THAT THIS CODE IS NOT VALID FOR "RPC" STYLE
        for (Interface _interface : description.getAllInterfaces()) {
            if (portName == null || portName.equals(_interface.getName().getLocalPart())) {
                for (InterfaceOperation operation : _interface.getAllInterfaceOperations()) {
                    if (operationName.equals(operation.getName().getLocalPart())) {
                        for (InterfaceMessageReference output : operation.getOutputs()) {
                            ElementDeclaration element = output.getElementDeclaration();
                            parameters.put(element.getName().getLocalPart(), (XmlSchemaObject)element.getContent());
                        }
                        return parameters;
                    }
                }
            }
        }
        return parameters;
    }
    
    public static synchronized WSDL20Parser getWSDL20Parser(String wsdlLocation) throws Exception {
        WSDL20Parser parser = null;
        
        if (parsers == null) {
            parsers = new TreeMap<String, WSDL20Parser>();
        } else {
            parser = parsers.get(wsdlLocation);
        }
        
        if (parser == null) {
            WSDL2Factory factory = WSDL2Factory.newInstance();
            WSDL2Reader reader = factory.getWSLD2Reader();
            InputSource source = new InputSource(wsdlLocation);
            Description description = reader.read(source);

            parser = new WSDL20Parser(description);
            
            parsers.put(wsdlLocation, parser);
        }
        
        return parser;
    }
}
