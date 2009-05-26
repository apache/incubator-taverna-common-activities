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
/*
 * Copyright (C) 2003 The University of Manchester 
 *
 * Modifications to the initial code base are copyright of their
 * respective authors, or their employers as appropriate.  Authorship
 * of the modifications may be determined from the ChangeLog placed at
 * the end of this file.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA.
 *
 */

package net.sf.taverna.t2.activities.wsdl;

import net.sf.taverna.raven.repository.ArtifactNotFoundException;
import net.sf.taverna.raven.repository.ArtifactStateException;
import net.sf.taverna.t2.activities.wsdl.wss4j.T2WSDoAllSender;
import net.sf.taverna.t2.security.credentialmanager.*;
import net.sf.taverna.t2.security.agents.*;
import net.sf.taverna.t2.security.requests.*;

import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.rpc.ServiceException;

import net.sf.taverna.wsdl.parser.UnknownOperationException;
import net.sf.taverna.wsdl.parser.WSDLParser;
import net.sf.taverna.wsdl.soap.WSDLSOAPInvoker;

import org.apache.axis.EngineConfiguration;
import org.apache.axis.Handler;
import org.apache.axis.client.Call;
import org.apache.axis.message.SOAPHeaderElement;
import org.apache.log4j.Logger;
import org.apache.ws.axis.security.handler.WSDoAllHandler;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.jdom.output.DOMOutputter;

