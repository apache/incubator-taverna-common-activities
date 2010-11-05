package net.sf.taverna.t2.activities.xpath;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.InvalidXPathException;
import org.dom4j.Node;
import org.dom4j.XPath;
import org.dom4j.XPathException;

import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.workflowmodel.processor.activity.AbstractAsynchronousActivity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivity;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivityCallback;

/**
 * Enhanced XPath activity.
 * 
 * @author Sergejs Aleksejevs
 */
public class XPathActivity extends
		AbstractAsynchronousActivity<XPathActivityConfigurationBean>
		implements AsynchronousActivity<XPathActivityConfigurationBean>
{
  
	// These ports are default ones (and only ones - XPath activity will not have dynamic ports)
  private static final String IN_XML = "xml_text";
  private static final String OUT_TEXT = "nodelist";
	private static final String OUT_XML = "nodelistAsXML";
	
	// Configuration bean for this activity - essentially defines a particular instance
	// of the activity through the values of its parameters
	private XPathActivityConfigurationBean configBean;
	
	
	@Override
  public XPathActivityConfigurationBean getConfiguration() {
    return this.configBean;
  }
	
	
	@Override
	public void configure(XPathActivityConfigurationBean configBean) throws ActivityConfigurationException
  {
	  // Check configBean is valid
	  if (! configBean.isValid()) {
	    throw new ActivityConfigurationException("Invalid configuration of XPath activity...");  // TODO - check this
	  }
	  
		// Store for getConfiguration()
		this.configBean = configBean;
		
		// (Re)create input/output ports depending on configuration
		configurePorts();
	}
	
	
	protected void configurePorts()
	{
		// ---- REMOVE OLD PORTS ----
	  
	  // In case we are being reconfigured - remove existing ports first to avoid duplicates
		removeInputs();
		removeOutputs();
		
		
		// ---- CREATE NEW INPUTS AND OUTPUTS ----
		
		// all ports in this activity are static, so no dependency on the values in config bean
		
		// single input port: the input XML text will be treated as String for now
	  addInput(IN_XML, 0, true, null, String.class);
		
		// all outputs are of depth 1 - lists of strings / XML nodes
		addOutput(OUT_TEXT, 1);
		addOutput(OUT_XML, 1);
	}
	
	
	
	/**
	 * This method executes pre-configured instance of XPath activity.
	 */
	public void executeAsynch(final Map<String,T2Reference> inputs, final AsynchronousActivityCallback callback)
	{
		// Don't execute service directly now, request to be run asynchronously
		callback.requestRun(new Runnable() {
			public void run() {
			  
				InvocationContext context = callback.getContext();
				ReferenceService referenceService = context.getReferenceService();
				
				// ---- RESOLVE INPUT ----
				
				String xmlInput = (String) referenceService.renderIdentifier(inputs.get(IN_XML), String.class, context);
				
				
				// ---- DO THE ACTUAL SERVICE INVOCATION ----
				
				List<Node> matchingNodes = new ArrayList<Node>();
				
				// only attempt to execute XPath expression if there is some input data
				if (xmlInput != null && xmlInput.length() > 0)
				{
  				// XPath configuration is taken from the config bean
  				XPath expr = null;
  		    try {
  		      expr = DocumentHelper.createXPath(configBean.getXpathExpression());
  		      expr.setNamespaceURIs(configBean.getXpathNamespaceMap());
  		    }
  		    catch (InvalidXPathException e)
  		    {
  		      callback.fail("Incorrect XPath Expression -- XPath processing library " +
  		      		"reported the following error: " + e.getMessage(), e);
  		      
            // make sure we don't call callback.receiveResult later
  		      return;
  		    }
  		    
  		    // Document to apply XPath expression to is the one that was obtained through the input of the processor
  		    Document doc = null;
  		    try {
  		      doc = DocumentHelper.parseText(xmlInput);
  		    }
  		    catch (DocumentException e)
  		    {
  		      callback.fail("XML document was not valid -- XPath processing library " +
                "reported the following error: " + e.getMessage(), e);
            
            // make sure we don't call callback.receiveResult later
            return;
  		    }
  		    
  		    
  		    try {
  		      matchingNodes = expr.selectNodes(doc);
  		    }
  		    catch (XPathException e)
  		    {
  		      callback.fail("Unexpected error has occurred while executing the XPath expression. " +
  		      		"-- XPath processing library reported the following error:\n" + e.getMessage(), e);
  		      
  		      // make sure we don't call callback.receiveResult later 
  		      return;
  		    }
				}
				
				
		    // --- PREPARE OUTPUTS ---
				
		    List<String> outNodesText = new ArrayList<String>();
		    List<String> outNodesXML = new ArrayList<String>();
		    for (Node n : matchingNodes) {
		      if (n.getStringValue() != null && n.getStringValue().length() > 0) {
		        outNodesText.add(n.getStringValue());
		      }
		      outNodesXML.add(n.asXML());
		    }
		    
				
				// ---- REGISTER OUTPUTS ----
		    
				Map<String, T2Reference> outputs = new HashMap<String, T2Reference>();
				
				T2Reference outNodesAsText = referenceService.register(outNodesText, 1, true, context);
				outputs.put(OUT_TEXT, outNodesAsText);
				
				T2Reference outNodesAsXML = referenceService.register(outNodesXML, 1, true, context);
        outputs.put(OUT_XML, outNodesAsXML);
				
				// return map of output data, with empty index array as this is
				// the only and final result (this index parameter is used if
				// pipelining output)
				callback.receiveResult(outputs, new int[0]);
			}
		});
	}

}
