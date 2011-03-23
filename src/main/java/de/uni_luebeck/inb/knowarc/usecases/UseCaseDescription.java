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

package de.uni_luebeck.inb.knowarc.usecases;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import de.uni_luebeck.inb.knowarc.grid.re.RuntimeEnvironmentConstraint;


/**
 * Class representation of XML-description of UseCases
 */
public class UseCaseDescription {

	private static Logger logger = Logger.getLogger(UseCaseDescription.class);
	
	/**
	 * Identifier for the retrieval of this UseCase in the sharedRepository
	 * database, respectively its XML export.
	 */
	private String usecaseid;
	/**
	 * Workflow Elements now can get a group identifier. There may be subgroups
	 * divided by :
	 */
	private String group = "";
	/**
	 * Textual description of the use case itself. This description is very
	 * short to fit on the single line that is prepared for such descriptions in
	 * the Taverna workflow element list.
	 */
	private String description;
	/**
	 * What is actually executed on the shell.
	 */
	private String command;

	/**
	 * Accessor function of command
	 * 
	 * @return shell-executable series of commands
	 */
	public String getCommand() {
		if (null == command) {
			// FIXME: Is this possible?
			return "";
		} else {
			return command;
		}
	}

	/**
	 * Routine that may be executed as a first check if the program is indeed
	 * installed.
	 */
	private String test_local;
	
	/**
	 * URL of an icon that would help users to recognise the use case
	 */
	private String icon_url;

	private int preparingTimeoutInSeconds = 20 * 60; // 20 minutes
	private int executionTimeoutInSeconds = 30 * 60; // 30 minutes

	private List<String> tags = new ArrayList<String>();
	private List<RuntimeEnvironmentConstraint> REs = new ArrayList<RuntimeEnvironmentConstraint>();
	private ArrayList<String> queue_preferred = new ArrayList<String>();
	private ArrayList<String> queue_deny = new ArrayList<String>();

	private List<ScriptInputStatic> static_inputs = new ArrayList<ScriptInputStatic>();
	private Map<String, ScriptInput> inputs = new HashMap<String, ScriptInput>();
	private Map<String, ScriptOutput> outputs = new HashMap<String, ScriptOutput>();

	/**
	 * Constructor, for special purpose usecases.
	 * 
	 * @param usecaseid
	 */
	public UseCaseDescription(String usecaseid) {
		this.setUsecaseid(usecaseid);
	}

	/**
	 * Constructor, expects an input stream containing the xml. for example, use
	 * getClass().getClassLoader().getResourceAsStream("..") to load a usecase
	 * from your program jar
	 */
	public UseCaseDescription(InputStream programAsStream) throws Exception {
		SAXBuilder builder = new SAXBuilder();
		Document doc = builder.build(programAsStream);
		programAsStream.close();
		readFromXmlElement(doc.getRootElement());
	}

	/**
	 * Constructor, expects an XML-root to dissect.
	 */
	public UseCaseDescription(Element programNode) throws Exception {
		readFromXmlElement(programNode);
	}

