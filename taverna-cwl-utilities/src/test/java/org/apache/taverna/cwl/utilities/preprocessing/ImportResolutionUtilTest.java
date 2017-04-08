/***************************************************************************************************
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **************************************************************************************************/

package org.apache.taverna.cwl.utilities.preprocessing;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.*;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;

import static org.apache.taverna.cwl.utilities.ImportNodeViaFile.getNode;
import static org.junit.Assert.assertEquals;

public class ImportResolutionUtilTest {
    static Process exec;
    JsonNode localFile, localFileResult, fileOverHttPWithFragment, fileOverHttPWithFragmentResult,
            fileOverHttpWithNamespace, fileOverHttpWithNamespaceResult, testProcessNode, testProcessNodeResult;

    @BeforeClass
    public static void setUpHttPServer() throws IOException {
        String cmd[] = {"python", "-m", "SimpleHTTPServer", "8000"};
        exec = Runtime.getRuntime().exec(cmd, null, new File(ImportResolutionUtilTest.class.getResource("/preprocessing/serverContent/").getPath()));
    }

    @AfterClass
    public static void stopHttPServer() {
        exec.destroy();
    }

    @Before
    public void setUp() throws Exception {

        localFile = getNode("/preprocessing/ImportResolutionUtil-replace-Method/localFile.yaml");
        fileOverHttPWithFragment = getNode("/preprocessing/ImportResolutionUtil-replace-Method/fileOverHttPWithFragment.yaml");
        fileOverHttpWithNamespace = getNode("/preprocessing/ImportResolutionUtil-replace-Method/fileOverHttpWithNamespace.yaml");
        localFileResult = getNode("/preprocessing/ImportResolutionUtil-replace-Method/localFileResult.yaml");
        fileOverHttPWithFragmentResult = getNode("/preprocessing/ImportResolutionUtil-replace-Method/fileOverHttPWithFragmentResult.yaml");
        fileOverHttpWithNamespaceResult = getNode("/preprocessing/ImportResolutionUtil-replace-Method/fileOverHttpWithNamespaceResult.yaml");
        testProcessNode = getNode("/preprocessing/ImportResoultionUtil-processNode-Method/processNode.yaml");
        testProcessNodeResult = getNode("/preprocessing/ImportResoultionUtil-processNode-Method/processNodeResult.yaml");
        System.out.println(testProcessNode);
    }

    @Test
    public void testReplaceOverHttpWithFragment() {
        ImportResolutionUtil cwlPreprocessor = new ImportResolutionUtil(fileOverHttPWithFragment, Paths.get(ImportResolutionUtilTest.class.getResource("/preprocessing/ImportResolutionUtil-replace-Method/").getPath()));
        cwlPreprocessor.replace(fileOverHttPWithFragment);
        assertEquals(fileOverHttPWithFragmentResult, fileOverHttPWithFragment);
    }

    @Test
    public void testReplaceOverLocalFile() {
        ImportResolutionUtil cwlPreprocessor = new ImportResolutionUtil(localFile, Paths.get(ImportResolutionUtilTest.class.getResource("/preprocessing/ImportResolutionUtil-replace-Method/").getPath()));
        cwlPreprocessor.replace(localFile);
        assertEquals(localFileResult, localFile);
    }

    @Test
    public void testReplaceOverHttpWithNamespace() {
        ImportResolutionUtil cwlPreprocessor = new ImportResolutionUtil(fileOverHttpWithNamespace, Paths.get(ImportResolutionUtilTest.class.getResource("/preprocessing/ImportResolutionUtil-replace-Method/").getPath()));
        cwlPreprocessor.replace(fileOverHttpWithNamespace);
        assertEquals(fileOverHttpWithNamespaceResult, fileOverHttpWithNamespace);
    }

    @Test
    public void testProcessNode() throws URISyntaxException {
        ImportResolutionUtil cwlPreprocessor = new ImportResolutionUtil(testProcessNode, Paths.get(ImportResolutionUtilTest.class.getResource("/preprocessing/ImportResoultionUtil-processNode-Method").getPath()));
        cwlPreprocessor.processNode(testProcessNode);
        assertEquals(testProcessNode, testProcessNodeResult);
    }

    @After
    public void tearDown() throws Exception {

    }
}