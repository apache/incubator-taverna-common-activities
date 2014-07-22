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
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

import javax.wsdl.Binding;
import javax.wsdl.BindingOperation;
import javax.wsdl.Definition;
import javax.wsdl.Operation;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.soap.SOAPBinding;
import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaObject;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.ibm.wsdl.extensions.soap.SOAPBindingImpl;


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

    private static final String SERVICE_SECURITY_URI = "http://security.introduce.cagrid.nci.nih.gov/ServiceSecurity/";
    
    private WSDL11Parser parser;

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
        //try {
        //    parser = WSDL20Parser.getWSDL20Parser(wsdlLocation);
        //} catch (Exception ex) {
            parser = WSDL11Parser.getWSDL11Parser(wsdlLocation);
        //}
    }

    public List<QName> getServices() {
        return parser.getServices();
    }
    
    public List<String> getPorts(QName serviceName) {
        return parser.getPorts(serviceName);
    }

    /**
     * @return a list of WSDLOperations for all operations for this service,
     */
    public List<String> getOperations() {
        List<String> operations = new ArrayList<String>();
        
        for (QName serviceName : parser.getServices()) {
            for (String portName : parser.getPorts(serviceName)) {
                operations.addAll(parser.getOperations(portName));
            }
        }
        return operations;
    }

    /**
     * Checks whether ANY of defined ports is an WSRF one.
     * 
     * @return true if there is at least one WSRF port found.
     */
    public boolean isWsrfService() {
        for (QName serviceName : parser.getServices()) {
            for (String portName : parser.getPorts(serviceName)) {
                if (parser.isWSRFPort(portName) != null) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * flushes all the caches of entries associated with provided wsdl location
     *
     * @param wsdlLocation
     */
    public synchronized static void flushCache(String wsdlLocation) {
    }

    /**
     * @return the wsdl location for which this parser was constructed
     */
    public String getWSDLLocation() {
        return parser.getDocumentBaseURI();
    }

    public String getOperationEndpointLocation(String portName) {
        return parser.getSoapAddress(portName);
    }
    
    public List<String> getOperationEndpointLocations(String operationName) throws UnknownOperationException {
        
        String soapAddress = parser.getOperationEndpointLocation(operationName);
        return Arrays.asList(soapAddress);
    }

    /**
     * N.B. a style is defined on per operation basis, so there can be different types within a same document.
     * This method is to be removed in the next versions of parser.
     * 
     * @return the style, i.e. document or rpc
     * @deprecated
     */
    @Deprecated
    public String getStyle() throws UnknownOperationException {
        for (QName serviceName : parser.getServices()) {
            for (String portName : parser.getPorts(serviceName)) {
                for (String operationName : parser.getOperations(portName)) {
                    return parser.getOperationStyle(portName, operationName);
                }
            }
        }
        return "document";
    }
    
    public String getStyle(String operationName) throws UnknownOperationException {
        return parser.getOperationStyle(null, operationName);
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

        return TypeDescriptors.getDescriptors(parser.getInputParameters(null, operationName));
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
        
        return TypeDescriptors.getDescriptors(parser.getOutputParameters(null, operationName));
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
        QName rpcMethodName = getOperationQname(operationName);
        return rpcMethodName == null ? "" : rpcMethodName.getNamespaceURI();
    }

    public QName getOperationQname(String operationName)
            throws UnknownOperationException {
        
        if ("rpc".equals(parser.getOperationStyle(null, operationName))) {
            QName rpcMethodName = parser.getRPCRequestMethodName(null, operationName);
            if (rpcMethodName == null) {
                parser.getRPCResponseMethodName(null, operationName);
            }
            
            return rpcMethodName;
        }
        
        // all these things are odd... there is no any wrapper for the 'document' style
        LinkedHashMap<String, XmlSchemaObject> parameters = parser.getInputParameters(null, operationName);
        if (parameters.size() > 0) {
            XmlSchemaObject xmlSchemaObject = parameters.values().iterator().next();
            if (xmlSchemaObject instanceof XmlSchemaElement) {
                XmlSchemaElement element = (XmlSchemaElement)xmlSchemaObject;
                return element.getQName();
            }
        }
        
        return null;
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
        String result = parser.getSoapInputUse(null, operationName);
        if (result == null) {
            result = parser.getSoapOutputUse(null, operationName);
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
        return parser.getSOAPActionURI(null, operationName);
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
        return parser.getOperationDocumentation(null, operationName);
    }

    public WSRF_Version isWSRFPort(String portName) {
        return parser.isWSRFPort(portName);
    }
    
    public Definition getDefinition() {
        return parser.getDefinition();
    }
    
	public String getServiceDocumentation() {
		if (getDefinition() == null){
			return "";
		}
		Service service = (Service) getDefinition().getServices().values()
				.iterator().next(); // Get the first service
		if (service != null) {
			if (service.getDocumentationElement() != null) {
				String text = getTextForNode(service.getDocumentationElement());
				if (text != null){
					return text;
				}
			}
		} else if (getDefinition().getDocumentationElement() != null) {
			String text = getTextForNode(getDefinition().getDocumentationElement());
			if (text != null){
				return text;
			}
		}
		return "";
	}
    
	
	// Get the text value of the node - look only at immediate children elements.
	// Services like:
	// http://api.bioinfo.no/wsdl/Blast.wsdl
	// http://api.bioinfo.no/wsdl/JasparDB.wsdl
	// have complex or mixed type elements for the documentation element so 
	// we have to do some smart extraction here.
	public static String getTextForNode(Node node){
		// If element contains text only - return that text
		if (node.getNodeType() == org.w3c.dom.Node.TEXT_NODE) {
			return node.getNodeValue();
		} else {
			// If element is a mixed content or complex element - try to extract the text inside
			NodeList list = node.getChildNodes();

			for (int i = 0; i < list.getLength(); i++) {
				if (list.item(i).getNodeType() == org.w3c.dom.Node.TEXT_NODE) {
					// return the fist text element
					return list.item(i).getNodeValue();
				}
			}
		}
		return null;
	}
	
	/**
	 * Returns transport attribute if binding is a SOAP binding or null
	 * otherwise.
	 * 
	 * @param binding
	 * @return
	 */
	public String getTransportForSOAPBinding(Binding binding) {

		String transport = null;
		List extensibilityElementList = binding.getExtensibilityElements();
		for (Object extensibilityElement : extensibilityElementList) {
			ExtensibilityElement ee = (ExtensibilityElement) extensibilityElement;
			if (ee instanceof SOAPBindingImpl) {
				SOAPBinding soapBinding = (SOAPBinding) ee;
				transport = soapBinding.getTransportURI();
			}
		}
		return transport;
	}

	/**
	 * Return the URI location of the port's SOAP address element, if it exists,
	 * or null otherwise.
	 * 
	 * @param port
	 * @return
	 */
	public String getSOAPAddressLocationForPort(Port port) {

		if (port == null){
			return null;
		}
		else{
			return parser.getSoapAddress(port.getName());
		}
	}
	
	public boolean isSOAPBinding(Binding binding) {
		List extensibilityElementList = binding.getExtensibilityElements();
		for (Object extensibilityElement : extensibilityElementList) {
			ExtensibilityElement ee = (ExtensibilityElement) extensibilityElement;
			if (ee instanceof SOAPBindingImpl) {
				return true;
			}
		}
		return false;
	}
	
    public List<Operation> getOperationsForPort(String portName) {
        List<Operation> operations = new ArrayList<Operation>();
        for (BindingOperation bindingOperation : parser.getBindingOperations(portName)) {
            Operation operation = bindingOperation.getOperation();
            if (operation != null) {
                operations.add(operation);
            }
        }
        return operations;
    }
	
	public String getParameterOrder(Operation operation) {
		if (operation.getParameterOrdering() != null) {
			return Arrays.toString(operation.getParameterOrdering().toArray());
		} else {
			return "";
		}
	}

}
