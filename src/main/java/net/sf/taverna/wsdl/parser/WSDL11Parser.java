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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.wsdl.Binding;
import javax.wsdl.BindingInput;
import javax.wsdl.BindingOperation;
import javax.wsdl.BindingOutput;
import javax.wsdl.Definition;
import javax.wsdl.Import;
import javax.wsdl.Input;
import javax.wsdl.Message;
import javax.wsdl.Operation;
import javax.wsdl.Output;
import javax.wsdl.Part;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.wsdl.Types;
import javax.wsdl.WSDLElement;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ElementExtensible;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.schema.Schema;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.wsdl.extensions.soap.SOAPBinding;
import javax.wsdl.extensions.soap.SOAPBody;
import javax.wsdl.extensions.soap.SOAPOperation;
import javax.wsdl.extensions.soap12.SOAP12Address;
import javax.wsdl.extensions.soap12.SOAP12Binding;
import javax.wsdl.extensions.soap12.SOAP12Body;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.apache.ws.commons.schema.XmlSchemaObject;
import org.apache.ws.commons.schema.utils.NamespaceMap;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

/**
 * This is an WSDL 1.1 / Xml Schema 1.0 parser based on WSDL4j and Apache XML Schema 2.0 libraries
 * 
 * @author Dmitry Repchevsky
 */

public class WSDL11Parser implements GenericWSDLParser {
    
    private static Map<String, WSDL11Parser> parsers;
    
    private final Definition definition;
    private final XmlSchemaCollection schemas;
    
    private final Map<String, WSRF_Version> wsrfPorts;
    
    private static final Logger logger = Logger.getLogger(WSDL11Parser.class.getName());
    
    private WSDL11Parser(Definition definition) throws IOException {
        this.definition = definition;
        
        schemas = new XmlSchemaCollection();
        addXmlSchemas(schemas, definition);
        
        wsrfPorts = new TreeMap<String, WSRF_Version>();
        checkWSRFPorts(wsrfPorts);
    }
    
    @Override
    public String getDocumentBaseURI() {
        return definition.getDocumentBaseURI();
    }

    @Override
    public List<QName> getServices() {
        List<QName> serviceNames = new ArrayList<QName>();
        
        Collection<Service> services = definition.getAllServices().values();
        for (Service service : services) {
            serviceNames.add(service.getQName());
        }
        
        return serviceNames;
    }
    
    @Override
    public List<String> getPorts(QName serviceName) {
        List<String> portNames = new ArrayList<String>();
        
        Service service = definition.getService(serviceName);
        if (service != null) {
            Collection<Port> ports = service.getPorts().values();
            for (Port port : ports) {
                portNames.add(port.getName());
            }
        }
        
        return portNames;
    }
    
    /**
     * The method is here only for compatibility with other modules and will be removed as soon as other modules are updated.
     * 
     * @return a list of all SOAP operations found in the WSDL (including all imported definitions)
     * @deprecated
     */
    @Deprecated
    List<Operation> getOperations() {
        List<Operation> operations = new ArrayList<Operation>();
        
        Collection<Binding> bindings = definition.getAllBindings().values();
        for (Binding binding : bindings) {
            List<ExtensibilityElement> bindingExElements = binding.getExtensibilityElements();
            for (ExtensibilityElement bindingExElement : bindingExElements) {
                if (bindingExElement instanceof SOAPBinding ||
                    bindingExElement instanceof SOAP12Binding) {
                    List<BindingOperation> bindingOperations = binding.getBindingOperations();
                    for (BindingOperation bindingOperation : bindingOperations) {
                        Operation operation = bindingOperation.getOperation();
                        if (operation != null) {
                            operations.add(operation);
                        }
                    }
                }
            }
        }
        
        return operations;
    }
    
    @Override
    public List<String> getOperations(String portName) {
        List<String> operations = new ArrayList<String>();
        for (BindingOperation bindingOperation : getBindingOperations(portName)) {
            Operation operation = bindingOperation.getOperation();
            if (operation != null) {
                operations.add(operation.getName());
            }
        }
        return operations;
    }
    
