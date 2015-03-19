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

package org.apache.taverna.wsdl.parser;

import java.io.InputStream;
import java.io.Reader;
import org.w3c.dom.ls.LSInput;
import org.xml.sax.InputSource;

/**
 * @author Dmitry Repchevsky
 */

public class LSInputImpl implements LSInput {
    private String baseURI;
    private InputSource source;
    
    public LSInputImpl(InputSource source) {
        this.source = source;
    }

    @Override
    public Reader getCharacterStream() {
        return source.getCharacterStream();
    }

    @Override
    public void setCharacterStream(Reader reader) {
        source.setCharacterStream(reader);
    }

    @Override
    public InputStream getByteStream() {
        return source.getByteStream();
    }

    @Override
    public void setByteStream(InputStream input) {
        source.setByteStream(input);
    }

    @Override
    public String getStringData() {
        return null;
    }

    @Override
    public void setStringData(String stringData) {
        
    }

    @Override
    public String getSystemId() {
        return source.getSystemId();
    }

    @Override
    public void setSystemId(String systemId) {
        source.setSystemId(systemId);
    }

    @Override
    public String getPublicId() {
        return source.getPublicId();
    }

    @Override
    public void setPublicId(String publicId) {
        source.setPublicId(publicId);
    }

    @Override
    public String getBaseURI() {
        return baseURI;
    }

    @Override
    public void setBaseURI(String baseURI) {
        this.baseURI = baseURI;
    }

    @Override
    public String getEncoding() {
        return source.getEncoding();
    }

    @Override
    public void setEncoding(String encoding) {
        source.setEncoding(encoding);
    }

    @Override
    public boolean getCertifiedText() {
        return false;
    }

    @Override
    public void setCertifiedText(boolean certifiedText) {
        
    }    
}
