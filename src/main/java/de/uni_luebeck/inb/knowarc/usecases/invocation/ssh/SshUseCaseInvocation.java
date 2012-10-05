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

package de.uni_luebeck.inb.knowarc.usecases.invocation.ssh;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Matcher;

import net.sf.taverna.t2.activities.externaltool.RetrieveLoginFromTaverna;
import net.sf.taverna.t2.reference.AbstractExternalReference;
import net.sf.taverna.t2.reference.ErrorDocument;
import net.sf.taverna.t2.reference.ErrorDocumentServiceException;
import net.sf.taverna.t2.reference.ExternalReferenceSPI;
import net.sf.taverna.t2.reference.Identified;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.ReferenceSet;
import net.sf.taverna.t2.reference.ReferencedDataNature;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.security.credentialmanager.CredentialManager;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.ChannelSftp.LsEntry;

import de.uni_luebeck.inb.knowarc.usecases.ScriptInput;
import de.uni_luebeck.inb.knowarc.usecases.ScriptOutput;
import de.uni_luebeck.inb.knowarc.usecases.UseCaseDescription;
import de.uni_luebeck.inb.knowarc.usecases.invocation.AskUserForPw;
import de.uni_luebeck.inb.knowarc.usecases.invocation.InvocationException;
import de.uni_luebeck.inb.knowarc.usecases.invocation.UseCaseInvocation;

/**
 * The job is executed by connecting to a worker pc using ssh, i.e. not via the
 * grid.
 *
 * @author Hajo Krabbenhoeft
 */
public class SshUseCaseInvocation extends UseCaseInvocation {

	private static Logger logger = Logger.getLogger(SshUseCaseInvocation.class);

	private SshUrl location = null;
	
	private InputStream stdInputStream = null;
	
	public static final String SSH_USE_CASE_INVOCATION_TYPE = "D0A4CDEB-DD10-4A8E-A49C-8871003083D8";
	private String tmpname;
	private final SshNode workerNode;
	private final AskUserForPw askUserForPw;

	private ChannelExec running;

	private List<String> precedingCommands = new ArrayList<String>();

	private final ByteArrayOutputStream stdout_buf = new ByteArrayOutputStream();
	private final ByteArrayOutputStream stderr_buf = new ByteArrayOutputStream();

	private static Map<String, Object> nodeLock = Collections.synchronizedMap(new HashMap<String,Object>());

	private static Map<String, Set<SshUrl>> runIdToTempDir = Collections.synchronizedMap(new HashMap<String, Set<SshUrl>> ());

	private static String SSH_INVOCATION_FILE = "sshInvocations";


	public static String test(final SshNode workerNode, final AskUserForPw askUserForPw) {
		try {
			Session sshSession = SshPool.getSshSession(workerNode, askUserForPw);

			ChannelSftp sftpTest = (ChannelSftp) sshSession.openChannel("sftp");
			sftpTest.connect();
			sftpTest.cd(workerNode.getDirectory());
			sftpTest.disconnect();
			sshSession.disconnect();
		} catch (JSchException e) {
			return e.toString();
		} catch (SftpException e) {
			return e.toString();
		}
		return null;
	}

	public SshUseCaseInvocation(UseCaseDescription desc, SshNode workerNodeA, AskUserForPw askUserForPwA)
			throws JSchException, SftpException {
		this.workerNode = workerNodeA;
		setRetrieveData(workerNodeA.isRetrieveData());
		this.askUserForPw = askUserForPwA;
		usecase = desc;

		ChannelSftp sftp = SshPool.getSftpPutChannel(workerNode, askUserForPw);
		synchronized(getNodeLock(workerNode)) {

			logger.info("Changing remote directory to " + workerNode.getDirectory());
			sftp.cd(workerNode.getDirectory());
			Random rnd = new Random();
			while (true) {
				tmpname = "usecase" + rnd.nextLong();
				try {
					sftp.lstat(workerNode.getDirectory() + tmpname);
					continue;
				} catch (Exception e) {
					// file seems to not exist :)
				}
				sftp.mkdir(workerNode.getDirectory() + tmpname);
				sftp.cd(workerNode.getDirectory() + tmpname);
				break;
			}
		}
	}

	private static void recursiveDelete(ChannelSftp sftp, String path) throws SftpException, JSchException {
		Vector<?> entries = sftp.ls(path);
		for (Object object : entries) {
			LsEntry entry = (LsEntry) object;
			if (entry.getFilename().equals(".")
					|| entry.getFilename().equals("..")) {
				continue;
			}
			if (entry.getAttrs().isDir()) {
				recursiveDelete(sftp, path + entry.getFilename() + "/");
			}
			else {
				sftp.rm(path + entry.getFilename());
			}
		}
		sftp.rmdir(path);
	}
	
