/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: NodeSupports.java,v $
 * $Revision: 1.4 $
 * $Date: 2006-04-28 13:52:49 $
 * $Author: braistedj $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.algorithm.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import org.tigr.microarray.mev.cluster.algorithm.AbortException;
import org.tigr.microarray.mev.cluster.algorithm.AbstractAlgorithm;
import org.tigr.microarray.mev.cluster.algorithm.Algorithm;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmData;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmEvent;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmException;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmParameters;
import org.tigr.microarray.util.BootstrappedMatrixByExps;
import org.tigr.microarray.util.BootstrappedMatrixByGenes;
import org.tigr.microarray.util.JacknifedMatrixByExps;
import org.tigr.microarray.util.JacknifedMatrixByGenes;
import org.tigr.util.FloatMatrix;


public class NodeSupports extends AbstractAlgorithm {
    
    public final static int NONE = 0;
    public final static int BOOT_EXPTS = 1;
    public final static int BOOT_GENES = 2;
    public final static int JACK_EXPTS = 3;
    public final static int JACK_GENES = 4;
    
    
    private boolean stop = false;
    
    public void abort() {
	stop = true;
    }
    
    
    public AlgorithmData execute(AlgorithmData data) throws AlgorithmException {
	AlgorithmParameters map = data.getParams();
	                         
	int function = map.getInt("distance-function", EUCLIDEAN); 
	float factor   = map.getFloat("distance-factor", 1.0f);
	boolean absolute = map.getBoolean("distance-absolute", false);
	boolean drawGeneTree = map.getBoolean("drawGeneTree", true);
	boolean drawExptTree = map.getBoolean("drawExptTree", true);
	boolean optimizeGeneOrdering = map.getBoolean("optimize-gene-ordering");
	boolean optimizeSampleOrdering = map.getBoolean("optimize-sample-ordering");
		/*
		System.out.println("distance-function: " + function);
		System.out.println("factor: " + factor);
		System.out.println("absolute: " + absolute);
		System.out.println("drawGeneTree: " + drawGeneTree);
		System.out.println("drawExptTree: " + drawExptTree);
		 */
	
	int method_linkage = map.getInt("method-linkage", 0);
	int geneTreeAnalysisOption = map.getInt("geneTreeAnalysisOption", 0);; //5 options: no resampling, bootstrap or jackknife exps or genes
	int exptTreeAnalysisOption = map.getInt("exptTreeAnalysisOption", 0);; //5 options: no resampling, bootstrap or jackknife exps or genes
		/*
		System.out.println("method-linkage: " + method_linkage);
		System.out.println("geneTreeAnalysisOption: " + geneTreeAnalysisOption);
		System.out.println("exptTreeAnalysisOption: " + exptTreeAnalysisOption);
		 */
	int geneTreeIterations;
	int exptTreeIterations;
	
	if (geneTreeAnalysisOption == 0) {
	    geneTreeIterations = 0;
	} else {
	    geneTreeIterations = map.getInt("geneTreeIterations", 0);
	}
	
	if (exptTreeAnalysisOption == 0) {
	    exptTreeIterations = 0;
	} else {
	    exptTreeIterations = map.getInt("exptTreeIterations", 0);
	}
	
	//System.out.println("geneTreeIterations: " + geneTreeIterations);
	//System.out.println("exptTreeIterations: " + exptTreeIterations);
	
	FloatMatrix expMatrix = data.getMatrix("experiment");
	
	printFloatMatrix(expMatrix);
	
	int number_of_genes   = expMatrix.getRowDimension();
	int number_of_samples = expMatrix.getColumnDimension();

	Algorithm sub_algo = new HCL();
	AlgorithmData sub_algo_data = new AlgorithmData();
	
	sub_algo_data.addMatrix("experiment", expMatrix);
	sub_algo_data.addParam("distance-factor", String.valueOf(factor));
	sub_algo_data.addParam("distance-absolute", String.valueOf(absolute));
	//HCL expects hcl-distance-function rather than distance-function tag
	sub_algo_data.addParam("hcl-distance-function", String.valueOf(function));
	
	sub_algo_data.addParam("method-linkage", String.valueOf(method_linkage));
	sub_algo_data.addParam("optimize-gene-ordering", String.valueOf(optimizeGeneOrdering));
	sub_algo_data.addParam("optimize-sample-ordering", String.valueOf(optimizeSampleOrdering));
	
	int iterations = (drawGeneTree) ? geneTreeIterations : exptTreeIterations;
	
	//System.out.println("iterations: " + iterations);
	
	AlgorithmEvent event = new AlgorithmEvent(this, AlgorithmEvent.SET_UNITS, iterations);
	fireValueChanged(event);
	event.setId(AlgorithmEvent.PROGRESS_VALUE);
	
	AlgorithmData sub_algo_result;
	int[] child1Arr = null;
	int[] child2Arr = null;
	int[] nodeOrder = null;
	FloatMatrix height = null;
	
	Vector geneTreeSupportVector = new Vector();
	Vector exptTreeSupportVector = new Vector();
	
	if(drawExptTree) {
	    
	    //System.out.println("Inside  if(drawExptTree): ");
	    
	    sub_algo_data.addParam("calculate-genes", "false");
	    sub_algo_result = sub_algo.execute(sub_algo_data);
	    
	    child1Arr = sub_algo_result.getIntArray("child-1-array");
	    child2Arr = sub_algo_result.getIntArray("child-2-array");
	    nodeOrder = sub_algo_result.getIntArray("node-order");
	    height = sub_algo_result.getMatrix("height");

		sub_algo_data.addParam("optimize-gene-ordering", String.valueOf(optimizeGeneOrdering));
		sub_algo_data.addParam("optimize-sample-ordering", String.valueOf(optimizeSampleOrdering));
	    
	    //System.out.println("Original child1 array: ");
	    printIntArray(child1Arr);
	    
	    //System.out.println("Original child2 array: ");
	    printIntArray(child2Arr);
	    
	    //System.out.println("Orig Node Order: ");
	    printIntArray(nodeOrder);
	    
	    //System.out.println("Orig Height: ");
	    printFloatMatrix(height);
	    
	    int[] resampArr1;
	    int[] resampArr2;
	    
	    
	    Vector childrenSetOfOrigTree = getAllSetsOfChildren(child1Arr, child2Arr);
	    
	    //System.out.println("childrenSetOfOrigTree: ");
	    printVectorOfSets(childrenSetOfOrigTree);
	    
	    
	    int numNodes = childrenSetOfOrigTree.size();
	    
	    //System.out.println("numNodes: " + numNodes);
	    
	    
	    int[] supportArray =new int[numNodes];
	    int[] denomArray = new int[numNodes];
	    double[] supportPercentageArray = new double[numNodes];
	    
	    for (int j = 0; j < numNodes; j++) {
		supportArray[j] = 0;
		denomArray[j] = 0;
	    }
	    
	    Vector childrenSetOfResampTree;
	    HashSet indexSetInResampledMatrix = null;
	    HashSet origLeavesOfNodeN;
	    
	    int denom = 0;
	    int support = 0;
	    
	    for (int i=0; i<exptTreeIterations; i++) {
		if (stop) {
		    throw new AbortException();
		}
		event.setIntValue(i);
		event.setDescription("Sample Tree Resampling: iteration "+String.valueOf(i+1));
		fireValueChanged(event);
		
		FloatMatrix resampExpMatrix = null;
		
		if (exptTreeAnalysisOption == 1) {
		    BootstrappedMatrixByExps resampMatrix = new BootstrappedMatrixByExps();
		    resampExpMatrix = resampMatrix.createResampExpMatrixObject(expMatrix);
		    indexSetInResampledMatrix = new HashSet(resampMatrix.resampledIndices);
		}
		
		else if (exptTreeAnalysisOption == 2) {
		    BootstrappedMatrixByGenes resampMatrix = new BootstrappedMatrixByGenes();
		    resampExpMatrix = resampMatrix.createResampExpMatrixObject(expMatrix);
		    indexSetInResampledMatrix = new HashSet(resampMatrix.resampledIndices);
		}
		
		else if (exptTreeAnalysisOption == 3) {
		    JacknifedMatrixByExps resampMatrix = new JacknifedMatrixByExps();
		    resampExpMatrix = resampMatrix.createResampExpMatrixObject(expMatrix);
		    indexSetInResampledMatrix = new HashSet(resampMatrix.resampledIndices);
		}
		
		else if (exptTreeAnalysisOption == 4) {
		    JacknifedMatrixByGenes resampMatrix = new JacknifedMatrixByGenes();
		    resampExpMatrix = resampMatrix.createResampExpMatrixObject(expMatrix);
		    indexSetInResampledMatrix = new HashSet(resampMatrix.resampledIndices);
		}
		
		//System.out.println("resampExpMatrix: ");
		printFloatMatrix(resampExpMatrix);
		
		//System.out.println("indexSetInResampledMatrix: ");
		printHashSet(indexSetInResampledMatrix);
		
		Algorithm resamp_algo = new HCL();
		AlgorithmData resamp_algo_data = new AlgorithmData();
		
		resamp_algo_data.addMatrix("experiment", resampExpMatrix);
		resamp_algo_data.addParam("distance-factor", String.valueOf(factor));
		resamp_algo_data.addParam("distance-absolute", String.valueOf(absolute));
		resamp_algo_data.addParam("hcl-distance-function", String.valueOf(function));
		resamp_algo_data.addParam("method-linkage", String.valueOf(method_linkage));
		resamp_algo_data.addParam("calculate-genes", "false");

		resamp_algo_data.addParam("optimize-gene-ordering", String.valueOf(optimizeGeneOrdering));
		resamp_algo_data.addParam("optimize-sample-ordering", String.valueOf(optimizeSampleOrdering));
		
		AlgorithmData resamp_algo_result = resamp_algo.execute(resamp_algo_data);
		
		resampArr1 = resamp_algo_result.getIntArray("child-1-array");
		resampArr2 = resamp_algo_result.getIntArray("child-2-array");
		
		//System.out.println("child-1-array: ");
		
		printIntArray(resampArr1);
		
		//System.out.println("child-2-array: ");
		
		printIntArray(resampArr2);
		
		childrenSetOfResampTree = new Vector();
		childrenSetOfResampTree = getAllSetsOfChildren(resampArr1, resampArr2); //upto here, have original leaf sets, and resampled leaf sets for this iteration
		
		//System.out.println("childrenSetOfResampTree: ");
		printVectorOfSets(childrenSetOfResampTree);
		
		for (int n = 0; n < numNodes; n++) {
		    origLeavesOfNodeN = new HashSet();
		    origLeavesOfNodeN = (HashSet)childrenSetOfOrigTree.get(n);
		    
		    //System.out.println("origLeavesOfNode " + n + " :");
		    printHashSet(origLeavesOfNodeN);
		    
		    if ((exptTreeAnalysisOption == 1)||(exptTreeAnalysisOption == 3)) {
			
			if (indexSetInResampledMatrix.containsAll(origLeavesOfNodeN)) {
			    denom++;
			    if (leafSetFound((HashSet) origLeavesOfNodeN, childrenSetOfResampTree)) support++;
			}
			
			supportArray[n] += support;
			denomArray[n] += denom;
			
			//System.out.println("supportArray[" + n + "] :" + supportArray[n]);
			//System.out.println("denomArray[" + n + "] :" + denomArray[n]);
			
			
			support = 0; //re-initialize
			denom = 0;
		    }
		    
		    else if ((exptTreeAnalysisOption == 2)||(exptTreeAnalysisOption == 4)) {
			if (leafSetFound((HashSet) origLeavesOfNodeN, childrenSetOfResampTree)) support++;
			supportArray[n] += support;
			//System.out.println("supportArray[" + n + "] :" + supportArray[n]);
			
			support = 0; //re-initialize
		    }
		}
		
		
	    }
	    
	    
	    if ((exptTreeAnalysisOption == 1)||(exptTreeAnalysisOption == 3)) {
		
		for (int k = 0; k < numNodes; k++) {
		    if (denomArray[k] != 0) {supportPercentageArray[k] = supportArray[k]*100/denomArray[k];}
		    else supportPercentageArray[k] = -10;
		    exptTreeSupportVector.add(new Double(supportPercentageArray[k]));
		}
	    }
	    
	    else if ((exptTreeAnalysisOption == 2)||(exptTreeAnalysisOption == 4)) {
		for (int k = 0; k < numNodes; k++) {
		    supportPercentageArray[k] = supportArray[k]*100/exptTreeIterations;
		    exptTreeSupportVector.add(new Double(supportPercentageArray[k]));
		}
	    }
	    
	    //System.out.println("Finished if(drawExptTree): ");
	    
	}
	
	
	if(drawGeneTree) {
	    
	    //System.out.println("Inside  if(drawGeneTree): ");
	    
	    sub_algo_data.addParam("calculate-genes", "true");
	    sub_algo_result = sub_algo.execute(sub_algo_data);
	    
	    child1Arr = sub_algo_result.getIntArray("child-1-array");
	    child2Arr = sub_algo_result.getIntArray("child-2-array");
	    nodeOrder = sub_algo_result.getIntArray("node-order");
	    height = sub_algo_result.getMatrix("height");
	    
	    //System.out.println("Original child1 array: ");
	    printIntArray(child1Arr);
	    
	    //System.out.println("Original child2 array: ");
	    printIntArray(child2Arr);
	    
	    //System.out.println("Orig Node Order: ");
	    printIntArray(nodeOrder);
	    
	    //System.out.println("Orig Height: ");
	    printFloatMatrix(height);
	    
	    int[] resampArr1;
	    int[] resampArr2;
	    
	    Vector childrenSetOfOrigTree = getAllSetsOfChildren(child1Arr, child2Arr);
	    
	    //System.out.println("childrenSetOfOrigTree: ");
	    printVectorOfSets(childrenSetOfOrigTree);
	    
	    int numNodes = childrenSetOfOrigTree.size();
	    
	    //System.out.println("numNodes: " + numNodes);
	    
	    
	    int[] supportArray =new int[numNodes];
	    int[] denomArray = new int[numNodes];
	    double[] supportPercentageArray = new double[numNodes];
	    
	    for (int j = 0; j < numNodes; j++) {
		supportArray[j] = 0;
		denomArray[j] = 0;
	    }
	    
	    Vector childrenSetOfResampTree;
	    HashSet indexSetInResampledMatrix = null;
	    HashSet origLeavesOfNodeN;
	    
	    int denom = 0;
	    int support = 0;
	    
	    for (int i=0; i<geneTreeIterations; i++) {
		if (stop) {
		    throw new AbortException();
		}
		event.setIntValue(i);
		event.setDescription("Gene Tree Resampling: iteration "+String.valueOf(i+1));
		fireValueChanged(event);
		
		FloatMatrix resampExpMatrix = null;
		
		if (geneTreeAnalysisOption == 1) {
		    BootstrappedMatrixByExps resampMatrix = new BootstrappedMatrixByExps();
		    resampExpMatrix = resampMatrix.createResampExpMatrixObject(expMatrix);
		    indexSetInResampledMatrix = new HashSet(resampMatrix.resampledIndices);
		}
		
		else if (geneTreeAnalysisOption == 2) {
		    BootstrappedMatrixByGenes resampMatrix = new BootstrappedMatrixByGenes();
		    resampExpMatrix = resampMatrix.createResampExpMatrixObject(expMatrix);
		    indexSetInResampledMatrix = new HashSet(resampMatrix.resampledIndices);
		}
		
		else if (geneTreeAnalysisOption == 3) {
		    JacknifedMatrixByExps resampMatrix = new JacknifedMatrixByExps();
		    resampExpMatrix = resampMatrix.createResampExpMatrixObject(expMatrix);
		    indexSetInResampledMatrix = new HashSet(resampMatrix.resampledIndices);
		}
		
		else if (geneTreeAnalysisOption == 4) {
		    JacknifedMatrixByGenes resampMatrix = new JacknifedMatrixByGenes();
		    resampExpMatrix = resampMatrix.createResampExpMatrixObject(expMatrix);
		    indexSetInResampledMatrix = new HashSet(resampMatrix.resampledIndices);
		}
		
		//System.out.println("resampExpMatrix: ");
		printFloatMatrix(resampExpMatrix);
		
		//System.out.println("indexSetInResampledMatrix: ");
		printHashSet(indexSetInResampledMatrix);
		
		Algorithm resamp_algo = new HCL();
		AlgorithmData resamp_algo_data = new AlgorithmData();
		
		resamp_algo_data.addMatrix("experiment", resampExpMatrix);
		resamp_algo_data.addParam("distance-factor", String.valueOf(factor));
		resamp_algo_data.addParam("distance-absolute", String.valueOf(absolute));
		resamp_algo_data.addParam("hcl-distance-function", String.valueOf(function));
		resamp_algo_data.addParam("method-linkage", String.valueOf(method_linkage));
		resamp_algo_data.addParam("calculate-genes", "true");

		resamp_algo_data.addParam("optimize-gene-ordering", String.valueOf(optimizeGeneOrdering));
		resamp_algo_data.addParam("optimize-sample-ordering", String.valueOf(optimizeSampleOrdering));
		
		AlgorithmData resamp_algo_result = resamp_algo.execute(resamp_algo_data);
		
		resampArr1 = resamp_algo_result.getIntArray("child-1-array");
		resampArr2 = resamp_algo_result.getIntArray("child-2-array");
		
		//System.out.println("child-1-array: ");
		
		printIntArray(resampArr1);
		
		//System.out.println("child-2-array: ");
		
		printIntArray(resampArr2);
		
		childrenSetOfResampTree = new Vector();
		childrenSetOfResampTree = getAllSetsOfChildren(resampArr1, resampArr2); //upto here, have original leaf sets, and resampled leaf sets for this iteration
		
		//System.out.println("childrenSetOfResampTree: ");
		printVectorOfSets(childrenSetOfResampTree);
		
		for (int n = 0; n < numNodes; n++) {
		    origLeavesOfNodeN = new HashSet();
		    origLeavesOfNodeN = (HashSet)childrenSetOfOrigTree.get(n);
		    
		    //System.out.println("origLeavesOfNode " + n + " :");
		    printHashSet(origLeavesOfNodeN);
		    
		    if ((geneTreeAnalysisOption == 2)||(geneTreeAnalysisOption == 4)) {
			
			if (indexSetInResampledMatrix.containsAll(origLeavesOfNodeN)) {
			    denom++;
			    if (leafSetFound((HashSet) origLeavesOfNodeN, childrenSetOfResampTree)) support++;
			}
			
			supportArray[n] += support;
			denomArray[n] += denom;
			
			//System.out.println("supportArray[" + n + "] :" + supportArray[n]);
			//System.out.println("denomArray[" + n + "] :" + denomArray[n]);
			
			support = 0; //re-initialize
			denom = 0;
		    }
		    
		    else if ((geneTreeAnalysisOption == 1)||(geneTreeAnalysisOption == 3)) {
			if (leafSetFound((HashSet) origLeavesOfNodeN, childrenSetOfResampTree)) support++;
			supportArray[n] += support;
			
			//System.out.println("supportArray[" + n + "] :" + supportArray[n]);
			
			support = 0; //re-initialize
		    }
		}
	    }
	    
	    
	    if ((geneTreeAnalysisOption == 2)||(geneTreeAnalysisOption == 4)) {
		
		for (int k = 0; k < numNodes; k++) {
		    if (denomArray[k] != 0) {supportPercentageArray[k] = supportArray[k]*100/denomArray[k];}
		    else supportPercentageArray[k] = -10;
		    geneTreeSupportVector.add(new Double(supportPercentageArray[k]));
		}
	    }
	    
	    else if ((geneTreeAnalysisOption == 1)||(geneTreeAnalysisOption == 3)) {
		for (int k = 0; k < numNodes; k++) {
		    supportPercentageArray[k] = supportArray[k]*100/geneTreeIterations;
		    geneTreeSupportVector.add(new Double(supportPercentageArray[k]));
		}
	    }
	    
	    //System.out.println("Finished if(drawGeneTree): ");
	    
	}
	
	
	
	
	AlgorithmData result = new AlgorithmData();
	
	if(drawGeneTree) {
	    FloatMatrix geneTreeSupportMatrix = new FloatMatrix(1, geneTreeSupportVector.size());
	    for (int i = 0; i < geneTreeSupportVector.size(); i++) {
		geneTreeSupportMatrix.A[0][i] = ((Double)(geneTreeSupportVector.get(i))).floatValue();
	    }
	    
	    //System.out.println("geneTreeSupportMatrix: ");
	    printFloatMatrix(geneTreeSupportMatrix);
	    
	    result.addMatrix("geneTreeSupportMatrix", geneTreeSupportMatrix);
	    //result.addMatrix("exptTreeSupportMatrix", new FloatMatrix(0,0));
	}
	if(drawExptTree) {
	    FloatMatrix exptTreeSupportMatrix = new FloatMatrix(1, exptTreeSupportVector.size());
	    for (int i = 0; i < exptTreeSupportVector.size(); i++) {
		exptTreeSupportMatrix.A[0][i] = ((Double)(exptTreeSupportVector.get(i))).floatValue();
	    }
	    
	    //System.out.println("exptTreeSupportMatrix: ");
	    printFloatMatrix(exptTreeSupportMatrix);
	    
	    
	    
	    result.addMatrix("exptTreeSupportMatrix", exptTreeSupportMatrix);
	    //result.addMatrix("geneTreeSupportMatrix", new FloatMatrix(0,0));
	}
	
	result.addIntArray("orig-child-1-array", child1Arr);
	result.addIntArray("orig-child-2-array", child2Arr);
	result.addIntArray("orig-node-order", nodeOrder);
	result.addMatrix("orig-height", height);
	
	return result;
    }
    
    
    
