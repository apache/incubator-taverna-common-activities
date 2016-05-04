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

package org.apache.taverna.activities.externaltool.desc;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.apache.taverna.workflowmodel.serialization.DeserializationException;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

public class UseCaseEnumeration {

	private static Logger logger = Logger.getLogger(UseCaseEnumeration.class);

	public static List<ToolDescription> readDescriptionsFromUrl(String xmlFileUrl) throws IOException {

		List<ToolDescription> ret = new ArrayList<ToolDescription>();
		URLConnection con = null;
		try {
			URL url = new URL(xmlFileUrl);

			con = url.openConnection();
			con.setConnectTimeout(4000);
			ret = readDescriptionsFromStream(con.getInputStream());
			
		} catch (IOException ioe) {
			logger.error("Problem retrieving from " + xmlFileUrl);
			logger.error(ioe);
			throw ioe;
		}
		finally {

		}

		return ret;

	}
	
	public static List<ToolDescription> readDescriptionsFromStream(InputStream is) {
		
		List<ToolDescription> ret = new ArrayList<ToolDescription>();

		SAXBuilder builder = new SAXBuilder();
		Document doc = null;
		try {
			doc = builder.build(is);
			is.close();
		} catch (JDOMException e1) {
			logger.error(e1);
			return ret;
		} catch (IOException e1) {
			logger.error(e1);
			return ret;
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				logger.error(e);
			}
		}

		Element usecases = doc.getRootElement();
		for (Object ochild : usecases.getChildren()) {
			Element child = (Element) ochild;
			if (child.getName().equalsIgnoreCase("program")) {
					try {
						ret.add(new ToolDescription(child));
					} catch (DeserializationException e) {
						logger.error(e);
					}
			}
		}
		return ret;
	}

	public static ToolDescription readDescriptionFromUrl(
			String repositoryUrl, String id) throws IOException {
		List<ToolDescription> descriptions = readDescriptionsFromUrl(repositoryUrl);
		for (ToolDescription usecase : descriptions) {
			if (usecase.getUsecaseid().equals(id)) {
				return usecase;
			}
		}
		return null;
	}
}
