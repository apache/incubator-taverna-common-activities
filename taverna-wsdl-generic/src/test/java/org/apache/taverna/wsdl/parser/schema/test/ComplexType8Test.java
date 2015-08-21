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
import org.apache.taverna.wsdl.parser.schema.XSType;
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

public class ComplexType8Test extends TestSchemaTreeModel {

    private final static String XML_FILE = "org/apache/taverna/wsdl/parser/schema/complex_type8.xml";
    private final static String XSD_FILE = "org/apache/taverna/wsdl/parser/schema/complex_type8.xsd";
    
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
        XSParticle particleNode = (XSParticle)element.getChildAt(0);

        XSType hobby1 = (XSType)particleNode.getChildAt(0);
        Object hobby1_value = xpath.evaluate(hobby1.getXPath(), doc, XPathConstants.STRING);
        Assert.assertEquals("wrong text value for the first 'hobby' element", hobby1.getUserObject(), hobby1_value);

        XSType hobby2 = (XSType)particleNode.getChildAt(1);
        Object hobby2_value = xpath.evaluate(hobby2.getXPath(), doc, XPathConstants.STRING);
        Assert.assertEquals("wrong text value for the second 'hobby' element", hobby2.getUserObject(), hobby2_value);
    }

    private void fillModel() {
        model.addGlobalElement(new QName("http://example.com","person"));

        Assert.assertTrue("the root node must have one child node", model.getChildCount() == 1);
        
        XSNode<XSNode, XSNode> node = model.getChildAt(0);
        Assert.assertTrue("the 'person' node must be the represented by an element node", node instanceof XSGlobalElement);
        Assert.assertEquals("the node 'person' should have one child node", 1, node.getChildCount());

        XSNode<XSNode, XSNode> particleNode = node.getChildAt(0);
        Assert.assertTrue("the sequence node must be represented by a particle node", particleNode instanceof XSParticle);
        
        // one empty node should have been created by the model.
        Assert.assertEquals("the sequence node should have one child node", 1, particleNode.getChildCount());
        
        XSNode node1 = particleNode.getChildAt(0);
        Assert.assertTrue("the child node must be represented by a type node", node1 instanceof XSType);
        
        XSType<XSNode, XSNode> hobby1 = (XSType)node1;
        hobby1.setUserObject("war");
        
        XSType hobby2 = new XSType(hobby1.getXSComponent());
        hobby2.setUserObject("archeology");
        ((XSParticle)particleNode).insert(hobby2, particleNode.getChildCount());
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
            Assert.assertNotNull("'person' element is missed", root);
            
            NodeList children = root.getChildNodes();
            Assert.assertEquals("'person' should have two child elements", 2, children.getLength());

            Node node1 = children.item(0);
            Assert.assertEquals("wrong first node type", Node.ELEMENT_NODE, node1.getNodeType());
            Assert.assertEquals("wrong first element name", "hobby", node1.getLocalName());
            Assert.assertEquals("wrong first element namespace", "http://example.com", node1.getNamespaceURI());
            Assert.assertEquals("wrong third element text content", "war", node1.getTextContent());
            
            Node node2 = children.item(1);
            Assert.assertEquals("wrong second node type", Node.ELEMENT_NODE, node2.getNodeType());
            Assert.assertEquals("wrong second element name", "hobby", node2.getLocalName());
            Assert.assertEquals("wrong second element namespace", "http://example.com", node2.getNamespaceURI());
            Assert.assertEquals("wrong second element text content", "archeology", node2.getTextContent());
            

        } catch(SAXException ex) {
            Assert.fail(ex.getMessage());
        } catch(IOException ex) {
            Assert.fail(ex.getMessage());
        }
    }
}
