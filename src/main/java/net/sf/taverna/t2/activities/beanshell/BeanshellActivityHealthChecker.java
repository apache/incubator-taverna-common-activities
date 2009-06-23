/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester   
 * 
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *    
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *    
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
package net.sf.taverna.t2.activities.beanshell;

import java.io.StringReader;

import net.sf.taverna.t2.workflowmodel.health.HealthChecker;
import net.sf.taverna.t2.workflowmodel.health.HealthReport;
import net.sf.taverna.t2.workflowmodel.health.HealthReport.Status;
import bsh.ParseException;
import bsh.Parser;

public class BeanshellActivityHealthChecker implements HealthChecker<BeanshellActivity> {

	public boolean canHandle(Object subject) {
		return (subject!=null && subject instanceof BeanshellActivity);
	}

	public HealthReport checkHealth(BeanshellActivity activity) {
		Parser parser = new Parser(new StringReader(activity.getConfiguration().getScript()));
		try {
			while (!parser.Line());
		} catch (ParseException e) {
			return new HealthReport("Beanshell Activity",e.getMessage(),Status.SEVERE);
		}
		return new HealthReport("Beanshell Acitivity","Parsed OK",Status.OK);
	}

}
