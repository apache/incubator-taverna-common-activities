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
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.apache.ws.commons.schema.XmlSchemaComplexContent;
import org.apache.ws.commons.schema.XmlSchemaComplexContentExtension;
import org.apache.ws.commons.schema.XmlSchemaComplexContentRestriction;
import org.apache.ws.commons.schema.XmlSchemaComplexType;
import org.apache.ws.commons.schema.XmlSchemaContent;
import org.apache.ws.commons.schema.XmlSchemaContentModel;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaParticle;
import org.apache.ws.commons.schema.XmlSchemaSimpleType;
import org.apache.ws.commons.schema.XmlSchemaType;

/**
 * The node that represents a global XML Schema element tree node.
 * This node should be an immediate child of XSModel node.
 * 
 * @author Dmitry Repchevsky
 */

public class XSGlobalElement<T,V extends T> extends XSComponent<T,V, XmlSchemaElement> {
    
    public XSGlobalElement(XmlSchemaElement element) {
        super(element);
    }

    public XSGlobalElement(XmlSchemaElement element, Object value) {
        super(element, value);
    }
    
    @Override
    public XmlSchemaType getType() {
        XmlSchemaElement element = XSParser.getElement(component);
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
    }

    @Override
    public QName getName() {
        return component.getQName();
    }

    @Override
    public Boolean validate() {
        final int n = getChildCount();

        // if there is no child nodes in the node suppose it's ok.
        if (n == 0) {
            return Boolean.TRUE;
        }

        Boolean valid = null;

        for (int i = 0; i < n; i++) {
            XSComponent child = (XSComponent)getChildAt(i);
            Boolean b = child.validate();
            if (Boolean.FALSE.equals(b)) {
                valid = Boolean.FALSE;
                break;
            }
            if (valid == null) {
                valid = b;
            }
        }

        return valid == null ? (component.isNillable() ? null : Boolean.FALSE) : Boolean.TRUE;
    }

    @Override
    public void write(XMLStreamWriter stream) throws XMLStreamException {
        Boolean isValid = validate();
        if (isValid != null && isValid) {
            XmlSchemaType type = component.getSchemaType();
            if (type instanceof XmlSchemaSimpleType) {
                writeType(stream);
            } else if (type instanceof XmlSchemaComplexType) {
                XmlSchemaComplexType complexType = (XmlSchemaComplexType)type;
                XmlSchemaParticle particle = complexType.getParticle();
                if (particle == null) {
                    final XmlSchemaContentModel contentModel = complexType.getContentModel();
                    if (contentModel instanceof XmlSchemaComplexContent) {
                        XmlSchemaContent content = contentModel.getContent();
                        if (content instanceof XmlSchemaComplexContentExtension) {
                            XmlSchemaComplexContentExtension complexContentExtension = (XmlSchemaComplexContentExtension)content;
                            particle = complexContentExtension.getParticle();
                        } else if (content instanceof XmlSchemaComplexContentRestriction) {
                            XmlSchemaComplexContentRestriction complexContentRestriction = (XmlSchemaComplexContentRestriction)content;
                            particle = complexContentRestriction.getParticle();
                        }
                    }
                }
                
                if (particle != null && particle.getMaxOccurs() > 1) {
                    writeModelGroup(stream);
                } else {
                    writeType(stream);
                }
            }
        }
    }

    /**
     * Write the element represented by this node to the stream.
     * 
     * @param stream
     * @throws XMLStreamException 
     */
    private void writeType(XMLStreamWriter stream) throws XMLStreamException {

        final QName qname = component.getQName();

        final String localName = qname.getLocalPart();
        final String namespace = qname.getNamespaceURI();

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
            XmlSchemaSimpleType simpleType = XSModel.getSimpleType(this);
            if (simpleType != null) {
                stream.writeCharacters(object.toString());
            }
        }

        stream.writeEndElement();
    }

    private void writeModelGroup(XMLStreamWriter stream) throws XMLStreamException {
        final QName name = component.getQName();

        final String localName = name.getLocalPart();
        final String namespace = name.getNamespaceURI();

        for (int i = 0, n = getChildCount(); i < n; i++) {
            if (namespace != null && namespace.length() > 0) {
                stream.writeStartElement(namespace, localName);
            } else {
                stream.writeStartElement(localName);
            }
            
            XSComponent child = (XSComponent)getChildAt(i);
            child.write(stream);

            stream.writeEndElement();
        }
    }
    
    @Override
    public String getXPath() {
        final StringBuilder xpath = new StringBuilder("/");
        final QName qname = getName();
        final String localpart = qname.getLocalPart();
        final String namespace = qname.getNamespaceURI();
        if (namespace.isEmpty()) {
            xpath.append(localpart);
        } else {
            xpath.append("*[namespace-uri()='").append(namespace).append("' and local-name()='").append(localpart).append("']");
        }
        return xpath.toString();
    }
}
