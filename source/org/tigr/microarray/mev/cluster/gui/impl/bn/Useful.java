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
/* Useful.java
 * Copyright (C) 2005 Amira Djebbari
 */
package org.tigr.microarray.mev.cluster.gui.impl.bn;import java.io.FileReader;
import java.io.LineNumberReader;
import java.io.File;import java.io.FileNotFoundException;import java.io.PrintWriter;
import java.io.FileOutputStream;import java.io.IOException;import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;import java.util.HashMap;
import java.util.ArrayList;
import java.util.Iterator;import java.util.Set;import org.tigr.microarray.mev.TMEV;
/**
 * The class <code>Useful</code> contains many useful methods for reading/writing from/to files
 *
 * @author <a href="mailto:amira@jimmy.harvard.edu"></a>
 */
public class Useful {
    /**
     * The <code>checkFile</code> method checks the file denoted by the given file name
     *
     * @param fileName a <code>String</code> corresponding to the given file name
     * @exception FileNotFoundException if an error occurs that the file denoted by the given file name does not exist
     */
	//private static File inputFile;
	
    public static void checkFile(String fileName) throws FileNotFoundException {
    /*
    	String dataPath = TMEV.getDataPath();
    	File pathFile = TMEV.getFile("data/");
    	if(dataPath != null) {
          pathFile = new File(dataPath);
            if(!pathFile.exists())
                pathFile = TMEV.getFile("data/");
        }
    	
    	//inputFile=new File(pathFile,fileName);
    	if(GetInteractionParemeterLitDialog.path!=null){
    		if(!(new File(GetInteractionParemeterLitDialog.path+"\\"+fileName)).exists()){
    			throw new FileNotFoundException("File denoted by "+"mmmm "+fileName+" does not exist!");
    		}
    	}
    	if(GetInteractionParemeterPPIDialog.path!=null){
    		if(!(new File(GetInteractionParemeterLitDialog.path+"\\"+fileName)).exists()){
    			throw new FileNotFoundException("File denoted by "+fileName+" does not exist!");
    		}
    	}
    	if(GetInteractionParemeterBothDialog.path!=null){
    		if(!(new File(GetInteractionParemeterBothDialog.path+"\\"+fileName)).exists()){
    			throw new FileNotFoundException("File denoted by "+fileName+" does not exist!");
    		}
    	}
    	*/
    	if(!(new File(fileName)).exists()){
			throw new FileNotFoundException("File denoted by "+"newwww "+fileName+" does not exist!");
		}
    }
    /*
    public static void checkFile(String fileName) throws FileNotFoundException {
    	String dataPath = TMEV.getDataPath();
    	File pathFile = TMEV.getFile("data/");
    	if(dataPath != null) {
          pathFile = new File(dataPath);
            if(!pathFile.exists())
                pathFile = TMEV.getFile("data/");
        }
    	//inputFile=new File(pathFile,fileName);
    		if(!(new File(fileName)).exists()){
    			throw new FileNotFoundException("File denoted by "+"hello"+fileName+" does not exist!");
    		}
    	
    }
    
    public static String getFilePath() throws FileNotFoundException {
    	return inputFile.toString();
    }
    */
    /**
     * The <code>readUniqueNamesFromFile</code> method reads all the lines in a given file into an ArrayList object
     *
     * @param fileName a <code>String</code> corresponding to the name of the file to read
     * @return an <code>HashSet</code> containing String objects corresponding to all the lines in the given file
     * @exception FileNotFoundException if an error occurs because the file denoted by the given fileName was not found
     */
    public static HashSet readUniqueNamesFromFile(String fileName) throws FileNotFoundException{
    	String path=null;    	System.out.println("readUniqueNamesFromFile()" + fileName);
    	checkFile(fileName);
    	
	try {
		/*
		if(GetInteractionParemeterLitDialog.path!=null)
			path=GetInteractionParemeterLitDialog.path;
		if(GetInteractionParemeterPPIDialog.path!=null)
			path=GetInteractionParemeterPPIDialog.path;
		if(GetInteractionParemeterBothDialog.path!=null)
			path=GetInteractionParemeterBothDialog.path;
			*/
	    HashSet names = new HashSet();
	    
	    FileReader fr = new FileReader(fileName);
	    LineNumberReader lnr = new LineNumberReader(fr);
	    String s = null;
	    String[] tokens = null;
	    while((s = lnr.readLine())!=null){
		s = s.trim();
		names.add(s);
	    }
	    lnr.close();
	    fr.close();
	    return names;
	}
	catch(IOException ioe){
	    System.out.println(ioe);
	}
	return null;
    }
    /**
     * The <code>readNamesFromFile</code> method reads all the lines in a given file into an ArrayList object
     *
     * @param fileName a <code>String</code> corresponding to the name of the file to read
     * @return an <code>ArrayList</code> containing String objects corresponding to all the lines in the given file
     * @exception FileNotFoundException if an error occurs because the file denoted by the given fileName was not found
     */
    public static ArrayList readNamesFromFile(String fileName) throws FileNotFoundException{
	try {
	    ArrayList names = new ArrayList();
	    /*
	    String dataPath = TMEV.getDataPath();
    	File pathFile = TMEV.getFile("data/");
    	if(dataPath != null) {
            pathFile = new File(dataPath);
            if(!pathFile.exists())
                pathFile = TMEV.getFile("data/");
        }
        */
    	//FileReader fr = new FileReader(pathFile+fileName);
    	//System.out.print(fileName);
	    FileReader fr = new FileReader(fileName);
	    LineNumberReader lnr = new LineNumberReader(fr);
	    String s = null;
	    String[] tokens = null;
	    while((s = lnr.readLine())!=null){
	    	s = s.trim();
	    	names.add(s);
	    }
	    lnr.close();
	    fr.close();
	    return names;
	}
	catch(IOException ioe){
	    System.out.println(ioe);
	}
	return null;
    }

