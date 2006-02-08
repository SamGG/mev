/*
 * TEASE.java
 * 
 * @version July 1, 2005
 * @author Annie Liu
 */
package org.tigr.microarray.mev.cluster.algorithm.impl.tease;

import java.util.*;
import java.io.*;

import org.tigr.microarray.mev.cluster.algorithm.AbstractAlgorithm;
import org.tigr.microarray.mev.cluster.algorithm.Algorithm;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmData;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmEvent;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmException;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmParameters;
import org.tigr.util.FloatMatrix;

/**
 * TEASE (Tree EASE) takes in the clustered tree data and 
 * conduct an EASE search at each node level. Each node is 
 * called in turn and all leaves under each node are put into
 * a list that will be used to find the most enriched category
 * in Genome Ontology (GO). This class determines which category 
 * is most enriched at each node level.
 */
public class TEASE extends AbstractAlgorithm {
	
	private EASEAnalysis ease;    //an instance of EASEAnalysis
	private boolean stop;         //boolean that allows the user to terminate the task
							      //while in process
    private int[] child1;         //child-1-array
    private int[] child2;         //child-2-array
    private int[] node;           //node-order
    private String[] annotation;   //annotations correspond to the indices of genes
    private ArrayList rootList;     //a list of all the nodes
    private HashMap leafMap;         //key: root, value: array of the leaves
    private HashMap selectedLeafMap; //select root that fall within min and max limit from leafmap
    
    private AlgorithmEvent event;
    
    /**
     * Constructor, create an instance of TEASE
     * create and initialize
     */
    public TEASE() {
		this.stop = false;
		this.leafMap = new HashMap();
		this.selectedLeafMap = new HashMap();
		this.ease = new EASEAnalysis();       //create an instance of EASEAnalysis
        this.event = new AlgorithmEvent(this, AlgorithmEvent.MONITOR_VALUE, 0);
    }
    
    /**
	 * Execute method is called by TEASEGUI. It takes AlgorithmData 
	 * as parameter, calculate the result, and store it in 
	 * AlgorithmData. This data is returned to the TEASEGUI
	 * 
	 * @param data clustering data and needs to be processed
	 * @return result a FloatMatrix of the result
	 */
	public AlgorithmData execute(AlgorithmData data) throws AlgorithmException {
		//initialize
	    AlgorithmParameters params = data.getParams();
	    AlgorithmData resultData = new AlgorithmData();    //result to be returned
	    HCL hcl = new HCL();
	    
	    resultData = hcl.execute(data);
	    resultData.addParam("hcl-only", String.valueOf(params.getBoolean("hcl-only")));
	    
	    if (params.getBoolean("hcl-only"))
	    	return resultData;
	    
	    setAndDisplayEvent("Complete HCL analysis. Assigning clusters...");
	    //System.out.println("Complete HCL analysis. Assigning clusters...");

	    this.annotation = data.getStringArray("annotation-list");
		this.child1 = resultData.getIntArray("child-1-array");
		this.child2 = resultData.getIntArray("child-2-array");
		this.node = resultData.getIntArray("node-order");
		//print();
		int min = params.getInt("minimum-genes");    //get min
		int max = params.getInt("maximum-genes");
		
		getSelectedLeafMap(min, max);	     //set selectedLeafMap
		setAndDisplayEvent("Complete leafmap. Setting up category map...");
		//System.out.println("Complete leafmap. Setting up category map...");
		
		this.ease.setCategories(data);   //read in annotation file(s)

		setAndDisplayEvent("Waiting for EASE iteration...");
		//System.out.println("Waiting for EASE iteration...");
		int[] rootArray = listToArray(this.rootList);

//		int root;
//		int total = 0;
//		ArrayList list;
//		for(int i = 0; i < rootArray.length; i++) {
//			root = rootArray[i];
//			list = (ArrayList)this.selectedLeafMap.get(new Integer(root));
//			System.out.println("root = "+root+" " +list);
//			total += list.size();
//		}
//		System.out.println("average sample list size = "+(total/rootArray.length));
		
		String[] sample;
		ArrayList indices;
		setAndDisplayEvent("number of EASE iteration: "+rootArray.length);
		//System.out.println("number of EASE iteration: "+rootArray.length);

		for (int i = 0; i < rootArray.length; i++) {       //iterator through all the node:children set in selectedLeafMap
			if (this.stop == true)        //break the loop when user abort
				return null;
			indices = (ArrayList)this.selectedLeafMap.get(new Integer(rootArray[i]));
			sample = mapIndiceToGene(indices);    //acquire genes ID array
			AlgorithmData singleResult = new AlgorithmData();
			singleResult.addParam("upper-boundary", params.getString("upper-boundary"));
			singleResult.addParam("lower-boundary", params.getString("lower-boundary"));
			singleResult.addIntArray("sample-indices", listToArray(indices));
			singleResult.addStringArray("sample-list", sample);       //sample-list with selected gene list
			singleResult = this.ease.runEASEAnalysis(singleResult);     //acquire result for each gene list
			resultData.addResultAlgorithmData(new Integer(rootArray[i]), singleResult);    //store result in AlgorithmData
			
			setAndDisplayEvent("Complete analyzing node: "+ rootArray[i]);
			//System.out.println("Complete analyzing node: "+ rootArray[i]);
//			System.out.println(root);
//			AlgorithmData al = resultData.getResultAlgorithmData(root);
//			System.out.println(al);
//			String[][] re = (String[][])al.getObjectMatrix("result-matrix");
//			for (int i = 0; i < 10; i ++) {
//				for (int j = 0; j < re[i].length; j++)
//					System.out.print(re[i][j]+ " ");
//				System.out.println();
//			}
		}
		setAndDisplayEvent("Complete execution. Exiting TEASE.");
    	//System.out.println("Complete execution. Exiting TEASE.");
    	
    	resultData.addIntArray("node-list", rootArray);
		resultData.addStringArray("name-list", data.getStringArray("name-list"));
		//printDataResult(resultData);
		return resultData; 
	}
	
