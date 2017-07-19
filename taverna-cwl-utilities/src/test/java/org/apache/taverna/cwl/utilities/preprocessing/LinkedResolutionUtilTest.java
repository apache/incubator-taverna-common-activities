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
package org.apache.taverna.cwl.utilities.preprocessing;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;

import static org.apache.taverna.cwl.utilities.preprocessing.ImportNodeViaFile.getNode;
import static org.junit.Assert.assertEquals;

public class LinkedResolutionUtilTest {
    JsonNode processUri,relativeUri;

    @Before
    public void setUp() throws Exception {

        processUri = getNode("/preprocessing/LinkedResoultionUtil-process-method/processURI.yaml");
        relativeUri = getNode("/preprocessing/LinkedResoultionUtil-process-method/relativeURI.yaml");
    }

    @Test
    public void testProcess() throws URISyntaxException {
        String path = LinkedResolutionUtil.class.getResource("/preprocessing/LinkedResoultionUtil-process-method/").getPath();
        LinkedResolutionUtil linkedResolutionUtil = new LinkedResolutionUtil(processUri, Paths.get(path));
        assertEquals(new URI("http://example.com/one"), getUri(linkedResolutionUtil, "uri1",processUri));
        assertEquals(new URI("http://example.com/base#two"), getUri(linkedResolutionUtil, "uri2",processUri));
        assertEquals(new URI("http://example.com/four#five"), getUri(linkedResolutionUtil, "uri3",processUri));
        assertEquals(new URI("http://example.com/acid#six"), getUri(linkedResolutionUtil, "uri4",processUri));
        assertEquals(new URI("file://" + path + "dummy1.yaml"), getUri(linkedResolutionUtil, "uri5",processUri));
        assertEquals(new URI("file://" + path + "dummy1.yaml#hello"), getUri(linkedResolutionUtil, "uri6",processUri));



    }
    @Test
    public void testRelativeURI() throws URISyntaxException {
        String path = LinkedResolutionUtil.class.getResource("/preprocessing/LinkedResoultionUtil-process-method/").getPath();
        LinkedResolutionUtil linkedResolutionUtil = new LinkedResolutionUtil(relativeUri, Paths.get(path));
        assertEquals(new URI("http://example.com/a/b.cwl"), getUri(linkedResolutionUtil, "uri1",relativeUri));
    }

    private URI getUri(LinkedResolutionUtil linkedResolutionUtil, String uri, JsonNode node) throws URISyntaxException {
        return linkedResolutionUtil.process(node.get(uri));
    }

    @After
    public void tearDown() throws Exception {


    }
}