	private static void deleteDirectory(SshUrl directory) throws InvocationException {
		URI uri;
		try {
			uri = new URI(directory.toString());
		
		
		ChannelSftp sftp;
		SshNode workerNode;
		String fullPath = uri.getPath();
		String path = fullPath.substring(0, fullPath.lastIndexOf("/"));
		String tempDir = fullPath.substring(fullPath.lastIndexOf("/"));
		try {
			workerNode = SshNodeFactory.getInstance().getSshNode(uri.getHost(), uri.getPort(), path);
			
			sftp = SshPool.getSftpPutChannel(workerNode, new RetrieveLoginFromTaverna(workerNode.getUrl().toString()));
		} catch (JSchException e) {
			throw new InvocationException(e);
		}
		synchronized(getNodeLock(workerNode)) {
		try {
			sftp.cd(path);
			recursiveDelete(sftp, path + "/" + tempDir + "/");
		} catch (SftpException e) {
			throw new InvocationException(e);
		} catch (JSchException e) {
			throw new InvocationException(e);
		}
		}
		} catch (URISyntaxException e1) {
			throw new InvocationException(e1);
		}
	}

	public static void cleanup(String runId, CredentialManager credentialManager) throws InvocationException {
		Set<SshUrl> tempDirectories = runIdToTempDir.get(runId);
		if (tempDirectories != null) {
			for (SshUrl tempUrl : tempDirectories) {
				deleteDirectory(tempUrl);
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
		String fullCommand = "cd " + workerNode.getDirectory() + tmpname;
		for (String preceding : precedingCommands) {
			fullCommand += " && " + preceding;
		}
		fullCommand += " && " + command;

		logger.info("Full command is " + fullCommand);

		try {
			running = SshPool.openExecChannel(workerNode, askUserForPw);
			running.setCommand(fullCommand);
			running.setOutputStream(stdout_buf);
			running.setErrStream(stderr_buf);
			if (stdInputStream != null) {
				running.setInputStream(stdInputStream);
			}
			running.connect();
		} catch (JSchException e) {
			throw new InvocationException(e);
		}

	}

	@Override
	public HashMap<String, Object> submit_wait_fetch_results(ReferenceService referenceService) throws InvocationException {
		while (!running.isClosed()) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					throw new InvocationException("Invocation interrupted:" + e.getMessage());
				}
		}

		int exitcode = running.getExitStatus();
		if (!usecase.getValidReturnCodes().contains(exitcode)) {
			try {
				throw new InvocationException("Invalid exit code " + exitcode + ":" + stderr_buf.toString("US-ASCII"));
			} catch (UnsupportedEncodingException e) {
				throw new InvocationException("Invalid exit code " + exitcode + ":" + stderr_buf.toString());
			}
		}

		HashMap<String, Object> results = new HashMap<String, Object>();


			results.put("STDOUT", stdout_buf.toByteArray());
			results.put("STDERR", stderr_buf.toByteArray());
			try {
				stdout_buf.close();
				stderr_buf.close();
			} catch (IOException e2) {
				throw new InvocationException(e2);
			}

		try {
			ChannelSftp sftp = SshPool.getSftpPutChannel(workerNode, askUserForPw);
		synchronized(getNodeLock(workerNode)) {
		for (Map.Entry<String, ScriptOutput> cur : usecase.getOutputs().entrySet()) {
			ScriptOutput scriptOutput = cur.getValue();
			String fullPath = workerNode.getDirectory() + tmpname + "/" + scriptOutput.getPath();
			try {
				if (sftp.stat(fullPath) != null) {
					SshUrl url = new SshUrl(workerNode);
					url.setSubDirectory(tmpname);
					url.setFileName(scriptOutput.getPath());
					if (scriptOutput.isBinary()) {
						url.setDataNature(ReferencedDataNature.BINARY);
					} else {
						url.setDataNature(ReferencedDataNature.TEXT);
						url.setCharset("UTF-8");
					}
					if (isRetrieveData()) {
						SshReference urlRef = new SshReference(url);
						InputStream is = urlRef.openStream(null);
						AbstractExternalReference ref;
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
						results.put(cur.getKey(), ref);
					} else {
						results.put(cur.getKey(), url);
					}
				} else {
					ErrorDocument ed = referenceService.getErrorDocumentService().registerError("No result for " + cur.getKey(), 0, getContext());
					results.put(cur.getKey(), ed);
				}
			} catch (SftpException e) {
				ErrorDocument ed = referenceService.getErrorDocumentService().registerError("No result for " + cur.getKey(), 0, getContext());
				results.put(cur.getKey(), ed);

			}
		}
		}
		} catch (JSchException e1) {
			throw new InvocationException(e1);
		} catch (ErrorDocumentServiceException e) {
			throw new InvocationException(e);
		}

		if (running != null) {
		    running.disconnect();
		}
		if (stdInputStream != null) {
			try {
				stdInputStream.close();
			} catch (IOException e) {
				throw new InvocationException(e);
			}
		}
		if (isRetrieveData()) {
			forgetRun();
			deleteDirectory(location);

		}
		return results;
	}


