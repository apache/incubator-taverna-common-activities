/**
 * 
 */
package net.sf.taverna.t2.activities.externaltool.manager;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import net.sf.taverna.raven.appconfig.ApplicationRuntime;
import net.sf.taverna.t2.activities.externaltool.local.ExternalToolLocalInvocationMechanism;
import net.sf.taverna.t2.lang.observer.MultiCaster;
import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;
import net.sf.taverna.t2.spi.SPIRegistry;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;


/**
 * @author alanrw
 *
 */
public class InvocationGroupManager implements Observable<InvocationManagerEvent>{
	
	private static final String DEFAULT_MECHANISM_NAME = "default local";
	private static final String DEFAULT_GROUP_NAME = "default";
	private HashSet<InvocationGroup> groups = new HashSet<InvocationGroup>();
	private InvocationGroup defaultGroup = null;
	
	private HashSet<InvocationMechanism> mechanisms = new HashSet<InvocationMechanism>();
	
	private static XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
	private static SAXBuilder builder = new SAXBuilder();
	
	private static SPIRegistry<MechanismCreator> mechanismCreatorRegistry = new SPIRegistry(MechanismCreator.class);

	private static SPIRegistry<InvocationPersister> invocationPersisterRegistry = new SPIRegistry(InvocationPersister.class);

	private static Logger logger = Logger.getLogger(InvocationGroupManager.class);
	
	private Map<InvocationGroup, InvocationGroup> groupReplacements = Collections.synchronizedMap(new HashMap<InvocationGroup, InvocationGroup>());
    
	private Map<String, InvocationMechanism> mechanismReplacements = Collections.synchronizedMap(new HashMap<String, InvocationMechanism>());
	
	private Map<String, InvocationGroup> groupImports = Collections.synchronizedMap(new HashMap<String, InvocationGroup> ());
	
	private Map<String, InvocationMechanism> mechanismImports = Collections.synchronizedMap(new HashMap<String, InvocationMechanism> ());
    
	
	private static class Singleton {
		private static InvocationGroupManager instance = new InvocationGroupManager();		
	}
	
	protected MultiCaster<InvocationManagerEvent> observers = new MultiCaster<InvocationManagerEvent>(
			this);
	
	private InvocationMechanism defaultMechanism = null;
	
	private InvocationGroupManager() {
		readConfiguration();
		defaultMechanism = getInvocationMechanism(DEFAULT_MECHANISM_NAME);
		if (defaultMechanism == null) {
			createDefaultMechanism();
		}
		defaultGroup = getInvocationGroup(DEFAULT_GROUP_NAME);
		if (defaultGroup == null) {
			createDefaultGroup();
		}
		
		saveConfiguration();
	}

	public static InvocationGroupManager getInstance() {
		return Singleton.instance;
	}

	public void addInvocationGroup(InvocationGroup group) {
		groups.add(group);
		observers.notify(new InvocationGroupAddedEvent(group));
	}
	
	public void replaceInvocationGroup(InvocationGroup originalGroup, InvocationGroup replacementGroup) {
		Set<String> toReplaceImports = new HashSet<String>();
		for (Entry<String, InvocationGroup> entry : groupImports.entrySet()) {
			if (entry.getValue() == originalGroup) {
				toReplaceImports.add(entry.getKey());
			}
		}
		for (String spec : toReplaceImports) {
			if (replacementGroup == null) {
				groupImports.remove(spec);
			} else {
				groupImports.put(spec, replacementGroup);
			}
		}
		if (replacementGroup != null) {
			groupReplacements.put(originalGroup, replacementGroup);
		}
		observers.notify(new InvocationGroupRemovedEvent(originalGroup, (replacementGroup == null? getDefaultGroup(): replacementGroup)));
	}
	
	public void removeInvocationGroup(InvocationGroup group) {
		groups.remove(group);
		replaceInvocationGroup(group, getDefaultGroup());
	}
	
	public void replaceInvocationMechanism(InvocationMechanism originalMechanism, InvocationMechanism replacementMechanism) {
		for (InvocationGroup g : groups) {
			if (g.getMechanism().equals(originalMechanism)) {
				if (replacementMechanism == null) {
					g.setMechanism(getDefaultMechanism());
				} else {
					g.setMechanism(replacementMechanism);					
				}
			}
		}
		Set<String> toRemoveImports = new HashSet<String>();
		for (Entry<String, InvocationMechanism> entry : mechanismImports.entrySet()) {
			if (entry.getValue() == originalMechanism) {
				toRemoveImports.add(entry.getKey());
			}
		}
		for (String spec : toRemoveImports) {
			if (replacementMechanism == null) {
				mechanismImports.remove(spec);
			} else {
				mechanismImports.put(spec, replacementMechanism);
			}
		}
		if (replacementMechanism != null) {
			mechanismReplacements.put(originalMechanism.getName() + ":" + originalMechanism.getXML(), replacementMechanism);
		}
		observers.notify(new InvocationMechanismRemovedEvent(originalMechanism, (replacementMechanism == null? getDefaultMechanism(): replacementMechanism)));
	}
	
