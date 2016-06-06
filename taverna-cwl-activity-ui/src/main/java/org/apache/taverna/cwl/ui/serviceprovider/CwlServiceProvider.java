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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;

import org.yaml.snakeyaml.Yaml;

import net.sf.taverna.t2.servicedescriptions.AbstractConfigurableServiceProvider;
import net.sf.taverna.t2.servicedescriptions.ConfigurableServiceProvider;

public class CwlServiceProvider extends AbstractConfigurableServiceProvider<CwlServiceProviderConfig>
		implements ConfigurableServiceProvider<CwlServiceProviderConfig> {

	CwlServiceProvider() {
		super(new CwlServiceProviderConfig());
	}
	private static final String providerName ="CWL Services";
	private static final URI providerId = URI
			.create("http://cwl.com/2016/service-provider/cwlcommandlinetools");
	private File cwlFilesLocation;

	@Override
	public void findServiceDescriptionsAsync(FindServiceDescriptionsCallBack callBack) {

		// get the location of the cwl tool from the workbench
		cwlFilesLocation = new File(getConfiguration().getPath());
		// This is holding the CWL configuration beans
		List<CwlServiceDesc> result = new ArrayList<CwlServiceDesc>();

		File[] cwlFiles = getCwlFiles();

		// Load the CWL file using SnakeYaml lib
		Yaml cwlReader = new Yaml();

		for (File file : cwlFiles) {
			Map cwlFile = null;

			try {
				cwlFile = (Map) cwlReader.load(new FileInputStream(file));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			if (cwlFile != null) {
				// Creating CWl service Description
				CwlServiceDesc cwlServiceDesc = new CwlServiceDesc();
				cwlServiceDesc.setCwlConfiguration(cwlFile);
				cwlServiceDesc.setToolName(file.getName().split("\\.")[0]);

				// add to the result
				result.add(cwlServiceDesc);
				// return the service description
				callBack.partialResults(result);
			}

		}
		callBack.finished();

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

	private File[] getCwlFiles() {
		// Get the .cwl files in the directory using the FileName Filter
		FilenameFilter fileNameFilter = new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				if (name.lastIndexOf('.') > 0) {
					// get last index for '.' char
					int lastIndex = name.lastIndexOf('.');

					// get extension
					String str = name.substring(lastIndex);

					// match path name extension
					if (str.equals(".cwl")) {
						return true;
					}
				}
				return false;
			}
		};

		return cwlFilesLocation.listFiles(fileNameFilter);
	}

	@Override
	protected List<? extends Object> getIdentifyingData() {
		return null;
	}
}
