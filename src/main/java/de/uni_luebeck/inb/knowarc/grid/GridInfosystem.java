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

package de.uni_luebeck.inb.knowarc.grid;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.apache.log4j.Logger;
import org.globus.ftp.exception.FTPException;
import org.globus.ftp.exception.ServerException;
import org.globus.gsi.GlobusCredential;
import org.globus.gsi.GlobusCredentialException;
import org.ietf.jgss.GSSException;

import de.uni_luebeck.inb.knowarc.grid.re.RuntimeEnvironment;
import de.uni_luebeck.inb.knowarc.grid.re.RuntimeEnvironmentConstraint;
import de.uni_luebeck.inb.knowarc.gui.KnowARCConfiguration;
import de.uni_luebeck.inb.knowarc.gui.KnowARCConfigurationDialog;
import de.uni_luebeck.inb.knowarc.usecases.invocation.InvocationException;

/**
 * The GridInfosystem is a resource with information on the availability of
 * Queues and RuntimeEnvironments.
 * 
 * @author Hajo Nils Krabbenhoeft some contribution by
 * @author Steffen Moeller
 */
public class GridInfosystem {
	
	private static Logger logger = Logger.getLogger(GridInfosystem.class);
	

	private final String configurationPath;
	private final ProgressDisplay progressDisplay;
	private File certificateFile;
	private GlobusCredential certificateData;
	public final ArrayList<String> lGIIS = new ArrayList<String>();

	public static class JobQueue {
		public String URL;
		public List<RuntimeEnvironment> REs;
		public List<String> queues;
	}

	public ArrayList<JobQueue> lJobQueues = new ArrayList<JobQueue>();
	public ArrayList<String> lStorageElements = new ArrayList<String>();

	public GridInfosystem(String configurationPath, ProgressDisplay progress) {
		this.configurationPath = configurationPath;
		this.progressDisplay = progress;

		// our default grid information systems
		lGIIS.add("ldap://index1.nordugrid.org:2135/mds-vo-name=NorduGrid,o=grid");
		lGIIS.add("ldap://index2.nordugrid.org:2135/mds-vo-name=NorduGrid,o=grid");
		lGIIS.add("ldap://index3.nordugrid.org:2135/mds-vo-name=NorduGrid,o=grid");
		lGIIS.add("ldap://index4.nordugrid.org:2135/mds-vo-name=NorduGrid,o=grid");

		lGIIS.add("ldap://knowarc1.grid.niif.hu:2135/mds-vo-name=local,o=grid");
		lGIIS.add("ldap://charged.uio.no:2135/mds-vo-name=local,o=grid");
		lGIIS.add("ldap://grid.tsl.uu.se:2135/mds-vo-name=local,o=grid");
		lGIIS.add("ldap://grid64inb.inb.uni-luebeck.de:2135/mds-vo-name=local,o=grid");
		lGIIS.add("ldap://pgs02.grid.upjs.sk:2135/mds-vo-name=local,o=grid");

		readConfiguration();
		readCache();
		readHistory();
	}

