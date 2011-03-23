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

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import net.sf.taverna.t2.reference.ExternalReferenceSPI;
import net.sf.taverna.t2.reference.Identified;
import net.sf.taverna.t2.reference.ReferenceContext;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.ReferenceSet;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.reference.impl.EmptyReferenceContext;
import net.sf.taverna.t2.reference.impl.external.file.FileReference;

import org.globus.ftp.exception.NotImplementedException;

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
	protected UseCaseDescription usecase;
	protected final HashMap<String, String> tags = new HashMap<String, String>();
	protected int nTempFiles = 0;
	public final ReferenceContext dummyContext = new EmptyReferenceContext();
	
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

	private static int submissionID = 0;

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
	public void setInput(String inputName, ReferenceService referenceService, T2Reference t2Reference) throws IOException {
		ScriptInputUser input = (ScriptInputUser) usecase.getInputs().get(inputName);
//		if (input.isList()) {
//			List data = null;
//			if (value instanceof OnDemandDownload) {
//				data = (List) ((OnDemandDownload) value).download();
//			} else {
//				data = (List) value;
//			}

//			if (!input.isConcatenate()) {
//				// this is a list input (not concatenated)
//				// so write every element to its own temporary file
//				// and create a filelist file
//
//				// we need to write the list elements to temporary files
//				ScriptInputUser listElements = new ScriptInputUser();
//				listElements.setBinary(input.isBinary());
//				listElements.setTempFile(true);
//
//				String lineEndChar = "\n";
//				if (!input.isFile() && !input.isTempFile())
//					lineEndChar = " ";
//
//				String list = "";
//				// create a list of all temp file names
//				for (Object cur : data) {
//					String tmp = setOneInput(cur, listElements);
//					list += tmp + lineEndChar;
//				}
//
//				String filenames = "";
//				// create a list of the original file names, works only in url
//				// mode
//				for (Object cur : data) {
//					String tmp = "NO_URL_MODE_ERROR";
//					if (cur instanceof OnDemandDownload) {
//						tmp = ((OnDemandDownload) cur).getReferenceURL();
//						int ind = tmp.lastIndexOf('/');
//						if (ind == -1)
//							ind = tmp.lastIndexOf('\\');
//						if (ind != -1)
//							tmp = tmp.substring(ind);
//					}
//					filenames += tmp + lineEndChar;
//				}
//
//				// how do we want the listfile to be stored?
//				ScriptInputUser listFile = new ScriptInputUser();
//				listFile.setBinary(false); // since its a list file
//				listFile.setFile(input.isFile());
//				listFile.setTempFile(input.isTempFile());
//				listFile.setTag(input.getTag());
//
//				tags.put(listFile.getTag(), setOneInput(list, listFile));
//
//				listFile.setTag(input.getTag() + "_NAMES");
//				tags.put(listFile.getTag(), setOneInput(filenames, listFile));
//			} else {
//				Object concat = null;
//				// first, concatenate all data
//				if (input.isBinary()) {
//					int size = 0;
//					for (Object cur : data) {
//						size += ((byte[]) cur).length;
//					}
//					byte[] concatb = new byte[size];
//					int pos = 0;
//					for (Object cur : data) {
//						byte[] curb = (byte[]) cur;
//						System.arraycopy(curb, 0, concatb, pos, curb.length);
//						pos += curb.length;
//					}
//					concat = concatb;
//				} else {
//					String concata = "";
//					for (Object cur : data) {
//						concata += (String) cur;
//					}
//					concat = concata;
//				}
//				// then set as normal input
//				tags.put(input.getTag(), setOneInput(concat, input));
//			}
//		} else {
			tags.put(input.getTag(), setOneInput(referenceService, t2Reference, input));
//		}
	}


	public abstract void Cleanup();

	public abstract void putFile(String name, byte[] contents);

	public abstract void putReference(String name, String source) throws NotImplementedException;

	/*
	 * submit a grid job and wait for it to finish, then get the result as
	 * on-demand downloads or directly as data (in case of local execution)
	 */
	public HashMap<String, Object> Submit(ReferenceService referenceService) throws Exception {
		submit_generate_job(referenceService);
		return submit_wait_fetch_results();
	}

	/*
	 * just submit the job. useful if you want to wait for it to finish later on
	 * 
	 * Can the statics be made more static?
	 */
	public void submit_generate_job(ReferenceService referenceService) throws Exception {
		for (ScriptInputStatic input : usecase.getStatic_inputs()) {
			T2Reference ref;
			if (input.getUrl() != null) {
				// Does this work OK with binary
				ref = referenceService.register(new URL(input.getUrl()), 0, true, null);
			} else {
				ref = referenceService.register((String) input.getContent(), 0, true, null);
			}
			tags.put(input.getTag(), setOneInput(referenceService, ref, input));
		}
		submit_generate_job_inner();
	}

	protected abstract void submit_generate_job_inner() throws Exception;

	/*
	 * wait for a submitted job to finish and fetch the results
	 */
	public abstract HashMap<String, Object> submit_wait_fetch_results() throws Exception;

	public abstract String setOneInput(ReferenceService referenceService, T2Reference t2Reference, ScriptInput input)
			throws UnsupportedEncodingException, IOException;

	protected InputStream getAsStream(ReferenceService referenceService, T2Reference t2Reference) {
		Identified identified = referenceService.resolveIdentifier(t2Reference, null, null);
		if (identified instanceof ReferenceSet) {
			ExternalReferenceSPI ref = ((ReferenceSet) identified).getExternalReferences().iterator().next();
			return ref.openStream(dummyContext);
		}
		return null;
	}

	protected FileReference getAsFileReference(ReferenceService referenceService, T2Reference t2Reference) {
		Identified identified = referenceService.resolveIdentifier(t2Reference, null, null);
		if (identified instanceof ReferenceSet) {
			for (ExternalReferenceSPI ref : ((ReferenceSet) identified).getExternalReferences()) {
				if (ref instanceof FileReference) {
					return (FileReference) ref;
				}
			}
		}
		return null;
	}
}
