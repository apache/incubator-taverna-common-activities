/**
 * 
 */
package net.sf.taverna.t2.activities.externaltool.manager;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

/**
 * @author alanrw
 *
 */
public abstract class InvocationMechanism {
	
	protected static XMLOutputter outputter = new XMLOutputter();


	public static String UNIX_SHELL = "/bin/sh -c";

	public static String UNIX_LINK = "/bin/ln -s %%PATH_TO_ORIGINAL%% %%TARGET_NAME%%";
	
	public static String UNIX_COPY = "/bin/cp %%PATH_TO_ORIGINAL%% %%TARGET_NAME%%";
	
	
	private String name;

	public final String getXML() {
		Document document = new Document(getXMLElement());
		String result = null;
		synchronized (outputter) {
			result = outputter.outputString(document);
		}
		return result;
	}
	
	public abstract Element getXMLElement();

	public abstract String getType();

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	public String toString() {
		return getName();
	}

}
