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
package org.tigr.microarray.mev.cluster.gui.impl.bn;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;

import org.tigr.microarray.mev.annotation.MevAnnotation;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.impl.bn.NullArgumentException;
import org.tigr.microarray.mev.cluster.gui.impl.bn.Useful;

/**
 * The class <code>UsefulInteractions</code> contains useful methods on interactions: reading/writing from/to files, 
 * getting nodes, checking for elements, etc...
 *
 * @author <a href="mailto:amira@jimmy.harvard.edu"></a>
 */
public class UsefulInteractions {
	/**
	 * The <code>readInteractions</code> method takes in a String corresponding to the name of the file containing
	 * interactions in SIF format and returns the interactions in an ArrayList of <code>SimpleGeneEdge</code> objects.
	 *
	 * @param fileName a <code>String</code> corresponding to the name of the file containing interactions 
	 * in the SIF cytoscape format: "node1 pp node2"
	 * @return an <code>ArrayList</code> of <code>SimpleGeneEdge</code> objects corresponding to the interactions
	 * contained in the file denoted by the given file name.
	 * @exception FileNotFoundException if an error occurs because the file denoted by the given fileName was not found
	 */
	public static ArrayList readInteractions(String fileName) throws FileNotFoundException{
		//Useful.checkFile(fileName);
		try {
			ArrayList inter = new ArrayList();
			FileReader fr = new FileReader(fileName);
			LineNumberReader lnr = new LineNumberReader(fr);
			String s = null;
			String[] tokens = null;
			while((s = lnr.readLine())!=null){
				s = s.trim();
				tokens = s.split(" pp ");
				if(tokens.length == 2){
					//System.out.println("readInteractions PPI " + tokens[0] + " " + tokens[1]);	
					inter.add(new SimpleGeneEdge(tokens[0], tokens[1]));
					inter.add(new SimpleGeneEdge(tokens[1], tokens[0]));
				}
			}
			lnr.close();
			fr.close();
			return inter;
		}
		catch(IOException ioe){
			System.out.println(ioe);
		}
		return null;
	}

