/*
 * Copyright (C) 2003 The University of Manchester 
 *
 * Modifications to the initial code base are copyright of their
 * respective authors, or their employers as appropriate.  Authorship
 * of the modifications may be determined from the ChangeLog placed at
 * the end of this file.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA.
 *
 ****************************************************************
 * Source code information
 * -----------------------
 * Filename           $RCSfile: SOAPResponsePrimitiveParser.java,v $
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007/11/28 16:05:45 $
 *               by   $Author: sowen70 $
 * Created on 05-May-2006
 *****************************************************************/
package net.sf.taverna.wsdl.soap;

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
import net.sf.taverna.wsdl.parser.ArrayTypeDescriptor;
import net.sf.taverna.wsdl.parser.BaseTypeDescriptor;
import net.sf.taverna.wsdl.parser.TypeDescriptor;
import net.sf.taverna.wsdl.parser.WSDL11Parser;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


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
