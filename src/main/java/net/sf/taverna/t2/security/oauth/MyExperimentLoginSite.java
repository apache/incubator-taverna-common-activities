/**
 * 
 */
package net.sf.taverna.t2.security.oauth;

import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.Api;
import org.scribe.model.Token;
import org.scribe.oauth.OAuthService;
import org.scribe.utils.OAuthEncoder;

/**
 * @author alanrw
 *
 */
public class MyExperimentLoginSite implements OAuthLoginSite {

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.security.oauth.OAuthLoginSite#getApiClass()
	 */
	@Override
	public Class<? extends Api> getApiClass() {
		return MyExperimentAPI.class;
	}

	/* (non-Javadoc)
	 * @see net.sf.taverna.t2.security.oauth.OAuthLoginSite#getName()
	 */
	@Override
	public String getName() {
		return "myExperiment";
	}

	@Override
	public String getAuthorizeUrl(String interactionUrl) {
		OAuthService service = new ServiceBuilder()
        .provider(MyExperimentAPI.class)
        .apiKey("QjrA10d0vdNjI1wt71pQ3g")
        .apiSecret("QvMEhIKz1IY31PCQgTgi2vCNxjXN6xLnCZcqaEM")
        .build();

		Token requestToken = service.getRequestToken();
		return (service.getAuthorizationUrl(requestToken) + "&oauth_callback=" + OAuthEncoder.encode(interactionUrl));		
	}

}
