package org.tigr.microarray.mev.resources;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

public class SupportFileUrlsPropertiesDefinition extends ISupportFileDefinition {

	@Override
	public URL getURL() throws MalformedURLException {
		return new URL("http://www.tm4.org/mev/support_file_url.properties");
	}

	@Override
	public String getUniqueName() {
		return "mev_url.properties";
	}

	@Override
	public boolean isValid(File f) {
		return true;
	}
	public boolean isVersioned() {
		return true;
	}
}
