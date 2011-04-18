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

package de.uni_luebeck.inb.knowarc.usecases.invocation.grid;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import net.sf.taverna.t2.reference.ExternalReferenceSPI;
import net.sf.taverna.t2.reference.Identified;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.ReferenceSet;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.reference.impl.external.file.FileReference;

import org.globus.ftp.exception.ClientException;
import org.globus.ftp.exception.NotImplementedException;
import org.globus.ftp.exception.ServerException;
import org.ietf.jgss.GSSException;

import de.uni_luebeck.inb.knowarc.grid.GridFtpConnection;
import de.uni_luebeck.inb.knowarc.grid.GridInfosystem;
import de.uni_luebeck.inb.knowarc.grid.GridJob;
import de.uni_luebeck.inb.knowarc.grid.ProgressDisplay;
import de.uni_luebeck.inb.knowarc.grid.xRslDescription;
import de.uni_luebeck.inb.knowarc.grid.re.RuntimeEnvironment;
import de.uni_luebeck.inb.knowarc.usecases.ScriptInput;
import de.uni_luebeck.inb.knowarc.usecases.ScriptOutput;
import de.uni_luebeck.inb.knowarc.usecases.UseCaseDescription;
import de.uni_luebeck.inb.knowarc.usecases.invocation.InvocationException;
import de.uni_luebeck.inb.knowarc.usecases.invocation.OnDemandDownloadGridftp;
import de.uni_luebeck.inb.knowarc.usecases.invocation.UseCaseInvocation;

/**
 * The UseCase is brought to the grid
 */
public class GridUseCaseInvocation extends UseCaseInvocation {
	private final StringBuilder log;
	private final GridFtpConnection conn;
	private final GridInfosystem infosystem;
	private final ProgressDisplay progressDisplay;
	private Date timeStarted;

	private xRslDescription gridjob = new xRslDescription();
	private HashMap<String, Object> uploads = new HashMap<String, Object>();
	private HashMap<String, String> fileUploads = new HashMap<String, String>();

	/**
	 * Constructor. The Grid-invocation only needs the description of the use
	 * case as an argument.
	 * 
	 * @param desc
	 *            - description of the use case as retrieved from the online
	 *            repository.
	 * @throws Exception
	 */
	public GridUseCaseInvocation(GridInfosystem infosystem, UseCaseDescription desc) throws Exception {
		this.usecase = desc;
		this.infosystem = infosystem;
		progressDisplay = infosystem.getProgressDisplay();

		log = new StringBuilder();
		conn = infosystem.getQueuePreferDenyWithRE(log, usecase.getQueue_preferred(), usecase.getQueue_deny(), usecase.getREs());
		progressDisplay.log(1, "JobQueue: " + conn.getUrl());
		List<RuntimeEnvironment> REsAtTarget = infosystem.getREsForJobQueue(conn.getUrl());

		if (REsAtTarget != null) {
			progressDisplay.log(1, "REs available at target queue: ");
			for (RuntimeEnvironment runtimeEnvironment : REsAtTarget) {
				progressDisplay.log(1, "\t" + runtimeEnvironment.getID());
			}

			// resolve the needed usecases to the ones present on the target
			// queue
			for (RuntimeEnvironment wantRE : usecase.getREs()) {
				SortedSet<RuntimeEnvironment> REsAtTargetThatWouldWork = new TreeSet<RuntimeEnvironment>();
				for (RuntimeEnvironment haveRE : REsAtTarget) {
					if (haveRE.atLeastAsCapableAs(wantRE)) {
						REsAtTargetThatWouldWork.add(haveRE);
					}
				}
				if (REsAtTargetThatWouldWork.size() > 0) {
					RuntimeEnvironment reWithHighestVersion = REsAtTargetThatWouldWork.last();
					gridjob.lRE.add(reWithHighestVersion.getID());
				} else {
					progressDisplay.log(0, "WARNING: unresolved RE: " + wantRE.getID());
				}
			}

		} else {
			progressDisplay.log(0, "WARNING: we do not know which REs are available at our target queue");
			// just submit with the values in the script file
			for (RuntimeEnvironment r : usecase.getREs()) {
				gridjob.lRE.add(r.getID());
			}
		}

		gridjob.name = "Janitor Use-case (Taverna): " + usecase.getDescription();
		gridjob.mOutput2Url.put("stdout", "");
		gridjob.mOutput2Url.put("stderr", "");

		gridjob.gmlog = "gmlog";
		gridjob.mOutput2Url.put("gmlog", "");

	}

	@Override
	/*
	 * Some data is transferred to the remote site.
	 * 
	 * @param file - name of file
	 * 
	 * @param contents - byte array to be transferred
	 */
	public void putFile(String file, byte[] contents) {
		gridjob.mInput2Url.put(file, Integer.toString(contents.length));
		uploads.put(file, contents);
	}

