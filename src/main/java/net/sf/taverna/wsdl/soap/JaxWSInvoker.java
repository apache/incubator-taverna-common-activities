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
package net.sf.taverna.wsdl.soap;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.bind.DatatypeConverter;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.transform.dom.DOMSource;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import javax.xml.ws.soap.SOAPBinding;
import net.sf.taverna.wsdl.parser.WSDLParser;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.message.WSSecHeader;
import org.apache.ws.security.message.WSSecUsernameToken;
import org.w3c.dom.Document;

/**
 * JAX-WS based invoker. The invoker uses Apache WSS4j security library.
 * 
 * @author Dmitry Repchevsky
 */

public class JaxWSInvoker {

    private Dispatch<SOAPMessage> dispatch;

    private String username;
    private String password;
    private WSSTokenProfile token;
        
    public JaxWSInvoker(WSDLParser parser, String portName, String operationName) {
        
        QName port_qname = null;
        for (QName service_name : parser.getServices()) {
            loop:
            for (String port_name : parser.getPorts(service_name)) { 
                if (portName == null) {
                    for (String operation_name : parser.getOperations(port_name)) {
                        if (operation_name.equals(operationName)) {
                            port_qname = new QName(port_name);
                            break loop;
                        }
                    }
                } else if (portName.equals(port_name)) {
                    port_qname = new QName(portName);
                    break;
                }
            }
            
            if (port_qname != null) {
                // found the port in this service
                Service service = Service.create(service_name);
                String endpoint = parser.getOperationEndpointLocation(port_qname.getLocalPart());
                service.addPort(port_qname, SOAPBinding.SOAP11HTTP_BINDING, endpoint); // TODO: SOAP version
                dispatch = service.createDispatch(port_qname, SOAPMessage.class, javax.xml.ws.Service.Mode.MESSAGE);
                
                List<Handler> chain = new ArrayList<Handler>();//Arrays.asList(new InvokerHandler());
                chain.add(new WSSHandler());

                dispatch.getBinding().setHandlerChain(chain);

                break;
            }
        }

        // assert(dispatch != null)
        
        if (operationName != null) {
            try {
                String soapAction = parser.getSOAPActionURI(operationName);
                if (soapAction != null) {
                    Map<String, Object> rc = dispatch.getRequestContext();
                    rc.put(BindingProvider.SOAPACTION_USE_PROPERTY, Boolean.TRUE);
                    rc.put(BindingProvider.SOAPACTION_URI_PROPERTY, soapAction);
                }
            }
            catch(Exception ex) {}
        }
    }
    
    /**
     * Sets user credentials. If there is no WSS token profile is defined HTTP security is used
     * 
     * @param username
     * @param password 
     */
    public void setCredentials(String username, String password) {
        this.username = username;
        this.password = password;
    }

    /**
     * Specifies a token profile for WSS security
     * 
     * @param token WSS security token to be used
     */
    public void setWSSSecurity(WSSTokenProfile token) {
        this.token = token;
    }
    
    /**
     * Sets up an http timeout.
     * 
     * @param timeout 
     */
    public void setTimeout(int timeout) {
        Map<String, Object> rc = dispatch.getRequestContext();
        
        // these parameters are SUN METRO JAX-WS implementation specific
        rc.put("com.sun.xml.internal.ws.request.timeout", timeout);
        rc.put("com.sun.xml.internal.ws.connect.timeout", timeout);
        
//        rc.put(BindingProviderProperties.REQUEST_TIMEOUT, timeout);
//        rc.put(BindingProviderProperties.CONNECT_TIMEOUT, timeout);
    }
    
    public SOAPMessage call(SOAPMessage message) throws Exception {
        
        Map<String, Object> rc = dispatch.getRequestContext();
        
        // if credentials are set, but no WS-Security token is defined
        // use Basic HTTP Authentication
        if (username != null && username.length() > 0 && 
            password != null && password.length() > 0 &&
            token == null) {
            rc.put(BindingProvider.USERNAME_PROPERTY, username);
            rc.put(BindingProvider.PASSWORD_PROPERTY, password);
        } else {
            if (rc.containsKey(BindingProvider.USERNAME_PROPERTY)) {
                rc.remove(BindingProvider.USERNAME_PROPERTY);
            }
            if (rc.containsKey(BindingProvider.PASSWORD_PROPERTY)) {
                rc.remove(BindingProvider.PASSWORD_PROPERTY);
            }
        }
                
        return dispatch.invoke(message);
    }
    
    class WSSHandler implements SOAPHandler<SOAPMessageContext> {

        @Override
        public Set getHeaders() {
            return Collections.EMPTY_SET;
        }

        @Override
        public void close(MessageContext context) {

        }

        @Override
        public boolean handleMessage(SOAPMessageContext context) { 
            
            if (Boolean.TRUE == context.get(SOAPMessageContext.MESSAGE_OUTBOUND_PROPERTY)) {
                if (username != null && username.length() > 0 && 
                    password != null && password.length() > 0 &&
                    token != null) {
                    
                    SOAPMessage message = context.getMessage();
                    SOAPPart part = message.getSOAPPart();
                    
                    try {
                        switch(token) {
                            case UsernameToken: {
                                WSSecUsernameToken builder = new WSSecUsernameToken();
                                builder.setPasswordType(WSConstants.PASSWORD_TEXT);
                                builder.setUserInfo(username, password);
                                
                                WSSecHeader secHeader = new WSSecHeader();
                                secHeader.insertSecurityHeader(part);
                                Document signed = builder.build(part, secHeader);
                                part.setContent(new DOMSource(signed));
                            }
                            case PasswordDigest: {
                                WSSecUsernameToken builder = new WSSecUsernameToken();
                                builder.setPasswordType(WSConstants.PASSWORD_TEXT);
                                MessageDigest sha = MessageDigest.getInstance("MD5");
                                sha.reset();
                                sha.update(password.getBytes());
                                String passwdDigest = DatatypeConverter.printBase64Binary(sha.digest());
                                builder.setUserInfo(username, passwdDigest);
                                WSSecHeader secHeader = new WSSecHeader();
                                secHeader.insertSecurityHeader(part);
                                Document signed = builder.build(part, secHeader);
                                part.setContent(new DOMSource(signed));
                            }
                        }
                    }
//                    catch (WSSecurityException ex) {}
                    catch (SOAPException ex) {}
                    catch (NoSuchAlgorithmException ex) {}
                }
            }
            
            return true;
        }

        @Override
        public boolean handleFault(SOAPMessageContext context) {
            return true;
        }
    }
}
