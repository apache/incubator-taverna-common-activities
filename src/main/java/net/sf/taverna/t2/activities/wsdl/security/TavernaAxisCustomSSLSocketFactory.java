package net.sf.taverna.t2.activities.wsdl.security;

import java.net.Socket;
import java.util.Hashtable;

import org.apache.axis.components.net.BooleanHolder;
import org.apache.axis.components.net.JSSESocketFactory;

public class TavernaAxisCustomSSLSocketFactory extends JSSESocketFactory {


	public TavernaAxisCustomSSLSocketFactory(Hashtable attributes) {
		super(attributes);
	}

	@Override
	public Socket create(String host, int port, StringBuffer otherHeaders,
			BooleanHolder useFullURL) throws Exception {
		initFactory();
		return super.create(host, port, otherHeaders, useFullURL);
	}
	
}
