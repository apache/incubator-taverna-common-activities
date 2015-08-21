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

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaObject;
import org.apache.ws.commons.schema.XmlSchemaType;
import org.apache.ws.commons.schema.utils.NamespacePrefixList;
import org.apache.ws.commons.schema.utils.XmlSchemaNamed;

/**
 * An abstract implementation of XML Schema component tree node.
 * 
 * @author Dmitry Repchevsky
 */

public abstract class XSComponent<T,V extends T, U extends XmlSchemaObject> extends XSNode<T,V> {
    protected U component;
    
    public XSComponent(U component) {
        this(component, null);
    }

    public XSComponent(U component, Object value) {
        this.component = component;
    }

    public U getXSComponent() {
        return component;
    }

    public final QName getTypeName() {
        XmlSchemaType type = getType();

        // the type could be anonymouse
        QName qname = type.getQName();
        
        return qname != null ? new QName(qname.getNamespaceURI(), qname.getLocalPart()) : new QName("");
    }
    
    public abstract QName getName();
    public abstract XmlSchemaType getType();

    public abstract Boolean validate();
    public abstract void write(XMLStreamWriter stream) throws XMLStreamException;
    public abstract String getXPath();

    protected String getPrefix(XMLStreamWriter stream) throws XMLStreamException {
        final QName qname = getName();
        final String namespace = qname.getNamespaceURI();

        if (namespace.length() > 0) {
            NamespaceContext ctx = stream.getNamespaceContext();
            if (ctx == null) {
                // warning !!!
            } else {
                String prefix = ctx.getPrefix(namespace);
                if (prefix == null && component instanceof XmlSchemaNamed) {
                    XmlSchemaNamed named = (XmlSchemaNamed)component;
                    XmlSchema schema = named.getParent();

                    NamespacePrefixList namespaces = schema.getNamespaceContext();
                    if (namespaces != null) {
                        prefix = namespaces.getPrefix(namespace);
                        if (prefix != null) {
                            final String nmsp = ctx.getNamespaceURI(prefix);
                            if (nmsp != null && !nmsp.equals(namespace)) {
                                prefix = null; // prefix is already in use
                            }
                        }
                    }
                }
                if (prefix == null) {
                    prefix = "ns0";
//                  JAVA BUG !!! getNamespaceURI() should never return null
//                  for (int i = 1; !XMLConstants.NULL_NS_URI.equals(ctx.getNamespaceURI(prefix)); i++) {
                    for (int i = 1;; i++) {
                        final String nmsp = ctx.getNamespaceURI(prefix);
                        if (nmsp == null || XMLConstants.NULL_NS_URI.equals(nmsp)) {
                            break;
                        }
                        prefix = "ns" + i;
                    }
                }
                //stream.setPrefix(prefix, namespace);
                return prefix;
            }
        }
        return null;
    }
}