    @Override
    public WSRF_Version isWSRFPort(String portName)
    {
        return wsrfPorts.get(portName);
    }

    @Deprecated
    String getOperationDocumentation(String operationName) throws UnknownOperationException {
        return getOperationDocumentation(null, operationName);
    }
    
    @Override
    public String getOperationDocumentation(String portName, String operationName) throws UnknownOperationException {
        if (portName == null) {
            portName = findPort(operationName);
        }

        BindingOperation bindingOperation = getBindingOperation(portName, operationName);
        Operation operation = bindingOperation.getOperation();
        
        return getDocumentation(operation);
    }
    
    @Deprecated
    String getOperationStyle(String operationName) throws UnknownOperationException {
        return getOperationStyle(null, operationName);
    }
    
    @Override
    public String getOperationStyle(String portName, String operationName) throws UnknownOperationException {
        if (portName == null) {
            portName = findPort(operationName);
        }

        BindingOperation bindingOperation = getBindingOperation(portName, operationName);
        
        String style = getStyle(bindingOperation);
        if (style == null) {
            Port port = getPort(portName);
            Binding binding = port.getBinding();
            style = getStyle(binding);
        }
        
        return style;
    }
    
    @Deprecated
    String getSOAPActionURI(String operationName) throws UnknownOperationException {
        return getSOAPActionURI(null, operationName);
    }
    
    @Override
    public String getSOAPActionURI(String portName, String operationName) throws UnknownOperationException {
        if (portName == null) {
            portName = findPort(operationName);
        }

        BindingOperation bindingOperation = getBindingOperation(portName, operationName);
        return getSOAPActionURI(bindingOperation);
    }
    
    @Deprecated
    public String getOperationEndpointLocation(String operationName) throws UnknownOperationException {
        return getSoapAddress(findPort(operationName));
    }
    
    @Override
    public String getSoapAddress(String portName) {
        Port port = getPort(portName);
        if (port == null) {
            return null;
        }

        return getSoapAddress(port);
    }
    
    @Deprecated
    QName getRPCRequestMethodName(String operationName) throws UnknownOperationException {
        return getRPCRequestMethodName(null, operationName);
    }
    
    /**
     * Returns a qualified name for a RPC method that is a root element for a SOAPBody.
     * 
     * @param portName a port name of the operation
     * @param operationName an operation name
     * 
     * @return QName wrapper that is qualified name of the method or null if no method to be defined (not "rpc" operation style)
     * 
     * @throws UnknownOperationException 
     */
    public QName getRPCRequestMethodName(String portName, String operationName) throws UnknownOperationException {
        if (portName == null) {
            portName = findPort(operationName);
        }

        BindingOperation bindingOperation = getBindingOperation(portName, operationName);
        
        String style = getOperationStyle(portName, operationName);
        if ("rpc".equals(style)) {
            BindingInput bindingInput = bindingOperation.getBindingInput();
            if (bindingInput != null) {
                String namespace = getNamespaceURI(bindingInput);
                if (namespace != null) {
                    return new QName(namespace, operationName);
                }
            }
            return new QName(operationName);
        }

        return null;
    }
    
    @Deprecated
    QName getRPCResponseMethodName(String operationName) throws UnknownOperationException {
        return getRPCResponseMethodName(null, operationName);
    }
    
    /**
     * Returns a qualified name for a RPC method that is a root element for a SOAPBody.
     * 
     * @param portName a port name of the operation
     * @param operationName an operation name
     * 
     * @return QName wrapper that is qualified name of the method or null if no method to be defined (not "rpc" operation style)
     * 
     * @throws UnknownOperationException 
     */
    public QName getRPCResponseMethodName(String portName, String operationName) throws UnknownOperationException {
        if (portName == null) {
            portName = findPort(operationName);
        }

        BindingOperation bindingOperation = getBindingOperation(portName, operationName);
        
        String style = getOperationStyle(portName, operationName);
        if ("rpc".equals(style)) {
            BindingOutput bindingOutput = bindingOperation.getBindingOutput();
            if (bindingOutput != null) {
                String namespace = getNamespaceURI(bindingOutput);
                if (namespace != null) {
                    return new QName(namespace, operationName);
                }
            }
            return new QName(operationName);
        }

        return null;
    }
    
