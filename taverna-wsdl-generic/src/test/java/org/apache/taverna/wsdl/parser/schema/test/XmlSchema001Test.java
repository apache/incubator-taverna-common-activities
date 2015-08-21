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
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.apache.taverna.wsdl.parser.schema.XSAttribute;
import org.apache.taverna.wsdl.parser.schema.XSGlobalElement;
import org.apache.taverna.wsdl.parser.schema.XSNode;
import org.apache.taverna.wsdl.parser.schema.XSParticle;
import org.apache.taverna.wsdl.parser.schema.XSType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * @author Dmitry Repchevsky
 */

public class XmlSchema001Test extends TestSchemaTreeModel {

    private final static String XML_FILE = "org/apache/taverna/wsdl/parser/schema/xml-schema-example-001.xml";
    private final static String XSD_FILE = "org/apache/taverna/wsdl/parser/schema/xml-schema-example-001.xsd";

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

        //...
    }
    
    private void fillModel() {
        model.addGlobalElement(new QName("publication"));

        Assert.assertTrue("the root node must have one child node", model.getChildCount() == 1);
        
        XSNode<XSNode, XSNode> element = model.getChildAt(0);
        Assert.assertTrue("the node 'publication' must be the represented by an element node", element instanceof XSGlobalElement);
        Assert.assertEquals("the node 'publication' should have four child nodes", 4, element.getChildCount());

        XSNode node0 = element.getChildAt(0);
        Assert.assertTrue("the node 'doi' must be the represented by an attribute node", node0 instanceof XSAttribute);
        Assert.assertEquals("the node 'doi' should have no child nodes", 0, node0.getChildCount());
        
        XSAttribute doi = (XSAttribute)node0;
        doi.setUserObject("0.1371/journal.pone.0107889");
        
        XSNode node1 = element.getChildAt(1);
        Assert.assertTrue("the node 'title' must be the represented by a particle node", node1 instanceof XSParticle);
        Assert.assertEquals("the node 'doi' should have no child nodes", 0, node1.getChildCount());

        XSParticle title = (XSParticle)node1;
        title.setUserObject("BioSWR - Semantic Web Services Registry for Bioinformatics.");
        
        XSNode node2 = element.getChildAt(2);
        Assert.assertTrue("the node 'journal' must be the represented by a particle node", node2 instanceof XSParticle);
        Assert.assertEquals("the node 'journal' should have no child nodes", 0, node2.getChildCount());

        XSParticle journal = (XSParticle)node2;
        journal.setUserObject("PLoS ONE");
        
        XSNode<XSNode, XSNode> node3 = element.getChildAt(3);
        Assert.assertTrue("the node 'author' must be the represented by a particle node", node3 instanceof XSParticle);
        Assert.assertEquals("the node 'author' should have one child node", 1, node3.getChildCount());

        XSNode<XSNode, XSNode> type0 = node3.getChildAt(0);
        Assert.assertTrue("the 'author' type node must be the represented by a type node", type0 instanceof XSType);
        Assert.assertEquals("the 'author' type node should have three child nodes", 3, type0.getChildCount());

        XSNode particle0 = type0.getChildAt(0);
        Assert.assertTrue("the node 'first_name' must be the represented by a particle node", particle0 instanceof XSParticle);
        Assert.assertEquals("the node 'first_name' should have no child nodes", 0, particle0.getChildCount());

        XSParticle firstName = (XSParticle)particle0;
        firstName.setUserObject("Dmitry");
        
        XSNode particle1 = type0.getChildAt(1);
        Assert.assertTrue("the node 'last_name' must be the represented by a particle node", particle1 instanceof XSParticle);
        Assert.assertEquals("the node 'last_name' should have no child nodes", 0, particle1.getChildCount());

        XSParticle lastName = (XSParticle)particle1;
        lastName.setUserObject("Repchevsky");

        XSNode<XSNode, XSNode> particle2 = type0.getChildAt(2);
        Assert.assertTrue("the node 'affiliation' must be the represented by a particle node", particle2 instanceof XSParticle);
        Assert.assertEquals("the node 'affiliation' should have one child node", 1, particle2.getChildCount());

        XSNode type = particle2.getChildAt(0);
        Assert.assertTrue("the 'affiliation' type node must be the represented by a type node", type instanceof XSType);
        Assert.assertEquals("the 'affiliation' type node should have no child nodes", 0, type.getChildCount());
        
        XSType affiliation = (XSType)type;
        affiliation.setUserObject("Barcelona Supercomputing Center");
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

            //...
        } catch(SAXException ex) {
            Assert.fail(ex.getMessage());
        } catch(IOException ex) {
            Assert.fail(ex.getMessage());
        }
    }
}
