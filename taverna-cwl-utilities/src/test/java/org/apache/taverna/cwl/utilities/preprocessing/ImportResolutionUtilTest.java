package org.apache.taverna.cwl.utilities.preprocessing;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.*;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import static org.junit.Assert.*;

public class ImportResolutionUtilTest {
    JsonNode localFile,localFileResult, fileOverHttPWithFragment,fileOverHttPWithFragmentResult,
            fileOverHttpWithNamespace,fileOverHttpWithNamespaceResult;

    static  Process exec;
    @BeforeClass
    public static void setUpHttPServer() throws IOException {
        String cmd[]={"python","-m","SimpleHTTPServer","8000"};
         exec = Runtime.getRuntime().exec(cmd,null,new File(ImportResolutionUtilTest.class.getResource("/preprocessing/serverContent/").getPath()));
    }

    @Before
    public void setUp() throws Exception {

        localFile =getNode("/preprocessing/ImportResolutionUtil-ReplaceMethod/localFile.yaml");
        fileOverHttPWithFragment =getNode("/preprocessing/ImportResolutionUtil-ReplaceMethod/fileOverHttPWithFragment.yaml");
        fileOverHttpWithNamespace=getNode("/preprocessing/ImportResolutionUtil-ReplaceMethod/fileOverHttpWithNamespace.yaml");
        localFileResult=getNode("/preprocessing/ImportResolutionUtil-ReplaceMethod/localFileResult.yaml");
        fileOverHttPWithFragmentResult= getNode("/preprocessing/ImportResolutionUtil-ReplaceMethod/fileOverHttPWithFragmentResult.yaml");
        fileOverHttpWithNamespaceResult = getNode("/preprocessing/ImportResolutionUtil-ReplaceMethod/fileOverHttpWithNamespaceResult.yaml");
    }
    private JsonNode getNode(String path){
        Yaml reader = new Yaml();
        ObjectMapper mapper = new ObjectMapper();
        return mapper.valueToTree(reader.load(ImportResolutionUtilTest.class.getResourceAsStream(path)));
    }
    @Test
    public void testReplaceOverHttpWithFragment(){
        ImportResolutionUtil cwlPreprocessor = new ImportResolutionUtil(fileOverHttPWithFragment, Paths.get(ImportResolutionUtilTest.class.getResource("/preprocessing/ImportResolutionUtil-ReplaceMethod/").getPath()));
        cwlPreprocessor.replace(fileOverHttPWithFragment);
        assertEquals(fileOverHttPWithFragmentResult, fileOverHttPWithFragment);
    }
    @Test
    public void testReplaceoverLocalFile(){
        ImportResolutionUtil cwlPreprocessor = new ImportResolutionUtil(localFile, Paths.get(ImportResolutionUtilTest.class.getResource("/preprocessing/ImportResolutionUtil-ReplaceMethod/").getPath()));
        cwlPreprocessor.replace(localFile);
        assertEquals(localFileResult, localFile);
    }
    @Test
    public void testReplaceOverHttpWithNamespace(){
        ImportResolutionUtil cwlPreprocessor = new ImportResolutionUtil(fileOverHttpWithNamespace, Paths.get(ImportResolutionUtilTest.class.getResource("/preprocessing/ImportResolutionUtil-ReplaceMethod/").getPath()));
        cwlPreprocessor.replace(fileOverHttpWithNamespace);
        assertEquals(fileOverHttpWithNamespaceResult, fileOverHttpWithNamespace);
    }

    @After
    public void tearDown() throws Exception {

    }
    @AfterClass
    public static  void stopHttPServer(){
        exec.destroy();
    }
}