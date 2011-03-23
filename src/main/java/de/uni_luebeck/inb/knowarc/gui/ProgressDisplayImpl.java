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

package de.uni_luebeck.inb.knowarc.gui;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import de.uni_luebeck.inb.knowarc.grid.re.RuntimeEnvironmentConstraint;
import de.uni_luebeck.inb.knowarc.gui.PleaseWaitDialog.WaitMessage;

public class ProgressDisplayImpl implements de.uni_luebeck.inb.knowarc.grid.ProgressDisplay{
	
	private static Logger logger = Logger.getLogger(ProgressDisplayImpl.class);
	
	private KnowARCConfiguration configuration;
	public ProgressDisplayImpl(KnowARCConfiguration configuration) {
		this.configuration = configuration;
	} 
	
	public void changeMessage(Object messageObject, String message) {
		((WaitMessage)messageObject).set(message);
	}

	public Object getMessage(String message) {
		WaitMessage msg = PleaseWaitDialog.instance.new WaitMessage();
		msg.set(message);
		return msg;
	}

	public void log(int level, String message) {
		configuration.debug(level, message);
	}

	public void logTrace(int level, Throwable logMyTrace) {
		StringWriter wr = new StringWriter();
		logMyTrace.printStackTrace(new PrintWriter(wr));
		wr.flush();
		log(level, wr.toString());
	}

	public void removeMessage(Object messageObject) {
		((WaitMessage)messageObject).done();
	}
	public void annoyTheUser(String message) {
		JOptionPane.showMessageDialog(null, message);
	}
}