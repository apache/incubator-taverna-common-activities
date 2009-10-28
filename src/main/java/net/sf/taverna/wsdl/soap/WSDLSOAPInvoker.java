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
 ****************************************************************
 * Source code information
 * -----------------------
 * Filename           $RCSfile: WSDLSOAPInvoker.java,v $
 * Revision           $Revision: 1.7 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2008/08/28 19:39:28 $
 *               by   $Author: stain $
 * Created on 07-Apr-2006
 *****************************************************************/
package net.sf.taverna.wsdl.soap;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.activation.DataHandler;
import javax.wsdl.WSDLException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.rpc.ServiceException;
import javax.xml.soap.SOAPException;
import org.apache.axis.message.SOAPHeaderElement;

import net.sf.taverna.wsdl.parser.UnknownOperationException;
import net.sf.taverna.wsdl.parser.WSDLParser;

import org.apache.axis.AxisFault;
import org.apache.axis.EngineConfiguration;
import org.apache.axis.attachments.AttachmentPart;
import org.apache.axis.client.Call;
import org.apache.axis.message.SOAPBodyElement;
import org.apache.axis.message.SOAPEnvelope;
import org.apache.axis.transport.http.HTTPTransport;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

/**
 * Invoke SOAP based webservices
 * 
 * @author Stuart Owen
 * 
 */
@SuppressWarnings("unchecked")
public class WSDLSOAPInvoker {

	private static final String ATTACHMENT_LIST = "attachmentList";
	private BodyBuilderFactory bodyBuilderFactory = BodyBuilderFactory.instance();
	private WSDLParser parser;
	private String operationName;
	private List<String> outputNames;
	
	private static Logger logger = Logger.getLogger(WSDLSOAPInvoker.class);

	public WSDLSOAPInvoker(WSDLParser parser, String operationName,
			List<String> outputNames) {
		this.parser = parser;
		this.operationName = operationName;
		this.outputNames = outputNames;
	}
	
	protected String getOperationName() {
		return operationName;
	}
	
	protected WSDLParser getParser() {
		return parser;
	}

	protected List<String> getOutputNames() {
		return outputNames;
	}
	

	/**
	 * Invokes the webservice with the supplied input Map, and returns a Map
	 * containing the outputs, mapped against their output names.
	 * 
	 * @param inputMap
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> invoke(Map inputMap) throws Exception {
		return invoke(inputMap, (EngineConfiguration)null);
	}

	/**
	 * Invokes the webservice with the supplied input Map and axis engine configuration, 
	 * and returns a Map containing the outputs, mapped against their output names.
	 */
	public Map<String, Object> invoke(Map inputMap, EngineConfiguration config)
			throws Exception {
		
		Call call = getCall(config);
		return invoke(inputMap, call);
	}
	
	/**
	 * Invokes the webservice with the supplied input Map and preconfigured axis call, 
	 * and returns a Map containing the outputs, mapped against their output names.
	 */
	public Map<String, Object> invoke(Map inputMap, Call call)
			throws Exception {
			
		call.setTimeout(getTimeout());

		SOAPEnvelope requestEnv = makeRequestEnvelope(inputMap);
		logger.info("Invoking service with SOAP envelope:\n"+requestEnv);
		
		SOAPEnvelope responseEnv = invokeCall(call, requestEnv);
		
		logger.info("Received SOAP response:\n"+responseEnv);

		Map<String, Object> result;
		if (responseEnv == null) {
			if (outputNames.size() == 1
					&& outputNames.get(0).equals(ATTACHMENT_LIST)) {
				// Could be axis 2 service with no output (TAV-617)
				result = new HashMap<String, Object>();
			} else {
				throw new IllegalStateException(
						"Missing expected outputs from service");
			}
		} else {
			List response = responseEnv.getBodyElements();
			logger.info("SOAP response was:" + response);
			SOAPResponseParser parser = SOAPResponseParserFactory
					.instance()
					.create(
							response,
							getUse(),
							getStyle(),
							this.parser
									.getOperationOutputParameters(operationName));
			result = parser.parse(response);
		}

		result.put(ATTACHMENT_LIST, extractAttachments(call));

		return result;
	}

