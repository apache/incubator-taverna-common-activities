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

import javax.xml.bind.DatatypeConverter;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaAttribute;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.apache.ws.commons.schema.XmlSchemaType;
import org.apache.ws.commons.schema.XmlSchemaUse;

/**
 * @author Dmitry Repchevsky
 */

public class XSAttribute<T,V extends T> extends XSComponent<T, V, XmlSchemaAttribute> {
    
    public XSAttribute(XmlSchemaAttribute attribute) {
        this(attribute, null);
    }

    public XSAttribute(XmlSchemaAttribute attribute, Object object) {
        super(attribute, object);
    }

    @Override
    public XmlSchemaType getType() {
        XmlSchemaType simpleType = component.getSchemaType();
        if (simpleType == null) {
            QName typeName = component.getSchemaTypeName();
            XmlSchema schema = component.getParent();
            XmlSchemaCollection schemaCollection = schema.getParent();
            if (schemaCollection != null) {
                simpleType = schemaCollection.getTypeByQName(typeName);
            } else {
                simpleType = schema.getTypeByName(typeName);
            }
        }
        return simpleType;
    }

    @Override
    public QName getName() {
        return component.getWireName();
    }

    @Override
    public Boolean validate() {
        if (getUserObject() != null) {
            return Boolean.TRUE;
        }    
        return XmlSchemaUse.REQUIRED == component.getUse() ? Boolean.FALSE : null;
    }

    @Override
    public void write(XMLStreamWriter stream) throws XMLStreamException {
        QName type = getTypeName();
        Object object = getUserObject();

        if (object != null) {
            if (object instanceof QName) {
                object = DatatypeConverter.printQName((QName)object, stream.getNamespaceContext());
            }
            
            final QName name = getName();
            final String localName = name.getLocalPart();
            final String namespace = name.getNamespaceURI();

            if (namespace != null && namespace.length() > 0) {
                final String prefix = getPrefix(stream);
                if (prefix != null) {
                    stream.writeAttribute(prefix, namespace, localName, object.toString());
                } else {
                    stream.writeAttribute(namespace, localName, object.toString());
                }
            } else {
                stream.writeAttribute(localName, object.toString());
            }
        } else if (XmlSchemaUse.REQUIRED == component.getUse()) {
            throw new XMLStreamException("Required attribute missing: " + type.toString());
        }
    }
    
    @Override
    public String getXPath() {
        final XSComponent parent = (XSComponent)getParent();
        final StringBuilder xpath = new StringBuilder(parent.getXPath());
        final QName qname = getName();
        final String localpart = qname.getLocalPart();
        final String namespace = qname.getNamespaceURI();
        if (namespace.isEmpty()) {
            xpath.append("/@").append(localpart);
        } else {
            xpath.append("/@[namespace-uri()='").append(namespace).append("' and local-name()='").append(localpart).append("']");
        }
        return xpath.toString();
    }
}
