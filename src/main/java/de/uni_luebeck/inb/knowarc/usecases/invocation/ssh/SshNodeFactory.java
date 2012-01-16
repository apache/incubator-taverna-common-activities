/**
 * 
 */
package de.uni_luebeck.inb.knowarc.usecases.invocation.ssh;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author alanrw
 *
 */
public class SshNodeFactory {
	
	private Map<String, SshNode> nodeMap = Collections.synchronizedMap(new HashMap<String, SshNode> ());
	
	private static SshNodeFactory INSTANCE = new SshNodeFactory();
	
	private SshNode defaultNode;
	
	private SshNodeFactory() {
		defaultNode = getSshNode(SshNode.DEFAULT_HOST, SshNode.DEFAULT_PORT, SshNode.DEFAULT_DIRECTORY);
	}

	public SshNode getDefaultNode() {
		return defaultNode;
	}

	public static SshNodeFactory getInstance() {
		return INSTANCE;
	}
	
	public SshNode getSshNode(String host, int port, String directory) {
		String url = makeUrl(host, port, directory);
		if (nodeMap.containsKey(url)) {
			return nodeMap.get(url);
		}
		else {
			SshNode newNode = new SshNode();
			newNode.setHost(host);
			newNode.setPort(port);
			newNode.setDirectory(directory);
			nodeMap.put(url, newNode);
			return newNode;
		}
	}
	
	public boolean containsSshNode(String host, int port, String directory) {
		return nodeMap.containsKey(makeUrl(host, port, directory));
	}
	
	public static String makeUrl(String host, int port, String directory) {
		return ("ssh://" + host + ":" + port + directory);
	}
}
