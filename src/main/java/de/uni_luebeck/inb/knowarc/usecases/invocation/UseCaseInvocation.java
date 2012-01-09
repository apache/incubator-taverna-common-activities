/* Part of the KnowARC Janitor Use-case processor for taverna
 *  written 2007-2010 by Hajo Nils Krabbenhoeft and Steffen Moeller
 *  University of Luebeck, Institute for Neuro- and Bioinformatics
 *  University of Luebeck, Institute for Dermatolgy
 *
 *  This package is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation; either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This package is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this package; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301 USA
 */

package de.uni_luebeck.inb.knowarc.usecases.invocation;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;

import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.reference.ExternalReferenceSPI;
import net.sf.taverna.t2.reference.Identified;
import net.sf.taverna.t2.reference.IdentifiedList;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.ReferenceServiceException;
import net.sf.taverna.t2.reference.ReferenceSet;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.reference.impl.external.object.InlineByteArrayReferenceBuilder;
import net.sf.taverna.t2.reference.impl.external.object.InlineStringReferenceBuilder;
import de.uni_luebeck.inb.knowarc.usecases.ScriptInput;
import de.uni_luebeck.inb.knowarc.usecases.ScriptInputStatic;
import de.uni_luebeck.inb.knowarc.usecases.ScriptInputUser;
import de.uni_luebeck.inb.knowarc.usecases.UseCaseDescription;

/**
 * An abstraction of various forms to bring job using the software that is
 * referenced as a use case towards their execution.
 * 
 * @author Hajo Nils Krabbenhoeft with some contribution by
 * @author Steffen Moeller
 */
public abstract class UseCaseInvocation {
	
	private String runId;
	
	
	protected static String getActualOsCommand(String osCommand, String pathToOriginal,
			String targetName, String pathTarget) {
				String actualOsCommand = osCommand;
				actualOsCommand = actualOsCommand.replace("%%PATH_TO_ORIGINAL%%", pathToOriginal);
				actualOsCommand = actualOsCommand.replace("%%TARGET_NAME%%", targetName);
				actualOsCommand = actualOsCommand.replace("%%PATH_TO_TARGET%%", pathTarget);
				return actualOsCommand;
			}

	protected UseCaseDescription usecase;
	protected final HashMap<String, String> tags = new HashMap<String, String>();
	protected int nTempFiles = 0;
	private static int submissionID = 0;
	protected static InlineByteArrayReferenceBuilder inlineByteArrayReferenceBuilder = new InlineByteArrayReferenceBuilder();
	protected static InlineStringReferenceBuilder inlineStringReferenceBuilder = new InlineStringReferenceBuilder();
	private InvocationContext invocationContext;
	private boolean retrieveData;
	
	/*
	 * get the class of the data we expect for a given input
	 */
	@SuppressWarnings("unchecked")
	public Class getType(String inputName) {
		if (!usecase.getInputs().containsKey(inputName))
			return null;
		ScriptInputUser input = (ScriptInputUser) usecase.getInputs().get(inputName);
		if (input.isList()) {
			if (input.isBinary())
				return List.class;
			else
				return List.class;
		} else {
			if (input.isBinary())
				return byte[].class;
			else
				return String.class;
		}
	}

	/*
	 * get a list of all the input port names
	 */
	public Set<String> getInputs() {
		return usecase.getInputs().keySet();
	}


	/*
	 * get a id, incremented with each job. thus, this should be thread-wide
	 * unique
	 */
	public synchronized int getSubmissionID() {
		return submissionID++;
	}

