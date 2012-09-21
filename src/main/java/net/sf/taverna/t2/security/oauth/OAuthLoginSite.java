/**
 * 
 */
package net.sf.taverna.t2.security.oauth;

import org.scribe.builder.api.Api;

/**
 * @author alanrw
 *
 */
public interface OAuthLoginSite {
	
	String getName();
	
	Class<? extends Api> getApiClass();
	
	String getAuthorizeUrl(String interactionUrl);

}
