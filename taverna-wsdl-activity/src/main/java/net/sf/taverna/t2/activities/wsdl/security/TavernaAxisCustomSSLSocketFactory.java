package net.sf.taverna.t2.activities.wsdl.security;

import java.io.IOException;
import java.net.Socket;
import java.util.Hashtable;

import javax.net.ssl.SSLContext;

import org.apache.axis.components.net.BooleanHolder;
import org.apache.axis.components.net.JSSESocketFactory;

public class TavernaAxisCustomSSLSocketFactory extends JSSESocketFactory {


	public TavernaAxisCustomSSLSocketFactory(Hashtable attributes) {
		super(attributes);
	}

	@Override
	public Socket create(String host, int port, StringBuffer otherHeaders,
			BooleanHolder useFullURL) throws Exception {
		// Make sure we always pick up the default socket factory from SSLContext, which is based on 
		// Taverna's Keystore and Truststore and gets updated when they get updated (it may have
		// been updated in the menatime so just refresh it here just in case).
		initFactory();
		return super.create(host, port, otherHeaders, useFullURL);
	}
	
	@Override
	protected void initFactory() throws IOException {
		try{
			// Set it to the default one from the SSLContext which is set to use Taverna's Keystore and Truststore
			sslFactory = SSLContext.getDefault().getSocketFactory();
		}
		catch (Exception e) {
			throw new IOException("Could not get the Taverna's default SSLSocketFactory from SSLContext",e);
		}
	}
	
}
