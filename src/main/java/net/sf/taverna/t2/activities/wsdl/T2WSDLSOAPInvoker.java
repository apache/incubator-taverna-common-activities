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

package net.sf.taverna.t2.activities.wsdl;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.StringReader;
import java.net.URI;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;

import net.sf.taverna.t2.security.credentialmanager.CMException;
import net.sf.taverna.t2.security.credentialmanager.CredentialManager;
import net.sf.taverna.t2.security.credentialmanager.UsernamePassword;
import net.sf.taverna.wsdl.parser.WSDLParser;
import net.sf.taverna.wsdl.soap.WSDLSOAPInvoker;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * Invokes SOAP based Web Services from T2.
 * 
 * Subclasses WSDLSOAPInvoker used for invoking Web Services from Taverna 1.x
 * and extends it to provide support for invoking secure Web services.
 * 
 * @author Stuart Owen
 * @author Alex Nenadic
 * @author Stian Soiland-Reyes
 * 
 */
public class T2WSDLSOAPInvoker extends WSDLSOAPInvoker {

	private static final String REFERENCE_PROPERTIES = "ReferenceProperties";
	private static final String ENDPOINT_REFERENCE = "EndpointReference";
	private static Logger logger = Logger.getLogger(T2WSDLSOAPInvoker.class);
        
        private static final String WSA200403NS = "http://schemas.xmlsoap.org/ws/2004/03/addressing";

	private String wsrfEndpointReference = null;

	private CredentialManager credentialManager;

	public T2WSDLSOAPInvoker(WSDLParser parser, String operationName,
			List<String> outputNames, CredentialManager credentialManager) {
		super(parser, operationName, outputNames);
		this.credentialManager = credentialManager;
	}

	public T2WSDLSOAPInvoker(WSDLParser parser, String operationName,
			List<String> outputNames, String wsrfEndpointReference, CredentialManager credentialManager) {
		this(parser, operationName, outputNames, credentialManager);
                this.wsrfEndpointReference = wsrfEndpointReference;
                
//                if (wsrfEndpointReference != null && 
//                    parser.isWSRFOperation(operationName) == null) {
//                    logger.warn("not a WSRF operation: " + operationName);
//                } else {
//                    this.wsrfEndpointReference = wsrfEndpointReference;
//                }
	}

//	protected void configureSecurity(Call call,
//			WSDLActivityConfigurationBean bean) throws Exception {
//
//		// If security settings require WS-Security - configure the axis call
//		// with appropriate properties
//		String securityProfile = bean.getSecurityProfile();
//		if (securityProfile
//				.equals(SecurityProfiles.WSSECURITY_USERNAMETOKEN_PLAINTEXTPASSWORD)
//				|| securityProfile
//						.equals(SecurityProfiles.WSSECURITY_USERNAMETOKEN_DIGESTPASSWORD)
//				|| securityProfile
//						.equals(SecurityProfiles.WSSECURITY_TIMESTAMP_USERNAMETOKEN_PLAINTEXTPASSWORD)
//				|| securityProfile
//						.equals(SecurityProfiles.WSSECURITY_TIMESTAMP_USERNAMETOKEN_DIGESTPASSWORD)) {
//			
//			UsernamePassword usernamePassword = getUsernameAndPasswordForService(bean, false);
//			call.setProperty(Call.USERNAME_PROPERTY, usernamePassword.getUsername());
//			call.setProperty(Call.PASSWORD_PROPERTY, usernamePassword.getPasswordAsString());
//			usernamePassword.resetPassword();
//		} else if (securityProfile.equals(SecurityProfiles.HTTP_BASIC_AUTHN)){
//			// Basic HTTP AuthN - set HTTP headers
//			// pathrecursion allowed
//			UsernamePassword usernamePassword = getUsernameAndPasswordForService(bean, true);
//			MessageContext context = call.getMessageContext();
//			context.setUsername(usernamePassword.getUsername());
//			context.setPassword(usernamePassword.getPasswordAsString());
//			usernamePassword.resetPassword();
//		} else {
//			logger.error("Unknown security profile " + securityProfile);
//		}
//	}


