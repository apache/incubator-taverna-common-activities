/**
 * 
 */
package de.uni_luebeck.inb.knowarc.usecases.invocation.ssh;

import java.io.InputStream;

import net.sf.taverna.t2.activities.externaltool.RetrieveLoginFromTaverna;
import net.sf.taverna.t2.reference.AbstractExternalReference;
import net.sf.taverna.t2.reference.DereferenceException;
import net.sf.taverna.t2.reference.ExternalReferenceSPI;
import net.sf.taverna.t2.reference.ReferenceContext;
import net.sf.taverna.t2.reference.ReferencedDataNature;

import org.apache.log4j.Logger;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;

/**
 * @author alanrw
 *
 */
public class SshReference extends AbstractExternalReference implements
	ExternalReferenceSPI {
	
	private static Logger logger = Logger.getLogger(SshReference.class);

	
	private String host = "127.0.0.1";
	private int port = 22;
	private String directory = "/tmp/";
	private String subDirectory;
	private String fileName;
	
	private int dataNatureInteger = ReferencedDataNature.UNKNOWN.ordinal();
	private String charset = "UTF-8";
	
	public SshReference() {
		super();
	}
	
	public SshReference(SshUrl url) {
		super();
		this.host = url.getSshNode().getHost();
		this.port = url.getSshNode().getPort();
		this.directory = url.getSshNode().getDirectory();
		this.subDirectory = url.getSubDirectory();
		this.fileName = url.getFileName();
		this.setDataNature(url.getDataNature());
		this.setCharset(url.getCharset());
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.reference.ExternalReferenceSPI#getApproximateSizeInBytes()
	 */
	@Override
	public Long getApproximateSizeInBytes() {
		return 10000L;
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.reference.ExternalReferenceSPI#openStream(net.sf.taverna.t2.reference.ReferenceContext)
	 */
	@Override
	public InputStream openStream(ReferenceContext context)
			throws DereferenceException {
		try {
			SshNode node = SshNodeFactory.getInstance().getSshNode(this.getHost(), this.getPort(), this.getDirectory());
			String fullPath = getDirectory() +  getSubDirectory() + "/" + getFileName();
			ChannelSftp channel = SshPool.getSftpGetChannel(node, new RetrieveLoginFromTaverna(new SshUrl(node).toString()));
			logger.info("Opening stream on " + fullPath);
			return (channel.get(fullPath));
		} catch (JSchException e) {
			//TODO
			logger.error(e);
		} catch (SftpException e) {
			// TODO Auto-generated catch block
			logger.error(e);
		}
		return null;
	}

	/**
	 * @return the host
	 */
	public String getHost() {
		return host;
	}

	/**
	 * @param host the host to set
	 */
	public void setHost(String host) {
		this.host = host;
	}

	/**
	 * @return the port
	 */
	public int getPort() {
		return port;
	}

	/**
	 * @param port the port to set
	 */
	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * @return the directory
	 */
	public String getDirectory() {
		return directory;
	}

	/**
	 * @param directory the directory to set
	 */
	public void setDirectory(String directory) {
		this.directory = directory;
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
	
	public String getFullPath() {
		return getDirectory() + "/" + getSubDirectory() + "/" + getFileName();
	}
	
	public ReferencedDataNature getDataNature() {
		return ReferencedDataNature.values()[dataNatureInteger];
	}

	public void setDataNature(ReferencedDataNature dataNature) {
		this.dataNatureInteger = dataNature.ordinal();
	}

	public String getCharset() {
		return charset;
	}

	public void setCharset(String charset) {
		this.charset = charset;
	}

	/**
	 * @return the dataNatureInteger
	 */
	public int getDataNatureInteger() {
		return dataNatureInteger;
	}

	/**
	 * @param dataNatureInteger the dataNatureInteger to set
	 */
	public void setDataNatureInteger(int dataNatureInteger) {
		this.dataNatureInteger = dataNatureInteger;
	}



}
