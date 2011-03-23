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

/**
 * This subclass of script input is used to manage static content
 * which is embedded into the use case description.
 */
public class ScriptInputStatic extends ScriptInput {

	public ScriptInputStatic() {
	}

	private String url = null;  //if this is set, load content from remote URL
	private Object content = null; //this may be a String or a byte[]

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
	public final void setUrl(String url) {
		this.url = url;
	}

	/**
	 * @return the content
	 */
	public final Object getContent() {
		return content;
	}

	/**
	 * @param content the content to set
	 */
	public final void setContent(Object content) {
		this.content = content;
	}
}
