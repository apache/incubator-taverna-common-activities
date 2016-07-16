package org.apache.taverna.cwl;



import static org.junit.Assert.assertEquals;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.yaml.snakeyaml.Yaml;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class CwlActivityFactoryTest {
	private static Logger logger = Logger.getLogger(CwlActivityFactoryTest.class);
	CwlActivityFactory cwlActivityFactory;
	public static final String CWL_CONF = "cwl_conf";
	private  JsonNode root;
	
	@Before
	public void setUp() throws Exception {
		System.out.println("1");
		cwlActivityFactory = new CwlActivityFactory();
		Yaml reader = new Yaml();
		ObjectMapper mapper = new  ObjectMapper();
		ObjectNode cwlFile = null;
		try {
			cwlFile = mapper.valueToTree(reader.load(new FileInputStream(getClass().getResource("/CWLFiles/customtool1.cwl").getPath())));
		} catch (IllegalArgumentException | FileNotFoundException e) {
			System.out.println(e);
		} 
	
		root =mapper.createObjectNode();
		((ObjectNode) root).put(CWL_CONF, cwlFile);
	}

	@Test
	public void testSchemaJson() {
		ObjectMapper objectMapper = new ObjectMapper();
		 try {
			assertEquals(cwlActivityFactory.getActivityConfigurationSchema(), objectMapper.readTree(getClass().getResource("/schema.json")));
		} catch (IOException e) {
			logger.error(e);
		}
		
	}
	@Test //FIXME 
	public void testgetInputPorts() {
		
//		try {
//			cwlActivityFactory.getInputPorts(root);
//		} catch (ActivityConfigurationException e) {
//			logger.error(e);
//			
//		}
	}
}
