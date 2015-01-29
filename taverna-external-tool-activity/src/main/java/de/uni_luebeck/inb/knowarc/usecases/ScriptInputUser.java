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
import java.util.ArrayList;

import net.sf.taverna.t2.activities.externaltool.ExternalToolActivity;
import net.sf.taverna.t2.workflowmodel.processor.config.ConfigurationBean;
import net.sf.taverna.t2.workflowmodel.processor.config.ConfigurationProperty;

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
