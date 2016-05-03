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

import org.apache.taverna.activities.externaltool.ExternalToolActivity;
import org.apache.taverna.workflowmodel.processor.config.ConfigurationBean;
import org.apache.taverna.workflowmodel.processor.config.ConfigurationProperty;

/**
 * This subclass of script input is used to manage static content
 * which is embedded into the use case description.
 */
@ConfigurationBean(uri = ExternalToolActivity.URI + "#ScriptInputStatic")
public class ScriptInputStatic extends ScriptInput {

	public ScriptInputStatic() {
	}

	private String url = null;  //if this is set, load content from remote URL
	private String content = null;

	@Override
	public String toString() {
		return "InputStatic[tag: " +
			getTag() + (isFile() ? ", file" : "") + (isTempFile() ? ", tempfile" : "") + (isBinary() ? ", binary" : "") + ", content: " + content + "]";
	}

	/**
	 * @return the url
	 */
	public final String getUrl() {
		return url;
	}

	/**
	 * @param url the url to set
	 */
	@ConfigurationProperty(name = "url", label = "URL", required=false)
	public final void setUrl(String url) {
		this.url = url;
	}

	/**
	 * @return the content
	 */
	public final String getContent() {
		return content;
	}

	/**
	 * @param content the content to set
	 */
	@ConfigurationProperty(name = "content", label = "Content", required=false)
	public final void setContent(String content) {
		this.content = content;
	}
}
