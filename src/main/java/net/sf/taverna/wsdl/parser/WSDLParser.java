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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.wsdl.Binding;
import javax.wsdl.BindingOperation;
import javax.wsdl.Definition;
import javax.wsdl.Import;
import javax.wsdl.Operation;
import javax.wsdl.Part;
import javax.wsdl.Port;
import javax.wsdl.PortType;
import javax.wsdl.Service;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.wsdl.extensions.soap.SOAPBinding;
import javax.wsdl.extensions.soap.SOAPBody;
import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import com.ibm.wsdl.extensions.soap.SOAPBindingImpl;
import com.ibm.wsdl.extensions.soap.SOAPOperationImpl;
import javax.wsdl.*;
import javax.wsdl.extensions.schema.Schema;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import org.apache.ws.commons.schema.*;
import org.apache.ws.commons.schema.utils.NamespaceMap;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

/**
 * A Parser for processing WSDL files to determine information about available
 * services and the required types needed to invoke that particular service.
 * Handles Complex Types and wsdl imports.
 *
 * @author Stuart Owen
 * @author Stian Soiland-Reyes
 *
 */
@SuppressWarnings("unchecked")
public class WSDLParser {

    private static final String GET_SERVICE_SECURITY_METADATA_REQUEST = "GetServiceSecurityMetadataRequest";
    private static final String GET_SERVICE_SECURITY_METADATA = "getServiceSecurityMetadata";
    private static final String SET_TERMINATION_TIME = "SetTerminationTime";
    private static final String GET_RESOURCE_PROPERTY = "GetResourceProperty";
    private static final String DESTROY = "Destroy";
    private static final String SERVICE_SECURITY_URI = "http://security.introduce.cagrid.nci.nih.gov/ServiceSecurity/";
    private static final String RESOURCE_LIFETIME_URI = "http://docs.oasis-open.org/wsrf/2004/06/wsrf-WS-ResourceLifetime/";
    private static final String RESOURCE_PROPERTIES_URI = "http://docs.oasis-open.org/wsrf/2004/06/wsrf-WS-ResourceProperties/";
    private static Logger logger = Logger.getLogger(WSDLParser.class);
    /**
     * Cache for operations, to remove the need for reprocessing each time.
     */
    private static Map<String, List<Operation>> operationMap = Collections.synchronizedMap(new HashMap<String, List<Operation>>());
    private static Map<String, Map<String, Binding>> bindingMap = Collections.synchronizedMap(new HashMap<String, Map<String, Binding>>());
    private static Map<String, String> styleMap = Collections.synchronizedMap(new HashMap<String, String>());
    private static Map<String, Map<String, PortType>> portTypeMap = Collections.synchronizedMap(new HashMap<String, Map<String, PortType>>());
    private Map<String, BindingOperation> bindingOperations = Collections.synchronizedMap(new HashMap<String, BindingOperation>());
    private boolean isWsrfService;
    private Definition definition;
    private TypeDescriptors types;

    public boolean isWsrfService() {
        return isWsrfService;
    }

