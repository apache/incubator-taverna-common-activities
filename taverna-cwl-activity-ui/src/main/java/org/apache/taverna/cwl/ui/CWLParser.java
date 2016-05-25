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
package org.apache.taverna.cwl.ui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;


public class CWLParser {

	private File cwlFile;

	private void setUpFile(File cwlFile) {

		this.cwlFile = cwlFile;
	}

	private void readKeyWords() throws FileNotFoundException {

		Yaml yaml = new Yaml();
		Map contents = (Map) yaml.load(new FileInputStream(cwlFile));

		for (Object key : contents.keySet()) {
			if (key.equals("inputs"))
				for (Object inputPairs : (ArrayList<String>) contents.get(key)) {
					Map test = (Map) inputPairs;
					System.out.println(inputPairs);

					for (Object inputKey : test.keySet()) {
						if (inputKey.equals("id"))
							System.out.println(test.get(inputKey));
						else if (inputKey.equals("type")) {
							try {
								Map inputType = (Map) test.get(inputKey);
								for (Object inputTypekey : inputType.keySet()) {
									if (inputTypekey.equals("inputBinding")) {
										Map typeInputBinding = (Map) inputType.get(inputTypekey);
										for (Object inputBindingKey : typeInputBinding.keySet()) {

											System.out.println(
													inputBindingKey + " " + typeInputBinding.get(inputBindingKey));
										}
									} else {
										System.out.println(inputTypekey + " " + inputType.get(inputTypekey));
									}
								}
							} catch (ClassCastException e) {
								System.out.println(inputKey + " " + test.get(inputKey));
							}
						} else {
							System.out.println(inputKey + " " + test.get(inputKey));
						}

					}

				}
		}

	}

	public static void main(String[] args) {
		CWLParser cwlParser = new CWLParser();

		cwlParser.setUpFile(new File("CWLFiles/binding-test.cwl"));

		try {
			cwlParser.readKeyWords();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	}

}
