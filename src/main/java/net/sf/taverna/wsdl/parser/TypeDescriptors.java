/**
 * *****************************************************************************
 * Copyright (C) 2012 Spanish National Bioinformatics Institute (INB),
 * Barcelona Supercomputing Center and The University of Manchester
 *
 * Modifications to the initial code base are copyright of their respective
 * authors, or their employers as appropriate.
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307
 *****************************************************************************
 */

package net.sf.taverna.wsdl.parser;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPConstants;
import org.apache.ws.commons.schema.*;
import org.apache.ws.commons.schema.utils.XmlSchemaObjectBase;
import org.apache.ws.commons.schema.utils.XmlSchemaRef;
import org.w3c.dom.Attr;
import org.xml.sax.InputSource;

/**
 * An utility class to construct WSDL type descriptors based on XML Schema.
 * 
 * @author Dmitry Repchevsky
 */

public class TypeDescriptors
{
    private XmlSchemaCollection schemas;
    
    /**
     * Constructor that takes a collection of XML Schemas.
     * 
     * @param schemas XML Schemas that this class uses to build type descriptors
     */
    public TypeDescriptors(XmlSchemaCollection schemas) {
        this.schemas = schemas;
        
        if (schemas.schemaForNamespace(SOAPConstants.URI_NS_SOAP_ENCODING) == null) {
            // get soapEncoding datatypes 
            try {
                InputStream soapEncodingStream = TypeDescriptors.class.getClassLoader().getResourceAsStream("META-INF/soap-encoding.xsd");
                try { 
                    schemas.read(new InputSource(soapEncodingStream));
                }
                finally {
                    soapEncodingStream.close();
                }

//                URL soapEncodingURL = new URL(SOAPConstants.URI_NS_SOAP_ENCODING);
//                InputStream soapEncodingStream = soapEncodingURL.openStream();
//                try { 
//                    schemas.read(new InputSource(soapEncodingStream));
//                }
//                finally {
//                    soapEncodingStream.close();
//                }
            }
            catch(MalformedURLException ex) {}
            catch(IOException ex) {}
        }
    }

    public static List<TypeDescriptor> getDescriptors(LinkedHashMap<String, XmlSchemaObject> map) {
        
        List<TypeDescriptor> result = new ArrayList<TypeDescriptor>();

        for (Map.Entry<String, XmlSchemaObject> parameter : map.entrySet()) {
            
            TypeDescriptor typeDescriptor;
            
            String partName = parameter.getKey();
            XmlSchemaObject xmlSchemaObject = parameter.getValue();
            
            if (xmlSchemaObject instanceof XmlSchemaElement) {
                typeDescriptor = getDescriptor((XmlSchemaElement)xmlSchemaObject);
            } else if (xmlSchemaObject instanceof XmlSchemaType) {
                typeDescriptor = getDescriptor((XmlSchemaType)xmlSchemaObject);
            } else {
                throw new IllegalArgumentException("wrong XmlSchemaObject. Mast be either xs:element or xs:type");
            }
            
            typeDescriptor.setName(partName);
            
            result.add(typeDescriptor);
        }
        
        return result;
    }
    
    public static TypeDescriptor getDescriptor(XmlSchemaElement element) {
        XmlSchema xmlSchema = element.getParent();
        
        XmlSchemaCollection schemas = xmlSchema.getParent();
        if (schemas == null) {
            schemas = new XmlSchemaCollection();
            // TODO: put somehow schema inside
        }
        
        TypeDescriptors descriptors = new TypeDescriptors(schemas);
        return descriptors.getElementDescriptor(element);
    }
    
    public static TypeDescriptor getDescriptor(XmlSchemaType type) {
        XmlSchema xmlSchema = type.getParent();
        
        XmlSchemaCollection schemas = xmlSchema.getParent();
        if (schemas == null) {
            schemas = new XmlSchemaCollection();
        }
        
        TypeDescriptors descriptors = new TypeDescriptors(schemas);
        return descriptors.getTypeDescriptor(type.getQName());
    }
    
    /**
     * Builds a type descriptor for a "Literal" parameter.
     * 
     * @param elementName an XML element name of the parameter.
     * @return a Type Descriptor for the parameter.
     */
    public TypeDescriptor getElementDescriptor(QName elementName) {
        XmlSchemaElement element = schemas.getElementByQName(elementName);
        if (element == null) {
            return null;
        }
        return getElementDescriptor(element);
    }

