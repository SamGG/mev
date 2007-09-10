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
/* TransitiveClosure.java
 * Copyright (C) 2005 Amira Djebbari
 */
package org.tigr.microarray.mev.cluster.gui.impl.bn.algs;import java.io.IOException;import java.io.FileInputStream;
import java.util.ArrayList;import java.util.Properties;import org.tigr.microarray.mev.cluster.gui.impl.bn.Useful;
import org.tigr.microarray.mev.cluster.gui.impl.bn.SimpleGeneEdge;
import org.tigr.microarray.mev.cluster.gui.impl.bn.UsefulInteractions;
import org.tigr.microarray.mev.cluster.gui.impl.bn.NullArgumentException;import org.tigr.microarray.mev.cluster.gui.impl.bn.OutOfRangeException;import org.tigr.microarray.mev.cluster.gui.impl.bn.Useful;
/**
 * The class <code>TransitiveClosure</code> contains methods to compute the transitive closure
 * of a given graph in an <code>ArrayList</code> of <code>SimpleGeneEdge</code> objects representation 
 * or in an adjacency matrix representation and get interactions from a given set of query nodes 
 * to any nodes reachable from these nodes
 *
 * @author <a href="mailto:amira@jimmy.harvard.edu"></a>
 */
public class TransitiveClosure{
    /**
     * The <code>transitiveClosure</code> method performs the transitive closure of a given graph 
     * in an adjacency matrix representation
     *
     * @param adjMatrix an <code>int[][]</code> corresponding to the adjacency matrix of the given graph
     * @param numNodes an <code>int</code> corresponding to the number of nodes in the given graph
     * @return an <code>int[][]</code> corresponding to the reachability matrix where A[i][j] = 1 
     * if j is reachable from i and 0 otherwise
     * @exception NullArgumentException if an error occurs because the given int[][] adjMatrix was null
     * @exception OutOfRangeException if an error occurs because the given numNodes is negative or zero
     */
    public static int[][] transitiveClosure(int[][] adjMatrix, int numNodes) throws NullArgumentException, OutOfRangeException{
	if(adjMatrix == null){
	    throw new NullArgumentException("Given adjacencyMatrix is null!");
	}
	if(numNodes <= 0){
	    throw new OutOfRangeException("Given numNodes is negative or zero!\nnumNodes="+numNodes);
	}
	for(int k = 0; k < numNodes; k++){
	    for(int i = 0; i < numNodes; i++){
		for(int j = 0; j < numNodes;j++){		    
		    adjMatrix[i][j] = adjMatrix[i][j] | adjMatrix[i][k] & adjMatrix[k][j];
		}
	    }
	}
	return adjMatrix;
    }
    
