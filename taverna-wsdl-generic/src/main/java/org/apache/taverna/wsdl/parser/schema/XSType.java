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
import org.apache.ws.commons.schema.XmlSchemaType;

/**
 * The node to hold a type value.
 * This node is used as a child of a parent particle (usually element), which
 * cardinality is more than 1. When cardinality of the parent particle is 1 
 * there is no need to make an additional node.
 * 
 * @author Dmitry Repchevsky
 */

public class XSType<T,V extends T> extends XSComponent<T,V, XmlSchemaType> {

    public XSType(XmlSchemaType type) {
        this(type, null);
    }

    public XSType(XmlSchemaType type, Object object) {
        super(type, object);
    }

    @Override
    public XmlSchemaType getType() {
        return component;
    }

    @Override
    public QName getName() {
        return component.getQName();
    }

    public boolean isSimpleType() {
        return XSModel.getSimpleType(this) != null;
    }
    
    @Override
    public Boolean validate() {
        int bad = 0;
        int good = isSimpleType() ? getUserObject() != null ? 1 : 0 : 0;

        for (int i = 0, n = getChildCount(); i < n; i++) {
            XSComponent child = (XSComponent)getChildAt(i);
            Boolean valid = child.validate();
            if (valid != null) {
                if (valid) {
                    good++;
                } else {
                    bad++;
                }
            }
        }

        Boolean isValid = good > 0 ? bad > 0 ? Boolean.FALSE : Boolean.TRUE : null;
        return isValid;
    }

    @Override
    public void write(XMLStreamWriter stream) throws XMLStreamException {
        Boolean isValid = validate();
        if (isValid != null && isValid) {
            // suppose that attributes are ALWAYS before elements
            for (int i = 0, n = getChildCount(); i < n; i++) {
                XSComponent child = (XSComponent)getChildAt(i);
                child.write(stream);
            }

            if (isSimpleType()) {
                Object object = getUserObject();
                if (object != null) {
                    stream.writeCharacters(object.toString());
                }
            }
        }
    }
    
    @Override
    public String getXPath() {
        XSComponent parent = (XSComponent)getParent();
        return parent.getXPath() + "[position()=" + (parent.getIndex(this) + 1) + "]";
    }
}
