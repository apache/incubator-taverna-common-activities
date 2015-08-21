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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.apache.taverna.wsdl.parser.schema.XSModel;
import org.apache.taverna.wsdl.parser.schema.XSNode;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.junit.Assert;
import org.junit.Before;

/**
 * @author Dmitry Repchevsky
 */

public abstract class TestSchemaTreeModel {
    
    XSModel<XSNode, XSNode> model;
    
    @Before
    public void init() {
        model = new XSModel();
    }

    /**
     * Loads the model from provided XML Schema file
     * 
     * @param xsd XML Schema URI location
     */
    void loadModel(final String xsd) {
        XmlSchemaCollection schemas = new XmlSchemaCollection();
        
        try {
            InputStream in = TestSchemaTreeModel.class.getClassLoader().getResourceAsStream(xsd);
            try {
                schemas.read(new InputStreamReader(in));
            } finally {
                in.close();
            }
        } catch(IOException ex) {
            Assert.fail(ex.getMessage());
        }

        model.setSchemaCollection(schemas);
    }
    
    /**
     * Fills the model with provided XML file
     * 
     * @param xml XML document URI location
     */
    void fillModel(final String xml) {
        try {
            InputStream in = TestSchemaTreeModel.class.getClassLoader().getResourceAsStream(xml);

            try {
                XMLInputFactory f = XMLInputFactory.newInstance();
                XMLStreamReader reader = f.createXMLStreamReader(in);
                model.read(reader);
            } catch(XMLStreamException ex) {
                Assert.fail(ex.getMessage());
            } finally {
                in.close();
            }
        } catch(IOException ex) {
            Assert.fail(ex.getMessage());
        }
    }
}
