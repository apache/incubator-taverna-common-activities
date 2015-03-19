/*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.apache.taverna.activities.interaction.velocity;

import java.util.Map;

import org.apache.velocity.runtime.parser.node.ASTDirective;
import org.apache.velocity.runtime.visitor.BaseVisitor;

/**
 * @author alanrw
 * 
 */
public class RequireChecker extends BaseVisitor {

	@Override
	public Object visit(final ASTDirective node, final Object data) {
		@SuppressWarnings("unchecked")
		final Map<String, Integer> map = (Map<String, Integer>) data;
		if (node.getDirectiveName().equals("require")) {
			final String key = String.valueOf(node.jjtGetChild(0).value(
					this.context));
			if (node.jjtGetNumChildren() > 1) {
				final Integer depth = (Integer) node.jjtGetChild(1).value(
						this.context);
				map.put(key, depth);
			} else {
				map.put(key, 0);
			}
		}
		return map;
	}

}
