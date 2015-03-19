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

package org.apache.taverna.wsdl.soap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPElement;
import org.apache.taverna.wsdl.parser.ArrayTypeDescriptor;
import org.apache.taverna.wsdl.parser.BaseTypeDescriptor;
import org.apache.taverna.wsdl.parser.TypeDescriptor;
import org.apache.taverna.wsdl.parser.WSDL11Parser;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;


/**
 * SOAPResponseParser responsible for parsing soap responses that map to outputs
 * that can be directly represented with Primitive types (i.e. int, String,
 * String[]).
 * 
 * @author sowen
 * 
 */
@SuppressWarnings("unchecked")
public class SOAPResponsePrimitiveParser implements SOAPResponseParser {

    private Unmarshaller u;
    private Map<String, TypeDescriptor> descriptors;

    private static final Logger logger = Logger.getLogger(WSDL11Parser.class.getName());
    
    public SOAPResponsePrimitiveParser(List<TypeDescriptor> outputDescriptors) {
        
        descriptors = new TreeMap<String, TypeDescriptor>();
        
        for (TypeDescriptor desc : outputDescriptors) {
            descriptors.put(desc.getName(), desc);
        }
        
        try {
            u  = JAXBContext.newInstance().createUnmarshaller();
        }
        catch(JAXBException ex) {}
    }

    /**
     * Parses each SOAPBodyElement for the primitive type, and places it in the
     * output Map
     */
    @Override
    public Map parse(List<SOAPElement> response) throws Exception {
        Map result = new HashMap();

        SOAPElement responseElement = response.get(0); 
        
        for (Iterator<SOAPElement> paramIterator = responseElement.getChildElements(); paramIterator.hasNext();) {
            SOAPElement param = paramIterator.next();
            
            TypeDescriptor typeDesc = descriptors.get(param.getLocalName());
            if (typeDesc != null) {
                if (typeDesc instanceof ArrayTypeDescriptor) {
                    ArrayTypeDescriptor arrayDesc = (ArrayTypeDescriptor)typeDesc;
                    typeDesc = arrayDesc.getElementType(); // do we need to know a type here?
                    List elements = new ArrayList();
                    for (Iterator<Element> iter = param.getChildElements(); iter.hasNext();) {
                        Element element = iter.next();
                        Object value = getNodeObject(element, typeDesc.getQname());
                        elements.add(value);
                    }
                    
                    result.put(param.getElementName().getLocalName(), ObjectConverter.convertObject(elements));
                }
                else if (typeDesc instanceof BaseTypeDescriptor) {
                    Object value = getNodeObject(param, typeDesc.getQname());
                    result.put(param.getElementName().getLocalName(), ObjectConverter.convertObject(value));
                }
            }
        }

        return result;
    }
    
    private Object getNodeObject(Element element, QName type) throws JAXBException {
        Attr attr = (Attr)element.getAttributes().getNamedItemNS(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, "type");
        if (attr != null) {
            String xsiType = attr.getValue();
            int idx = xsiType.indexOf(':');
            if (idx > 0) {
                String prefix = xsiType.substring(0, idx);
                String namespace = element.lookupNamespaceURI(prefix);
                if (namespace != null) {
                    String localName = xsiType.substring(idx + 1);
                    QName typeName = new QName(namespace, localName);
                    if (typeName.equals(type)) {
                        return u.unmarshal(element, Object.class).getValue();
                    }
                    logger.log(Level.WARNING, "different types in parameter {0}defined: {1}, expected: {2}", new Object[]{element.getLocalName(), typeName, type});
                } else {
                    logger.log(Level.WARNING, "no xsi:type namespace found: {0}", xsiType);
                }
            } else {
                logger.log(Level.WARNING, "missing parameter xsi:type namespace {0} xsi:type='{1}'", new Object[]{element.getLocalName(), xsiType});
            }
        }

        // this is a safeguard code where there is no xsi:type specified, so using provided type
        attr = element.getOwnerDocument().createAttributeNS(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, "type");
        
        String prefix = element.lookupPrefix(type.getNamespaceURI());
        if (prefix == null) {
            // it is supposed that elements are of primitive types...
            element.setAttribute(XMLConstants.XML_NS_PREFIX + ":xsd", type.getNamespaceURI());
            prefix = "xsd";
        }
        attr.setValue(prefix + ":" + type.getLocalPart());
        
        element.getAttributes().setNamedItemNS(attr);

        return u.unmarshal(element, Object.class).getValue();
    }
}