	/**
	 * To Craete SimpleGeneEdge Objects from string encoded edges
	 * @param edges
	 * @return
	 * @throws Exception
	 */
	public static ArrayList readInteractions(Vector edges) throws Exception {
		//Useful.checkFile(fileName);
		try {
			ArrayList inter = new ArrayList();
			String s = null;
			String[] tokens = null;
			Enumeration enumerate = edges.elements();
			while(enumerate.hasMoreElements()){
				s = (String)enumerate.nextElement();
				//System.out.println("readInteractions from Vector: " + s);
				String splitOn = " pd ";
				if (s.contains(" pp ")) splitOn = " pp ";
				if (s.contains(" pd ")) splitOn = " pd ";
				if (s.contains(" - ")) splitOn = " - "; 
				tokens = s.split(splitOn);
				if(tokens.length == 2){
					//System.out.println(tokens[0]);	
					inter.add(new SimpleGeneEdge(tokens[0], tokens[1]));
					//inter.add(new SimpleGeneEdge(tokens[1], tokens[0]));
				}
			}
			return inter;
		}
		catch(Exception ioe){
			//System.out.println(ioe);
			throw ioe;
		}
	}
	/**
	 * The <code>readInteractionsWithWeights</code> method takes in a String corresponding to the name of the file
	 * containing interactions in a modified SIF format to contain weights and returns the interactions 
	 * in an ArrayList of <code>SimpleGeneEdge</code> objects.
	 *
	 * @param fileName a <code>String</code> corresponding to the name of the file containing interactions 
	 * in a modified SIF cytoscape format: "node1 (pp) node2 = weight" where the weight is a double. 
	 * Example: "node1 (pp) node2 = 1.0". This format is used for edgeAttributes files in Cytoscape
	 * except the initial comment line.
	 * @return an <code>ArrayList</code> of <code>SimpleGeneEdge</code> objects corresponding
	 * to the interactions with weights contained in the file denoted by the given file name.
	 * @exception FileNotFoundException if an error occurs because the file denoted by the given fileName was not found
	 */
	public static ArrayList readInteractionsWithWeights(String fileName) throws FileNotFoundException {
		//System.out.println("readInteractionsWithWeights(), File: " + fileName);
		Useful.checkFile(fileName);
		try {
			ArrayList inter = new ArrayList();
			FileReader fr = new FileReader(fileName);
			LineNumberReader lnr = new LineNumberReader(fr);
			String s = null;
			String[] subTokens = null;
			int index = -1;
			while((s = lnr.readLine()) != null){
				s = s.trim();
				//System.out.println("Line: " + s);
				index = s.indexOf(" (pp) ");
				if(index == -1){
					continue;
				}
				subTokens = s.substring(index+5, s.length()).trim().split(" = ");
				if(subTokens.length == 2){
					inter.add(new SimpleGeneEdge(s.substring(0,index).trim(), subTokens[0].trim(), (new Double(subTokens[1].trim())).doubleValue()));
					inter.add(new SimpleGeneEdge(subTokens[0].trim(), s.substring(0,index).trim(), (new Double(subTokens[1].trim())).doubleValue()));
				}
			}
			lnr.close();
			fr.close();
			return inter;
		}
		catch(IOException ioe){
			System.out.println(ioe);
		}
		return null;
	}
	/**
	 * The <code>containsEitherWay</code> method checks whether a given undirected <code>SimpleGeneEdge</code> object 
	 * is contained in an ArrayList of <code>SimpleGeneEdge</code> objects
	 *
	 * @param inter an <code>ArrayList</code> of <code>SimpleGeneEdge</code> objects corresponding to interactions
	 * @param sGE a <code>SimpleGeneEdge</code> corresponding to the interaction to look for
	 * @return a <code>boolean</code> true if the given undirected <code>SimpleGeneEdge</code>
	 * is found in the given ArrayList of <code>SimpleGeneEdge</code> objects, false otherwise.
	 * @exception NullArgumentException if an error occurs because at least one of the given
	 * <code>ArrayList</code> inter or the <code>SimpleGeneEdge</code> sGE was null
	 */
	public static boolean containsEitherWay(ArrayList inter, SimpleGeneEdge sGE) throws NullArgumentException{
		if(inter == null || sGE == null){
			throw new NullArgumentException("At least one of the parameters inter or sGE was null\ninter="+inter+"\nsGE="+sGE);
		}
		if(inter.size() == 0){
			return false;
		}
		SimpleGeneEdge sGEi = null;
		String from = sGE.getFrom();
		String to = sGE.getTo();
		// no self-loops
		if(from.equals(to)){
			return true;
		}	
		String from_i = null;
		String to_i = null;
		for(int i = 0; i < inter.size(); i++){
			sGEi = (SimpleGeneEdge) inter.get(i);
			from_i = sGEi.getFrom();
			to_i = sGEi.getTo();
			if(from_i.equals(from) && to_i.equals(to)){
				return true;
			}
			if(to_i.equals(from) && from_i.equals(to)){
				return true;
			}
		}
		return false;
	}	    
	/**
	 * The <code>containsOneWay</code> method checks whether a given directed <code>SimpleGeneEdge</code> object
	 * is contained in an ArrayList of <code>SimpleGeneEdge</code> objects
	 *
	 * @param inter an <code>ArrayList</code> of <code>SimpleGeneEdge</code> objects corresponding to interactions
	 * @param sGE a <code>SimpleGeneEdge</code> corresponding to the interaction to look for
	 * @return a <code>boolean</code> true if the given directed <code>SimpleGeneEdge</code> is found
	 * in the given ArrayList of <code>SimpleGeneEdge</code> objects, false otherwise.
	 * @exception NullArgumentException if an error occurs because at least one of the given <code>ArrayList</code>
	 * inter or the <code>SimpleGeneEdge</code> sGE was null
	 */
	public static boolean containsOneWay(ArrayList inter, SimpleGeneEdge sGE) throws NullArgumentException{
		if(inter == null || sGE == null){
			throw new NullArgumentException("At least one of the parameters inter or sGE was null\ninter="+inter+"\nsGE="+sGE);
		}
		SimpleGeneEdge sGEi = null;
		String from = sGE.getFrom();
		String to = sGE.getTo();
		String from_i = null;
		String to_i = null;
		for(int i = 0; i < inter.size(); i++){
			sGEi = (SimpleGeneEdge) inter.get(i);
			from_i = sGEi.getFrom();
			to_i = sGEi.getTo();
			if(from_i.equals(from) && to_i.equals(to)){
				return true;
			}
		}
		return false;
	}	    
	/**
	 * The <code>readDirectedInteractions</code> method takes in a String corresponding to the name of the file 
	 * containing directed interactions in SIF format and returns the directed interactions in an ArrayList
	 * of <code>SimpleGeneEdge</code> objects.
	 *
	 * @param fileName a <code>String</code> corresponding to the name of the file containing interactions
	 * in the SIF cytoscape format: "node1 pd node2" such that the interaction is from node1 to node2.
	 * @return an <code>ArrayList</code> of <code>SimpleGeneEdge</code> objects corresponding to 
	 * the interactions contained in the file denoted by the given file name.
	 * @exception FileNotFoundException if an error occurs because the file denoted by the given fileName was not found
	 */
	public static ArrayList readDirectedInteractions(String fileName) throws FileNotFoundException {
		//System.out.println("readDirectedInteractions()" + fileName);
		Useful.checkFile(fileName);
		try {
			ArrayList inter = new ArrayList();
			FileReader fr = new FileReader(fileName);
			LineNumberReader lnr = new LineNumberReader(fr);
			String s = null;
			String[] tokens = null;
			while((s = lnr.readLine())!=null){
				s = s.trim();
				tokens = s.split(" pd ");
				if(tokens.length == 2){
					inter.add(new SimpleGeneEdge(tokens[0], tokens[1]));
				}
			}
			lnr.close();
			fr.close();
			return inter;
		}
		catch(IOException ioe){
			System.out.println(ioe);
		}
		return null;
	}
	/**
	 * The <code>readDirectedInteractionsWithWeights</code> method method takes in a String corresponding to
	 * the name of the file containing directed interactions in a modified SIF format to contain weights 
	 * and returns the directed interactions in an ArrayList of <code>SimpleGeneEdge</code> objects.
	 *
	 * @param fileName a <code>String</code> corresponding to the name of the file containing interactions 
	 * in a modified SIF cytoscape format: "node1 (pd) node2 = weight" where the weight is a double.
	 * Example: "node1 (pd) node2 = 1.0" such that the interaction is from node1 to node2. 
	 * This format is used for edgeAttributes files in Cytoscape except the initial comment line.
	 * @return an <code>ArrayList</code> of <code>SimpleGeneEdge</code> objects corresponding to
	 * the interactions with weights contained in the file denoted by the given file name.
	 * @exception FileNotFoundException if an error occurs because the file denoted by the given fileName was not found
	 */
	public static ArrayList readDirectedInteractionsWithWeights(String fileName) throws FileNotFoundException {
		//System.out.println("readDirectedInteractionsWithWeights()" + fileName);
		Useful.checkFile(fileName);
		try {
			ArrayList inter = new ArrayList();
			FileReader fr = new FileReader(fileName);
			LineNumberReader lnr = new LineNumberReader(fr);
			String s = null;
			String[] subTokens = null;
			int index = -1;
			while((s = lnr.readLine())!=null){
				s = s.trim();
				index = s.indexOf(" (pd) ");
				if(index == -1){
					continue;
				}
				subTokens = s.substring(index+5, s.length()).trim().split(" = ");
				if(subTokens.length == 2){
					inter.add(new SimpleGeneEdge(s.substring(0,index).trim(), subTokens[0].trim(), (new Double(subTokens[1].trim())).doubleValue()));
				}
			}
			lnr.close();
			fr.close();
			return inter;
		}
		catch(IOException ioe){
			System.out.println(ioe);
		}
		return null;
	}
	/**
	 * The <code>writeSifFile</code> method writes a given ArrayList of interactions to file.
	 *
	 * @param inter an <code>ArrayList</code> of <code>SimpleGeneEdge</code> objects corresponding to
	 * directed unweighted interactions.
	 * @param fileName a <code>String</code> denoting the name of the output file where the interactions 
	 * are written in the SIF Cytosape format: "node1 pd node2" meaning an interaction from node1 to node2.
	 * @exception NullArgumentException if an error occurs because the given <code>ArrayList</code> inter was null
	 */
	public static void writeSifFile(ArrayList inter, String fileName) throws NullArgumentException {
		try {
			if(inter == null){
				throw new NullArgumentException("Given inter was null!");
			}
			String path = BNConstants.getBaseFileLocation() + BNConstants.SEP + BNConstants.RESULT_DIR + BNConstants.SEP;
			FileOutputStream fos = new FileOutputStream(path + fileName);
			PrintWriter pw = new PrintWriter(fos, true);
			SimpleGeneEdge sGE = null;
			for(int i = 0; i < inter.size(); i++){
				sGE = (SimpleGeneEdge) inter.get(i);
				pw.println(sGE.getFrom() + " pd " + sGE.getTo());
			}
		}
		catch(IOException ioe){
			System.out.println(ioe);
		}
	}

