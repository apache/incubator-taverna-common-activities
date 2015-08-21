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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.xml.bind.DatatypeConverter;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaAll;
import org.apache.ws.commons.schema.XmlSchemaAttribute;
import org.apache.ws.commons.schema.XmlSchemaChoice;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.apache.ws.commons.schema.XmlSchemaComplexContentExtension;
import org.apache.ws.commons.schema.XmlSchemaComplexContentRestriction;
import org.apache.ws.commons.schema.XmlSchemaComplexType;
import org.apache.ws.commons.schema.XmlSchemaContent;
import org.apache.ws.commons.schema.XmlSchemaContentModel;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaObject;
import org.apache.ws.commons.schema.XmlSchemaParticle;
import org.apache.ws.commons.schema.XmlSchemaSequence;
import org.apache.ws.commons.schema.XmlSchemaSequenceMember;
import org.apache.ws.commons.schema.XmlSchemaSimpleContentExtension;
import org.apache.ws.commons.schema.XmlSchemaSimpleType;
import org.apache.ws.commons.schema.XmlSchemaType;
import org.apache.ws.commons.schema.constants.Constants;

/**
 * XML Schema model class that represent the root node and contains a set of auxiliary
 * methods to manage the tree.
 * 
 * @author Dmitry Repchevsky
 */

public class XSModel<T, V extends T> extends XSNode<T,V> {
    private XmlSchemaCollection schemas;

    public XSModel() {
        schemas = new XmlSchemaCollection();
    }

    public XSModel(XmlSchemaCollection schemas) {
        this.schemas = schemas;
    }
    
    /**
     * Binds XML Schemas to the model. Execution of this method cleans the model (tree).
     * 
     * @param schemas new XML Schemas this model is based on.
     */
    public void setSchemaCollection(XmlSchemaCollection schemas) {
        removeAllChildren();
        this.schemas = schemas;
    }
    
    /**
     * Finds a node that corresponds to the provided XPath
     * 
     * @param xpath XPath expression for the queried node
     * 
     * @return the node in the model that corresponds to the XPath query
     */
    public XSNode<T,V> findNode(String xpath) {
        return findComponent(this, xpath);
    }
    
    private XSComponent findComponent(XSNode<T,V> node, String xpath) {
        if (node instanceof XSComponent) {
            XSComponent component = (XSComponent)node;
            if (xpath.equals(component.getXPath())) {
                return component;
            }
        }
        
        for (int i = 0, n = node.getChildCount(); i < n; i++) {
            XSNode<T, V> childNode = (XSNode<T, V>)node.getChildAt(i);
            XSComponent component = findComponent(childNode, xpath);
            if (component != null) {
                return component;
            }
        }
        
        return null;
    }

    /**
     * Includes the element (with all its subelements obtained from the schema) to the model.
     *
     * @param element the element name to include into the model.
     */
    public void addGlobalElement(QName element) {
        XSComponent node = (XSComponent)findElement(element);

        if (node != null) {
            parse(node);
            insert((V)node, getChildCount());
        }
    }

    /**
     * Includes the type (with all its subelements obtained from the schema) to the model.<br/>
     * Note that the model will serialize this global type as if it was the element of the given type.<br/>
     * 
     * @param type a global type name to add to the model
     * @param name the top level name to be serialized. 
     */
    public void addGlobalType(QName type, QName name) {
        XSGlobalType node = findType(type, name);
        if (node != null) {
            parse(node);
            insert((V)node, getChildCount());
        }
    }

    /**
     * Validates the model tree against provided XML Schemas.
     * Basically it validates whether all obligatory components have their values set.
     * 
     * @return true if model is valid, false otherwise.
     */
    public boolean validate() {
        Boolean valid = null;

        for (int i = 0, n = getChildCount(); i < n; i++) {
            XSComponent node = (XSComponent)getChildAt(i);

            Boolean b = node.validate();
            if (Boolean.FALSE.equals(b)) {
                valid = Boolean.FALSE;
                break;
            }

            if (valid == null) {
                valid = b;
            }
        }
        return valid != null && valid;
    }