    /**
     * Builds a type descriptor for a "RPC" parameter.
     * 
     * @param typeName an XML type name of the parameter.
     * @return a Type Descriptor for the parameter.
     */
    public TypeDescriptor getTypeDescriptor(QName typeName) {
        XmlSchemaType xmlSchemaType = schemas.getTypeByQName(typeName);

        if (xmlSchemaType == null) {
            return null;
        }
        
        TypeDescriptor typeDesc;
        if (SOAPConstants.URI_NS_SOAP_ENCODING.equals(typeName.getNamespaceURI()) &&
            "Array".equals(typeName.getLocalPart())) {
            typeDesc = new ArrayTypeDescriptor();
        } else {
            typeDesc = getTypeDescriptor(xmlSchemaType);
        }

        typeDesc.setType(typeName.getLocalPart());

        return typeDesc;
    }

    private TypeDescriptor getElementDescriptor(XmlSchemaElement element) {
        TypeDescriptor elementDescriptor;

        QName typeName;
        if (element.isRef()) {
            XmlSchemaRef<XmlSchemaElement> ref = element.getRef();

            XmlSchemaElement target = ref.getTarget();
            if (target == null) {
                QName targetName = ref.getTargetQName();
                if (targetName != null) {
                    target = schemas.getElementByQName(targetName);
                }
            }
            typeName = target.getSchemaTypeName();
        } else {
            typeName = element.getSchemaTypeName();
        }

        TypeDescriptor typeDesc;
        if (typeName != null) {
            typeDesc = getTypeDescriptor(typeName);
        } else {
            XmlSchemaType xmlSchemaType = element.getSchemaType();
            typeDesc = getTypeDescriptor(xmlSchemaType);
        }

        if (element.getMaxOccurs() > 1) {
            ArrayTypeDescriptor arrayElementDescriptor = new ArrayTypeDescriptor();
            arrayElementDescriptor.setElementType(typeDesc);

            elementDescriptor = arrayElementDescriptor;
        }
        else if (typeDesc instanceof ComplexTypeDescriptor) {
            ComplexTypeDescriptor complexElementDescr = (ComplexTypeDescriptor)typeDesc;
            ComplexTypeDescriptor complexTypeDesc = new ComplexTypeDescriptor(); 

            complexTypeDesc.getElements().addAll(complexElementDescr.getElements());
            complexTypeDesc.getAttributes().addAll(complexElementDescr.getAttributes());

            elementDescriptor = complexTypeDesc;
        } else {
            elementDescriptor = new BaseTypeDescriptor();
        }
        elementDescriptor.setType(typeName == null ? "" : typeName.getLocalPart());

        elementDescriptor.setName(element.isRef() ? element.getWireName().getLocalPart() : element.getName());
        elementDescriptor.setQname(element.getWireName());
        elementDescriptor.setOptional(element.getMinOccurs() == 0);
        elementDescriptor.setUnbounded(Long.MAX_VALUE == element.getMaxOccurs());
        elementDescriptor.setNillable(element.isNillable());

        return elementDescriptor;
    }

    private TypeDescriptor getTypeDescriptor(XmlSchemaType xmlSchemaType) {
        TypeDescriptor typeDesc;

        if (xmlSchemaType instanceof XmlSchemaSimpleType) {
            typeDesc = getSimpleTypeDescriptor((XmlSchemaSimpleType)xmlSchemaType);
        } else {
            typeDesc = getComplexTypeDescriptor((XmlSchemaComplexType)xmlSchemaType);
        }

        if (!xmlSchemaType.isAnonymous()) {
            typeDesc.setName(xmlSchemaType.getName());
        }

        typeDesc.setQname(xmlSchemaType.getQName());

        return typeDesc;
    }

