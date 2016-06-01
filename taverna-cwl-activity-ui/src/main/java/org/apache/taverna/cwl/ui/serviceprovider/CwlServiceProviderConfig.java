package org.apache.taverna.cwl.ui.serviceprovider;

import java.net.URI;

public class CwlServiceProviderConfig {

	public String path;
	public URI uri;
	
	public URI getUri() {
		return uri;
	}

	public void setUri(URI uri) {
		this.uri = uri;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}
}
