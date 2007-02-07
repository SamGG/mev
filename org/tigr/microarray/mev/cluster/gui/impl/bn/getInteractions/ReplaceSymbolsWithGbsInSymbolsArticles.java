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
/* ReplaceSymbolsWithGbsInSymbolsArticles.java
 * Copyright (C) 2005 Amira Djebbari
 */
package org.tigr.microarray.mev.cluster.gui.impl.bn.getInteractions;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Set;
import java.util.Iterator;
import org.tigr.microarray.mev.cluster.gui.impl.bn.Useful;
import org.tigr.microarray.mev.cluster.gui.impl.bn.NullArgumentException;
/**
 * The class <code>ReplaceSymbolsWithGbsInSymbolsArticles</code> takes in key value pairs of symbols/GenBank 
 * accessions and symbols/articles and returns key value pairs of GenBank accessions/articles
 *
 * @author <a href="mailto:amira@jimmy.harvard.edu"></a>
 */
public class ReplaceSymbolsWithGbsInSymbolsArticles {
    public static boolean debug = true;
    /**
     * The <code>getGbArts</code> method gets key value pairs of GenBank accessions/articles 
     * given key value pairs of symbols/GenBank accessions and GenBank accessions/articles
     *
     * @param gbSymbols a <code>HashMap</code> contains key value pairs of symbols/GenBank accessions
     * @param symbolsArticles a <code>HashMap</code> contains key value pairs of symbols/articles
     * @return a <code>HashMap</code> corresponding to key value pairs of GenBank accessions/articles
     * @exception NullArgumentException if an error occurs because at least one of the given <code>HashMap</code> 
     * gbSymbols or symbolsArticles was null
     */
    public static HashMap getGbArticles(HashMap gbSymbols, HashMap symbolsArticles) throws NullArgumentException{
	if(gbSymbols == null || symbolsArticles == null){
	    throw new NullArgumentException("At least one of the given gbSymbols or symbolsArticles is null!\ngbSymbols="+gbSymbols+"\nsymbolsArticles="+symbolsArticles);
	}
	HashMap gbArticles = new HashMap();
	Set keySet = gbSymbols.keySet();
	Iterator it = keySet.iterator();
	String gb = null;
	String symbols = null;
	// for each gb in gbSymbols
	while(it.hasNext()){
	    gb = (String) it.next();
	    // put gb and articles from symbolsArticles 
	    // for the symbol associated with gb from gbSymbols
	    symbols = (String) symbolsArticles.get((String) gbSymbols.get(gb));
	    if(symbols != null){
		gbArticles.put(gb, symbols);
	    }	    
	}
	return gbArticles;
    }

    /**
     * This <code>usage</code> method displays the usage of this class
     *
     */
    public static void usage(){
	System.out.println("Usage: java ReplaceSymbolsWithGbsInSymbolsArticles gbSymsFileName symArtsFileName outputFileName\nExample: java ReplaceSymbolsWithGbsInSymbolsArticles gbSyms.txt symArts.txt outGbArtsFileName");
	System.exit(0);
    }

    /**
     * The <code>test</code> method tests the <code>getGBArticles</code> method given the name of the gbSymbols file
     * in tab delimited format, the name of the symbolsArticles file in tab delimited format and writes the gbArticles 
     * in tab delimited format
     *
     * @param gbSymbolsFileName a <code>String</code> corresponding to the name of the file containing gbSymbols 
     * in tab-delimited format: gb\tsymbol
     * @param symbolsArticlesFileName a <code>String</code> corresponding to the name of the file containing
     * symbolsArticles in tab-delimited format: symbol\tarticle_1,article_2,...,article_n
     * @param outputFileName a <code>String</code> corresponding to the name of the output file 
     * that will be written in tab-delimited format: gb\tarticle_1,article_2,...,article_n
     */
    public static void test(String gbSymbolsFileName, String symbolsArticlesFileName, String outputFileName){
	try {
	    Useful.checkFile(gbSymbolsFileName);
	    Useful.checkFile(symbolsArticlesFileName);
	    HashMap gbSymbols = Useful.readHashMapFromFile(gbSymbolsFileName);
	    HashMap symbolsArticles = Useful.readHashMapFromFile(symbolsArticlesFileName);
	    HashMap gbArticles = getGbArticles(gbSymbols, symbolsArticles);
	    Useful.writeHashMapToFile(gbArticles, outputFileName);
	}
	catch(FileNotFoundException fnfe){
	    System.out.println(fnfe);
	}
	catch(NullArgumentException nae){
	    System.out.println(nae);
	}
    }

    public static void main(String[] argv){
	if(argv.length != 3){
	    usage();
	}
	String gbSymbolsFileName = argv[0];
	String symbolsArticlesFileName = argv[1];
	String outputFileName = argv[2];
	test(gbSymbolsFileName, symbolsArticlesFileName, outputFileName);
    }
}
    
	
    
