/**
 *
 */
package net.sf.taverna.t2.activities.interaction.velocity;

import java.util.Map;

import org.apache.velocity.runtime.parser.node.ASTDirective;
import org.apache.velocity.runtime.visitor.BaseVisitor;

/**
 * @author alanrw
 * 
 */
public class RequireChecker extends BaseVisitor {
	@Override
	public Object visit(ASTDirective node, Object data) {
		@SuppressWarnings("unchecked")
		Map<String, Integer> map = (Map<String, Integer>) data;
		if (node.getDirectiveName().equals("require")) {
			String key = String
					.valueOf(node.jjtGetChild(0).value(context));
			if (node.jjtGetNumChildren() > 1) {
				map.put(key, (Integer) node.jjtGetChild(1).value(context));
			} else {
				map.put(key, 0);
			}
		}
		return map;
	}
}
