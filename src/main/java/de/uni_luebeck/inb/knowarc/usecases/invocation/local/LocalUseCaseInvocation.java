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

package de.uni_luebeck.inb.knowarc.usecases.invocation.local;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.taverna.t2.reference.ExternalReferenceSPI;
import net.sf.taverna.t2.reference.Identified;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.ReferenceSet;
import net.sf.taverna.t2.reference.ReferencedDataNature;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.reference.impl.external.file.FileReference;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.globus.ftp.exception.NotImplementedException;

import de.uni_luebeck.inb.knowarc.usecases.ScriptInput;
import de.uni_luebeck.inb.knowarc.usecases.ScriptOutput;
import de.uni_luebeck.inb.knowarc.usecases.UseCaseDescription;
import de.uni_luebeck.inb.knowarc.usecases.invocation.InvocationException;
import de.uni_luebeck.inb.knowarc.usecases.invocation.UseCaseInvocation;

/**
 * The job is executed locally, i.e. not via the grid.
 * @author Hajo Krabbenhoeft
 */
public class LocalUseCaseInvocation extends UseCaseInvocation {

	private static Logger logger = Logger.getLogger(LocalUseCaseInvocation.class);

	private final File tempDir;
	
	public static String LOCAL_USE_CASE_INVOCATION_TYPE = "789663B8-DA91-428A-9F7D-B3F3DA185FD4";
	
	private Process running;

	private final String shellPrefix;

	private final String linkCommand;

	public LocalUseCaseInvocation(UseCaseDescription desc, String mainTempDirectory, String shellPrefix, String linkCommand) throws IOException {

		usecase = desc;
		this.shellPrefix = shellPrefix;
		this.linkCommand = linkCommand;
		
		if (mainTempDirectory != null) {
		
			File mainTempDir = new File(mainTempDirectory);

			tempDir = File.createTempFile("usecase", "dir", mainTempDir);
		} else {
			tempDir = File.createTempFile("usecase", "dir");
		}
		tempDir.delete();
		tempDir.mkdir();
		System.err.println("mainTempDirectory is " + mainTempDirectory);
		System.err.println("Using tempDir " + tempDir.getAbsolutePath());

	}

	void recDel(File c) {
		File[] files = c.listFiles();
		if (files != null) {
			for (File cc : files)
				recDel(cc);
		}
		c.delete();
	}
	
	@Override
	public String setOneInput(ReferenceService referenceService, T2Reference t2Reference, ScriptInput input) throws InvocationException {
		
		if (input.getCharsetName() == null) {
			input.setCharsetName(Charset.defaultCharset().name());
		}
		String target = null;
		String targetSuffix = null;
		if (input.isFile()) {
			targetSuffix = input.getTag();
		} else if (input.isTempFile()) {
			targetSuffix = "tempfile." + (nTempFiles++) + ".tmp";
		}
		logger.info("Target is " + target);
		if (input.isFile() || input.isTempFile()) {
			target = tempDir.getAbsolutePath() + "/" + targetSuffix;
			// Try to get it as a file
			Reader r;
			Writer w;
			FileReference fileRef = getAsFileReference(referenceService, t2Reference);
			if (fileRef != null) {
				
				if (!input.isForceCopy()) {
					if (linkCommand != null) {
						String source = fileRef.getFile().getAbsolutePath();
						String actualLinkCommand = getActualOsCommand(linkCommand, source, targetSuffix, target);
						logger.error("Link command is " + actualLinkCommand);
						String[] splitCmds = actualLinkCommand.split(" ");
						ProcessBuilder builder = new ProcessBuilder(splitCmds);
						builder.directory(tempDir);
						try {
							int code = builder.start().waitFor();
							if (code == 0) {
								return target;
							} else {
								logger.error("Link command gave errorcode: " + code);
							}
								
						} catch (InterruptedException e) {
							// go through
						} catch (IOException e) {
							// go through
						}

					}
				}
				
				if (input.isBinary()) {
						try {
							r = new FileReader(fileRef.getFile());
						} catch (FileNotFoundException e) {
							throw new InvocationException(e);
						}
						
				} else {
					if (fileRef.getDataNature().equals(ReferencedDataNature.TEXT)) {
						r = new InputStreamReader(fileRef.openStream(dummyContext), Charset.forName(fileRef.getCharset()));
					} else {
						try {
							r = new FileReader(fileRef.getFile());
						} catch (FileNotFoundException e) {
							throw new InvocationException(e);
						}
					}
				}
			} else {
					r = new InputStreamReader(getAsStream(referenceService, t2Reference));
			}
			if (input.isBinary()) {
				try {
					w = new FileWriter(target);
				} catch (IOException e) {
					throw new InvocationException(e);
				}				
			} else {
				try {
					w = new OutputStreamWriter(new FileOutputStream(target), input.getCharsetName());
				} catch (UnsupportedEncodingException e) {
					throw new InvocationException(e);
				} catch (FileNotFoundException e) {
					throw new InvocationException(e);
				}				
			}
			try {
				IOUtils.copyLarge(r, w);
			} catch (IOException e) {
				throw new InvocationException(e);
			}
			try {
				r.close();
				w.close();
			} catch (IOException e) {
				throw new InvocationException(e);
			}
			return target;
		}
		else {
			String value = (String) referenceService.renderIdentifier(t2Reference, String.class, dummyContext);
			return value;
		}
	}
	
