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

import java.util.Map;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import org.apache.taverna.wsdl.parser.UnknownOperationException;
import org.apache.taverna.wsdl.parser.WSDLParser;
import org.apache.taverna.wsdl.parser.schema.XSComponent;
import org.apache.taverna.wsdl.parser.schema.XSModel;
import org.apache.taverna.wsdl.parser.schema.XSNode;

/**
 * @author Dmitry Repchevsky
 */

public abstract class AbstractSOAPInvoker implements SOAPInvoker {
    private final XSModel<XSComponent, XSComponent> input, output;

    public AbstractSOAPInvoker(WSDLParser parser, String port, String operationXPointer) throws UnknownOperationException {
        input = parser.getInputDataModel(operationXPointer);
        output = parser.getInputDataModel(operationXPointer);
    }


    protected void writeInputs(XMLStreamWriter writer, Map<String, Object> inputs) throws XMLStreamException {
        for (Map.Entry<String, Object> entry : inputs.entrySet()) {
            XSNode node = input.findNode(entry.getKey());
            if (node != null) {
                node.setUserObject(entry.getValue());
            }
        }
        
        input.write(writer);
    }
    
    protected void readOutputs(XMLStreamReader reader, Map<String, Object> outputs) throws XMLStreamException {
        output.read(reader);
        addOutputs(output, outputs);
    }
    
    private void addOutputs(XSNode<XSComponent, XSComponent> node, Map<String, Object> outputs) {
        for (int i = 0, n = node.getChildCount(); i < n; i++) {
            XSComponent component = node.getChildAt(i);
            Object value = component.getUserObject();
            if (value != null && value.toString().length() > 0) {
                outputs.put(component.getXPath(), value);
            }
            addOutputs(component, outputs);
        }
    }
}
