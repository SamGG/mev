package org.tigr.microarray.mev.resources;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Vector;

public class GseaMultiSuppFileDefinition extends IMultiSupportFileDefinition {
	private static String rootURL = ISupportFileDefinition.getBaseResourceURL("gsea_support_file_location");

	@Override
	public Collection<ISupportFileDefinition> getFileDefinitions(String[] filenames) {
		Vector<ISupportFileDefinition> v = new Vector<ISupportFileDefinition>();
		for(int i=0; i<filenames.length; i++) {
			v.add(new GseaSupportDataFile(filenames[i]));
		}
		return v;
	}
	public URL getURL() throws MalformedURLException {
		return new URL(rootURL);
	}

}