	/**
	 * Specifies the UseCaseDescription from the root of an XML description
	 * which is accessible online.
	 * 
	 * @param programNode
	 * @throws Exception
	 */
	private void readFromXmlElement(Element programNode) throws Exception {
		if (programNode.getName().compareToIgnoreCase("program") != 0)
			throw new Exception("Expected <program>, read '" + programNode.getName() + "'");

		setUsecaseid(programNode.getAttributeValue("name"));
		setDescription(programNode.getAttributeValue("description"));
		setCommand(programNode.getAttributeValue("command"));
		String timeoutStr = programNode.getAttributeValue("timeout");
		if (timeoutStr != null)
			setExecutionTimeoutInSeconds(Integer.parseInt(timeoutStr));
		timeoutStr = programNode.getAttributeValue("preparing_timeout");
		if (timeoutStr != null)
			setPreparingTimeoutInSeconds(Integer.parseInt(timeoutStr));

		for (Object cur_ob : programNode.getChildren()) {
			Element cur = (Element) cur_ob;

			String name = cur.getAttributeValue("name");
			String type = cur.getName();
			boolean binary = false;
			if (null != cur.getAttributeValue("binary") && cur.getAttributeValue("binary").equalsIgnoreCase("true"))
				binary = true;
			boolean list = false;
			if (null != cur.getAttributeValue("list") && cur.getAttributeValue("list").equalsIgnoreCase("true"))
				list = true;
			boolean concatenate = false;
			if (null != cur.getAttributeValue("concatenate") && cur.getAttributeValue("concatenate").equalsIgnoreCase("true"))
				concatenate = true;

			Element inner = null;
			String innerType = null, tag = null, path = null;
			if (cur.getChildren().size() > 0) {
				inner = (Element) cur.getChildren().get(0);
				innerType = inner.getName();
				tag = inner.getAttributeValue("tag");
				path = inner.getAttributeValue("path");
			}
			// build mime type declaration list
			ArrayList<String> mime = new ArrayList<String>();
			for (Object child : cur.getChildren()) {
				Element curChild = (Element) child;
				if (curChild.getName().equalsIgnoreCase("mime")) {
					mime.add(curChild.getAttributeValue("type"));
				}
			}
			if (type.equalsIgnoreCase("static")) {
				ScriptInputStatic si = new ScriptInputStatic();
				Element content = cur.getChild("content");
				if (content == null)
					throw new Exception("FIXME: script tag without embedded content tag");
				si.setUrl(content.getAttributeValue("url"));
				if (si.getUrl() == null)
					si.setContent(content.getText());
				fillInputDescription(si, binary, innerType, tag, path);
				getStatic_inputs().add(si);
			} else if (type.equalsIgnoreCase("input")) {
				ScriptInputUser indesc = new ScriptInputUser();
				indesc.setList(list);
				indesc.setMime(mime);
				indesc.setConcatenate(concatenate);
				fillInputDescription(indesc, binary, innerType, tag, path);
				getInputs().put(name, indesc);
			} else if (type.equalsIgnoreCase("output")) {
				ScriptOutput outdesc = new ScriptOutput();
				outdesc.setMime(mime);

				boolean ok = true;
				if (null == innerType) {
					// don't know what to do
					throw new Exception("FIXME: Found null == innerType for output, is this the bug?");
				} else if (innerType.equalsIgnoreCase("fromfile")) {
					outdesc.setPath(path);
					outdesc.setBinary(binary);
				} else {
					throw new Exception("Problem reading output port: unknown innerType '" + innerType + "'");
				}
				if (ok) {
					getOutputs().put(name, outdesc);
				}
			} else if (type.equalsIgnoreCase("rte") || type.equalsIgnoreCase("re")) {
				getREs().add(new RuntimeEnvironmentConstraint(name, cur.getAttributeValue("relation")));
			} else if (type.equalsIgnoreCase("group")) {
				group = name;
			} else if (type.equalsIgnoreCase("test")) {
				test_local = cur.getAttributeValue("local");
			} else if (type.equalsIgnoreCase("icon")) {
				icon_url = cur.getAttributeValue("url");
			} else if (type.equalsIgnoreCase("queue")) {
				for (Object child_ob : cur.getChildren()) {
					Element child = (Element) child_ob;
					if (child.getName().equalsIgnoreCase("prefer"))
						getQueue_preferred().add(child.getAttributeValue("url"));
					else if (child.getName().equalsIgnoreCase("deny"))
						getQueue_deny().add(child.getAttributeValue("url"));
					else
						throw new Exception("Error while reading usecase " + this.getUsecaseid() + ": invalid queue entry");
				}
			} else if (type.equalsIgnoreCase("command")) {
				// i like to have the ability to inject complete shell script
				// fragments into the use case,
				// so this should be replace and should allow multiple lines
				if (getCommand() != null)
					throw new Exception("You have specified both command attribute and command tag.");
				setCommand(cur.getText());
			} else {
				throw new Exception("Unexpected and uninterpreted attribute " + type);
			}
		}
	}

	private void fillInputDescription(ScriptInput fillMe, boolean binary, String innerType, String tag, String path) throws Exception {
		fillMe.setBinary(binary);
		if (null == innerType) {
			// don't know what to do
			throw new Exception("FIXME: Found null == innerType for input, is this the bug?");
		} else if (innerType.equalsIgnoreCase("replace")) {
			fillMe.setTag(tag);
			fillMe.setTempFile(false);
			fillMe.setFile(false);
			getTags().add(tag);
		} else if (innerType.equalsIgnoreCase("tempfile")) {
			fillMe.setTag(tag);
			fillMe.setTempFile(true);
			fillMe.setFile(false);
			getTags().add(tag);
		} else if (innerType.equalsIgnoreCase("file")) {
			fillMe.setTag(path);
			fillMe.setTempFile(false);
			fillMe.setFile(true);
		} else {
			throw new Exception("Problem reading input port: unknown innerType '" + innerType + "'");
		}
	}

	/**
	 * returns icon that is referenced in use case description
	 */
	public ImageIcon getImageIcon() {
		if (null == icon_url) return null;
		try {
			URL u = new URL(icon_url);
			return new ImageIcon(u, getUsecaseid());
		} catch (Exception e) {
			logger.error(e);
			return null;
		}
	}