	/*
	 * set the data for the input port with given name
	 */
	@SuppressWarnings("unchecked")
	public void setInput(String inputName, ReferenceService referenceService, T2Reference t2Reference) throws InvocationException {
		if (t2Reference == null) {
			throw new InvocationException("No input specified for " + inputName);
		}
		ScriptInputUser input = (ScriptInputUser) usecase.getInputs().get(inputName);
		if (input.isList()) {
			IdentifiedList<T2Reference> listOfReferences = (IdentifiedList<T2Reference>) referenceService
					.getListService().getList(t2Reference);

			if (!input.isConcatenate()) {
				// this is a list input (not concatenated)
				// so write every element to its own temporary file
				// and create a filelist file

				// we need to write the list elements to temporary files
				ScriptInputUser listElementTemp = new ScriptInputUser();
				listElementTemp.setBinary(input.isBinary());
				listElementTemp.setTempFile(true);

				String lineEndChar = "\n";
				if (!input.isFile() && !input.isTempFile()) {
					lineEndChar = " ";
				}

				String listFileContent = "";
				String filenamesFileContent = "";
				// create a list of all temp file names
				for (T2Reference cur : listOfReferences) {
					String tmp = setOneInput(referenceService, cur,
							listElementTemp);
					listFileContent += tmp + lineEndChar;
					int ind = tmp.lastIndexOf('/');
					if (ind == -1) {
						ind = tmp.lastIndexOf('\\');
					}
					if (ind != -1) {
						tmp = tmp.substring(ind + 1);
					}
					filenamesFileContent += tmp + lineEndChar;
				}

				// how do we want the listfile to be stored?
				ScriptInputUser listFile = new ScriptInputUser();
				listFile.setBinary(false); // since its a list file
				listFile.setFile(input.isFile());
				listFile.setTempFile(input.isTempFile());
				listFile.setTag(input.getTag());
				T2Reference listFileContentReference = referenceService
						.register(listFileContent, 0, true, invocationContext);

				tags.put(listFile.getTag(), setOneInput(referenceService,
						listFileContentReference, listFile));

				listFile.setTag(input.getTag() + "_NAMES");
				T2Reference filenamesFileContentReference = referenceService
						.register(filenamesFileContent, 0, true, null);
				tags.put(listFile.getTag(), setOneInput(referenceService,
						filenamesFileContentReference, listFile));
			} else {
				try {
					// first, concatenate all data
					if (input.isBinary()) {
						ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
						BufferedWriter outputWriter = new BufferedWriter(
								new OutputStreamWriter(outputStream));
						for (T2Reference cur : listOfReferences) {
							InputStreamReader inputReader = new InputStreamReader(
									getAsStream(referenceService, cur));
							IOUtils.copyLarge(inputReader, outputWriter);
							inputReader.close();
						}
						outputWriter.close();
						T2Reference binaryReference = referenceService
								.register(outputStream.toByteArray(), 0, true,
										invocationContext);
						tags.put(input.getTag(), setOneInput(referenceService,
								binaryReference, input));
					} else {
						ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
						BufferedWriter outputWriter = new BufferedWriter(
								new OutputStreamWriter(outputStream));
						for (T2Reference cur : listOfReferences) {
							InputStreamReader inputReader = new InputStreamReader(
									getAsStream(referenceService, cur));
							IOUtils.copyLarge(inputReader, outputWriter);
							outputWriter.write(" ");
							inputReader.close();
						}
						outputWriter.close();
						T2Reference binaryReference = referenceService
								.register(outputStream.toByteArray(), 0, true,
										invocationContext);
						tags.put(input.getTag(), setOneInput(referenceService,
								binaryReference, input));
					}
				} catch (IOException e) {
					throw new InvocationException(e);
				}
			}
		} else {
			tags.put(input.getTag(), setOneInput(referenceService, t2Reference,
					input));
		}
	}

	/*
	 * submit a grid job and wait for it to finish, then get the result as
	 * on-demand downloads or directly as data (in case of local execution)
	 */
	public HashMap<String, Object> Submit(ReferenceService referenceService) throws InvocationException {
		submit_generate_job(referenceService);
		return submit_wait_fetch_results(referenceService);
	}

	/*
	 * just submit the job. useful if you want to wait for it to finish later on
	 * 
	 * Can the statics be made more static?
	 */
	public void submit_generate_job(ReferenceService referenceService) throws InvocationException {
		for (ScriptInputStatic input : usecase.getStatic_inputs()) {
			T2Reference ref;
			if (input.getUrl() != null) {
				// Does this work OK with binary
				try {
					ref = referenceService.register(new URL(input.getUrl()), 0, true, null);
				} catch (ReferenceServiceException e) {
					throw new InvocationException(e);
				} catch (MalformedURLException e) {
					throw new InvocationException(e);
				}
			} else {
				ref = referenceService.register((String) input.getContent(), 0, true, null);
			}
				tags.put(input.getTag(), setOneInput(referenceService, ref, input));
			
		}
		submit_generate_job_inner();
	}

	protected abstract void submit_generate_job_inner() throws InvocationException;

	/*
	 * wait for a submitted job to finish and fetch the results
	 */
	public abstract HashMap<String, Object> submit_wait_fetch_results(ReferenceService referenceService) throws InvocationException;

	public abstract String setOneInput(ReferenceService referenceService, T2Reference t2Reference, ScriptInput input) throws InvocationException;

	protected InputStream getAsStream(ReferenceService referenceService, T2Reference t2Reference) {
		Identified identified = referenceService.resolveIdentifier(t2Reference, null, null);
		if (identified instanceof ReferenceSet) {
			ExternalReferenceSPI ref = ((ReferenceSet) identified).getExternalReferences().iterator().next();
			return ref.openStream(invocationContext);
		}
		return null;
	}

	public void setContext(InvocationContext context) {
		this.invocationContext = context;
		
	}
	
	public InvocationContext getContext() {
		return this.invocationContext;
	}

	public abstract void setStdIn(ReferenceService referenceService,
			T2Reference t2Reference);

	public abstract void rememberRun(String runId);

	/**
	 * @return the runId
	 */
	protected String getRunId() {
		return runId;
	}

	/**
	 * @param runId the runId to set
	 */
	protected void setRunId(String runId) {
		this.runId = runId;
	}

	/**
	 * @return the retrieveData
	 */
	protected boolean isRetrieveData() {
		return retrieveData;
	}

	/**
	 * @param retrieveData the retrieveData to set
	 */
	protected void setRetrieveData(boolean retrieveData) {
		this.retrieveData = retrieveData;
	}
	

}
