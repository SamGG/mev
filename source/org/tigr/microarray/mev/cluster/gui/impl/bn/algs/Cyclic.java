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
/* Cyclic.java
 * Copyright (C) 2005 Amira Djebbari
 */
package org.tigr.microarray.mev.cluster.gui.impl.bn.algs;import org.tigr.microarray.mev.cluster.gui.impl.bn.SimpleGeneEdge;import org.tigr.microarray.mev.cluster.gui.impl.bn.UsefulInteractions;import org.tigr.microarray.mev.cluster.gui.impl.bn.Useful;
import org.tigr.microarray.mev.cluster.gui.impl.bn.NullArgumentException;
import org.tigr.microarray.mev.cluster.gui.impl.bn.OutOfRangeException;
import java.util.ArrayList;
import java.io.FileNotFoundException;
/**
 * The class <code>Cyclic</code> contains methods to determine whether a given graph in an <code>ArrayList</code>
 * of <code>SimpleGeneEdge</code> objects representation is cyclic or not by performing a Depth-First Search (DFS) on it.
 *
 * @author <a href="mailto:amira@jimmy.harvard.edu"></a>
 */
public class Cyclic {
	/**
	 * The <code>isCyclic</code> method determines whether a given graph 
	 * in an <code>ArrayList</code> of <code>SimpleGeneEdge</code> objects representation 
	 * is cyclic or not by performing a Depth-First Search (DFS) on it.
	 *
	 * @param inter an <code>ArrayList</code> of <code>SimpleGeneEdge</code> objects representation of the given graph
	 * @return a <code>boolean</code> true if it is cyclic, false otherwise
	 * @exception NullArgumentException if an error occurs because the given <code>ArrayList</code> inter was null or empty
	 */
	public static boolean isCyclic(ArrayList inter) throws NullArgumentException{
		boolean isCyclic = false;
		try {
			if(inter == null || inter.size()==0){
				throw new NullArgumentException("Given ArrayList inter is null or empty!");
			}
			ArrayList nodes = UsefulInteractions.getNodes(inter);
			int[] color = new int[nodes.size()];
			// color: white = -1, grey = 0, black = 1
			ArrayList[] p = new ArrayList[nodes.size()];
			for(int i = 0; i < nodes.size(); i++){
				color[i] = -1; 
				p[i]= new ArrayList();
			}
			int time = 0;
			int[] d = new int[nodes.size()];
			for(int i =0; i < nodes.size(); i++){
				if(color[i] == -1){
					isCyclic = DFS(i, d, p, color, time, inter, nodes, isCyclic);
				}	    
			}
			return isCyclic;
		}
		catch(OutOfRangeException oore){
			System.out.println(oore);
		}
		return isCyclic;
	}

	/**
	 * The <code>getDAGFromUndirectedGraph</code> method transforms a given undirected graph 
	 * into an <code>ArrayList</code> of <code>SimpleGeneEdge</code> objects representation 
	 * of a DAG by performing a modification of DFS on it.
	 *
	 * @param inter an <code>ArrayList</code> of <code>SimpleGeneEdge</code> objects representation of the given undirected graph
	 * @return an <code>ArrayList</code> of <code>SimpleGeneEdge</code> objects representation of the output DAG
	 * @exception NullArgumentException if an error occurs because the given <code>ArrayList</code> inter was null or empty
	 */
	public static ArrayList getDAGFromUndirectedGraph(ArrayList inter) throws NullArgumentException{	
		boolean isCyclic = false;
		ArrayList DAG = new ArrayList();
		try {
			if(inter == null || inter.size()==0){
				throw new NullArgumentException("Given ArrayList inter is null or empty!");
			}
			ArrayList nodes = UsefulInteractions.getNodes(inter);
			int[] color = new int[nodes.size()];
			// color: white = -1, grey = 0, black = 1
			ArrayList[] p = new ArrayList[nodes.size()];
			for(int i = 0; i < nodes.size(); i++){
				color[i] = -1; 
				p[i]= new ArrayList();
			}
			int time = 0;
			int[] d = new int[nodes.size()];
			for(int i =0; i < nodes.size(); i++){
				if(color[i] == -1){
					DAG = DFSModification(i, d, p, color, time, inter, nodes, isCyclic, DAG);
				}	    
			}
			return DAG;
		}
		catch(OutOfRangeException oore){
			System.out.println(oore);
		}
		return DAG;
	}


