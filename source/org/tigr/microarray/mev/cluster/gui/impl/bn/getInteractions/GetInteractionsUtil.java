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
/* GetInteractionsUtil.java
 * Copyright (C) 2005 Amira Djebbari
 */
package org.tigr.microarray.mev.cluster.gui.impl.bn.getInteractions;import java.util.HashMap;import java.util.HashSet;import java.util.ArrayList;
import java.util.Properties;
import java.io.FileReader;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.FileInputStream;
import java.io.LineNumberReader;

import org.tigr.microarray.mev.cluster.gui.impl.bn.BNConstants;
import org.tigr.microarray.mev.cluster.gui.impl.bn.Useful;import org.tigr.microarray.mev.cluster.gui.impl.bn.UsefulInteractions;import org.tigr.microarray.mev.cluster.gui.impl.bn.GetUnionOfInters;
import org.tigr.microarray.mev.cluster.gui.impl.bn.SimpleGeneEdge;
import org.tigr.microarray.mev.cluster.gui.impl.bn.NullArgumentException;
import org.tigr.microarray.mev.cluster.gui.impl.bn.OutOfRangeException;
/**
 * The class <code>GetInteractionsUtil</code> gets contains methods useful for obtaining gene interactions 
 * from either co-occurrences in the literature by combining 3 sources of articles (Resourcerer, Entrez Gene, Pubmed)
 * or protein protein interactions or both
 *
 * @author <a href="mailto:amira@jimmy.harvard.edu"></a>
 */
public class GetInteractionsUtil {
    public static boolean debug = true;

    /**
     * The <code>getOfficialGeneSymbols</code> method gets the official gene symbols for GenBank accessions 
     * in a given file using a given Resourcerer data file
     *
     * @param resFileName a <code>String</code> denoting the name of the Resourcerer data file
     * @param gbAccessionsFileName a <code>String</code> denoting the name of the file containing GenBank accessions
     * @return a <code>HashMap</code> with GenBank accessions as keys and their corresponding gene symbols as values.
     * @exception FileNotFoundException if an error occurs because at least one of the files
     * denoted by resFileName or gbAccessionsFileName was not found
     */
    public static HashMap getOfficialGeneSymbols(String resFileName, String gbAccessionsFileName) throws FileNotFoundException{
	//Useful.checkFile(resFileName);
	//Useful.checkFile(gbAccessionsFileName);
	return GetOfficialGeneSymbols.getGBsSymbolsGivenResourcererAndGBsFileName(gbAccessionsFileName, resFileName);
    }

    /**
     * The <code>getAccessions</code> method gets the GenBank accessions for the given official gene symbols 
     * using a given Resourcerer data file
     *
     * @param resFileName a <code>String</code> denoting the name of the Resourcerer data file
     * @param geneSymbols an <code>ArrayList</code> corresponding to the list of official gene symbols
     * @return a <code>HashMap</code> with official gene symbols as keys and GenBank accessions as values
     * @exception FileNotFoundException if an error occurs because the file denoted by resFileName was not found
     */
    public static HashMap getAccessions(String resFileName, ArrayList geneSymbols) throws FileNotFoundException{
	try {
	    //Useful.checkFile(resFileName);
	    return GetAccessions.getAccessions(resFileName, geneSymbols);
	}
	catch(NullArgumentException nae){
	    System.out.println(nae);
	}
	return null;
    }

    /**
     * The <code>getGOs</code> method gets the Gene Ontology (GO) terms for GenBank accessions in a given file
     * using a given Resourcerer data file
     *
     * @param resFileName a <code>String</code> denoting the name of the Resourcerer data file
     * @param gbAccessionsFileName a <code>String</code> denoting the name of the file containing GenBank accessions
     * @return a <code>HashMap</code> with GenBank accessions as keys and their corresponding GO terms as values.
     * @exception FileNotFoundException if an error occurs because at least one of the files
     * denoted by resFileName or gbAccessionsFileName was not found
     */
    public static HashMap getGOs(String resFileName, String gbAccessionsFileName) throws FileNotFoundException{
	//Useful.checkFile(resFileName);
	//Useful.checkFile(gbAccessionsFileName);
	return GetGOs.getGOsGivenResourcererAndGBsFileName(resFileName, gbAccessionsFileName);
    }


