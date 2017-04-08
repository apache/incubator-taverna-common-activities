package org.apache.taverna.cwl.utilities.preprocessing;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.representer.Representer;
import org.yaml.snakeyaml.resolver.Resolver;

import java.io.InputStream;
import java.net.URI;

/**
 * Created by maanadev on 4/5/17.
 */
public interface ImportData {


    default JsonNode getNode(InputStream inputStream,String fragment){
        Yaml reader = new Yaml();
        ObjectMapper mapper = new ObjectMapper();
        if(fragment!=null){
            return mapper.valueToTree(reader.load(inputStream)).get(fragment);
        }
        return mapper.valueToTree(reader.load(inputStream));
    }
    JsonNode importData(URI uri);
}
