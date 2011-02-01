/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.sf.taverna.wsdl.xmlsplitter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.sf.taverna.wsdl.parser.TypeDescriptor;
import net.sf.taverna.wsdl.parser.WSDLParser;
import net.sf.taverna.wsdl.testutils.WSDLTestHelper;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author sowen
 */
public class XMLOutputSplitterTest extends WSDLTestHelper {

    @Test
	public void testRPCComplexWithInternalList() throws Exception {
            
        WSDLParser parser = new WSDLParser(WSDLTestHelper.wsdlResourcePath("menagerie-complex-rpc.wsdl"));
		TypeDescriptor descriptor = parser.getOperationOutputParameters("getComplexWithInternalList").get(0);
        XMLOutputSplitter splitter = new XMLOutputSplitter(descriptor, new String [] {"length","innerArray","innerList"}, new String [] {"text/plain","l('text/plain')","l('text/plain')"}, new String[] {"input"});
        String inputXML=getResourceContentsString("getComplexWithInternalListResponse.xml");
        Map<String,String> inputMap = new HashMap<String,String>();
        inputMap.put("input", inputXML);
        Map<String,Object> outputMap = splitter.execute(inputMap);
        assertNotNull(outputMap.get("length"));
        assertNotNull(outputMap.get("innerList"));
        assertNotNull(outputMap.get("innerArray"));

        assertEquals("4",outputMap.get("length"));

        List<String> array = ( List<String> )outputMap.get("innerArray");
		assertEquals(4,array.size());
		assertEquals("String A",array.get(0));
		assertEquals("String B",array.get(1));
		assertEquals("String C",array.get(2));
		assertEquals("String D",array.get(3));

        array = ( List<String> )outputMap.get("innerList");
		assertEquals(4,array.size());
		assertEquals("String A",array.get(0));
		assertEquals("String B",array.get(1));
		assertEquals("String C",array.get(2));
		assertEquals("String D",array.get(3));
    }

    @Test
    public void testWrappedArrayDefinedWithRestriction() throws Exception {
        WSDLParser parser = new WSDLParser(WSDLTestHelper.wsdlResourcePath("jws-online.wsdl"));
		TypeDescriptor descriptor = parser.getOperationOutputParameters("getSteadyStateTable").get(0);
        XMLOutputSplitter splitter = new XMLOutputSplitter(descriptor, new String [] {"model","fluxNames","fluxVals"}, new String [] {"text/plain","l('text/plain')","l('text/plain')"}, new String[] {"input"});
        String inputXML=getResourceContentsString("jws-splitter-input.xml");

        Map<String,String> inputMap = new HashMap<String,String>();
        inputMap.put("input", inputXML);
        Map<String,Object> outputMap = splitter.execute(inputMap);

        assertNotNull(outputMap.get("model"));
        assertNotNull(outputMap.get("fluxNames"));
        assertNotNull(outputMap.get("fluxVals"));

        assertEquals("teusink",outputMap.get("model"));
        List<String> array = ( List<String> )outputMap.get("fluxNames");
        assertEquals(17,array.size());
        assert(array.contains("v[G3PDH]"));

        array = ( List<String> )outputMap.get("fluxVals");
        assertEquals(17,array.size());
        assert(array.contains("88.15049285974906"));
    }

}
