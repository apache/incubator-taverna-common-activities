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

package de.uni_luebeck.inb.knowarc.usecases;

import java.nio.charset.Charset;

import org.apache.taverna.activities.externaltool.ExternalToolActivity;
import org.apache.taverna.workflowmodel.processor.config.ConfigurationBean;
import org.apache.taverna.workflowmodel.processor.config.ConfigurationProperty;

/**
 * Integrates inputs to the grid that come from the use case descriptions
 * with those that are fed through the workflow.
 *
 * this class controls name and data storage of one input,
 * no matter where the data comes from
 */
@ConfigurationBean(uri = ExternalToolActivity.URI + "#AbstractScriptInput")
public abstract class ScriptInput {
	/**
	 * This input can be referenced under the name 'tag'.
	 */
	private String tag = null;
	/**
	 * In most cases, the data will be stored under a specific
	 * filename.
	 */
	private boolean file = false;
	/**
	 * Set, if the name of the file to be executed is not
	 * explicitly set but prepared by some automatism and referenced
	 * via the tagging principle.
	 */
	private boolean tempFile = false;
	/**
	 * True if (!) the data is binary. (Text otherwise)
	 */
	private boolean binary = false;

	private String charsetName = Charset.defaultCharset().name();
	private boolean forceCopy = false;

	/**
	 * @return the tag
	 */
	public final String getTag() {
		return tag;
	}
	/**
	 * @param tag the tag to set
	 */
	@ConfigurationProperty(name = "tag", label = "Tag")
	public final void setTag(String tag) {
		this.tag = tag;
	}
	/**
	 * @return the file
	 */
	public final boolean isFile() {
		return file;
	}
	/**
	 * @param file the file to set
	 */
	@ConfigurationProperty(name = "file", label = "File")
	public final void setFile(boolean file) {
		this.file = file;
	}
	/**
	 * @return the tempFile
	 */
	public final boolean isTempFile() {
		return tempFile;
	}
	/**
	 * @param tempFile the tempFile to set
	 */
	@ConfigurationProperty(name = "tempFile", label = "Temporary File")
	public final void setTempFile(boolean tempFile) {
		this.tempFile = tempFile;
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

	public String getCharsetName() {
		return this.charsetName;
	}
	/**
	 * @param charsetName the charsetName to set
	 */
	@ConfigurationProperty(name = "charsetName", label = "Chararter Set")
	public void setCharsetName(String charsetName) {
		this.charsetName = charsetName;
	}

	@ConfigurationProperty(name = "forceCopy", label = "Force Copy")
	public final void setForceCopy(boolean forceCopy) {
		this.forceCopy = forceCopy;

	}
	/**
	 * @return the forceCopy
	 */
	public boolean isForceCopy() {
		return forceCopy;
	}
}
