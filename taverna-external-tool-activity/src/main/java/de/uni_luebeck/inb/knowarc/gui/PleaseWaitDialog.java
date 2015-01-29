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

import java.util.ArrayList;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JLabel;

public class PleaseWaitDialog extends JDialog {
	private static final long serialVersionUID = 1L;

	static PleaseWaitDialog instance = new PleaseWaitDialog();

	public class WaitMessage{
		public WaitMessage() {
		}
		
		String str;
		public void set(String newstr) {
			synchronized (PleaseWaitDialog.instance) {
				if(this.str != null && this.str.equals(newstr)) return;
				if(this.str != null) messages.remove(this.str);
				this.str = newstr;
				if(this.str != null) messages.add(this.str);
			}
			updateDialog();
		}
		public void done() {
			set(null);
		}
	}

	private void updateDialog() {
		synchronized (PleaseWaitDialog.instance) {
			if(messages.size() > 0) this.setVisible(true);
			else this.setVisible(false);
			String t = "";
			for (String  cur : messages) {
				t += cur + "<br>";
			}
			l.setText("<html>"+t+"</html>");
		}
	}
	
	List<String> messages = new ArrayList<String>();
	
	JLabel l;
	private PleaseWaitDialog() {
		this.setAlwaysOnTop(true);
		this.setTitle("Please wait");
		l = new JLabel("");
		this.add(l);
		this.setSize(400, 100);
	}
}
