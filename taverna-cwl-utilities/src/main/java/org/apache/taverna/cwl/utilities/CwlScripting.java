package org.apache.taverna.cwl.utilities;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class CwlScripting {

	
	public static void main(String[] args) {
		ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("nashorn");
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode node=mapper.createObjectNode();
        node.put("hello", "world");
        engine.put("import.yml", node);
        // evaluate JavaScript code
        try {
			engine.eval("print(\"import.yml\");");
		} catch (ScriptException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
}
