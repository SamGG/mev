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
/* SifToXMLBif.java
 * Copyright (C) 2005 Amira Djebbari
 */
package org.tigr.microarray.mev.cluster.gui.impl.bn.prepareXMLBif;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Properties;
import org.tigr.microarray.mev.cluster.gui.impl.bn.Useful;
import org.tigr.microarray.mev.cluster.gui.impl.bn.UsefulInteractions;
import org.tigr.microarray.mev.cluster.gui.impl.bn.SimpleGeneEdge;
import org.tigr.microarray.mev.cluster.gui.impl.bn.algs.Cyclic;
import org.tigr.microarray.mev.cluster.gui.impl.bn.NullArgumentException;
import org.tigr.microarray.mev.cluster.gui.impl.bn.NotDAGException;
/**
 * The class <code>SifToXMLBif</code> takes in a directed acyclic graph (DAG) in modified Cytoscape SIF format with weights
 * and transforms it into an XML BIF format (standard format to encode Bayesian networks that can be used as input to WEKA).
 * Inspired by: Remco Bouckaert of the WEKA team
 * @author <a href="mailto:amirad@jimmy.harvard.edu"></a>
 */
public class SifToXMLBif {
    /**
     * The variable <code>debug</code> is a debug flag.
     */
    public static boolean debug = false;

    /**
     * Describe <code>createXMLBifGivenSifFile</code> method takes in a directed acyclic graph
     * in modified Cytoscape SIF format with weights and transforms it into an XML BIF format
     * (standard format to encode Bayesian networks that can be used as input to WEKA).
     *
     * @param interactions an <code>ArrayList</code> of <code>SimpleGeneEdge</code> objects 
     * representation of the given graph 
     * @param names an <code>ArrayList</code> of <code>String</code>s corresponding to 
     * the random variable (RV) names to be included in the XML BIF format.
     * @param pw a <code>PrintWriterXML</code> where the XML BIF 0.3 description of the given DAG with the given CPTs will be written
     * @param props a <code>Properties</code> containing 2 required properties:
     * <ul>
     * <li> sifFileName which denotes the name of the file where the given graph can be found 
     * in modified Cytoscape directed SIF format with weights: 
     * "node1 (pd) node2 = weight" meaning an interaction from node1 to node2 
     * with the given weight as a double. Example: "node1 (pd) node2 = 1.0". 
     * This is the format used in Cytoscape edgeAttributes file except the initial comment line.
     * <li> namesFileName which denotes the name of the file where the variable names to be written 
     * to the XML BIF file can be found
     * </ul>
     * and 6 optional properties:
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
     * will be saved. The default is "outXMLBifFileName".
     * @exception NullArgumentException if an error occurs because at least one of the 
     * given parameters interactions or names was null.
     */
    public static void createXMLBifGivenSifFile(ArrayList interactions, ArrayList names, PrintWriter pw, Properties props) throws NullArgumentException, NotDAGException {
	if((interactions == null) || (names == null) || props == null){
	    throw new NullArgumentException("At least one of interactions or names or props was null\ninteractions="+interactions+"\nnames="+names+"\nprops="+props);
	}	
	if(Cyclic.isCyclic(interactions)){
	    throw new NotDAGException("The given graph is cyclic!\ninteractions="+interactions);
	}
	boolean uniform = true;
	if(props.getProperty("distributionFromWeights", "false").equals("true")){
	    uniform = false;
	}	
	System.gc();
	ArrayList values = new ArrayList();
	int numStates = Integer.parseInt(props.getProperty("numStates", "3"));
	ArrayList gene1values = new ArrayList(numStates);
	for(int i = 0; i < numStates; i++){
	    gene1values.add(props.getProperty("state"+i, "state"+i));
	}
	for(int i = 0; i < names.size(); i++){
	    values.add((ArrayList)gene1values.clone());
	}
	int numClasses = Integer.parseInt(props.getProperty("numClasses", "2"));
	ArrayList classValues = new ArrayList(numClasses);
	for(int i = 0; i < numClasses; i++){
	    classValues.add(props.getProperty("class"+i, "class"+i));
	}
	int maxParents = SifToXMLBifUtil.getMaxParents(interactions, names);
	if(debug){
	    System.out.println("maxParents="+maxParents);
	}
	ArrayList[][] distributions = null;
	//System.out.println("before distributions");
	if(!uniform){
	    distributions = SifToXMLBifUtil.computeDistributions(names, values, maxParents, interactions);
	   // System.out.println("after distributions by weights");
	}
	else {
	    System.gc();
	    distributions = SifToXMLBifUtil.makeUniformDistributions(names, values, maxParents, interactions);
	   // System.out.println("after distributions uniform");
	}
	//System.out.println("before toXMLBIF");
	toXMLBIF03(classValues,interactions,names.size(),names,values,distributions,pw);
    }


