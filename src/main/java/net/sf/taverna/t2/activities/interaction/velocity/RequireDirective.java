/**
 * 
 */
package net.sf.taverna.t2.activities.interaction.velocity;

import java.io.IOException;
import java.io.Writer;

import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.directive.Directive;
import org.apache.velocity.runtime.parser.node.Node;

/**
 * @author alanrw
 *
 */
public class RequireDirective extends Directive {

    /* (non-Javadoc)                                                                                                                 
     * @see org.apache.velocity.runtime.directive.Directive#getName()                                                                
     */
    @Override
    public String getName() {
            return "require";
    }

    /* (non-Javadoc)                                                                                                                 
     * @see org.apache.velocity.runtime.directive.Directive#getType()                                                                
     */
    @Override
    public int getType() {
            return LINE;
    }

    /* (non-Javadoc)                                                                                                                 
     * @see org.apache.velocity.runtime.directive.Directive#render(org.apache.velocity.context.InternalContextAdapter, java.io.Write\
r, org.apache.velocity.runtime.parser.node.Node)                                                                                         
     */
    @Override
    public boolean render(InternalContextAdapter context, Writer writer,
                    Node node) throws IOException, ResourceNotFoundException,
                    ParseErrorException, MethodInvocationException {
            return true;
    }

}
