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
/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
package org.tigr.microarray.mev.cluster.gui.impl.bn.getInteractions;
import java.util.HashMap;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import org.tigr.microarray.mev.cluster.gui.impl.bn.Useful;
import org.tigr.microarray.mev.cluster.gui.impl.bn.NullArgumentException;
import org.tigr.microarray.mev.cluster.gui.impl.bn.Constants;
/**
 * The class <code>GetGOs</code> takes in GenBank accessions and Resourcerer file name
 * and returns the GO terms associated with the GenBank accessions
 *
 * @author <a href="mailto:amira@jimmy.harvard.edu"></a>
 */
public class GetGOs {
    /**
     * The <code>getGOs</code> method gets the GO (Gene Ontology) terms given:
     *
     * @param resourcererFileName a <code>String</code> corresponding to the name of the Resourcerer file
     * @param accessions an <code>ArrayList</code> corresponding to the list of GenBank accessions
     * @return a <code>HashMap</code> with GenBank accessions as keys and GO terms as values
     * @exception NullArgumentException if an error occurs because the given <code>ArrayList</code> accessions was null
     */
    public static HashMap getGOs(String resourcererFileName, ArrayList accessions) throws NullArgumentException {
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
	    while((s = lnr.readLine())!=null){
		s = s.trim();
		tokens = s.split("\t");
		if(tokens.length >= Constants.GO_COLUMN_NUMBER){
		    if(Useful.find(accessions,tokens[Constants.GB_COLUMN_NUMBER].trim())){
			hm.put(tokens[Constants.GB_COLUMN_NUMBER], tokens[Constants.GO_COLUMN_NUMBER]);
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
     * The <code>getGOsGivenResourcererAndGBsFileName</code> method gets GenBank accessions and their associated
     * GO terms given:
     *
     * @param resourcererFileName a <code>String</code> denoting the name of the Resourcerer file
     * @param gbAccessionsFileName a <code>String</code> denoting the name of the file containing GenBank accessions
     * @return a <code>HashMap</code> with GenBank accessions as keys and GO terms as values
     * @exception FileNotFoundException if an error occurs because at least one of the files
     * denoted by gbAccessionsFileName or resourcererFileName was not found
     */
    public static HashMap getGOsGivenResourcererAndGBsFileName(String resourcererFileName, String gbAccessionsFileName) throws FileNotFoundException {
	try {
		
		System.out.println("getGOsGivenResourcererAndGBsFileName()" + resourcererFileName);
		System.out.println("getGOsGivenResourcererAndGBsFileName()" + gbAccessionsFileName);
	    Useful.checkFile(resourcererFileName);
	    Useful.checkFile(gbAccessionsFileName);
	    ArrayList gbs = Useful.readNamesFromFile(gbAccessionsFileName);
	    HashMap hm = getGOs(resourcererFileName, gbs);
	    return hm;
	}
	catch(NullArgumentException nae){
	    System.out.println(nae);
	}
	return null;
    }

    /**
     * The <code>writeGOsGivenResourcererAndGBsList</code> method writes GenBank accessions and their associated
     * GO terms given:
     *
     * @param gbAccessionsFileName a <code>String</code> denoting the name of the file containing GenBank accessions
     * @param resourcererFileName a <code>String</code> denoting the name of the Resourcerer file
     * @param outputFileName a <code>String</code> denoting the name of the GenBank accessions/GO terms
     *                       in the format GenBank accession \t GO terms
     * @exception FileNotFoundException if an error occurs because at least one of the files
     * denoted by gbAccessionsFileName or resourcererFileName was not found
     */
    public static void writeGOTermsGivenResourcererAndGBsList(String gbAccessionsFileName, String resourcererFileName, String outputFileName) throws FileNotFoundException{
	try {
		System.out.println("writeGOTermsGivenResourcererAndGBsList()" + gbAccessionsFileName);
		System.out.println("writeGOTermsGivenResourcererAndGBsList()" + resourcererFileName);
		System.out.println("writeGOTermsGivenResourcererAndGBsList()" + outputFileName);
	    Useful.checkFile(resourcererFileName);
	    Useful.checkFile(gbAccessionsFileName);
	    ArrayList gbs = Useful.readNamesFromFile(gbAccessionsFileName);
	    HashMap hm = getGOs(resourcererFileName, gbs);
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
    public static void usage() {
	System.out.println("Usage: java GetGOs gbAccessionsFileName resourcererFileName outputFileName\nExample: java GetGOs myList_gbs.txt TIGR_40K_Human_Set myList_gbsGOs.txt");
	System.exit(0);
    }

     /**
     * The <code>test</code> method tests the <code>writeGOTermsGivenResourcererAndGBsList</code> method
     * given the name of the GenBank accessions file, the name of the Resourcerer data file
     * and the name of the output file where GO terms for the given GenBank accession will be written
     *
     * @param gbAccessionsFileName a <code>String</code> corresponding to the name of the file containing GenBank accessions
     * @param resourcererFileName a <code>String</code> corresponding to the name of the Resourcerer data file
     * @param outputFileName a <code>String</code> corresponding to the name of the output file
     * where GO terms for the given GenBank accessions will be written
     */
    public static void test(String gbAccessionsFileName, String resourcererFileName, String outputFileName){
	try {
	    writeGOTermsGivenResourcererAndGBsList(gbAccessionsFileName, resourcererFileName, outputFileName);	
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






