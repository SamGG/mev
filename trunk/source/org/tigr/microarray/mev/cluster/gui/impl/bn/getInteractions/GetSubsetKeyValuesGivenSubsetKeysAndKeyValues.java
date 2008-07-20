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
import java.util.HashSet;
import java.util.HashMap;
import java.util.Set;
import java.util.Iterator;
import org.tigr.microarray.mev.cluster.gui.impl.bn.Useful;
import org.tigr.microarray.mev.cluster.gui.impl.bn.NullArgumentException;
/**
 * The class <code>GetSubsetKeyValuesGivenSubsetKeysAndKeyValues</code> takes in subset of keys file of Strings 
 * one per line and a tab delimited key values file and writes the key values pairs associated with the given subset of keys
 *
 * @author <a href="mailto:amira@jimmy.harvard.edu"></a>
 */
public class GetSubsetKeyValuesGivenSubsetKeysAndKeyValues {
    /**
     * The <code>getSubsetKeyValues</code> method takes in a HashSet of Strings corresponding to a subset of keys
     * and a HashMap containing all key values pairs and returns a HashMap with key values pairs corresponding 
     * to the subset of keys given in the HashSet with key values pairs corresponding to the subset of keys
     * given in the HashSet
     *
     * @param subsetOfKeys a <code>HashSet</code> denoting the given HashSet of Strings corresponding to the subset of keys
     * @param keyValues a <code>HashMap</code> denoting the given HashMap corresponding to keyValuePairs
     * @return a <code>HashMap</code> with key values pairs corresponding to the subset of keys given in the HashSet
     * @exception NullArgumentException if an error occurs because at least one of the given <code>HashMap</code>
     * subsetOfKeys or keyValues was null
     */
    public static HashMap getSubsetKeyValues(HashSet subsetOfKeys, HashMap keyValues) throws NullArgumentException{
	if(subsetOfKeys == null || keyValues == null){
	    throw new NullArgumentException("At least one of the given subsetOfKeys or keyValues is null!\nsubsetOfKeys="+subsetOfKeys+"\nkeyValues="+keyValues);
	}
	Set keySet = keyValues.keySet();
	Iterator it = keySet.iterator();
	String key = null;
	HashMap subsetKeyValues = new HashMap();
	while(it.hasNext()){
	    key = (String) it.next();
	    if(Useful.find(subsetOfKeys,key)){
		subsetKeyValues.put(key, keyValues.get(key));
	    }
	}
	return subsetKeyValues;
    }


    /**
     * The <code>getSubsetKeyValues</code> method takes in a HashSet of Strings corresponding to a subset of keys
     * and the name of a file containing all key values pairs in tab-delimited format and returns a HashMap 
     * with key values pairs corresponding to the subset of keys given in the HashSet
     *
     * @param subsetKeys a <code>HashSet</code> denoting the given HashSet of Strings corresponding to the subset of keys
     * @param keyValuesInTabDelimitedFormat a <code>String</code> denoting the given name of a file
     * containing all key values pairs in tab-delimited format
     * @return a <code>HashMap</code> with key values pairs corresponding to the subset of keys given in the HashSet
     * @exception NullArgumentException if an error occurs because the given <code>HashSet</code> subsetKeys was null
     * @exception FileNotFoundException if an error occurs because the file denoted by keyValuesInTabDelimitedFormat
     * was not found
     */
    public static HashMap getSubsetKeyValues(HashSet subsetKeys, String keyValuesInTabDelimitedFormat) throws FileNotFoundException, NullArgumentException {
    	System.out.println("getSubsetKeyValues()" + keyValuesInTabDelimitedFormat);
    	Useful.checkFile(keyValuesInTabDelimitedFormat);     
	
	HashMap keyValues = Useful.readHashMapFromFile(keyValuesInTabDelimitedFormat);
	HashMap subsetKeyValues = getSubsetKeyValues(subsetKeys, keyValues);
	return subsetKeyValues;
    }


    /**
     * The <code>getSubsetKeyValues</code> method takes in the name of a file containing a subset of keys (one per line) 
     * and the name of a file containing all key values pairs in tab-delimited format and returns a HashMap 
     * with key values pairs corresponding to the subset of keys given in the HashSet 
     *
     * @param subsetKeysFileName a <code>String</code> denoting the given name of a file containing a subset of keys 
     * (one per line)
     * @param keyValuesInTabDelimitedFormat a <code>String</code> denoting the given name of a file containing 
     * all key values pairs in tab-delimited format 
     * @return a <code>HashMap</code> with key values pairs corresponding to the subset of keys given in the HashSet
     * @exception FileNotFoundException if an error occurs because at least one of the files denoted by 
     * subsetKeysFileName or keyValuesInTabDelimitedFormat was not found
     */
    public static HashMap getSubsetKeyValues(String subsetKeysFileName, String keyValuesInTabDelimitedFormat) throws FileNotFoundException {
	try {
		System.out.println("getSubsetKeyValues()" + subsetKeysFileName);
		System.out.println("getSubsetKeyValues()" + keyValuesInTabDelimitedFormat);
	    Useful.checkFile(subsetKeysFileName);
	    Useful.checkFile(keyValuesInTabDelimitedFormat);
	    HashSet subsetKeys = Useful.readUniqueNamesFromFile(subsetKeysFileName);
	    HashMap keyValues = Useful.readHashMapFromFile(keyValuesInTabDelimitedFormat);
	    HashMap subsetKeyValues = getSubsetKeyValues(subsetKeys, keyValues);
	    return subsetKeyValues;
	}
	catch(NullArgumentException nae){
	    System.out.println(nae);
	}
	return null;
    }

    /**
     * The <code>test</code> method tests the <code>getSubsetKeyValues</code> method and writes the subset of key values
     * for a given subset of keys and the key values to a given output file
     *
     * @param subsetKeysFileName a <code>String</code> corresponding to the name of the file containing the subset of keys
     * @param keyValuesFileName a <code>String</code> corresponding to the name of the file containing key values
     * @param outputFileName a <code>String</code> corresponding to the name of the output file 
     * where the subset of key values for the given subset of keys and the key values will be written
     */
    public static void test(String subsetKeysFileName, String keyValuesFileName, String outputFileName){
	try {
	    HashMap subsetKeyValues = getSubsetKeyValues(subsetKeysFileName, keyValuesFileName);
	    Useful.writeHashMapToFile(subsetKeyValues, outputFileName);
	}
	catch(FileNotFoundException fnfe){
	    System.out.println(fnfe);
	}
	catch(NullArgumentException nae){
	    System.out.println(nae);
	}
    }
    
    /**
     * The <code>usage</code> method displays the usage.
     *
     */
    public static void usage(){
	System.out.println("Usage: java GetSubsetKeyValuesGivenSubsetKeysAndKeyValues subsetKeysFileName keyValuesFileName outputSubsetOfKeyValuesFileName\nExample: java GetSubsetKeyValuesGivenSubsetKeysAndKeyValues subsetOfSyms.txt symGBs.txt subsetOfSymsGBs.txt");	
    }

    public static void main(String[] argv){
	if(argv.length != 3){
	    usage();
	    System.exit(0);
	}
	String subsetKeysFileName = argv[0];
	String keyValuesFileName = argv[1];
	String outputFileName = argv[2];
	test(subsetKeysFileName, keyValuesFileName, outputFileName);
    }
}
    
	
    
