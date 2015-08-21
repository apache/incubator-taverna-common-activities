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
import java.io.InputStream;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
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

public class ComplexType1Test extends TestSchemaTreeModel {

    private final static String XML_FILE = "org/apache/taverna/wsdl/parser/schema/complex_type1.xml";
    private final static String XSD_FILE = "org/apache/taverna/wsdl/parser/schema/complex_type1.xsd";

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
    
    @Test
    public void XPathTest() throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        
        InputStream in = ComplexType1Test.class.getClassLoader().getResourceAsStream(XML_FILE);
        Document doc;
        try {
            doc = db.parse(in);
        } finally {
            in.close();
        }
        
        XPath xpath = XPathFactory.newInstance().newXPath();

        fillModel(XML_FILE);

        XSGlobalElement element = (XSGlobalElement)model.getChildAt(0);
        
        XSParticle particle1 = (XSParticle)element.getChildAt(0);
        Object name_value = xpath.evaluate(particle1.getXPath(), doc, XPathConstants.STRING);
        Assert.assertEquals("wrong text value for the 'name' element", particle1.getUserObject(), name_value);
        
        XSParticle particle2 = (XSParticle)element.getChildAt(1);
        Object surname_value = xpath.evaluate(particle2.getXPath(), doc, XPathConstants.STRING);
        Assert.assertEquals("wrong text value for the 'surname' element", particle2.getUserObject(), surname_value);
    }
    
    private void fillModel() {
        model.addGlobalElement(new QName("http://example.com","person"));

        Assert.assertTrue("the root node must have one child node", model.getChildCount() == 1);
        
        XSNode<XSNode, XSNode> node = model.getChildAt(0);
        Assert.assertEquals("the node 'person' should have two child nodes", 2, node.getChildCount());
        Assert.assertTrue("the node 'person' must be represented by an element node", node instanceof XSGlobalElement);
        
        XSNode node1 = node.getChildAt(0);
        Assert.assertTrue("the node 'name' must be represented by a particle node", node1 instanceof XSParticle);

        XSParticle particle1 = (XSParticle)node1;
        particle1.setUserObject("Napoleon");

        XSNode node2 = node.getChildAt(1);
        Assert.assertTrue("the node 'surname' must be represented by a particle node", node2 instanceof XSParticle);
        
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
            Assert.assertNotNull("missed root ('person') element", root);
            
            NodeList children = root.getChildNodes();
            Assert.assertEquals("the element '<person>' should have two child nodes", 2, children.getLength());
            
            Node node1 = children.item(0);
            Assert.assertEquals("wrong first child node type", Node.ELEMENT_NODE, node1.getNodeType());
            Assert.assertEquals("wrong first child element name", "name", node1.getLocalName());
            Assert.assertEquals("wrong 'name' element namespace", "http://example.com", node1.getNamespaceURI());
            Assert.assertEquals("wrong '{http://example.com}name' text content", "Napoleon", node1.getTextContent());
            
            Node node2 = children.item(1);
            Assert.assertEquals("wrong second child node type", Node.ELEMENT_NODE, node2.getNodeType());
            Assert.assertEquals("wrong second child element name", "surname", node2.getLocalName());
            Assert.assertEquals("wrong 'surname' element namespace", "http://example.com", node2.getNamespaceURI());
            Assert.assertEquals("wrong '{http://example.com}surname' element text content", "Bonaparte", node2.getTextContent());

        } catch(SAXException ex) {
            Assert.fail(ex.getMessage());
        } catch(IOException ex) {
            Assert.fail(ex.getMessage());
        }
    }
}
