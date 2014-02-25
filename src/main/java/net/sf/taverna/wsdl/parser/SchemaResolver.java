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

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.ws.commons.schema.resolver.URIResolver;
import org.w3c.dom.Element;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Schema Resolver that resolves schemas for various APIs at a time (SAX, DOM and Apache XML Schema 2.0)
 * It also used for keeping a List of DOM Elements  defined in WSDL Types.
 * 
 * @author Dmitry Repchevsky
 */

public class SchemaResolver extends ArrayList<Element> implements EntityResolver, URIResolver, LSResourceResolver {
    public final String baseUri;

    public SchemaResolver(String baseUri) {
        this.baseUri = baseUri;
    }

    @Override
    public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
        if (publicId != null) {
            for (Element element : this) {
                String targetNamespace = element.getAttribute("targetNamespace");
                if (publicId.equals(targetNamespace)) {
                    InputSource source = getInputSource(element);
                    source.setPublicId(publicId);

                    return source;
                }
            }
        }

        return resolveEntity2(publicId, systemId, this.baseUri);
    }


    @Override
    public InputSource resolveEntity(String targetNamespace, String schemaLocation, String baseUri) {
        try {
            return baseUri == null || baseUri.isEmpty() ? resolveEntity(targetNamespace, schemaLocation) :
                                                          resolveEntity2(targetNamespace, schemaLocation, baseUri);
        }
        catch(Exception ex) {}
        
        return null;
    }
    
    private InputSource resolveEntity2(String targetNamespace, String schemaLocation, String baseUri) throws IOException
    {
        URL url = new URL(new URL(baseUri), schemaLocation);

        InputStream in = url.openStream();
        InputSource source = new InputSource(in);
        source.setPublicId(targetNamespace);
        source.setSystemId(url.toString());

        return source;
    }
    
    public InputSource getInputSource(Element element) throws IOException {
        CharArrayWriter writer = new CharArrayWriter();

        try {
            TransformerFactory.newInstance().newTransformer().transform(new DOMSource(element), new StreamResult(writer));
        } catch (TransformerException ex) {
            throw new IOException(ex.getMessage(), ex);
        }
        InputSource source = new InputSource(new StringReader(writer.toString()));
        
        String publicId = element.getAttribute("targetNamespace");
        source.setPublicId(publicId);

        if (publicId.isEmpty()) {
            //source.setPublicId(element.getNamespaceURI());
            // if the element has no targetNamespace defined, provide a synthetic systemId, so both publicId = "" + systenId never clashes.
            URI base = URI.create(baseUri);
            URI synthetic;
            try {
               synthetic = new URI(base.getScheme(), base.getUserInfo(), base.getHost(), base.getPort(), base.getPath(), base.getQuery(), String.valueOf(element.hashCode()));
            } catch(URISyntaxException ex) {
                synthetic = base; // never ever happens
            }
            source.setSystemId(synthetic.toString());
        } else {
            source.setSystemId(baseUri);
        }

        return source;
    }

    @Override
    public LSInput resolveResource(String type, String namespaceURI, String publicId, String systemId, String baseURI) {
        try {
            InputSource source = resolveEntity(publicId != null ? publicId : namespaceURI, systemId);
            if (source != null) {
                return new LSInputImpl(source);
            }
        } catch (Exception ex) {}
        
        return null;
    }
}