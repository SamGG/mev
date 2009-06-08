/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
package org.tigr.microarray.mev.annotation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Field;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.tigr.util.StringSplitter;


public class PublicURL {

	private static boolean urlLoaded = false;
	private static Hashtable<String, String> urlHash;
	
	/*************************************************************************
	 * AnnotationURLConstants class is used to keep track of the URLs that are 
	 * currently avaialble in the Model. Class is kep separate for a simple 
	 * implementation of Java Reflection to query URL names at run time.
	 *************************************************************************/
	public static AnnotationConstants urlConsts = new AnnotationConstants();
	
	/******************************************************
	 * Static Functions to Retreive URLs by URL constant
	 * Names as defined in the AnnotationURLConstants class
	 ******************************************************/
	
	/**
	 * Generic Function to get the URL string for any URL Key.
	 * The params string array could be 1 or many Strings are required
	 * by the URL template defined in the org/tigr/microarray/mev/config/annotation_URL.txt
	 * file. The number of paramets should equal the number of "FIELD#"
	 * occurs int the template. URLKeys are the constants as defined in the
	 * AnnotationURLConstants class.
	 */
	public static String getURL(final String URLKey, String[] params) throws InsufficientArgumentsException, URLNotFoundException, IllegalArgumentException {
		if(!isUrlLoaded())
			throw new URLNotFoundException("No URLs were loaded");
		String urlTemplate = urlHash.get(URLKey.trim());
		if(urlTemplate == null) throw new URLNotFoundException(); 
		
		//Special handling for go terms. Multiple terms can be queried together

		//TGI accessions are a special case in that they require a species name
		//added to their accession. For all other accessions, multiple accession values are 
		//added together into one string to put into the query.
		if(URLKey.equals(AnnotationConstants.GO_TERMS)) {
			String temp = params[0];
			for(int i=1; i<params.length; i++) {
				temp += " " + params[i];
			}
			params = new String[]{temp};
		} else if(!URLKey.equals(AnnotationConstants.TGI_GC) &&
				!URLKey.equals(AnnotationConstants.TGI_ORTH) &&
						!URLKey.equals(AnnotationConstants.TGI_TC)
				) {
			String temp = params[0];
			for(int i=1; i<params.length; i++) {
				temp += "," + params[i];
			}
			params = new String[]{temp};
		}

		Vector<Integer> paramSites = getParamIndices("FIELD", urlTemplate);
		if(paramSites.size() != params.length) {
			String _temp = "For field " + URLKey + ": " + paramSites.size() + " params required. " + params.length + " params returned.";
			throw new InsufficientArgumentsException(_temp);
		}
		
		// Returns following as individual elements of the array:
		// 1 - The actual template with place holders for parameters 
		// 2 - The data types of the parameters. The order in which they appear has to 
		// match the order in which the params argument is supplied to the funciton.
		// 3 - A description of each parameter
		String[] _TemplateChop = urlTemplate.split("\\|");

		
		Integer[] indx = new Integer[paramSites.size()];
		paramSites.toArray(indx);
		String formedURL = fillParameters(_TemplateChop[0], params, indx);
		return formedURL;
	}
	
	protected static String fillParameters(String template, String[] params, Integer[] indices) {
		//www.ncbi.nlm.nih.gov/mapview/maps.cgi?TAXID=FIELD&CHR=FIELD&BEG=FIELD&END=FIELD&thmb=on
		String formedURL = "";
		int startIndex = 0;
		for(int i = 0; i < indices.length; i++) {
			String _tmp = template.substring(startIndex, indices[i]);
			formedURL = formedURL + _tmp + params[i];
			//System.out.println("Cur URL: " + formedURL + " Cur Param: " + params[i]);
			startIndex = indices[i] + "FIELD".length();
		}
		return formedURL;
	}

	private static boolean checkParamTypeAndOrder(String[] providedParams, String[] requiredParams) {
		for(int i = 0; i < providedParams.length; i++) {
			String _temp = requiredParams[i];
			String _tmp = providedParams[i];
			try {
				if(_temp.equals("int")){
					//System.out.println("In int: " + _tmp);
					Integer.parseInt(_tmp);
				} else if (_temp.equals("float")) {
					Float.parseFloat(_tmp);
				} else if (_temp.equals("double")) {
					Double.parseDouble(_tmp);
				} else if (_temp.equals("String")) {
					//if(!_tmp.getClass().getName().equals("String"))
						//throw new Exception();
				}
			} catch(Exception e) {
				System.out.println("Possible Class Cast or Conversion Exception");
				e.printStackTrace();
				return false;
			}
		}
		return true;
	}