    private TypeDescriptor getComplexTypeDescriptor(XmlSchemaComplexType xmlSchemaComplexType) {
        ComplexTypeDescriptor typeDesc = new ComplexTypeDescriptor();

        List<XmlSchemaAttributeOrGroupRef> attributes = xmlSchemaComplexType.getAttributes();
        for (XmlSchemaAttributeOrGroupRef attribute : attributes) {
            // all XmlSchemaAttributeOrGroupRef descendants are XmlSchemaAttributeGroupMember
            addAttributeTypeDescriptors(typeDesc.getAttributes(), (XmlSchemaAttributeGroupMember)attribute);
        }

        XmlSchemaContentModel xmlSchemaContentModel = xmlSchemaComplexType.getContentModel();
        if (xmlSchemaContentModel != null) {
            XmlSchemaContent content = xmlSchemaContentModel.getContent();

            if (content instanceof XmlSchemaComplexContentExtension) {
                XmlSchemaComplexContentExtension complexContentExtension = (XmlSchemaComplexContentExtension)content;

                attributes = complexContentExtension.getAttributes();

                XmlSchemaParticle particle = complexContentExtension.getParticle();
                addElementTypeDescriptors(typeDesc.getElements(), particle);

                QName baseTypeName = complexContentExtension.getBaseTypeName();
                if (baseTypeName != null) {
                    TypeDescriptor baseTypeDesc = getTypeDescriptor(baseTypeName);
                    if (baseTypeDesc instanceof ComplexTypeDescriptor) {
                        ComplexTypeDescriptor base = (ComplexTypeDescriptor)baseTypeDesc;
                        typeDesc.getElements().addAll(base.getElements());
                    }
                }
            } else if (content instanceof XmlSchemaComplexContentRestriction) {
                XmlSchemaComplexContentRestriction complexContentRestriction = (XmlSchemaComplexContentRestriction)content;

                attributes = complexContentRestriction.getAttributes();

                // check for "SOAP Encoding"
                QName baseTypeName = complexContentRestriction.getBaseTypeName();
                if (baseTypeName != null) {
                    TypeDescriptor baseTypeDesc = getTypeDescriptor(baseTypeName);
                    if (baseTypeDesc instanceof ArrayTypeDescriptor) {
                        ArrayTypeDescriptor arrayDesc = (ArrayTypeDescriptor)baseTypeDesc;
                        TypeDescriptor arrayTypeDesc = getArrayType(attributes);
                        arrayDesc.setElementType(arrayTypeDesc);
                        return arrayDesc;
                    }
                }

                XmlSchemaParticle particle = complexContentRestriction.getParticle();
                addElementTypeDescriptors(typeDesc.getElements(), particle);
            } else if (content instanceof XmlSchemaSimpleContentExtension) {
                XmlSchemaSimpleContentExtension xmlSchemaSimpleContentExtension = (XmlSchemaSimpleContentExtension)content;

                attributes = xmlSchemaSimpleContentExtension.getAttributes();

                QName baseTypeName = xmlSchemaSimpleContentExtension.getBaseTypeName();
                if (baseTypeName != null) {
                    TypeDescriptor baseTypeDesc = getTypeDescriptor(baseTypeName);
                    typeDesc.setType(baseTypeDesc.getType());
                }
            } else if (content instanceof XmlSchemaSimpleContentRestriction){
                XmlSchemaSimpleContentRestriction xmlSchemaSimpleContentRestriction = (XmlSchemaSimpleContentRestriction)content;

                attributes = xmlSchemaSimpleContentRestriction.getAttributes();

                QName baseTypeName = xmlSchemaSimpleContentRestriction.getBaseTypeName();
                if (baseTypeName == null) {
                    XmlSchemaSimpleType simpleType = xmlSchemaSimpleContentRestriction.getBaseType();
                    baseTypeName = simpleType.getQName();
                }

                TypeDescriptor baseTypeDesc = getTypeDescriptor(baseTypeName);
                typeDesc.setType(baseTypeDesc.getType());
            }

            for (XmlSchemaAttributeOrGroupRef attribute : attributes) {
                addAttributeTypeDescriptors(typeDesc.getAttributes(), (XmlSchemaAttributeGroupMember)attribute);
            }
        } else {
            XmlSchemaParticle particle = xmlSchemaComplexType.getParticle();
            addElementTypeDescriptors(typeDesc.getElements(), particle);
        }

        return typeDesc;
    }