    /**
     * Constructor which takes the location of the base wsdl file, and begins to
     * process it
     *
     * @param wsdlLocation - the location of the wsdl file
     * @throws ParserConfigurationException
     * @throws WSDLException
     * @throws IOException
     * @throws SAXException
     */
    public WSDLParser(String wsdlLocation) throws ParserConfigurationException,
            WSDLException, IOException, SAXException {
        try {
            WSDLFactory factory = WSDLFactory.newInstance();
            WSDLReader reader = factory.newWSDLReader();
            reader.setFeature("javax.wsdl.verbose", false);
            definition = reader.readWSDL(wsdlLocation);

            XmlSchemaCollection schemas = new XmlSchemaCollection();
            addXmlSchemas(schemas, definition);

            types = new TypeDescriptors(schemas);

            operationMap.put(wsdlLocation, determineOperations());
        } catch (WSDLException ex) {
            throw new IOException(ex.getMessage());
        }

        checkWSRF();
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
                if (xmlSchemaCollection.schemaForNamespace(source.getPublicId()) == null) {
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

    /**
     * flushes all the caches of entries associated with provided wsdl location
     *
     * @param wsdlLocation
     */
    public synchronized static void flushCache(String wsdlLocation) {
        operationMap.remove(wsdlLocation);
        bindingMap.remove(wsdlLocation);
        styleMap.remove(wsdlLocation);
        portTypeMap.remove(wsdlLocation);
    }

    /**
     * @return a list of WSDLOperations for all operations for this service,
     */
    public List<Operation> getOperations() {
        return operationMap.get(getWSDLLocation());
    }

    /**
     * @return the wsdl location for which this parser was constructed
     */
    public String getWSDLLocation() {
        return definition.getDocumentBaseURI();
    }

    /**
     * @return the Definition for this service
     */
    public Definition getDefinition() {
        return definition;
    }

    public List<String> getOperationEndpointLocations(String operationName) {
        List<String> result = new ArrayList<String>();
        Collection<Service> services = getDefinition().getServices().values();
        Binding binding = getBinding(operationName);
        for (Service service : services) {
            Collection<Port> ports = service.getPorts().values();
            for (Port port : ports) {
                if (port.getBinding().equals(binding)) {
                    for (Object obj : port.getExtensibilityElements()) {
                        if (obj instanceof SOAPAddress) {
                            SOAPAddress address = (SOAPAddress) obj;
                            String endpoint = address.getLocationURI();
                            result.add(endpoint);
                        }
                    }
                }
            }
        }
        return result;
    }

    private List<Operation> determineOperations() {
        List<Operation> result = new ArrayList<Operation>();

        Map<String, PortType> portToOperationMap = portTypeToOperationMap();
        Map<String, Binding> bindingToOperationMap = bindingToOperationMap();

        Map bindings = definition.getBindings();
        for (Iterator iterator = bindings.values().iterator(); iterator.hasNext();) {
            Binding binding = (Binding) iterator.next();
            List extensibilityElementList = binding.getExtensibilityElements();
            for (Iterator k = extensibilityElementList.iterator(); k.hasNext();) {
                ExtensibilityElement ee = (ExtensibilityElement) k.next();
                if (ee instanceof SOAPBindingImpl) {
                    SOAPBinding soapBinding = (SOAPBinding) ee;
                    PortType portType = binding.getPortType();

                    setStyleForBinding(soapBinding);

                    for (Iterator opIterator = portType.getOperations().iterator(); opIterator.hasNext();) {
                        Operation op = (Operation) opIterator.next();
                        result.add(op);
                        portToOperationMap.put(op.getName(), portType);
                        bindingToOperationMap.put(op.getName(), binding);
                    }
                }
            }
        }

        Map imports = definition.getImports();
        if (imports != null && imports.size() > 0) {
            result.addAll(processImports(imports));
        }

        return result;
    }

    private Map<String, Binding> bindingToOperationMap() {
        Map<String, Binding> bindingToOperationMap = bindingMap.get(getWSDLLocation());
        if (bindingToOperationMap == null) {
            bindingToOperationMap = new HashMap<String, Binding>();
            bindingMap.put(getWSDLLocation(), bindingToOperationMap);
        }
        return bindingToOperationMap;
    }

    private Map<String, PortType> portTypeToOperationMap() {
        Map<String, PortType> portToOperationMap = portTypeMap.get(getWSDLLocation());
        if (portToOperationMap == null) {
            portToOperationMap = new HashMap<String, PortType>();
            portTypeMap.put(getWSDLLocation(), portToOperationMap);
        }
        return portToOperationMap;
    }

    private List<Operation> processImports(Map imports) {
        List<Operation> result = new ArrayList<Operation>();

        Map<String, PortType> portToOperationMap = portTypeToOperationMap();
        Map<String, Binding> bindingToOperationMap = bindingToOperationMap();

        for (Iterator iterator = imports.values().iterator(); iterator.hasNext();) {
            List list = (List) iterator.next();
            for (Iterator importIterator = list.iterator(); importIterator.hasNext();) {
                Import imp = (Import) importIterator.next();
                Map bindings = imp.getDefinition().getBindings();
                for (Iterator bindingsIterator = bindings.values().iterator(); bindingsIterator.hasNext();) {
                    Binding binding = (Binding) bindingsIterator.next();
                    List extensibilityElementList = binding.getExtensibilityElements();
                    for (Iterator k = extensibilityElementList.iterator(); k.hasNext();) {
                        ExtensibilityElement ee = (ExtensibilityElement) k.next();
                        if (ee instanceof SOAPBindingImpl) {
                            SOAPBinding soapBinding = (SOAPBinding) ee;
                            PortType portType = binding.getPortType();

                            setStyleForBinding(soapBinding);

                            for (Iterator opIterator = portType.getOperations().iterator(); opIterator.hasNext();) {
                                Operation op = (Operation) opIterator.next();
                                result.add(op);
                                portToOperationMap.put(op.getName(), portType);
                                bindingToOperationMap.put(op.getName(), binding);
                            }
                        }
                    }
                }

            }
        }

        return result;
    }

    private Binding getBinding(String operationName) {
        Binding result = null;
        Map<String, Binding> bindingToOpMap = bindingMap.get(getWSDLLocation());
        if (bindingToOpMap != null) {
            result = bindingToOpMap.get(operationName);
        }
        return result;
    }

    /**
     *
     * @return the style, i.e. document or rpc
     */
    public String getStyle() {
        return styleMap.get(getWSDLLocation());
    }

    /**
     * Provides the PortType for a given operation.
     *
     * @param operationName the name of the operation the PortType is required
     * for.
     * @return the PortType
     */
    public PortType getPortType(String operationName) {
        PortType result = null;
        Map<String, PortType> portToOpMap = portTypeMap.get(getWSDLLocation());
        if (portToOpMap != null) {
            result = portToOpMap.get(operationName);
        }
        return result;
    }

    /**
     * Returns a List of the TypeDescriptors representing the parameters for the
     * inputs to the service
     *
     * @param operationName
     * @return List of TypeDescriptor
     * @throws UnknownOperationException if no operation matches the name
     * @throws IOException
     *
     */
    public List<TypeDescriptor> getOperationInputParameters(String operationName)
            throws UnknownOperationException, IOException {

        List<TypeDescriptor> result = new ArrayList<TypeDescriptor>();

        BindingOperation bindingOperation = getBindingOperation(operationName);
        Operation operation = bindingOperation.getOperation();

        Input input = operation.getInput();
        Message inputMessage = input.getMessage();

        List<ExtensibilityElement> extensibilityElements = bindingOperation.getBindingInput().getExtensibilityElements();
        for (ExtensibilityElement extensibilityElement : extensibilityElements) {
            if (extensibilityElement instanceof SOAPBody) {
                SOAPBody soapBody = (SOAPBody) extensibilityElement;

                Collection<String> partNames = soapBody.getParts();

                if (partNames == null) {
                    partNames = inputMessage.getParts().keySet();
                }

                for (String partName : partNames) {
                    Part part = inputMessage.getPart(partName);
                    result.add(processParameter(part));
                }
            }
        }

        return result;
    }

    /**
     * Returns a List of the TypeDescriptors representing the parameters for the
     * outputs of the service
     *
     * @param operationName
     * @return List of TypeDescriptor
     * @throws UnknownOperationException if no operation matches the name
     * @throws IOException
     */
    public List<TypeDescriptor> getOperationOutputParameters(
            String operationName) throws UnknownOperationException, IOException {
        List<TypeDescriptor> result = new ArrayList<TypeDescriptor>();

        BindingOperation bindingOperation = getBindingOperation(operationName);
        Operation operation = bindingOperation.getOperation();

        Output output = operation.getOutput();
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
                    result.add(processParameter(part));
                }
            }
        }

