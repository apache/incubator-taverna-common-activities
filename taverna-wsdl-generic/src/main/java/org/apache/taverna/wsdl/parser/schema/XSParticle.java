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
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaAny;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaParticle;
import org.apache.ws.commons.schema.XmlSchemaType;

/**
 * Represents an XML element node which is based on XML Schema particle.
 * 
 * @author Dmitry Repchevsky
 */

public class XSParticle<T,V extends T> extends XSComponent<T,V, XmlSchemaParticle> {

    public XSParticle(XmlSchemaParticle particle) {
        super(particle);
    }

    @Override
    public XmlSchemaType getType() {
        if (component instanceof XmlSchemaElement) {
            XmlSchemaElement element = XSParser.getElement((XmlSchemaElement)component);
            XmlSchemaType type = element.getSchemaType();
            if (type == null) {
                QName typeName = element.getSchemaTypeName();
                XmlSchema schema = element.getParent();
                XmlSchemaCollection schemaCollection = schema.getParent();
                if (schemaCollection != null) {
                    type = schemaCollection.getTypeByQName(typeName);
                } else {
                    type = schema.getTypeByName(typeName);
                }
            }
            
            return type;
        } else if (component instanceof XmlSchemaAny) {
            
        }
        return null; // ??? TODO ???
    }

    @Override
    public QName getName() {
        if (component instanceof XmlSchemaElement) {
            XmlSchemaElement element = (XmlSchemaElement)component; //XSParser.getElement((XmlSchemaElement)component);
            return element.getWireName();
        }
        return null;
    }

    @Override
    public Boolean validate() {
        Boolean isValid;

        final long min = component.getMinOccurs();
        final long max = component.getMaxOccurs();

        if (max > 1) {
            int bad = 0;
            int good = 0;
            int total = getChildCount();

            for (int i = 0; i < total; i++) {
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

            // ALL values must be the same - otherwise FALSE
            isValid = (bad == 0 && good == 0) ? null : bad == total ? null : good == total ? Boolean.TRUE : Boolean.FALSE;
        } else {
            int bad = 0;
            int good = XSModel.getSimpleType(this) != null ? getUserObject() != null ? 1 : 0 : 0;

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

            isValid = good > 0 && bad > 0 ? Boolean.FALSE : bad > 0 ? min == 0 ? null : Boolean.FALSE : good > 0 ? Boolean.TRUE : null;
        }

        return isValid;
    }

    @Override
    public void write(XMLStreamWriter stream) throws XMLStreamException {
        Boolean isValid = validate();
        if (isValid != null && isValid) {
            
            final QName qname = getName();
            final String localName = qname.getLocalPart();
            final String namespace = qname.getNamespaceURI();

            final long max = component.getMaxOccurs();
            if (max > 1) {
                for (int i = 0, n = getChildCount(); i < n; i++) {
                    if (namespace != null && namespace.length() > 0) {
                        final String prefix = getPrefix(stream);
                        if (prefix != null) {
                            stream.writeStartElement(prefix, localName, namespace);
                        } else {
                            stream.writeStartElement(namespace, localName);
                        }
                    } else {
                        stream.writeStartElement(localName);
                    }

                    XSComponent child = (XSComponent)getChildAt(i);
                    child.write(stream);

                    stream.writeEndElement();
                }
            } else {
                if (namespace != null && namespace.length() > 0) {
                    final String prefix = getPrefix(stream);
                    if (prefix != null) {
                        stream.writeStartElement(prefix, localName, namespace);
                    } else {
                        stream.writeStartElement(namespace, localName);
                    }
                } else {
                    stream.writeStartElement(localName);
                }

                for (int i = 0, n = getChildCount(); i < n; i++) {
                    XSComponent child = (XSComponent)getChildAt(i);
                    child.write(stream);
                }

                Object object = getUserObject();
                if (object != null) {
                    stream.writeCharacters(object.toString());
                }

                stream.writeEndElement();
            }
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
            xpath.append('/').append(localpart);
        } else {
            xpath.append("/*[namespace-uri()='").append(namespace).append("' and local-name()='").append(localpart).append("']");
        }
        return xpath.toString();
    }
}
