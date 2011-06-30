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

/**
 * Internal description of input
 */
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
	public final void setMime(ArrayList<String> mime) {
		this.mime = mime;
	}
};