	@Override
	public String setOneInput(ReferenceService referenceService,
			T2Reference t2Reference, ScriptInput input)
			throws InvocationException {
		String target = null;
		String remoteName = null;
		if (input.isFile()) {
			remoteName = input.getTag();
		} else if (input.isTempFile()) {
			remoteName = "tempfile." + (nTempFiles++) + ".tmp";

		}
		if (input.isFile() || input.isTempFile()) {
			SshReference sshRef = getAsSshReference(referenceService, t2Reference, workerNode);
			target = workerNode.getDirectory() + tmpname + "/" + remoteName;
			logger.info("Target is " + target);
			if (sshRef != null) {
				if (!input.isForceCopy()) {
					String linkCommand = workerNode.getLinkCommand();
					if (linkCommand != null) {
						String actualLinkCommand = getActualOsCommand(linkCommand, sshRef.getFullPath(), remoteName, target);
						precedingCommands.add(actualLinkCommand);
						return target;

					}
				}
				String copyCommand = workerNode.getCopyCommand();
				if (copyCommand != null) {
					String actualCopyCommand = getActualOsCommand(copyCommand, sshRef.getFullPath(), remoteName, target);
					precedingCommands.add(actualCopyCommand);
					return target;
				}
			}
			try {
				ChannelSftp sftp = SshPool.getSftpPutChannel(workerNode,
						askUserForPw);
				synchronized (getNodeLock(workerNode)) {
					InputStream r = getAsStream(referenceService, t2Reference);
					sftp.put(r, target);
					r.close();
				}
			} catch (SftpException e) {
				throw new InvocationException(e);
			} catch (JSchException e) {
				throw new InvocationException(e);
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

    public SshReference getAsSshReference(ReferenceService referenceService,
			T2Reference t2Reference, SshNode workerNode) {
    	Identified identified = referenceService.resolveIdentifier(t2Reference, null, null);
		if (identified instanceof ReferenceSet) {
			for (ExternalReferenceSPI ref : ((ReferenceSet) identified).getExternalReferences()) {
				if (ref instanceof SshReference) {
					SshReference sshRef = (SshReference) ref;
					if (sshRef.getHost().equals(workerNode.getHost())) {
						return sshRef;
					}
				}
			}
		}
		return null;
	}


	private static Object getNodeLock(final SshNode node) {
	return getNodeLock(node.getHost());
    }

    private static synchronized Object getNodeLock(String hostName) {
	if (!nodeLock.containsKey(hostName)) {
	    nodeLock.put(hostName, new Object());
	}
	return nodeLock.get(hostName);
    }

	@Override
	public void setStdIn(ReferenceService referenceService,
			T2Reference t2Reference) {
		stdInputStream = new BufferedInputStream(getAsStream(referenceService, t2Reference));
	}

	@Override
	public void rememberRun(String runId) {
		this.setRunId(runId);
		Set<SshUrl> directories = runIdToTempDir.get(runId);
		if (directories == null) {
			directories = Collections.synchronizedSet(new HashSet<SshUrl> ());
			runIdToTempDir.put(runId, directories);
		}
			location = new SshUrl(workerNode);
			location.setSubDirectory(tmpname);
			directories.add(location);
	}
	
	private void forgetRun() {
		Set<SshUrl> directories = runIdToTempDir.get(getRunId());
		directories.remove(location);
	}

	public static void load(File directory) {
		File invocationsFile = new File(directory, SSH_INVOCATION_FILE);
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
				String urlString = parts[1];
				Set<SshUrl> urls = runIdToTempDir.get(runId);
				if (urls == null) {
					urls = new HashSet<SshUrl>();
					runIdToTempDir.put(runId, urls);
				}
				URI uri = new URI(urlString);
				String fullPath = uri.getPath();
				String path = fullPath.substring(0, fullPath.lastIndexOf("/"));
				String tempDir = fullPath.substring(fullPath.lastIndexOf("/"));
				SshNode node = SshNodeFactory.getInstance().getSshNode(
						uri.getHost(), uri.getPort(), path);
				SshUrl newUrl = new SshUrl(node);
				newUrl.setSubDirectory(tempDir);
				urls.add(newUrl);
				line = reader.readLine();
			}
		} catch (FileNotFoundException e) {
			logger.error(e);
		} catch (URISyntaxException e) {
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
		File invocationsFile = new File(directory, SSH_INVOCATION_FILE);
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(invocationsFile));
			for (String runId : runIdToTempDir.keySet()) {
				for (SshUrl url : runIdToTempDir.get(runId)) {
					writer.write(runId);
					writer.write(" ");
					writer.write(url.toString());
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