    /**
     * Constructs the model based on XML obtained from the provided stream
     *
     * @param stream The XML reader where the XML is read from
     * @throws javax.xml.stream.XMLStreamException
     */
    public void read(XMLStreamReader stream) throws XMLStreamException {
        removeAllChildren();

        readElement(this, stream);
    }

    /**
     * Constructs an XML based on the data provided by the model.
     * 
     * @param stream XML writer to write generated XML
     * @throws javax.xml.stream.XMLStreamException
     */
    public void write(XMLStreamWriter stream) throws XMLStreamException {
        for (int i = 0, n = getChildCount(); i < n; i++) {
            XSComponent node = (XSComponent)getChildAt(i);
            node.write(stream);
        }
    }

    protected XSParticle newParticle(XmlSchemaParticle particle) {
        return new XSParticle(particle);
    }
    
    protected XSGlobalElement newGlobalElement(XmlSchemaElement element) {
        return new XSGlobalElement(element);
    }

    protected XSGlobalType newGlobalType(XmlSchemaType type, QName name) {
        return new XSGlobalType(type, name);
    }
    
    protected XSType newType(XmlSchemaType type) {
        return new XSType(type);
    }
    
    protected XSAttribute newAttribute(XmlSchemaAttribute attribute) {
        return new XSAttribute(attribute);
    }

    protected XSMixedText newMixedText() {
        return new XSMixedText();
    }
    /**
     * Builds the tree branch (child nodes) for the node.
     */
    private void parse(XSComponent node) {
        XmlSchemaType type = node.getType();
        if (type instanceof XmlSchemaComplexType) {
            addComplexType(node, (XmlSchemaComplexType)type);
        }
    }

    private void readElement(XSNode parent, XMLStreamReader reader) throws XMLStreamException {
        StringBuilder text = new StringBuilder();

        while (reader.hasNext()) {
            int eventType = reader.next();
            if (eventType == XMLStreamReader.END_ELEMENT) {
                if (text.length() > 0 && parent instanceof XSComponent) {
                    XSComponent node = (XSComponent)parent;
                    if (XSModel.getSimpleType(node) != null) {
                        node.setUserObject(text.toString());
                    }
                }
                return;
            } else if (eventType == XMLStreamReader.START_ELEMENT) {
                QName elementName = reader.getName();

                if (parent.getParent() == null) {
                    XSGlobalElement node = findElement(elementName);
                    if (node != null) {
                        parent.insert(node, parent.getChildCount());
                        readAttributes(node, reader);
                        readElement(node, reader);
                    }
                } else {
                    if (parent instanceof XSParticle) {
                        XSParticle<XSNode, XSNode> node = (XSParticle)parent;
                        XmlSchemaParticle particle = node.getXSComponent();
                        if (particle.getMaxOccurs() > 1) {
                            if (elementName.equals(node.getName())) {
                                XSType typeNode = newType(node.getType());
                                node.insert(typeNode, node.getChildCount());
                                readAttributes(typeNode, reader);
                                readElement(typeNode, reader);
                                continue;
                            }

                            // we have reached a last element in a repeated sequence...
                            // move up to one level
                            parent = node.getParent();
                        }
                    }

                    XSComponent node = (XSComponent)parent;
                    XmlSchemaType type = node.getType();

                    if (type instanceof XmlSchemaComplexType) {
                        Map<QName, XmlSchemaElement> elements = getElements(node);
                        XmlSchemaElement element = elements.get(elementName);
                        if (element != null) {
                            XSParticle particleNode = newParticle(element);
                            node.insert(particleNode, node.getChildCount());

                            if (element.getMaxOccurs() > 1) {
                                XSType typeNode = newType(particleNode.getType());
                                particleNode.insert(typeNode, particleNode.getChildCount());
                                parent = particleNode;

                                readAttributes(typeNode, reader);
                                readElement(typeNode, reader);
                            } else {
                                readAttributes(particleNode, reader);
                                readElement(particleNode, reader);
                            }
                        }
                    } else {
                        throw new XMLStreamException("Simple type " + type.getQName() + " cannot have children! ");
                    }
                }
            } else if (parent.getParent() != null && 
                      (eventType == XMLStreamReader.CHARACTERS ||
                       eventType == XMLStreamReader.CDATA)) {
                text.append(reader.getText()); //.append(LINE_SEPARATOR);

                XSComponent node = (XSComponent)parent;
                XmlSchemaType type = node.getType();

                if (type instanceof XmlSchemaComplexType) {
                    XmlSchemaComplexType complexType = (XmlSchemaComplexType)type;
                    if (complexType.isMixed()) {
                        XSMixedText textNode = newMixedText();
                        parent.insert(textNode, parent.getChildCount());

                        textNode.setUserObject(text.toString());
                        text.setLength(0);
                    }
                }
            }
        }
    }

