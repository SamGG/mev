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
/* PrepareXMLBifModule.java
 * Copyright (C) 2005 Amira Djebbari
 */
package org.tigr.microarray.mev.cluster.gui.impl.bn.prepareXMLBif;
import java.io.FileInputStream;import java.io.IOException;import java.io.FileOutputStream;
import java.io.PrintWriter;import java.util.ArrayList;import java.util.HashMap;import java.util.Properties;import org.tigr.microarray.mev.cluster.gui.impl.bn.algs.DFSModification;
import org.tigr.microarray.mev.cluster.gui.impl.bn.BNConstants;
import org.tigr.microarray.mev.cluster.gui.impl.bn.Useful;import org.tigr.microarray.mev.cluster.gui.impl.bn.NullArgumentException;
import org.tigr.microarray.mev.cluster.gui.impl.bn.NotDAGException;
import org.tigr.microarray.mev.cluster.gui.impl.bn.UsefulInteractions;
/**
 * The class <code>PrepareXMLBifModule</code> takes in an undirected graph and directs it using either one of 2 methods: <ul>
 * <li> according to GO terms information such that and an edge (a,b) is directed from a to b if a contains TF as a GO term and b does not. 
 * The remaining undirected edges, if any, are randomly directed such that the resulting directed graph is acyclic.
 * <li> using a modification of depth-first search that directing back edges in increasing order of visiting timestamp
 * </ul>
 * The resulting directed acyclic graph (DAG) in modified Cytoscape SIF format with weights is transformed into
 * an XML BIF format (standard format to encode Bayesian networks that can be used as input to WEKA).
 *
 * @author <a href="mailto:amirad@AMIRA"></a>
 */
public class PrepareXMLBifModule {
    public static boolean debug = false;
    public static String logFileName = null;
    static String path;
    /**
     * The <code>getInteractionsFromGB_GOs</code> takes in a given graph 
     * in an <code>ArrayList</code> of <code>SimpleGeneEdge</code> objects representation 
     * and keyValuesPairs with GenBank accessions as keys and their corresponding GO terms as values
     * and returns a graph containing only edges that could be directed using GO terms
     * such that and an edge (a,b) is directed from a to b if a contains TF as a GO term and b does not.
     *
     * @param interWithWeights an <code>ArrayList</code> of <code>SimpleGeneEdge</code> objects representation
     * of the given graph
     * @param gbGOs a <code>HashMap</code> of keyValuesPairs with GenBank accessions as keys 
     * and their corresponding GO terms as values
     * @return an <code>ArrayList</code> of <code>SimpleGeneEdge</code> objects representation 
     * of the resulting directed graph containing only edges that could be directed using GO terms 
     * such that and an edge (a,b) is directed from a to b if a contains TF as a GO term and b does not.
     * @exception NullArgumentException if an error occurs because at least one of the given parameters
     * interactions or gbGOs was null
     */
    public static ArrayList getInteractionsFromGB_GOs(ArrayList interWithWeights, HashMap gbGOs) throws NullArgumentException {
	if(debug == true){
	    ParseGB_GO.setDebug(true, logFileName);
	}
	return ParseGB_GO.getInteractionsFromGB_GOs(interWithWeights, gbGOs);
    }
    /**
     * The <code>getInteractionsFromGB_GOsRandom</code> takes in a given undirected graph 
     * in an <code>ArrayList</code> of <code>SimpleGeneEdge</code> objects representation 
     * and a given directed graph (in this context, containing edges that could be directed using GO terms
     * such that and an edge (a,b) is directed from a to b if a contains TF as a GO term and b does not). 
     * The remaining undirected edges, if any, are randomly directed such that the resulting directed graph is acyclic.
     *
     * @param interWithWeights an <code>ArrayList</code> of <code>SimpleGeneEdge</code> objects representation 
     * of the given undirected graph
     * @param seed a <code>long</code> corresponding to the seed of the random number generator 
     * used to randomly direct remaining undirected edges
     * @return an <code>ArrayList</code> of <code>SimpleGeneEdge</code> objects representation 
     * of the resulting directed graph containing the edges in the given directed graph newInter.
     * The remaining undirected edges of interactions, if any, are randomly directed 
     * such that the resulting directed graph is acyclic.
     * @exception NullArgumentException if an error occurs because at least one of the given parameters 
     * interactions or newInter was null
     */
    public static ArrayList getInteractionsFromGB_GOsAndRandom(ArrayList interWithWeights, HashMap gbGOs, long seed) throws NullArgumentException {
	if(debug == true){
	    ParseGB_GO.setDebug(true, logFileName);
	}
	return ParseGB_GO.getInteractionsFromGB_GOsRandom(interWithWeights, gbGOs, seed);
    }
    
