package org.tigr.microarray.mev.cluster.gui.impl.ease;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.tigr.microarray.mev.resources.ISupportFileDefinition;

public class EASEImpliesAndURLDataFile extends ISupportFileDefinition {
	private static String easeFileRoot;
	
	public EASEImpliesAndURLDataFile() {
		easeFileRoot =  getBaseResourceURL("ease_implies_file_location");
	} 
	@Override
	public URL getURL() throws MalformedURLException {
		return new URL(easeFileRoot + getUniqueName() + ".zip");
	}

	@Override
	public String getUniqueName() {
		return "Implies_and_URL_data";
	}
	
	public boolean isSingleFile() {
		return false;
	}
	
	public boolean fileNeedsUnzipping() {
		return true;
	}
	@Override
	public boolean isValid(File f) {
		return EASEImpliesAndURLDataFile.isValidFile(f);
	}
	public static boolean isValidFile(File f) {
		// TODO Auto-generated method stub
		return true;
	}
	public boolean isVersioned() {
		return true;
	}
	public String getImpliesLocation(File f) {
		if(f == null)
			return null;
		return new File(f, "Implies").getAbsolutePath();
	}	
	public String getTagsLocation(File f) {
		return new File(new File(f, "URL data"), "Tags").getAbsolutePath();
	}
	public String getURLDataLocation(File f) {
		return new File(f, "URL data").getAbsolutePath();
	}
}
