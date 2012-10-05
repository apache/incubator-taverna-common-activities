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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.ImageIcon;

import net.sf.taverna.t2.activities.externaltool.ExternalToolActivity;
import net.sf.taverna.t2.workflowmodel.processor.config.ConfigurationBean;
import net.sf.taverna.t2.workflowmodel.processor.config.ConfigurationProperty;
import net.sf.taverna.t2.workflowmodel.processor.config.ConfigurationProperty.OrderPolicy;
import net.sf.taverna.t2.workflowmodel.serialization.DeserializationException;
import net.sf.taverna.t2.workflowmodel.utils.Tools;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import org.apache.commons.lang.StringUtils;

/**
 * Class representation of XML-description of UseCases
 */
@ConfigurationBean(uri = ExternalToolActivity.URI + "#ToolDescription")
public class UseCaseDescription {

	private static Logger logger = Logger.getLogger(UseCaseDescription.class);

	/**
	 * Identifier for the retrieval of this UseCase in the sharedRepository
	 * database, respectively its XML export.
	 */
	private String usecaseid = "";
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
	private String description = "";
	/**
	 * What is actually executed on the shell.
	 */
	private String command = "";

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
	private String test_local = null;

	/**
	 * URL of an icon that would help users to recognise the use case
	 */
	private String icon_url = null;

	private int preparingTimeoutInSeconds = 20 * 60; // 20 minutes
	private int executionTimeoutInSeconds = 30 * 60; // 30 minutes

	private List<String> tags = new ArrayList<String>();
	private List<RuntimeEnvironmentConstraint> REs = new ArrayList<RuntimeEnvironmentConstraint>();
	private ArrayList<String> queue_preferred = new ArrayList<String>();
	private ArrayList<String> queue_deny = new ArrayList<String>();

	private List<ScriptInputStatic> static_inputs = new ArrayList<ScriptInputStatic>();
	private Map<String, ScriptInput> inputs = new HashMap<String, ScriptInput>();
	private Map<String, ScriptOutput> outputs = new HashMap<String, ScriptOutput>();

	private boolean includeStdIn = false;
	private boolean includeStdOut = true;
	private boolean includeStdErr = true;

	private List<Integer> validReturnCodes = new ArrayList<Integer>();
	
	/**
	 * Default constructor to make xstream happy
	 */
	public UseCaseDescription() {
		
	}

	public UseCaseDescription() {
	}

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
	public UseCaseDescription(InputStream programAsStream) throws DeserializationException {
		SAXBuilder builder = new SAXBuilder();
		Document doc;
		try {
			doc = builder.build(programAsStream);
			programAsStream.close();
		} catch (JDOMException e) {
			throw new DeserializationException("Error deserializing usecase", e);
		} catch (IOException e) {
			throw new DeserializationException("Error deserializing usecase", e);
		}
		readFromXmlElement(doc.getRootElement());
	}