	/**
	 * Writes a network in sif file format
	 * @param inter
	 * @param fileName
	 * @throws NullArgumentException
	 */
	public static void writeSifFileUndir(ArrayList inter, String fileName) throws NullArgumentException {
		FileOutputStream fos = null;
		//String path=System.getProperty("user.dir");
		//String sep=System.getProperty("file.separator");
		//path=path+sep+"data"+sep+"bn"+sep; //Raktim - Old Way
		//path=path+sep+"data"+sep+"bn"+sep+BNConstants.RESULT_DIR+sep;
		//path = BNConstants.getBaseFileLocation() + BNConstants.SEP + BNConstants.RESULT_DIR + BNConstants.SEP;
		try {
			if(inter == null){
				//System.out.println("UsefulInteractions-writeSif");  
				throw new NullArgumentException("Given inter was null!");
			}

			//fos = new FileOutputStream(path + fileName);
			fos = new FileOutputStream(fileName);
			PrintWriter pw = new PrintWriter(fos, true);
			SimpleGeneEdge sGE = null;
			for(int i = 0; i < inter.size(); i++){
				sGE = (SimpleGeneEdge) inter.get(i);
				//System.out.println("UsefulInteractions-writeSif"+inter.size());
				pw.println(sGE.getFrom() + " pp " + sGE.getTo());
			}
		}
		catch(IOException ioe){
			System.out.println(ioe);
		}
	}
	/**
	 * The <code>writeSifFileWithWeights</code> method writes a given ArrayList of interactions to file.
	 *
	 * @param inter an <code>ArrayList</code> of <code>SimpleGeneEdge</code> objects corresponding to directed 
	 * weighted interactions.
	 * @param fileName a <code>String</code> denoting the name of the output file where the interactions 
	 * are written in the modified SIF Cytoscape format: "node1 (pd) node2 = weight" meaning an interaction 
	 * from node1 to node2 with the given weight as a double. Example: "node1 (pd) node2 = 1.0". 
	 * This is the format used in Cytoscape edgeAttributes file except the initial comment line.
	 * @exception NullArgumentException if an error occurs because the given <code>ArrayList</code> inter was null
	 */
	public static void writeSifFileWithWeights(ArrayList inter, String fileName) throws NullArgumentException {
		String path=System.getProperty("user.dir");
		String sep=System.getProperty("file.separator");
		//path=path+sep+"data"+sep+"bn"+sep;//Raktim - Old Way
		//path=path+sep+"data"+sep+"bn"+sep+BNConstants.RESULT_DIR+sep;
		path = BNConstants.getBaseFileLocation() + BNConstants.SEP + BNConstants.RESULT_DIR + BNConstants.SEP;
		try {
			if(inter == null){
				throw new NullArgumentException ("Given inter was null!");
			}
			FileOutputStream fos = new FileOutputStream(path+fileName);
			PrintWriter pw = new PrintWriter(fos, true);
			SimpleGeneEdge sGE = null;
			for(int i = 0; i < inter.size(); i++){
				sGE = (SimpleGeneEdge) inter.get(i);
				pw.println(sGE.getFrom()+" (pd) "+sGE.getTo()+" = "+sGE.getWeight());
			}
		}
		catch(IOException ioe){
			System.out.println(ioe);
		}
	}

