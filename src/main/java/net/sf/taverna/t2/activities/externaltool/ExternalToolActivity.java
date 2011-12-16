/*******************************************************************************
 * Copyright (C) 2009 Hajo Nils Krabbenhoeft, INB, University of Luebeck   
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

package net.sf.taverna.t2.activities.externaltool;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.taverna.t2.activities.externaltool.manager.InvocationGroup;
import net.sf.taverna.t2.activities.externaltool.manager.InvocationGroupManager;
import net.sf.taverna.t2.activities.externaltool.manager.InvocationMechanism;
import net.sf.taverna.t2.annotation.Annotated;
import net.sf.taverna.t2.annotation.annotationbeans.MimeType;
import net.sf.taverna.t2.reference.ExternalReferenceSPI;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.reference.WorkflowRunIdEntity;
import net.sf.taverna.t2.spi.SPIRegistry;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.EditsRegistry;
import net.sf.taverna.t2.workflowmodel.OutputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.AbstractAsynchronousActivity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityInputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivityCallback;

import org.apache.log4j.Logger;

import de.uni_luebeck.inb.knowarc.usecases.ScriptInput;
import de.uni_luebeck.inb.knowarc.usecases.ScriptInputUser;
import de.uni_luebeck.inb.knowarc.usecases.ScriptOutput;
import de.uni_luebeck.inb.knowarc.usecases.UseCaseDescription;
import de.uni_luebeck.inb.knowarc.usecases.invocation.InvocationException;
import de.uni_luebeck.inb.knowarc.usecases.invocation.UseCaseInvocation;
import de.uni_luebeck.inb.knowarc.usecases.invocation.ssh.SshReference;

/**
 * This is the main class of the use case activity plugin. Here we store the
 * configuration and the description of a use case activity, configure the input
 * and output port and provide use case activity invocation
 * 
 * @author Hajo Nils Krabbenhoeft
 */
public class ExternalToolActivity extends AbstractAsynchronousActivity<ExternalToolActivityConfigurationBean> {
	
	private static final String STDERR = "STDERR";

	private static final String STDOUT = "STDOUT";

	private static final String STDIN = "STDIN";

	private static Logger logger = Logger.getLogger(ExternalToolActivity.class);
	
	private ExternalToolActivityConfigurationBean configurationBean;
	private UseCaseDescription mydesc;

	/**
	 * Add the given MIME types to the given input/output port.
	 * 
	 * @param annotated
	 *            The port to which to add the MIME types.
	 * @param mimeTypes
	 *            A list of Strings specifying the MIME types to add.
	 */
	private void addMimeTypes(Annotated<?> annotated, List<String> mimeTypes) {
		for (String mimeType : mimeTypes) {
			MimeType mimeTypeAnnotation = new MimeType();
			mimeTypeAnnotation.setText(mimeType);
			try {
				EditsRegistry.getEdits().getAddAnnotationChainEdit(annotated, mimeTypeAnnotation).doEdit();
			} catch (EditException e) {
				Logger.getLogger(ExternalToolActivity.class).error(e);
			}
		}
	}

	/**
	 * Create a new input port with the given name, depth, element class and
	 * MIME types.
	 * 
	 * @param portName
	 *            Name of the new port
	 * @param portDepth
	 *            Depth of the new port
	 * @param translatedElementClass
	 *            Which class of elements would this port like?
	 * @param mimeTypes
	 *            Accepted mime types for this port
	 */
	private void addInputWithMime(String portName, int portDepth, Class<?> translatedElementClass, List<String> mimeTypes) {
		List<Class<? extends ExternalReferenceSPI>> handledReferenceSchemes = Collections.emptyList();
		ActivityInputPort inputPort = EditsRegistry.getEdits().createActivityInputPort(portName, portDepth, true, handledReferenceSchemes,
				translatedElementClass);
		inputPorts.add(inputPort);
		if (mimeTypes != null) {
			addMimeTypes(inputPort, mimeTypes);
		}
	}

	/**
	 * Create a new output port with the given MIME types
	 * 
	 * @param portName
	 *            Name of the new port
	 * @param portDepth
	 *            Depth of the new port
	 * @param mimeTypes
	 *            Accepted mime types for this port
	 */
	private void addOutputWithMime(String portName, int portDepth, List<String> mimeTypes) {
		OutputPort outputPort = EditsRegistry.getEdits().createActivityOutputPort(portName, portDepth, portDepth);
		outputPorts.add(outputPort);
		addMimeTypes(outputPort, mimeTypes);
	}