	protected static Vector<Integer> getParamIndices(String str, String withinStr) {
		//System.out.println("urlTemplate: " + withinStr);
		int fromIndex = 0;
		int hitIndex = 0;
		Vector<Integer> indices = new Vector<Integer>();
		while(hitIndex != -1) {
			hitIndex = withinStr.indexOf(str, fromIndex);
			if(hitIndex != -1) {
				//System.out.println("Hit !!");
				indices.add(new Integer(hitIndex));
				fromIndex = hitIndex + 1;
			}
		}
		return indices;
	}
	
	public static String getURL_NCBI_Gene(String geneName) throws InsufficientArgumentsException, URLNotFoundException, IllegalArgumentException  {
		String urlTemplate = urlHash.get(AnnotationURLConstants.NCBI_GENE);
		if(urlTemplate == null) throw new URLNotFoundException();
		
		Vector<Integer> paramSites = getParamIndices("FIELD", urlTemplate);
		
		String[] _TemplateChop = urlTemplate.split("\\|");
		
		Integer[] indx = new Integer[paramSites.size()];
		paramSites.toArray(indx);
		String formedURL = fillParameters(_TemplateChop[0], new String[] {geneName}, indx);
		return formedURL;
	}
	
	public static String getURL_NCBI_Protein(String proteinName) throws InsufficientArgumentsException, URLNotFoundException, IllegalArgumentException  {
		String urlTemplate = urlHash.get(AnnotationURLConstants.NCBI_PROTEIN);
		if(urlTemplate == null) throw new URLNotFoundException();
		
		Vector<Integer> paramSites = getParamIndices("FIELD", urlTemplate);
		
		String[] _TemplateChop = urlTemplate.split("\\|");
		
		Integer[] indx = new Integer[paramSites.size()];
		paramSites.toArray(indx);
		String formedURL = fillParameters(_TemplateChop[0], new String[] {proteinName}, indx);
		return formedURL;
	}
	
	public static String getURL_NCBI_UniGene(String UniGeneID) throws InsufficientArgumentsException, URLNotFoundException, IllegalArgumentException  {
		String urlTemplate = urlHash.get(AnnotationURLConstants.NCBI_UNIGENE);
		if(urlTemplate == null) throw new URLNotFoundException();
		
		Vector<Integer> paramSites = getParamIndices("FIELD", urlTemplate);
		
		String[] _TemplateChop = urlTemplate.split("\\|");
		
		Integer[] indx = new Integer[paramSites.size()];
		paramSites.toArray(indx);
		String formedURL = fillParameters(_TemplateChop[0], new String[] {UniGeneID}, indx);
		return formedURL;
	}
	
	public static String getURL_NCBI_MapViewer(String chr, String species, int startBP, int endBP) throws InsufficientArgumentsException, URLNotFoundException, IllegalArgumentException  {
		String urlTemplate = urlHash.get(AnnotationURLConstants.NCBI_MAPVIEWER);
		if(urlTemplate == null) throw new URLNotFoundException();
		
		Vector<Integer> paramSites = getParamIndices("FIELD", urlTemplate);
		
		String[] _TemplateChop = urlTemplate.split("\\|");
		String[] _paramDataTypes = _TemplateChop[1].split(":");
		String[] _paramDesc = _TemplateChop[2].split(":");
		
		String[] params = new String[] {chr, species, String.valueOf(startBP), String.valueOf(endBP)};
		if(!checkParamTypeAndOrder(params, _paramDataTypes)) {
			String _temp = "", _tmp = "";
			for(int i = 0; i < _paramDesc.length; i++){
				_temp += _paramDesc[i] + " ";
			}
			for(int i = 0; i < params.length; i++){
				_tmp += params[i] + " ";
			}
			throw new IllegalArgumentException("Expected argument(s): " + _temp + ". Given argument(s): " + _tmp);
		}
		
		Integer[] indx = new Integer[paramSites.size()];
		paramSites.toArray(indx);
		String formedURL = fillParameters(_TemplateChop[0], params, indx);
		return formedURL;
	}
	
	public static String getURL_NCBI_GenBank(String genBankID) throws InsufficientArgumentsException, URLNotFoundException, IllegalArgumentException  {
		String urlTemplate = urlHash.get(AnnotationURLConstants.NCBI_GENBANK);
		if(urlTemplate == null) throw new URLNotFoundException();
		
		Vector<Integer> paramSites = getParamIndices("FIELD", urlTemplate);
		
		String[] _TemplateChop = urlTemplate.split("\\|");
		
		Integer[] indx = new Integer[paramSites.size()];
		paramSites.toArray(indx);
		String formedURL = fillParameters(_TemplateChop[0], new String[] {genBankID}, indx);
		return formedURL;
	}
	