    /**
     * The <code>getResourcererArticles</code> method gets the Resourcerer articles for GenBank accessions 
     * in a given file using a given Resourcerer data file
     *
     * @param resFileName a <code>String</code> denoting the name of the Resourcerer data file
     * @param gbAccessionsFileName a <code>String</code> denoting the name of the file containing GenBank accessions
     * @return a <code>HashMap</code> with GenBank accessions as keys and their corresponding articles 
     * from Resourcerer as values.
     * @exception FileNotFoundException if an error occurs because at least one of the files
     * denoted by resFileName or gbAccessionsFileName was not found
     */
    public static HashMap getResourcererArticles(String resFileName, String gbAccessionsFileName) throws FileNotFoundException{
	//Useful.checkFile(resFileName);
	//Useful.checkFile(gbAccessionsFileName);
	return GetResourcererArticles.getGBsArticlesGivenResourcererAndGBsFileName(resFileName, gbAccessionsFileName);
    }
    /**
     * The <code>getUniqueSymbols</code> method takes in a <code>HashMap</code> with GenBank accessions as keys 
     * and their corresponding official gene symbols as values and returns a <code>HashSet</code>
     * containing the unique gene symbols in the values of the given <code>HashMap</code>
     *
     * @param gbSymbols a <code>HashMap</code> with GenBank accessions as keys and their corresponding
     * official gene symbols as values.
     * @return a <code>HashSet</code> containing the unique gene symbols as <code>String</code>s in the values
     * of the given <code>HashMap</code>
     * @exception NullArgumentException if an error occurs because the given <code>HashMap</code> was null
     */
    public static HashSet getUniqueSymbols(HashMap gbSymbols) throws NullArgumentException {
	if(gbSymbols == null){
	    throw new NullArgumentException("The argument gbSymbols was null");
	}
	return Useful.getUniqueSymbols(gbSymbols);
    }
    /**
     * The <code>getSubsetSymbolsArticlesFromSymbolsArticles</code> method takes in a <code>HashSet</code>
     * of unique gene symbols and a <code>HashMap</code> of gene symbols as keys and corresponding articles as values
     *
     * @param uniqueSymbols a <code>HashSet</code> containing unique gene symbols as <code>String</code>s
     * @param symbolsArticles a <code>HashMap</code> containing gene symbols as <code>String</code>s as keys
     * and their corresponding articles as values
     * @return a <code>HashMap</code> with the given subset of keys as keys and their values found
     * in the given <code>HashMap</code> as values.
     * @exception NullArgumentException if an error occurs because at least one of the given arguments was null.
     */
    public static HashMap getSubsetSymbolsArticlesFromSymbolsArticles(HashSet uniqueSymbols,  HashMap symbolsArticles) throws NullArgumentException {
	if(uniqueSymbols == null || symbolsArticles == null){
	    throw new NullArgumentException("At least one of uniqueSymbols or symbolsArticles was null");
	}
	return GetSubsetKeyValuesGivenSubsetKeysAndKeyValues.getSubsetKeyValues(uniqueSymbols, symbolsArticles);
    }
    /**
     * The <code>replaceSymbolsWithGbsInSymbolsArticles</code> method takes in 2 <code>HashMap</code>s
     * where the values of the first HashMap correspond to the keys of the second HashMap
     * and returns a new HashMap by replacing the keys in the second HashMap by the corresponding values in the first HashMap
     *
     * @param gbSymbols a <code>HashMap</code> corresponding to key value pairs, in this application,
     * GenBank accessions as keys and their corresponding official gene symbols as values.
     * @param symbolsArticles a <code>HashMap</code> corresponding to key value pairs where the keys
     * are the same as the value of the first HashMap, in this application, official gene symbols as keys
     * and their corresponding articles as values.
     * @return a <code>HashMap</code> by replacing the keys in the second HashMap by the corresponding values
     * in the first HashMap
     * @exception NullArgumentException if an error occurs because at least one of the arguments was null
     */
    public static HashMap replaceSymbolsWithGbsInSymbolsArticles(HashMap gbSymbols, HashMap symbolsArticles) throws NullArgumentException {
	if(gbSymbols == null || symbolsArticles == null){
	    throw new NullArgumentException("At least one of gbSymbols or symbolsArticles was null");
	}	
	return ReplaceSymbolsWithGbsInSymbolsArticles.getGbArticles(gbSymbols, symbolsArticles);
    }