	@Override
	public void putFile(String name, byte[] contents) {
		try {
			FileOutputStream out = new FileOutputStream(tempDir.getAbsolutePath() + "/" + name, false);
			out.write(contents);
			out.close();
		} catch (Exception e) {
		    // TODO
		}
	}

	@Override
	public void putReference(String name, String source) throws NotImplementedException {
		throw new NotImplementedException();
	}

	@Override
	public void Cleanup() {
//		recDel(tempDir);
//		tempFile.delete();
	}
	
	@Override
	protected void submit_generate_job_inner() throws InvocationException {
		tags.put("uniqueID", "" + getSubmissionID());
		String command = usecase.getCommand();
		for (String cur : tags.keySet()) {
			command = command.replaceAll("\\Q%%" + cur + "%%\\E", tags.get(cur));
		}

		List<String> cmds = new ArrayList<String>();
		if ((shellPrefix != null) && !shellPrefix.isEmpty()) {
			String[] prefixCmds = shellPrefix.split(" ");
			for (int i = 0; i < prefixCmds.length; i++) {
				cmds.add(prefixCmds[i]);
			}
			cmds.add(command);
		} else {
			String[] splitCmds = command.split(" ");
			for (int i = 0; i < splitCmds.length; i++) {
				cmds.add(splitCmds[i]);
			}
		}
	
		ProcessBuilder builder = new ProcessBuilder(cmds);
		builder.directory(tempDir);

		for (int i = 0; i < cmds.size(); i++) {
			logger.error("cmds[" + i + "] = " + cmds.get(i));
		}
		logger.error("Command is " + command + " in directory " + tempDir);
		try {
			running = builder.start();
		} catch (IOException e) {
			throw new InvocationException(e);
		}
	}

	private void copy_stream(InputStream read, OutputStream write) throws IOException {
		int a = read.available();
		if (a > 0) {
			byte[] buf = new byte[a];
			read.read(buf);
			write.write(buf);
		}
	}

	@Override
	public HashMap<String, Object> submit_wait_fetch_results() throws InvocationException {
		ByteArrayOutputStream stdout_buf = new ByteArrayOutputStream();
		ByteArrayOutputStream stderr_buf = new ByteArrayOutputStream();
		while (true) {
			try {
				copy_stream(running.getInputStream(), stdout_buf);
				copy_stream(running.getErrorStream(), stderr_buf);
			} catch (IOException e1) {
				throw new InvocationException(e1);
			}
			try {
				int errorCode = running.exitValue();
				if (errorCode != 0)
					throw new InvocationException("Exit code: " + errorCode + stderr_buf.toString("US-ASCII"));
				else
					break;
			} catch (IllegalThreadStateException e) {
				try {
					Thread.sleep(100);
				} catch (Exception e2) {
				}
			} catch (UnsupportedEncodingException e) {
				throw new InvocationException(e);
			}
		}

		HashMap<String, Object> results = new HashMap<String, Object>();
		try {
			results.put("STDOUT", stdout_buf.toString("US-ASCII"));
			results.put("STDERR", stderr_buf.toString("US-ASCII"));
		} catch (UnsupportedEncodingException e) {
			throw new InvocationException(e);
		}

		for (Map.Entry<String, ScriptOutput> cur : usecase.getOutputs().entrySet()) {
			FileReference ref = new FileReference(new File(tempDir.getAbsoluteFile() + "/" + cur.getValue().getPath()));
			ref.setReferencingDeletableData(true);
			ref.setReferencingMutableData(true);
			results.put(cur.getKey(), ref);
		}

		return results;
	}

	private FileReference getAsFileReference(ReferenceService referenceService, T2Reference t2Reference) {
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