	/**
	 * Get username and password from Credential Manager or ask user to supply
	 * one. Username is the first element of the returned array, and the
	 * password is the second.
	 */
	protected UsernamePassword getUsernameAndPasswordForService(
			JsonNode configurationBean, boolean usePathRecursion) throws CMException {

		// Try to get username and password for this service from Credential
		// Manager (which should pop up UI if needed)
		URI serviceUri = URI.create(configurationBean.get("operation").get("wsdl").textValue());
		UsernamePassword username_password = credentialManager.getUsernameAndPasswordForService(serviceUri, usePathRecursion, null);
		if (username_password == null) {
			throw new CMException("No username/password provided for service " + serviceUri);
		}
		return username_password;
	}
        
        @Override
        protected void addSoapHeader(SOAPEnvelope envelope) throws SOAPException
        {
            if (wsrfEndpointReference != null) {
                
		// Extract elements
		// Add WSA-stuff
		// Add elements

		Document wsrfDoc;
		try {
			wsrfDoc = parseWsrfEndpointReference(wsrfEndpointReference);
		} catch (Exception e) {
			logger.warn("Could not parse endpoint reference, ignoring:\n"
					+ wsrfEndpointReference, e);
			return;
		}

		Element wsrfRoot = wsrfDoc.getDocumentElement();
                
		Element endpointRefElem = null;
                if (!wsrfRoot.getNamespaceURI().equals(WSA200403NS)
				|| !wsrfRoot.getLocalName().equals(ENDPOINT_REFERENCE)) {
                    // Only look for child if the parent is not an EPR
                    NodeList nodes = wsrfRoot.getChildNodes();
                    for (int i = 0, n = nodes.getLength(); i < n; i++) {
                        Node node = nodes.item(i);
                        if (Node.ELEMENT_NODE == node.getNodeType() &&
                            node.getLocalName().equals(ENDPOINT_REFERENCE) &&
                            node.getNamespaceURI().equals(WSA200403NS)) {
                            // Support wrapped endpoint reference for backward compatibility
                            // and convenience (T2-677)
                            endpointRefElem = (Element)node;
                            break;
                        }
                    }
		}
                
                if (endpointRefElem == null) {
                    logger.warn("Unexpected element name for endpoint reference, but inserting anyway: " + wsrfRoot.getTagName());
                    endpointRefElem = wsrfRoot;
                }


                Element refPropsElem = null;
                NodeList nodes = endpointRefElem.getChildNodes();
                for (int i = 0, n = nodes.getLength(); i < n; i++) {
                    Node node = nodes.item(i);
                    if (Node.ELEMENT_NODE == node.getNodeType() &&
                        node.getLocalName().equals(REFERENCE_PROPERTIES) &&
                        node.getNamespaceURI().equals(WSA200403NS)) {
                        refPropsElem = (Element)node;
                        break;
                    }
                }
		if (refPropsElem == null) {
			logger.warn("Could not find " + REFERENCE_PROPERTIES);
			return;
		}

                SOAPHeader header = envelope.getHeader();
                if (header == null) {
                    header = envelope.addHeader();
                }
                
		NodeList refProps = refPropsElem.getChildNodes();

                for (int i = 0, n = refProps.getLength(); i < n; i++) {
                    Node node = refProps.item(i);
                    
                    if (Node.ELEMENT_NODE == node.getNodeType()) {
                        SOAPElement soapElement = SOAPFactory.newInstance().createElement((Element)node);
                        header.addChildElement(soapElement);

                        Iterator<SOAPHeaderElement> headers = header.examineAllHeaderElements();
                        while (headers.hasNext()) {
                            SOAPHeaderElement headerElement = headers.next();
                            if (headerElement.getElementQName().equals(soapElement.getElementQName())) {
                                headerElement.setMustUnderstand(false);
                                headerElement.setActor(null);
                            }
                        }
                    }
                }
            }
        }