    /**
     * The <code>uniquelyMergeArrayLists</code> method takes in 3 <code>ArrayList</code>s
     * of <code>SimpleGeneEdge</code> objects and returns the union of them.
     *
     * @param inter1 an <code>ArrayList</code> of <code>SimpleGeneEdge</code> objects
     * @param inter2 an <code>ArrayList</code> of <code>SimpleGeneEdge</code> objects
     * @param inter3 an <code>ArrayList</code> of <code>SimpleGeneEdge</code> objects
     * @return an <code>ArrayList</code> of <code>SimpleGeneEdge</code> objects corresponding to the union of
     * the 3 given <code>ArrayList</code> of <code>SimpleGeneEdge</code> objects
     * @exception NullArgumentException if an error occurs because at least one of the given arguments was null
     */
    public static ArrayList uniquelyMergeArrayLists(ArrayList inter1, ArrayList inter2, ArrayList inter3) throws NullArgumentException {
	if(inter1 == null || inter2 == null || inter3 == null){
	    throw new NullArgumentException("At least one of inter1, inter2 or inter3 is null\ninter1="+inter1+"\ninter2="+inter2+"\ninter3="+inter3);
	}
	ArrayList union1And2 = GetUnionOfInters.uniquelyMergeArrayLists(inter1, inter2);
	return GetUnionOfInters.uniquelyMergeArrayLists(inter3, union1And2);
    }


    /**
     * The <code>replaceSymsWithGBsInInter</code> method takes in a Resourcerer file name and an <code>ArrayList</code> of 
     * <code>SimpleGeneEdge</code> objects using official gene symbols and replaces the official gene symbols with their
     * corresponding GenBank accessions as found in the given Resourcerer file
     *
     * @param resourcererFileName a <code>String</code> denoting the name of the Resourcerer file
     * @param interFromPpiSyms an <code>ArrayList</code> of <code>SimpleGeneEdge</code> objects using official gene symbols 
     * @return an <code>ArrayList</code>  of <code>SimpleGeneEdge</code> objects after replacing the official gene symbols 
     * with their corresponding GenBank accessions as found in the given Resourcerer file
     * @exception NullArgumentException if an error occurs because the given interFromPpiSyms was null
     */
    public static ArrayList replaceSymsWithGBsInInter(String resourcererFileName, ArrayList interFromPpiSyms) throws NullArgumentException{
	try {
	    //Useful.checkFile(resourcererFileName);
	    if(interFromPpiSyms == null){
		throw new NullArgumentException("Given interFromPpiSyms is null!");
	    } 
	    ArrayList newGeneSymbols = UsefulInteractions.getNodes(interFromPpiSyms);
	    HashMap newGeneSymbolsGBs = GetInteractionsUtil.getAccessions(resourcererFileName, newGeneSymbols);
	    SimpleGeneEdge sGE = null;
	    SimpleGeneEdge replacedSGE = null;
	    String from = null;
	    String to = null;
	    String replacedFrom = null;
	    String replacedTo = null;
	    ArrayList result = new ArrayList();
	    for(int i = 0; i < interFromPpiSyms.size(); i++){
		sGE = (SimpleGeneEdge) interFromPpiSyms.get(i);
		from = sGE.getFrom();
		to = sGE.getTo();
		replacedFrom = (String) newGeneSymbolsGBs.get(from);
		replacedTo = (String) newGeneSymbolsGBs.get(to);
		if(replacedFrom != null && replacedTo != null){
		    replacedSGE = new SimpleGeneEdge(replacedFrom, replacedTo, sGE.getWeight());
		    if(!UsefulInteractions.containsEitherWay(result, replacedSGE)){
			result.add(replacedSGE);
		    }
		}		
	    }
	    return result;
	}
	catch(FileNotFoundException fnfe){
	    System.out.println(fnfe);
	}
	return null;
    }
    