    private void readAttributes(XSComponent node, XMLStreamReader reader) throws XMLStreamException {
        Map<QName, XmlSchemaAttribute> attributes = getAttributes(node);
        
        for (int i = 0, n = reader.getAttributeCount(); i < n; i++) {
            QName attributeName = reader.getAttributeName(i);

            XmlSchemaAttribute attribute = attributes.get(attributeName);
            if (attribute != null) {
                XSAttribute attributeNode = newAttribute(attribute);
                node.insert(attributeNode, node.getChildCount());
                String value = reader.getAttributeValue(i);
                if (value != null && value.length() > 0) {
                    XmlSchemaSimpleType simpleType = XSModel.getSimpleType(attributeNode);
                    if (Constants.XSD_QNAME.equals(simpleType.getQName())) {
                        QName qname = DatatypeConverter.parseQName(value, reader.getNamespaceContext());
                        attributeNode.setUserObject(qname);
                    } else {
                        attributeNode.setUserObject(value);
                    }
                }
            }
        }
    }
    
    private XSGlobalElement findElement(QName elementName) {
        XmlSchemaElement element = schemas.getElementByQName(elementName);
        if (element != null) {
            return newGlobalElement(element);
        }
        return null;
    }

    private XSGlobalType findType(QName typeName, QName partName) {
        XmlSchemaType type = schemas.getTypeByQName(typeName);
        if (type != null) {
            return newGlobalType(type, partName);
        }
        return null;
    }

    private void addComplexType(XSComponent component, XmlSchemaComplexType complexType) {
        Map<QName, XmlSchemaAttribute> attributes = new LinkedHashMap<QName, XmlSchemaAttribute>();
        XSParser.addAttributes(attributes, complexType.getAttributes());

        XmlSchemaParticle particle = complexType.getParticle();
        if (particle != null) {
            addParticle(component, particle);
        } else {
            XmlSchemaContentModel contentModel = complexType.getContentModel();
            if (contentModel != null) {
                XmlSchemaContent content = contentModel.getContent();
                if (content instanceof XmlSchemaComplexContentExtension) {
                    XmlSchemaComplexContentExtension complexContentExtension = (XmlSchemaComplexContentExtension)content;
                    XSParser.addAttributes(attributes, complexContentExtension.getAttributes());
                    
                    final QName baseTypeName = complexContentExtension.getBaseTypeName();
                    final XmlSchemaType baseType;
                    final XmlSchema xmlSchema = complexType.getParent();
                    final XmlSchemaCollection xmlSchemaCollection = xmlSchema.getParent();
                    if (xmlSchemaCollection != null) {
                        baseType = xmlSchemaCollection.getTypeByQName(baseTypeName);
                    } else {
                        baseType = xmlSchema.getTypeByName(baseTypeName);
                    }
                    addComplexType(component, (XmlSchemaComplexType)baseType);
                    
                    particle = complexContentExtension.getParticle();
                    addParticle(component, particle);
                } else if (content instanceof XmlSchemaComplexContentRestriction) {
                    XmlSchemaComplexContentRestriction complexContentRestriction = (XmlSchemaComplexContentRestriction)content;
                    XSParser.addAttributes(attributes, complexContentRestriction.getAttributes());
                    particle = complexContentRestriction.getParticle();
                    addParticle(component, particle);
                } else if (content instanceof XmlSchemaSimpleContentExtension) {
                    XmlSchemaSimpleContentExtension simpleContentExtension = (XmlSchemaSimpleContentExtension)content;
                    XSParser.addAttributes(attributes, simpleContentExtension.getAttributes());
                } 
            }
        }
        
        for (XmlSchemaAttribute attribute : attributes.values()) {
            XSAttribute child = newAttribute(attribute);
            String value = attribute.getDefaultValue();
            if (value != null) {
                child.setUserObject(value);
            }
            component.insert((V)child, 0);
        }
    }

