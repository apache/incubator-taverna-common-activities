/**
 * 
 */
package net.sf.taverna.t2.activities.externaltool.manager;

import java.io.IOException;
import java.io.StringReader;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

/**
 * @author alanrw
 *
 */
public abstract class MechanismCreator {
	
	private static SAXBuilder builder = new SAXBuilder();

	private static Logger logger = Logger.getLogger(MechanismCreator.class);
	
	public abstract boolean canHandle(String mechanismType);
	
	public InvocationMechanism convert(String xml, String name) {
		
		Document document;
		try {
			synchronized (builder) {
				document = builder.build(new StringReader(xml));
			}
		} catch (JDOMException e1) {
			logger.error("Null invocation", e1);
			return null;
		} catch (IOException e1) {
			logger.error("Null invocation", e1);
			return null;
		}
		Element top = document.getRootElement();
		
		return convert(top, name);
	}

	public abstract InvocationMechanism convert(Element detailsElement,
			String mechanismName);

}