    /**
     * The <code>readHashMapFromFile</code> method reads from a given file name
     * containing Strings in tab-delimited format (2 columns) 
     * and returns a HashMap containing the first column Strings as keys and
     * the second column Strings as values
     *
     * @param fileName a <code>String</code> corresponding to the name of a 
     * file containing Strings in tab-delimited format (2 columns)
     * @return a <code>HashMap</code> containing the first column Strings 
     * as keys and the second column Strings as values
     * @exception FileNotFoundException if an error occurs because the file denoted by the given fileName was not found
     */
    public static HashMap readHashMapFromFile(String fileName) throws FileNotFoundException{    	System.out.println("readHashMapFromFile()" + fileName);
    	checkFile(fileName);
	try {
	    HashMap result = new HashMap();
	    FileReader fr = new FileReader(fileName);
	    LineNumberReader lnr = new LineNumberReader(fr);
	    String s = null;
	    String[] tokens = null;
	    while((s = lnr.readLine())!=null){
		s = s.trim();
		tokens = s.split("\t");
		if(tokens.length >= 2){
		    result.put(tokens[0], tokens[1]);
		}
	    }
	    lnr.close();
	    fr.close();
	    return result;
	}
	catch(IOException ioe){
	    System.out.println(ioe);
	}
	return null;
    }
    /**
     * The <code>find</code> method searches for a given String in a given ArrayList of Strings
     *
     * @param names an <code>ArrayList</code> corresponding to the given ArrayList of Strings
     * @param toFind a <code>String</code> corresponding to the search String
     * @return a <code>boolean</code> returns true if the given ArrayList contains the given search String
     *                                returns false otherwise
     * @exception NullArgumentException if an error occurs because at least one of the given <code>ArrayList</code>
     * names or <code>String</code> toFind was null
     */
    public static boolean find(ArrayList names, String toFind) throws NullArgumentException{
	if(names == null || toFind == null){
	    throw new NullArgumentException("At least one of the given names or toFind is null\nnames="+names+"\ntoFind="+toFind);
	}
	for(int i = 0; i < names.size(); i++){
	    if(((String)names.get(i)).equals(toFind)){
		return true;
	    }
	}
	return false;
    }
    /**
     * The <code>find</code> method searches for a given String in a given HashSet of Strings
     *
     * @param names a <code>HashSet</code> corresponding to the given HashSet of Strings
     * @param toFind a <code>String</code> corresponding to the search String
     * @return a <code>boolean</code> returns true if the given HashSet contains the given search String
     *                                returns false otherwise
     * @exception NullArgumentException if an error occurs because at least one of the given <code>HashSet</code>
     * names or <code>String</code> toFind was null
     */
    public static boolean find(HashSet names, String toFind) throws NullArgumentException{
	if(names == null || toFind == null){
	    throw new NullArgumentException("At least one of the given subsetOfKeys or toFind is null\nnames="+names+"\ntoFind="+toFind);
	}
	Iterator it = names.iterator();
	while(it.hasNext()){
	    if(((String)it.next()).equals(toFind)){
		return true;
	    }
	}
	return false;
    }
    /**
     * The <code>writeHashMapToPrintWriter</code> method writes a given HashMap to a given PrintWriter
     *
     * @param hm a <code>HashMap</code> containing keys associated with values
     * @param pw a <code>PrintWriter</code> where to write the HashMap in the format: key \t value
     * @exception NullArgumentException if an error occurs because the given <code>HashMap</code>hm was null
     */
    public static void writeHashMapToPrintWriter(HashMap hm, PrintWriter pw) throws NullArgumentException{
	if(hm == null){
	    throw new NullArgumentException("Given HashMap is null!");
	}
	try {
	    Set keys = hm.keySet();
	    Iterator it = keys.iterator();
	    String key = null;
	    while(it.hasNext()){
		key = (String) it.next();
		pw.println(key+"\t"+hm.get(key));
	    }
	    pw.close();
	}
	catch(Exception e){
	    System.out.println(e);
	}
    }
    /**
     * The <code>writeHashMap</code> method writes a given HashMap to the standard output
     *
     * @param hm a <code>HashMap</code> containing keys associated with values
     * @exception NullArgumentException if an error occurs because the given <code>HashMap</code>hm was null
     */
    public static void writeHashMap(HashMap hm) throws NullArgumentException{
	if(hm == null){
	    throw new NullArgumentException("Given HashMap is null!");
	}
	writeHashMapToPrintWriter(hm, new PrintWriter(System.out, true));
    }
    /**
     * The <code>writeHashMapToFile</code> method writes a given HashMap to a given file
     *
     * @param hm a <code>HashMap</code> containing keys associated with values
     * @param fileName a <code>String</code> corresponding to the name of the file where to write the HashMap     
     * in the format key \t value
     * @exception NullArgumentException if an error occurs because the given <code>HashMap</code>hm was null
     */
    public static void writeHashMapToFile(HashMap hm, String fileName) throws NullArgumentException {
	if(hm == null){
	    throw new NullArgumentException("Given HashMap is null!");
	}
	try {
	    FileOutputStream fos = new FileOutputStream(fileName, true);
	    PrintWriter pw = new PrintWriter(fos, true);
	    writeHashMapToPrintWriter(hm, pw);
	}
	catch(IOException ioe){
	    System.out.println(ioe);
	}
    }
    /**
     * The <code>writeStrToFile</code> method writes a given String to a given file
     *
     * @param toWrite a <code>String</code> corresponding to the String to write
     * @param fileName a <code>String</code> corresponding to the name of the file where to write the given String
     * @exception NullArgumentException if an error occurs because the given <code>String</code>toWrite was null
     */
    public static void writeStrToFile(String toWrite, String fileName) throws NullArgumentException{
	if(toWrite == null){
	    throw new NullArgumentException("Given String toWrite is null!");
	}
	try {
	    FileOutputStream fos = new FileOutputStream(fileName);
	    PrintWriter pw = new PrintWriter(fos, true);
	    pw.println(toWrite);
	    pw.close();
	    fos.close();
	}
	catch(IOException ioe){
	    System.out.println(ioe);
	}
    }
    /**
     * The <code>dec2bin</code> method returns a bit string representation of a given decimal number 
     * in a given number of bits
     *
     * @param dec an <code>int</code> corresponding to a decimal number
     * @param numBits an <code>int</code> corresponding to the number of bits in which to encode the given decimal number 
     * @return a <code>String</code> which is a bit string representation of the given decimal number
     * in the given number of bits with leading zeros if necessary
     */
    public static String dec2bin(int dec, int numBits){	
	String decStr = Integer.toBinaryString(dec);
	while(decStr.length() < numBits){
	    decStr = "0"+decStr;
	}
	return decStr;
    }
     
