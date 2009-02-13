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
import java.util.Set;

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

import org.apache.axis.wsdl.gen.NoopFactory;
import org.apache.axis.wsdl.symbolTable.BindingEntry;
import org.apache.axis.wsdl.symbolTable.CollectionElement;
import org.apache.axis.wsdl.symbolTable.CollectionType;
import org.apache.axis.wsdl.symbolTable.DefinedElement;
import org.apache.axis.wsdl.symbolTable.DefinedType;
import org.apache.axis.wsdl.symbolTable.ElementDecl;
import org.apache.axis.wsdl.symbolTable.Parameter;
import org.apache.axis.wsdl.symbolTable.Parameters;
import org.apache.axis.wsdl.symbolTable.SymbolTable;
import org.apache.axis.wsdl.symbolTable.TypeEntry;
import org.apache.log4j.Logger;
//import org.apache.wsif.providers.soap.apacheaxis.WSIFDynamicProvider_ApacheAxis;
//import org.apache.wsif.util.WSIFPluggableProviders;
import org.xml.sax.SAXException;

import com.ibm.wsdl.extensions.soap.SOAPBindingImpl;
import com.ibm.wsdl.extensions.soap.SOAPOperationImpl;

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
	
	private static final String GET_RESOURCE_PROPERTY_METHOD = "GetResourceProperty";

	private static final String GET_RESOURCE_PROPERTY_ACTION = "http://docs.oasis-open.org/wsrf/2004/06/wsrf-WS-ResourceProperties/GetResourceProperty";

	private static Logger logger = Logger.getLogger(WSDLParser.class);
	
	private String wsdlLocation;

	/**
	 * Cache for SymbolTable to remove the need for reprocessing each time.
	 */
	private static Map<String, SymbolTable> symbolTableMap = Collections
			.synchronizedMap(new HashMap<String, SymbolTable>());

	/**
	 * Cache for operations, to remove the need for reprocessing each time.
	 */
	private static Map<String, List<Operation>> operationMap = Collections
			.synchronizedMap(new HashMap<String, List<Operation>>());

	private static Map<String, Map<String, Binding>> bindingMap = Collections
			.synchronizedMap(new HashMap<String, Map<String, Binding>>());

	private static Map<String, String> styleMap = Collections
			.synchronizedMap(new HashMap<String, String>());

	private static Map<String, Map<String, PortType>> portTypeMap = Collections
			.synchronizedMap(new HashMap<String, Map<String, PortType>>());

	private Map<String, ComplexTypeDescriptor> cachedComplexTypes = Collections
			.synchronizedMap(new HashMap<String, ComplexTypeDescriptor>());

	private Map<String, BindingOperation> bindingOperations = Collections
			.synchronizedMap(new HashMap<String, BindingOperation>());

	private boolean isWsrfService;

	public boolean isWsrfService() {
		return isWsrfService;
	}

	/**
	 * Constructor which takes the location of the base wsdl file, and begins to
	 * process it
	 * 
	 * @param wsdlLocation -
	 *            the location of the wsdl file
	 * @throws ParserConfigurationException
	 * @throws WSDLException
	 * @throws IOException
	 * @throws SAXException
	 */
	public WSDLParser(String wsdlLocation) throws ParserConfigurationException,
			WSDLException, IOException, SAXException {

		this.wsdlLocation = wsdlLocation;						
		
//		WSIFPluggableProviders.overrideDefaultProvider(
//				"http://schemas.xmlsoap.org/wsdl/soap/",
//				new WSIFDynamicProvider_ApacheAxis());

		if (!symbolTableMap.containsKey(wsdlLocation)) {
			SymbolTable symbolTable = new SymbolTable(new NoopFactory()
					.getBaseTypeMapping(), true, false, false);									
			symbolTable.populate(wsdlLocation);
			symbolTableMap.put(wsdlLocation, symbolTable);
			operationMap.put(wsdlLocation, determineOperations());
		}
		checkWSRF();
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
		symbolTableMap.remove(wsdlLocation);
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
		return wsdlLocation;
	}

	/**
	 * @return the Definition for this service
	 */
	public Definition getDefinition() {
		return getSymbolTable().getDefinition();
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
							SOAPAddress address = (SOAPAddress)obj;
							String endpoint=address.getLocationURI();
							result.add(endpoint);
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
	 * @param operationName the name of the operation the PortType is required for.
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
	 * @throws UnknownOperationException
	 *             if no operation matches the name
	 * @throws IOException
	 * 
	 */
	public List<TypeDescriptor> getOperationInputParameters(String operationName)
			throws UnknownOperationException, IOException {
		Operation operation = getOperation(operationName);
		List<TypeDescriptor> result = new ArrayList<TypeDescriptor>();
		if (operation == null) {
			throw new UnknownOperationException("operation called "
					+ operationName + " does not exist for this wsdl");
		}

		Parameters parameters = getSymbolTable().getOperationParameters(
				operation, "", new BindingEntry(getBinding(operationName)));

		for (Iterator iterator = parameters.list.iterator(); iterator.hasNext();) {
			Parameter param = (Parameter) iterator.next();
			if (param.getMode() == Parameter.IN) {
				TypeDescriptor typeDescriptor = processParameter(param);
				if (typeDescriptor instanceof ComplexTypeDescriptor
						&& getStyle().equals("document")) {
					// for document based, if operation requires no parameters
					// the param still exists (representing the operation) but
					// with empty inner elements
					if (((ComplexTypeDescriptor) typeDescriptor).getElements()
							.size() > 0) {
						result.add(typeDescriptor);
					}
				} else {
					result.add(typeDescriptor);
				}
			} else if (param.getMode() == Parameter.INOUT) {
				result.add(processParameter(param));
			}

		}

		cachedComplexTypes.clear();
		return result;
	}

	/**
	 * Returns a List of the TypeDescriptors representing the parameters for the
	 * outputs of the service
	 * 
	 * @param operationName
	 * @return List of TypeDescriptor
	 * @throws UnknownOperationException
	 *             if no operation matches the name
	 * @throws IOException
	 */
	public List<TypeDescriptor> getOperationOutputParameters(
			String operationName) throws UnknownOperationException, IOException {
		Operation operation = getOperation(operationName);
		List<TypeDescriptor> result = new ArrayList<TypeDescriptor>();
		if (operation == null) {
			throw new UnknownOperationException("operation called "
					+ operationName + " does not exist for this wsdl");
		}

		Parameters parameters = getSymbolTable().getOperationParameters(
				operation, "", new BindingEntry(getBinding(operationName)));

		for (Iterator iterator = parameters.list.iterator(); iterator.hasNext();) {
			Parameter param = (Parameter) iterator.next();
			if (param.getMode() == Parameter.OUT)
				result.add(processParameter(param));
			else if (param.getMode() == Parameter.INOUT) {
				result.add(processParameter(param));
			}

		}
		if (parameters.returnParam != null) {
			result.add(processParameter(parameters.returnParam));
		}

		cachedComplexTypes.clear();
		return result;
	}

	/**
	 * returns the namespace uri for the given operation name, throws
	 * UnknownOperationException if the operationName is not matched to one
	 * described by the WSDL.
	 * <p>
	 * Note that if you need the namespace for constructing the fully qualified
	 * element name of the operation, you might want to use
	 * {@link #getOperationQname(String)} instead.
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
				result = ((Part) getBindingOperation(operationName)
						.getOperation().getInput().getMessage()
						.getOrderedParts(null).get(0)).getElementName()
						.getNamespaceURI();
			} catch (Exception e) {
				// .... but this gets a good approximation if the above fails
				result = getDefinition().getTargetNamespace();
			}
		} else {
			BindingOperation binding = getBindingOperation(operationName);
			List extElements = binding.getBindingInput()
					.getExtensibilityElements();
			if (extElements != null && extElements.size() > 0) {
				SOAPBody body = (SOAPBody) extElements.get(0);
				result = body.getNamespaceURI();
			} else {
				extElements = binding.getBindingOutput()
						.getExtensibilityElements();
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

	public QName getOperationQname(String operationName) throws UnknownOperationException {
		if (getStyle().equals("document")) {
			try {
			// Get the QName of the first element of the input message
				return ((Part) getBindingOperation(operationName)
						.getOperation().getInput().getMessage()
						.getOrderedParts(null).get(0)).getElementName();
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
				result = operation.getDocumentationElement().getFirstChild()
						.getNodeValue();
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
	 * @throws UnknowOperationException
	 *             if no operation matches the name
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
		if (result == null)
			throw new UnknownOperationException("No operation named: "
					+ operationName + " exists");
		return result;
	}

	/**
	 * Check if this is a WSRF-resource property supporting binding.
	 * 
	 */
	protected void checkWSRF() {
		String actionURI;
		try {
			actionURI = getSOAPActionURI(GET_RESOURCE_PROPERTY_METHOD);
		} catch (UnknownOperationException e) {
			isWsrfService = false;
			return;
		}
		isWsrfService = GET_RESOURCE_PROPERTY_ACTION.equals(actionURI);
	}

	private SymbolTable getSymbolTable() {
		return symbolTableMap.get(getWSDLLocation());
	}

	private List<Operation> determineOperations() {
		List<Operation> result = new ArrayList<Operation>();

		Map<String, PortType> portToOperationMap = portTypeToOperationMap();
		Map<String, Binding> bindingToOperationMap = bindingToOperationMap();

		Map bindings = getSymbolTable().getDefinition().getBindings();
		for (Iterator iterator = bindings.values().iterator(); iterator
				.hasNext();) {
			Binding binding = (Binding) iterator.next();
			List extensibilityElementList = binding.getExtensibilityElements();
			for (Iterator k = extensibilityElementList.iterator(); k.hasNext();) {
				ExtensibilityElement ee = (ExtensibilityElement) k.next();
				if (ee instanceof SOAPBindingImpl) {
					SOAPBinding soapBinding = (SOAPBinding) ee;
					PortType portType = binding.getPortType();

					setStyleForBinding(soapBinding);

					for (Iterator opIterator = portType.getOperations()
							.iterator(); opIterator.hasNext();) {
						Operation op = (Operation) opIterator.next();
						result.add(op);
						portToOperationMap.put(op.getName(), portType);
						bindingToOperationMap.put(op.getName(), binding);
					}
				}
			}
		}

		Map imports = getSymbolTable().getDefinition().getImports();
		if (imports != null && imports.size() > 0) {
			result.addAll(processImports(imports));
		}

		return result;
	}

	private void setStyleForBinding(SOAPBinding soapBinding) {
		String style = soapBinding.getStyle();
		if (style==null) style="document"; //soap spec specifies to default to document if missing.
		styleMap.put(getWSDLLocation(), style);
	}

	private Map<String, PortType> portTypeToOperationMap() {
		Map<String, PortType> portToOperationMap = portTypeMap
				.get(getWSDLLocation());
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

		for (Iterator iterator = imports.values().iterator(); iterator
				.hasNext();) {
			List list = (List) iterator.next();
			for (Iterator importIterator = list.iterator(); importIterator
					.hasNext();) {
				Import imp = (Import) importIterator.next();
				Map bindings = imp.getDefinition().getBindings();
				for (Iterator bindingsIterator = bindings.values().iterator(); bindingsIterator
						.hasNext();) {
					Binding binding = (Binding) bindingsIterator.next();
					List extensibilityElementList = binding
							.getExtensibilityElements();
					for (Iterator k = extensibilityElementList.iterator(); k
							.hasNext();) {
						ExtensibilityElement ee = (ExtensibilityElement) k
								.next();
						if (ee instanceof SOAPBindingImpl) {
							SOAPBinding soapBinding = (SOAPBinding) ee;
							PortType portType = binding.getPortType();

							setStyleForBinding(soapBinding);

							for (Iterator opIterator = portType.getOperations()
									.iterator(); opIterator.hasNext();) {
								Operation op = (Operation) opIterator.next();
								result.add(op);
								portToOperationMap.put(op.getName(), portType);
								bindingToOperationMap
										.put(op.getName(), binding);
							}
						}
					}
				}

			}
		}

		return result;
	}

	private Map<String, Binding> bindingToOperationMap() {
		Map<String, Binding> bindingToOperationMap = bindingMap
				.get(getWSDLLocation());
		if (bindingToOperationMap == null) {
			bindingToOperationMap = new HashMap<String, Binding>();
			bindingMap.put(getWSDLLocation(), bindingToOperationMap);
		}
		return bindingToOperationMap;
	}

	private BindingOperation getBindingOperation(String operationName)
			throws UnknownOperationException {
		BindingOperation result = bindingOperations
				.get(operationName);
		if (result == null) {
			Binding binding = getBinding(operationName);
			if (binding != null) {
				List bindings = binding.getBindingOperations();
				for (Iterator iterator = bindings.iterator(); iterator.hasNext();) {
					BindingOperation bindingOperation = (BindingOperation) iterator
							.next();
					if (bindingOperation.getOperation().getName().equals(
							operationName)) {
						result = bindingOperation;
						bindingOperations.put(operationName, result);
						break;
					}
				}
			}
		}
		if (result == null)
			throw new UnknownOperationException(
					"Can't find binding operation for '" + operationName + "'");
		return result;
	}

	private TypeDescriptor processParameter(Parameter param) {
		TypeDescriptor typeDesc = constructType(param.getType());

		typeDesc.setName(param.getName());

		return typeDesc;
	}

	private TypeDescriptor constructType(TypeEntry type) {
		TypeDescriptor result = null;
		if (type instanceof CollectionType || type instanceof CollectionElement) {
			result = constructArrayType(type);
			result.setType(type.getRefType().getQName().getLocalPart());
		} else if (type instanceof DefinedType
				|| type instanceof DefinedElement) {
			if (type.getComponentType() == null) {
				if (type instanceof DefinedElement) {
					if (type.isBaseType()) {
						result = constructBaseType((DefinedElement) type);
					} else {
						result = constructComplexType((DefinedElement) type);
					}
				} else {
					if (type.isSimpleType()) {
						result = constructForSimpleType(type);
					}
					else {
						result = constructComplexType((DefinedType) type);
					}
				}
			} else {
				result = constructArrayType(type);				
			}
		} else {
			if (type.getQName().getLocalPart().equals("Map")) {
				// axis treats Map as a base type, Taverna doesn't.
				result = constructMapType(type);
			} else {
				result = constructBaseType(type);
			}
		}

		return result;
	}

	private TypeDescriptor constructForSimpleType(TypeEntry type) {
		Set nested = type.getNestedTypes(getSymbolTable(), true);
		
		TypeDescriptor result = constructType((TypeEntry)nested.toArray()[0]);
		result.setQname(type.getQName());
		result.setName(type.getQName().getLocalPart());
		return result;
	}
	
	private ArrayTypeDescriptor constructMapType(TypeEntry type) {
		ArrayTypeDescriptor result = new ArrayTypeDescriptor();
		TypeEntry mapItem = getSymbolTable().getType(type.getItemQName());
		if (mapItem == null) {
			mapItem = getSymbolTable().getType(
					new QName(type.getQName().getNamespaceURI(), "mapItem"));
		}

		result.setElementType(constructType(mapItem));

		result.setQname(type.getQName());
		result.setType(type.getQName().getLocalPart());

		return result;
	}

	private ComplexTypeDescriptor constructComplexType(DefinedElement type) {

		ComplexTypeDescriptor result = new ComplexTypeDescriptor();

		if (cachedComplexTypes.get(type.getQName().toString()) != null) {
			result = copyFromCache(type.getQName().toString());
		} else {
			// caching the type is not really to improve performance, but is
			// to handle types that contain elements that reference
			// itself or another parent. Without the caching, this could lead to
			// infinate
			// recursion.
			cachedComplexTypes.put(type.getQName().toString(), result);

			result.setType(type.getQName().getLocalPart());
			result.setQname(type.getQName());
			List containedElements = type.getRefType().getContainedElements();
			if (containedElements != null) {
				result.getElements().addAll(
						constructElements(containedElements));
			}
		}

		return result;
	}

	private ComplexTypeDescriptor constructComplexType(DefinedType type) {
		ComplexTypeDescriptor result = new ComplexTypeDescriptor();

		if (cachedComplexTypes.get(type.getQName().toString()) != null) {
			result = copyFromCache(type.getQName().toString());
		} else {
			result.setType(type.getQName().getLocalPart());
			cachedComplexTypes.put(type.getQName().toString(), result);
			List containedElements = type.getContainedElements();
			if (containedElements != null) {
				result.getElements().addAll(
						constructElements(containedElements));
			}
			result.setQname(type.getQName());
		}
		return result;
	}

	private List constructElements(List elements) {
		List result = new ArrayList();

		for (Iterator iterator = elements.iterator(); iterator.hasNext();) {
			ElementDecl el = (ElementDecl) iterator.next();
			TypeDescriptor elType = constructType(el.getType());
			elType.setOptional(el.getOptional() || el.getMinOccursIs0());
			elType.setUnbounded(el.getMaxOccursIsUnbounded());
			elType.setName(el.getQName().getLocalPart());
			elType.setQname(el.getQName());
			result.add(elType);
		}

		return result;
	}

	private ArrayTypeDescriptor constructArrayType(TypeEntry type) {
		ArrayTypeDescriptor result = new ArrayTypeDescriptor();
		result.setElementType(constructType(type.getRefType()));
		result.setType(type.getQName().getLocalPart());
		result.setQname(type.getQName());
		
		result.setWrapped(type.getItemQName()!=null);
		
		return result;
	}

	private BaseTypeDescriptor constructBaseType(TypeEntry type) {
		BaseTypeDescriptor result = new BaseTypeDescriptor();
		result.setType(type.getQName().getLocalPart());
		result.setQname(type.getQName());
		return result;
	}

	private BaseTypeDescriptor constructBaseType(DefinedElement type) {

		BaseTypeDescriptor result = null;
		if (type.getRefType() == null) {
			result = constructBaseType((TypeEntry) type);
		} else {
			result = new BaseTypeDescriptor();
			result.setType(type.getRefType().getQName().getLocalPart());
			result.setQname(type.getQName());
		}
		return result;
	}

	private ComplexTypeDescriptor copyFromCache(String key) {
		ComplexTypeDescriptor cached = cachedComplexTypes
				.get(key);
		ComplexTypeDescriptor result = new ComplexTypeDescriptor();
		result.setQname(cached.getQname());
		result.setElements(cached.getElements());
		result.setType(cached.getType());

		return result;
	}

}