	/**
	 * The <code>writeSifFileUndirWithWeights</code> method writes a given ArrayList of interactions to file.
	 *
	 * @param inter an <code>ArrayList</code> of <code>SimpleGeneEdge</code> objects corresponding to
	 * undirected weighted interactions.
	 * @param fileName a <code>String</code> denoting the name of the output file where the interactions 
	 * are written in the modified SIF Cytosape format: "node1 (pp) node2 = weight" with the given weight as a double.
	 * Example: "node1 (pp) node2 = 1.0". This is the format used in Cytoscape edgeAttributes file except
	 * the initial comment line.
	 * @exception NullArgumentException if an error occurs because the given <code>ArrayList</code> inter was null
	 */

	public static void writeSifFileUndirWithWeights(ArrayList inter, String fileName) throws NullArgumentException{
		FileOutputStream fos=null;
		String path=System.getProperty("user.dir");
		String sep=System.getProperty("file.separator");
		//path=path+sep+"data"+sep+"bn"+sep;//Raktim - Old Way
		//path=path+sep+"data"+sep+"bn"+sep+"tmp"+sep;
		path = BNConstants.getBaseFileLocation() + BNConstants.SEP + BNConstants.TMP_DIR + BNConstants.SEP;
		try {
			if(inter == null){
				throw new NullArgumentException("Given inter was null!");
			}
			fos = new FileOutputStream(path+fileName);
			PrintWriter pw = new PrintWriter(fos, true);
			SimpleGeneEdge sGE = null;
			for(int i = 0; i < inter.size(); i++){
				sGE = (SimpleGeneEdge) inter.get(i);
				pw.println(sGE.getFrom()+" (pp) "+ sGE.getTo()+ " = "+sGE.getWeight());
			}
		}
		catch(IOException ioe){
			System.out.println(ioe);
		}
	}
	/*
    public static void writeSifFileUndirWithWeights(ArrayList inter, String fileName) throws NullArgumentException{
    	FileOutputStream fos=null;
    	try {
	    if(inter == null){
		throw new NullArgumentException("Given inter was null!");
	    }
	    fos = new FileOutputStream(fileName);
	    PrintWriter pw = new PrintWriter(fos, true);
	    SimpleGeneEdge sGE = null;
	    for(int i = 0; i < inter.size(); i++){
		sGE = (SimpleGeneEdge) inter.get(i);
		pw.println(sGE.getFrom()+" (pp) "+ sGE.getTo()+ " = "+sGE.getWeight());
	    }
	}
	catch(IOException ioe){
	    System.out.println(ioe);
	}
    }
	 */