	/**
	 * All the LDAP servers in lGIIS are queried to learn about available queues
	 * and storage elements.
	 * 
	 * @return true if successful
	 */
	public boolean Update() {
		if (null == certificateData) {
			// THIS IS BAD
			// PleaseWaitDialog.WaitMessage msg =
			// PleaseWaitDialog.getMessage("Certificate passed to is invalid.");
			// because the message will never be removed, thus annoying the user
			// ^^
			return false;
		}

		// store settings
		saveConfiguration();

		// remove all job queues
		lJobQueues.clear();

		Object statusMessage = progressDisplay.getMessage("Collecting resources from grid info system");
		Hashtable<String, String> env = new Hashtable<String, String>();
		env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		for (String giisUrl : lGIIS) {
			try {
				progressDisplay.changeMessage(statusMessage, "Collecting from " + giisUrl);

				env.put(Context.PROVIDER_URL, giisUrl);
				DirContext ctx = new InitialDirContext(env);
				SearchControls controls = new SearchControls();
				controls.setCountLimit(0);
				controls.setTimeLimit(10000);
				controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
				String filter = "(|" + "(objectclass=nordugrid-cluster)" + "(objectclass=nordugrid-queue)" +

				"(&" + "(objectclass=nordugrid-se)" + "(nordugrid-se-authuser=" + certificateData.getIdentity() + ")" + ")" +

				"(&" + "(objectclass=nordugrid-authuser)" + "(nordugrid-authuser-sn=" + certificateData.getIdentity() + ")" + ")" + ")";

				// ctx.getNameInNamespace()
				NamingEnumeration<SearchResult> resultp = ctx.search("", filter, controls);
				Attributes current_cluster = null;
				Attributes current_queue = null;
				while (resultp.hasMore()) {
					SearchResult result = resultp.next();
					Attribute oclass = result.getAttributes().get("objectClass");
					if (oclass.contains("nordugrid-cluster")) {
						// store cluster in case we receive authuser
						current_cluster = result.getAttributes();
					} else if (oclass.contains("nordugrid-queue")) {
						// store cluster in case we receive authuser
						current_queue = result.getAttributes();
					} else if (oclass.contains("nordugrid-authuser")) {
						// seems like we're authorized to use cluster
						String cluster_url = (String) current_cluster.get("nordugrid-cluster-contactstring").get();
						String queue_name = (String) current_queue.get("nordugrid-queue-name").get();
						Attribute REAttrib = current_cluster.get("nordugrid-cluster-runtimeenvironment");
						List<RuntimeEnvironment> REs = new ArrayList<RuntimeEnvironment>();
						if (REAttrib != null) {
							NamingEnumeration<?> tmpREs = REAttrib.getAll();
							while (tmpREs.hasMore()) {
								RuntimeEnvironment r = new RuntimeEnvironment((String) tmpREs.next());
								REs.add(r);
							}
						}
						addJobQueue(cluster_url, REs, queue_name);
					} else if (oclass.contains("nordugrid-se")) {
						Attribute a = result.getAttributes().get("nordugrid-se-url");
						if (null == a) {
							progressDisplay.log(1, "No such attribute: nordugrid-se-url");
						} else {
							String path = (String) a.get();
							if (!path.contains("gacl")) {
								if (lStorageElements.contains(path)) {
									progressDisplay.log(2, "doublette: se " + path);
								} else {
									lStorageElements.add(path);
								}
							}
						}
					}
				}
			} catch (NamingException ex) {
				progressDisplay.log(0, "Problem with site " + giisUrl + ".");
				progressDisplay.logTrace(0, ex);
				logger.error(ex);
			} catch (NullPointerException ex) {
				progressDisplay.log(0, "Programming error that manifested itself with site " + giisUrl + ".");
				progressDisplay.logTrace(0, ex);
				progressDisplay.log(0, "This problem is most unlikely to affect your computations. Please ignore.");
			}
		}

		progressDisplay.changeMessage(statusMessage, "saving cache ...");
		saveCache();

		progressDisplay.removeMessage(statusMessage);
		return true;
	}

	/**
	 * A queue of interest is added to the internal Queue organisation. Their
	 * RuntimeEnvironemnts are also maintained.
	 */
	private void addJobQueue(String url, Collection<RuntimeEnvironment> REs, String queue) {
		for (JobQueue cur : lJobQueues) {
			if (cur.URL.equals(url)) {
				for (String cur2 : cur.queues) {
					if (cur2.equals(queue)) {
						progressDisplay.log(1, "doublette: URL " + url + " QUEUE " + queue);
						return;
					}
				}
				cur.queues.add(queue);
				return;
			}
		}
		JobQueue addme = new JobQueue();
		addme.URL = url;
		addme.REs = new ArrayList<RuntimeEnvironment>();
		if (REs != null) {
			addme.REs.addAll(REs);
		}
		addme.queues = new ArrayList<String>();
		addme.queues.add(queue);
		lJobQueues.add(addme);
	}

	/*
	 * get a list of all the runtime environments available at the job queue
	 * with the given url
	 */
	public List<RuntimeEnvironment> getREsForJobQueue(String url) {
		for (JobQueue cur : lJobQueues) {
			if (cur.URL.equals(url)) {
				return cur.REs;
			}
		}
		return null;
	}

	int nTryConnectRoundtrip = 0;

	/**
	 * From a list of hosts, sequentially all are tested for accessibility and
	 * the first one wins. This does not take the current load of that site into
	 * account. This brokerage probably deserves a class on its own.
	 */
	public GridFtpConnection tryConnectTo(List<String> compatibleQueues, StringBuilder log) {
		nTryConnectRoundtrip++;
		for (int i = 0; i < compatibleQueues.size(); i++) {
			int ind = (i + nTryConnectRoundtrip) % compatibleQueues.size();
			try {
				final String mergedUrl = compatibleQueues.get(ind);

				String url = mergedUrl;
				String queue = null;

				final int index = mergedUrl.lastIndexOf("###");
				if (index > 0) {
					url = mergedUrl.substring(0, index);
					queue = mergedUrl.substring(index + 3);
				}
				return new GridFtpConnection(progressDisplay, url, queue, certificateData);
			} catch (GSSException e) {
				logFtpConnectionException(e, log);
			} catch (ServerException e) {
				logFtpConnectionException(e, log);
			} catch (IOException e) {
				logFtpConnectionException(e, log);
			}
		}
		return null;
	}
	