    private void identifyChildren(int node, Vector v, int[] arr1, int[] arr2) {// get the vector v of children of a given node in the tree that specified by the two children arrays arr1 and arr2
	
	if (arr1[node] == -1) {return;}
	
	else
	    if ((arr1[arr1[node]] == -1) && (arr1[arr2[node]] == -1)) {
		v.add(new Integer(arr1[node]));
		v.add(new Integer(arr2[node]));
		return;
	    }
	
	    else
		if ((arr1[arr1[node]] == -1) && (arr1[arr2[node]] != -1)) {
		    v.add(new Integer(arr1[node]));
		    identifyChildren(arr2[node], v, arr1, arr2);
		    return;
		}
	
		else
		    if ((arr1[arr1[node]] != -1) && (arr1[arr2[node]] == -1)) {
			identifyChildren(arr1[node], v, arr1, arr2);
			v.add(new Integer(arr2[node]));
			return;
		    }
	
		    else
			if ((arr1[arr1[node]] != -1) && (arr1[arr2[node]] != -1)) {
			    identifyChildren(arr1[node], v, arr1, arr2);
			    identifyChildren(arr2[node], v, arr1, arr2);
			    return;
			}
	
    }
    
    
    private Vector getAllSetsOfChildren(int[] child1arr, int[] child2arr) {// get the vectors of children for all nodes of the tree specified by the two children arrays
	
	Vector allSetsOfChildren = new Vector();
	Vector childrenOfANode = new Vector();
	Set childrenSetOfANode = new HashSet();
	int nLeaves = (int)(child1arr.length/2); // the number of leaves
	for(int j = 0; j < (nLeaves - 1); j++) {// in a binary tree, the number of nodes is (nLeaves - 1)
	    
	    if (stop == true) return null;
	    identifyChildren(j + nLeaves /*+ 1*/, childrenOfANode, child1arr, child2arr); // in origChild1[] and origChild2[], the indices of the nodes go from [nLeaves+1] to [2*nLeaves - 1]
	    childrenSetOfANode = new HashSet(childrenOfANode);
	    allSetsOfChildren.add(childrenSetOfANode);
	    childrenOfANode = new Vector(); //re-initializing childrenOfANode, as we don't want it to contain the values from the previous iteration
	    childrenSetOfANode = new HashSet();//re-initializing childrenSetOfANode, as we don't want it to contain the values from the previous iteration
	}
	
	return allSetsOfChildren;
    }
    
    
    protected void printVectorOfSets(Vector v) {
	
	if (true) return; //Disable the vector printing
	if (v == null) {
	    System.out.println("NULL VECTOR!");
	    return;
	}
	
	if (v.size() == 0) {
	    System.out.println("NO VECTOR!");
	    return;
	}
	
	for (int i = 0; i < v.size(); i++) {
	    System.out.println("Node: " + i);
	    HashSet h = (HashSet) v.elementAt(i);
	    for (Iterator it = h.iterator(); it.hasNext(); ) {
		System.out.print(((Integer) it.next()) + ", ");
	    }
	    System.out.print("\n\n\n");
	}
    }
    
    
    protected void printHashSet(HashSet h) {
	if (true) return; //Disable the printing
	
	for (Iterator it = h.iterator(); it.hasNext(); ) {
	    System.out.print(((Integer) it.next()) + ", ");
	}
	System.out.print("\n\n\n");
	
    }
    
    
    boolean leafSetFound(HashSet leavesAtANode, Vector leafSets) {
	boolean found = false;
	int n = leafSets.size();
	Set leaves;
	for (int i = 0; i < n; i++) {
	    leaves = new HashSet((Collection) leafSets.get(i));
	    if (leavesAtANode.equals(leaves)) {
		found = true;
		return found;
	    }
	}
	
	return found;
    }
    
    
    void printFloatMatrix(FloatMatrix matrix) {
	if (true) return; //Disable the printing
	
	for (int i = 0; i < matrix.getRowDimension(); i++) {
	    for (int j = 0; j < matrix.getColumnDimension(); j++) {
		System.out.print("" + matrix.A[i][j]*100 + " ");
	    }
	    System.out.println();
	}
	
	System.out.println();
    }
    
    
    void printIntArray(int[] array) {
	if (true) return; //Disable the printing
	
	for (int i = 0; i < array.length; i++) {
	    System.out.print("" + array[i]);
	}
	
	System.out.println();
    }
    
}














