    public static ArrayList loadKeggInteractions(String species, String location){
    	ArrayList<String> kegg = new ArrayList<String>();
    	String fileName = location + species + "_kegg_edges.txt";
    	try {    	    
    	    FileReader fr = new FileReader(fileName);
    	    LineNumberReader lnr = new LineNumberReader(fr);
    	    String s = null;
    	    while((s = lnr.readLine())!=null){
    		s = s.trim();
    		kegg.add(s);
    	    }
    	    lnr.close();
    	    fr.close();
    	    System.out.println("Loaded KEGG edges: " + kegg.size());
    	    return kegg;
    	}
    	catch(IOException ioe){
    	    System.out.println(ioe);
    	}
    	
    	return null;
    }
    
    public static ArrayList getEdgesfromKegg(ArrayList keggListAll, String accListFile) {
    	ArrayList<String> dataNodes = new ArrayList<String>();
    	
    	try {    	    
    	    FileReader fr = new FileReader(accListFile);
    	    LineNumberReader lnr = new LineNumberReader(fr);
    	    String s = null;
    	    while((s = lnr.readLine())!=null){
    		s = s.trim();
    		dataNodes.add(s);
    	    }
    	    lnr.close();
    	    fr.close();
    	}
    	catch(IOException ioe){
    	    System.out.println(ioe);
    	}
    	
    	ArrayList<SimpleGeneEdge> keggMatches = new ArrayList<SimpleGeneEdge>();
    	ArrayList<String> keggMatchesAsStrings = new ArrayList<String>();
    	// Find Kegg edges that match the data
    	for(int i = 0; i < dataNodes.size(); i++) {
    		String _curAcc = (String)dataNodes.get(i);
    		for(int ii = i+1; ii < dataNodes.size(); ii++) {
    			String _nextAcc = (String)dataNodes.get(ii);
    			if(!_nextAcc.equals(_curAcc)) {
	    			String _curEdge1 = _curAcc + "-" + _nextAcc;
	    			String _curEdge2 = _nextAcc + "-" + _curAcc;
	    			System.out.println("Searchin for: " + _curEdge1 + " and reverse");
	    			if(!keggMatchesAsStrings.contains(_curEdge1) && !keggMatchesAsStrings.contains(_curEdge2)) {
	    				keggMatches.add(new SimpleGeneEdge(_curAcc, _nextAcc, 1.0));
	    				keggMatches.add(new SimpleGeneEdge(_nextAcc, _curAcc, 1.0));
	    				keggMatchesAsStrings.add(_curEdge1);
	    				keggMatchesAsStrings.add(_curEdge2);
	    			}
    			}
    		}
    		//dataNodes.remove(i);
    	}
    	if(keggMatches.size() > 0) {
    		System.out.println("Returning KEGG matches: " + keggMatches.size());
    		keggMatchesAsStrings = null;
    		return keggMatches;
    	}
    	else {
    		System.out.println("Returning KEGG matches: NONE Found!!");
    		return null;
    	}
    }
}