	public static String getURL_NCBI_Snp(String Snp) throws InsufficientArgumentsException, URLNotFoundException, IllegalArgumentException  {
		String urlTemplate = urlHash.get(AnnotationURLConstants.NCBI_SNP);
		if(urlTemplate == null) throw new URLNotFoundException();
		
		Vector<Integer> paramSites = getParamIndices("FIELD", urlTemplate);
		
		String[] _TemplateChop = urlTemplate.split("\\|");
		
		Integer[] indx = new Integer[paramSites.size()];
		paramSites.toArray(indx);
		String formedURL = fillParameters(_TemplateChop[0], new String[] {Snp}, indx);
		return formedURL;
	}
	
	public static String getURL_NCBI_Genome(String searchTerm) throws InsufficientArgumentsException, URLNotFoundException, IllegalArgumentException  {
		String urlTemplate = urlHash.get(AnnotationURLConstants.NCBI_GENOME);
		if(urlTemplate == null) throw new URLNotFoundException();
		
		Vector<Integer> paramSites = getParamIndices("FIELD", urlTemplate);
		
		String[] _TemplateChop = urlTemplate.split("\\|");
		
		Integer[] indx = new Integer[paramSites.size()];
		paramSites.toArray(indx);
		String formedURL = fillParameters(_TemplateChop[0], new String[] {searchTerm}, indx);
		return formedURL;
	}
	
	public static String getURL_NCBI_Pubmed(String searchTerm) throws InsufficientArgumentsException, URLNotFoundException, IllegalArgumentException  {
		String urlTemplate = urlHash.get(AnnotationURLConstants.NCBI_PUBMED);
		if(urlTemplate == null) throw new URLNotFoundException();
		
		Vector<Integer> paramSites = getParamIndices("FIELD", urlTemplate);
		
		String[] _TemplateChop = urlTemplate.split("\\|");
		
		Integer[] indx = new Integer[paramSites.size()];
		paramSites.toArray(indx);
		String formedURL = fillParameters(_TemplateChop[0], new String[] {searchTerm}, indx);
		return formedURL;
	}
	
	public static String getURL_NCBI_Generic(String anyTerm) throws InsufficientArgumentsException, URLNotFoundException, IllegalArgumentException  {
		String urlTemplate = urlHash.get(AnnotationURLConstants.GENERIC);
		if(urlTemplate == null) throw new URLNotFoundException();
		
		Vector<Integer> paramSites = getParamIndices("FIELD", urlTemplate);
		
		String[] _TemplateChop = urlTemplate.split("\\|");
		
		Integer[] indx = new Integer[paramSites.size()];
		paramSites.toArray(indx);
		String formedURL = fillParameters(_TemplateChop[0], new String[] {anyTerm}, indx);
		return formedURL;
	}
	
	public static String getURL_UCSC_Browser(int taxID, String dbBld, int startBP, int endBP) throws InsufficientArgumentsException, URLNotFoundException, IllegalArgumentException  {
		String urlTemplate = urlHash.get(AnnotationURLConstants.UCSC_BROWSER);
		if(urlTemplate == null) throw new URLNotFoundException();
		
		Vector<Integer> paramSites = getParamIndices("FIELD", urlTemplate);
		
		String[] _TemplateChop = urlTemplate.split("\\|");
		String[] _paramDataTypes = _TemplateChop[1].split(":");
		String[] _paramDesc = _TemplateChop[2].split(":");
		
		String[] params = new String[] {String.valueOf(taxID), dbBld, String.valueOf(startBP), String.valueOf(endBP)};
		if(!checkParamTypeAndOrder(params, _paramDataTypes)) {
			String _temp = "", _tmp = "";
			for(int i = 0; i < _paramDesc.length; i++){
				_temp += _paramDesc[i] + " ";
			}
			for(int i = 0; i < params.length; i++){
				_tmp += params[i] + " ";
			}
			throw new IllegalArgumentException("Expected argument(s): " + _temp + ". Given argument(s): " + _tmp);
		}
		
		Integer[] indx = new Integer[paramSites.size()];
		paramSites.toArray(indx);
		String formedURL = fillParameters(_TemplateChop[0], params, indx);
		return formedURL;
	}
	
