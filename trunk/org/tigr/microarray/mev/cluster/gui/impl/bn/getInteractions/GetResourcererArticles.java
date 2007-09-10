/* This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
/* GetResourcererArticles.java
 * Copyright (C) 2005 Amira Djebbari
 */
package org.tigr.microarray.mev.cluster.gui.impl.bn.getInteractions;
import java.util.HashMap;
import java.util.ArrayList;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.io.IOException;
import java.io.FileNotFoundException;
import org.tigr.microarray.mev.cluster.gui.impl.bn.Useful;
import org.tigr.microarray.mev.cluster.gui.impl.bn.NullArgumentException;
import org.tigr.microarray.mev.cluster.gui.impl.bn.Constants;
/**
 * The class <code>GetResourcererArticles</code> This class takes in GenBank accessions and Resourcerer file name 
 * and returns the GenBank accessions associated with the articles in Resourcerer as PMIDs (Pubmed IDs)
 *
 * @author <a href="mailto:amira@jimmy.harvard.edu"></a> 
 */
public class GetResourcererArticles {
    /**
     * The <code>getResourcererArticles</code> method gets the Resourcerer articles given:
     *
     * @param resourcererFileName a <code>String</code> corresponding to the name of the Resourcerer file
     * @param accessions an <code>ArrayList</code> corresponding to the list of GenBank accessions
     * @return a <code>HashMap</code> with GenBank accessions as keys and their associated Resourcerer articles as values
     * @exception NullArgumentException if an error occurs because the given <code>ArrayList</code> accessions was null
     */
    public static HashMap getResourcererArticles(String resourcererFileName, ArrayList accessions)  throws NullArgumentException {
	try {
	    //Useful.checkFile(resourcererFileName);
	    if(accessions == null){
		throw new NullArgumentException("Given accessions is null!");
	    }
	    HashMap hm = new HashMap();
	    // Read tab-delimited Resourcerer file line by line
	    FileReader fr = new FileReader(resourcererFileName);
	    LineNumberReader lnr = new LineNumberReader(fr);
	    String s = null;
	    String[] tokens = null;
	    String[] subTokens = null;
	    String articles = null;
	    while((s = lnr.readLine())!=null){
		s = s.trim();
		tokens = s.split("\t");
		if(tokens.length >= Constants.RESOURCERER_ARTICLES_COLUMN_NUMBER){
		    if(Useful.find(accessions,tokens[Constants.GB_COLUMN_NUMBER].trim())){
			articles = removeSpacesFromString(tokens[Constants.RESOURCERER_ARTICLES_COLUMN_NUMBER]);
			hm.put(tokens[Constants.GB_COLUMN_NUMBER], articles);
		    }
		}
	    }
	    lnr.close();
	    fr.close();
	    return hm;
	}
	catch(IOException ioe){
	    System.out.println(ioe);
	}
	return null;
    }

    /**
     * The <code>removeSpacesFromString</code> method removes spaces from a given String
     *
     * @param inString a <code>String</code> corresponding to the given String including spaces
     * @return a <code>String</code> corresponding to the new String with all characters
     * in the given String except the spaces
     * @exception NullArgumentException if an error occurs because the given <code>String</code> inString was null
     */
    public static String removeSpacesFromString(String inString) throws NullArgumentException{
	if(inString == null){
	    throw new NullArgumentException("Given inString is null!");
	}
	char[] inCharArray = inString.toCharArray();
	char[] outCharArray = new char[inCharArray.length];
	int count = 0;
	for(int i = 0; i < inCharArray.length; i++){
	    if(inCharArray[i] != ' '){
		outCharArray[count] = inCharArray[i];
		count++;
	    }
	}
	String outString = new String(outCharArray);
	outString = outString.trim();
	return outString;
    }