	/**
	 * Constructor, expects an XML-root to dissect.
	 */
	public UseCaseDescription(Element programNode) throws DeserializationException {
		readFromXmlElement(programNode);
	}

/**
 * Produce an XML description of the UseCaseDescription
 */
	public Element writeToXMLElement() {
		Element programNode = new Element("program");
		programNode.setAttribute("name", getUsecaseid());
		programNode.setAttribute("description", getDescription());
		// Always use element version of command
//		programNode.setAttribute("command", getCommand());
		Element commandNode = new Element("command");
		commandNode.addContent(getCommand());
		programNode.addContent(commandNode);
		programNode.setAttribute("timeout", Integer.toString(getExecutionTimeoutInSeconds()));
		programNode.setAttribute("preparing_timeout", Integer.toString(getPreparingTimeoutInSeconds()));
		programNode.setAttribute("includeStdIn", Boolean.toString(isIncludeStdIn()));
		programNode.setAttribute("includeStdOut", Boolean.toString(isIncludeStdOut()));
		programNode.setAttribute("includeStdErr", Boolean.toString(isIncludeStdErr()));
		for (ScriptInputStatic si : getStatic_inputs()) {
			Element staticNode = new Element("static");
			if (si.isBinary()) {
				staticNode.setAttribute("binary", "true");
			}
			if (si.isForceCopy()) {
				staticNode.setAttribute("forceCopy", "true");
			}
			if (si.isFile()) {
				Element fileNode = new Element("file");
				fileNode.setAttribute("path", si.getTag());
				staticNode.addContent(fileNode);
			} else if (si.isTempFile()) {
				Element tempfileNode = new Element("tempfile");
				tempfileNode.setAttribute("tag", si.getTag());
				staticNode.addContent(tempfileNode);
			} else {
				Element replaceNode = new Element("replace");
				replaceNode.setAttribute("tag", si.getTag());
				staticNode.addContent(replaceNode);
			}
			if (si.getUrl() != null) {
				Element contentNode = new Element("content");
				contentNode.setAttribute("url", si.getUrl());
				staticNode.addContent(contentNode);
			} else {
				Element contentNode = new Element("content");
				contentNode.addContent((String) si.getContent());
				staticNode.addContent(contentNode);
			}
			programNode.addContent(staticNode);
		}
		for (Entry<String, ScriptInput> entry : getInputs().entrySet()) {
			String name = entry.getKey();
			ScriptInputUser si = (ScriptInputUser) entry.getValue();
			Element inputNode = new Element("input");
			inputNode.setAttribute("name", name);
			if (si.isBinary()) {
				inputNode.setAttribute("binary", "true");
			}
			if (si.isForceCopy()) {
				inputNode.setAttribute("forceCopy", "true");
			}
			if (si.isConcatenate()) {
				inputNode.setAttribute("concatenate", "true");
			}
			if (si.isList()) {
				inputNode.setAttribute("list", "true");
			}
			if (si.isFile()) {
				Element fileNode = new Element("file");
				fileNode.setAttribute("path", si.getTag());
				inputNode.addContent(fileNode);
			} else if (si.isTempFile()) {
				Element tempfileNode = new Element("tempfile");
				tempfileNode.setAttribute("tag", si.getTag());
				inputNode.addContent(tempfileNode);
			} else {
				Element replaceNode = new Element("replace");
				replaceNode.setAttribute("tag", si.getTag());
				inputNode.addContent(replaceNode);
			}
			for (String mime : si.getMime()) {
				Element mimeNode = new Element("mime");
				mimeNode.setAttribute("type", mime);
				inputNode.addContent(mimeNode);
			}
			programNode.addContent(inputNode);
		}
		for (Entry<String, ScriptOutput> entry : getOutputs().entrySet()) {
			String name = entry.getKey();
			ScriptOutput so = entry.getValue();
			Element outputNode = new Element("output");
			outputNode.setAttribute("name", name);
			if (so.isBinary()) {
				outputNode.setAttribute("binary", "true");
			}
			Element fromfileNode = new Element("fromfile");
			fromfileNode.setAttribute("path", so.getPath());
			outputNode.addContent(fromfileNode);
			for (String mime : so.getMime()) {
				Element mimeNode = new Element("mime");
				mimeNode.setAttribute("type", mime);
				outputNode.addContent(mimeNode);
			}
			programNode.addContent(outputNode);
		}
		for (RuntimeEnvironmentConstraint rec : getREs()) {
			Element rteNode = new Element("rte");
			rteNode.setAttribute("name", rec.getID());
			rteNode.setAttribute("relation", rec.getRelation());
			programNode.addContent(rteNode);
		}
		if ((group != null) && !group.isEmpty()) {
			Element groupNode = new Element("group");
			groupNode.setAttribute("name", group);
			programNode.addContent(groupNode);
		}
		if ((test_local != null) && !test_local.isEmpty()) {
			Element testNode = new Element("test");
			testNode.setAttribute("local", test_local);
			programNode.addContent(testNode);
		}
		if ((icon_url != null) && !icon_url.isEmpty()) {
			Element iconNode = new Element("icon");
			iconNode.setAttribute("url", icon_url);
			programNode.addContent(iconNode);
		}
		if (!getQueue_preferred().isEmpty() || !getQueue_deny().isEmpty()) {
			Element queueNode = new Element("queue");
			for (String url : getQueue_preferred()) {
				Element preferredNode = new Element("prefer");
				preferredNode.setAttribute("url", url);
				queueNode.addContent(preferredNode);
			}
			for (String url : getQueue_deny()) {
				Element denyNode = new Element("deny");
				denyNode.setAttribute("url", url);
				queueNode.addContent(denyNode);
			}
			programNode.addContent(queueNode);
		}
			Element validReturnCodesNode = new Element("validReturnCodes");
			validReturnCodesNode.setAttribute("codes", getReturnCodesAsText());
			programNode.addContent(validReturnCodesNode);

		return programNode;
	}
	/**
	 * Specifies the UseCaseDescription from the root of an XML description
	 * which is accessible online.
	 *
	 * @param programNode
	 * @throws DeserializationException
	 */
	private void readFromXmlElement(Element programNode) throws DeserializationException {
		if (programNode.getName().compareToIgnoreCase("program") != 0)
			throw new DeserializationException("Expected <program>, read '" + programNode.getName() + "'");

		setUsecaseid(programNode.getAttributeValue("name"));
		setDescription(programNode.getAttributeValue("description"));
		setCommand(programNode.getAttributeValue("command"));
		String timeoutStr = programNode.getAttributeValue("timeout");
		if (timeoutStr != null)
			setExecutionTimeoutInSeconds(Integer.parseInt(timeoutStr));
		timeoutStr = programNode.getAttributeValue("preparing_timeout");
		if (timeoutStr != null)
			setPreparingTimeoutInSeconds(Integer.parseInt(timeoutStr));

		String includeStdInStr = programNode.getAttributeValue("includeStdIn");
		if (includeStdInStr != null && !includeStdInStr.isEmpty()) {
			setIncludeStdIn(includeStdInStr.equals("true"));
		}

		String includeStdOutStr = programNode.getAttributeValue("includeStdOut");
		if (includeStdOutStr != null && !includeStdOutStr.isEmpty()) {
			setIncludeStdOut(includeStdOutStr.equals("true"));
		}

		String includeStdErrStr = programNode.getAttributeValue("includeStdErr");
		if (includeStdErrStr != null && !includeStdErrStr.isEmpty()) {
			setIncludeStdErr(includeStdErrStr.equals("true"));
		}

		for (Object cur_ob : programNode.getChildren()) {
			Element cur = (Element) cur_ob;

			String name = cur.getAttributeValue("name");

			String type = cur.getName();
			boolean binary = false;
			if (null != cur.getAttributeValue("binary") && cur.getAttributeValue("binary").equalsIgnoreCase("true")) {
				binary = true;
			}
			boolean list = false;
			if (null != cur.getAttributeValue("list") && cur.getAttributeValue("list").equalsIgnoreCase("true")) {
				list = true;
			}
			boolean concatenate = false;
			if (null != cur.getAttributeValue("concatenate") && cur.getAttributeValue("concatenate").equalsIgnoreCase("true")) {
				concatenate = true;
			}
			boolean forceCopy = false;
			if (null != cur.getAttributeValue("forceCopy") && cur.getAttributeValue("forceCopy").equalsIgnoreCase("true")) {
				forceCopy = true;
			}

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
					throw new DeserializationException("FIXME: script tag without embedded content tag");
				si.setUrl(content.getAttributeValue("url"));
				if (si.getUrl() == null)
					si.setContent(content.getText());
				fillInputDescription(si, binary, forceCopy, innerType, tag, path);
				getStatic_inputs().add(si);
			} else if (type.equalsIgnoreCase("input")) {
				ScriptInputUser indesc = new ScriptInputUser();
				indesc.setList(list);
				indesc.setMime(mime);
				indesc.setConcatenate(concatenate);
				fillInputDescription(indesc, binary, forceCopy, innerType, tag, path);
				getInputs().put(Tools.sanitiseName(name), indesc);
			} else if (type.equalsIgnoreCase("output")) {
				ScriptOutput outdesc = new ScriptOutput();
				outdesc.setMime(mime);

				boolean ok = true;
				if (null == innerType) {
					// don't know what to do
					throw new DeserializationException("FIXME: Found null == innerType for output, is this the bug?");
				} else if (innerType.equalsIgnoreCase("fromfile")) {
					outdesc.setPath(path);
					outdesc.setBinary(binary);
				} else {
					throw new DeserializationException("Problem reading output port: unknown innerType '" + innerType + "'");
				}
				if (ok) {
					getOutputs().put(Tools.sanitiseName(name), outdesc);
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
						throw new DeserializationException("Error while reading usecase " + this.getUsecaseid() + ": invalid queue entry");
				}
			} else if (type.equalsIgnoreCase("command")) {
				// i like to have the ability to inject complete shell script
				// fragments into the use case,
				// so this should be replace and should allow multiple lines
				if ((getCommand() != null) && !getCommand().isEmpty()) {
					throw new DeserializationException("You have specified both command attribute and command tag.");
				}
				setCommand(cur.getText());
			} else if (type.equalsIgnoreCase("validReturnCodes")) {
					String codeString = cur.getAttributeValue("codes");
					if (codeString != null) {
						setReturnCodesAsText(codeString);
					}
			}
			else {
				throw new DeserializationException("Unexpected and uninterpreted attribute " + type);
			}
		}
	}

	private void fillInputDescription(ScriptInput fillMe, boolean binary, boolean forceCopy, String innerType, String tag, String path) throws DeserializationException {
		fillMe.setBinary(binary);
		fillMe.setForceCopy(forceCopy);
		if (null == innerType) {
			// don't know what to do
			throw new DeserializationException("FIXME: Found null == innerType for input, is this the bug?");
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
			throw new DeserializationException("Problem reading input port: unknown innerType '" + innerType + "'");
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
	 * @throws IOException
	 * @throws MalformedURLException
	 * @throws DeserializationException
	 */
	public static void main(String[] argv) throws MalformedURLException, IOException, DeserializationException {
		UseCaseDescription d = new UseCaseDescription(new URL(argv[0]).openStream());
		logger.info(d.getCommand());
	}

	/**
	 * @param command the command to set
	 */
	@ConfigurationProperty(name = "command", label = "Command", description="What is actually executed on the shell")
	public void setCommand(String command) {
		this.command = command;
	}

	/**
	 * @param description the description to set
	 */
	@ConfigurationProperty(name = "description", label = "Description", description="Textual description of the tool", required=false, uri="http://purl.org/dc/elements/1.1/description")
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
	@ConfigurationProperty(name = "executionTimeoutInSeconds", label = "Execution Timeout In Seconds")
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

	@ConfigurationProperty(name = "inputs", label = "Inputs", required=false)
	public void setInputs(Set<InputMap> inputs) {
		if (inputs != null) {
			this.inputs = new HashMap<String, ScriptInput>();
			for (InputMap inputMap : inputs) {
				this.inputs.put(inputMap.getPort(), inputMap.getInput());
			}
		} else {
			this.inputs = null;
		}
	}

	/**
	 * @return the inputs
	 */
	public Map<String, ScriptInput> getInputs() {
		if (inputs == null) {
			inputs = new HashMap<String, ScriptInput>();
		}
		return inputs;
	}

	/**
	 * @param outputs the outputs to set
	 */
	public void setOutputs(Map<String, ScriptOutput> outputs) {
		this.outputs = outputs;
	}

	@ConfigurationProperty(name = "outputs", label = "Outputs", required=false)
	public void setOutputs(Set<OutputMap> outputs) {
		if (outputs != null) {
			this.outputs = new HashMap<String, ScriptOutput>();
			for (OutputMap outputMap : outputs) {
				this.outputs.put(outputMap.getPort(), outputMap.getOutput());
			}
		} else {
			this.outputs = null;
		}
	}

	/**
	 * @return the outputs
	 */
	public Map<String, ScriptOutput> getOutputs() {
		if (outputs == null) {
			outputs = new HashMap<String, ScriptOutput>();
		}
		return outputs;
	}

	/**
	 * @param preparingTimeoutInSeconds the preparingTimeoutInSeconds to set
	 */
	@ConfigurationProperty(name = "preparingTimeoutInSeconds", label = "Preparing Timeout In Seconds")
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
		if (queue_deny == null) {
			queue_deny = new ArrayList<String>();
		}
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
		if (queue_preferred == null) {
			queue_preferred = new ArrayList<String>();
		}
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
		if (REs == null) {
			REs = new ArrayList<RuntimeEnvironmentConstraint>();
		}
		return REs;
	}

	/**
	 * @param static_inputs the static_inputs to set
	 */
	@ConfigurationProperty(name = "staticInputs", label = "Static Inputs", ordering=OrderPolicy.NON_ORDERED, required=false)
	public void setStatic_inputs(List<ScriptInputStatic> static_inputs) {
		this.static_inputs = static_inputs;
	}

	/**
	 * @return the static_inputs
	 */
	public List<ScriptInputStatic> getStatic_inputs() {
		if (static_inputs == null) {
			static_inputs = new ArrayList<ScriptInputStatic>();
		}
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
		if (tags == null) {
			tags = new ArrayList<String>();
		}
		return tags;
	}

	/**
	 * @param usecaseid the usecaseid to set
	 */
	@ConfigurationProperty(name = "usecaseid", label = "Title", uri="http://purl.org/dc/elements/1.1/title", required=false)
	public void setUsecaseid(String usecaseid) {
		this.usecaseid = usecaseid;
	}

	/**
	 * @return the usecaseid
	 */
	public String getUsecaseid() {
		return usecaseid;
	}

	public boolean isIncludeStdIn() {
		return includeStdIn;
	}

	@ConfigurationProperty(name = "includeStdIn", label = "Include STDIN")
	public void setIncludeStdIn(boolean includeStdIn) {
		this.includeStdIn = includeStdIn;
	}

	public boolean isIncludeStdOut() {
		return includeStdOut;
	}

	@ConfigurationProperty(name = "includeStdOut", label = "Include STDOUT")
	public void setIncludeStdOut(boolean includeStdOut) {
		this.includeStdOut = includeStdOut;
	}

	public boolean isIncludeStdErr() {
		return includeStdErr;
	}

	@ConfigurationProperty(name = "includeStdErr", label = "Include STDERR")
	public void setIncludeStdErr(boolean includeStdErr) {
		this.includeStdErr = includeStdErr;
	}

	/**
	 * @return the validReturnCodes
	 */
	public List<Integer> getValidReturnCodes() {
		if (validReturnCodes == null) {
			validReturnCodes = new ArrayList<Integer>();
		}
		if (validReturnCodes.isEmpty()) {
			validReturnCodes.add(0);
		}
		return validReturnCodes;
	}

	/**
	 * @param validReturnCodes the validReturnCodes to set
	 */
	public void setValidReturnCodes(List<Integer> validReturnCodes) {
		this.validReturnCodes = validReturnCodes;
	}

	public String getReturnCodesAsText() {
		return StringUtils.join(getValidReturnCodes(), ",");
	}

	public void setReturnCodesAsText(String text) {
		if (getValidReturnCodes() == null) {
			validReturnCodes = new ArrayList<Integer>();
		}
		validReturnCodes.clear();
		String[] codes = text.split(",");
		for (String code : codes) {
			try {
				Integer codeInt = new Integer(code);
				if (!validReturnCodes.contains(codeInt)) {
					validReturnCodes.add(codeInt);
				}
			}
			catch (NumberFormatException e) {
				logger.error(e);
			}
		}
		if (validReturnCodes.isEmpty()) {
			validReturnCodes.add(0);
		}
		Collections.sort(validReturnCodes);
	}

	/**
	 * @return the group
	 */
	public String getGroup() {
		return group;
	}

	/**
	 * @param group the group to set
	 */
	public void setGroup(String group) {
		this.group = group;
	}

	/**
	 * @return the icon_url
	 */
	public String getIcon_url() {
		return icon_url;
	}

	@ConfigurationBean(uri = ExternalToolActivity.URI + "#OutputMap")
	public static class OutputMap {
		private String port;

		private ScriptOutput output;

		public String getPort() {
			return port;
		}

		@ConfigurationProperty(name = "port", label = "Port")
		public void setPort(String port) {
			this.port = port;
		}

		public ScriptOutput getOutput() {
			return output;
		}

		@ConfigurationProperty(name = "output", label = "Output")
		public void setOutput(ScriptOutput output) {
			this.output = output;
		}
	}

	@ConfigurationBean(uri = ExternalToolActivity.URI + "#InputMap")
	public static class InputMap {
		private String port;

		private ScriptInputUser input;

		public String getPort() {
			return port;
		}

		@ConfigurationProperty(name = "port", label = "Port")
		public void setPort(String port) {
			this.port = port;
		}

		public ScriptInputUser getInput() {
			return input;
		}

		@ConfigurationProperty(name = "input", label = "Input")
		public void setInput(ScriptInputUser input) {
			this.input = input;
		}
	}

}