    /*
     * Adds all elements found in the "particle" to the "elements" list
     */
    private void addElementTypeDescriptors(List<TypeDescriptor> elements, XmlSchemaParticle particle)
    {
        if (particle instanceof XmlSchemaElement){
            XmlSchemaElement element = (XmlSchemaElement)particle;
            TypeDescriptor elementTypeDescriptor = getElementDescriptor(element);
            elements.add(elementTypeDescriptor);
        }
        else if (particle instanceof XmlSchemaAll) {
            addElementTypeDescriptors(elements, (XmlSchemaAll)particle);
        }
        else if (particle instanceof XmlSchemaSequence) {
            addElementTypeDescriptors(elements, (XmlSchemaSequence)particle);
        }
        else if (particle instanceof XmlSchemaChoice) {
            addElementTypeDescriptors(elements, (XmlSchemaChoice)particle);
        }
    }

    /*
     * Adds all elements found in a sequence to the list of elements
     */
    private void addElementTypeDescriptors(List<TypeDescriptor> elements, XmlSchemaSequence sequence) {
        List<XmlSchemaSequenceMember> items = sequence.getItems();
        addElementTypeDescriptors(elements, items);
    }

    /*
     * Adds all elements found in a "all" particle to the list of elements
     */
    private void addElementTypeDescriptors(List<TypeDescriptor> elements, XmlSchemaAll all) {
        for (XmlSchemaElement element : all.getItems()) {
            TypeDescriptor elementTypeDescriptor = getElementDescriptor(element);
            elements.add(elementTypeDescriptor);
        }
    }

    /*
     * Adds all elements found in a "choice" particle to the list of elements
     */

    private void addElementTypeDescriptors(List<TypeDescriptor> elements, XmlSchemaChoice choice) {
        List<XmlSchemaObject> items = choice.getItems();
        addElementTypeDescriptors(elements, items);
    }

    private void addElementTypeDescriptors(List<TypeDescriptor> elements, List<? extends XmlSchemaObjectBase> items) {
        for (XmlSchemaObjectBase item : items) {
            if (item instanceof XmlSchemaElement) {
                addElementTypeDescriptors(elements, (XmlSchemaElement)item);
            } else if(item instanceof XmlSchemaSequence) {
                addElementTypeDescriptors(elements, (XmlSchemaSequence)item);
            } else if (item instanceof XmlSchemaChoice) {
                addElementTypeDescriptors(elements, (XmlSchemaChoice)item);
            } else if (item instanceof XmlSchemaGroup) {
                XmlSchemaGroup group = (XmlSchemaGroup)item;
                XmlSchemaGroupParticle groupParticle = group.getParticle();
                addElementTypeDescriptors(elements, groupParticle);
            } else if (item instanceof XmlSchemaGroupRef) {
                XmlSchemaGroupRef groupRef = (XmlSchemaGroupRef)item;
                XmlSchemaGroupParticle groupParticle = groupRef.getParticle();
                addElementTypeDescriptors(elements, groupParticle);
            }
        }
    }

    /*
     * Builds a BaseTypeDescriptor from a simpleType definition.
     * Method processes simpleType restrictions to find an appropriate XML primitive type
     */
    private BaseTypeDescriptor getSimpleTypeDescriptor(XmlSchemaSimpleType xmlSchemaSimpleType) {
        BaseTypeDescriptor typeDesc = new BaseTypeDescriptor();

        QName typeName = xmlSchemaSimpleType.getQName();
        if (typeName != null && XMLConstants.W3C_XML_SCHEMA_NS_URI.equals(typeName.getNamespaceURI())) {
            typeDesc.setType(typeName.getLocalPart());
        } else {
            XmlSchemaSimpleTypeContent simpleTypeContent = xmlSchemaSimpleType.getContent();
            if (simpleTypeContent instanceof XmlSchemaSimpleTypeRestriction) {
                XmlSchemaSimpleTypeRestriction simpleTypeRestriction = (XmlSchemaSimpleTypeRestriction)simpleTypeContent;

                QName baseTypeName = simpleTypeRestriction.getBaseTypeName();
                if (baseTypeName == null) {
                    XmlSchemaSimpleType baseType = simpleTypeRestriction.getBaseType();
                    baseTypeName = baseType.getQName();
                }

                TypeDescriptor baseTypeDesc = getTypeDescriptor(baseTypeName);
                typeDesc.setType(baseTypeDesc.getType());
            }
            else if (simpleTypeContent instanceof XmlSchemaSimpleTypeList) {
                XmlSchemaSimpleTypeList simpleTypeList = (XmlSchemaSimpleTypeList)simpleTypeContent;

                QName itemTypeName = simpleTypeList.getItemTypeName();
                if (itemTypeName == null) {
                    XmlSchemaSimpleType itemType = simpleTypeList.getItemType();
                    itemTypeName = itemType.getQName();
                }

                if (XMLConstants.W3C_XML_SCHEMA_NS_URI.equals(itemTypeName.getNamespaceURI())) {
                    typeDesc.setType(itemTypeName.getLocalPart());
                }
                else {
                    TypeDescriptor baseTypeDesc = getTypeDescriptor(itemTypeName);
                    typeDesc.setType(baseTypeDesc.getType());                        
                }
            }
            else if (simpleTypeContent instanceof XmlSchemaSimpleTypeUnion) {
                XmlSchemaSimpleTypeUnion typeUnion = (XmlSchemaSimpleTypeUnion)simpleTypeContent;

                // TODO
            }
        }

        return typeDesc;
    }
    
