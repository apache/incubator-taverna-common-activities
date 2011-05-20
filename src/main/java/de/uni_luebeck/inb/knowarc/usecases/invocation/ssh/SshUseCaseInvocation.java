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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Vector;

import net.sf.taverna.t2.reference.ErrorDocument;
import net.sf.taverna.t2.reference.ErrorDocumentServiceException;
import net.sf.taverna.t2.reference.ExternalReferenceSPI;
import net.sf.taverna.t2.reference.Identified;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.ReferenceSet;
import net.sf.taverna.t2.reference.T2Reference;

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
	
	private InputStream stdInputStream = null;
	
	
	public static final String SSH_USE_CASE_INVOCATION_TYPE = "D0A4CDEB-DD10-4A8E-A49C-8871003083D8";
	private String tmpname;
	private final SshNode workerNode;
	private final AskUserForPw askUserForPw;
	
	private ChannelExec running;
	
	private List<String> precedingCommands = new ArrayList<String>();
	
	private final ByteArrayOutputStream stdout_buf = new ByteArrayOutputStream();
	private final ByteArrayOutputStream stderr_buf = new ByteArrayOutputStream();

    private static HashMap<String, Object> nodeLock = new HashMap<String,Object>();

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

	private void recursiveDelete(ChannelSftp sftp, String path) throws SftpException, JSchException {
		if (!path.startsWith(workerNode.getDirectory() + tmpname)) {
			return;
		}
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

	@Override
	public void cleanup() throws InvocationException {
			ChannelSftp sftp;
			try {
				sftp = SshPool.getSftpPutChannel(workerNode, askUserForPw);
			} catch (JSchException e) {
				throw new InvocationException(e);
			}
			synchronized(getNodeLock(workerNode)) {
			try {
				sftp.cd(workerNode.getDirectory());
				recursiveDelete(sftp, workerNode.getDirectory() + tmpname + "/");
			} catch (SftpException e) {
				throw new InvocationException(e);
			} catch (JSchException e) {
				throw new InvocationException(e);
			}
			}
	}

	@Override
	protected void submit_generate_job_inner() throws InvocationException {
		tags.put("uniqueID", "" + getSubmissionID());
		String command = usecase.getCommand();
		for (String cur : tags.keySet()) {
			command = command.replaceAll("\\Q%%" + cur + "%%\\E", tags.get(cur));
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
		if (exitcode < 0) {
			try {
				throw new InvocationException("Nonzero exit code " + exitcode + ":" + stderr_buf.toString("US-ASCII"));
			} catch (UnsupportedEncodingException e) {
				throw new InvocationException("Nonzero exit code " + exitcode + ":" + stderr_buf.toString());
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
			String fullPath = workerNode.getDirectory() + tmpname + "/" + cur.getValue().getPath();
			try {
				if (sftp.stat(fullPath) != null) {
					SshUrl url = new SshUrl(workerNode);
					url.setSubDirectory(tmpname);
					url.setFileName(cur.getValue().getPath());
					results.put(cur.getKey(), url);
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
}