	private void logFtpConnectionException(Exception e, StringBuilder log) {
		logger.error(e);
		if (log != null) {
			StringWriter wr = new StringWriter();
			logger.error(new PrintWriter(wr));
			log.append(wr.toString());
		}
		
	}

	/**
	 * A queue is seeked that offers all Runtime Environments. Of these, with
	 * the 'tryConnectTo' method the first is checked that allows the
	 * submission. While trying to connect to grid queues, every exception or
	 * error encountered is written to the StringBuilder log.
	 */
	public GridFtpConnection getQueuePreferDenyWithRE(StringBuilder log, ArrayList<String> lPreferred, ArrayList<String> lDenied,
			List<RuntimeEnvironmentConstraint> list) throws InvocationException {
		GridFtpConnection c = tryConnectTo(lPreferred, log);
		if (null != c) {
			// we have a connection already
			return c;
		}

		if (lJobQueues.size() == 0)
			throw new InvocationException(
					"You do not have access to any Job queues. Please configure the grid information systems to include at least one job queue that you are allowed to use.");

		List<String> compatibleQueues = getCompatibleQueuesForREs(list);

		if (compatibleQueues.size() == 0)
			throw new InvocationException("No eligible job queue has needed RTEs: " + log);
		compatibleQueues.removeAll(lDenied);
		if (compatibleQueues.size() == 0)
			throw new InvocationException("No eligible job queue has needed RTEs, except for ones denied by the use case: " + log);

		c = tryConnectTo(compatibleQueues, log);
		if (null == c) {
			throw new InvocationException("Could not connect to any job queue: " + log);
		}
		return c;
	}

	public ArrayList<String> getCompatibleQueuesForREs(List<RuntimeEnvironmentConstraint> list) {
		ArrayList<String> compatibleQueues = new ArrayList<String>();
		for (JobQueue cur : lJobQueues) {
			// flag indicating if the queue inspected is still worthy
			boolean currentQueueIsAcceptable = true;
			// iterator over runtime environments to check against - will become
			// list of constraints
			ListIterator<RuntimeEnvironmentConstraint> neededREsIterator = list.listIterator();
			// iterator over those runtime environments
			// inspecting all needed runtime environments
			while (currentQueueIsAcceptable && neededREsIterator.hasNext()) {
				RuntimeEnvironmentConstraint rc = neededREsIterator.next();
				currentQueueIsAcceptable = rc.isFulfilledByAtLeastOneIn(cur.REs);
				if (!currentQueueIsAcceptable) {
					progressDisplay.log(2, "Rejecting '" + cur.URL + "', which lacks RE '" + rc.toString() + "'");
				}
			}
			if (currentQueueIsAcceptable) {
				for (String curQ : cur.queues) {
					compatibleQueues.add(cur.URL + "###" + curQ);
				}
			}
		}
		return compatibleQueues;
	}

	/**
	 * An arbitrary storage element is selected
	 */
	public GridFtpConnection getStorage(StringBuilder log) {
		return tryConnectTo(lStorageElements, log);
	}

	public void setCertificateFile(File file) {
		this.certificateFile = file;
		this.certificateData = null;
		try {
			this.certificateData = new GlobusCredential(file.getAbsolutePath());
		} catch (Exception e) {
			progressDisplay.log(0, "Error loading certificate " + file.getAbsolutePath());
			progressDisplay.logTrace(1, e);
		}
	}

	public GlobusCredential getCertificateData() {
		return certificateData;
	}

	public ProgressDisplay getProgressDisplay() {
		return progressDisplay;
	}

	public void readConfiguration() {
		try {
			File config = new File(configurationPath + File.separator + "janitor-knowarc.conf");
			if (config.exists()) {
				BufferedReader read = new BufferedReader(new FileReader(config));
				certificateFile = new File(read.readLine());
				if (certificateFile.exists()) {
					certificateData = new GlobusCredential(certificateFile.getAbsolutePath());
					if (certificateData == null) {
						progressDisplay.annoyTheUser("Your proxy certificate file " + certificateFile.getAbsolutePath() + " was found but could not be read.");
					} else if (certificateData.getTimeLeft() < 10) {
						progressDisplay.annoyTheUser("Your proxy certificate file " + certificateFile.getAbsolutePath() + " has expired.");
					}
				} else {
					progressDisplay.annoyTheUser("Your proxy certificate file " + certificateFile.getAbsolutePath() + " could not be found.");
				}
				lGIIS.clear();
				String line;
				while (null != (line = read.readLine())) {
					lGIIS.add(line);
				}
				read.close();
			}
		} catch (IOException e) {
			progressDisplay.logTrace(0, e);
		} catch (GlobusCredentialException e) {
			progressDisplay.logTrace(0, e);			
		}
	}