	/**
	 * 
	 */
	public static void writeXgmmlFileUndir(ArrayList<SimpleGeneEdge> inter, String fileName, HashMap probeIndexAssocHash, IData data) throws NullArgumentException, IOException {
		FromWekaToSif.fromSimpleGeneEdgeToXgmml(false, inter, fileName, probeIndexAssocHash, data);
	}


	/**
	 * The <code>createAdjMatrix</code> method takes in an <code>ArrayList</code> of <code>SimpleGeneEdge</code>objects
	 * corresponding to interactions and returns a 2D array of ints corresponding to the adjacency matrix representation
	 * of the given graph as a set of interactions
	 *
	 * @param inter an <code>ArrayList</code> of <code>SimpleGeneEdge</code>objects corresponding to interactions 
	 * @return an <code>int[][]</code> corresponding to the adjacency matrix representation of the given graph
	 * as a set of interactions
	 * @exception NullArgumentException if an error occurs because the given <code>ArrayList</code> inter was null
	 */
	public static int[][] createAdjMatrix(ArrayList inter) throws NullArgumentException{
		if(inter == null){
			throw new NullArgumentException("Given inter was null!");
		}
		ArrayList nodes = getNodes(inter);
		int numNodes = nodes.size();
		int[][] adjMatrix = new int[numNodes][numNodes];
		for(int i = 0; i < numNodes; i++){
			for(int j = 0; j < numNodes;j++){
				if(containsOneWay(inter, new SimpleGeneEdge((String)nodes.get(i), (String)nodes.get(j)))){
					adjMatrix[i][j] = 1;
				}
				else {
					adjMatrix[i][j] = 0;
				}
			}
		}
		return adjMatrix;
	}

