package org.tigr.microarray.mev.cluster.gui.impl.ease;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.tigr.microarray.mev.resources.ISupportFileDefinition;

public class EASESupportDataFile extends ISupportFileDefinition {
	private static String easeFileRoot;
	private String species;
	private String array;
	
	public EASESupportDataFile(String species, String array) {
		this.species = species;
		this.array = array;
		easeFileRoot =  getBaseResourceURL("ease_support_file_location");
	} 
	@Override
	public URL getURL() throws MalformedURLException {
		return new URL(easeFileRoot + species + "/" + array + "_EASE.zip");
	}

	@Override
	public String getUniqueName() {
		return species + "_" + array;
	}
	
	public boolean isSingleFile() {
		return false;
	}
	
	public boolean fileNeedsUnzipping() {
		return true;
	}
	@Override
	public boolean isValid(File f) {
		return EASESupportDataFile.isValidFile(f);
	}
	public static boolean isValidFile(File f) {
		// TODO Auto-generated method stub
		return true;
	}
	public boolean isVersioned() {
		return true;
	}
}