	@Override
	public void configure(ExternalToolActivityConfigurationBean bean) throws ActivityConfigurationException {
		this.configurationBean = bean;

		try {
			mydesc = bean.getUseCaseDescription();		
			
			inputPorts.clear();
			outputPorts.clear();
			
			if (mydesc != null) {
			
			// loop through all script inputs and add them as taverna activity
			// input ports
			for (Map.Entry<String, ScriptInput> cur : mydesc.getInputs().entrySet()) {
				ScriptInputUser scriptInputUser = (ScriptInputUser) cur.getValue();
				// if the input port is a list, depth is 1 otherwise it is a
				// single element, therefore depth 0
				// if the input port is binary, we would like byte arrays,
				// otherwise we require strings
				addInputWithMime(cur.getKey(), scriptInputUser.isList() ? 1 : 0, cur.getValue().isBinary() ? byte[].class : String.class, scriptInputUser.getMime());

			}
			// loop through all script outputs and add them to taverna
			for (Map.Entry<String, ScriptOutput> cur : mydesc.getOutputs().entrySet()) {
				addOutputWithMime(cur.getKey(), 0, cur.getValue().getMime());
			}
			}

			if (mydesc.isIncludeStdIn()) {
				addInputWithMime(STDIN, 0, byte[].class, null);
			}
			if (mydesc.isIncludeStdOut()) {
				addOutput(STDOUT, 0);
			}
			if (mydesc.isIncludeStdErr()) {
				addOutput(STDERR, 0);
			}
		} catch (Exception e) {
			throw new ActivityConfigurationException("Couldn't create ExternalTool Activity", e);
		}
	}

	@Override
	public ExternalToolActivityConfigurationBean getConfiguration() {
		if (configurationBean != null) {
			InvocationGroup invocationGroup = configurationBean.getInvocationGroup();
			if (invocationGroup == null) {
				if (configurationBean.getMechanism() != null) {
					configurationBean.convertMechanismToDetails();
				}
			} else {
				if (invocationGroup.getMechanism() != null) {
					invocationGroup.convertMechanismToDetails();
				}
			}
		}
		return configurationBean;
	}
	
	public ExternalToolActivityConfigurationBean getConfigurationNoConversion() {
		return configurationBean;
	}
	
	public InvocationMechanism recreateMechanism() {
		if (configurationBean.getInvocationGroup() != null) {
			if (configurationBean.getInvocationGroup().getMechanism() == null) {
				configurationBean.getInvocationGroup().convertDetailsToMechanism();			
			}
			return configurationBean.getInvocationGroup().getMechanism();
		} else {
			if (configurationBean.getMechanism() == null) {
				configurationBean.convertDetailsToMechanism();
			}
			return configurationBean.getMechanism();
		}
	}

	@Override
	public void executeAsynch(final Map<String, T2Reference> data, final AsynchronousActivityCallback callback) {

		callback.requestRun(new Runnable() {

			public void run() {
				ReferenceService referenceService = callback.getContext().getReferenceService();
				UseCaseInvocation invoke = null;
				
				/**
				 * Note that retrying needs to be either done via Taverna's retry mechanism or as part of the specific invocation
				 */
				try {

					invoke = getInvocation(recreateMechanism(),
							configurationBean.getUseCaseDescription(), data, referenceService);
					if (invoke == null) {
						logger.error("Invoke is null");
						callback.fail("No invocation mechanism found");
					}
					String runId = callback.getContext()
							.getEntities(WorkflowRunIdEntity.class).get(0)
							.getWorkflowRunId();
					logger.info("Run id is " + runId);
					invoke.rememberRun(runId);

					invoke.setContext(callback.getContext());

					// look at every use dynamic case input
					for (String cur : invoke.getInputs()) {
						if (!cur.equals(STDIN)) {
							invoke.setInput(cur, referenceService,
									data.get(cur));
						}
					}

					if (mydesc.isIncludeStdIn() && (data.get(STDIN) != null)) {
						invoke.setStdIn(referenceService, data.get(STDIN));
					}

					// submit the use case to its invocation mechanism
					invoke.submit_generate_job(referenceService);

					// retrieve the result.
					Map<String, Object> downloads = invoke
							.submit_wait_fetch_results(referenceService);
					Map<String, T2Reference> result = new HashMap<String, T2Reference>();
					for (Map.Entry<String, Object> cur : downloads.entrySet()) {
						Object value = cur.getValue();

						// register the result value with taverna
						T2Reference reference = referenceService.register(
								value, 0, true, callback.getContext());
						
						// store the reference into the activity result
						// set
						result.put(cur.getKey(), reference);
					}
					callback.receiveResult(result, new int[0]);
				} catch (InvocationException e) {
					callback.fail(e.getMessage(), e);
				}
			}

		});

	}
	
	private static SPIRegistry<InvocationCreator> invocationCreatorRegistry = new SPIRegistry(InvocationCreator.class);
	
	private UseCaseInvocation getInvocation(InvocationMechanism mechanism, UseCaseDescription description, Map<String, T2Reference> data, ReferenceService referenceService) {
		UseCaseInvocation result = null;
		InvocationCreator creator = null;
		for (InvocationCreator c : invocationCreatorRegistry.getInstances()) {
			if (c.canHandle(mechanism.getType())) {
				creator = c;
				break;
			}
		}
		if (creator != null) {
			result = creator.convert(mechanism, description, data, referenceService);
		}
		return result;
	}

}
