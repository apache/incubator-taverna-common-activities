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

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaAttribute;
import org.apache.ws.commons.schema.XmlSchemaAttributeGroup;
import org.apache.ws.commons.schema.XmlSchemaAttributeGroupMember;
import org.apache.ws.commons.schema.XmlSchemaAttributeGroupRef;
import org.apache.ws.commons.schema.XmlSchemaAttributeOrGroupRef;
import org.apache.ws.commons.schema.XmlSchemaChoice;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.apache.ws.commons.schema.XmlSchemaComplexContentExtension;
import org.apache.ws.commons.schema.XmlSchemaComplexContentRestriction;
import org.apache.ws.commons.schema.XmlSchemaComplexType;
import org.apache.ws.commons.schema.XmlSchemaContent;
import org.apache.ws.commons.schema.XmlSchemaContentModel;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaGroup;
import org.apache.ws.commons.schema.XmlSchemaGroupParticle;
import org.apache.ws.commons.schema.XmlSchemaGroupRef;
import org.apache.ws.commons.schema.XmlSchemaParticle;
import org.apache.ws.commons.schema.XmlSchemaSequence;
import org.apache.ws.commons.schema.XmlSchemaSimpleContentExtension;
import org.apache.ws.commons.schema.XmlSchemaSimpleType;
import org.apache.ws.commons.schema.XmlSchemaType;
import org.apache.ws.commons.schema.utils.XmlSchemaObjectBase;
import org.apache.ws.commons.schema.utils.XmlSchemaRef;

/**
 * @author Dmitry Repchevsky
 */

public class XSParser {

    public static void addElements(Map<QName, XmlSchemaElement> elements, XmlSchemaComplexType complexType) {
        XmlSchemaParticle particle = complexType.getParticle();
        if (particle != null) {
            addElement(elements, Arrays.asList(particle));
        } else {
            XmlSchemaContentModel contentModel = complexType.getContentModel();
            if (contentModel != null) {
                XmlSchemaContent content = contentModel.getContent();
                if (content instanceof XmlSchemaComplexContentExtension) {
                    XmlSchemaComplexContentExtension complexContentExtension = (XmlSchemaComplexContentExtension)content;
                    QName baseTypeName = complexContentExtension.getBaseTypeName();

                    XmlSchema schema = complexType.getParent();
                    XmlSchemaCollection schemaCollection = schema.getParent();

                    final XmlSchemaType baseType;
                    if (schemaCollection != null) {
                        baseType = schemaCollection.getTypeByQName(baseTypeName);
                    } else {
                        baseType = schema.getTypeByName(baseTypeName);
                    }

                    if (baseType instanceof XmlSchemaComplexType) {
                        addElements(elements, (XmlSchemaComplexType)baseType);
                    }

                    particle = complexContentExtension.getParticle();
                    if (particle != null) {
                        addElement(elements, Arrays.asList(particle));
                    }
                } else if (content instanceof XmlSchemaComplexContentRestriction) {
                    XmlSchemaComplexContentRestriction complexContentRestriction = (XmlSchemaComplexContentRestriction)content;
                    particle = complexContentRestriction.getParticle();
                    if (particle != null) {
                        addElement(elements, Arrays.asList(particle));
                    }
                }
            }
        }
    }

    private static void addElement(Map<QName, XmlSchemaElement> elements, List<? extends XmlSchemaObjectBase> items) {
        for (XmlSchemaObjectBase item : items) {
            if (item instanceof XmlSchemaElement) {
                XmlSchemaElement element = (XmlSchemaElement)item;
                elements.put(element.getWireName(), element); // getElement(element)
            } else if(item instanceof XmlSchemaSequence) {
                final XmlSchemaSequence sequence = (XmlSchemaSequence)item;
                addElement(elements, sequence.getItems());
            } else if (item instanceof XmlSchemaChoice) {
                XmlSchemaChoice choice = (XmlSchemaChoice)item;
                addElement(elements, choice.getItems());
            } else if (item instanceof XmlSchemaGroup) {
                XmlSchemaGroup group = (XmlSchemaGroup)item;
                XmlSchemaGroupParticle groupParticle = group.getParticle();
                addElement(elements, Arrays.asList(groupParticle));
            } else if (item instanceof XmlSchemaGroupRef) {
                XmlSchemaGroupRef groupRef = (XmlSchemaGroupRef)item;
                XmlSchemaGroupParticle groupParticle = groupRef.getParticle();
                addElement(elements, Arrays.asList(groupParticle));
            }
        }
    }

    public static XmlSchemaSimpleType getSimpleType(XmlSchemaType type) {
        if (type instanceof XmlSchemaSimpleType) {
            return (XmlSchemaSimpleType)type;
        }
        
        XmlSchemaComplexType complexType = (XmlSchemaComplexType)type;
        return getSimpleType(complexType);        
    }
    
    private static XmlSchemaSimpleType getSimpleType(XmlSchemaComplexType complexType) {
        XmlSchemaContentModel contentModel = complexType.getContentModel();
        if (contentModel == null) {
            return null;
        }
        
        XmlSchemaContent content = contentModel.getContent();
        
        QName baseTypeName;
        if (content instanceof XmlSchemaComplexContentExtension) {
            XmlSchemaComplexContentExtension complexContentExtension = (XmlSchemaComplexContentExtension)content;
            baseTypeName = complexContentExtension.getBaseTypeName();
        } else if (content instanceof XmlSchemaComplexContentRestriction) {
            XmlSchemaComplexContentRestriction complexContentRestriction = (XmlSchemaComplexContentRestriction)content;
            baseTypeName = complexContentRestriction.getBaseTypeName();
        } else if (content instanceof XmlSchemaSimpleContentExtension) {
            XmlSchemaSimpleContentExtension simpleContentExtension = (XmlSchemaSimpleContentExtension)content;
            baseTypeName = simpleContentExtension.getBaseTypeName();
        } else {
            return null; // shouldn't happen
        }
        
        XmlSchemaType baseType;
        XmlSchema schema = complexType.getParent();
        XmlSchemaCollection schemaCollection = schema.getParent();
        if (schemaCollection != null) {
            baseType = schemaCollection.getTypeByQName(baseTypeName);
        } else {
            baseType = schema.getTypeByName(baseTypeName);
        }
        
        return getSimpleType(baseType);
    }