    @Deprecated
    String getSoapInputUse(String operationName) throws UnknownOperationException {
        return getSoapInputUse(null, operationName);
    }
    
    @Override
    public String getSoapInputUse(String portName, String operationName) throws UnknownOperationException {
        if (portName == null) {
            portName = findPort(operationName);
        }

        BindingOperation bindingOperation = getBindingOperation(portName, operationName);
        BindingInput bindingInput = bindingOperation.getBindingInput();
        if (bindingInput != null) {
            return getUse(bindingInput);
        }
        return null;
    }
    
    @Deprecated
    String getSoapOutputUse(String operationName) throws UnknownOperationException {
        return getSoapOutputUse(null, operationName);
    }
    
    @Override
    public String getSoapOutputUse(String portName, String operationName) throws UnknownOperationException {
        if (portName == null) {
            portName = findPort(operationName);
        }

        BindingOperation bindingOperation = getBindingOperation(portName, operationName);
        BindingOutput bindingOutput = bindingOperation.getBindingOutput();
        if (bindingOutput != null) {
            return getUse(bindingOutput);
        }
        return null;
    }
    
    @Deprecated
    LinkedHashMap<String, XmlSchemaObject> getInputParameters(String operationName) throws UnknownOperationException {
        return getInputParameters(null, operationName);
    }
    
    @Override
    public LinkedHashMap<String, XmlSchemaObject> getInputParameters(String portName, String operationName) throws UnknownOperationException {
        if (portName == null) {
            portName = findPort(operationName);
        }

        List<Part> parts = getInputParts(portName, operationName);
        return getParameters(parts);
    }

    @Deprecated
    LinkedHashMap<String, XmlSchemaObject> getOutputParameters(String operationName) throws UnknownOperationException {
        return getOutputParameters(null, operationName);
    }
    
    public LinkedHashMap<String, XmlSchemaObject> getOutputParameters(String portName, String operationName) throws UnknownOperationException {
        if (portName == null) {
            portName = findPort(operationName);
        }

        List<Part> parts = getOutputParts(portName, operationName);
        return getParameters(parts);
    }
    
    private LinkedHashMap<String, XmlSchemaObject> getParameters(List<Part> parts) {
        LinkedHashMap<String, XmlSchemaObject> parameters = new LinkedHashMap<String, XmlSchemaObject>();
        
        for (Part part : parts) {
            XmlSchemaObject parameter = getParameter(part);

            if (parameter == null) {
                logger.log(Level.WARNING, "can't find parameter type: {0}", part.getName());
            } else {
                parameters.put(part.getName(), parameter);
            }
        }
        
        return parameters;
    }
    
    private List<Part> getInputParts(String portName, String operationName) throws UnknownOperationException {
        
        List<Part> parts = new ArrayList<Part>();
        
        BindingOperation bindingOperation = getBindingOperation(portName, operationName);
        BindingInput bindingInput = bindingOperation.getBindingInput();
        if (bindingInput != null) {
            Operation operation = bindingOperation.getOperation();

            Input input = operation.getInput();
            if (input != null) {
                Collection<String> partNames = null;
                Message inputMessage = input.getMessage();

                List<ExtensibilityElement> extensibilityElements = bindingInput.getExtensibilityElements();
                for (ExtensibilityElement extensibilityElement : extensibilityElements) {
                    
                    
                    if (extensibilityElement instanceof SOAPBody) {
                        SOAPBody soapBody = (SOAPBody)extensibilityElement;
                        partNames = soapBody.getParts();
                    }
                    else if (extensibilityElement instanceof SOAP12Body) {
                        SOAP12Body soapBody = (SOAP12Body)extensibilityElement;
                        partNames = soapBody.getParts();
                    }
                    else {
                        continue;
                    }
                    
                    if (partNames == null) {
                        partNames = inputMessage.getParts().keySet();
                    }
                    
                    for (String partName : partNames) {
                        Part part = inputMessage.getPart(partName);
                        parts.add(part);
                    }
                }
            }
        }

        return parts;
    }
    
