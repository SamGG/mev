package org.tigr.microarray.mev.resources;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.tigr.microarray.mev.resources.ISupportFileDefinition;

/**
 * An object that describes how to get and store an annotation file for a particular species and array
 * from the Resourcerer annotation website. 
 * @author Eleanor
 *
 */
public class ResourcererAnnotationFileDefinition extends ISupportFileDefinition {
	private static String resourcererAnnotationRoot =  getBaseResourceURL("resourcerer_annotation");

	/* The species name of the array this annotation file is for. */
	private String speciesName;
	
	/* The name of the array that the annotation will be for. */
	private String arrayName;
	
	/* Currently all annotation datafiles in resourcerer are zipped */
	private boolean isZipped = true;
	
	/**
	 * Creates a new ResourcererAnnotationFileDefinition for 
	 * this species and array. 
	 * @param speciesname
	 * @param array
	 * @throws MalformedURLException 
	 */
	public ResourcererAnnotationFileDefinition(String speciesname, String array) {
		this.speciesName = speciesname;
		this.arrayName = array;
	}
	
	/**
	 * Returns the complete URI indicating where this particular file can be downloaded.
	 */
	public URL getURL() throws MalformedURLException{
		return new URL(resourcererAnnotationRoot + speciesName + "/" + arrayName + ".zip");
	}

	/**
	 * Returns a unique name for this particular file. It's created by appending the 
	 * species name and array name together with a dash and adding ".txt".
	 */
	public String getUniqueName() {
		return speciesName + "-" + arrayName + ".txt";
	}

	/**
	 * Currently always returns true. Need to write a validator for this type of file. Once that is done, this 
	 * method will check the file provided to see if it is a valid instance of this file type.
	 */
	public boolean isValid(File f) {
		return ResourcererAnnotationFileDefinition.isValidFile(f);
	}

	public boolean fileNeedsUnzipping() {
		return isZipped;
	}
	
	// TODO write a validator for these files
	public static boolean isValidFile(File f) {
//		try {
			if(f.exists() && f.canRead())
				return true;
			return false;
//		} catch (IOException ioe) {
//			return false;
//		}
	}
	public boolean isVersioned() {
		return true;
	}
}