        return result;
    }

    /**
     * returns the namespace uri for the given operation name, throws
     * UnknownOperationException if the operationName is not matched to one
     * described by the WSDL. <p> Note that if you need the namespace for
     * constructing the fully qualified element name of the operation, you might
     * want to use {@link #getOperationQname(String)} instead.
     *
     * @see #getOperationQname(String)
     * @param operationName
     * @return
     * @throws UnknownOperationException
     */
    public String getOperationNamespaceURI(String operationName)
            throws UnknownOperationException {

        String result = null;
        if (getStyle().equals("document")) {
            try {
                // this lovely line of code gets the correct namespace ....
                result = ((Part) getBindingOperation(operationName).getOperation().getInput().getMessage().getOrderedParts(null).get(0)).getElementName().getNamespaceURI();
            } catch (Exception e) {
                // .... but this gets a good approximation if the above fails
                result = getDefinition().getTargetNamespace();
            }
        } else {
            BindingOperation binding = getBindingOperation(operationName);
            List extElements = binding.getBindingInput().getExtensibilityElements();
            if (extElements != null && extElements.size() > 0) {
                SOAPBody body = (SOAPBody) extElements.get(0);
                result = body.getNamespaceURI();
            } else {
                extElements = binding.getBindingOutput().getExtensibilityElements();
                if (extElements != null && extElements.size() > 0) {
                    SOAPBody body = (SOAPBody) extElements.get(0);
                    result = body.getNamespaceURI();
                }
            }

            if (result == null) {
                // as a fall back, this almost always gives the right namespace
                result = getDefinition().getTargetNamespace();
            }
        }

        return result;
    }

    public QName getOperationQname(String operationName)
            throws UnknownOperationException {
        if (getStyle().equals("document")) {
            try {
                // Get the QName of the first element of the input message
                return ((Part) getBindingOperation(operationName).getOperation().getInput().getMessage().getOrderedParts(null).get(0)).getElementName();
            } catch (RuntimeException e) {
                logger.warn("Could not find qname of message for operation "
                        + operationName, e);
                String ns = getDefinition().getTargetNamespace();
                return new QName(ns, operationName);
            }
        } else {
            String ns = getOperationNamespaceURI(operationName);
            return new QName(ns, operationName);
        }
    }

    /**
     * Returns either literal or encoded, describing the 'use' for this
     * operation
     *
     * @param operationName
     * @return
     * @throws UnknownOperationException
     */
    public String getUse(String operationName) throws UnknownOperationException {
        String result = null;

        BindingOperation binding = getBindingOperation(operationName);
        List extElements = binding.getBindingInput().getExtensibilityElements();
        if (extElements != null && extElements.size() > 0) {
            SOAPBody body = (SOAPBody) extElements.get(0);
            result = body.getUse();
        } else {
            extElements = binding.getBindingOutput().getExtensibilityElements();
            if (extElements != null && extElements.size() > 0) {
                SOAPBody body = (SOAPBody) extElements.get(0);
                result = body.getUse();
            }
        }

        return result;
    }

    /**
     * Returns the actionURI for the given operation
     *
     * @param operationName
     * @return
     * @throws UnknownOperationException
     */
    public String getSOAPActionURI(String operationName)
            throws UnknownOperationException {
        String result = null;
        BindingOperation op = getBindingOperation(operationName);
        List elements = op.getExtensibilityElements();
        for (Iterator elIterator = elements.iterator(); elIterator.hasNext();) {
            SOAPOperationImpl extension = (SOAPOperationImpl) elIterator.next();
            result = extension.getSoapActionURI();
            break;
        }
        return result;
    }

    /**
     * Provides the documentation for the given operation name, or returns an
     * empty string if no documentation is provided by the WSDL.
     *
     * @param operationName
     * @return
     * @throws UnknownOperationException
     */
    public String getOperationDocumentation(String operationName)
            throws UnknownOperationException {
        String result = "";

        Operation operation = getOperation(operationName);
        if (operation.getDocumentationElement() != null) {
            if (operation.getDocumentationElement().getFirstChild() != null) {
                result = operation.getDocumentationElement().getFirstChild().getNodeValue();
            }
        }

        return result;
    }

    /**
     * Returns a WSDLOperation descriptor for an operation that matches the
     * operationName.
     *
     * @param operationName
     * @return a matching WSDLOperation descriptor
     * @throws UnknowOperationException if no operation matches the name
     */
    public Operation getOperation(String operationName)
            throws UnknownOperationException {
        Operation result = null;

        for (Iterator iterator = getOperations().iterator(); iterator.hasNext();) {
            Operation op = (Operation) iterator.next();
            if (op.getName().equals(operationName)) {
                result = op;
                break;
            }
        }
        if (result == null) {
            throw new UnknownOperationException("No operation named: "
                    + operationName + " exists");
        }
        return result;
    }

    /**
     * SOAP actions/operations that if present indicates a WSRF service. <p>
     * Used by {@link #checkWSRF()}
     *
     * @return A {@link Map} mapping SOAP operation name to SOAP action URI.
     */
    protected Map<String, String> getWSRFPredictorOperations() {
        Map<String, String> operations = new HashMap<String, String>();

        operations.put(GET_RESOURCE_PROPERTY, RESOURCE_PROPERTIES_URI
                + GET_RESOURCE_PROPERTY);

        operations.put(DESTROY, RESOURCE_LIFETIME_URI + DESTROY);

        operations.put(SET_TERMINATION_TIME, RESOURCE_LIFETIME_URI
                + SET_TERMINATION_TIME);

        operations.put(GET_SERVICE_SECURITY_METADATA, SERVICE_SECURITY_URI
                + GET_SERVICE_SECURITY_METADATA_REQUEST);

        return operations;
    }

    /**
     * Check if this is a WSRF-resource property supporting binding. <p> The
     * service is determined to be WSRF-supporting if the WSDL contains at least
     * one of the operations specified by {@link #getWSRFPredictorOperations()}.
     *
     */
    protected void checkWSRF() {
        isWsrfService = false;
        for (Entry<String, String> resourceEntry : getWSRFPredictorOperations().entrySet()) {
            String actionURI;
            try {
                actionURI = getSOAPActionURI(resourceEntry.getKey());
            } catch (UnknownOperationException e) {
                continue;
            }
            isWsrfService = resourceEntry.getValue().equals(actionURI);
            if (isWsrfService) {
                // Just need to match one of the predictors
                break;
            }
        }
    }

    private void setStyleForBinding(SOAPBinding soapBinding) {
        String style = soapBinding.getStyle();
        if (style == null) {
            style = "document"; // soap spec specifies to default to document if
        }								// missing.
        styleMap.put(getWSDLLocation(), style);
    }

    private BindingOperation getBindingOperation(String operationName)
            throws UnknownOperationException {
        BindingOperation result = bindingOperations.get(operationName);
        if (result == null) {
            Binding binding = getBinding(operationName);
            if (binding != null) {
                List bindings = binding.getBindingOperations();
                for (Iterator iterator = bindings.iterator(); iterator.hasNext();) {
                    BindingOperation bindingOperation = (BindingOperation) iterator.next();
                    if (bindingOperation.getOperation().getName().equals(
                            operationName)) {
                        result = bindingOperation;
                        bindingOperations.put(operationName, result);
                        break;
                    }
                }
            }
        }
        if (result == null) {
            throw new UnknownOperationException(
                    "Can't find binding operation for '" + operationName + "'");
        }
        return result;
    }

    private TypeDescriptor processParameter(Part part) {
        TypeDescriptor typeDesc;

        QName elementName = part.getElementName();
        if (elementName != null) {
            typeDesc = types.getElementDescriptor(elementName);
        } else {
            QName typeName = part.getTypeName();
            if (typeName != null) {
                typeDesc = types.getTypeDescriptor(typeName);
            } else {
                return null;
            }
        }

        typeDesc.setName(part.getName());

        return typeDesc;
    }
}
