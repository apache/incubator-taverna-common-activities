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
import java.util.ArrayList;

import org.apache.taverna.activities.externaltool.ExternalToolActivity;
import org.apache.taverna.workflowmodel.processor.config.ConfigurationBean;
import org.apache.taverna.workflowmodel.processor.config.ConfigurationProperty;

/**
 * Internal description of input
 */
@ConfigurationBean(uri = ExternalToolActivity.URI + "#ScriptOutput")
public class ScriptOutput {
	private String path;
	private boolean binary;
	private ArrayList<String> mime = new ArrayList<String>();

	@Override
	public String toString() {
		return "Output[path: " + path + (binary ? ", binary" : "")
				+ " mime: " + mime.toString() + "]";
	}

	/**
	 * @return the path
	 */
	public final String getPath() {
		return path;
	}

	/**
	 * @param path the path to set
	 */
	@ConfigurationProperty(name = "path", label = "Path")
	public final void setPath(String path) {
		this.path = path;
	}

	/**
	 * @return the binary
	 */
	public final boolean isBinary() {
		return binary;
	}

	/**
	 * @param binary the binary to set
	 */
	@ConfigurationProperty(name = "binary", label = "Binary")
	public final void setBinary(boolean binary) {
		this.binary = binary;
	}

	/**
	 * @return the mime
	 */
	public final ArrayList<String> getMime() {
		if (mime == null) {
			mime = new ArrayList<String>();
		}
		return mime;
	}

	/**
	 * @param mime the mime to set
	 */
	@ConfigurationProperty(name = "mime", label = "Mime Types", required=false)
	public final void setMime(ArrayList<String> mime) {
		this.mime = mime;
	}
};
