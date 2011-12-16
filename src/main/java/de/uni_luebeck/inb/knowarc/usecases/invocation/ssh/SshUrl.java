/**
 * 
 */
package de.uni_luebeck.inb.knowarc.usecases.invocation.ssh;

import net.sf.taverna.t2.reference.ReferencedDataNature;

/**
 * @author alanrw
 *
 */
public class SshUrl {
	
	private SshNode sshNode;
	private String subDirectory;
	private String fileName;

	private ReferencedDataNature dataNature = ReferencedDataNature.UNKNOWN;
	private String charset = "UTF-8";
	

	public SshUrl(SshNode sshNode) {
		this.setSshNode(sshNode);
	}
	

	/**
	 * @return the host
	 */
	public String getHost() {
		return getSshNode().getHost();
	}
	/**
	 * @param host the host to set
	 */
	public void setHost(String host) {
		getSshNode().setHost(host);
	}
	/**
	 * @return the port
	 */
	public int getPort() {
		return getSshNode().getPort();
	}
	/**
	 * @param port the port to set
	 */
	public void setPort(int port) {
		getSshNode().setPort(port);
	}
	/**
	 * @return the directory
	 */
	public String getDirectory() {
		return getSshNode().getDirectory();
	}
	/**
	 * @param directory the directory to set
	 */
	public void setDirectory(String directory) {
		getSshNode().setDirectory(directory);
	}
	/**
	 * @return the subDirectory
	 */
	public String getSubDirectory() {
		return subDirectory;
	}
	/**
	 * @param subDirectory the subDirectory to set
	 */
	public void setSubDirectory(String subDirectory) {
		this.subDirectory = subDirectory;
	}
	/**
	 * @return the fileName
	 */
	public String getFileName() {
		return fileName;
	}
	/**
	 * @param fileName the fileName to set
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
	public String toString() {
		String result = SshNodeFactory.makeUrl(getHost(), getPort(), getDirectory());
		if (getSubDirectory() != null) {
			result += getSubDirectory();
		}
		if (getFileName() != null) {
			result += "/" + getFileName();
		}
		return result;
	}
	
	public int hashCode() {
		return toString().hashCode();
		
	}
	
	public boolean equals(Object obj) {
		if ((obj == null) || !(obj instanceof SshUrl)) {
			return false;
		}
		return (this.hashCode() == obj.hashCode());
	}
	
	public SshUrl getBaseUrl() {
		SshUrl result = new SshUrl(this.getSshNode());
		return result;
	}


	/**
	 * @return the sshNode
	 */
	public SshNode getSshNode() {
		return sshNode;
	}


	/**
	 * @param sshNode the sshNode to set
	 */
	public void setSshNode(SshNode sshNode) {
		this.sshNode = sshNode;
	}
	
	public ReferencedDataNature getDataNature() {
		return dataNature;
	}


	public void setDataNature(ReferencedDataNature dataNature) {
		this.dataNature = dataNature;
	}


	public String getCharset() {
		return charset;
	}


	public void setCharset(String charset) {
		this.charset = charset;
	}


	
}
