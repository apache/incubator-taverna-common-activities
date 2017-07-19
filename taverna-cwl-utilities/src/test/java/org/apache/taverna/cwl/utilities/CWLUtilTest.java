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
package org.apache.taverna.cwl.utilities;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(JUnitParamsRunner.class)

public class CWLUtilTest {


    @Test
    @Parameters(source = CWLUtilTestResource.class, method = "extractDescriptionResources")

    public void extractDescriptionTest(CWLUtil cwlUtil, String expected, JsonNode input) {
        PortDetail detail = new PortDetail();

        cwlUtil.extractDescription(input, detail);
        assertEquals(expected, detail.getDescription());


    }

    @Test
    @Parameters(source = CWLUtilTestResource.class, method = "processResources")
    public void processTest(Map<String, Integer> expected, Map<String, Integer> actual) {

        for (Map.Entry<String, Integer> input : expected.entrySet()) {
            assertEquals(input.getValue(), actual.get(input.getKey()));
        }

    }

    @Test
    @Parameters(source = CWLUtilTestResource.class, method = "isValidArrayTypeTrueResources")
    public void isValidArrayTypeTrueTest(CWLUtil cwlUtil, String input) {


        assertTrue(cwlUtil.isValidArrayType(input));
    }

    @Test
    @Parameters(source = CWLUtilTestResource.class, method = "isValidArrayTypeFalseResources")
    public void isValidArrayTypeFalseTest(CWLUtil cwlUtil, String input) {


        assertTrue(!cwlUtil.isValidArrayType(input));

    }


    @Test
    @Parameters(source = CWLUtilTestResource.class, method = "isValidDataTypeTrueResources")
    public void isValidDataTypeTrueTest(CWLUtil cwlUtil, ArrayNode node) {

        assertTrue(cwlUtil.isValidDataType(node));
    }

    @Test
    @Parameters(source = CWLUtilTestResource.class, method = "isValidDataTypeFalseResources")
    public void isValidDataTypeFalseTest(CWLUtil cwlUtil, ArrayNode node) {

        assertTrue(!cwlUtil.isValidDataType(node));
    }

    @Test
    @Parameters(source = CWLUtilTestResource.class, method = "processNameSpaceResources")
    public void processNameSpaceTest(CWLUtil cwlUtil, String tag, String format, int size) {
        JsonNode nameSpace = cwlUtil.getNameSpace();

        assertEquals(size, nameSpace.size());
        assertTrue(nameSpace.has(tag));
        assertEquals(format, nameSpace.get(tag).asText());
    }

    @Test
    @Parameters(source = CWLUtilTestResource.class, method = "extractLabelResources")
    public void extractLabelTest(CWLUtil cwlUtil, JsonNode input, String expected) {
        PortDetail detail = new PortDetail();

        cwlUtil.extractLabel(input, detail);
        assertEquals(expected, detail.getLabel());

    }

    @Test
    @Parameters(source = CWLUtilTestResource.class, method = "figureOutFormatsResources")
    public void figureOutFormatsTest(CWLUtil cwlUtil, String format, PortDetail detail, String expected, int index) {

        cwlUtil.figureOutFormats(format, detail);
        assertEquals(expected, detail.getFormat().get(index));
    }

    @Test
    @Parameters(source = CWLUtilTestResource.class, method = "processDetailsResources")
    public void processDetailsTest(CWLUtil cwlUtil, Map<String, PortDetail> expected) {

        Map<String, PortDetail> actual = cwlUtil.processInputDetails();
        for (Map.Entry<String, PortDetail> input : expected.entrySet()) {
            PortDetail expectedDetail = input.getValue();
            PortDetail actualDetail = actual.get(input.getKey());
            assertEquals(expectedDetail.getFormat(), actualDetail.getFormat());
            assertEquals(expectedDetail.getLabel(), actualDetail.getLabel());
            assertEquals(expectedDetail.getDescription(), actualDetail.getDescription());
        }

    }
}