    private List<Part> getOutputParts(String portName, String operationName) throws UnknownOperationException {
        List<Part> parts = new ArrayList<Part>();
        
        BindingOperation bindingOperation = getBindingOperation(portName, operationName);
        Operation operation = bindingOperation.getOperation();

        Output output = operation.getOutput();
        if (output != null) {
            Message outputMessage = output.getMessage();

            List<ExtensibilityElement> extensibilityElements = bindingOperation.getBindingOutput().getExtensibilityElements();
            for (ExtensibilityElement extensibilityElement : extensibilityElements) {
                if (extensibilityElement instanceof SOAPBody) {
                    SOAPBody soapBody = (SOAPBody) extensibilityElement;

                    Collection<String> partNames = soapBody.getParts();

                    if (partNames == null) {
                        partNames = outputMessage.getParts().keySet();
                    }

                    for (String partName : partNames) {
                        Part part = outputMessage.getPart(partName);
                        parts.add(part);
                    }
                }
            }
        }

        return parts;
    }
    
    private XmlSchemaObject getParameter(Part part) {
        XmlSchemaObject parameter;
        
        QName elementName = part.getElementName();
        if (elementName != null) {
            parameter = schemas.getElementByQName(elementName);
            if (parameter == null) {
                logger.log(Level.WARNING, "can't find a global element: {0} trying with type...", elementName);
                parameter = schemas.getTypeByQName(elementName);
            }
        } else {
            QName typeName = part.getTypeName();
            if (typeName != null) {
                parameter = schemas.getTypeByQName(typeName);
            } else {
                return null;
            }
        }
        
        return parameter;
    }
    
    private String findPort(String operationName) throws UnknownOperationException {
        String port_name = null;
        
        loop:
        for (QName serviceName : getServices()) {
            for (String portName : getPorts(serviceName)) {
                for(BindingOperation bindingOperation : getBindingOperations(portName)) {
                    Operation operation = bindingOperation.getOperation();
                    if (operationName.equals(operation.getName())) {
                        if (port_name == null) {
                            port_name = portName;
                            break;
                        } else {
                            logger.log(Level.WARNING, "ambiguous operation name detected ({0})", operationName);
                            break loop;
                        }
                    }
                }
            }
        }
        
        if (port_name == null) {
            throw new UnknownOperationException("no '" + operationName + "' operation found");
        }
        
        return port_name;
    }
    
    private BindingOperation getBindingOperation(String portName, String operationName) throws UnknownOperationException {
        
        BindingOperation bindingOperation = null;
        
        for(BindingOperation bOperation : getBindingOperations(portName)) {
            Operation operation = bOperation.getOperation();
            if (operationName.equals(operation.getName())) {
                if (bindingOperation == null) {
                    bindingOperation = bOperation;
                } else {
                    logger.log(Level.WARNING, "overloaded operation detected ({0})", operationName);
                    break;
                }
            }
        }
        
        if (bindingOperation == null) {
            throw new UnknownOperationException("no '" + operationName + "' operation found in a '" + portName + "' port");
        }
        
        return bindingOperation;
    }
    
    private List<BindingOperation> getBindingOperations(String portName) {
        
        Port port = getPort(portName);
        if (port != null) {
            Binding binding = port.getBinding();
            if (binding != null) {
                List<ExtensibilityElement> bindingExElements = binding.getExtensibilityElements();
                for (ExtensibilityElement bindingExElement : bindingExElements) {
                    if (bindingExElement instanceof SOAPBinding ||
                        bindingExElement instanceof SOAP12Binding) {
                        return binding.getBindingOperations();
                    }
                }
            }
        }

        return Collections.EMPTY_LIST;
    }

    private Port getPort(String portName) {

        Collection<Service> services = definition.getAllServices().values();
        for (Service service : services) {
            Collection<Port> ports = service.getPorts().values();
            for (Port port : ports) {
                if (portName.equals(port.getName())) {
                    return port;
                }
            }
        }
        
        return null;
    }

