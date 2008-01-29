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
/* GetAccessions.java
 * Copyright (C) 2005 Amira Djebbari
 */
package org.tigr.microarray.mev.cluster.gui.impl.bn.getInteractions;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.ArrayList;
import org.tigr.microarray.mev.cluster.gui.impl.bn.Useful;
import org.tigr.microarray.mev.cluster.gui.impl.bn.NullArgumentException;
import org.tigr.microarray.mev.cluster.gui.impl.bn.Constants;
/**
 * The class <code>GetAccessions</code> takes in official gene symbols and Resourcerer file name 
 * and returns the GenBank accessions associated with the official gene symbols
 *
 * @author <a href="mailto:amira@jimmy.harvard.edu"></a>
 */
public class GetAccessions {
    /**
     * The <code>getAccessions</code> method gets the GenBank accessions given:
     *
     * @param resourcererFileName a <code>String</code> corresponding to the name of the Resourcerer file
     * @param geneSymbols an <code>ArrayList</code> corresponding to the list of official gene symbols
     * @return a <code>HashMap</code> with official gene symbols as keys and GenBank accessions as values
     * @exception NullArgumentException if an error occurs because the given <code>ArrayList</code> geneSymbols was null
     */
    public static HashMap getAccessions(String resourcererFileName, ArrayList geneSymbols) throws NullArgumentException {
    	 FileReader fr=null;
	try {
	    //Useful.checkFile(resourcererFileName);
	    if(geneSymbols == null){
	    	throw new NullArgumentException("Given geneSymbols is null!");
	    }
	    HashMap hm = new HashMap();
	    // Read tab-delimited Resourcerer file line by line
	    
	    /*if(GetInteractionParemeterPPIDialog.path!=null)
	    	fr = new FileReader(GetInteractionParemeterPPIDialog.path+"\\"+resourcererFileName);
	    if(GetInteractionParemeterBothDialog.path!=null)
	    	fr = new FileReader(GetInteractionParemeterBothDialog.path+"\\"+resourcererFileName);    
	    */
	    //System.out.println("GetAccessions.getAccessions() File Name: " + resourcererFileName);
	    fr = new FileReader(resourcererFileName);
	    LineNumberReader lnr = new LineNumberReader(fr);
	    String s = null;
	    String[] tokens = null;
	    String[] subTokens = null;
	    int dummy = 2;
	    while((s = lnr.readLine())!= null){
	    	if(dummy <= 2)
	    		continue; //Skip irst 2 lines
			s = s.trim();
			tokens = s.split("\t");
			if(tokens.length >= Constants.SYMBOLS_COLUMN_NUMBER){
			    // Resourcerer file format for the official gene symbols is:
			    // official gene symbol ; gene common name
			    subTokens = tokens[Constants.SYMBOLS_COLUMN_NUMBER].split(";");
			    if(subTokens.length >= 2){
			    	if(Useful.find(geneSymbols,subTokens[0].trim())){
			    		hm.put(subTokens[0], tokens[Constants.GB_COLUMN_NUMBER]);
			    	}
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
     * The <code>writeAccessionsGivenResourcererAndGeneSymbolsList</code> method writes official gene symbols and their
     * associated GenBank accessions given:
     *
     * @param geneSymbolsFileName a <code>String</code> denoting the name of the file containing official gene symbols
     * @param resourcererFileName a <code>String</code> denoting the name of the Resourcerer file
     * @param outputFileName a <code>String</code> denoting the name of the gene symbols/GenBank accessions 
     *                       in the format gene symbol \t GenBank accession
     * @exception FileNotFoundException if an error occurs because at least one of the files denoted by
     * geneSymbolsFileName or resourcererFileName was not found
     */
    public static void writeAccessionsGivenResourcererAndGeneSymbolsList(String geneSymbolsFileName, String resourcererFileName, String outputFileName) throws FileNotFoundException {
	try {
		//System.out.println("writeAccessionsGivenResourcererAndGeneSymbolsList()" + geneSymbolsFileName);
		//System.out.println("writeAccessionsGivenResourcererAndGeneSymbolsList()" + resourcererFileName);
		//System.out.println("writeAccessionsGivenResourcererAndGeneSymbolsList()" + outputFileName);
	    Useful.checkFile(geneSymbolsFileName);
	    Useful.checkFile(resourcererFileName);
	    ArrayList geneSymbols = Useful.readNamesFromFile(geneSymbolsFileName);
	    HashMap hm = getAccessions(resourcererFileName, geneSymbols);
	    Useful.writeHashMapToFile(hm, outputFileName);
	}
	catch(NullArgumentException nae){
	    System.out.println(nae);
	}
    }

    /**
     * This <code>usage</code> method displays the usage of this class
     *
     */
    public static void usage(){
	System.out.println("Usage: java GetAccessions geneSymbolsFileName resourcererFileName outputFileName\nExample: java GetAccessions myList_gene_symbols.txt TIGR_40K_Human_Set myList_gene_symbolsGbs.txt");
	System.exit(0);
    }


    /**
     * The <code>test</code> method tests the writeAccessionsGivenResourcererAndGeneSymbolsList method 
     * given the name of the GenBank accessions file, the name of the Resourcerer data file 
     * and the name of the output file where GenBank accessions for the given gene symbols will be written.
     *
     * @param geneSymbolsFileName a <code>String</code> corresponding to the name of the file containing GenBank accessions
     * @param resourcererFileName a <code>String</code> corresponding to the name of the Resourcerer data file
     * @param outputFileName a <code>String</code> corresponding to the name of the output file
     * where GenBank accessions for the given gene symbols will be written
     */
    public static void test(String geneSymbolsFileName, String resourcererFileName, String outputFileName){
	try {
	    writeAccessionsGivenResourcererAndGeneSymbolsList(geneSymbolsFileName, resourcererFileName, outputFileName);	
	}
	catch(FileNotFoundException fnfe){
	    System.out.println(fnfe);
	}
    }

    public static void main(String[] argv){
	if(argv.length != 3){
	    usage();
	}
	String geneSymbolsFileName = argv[0];
	String resourcererFileName = argv[1];
	String outputFileName = argv[2];
	test(geneSymbolsFileName, resourcererFileName, outputFileName);       
    }
}







