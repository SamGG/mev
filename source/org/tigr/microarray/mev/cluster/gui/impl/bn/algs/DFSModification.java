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
package org.tigr.microarray.mev.cluster.gui.impl.bn.algs;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.io.LineNumberReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Random;
import java.util.Properties;
import org.tigr.microarray.mev.cluster.gui.impl.bn.Useful;
import org.tigr.microarray.mev.cluster.gui.impl.bn.NullArgumentException;
import org.tigr.microarray.mev.cluster.gui.impl.bn.UsefulInteractions;
import org.tigr.microarray.mev.cluster.gui.impl.bn.SimpleGeneEdge;
import org.tigr.microarray.mev.cluster.gui.impl.bn.algs.Cyclic;
/**
 * The class <code>DFSModification</code> contains methods to convert a given undirected graph into a DAG
 * such that a depth-first search is performed on the undirected graph and edges are directed in order of visiting them 
 * and back edges (if any) are directed in increasing order of DFN number to make sure a DAG is obtained in the end. 
 * Note that edges directed in such a way may or may not follow the GO terms direction.
 *
 * @author <a href="mailto:amira@jimmy.harvard.edu></a>
 */
public class DFSModification {
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
    public static ArrayList getDAGFromUndirectedGraph(ArrayList inter) throws NullArgumentException {
	return Cyclic.getDAGFromUndirectedGraph(inter);
    }

    /**
     * The <code>test</code> method tests the <code>getDAGFromUndirectedGraph</code> method
     *
     * @param propsFileName a <code>String</code> containing one required property: 
     * <ul> 
     * <li> sifFileName denoting the name of the file containing all undirected interactions
     * in the modified SIF Cytoscape format with weights: "node1 (pp) node2 = weight" 
     * meaning an interaction from node1 to node2 with the given weight as a double.
     * Example: "node1 (pp) node2 = 1.0". This is the format used in Cytoscape edgeAttributes 
     * file except the initial comment line.
     * </ul>
     * <br>
     * and 2 optional properties:
     * <li> outDirectedInteractionsFileName which is the name of the file where the resulting directed graph is written. 
     * The graph is written in modified Cytoscape directed SIF format with weights: 
     * "node1 (pd) node2 = weight" meaning an interaction from node1 to node2 
     * with the given weight as a double. Example: "node1 (pd) node2 = 1.0". 
     * This is the format used in Cytoscape edgeAttributes file except the initial comment line.
     * The default is "outDirectedInteractions.txt".
     * <li> seed which corresponds to the desired seed of to be used in the random number generator 
     * to randomly direct remaining undirected edges. The default is 1.
     * </ul>
     */
    public static void test(String propsFileName){
	try {
	    Properties props = new Properties();
	    props.load(new FileInputStream(propsFileName));
	    String sifFileName = props.getProperty("sifFileName", null);	    
	    long seed = (long) Integer.parseInt(props.getProperty("seed", "1"));
	    String newInterFileName = props.getProperty("outDirectedInteractionsFileName", "outDirectedInteractions.txt");
	    System.out.println("test()" + sifFileName);
	    Useful.checkFile(sifFileName);
	    ArrayList inter = UsefulInteractions.readInteractionsWithWeights(sifFileName);
	    ArrayList newInter = getDAGFromUndirectedGraph(inter);
	    System.out.println("cyclic? "+Cyclic.isCyclic(newInter));
	    UsefulInteractions.writeSifFileWithWeights(newInter, newInterFileName);
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
	System.out.println("Usage: java DFSModification propsFileName\nExample: java DFSModification testDFSModification.props");
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




