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

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * Representation of information about a single runtime environment. It is used
 * to decide if one runtime environment is possibly compatible with another one
 * for the selection of queues to commit to.
 * 
 * To experiment/test these functions, run the following java -cp
 * target/taverna-knowarc-processor-0.1.7.jar
 * de.uni_luebeck.janitor.ldap.RuntimeEnvironment compare bla-1 foo-1 java -cp
 * target/taverna-knowarc-processor-0.1.7.jar
 * de.uni_luebeck.janitor.ldap.RuntimeEnvironment compare bla-1 bla-2 java -cp
 * target/taverna-knowarc-processor-0.1.7.jar
 * de.uni_luebeck.janitor.ldap.RuntimeEnvironment compare bla-1.2 bla-1
 * 
 * @author Steffen Moeller
 */
@SuppressWarnings("unchecked")
public class RuntimeEnvironment implements Comparable {
	
	private static Logger logger = Logger.getLogger(RuntimeEnvironment.class);


	/**
	 * Unique identification of the runtime environment - the full name
	 */
	protected String id;

	/**
	 * Accessor function for the complete identifier of the runtime environment
	 */
	public String getID() {
		return id;
	}

	protected String name;

	/**
	 * Accessor function for the RE's name
	 */
	public String getName() {
		return name;
	}

	protected String version;

	/**
	 * Accessfor function for the version
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * for those busy fellows who don't have the time to convert the String into
	 * a RuntimeEnvironment object.
	 * 
	 * @author Steffen Moeller
	 */
	public boolean atLeastAsCapableAs(String s) {
		RuntimeEnvironment tmpRE = new RuntimeEnvironment(s);
		return this.atLeastAsCapableAs(tmpRE);
	}

	/**
	 * Indicates if a runtime environment has the same name, and if so, if the
	 * given RE has the same or a later version.
	 * 
	 * @author Steffen Moeller
	 */
	public boolean atLeastAsCapableAs(RuntimeEnvironment re) {
		if (!name.equals(re.name))
			return false;
		int c = compareVersions(getVersion(), re.getVersion());
		if (c >= 0) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Indicates if this runtimeEnvironment is the same version or later as any
	 * in that list.
	 * 
	 * @author Steffen Moeller
	 */
	public boolean atLeastAsCapableAsAnyOf(Iterable<RuntimeEnvironment> res) {
		boolean compatibleOneFound = false;
		Iterator<RuntimeEnvironment> i = res.iterator();
		while (i.hasNext() && !compatibleOneFound) {
			RuntimeEnvironment r = i.next();
			compatibleOneFound = atLeastAsCapableAs(r);
		}
		return compatibleOneFound;
	}

	/**
	 * Indicates if any of the runtime environments listed is the same version
	 * as this or later.
	 * 
	 * @author Steffen Moeller
	 */
	public boolean isInferiorToAtLeastOneIn(Iterable<RuntimeEnvironment> res) {
		boolean compatibleOneFound = false;
		Iterator<RuntimeEnvironment> i = res.iterator();
		while (i.hasNext() && !compatibleOneFound) {
			RuntimeEnvironment r = i.next();
			compatibleOneFound = r.atLeastAsCapableAs(this);
		}
		return compatibleOneFound;
	}

	/**
	 * Parses a string as commonly presented by the infosystem
	 * 
	 * @author Steffen Moeller
	 */
	public RuntimeEnvironment(String raw) {
		id = raw;
		int dashpos = raw.indexOf("-");
		if (-1 == dashpos) {
			version = "";
			name = raw;
		} else {
			name = raw.substring(0, dashpos);
			if (dashpos + 1 <= raw.length()) {
				version = raw.substring(dashpos + 1, raw.length());
			} else {
				version = "";
			}
		}
	}

	/**
	 * to make it behave like a string at time, as it was originally implemented
	 */
	@Override
	public String toString() {
		return id;
	}

	/**
	 * Implementation of Comparable interface. It comes handy albeit this
	 * function says nothing about the compatibility of two runtime environments
	 * unless their names are identical and the relation of the constraint was
	 * taken into account. It just sorts them in lists.
	 */
	public int compareTo(Object o) throws ClassCastException {
		RuntimeEnvironment r = (RuntimeEnvironment) o;
		if (getName().equals(r.getName())) {
			return RuntimeEnvironment.compareVersions(getVersion(), r.getVersion());
		} else {
			return id.compareTo(r.getID());
		}
	}

	/**
	 * FIXME: For the sake of simplicity, this implementation makes an error in
	 * treating . and - in the versions equally. Versions, if numerical, are
	 * treated numerically. Otherwise it is lexicographical, which is error
	 * prone, though. Should the 'Scanner' class should be tapped into?
	 * 
	 * @author Steffen Moeller
	 */
	public static int compareVersions(String a, String b) {

		// null pointer exceptions are not risked .. we are nice
		if (null == a)
			a = "";
		if (null == b)
			b = "";

		// catching the dumb case first
		if (a.equals(b))
			return 0;

		List as = Arrays.asList(a.split("[.-]"));
		List bs = Arrays.asList(b.split("[.-]"));

		// both lists have the empty element as members at least
		Iterator aIterator = as.iterator();
		Iterator bIterator = bs.iterator();

		while (aIterator.hasNext()) {
			String aa = (String) aIterator.next();
			if (!bIterator.hasNext()) {
				// a is longer while equal so far
				return 1; // a > b
			}
			String bb = (String) bIterator.next();
			if (!aa.equals(bb)) {
				// a and b differ
				try {
					Integer aInt = Integer.parseInt(aa);
					Integer bInt = Integer.parseInt(bb);
					return aInt.compareTo(bInt);
				} catch (Exception e) {
					return aa.compareTo(bb);
				}
			}
		}
		if (bIterator.hasNext()) {
			// b is longer while equal so far
			return -1; // a < b
		}
		return 0; // a == b
	}

	/**
	 * For testing purposes
	 */
	public static void main(String argv[]) {
		if ("compare".equals(argv[0])) {
			RuntimeEnvironment r1 = new RuntimeEnvironment(argv[1]);
			RuntimeEnvironment r2 = new RuntimeEnvironment(argv[2]);
			logger.info("r1.getName(): " + r1.getName());
			logger.info("r1.getVersion(): " + r1.getVersion());
			logger.info("r2.getName(): " + r2.getName());
			logger.info("r2.getVersion(): " + r2.getVersion());
			logger.info("r1.atLeastAsCapableAs(r2): " + String.valueOf(r1.atLeastAsCapableAs(r2)));
		} else {
			logger.info("Don't know how to '" + argv[0] + "'");
		}
	}

}
