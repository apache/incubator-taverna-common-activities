/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.uni_luebeck.inb.knowarc.usecases.invocation.ssh;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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
