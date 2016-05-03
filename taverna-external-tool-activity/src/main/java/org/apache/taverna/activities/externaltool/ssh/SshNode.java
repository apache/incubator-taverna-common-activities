/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.taverna.activities.externaltool.ssh;

import org.apache.taverna.activities.externaltool.manager.InvocationMechanism;

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