    private Map<QName, XmlSchemaAttribute> getAttributes(XSComponent component) {
        Map<QName, XmlSchemaAttribute> attributes = new LinkedHashMap<QName, XmlSchemaAttribute>();
        XmlSchemaType type = component.getType();
        if (type instanceof XmlSchemaComplexType) {
            XSParser.addAttributes(attributes, (XmlSchemaComplexType)type);
        }
        return attributes;
    }

    private static Map<QName, XmlSchemaElement> getElements(XSComponent component) {
        Map<QName, XmlSchemaElement> elements = new LinkedHashMap();
        XmlSchemaType type = component.getType();
        if (type instanceof XmlSchemaComplexType) {
            XSParser.addElements(elements, (XmlSchemaComplexType)type);
        }
        return elements;
    }

    /**
     * Returns closest simple type that corresponds to this node text content.
     * In simple it returns a simple type if node is editable or null otherwise.
     * 
     * @param component
     * 
     * @return a simple type for the node content or null.
     */
    public final static XmlSchemaSimpleType getSimpleType(XSComponent component) {
        final XmlSchemaType type = component.getType();
        return type == null ? null : XSParser.getSimpleType(type);
    }
    
    public void addParticle(XSComponent component, XmlSchemaParticle particle) {
        if (particle instanceof XmlSchemaElement) {
            XSParticle node = newParticle(particle);
            component.insert((V)node, component.getChildCount());

            XmlSchemaElement element = (XmlSchemaElement)particle;
            String value = element.getDefaultValue();
            if (value != null) {
                node.setUserObject(value);
            }
            
            if (element.getMaxOccurs() <= 1) {
                parse(node);
            } else {
                XmlSchemaType type = node.getType();
                XSType tNode = newType(type);
                parse(tNode);
                node.insert(tNode, node.getChildCount());
//                for (int i = 0, n = Math.max(1, (int)particle.getMinOccurs()); i < n; i++) {
//                    XSTypeNode tNode = new XSTypeNode(type);
//                    node.add(tNode);
//                }
            }
        } else if (particle instanceof XmlSchemaSequence) {
            XmlSchemaSequence sequence = (XmlSchemaSequence)particle;
            List<XmlSchemaSequenceMember> items = sequence.getItems();
            for (XmlSchemaSequenceMember item : items) {
                addParticle(component, (XmlSchemaParticle)item);
            }
        } else if (particle instanceof XmlSchemaAll) {
            XmlSchemaAll all = (XmlSchemaAll)particle;
            List<XmlSchemaElement> elements = all.getItems();
            for (XmlSchemaElement element : elements) {
                addParticle(component, element);
            }
        } else if (particle instanceof XmlSchemaChoice) {
            XmlSchemaChoice xmlSchemaChoice = (XmlSchemaChoice)particle;
            List<XmlSchemaObject> items = xmlSchemaChoice.getItems();
            
            // for the choice add only one element
            addParticle(component, (XmlSchemaParticle)items.get(0));
        }
    }
}
