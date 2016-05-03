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
