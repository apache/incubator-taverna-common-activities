package net.sf.taverna.t2.activities.xpath;

import java.io.Serializable;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import net.sf.taverna.t2.workflowmodel.processor.config.ConfigurationBean;
import net.sf.taverna.t2.workflowmodel.processor.config.ConfigurationProperty;

import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.InvalidXPathException;

/**
 *
 * @author Sergejs Aleksejevs
 */
@ConfigurationBean(uri = XPathActivity.URI + "#Config")
public class XPathActivityConfigurationBean implements Serializable {
	// --- CONSTANTS ---
	public static final int XPATH_VALID = 1;
	public static final int XPATH_EMPTY = 0;
	public static final int XPATH_INVALID = -1;

	private String xmlDocument;
	private String xpathExpression;
	private Map<String, String> xpathNamespaceMap;

	/**
	 * @return An instance of the {@link XPathActivityConfigurationBean}
	 *         pre-configured with default settings for all parameters.
	 * @throws DocumentException
	 */
	public static XPathActivityConfigurationBean getDefaultInstance() {
		// document will not be set
		XPathActivityConfigurationBean defaultBean = new XPathActivityConfigurationBean();
		defaultBean.setXpathExpression("/");
		defaultBean.setXpathNamespaceMap(new HashMap<String, String>(0));

		return (defaultBean);
	}

	/**
	 * Validates an XPath expression.
	 *
	 * @return {@link XPathActivityConfigurationBean#XPATH_VALID XPATH_VALID} -
	 *         if the expression is valid;<br/>
	 *         {@link XPathActivityConfigurationBean#XPATH_EMPTY XPATH_EMPTY} -
	 *         if expression is empty;<br/>
	 *         {@link XPathActivityConfigurationBean#XPATH_INVALID
	 *         XPATH_INVALID} - if the expression is invalid / ill-formed.<br/>
	 */
	public static int validateXPath(String xpathExpressionToValidate) {
		// no XPath expression
		if (xpathExpressionToValidate == null
				|| xpathExpressionToValidate.trim().length() == 0) {
			return (0);
		}

		try {
			// try to parse the XPath expression...
			DocumentHelper.createXPath(xpathExpressionToValidate.trim());
			// ...success
			return (1);
		} catch (InvalidXPathException e) {
			// ...failed to parse the XPath expression: notify of the error
			return (-1);
		}
	}

	/**
	 * Tests validity of the configuration held in this bean.
	 *
	 * @return <code>true</code> if the configuration in the bean is valid;
	 *         <code>false</code> otherwise.
	 */
	public boolean isValid() {
		return (xpathExpression != null
				&& validateXPath(xpathExpression) == XPATH_VALID && getXpathNamespaceMap() != null);
	}

	public String getXmlDocument() {
		return xmlDocument;
	}

	@ConfigurationProperty(name = "exampleXmlDocument", label = "Example XML document", required = false)
	public void setXmlDocument(String xmlDocument) {
		this.xmlDocument = xmlDocument;
	}

	public String getXpathExpression() {
		return xpathExpression;
	}

	@ConfigurationProperty(name = "xpathExpression", label = "XPath expression")
	public void setXpathExpression(String xpathExpression) {
		this.xpathExpression = xpathExpression;
	}

	public Map<String, String> getXpathNamespaceMap() {
		return xpathNamespaceMap;
	}

	public void setXpathNamespaceMap(Map<String, String> xpathNamespaceMap) {
		this.xpathNamespaceMap = xpathNamespaceMap;
	}

	@ConfigurationProperty(name = "xpathNamespaceMap", label = "XPath Namespace Map", required = false)
	public void setXpathNamespaceMap(Set<NamespaceMapping> xpathNamespaceMap) {
		Map<String, String> namespaceMap = new HashMap<String, String>();
		for (NamespaceMapping namespaceMapping : xpathNamespaceMap) {
			namespaceMap.put(namespaceMapping.getPrefix(), namespaceMapping.getUri().toASCIIString());
		}
		setXpathNamespaceMap(namespaceMap);
	}

	@ConfigurationBean(uri = XPathActivity.URI + "/NamespaceMapping")
	public static class NamespaceMapping {
		private String prefix;

		private URI uri;

		public String getPrefix() {
			return prefix;
		}

		@ConfigurationProperty(name = "prefix", label = "Namespace Prefix")
		public void setPrefix(String prefix) {
			this.prefix = prefix;
		}

		public URI getUri() {
			return uri;
		}

		@ConfigurationProperty(name = "uri", label = "Namespace URI")
		public void setUri(URI uri) {
			this.uri = uri;
		}

	}

}
