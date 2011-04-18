/**
 * 
 */
package net.sf.taverna.t2.activities.externaltool.local;

import java.io.IOException;
import java.io.StringReader;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import net.sf.taverna.t2.activities.externaltool.InvocationCreator;
import de.uni_luebeck.inb.knowarc.grid.re.RuntimeEnvironmentConstraint;
import de.uni_luebeck.inb.knowarc.usecases.UseCaseDescription;
import de.uni_luebeck.inb.knowarc.usecases.invocation.UseCaseInvocation;
import de.uni_luebeck.inb.knowarc.usecases.invocation.local.LocalUseCaseInvocation;

/**
 * @author alanrw
 *
 */
public final class LocalInvocationCreator implements
		InvocationCreator {
	
	private static Logger logger = Logger.getLogger(LocalInvocationCreator.class);

	private static SAXBuilder builder = new SAXBuilder();

	public boolean equals(Object o) {
		return (o instanceof LocalInvocationCreator);
	}

	@Override
	public boolean canHandle(String mechanismType) {
		return mechanismType.equals(LocalUseCaseInvocation.LOCAL_USE_CASE_INVOCATION_TYPE);
	}

	@Override
	public UseCaseInvocation convert(String xml, UseCaseDescription description) {
		UseCaseInvocation result = null;
		try {
			Document document;
			try {
				document = builder.build(new StringReader(xml));
			} catch (JDOMException e1) {
				logger.error("Null invocation", e1);
				return null;
			} catch (IOException e1) {
				logger.error("Null invocation", e1);
				return null;
			}
			Element top = document.getRootElement();
			Element directoryElement = top.getChild("directory");
			String tempDir = null;
			if (directoryElement != null) {
				tempDir = directoryElement.getText();
			}
			String shellPrefix = null;
			Element shellPrefixElement = top.getChild("shellPrefix");
			if (shellPrefixElement != null) {
				shellPrefix = shellPrefixElement.getText();
			}
			String linkCommand = null;
			Element linkCommandElement = top.getChild("linkCommand");
			if (linkCommandElement != null) {
				linkCommand = linkCommandElement.getText();
			}
			result = new LocalUseCaseInvocation(description, tempDir, shellPrefix, linkCommand);
		} catch (IOException e) {
			logger.error(e);
		}
		return result;
	}
}
