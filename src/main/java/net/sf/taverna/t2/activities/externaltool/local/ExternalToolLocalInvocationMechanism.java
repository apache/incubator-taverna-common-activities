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

	@Override
	public String getType() {
		return LocalUseCaseInvocation.LOCAL_USE_CASE_INVOCATION_TYPE;
	}

	@Override
	public Element getXMLElement() {
		Element result = new Element("localInvocation");
		if (directory != null) {
			Element directoryElement = new Element("directory");
			directoryElement.addContent(new Text(directory));
			result.addContent(directoryElement);
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

}
