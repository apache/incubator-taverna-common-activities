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
import org.apache.taverna.wsdl.parser.schema.XSParticle;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * @author Dmitry Repchevsky
 */

public class ComplexType5Test extends TestSchemaTreeModel {

    private final static String XML_FILE = "org/apache/taverna/wsdl/parser/schema/complex_type5.xml";
    private final static String XSD_FILE = "org/apache/taverna/wsdl/parser/schema/complex_type5.xsd";

    @Before
    public void loadModel() {
        loadModel(XSD_FILE);
    }

    @Test 
    public void complexTypeReadTest() {
        fillModel(XML_FILE);
        testModel();
    }

    @Test
    public void complexTypeWriteTest() {
        fillModel();
        testModel();
    }
    
    private void fillModel() {
        model.addGlobalElement(new QName("http://example.com","person"));

        Assert.assertTrue("the root node must have one child node", model.getChildCount() == 1);
        
        XSNode<XSNode, XSNode> node = model.getChildAt(0);
        Assert.assertTrue("the 'person' node must be the represented by an element node", node instanceof XSGlobalElement);
        Assert.assertEquals("the node `person' should have three child nodes", 3, node.getChildCount());
        
        for (int i = 0, n = node.getChildCount(); i <n; i++) {
            XSNode child = node.getChildAt(i);
            Assert.assertTrue("the 'child' node must be the represented by a particle node", child instanceof XSParticle);
            
            final XSParticle particleNode = (XSParticle)child;
            final QName qname = particleNode.getName();

            final String namespaceURI = qname.getNamespaceURI();
            Assert.assertEquals("wrong element namespace", "http://example.com", namespaceURI);

            final String localPart = qname.getLocalPart();
            switch(localPart) {
                case "name": particleNode.setUserObject("Napoleon");break;
                case "surname": particleNode.setUserObject("Bonaparte");break;
                case "hobby": particleNode.setUserObject("war");break;
                default: Assert.fail("unknown element node found");
            }
        }
    }
    
    private void testModel() {
        XMLOutputFactory f = XMLOutputFactory.newInstance();
        f.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, Boolean.TRUE);
        
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            XMLStreamWriter writer = f.createXMLStreamWriter(out);
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

            Element root = doc.getDocumentElement();
            Assert.assertNotNull("missed root ('person') element", root);
            
            NodeList children = root.getChildNodes();
            Assert.assertEquals("'person' should have three child elements", 3, children.getLength());
            
            Node node1 = children.item(0);
            
            Assert.assertEquals("wrong first child element type", Node.ELEMENT_NODE, node1.getNodeType());
            Assert.assertEquals("wrong first child element name", "name", node1.getLocalName());
            Assert.assertEquals("wrong first child element namespace", "http://example.com", node1.getNamespaceURI());
            Assert.assertEquals("wrong first element text content", "Napoleon", node1.getTextContent());
            
            Node node2 = children.item(1);
            
            Assert.assertEquals("wrong second node type", Node.ELEMENT_NODE, node2.getNodeType());
            Assert.assertEquals("wrong second element name", "surname", node2.getLocalName());
            Assert.assertEquals("wrong second element namespace", "http://example.com", node2.getNamespaceURI());
            Assert.assertEquals("wrong second element text content", "Bonaparte", node2.getTextContent());

            Node node3 = children.item(2);
            
            Assert.assertEquals("wrong third node type", Node.ELEMENT_NODE, node3.getNodeType());
            Assert.assertEquals("wrong third element name", "hobby", node3.getLocalName());
            Assert.assertEquals("wrong third element namespace", "http://example.com", node3.getNamespaceURI());
            Assert.assertEquals("wrong third element text content", "war", node3.getTextContent());

        } catch(SAXException ex) {
            Assert.fail(ex.getMessage());
        } catch(IOException ex) {
            Assert.fail(ex.getMessage());
        }
    }
}
