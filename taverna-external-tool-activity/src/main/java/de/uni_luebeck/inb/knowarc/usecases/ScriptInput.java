/* Part of the KnowARC Janitor Use-case processor for taverna
 *  written 2007-2010 by Hajo Nils Krabbenhoeft and Steffen Moeller
 *  University of Luebeck, Institute for Neuro- and Bioinformatics
 *  University of Luebeck, Institute for Dermatolgy
 *
 *  This package is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation; either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This package is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this package; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301 USA
 */

package de.uni_luebeck.inb.knowarc.usecases;

import java.nio.charset.Charset;

import net.sf.taverna.t2.activities.externaltool.ExternalToolActivity;
import net.sf.taverna.t2.workflowmodel.processor.config.ConfigurationBean;
import net.sf.taverna.t2.workflowmodel.processor.config.ConfigurationProperty;

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