	public void removeMechanism(InvocationMechanism mechanism) {
		mechanisms.remove(mechanism);
		replaceInvocationMechanism(mechanism, getDefaultMechanism());
	}
	
	public HashSet<InvocationGroup> getInvocationGroups() {
		return groups;
	}

	public InvocationGroup getDefaultGroup() {
		if (defaultGroup == null) {
			createDefaultGroup();
		}
		return defaultGroup;
	}

	public Set<InvocationMechanism> getMechanisms() {
		return mechanisms;
	}

	public void addMechanism(InvocationMechanism mechanism) {
		mechanisms.add(mechanism);
		observers.notify(new InvocationMechanismAddedEvent(mechanism));
	}

	public InvocationMechanism getDefaultMechanism() {
		if (defaultMechanism == null) {
			createDefaultMechanism();
		}
		return defaultMechanism;
	}

	public boolean containsGroup(InvocationGroup group) {
		return groups.contains(group);
	}
	
	public InvocationMechanism getInvocationMechanism(
			String defaultMechanismName) {
		for (InvocationMechanism m : mechanisms) {
			if (m.getName().equals(defaultMechanismName)) {
				return m;
			}
		}
		return null;
	}
	
	InvocationGroup getInvocationGroup(String groupName) {
		for (InvocationGroup g : groups) {
			if (g.getName().equals(groupName)) {
				return g;
			}
		}
		return null;
	}
	
	public void mechanismChanged(InvocationMechanism im) {
		observers.notify(new InvocationMechanismChangedEvent(im));
	}

	
	private void createDefaultMechanism() {
		defaultMechanism = new ExternalToolLocalInvocationMechanism();
		defaultMechanism.setName(DEFAULT_MECHANISM_NAME);
		mechanisms.add(defaultMechanism);
	}
	
	private void createDefaultGroup() {
		defaultGroup = new InvocationGroup();
		defaultGroup.setName(DEFAULT_GROUP_NAME);
		defaultGroup.setMechanism(defaultMechanism);
		groups.add(defaultGroup);
	}
	
	private void readConfiguration() {
		File f = new File(getInvocationManagerDirectory(), "invocationManager.xml");
		if (!f.exists()) {
			return;
		}
		try {
			Document document = null;
			synchronized(builder) {
				document = builder.build(f);
			}
			Element topElement = document.getRootElement();
			Element mechanismsElement = topElement.getChild("invocationMechanisms");
			for (Object mechanismObject : mechanismsElement.getChildren("invocationMechanism")) {
				Element mechanismElement = (Element) mechanismObject;
				Element mechanismNameElement = mechanismElement.getChild("invocationMechanismName");
				String mechanismName = mechanismNameElement.getText();
				Element mechanismTypeElement = mechanismElement.getChild("invocationMechanismType");
				String mechanismType = mechanismTypeElement.getText();
				Element mechanismDetailsElement = mechanismElement.getChild("mechanismDetails");
				Element detailsElement = (Element) mechanismDetailsElement.getChildren().get(0);
				InvocationMechanism mechanism = null;
				List<MechanismCreator> instances = null;
				synchronized (mechanismCreatorRegistry) {
					instances = mechanismCreatorRegistry.getInstances();
				}
				for (MechanismCreator mc : instances) {
					if (mc.canHandle(mechanismType)) {
						mechanism = mc.convert(detailsElement, mechanismName);
					}
				}
				if (mechanism != null) {
					this.addMechanism(mechanism);
				}
			}
			
			Element groupsElement = topElement.getChild("invocationGroups");
			for (Object groupObject : groupsElement.getChildren("invocationGroup")) {
				Element groupElement = (Element) groupObject;
				Element groupNameElement = groupElement.getChild("invocationGroupName");
				String groupName = groupNameElement.getText();
				Element mechanismNameElement = groupElement.getChild("mechanismName");
				String mechanismName = mechanismNameElement.getText();
				InvocationMechanism mechanism = getInvocationMechanism(mechanismName);
				if (mechanism == null) {
					logger.warn("Could not find mechanism " + mechanismName);
					mechanism = getDefaultMechanism();
				}
				InvocationGroup group = new InvocationGroup();
				group.setName(groupName);
				group.setMechanism(mechanism);
				group.convertMechanismToDetails();
				this.addInvocationGroup(group);
			}
		} catch (JDOMException e) {
			logger.error("XML parsing problem", e);
		} catch (IOException e) {
			logger.error("Unable to read invocation manager", e);
		}
	}
	
