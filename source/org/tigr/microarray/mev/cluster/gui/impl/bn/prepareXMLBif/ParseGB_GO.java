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
/* ParseGB_GO.java
 * Copyright (C) 2005 Amira Djebbari
 */
package org.tigr.microarray.mev.cluster.gui.impl.bn.prepareXMLBif;import java.io.FileOutputStream;import java.io.FileInputStream;
import java.io.PrintWriter;
import java.io.LineNumberReader;
import java.io.FileReader;import java.io.IOException;import java.util.HashMap;import java.util.ArrayList;
import java.util.Random;import java.util.Properties;
import org.tigr.microarray.mev.cluster.gui.impl.bn.Useful;import org.tigr.microarray.mev.cluster.gui.impl.bn.NullArgumentException;
import org.tigr.microarray.mev.cluster.gui.impl.bn.UsefulInteractions;import org.tigr.microarray.mev.cluster.gui.impl.bn.SimpleGeneEdge;
import org.tigr.microarray.mev.cluster.gui.impl.bn.algs.Cyclic;
/**
 * The class <code>ParseGB_GO</code> parses GO terms associated with GenBank accessions 
 * to direct a given undirected graph such that and an edge (a,b) is directed from a to b 
 * if a contains TF as a GO term and b does not. The remaining undirected edges, if any, 
 * are randomly directed such that the resulting directed graph is acyclic.
 *
 * @author <a href="mailto:amira@jimmy.harvard.edu></a>
 */
public class ParseGB_GO {
    /**
     * The variable <code>debug</code> corresponds to a debug flag. If true, 
     * verbose information will be written to the <code>PrintWriter</code>, otherwise not.
     */
    public static boolean debug = false;
    
    /**
     * Describe variable <code>pw</code> corresponds to the <code>PrintWriter</code> where 
     * verbose information will be written if the debug flag is true. It is set to write to screen as default.
     */
    public static PrintWriter pw = new PrintWriter(System.out, true);
    
