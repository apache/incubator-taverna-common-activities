/*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.apache.taverna.activities.xpath;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.reference.ErrorDocumentService;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.workflowmodel.processor.activity.AbstractAsynchronousActivity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivityCallback;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.InvalidXPathException;
import org.dom4j.Node;
import org.dom4j.XPath;
import org.dom4j.XPathException;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Enhanced XPath activity.
 *
 * @author Sergejs Aleksejevs
 */
public class XPathActivity extends AbstractAsynchronousActivity<JsonNode> {

	public static final String URI = "http://ns.taverna.org.uk/2010/activity/xpath";

	// These ports are default ones (and only ones - XPath activity will not have dynamic ports)
	public static final String IN_XML = "xml_text";
	public static final String OUT_TEXT = "nodelist";
	public static final String OUT_XML = "nodelistAsXML";

	private static final String SINGLE_VALUE_TEXT = "firstNode";
	private static final String SINGLE_VALUE_XML = "firstNodeAsXML";
	
	// Configuration bean for this activity - essentially defines a particular instance
	// of the activity through the values of its parameters
	private JsonNode json;

	@Override
	public JsonNode getConfiguration() {
		return this.json;
	}

	@Override
	public void configure(JsonNode json) throws ActivityConfigurationException {
		// Check configBean is valid
		if (!XPathUtils.isValid(json)) {
			throw new ActivityConfigurationException("Invalid configuration of XPath activity...");
			// TODO - check this
		}

		// Store for getConfiguration()
		this.json = json;
	}

	protected void configurePorts() {
		// ---- REMOVE OLD PORTS ----
	  
		// In case we are being reconfigured - remove existing ports first to avoid duplicates
		removeInputs();
		removeOutputs();

		// ---- CREATE NEW INPUTS AND OUTPUTS ----
		
		// all ports in this activity are static, so no dependency on the values in config bean
		
		// single input port: the input XML text will be treated as String for now
		addInput(IN_XML, 0, true, null, String.class);

		addOutput(SINGLE_VALUE_TEXT, 0);
		addOutput(SINGLE_VALUE_XML, 0);
		addOutput(OUT_TEXT, 1);
		addOutput(OUT_XML, 1);
	}

	/**
	 * This method executes pre-configured instance of XPath activity.
	 */
	@Override
	public void executeAsynch(final Map<String, T2Reference> inputs,
			final AsynchronousActivityCallback callback) {
		// Don't execute service directly now, request to be run asynchronously
		callback.requestRun(new Runnable() {
			@Override
			@SuppressWarnings("unchecked")
			public void run() {

				InvocationContext context = callback.getContext();
				ReferenceService referenceService = context.getReferenceService();

				// ---- RESOLVE INPUT ----

				String xmlInput = (String) referenceService.renderIdentifier(inputs.get(IN_XML),
						String.class, context);

				// ---- DO THE ACTUAL SERVICE INVOCATION ----

				List<Node> matchingNodes = new ArrayList<Node>();

				// only attempt to execute XPath expression if there is some input data
				if (xmlInput != null && xmlInput.length() > 0) {
					// XPath configuration is taken from the config bean
					try {
						XPath expr = DocumentHelper.createXPath(json.get("xpathExpression").textValue());
						Map<String, String> xpathNamespaceMap = new HashMap<>();
						for (JsonNode namespaceMapping : json.get("xpathNamespaceMap")) {
							xpathNamespaceMap.put(namespaceMapping.get("prefix").textValue(),
									namespaceMapping.get("uri").textValue());
						}
						expr.setNamespaceURIs(xpathNamespaceMap);
						Document doc = DocumentHelper.parseText(xmlInput);
						matchingNodes = expr.selectNodes(doc);
					} catch (InvalidXPathException e) {
						callback.fail("Incorrect XPath Expression -- XPath processing library "
								+ "reported the following error: " + e.getMessage(), e);

						// make sure we don't call callback.receiveResult later
						return;
					} catch (DocumentException e) {
						callback.fail("XML document was not valid -- XPath processing library "
								+ "reported the following error: " + e.getMessage(), e);

						// make sure we don't call callback.receiveResult later
						return;
					} catch (XPathException e) {
						callback.fail(
								"Unexpected error has occurred while executing the XPath expression. "
										+ "-- XPath processing library reported the following error:\n"
										+ e.getMessage(), e);

						// make sure we don't call callback.receiveResult later
						return;
					}
				}
				
				// --- PREPARE OUTPUTS ---

				List<String> outNodesText = new ArrayList<String>();
				List<String> outNodesXML = new ArrayList<String>();
				Object textValue = null;
				Object xmlValue = null;

				for (Object o : matchingNodes) {
					if (o instanceof Node) {
						Node n = (Node) o;
						if (n.getStringValue() != null
								&& n.getStringValue().length() > 0) {
							outNodesText.add(n.getStringValue());
							if (textValue == null)
								textValue = n.getStringValue();
						}
						outNodesXML.add(n.asXML());
						if (xmlValue == null)
							xmlValue = n.asXML();
					} else {
						outNodesText.add(o.toString());
						if (textValue == null)
							textValue = o.toString();
					}
				}

				// ---- REGISTER OUTPUTS ----

				Map<String, T2Reference> outputs = new HashMap<String, T2Reference>();
				if (textValue == null) {
					ErrorDocumentService errorDocService = referenceService
							.getErrorDocumentService();
					textValue = errorDocService.registerError(
							"No value produced", 0, callback.getContext());
				}

				if (xmlValue == null) {
					ErrorDocumentService errorDocService = referenceService
							.getErrorDocumentService();
					xmlValue = errorDocService.registerError(
							"No value produced", 0, callback.getContext());
				}

				T2Reference firstNodeAsText = referenceService.register(
						textValue, 0, true, context);
				outputs.put(SINGLE_VALUE_TEXT, firstNodeAsText);

				T2Reference firstNodeAsXml = referenceService.register(
						xmlValue, 0, true, context);
				outputs.put(SINGLE_VALUE_XML, firstNodeAsXml);

				T2Reference outNodesAsText = referenceService.register(
						outNodesText, 1, true, context);
				outputs.put(OUT_TEXT, outNodesAsText);

				T2Reference outNodesAsXML = referenceService.register(
						outNodesXML, 1, true, context);
				outputs.put(OUT_XML, outNodesAsXML);

				// return map of output data, with empty index array as this is
				// the only and final result (this index parameter is used if
				// pipelining output)
				callback.receiveResult(outputs, new int[0]);
			}
		});
	}

}
