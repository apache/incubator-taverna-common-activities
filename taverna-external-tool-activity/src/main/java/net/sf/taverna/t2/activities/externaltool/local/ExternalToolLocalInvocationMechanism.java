/**
 * 
 */
package net.sf.taverna.t2.activities.externaltool.local;

import net.sf.taverna.t2.activities.externaltool.manager.InvocationMechanism;

import org.jdom.Element;
import org.jdom.Text;

import de.uni_luebeck.inb.knowarc.usecases.invocation.local.LocalUseCaseInvocation;

/**
 * @author alanrw
 *
 */
public class ExternalToolLocalInvocationMechanism extends
		InvocationMechanism {
	
	private String directory;
	
	private String shellPrefix;
	
	private String linkCommand;
	
	private boolean retrieveData;
	
	/**
	 * 
	 */
	public ExternalToolLocalInvocationMechanism() {
		super();
		String os = System.getProperty("os.name");
		if (!os.startsWith("Windows")) {
			setShellPrefix(UNIX_SHELL);
			setLinkCommand(UNIX_LINK);
		}
	}

	@Override
	public String getType() {
		return LocalUseCaseInvocation.LOCAL_USE_CASE_INVOCATION_TYPE;
	}

	@Override
	public Element getXMLElement() {
		Element result = new Element("localInvocation");
		if ((directory != null) && !directory.isEmpty()){
			Element directoryElement = new Element("directory");
			directoryElement.addContent(new Text(directory));
			result.addContent(directoryElement);
		}
		if ((shellPrefix != null) && !shellPrefix.isEmpty()) {
			Element shellPrefixElement = new Element("shellPrefix");
			shellPrefixElement.addContent(new Text(shellPrefix));
			result.addContent(shellPrefixElement);
		}
		if ((linkCommand != null) && !linkCommand.isEmpty()) {
			Element linkCommandElement = new Element("linkCommand");
			linkCommandElement.addContent(new Text(linkCommand));
			result.addContent(linkCommandElement);
		}
		if (isRetrieveData()) {
			Element retrieveDataElement = new Element("retrieveData");
			result.addContent(retrieveDataElement);
		}
		return result;
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
	 * @return the shellPrefix
	 */
	public String getShellPrefix() {
		return shellPrefix;
	}

	/**
	 * @param shellPrefix the shellPrefix to set
	 */
	public void setShellPrefix(String shellPrefix) {
		this.shellPrefix = shellPrefix;
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
	    if ((linkCommand == null) || linkCommand.isEmpty()) {
			this.linkCommand = null;
		} else {
			this.linkCommand = linkCommand;
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
