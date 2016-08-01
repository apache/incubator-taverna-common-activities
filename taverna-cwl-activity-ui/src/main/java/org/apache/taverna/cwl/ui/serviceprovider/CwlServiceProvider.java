/*******************************************************************************
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *     contributor license agreements.  See the NOTICE file distributed with
 *     this work for additional information regarding copyright ownership.
 *     The ASF licenses this file to You under the Apache License, Version 2.0
 *     (the "License"); you may not use this file except in compliance with
 *     the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *******************************************************************************/
package org.apache.taverna.cwl.ui.serviceprovider;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.swing.Icon;

import org.apache.log4j.Logger;
import org.apache.taverna.scufl2.api.common.Visitor;
import org.apache.taverna.scufl2.api.configurations.Configuration;
import org.apache.taverna.servicedescriptions.AbstractConfigurableServiceProvider;
import org.apache.taverna.servicedescriptions.ConfigurableServiceProvider;
import org.apache.taverna.servicedescriptions.ServiceDescriptionProvider;
import org.yaml.snakeyaml.Yaml;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class CwlServiceProvider extends AbstractConfigurableServiceProvider implements ConfigurableServiceProvider {

	public static final String TOOL_NAME = "toolName";
	public static final String CWL_CONF = "cwl_conf";
	public static final String CWL_PATH = "cwl_path";

	public static final String DEFAULT_PATH_1 = "/usr/share/commonwl/";
	public static final String DEFAULT_PATH_2 = "/usr/local/share/commonwl/";
	public static final String XDF_DATA_HOME = "XDF_DATA_HOME";
	public static final String COMMONWL = "commonwl/";
	private static Logger logger = Logger.getLogger(CwlServiceProvider.class);

	CwlServiceProvider() {
		// FIXME
		super(getDefaultConfiguration());
	}

	private static final String providerName = "CWL Services";
	private static final URI providerId = CwlServiceDesc.ACTIVITY_TYPE.resolve("#provider");

	@Override
	public void findServiceDescriptionsAsync(FindServiceDescriptionsCallBack callBack) {

		// get the location of the cwl tool from the workbench
		List<Path> paths = getPath();

		for (Path path : paths) {
			// figure out the dots in the path ex: /maanadev/../cwltools
			Path normalizedPath = path.normalize();

			DirectoryStream<Path> stream = null;
			try {
				stream = Files.newDirectoryStream(normalizedPath, "*.cwl");
			} catch (IOException e) {
				
				logger.warn("Path is not correct !");
				callBack.finished();
				return;
			}
			// create stream with parallel capabilities
			Stream<Path> paralleStream = StreamSupport.stream(stream.spliterator(), true);

			paralleStream.forEach(p -> {
				Yaml reader = getYamlReader();

				Map cwlFile;
				try (FileInputStream file = new FileInputStream(path.toFile())) {
					cwlFile = (Map) reader.load(file);
					JsonNode config = createJsonNode(p, cwlFile);
					// Creating CWl service Description
					CwlServiceDesc cwlServiceDesc = createCWLDesc(config);
					// return the service description
					callBack.partialResults(Arrays.asList(cwlServiceDesc));

				} catch (IOException e) {

					logger.warn("File not Found !");

				}

			});
			callBack.finished();
		}

	}
/**
 * This method checks whether provided path is valid or not and if it's valid the it's added to the list
 * @param defaultPaths List to hold valid paths
 * @param path 
 * @param path1 if there is no second path argument this should be set to null
 */
	public void addPath(List<Path> defaultPaths, String path, String path1) {

		Path defaultPath;
		if (path1 == null)
			defaultPath = Paths.get(path);
		else
			defaultPath = Paths.get(path, path1);

		if (defaultPath.isAbsolute())
			defaultPaths.add(defaultPath);
	}

	private List<Path> getPath() {
		String userInput = getConfiguration().getJsonAsObjectNode().get("path").asText();
		// If user haven't provided a PATH 
		if (userInput.isEmpty()||userInput==null) {
			List<Path> defaultPaths = new ArrayList<>();
			addPath(defaultPaths, DEFAULT_PATH_1, null);
			addPath(defaultPaths, DEFAULT_PATH_2, null);
			addPath(defaultPaths, XDF_DATA_HOME, COMMONWL);
			return defaultPaths;
		}

		return  Arrays.asList(Paths.get(userInput));
	}

	/**
	 * This method is creating a JsonNode object which contains Tool as a map
	 * and it's Path,Name
	 * 
	 * @param p
	 *            Path of the CWL tool
	 * @param cwlFile
	 *            Output of the YAML reader
	 * @return
	 */
	private JsonNode createJsonNode(Path p, Map cwlFile) {
		ObjectMapper mapper = new ObjectMapper();
		JsonNode root = mapper.createObjectNode();
		JsonNode cwl_map = mapper.valueToTree(cwlFile);
		((ObjectNode) root).put(TOOL_NAME, p.getFileName().toString().split("\\.")[0]);
		((ObjectNode) root).put(CWL_CONF, cwl_map);
		((ObjectNode) root).put(CWL_PATH, p.toString());
		return root;
	}

	/**
	 * 
	 * This method creates CwlServiceDesc which hold the configuration of the
	 * tool and the tool name
	 * 
	 * @param node
	 *            JsonnNode which holds the final configuration of the tool
	 * @return
	 */

	private CwlServiceDesc createCWLDesc(JsonNode node) {
		CwlServiceDesc cwlServiceDesc = new CwlServiceDesc();
		cwlServiceDesc.setCwlConfiguration(node);
		cwlServiceDesc.setToolName(node.get(CwlServiceProvider.TOOL_NAME).asText());
		return cwlServiceDesc;
	}

	@Override
	public Icon getIcon() {
		return null;
	}

	@Override
	public String getId() {
		return providerId.toASCIIString();
	}

	@Override
	public String getName() {
		return providerName;
	}

	@Override
	protected List<? extends Object> getIdentifyingData() {
		return Arrays.<Object> asList(getPath());
	}

	public Yaml getYamlReader() {
		Yaml reader = new Yaml();
		return reader;
	}

	@Override
	public ServiceDescriptionProvider newInstance() {
		return new CwlServiceProvider();
	}

	@Override
	public URI getType() {
		return providerId;
	}

	@Override
	public void setType(URI arg0) {

	}

	@Override
	public boolean accept(Visitor arg0) {
		return false;
	}
	/**
	 * Set the Configuration such that when service provider is created user is asked for the PATH
	 * @return
	 */
	public static Configuration getDefaultConfiguration() {
		Configuration c = new Configuration();
		ObjectNode conf = c.getJsonAsObjectNode();
		conf.put("path", "");
		return c;
	}
}
