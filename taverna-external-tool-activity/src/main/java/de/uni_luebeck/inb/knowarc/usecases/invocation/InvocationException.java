/**
 * 
 */
package de.uni_luebeck.inb.knowarc.usecases.invocation;

/**
 * @author alanrw
 *
 */
public class InvocationException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 437316164959631591L;

	public InvocationException(String string) {
		super(string);
	}
	
	public InvocationException(Exception e) {
		super(e);
	}

}