    /**
     * The <code>getDAGFromUndirectedGraph</code> takes in a given undirected graph 
     * in an <code>ArrayList</code> of <code>SimpleGeneEdge</code> objects representation 
     * and returns a DAG computed by performing a modification of DFS such that     * back edges are directed in increasing number of DFN number.
     *
     * @param interWithWeights an <code>ArrayList</code> of <code>SimpleGeneEdge</code> objects representation 
     * of the given undirected graph
     * @return an <code>ArrayList</code> of <code>SimpleGeneEdge</code> objects representation 
     * of the resulting directed acyclic graph containing the edges in the given undirected graph directed using a modification of DFS such that back edges are directed in increasing number of DFN number.
     * @exception NullArgumentException if an error occurs because at least one of the given parameters 
     * interactions or newInter was null
     */
    public static ArrayList getDAGFromUndirectedGraph(ArrayList interWithWeights) throws NullArgumentException {
	if(debug == true){
	    DFSModification.setDebug(true, logFileName);
	}
	return DFSModification.getDAGFromUndirectedGraph(interWithWeights);
    }

    /**
     * The <code>test</code> method tests the <code>getInteractionsFromGB_GOs</code> 
     * and <code>getInteractionsFromGB_GOsRandom</code> methods
     *
     * @param propsFileName a <code>String</code> containing 2 required properties: 
     * <ul> 
     * <li> sifFileName denoting the name of the file containing all undirected interactions
     * in the modified SIF Cytoscape format with weights: "node1 (pp) node2 = weight" 
     * meaning an interaction from node1 to node2 with the given weight as a double.
     * Example: "node1 (pp) node2 = 1.0". This is the format used in Cytoscape edgeAttributes 
     * file except the initial comment line.
     * <li> namesFileName denoting the name of the file containing Strings corresponding to
     * the random variable (RV) names to be included in the XML BIF format.
     * </ul>
     * <br>
     * and 9 optional properties:
     * <li> useGO which is true if GO terms are to be used to direct edges, false otherwise and the edges will be directed 
     * using a modification of DFS such that back edges are directed in increasing order of DFN number. The default is false.
     * <li> Properties to include if useGO is true.
     * <ul>
     * <li> gbGOsFileName denoting to the name of the file containing GenBank accessions 
     * and their corresponding GO terms (space separated) in tab-delimited format: GB\tGO_1 GO_2 ... GO_n
     * <li> seed which corresponds to the desired seed of to be used in the random number generator 
     * to randomly direct remaining undirected edges. The default is 1.
     * </ul>
     * <li> distributionFromWeights which is a flag denoting whether the conditional probability tables (CPTs)
     * are to be computed from weights or using uniform distribution.
     * If distributionFromWeights is true, the CPTs are computed from the weights of the edges in the given graph G(V,E)
     * such that given 2 variables with a directed edge from a to b with weight w(A,B),
     * the probability of A given B will be computed as: p(A|B) = w(A,B) / max forall e in E of w(e)
     * Otherwise, the CPTs are computed from uniform distribution based on the number of states.
     * For example, if variable A has no parents and 3 states, 
     * its CPT will be p(A=state1) = 0.33 p(A=state2) = 0.33 p(A=state3) = 0.33.
     * <li> numStates which is an int corresponding to the number of states for a given RV. The default is 3.
     * <li> statei which is the name of ith state. For example, if we have 3 states:
     * state0=myState0, state1=myState1, state2=myState2. The defaults are for each state i, the name is "state"i. 
     * In the example, state0, state1, state2.
     * <li> numClasses which is an int corresponding to the number of classes the CLASS variable can take. The default is 2.
     * <li> classi which is the name of the ith class. For example, if we have 2 classes: class0=AML, class1=ALL.
     * The defaults are for each class i, the name is "class"i. In the example: class0, class1
     * <li> outXMLBifFileName: the name of the file where the XML BIF format representation of the Bayesian network 
     * will be saved. The default is "out_bif.xml".
     * </ul>
     */
    public static void test(String propsFileName){	
    	//String path=System.getProperty("user.dir");
    	String path=BNConstants.getBaseFileLocation();
    	System.out.println("PrepareXMLBifModule path: user.dir: " + path);
    	//String sep=System.getProperty("file.separator");    	//path=path+sep+"data"+sep+"bn"+sep; //Raktim - Use tmp Dir
    	//path=path+BNConstants.SEP+"data"+BNConstants.SEP+"bn"+BNConstants.SEP+BNConstants.TMP_DIR+BNConstants.SEP;
    	path=path+BNConstants.SEP+BNConstants.TMP_DIR+BNConstants.SEP;
	try {
	    Properties props = new Properties();
	    props.load(new FileInputStream(propsFileName));
	    String isDebugStr = props.getProperty("debug", "false");
	    if(isDebugStr.equals("true")){
		debug = true;
	    }
	    String sifFileName = path+props.getProperty(BNConstants.SIF_FILE_NAME, null);
	    boolean useGO = Boolean.getBoolean(props.getProperty(BNConstants.USE_GO, "false"));	    
	    String gbGOsFileName = path+props.getProperty(BNConstants.GB_GO_FILE_NAME, null); //"gbGOsFileName"
	    String namesFileName = path+props.getProperty(BNConstants.NAMES_FILE_NAME,null);
	    long seed = (long) Integer.parseInt(props.getProperty("seed", "1"));	    System.out.println("test()" + namesFileName);
	    System.out.println("test()" + sifFileName);
	    Useful.checkFile(sifFileName);
	    Useful.checkFile(namesFileName);
	    if(useGO){
	    	Useful.checkFile(gbGOsFileName);
	    }	    //String outXMLBifFileName = System.getProperty("user.dir")+sep+props.getProperty("outXMLBifFileName","out_bif.xml");
	    // Raktim - Use tmp Dir
	    //String fileLoc = System.getProperty("user.dir")+BNConstants.SEP+"data"+BNConstants.SEP+"bn"+BNConstants.SEP+BNConstants.TMP_DIR+BNConstants.SEP;
	    //String fileLoc = path+BNConstants.SEP+BNConstants.TMP_DIR+BNConstants.SEP;
	    String outXMLBifFileName = path+props.getProperty(BNConstants.OUT_XML_BIF_FILE_NAME,BNConstants.OUT_XML_BIF_FILE);
	    ArrayList inter = UsefulInteractions.readInteractionsWithWeights(sifFileName);
	    ArrayList names = Useful.readNamesFromFile(namesFileName);
	    ArrayList newInter = null;
	    if(useGO){	    	HashMap gbGOs = ParseGB_GO.readGB_GOs(gbGOsFileName);	    	newInter = getInteractionsFromGB_GOsAndRandom(inter, gbGOs, seed);	    }
	    else {	    	newInter = getDAGFromUndirectedGraph(inter);	    }	    PrintWriter pw = new PrintWriter(new FileOutputStream(outXMLBifFileName), true);	    	    SifToXMLBif.createXMLBifGivenSifFile(newInter, names, pw, props);	    pw.close();	}
	catch(IOException ioe){	    System.out.println(ioe);	    ioe.printStackTrace();	}
	catch(NullArgumentException nae){	    System.out.println(nae);	    nae.printStackTrace();	}
	catch(NotDAGException nde){	    System.out.println(nde);	    nde.printStackTrace();	}
    }

    /**
     * The <code>usage</code> method displays the usage.
     *
     */
    public static void usage(){
	System.out.println("Usage: java PrepareXMLBifModule propsFileName\nExample: java PrepareXMLBifModule prepareXMLBif.props");
	System.exit(0);
    }

    public static void main(String[] argv){
	if(argv.length != 1){
	    usage();
	}
	String propsFileName = argv[0];
	test(propsFileName);
    }
}