	  /**
	  * update set event message and fire event value changed to the viewer
	  * @param eventMessage string to be displayed in viewer
	  */
	 private void setAndDisplayEvent(String eventMessage) {
	 	this.event.setDescription("\n" + eventMessage);
	 	this.fireValueChanged(this.event);
	 }
	
	private void print() {
		
		System.out.println("annotation-array");
		for (int i = 0; i < this.annotation.length; i++) {
			System.out.print(this.annotation[i] + " ");
		}
		System.out.println("\nchild1-array");
		for (int i = 0; i < this.child1.length; i++) {
			System.out.print(this.child1[i] + " ");
		}
		
		System.out.println("\nchild2-array" );
		for (int i = 0; i < this.child2.length; i++) {
			System.out.print(this.child2[i] + " ");
		}
		
		System.out.println("\nnode-array");
		for (int i = 0; i < this.node.length; i++) {
			System.out.print(this.node[i] + " ");
		}
	}
	
	private String[] mapIndiceToGene(ArrayList arr) {
		String[] str = new String[arr.size()];
		for (int i = 0; i < arr.size(); i++)
			str[i] = this.annotation[((Integer)arr.get(i)).intValue()];
		return str;
	}
	/**
	 * Change an Integer ArrayList to an int[]
	 * @param rootList
	 * @return
	 */
	private int[] listToArray(ArrayList rootList) {
		int[] roots = new int[rootList.size()];        //change ArrayList rootList into an intArray
		for (int i = 0; i < rootList.size(); i++) 
			roots[i] = ((Integer)rootList.get(i)).intValue();
		return roots;
	}
	
	/**
	 * Termminate the calculation, set parameter stop to true.
	 */
	public void abort() {
		this.stop = true;   //stop the task
		this.ease.abort();
	}
	
//	/**
//	 * recursively get all root nodes that can be reached at the required depth
//	 * Store the node numbers in rootList arraylist
//	 * @param depth current depth during the traversal
//	 * @param root the root node 
//	 */
////	private void setRootList(int depth, int root) {	
////		if (depth == 0)             //to the bottom of the indeicated depth
////			return;
////		if (root < node.length)     //if the root node is a leaf, stop trversal at this branch
////			return;
////		else {                     //update rootList
////			rootList.add(new Integer(root));   //add root node in the rootList
////			setRootList(depth-1, child1[root]);   //search the right branch
////			setRootList(depth-1, child2[root]);   //search the left branch
////		}
////	}
	/**
	 * get selectedLeafMap that contains roots of within the min and max limit
	 * 
	 * @param min
	 * @param max
	 */
	private void getSelectedLeafMap(int min, int max) {	
		HashMap leafMap = getLeafMap();          //acquire map that holds all the nodes and its childs
		this.rootList = new ArrayList();
		ArrayList leaves;
		
		for (int i = 0; i < this.node.length -1 ; i++) {
			Integer root = new Integer(this.node[i]);
			leaves = (ArrayList)leafMap.get(root);
			if (leaves.size() >= min && leaves.size() <= max) {  //if number of genes is within the limit
				this.rootList.add(root);              //store the root in rootList
				this.selectedLeafMap.put(root, leaves);    //store in selectedLeafMap
			}
		}
	}
	
