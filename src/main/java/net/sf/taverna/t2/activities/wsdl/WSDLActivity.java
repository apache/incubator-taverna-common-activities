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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.wsdl.WSDLException;
import javax.xml.parsers.ParserConfigurationException;

import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.ReferenceServiceException;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.security.credentialmanager.CredentialManager;
import net.sf.taverna.t2.workflowmodel.OutputPort;
import net.sf.taverna.t2.workflowmodel.health.RemoteHealthChecker;
import net.sf.taverna.t2.workflowmodel.processor.activity.AbstractAsynchronousActivity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityOutputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivityCallback;
import net.sf.taverna.wsdl.parser.TypeDescriptor;
import net.sf.taverna.wsdl.parser.UnknownOperationException;
import net.sf.taverna.wsdl.parser.WSDLParser;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * An asynchronous Activity that is concerned with WSDL based web-services.
 * <p>
 * The activity is configured according to the WSDL location and the operation.<br>
 * The ports are defined dynamically according to the WSDL specification, and in
 * addition an output<br>
 * port <em>attachmentList</em> is added to represent any attachements that are
 * returned by the webservice.
 * </p>
 *
 * @author Stuart Owen
 * @author Stian Soiland-Reyes
 */
public class WSDLActivity extends
		AbstractAsynchronousActivity<JsonNode> implements
		InputPortTypeDescriptorActivity, OutputPortTypeDescriptorActivity {

	public static final String URI = "http://ns.taverna.org.uk/2010/activity/wsdl";

	public static final String ENDPOINT_REFERENCE = "EndpointReference";
	private JsonNode configurationBean;
	private WSDLParser parser;
//	private Map<String, Integer> outputDepth = new HashMap<String, Integer>();
	private boolean isWsrfService = false;
//	private String endpointReferenceInputPortName;
	private CredentialManager credentialManager;

	public WSDLActivity(CredentialManager credentialManager) {
		this.credentialManager = credentialManager;
	}

	public boolean isWsrfService() {
		return isWsrfService;
	}

	private static Logger logger = Logger.getLogger(WSDLActivity.class);

	/**
	 * Configures the activity according to the information passed by the
	 * configuration bean.<br>
	 * During this process the WSDL is parsed to determine the input and output
	 * ports.
	 *
	 * @param bean
	 *            the {@link WSDLActivityConfigurationBean} configuration bean
	 */
	@Override
	public void configure(JsonNode bean)
			throws ActivityConfigurationException {
		if (this.configurationBean != null) {
//			throw new IllegalStateException(
//					"Reconfiguring WSDL activity not yet implemented");
			this.configurationBean = bean;
		}
		else{
			this.configurationBean = bean;
			try {
				parseWSDL();
//				configurePorts();
			} catch (Exception ex) {
				throw new ActivityConfigurationException(
						"Unable to parse the WSDL " + bean.get("operation").get("wsdl").textValue(), ex);
			}
		}
	}

	@Override
	public JsonNode getConfiguration() {
		return configurationBean;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @seenet.sf.taverna.t2.activities.wsdl.InputPortTypeDescriptorActivity#
	 * getTypeDescriptorForInputPort(java.lang.String)
	 */
	public TypeDescriptor getTypeDescriptorForInputPort(String portName)
			throws UnknownOperationException, IOException {
		List<TypeDescriptor> inputDescriptors = parser
				.getOperationInputParameters(configurationBean.get("operation").get("name").textValue());
		TypeDescriptor result = null;
		for (TypeDescriptor descriptor : inputDescriptors) {
			if (descriptor.getName().equals(portName)) {
				result = descriptor;
				break;
			}
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @seenet.sf.taverna.t2.activities.wsdl.InputPortTypeDescriptorActivity#
	 * getTypeDescriptorsForInputPorts()
	 */
	public Map<String, TypeDescriptor> getTypeDescriptorsForInputPorts()
			throws UnknownOperationException, IOException {
		Map<String, TypeDescriptor> descriptors = new HashMap<String, TypeDescriptor>();
		List<TypeDescriptor> inputDescriptors = parser
				.getOperationInputParameters(configurationBean.get("operation").get("name").textValue());
		for (TypeDescriptor descriptor : inputDescriptors) {
			descriptors.put(descriptor.getName(), descriptor);
		}
		return descriptors;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @seenet.sf.taverna.t2.activities.wsdl.OutputPortTypeDescriptorActivity#
	 * getTypeDescriptorForOutputPort(java.lang.String)
	 */
	public TypeDescriptor getTypeDescriptorForOutputPort(String portName)
			throws UnknownOperationException, IOException {
		TypeDescriptor result = null;
		List<TypeDescriptor> outputDescriptors = parser
				.getOperationOutputParameters(configurationBean.get("operation").get("name").textValue());
		for (TypeDescriptor descriptor : outputDescriptors) {
			if (descriptor.getName().equals(portName)) {
				result = descriptor;
				break;
			}
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @seenet.sf.taverna.t2.activities.wsdl.OutputPortTypeDescriptorActivity#
	 * getTypeDescriptorsForOutputPorts()
	 */
	public Map<String, TypeDescriptor> getTypeDescriptorsForOutputPorts()
			throws UnknownOperationException, IOException {
		Map<String, TypeDescriptor> descriptors = new HashMap<String, TypeDescriptor>();
		List<TypeDescriptor> inputDescriptors = parser
				.getOperationOutputParameters(configurationBean.get("operation").get("name").textValue());
		for (TypeDescriptor descriptor : inputDescriptors) {
			descriptors.put(descriptor.getName(), descriptor);
		}
		return descriptors;
	}

	private void parseWSDL() throws ParserConfigurationException,
			WSDLException, IOException, SAXException, UnknownOperationException {
		String wsdlLocation = configurationBean.get("operation").get("wsdl").textValue();
		URLConnection connection = null;
		try {
			URL wsdlURL = new URL(wsdlLocation);
			connection = wsdlURL.openConnection();
			connection.setConnectTimeout(RemoteHealthChecker.getTimeoutInSeconds() * 1000);
			connection.connect();
		} catch (MalformedURLException e) {
			throw new IOException("Malformed URL", e);
		} catch (SocketTimeoutException e) {
			throw new IOException("Timeout", e);
		} catch (IOException e) {
			throw e;
		} finally {
			if ((connection != null) && (connection.getInputStream() != null)) {
				connection.getInputStream().close();
			}
		}
		parser = new WSDLParser(wsdlLocation);
		isWsrfService = parser.isWsrfService();
	}

//	private void configurePorts() throws UnknownOperationException, IOException {
//		List<TypeDescriptor> inputDescriptors = parser
//				.getOperationInputParameters(configurationBean.get("operation").get("name").textValue());
//		List<TypeDescriptor> outputDescriptors = parser
//				.getOperationOutputParameters(configurationBean.get("operation").get("name").textValue());
//		for (TypeDescriptor descriptor : inputDescriptors) {
//			addInput(descriptor.getName(), descriptor.getDepth(), true, null,
//					String.class);
//		}
//		isWsrfService = parser.isWsrfService();
//		if (isWsrfService) {
//			// Make sure the port name is unique
//			endpointReferenceInputPortName = ENDPOINT_REFERENCE;
//			int counter = 0;
//			while (Tools.getActivityInputPort(this,
//					endpointReferenceInputPortName) != null) {
//				endpointReferenceInputPortName = ENDPOINT_REFERENCE + counter++;
//			}
//			addInput(endpointReferenceInputPortName, 0, true, null,
//					String.class);
//		}
//
//		for (TypeDescriptor descriptor : outputDescriptors) {
//			addOutput(descriptor.getName(), descriptor.getDepth());
//			outputDepth.put(descriptor.getName(), Integer.valueOf(descriptor
//					.getDepth()));
//		}
//
//		// add output for attachment list
//		addOutput("attachmentList", 1);
//		outputDepth.put("attachmentList", Integer.valueOf(1));
//	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void executeAsynch(final Map<String, T2Reference> data,
			final AsynchronousActivityCallback callback) {

		callback.requestRun(new Runnable() {

			public void run() {

				ReferenceService referenceService = callback.getContext()
						.getReferenceService();

				Map<String, T2Reference> outputData = new HashMap<String, T2Reference>();
				Map<String, Object> invokerInputMap = new HashMap<String, Object>();
				String endpointReferenceInputPortName = getEndpointReferenceInputPortName();

				try {
					String endpointReference = null;
					for (String key : data.keySet()) {
						Object renderIdentifier = referenceService
								.renderIdentifier(data.get(key), String.class,
										callback.getContext());
						if (isWsrfService()
								&& key.equals(endpointReferenceInputPortName)) {
							endpointReference = (String) renderIdentifier;
						} else {
							invokerInputMap.put(key, renderIdentifier);
						}
					}
					List<String> outputNames = new ArrayList<String>();
					for (OutputPort port : getOutputPorts()) {
						outputNames.add(port.getName());
					}

					T2WSDLSOAPInvoker invoker = new T2WSDLSOAPInvoker(parser,
							configurationBean.get("operation").get("name").textValue(), outputNames,
							endpointReference, credentialManager);

					Map<String, Object> invokerOutputMap = invoker.invoke(
							invokerInputMap, configurationBean);

					for (String outputName : invokerOutputMap.keySet()) {
						Object value = invokerOutputMap.get(outputName);

						if (value != null) {
							Integer depth = getOutputPortDepth(outputName);
							if (depth != null) {
								outputData.put(outputName, referenceService
										.register(value, depth, true, callback
												.getContext()));
							} else {
								logger.info("Depth not recorded for output:"
												+ outputName);
								// TODO what should the depth be in this case?
								outputData.put(outputName, referenceService
										.register(value, 0, true, callback
												.getContext()));
							}
						}
					}
					callback.receiveResult(outputData, new int[0]);
				} catch (ReferenceServiceException e) {
					logger.error("Error finding the input data for "
							+ getConfiguration().get("operation"), e);
					callback.fail("Unable to find input data", e);
					return;
				} catch (Exception e) {
					logger.error("Error invoking WSDL service "
							+ getConfiguration().get("operation"), e);
					callback.fail(
							"An error occurred invoking the WSDL service", e);
					return;
				}

			}

		});

	}

	private Integer getOutputPortDepth(String portName) {
		for (ActivityOutputPort outputPort : getOutputPorts()) {
			if (outputPort.getName().equals(portName)) {
				return outputPort.getDepth();
			}
		}
		return null;
	}

	private String getEndpointReferenceInputPortName() {
		String endpointReferenceInputPortName = null;
		if (parser.isWsrfService()) {
			Set<String> inputPorts = new HashSet<>();
			try {
				List<TypeDescriptor> inputDescriptors = parser.getOperationInputParameters(configurationBean
						.get("operation").get("name").textValue());
				for (TypeDescriptor descriptor : inputDescriptors) {
					inputPorts.add(descriptor.getName());
				}
			} catch (UnknownOperationException | IOException e) {
			}
			// Make sure the port name is unique
			endpointReferenceInputPortName = WSDLActivity.ENDPOINT_REFERENCE;
			int counter = 0;
			while (inputPorts.contains(endpointReferenceInputPortName)) {
				endpointReferenceInputPortName = WSDLActivity.ENDPOINT_REFERENCE + counter++;
			}
		}
		return endpointReferenceInputPortName;
	}

}
