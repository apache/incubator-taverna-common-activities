/*******************************************************************************
 * Copyright (C) 2009 The University of Manchester   
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
package net.sf.taverna.t2.activities.spreadsheet;

/**
 * Enumeration of the formats for output of spreadsheet cell values.
 * <p>
 * <dl>
 * <dt>PORT_PER_COLUMN</dt>
 * <dd>One port of depth 1 (a list) per column</dd>
 * <dt>SINGLE_PORT</dt>
 * <dd>A single port of depth 0, formatted as CSV</dd>
 * </dl>
 *
 * @author David Withers
 */
public enum SpreadsheetOutputFormat {
	PORT_PER_COLUMN, SINGLE_PORT
}
