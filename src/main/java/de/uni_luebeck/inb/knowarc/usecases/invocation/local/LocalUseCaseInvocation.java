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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
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
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;

import net.sf.taverna.t2.reference.AbstractExternalReference;
import net.sf.taverna.t2.reference.ErrorDocument;
import net.sf.taverna.t2.reference.ExternalReferenceSPI;
import net.sf.taverna.t2.reference.Identified;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.ReferenceSet;
import net.sf.taverna.t2.reference.ReferencedDataNature;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.reference.impl.external.file.FileReference;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import de.uni_luebeck.inb.knowarc.usecases.ScriptInput;
import de.uni_luebeck.inb.knowarc.usecases.ScriptOutput;
import de.uni_luebeck.inb.knowarc.usecases.UseCaseDescription;
import de.uni_luebeck.inb.knowarc.usecases.invocation.InvocationException;
import de.uni_luebeck.inb.knowarc.usecases.invocation.UseCaseInvocation;
import de.uni_luebeck.inb.knowarc.usecases.invocation.ssh.SshNode;
import de.uni_luebeck.inb.knowarc.usecases.invocation.ssh.SshNodeFactory;
import de.uni_luebeck.inb.knowarc.usecases.invocation.ssh.SshUrl;

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

	private Reader stdInReader = null;
	
	private static Map<String, Set<String>> runIdToTempDir = Collections.synchronizedMap(new HashMap<String, Set<String>> ());
	
	private static String LOCAL_INVOCATION_FILE = "localInvocations";

	public LocalUseCaseInvocation(UseCaseDescription desc, boolean retrieveData, String mainTempDirectory, String shellPrefix, String linkCommand) throws IOException {

		usecase = desc;
		setRetrieveData(retrieveData);
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
		logger.info("mainTempDirectory is " + mainTempDirectory);
		logger.info("Using tempDir " + tempDir.getAbsolutePath());

	}

	void recDel(File c) {
		File[] files = c.listFiles();
		if (files != null) {
			for (File cc : files)
				recDel(cc);
		}
		c.delete();
	}
	
	private String setOneBinaryInput(ReferenceService referenceService,
			T2Reference t2Reference, ScriptInput input, String targetSuffix)
			throws InvocationException {

		if (input.isFile() || input.isTempFile()) {
			// Try to get it as a file
			String target = tempDir.getAbsolutePath() + "/" + targetSuffix;
			FileReference fileRef = getAsFileReference(referenceService,
					t2Reference);
			if (fileRef != null) {

				if (!input.isForceCopy()) {
					if (linkCommand != null) {
						String source = fileRef.getFile().getAbsolutePath();
						String actualLinkCommand = getActualOsCommand(
								linkCommand, source, targetSuffix, target);
						logger.info("Link command is " + actualLinkCommand);
						String[] splitCmds = actualLinkCommand.split(" ");
						ProcessBuilder builder = new ProcessBuilder(splitCmds);
						builder.directory(tempDir);
						try {
							int code = builder.start().waitFor();
							if (code == 0) {
								return target;
							} else {
								logger.error("Link command gave errorcode: "
										+ code);
							}

						} catch (InterruptedException e) {
							// go through
						} catch (IOException e) {
							// go through
						}

					}
				}
			}

			InputStream is = null;
			OutputStream os = null;
			is = getAsStream(referenceService, t2Reference);

			try {
				os = new FileOutputStream(target);
			} catch (FileNotFoundException e) {
				throw new InvocationException(e);
			}

			try {
				IOUtils.copyLarge(is, os);
			} catch (IOException e) {
				throw new InvocationException(e);
			}
			try {
				is.close();
				os.close();
			} catch (IOException e) {
				throw new InvocationException(e);
			}
			return target;
		} else {
			String value = (String) referenceService.renderIdentifier(
					t2Reference, String.class, this.getContext());
			return value;
		}
	}
	
	@Override
	public String setOneInput(ReferenceService referenceService,
			T2Reference t2Reference, ScriptInput input)
			throws InvocationException {

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

		if (input.isBinary()) {
			return setOneBinaryInput(referenceService, t2Reference, input,
					targetSuffix);
		}

		logger.info("Target is " + target);
		if (input.isFile() || input.isTempFile()) {
			target = tempDir.getAbsolutePath() + "/" + targetSuffix;
			// Try to get it as a file
			Reader r;
			Writer w;
			FileReference fileRef = getAsFileReference(referenceService,
					t2Reference);
			if (fileRef != null) {

				if (!input.isForceCopy()) {
					if (linkCommand != null) {
						String source = fileRef.getFile().getAbsolutePath();
						String actualLinkCommand = getActualOsCommand(
								linkCommand, source, targetSuffix, target);
						logger.info("Link command is " + actualLinkCommand);
						String[] splitCmds = actualLinkCommand.split(" ");
						ProcessBuilder builder = new ProcessBuilder(splitCmds);
						builder.directory(tempDir);
						try {
							int code = builder.start().waitFor();
							if (code == 0) {
								return target;
							} else {
								logger.error("Link command gave errorcode: "
										+ code);
							}

						} catch (InterruptedException e) {
							// go through
						} catch (IOException e) {
							// go through
						}

					}
				}

				if (fileRef.getDataNature().equals(ReferencedDataNature.TEXT)) {
					r = new InputStreamReader(fileRef.openStream(this
							.getContext()), Charset.forName(fileRef
							.getCharset()));
				} else {
					try {
						r = new FileReader(fileRef.getFile());
					} catch (FileNotFoundException e) {
						throw new InvocationException(e);
					}
				}
			} else {
				r = new InputStreamReader(getAsStream(referenceService,
						t2Reference));
			}
			try {
				w = new OutputStreamWriter(new FileOutputStream(target), input
						.getCharsetName());
			} catch (UnsupportedEncodingException e) {
				throw new InvocationException(e);
			} catch (FileNotFoundException e) {
				throw new InvocationException(e);
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
		} else {
			String value = (String) referenceService.renderIdentifier(
					t2Reference, String.class, this.getContext());
			return value;
		}
	}
	
	private void forgetRun() {
		Set<String> directories = runIdToTempDir.get(getRunId());
		try {
			directories.remove(tempDir.getCanonicalPath());
		} catch (IOException e) {
			logger.error(e);
		}
	}

	private static void deleteDirectory(String location) {
		try {
			FileUtils.deleteDirectory(new File(location));
		} catch (IOException e) {
			logger.error("Problem deleting " + location, e);
		}
	}
	
	public static void cleanup(String runId) {
		Set<String> tempDirectories = runIdToTempDir.get(runId);
		if (tempDirectories != null) {
			for (String tempDir : tempDirectories) {
				deleteDirectory(tempDir);
			}
			runIdToTempDir.remove(runId);
		}
	}
	
	@Override
	protected void submit_generate_job_inner() throws InvocationException {
		tags.put("uniqueID", "" + getSubmissionID());
		String command = usecase.getCommand();
		for (String cur : tags.keySet()) {
		    command = command.replaceAll("\\Q%%" + cur + "%%\\E", Matcher.quoteReplacement(tags.get(cur)));
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
			logger.info("cmds[" + i + "] = " + cmds.get(i));
		}
		logger.info("Command is " + command + " in directory " + tempDir);
		try {
			running = builder.start();
			if (stdInReader != null) {
				BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(running.getOutputStream()));
				IOUtils.copyLarge(stdInReader, writer);
				writer.close();
			}
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
	public HashMap<String, Object> submit_wait_fetch_results(ReferenceService referenceService) throws InvocationException {
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
				int exitcode = running.exitValue();
				if (!usecase.getValidReturnCodes().contains(exitcode)) {
					try {
						throw new InvocationException("Invalid exit code " + exitcode + ":" + stderr_buf.toString("US-ASCII"));
					} catch (UnsupportedEncodingException e) {
						throw new InvocationException("Invalid exit code " + exitcode + ":" + stderr_buf.toString());
					}
				}
				else
					break;
			} catch (IllegalThreadStateException e) {

				try {
					Thread.sleep(100);
				} catch (InterruptedException e1) {
					throw new InvocationException(e);
				}

			}
		}

		HashMap<String, Object> results = new HashMap<String, Object>();
			results.put("STDOUT", stdout_buf.toByteArray());
			results.put("STDERR", stderr_buf.toByteArray());

		for (Map.Entry<String, ScriptOutput> cur : usecase.getOutputs().entrySet()) {
			ScriptOutput scriptOutput = cur.getValue();
			File result = new File(tempDir.getAbsoluteFile() + "/" + cur.getValue().getPath());
			if (result.exists()) {
				AbstractExternalReference ref;
				if (isRetrieveData()) {
					FileInputStream is;
					try {
						is = new FileInputStream(result);
					} catch (FileNotFoundException e) {
						throw new InvocationException(e);
					}
					if (scriptOutput.isBinary()) {
						ref = inlineByteArrayReferenceBuilder.createReference(is, null);
					} else {
						ref = inlineStringReferenceBuilder.createReference(is, null);
					}
					try {
						is.close();
					} catch (IOException e) {
						throw new InvocationException(e);
					}
				}
				else {
					ref = new FileReference(result);
					if (scriptOutput.isBinary()) {
						((FileReference) ref)
								.setDataNature(ReferencedDataNature.BINARY);
					} else {
						((FileReference) ref)
								.setDataNature(ReferencedDataNature.TEXT);
						((FileReference) ref).setCharset("UTF-8");
					}
				}
				results.put(cur.getKey(), ref);
			} else {
				ErrorDocument ed = referenceService.getErrorDocumentService().registerError("No result for " + cur.getKey(), 0, getContext());
				results.put(cur.getKey(), ed);
			}
		}
		
		if (isRetrieveData()) {
			forgetRun();
			try {
				deleteDirectory(tempDir.getCanonicalPath());
			} catch (IOException e) {
				throw new InvocationException(e);
			}
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

	@Override
	public void setStdIn(ReferenceService referenceService,
			T2Reference t2Reference) {
		stdInReader = new BufferedReader(new InputStreamReader(getAsStream(referenceService, t2Reference)));
	}

	@Override
	public void rememberRun(String runId) {
		this.setRunId(runId);
		Set<String> directories = runIdToTempDir.get(runId);
		if (directories == null) {
			directories = Collections.synchronizedSet(new HashSet<String> ());
			runIdToTempDir.put(runId, directories);
		}
		try {
			directories.add(tempDir.getCanonicalPath());
		} catch (IOException e) {
			logger.error("Unable to record temporary directory: " + tempDir, e);
		}
	}

	public static void load(File directory) {
		File invocationsFile = new File(directory, LOCAL_INVOCATION_FILE);
		if (!invocationsFile.exists()) {
			return;
		}
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(invocationsFile));
			String line = reader.readLine();
			while (line != null) {
				String[] parts = line.split(" ");
				if (parts.length != 2) {
					break;
				}
				String runId = parts[0];
				String tempDirString = parts[1];
				Set<String> tempDirs = runIdToTempDir.get(runId);
				if (tempDirs == null) {
					tempDirs = new HashSet<String>();
					runIdToTempDir.put(runId, tempDirs);
				}
				tempDirs.add(tempDirString);
				line = reader.readLine();
			}
		} catch (FileNotFoundException e) {
			logger.error(e);
		} catch (IOException e) {
			logger.error(e);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					logger.error(e);
				}
			}
		}
	}

	public static void persist(File directory) {
		File invocationsFile = new File(directory, LOCAL_INVOCATION_FILE);
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(invocationsFile));
			for (String runId : runIdToTempDir.keySet()) {
				for (String tempDir : runIdToTempDir.get(runId)) {
					writer.write(runId);
					writer.write(" ");
					writer.write(tempDir);
					writer.newLine();
				}
			}
		} catch (IOException e) {
			logger.error(e);
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					logger.error(e);
				}
			}
		}
	}

}