	/**
	 * find all leaves under each node and push it into a hashmap -> leafMap
	 * key: root node, value: array of the leaves under ths root
	 * @return leafMap 
	 */
	private HashMap getLeafMap() {
		HashMap leafMap = new HashMap();
		for (int i = 0; i < this.node.length -1 ; i++) {
			int root = this.node[i];
			ArrayList leafList = new ArrayList();
			Integer node1 = new Integer(this.child1[root]);
			Integer node2 = new Integer(this.child2[root]);
			
			if (leafMap.containsKey(node1))                        //if right child has been searched through
				stitch(leafList, (ArrayList)leafMap.get(node1));   //extract the leaves of the right child
			else                                                   //and stick onto the leafList of the root 
				leafList = findLeaves(leafList, node1.intValue()); //find all leaves of the right child      
			
			if (leafMap.containsKey(node2))                        //if the left child has been searched through 
				stitch(leafList, (ArrayList)leafMap.get(node2));
			else
				leafList = findLeaves(leafList, node2.intValue());
			
//			System.out.println("root = "+root);      
//			for(int k = 0; k < leafList.size(); k++) {
//				System.out.print(leafList.get(k) + " ");
//			}
//			System.out.println();
			
			leafMap.put(new Integer(root), leafList);     //store the key, value set into the leafMap
		}        
		return leafMap;          //key: root, value: leafList leaves under the root
	}
	
	/**
	 * stitch list2 to list1
	 * @param list1 receptor 
	 * @param list2 list to add
	 * @return list1
	 */
	private ArrayList stitch(ArrayList list1, ArrayList list2) {
		for (int i = 0; i < list2.size(); i++)
			list1.add(list2.get(i));
		return list1;
	}
	
	/**
	 * recursive method that finds all leaves under the root node given
	 * @param leafList
	 * @return leafList
	 */
	private ArrayList findLeaves(ArrayList leafList, int root) {
		if (root < this.node.length) {
			leafList.add(new Integer(root));
			return leafList;
		}
		findLeaves(leafList, this.child1[root]);        //search right branch
		findLeaves(leafList, this.child2[root]);	       //search left branch
		return leafList; 
	}
	
	
	
	/*******************************************************************************/
	/**
	 * main method for testing, using arbitrary data set.
	 */
	final static int NUMBER = 1000;
	