	protected Document parseWsrfEndpointReference(
			String wsrfEndpointReference) throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                factory.setNamespaceAware(true);
                DocumentBuilder builder = factory.newDocumentBuilder();
		return builder.parse(new InputSource(new StringReader(wsrfEndpointReference)));
	}

    @Override
        public Map<String, Object> invoke(SOAPMessage message)
                                throws Exception {
            return super.invoke(message);
        }
        
	public Map<String, Object> invoke(Map<String, Object> inputMap,
			JsonNode configurationBean) throws Exception {

//		String securityProfile = bean.getSecurityProfile();
//		EngineConfiguration wssEngineConfiguration = null;
//		if (securityProfile != null) {
//			// If security settings require WS-Security and not just e.g. Basic HTTP
//			// AuthN - configure the axis engine from the appropriate config strings
//			if (securityProfile
//					.equals(SecurityProfiles.WSSECURITY_USERNAMETOKEN_PLAINTEXTPASSWORD)) {
//				wssEngineConfiguration = new XMLStringProvider(
//						SecurityProfiles.WSSECURITY_USERNAMETOKEN_PLAINTEXTPASSWORD_CONFIG);
//			} else if (securityProfile
//					.equals(SecurityProfiles.WSSECURITY_USERNAMETOKEN_DIGESTPASSWORD)) {
//				wssEngineConfiguration = new XMLStringProvider(
//						SecurityProfiles.WSSECURITY_USERNAMETOKEN_DIGESTPASSWORD_CONFIG);
//			} else if (securityProfile
//					.equals(SecurityProfiles.WSSECURITY_TIMESTAMP_USERNAMETOKEN_PLAINTEXTPASSWORD)) {
//				wssEngineConfiguration = new XMLStringProvider(
//						SecurityProfiles.WSSECURITY_TIMESTAMP_USERNAMETOKEN_PLAINTETPASSWORD_CONFIG);
//			} else if (securityProfile
//					.equals(SecurityProfiles.WSSECURITY_TIMESTAMP_USERNAMETOKEN_DIGESTPASSWORD)) {
//				wssEngineConfiguration = new XMLStringProvider(
//						SecurityProfiles.WSSECURITY_TIMESTAMP_USERNAMETOKEN_DIGESTPASSWORD_CONFIG);
//			}
//		}
//
//		// This does not work
////		ClassUtils.setClassLoader("net.sf.taverna.t2.activities.wsdl.security.TavernaAxisCustomSSLSocketFactory",TavernaAxisCustomSSLSocketFactory.class.getClassLoader());
//		
//		// Setting Axis property only works when we also set the Thread's classloader as below 
//		// (we do it from the net.sf.taverna.t2.workflowmodel.processor.dispatch.layers.Invoke.requestRun())
////		Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
//		if (AxisProperties.getProperty("axis.socketSecureFactory")== null || !AxisProperties.getProperty("axis.socketSecureFactory").equals("net.sf.taverna.t2.activities.wsdl.security.TavernaAxisCustomSSLSocketFactory")){
//			AxisProperties.setProperty("axis.socketSecureFactory", "net.sf.taverna.t2.activities.wsdl.security.TavernaAxisCustomSSLSocketFactory");
//			logger.info("Setting axis.socketSecureFactory property to " + AxisProperties.getProperty("axis.socketSecureFactory"));
//		}
//        
//		// This also does not work
//		//AxisProperties.setClassDefault(SecureSocketFactory.class, "net.sf.taverna.t2.activities.wsdl.security.TavernaAxisCustomSSLSocketFactory");
//        
//		//Call call = super.getCall(wssEngineConfiguration);
//		
//		// Now that we have an axis Call object, configure any additional
//		// security properties on it (or its message context or its Transport
//		// handler),
//		// such as WS-Security UsernameToken or HTTP Basic AuthN
//		if (securityProfile != null) {
//			configureSecurity(call, bean);
//		}

		return invoke(inputMap);
	}

}
