/**
 * 
 */
package net.sf.taverna.t2.activities.externaltool;

import java.io.UnsupportedEncodingException;

import org.jdom.Element;
import org.jdom.output.XMLOutputter;

import de.uni_luebeck.inb.knowarc.usecases.UseCaseDescription;

/**
 * @author alanrw
 *
 */
public class AdHocExternalToolActivityConfigurationBean extends
		ExternalToolActivityConfigurationBean {
	
	private Element programNode;
	
	private String programNodeText = "";
	
	public AdHocExternalToolActivityConfigurationBean() {
		programNode = new Element("program");
		programNode.setAttribute("name", "");
		programNode.setAttribute("description", "");
		programNode.setAttribute("command", "");
	}
	
	public void setProgramNode(Element programNode) {
		this.programNode = programNode;
		System.err.println("Program node set");
		if (programNode != null) {
			XMLOutputter xo = new XMLOutputter();
			programNodeText = xo.outputString(programNode);
			System.err.println("ProgramNodeText set to " + programNodeText);
			try {
				useCaseDescription = new UseCaseDescription(programNode);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public Element getProgramNode() {
		return programNode;
	}
	
	public String getProgramText() {
		return programNodeText;
	}

	private UseCaseDescription useCaseDescription = null;

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.activities.externaltool.ExternalToolActivityConfigurationBean#getUseCaseDescription()
	 */
	@Override
	public UseCaseDescription getUseCaseDescription() {
		if (useCaseDescription == null) {
			if (programNode != null) {
				try {
					useCaseDescription = new UseCaseDescription(programNode);
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return useCaseDescription;
	}

}