	/**
	 * The data transferred is available at another URL.
	 * 
	 * @param file
	 *            - name under which the content shall appear
	 * @param source
	 *            - URL at which it is available now
	 */
	@Override
	public void putReference(String file, String source) {
		if (source.startsWith("file")) {
			try {
				URL fileUrl = new URL(source);
				String path = fileUrl.getPath();
				long len = new File(path).length();
				fileUploads.put(file, source);
				gridjob.mInput2Url.put(file, Long.toString(len));
			} catch (Exception e) {
				progressDisplay.log(0, "Error preparing upload of local file url: " + source);
				throw new NotImplementedException();
			}
		} else {
			gridjob.mInput2Url.put(file, source);
		}
	}

	/**
	 * Upon submission, a grid job is created.
	 */
	private GridJob jobhandle = null;

	@Override
	protected void submit_generate_job_inner() throws InvocationException {
		for (Map.Entry<String, ScriptOutput> cur : usecase.getOutputs().entrySet()) {
			String nopath = cur.getValue().getPath().replaceAll("/", "_");
			gridjob.mOutput2Url.put(cur.getValue().getPath(), nopath);
		}

		progressDisplay.log(3, "Preparing GridJob.");
		jobhandle = new GridJob(conn);

		tags.put("uniqueID", "" + getSubmissionID());

		// i dont know why, but condor fails horribly without a shebang line
		// and java fails without a startup script ^^
		String cmd = "#!/bin/sh\n" + usecase.getCommand() + "\n";
		for (String cur : tags.keySet()) {
			String newVal = tags.get(cur).toString();
			progressDisplay.log(2, "Substituting '" + cur + "' for '" + newVal + "'");
			cmd = cmd.replaceAll("\\Q%%" + cur + "%%\\E", newVal);
		}
		cmd += "\n" + "USECASEEXITCODE=$?" + "\n" + "echo \"Exit code: $USECASEEXITCODE\" " + "\n" + "exit $USECASEEXITCODE" + "\n";
		progressDisplay.log(2, cmd);

		String cmdName = "usecase-startup-script";
		try {
			putFile(cmdName, cmd.getBytes("US-ASCII"));
		} catch (UnsupportedEncodingException e) {
			throw new InvocationException(e);
		}
		gridjob.executable = cmdName;
		gridjob.maximumWalltimeInSeconds = usecase.getExecutionTimeoutInSeconds();
		gridjob.queue = conn.getQueueToSubmitTo();

		jobhandle.xrsl = gridjob.toString();

		for (String cur : tags.keySet()) {
			jobhandle.xrsl = jobhandle.xrsl.replaceAll("\\Q%%" + cur + "%%\\E", tags.get(cur));
		}
		progressDisplay.log(2, "xRSL: \n" + jobhandle.xrsl);
		progressDisplay.log(1, "target queue: " + conn.getUrl());
		try {
			jobhandle.Submit();
		} catch (ServerException e) {
			throw new InvocationException(e);
		} catch (ClientException e) {
			throw new InvocationException(e);
		} catch (IOException e) {
			throw new InvocationException(e);
		}
		String gridjobUrl = conn.getUrl() + "/" + jobhandle.jobid;
		progressDisplay.log(0, "jobid: " + gridjobUrl);
		infosystem.addHistory(gridjobUrl);

		for (String upload : uploads.keySet()) {
			byte[] str = (byte[]) uploads.get(upload);
			try {
				jobhandle.Input(upload, new ByteArrayInputStream(str));
			} catch (ServerException e) {
				throw new InvocationException(e);
			} catch (ClientException e) {
				throw new InvocationException(e);
			} catch (IOException e) {
				throw new InvocationException(e);
			}
		}

		for (String upload : fileUploads.keySet()) {
			try {
				jobhandle.InputLocal(upload, fileUploads.get(upload));
			} catch (ServerException e) {
				throw new InvocationException(e);
			} catch (ClientException e) {
				throw new InvocationException(e);
			} catch (IOException e) {
				throw new InvocationException(e);
			}
		}

		progressDisplay.log(1, "All uploads are finished for job " + jobhandle.jobid);

		// we want to free the space. let it be garbage collected :)
		uploads = null;
		fileUploads = null;
		gridjob = null;

		timeStarted = new Date();
	}