	public void saveConfiguration() {
		try {
			File config = new File(configurationPath + File.separator + "janitor-knowarc.conf");
			BufferedWriter write = new BufferedWriter(new FileWriter(config));
			write.write(certificateFile.getAbsolutePath() + "\n");
			for (String cur : lGIIS) {
				write.write(cur + "\n");
			}
			write.flush();
			write.close();
		} catch (IOException e) {
			progressDisplay.logTrace(0, e);
		}
	}

	public void readCache() {
		try {
			File config = new File(configurationPath + File.separator + "janitor-knowarc.cache");
			if (config.exists()) {
				BufferedReader read = new BufferedReader(new FileReader(config));
				lJobQueues.clear();
				lStorageElements.clear();
				String line;
				JobQueue last = null;
				while (null != (line = read.readLine())) {
					char type = line.charAt(0);
					line = line.substring(1);
					if (type == 'J') {
						if (null != last)
							lJobQueues.add(last);
						last = new JobQueue();
						last.URL = line;
						last.REs = new ArrayList<RuntimeEnvironment>();
						last.queues = new ArrayList<String>();
					} else if (type == 'R') {
						last.REs.add(new RuntimeEnvironment(line));
					} else if (type == 'Q') {
						last.queues.add(line);
					} else if (type == 'S') {
						lStorageElements.add(line);
					}
				}
				if (null != last)
					lJobQueues.add(last);
				read.close();
			}
		} catch (IOException e) {
			progressDisplay.logTrace(0, e);
		}
	}

	public void saveCache() {
		try {
			File config = new File(configurationPath + File.separator + "janitor-knowarc.cache");
			BufferedWriter write = new BufferedWriter(new FileWriter(config));
			for (JobQueue cur : lJobQueues) {
				write.write("J" + cur.URL + "\n");
				for (RuntimeEnvironment curre : cur.REs) {
					write.write("R" + curre.getID() + "\n");
				}
				for (String curq : cur.queues) {
					write.write("Q" + curq + "\n");
				}
			}
			for (String cur : lStorageElements) {
				write.write("S" + cur + "\n");
			}
			write.flush();
			write.close();
		} catch (IOException e) {
			progressDisplay.logTrace(0, e);
		}
	}

	private final Set<String> gridJobHistory = new HashSet<String>();

	public void readHistory() {
		try {
			File config = new File(configurationPath + File.separator + "janitor-knowarc-history.txt");
			if (config.exists()) {
				BufferedReader read = new BufferedReader(new FileReader(config));
				String line = null;
				while ((line = read.readLine()) != null) {
					if (gridJobHistory.contains(line))
						continue;
					gridJobHistory.add(line);
				}
				read.close();
			}
		} catch (IOException e) {
			progressDisplay.logTrace(0, e);
		}
	}

	public void saveHistory() {
		try {
			File config = new File(configurationPath + File.separator + "janitor-knowarc-history.txt");
			BufferedWriter write = new BufferedWriter(new FileWriter(config));
			for (String cur : gridJobHistory) {
				write.write(cur + "\n");
			}
			write.flush();
			write.close();
		} catch (IOException e) {
			progressDisplay.logTrace(0, e);
		}
	}

	public synchronized String getHistory() {
		StringBuilder ret = new StringBuilder();
		for (String cur : gridJobHistory) {
			ret.append(cur + "\n");
		}
		return ret.toString();
	}

	public synchronized void addHistory(String gridJobUrl) {
		gridJobHistory.add(gridJobUrl);
		saveHistory();
	}

	/*
	 * simple test to verify that grid history cleaning works correctly and it
	 * does :) the folder is gone and the job status is set to DELETED i dont
	 * know, however, how to get them out of the infosystem ^^
	 */
	public synchronized void cleanHistory() {
		for (String cur : gridJobHistory) {
			String connurl = cur.substring(0, cur.lastIndexOf('/'));
			String folder = cur.substring(cur.lastIndexOf('/') + 1);
			try {
				GridFtpConnection conn = new GridFtpConnection(progressDisplay, connurl, null, certificateData);
				conn.deleteFolder(folder);
				conn.Disconnect();
			} catch (IOException e) {
				logCleanHistoryException(e, cur);
			} catch (FTPException e) {
				logCleanHistoryException(e, cur);
			} catch (GSSException e) {
				logCleanHistoryException(e, cur);
			}
		}
		gridJobHistory.clear();
	}
	
	private void logCleanHistoryException(Exception e, String cur) {
		progressDisplay.log(0, "Error cleaning grid job: " + cur);
		progressDisplay.logTrace(1, e);		
	}

	public static void main(String[] args) {
		KnowARCConfiguration conf = new KnowARCConfiguration(new File("/Volumes/Important/fxtentacle/Library/Application Support/taverna-2.1.2/"));
		new KnowARCConfigurationDialog(null, true, conf).setVisible(true);
	}
}
