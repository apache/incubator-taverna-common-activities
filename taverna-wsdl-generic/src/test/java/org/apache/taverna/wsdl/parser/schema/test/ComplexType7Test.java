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

public class ComplexType7Test extends TestSchemaTreeModel {

    private final static String XML_FILE = "org/apache/taverna/wsdl/parser/schema/complex_type7.xml";
    private final static String XSD_FILE = "org/apache/taverna/wsdl/parser/schema/complex_type7.xsd";

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

        Assert.assertTrue("root node must have one child node", model.getChildCount() == 1);
        
        XSNode<XSNode, XSNode> node = model.getChildAt(0);
        Assert.assertTrue("node 'person' must be the represented by an element node", node instanceof XSGlobalElement);
        Assert.assertEquals("node 'person' should have one child node", 1, node.getChildCount());
        
        XSNode<XSNode, XSNode> particle = node.getChildAt(0);
        Assert.assertTrue("the node 'fullname' must be the represented by a particle node", particle instanceof XSParticle);
        Assert.assertEquals("node 'fullname' should have two child nodes", 2, particle.getChildCount());
        
        XSNode node1 = particle.getChildAt(0);
        Assert.assertTrue("the node 'name' must be the represented by a particle node", node1 instanceof XSParticle);
        
        XSParticle particle1 = (XSParticle)node1;
        particle1.setUserObject("Napoleon");

        XSNode node2 = particle.getChildAt(1);
        Assert.assertTrue("the 'surname' node must be the represented by a particle node", node2 instanceof XSParticle);
        
        XSParticle particle2 = (XSParticle)node2;
        particle2.setUserObject("Bonaparte");
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
            Assert.assertNotNull("root element 'person' is missed", root);
            Assert.assertEquals("wrong root element name", "person", root.getLocalName());
            Assert.assertEquals("wrong root element namespace", "http://example.com", root.getNamespaceURI());
            Assert.assertEquals("the element 'person' should have one child node", 1, root.getChildNodes().getLength());
            
            Node node = root.getFirstChild();
            
            Assert.assertEquals("wrong child element type", Node.ELEMENT_NODE, node.getNodeType());        
            Assert.assertEquals("wrong child element name", "fullname", node.getLocalName());
            Assert.assertEquals("wrong child element namespace", "http://example.com", node.getNamespaceURI());

            NodeList children = node.getChildNodes();
            Assert.assertEquals("element 'fullname' should have two child elements", 2, children.getLength());
            
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

        } catch(SAXException ex) {
            Assert.fail(ex.getMessage());
        } catch(IOException ex) {
            Assert.fail(ex.getMessage());
        }
    }
}