    /*
     * message is ether BindingInput or BindingOutput
     */
    private String getNamespaceURI(ElementExtensible message) {

        List<ExtensibilityElement> extensibilityElements = message.getExtensibilityElements();
        for (ExtensibilityElement extensibilityElement : extensibilityElements) {
            if (extensibilityElement instanceof SOAPBody) {
                SOAPBody soapBody = (SOAPBody)extensibilityElement;
                return soapBody.getNamespaceURI();
            } else if (extensibilityElement instanceof SOAP12Body) {
                SOAP12Body soapBody = (SOAP12Body)extensibilityElement;
                return soapBody.getNamespaceURI();
            }
        }
        
        return null;
    }
    
    /*
     * message is ether BindingInput or BindingOutput
     */
    private String getUse(ElementExtensible message) {

        List<ExtensibilityElement> extensibilityElements = message.getExtensibilityElements();
        for (ExtensibilityElement extensibilityElement : extensibilityElements) {
            if (extensibilityElement instanceof SOAPBody) {
                SOAPBody soapBody = (SOAPBody)extensibilityElement;
                return soapBody.getUse();
            } else if (extensibilityElement instanceof SOAP12Body) {
                SOAP12Body soapBody = (SOAP12Body)extensibilityElement;
                return soapBody.getUse();
            }
        }
        
        return null;
    }
    
    private String getStyle(Binding binding) {
        String style = null;
        
        List<ExtensibilityElement> bindingExElements = binding.getExtensibilityElements();
        for (ExtensibilityElement bindingExElement : bindingExElements) {
            if (bindingExElement instanceof SOAPBinding) {
                SOAPBinding soapBinding = (SOAPBinding)bindingExElement;
                style = soapBinding.getStyle();
                break;
            } else if (bindingExElement instanceof SOAP12Binding) {
                SOAP12Binding soap12Binding = (SOAP12Binding)bindingExElement;
                style = soap12Binding.getStyle();
                break;
            }
        }
        
        return style != null ? style : "document";
    }
    
    private String getStyle(BindingOperation bindingOperation) {
        
        String style = null;
        
        List<ExtensibilityElement> extensibilityElements = bindingOperation.getExtensibilityElements();
        for (ExtensibilityElement extensibilityElement : extensibilityElements) {
            if (extensibilityElement instanceof SOAPOperation) {
                SOAPOperation soapOperation = (SOAPOperation) extensibilityElement;
                style = soapOperation.getStyle();
            }
        }
        
        return style;
    }
    
    private String getSOAPActionURI(BindingOperation bindingOperation) {
        
        List<ExtensibilityElement> extensibilityElements = bindingOperation.getExtensibilityElements();
        for (ExtensibilityElement extensibilityElement : extensibilityElements) {
            if (extensibilityElement instanceof SOAPOperation) {
                SOAPOperation soapOperation = (SOAPOperation) extensibilityElement;
                return soapOperation.getSoapActionURI();
            }
        }
        
        return null;
    }

    private String getSoapAddress(Port port) {
        List<ExtensibilityElement> extensibilityElements = port.getExtensibilityElements();
        for (ExtensibilityElement extensibilityElement : extensibilityElements) {
            if (extensibilityElement instanceof SOAPAddress) {
                SOAPAddress soapAddress = (SOAPAddress)extensibilityElement;
                return soapAddress.getLocationURI();
            } else if (extensibilityElement instanceof SOAP12Address) {
                SOAP12Address soapAddress = (SOAP12Address)extensibilityElement;
                return soapAddress.getLocationURI();
            }
        }
        
        return null;
    }
    
