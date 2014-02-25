/**
 * *****************************************************************************
 * Copyright (C) 2012 Spanish National Bioinformatics Institute (INB),
 * Barcelona Supercomputing Center and The University of Manchester
 *
 * Modifications to the initial code base are copyright of their respective
 * authors, or their employers as appropriate.
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307
 *****************************************************************************
 */

package net.sf.taverna.wsdl.parser;

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
