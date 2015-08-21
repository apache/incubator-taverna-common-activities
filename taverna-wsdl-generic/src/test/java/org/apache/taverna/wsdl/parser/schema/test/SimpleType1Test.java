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

package org.apache.taverna.wsdl.parser.schema.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.apache.taverna.wsdl.parser.schema.XSGlobalElement;
import org.apache.taverna.wsdl.parser.schema.XSNode;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * @author Dmitry Repchevsky
 */

public class SimpleType1Test extends TestSchemaTreeModel {
    
    private final static String XML_FILE = "org/apache/taverna/wsdl/parser/schema/simple_type1.xml";
    private final static String XSD_FILE = "org/apache/taverna/wsdl/parser/schema/simple_type1.xsd";

    @Before
    public void loadModel() {
        loadModel(XSD_FILE);
    }
    
    @Test
    public void simpleTypeReadTest() {
        fillModel(XML_FILE);       
        testModel();
    }
    
    @Test
    public void simpleTypeWriteTest() {
        fillModel();
        testModel();
    }
    
    private void fillModel() {
        model.addGlobalElement(new QName("http://example.com","name"));

        Assert.assertTrue("there must be ONE simple elements in the model", model.getChildCount() == 1);
        
        XSNode node = model.getChildAt(0);
        
        Assert.assertTrue("the 'name' must be the represented by an element node", node instanceof XSGlobalElement);
        
        XSGlobalElement element = (XSGlobalElement)node;
        element.setUserObject("Napoleon");
    }

    private void testModel() {
        XMLOutputFactory of = XMLOutputFactory.newInstance();
        of.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, Boolean.TRUE);
        
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            XMLStreamWriter writer = of.createXMLStreamWriter(out);
            try {
                model.write(writer);
            } finally {
                writer.close();
            }
        } catch(XMLStreamException ex) {
            Assert.fail(ex.getMessage());
        }

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder db = null;
        try {
            db = dbf.newDocumentBuilder();
        } catch(ParserConfigurationException ex) {
            Assert.fail(ex.getMessage());
        }
        
        try {
            Document doc = db.parse(new ByteArrayInputStream(out.toByteArray()));

            Element node = doc.getDocumentElement();
            
            Assert.assertEquals("wrong element type", Node.ELEMENT_NODE, node.getNodeType());
            Assert.assertEquals("wrong element name", "name", node.getLocalName());
            Assert.assertEquals("wrong 'name' element namespace", "http://example.com", node.getNamespaceURI());
            Assert.assertEquals("wrong '{http://example.com}name' element text content", "Napoleon", node.getTextContent());
        } catch(SAXException ex) {
            Assert.fail(ex.getMessage());
        } catch(IOException ex) {
            Assert.fail(ex.getMessage());
        }
    }
}