	/**
	 * The <code>printAdjMatrix</code> method prints the given adjacency matrix to screen
	 *
	 * @param adjMatrix an <code>int[][]</code> corresponding to the given adjacency matrix representation of a graph
	 * @param numNodes an <code>int</code> corresponding to the given number of nodes in the graph
	 */
	public static void printAdjMatrix(int[][] adjMatrix, int numNodes) {
		if(adjMatrix == null){
			System.out.println("null");
		}
		else {
			for(int i = 0; i < numNodes; i++){
				for(int j = 0; j < numNodes;j++){
					System.out.print(adjMatrix[i][j]+"\t");
				}
				System.out.println();
			}
			System.out.println();
		}
	}

	/**
	 * The <code>createWeightMatrix</code> method takes in an <code>ArrayList</code> of <code>SimpleGeneEdge</code>objects
	 * corresponding to interactions and returns a 2D array of doubles corresponding to the weight matrix representation
	 * of the given graph as a set of interactions
	 *
	 * @param inter an <code>ArrayList</code> of <code>SimpleGeneEdge</code>objects corresponding to interactions 
	 * @return an <code>double[][]</code> corresponding to the adjacency matrix representation of the given graph
	 * as a set of interactions
	 * @exception NullArgumentException if an error occurs because the given <code>ArrayList</code> inter was null
	 */
	public static double[][] createWeightMatrix(ArrayList inter) throws NullArgumentException{
		if(inter == null){
			throw new NullArgumentException("Given inter was null!");
		}
		ArrayList nodes = getNodes(inter);
		int numNodes = nodes.size();
		double[][] weightMatrix = new double[numNodes][numNodes];
		SimpleGeneEdge sGE = null;
		for(int i = 0; i < numNodes; i++){
			for(int j = 0; j < numNodes;j++){
				sGE = new SimpleGeneEdge((String)nodes.get(i), (String)nodes.get(j));
				if(containsOneWay(inter, sGE)){
					weightMatrix[i][j] = sGE.getWeight();
				}
				else {
					weightMatrix[i][j] = java.lang.Double.MAX_VALUE;
				}
			}
		}
		return weightMatrix;
	}

	/**
	 * The <code>printWeightMatrix</code> method prints the given adjacency matrix to screen
	 *
	 * @param weightMatrix an <code>double[][]</code> corresponding to the given adjacency matrix representation of a graph
	 * @param numNodes an <code>int</code> corresponding to the given number of nodes in the graph
	 */
	public static void printWeightMatrix(double[][] weightMatrix, int numNodes) {
		if(weightMatrix == null){
			System.out.println("null");
		}
		else {
			for(int i = 0; i < numNodes; i++){
				for(int j = 0; j < numNodes;j++){
					if(weightMatrix[i][j] == java.lang.Double.MAX_VALUE){
						System.out.print("inf\t");
					}
					else {
						System.out.print(weightMatrix[i][j]+"\t");
					}
				}
				System.out.println();
			}
			System.out.println();
		}
	}


	/**
	 * The <code>getNodes</code> method returns the unique set of nodes found in the given ArrayList
	 * of <code>SimpleGeneEdge</code> objects representing interactions
	 *
	 * @param inter an <code>ArrayList</code> of <code>SimpleGeneEdge</code> objects representing interactions 
	 * @return an <code>ArrayList</code> of unique set of nodes found in the given ArrayList of interactions
	 * @exception NullArgumentException if an error occurs because the given <code>ArrayList</code> inter was null
	 */
	public static ArrayList getNodes(ArrayList inter) throws NullArgumentException{
		if(inter == null){
			throw new NullArgumentException("Given inter was null!");
		}
		HashSet nodes = new HashSet();
		SimpleGeneEdge sGE = null;
		for(int i = 0; i < inter.size(); i++){
			sGE = (SimpleGeneEdge) inter.get(i);
			nodes.add(sGE.getFrom());
			nodes.add(sGE.getTo());
		}
		ArrayList result = new ArrayList();
		result.addAll(nodes);
		return result;
	}

