/**
 *
 */
package net.sf.taverna.t2.activities.interaction.velocity;

import org.apache.velocity.runtime.parser.node.ASTDirective;
import org.apache.velocity.runtime.visitor.BaseVisitor;

import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * @author alanrw
 * 
 */
public class NotifyChecker extends BaseVisitor {

	@Override
	public Object visit(final ASTDirective node, final Object data) {
		ObjectNode json = (ObjectNode) data;
		if (node.getDirectiveName().equals("notify")) {
			json.put("progressNotification", true);
		}
		return null;
	}

}
