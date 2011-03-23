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
	
	
	private String name;

	public final String getXML() {
		Document document = new Document(getXMLElement());
		return outputter.outputString(document);
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