/**
 * Invokes SOAP based Web Services from T2.
 * 
 * Subclasses WSDLSOAPInvoker used for invoking Web Services from Taverna 1.x
 * and overrides the getCall(EngineConfiguration config) method to enable
 * invocation of secure Web Services using the T2 Security Agents.
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
	private static final Namespace wsaNS = Namespace.getNamespace("wsa", "http://schemas.xmlsoap.org/ws/2004/03/addressing");

	private String wsrfEndpointReference = null;

	public T2WSDLSOAPInvoker(WSDLParser parser, String operationName,
			List<String> outputNames) {
		super(parser, operationName, outputNames);
	}

	public T2WSDLSOAPInvoker(WSDLParser parser, String operationName,
			List<String> outputNames, String wsrfEndpointReference) {
		this(parser, operationName, outputNames);
		this.wsrfEndpointReference = wsrfEndpointReference;
	}

	@SuppressWarnings("unchecked")
	protected void addEndpointReferenceHeaders(
			List<SOAPHeaderElement> soapHeaders) {
		// Extract elements
		// Add WSA-stuff
		// Add elements

		Document wsrfDoc;
		try {
			wsrfDoc = parseWsrfEndpointReference(wsrfEndpointReference);
		} catch (JDOMException e) {
			logger.warn("Could not parse endpoint reference, ignoring:\n"
					+ wsrfEndpointReference, e);
			return;
		} catch (IOException e) {
			logger.error("Could not read endpoint reference, ignoring:\n"
					+ wsrfEndpointReference, e);
			return;
		}
		
		//Element endpointRefElem = wsrfDoc.getRootElement();
		Element endpointRefElem = wsrfDoc.getRootElement().getChild(ENDPOINT_REFERENCE, wsaNS);
		if (endpointRefElem == null) {
			logger.warn("Could not find " + ENDPOINT_REFERENCE);
			return;
		}
		Element refPropsElem = endpointRefElem.getChild(REFERENCE_PROPERTIES, wsaNS);
		if (refPropsElem == null) {
			logger.warn("Could not find " + REFERENCE_PROPERTIES);
			return;
		}
		
		List<Element> refProps = refPropsElem.getChildren();
		// Make a copy of the list as it would be modified by
		// prop.detach();
		for (Element prop : new ArrayList<Element>(refProps)) {
			DOMOutputter domOutputter = new DOMOutputter();
			SOAPHeaderElement soapElem;
			prop.detach();
			try {
				org.w3c.dom.Document domDoc = domOutputter.output(new Document(prop));
				soapElem = new SOAPHeaderElement(domDoc.getDocumentElement());			
			} catch (JDOMException e) {
				logger.warn("Could not translate wsrf element to DOM:\n" + prop, e);
				continue;
			}
			soapElem.setMustUnderstand(false);
			soapElem.setActor(null);
			soapHeaders.add(soapElem);
		}
		
		

//		soapHeaders.add(new SOAPHeaderElement((Element) wsrfDoc
	//			.getDocumentElement()));
	}

	protected void configureSecurity(Call call) {
			
		// Call's USERNAME_PROPERTY is here simply used to pass the credential's
		// alias to fetch it from the Keystore. As alias value we use wsdlLocation so that the
		// credential is tied to a particular service. Once Security Agent picks up the
		// alias, it will set the USERNAME_PROPERTY to null or to a proper username.
		// Note that WSS4J's handler WSDoAllSender expects (which is invoked
		// before our T2DoAllSender takes over) the USERNAME_PROPERTY to be set 
		// to whatever non-empty value for almost all security operations 
		// (even for signing, except for encryption), otherwise it raises an exception.

		 call.setProperty(Call.USERNAME_PROPERTY,
		 getParser().getWSDLLocation());

		 // Get the appropriate security agent
		 /*CredentialManager credManager;
		 try {
		 credManager = CredentialManager.getInstance();
		 SecurityAgentManager saManager =
		 credManager.getSecurityAgentManager();
		 WSSecurityRequest wsSecReq = new
		 WSSecurityRequest(getParser().getWSDLLocation(), null);
		
		 WSSecurityAgent sa = (WSSecurityAgent)
		 saManager.getSecurityAgent((SecurityRequest) wsSecReq);
		 call.setProperty("security_agent", sa);
					
		 } catch (CMException e) {
		 // TODO Auto-generated catch block
		 e.printStackTrace();
		 } catch (CMNotInitialisedException e) {
		 // TODO Auto-generated catch block
		 e.printStackTrace();
		 }*/
	}

	/**
	 * Returns an Axis-based Call, initialised for the operation that needs to
	 * be invoked.
	 * 
	 * @param config
	 *            - Axis engine configuration containing settings for the
	 *            transport and WSS4J handlers that will add WS-Security headers
	 *            to the SOAP envelope in order to make secure WSs invocation.
	 * @return Call object initialised for the operation that needs to be
	 *         invoked.
	 * @throws ServiceException
	 * @throws UnknownOperationException
	 * @throws MalformedURLException
	 */
	@Override
	protected Call getCall(EngineConfiguration config) throws ServiceException,
			UnknownOperationException, MalformedURLException {
		
		
		//logger.info("Trying to get classloader for T2WSDoAllSender... " );//+ T2WSDoAllSender.class.getClassLoader());	
		//logger.info("Classloader = "+T2WSDLSOAPInvoker.class.getClassLoader());
		//logger.info("Trying to get Classloader for Handler... ");
		//logger.info("Classloader = "+Handler.class.getClassLoader());
		//logger.info("Trying to get Classloader for WSDoAllHandler... ");
		//logger.info("Classloader = "+WSDoAllHandler.class.getClassLoader());
		
		/*
		logger.info("Passing the classloaders for T2WSDoAllSender and wss4j to axis...");
		ClassLoader t2WSSenderLoader=null, wss4jLoader=null, xmlsecLoader=null;
		try {
			t2WSSenderLoader = ((net.sf.taverna.raven.repository.impl.LocalArtifactClassLoader)T2WSDoAllSender.class.getClassLoader()).getRepository().getLoader(new net.sf.taverna.raven.repository.BasicArtifact("net.sf.taverna.t2", "wsdl-activity", "0.3"), null);
			wss4jLoader = ((net.sf.taverna.raven.repository.impl.LocalArtifactClassLoader)T2WSDoAllSender.class.getClassLoader()).getRepository().getLoader(new net.sf.taverna.raven.repository.BasicArtifact("org.apache.ws.security", "wss4j", "1.5.4-taverna"), null);
			//xmlsecLoader = ((net.sf.taverna.raven.repository.impl.LocalArtifactClassLoader)T2WSDoAllSender.class.getClassLoader()).getRepository().getLoader(new net.sf.taverna.raven.repository.BasicArtifact("org.apache.santuario", "xmlsec", "1.4.0"), null);

		} catch (ArtifactNotFoundException e) {
			logger.info("Artifact not found");
			e.printStackTrace();
		} catch (ArtifactStateException e) {
			logger.info("Artifact state exception");
			e.printStackTrace();
		}
	
		org.apache.axis.utils.ClassUtils.setClassLoader("net.sf.taverna.t2.activities.wsdl.wss4j.T2WSDoAllSender",t2WSSenderLoader);
		org.apache.axis.utils.ClassUtils.setClassLoader("org.apache.ws.axis.security.handler.WSDoAllHandler",wss4jLoader);
		*/
		
		//org.apache.axis.utils.ClassUtils.setDefaultClassLoader(T2WSDoAllSender.class.getClassLoader());
		//org.apache.axis.utils.ClassUtils.setClassLoader("org.apache.axis.Handler",T2WSDLSOAPInvoker.class.getClassLoader());
		//org.apache.axis.utils.ClassUtils.setClassLoader("net.sf.taverna.t2.activities.wsdl.wss4j.T2WSDoAllSender",T2WSDLSOAPInvoker.class.getClassLoader());

		//		
//		org.apache.axis.utils.ClassUtils.setClassLoader(org.apache.ws.security.transform.STRTransform.class.getName(), (net.sf.taverna.raven.repository.impl.LocalArtifactClassLoader)STRTransform.class.getClassLoader()).getRepository().getLoader(new net.sf.taverna.raven.repository.BasicArtifact("org.apache.ws.security", "wss4j", "1.5.4"));
//		org.apache.axis.utils.ClassUtils.setClassLoader(org.apache.xml.security.transforms.Transform.class.getName(), org.apache.xml.security.transforms.Transform.class.getClassLoader());
		//org.apache.axis.utils.ClassUtils.setClassLoader(WSDoAllHandler.class.getName(), WSDoAllHandler.class.getClassLoader());

		Call call = super.getCall(config);
		
		if (config != null) {
			configureSecurity(call);
		}
		return call;
	}
	
	@Override
	protected List<SOAPHeaderElement> makeSoapHeaders() {
		List<SOAPHeaderElement> soapHeaders = new ArrayList<SOAPHeaderElement>(
				super.makeSoapHeaders());
		if (wsrfEndpointReference != null && getParser().isWsrfService()) {
			addEndpointReferenceHeaders(soapHeaders);
		}
		return soapHeaders;
	}

	protected org.jdom.Document parseWsrfEndpointReference(
			String wsrfEndpointReference) throws JDOMException, IOException {
		SAXBuilder builder = new SAXBuilder();
		return builder.build(new StringReader(wsrfEndpointReference));
	}

}