    /*
     * Inserts attribute descriptions specified by the attribute (may be a group of attributes) into the attributes list
     */
    private void addAttributeTypeDescriptors(List<TypeDescriptor> attributes, XmlSchemaAttributeGroupMember attribute) {
        AttributeTypeDescriptor typeDesc = new AttributeTypeDescriptor();

        if (attribute instanceof XmlSchemaAttribute) {
            XmlSchemaAttribute xmlSchemaAttribute = (XmlSchemaAttribute)attribute;

            QName attrName;
            if (xmlSchemaAttribute.isRef()) {
                XmlSchemaRef<XmlSchemaAttribute> xmlSchemaRef = xmlSchemaAttribute.getRef();

                attrName = xmlSchemaRef.getTargetQName();
                if (attrName == null) {
                    XmlSchemaAttribute xmlSchemaRefAttribute = xmlSchemaRef.getTarget();
                    attrName = xmlSchemaRefAttribute.getWireName();
                }
            } else {
                    attrName = xmlSchemaAttribute.getWireName();
            }
            
            typeDesc.setName(attrName.getLocalPart());
            typeDesc.setQname(attrName);

            QName attrTypeName = xmlSchemaAttribute.getSchemaTypeName();
            if (attrTypeName == null) {
                XmlSchemaSimpleType xmlSchemaSimpleType = xmlSchemaAttribute.getSchemaType();
                attrTypeName = xmlSchemaSimpleType.getQName();
            }

            TypeDescriptor attrTypeDesc = getTypeDescriptor(attrTypeName);
            typeDesc.setType(attrTypeDesc.getType());

            typeDesc.setOptional(XmlSchemaUse.OPTIONAL == xmlSchemaAttribute.getUse());
            attributes.add(typeDesc);
        }
        else {
            XmlSchemaAttributeGroup xmlSchemaAttributeGroup;
            if (attribute instanceof XmlSchemaAttributeGroup) {
                xmlSchemaAttributeGroup = (XmlSchemaAttributeGroup)attribute;
            }
            else {
                XmlSchemaAttributeGroupRef xmlSchemaAttributeGroupRef = (XmlSchemaAttributeGroupRef)attribute;
                XmlSchemaRef<XmlSchemaAttributeGroup> xmlSchemaRef = xmlSchemaAttributeGroupRef.getRef();
                xmlSchemaAttributeGroup = xmlSchemaRef.getTarget();
            }

            List<XmlSchemaAttributeGroupMember> xmlSchemaAttributeGroupMembers = xmlSchemaAttributeGroup.getAttributes();
            for (XmlSchemaAttributeGroupMember xmlSchemaAttributeGroupMember : xmlSchemaAttributeGroupMembers) {
                addAttributeTypeDescriptors(attributes, xmlSchemaAttributeGroupMember);
            }
        }
    }
    
    /*
     * Looking for "SOAP-ENC:arrayType" attribute in attributes list
     */
    private TypeDescriptor getArrayType(List<XmlSchemaAttributeOrGroupRef> attributes) {
        for (XmlSchemaAttributeOrGroupRef attribute : attributes) {
                TypeDescriptor arrayTypeDesc = getArrayType((XmlSchemaAttributeGroupMember)attribute);
                if (arrayTypeDesc != null) {
                    return arrayTypeDesc;
                }
        }
        return null;
    }

