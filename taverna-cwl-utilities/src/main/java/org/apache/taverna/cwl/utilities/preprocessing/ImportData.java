package org.apache.taverna.cwl.utilities.preprocessing;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.net.URI;

/**
 * This interface represents the methods that can be used to import a node
 */
public interface ImportData {


    default JsonNode getNode(InputStream inputStream, String fragment) {
        Yaml reader = new Yaml();
        ObjectMapper mapper = new ObjectMapper();
        if (fragment != null) {
            return mapper.valueToTree(reader.load(inputStream)).get(fragment);
        }
        return mapper.valueToTree(reader.load(inputStream));
    }

    JsonNode importData(URI uri);
}