	/**
	 * String representation of the use case. It also contains interesting
	 * information on the availability of resources in the grid to actually
	 * execute that workflow element.
	 * 
	 * @return String
	 */
	@Override
	public String toString() {
		List<String> hlp = new ArrayList<String>();
		hlp.add("usecaseid: " + getUsecaseid());
		hlp.add("description: " + getDescription());
		hlp.add("group: " + group);
		hlp.add("test: " + test_local);
		hlp.add("tags: " + getTags());
		for (Map.Entry<String, ScriptInput> cur : getInputs().entrySet()) {
			hlp.add(">" + cur.getKey() + ">: " + cur.getValue().toString());
		}
		for (Map.Entry<String, ScriptOutput> cur : getOutputs().entrySet()) {
			hlp.add("<" + cur.getKey() + "<: " + cur.getValue().toString());
		}
		hlp.add("RE: " + getREs().toString());
		hlp.add("preferred queues: " + getQueue_preferred());
		hlp.add("denied queues: " + getQueue_deny());
		String tos = super.toString() + "[";
		for (int i = 0; i < hlp.size(); i++) {
			if (i != 0)
				tos += ", ";
			tos += hlp.get(i);
		}
		return tos + " ]";
	}

	/**
	 * hajo's test just pass an url or file url to an xml file
	 * 
	 * @throws Exception
	 * @throws IOException
	 * @throws MalformedURLException
	 */
	public static void main(String[] argv) throws MalformedURLException, IOException, Exception {
		UseCaseDescription d = new UseCaseDescription(new URL(argv[0]).openStream());
		logger.info(d.getCommand());
	}

	/**
	 * @param command the command to set
	 */
	public void setCommand(String command) {
		this.command = command;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param executionTimeoutInSeconds the executionTimeoutInSeconds to set
	 */
	public void setExecutionTimeoutInSeconds(int executionTimeoutInSeconds) {
		this.executionTimeoutInSeconds = executionTimeoutInSeconds;
	}

	/**
	 * @return the executionTimeoutInSeconds
	 */
	public int getExecutionTimeoutInSeconds() {
		return executionTimeoutInSeconds;
	}

	/**
	 * @param inputs the inputs to set
	 */
	public void setInputs(Map<String, ScriptInput> inputs) {
		this.inputs = inputs;
	}

	/**
	 * @return the inputs
	 */
	public Map<String, ScriptInput> getInputs() {
		return inputs;
	}

	/**
	 * @param outputs the outputs to set
	 */
	public void setOutputs(Map<String, ScriptOutput> outputs) {
		this.outputs = outputs;
	}

	/**
	 * @return the outputs
	 */
	public Map<String, ScriptOutput> getOutputs() {
		return outputs;
	}

	/**
	 * @param preparingTimeoutInSeconds the preparingTimeoutInSeconds to set
	 */
	public void setPreparingTimeoutInSeconds(int preparingTimeoutInSeconds) {
		this.preparingTimeoutInSeconds = preparingTimeoutInSeconds;
	}

	/**
	 * @return the preparingTimeoutInSeconds
	 */
	public int getPreparingTimeoutInSeconds() {
		return preparingTimeoutInSeconds;
	}

	/**
	 * @param queue_deny the queue_deny to set
	 */
	public void setQueue_deny(ArrayList<String> queue_deny) {
		this.queue_deny = queue_deny;
	}

	/**
	 * @return the queue_deny
	 */
	public ArrayList<String> getQueue_deny() {
		return queue_deny;
	}

	/**
	 * @param queue_preferred the queue_preferred to set
	 */
	public void setQueue_preferred(ArrayList<String> queue_preferred) {
		this.queue_preferred = queue_preferred;
	}

	/**
	 * @return the queue_preferred
	 */
	public ArrayList<String> getQueue_preferred() {
		return queue_preferred;
	}

	/**
	 * @param rEs the rEs to set
	 */
	public void setREs(List<RuntimeEnvironmentConstraint> rEs) {
		REs = rEs;
	}

	/**
	 * @return the rEs
	 */
	public List<RuntimeEnvironmentConstraint> getREs() {
		return REs;
	}

	/**
	 * @param static_inputs the static_inputs to set
	 */
	public void setStatic_inputs(List<ScriptInputStatic> static_inputs) {
		this.static_inputs = static_inputs;
	}

	/**
	 * @return the static_inputs
	 */
	public List<ScriptInputStatic> getStatic_inputs() {
		return static_inputs;
	}

	/**
	 * @param tags the tags to set
	 */
	public void setTags(List<String> tags) {
		this.tags = tags;
	}

	/**
	 * @return the tags
	 */
	public List<String> getTags() {
		return tags;
	}

	/**
	 * @param usecaseid the usecaseid to set
	 */
	public void setUsecaseid(String usecaseid) {
		this.usecaseid = usecaseid;
	}

	/**
	 * @return the usecaseid
	 */
	public String getUsecaseid() {
		return usecaseid;
	}
}
