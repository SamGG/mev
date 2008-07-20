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
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashSet;
import java.util.Set;
import org.tigr.microarray.mev.cluster.gui.impl.bn.Useful;
import org.tigr.microarray.mev.cluster.gui.impl.bn.UsefulInteractions;
import org.tigr.microarray.mev.cluster.gui.impl.bn.SimpleGeneEdge;
import org.tigr.microarray.mev.cluster.gui.impl.bn.OutOfRangeException;
import org.tigr.microarray.mev.cluster.gui.impl.bn.NullArgumentException;
/**
 * The class <code>GeneInteractions</code> contains methods to createInteractions from given keyValue pairs.
 * Using this class, one can create interactions between keys that have some values in common. 
 * Furthermore, one can filter out values associated with a number of keys greater than a threshold 
 * and thus create interactions in a stricter fashion. 
 *
 * @author <a href="mailto:amira@jimmy.harvard.edu"></a>
 */
public class GeneInteractions {
    /**
     * The variable <code>debug</code> is a debug flag, debug information will be displayed if true, not otherwise
     */
    public static boolean debug = false;
    
    /**
     * The <code>createInteractions</code> method creates interactions from a HashMap of key value pairs
     * such that two keys are connected if they share some values and returns the interactions as an ArrayList 
     * of SimpleGeneEdge objects
     *
     * @param symbolsArticles a <code>HashMap</code> corresponding to key value pairs
     * @return an <code>ArrayList</code> of SimpleGeneEdge objects corresponding to interactions 
     * between two keys if they have some values in common
     * @exception NullArgumentException if an error occurs because the given <cde>HashMap</code> symbolsArticles was null
     */
    public ArrayList createInteractions(HashMap symbolsArticles) throws NullArgumentException{
	if(symbolsArticles == null){
	    throw new NullArgumentException("Given symbolsArticles is null!");
	}
	ArrayList interactions = new ArrayList();
	Set symbols = symbolsArticles.keySet();
	Iterator symbolsIt1 = symbols.iterator();
	Iterator symbolsIt2 = null;
	String symbol1 = null;
	String symbol2 = null;
	HashSet articles1 = null;
	HashSet articles2 = null;
	HashSet commonArticles = null;
	int weight = 0;
	SimpleGeneEdge sGE = null;
	HashSet done = new HashSet();
	// for each key (symbol1) in the HashMap of key value pairs
	while(symbolsIt1.hasNext()){
	    symbol1 = (String) symbolsIt1.next();
	    // get the values (articles1) associated with this key
	    articles1 = Useful.StringToHashSet((String) symbolsArticles.get(symbol1));
	    // for each key (symbol2)
	    symbolsIt2 = symbols.iterator();
	    while(symbolsIt2.hasNext()){		
		symbol2 = (String) symbolsIt2.next();
		// if both keys are non null, not equal to each other, 
		// and this possible association hasn't been looked at before
		if((symbol1 != null) && (symbol2 != null)){
		    if(!symbol1.equals(symbol2)){
			if(!done.contains(symbol1+"_"+symbol2)&& !done.contains(symbol2+"_"+symbol1)){			    
			    // get the values (articles2) associated with this key (symbol2)
			    articles2 = Useful.StringToHashSet((String) symbolsArticles.get(symbol2));
			    // if the respective values of symbol1 and symbol2, 
			    //namely articles1 and articles2 contain any values in common (commonArticles)
			    commonArticles = getArticlesInCommon(articles1, articles2);
			    if(commonArticles != null){
				if(commonArticles.size()!=0){
				    // the weight of the interaction is equal to the number of values in common
				    weight = commonArticles.size();			    
				    // create an undirected weighted edge between symbol1 and symbol2 with this weight
				    sGE = new SimpleGeneEdge(symbol1, symbol2, weight);
				    // add this edge to the ArrayList of interactions, if it's not already there
				    System.out.println("Simple Gene Edge: " + sGE.toString());
				    if(!UsefulInteractions.containsEitherWay(interactions, sGE)){
					interactions.add(sGE); 		
				    }
				}				
			    }
			}
			// add this key pair to the set of already done potential associations
			done.add(symbol1+"_"+symbol2);
						//Raktim - Debugging for LM DAG problem.
						//System.out.println("Done Added: " + symbol1+"_"+symbol2);
		    }
		}   
	    }
	}	
		//System.out.println("Done");
	return interactions;
    }
    /**
     * The <code>getArticlesInCommon</code> method returns the elements in common between two HashSet objects, 
     * in the form of a HashSet
     *
     * @param articles1 a <code>HashSet</code> of Strings
     * @param articles2 a <code>HashSet</code> of Strings
     * @return a <code>HashSet</code> of Strings corresponding to the elements in common between the two given HashSets.
     */
    public HashSet getArticlesInCommon(HashSet articles1, HashSet articles2){
	if((articles1 == null) || (articles2 == null)){
	    return null;
	}
	HashSet commonArticles = new HashSet();
	Iterator articles1It = articles1.iterator();
	String article1 = null;
	while(articles1It.hasNext()){
	    article1 = (String) articles1It.next();
	    if(articles2.contains(article1)){
		commonArticles.add(article1);
	    }
	}
	return commonArticles;
    }
    /**
     * The <code>backwards</code> method takes in a HashMap of key value pairs and flips it 
     * so that it returns a new HashMap with keys corresponding to the values of the first HashMap
     * and values corresponding to keys of the first HashMap
     *
     * @param symbolsArticles a <code>HashMap</code> of key value pairs
     * @return a <code>HashMap</code> with keys corresponding to the values of the first HashMap and values 
     * corresponding to keys of the first HashMap. Example: Suppose the given HashMap contains <key1,{value1,value2}>, 
     * this method will return a new HashMap containing <value1, key1>, <value2, key1>.
     * @exception NullArgumentException if an error occurs because the given symbolsArticles was null
     */
    public static HashMap backwards(HashMap symbolsArticles) throws NullArgumentException {
	if(symbolsArticles == null){
	    throw new NullArgumentException("Given symbolsArticles is null!");
	}
	Set keys = symbolsArticles.keySet();
	Iterator itKeys = keys.iterator();
	String key = null;
	HashSet articles = null;
	HashMap artSymbols = new HashMap();
	HashSet symbols = null;
	Iterator itArt = null;
	String article = null;
	// for each key 
	while(itKeys.hasNext()){
	    key = (String) itKeys.next();
	    // get the values (articles) associated with this key
	    articles = Useful.StringToHashSet((String) symbolsArticles.get(key));
	    if(articles!=null){
		itArt = articles.iterator();
		// for each value (article)
		while(itArt.hasNext()){
		    article = (String) itArt.next();
		    // get the keys already associated with it in the new HashMap
		    symbols = (HashSet) artSymbols.get(article);
		    // if there aren't any, initialize a new HashSet of keys
		    if(symbols == null){
			symbols = new HashSet();
		    }
		    // add this key 
		    symbols.add(key);
		    // put in the new HashMap the following key value pair <value, HashSet of keys associated with it
		    artSymbols.put(article, symbols);
		}
	    }
	}	
	return artSymbols;
    }
    /**
     * The <code>filter</code> method takes in a HashMap of value key pairs, a HashMap of key value pairs 
     * and a threshold and removes values that are associated with more keys than the given threshold 
     * from the HashMap of key value pairs 
     *
     * @param artSymbols a <code>HashMap</code> corresponding to value key pairs (flipped HashMap as produced
     * from the <code>backwards</code> method
     * @param symbolsArticles a <code>HashMap</code> corresponding to the original key value pairs
     * @param threshold an <code>int</code> such that values that are associated with greater 
     * than threshold number of keys will be removed from the original key value pairs, 
     * possible values of threshold are any int >= 2
     * @return a <code>HashMap</code> containing key value pairs from the original symbolsArticles 
     * key value pairs such that values (articles) associated with more than threshold number of keys (genes) are removed
     * @exception OutOfRangeException if an error occurs because the threshold is out of range
     */
    public static HashMap filter(HashMap artSymbols, HashMap symbolsArticles, int threshold) throws OutOfRangeException, NullArgumentException {
	if(artSymbols == null || symbolsArticles == null){
	    throw new NullArgumentException("At least one of the given <code>HashMap</code>s artSymbols or symbolsArticles is null!\nartSymbols="+artSymbols+"\nsymbolsArticles="+symbolsArticles);
	}
	if(threshold < 2){
	    throw new OutOfRangeException("Threshold "+threshold+" is out of range!");
	}
	Set keys1 = artSymbols.keySet();
	Iterator itkeys1 = keys1.iterator();
	String art = null;
	HashSet symb = null;
	HashSet articlesToRemove = new HashSet();
	// for each key (article)
	while(itkeys1.hasNext()){
	    art = (String) itkeys1.next();
	    // get the value (HashSet of gene symbols) associated with this key (article)
	    symb = (HashSet) artSymbols.get(art);
	    if(symb != null){
		// if there are more than threshold number of genes associated with this article 
		if(symb.size() > threshold){
		    // add this article to the HashSet of articles to remove
		    articlesToRemove.add(art);
		}
	    }
	}
	Set keys2 = symbolsArticles.keySet();
	Iterator itkeys2 = keys2.iterator();
	String symbol = null;
	// for each key (gene symbol)
	while(itkeys2.hasNext()){
	    symbol = (String) itkeys2.next();
	    // get the value (HashSet of articles) associated with this key (gene symbol)
	    HashSet articles = Useful.StringToHashSet((String) symbolsArticles.get(symbol));
	    Iterator itrem = articlesToRemove.iterator();
	    // for each article to remove
	    while(itrem.hasNext()){
		// get the article
		art = (String) itrem.next();
		if(art != null && articles != null){
		    // remove it
		    articles.remove(art);
		    // put back the modified HashSet of articles in the HashMap
		    symbolsArticles.put(symbol, Useful.HashSetToString(articles));
		}
	    }
	}
	return symbolsArticles;
    }
    /**
     * The <code>testCreateInteractions</code> method tests creating interactions from a given file
     * containing key values pairs (in this application, GenBank accessions with their corresponding articles)
     *
     * @param gbArticlesFileName a <code>String</code> denoting the name of the file containing key value pairs in tab-delimited format such that: key_1\tvalue_1,value_2,...,value_n
     */
    public static void testCreateInteractions(String gbArticlesFileName){      
	try {
		System.out.println("testCreateInteractions()" + gbArticlesFileName);
	    Useful.checkFile(gbArticlesFileName);
	    HashMap gbArticles = Useful.readHashMapFromFile(gbArticlesFileName);
	    System.out.println("gbArticles="+gbArticles);
	    GeneInteractions gI = new GeneInteractions();
	    ArrayList inter = (ArrayList) gI.createInteractions(gbArticles);    
	    System.out.println("inter="+inter);
	}
	catch(FileNotFoundException fnfe){
	    System.out.println(fnfe);
	}
	catch(NullArgumentException nae){
	    System.out.println(nae);
	}
    }
    /**
     * The <code>testCreateInteractionsWithFilter</code> method tests creating interactions from a given file 
     * containing key values pairs (in this application, GenBank accessions with their corresponding articles)
     * and a given threshold use to filter to remove values associated with more genes than the threshold 
     * (in this application, articles associated with more genes than the threshold
     *
     * @param gbArticlesFileName a <code>String</code> denoting the name of the file containing key value pairs 
     * in tab-delimited format such that: key_1\tvalue_1,value_2,...,value_n
     * @param threshold an <code>int</code> corresponding to the threshold used to remove values 
     * associated with greater than or equal to threshold number of keys.
     */
    public static void testCreateInteractionsWithFilter(String gbArticlesFileName, int threshold){
	try {
	    System.out.println("--> in testCreateInteractionsWithFilter");
	    HashMap gbArticles = Useful.readHashMapFromFile(gbArticlesFileName);
	    System.out.println("gbArticlesBeforeFilter="+gbArticles);
	    HashMap articlesGbs = backwards(gbArticles);
	    HashMap gbArticles2 = null;
	    gbArticles2 = filter(articlesGbs, gbArticles, threshold);
	    System.out.println("gbArticlesAfterFilter="+gbArticles2);
	    GeneInteractions gI = new GeneInteractions();
	    ArrayList inter = (ArrayList) gI.createInteractions(gbArticles2);    
	    System.out.println("inter="+inter);
	}
	catch(OutOfRangeException oore){
	    System.out.println(oore);
	}
	catch(FileNotFoundException fnfe){
	    System.out.println(fnfe);
	}
	catch(NullArgumentException nae){
	    System.out.println(nae);
	}
    }

    /**
     * The <code>usage</code> method displays the usage
     *
     */
    public static void usage(){
	System.out.println("Usage: java GeneInteractions gbArticlesFileName articleRemovalThreshold\nExample: java GeneInteractions gbArticles.txt 2");
	System.exit(0);
    }

    public static void main(String[] argv){
	if(argv.length != 2){
	    usage();
	}
	testCreateInteractions(argv[0]);
	testCreateInteractionsWithFilter(argv[0], Integer.parseInt(argv[1]));
    }
}














