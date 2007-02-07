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
/* AllPairsShortestPaths.java
 * Copyright (C) 2005 Amira Djebbari
 */
package org.tigr.microarray.mev.cluster.gui.impl.bn.algs;
import java.io.IOException;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Properties;
import org.tigr.microarray.mev.cluster.gui.impl.bn.Useful;
import org.tigr.microarray.mev.cluster.gui.impl.bn.SimpleGeneEdge;
import org.tigr.microarray.mev.cluster.gui.impl.bn.UsefulInteractions;
import org.tigr.microarray.mev.cluster.gui.impl.bn.NullArgumentException;
import org.tigr.microarray.mev.cluster.gui.impl.bn.OutOfRangeException;
import org.tigr.microarray.mev.cluster.gui.impl.bn.Useful;
/**
 * The class <code>AllPairsShortestPaths</code> contains methods 
 * to compute all pairs shortest paths of a given graph 
 * in an <code>ArrayList</code> of <code>SimpleGeneEdge</code> objects representation 
 * or in a weight matrix representation
 * and get interactions from a given set of query nodes to nodes at distance K
 *
 * @author <a href="mailto:amira@jimmy.harvard.edu"></a>
 */
public class AllPairsShortestPaths {
    /**
     * The <code>allPairsShortestPaths</code> method 
     * performs the all pairs shortest paths (using O(n^3) time Floyd's algorithm) 
     * of a given graph in an adjacency matrix representation
     *
     * @param weightMatrix an <code>double[][]</code> corresponding to the adjacency matrix of the given graph
     * @param numNodes an <code>int</code> corresponding to the number of nodes in the given graph
     * @return an <code>double[][]</code> corresponding to the all pairs shortest paths matrix 
     * where A[i][j] = w which is the sum of the weights on the shortest path from i to j if j is reachable from i 
     * and java.lang.Double.MAX_VALUE (instead of infinity) otherwise
     * @exception NullArgumentException if an error occurs because the given double[][] weightMatrix was null
     * @exception OutOfRangeException if an error occurs because the given numNodes is negative or zero
     */
    public static double[][] allPairsShortestPaths(double[][] weightMatrix, int numNodes) throws NullArgumentException, OutOfRangeException{
	if(weightMatrix == null){
	    throw new NullArgumentException("Given weightMatrix is null");
	}
	if(numNodes <= 0){
	    throw new OutOfRangeException("Given numNodes is negative or zero!\nnumNodes="+numNodes);
	}
	double[][] A = new double[numNodes][numNodes];
	for(int i = 0; i < numNodes; i++){
	    for(int j = 0; j < numNodes; j++){
		A[i][j] = weightMatrix[i][j];
	    }
	}
	for(int k = 0; k < numNodes; k++){
	    for(int i = 0; i < numNodes; i++){
		for(int j = 0; j < numNodes;j++){		    
		    A[i][j]=min(A[i][j],A[i][k] + A[k][j]);
		}
	    }
	}
	return A;
    }

    /**
     * The <code>min</code> method takes in 2 doubles and returns the minimum
     *
     * @param i a <code>double</code> value
     * @param j a <code>double</code> value
     * @return a <code>double</code> corresponding to the minimum of i and j
     */
    public static double min(double i, double j){
	if(i < j){
	    return i;
	}
	return j;
    }
    
    /**
     * The <code>allPairsShortestPaths</code> method performs the all pairs shortest paths
     * (using O(n^3) time Floyd's algorithm) of a given graph 
     * in an <code>ArrayList</code> of <code>SimpleGeneEdge</code> objects representation
     *
     * @param ppi an <code>ArrayList</code> of <code>SimpleGeneEdge</code> objects representation of the given graph
     * @return a <code>double[][]</code>  corresponding to the all pairs shortest paths matrix 
     * where A[i][j] = w which is the sum of the weights on the shortest path from i to j if j is reachable from i 
     * and java.lang.Double.MAX_VALUE (instead of infinity) otherwise
     * @exception NullArgumentException if an error occurs because the given <code>ArrayList</code> ppi was null
     */
    public static double[][] allPairsShortestPaths(ArrayList ppi) throws NullArgumentException{
	try {
	    if(ppi == null){
		throw new NullArgumentException("Given ppi ArrayList is null!");
	    }
	    ArrayList ppiNodes = UsefulInteractions.getNodes(ppi);	
	    double[][] weightMatrix = UsefulInteractions.createWeightMatrix(ppi);	
	    UsefulInteractions.printWeightMatrix(weightMatrix, ppiNodes.size());
	    double[][] apspMatrix = allPairsShortestPaths(weightMatrix,ppiNodes.size());
	    UsefulInteractions.printWeightMatrix(apspMatrix, ppiNodes.size());
	    return apspMatrix;
	}
	catch(OutOfRangeException oore){
	    System.out.println(oore);
	}	
	return null;
    }


