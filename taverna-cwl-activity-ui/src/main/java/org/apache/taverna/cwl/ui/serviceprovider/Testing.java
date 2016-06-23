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
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.yaml.snakeyaml.Yaml;


public class Testing {
	private static final String INPUTS = "inputs";
	private static final String ID = "id";
	private static final String TYPE = "type";
	private static final String ARRAY = "array";
	private static final String ITEMS = "items";
	static int i = 0;

	private static final int DEPTH_0 = 0;
	private static final int DEPTH_1 = 1;
	private static final int DEPTH_2 = 2;
	private static final String LABEL = "label";
	
	interface my{
		public void printName();
	}
	public void print(){
		System.out.println("ok");
	}
	public static Yaml getReader(){
		Yaml reader = new Yaml();
		return reader;
	}
	public static void main(String[] args) {

		Path path1 = Paths.get("/home/maanadev/cwlTools");
		Path path2 = path1.normalize();

		boolean pathExits = Files.exists(path2, new LinkOption[] { LinkOption.NOFOLLOW_LINKS });

		
		try {
			DirectoryStream<Path> stream = Files.newDirectoryStream(path2,"*.cwl");
			Stream<Path> parrale = StreamSupport.stream(stream.spliterator(), true);
		
			
			
//			stream.forEach(path->System.out.println(path));
			parrale.forEach(path->{
				Yaml reader =getReader();
				try {
					Map map=(Map) reader.load(new FileInputStream(path.toFile()));
					System.out.println(map);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}); 

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
