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
import static org.junit.Assert.*;

public class LinkedResolutionUtilTest {
    JsonNode processUri;
    @Before
    public void setUp() throws Exception {

        processUri = getNode("/preprocessing/LinkedResoultionUtil-process-method/processURI.yaml");
    }

    @Test
    public void testProcess() throws URISyntaxException {
        String path =LinkedResolutionUtil.class.getResource("/preprocessing/LinkedResoultionUtil-process-method/").getPath();
        LinkedResolutionUtil linkedResolutionUtil = new LinkedResolutionUtil(processUri, Paths.get(path));
        assertEquals(new URI("http://example.com/one"),getUri1(linkedResolutionUtil,"uri1"));
        assertEquals(new URI("http://example.com/base#two"),getUri1(linkedResolutionUtil,"uri2"));
        assertEquals(new URI("http://example.com/four#five"),getUri1(linkedResolutionUtil,"uri3"));
        assertEquals(new URI("http://example.com/acid#six"),getUri1(linkedResolutionUtil,"uri4"));
        assertEquals(new URI("file://"+path+"dummy1.yaml"),getUri1(linkedResolutionUtil,"uri5"));
        assertEquals(new URI("file://"+path+"dummy1.yaml#hello"),getUri1(linkedResolutionUtil,"uri6"));

    }

    private URI getUri1(LinkedResolutionUtil linkedResolutionUtil,String uri) throws URISyntaxException {
        return linkedResolutionUtil.process(processUri.get(uri));
    }

    @After
    public void tearDown() throws Exception {


    }
}