    private String getDocumentation(WSDLElement wsdlElement) {

        Element element = wsdlElement.getDocumentationElement();
        if (element != null && element.getFirstChild() != null) {
            return element.getFirstChild().getNodeValue();
        }

        return "";
    }
    /*
     * Recursive method to process all Schemas defined in definition and its imports
     */
    private void addXmlSchemas(XmlSchemaCollection xmlSchemaCollection, Definition definition) throws IOException {
        xmlSchemaCollection.setNamespaceContext(new NamespaceMap(definition.getNamespaces()));

        SchemaResolver resolver = new SchemaResolver(definition.getDocumentBaseURI());
        xmlSchemaCollection.setSchemaResolver(resolver);

        Types types = definition.getTypes();
        if (types != null) {
            List elements = types.getExtensibilityElements();
            for (Object o : elements) {
                if (o instanceof Schema) {
                    Schema schema = (Schema) o;
                    Element element = schema.getElement();
                    if (element != null) {
                        resolver.add(element);
                    }
                }
            }

            for (Element element : resolver) {
                InputSource source = resolver.getInputSource(element);
                String publicId = source.getPublicId();
                if ((publicId.isEmpty() && xmlSchemaCollection.getXmlSchema(source.getSystemId()).length == 0) || 
                    xmlSchemaCollection.schemaForNamespace(publicId) == null) {
                    xmlSchemaCollection.read(source);
                }
            }
        }

        // process imported schemas
        Map imports = definition.getImports();
        if (imports != null) {
            for (Iterator<List<Import>> iter = imports.values().iterator(); iter.hasNext();) {
                for (Import _import : iter.next()) {
                    addXmlSchemas(xmlSchemaCollection, _import.getDefinition());
                }
            }
        }
    }
    
    private void checkWSRFPorts(Map<String, WSRF_Version> wsrfPorts) {
        Collection<Service> services = definition.getAllServices().values();
        for (Service service : services) {
            Collection<Port> ports = service.getPorts().values();
            loop:
            for (Port port : ports) {
                String portName = port.getName();
                Binding binding = port.getBinding();
                List<BindingOperation> bindingOperations = binding.getBindingOperations();
                for(BindingOperation bindingOperation : bindingOperations) {
                    Operation operation = bindingOperation.getOperation();
                    String operationName = operation.getName();
                    for (WSRF_RPOperation resourcePropertyOperation : WSRF_RPOperation.values()) {
                        if (operationName.equals(resourcePropertyOperation.name())) {
                            try {
                                List<Part> parts = getInputParts(portName, operationName);
                                if (parts.size() > 0) {
                                    Part part = parts.get(0);
                                    QName qname = part.getElementName();
                                    if (qname == null) {
                                        qname = part.getTypeName();
                                    }
                                    
                                    if (qname != null) {
                                        String namespace = qname.getNamespaceURI();
                                        if (namespace != null && namespace.length() > 0) {
                                            for (WSRF_Version wsrfVersion : WSRF_Version.values()) {
                                                if (wsrfVersion.WSRF_RP.equals(namespace)) {
                                                    if (!WSRF_Version.Standard.equals(wsrfVersion)) {
                                                        logger.log(Level.WARNING, "draft WSRF version found for the WSRF operation: {0} ({1})", new Object[]{operationName, wsrfVersion.name()});
                                                    }
                                                    else {
                                                        String soapAction = getSOAPActionURI(bindingOperation);
                                                        if (soapAction == null) {
                                                            logger.log(Level.WARNING, "no soap action for the WSRF operation: {0}( {1})", new Object[]{operationName, resourcePropertyOperation.SOAP_ACTION});
                                                        } else if (!resourcePropertyOperation.SOAP_ACTION.equals(soapAction)) {
                                                            logger.log(Level.WARNING, "wrong soap action for the WSRF operation: {0}( {1})", new Object[]{operationName, resourcePropertyOperation.SOAP_ACTION});
                                                        }
                                                    }

                                                    wsrfPorts.put(portName, wsrfVersion);
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                }
                                
                            } catch (UnknownOperationException ex) {}
                            break loop;
                        }
                    }
                }
            }
        }
    }

    public static synchronized WSDL11Parser getWSDL11Parser(String wsdlLocation) throws WSDLException, IOException {
        WSDL11Parser parser = null;
        
        if (parsers == null) {
            parsers = new TreeMap<String, WSDL11Parser>();
        } else {
            parser = parsers.get(wsdlLocation);
        }
        
        if (parser == null) {
            WSDLFactory factory = WSDLFactory.newInstance();
            WSDLReader reader = factory.newWSDLReader();
            reader.setFeature("javax.wsdl.verbose", false);
            Definition definition = reader.readWSDL(wsdlLocation);

            parser = new WSDL11Parser(definition);
            
            parsers.put(wsdlLocation, parser);
        }
        
        return parser;
    }
}
