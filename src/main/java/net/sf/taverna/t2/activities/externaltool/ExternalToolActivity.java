/*******************************************************************************
 * Copyright (C) 2009 Hajo Nils Krabbenhšft, INB, University of Luebeck   
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

import java.io.IOException;
import java.rmi.ServerException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.taverna.t2.annotation.Annotated;
import net.sf.taverna.t2.annotation.annotationbeans.MimeType;
import net.sf.taverna.t2.reference.ExternalReferenceSPI;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.ReferenceServiceException;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.EditsRegistry;
import net.sf.taverna.t2.workflowmodel.OutputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.AbstractAsynchronousActivity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityInputPort;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivityCallback;

import org.apache.log4j.Logger;
import org.globus.ftp.exception.ClientException;

import de.uni_luebeck.inb.knowarc.gui.ProgressDisplayImpl;
import de.uni_luebeck.inb.knowarc.usecases.ScriptInput;
import de.uni_luebeck.inb.knowarc.usecases.ScriptInputUser;
import de.uni_luebeck.inb.knowarc.usecases.ScriptOutput;
import de.uni_luebeck.inb.knowarc.usecases.UseCaseDescription;
import de.uni_luebeck.inb.knowarc.usecases.UseCaseEnumeration;
import de.uni_luebeck.inb.knowarc.usecases.invocation.OnDemandDownload;
import de.uni_luebeck.inb.knowarc.usecases.invocation.UseCaseInvokation;

/**
 * This is the main class of the use case activity plugin. Here we store the
 * configuration and the description of a use case activity, configure the input
 * and output port and provide use case activity invocation
 * 
 * @author Hajo Nils Krabbenhšft
 */
public class ExternalToolActivity extends AbstractAsynchronousActivity<ExternalToolActivityConfigurationBean> {
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
		addMimeTypes(inputPort, mimeTypes);
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
			// re-parse the use case XML file
			List<UseCaseDescription> usecases = UseCaseEnumeration.enumerateXmlFile(new ProgressDisplayImpl(KnowARCConfigurationFactory.getConfiguration()),
					bean.getRepositoryUrl());
			// retrieve the UseCaseDescription for the given configuration bean
			// and store it into mydesc
			for (UseCaseDescription usecase : usecases) {
				if (!usecase.usecaseid.equalsIgnoreCase(bean.getExternaltoolid()))
					continue;
				mydesc = usecase;
				break;
			}

			// loop through all script inputs and add them as taverna activity
			// input ports
			for (Map.Entry<String, ScriptInput> cur : mydesc.inputs.entrySet()) {
				ScriptInputUser scriptInputUser = (ScriptInputUser) cur.getValue();
				// if the input port is a list, depth is 1 otherwise it is a
				// single element, therefore depth 0
				// if the input port is binary, we would like byte arrays,
				// otherwise we require strings
				addInputWithMime(cur.getKey(), scriptInputUser.list ? 1 : 0, cur.getValue().binary ? byte[].class : String.class, scriptInputUser.mime);

			}
			// loop through all script outputs and add them to taverna
			for (Map.Entry<String, ScriptOutput> cur : mydesc.outputs.entrySet()) {
				addOutputWithMime(cur.getKey(), 0, cur.getValue().mime);
			}

			// we always add STDOUT and STDERR output ports, even if these are
			// not given in the use case description. This makes sence since we
			// always invoke command line programs, thus always get STDOUT and
			// STDERR.
			addOutput("STDOUT", 0);
			addOutput("STDERR", 0);
		} catch (Exception e) {
			throw new ActivityConfigurationException("Couldn't create ExternalTool Activity", e);
		}
	}

	@Override
	public ExternalToolActivityConfigurationBean getConfiguration() {
		return configurationBean;
	}

	@Override
	public void executeAsynch(final Map<String, T2Reference> data, final AsynchronousActivityCallback callback) {

		callback.requestRun(new Runnable() {

			public void run() {
				ReferenceService referenceService = callback.getContext().getReferenceService();
				UseCaseInvokation invoke = null;
				try {
					int retries = 5;
					// retry the job submission 5 times. this is needed since
					// sadly not every grid job queue listed in the information
					// systems is still online. we only retry job submission,
					// not result fetching.
					while (true) {
						retries--;
						try {
							// we ask the UseCaseInvokation implementation to
							// choose a
							// matching invocation algorithm based on the plugin
							// configuration and use case description.
							invoke = UseCaseInvokation.createAppropriateInvokationFor(KnowARCConfigurationFactory.getConfiguration(), mydesc,
									new RetrieveLoginFromTaverna());

							// look at every use dynamic case input
							for (String cur : invoke.getInputs()) {
								// retrieve the value from taverna's reference
								// service
								Object value = referenceService.renderIdentifier(data.get(cur), invoke.getType(cur), callback.getContext());
								// and send it to the UseCaseInvokation
								invoke.setInput(cur, value);
							}

							// submit the use case to its invocation mechanism
							invoke.submit_generate_job();

							// do not retry, we succeeded :)
							break;
						} catch (Exception e) {
							// for the last retry, throw the exception
							if (retries <= 0)
								throw e;
						}
					}

					// retrieve the result.
					Map<String, Object> downloads = invoke.submit_wait_fetch_results();
					Map<String, T2Reference> result = new HashMap<String, T2Reference>();
					for (Map.Entry<String, Object> cur : downloads.entrySet()) {
						Object value = cur.getValue();

						// if the value is a reference, dereference it
						if (value instanceof OnDemandDownload)
							value = ((OnDemandDownload) value).download();

						// register the result value with taverna
						T2Reference reference = referenceService.register(value, 0, true, callback.getContext());
						// store the reference into the activity result
						// set
						result.put(cur.getKey(), reference);
					}
					callback.receiveResult(result, new int[0]);
				} catch (ServerException e) {
					callback.fail("Problem submitting job: ServerException: ", e);
				} catch (ReferenceServiceException e) {
					callback.fail("Problem with job input / output port: ", e);
				} catch (ClientException e) {
					callback.fail("Problem submitting job: ClientException: ", e);
				} catch (IOException e) {
					callback.fail("Problem submitting job: IOException: ", e);
				} catch (Exception e) {
					callback.fail(e.getMessage(), e);
				} finally {
					// clean up temporary invocation files, terminate network
					// connections
					if (invoke != null)
						invoke.Cleanup();
				}
			}

		});

	}

}
