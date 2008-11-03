/*******************************************************************************
 * Copyright (c) 1999-2005, The Institute for Genomic Research (TIGR).
 * Copyright (c) 1999, 2008, the Dana-Farber Cancer Institute (DFCI), 
 * the J. Craig Science Foundataion (JCVI), and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
package org.tigr.microarray.mev.resources;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;

public abstract class  IMultiSupportFileDefinition {

	public abstract Collection<ISupportFileDefinition> getFileDefinitions(String[] filenames) ;
	public abstract URL getURL() throws MalformedURLException;
	
	
}