	protected SOAPEnvelope makeRequestEnvelope(Map inputMap)
			throws UnknownOperationException, IOException, WSDLException,
			ParserConfigurationException, SOAPException, SAXException {
	
		SOAPEnvelope requestEnv = new SOAPEnvelope();
		for (SOAPHeaderElement headerElement : makeSoapHeaders()) {
			requestEnv.addHeader(headerElement);
		}
		requestEnv.addBodyElement(makeSoapBody(inputMap));
		return requestEnv;
	}

	protected List<SOAPHeaderElement> makeSoapHeaders() {
		return Collections.emptyList();
	}

	protected SOAPBodyElement makeSoapBody(Map inputMap)
			throws UnknownOperationException, IOException, WSDLException,
			ParserConfigurationException, SOAPException, SAXException {
		BodyBuilder builder = bodyBuilderFactory.create(parser,
				operationName,
				parser.getOperationInputParameters(operationName));
		return builder.build(inputMap);
	}

	protected SOAPEnvelope invokeCall(Call call, SOAPEnvelope requestEnv) throws AxisFault {
		return call.invoke(requestEnv);
	}

	/**
	 * Reads the property taverna.wsdl.timeout, default to 5 minutes if missing.
	 * 
	 * @return
	 */
	protected Integer getTimeout() {
		int result = 300000;
		String minutesStr = System.getProperty("taverna.wsdl.timeout");

		if (minutesStr == null) {
			// using default of 5 minutes
			return result;
		}
		try {
			int minutes = Integer.parseInt(minutesStr.trim());
			result = minutes * 1000 * 60;
		} catch (NumberFormatException e) {
			e.printStackTrace();
			return result;
		}
		return result;
	}

	protected String getStyle() {
		return parser.getStyle();
	}

	protected String getUse() throws UnknownOperationException {
		return parser.getUse(operationName);
	}

	/**
	 * Returns an axis based Call, initialised for the operation that needs to
	 * be invoked
	 * 
	 * @return
	 * @throws ServiceException
	 * @throws UnknownOperationException
	 * @throws MalformedURLException 
	 * @throws WSDLException
	 * @throws WSIFException
	 */
	protected Call getCall(EngineConfiguration config)  throws ServiceException, UnknownOperationException, MalformedURLException {
		
		org.apache.axis.client.Service service;
		if (config==null) {
			service = new org.apache.axis.client.Service();
		}
		else {
			service = new org.apache.axis.client.Service(config);
		}
		
		Call call = new Call(service);
		
		call.setTransport(new HTTPTransport());
		call.setTargetEndpointAddress(parser.getOperationEndpointLocations(operationName).get(0));
		//result.setPortName(parser.getPortType(operationName).getQName());
		//result.setOperation(operationName);
		
		String use = parser.getUse(operationName);
		call.setUseSOAPAction(true);
		call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR,
				Boolean.FALSE);
		call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS,
				Boolean.FALSE);
		call
				.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
		
		if (parser.getSOAPActionURI(operationName)!=null) {
			call.setSOAPActionURI(parser.getSOAPActionURI(operationName));
		}
		
		if (use.equalsIgnoreCase("literal")) {
			call.setEncodingStyle(null);
		}
		return call;
	}
	

	/**
	 * Exctracts any attachments that result from invoking the service, and
	 * returns them as a List wrapped within a DataThing
	 * 
	 * @param axisCall
	 * @return
	 * @throws SOAPException
	 * @throws IOException
	 */
	protected List extractAttachments(Call axisCall)
			throws SOAPException, IOException {
		List attachmentList = new ArrayList();
		if (axisCall.getResponseMessage() != null
				&& axisCall.getResponseMessage().getAttachments() != null) {
			for (Iterator i = axisCall.getResponseMessage().getAttachments(); i
					.hasNext();) {
				AttachmentPart ap = (AttachmentPart) i.next();
				DataHandler dh = ap.getDataHandler();
				BufferedInputStream bis = new BufferedInputStream(dh
						.getInputStream());
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				int c;
				while ((c = bis.read()) != -1) {
					bos.write(c);
				}
				bis.close();
				bos.close();
				String mimeType = dh.getContentType();
				if (mimeType.matches(".*image.*")
						|| mimeType.matches(".*octet.*")
						|| mimeType.matches(".*audio.*")
						|| mimeType.matches(".*application/zip.*")) {
					attachmentList.add(bos.toByteArray());
				} else {
					attachmentList.add(new String(bos.toByteArray()));
				}
			}
		}

		return attachmentList;
	}

	
}