    /**
     * The <code>contains</code> method searches for a given Integer in a given ArrayList of Integers
     *
     * @param al an <code>ArrayList</code> corresponding to a list of Integers
     * @param u an <code>Integer</code> corresponding to a given Integer to search for
     * @return a <code>boolean</code> returns true if the given Integer was found in the given ArrayList 
     *                                returns false otherwise
     * @exception NullArgumentException if an error occurs because at least one of the given <code>ArrayList</code> 
     * al or <code>Integer</code> u was null
     */
    public static boolean contains(ArrayList al, Integer u) throws NullArgumentException{
	if(al == null || u == null){
	    throw new NullArgumentException("At least one of the given ArrayList al or Integer u is null\nal="+al+"\nu="+u);
	}
	for(int i = 0; i < al.size(); i++){
	    if(((Integer)al.get(i)).equals(u)){
		return true;
	    }
	}
	return false;
    }
    /**
     * The <code>getUniqueSymbols</code> method returns the unique values from a given HashMap
     *
     * @param gbSymbols a <code>HashMap</code> corresponding to the given HashMap where values are Strings
     * @return a <code>HashSet</code> containing the set of unique String values in the given HashMap
     * @exception NullArgumentException if an error occurs because the given <code>HashMap</code> gbSymbols was null
     */
    public static HashSet getUniqueSymbols(HashMap gbSymbols) throws NullArgumentException{
	if(gbSymbols == null){
	    throw new NullArgumentException("Given HashMap gbSymbols was null!");
	}
	HashSet uniqueSymbols = new HashSet();
	Set keySet = gbSymbols.keySet();
	Iterator it = keySet.iterator();
	while(it.hasNext()){
	    uniqueSymbols.add((String)gbSymbols.get((String) it.next()));
	}
	return uniqueSymbols;
    }
    /**
     * The <code>StringToHashSet</code> method takes in a String containing a comma-delimited elements 
     * and returns a HashSet representation of this String.
     *
     * @param s a <code>String</code> corresponding to comma-delimited elements
     * @return a <code>HashSet</code> corresponding to the HashSet representation of the given comma-delimited elements
     */
    public static HashSet StringToHashSet(String s){
	if(s == null){
	    return null;
	}
	else {
	    s = s.trim();
	    HashSet hs = new HashSet();
	    String[] tokens = s.split(",");
	    for(int i = 0; i < tokens.length; i++){
		tokens[i] = tokens[i].trim();
		hs.add(tokens[i]);
	    }
	    return hs;
	}
    }
    /**
     * The <code>HashSetToString</code> method takes in a HashSet of elements 
     * and returns a comma-delimited String representation of the elements in the given HashSet.
     *
     * @param hs a <code>HashSet</code> of elements 
     * @return a <code>String</code> corresponding to the comma-delimited String representation of 
     * the elements in the given HashSet.
     */
    public static String HashSetToString(HashSet hs){
	if(hs == null || hs.size() == 0){
	    return null;
	}
	else {
	    String s = "";
	    Iterator it = hs.iterator();
	    while(it.hasNext()){
		s += (String) it.next()+",";
	    }
	    return s.substring(0,s.length()-1);
	}       
    }
    /**
     * Raktim - Modified
     * To Generate a Unique for a File Name based on a Time Stamp.
     * @return
     */
    public static String getUniqueFileID() {
		Date now = new Date();
        String dateString = now.toString();

        SimpleDateFormat formatDt = new SimpleDateFormat("MMM_dd_yy_HH_mm_ss_SSS");
        dateString = formatDt.format(now);
        //System.out.println(" 2. " + dateString);
		return dateString;
	}
    