	/**
	 * Get the directory where the invocation information will be/is saved to.
	 */
	public static File getInvocationManagerDirectory() {
		
		File home = ApplicationRuntime.getInstance().getApplicationHomeDir();

		File invocationManagerDirectory = new File(home,"externaltool");
		if (!invocationManagerDirectory.exists()) {
			invocationManagerDirectory.mkdir();
		}
		return invocationManagerDirectory;
	}

	public void saveConfiguration() {
		File f = new File(getInvocationManagerDirectory(), "invocationManager.xml");
		
		Document configDocument = new Document();
		Element topElement = new Element("invocationManager");
		Element mechanismsElement = new Element("invocationMechanisms");

		for (InvocationMechanism m : mechanisms) {
			Element mechanismElement = new Element("invocationMechanism");
			Element nameElement = new Element("invocationMechanismName");
			nameElement.setText(m.getName());
			mechanismElement.addContent(nameElement);
			Element typeElement = new Element("invocationMechanismType");
			typeElement.setText(m.getType());
			mechanismElement.addContent(typeElement);
			Element mechanismDetails = new Element("mechanismDetails");
			mechanismDetails.addContent(m.getXMLElement());
			mechanismElement.addContent(mechanismDetails);
			
			mechanismsElement.addContent(mechanismElement);
		}
		topElement.addContent(mechanismsElement);
		
		Element groupsElement = new Element("invocationGroups");
		for (InvocationGroup g : groups) {
			Element groupElement = new Element("invocationGroup");
			Element nameElement = new Element("invocationGroupName");
			nameElement.setText(g.getName());
			groupElement.addContent(nameElement);
			Element mechanismNameElement = new Element("mechanismName");
			mechanismNameElement.setText(g.getMechanism().getName());
			groupElement.addContent(mechanismNameElement);
			groupsElement.addContent(groupElement);
		}
		topElement.addContent(groupsElement);
		
		configDocument.setRootElement(topElement);
		
		FileWriter writer;
		try {
			writer = new FileWriter(f);
			synchronized(outputter) {
				outputter.output(configDocument, writer);
			}
			writer.close();
		} catch (IOException e) {
			logger.error("Unable to save invocation manager", e);
		}
	}

	public void groupChanged(InvocationGroup group) {
		observers.notify(new InvocationGroupChangedEvent(group));
	}

	@Override
	public void addObserver(Observer<InvocationManagerEvent> observer) {
		observers.addObserver(observer);
	}

	@Override
	public List<Observer<InvocationManagerEvent>> getObservers() {
		return observers.getObservers();
	}

	@Override
	public void removeObserver(Observer<InvocationManagerEvent> observer) {
		observers.removeObserver(observer);
	}
	
	public void deleteRun(String runId) {
		List<InvocationPersister> instances = null;
		synchronized (invocationPersisterRegistry) {
			instances = invocationPersisterRegistry.getInstances();
		}
		for (InvocationPersister persister : instances) {
			persister.deleteRun(runId);
		}
	}
	
	public void persistInvocations() {
		List<InvocationPersister> instances = null;
		synchronized (invocationPersisterRegistry) {
			instances = invocationPersisterRegistry.getInstances();
		}
		for (InvocationPersister persister : instances) {
			persister.persist(getInvocationManagerDirectory());
		}
	}
	
	public void loadInvocations() {
		List<InvocationPersister> instances = null;
		synchronized (invocationPersisterRegistry) {
			instances = invocationPersisterRegistry.getInstances();
		}
		for (InvocationPersister persister : instances) {
			persister.load(getInvocationManagerDirectory());
		}
	}

	public boolean containsMechanism(InvocationMechanism invocationMechanism) {
		return mechanisms.contains(invocationMechanism);
	}
	
	public InvocationGroup getGroupReplacement(InvocationGroup group) {
		return groupReplacements.get(group);
	}
	
	public InvocationMechanism getMechanismReplacement(String invocationMechanismSpecification) {
		return mechanismReplacements.get(invocationMechanismSpecification);
	}
	
	public InvocationGroup getImportedGroup(String groupSpecification) {
		return groupImports.get(groupSpecification);
	}

	public InvocationMechanism getImportedMechanism(String mechanismSpecification) {
		return mechanismImports.get(mechanismSpecification);
	}

	public void importMechanism(String invocationMechanismSpecification,
			InvocationMechanism createdMechanism) {
		addMechanism(createdMechanism);
		mechanismImports.put(invocationMechanismSpecification, createdMechanism);
	}

	public void importInvocationGroup(String invocationGroupSpecification,
			InvocationGroup invocationGroup) {
		addInvocationGroup(invocationGroup);
		groupImports.put(invocationGroupSpecification, invocationGroup);
	}

}