    /**
     * The <code>getInteractionsWithReachableNodes</code> method returns the list of interactions 
     * between some queryNodes and all the nodes reachable from these in the given graph
     *
     * @param queryNodes an <code>ArrayList</code> corresponding to the list of query nodes 
     * from which the interactions will be computed
     * @param ppiNodes an <code>ArrayList</code> corresponding to the list of all the nodes in the graph, 
     * in this application, ppiNodes
     * @param adjMatrix an <code>int[][]</code> corresponding to the adjacency matrix representation of the given graph
     * @return an <code>ArrayList</code> of <code>SimpleGeneEdge</code> objects corresponding to the list of interactions
     * between some queryNodes and all the nodes reachable from these in the given graph
     * @exception NullArgumentException if an error occurs because at least one of the parameters 
     * queryNodes, ppiNodes or adjMatrix was null
     */
    public static ArrayList getInteractionsWithReachableNodes(ArrayList queryNodes, ArrayList ppiNodes, int[][] adjMatrix) throws NullArgumentException{
	if(queryNodes == null || ppiNodes == null || adjMatrix == null){
	    throw new NullArgumentException("At least one of queryNodes, ppiNodes or adjacencyMatrix is null!\nqueryNodes="+queryNodes+"\nppiNodes="+ppiNodes+"adjMatrix="+adjMatrix);
	}
	ArrayList result = new ArrayList();
	int[] queryNodesIndicesInPpi = new int[queryNodes.size()];
	for(int i = 0; i < queryNodes.size(); i++){
	    queryNodesIndicesInPpi[i] = ppiNodes.indexOf((String)queryNodes.get(i));
	}
	SimpleGeneEdge sGE = null;       
	for(int i = 0; i < queryNodesIndicesInPpi.length; i++){
	    for(int j = 0; j < adjMatrix[i].length; j++){
		if(queryNodesIndicesInPpi[i]!=-1){
		    if(adjMatrix[queryNodesIndicesInPpi[i]][j] == 1){
			sGE = new SimpleGeneEdge((String)queryNodes.get(i), (String) ppiNodes.get(j), 1.0);
			result.add(sGE);
		    }
		}
	    }
	}
	return result;
    }
    /**
     * The <code>getInteractionsWithReachableNodes</code> method returns the list of interactions 
     * between some queryNodes and all the nodes reachable from these in the given graph
     *
     * @param ppi an <code>ArrayList</code> of <code>SimpleGeneEdge</code> objects representation of the given graph
     * @param queryNodes an <code>ArrayList</code> corresponding to the list of query nodes 
     * from which the interactions will be computed
     * @return an <code>ArrayList</code> of <code>SimpleGeneEdge</code> objects corresponding to the list of interactions
     * between some queryNodes and all the nodes reachable from these in the given graph
     * @exception NullArgumentException if an error occurs because at least one of the parameters ppi or queryNodes was null
     */
    public static ArrayList getInteractionsWithReachableNodes(ArrayList ppi, ArrayList queryNodes) throws NullArgumentException{
	try {
	    if(ppi == null || queryNodes == null){
		throw new NullArgumentException("At least one of ppi or queryNodes was null\nppi="+ppi+"\nqueryNodes="+queryNodes);
	    }
	    ArrayList ppiNodes = UsefulInteractions.getNodes(ppi);	
	    int[][] adjMatrix = UsefulInteractions.createAdjMatrix(ppi);
	    int[][] reachMatrix = transitiveClosure(adjMatrix,ppiNodes.size());
	    ArrayList reachableInters = getInteractionsWithReachableNodes(queryNodes, ppiNodes, reachMatrix);
	    return reachableInters;
	}
	catch(OutOfRangeException oore){
	    System.out.println(oore);
	}
	return null;
    }
    /**
     * The <code>test</code> method tests the <code>getInteractionsWithReachableNodes</code> method
     *
     * @param propsFileName a <code>String</code> containing 2 required properties: 
     * <ul> 
     * <li> ppiFileName denoting the name of the file containing all undirected interactions
     * in Cytoscape directed SIF format: node1 pd node2
     * <li> queryNodesFileName denoting the name of the file containing the queryNodes one per line 
     * </ul>
     * <br>
     * and one optional property outInteractionsWithReachableNodesFileName which is the name of the file 
     * where the resulting interactions from the given queryNodes to any reachable nodes in the given graph will be written. 
     * The default is "outInteractionsWithReachableNodes.txt".
     */
    public static void test(String propsFileName) {	
	try {
	    Properties props = new Properties();
	    props.load(new FileInputStream(propsFileName));
	    String ppiFileName = props.getProperty("ppiFileName", null);
	    String queryNodesFileName = props.getProperty("queryNodesFileName", null);	    
	    String outInterWithReachableFileName = props.getProperty("outInteractionsWithReachableNodesFileName", "outInteractionsWithReachableNodes.txt");	    	    System.out.println("test()" + ppiFileName);
	    System.out.println("test()" + queryNodesFileName);
	    Useful.checkFile(ppiFileName);
	    Useful.checkFile(queryNodesFileName);
	    ArrayList ppi = UsefulInteractions.readDirectedInteractions(ppiFileName);
	    ArrayList queryNodes = Useful.readNamesFromFile(queryNodesFileName);
	    ArrayList reachableInters = getInteractionsWithReachableNodes(ppi, queryNodes);
	    UsefulInteractions.writeSifFileWithWeights(reachableInters, outInterWithReachableFileName);
	}
	catch(IOException ioe){
	    System.out.println(ioe);
	}
	catch(NullArgumentException nae){
	    System.out.println(nae);
	}
    }
    
    /**
     * Describe <code>test</code> method tests the <code>getInteractionsWithReachableNodes</code> method 
     * with a graph G(V,E) where V = {a,b,c,d} and E = {(a,b),(b,c),(c,d)} and queryNodes = {b}
     * Result should be {(b,c),(b,d)}
     *
     */
    public static void test(){
	try {
	    ArrayList inter = new ArrayList();
	    inter.add(new SimpleGeneEdge("a", "b", 1.0));
	    inter.add(new SimpleGeneEdge("b", "c", 1.0));
	    inter.add(new SimpleGeneEdge("c", "d", 1.0));
	    ArrayList query = new ArrayList();
	    query.add("b");
	    System.out.println("Testing with given graph: "+inter+"\nand given queryNode="+query);
	    ArrayList reachableInters = getInteractionsWithReachableNodes(inter, query);
	    System.out.println("Result: "+reachableInters);	    
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
	System.out.println("Usage: java TransitiveClosure propsFileName\nExample: java TransitiveClosure GraphUtilsAlgs.props");
	System.exit(0);
    }

    public static void main(String[] argv){
	if(argv.length != 1){
	    test();
	    usage();
	}
	String propsFileName = argv[0];
	test(propsFileName);
    }
}