    /**
     * The <code>toXMLBIF03</code> method takes in a given DAG and given CPTs corresponding to a Bayesian network and
     * returns a description of it in XML BIF 0.3 format
     * See <a href=http://www-2.cs.cmu.edu/~fgcozman/Research/InterchangeFormat/>The Interchange Format for Bayesian Networks</a>
     * for details on XML BIF.
     *
     * @param classValues an <code>ArrayList</code> of <code>String</code>s corresponding to values
     * the CLASS variable can take
     * @param interactions an <code>ArrayList</code> of <code>SimpleGeneEdge</code> objects corresponding to the given DAG
     * @param numAttributes an <code>int</code> corresponding to the number of attributes or variables 
     * @param names an <code>ArrayList</code> of <code>String</code> corresponding to the names of all the given variables
     * @param values an <code>ArrayList</code> of <code>String</code> corresponding to the values the attributes can take
     * @param distributions an <code>ArrayList[][]</code> which is 2D array of s<code>ArrayList</code>s 
     * representing the CPT for each RV, namely, distributions for each RV given its parents. 
     * The distributions may be computed using uniform distribution or from weights.
     * @param pw a <code>PrintWriterXML</code> where the XML BIF 0.3 description of the given DAG with the given CPTs will be written
     */
    public static void toXMLBIF03(ArrayList classValues, ArrayList interactions, int numAttributes, ArrayList names, ArrayList values, ArrayList[][] distributions, PrintWriter pw) {
	if (interactions == null) {
	    pw.print("<!--No model built yet-->");
	}
	pw.print("<?xml version=\"1.0\"?>\n");
	pw.print("<!-- DTD for the XMLBIF 0.3 format -->\n");
	pw.print("<!DOCTYPE BIF [\n");
	pw.print("	<!ELEMENT BIF ( NETWORK )*>\n");
	pw.print("	      <!ATTLIST BIF VERSION CDATA #REQUIRED>\n");
	pw.print("	<!ELEMENT NETWORK ( NAME, ( PROPERTY | VARIABLE | DEFINITION )* )>\n");
	pw.print("	<!ELEMENT NAME (#PCDATA)>\n");
	pw.print("	<!ELEMENT VARIABLE ( NAME, ( OUTCOME |  PROPERTY )* ) >\n");
	pw.print("	      <!ATTLIST VARIABLE TYPE (nature|decision|utility) \"nature\">\n");
	pw.print("	<!ELEMENT OUTCOME (#PCDATA)>\n");
	pw.print("	<!ELEMENT DEFINITION ( FOR | GIVEN | TABLE | PROPERTY )* >\n");
	pw.print("	<!ELEMENT FOR (#PCDATA)>\n");
	pw.print("	<!ELEMENT GIVEN (#PCDATA)>\n");
	pw.print("	<!ELEMENT TABLE (#PCDATA)>\n");
	pw.print("	<!ELEMENT PROPERTY (#PCDATA)>\n");
	pw.print("]>\n");
	pw.print("\n");
	pw.print("\n");
	pw.print("<BIF VERSION=\"0.3\">\n");
	pw.print("<NETWORK>\n");
	pw.print("<NAME>" + "relation name goes here" + "</NAME>\n");
	// writing class var
	pw.print("<VARIABLE TYPE=\"nature\">\n");
	pw.print("<NAME>CLASS</NAME>\n");
	for(int iClassValue = 0; iClassValue < classValues.size(); iClassValue++){
	    pw.print("\t<OUTCOME>"+classValues.get(iClassValue)+"</OUTCOME>\n");
	}
	pw.print("</VARIABLE>\n");
	
	for (int iAttribute = 0; iAttribute < numAttributes; iAttribute++) {
	    pw.print("<VARIABLE TYPE=\"nature\">\n");
	    pw.print("<NAME>" + names.get(iAttribute) + "</NAME>\n");
	    for (int iValue = 0; iValue < ((ArrayList) values.get(iAttribute)).size(); iValue++) {
		pw.print("\t<OUTCOME>" + ((ArrayList) values.get(iAttribute)).get(iValue) + "</OUTCOME>\n");
	    }
	    pw.print("</VARIABLE>\n");
	}
	// writing CLASS def
	pw.print("<DEFINITION>\n");
	pw.print("<FOR>CLASS</FOR>\n");
	pw.print("<TABLE>\n");
	ArrayList classDistr = SifToXMLBifUtil.getOneUniformDistribution(classValues.size());
	for(int iClassValue =0; iClassValue < classValues.size(); iClassValue++){
	    pw.print(classDistr.get(iClassValue));
	    pw.print(" ");
	}
	pw.print("\n</TABLE>\n");
	pw.print("</DEFINITION>\n");
	
	for (int iAttribute = 0; iAttribute < numAttributes; iAttribute++) {
	    pw.print("<DEFINITION>\n");
	    pw.print("<FOR>" + names.get(iAttribute) + "</FOR>\n");
	    ArrayList parents = SifToXMLBifUtil.getParents(names,iAttribute,interactions);
	    for (int iParent = 0; iParent < parents.size(); iParent++) {
		pw.print(
			 "\t<GIVEN>"
			 + parents.get(iParent)
			 + "</GIVEN>\n");
	    }
	    pw.print("<TABLE>\n");
	    for (int iParent = 0; iParent < SifToXMLBifUtil.getCardinalityOfParents(parents,iAttribute,values); iParent++) {
		for (int iValue = 0; iValue < ((ArrayList) values.get(iAttribute)).size(); iValue++) {
		    pw.print(((ArrayList) distributions[iAttribute][iParent]).get(iValue));
		    pw.print(" ");
		}
		pw.print("\n");
	    }
	    pw.print("</TABLE>\n");
	    pw.print("</DEFINITION>\n");
	}
	pw.print("</NETWORK>\n");
	pw.print("</BIF>\n");	
    } 