    /**
     * The <code>setDebug</code> method sets the debug flag and logFileName
     *
     * @param isDebug a <code>boolean</code> value
     */
    public static void setDebug(boolean isDebug){
	debug = isDebug;
    }
    /**
     * The <code>setDebug</code> method sets the debug flag and logFileName
     *
     * @param isDebug a <code>boolean</code> value
     * @param logFileName a <code>String</code> corresponding to the name of the desired file 
     * where verbose information will be written, if debug flag is true
     */
    public static void setDebug(boolean isDebug, String logFileName){
	try {
	    debug = isDebug;
	    if(debug) {
		pw = new PrintWriter(new FileOutputStream(logFileName), true);
	    }
	}
	catch(IOException ioe){
	    System.out.println(ioe);
	}
    }
    /**
     * The <code>readGB_GOs</code> method takes in the name of the file containing GenBank accessions 
     * and their corresponding GO terms (space separated) in tab-delimited format: GB\tGO_1 GO_2 ... GO_n 
     * and returns GenBank and corresponding GO terms key value pairs in a <code>HashMap</code> representation
     *
     * @param fileName a <code>String</code> corresponding to the name of the file containing GenBank accessions
     * and their corresponding GO terms (space separated) in tab-delimited format: GB\tGO_1 GO_2 ... GO_n
     * @return a <code>HashMap</code> representation of the GenBank and corresponding GO terms key value pairs
     * as read in the given file.
     */
    public static HashMap readGB_GOs(String fileName){
	try {		System.out.println("readGB_GOs()" + fileName);
	    Useful.checkFile(fileName);
	    HashMap hm = new HashMap();
	    FileReader fr = new FileReader(fileName);
	    LineNumberReader lnr = new LineNumberReader(fr);
	    String s = null;
	    String[] tokens = null;
	    String[] subTokens = null;
	    ArrayList gos = null;
	    while((s = lnr.readLine())!=null){
		s = s.trim();
		tokens = s.split("\t");
		if(tokens.length >= 2){
		    subTokens = tokens[1].split("GO:");
		    gos = new ArrayList(subTokens.length);
		    for(int i = 0; i < subTokens.length; i++) {
			gos.add(subTokens[i].trim());
		    }
		    hm.put(tokens[0],gos);
		}
		else {
		    hm.put(tokens[0],null);
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
     * The <code>getInteractionsFromGB_GOs</code> takes in a given graph 
     * in an <code>ArrayList</code> of <code>SimpleGeneEdge</code> objects representation 
     * and keyValuesPairs with GenBank accessions as keys and their corresponding GO terms as values
     * and returns a graph containing only edges that could be directed using GO terms
     * such that and an edge (a,b) is directed from a to b if a contains TF as a GO term and b does not.
     *
     * @param interactions an <code>ArrayList</code> of <code>SimpleGeneEdge</code> objects representation
     * of the given graph
     * @param gbGOs a <code>HashMap</code> of keyValuesPairs with GenBank accessions as keys 
     * and their corresponding GO terms as values
     * @return an <code>ArrayList</code> of <code>SimpleGeneEdge</code> objects representation 
     * of the resulting directed graph containing only edges that could be directed using GO terms 
     * such that and an edge (a,b) is directed from a to b if a contains TF as a GO term and b does not.
     * @exception NullArgumentException if an error occurs because at least one of the given parameters
     * interactions or gbGOs was null
     */
    public static ArrayList getInteractionsFromGB_GOs(ArrayList interactions, HashMap gbGOs) throws NullArgumentException{
	if(interactions == null || gbGOs == null){
	    throw new NullArgumentException("At least one of the given parameters interactions or gbGOs was null!\ninteractions="+interactions+"\ngbGOs="+gbGOs);
	}
        ArrayList newInter = new ArrayList();
	SimpleGeneEdge sGE = null;
	String from = null;
	String to = null;
	ArrayList gos = null;
	ArrayList gos2 = null;
	boolean isTFgos = false;
	boolean isTFgos2 = false;
	double weight = 0.0;
	SimpleGeneEdge newSGE = null;
	ArrayList nodes = UsefulInteractions.getNodes(interactions);
	for(int i = 0; i < interactions.size(); i++){
	    sGE = (SimpleGeneEdge) interactions.get(i);
	    from = sGE.getFrom();
	    to = sGE.getTo();
	    weight = sGE.getWeight();
	    if(debug){
		pw.println("considering edge="+sGE);
	    }
	    if(gbGOs.get(from) != null|| gbGOs.get(to)!=null){
		gos = (ArrayList) gbGOs.get(from);
		gos2 = (ArrayList) gbGOs.get(to);
		isTFgos = isTF(gos);
		if(debug){
		    pw.println("from isTF?"+isTFgos);
		}
		isTFgos2 = isTF(gos2);
		if(debug){
		    pw.println("from isTF?"+isTFgos2);
		}
		if(isTFgos&&!isTFgos2||isTFgos2&&!isTFgos){
		    newSGE = new SimpleGeneEdge(from, to, weight);
		    if(!UsefulInteractions.containsEitherWay(newInter, newSGE)){
			if(newInter.size() > (nodes.size() -1)){
			    break;
			}
			newInter.add(newSGE);
			if(Cyclic.isCyclic(newInter)){
			    newInter.remove(new SimpleGeneEdge(from, to, weight));
			}
			if(SifToXMLBifUtil.getMaxParents(newInter, nodes) > 3){
			    newInter.remove(new SimpleGeneEdge(from, to, weight));			    
			}
		    }
		}
	    }
	}
	return newInter;
    }
    
    /**
     * The <code>isTF</code> method takes in an <code>ArrayList</code> of GO terms
     *
     * @param gos an <code>ArrayList</code> of <code>String</code> corresponding to GO terms
     * @return a <code>boolean</code> true if at least one of the GO terms in the given <code>ArrayList</code>
     * contains "transcription factor", false otherwise
     */
    public static boolean isTF(ArrayList gos){
	if(gos == null){
	    return false;
	}
	String s = null;
	for(int i = 0; i < gos.size(); i++){
	    s = (String) gos.get(i);
	    if(s.indexOf("transcription factor")!= -1){	      
		return true;
	    }
	}
	return false;
    }
	
    /**
     * The <code>getInteractionsFromGB_GOsRandom</code> takes in a given undirected graph 
     * in an <code>ArrayList</code> of <code>SimpleGeneEdge</code> objects representation 
     * and a given directed graph (in this context, containing edges that could be directed using GO terms
     * such that and an edge (a,b) is directed from a to b if a contains TF as a GO term and b does not). 
     * The remaining undirected edges, if any, are randomly directed such that the resulting directed graph is acyclic.
     *
     * @param interactions an <code>ArrayList</code> of <code>SimpleGeneEdge</code> objects representation 
     * of the given undirected graph
     * @param newInter an <code>ArrayList</code> of <code>SimpleGeneEdge</code> objects representation of
     * the given directed graph (in this context, containing edges that could be directed using GO terms 
     * such that and an edge (a,b) is directed from a to b if a contains TF as a GO term and b does not). 
     * @param seed a <code>long</code> corresponding to the seed of the random number generator 
     * used to randomly direct remaining undirected edges
     * @return an <code>ArrayList</code> of <code>SimpleGeneEdge</code> objects representation 
     * of the resulting directed graph containing the edges in the given directed graph newInter.
     * The remaining undirected edges of interactions, if any, are randomly directed 
     * such that the resulting directed graph is acyclic.
     * @exception NullArgumentException if an error occurs because at least one of the given parameters 
     * interactions or newInter was null
     */
    public static ArrayList getInteractionsFromGB_GOsRandom(ArrayList interactions, ArrayList newInter, long seed) throws NullArgumentException{
	if(interactions == null || newInter == null){
	    throw new NullArgumentException("At least one of the given parameters interactions or newInter was null!\ninteractions="+interactions+"\nnewInter="+newInter);
	}
	SimpleGeneEdge sGE = null;
	Random rand = new Random(seed);
	String from = null;
	String to = null;
	double weight = 0.0;
	ArrayList nodes = UsefulInteractions.getNodes(interactions);
	int max = nodes.size() - 1;
	for(int i = 0; i < max; i++){
	    System.gc();
	    sGE = (SimpleGeneEdge) interactions.get(i);
	    from = sGE.getFrom();
	    to = sGE.getTo();
	    weight = sGE.getWeight();
	    if(!UsefulInteractions.containsEitherWay(newInter, sGE)){
		if(rand.nextBoolean()){
		    newInter.add(new SimpleGeneEdge(from, to, weight));
		    System.gc();
		    if(Cyclic.isCyclic(newInter)){
			newInter.remove(new SimpleGeneEdge(from, to, weight));
		    }
		}
		else {
		    newInter.add(new SimpleGeneEdge(to, from, weight));
		    System.gc();
		    if(Cyclic.isCyclic(newInter)){
			newInter.remove(new SimpleGeneEdge(to, from, weight));
		    }			
		}
	    }
	}
	return newInter;
    }
    /**
     * The <code>getInteractionsFromGB_GOsRandom</code> takes in a given graph in an <code>ArrayList</code>
     * of <code>SimpleGeneEdge</code> objects representation and keyValuesPairs with GenBank accessions as keys 
     * and their corresponding GO terms as values and returns a graph containing edges that could be directed 
     * using GO terms such that and an edge (a,b) is directed from a to b if a contains TF as a GO term and b does not.
     * The remaining undirected edges, if any, are randomly directed such that the resulting directed graph is acyclic.
     *
     * @param interWithWeights an <code>ArrayList</code> of <code>SimpleGeneEdge</code> objects representation 
     * of the given graph
     * @param gbGOs a <code>HashMap</code> of keyValuesPairs with GenBank accessions as keys 
     * and their corresponding GO terms as values
     * @param seed a <code>long</code> corresponding to the seed of the random number generator
     * used to randomly direct remaining undirected edges
     * @return an <code>ArrayList</code> of <code>SimpleGeneEdge</code> objects representation 
     * of the resulting directed graph containing only edges that could be directed using GO terms 
     * such that and an edge (a,b) is directed from a to b if a contains TF as a GO term and b does not. 
     * The remaining undirected edges, if any, are randomly directed such that the resulting directed graph is acyclic.
     * @exception NullArgumentException if an error occurs because at least one of the given parameters 
     * interactions or gbGOs was null
     */    
    public static ArrayList getInteractionsFromGB_GOsRandom(ArrayList interWithWeights, HashMap gbGOs, long seed) throws NullArgumentException{
	if(interWithWeights == null || gbGOs == null){
	    throw new NullArgumentException("At least one of the given parameters interactions or gbGOs was null!\ninteractions="+interWithWeights+"\ngbGOs="+gbGOs);
	}
	ArrayList gODirectedInters = ParseGB_GO.getInteractionsFromGB_GOs(interWithWeights, gbGOs);
	return getInteractionsFromGB_GOsRandom(interWithWeights, gODirectedInters, seed);
    }
	
    /**
     * The <code>test</code> method tests the <code>getInteractionsFromGB_GOs</code> 
     * and <code>getInteractionsFromGB_GOsRandom</code> methods
     *
     * @param propsFileName a <code>String</code> containing 2 required properties: 
     * <ul> 
     * <li> gbGOsFileName denoting to the name of the file containing GenBank accessions 
     * and their corresponding GO terms (space separated) in tab-delimited format: GB\tGO_1 GO_2 ... GO_n
     * <li> sifFileName denoting the name of the file containing all undirected interactions
     * in the modified SIF Cytoscape format with weights: "node1 (pp) node2 = weight" 
     * meaning an interaction from node1 to node2 with the given weight as a double.
     * Example: "node1 (pp) node2 = 1.0". This is the format used in Cytoscape edgeAttributes 
     * file except the initial comment line.
     * </ul>
     * <br>
     * and 3 optional properties:
     * <li> outDirectedInteractionsFileName which is the name of the file where the resulting directed graph is written. 
     * This graph contains only edges that could be directed using GO terms 
     * such that and an edge (a,b) is directed from a to b if a contains TF
     * as a GO term and b does not and that it is acyclic. 
     * The graph is written in modified Cytoscape directed SIF format with weights: 
     * "node1 (pd) node2 = weight" meaning an interaction from node1 to node2 
     * with the given weight as a double. Example: "node1 (pd) node2 = 1.0". 
     * This is the format used in Cytoscape edgeAttributes file except the initial comment line.
     * The default is "outDirectedInteractions.txt".
     * <li> seed which corresponds to the desired seed of to be used in the random number generator 
     * to randomly direct remaining undirected edges. The default is 1.
     * <li> outDirectedInteractionsAndRandomFileName which is the name of the file where the resulting 
     * directed graph is written. This graph contains edges that could be directed using GO terms 
     * such that and an edge (a,b) is directed from a to b if a contains TF as a GO term and b does not. 
     * The remaining undirected edges, if any, are randomly directed such that the resulting directed graph is acyclic. 
     * The graph is written in Cytoscape directed SIF format with weights: "node1 (pd) node2 = weight" 
     * meaning an interaction from node1 to node2 with the given weight as a double. Example: "node1 (pd) node2 = 1.0".
     * This is the format used in Cytoscape edgeAttributes file except the initial comment line. 
     * The default is "outDirectedInteractionsAndRandom.txt".
     * </ul>
     */
    public static void test(String propsFileName){
	try {
	    Properties props = new Properties();
	    props.load(new FileInputStream(propsFileName));
	    String gbGOsFileName = props.getProperty("gbGOsFileName", null);
	    String sifFileName = props.getProperty("sifFileName", null);	    
	    long seed = (long) Integer.parseInt(props.getProperty("seed", "1"));
	    String newInterFileName = props.getProperty("outDirectedInteractionsFileName", "outDirectedInteractions.txt");
	    String newInterAndRandomFileName = props.getProperty("outDirectedInteractionsAndRandomFileName", "outDirectedInteractionsAndRandom.txt");	    System.out.println("test()" + gbGOsFileName);
	    System.out.println("test()" + sifFileName);
	    Useful.checkFile(gbGOsFileName);
	    Useful.checkFile(sifFileName);
	    ArrayList inter = UsefulInteractions.readInteractionsWithWeights(sifFileName);
	    HashMap gbGOs = readGB_GOs(gbGOsFileName);
	    ArrayList newInter = getInteractionsFromGB_GOs(inter, gbGOs);
	    UsefulInteractions.writeSifFileWithWeights(newInter, newInterFileName);
	    ArrayList newInterAndRandom = getInteractionsFromGB_GOsRandom(inter, newInter, seed);
	    UsefulInteractions.writeSifFileWithWeights(newInterAndRandom, newInterAndRandomFileName);
	}
	catch(IOException ioe){
	    System.out.println(ioe);
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
	System.out.println("Usage: java ParseGB_GO propsFileName\nExample: java ParseGB_GO testParseGB_GO.props");
	System.exit(0);
    }

    public static void main(String[] argv){
	if(argv.length!=1){
	    usage();
	}
	String propsFileName = argv[0];
	test(propsFileName);
    }
}




