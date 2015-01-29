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

import net.sf.taverna.t2.activities.externaltool.manager.InvocationMechanism;

public class SshNode {
	
    public static String DEFAULT_HOST = "127.0.0.1";
    public static int DEFAULT_PORT = 22;
    public static String DEFAULT_DIRECTORY = "/tmp/";

    private String host = DEFAULT_HOST;
    private int port = DEFAULT_PORT;
    private String directory = DEFAULT_DIRECTORY;
	
	private SshUrl url;
	
	private String linkCommand = null;
	private String copyCommand = null;
	private boolean retrieveData = false;

	/**
	 * 
	 */
	SshNode() {
		super();
		linkCommand = InvocationMechanism.UNIX_LINK;
		copyCommand = InvocationMechanism.UNIX_COPY;
		
	}
	/**
	 * @param directory the directory to set
	 */
	public void setDirectory(String directory) {
		if ((directory != null) && !directory.isEmpty()) {
			if (!directory.endsWith("/")) {
				directory = directory + "/";
			}
			this.directory = directory;
		}
	}

	/**
	 * @return the directory
	 */
	public String getDirectory() {
		return directory;
	}

	/**
	 * @param host the host to set
	 */
	public void setHost(String host) {
		this.host = host;
	}

	/**
	 * @return the host
	 */
	public String getHost() {
		return host;
	}

	/**
	 * @param port the port to set
	 */
	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * @return the port
	 */
	public int getPort() {
		return port;
	}
	
	SshUrl getUrl() {
		if (url == null) {
			url = new SshUrl(this);
		}
		return url;
	}
	
	public int hashCode() {
		return getUrl().hashCode();	
	}

	public boolean equals(Object obj) {
		if ((obj == null) || !(obj instanceof SshNode)) {
			return false;
		}
		return (this.hashCode() == obj.hashCode());
	}
	/**
	 * @return the linkCommand
	 */
	public String getLinkCommand() {
		return linkCommand;
	}
	/**
	 * @param linkCommand the linkCommand to set
	 */
	public void setLinkCommand(String linkCommand) {
		if ((linkCommand != null) && linkCommand.isEmpty()) {
			this.linkCommand = null;
		} else {
			this.linkCommand = linkCommand;
		}	}
	/**
	 * @return the copyCommand
	 */
	public String getCopyCommand() {
		return copyCommand;
	}
	/**
	 * @param copyCommand the copyCommand to set
	 */
	public void setCopyCommand(String copyCommand) {
		if ((copyCommand != null) && copyCommand.isEmpty()) {
			this.copyCommand = null;
		} else {
			this.copyCommand = copyCommand;
		}
	}
	
	/**
	 * @return the retrieveData
	 */
	public boolean isRetrieveData() {
		return retrieveData;
	}
	/**
	 * @param retrieveData the retrieveData to set
	 */
	public void setRetrieveData(boolean retrieveData) {
		this.retrieveData = retrieveData;
	}
	
}
