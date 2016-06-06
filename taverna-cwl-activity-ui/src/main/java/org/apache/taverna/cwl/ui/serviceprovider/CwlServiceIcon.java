/*******************************************************************************
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *     contributor license agreements.  See the NOTICE file distributed with
 *     this work for additional information regarding copyright ownership.
 *     The ASF licenses this file to You under the Apache License, Version 2.0
 *     (the "License"); you may not use this file except in compliance with
 *     the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *******************************************************************************/
package org.apache.taverna.cwl.ui.serviceprovider;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.apache.taverna.cwl.CwlDumyActivity;

import net.sf.taverna.t2.workbench.activityicons.ActivityIconSPI;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

public class CwlServiceIcon implements ActivityIconSPI {
	private static Icon icon;

	@Override
	public int canProvideIconScore(Activity<?> activity) {
		if (activity instanceof CwlDumyActivity) {
			return DEFAULT_ICON;
		}
		return NO_ICON;
	}

	@Override
	public Icon getIcon(Activity<?> arg0) {
		return getIcon();
	}

	public static Icon getIcon() {
		if (icon == null) {
			icon = new ImageIcon("/Icon/cwl-logo-header.png");
		}
		return icon;
	}
}