    /**
     * The <code>getGBsArticlesGivenResourcererAndGBsFileName</code> method writes GenBank accessions and their associated 
     * articles given: 
     *
     * @param gbAccessionsFileName a <code>String</code> denoting the name of the file containing GenBank accessions
     * @param resourcererFileName a <code>String</code> denoting the name of the Resourcerer file
     * @return a <code>HashMap</code> with GenBank accessions as keys and their associated Resourcerer articles as values
     * @exception FileNotFoundException if an error occurs because at least one of the files
     * denoted by gbAccessionsFileName or resourcererFileName was not found
     */
    public static HashMap getGBsArticlesGivenResourcererAndGBsFileName(String resourcererFileName, String gbAccessionsFileName)  throws FileNotFoundException{
	try {
	    //Useful.checkFile(resourcererFileName);
	    //Useful.checkFile(gbAccessionsFileName);
	    ArrayList gbs = Useful.readNamesFromFile(gbAccessionsFileName);
	    HashMap hm = getResourcererArticles(resourcererFileName, gbs);
	    return hm;
	}
	catch(NullArgumentException nae){
	    System.out.println(nae);
	}
	return null;	
    }
    
    /**
     * The <code>writeGBsArticlesGivenResourcererAndGBsList</code> method writes GenBank accessions and their associated 
     * articles given: 
     *
     * @param gbAccessionsFileName a <code>String</code> denoting the name of the file containing GenBank accessions
     * @param resourcererFileName a <code>String</code> denoting the name of the Resourcerer file
     * @param outputFileName a <code>String</code> denoting the name of the output file
     *                       in the format GenBank accession \t pmid1,pmid2,... 
     *                       where pmid are PubMed IDs of the articles
     * @exception FileNotFoundException if an error occurs because at least one of the files
     * denoted by gbAccessionsFileName or resourcererFileName was not found
     */
    public static void writeGBsArticlesGivenResourcererAndGBsList(String gbAccessionsFileName, String resourcererFileName, String outputFileName) throws FileNotFoundException{
	try {
		System.out.println("writeGBsArticlesGivenResourcererAndGBsList()" + gbAccessionsFileName);
		System.out.println("writeGBsArticlesGivenResourcererAndGBsList()" + resourcererFileName);
		System.out.println("writeGBsArticlesGivenResourcererAndGBsList()" + outputFileName);
	    Useful.checkFile(resourcererFileName);
	    Useful.checkFile(gbAccessionsFileName);
	    ArrayList gbs = Useful.readNamesFromFile(gbAccessionsFileName);
	    HashMap hm = getResourcererArticles(resourcererFileName, gbs);
	    Useful.writeHashMapToFile(hm, outputFileName);
	}
	catch(NullArgumentException nae){
	    System.out.println(nae);
	}
    }
								  
    /**
     * This <code>usage</code> method diplays the usage for this class
     *
     */
    public static void usage(){
	System.out.println("Usage: java GetResourcererArticles gbAccessionsFileName resourcererFileName outputFileName\nExample: java GetResourcererArticles myList_gbs.txt TIGR_40K_Human_Set myList_gbsArticlesFromResourcerer.txt");
	System.exit(0);
    }

    /**
     * The <code>test</code> method tests the <code>writeGBsArticlesGivenResourcererAndGBsList</code> method
     * given the name of the GenBank accessions file, the name of the Resourcerer data file and the name of
     * the output file where articles for the given GenBank accession will be written
     *     
     * @param gbAccessionsFileName a <code>String</code> corresponding to the name of the file containing GenBank accessions
     * @param resourcererFileName a <code>String</code> corresponding to the name of the Resourcerer data file
     * @param outputFileName a <code>String</code> corresponding to the name of the output file
     * where articles for the given GenBank accessions will be written
     */
    public static void test(String gbAccessionsFileName, String resourcererFileName, String outputFileName){
	try {
	    writeGBsArticlesGivenResourcererAndGBsList(gbAccessionsFileName, resourcererFileName, outputFileName);
	}
	catch(FileNotFoundException fnfe){
	    System.out.println(fnfe);
	}
    }
    public static void main(String[] argv){
	if(argv.length != 3){
	    usage();
	}
	String gbAccessionsFileName = argv[0];
	String resourcererFileName = argv[1];
	String outputFileName = argv[2];
	test(gbAccessionsFileName, resourcererFileName, outputFileName);

	
    }
}
