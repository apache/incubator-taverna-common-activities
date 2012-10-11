/**
 * 
 */
package net.sf.taverna.t2.security.oauth;

import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.Api;
import org.scribe.builder.api.TwitterApi;
import org.scribe.model.Token;
import org.scribe.oauth.OAuthService;
import org.scribe.utils.OAuthEncoder;

/**
 * @author alanrw
 *
 */
public class TwitterLoginSite implements OAuthLoginSite {

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.security.oauth.OAuthLoginSite#getApiClass()
	 */
	@Override
	public Class<? extends Api> getApiClass() {
		return TwitterApi.class;
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.security.oauth.OAuthLoginSite#getName()
	 */
	@Override
	public String getName() {
		return "Twitter";
	}

	@Override
	public String getAuthorizeUrl(String interactionUrl) {
		OAuthService service = new ServiceBuilder()
        .provider(TwitterApi.class)
        .apiKey("6icbcAXyZx67r8uTAUM5Qw")
        .apiSecret("SCCAdUUc6LXxiazxH3N0QfpNUvlUy84mZ2XZKiv39s")
        .build();


		Token requestToken = service.getRequestToken();
		return (service.getAuthorizationUrl(requestToken) + "&oauth_callback=" + "http://www.mygrid.org.uk");		
	}

}
