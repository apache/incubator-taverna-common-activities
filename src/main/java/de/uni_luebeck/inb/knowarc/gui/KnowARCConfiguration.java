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

package de.uni_luebeck.inb.knowarc.gui;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFileChooser;

import org.apache.log4j.Logger;

import de.uni_luebeck.inb.knowarc.grid.GridInfosystem;
import de.uni_luebeck.inb.knowarc.grid.re.RuntimeEnvironment;
import de.uni_luebeck.inb.knowarc.grid.re.RuntimeEnvironmentConstraint;
import de.uni_luebeck.inb.knowarc.usecases.invocation.ssh.SshNode;
import de.uni_luebeck.inb.knowarc.usecases.invocation.ssh.SshUseCaseInvocation;

/**
 * Collector of parameters
 */
public class KnowARCConfiguration {

	private static Logger logger = Logger.getLogger(KnowARCConfiguration.class);

	public KnowARCConfiguration(File configurationPath) {
		this.configurationPath = configurationPath;
		info = new GridInfosystem(configurationPath.getAbsolutePath(), new ProgressDisplayImpl(this));
		try {
			ClassLoader classLoader = this.getClass().getClassLoader();
			icon_knowarc = new ImageIcon(classLoader.getResource("de/uni_luebeck/usecase/knowarc.png"));
			icon_server = new ImageIcon(classLoader.getResource("de/uni_luebeck/usecase/knowarc.png"));
		} catch (NullPointerException e) {
			logger.error(e);
		}
		readSshConfiguration();
		readInvocationMethod();
	}

	// file with grid certificate as created with grid-proxy-init
	public GridInfosystem info;
	// the debug level - 0 : don't show any non-error notes
	// 1 : describe the control flow - coarse grained
	// 2 : tell in detail what the system is doing
	// 3 : show internal data
	public byte debug = 3;

	private ImageIcon icon_knowarc;
	private ImageIcon icon_server;

	public ImageIcon getKnowARCImageIcon() {
		return icon_knowarc;
	}

	public ImageIcon getServerImageIcon() {
		return icon_server;
	}

	public ImageIcon getIcon() {
		return getServerImageIcon();
	}

	/**
	 * The debug level is a byte not an int, which is not auto-casted by javac
	 */
	public void debug(int debugThreshold, String message) {
		debug((byte) debugThreshold, message);
	}

	public void debug(byte debugThreshold, String message) {
		if (debugThreshold <= debug) {
			logger.error(message);
		}
	}

	public String chooseCertificate(JDialog frame) {
		JFileChooser fc;
		File tmpdir = new File("/tmp");
		if (tmpdir.exists() && tmpdir.isDirectory()) {
			fc = new JFileChooser(tmpdir);
		} else {
			fc = new JFileChooser();
		}
		fc.setAcceptAllFileFilterUsed(true);
		fc.setApproveButtonText("Load Certificate");
		if (fc.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION)

			info.setCertificateFile(fc.getSelectedFile());
		return getCertificateIdentity();
	}

	public String chooseKeyfile(JDialog frame) {
		JFileChooser fc = new JFileChooser();

		fc.setAcceptAllFileFilterUsed(true);
		fc.setApproveButtonText("Load SSH authorization file");
		if (fc.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION)

			return fc.getSelectedFile().getAbsolutePath();
		return "";
	}

	public String updateGridQueues() {
		info.Update();
		StringBuilder ret = new StringBuilder();
		ret.append("<html><head></head><body>");
		for (GridInfosystem.JobQueue cur : info.lJobQueues) {
			ret.append("<b>Job queue</b> " + cur.URL + "<br>RE available:<br>");
			ret.append("<ul>");
			for (RuntimeEnvironment re : cur.REs) {
				ret.append(re.getID() + "<br>");
			}
			ret.append("</ul>");
			ret.append("<br>Queues available:<br>");
			ret.append("<ul>");
			for (String q : cur.queues) {
				ret.append(q + "<br>");
			}
			ret.append("</ul>");
		}
		for (String cur : info.lStorageElements) {
			ret.append("<b>Storage element</b> " + cur + "<br>");
		}
		ret.append("</body></html>");
		return ret.toString();
	}

	public String getCAPath() {
		return new File(System.getProperty("user.home") + "/.globus/certificates").getAbsolutePath();
	}

	public String getCertificateIdentity() {

		if (info.getCertificateData() == null)
			return "NO CERTIFICATE LOADED";
		return info.getCertificateData().getIdentity();
	}

	public String invocationMethod = "grid";

	public void readInvocationMethod() {
		try {
			File config = new File(configurationPath, "usecase-method.conf");
			if (config.exists()) {
				BufferedReader read = new BufferedReader(new FileReader(config));
				invocationMethod = read.readLine();
				read.close();
			}
		} catch (Exception e) {
			info.getProgressDisplay().logTrace(0, e);
		}
	}

	public void saveInvocationMethod() {
		try {
			File config = new File(configurationPath, "usecase-method.conf");
			BufferedWriter write = new BufferedWriter(new FileWriter(config));
			write.write(invocationMethod + "\n");
			write.flush();
			write.close();
		} catch (Exception e) {
			info.getProgressDisplay().logTrace(0, e);
		}
	}

	public List<SshNode> sshWorkerNodes = new ArrayList<SshNode>();
	private int currentWorkerNode = 0;

	public synchronized SshNode getNextWorkerNode() {
		currentWorkerNode %= sshWorkerNodes.size();
		return sshWorkerNodes.get(currentWorkerNode++);
	}

	public File configurationPath;

	public void readSshConfiguration() {
		try {
			File config = new File(configurationPath, "usecase-ssh.conf");
			if (config.exists()) {
				BufferedReader read = new BufferedReader(new FileReader(config));
				String line;
				sshWorkerNodes.clear();
				while (null != (line = read.readLine())) {
					SshNode node = new SshNode();
					node.setHost(line);
					node.setPort(Integer.parseInt(read.readLine()));
					read.readLine();
					read.readLine();
					read.readLine();
					node.setDirectory(read.readLine());
					sshWorkerNodes.add(node);
				}
				read.close();
			}
		} catch (Exception e) {
			info.getProgressDisplay().logTrace(0, e);
		}

//		SshUseCaseInvocation.initLogger(info.getProgressDisplay());
	}

	public void saveSshConfiguration() {
		try {
			File config = new File(configurationPath, "usecase-ssh.conf");
			BufferedWriter write = new BufferedWriter(new FileWriter(config));
			for (SshNode cur : sshWorkerNodes) {
				write.write(cur.getHost() + "\n");
				write.write(cur.getPort() + "\n");
				write.write("user\n");
				write.write("pass\n");
				write.write("keyfile\n");
				write.write(cur.getDirectory() + "\n");
			}
			write.flush();
			write.close();
		} catch (Exception e) {
			info.getProgressDisplay().logTrace(0, e);
		}
	}

}