	public static void main(String[] args) {
		Algorithm algorithm = new TEASE();
		AlgorithmData data = new AlgorithmData();
		AlgorithmParameters params = new AlgorithmParameters();
		
		int max = 100;
		int min = 10;
		String inputFile = "C:/Documents and Settings/hwl2/Desktop/data/expdata.txt";
		String outputFile = "C:/Documents and Settings/hwl2/Desktop/output.txt";
		String dataFile = "C:/Documents and Settings/hwl2/Desktop/data/RV14-test("+NUMBER+").txt";
		
//		int[] a = {-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,    //child1
//					4,7,10,0,2,1,3,16,15,-1}; 
//		int[] b = {-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,    //child2
//					5,8,6,9,13,11,12,14,17,-1};      //node
//		int[] c = {10,11,12,13,14,15,16,17,18,-1};
		
		try {
			BufferedReader buff = new BufferedReader(new FileReader(dataFile));
			data = readData(buff);
			System.out.println("Completed reading data. Waiting for HCL data...");
			buff.close();
			//FloatMatrix matrix = FloatMatrix.read(buff);
			//data.addMatrix("experiment", matrix);
			//System.out.println("matrix is null ? "+(matrix == null));
//			System.out.println("TEASE 279");
//			for (int x = 0; x < matrix.getRowDimension(); x ++) {  //************************************
//				for(int y = 0; y < matrix.getColumnDimension(); y ++)
//					System.out.print(matrix.get(x,y)+" ");
//				System.out.println();
//			}
		}catch (IOException e) {
			e.printStackTrace();
		}

		String[] annotations = new String[1];
		//String[] indi = {"780", "5982", "3310", "7849", "2978", "7318", "7067", "11099", "6352", "1571"};
		annotations[0] = "C:/MeV3.1/data/ease/Data/Class/GO Biological Process.txt";
		
		data.addParam("minimum-genes", String.valueOf(min));
		data.addParam("maximum-genes", String.valueOf(max));
//		data.addIntArray("child-1-array", a);
//		data.addIntArray("child-2-array", b);
//		data.addIntArray("node-order", c);
		
		data.addParam("perform-cluster-analysis", "false");
		data.addParam("trim-option", "NO_TRIM");
//		data.addStringArray("indices-list", indi);
//		data.addStringArray("population-list", indi);
		data.addStringArray("annotation-file-list", annotations);
		
        data.addParam("hcl-distance-function", "1");   //pearson correlation
        data.addParam("hcl-distance-absolute", "false"); //relative distance
        data.addParam("method-linkage", "0");  //average-link
        
		//execute and print result in indicated output file
        try {		
        	AlgorithmData result = algorithm.execute(data);  //execute
			int[] roots = result.getIntArray("node-list");
			
        	PrintWriter out = new PrintWriter(new FileOutputStream(outputFile));  //create output writer
        	out.println("size of data set: " + NUMBER);
        	out.println("number of iteration: " + roots.length);
        	out.println("\n\n");

			for (int i = 0; i < roots.length; i ++) {
				out.println("node = " +roots[i]);
				AlgorithmData indiData = result.getResultAlgorithmData(new Integer(roots[i]));
				String[] names = result.getStringArray("name-list");
				printResult(indiData, names, out);
			}
			out.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	private static void printDataResult(AlgorithmData data) {
		int[] nodes = data.getIntArray("node-list");
		String[] names = data.getStringArray("name-list");
		String outputFile = "C:/Documents and Settings/hwl2/Desktop/output.txt";
		AlgorithmData single;
		try {
			PrintWriter out = new PrintWriter(new FileOutputStream(outputFile));  //create output writer
        	out.println("size of data set: ");
        	out.println("number of iteration: " + nodes.length);
        	out.println("\n\n");
			for (int i = 0; i < nodes.length; i++) {
				out.println("node = " +nodes[i]);
				single = data.getResultAlgorithmData(new Integer(nodes[i]));
				printResult(single, names, out);
			}
			out.close();
		}catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void printResult(AlgorithmData result, String[] names, PrintWriter out) throws IOException{
		String[][] re = (String[][])result.getObjectMatrix("result-matrix");

		String[] sample = result.getStringArray("sample-list");  //print sample genes
		int[] indices = result.getIntArray("sample-indices");
//		System.out.println(sample == null);
//		System.out.println(indices == null);
//		System.out.println(names == null);
		for (int j = 0; j < sample.length; j++) {
			out.print(sample[j]+ ": "/*+ names[indices[j]]+"\t"*/);
		}
		out.println();
		
		String[] header = result.getStringArray("header-names");  //print header
		for (int j = 0; j < header.length; j++) {
			out.print(header[j]+ "\t");
		}
		out.println();
		
		for (int x = 0;  x< 5; x ++) {   //print categories
			for (int y = 0; y < re[x].length; y++)
				out.print(re[x][y]+ "\t");
			out.println();
		}
		out.println();
		out.println();
	}
	
	private static AlgorithmData readData(BufferedReader buff) throws IOException{
		//System.out.println(buff == null);
		AlgorithmData data = new AlgorithmData();
		FloatMatrix matrix = new FloatMatrix(NUMBER, 12);
		String line = buff.readLine();
		String[] genes = new String[NUMBER];
		String[] names = new String[NUMBER];
		int i = 0;
		int parse1 = 0;
		int parse2 = 0;
		
		while(i < NUMBER) {
			line = buff.readLine();
			//System.out.println(line);
			parse2 = line.indexOf("\t");
	        genes[i] = line.substring(0, parse2);
	        parse1 = parse2+1;
	        parse2 = line.indexOf("\t", parse1+1);
	        names[i] = line.substring(parse1, parse2);
			for (int j = 0; j < 11; j++) {
                parse1 = parse2+1;
                parse2 = line.indexOf( "\t", parse1+1);
                //System.out.println("parse1 = "+parse1+"  parse2 = "+parse2);
				String exp = line.substring(parse1, parse2); 
				matrix.set(i, j, (Float.valueOf(exp)).floatValue());
            }
			String exp = line.substring(parse2+1, line.length()); 
			matrix.set(i, 11, (Float.valueOf(exp)).floatValue());
			i++;
		}
		
//		for (int x = 0; x < matrix.getRowDimension(); x ++) {  //************************************
//			for(int y = 0; y < matrix.getColumnDimension(); y ++)
//				System.out.print(matrix.get(x,y)+" ");
//			System.out.println();
//		}
//		for (int k = 0; k < genes.length; k ++) {
//			System.out.print(genes[k]+"**");
//		}
		data.addMatrix("experiment", matrix);
		data.addStringArray("population-list", genes);
		data.addStringArray("annotation-list", genes);
		data.addStringArray("name-list", names);
		return data;
	}
}
