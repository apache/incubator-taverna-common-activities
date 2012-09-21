/**
 * 
 */
package net.sf.taverna.t2.security.oauth;

import java.util.List;

import net.sf.taverna.t2.spi.SPIRegistry;

import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.GoogleApi;
import org.scribe.model.Token;
import org.scribe.oauth.OAuthService;
import org.scribe.utils.OAuthEncoder;

/**
 * @author alanrw
 *
 */
public class OAuthUtils {
	  
	private static OAuthUtils INSTANCE = new OAuthUtils();
	
	private OAuthUtils() {
		
	}

	public static OAuthUtils getInstance() {
		return INSTANCE;
	}
	

	public List<OAuthLoginSite> getLoginSites() {
		SPIRegistry<OAuthLoginSite> registry = new SPIRegistry<OAuthLoginSite> (OAuthLoginSite.class);
		return registry.getInstances();
	}
}
