/*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.apache.taverna.wsdl.parser;

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