	/**
	 * The <code>getSubsetInteractionsGivenNodesOnlyWithin</code> method is given a set of interactions 
	 * and a set of queryNodes and returns the subset of interactions for which each interaction
	 * has both end nodes contained in the set of queryNodes
	 *
	 * @param inter an <code>ArrayList</code> of <code>SimpleGeneEdge</code> objects representing interactions 
	 * @param queryNodes an <code>ArrayList</code> of <code>String</code>s reprepresenting nodes
	 * @return an <code>ArrayList</code> of  <code>SimpleGeneEdge</code> objects corresponding to
	 * the subset of interactions for which each interaction has both end nodes contained in the set of queryNodes
	 * @exception NullArgumentException if an error occurs because at least one of the given <code>ArrayList</code>s 
	 * inter or queryNodes was null
	 */
	public static ArrayList getSubsetInteractionsGivenNodesOnlyWithin(ArrayList inter, ArrayList queryNodes) throws NullArgumentException {	
		if(inter == null || queryNodes == null){
			throw new NullArgumentException("At least one of given inter or queryNodes was null!\ninter="+inter+"\nqueryNodes="+queryNodes);
		}
		ArrayList result = new ArrayList();
		SimpleGeneEdge sGE = null;
		for(int i = 0; i < inter.size(); i++){
			//System.out.println("getSut"+inter.size());
			sGE = (SimpleGeneEdge)inter.get(i);
			if(!sGE.getFrom().equals(sGE.getTo())) { //To prevent self loops
				if(queryNodes.contains(sGE.getFrom())&& queryNodes.contains(sGE.getTo())){
					if(!containsEitherWay(result,sGE)){
						//System.out.println("PPI getSubsetInteractionsGivenNodesOnlyWithin: " + sGE.toString());
						result.add(sGE);
					}
				}
			}
		}
		return result;
	}

	/**
	 * The <code>getSubsetInteractionsGivenNodes</code> method is given a set of interactions and a set of queryNodes
	 * and returns the subset of interactions for which each interaction has at least one end node in the set of queryNodes
	 *
	 * @param inter an <code>ArrayList</code> of <code>SimpleGeneEdge</code> objects representing interactions 
	 * @param queryNodes an <code>ArrayList</code> of <code>String</code>s reprepresenting nodes
	 * @return an <code>ArrayList</code> of  <code>SimpleGeneEdge</code> objects corresponding to the subset of
	 * interactions for which each interaction has at least one end node is contained in the set of queryNodes
	 * @exception NullArgumentException if an error occurs because at least one of the given <code>ArrayList</code>s
	 * inter or queryNodes was null
	 */
	public static ArrayList getSubsetInteractionsGivenNodes(ArrayList inter, ArrayList queryNodes) throws NullArgumentException {
		if(inter == null || queryNodes == null){
			throw new NullArgumentException("At least one of given inter or queryNodes was null!\ninter="+inter+"\nqueryNodes="+queryNodes);
		}
		ArrayList result = new ArrayList();
		SimpleGeneEdge sGE = null;
		for(int i = 0; i < inter.size(); i++){
			sGE = (SimpleGeneEdge)inter.get(i);
			if(queryNodes.contains(sGE.getFrom())|| queryNodes.contains(sGE.getTo())){
				result.add(sGE);
			}
		}
		return result;
	}
	/**
	 * The <code>getNeighbors</code> method is given a set of interactions, a set of nodes and the index of
	 * a particular node and returns the indices of the neighbors of this node in the given graph
	 *
	 * @param u an <code>int</code> corresponding to the index of a particular node in the graph
	 * @param inter an <code>ArrayList</code> of <code>SimpleGeneEdge</code> objects representation of the given graph
	 * @param nodes an <code>ArrayList</code> of <code>String</code>s corresponding to given nodes in the graph
	 * @return an <code>int[]</code> corresponding to the indices of the neighbors of the given node in the given graph
	 */
	public static int[] getNeighbors(int u, ArrayList inter, ArrayList nodes){
		SimpleGeneEdge sGE = null;
		ArrayList neighbors = new ArrayList();
		String uStr = null;
		uStr = (String) nodes.get(u);
		for(int i = 0; i < inter.size(); i++){
			sGE = (SimpleGeneEdge) inter.get(i);
			if(uStr.equals(sGE.getFrom())){
				neighbors.add(sGE.getTo());
			}
		}
		int[] result = new int[neighbors.size()];
		String oneNeighbor = null;
		for(int i = 0; i < neighbors.size(); i++){
			oneNeighbor = (String) neighbors.get(i);
			result[i] = nodes.indexOf(oneNeighbor);
		}
		return result;
	}