    /**
     * The <code>getInteractionsWithNodesAtDistanceK</code> method returns the list of interactions 
     * between some queryNodes and all the nodes at distanceK from these in the given graph
     *
     * @param ppi an <code>ArrayList</code> of <code>SimpleGeneEdge</code> objects representation of the given graph
     * @param queryNodes an <code>ArrayList</code> corresponding to the list of query nodes 
     * from which the interactions will be computed
     * @param distanceK a <code>double</code> the distance for which the interactions will be computed 
     * from the given nodes to nodes at distanceK from the given nodes in the given graph
     * @return an <code>ArrayList</code> of <code>SimpleGeneEdge</code> objects representation 
     * of the resulting graph with interactions from the given nodes to nodes at distanceK from the given nodes 
     * in the given graph
     * @exception NullArgumentException if an error occurs
     */
    public static ArrayList getInteractionsWithNodesAtDistanceK(ArrayList ppi, ArrayList queryNodes, double distanceK) throws NullArgumentException {
	try {
	    if(queryNodes == null || ppi == null){
		throw new NullArgumentException("At least one of queryNodes or ppi was null\nqueryNodes="+queryNodes+"\nppi="+ppi);
	    }
	    ArrayList result = new ArrayList();
	    ArrayList ppiNodes = UsefulInteractions.getNodes(ppi);
	    double[][] weightMatrix = UsefulInteractions.createWeightMatrix(ppi);
	    double[][] apspMatrix = allPairsShortestPaths(weightMatrix,ppiNodes.size());
	    int[] queryNodesIndicesInPpi = new int[queryNodes.size()];
	    for(int i = 0; i < queryNodes.size(); i++){
		queryNodesIndicesInPpi[i] = ppiNodes.indexOf((String)queryNodes.get(i));
	    }
	    SimpleGeneEdge sGE = null;
	    for(int i = 0; i < queryNodesIndicesInPpi.length; i++){
		for(int j = 0; j < apspMatrix[i].length; j++){
		    if(queryNodesIndicesInPpi[i]!=-1){
			if(apspMatrix[queryNodesIndicesInPpi[i]][j] <= distanceK){
			    sGE = new SimpleGeneEdge((String)queryNodes.get(i), (String) ppiNodes.get(j), apspMatrix[queryNodesIndicesInPpi[i]][j]);
			    result.add(sGE);
			}
		    }
		}
	    }
	    return result;
	}
	catch(OutOfRangeException oore){
	    System.out.println(oore);
	}
	return null;
    }

    /**
     * The <code>test</code> method tests the <code>getInteractionsWithNodesAtDistanceK</code> method
     *
     * @param propsFileName a <code>String</code> containing 2 required properties:
     * <ul> 
     * <li> ppiFileName denoting the name of the file containing all undirected interactions 
     * in Cytoscape directed SIF format: node1 pd node2
     * <li> queryNodesFileName denoting the name of the file containing the queryNodes one per line 
     * </ul>
     * <p>
     * and 2 optional properties:
     * <ul> 
     * <li> distanceK which is the distance for which the interactions will be computed 
     * from the given nodes to nodes at distanceK from the given nodes in the given graph. The default is distanceK = 3.
     * <li> outInteractionsWithNodesAtDistanceKFileName which is the name of the file 
     * where the resulting interactions from the given queryNodes to any reachable nodes in the given graph will be written. 
     * The default is "outInteractionsWithNodesAtDistanceK.txt".
     * </ul>
     */
    public static void test(String propsFileName) {	
	try {
	    Properties props = new Properties();
	    props.load(new FileInputStream(propsFileName));
	    String ppiFileName = props.getProperty("ppiFileName", null);
	    String queryNodesFileName = props.getProperty("queryNodesFileName", null);	    
	    double distanceK = Double.parseDouble(props.getProperty("distanceK", "3"));	    
	    String outInterWithNodesAtDistanceKFileName = props.getProperty("outInteractionsWithNodesAtDistanceKFileName", "outInteractionsWithNodesAtDistanceK.txt");	    
	    Useful.checkFile(ppiFileName);
	    Useful.checkFile(queryNodesFileName);
	    ArrayList ppi = UsefulInteractions.readDirectedInteractions(ppiFileName);
	    ArrayList queryNodes = Useful.readNamesFromFile(queryNodesFileName);
	    ArrayList interWithNodesAtDistanceK = getInteractionsWithNodesAtDistanceK(ppi, queryNodes, distanceK);
	    UsefulInteractions.writeSifFileWithWeights(interWithNodesAtDistanceK, outInterWithNodesAtDistanceKFileName);
	}
	catch(IOException ioe){
	    System.out.println(ioe);
	}
	catch(NullArgumentException nae){
	    System.out.println(nae);
	}
	
    }

    /**
     * Describe <code>test</code> method tests the <code>getInteractionsWithNodesAtDistanceK</code> method
     * with a graph G(V,E) where V = {a,b,c,d} and E = {(a,b),(b,c),(c,d)} 
     * and queryNodes = {a}, distance K = 2.0
     * <br>
     * Result should be {(a,b),(a,c)}
     *
     */
    public static void test(){
	try {
	    ArrayList inter = new ArrayList();
	    inter.add(new SimpleGeneEdge("a", "b", 1.0));
	    inter.add(new SimpleGeneEdge("b", "c", 1.0));
	    inter.add(new SimpleGeneEdge("c", "d", 1.0));
	    ArrayList query = new ArrayList();
	    query.add("a");
	    double distanceK = 2.0;
	    System.out.println("Testing with given graph: "+inter+"\nand given queryNode="+query+"\nand given distanceK="+distanceK);
	    ArrayList distanceKInters = getInteractionsWithNodesAtDistanceK(inter, query, distanceK);
	    System.out.println("Result: "+distanceKInters);	    
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
	System.out.println("Usage: java AllPairsShortestPaths propsFileName\nExample: java AllPairsShortestPaths GraphUtilsAlgs.props");
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


















