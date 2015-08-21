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

package org.apache.taverna.wsdl.parser.schema;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.apache.ws.commons.schema.XmlSchemaObject;
import org.apache.ws.commons.schema.XmlSchemaType;

/**
 * @author Dmitry Repchevsky
 */

public class XSMixedText<T,V extends T> extends XSComponent<T,V, XmlSchemaObject> {

    public XSMixedText() {
        super(null);
    }
    
    @Override
    public QName getName() {
        return new QName("");
    }

    @Override
    public XmlSchemaType getType() {
        return null;
    }

    @Override
    public Boolean validate() {
        return null;
    }

    @Override
    public void write(XMLStreamWriter stream) throws XMLStreamException {
        Object object = getUserObject();
        if (object != null) {
            stream.writeCharacters(object.toString());
        }
    }
    
    @Override
    public String getXPath() {
        XSComponent parentNode = (XSComponent)getParent();
        return parentNode.getXPath() + "[" + parentNode.getIndex(this) + "]";
    }
}
