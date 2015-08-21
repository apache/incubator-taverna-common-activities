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

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import org.apache.taverna.wsdl.parser.WSDLParser;

/**
 * @author Dmitry Repchevsky
 */

public class CommandLineInvoker {
    
    public static void main(String[] args)  {
        
        String wsdlLocation = null;
        String operation = null;
        String xpath = null;

        String username = null;
        String password = null;
        
        final Map<String, Object> inputs = new HashMap();
        final Map<String, Object> files = new HashMap();
        final Map<String, Object> outputs = new HashMap();
        
        Map<String, Object> parameters = null;
        
        for (String arg : args) {
            switch(arg) {
                case "-u" : xpath = null; username = ""; break;
                case "-p" : xpath = null; password = ""; break;
                case "-wsdl" : xpath = null; wsdlLocation = ""; break;
                case "-operation" : xpath = null; operation = ""; break;
                case "-in" : xpath = null; parameters = inputs; break;
                case "-file" : xpath = null; parameters = files; break;
                case "-out" : xpath = null; parameters = outputs; break;
                default : if (username != null && username.isEmpty()) {
                            username = arg.isEmpty() ? null : arg;
                          } else if (password != null && password.isEmpty()) {
                            password = arg.isEmpty() ? null : arg;
                          } else if (wsdlLocation != null && wsdlLocation.isEmpty()) {
                              wsdlLocation = arg;
                          } else if (operation != null && operation.isEmpty()) {
                              operation = arg;
                          } else if (xpath == null){
                              xpath = arg;
                          } else if (parameters != null) {
                              parameters.put(xpath, arg);
                          } else {
                              throw new IllegalArgumentException();
                          }
            }
        }
        
        if (wsdlLocation == null || wsdlLocation.isEmpty()) {
            throw new IllegalArgumentException("no wsdl location specified");
        }
        
        try {
            WSDLParser parser = new WSDLParser(wsdlLocation);
            
            for(Map.Entry<String, Object> entry : files.entrySet()) {
                final String path = entry.getValue().toString();
                byte[] file = Files.readAllBytes(Paths.get(path));
                inputs.put(entry.getKey(), new String(file));
            }
            
            SOAPInvokerFactory factory = SOAPInvokerFactory.newInstance();
            SOAPInvoker invoker = factory.newSOAPInvoker(parser, null, operation);
            
            if (username != null && password != null) {
                invoker.setCredentials(username, password);
            }
            
            Map<String, Object> result = invoker.invoke(inputs);
        
            for(Map.Entry<String, Object> entry : outputs.entrySet()) {
                final String key = entry.getKey();
                final String path = entry.getValue().toString();

                String value = result.get(key).toString();
                Files.write(Paths.get(path), value.getBytes(), StandardOpenOption.CREATE);
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
