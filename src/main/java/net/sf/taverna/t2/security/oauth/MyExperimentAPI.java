/**
 * 
 */
package net.sf.taverna.t2.security.oauth;

import org.scribe.builder.api.DefaultApi10a;
import org.scribe.model.Token;

/**
 * @author alanrw
 *
 */
public class MyExperimentAPI extends DefaultApi10a{
	
	  private static final String AUTHORIZE_URL = "http://www.myexperiment.org/oauth/authorize?oauth_token=%s";

	@Override
	public String getAccessTokenEndpoint() {
		return "http://www.myexperiment.org/oauth/access_token";
	}

	@Override
	public String getAuthorizationUrl(Token requestToken) {
		return String.format(AUTHORIZE_URL, requestToken.getToken());
	}

	@Override
	public String getRequestTokenEndpoint() {
		return "http://www.myexperiment.org/oauth/request_token";
	}

}