    /*
     * Getting a TypeDescriptor for the attribute ("SOAP-ENC:arrayType")
     */
    private TypeDescriptor getArrayType(XmlSchemaAttributeGroupMember attribute) {
        if (attribute instanceof XmlSchemaAttribute) {
            XmlSchemaAttribute xmlSchemaAttribute = (XmlSchemaAttribute)attribute;
            QName attrName;
            if (xmlSchemaAttribute.isRef()) {
                XmlSchemaRef<XmlSchemaAttribute> xmlSchemaRef = xmlSchemaAttribute.getRef();

                attrName = xmlSchemaRef.getTargetQName();
                if (attrName == null) {
                    XmlSchemaAttribute xmlSchemaRefAttribute = xmlSchemaRef.getTarget();
                    attrName = xmlSchemaRefAttribute.getWireName();
                }
            } else {
                    attrName = xmlSchemaAttribute.getWireName();
            }

            if (SOAPConstants.URI_NS_SOAP_ENCODING.equals(attrName.getNamespaceURI()) &&
                "arrayType".equals(attrName.getLocalPart())) {

                Attr[] unhandled = xmlSchemaAttribute.getUnhandledAttributes();

                String arrayType = null;
                for (Attr attr : unhandled) {
                    if ("arrayType".equals(attr.getLocalName())) {
                        if (SOAPConstants.URI_NS_SOAP_ENCODING.equals(attr.getNamespaceURI())) {
                            arrayType = attr.getValue();
                            break;
                        }
                        if ("http://schemas.xmlsoap.org/wsdl/".equals(attr.getNamespaceURI())) {
                            arrayType = attr.getValue();
                        }
                    }
                }

                if (arrayType != null) {
                    QName arrayTypeQName = getArrayTypeQName(arrayType);

                    if (arrayTypeQName.getNamespaceURI() == null || arrayTypeQName.getNamespaceURI().isEmpty())
                    {
                        String namespace = xmlSchemaAttribute.getParent().getNamespaceContext().getNamespaceURI(XMLConstants.DEFAULT_NS_PREFIX);
                        arrayTypeQName = new QName(namespace, arrayTypeQName.getLocalPart());
                    }
                    return getTypeDescriptor(arrayTypeQName);
                }
            }
        }
        else {
            XmlSchemaAttributeGroup xmlSchemaAttributeGroup;
            if (attribute instanceof XmlSchemaAttributeGroup) {
                xmlSchemaAttributeGroup = (XmlSchemaAttributeGroup)attribute;
            }
            else {
                XmlSchemaAttributeGroupRef xmlSchemaAttributeGroupRef = (XmlSchemaAttributeGroupRef)attribute;
                XmlSchemaRef<XmlSchemaAttributeGroup> xmlSchemaRef = xmlSchemaAttributeGroupRef.getRef();
                xmlSchemaAttributeGroup = xmlSchemaRef.getTarget();
            }

            List<XmlSchemaAttributeGroupMember> xmlSchemaAttributeGroupMembers = xmlSchemaAttributeGroup.getAttributes();
            for (XmlSchemaAttributeGroupMember xmlSchemaAttributeGroupMember : xmlSchemaAttributeGroupMembers) {
                TypeDescriptor arrayTypeDesc = getArrayType(xmlSchemaAttributeGroupMember);
                if (arrayTypeDesc != null) {
                    return arrayTypeDesc;
                }
            }
        }

        return null;
    }

    /*
     * Parsing an arrayTypeValue to get its name.
     * 
     * arrayTypeValue = atype asize
     * atype          = QName *( rank )
     * rank           = "[" *( "," ) "]"
     * asize          = "[" #length "]"
     * length         = 1*DIGIT
     */
    private QName getArrayTypeQName(String arrayType) {
        final int prefixIdx = arrayType.indexOf(':');
        final int rankIdx = arrayType.indexOf('[');

        if (prefixIdx < 0) {
            String localName = arrayType.substring(0, rankIdx);
            return new QName(localName);
        }

        String localName = arrayType.substring(prefixIdx + 1, rankIdx);

        String prefix = arrayType.substring(0, prefixIdx);
        String namespace = schemas.getNamespaceContext().getNamespaceURI(prefix);

        return new QName(namespace, localName, prefix);
    }
}
