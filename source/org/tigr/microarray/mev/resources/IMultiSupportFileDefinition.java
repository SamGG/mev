package org.tigr.microarray.mev.resources;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;

public abstract class  IMultiSupportFileDefinition {

	public abstract Collection<ISupportFileDefinition> getFileDefinitions(String[] filenames) ;
	public abstract URL getURL() throws MalformedURLException;
	
	
}