	@Override
	public HashMap<String, Object> submit_wait_fetch_results() throws InvocationException {
		boolean weArePreparing = true;
		boolean weAreRunning = false;

		/*
		 * let me try to explain wait logic here we have our first timeout,
		 * which is usecase.preparingTimeoutInSeconds and which is measured from
		 * the time WE finished uploading files to the first time the job gets
		 * into the queueing system (INLRMS:Q) this means the preparing timeout
		 * will fire if: a grid job stays in preparing or the queueing system
		 * does not accept our job fast enough
		 * 
		 * then once our job starts running (INLRMS:R) our time measurement is
		 * reset and we start our second timeout,
		 * usecase.executionTimeoutInSeconds which fires if: the grid job
		 * executes too long, the queueing system dies or looses the job or the
		 * grid manager dies in FINISHING
		 * 
		 * it's kinda sad that we need such a complex timeout and cancel logic,
		 * but all of the above failure cases have happened to me
		 */

		boolean firstRetry = true;
		int numberOfExceptionsToSwallow = 10;
		int numberOfPolls = 0;
		while (true) {
			try {
				String state = jobhandle.State();
				if (state.equals("FINISHED"))
					break;
				if (state.startsWith("INLRMS")) {
					weArePreparing = false;
					if (!weAreRunning && jobhandle.hasGramiFile()) {
						weAreRunning = true;
						timeStarted = new Date();
					}
				}
			} catch (Exception e) {
				numberOfExceptionsToSwallow--;
				if (numberOfExceptionsToSwallow < 0) {
					throw new InvocationException(e);
				}
				else {
					// since polling the job result failed,
					// which cannot be due to the job failing,
					// maybe we had a connection timout
					try {
						jobhandle.reconnect(infosystem.getCertificateData());
					} catch (ServerException e1) {
						throw new InvocationException(e1);
					} catch (GSSException e1) {
						throw new InvocationException(e1);
					} catch (IOException e1) {
						throw new InvocationException(e1);
					}
				}
			}
			if (++numberOfPolls > 3) {
				Date cur = new Date();
				int timeRunningInSeconds = (int) ((cur.getTime() - timeStarted.getTime()) / 1000);
				boolean timeout = false;
				if (weArePreparing && timeRunningInSeconds > usecase.getPreparingTimeoutInSeconds())
					timeout = true;
				if (weAreRunning && timeRunningInSeconds > usecase.getExecutionTimeoutInSeconds())
					timeout = true;
				if (timeout) {
					// we have reached a timeout
//					try {
						// kill grid job
						try {
							jobhandle.Kill();
						} catch (ServerException e) {
							throw new InvocationException(e);
						} catch (IOException e) {
							throw new InvocationException(e);
						}
//					} catch (Exception e) {
//						progressDisplay.log(2, "Error killing grid job which has reached timeout: " + jobhandle.jobid);
//						progressDisplay.logTrace(3, e);
//					}
					throw new InvocationException("Grid job has reached timeout of " + usecase.getExecutionTimeoutInSeconds() + "s");
				}
			}
			if (firstRetry) {
				// if this is the first error, maybe we just got a timeout while
				// submitting all the other jobs
				firstRetry = false;
			} else {
				progressDisplay.log(3, "Job has not yet finished, sleeping .. " + jobhandle.jobid);
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
				}
			}
		}

		progressDisplay.log(2, "Job has finished. " + jobhandle.jobid);

		OnDemandDownloadGridftp dlStdOut = new OnDemandDownloadGridftp(infosystem.getProgressDisplay(), infosystem.getCertificateData(), conn.getUrl(),
				jobhandle.jobid + "/stdout", false);
		OnDemandDownloadGridftp dlStdErr = new OnDemandDownloadGridftp(infosystem.getProgressDisplay(), infosystem.getCertificateData(), conn.getUrl(),
				jobhandle.jobid + "/stderr", false);

		String failed;
		try {
			failed = jobhandle.Failed();
		} catch (ClientException e1) {
			throw new InvocationException(e1);
		} catch (IOException e1) {
			throw new InvocationException(e1);
		}

		if (failed != null) {
			progressDisplay.log(0, "GridJob failed. " + jobhandle.jobid);

			String stdOut = "";
			String stdErr = "";

//			try {
				stdOut += dlStdOut.download();
//			} catch (Throwable e) {
//				stdOut += "Error downloading STDOUT: " + e.getMessage();
//			}
			try {
				stdErr += dlStdErr.download();
			} catch (Throwable e) {
				stdErr += "Error downloading STDOUT: " + e.getMessage();
			}

			throw new InvocationException("Grid job failed with: " + failed + "\n STDOUT: \n" + stdOut + "\n STDERR: \n" + stdErr);
		}

		progressDisplay.log(0, "JOB FINISHED! " + jobhandle.jobid + "\n");

		HashMap<String, Object> downloads = new HashMap<String, Object>();
		for (Map.Entry<String, ScriptOutput> cur : usecase.getOutputs().entrySet()) {
			String nopath = cur.getValue().getPath().replaceAll("/", "_");
			downloads.put(cur.getKey(), new OnDemandDownloadGridftp(infosystem.getProgressDisplay(), infosystem.getCertificateData(), conn.getUrl(),
					jobhandle.jobid + "/" + nopath, cur.getValue().isBinary()));
		}

		downloads.put("STDOUT", dlStdOut);
		downloads.put("STDERR", dlStdErr);

		return downloads;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_luebeck.janitor.usecase.UseCaseInvokation#Cleanup()
	 */
	@Override
	public void Cleanup() {
		if (conn != null)
			conn.Disconnect();
	}

	@Override
	public String setOneInput(ReferenceService referenceService,
			T2Reference t2Reference, ScriptInput input) {
		// TODO Auto-generated method stub
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