	public static String getURL_EBI_GO(String searchTerm) throws InsufficientArgumentsException, URLNotFoundException, IllegalArgumentException  {
		String urlTemplate = urlHash.get(AnnotationURLConstants.EBI_GO);
		if(urlTemplate == null) throw new URLNotFoundException();
		
		Vector<Integer> paramSites = getParamIndices("FIELD", urlTemplate);
		
		String[] _TemplateChop = urlTemplate.split("\\|");
		
		Integer[] indx = new Integer[paramSites.size()];
		paramSites.toArray(indx);
		String formedURL = fillParameters(_TemplateChop[0], new String[] {searchTerm}, indx);
		return formedURL;
	}
	
	public static String getURL_KEGG(String searchTerm) throws InsufficientArgumentsException, URLNotFoundException, IllegalArgumentException  {
		String urlTemplate = urlHash.get(AnnotationURLConstants.KEGG);
		if(urlTemplate == null) throw new URLNotFoundException();
		
		Vector<Integer> paramSites = getParamIndices("FIELD", urlTemplate);
		
		String[] _TemplateChop = urlTemplate.split("\\|");
		
		Integer[] indx = new Integer[paramSites.size()];
		paramSites.toArray(indx);
		String formedURL = fillParameters(_TemplateChop[0], new String[] {searchTerm}, indx);
		return formedURL;
	}
	public static String getURL_GenMAPP(String searchTerm) throws InsufficientArgumentsException, URLNotFoundException, IllegalArgumentException  {
		String urlTemplate = urlHash.get(AnnotationURLConstants.GENMAPP);
		if(urlTemplate == null) throw new URLNotFoundException();
		
		Vector<Integer> paramSites = getParamIndices("FIELD", urlTemplate);
		
		String[] _TemplateChop = urlTemplate.split("\\|");
		
		Integer[] indx = new Integer[paramSites.size()];
		paramSites.toArray(indx);
		String formedURL = fillParameters(_TemplateChop[0], new String[] {searchTerm}, indx);
		return formedURL;
	}
	public static String getURL_Pfam(String searchTerm) throws InsufficientArgumentsException, URLNotFoundException, IllegalArgumentException  {
		String urlTemplate = urlHash.get(AnnotationURLConstants.PFAM);
		if(urlTemplate == null) throw new URLNotFoundException();
		
		Vector<Integer> paramSites = getParamIndices("FIELD", urlTemplate);
		
		String[] _TemplateChop = urlTemplate.split("\\|");
		
		Integer[] indx = new Integer[paramSites.size()];
		paramSites.toArray(indx);
		String formedURL = fillParameters(_TemplateChop[0], new String[] {searchTerm}, indx);
		return formedURL;
	}
	public static String getURL_TGI_orth(String searchTerm) throws InsufficientArgumentsException, URLNotFoundException, IllegalArgumentException  {
		String urlTemplate = urlHash.get(AnnotationConstants.TGI_ORTH);
		if(urlTemplate == null) throw new URLNotFoundException();
		String species = searchTerm.substring(0, searchTerm.indexOf('_'));
		String TC = searchTerm.substring(searchTerm.indexOf('_')+1);
		
		Vector<Integer> paramSites = getParamIndices("FIELD", urlTemplate);
		
		String[] _TemplateChop = urlTemplate.split("\\|");
		
		Integer[] indx = new Integer[paramSites.size()];
		paramSites.toArray(indx);
		String formedURL = fillParameters(_TemplateChop[0], new String[] {species, TC}, indx);
		
		return formedURL;
	}

	
	
	public static boolean isUrlLoaded() {
		return urlLoaded;
	}
	
	public static int loadURLs(File configFile) throws FileNotFoundException {
		Hashtable<String, String> tempHash = new Hashtable<String, String>();
        try {
            FileReader fr = new FileReader(configFile);
            BufferedReader buff = new BufferedReader(fr);
            StringSplitter st = new StringSplitter((char)0x09);
            boolean eof = false;
            while (!eof) {
                String line = buff.readLine();
                if (line == null) eof = true;
                else {
                    st.init(line);
                    String _tempKey = st.nextToken();
                    String _tempURL = st.nextToken();
                    st.nextToken();
                    tempHash.put(_tempKey.trim(), _tempURL);
                }
            }
            buff.close();
        } catch (Exception e) {
        	return -1;
        }
        
        if(tempHash.size() < 1) 
        	return -1;
        urlHash = tempHash;
		urlLoaded = true;
		return 0; 
	}
	public static boolean hasUrlForKey(String key) {
		if(urlHash == null)
			return false;
		if(urlHash.get(key.trim()) != null)
			return true;
		return false;
	}
}
