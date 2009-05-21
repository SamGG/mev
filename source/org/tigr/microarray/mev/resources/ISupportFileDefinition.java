/*******************************************************************************
 * Copyright (c) 1999-2005, The Institute for Genomic Research (TIGR).
 * Copyright (c) 1999, 2008, the Dana-Farber Cancer Institute (DFCI), 
 * the J. Craig Science Foundataion (JCVI), and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
package org.tigr.microarray.mev.resources;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Properties;

import org.tigr.microarray.mev.TMEV;


public abstract class ISupportFileDefinition  implements FilenameFilter{
	private static Hashtable<String, String> baseResourceUrls = initializeBaseResourceUrls();
	private static Hashtable<String, String> initializeBaseResourceUrls() {
		baseResourceUrls = new Hashtable<String, String>();
		Properties mevUrls = new Properties();
		try {
			InputStream in = TMEV.class.getClassLoader().getResourceAsStream("org/tigr/microarray/mev/resources/support_file_url.properties");
			if (in != null) {
				mevUrls.load(in); // Can throw IOException
			}
		} catch (IOException ioe) {
			System.out.println("Could not load default properties from org/tigr/microarray/mev/resources/support_file_url.properties");
		}
		Enumeration keys = mevUrls.keys();
		Hashtable<String, String> temp = new Hashtable<String, String>();
		while(keys.hasMoreElements()) {
			String key = (String)keys.nextElement();
			temp.put(key, (String)mevUrls.get(key));
		}
		return temp;
	}

	public static void addBaseUrls(Hashtable<String, String> hash) {
		baseResourceUrls.putAll(hash);
	}
	public static String getBaseResourceURL(String key) {
		return baseResourceUrls.get(key);
	}
	
	/**
	 * Implement this method to provide a validation function for this file. Note that the file passed in should be the 
	 * uncompressed version that is stored locally in the Repository, not the zipped or otherwise-compressed version
	 * that is downloaded from the web.
	 * 
	 * @param f the file to be validated as a file of type @this
	 * @return true if file f is a valid instance of this file definition
	 */
	public abstract boolean isValid(File f);
	
	/**
	 * Returns the complete URL where this file should be found. It should include the protocol, 
	 * host and path, plus filename. Directory downloads are not currently supported.. 
	 * @return
	 * @throws MalformedURLException
	 */
	public abstract URL getURL() throws MalformedURLException;
	
	/**
	 * Returns the string that should be used to name this file in the repository. This name should be unique to this 
	 * file definition - that is, the name should be able to distinguish this file from any other of its type. If this ISupportFileDefinition
	 * describes a multifile (if isSingleFile() returns false), this name will be used to name the directory in the repository where the 
	 * contents of this file will be stored. If isSingleFile() returns true, this name will be used as a filename and should include an 
	 * appropriate extension.
	 * @return
	 */
	public abstract String getUniqueName();
	
	/**
	 * Returns true if this file, when downloaded, should be unzipped before being stored in the repository and returned to the calling class. 
	 * Use isSingleFile() to indicate whether the zipped contents of this file are many files or only one file.
	 * @return
	 */
	public boolean fileNeedsUnzipping(){
		return false;
	}
	
	/**
	 * Return true if this file definition represents a single file, false if this
	 * definition represents a zipped (or otherwise compressed or tarred) archive
	 * of multiple files. If the latter, ResourceManager will uncompress these files and 
	 * put them into a directory of the name returned by getUniqueName()
	 * @return 
	 */
	public boolean isSingleFile() {
		return true;
	}
	
	/**
	 * Return true if this file definition represents a file that can should be version-managed.
	 * That is, if the file as it is stored on the web is updated periodically with the same name. The 
	 * ResourceManager will attempt to track versions of the file based on last-modified datestamps
	 * on the server side.
	 * @return whether the file should be version-checked.
	 */
	public boolean isVersioned() {
		return false;
	}
	
	/**
	 * Implementation of FilenameFilter returns true if the submitted file name could represent a versioned copy of 
	 * a file defined by this instance of ISupportFileDefinition. Default implementation 
	 */
	public boolean accept(File dir, String name) {
		String uniqueName = getUniqueName();
		int lastIndex = uniqueName.lastIndexOf('.');
		if(lastIndex <= 0)
			lastIndex = uniqueName.length();
		String prefix = uniqueName.substring(0, lastIndex);

		try {
			Date temp;
			DateFormat df = new SimpleDateFormat("_yyyy-MM-dd", new Locale("en"));
			
			if(name.contains(".".subSequence(0, 1))) {
				temp = df.parse(name.substring(name.lastIndexOf(prefix)+prefix.length()+1, name.lastIndexOf('.')));
			} else {
				temp = df.parse(name.substring(name.lastIndexOf(prefix)+prefix.length()+1, name.length()));
			}
			
			return true;
		} catch (ParseException pe) {
			return false;
		}
	}

	/**
	 * Checks that the requested file is allowable to be downloaded. No executables
	 * can be downloaded. 
	 * @return
	 */
	public final boolean isAllowed() {
		String filename = "";
		try {
			filename = getURL().toString();
		} catch (MalformedURLException mue) {
			return false;
		}
		if(		filename.endsWith(".msi") ||
				filename.endsWith(".exe") ||
				filename.endsWith(".bat") ||
				filename.endsWith(".sh") ||
				filename.endsWith(".exe")
		) {
			return false;
		}
		return true;
	}
	public boolean matches(ISupportFileDefinition otherDef) {
		if(otherDef.getClass().equals(getClass())) {
			if(otherDef.getUniqueName().equals(getUniqueName()))
				return true;
		}
		return false;
	}
}
