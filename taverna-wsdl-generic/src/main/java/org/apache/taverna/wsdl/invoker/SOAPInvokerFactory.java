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

package org.apache.taverna.wsdl.invoker;

import java.util.Iterator;
import java.util.ServiceLoader;
import org.apache.taverna.wsdl.parser.UnknownOperationException;
import org.apache.taverna.wsdl.parser.WSDLParser;

/**
 * @author Dmitry Repchevsky
 */

public abstract class SOAPInvokerFactory {

    public abstract SOAPInvoker newSOAPInvoker(WSDLParser parser, String portName, String operationXPointer)
            throws UnknownOperationException;
    
    public static SOAPInvokerFactory newInstance() {
        ServiceLoader<SOAPInvokerFactory> loader = ServiceLoader.load(SOAPInvokerFactory.class, SOAPInvokerFactory.class.getClassLoader());
        Iterator<SOAPInvokerFactory> iterator = loader.iterator();

        if (iterator.hasNext()) {
            return iterator.next();
        }
        
        throw new RuntimeException("implementation not found");
    }

}
