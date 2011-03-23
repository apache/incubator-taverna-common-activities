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
 
public interface ProgressDisplay {
	// long staying messages, like user popups
	public abstract Object getMessage(String message);
	public abstract void changeMessage(Object messageObject, String message);
	public abstract void removeMessage(Object messageObject);
	// logging
	/* levels are:
	 * 0 = ERROR
	 * 1 = WARNING
	 * 2 = INFO (like job submitted)
	 * 3 = PROGRESS (like still waiting for job)
	 * 4 = DEBUG (for example raw data dumps)
	 */
	public abstract void log(int level, String message);
	public abstract void logTrace(int level, Throwable logMyTrace);
	
	public abstract void annoyTheUser(String message);
}
