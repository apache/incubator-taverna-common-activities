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

package de.uni_luebeck.inb.knowarc.grid;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * xRSL is the format in which jobs are specified. This class represents the
 * internal representation of such a description.
 */
public class xRslDescription {
	public String name = null;
	public String executable = null;
	public ArrayList<String> commandline = new ArrayList<String>();
	public HashMap<String, String> mInput2Url = new HashMap<String, String>();
	public HashMap<String, String> mOutput2Url = new HashMap<String, String>();
	public ArrayList<String> lRE = new ArrayList<String>();
	public String stdout = "stdout";
	public String stderr = "stderr";
	public String stdin = null;
	public String gmlog = null;
	public int maximumWalltimeInSeconds = 0;
	public String queue = null;

	/**
	 * String representation of xRSL job description.
	 */
	@Override
	public String toString() {
		String addInputFiles = "";
		String ret = "&";

		String myexec = executable;
		if (executable.contains("://")) {
			myexec = executable.substring(executable.lastIndexOf('/') + 1);
			addInputFiles += "(\"" + myexec + "\" \"" + executable + "\")";
		}

		ret += "(\"executable\" = \"" + executable + "\")";
		if (!executable.startsWith("/"))
			ret += "(\"executables\" = \"" + executable + "\")";

		ret += "(\"action\" = \"request\")";
		if (commandline.size() > 0) {
			ret += "(\"arguments\" = ";
			for (String cmdl : commandline) {
				ret += "\"" + cmdl + "\" ";
			}
			ret += ")";
		}
		if (mInput2Url.size() > 0) {
			ret += "(\"inputFiles\" = " + addInputFiles;
			for (Map.Entry<String, String> inputs : mInput2Url.entrySet()) {
				ret += "(\"" + inputs.getKey() + "\" \"" + inputs.getValue() + "\") ";
			}
			ret += ")";
		}
		if (mOutput2Url.size() > 0) {
			ret += "(\"outputFiles\" = ";
			for (Map.Entry<String, String> inputs : mOutput2Url.entrySet()) {
				ret += "(\"" + inputs.getKey() + "\" \"" + inputs.getValue() + "\") ";
			}
			ret += ")";
		}
		if (lRE.size() > 0) {
			// we are submitting to the grid manager directly,
			// so we need to specify the REs we want, the exact version number.
			// >= will normally be resolved by ngsub, so in our case,
			// we need to do it by hand in UseCaseInvocation.Submit
			ret += "(\"runTimeEnvironment\" = ";
			for (String r : lRE)
				ret += "\"" + r + "\" ";
			ret += ")";
		}
		ret += "(\"jobName\" = \"" + name + "\")";
		if (stdout != null)
			ret += "(\"stdout\" = \"" + stdout + "\")";
		if (stderr != null)
			ret += "(\"stderr\" = \"" + stderr + "\")";
		if (stdin != null)
			ret += "(\"stdin\" = \"" + stdin + "\")";
		if (gmlog != null)
			ret += "(\"gmlog\" = \"" + gmlog + "\")";

		if (queue != null)
			ret += "(\"queue\" = \"" + queue + "\")";
		else
			ret += "(\"queue\" = \"\")";

		if (maximumWalltimeInSeconds != 0)
			ret += "(\"walltime\" = \"" + maximumWalltimeInSeconds + "\")";
		return ret;
	}
}