    public static void addAttributes(Map<QName, XmlSchemaAttribute> attributes, XmlSchemaComplexType complexType) {
        addAttributes(attributes, complexType.getAttributes());

        XmlSchemaContentModel contentModel = complexType.getContentModel();
        if (contentModel != null) {
            XmlSchemaContent content = contentModel.getContent();
            if (content instanceof XmlSchemaComplexContentExtension) {
                XmlSchemaComplexContentExtension complexContentExtension = (XmlSchemaComplexContentExtension)content;
                addAttributes(attributes, complexContentExtension.getAttributes());

                final QName baseTypeName = complexContentExtension.getBaseTypeName();
                final XmlSchemaType baseType;
                final XmlSchema xmlSchema = complexType.getParent();
                final XmlSchemaCollection xmlSchemaCollection = xmlSchema.getParent();
                if (xmlSchemaCollection != null) {
                    baseType = xmlSchemaCollection.getTypeByQName(baseTypeName);
                } else {
                    baseType = xmlSchema.getTypeByName(baseTypeName);
                }
                addAttributes(attributes, (XmlSchemaComplexType) baseType);
            } else if (content instanceof XmlSchemaComplexContentRestriction) {
                XmlSchemaComplexContentRestriction complexContentRestriction = (XmlSchemaComplexContentRestriction)content;
                addAttributes(attributes, complexContentRestriction.getAttributes());
            } else if (content instanceof XmlSchemaSimpleContentExtension) {
                XmlSchemaSimpleContentExtension simpleContentExtension = (XmlSchemaSimpleContentExtension)content;
                addAttributes(attributes, simpleContentExtension.getAttributes());
            }
        }
    }

    static void addAttributes(Map<QName, XmlSchemaAttribute> attributes, List<XmlSchemaAttributeOrGroupRef> attributeOrGroupRefs) {
        for (XmlSchemaAttributeOrGroupRef attributeOrGroupRef : attributeOrGroupRefs) {
            addAttributes(attributes, (XmlSchemaAttributeGroupMember)attributeOrGroupRef);
        }
    }

    private static void addAttributes(Map<QName, XmlSchemaAttribute> attributes, XmlSchemaAttributeGroupMember attributeGroupMember) {
        if (attributeGroupMember instanceof XmlSchemaAttribute) {
            XmlSchemaAttribute attribute = (XmlSchemaAttribute)attributeGroupMember; //getAttribute((XmlSchemaAttribute)attributeGroupMember);
            attributes.put(attribute.getWireName(), attribute);
        } else {
            XmlSchemaAttributeGroup attributeGroup;
            if (attributeGroupMember instanceof XmlSchemaAttributeGroup) {
                attributeGroup = (XmlSchemaAttributeGroup)attributeGroupMember;
            }
            else {
                XmlSchemaAttributeGroupRef attributeGroupRef = (XmlSchemaAttributeGroupRef)attributeGroupMember;
                XmlSchemaRef<XmlSchemaAttributeGroup> ref = attributeGroupRef.getRef();
                attributeGroup = ref.getTarget();
            }

            List<XmlSchemaAttributeGroupMember> attributeGroupMembers = attributeGroup.getAttributes();
            for (XmlSchemaAttributeGroupMember xmlSchemaAttributeGroupMember : attributeGroupMembers) {
                addAttributes(attributes, xmlSchemaAttributeGroupMember);
            }
        }
    }

    /**
     * Finds the attribute declaration.
     * <xs:attribute name="surname" type="xs:string" />
     * ...
     * <xs:attribute ref="surname"/>
     * 
     * 
     * @param attribute the attribute for which is declaration is needed
     * 
     * @return attribute declaration which is either a referred attribute or
     * attribute itself.
     */
    private static XmlSchemaAttribute getAttribute(XmlSchemaAttribute attribute) {
        if (attribute.isRef()) {
            XmlSchemaRef<XmlSchemaAttribute> ref = attribute.getRef();
            XmlSchemaAttribute refAttribute = ref.getTarget();
            if (refAttribute != null) {
                return refAttribute;
            }
            QName targetName = ref.getTargetQName();
            XmlSchema schema = attribute.getParent();
            XmlSchemaCollection schemaCollection = schema.getParent();
            if (schemaCollection != null) {
                attribute = schemaCollection.getAttributeByQName(targetName);
            } else {
                attribute = schema.getAttributeByName(targetName);
            }
        }
        return attribute;
    }
    
    final static XmlSchemaElement getElement(XmlSchemaElement element) {
        XmlSchemaRef<XmlSchemaElement> ref = element.getRef();
        if (ref != null) {
            XmlSchemaElement refElement = ref.getTarget();
            if (refElement != null) {
                return refElement;
            }
            QName targetName = ref.getTargetQName();
            if (targetName != null) {
                XmlSchema schema = element.getParent();
                XmlSchemaCollection schemaCollection = schema.getParent();
                if (schemaCollection != null) {
                    element = schemaCollection.getElementByQName(targetName);
                } else {
                    element = schema.getElementByName(targetName);
                }
            }
        }
        return element;
    }

}
