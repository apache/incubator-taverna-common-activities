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
 * Internal description of output
 */
@ConfigurationBean(uri = ExternalToolActivity.URI + "#ScriptInput")
public class ScriptInputUser extends ScriptInput {

	/**
	 * This input may be fed from multiple ouputs.
	 */
	private boolean list = false;
	/**
	 * True if the data from a list input in taverna is concatenated into one single input file.
	 */
	private boolean concatenate = false;

	private ArrayList<String> mime = new ArrayList<String>();

	@Override
	public String toString() {
		return "Input[tag: " + getTag() + (isFile() ? ", file" : "")
				+ (isTempFile() ? ", tempfile" : "")
				+ (isBinary() ? ", binary" : "") + (list ? ", list" : "")
				+ (concatenate ? ", concatenate" : "")
				+ " mime: " + mime.toString() + "]";
	}

	/**
	 * @return the list
	 */
	public final boolean isList() {
		return list;
	}

	/**
	 * @param list the list to set
	 */
	@ConfigurationProperty(name = "list", label = "List")
	public final void setList(boolean list) {
		this.list = list;
	}

	/**
	 * @return the concatenate
	 */
	public final boolean isConcatenate() {
		return concatenate;
	}

	/**
	 * @param concatenate the concatenate to set
	 */
	@ConfigurationProperty(name = "concatenate", label = "Concatenate")
	public final void setConcatenate(boolean concatenate) {
		this.concatenate = concatenate;
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
