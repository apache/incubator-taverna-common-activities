package org.apache.taverna.cwl.utilities;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.taverna.cwl.utilities.preprocessing.ImportResolutionUtilTest;
import org.yaml.snakeyaml.Yaml;

/**
 * Created by maanadev on 4/8/17.
 */
public class ImportNodeViaFile {
    public static JsonNode getNode(String path){
        Yaml reader = new Yaml();
        ObjectMapper mapper = new ObjectMapper();
        return mapper.valueToTree(reader.load(ImportResolutionUtilTest.class.getResourceAsStream(path)));
    }
}
