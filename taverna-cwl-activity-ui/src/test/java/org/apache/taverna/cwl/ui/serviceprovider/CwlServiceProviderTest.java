package org.apache.taverna.cwl.ui.serviceprovider;

import static org.junit.Assert.*;

import java.util.Collection;

import org.apache.taverna.scufl2.api.configurations.Configuration;
import org.apache.taverna.servicedescriptions.ServiceDescription;
import org.apache.taverna.servicedescriptions.ServiceDescriptionProvider;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class CwlServiceProviderTest {

	private static final String FINISHED="Finished";
	class Results implements ServiceDescriptionProvider.FindServiceDescriptionsCallBack {
		String result;
		@Override
		public void partialResults(Collection<? extends ServiceDescription> serviceDescriptions) {
			
		}

		@Override
		public void status(String message) {
			
		}

		@Override
		public void warning(String message) {
			
		}

		@Override
		public void finished() {
			result=FINISHED;
			
		}

		@Override
		public void fail(String message, Throwable ex) {
			
		}
		
	}
	
	Results results;
	static CwlServiceProvider cwlServiceProvider;
	@Before
	public void setUp() throws Exception {
		 cwlServiceProvider = new CwlServiceProvider();
		 results=new  Results();
	}

	@SuppressWarnings("static-access")
	@Test
	public void defaultConfigurationtest() {
		Configuration c = new Configuration();
		ObjectNode conf = c.getJsonAsObjectNode();
		conf.put("path", "");
		assertEquals(c.getJson(),cwlServiceProvider.getDefaultConfiguration().getJson() );
	}
	@Test
	public void findServiceDescriptionsAsynctest(){
		cwlServiceProvider.findServiceDescriptionsAsync(results);
		assertEquals(FINISHED,results.result );
	}

}
