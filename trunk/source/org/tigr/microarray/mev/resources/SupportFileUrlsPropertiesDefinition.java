/*******************************************************************************
 * Copyright (c) 1999-2005, The Institute for Genomic Research (TIGR).
 * Copyright (c) 1999, 2008, the Dana-Farber Cancer Institute (DFCI), 
 * the J. Craig Science Foundataion (JCVI), and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
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