    public static String getWekaArgs(String path, String outArffFileName, String sAlgorithm, boolean useArc, String numParents, String sType, int kfolds) {
    	
    	String arguments = "-t " + path + outArffFileName + " -c 1 -x " + kfolds + " -Q weka.classifiers.bayes.net.search.local."+sAlgorithm+" -- ";
        if(useArc){
        	arguments +="-R";
        }
        arguments +=" -P "+numParents+" -S "+sType;
        while(!BNGUI.done){
        	try{
        		Thread.sleep(10000);	
        	}catch(InterruptedException x){
        		//ignore;
        	}
        }
        if(BNGUI.prior){     
        	arguments += " -X " + path+ "resultBif.xml";
        	//System.out.print("my prior");
        }
        arguments += " -E weka.classifiers.bayes.net.estimate.SimpleEstimator -- -A 0.5";
        return arguments;
    }
    
public static String getWekaArgsWithCPTs(String path, String outArffFileName, String sAlgorithm, boolean useArc, String numParents, String sType, int kfolds) {
    	
    	String arguments = "-t " + path + outArffFileName + " -c 1 -x " + kfolds + " -Q weka.classifiers.bayes.net.search.local."+sAlgorithm+" -- ";
        if(useArc){
        	arguments +="-R";
        }
        arguments +=" -P "+numParents+" -S "+sType;
        while(!BNGUI.done){
        	try{
        		Thread.sleep(10000);	
        	}catch(InterruptedException x){
        		//ignore;
        	}
        }
        if(BNGUI.prior){     
        	arguments += " -X " + path+ "resultBif.xml";
        	//System.out.print("my prior");
        }
        arguments += " -E weka.classifiers.bayes.net.estimate.BMAEstimator -- -A 0.5";
        return arguments;
    }
    
}









