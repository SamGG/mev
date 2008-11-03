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

public class GseaSupportDataFile extends ISupportFileDefinition {
	private static String rootURL = getBaseResourceURL("gsea_support_file_location");
	private String symbolsName;
	
	public GseaSupportDataFile(String symbolsName) {
		this.symbolsName = symbolsName;
	}
	@Override
	public URL getURL() throws MalformedURLException {
		return new URL(rootURL + symbolsName);
	}

	@Override
	public String getUniqueName() {
		return symbolsName;
	}

	@Override
	public boolean isValid(File f) {
		// TODO write this
		return true;
	}
	public boolean isVersioned() {
		return true;
	}
}
