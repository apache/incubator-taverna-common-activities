/**
 * *****************************************************************************
 * Copyright (C) 2012 Spanish National Bioinformatics Institute (INB),
 * Barcelona Supercomputing Center and The University of Manchester
 *
 * Modifications to the initial code base are copyright of their respective
 * authors, or their employers as appropriate.
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307
 *****************************************************************************
 */

package net.sf.taverna.wsdl.parser;

/**
 * @author Dmitry Repchevsky
 */

public enum WSRF_Version {
    
    Draft01("http://schemas.xmlsoap.org/ws/2004/08/addressing",
            "http://docs.oasis-open.org/wsrf/2004/06/wsrf-WS-ResourceProperties-1.2-draft-01.xsd",
            "http://docs.oasis-open.org/wsrf/2004/06/wsrf-WS-ResourceLifetime-1.2-draft-01.xsd"), // 2004_10_01

    Draft02("http://schemas.xmlsoap.org/ws/2004/08/addressing",
            "http://docs.oasis-open.org/wsrf/2004/11/wsrf-WS-ResourceProperties-1.2-draft-05.xsd", 
            "http://docs.oasis-open.org/wsrf/2004/11/wsrf-WS-ResourceLifetime-1.2-draft-04.xsd"), // 2004_12_09

    Draft03("http://schemas.xmlsoap.org/ws/2004/08/addressing", 
            "http://docs.oasis-open.org/wsrf/2005/03/wsrf-WS-ResourceProperties-1.2-draft-06.xsd",
            "http://docs.oasis-open.org/wsrf/2005/03/wsrf-WS-ResourceLifetime-1.2-draft-05.xsd"), // 2005_03_08
    
    PR01("http://www.w3.org/2005/03/addressing", 
         "http://docs.oasis-open.org/wsrf/rp-1",
         "http://docs.oasis-open.org/wsrf/rl-1"), // 2005_06_10

//    PR02("http://www.w3.org/2005/08/addressing", 
//         "http://docs.oasis-open.org/wsrf/rp-2",
//         "http://docs.oasis-open.org/wsrf/rl-2"), // 2005_10_06
    
    Standard("http://www.w3.org/2005/08/addressing", 
             "http://docs.oasis-open.org/wsrf/rp-2", 
             "http://docs.oasis-open.org/wsrf/rl-2"); // 2006_04_01
    
    public final String WSA;
    
    public final String WSRF_RP;
    public final String WSRF_RL;

    WSRF_Version(String wsa, String wsrf_rp, String wsrf_rl) {
        WSA = wsa;

        WSRF_RP = wsrf_rp;
        WSRF_RL = wsrf_rl;
    }
}