	public static void compareUndirectedToDirectedGraph(ArrayList undirInter, ArrayList dirInter) throws NullArgumentException {
		if(undirInter == null || dirInter == null){
			throw new NullArgumentException("At least one of given interactions was null!\nundirInter="+undirInter+"\ndirInter="+dirInter);
		}
		SimpleGeneEdge sGE = null;
		ArrayList missing = new ArrayList();
		System.out.print("Missing edges: ");
		for(int i = 0; i < undirInter.size(); i++){
			sGE = (SimpleGeneEdge) undirInter.get(i);
			if(!containsEitherWay(dirInter, sGE)){
				if(!containsEitherWay(missing, sGE)){
					missing.add(sGE); 
				}
			}
		}
		System.out.println(missing.size());
		for(int i = 0; i < missing.size(); i++){
			sGE = (SimpleGeneEdge) missing.get(i);
			System.out.println(sGE);
		}
		int e = 0;
		System.out.println("Extra edges: ");
		for(int i = 0; i < dirInter.size(); i++){
			sGE = (SimpleGeneEdge) dirInter.get(i);
			if(!containsEitherWay(undirInter, sGE)){
				System.out.println(sGE);
				e++;
			}
		}
		System.out.println("Extra edges: "+e);
	}
	/**
	 * Raktim - New function to remove reverse edges between 2 nodes (cycles) from Lit mining interaction.
	 * E.g - if there is an edge A -> B, there *cannot be an Edge B -> A to make it a DAG
	 * This function takes a list of edges and removes all removes edge B - > A if edge A -> B already exists.
	 * @param unionOfInter
	 * @return
	 */
	public static ArrayList removeReverseEdge(ArrayList unionOfInter) {
		ArrayList edges = new ArrayList();
		for(int i = 0; i < unionOfInter.size(); i++){
			SimpleGeneEdge curEdge = (SimpleGeneEdge)unionOfInter.get(i);
			String from = curEdge.getFrom();
			String to = curEdge.getTo();

			if(!to.equals("D") || !from.equals("D")){
				SimpleGeneEdge toFind = new SimpleGeneEdge(to, from);
				unionOfInter.set(i, new SimpleGeneEdge("D", "D"));
				int ind = unionOfInter.indexOf(toFind);
				if(ind != -1){
					unionOfInter.set(ind, new SimpleGeneEdge("D", "D"));
				}
				edges.add(curEdge);
			}
		}
		return edges;
	}

	public static ArrayList<SimpleGeneEdge> mergeInteractions(
			ArrayList<SimpleGeneEdge> networkSeedEdgeList,
			ArrayList<SimpleGeneEdge> lmDirEdges, boolean netSeedRules) {

		ArrayList<SimpleGeneEdge> edges = new ArrayList<SimpleGeneEdge>();
		ArrayList<SimpleGeneEdge> to_test_list, searchWithin_list;
		if(netSeedRules) {
			to_test_list = lmDirEdges;
			searchWithin_list = networkSeedEdgeList;
			edges.addAll(networkSeedEdgeList);
		} else { 
			to_test_list = networkSeedEdgeList;
			searchWithin_list = lmDirEdges;
			edges.addAll(lmDirEdges);
		}
		
		boolean add = true;
		for(int i = 0; i < to_test_list.size(); i++){
			SimpleGeneEdge curEdge = (SimpleGeneEdge)to_test_list.get(i);
			for(int ii = 0; ii < searchWithin_list.size(); ii++){
				if(curEdge.equals(searchWithin_list.get(ii)) 
						|| 
						curEdge.reverseEquals(searchWithin_list.get(ii))) {
					add = false;
					break;
				}
			}
			if(add)
				edges.add(curEdge);
		}
		return edges;
	}
}