	/**
	 * The <code>DFS</code> method performs a Depth-First search on the given graph 
	 * in order to determine whether the given graph is cyclic or not.
	 * <br>
	 * Theorem: A graph is cyclic iff at some point during the traversal, 
	 * when u is grey, one of the neighbors v of u considered in DFS is colored grey and is not p[u]. 
	 *
	 * @param u an <code>int</code> corresponding to the index of a particular white colored node in the graph
	 * @param d an <code>int[]</code> corresponding to the array used to stored the timestamps of nodes at each index 
	 * @param p an <code>ArrayList[]</code> corresponding to the parents of the node at each index
	 * @param color an <code>int[]</code> corresponding to the color of the nodes at each index
	 * @param time an <code>int</code> corresponding to the timestamp
	 * @param inter an <code>ArrayList</code> of <code>SimpleGeneEdge</code> objects representation of the given graph
	 * @param nodes an <code>ArrayList</code> of <code>String</code> corresponding to the nodes of the given graph
	 * @param cyclic a <code>boolean</code> true if cyclic, false otherwise
	 * @return a <code>boolean</code> true if cyclic, false otherwise
	 * @exception NullArgumentException if an error occurs because at least one of the given parameters d, p, color, 
	 * inter or nodes was null
	 * @exception OutOfRangeException if an error occurs because at least one of the given parameters u or time was negative
	 */
	public static boolean DFS(int u, int[] d, ArrayList[] p, int[] color, int time, ArrayList inter, ArrayList nodes, boolean cyclic) throws NullArgumentException, OutOfRangeException{
		if(d == null || p == null || color == null || inter == null || nodes == null){
			throw new NullArgumentException("At least one of the given parameters int[] d, ArrayList[] p, int[] color, ArrayList inter or ArrayList nodes was null!\nd="+d+"\np="+p+"\ncolor="+color+"\ninter="+inter+"\nnodes="+nodes);
		}
		if(u < 0 || time < 0){
			throw new OutOfRangeException("At least one of the given int u or int time was null!\nu="+u+"\ntime="+time);
		}
		d[u] = time;
		color[u] = 0;
		int[] neighbors = UsefulInteractions.getNeighbors(u, inter, nodes);
		int v = 0;
		for(int i = 0; i < neighbors.length; i++){
			v = neighbors[i];
			if((color[v] == 0) && (!Useful.contains(p[v],new Integer(u)))){
				cyclic = true;
			}
			if(color[v] == -1){
				p[v].add(new Integer(u));
				cyclic = DFS(v, d , p, color, time, inter, nodes, cyclic);
			}
		}
		time = time + 1;
		color[u] = 1;
		return cyclic;
	}