    /**
     * The <code>test</code> method tests the <code>createXMLBifGivenSifFile</code> method
     *
     * @param propsFileName a <code>String</code> containing required and optional properties
     * see <code>createXMLBifGivenSifFile</code> method for details.
     */
    public static void test(String propsFileName) {
	try {
	    String XMLBifStr = null;
	    Properties props = new Properties();
	    props.load(new FileInputStream(propsFileName));
	    String sifFileName = props.getProperty("sifFileName",null);
	    String namesFileName = props.getProperty("namesFileName",null);
	    Useful.checkFile(sifFileName);
	    Useful.checkFile(namesFileName);
	    ArrayList interactions = UsefulInteractions.readDirectedInteractionsWithWeights(sifFileName);
	    ArrayList names = Useful.readNamesFromFile(namesFileName);
	    String outXMLBifFileName = props.getProperty("outXMLBifFileName","out_bif.xml");
	    PrintWriter pw = new PrintWriter(new FileOutputStream(outXMLBifFileName), true);
	    pw.close();
	    createXMLBifGivenSifFile(interactions, names, pw, props);
	}
	catch(IOException ioe){
	    System.out.println(ioe);
	    ioe.printStackTrace();
	}
	catch(NullArgumentException nae){
	    System.out.println(nae);
	    nae.printStackTrace();
	}
	catch(NotDAGException nde){
	    System.out.println(nde);
	    nde.printStackTrace();
	}
    }
    public static void main(String[] argv){
	if(argv.length!=1){
	    System.out.println("Usage: java SifToXMLBif propsFileName\nExample: java SifToXMLBif prepareXMLBif.props");
	    System.exit(0);
	}
	String propsFileName = argv[0];
	test(propsFileName);
    }
}


