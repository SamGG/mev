/*******************************************************************************
 * Copyright (c) 1999-2005, The Institute for Genomic Research (TIGR).
 * Copyright (c) 1999, 2008, the Dana-Farber Cancer Institute (DFCI), 
 * the J. Craig Science Foundataion (JCVI), and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
package org.tigr.microarray.mev.resources;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Hashtable;
import java.util.NoSuchElementException;
import java.util.Vector;

import org.tigr.util.StringSplitter;

public class AvailableAnnotationsFileDefinition extends ISupportFileDefinition {
	private String urlString = getBaseResourceURL("resourcerer_supported_annotations_list");
	public static boolean isValidFile(File f) {
		//TODO read file, check that it has the right format.
		/*try {
			FileReader fr = new FileReader(f);
			return true;			
		} catch (IOException ioe) {
			return false;
		}*/
		return true;
	}
	
	@Override
	public URL getURL() throws MalformedURLException {
		return new URL(urlString);
	}

	@Override
	public String getUniqueName() {
		return "kingdom_species_cloneset_list.txt";
	}

	@Override
	public boolean isValid(File f) {
		// TODO Auto-generated method stub
		return AvailableAnnotationsFileDefinition.isValidFile(f);
	}
	public boolean isVersioned() {
		return true;
	}
	/**
	 * Reads the file taxonfile and parses it into a Hashtable that maps each organism name to the many 
	 * chip types that can correspond to that organism. 
	 * @param taxonfile
	 * @return
	 * @throws IOException
	 */
	public Hashtable<String, Vector<String>> parseAnnotationListFile(File taxonfile) throws IOException {
		BufferedReader breader = new BufferedReader(new FileReader(taxonfile));
		Hashtable<String, Vector<String>> orgToChipMap = new Hashtable<String, Vector<String>>();
		String currentLine;
		StringSplitter ss = new StringSplitter('\t');

		try {
			while ((currentLine = breader.readLine()) != null) {
				ss.init(currentLine);
				while (ss.hasMoreTokens()) {
					ss.nextToken();
					String orgName = (String) ss.nextToken();
					String chipType = (String) ss.nextToken();
					if (!orgToChipMap.containsKey(orgName)) {
						Vector<String> chipTypes = new Vector<String>();
						chipTypes.add(chipType);
						orgToChipMap.put(orgName, chipTypes);
					} else {
						orgToChipMap.get(orgName).add(chipType);
					}
				}
			}
			return orgToChipMap;
		} catch (NoSuchElementException nsee) {
			throw new IOException(nsee);
		}
	}
}
