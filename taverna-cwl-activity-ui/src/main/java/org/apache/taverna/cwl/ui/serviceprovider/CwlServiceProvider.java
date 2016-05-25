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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;

import org.yaml.snakeyaml.Yaml;

import net.sf.taverna.t2.servicedescriptions.ServiceDescriptionProvider;

public class CwlServiceProvider implements ServiceDescriptionProvider {

	private static final File cwlFilesLocation = new File("CWLFiles");

	@Override
	public void findServiceDescriptionsAsync(FindServiceDescriptionsCallBack callBack) {
		
		//This is holding the CWL configuration beans
		List<CwlServiceDesc>  result = new ArrayList<CwlServiceDesc>();
		
		File[] cwlFiles = getCwlFiles();

		for (File file : cwlFiles) {
			Map cwlFile = null;
			// Load the CWL file using SnakeYaml lib
			Yaml cwlReader = new Yaml();
			try {
				cwlFile = (Map) cwlReader.load(new FileInputStream(file));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			if (cwlFile != null) {
				//Creating CWl service Description
				CwlServiceDesc cwlServiceDesc = new CwlServiceDesc();
				cwlServiceDesc.setCwlConfiguration(cwlFile);
				//add to the result
				result.add(cwlServiceDesc);
				//return the service description
				callBack.partialResults(result);
			}

		}
		callBack.finished();
	

	}

	@Override
	public Icon getIcon() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
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
}
