/**
 *
 */
package net.sf.taverna.t2.activities.interaction.velocity;

import net.sf.taverna.t2.activities.interaction.InteractionActivityConfigurationBean;

import org.apache.velocity.runtime.parser.node.ASTDirective;
import org.apache.velocity.runtime.visitor.BaseVisitor;

/**
 * @author alanrw
 * 
 */
public class NotifyChecker extends BaseVisitor {
	@Override
	public Object visit(ASTDirective node, Object data) {
		InteractionActivityConfigurationBean config = (InteractionActivityConfigurationBean) data;
		if (node.getDirectiveName().equals("notify")) {
			config.setProgressNotification(true);
		}
		return null;
	}
}