	/**
	 * The <code>DFSModification</code> method performs a Depth-First search on the given undirected graph 
	 * in order to return a DAG
	 * <br>
	 * Theorem: A graph is cyclic iff at some point during the traversal, 
	 * when u is grey, one of the neighbors v of u considered in DFS is colored grey and is not p[u]. 
	 *
	 * @param u an <code>int</code> corresponding to the index of a particular white colored node in the graph
	 * @param d an <code>int[]</code> corresponding to the array used to stored the timestamps of nodes at each index 
	 * @param p an <code>ArrayList[]</code> corresponding to the parents of the node at each index
	 * @param color an <code>int[]</code> corresponding to the color of the nodes at each index
	 * @param time an <code>int</code> corresponding to the timestamp
	 * @param inter an <code>ArrayList</code> of <code>SimpleGeneEdge</code> objects representation of the given graph
	 * @param nodes an <code>ArrayList</code> of <code>String</code> corresponding to the nodes of the given graph
	 * @param cyclic a <code>boolean</code> true if cyclic, false otherwise
	 * @return a <code>ArrayList</code> of <code>SimpleGeneEdge</code> objects representation of the output DAG
	 * such that a depth-first search is performed on the undirected graph and forward edges are directed 
	 * in order of visiting them and back edges (if any) are directed in increasing order of DFN number
	 * to make sure a DAG is obtained in the end. 
	 * @exception NullArgumentException if an error occurs because at least one of the given parameters d, p, color, 
	 * inter or nodes was null
	 * @exception OutOfRangeException if an error occurs because at least one of the given parameters u or time was negative
	 */
	public static ArrayList DFSModification(int u, int[] d, ArrayList[] p, int[] color, int time, ArrayList inter, ArrayList nodes, boolean cyclic, ArrayList DAG) throws NullArgumentException, OutOfRangeException{       
		if(d == null || p == null || color == null || inter == null || nodes == null){
			throw new NullArgumentException("At least one of the given parameters int[] d, ArrayList[] p, int[] color, ArrayList inter or ArrayList nodes was null!\nd="+d+"\np="+p+"\ncolor="+color+"\ninter="+inter+"\nnodes="+nodes);
		}
		if(u < 0 || time < 0){
			throw new OutOfRangeException("At least one of the given int u or int time was null!\nu="+u+"\ntime="+time);
		}
		SimpleGeneEdge sGE = null;
		d[u] = time;
		color[u] = 0;
		int[] neighbors = UsefulInteractions.getNeighbors(u, inter, nodes);
		int v = 0;
		for(int i = 0; i < neighbors.length; i++){
			v = neighbors[i];
			if((color[v] == 0) && (!Useful.contains(p[v],new Integer(u)))){
				cyclic = true;
				sGE = new SimpleGeneEdge((String) nodes.get(v), (String) nodes.get(u));
				if(!UsefulInteractions.containsEitherWay(DAG, sGE)){
					DAG.add(sGE);
				}    
			}
			if(color[v] == -1){
				p[v].add(new Integer(u));
				sGE = new SimpleGeneEdge((String) nodes.get(u), (String) nodes.get(v));
				if(!UsefulInteractions.containsEitherWay(DAG, sGE)){
					DAG.add(sGE);
				}
				DAG = DFSModification(v, d , p, color, time, inter, nodes, cyclic, DAG);
			}
		}
		time = time + 1;
		color[u] = 1;
		return DAG;
	}


	/**
	 * The <code>usage</code> method displays the usage
	 *
	 */
	public static void usage(){
		System.out.println("Usage: java Cyclic interFileName\nExample: java Cyclic myInter.sif");
	}
	/**
	 * The <code>test</code> method tests the <code>isCyclic</code> method with a given graph
	 * 
	 * @param interFileName a <code>String</code> corresponding to the name of the file containing 
	 * the directed graph in cytoscape SIF format: node1 pd node2
	 */
	public static void test(String interFileName){ 
		try {
			ArrayList inter = UsefulInteractions.readDirectedInteractions(interFileName);
			System.out.println("given test inter="+inter);
			System.out.println("isCyclic?"+isCyclic(inter));	
		}
		catch(FileNotFoundException fnfe){
			System.out.println(fnfe);
		}
		catch(NullArgumentException nae){
			System.out.println(nae);
		}
	}

	/**
	 * Describe <code>test</code> method tests the <code>isCyclic</code> method 
	 * with a graph G1(V1,E1) where V1 = {a,b,c,d} and E1 = {(a,b),(b,c),(c,d)} 
	 * and G2(V2,E2) where V2=V1 and E2 = E1 U {(d,b)} 
	 * <br>
	 * Result should be: G1 is not cyclic and G2 is cyclic
	 *
	 */
	public static void test(){
		try {
			ArrayList inter = new ArrayList();
			inter.add(new SimpleGeneEdge("a", "b", 1.0));
			inter.add(new SimpleGeneEdge("b", "c", 1.0));
			inter.add(new SimpleGeneEdge("c", "d", 1.0));
			System.out.println("testing G1="+inter);
			System.out.println("isCyclic?"+isCyclic(inter));
			inter.add(new SimpleGeneEdge("d", "b", 1.0));
			System.out.println("testing G2="+inter);
			System.out.println("isCyclic?"+isCyclic(inter));
		}
		catch(NullArgumentException nae){
			System.out.println(nae);
		}
	}
	public static void main(String[] argv){
		if(argv.length != 1){
			test();
			usage();
			System.exit(0);
		}
		String interFileName = argv[0];
		test(interFileName);
	}
}










