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
public class ProduceChecker extends BaseVisitor {

	@Override
	public Object visit(final ASTDirective node, final Object data) {
		final Map<String, Integer> map = (Map<String, Integer>) data;
		if (node.getDirectiveName().equals("produce")) {
			final String key = String.valueOf(node.jjtGetChild(0).value(this.context));
			if (node.jjtGetNumChildren() > 1) {
				final Integer depth = (Integer) node.jjtGetChild(1).value(this.context);
				map.put(key, depth);
			} else {
				map.put(key, 0);
			}
		}
		return map;
	}


}
