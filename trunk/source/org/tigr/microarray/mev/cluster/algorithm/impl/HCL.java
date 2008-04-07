/*
Copyright @ 1999-2004, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: HCL.java,v $
 * $Revision: 1.3 $
 * $Date: 2005/03/10 15:45:20 $
 * $Author: braistedj $
 * $State: Exp $
 */

package org.tigr.microarray.mev.cluster.algorithm.impl;

import org.tigr.microarray.mev.cluster.algorithm.AbortException;
import org.tigr.microarray.mev.cluster.algorithm.AbstractAlgorithm;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmData;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmEvent;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmException;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmParameters;
import org.tigr.util.FloatMatrix;
import java.util.Arrays;

public class HCL extends AbstractAlgorithm {
    
    private boolean stop = false;
    
    private int parentless;
    private double TreeHeight;
    private int Assigned;
    private int OptProgress;
    
    public HCL() {}
    
    public void abort() {
	stop = true;
    }
    
    public AlgorithmData execute(AlgorithmData data) throws AlgorithmException {
	
	FloatMatrix expMatrix = data.getMatrix("experiment");
	if (expMatrix == null) {
	    throw new AlgorithmException("Input data is absent.");
	}
	AlgorithmParameters map = data.getParams();
	
	int function = map.getInt("hcl-distance-function", EUCLIDEAN);
	float factor; // = map.getFloat("distance-factor", 1.0f);
	boolean absolute = map.getBoolean("hcl-distance-absolute", false);
	boolean genes = map.getBoolean("calculate-genes", true);
	boolean optimizeOrdering = map.getBoolean("optimize-sample-ordering", false);
	if (genes) optimizeOrdering = map.getBoolean("optimize-gene-ordering", false);
	int method = map.getInt("method-linkage", 0);
	
	//============= Init ====================
	
	int n;
	if (genes) {
	    n = expMatrix.getRowDimension();
	} else {
	    n = expMatrix.getColumnDimension();
	}
	
	int two_n = 2*n;
	Assigned = n;
	parentless = n;
	TreeHeight = 0;
	double MaxCorrelation = 0;
	
	float[] Height = new float[two_n];
	float[][] OptimalSum = new float[n][n];
	int[] Parent = new int[two_n];
	int[] Child1 = new int[two_n];
	int[] Child2 = new int[two_n];
	int[] NodeHeight = new int[two_n];
	int[] NodeOrder  = new int[n];
	int[] NumberOfChildren = new int[two_n];
	int[][] LeavesUnder = new int[two_n][];
	
	
	for (int i=0; i<two_n; ++i) {
	    Height[i] = 0.0f;
	    Parent[i] = -1;
	    Child1[i] = -1;
	    Child2[i] = -1;
	    NodeHeight[i] = 0;
	}
	
	for (int i=0; i<n; ++i) {
	    NodeOrder[i]=-1;
	    NumberOfChildren[i]=1;
	}
	//======== Init =========
	float[][] SimilarityMatrix = new float[n][];
	float[][] squareMatrix = new float[n][n];
	float[] Min = new float[n];
	int[] MinIndex = new int[n];
	final int UNITS = 200;
	
	AlgorithmEvent event = null;
	event = new AlgorithmEvent(this, AlgorithmEvent.SET_UNITS, UNITS, "Creating similarity matrix");
	// set progress limit
	fireValueChanged(event);
	event.setId(AlgorithmEvent.PROGRESS_VALUE);
	event.setIntValue(0);
	// set zero position
	fireValueChanged(event);
	int i;
	int CurrentProgress = 0;
	int OldCurrentProgress = 0;
	double Factor=UNITS/(double)n;
      /*  if ((function==PEARSON)           ||
	    (function==PEARSONUNCENTERED) ||
	    (function==PEARSONSQARED)     ||
	    (function==COSINE)            ||
	    (function==COVARIANCE)        ||
	    (function==DOTPRODUCT)        ||
	    (function==SPEARMANRANK)      ||
	    (function==KENDALLSTAU)) {
	    factor = -1.0f;
	} else {
	    factor = 1.0f;
	}
       */
	
	factor = (float)1.0;  //factor is used as an optional scaling factor
	for (i=1; i<n; ++i) {
	    CurrentProgress=(int)(i*Factor);
	    if (CurrentProgress>OldCurrentProgress) {
		event.setIntValue(CurrentProgress);
		fireValueChanged(event);
		OldCurrentProgress=CurrentProgress;
	    }
	    SimilarityMatrix[i] = new float[n];

	    Min[i] = Float.POSITIVE_INFINITY;
	    for (int j=0; j<i; ++j) {
		if (stop) {
		    throw new AbortException();
		}
		
		if (genes) {
		    SimilarityMatrix[i][j] = ExperimentUtil.geneDistance(expMatrix, null, i, j, function, factor, absolute);//ExpMatrix.GeneDistance(i,j,null);
		} else {
		    SimilarityMatrix[i][j] = ExperimentUtil.distance(expMatrix, i, j, function, factor, absolute); //ExpMatrix.ExperimentDistance(i,j);
		}
		squareMatrix[i][j] = SimilarityMatrix[i][j]; //square matrix created from  
		squareMatrix[j][i] = SimilarityMatrix[i][j]; //triangular Similarity Matrix
		if (SimilarityMatrix[i][j] < Min[i]) {
		    Min[i] = SimilarityMatrix[i][j];
		    MinIndex[i] = j;
		}
	    }
	}
	
	//========================================
	
	if (stop) {
	    throw new AbortException();
	}
	
	event = new AlgorithmEvent(this, AlgorithmEvent.SET_UNITS, UNITS, "Calculating tree");
	// set progress limit
	fireValueChanged(event);
	event.setId(AlgorithmEvent.PROGRESS_VALUE);
	event.setIntValue(0);
	// set zero position
	fireValueChanged(event);
	
	long CalculatedNodes=0;
	CurrentProgress=0;
	OldCurrentProgress=0;
	Factor=UNITS/(double)n;
	int j,k,p;
	int testcount = 0;
	int Counter;
	int NodeCounter = 0;
	double MaxDistance=0;
	double MinDistance=Double.POSITIVE_INFINITY;
	MaxCorrelation=Double.POSITIVE_INFINITY;
	double MinCorrelation=Double.POSITIVE_INFINITY;
	int owner[] = new int[n];
	for (i=0; i<n; i++)
	    owner[i] = i;
	
	while (parentless > 1) { 				//main loop runs until every node except the root node is assigned a parent node.
	    if (stop) {
		throw new AbortException();
	    }
	    CurrentProgress=(int)((CalculatedNodes+1)*Factor);
	    if (CurrentProgress>OldCurrentProgress) {
		event.setIntValue(CurrentProgress);
		fireValueChanged(event);
		OldCurrentProgress=CurrentProgress;
	    }
	    CalculatedNodes++;
	    double close_d = Double.POSITIVE_INFINITY;              // first find the closest pair
	    double test_d = Double.POSITIVE_INFINITY;
	    double TestMin = Double.POSITIVE_INFINITY;
	    int test_i = -2;
	    int test_j = -2;
	    int close_i = -2, close_j = -2;
	    for (i=1; i<n; ++i) {  						//finds closest remaining elements
		if (owner[i] != -1) {  						//overlooks elements already counted
		    if (Min[i] < test_d) {
			test_d = Min[i];
			test_i = i;
			test_j = MinIndex[i];
		    }
		}
	    }
	    i = close_i; // lexa: ???
	    j = close_j; // lexa: ???
	    i = test_i;
	    j = test_j;
            
                        //JCB 
          //  if(i >= n || j >= n || i < 0 || j < 0)
              //  break;
            
	    close_d = test_d;
	    double height_k = close_d;                              //was close_d/2.0 ????????
	    if ((Math.abs(close_d)>0) && (Math.abs(close_d)<MinDistance))
		MinDistance=Math.abs(close_d);
	    //       if ((close_d>0) && (close_d<MinDistance)) MinDistance=close_d;
	    if ((close_d!=1) && (close_d<MaxCorrelation))
		MaxCorrelation=close_d;
	    if ((close_d>MaxCorrelation) && (close_d<MinCorrelation))
		MinCorrelation=close_d;
	    if (close_d>MaxDistance)
		MaxDistance=close_d;
	    try {
		//System.out.println(" owner["+i+"]="+owner[i]);
		if (owner[i]>=n && Height[owner[i]]>height_k) {        // Gene1 already assignd to a node, was >= ?!
		    k = owner[i];
		    AssertParentage(Parent,NumberOfChildren, Child1, Child2,owner[j],k);
		} else if (owner[j]>=n && Height[owner[j]]>height_k) { // Gene2 already assignd to node was >= ?!
		    k = owner[j];
		    AssertParentage(Parent,NumberOfChildren,Child1, Child2,owner[i],k);
		} else {
		    k = NewNode(Height, height_k);
		    AssertParentage(Parent,NumberOfChildren,Child1, Child2,owner[i],k);
		    AssertParentage(Parent,NumberOfChildren,Child1, Child2,owner[j],k);
		}
		
		
		
		
		NodeOrder[NodeCounter]=k;
		NodeHeight[k]=Math.max(NodeHeight[Child1[k]]+1,NodeHeight[Child2[k]]+1);
	    } catch (Exception e) {
		e.printStackTrace();
		fireValueChanged(new AlgorithmEvent(this, AlgorithmEvent.WARNING, 0, "Error: "+e.toString()+" - Height("+String.valueOf(height_k)+","+")"));
		k=0;
	    }

	    NodeCounter++;
	    owner[i] = k;    //node k replaces child node or leaf
	    owner[j] = -1;  //eliminates j from search
	    if (method == -1) {       // minimum method
		for (p=0; p<j; ++p)
		    if (owner[p] != -1) SimilarityMatrix[i][p] = (float)Math.min(SimilarityMatrix[i][p],SimilarityMatrix[j][p]);
		for (p=j+1; p<i; ++p)
		    if (owner[p] != -1) SimilarityMatrix[i][p] = (float)Math.min(SimilarityMatrix[i][p],SimilarityMatrix[p][j]);
		for (p=i+1; p<n; ++p)
		    if (owner[p] != -1) SimilarityMatrix[p][i] = (float)Math.min(SimilarityMatrix[p][i],SimilarityMatrix[p][j]);
	    } else if (method == +1) {   // maximum method
		for (p=0; p<j; ++p)
		    if (owner[p] != -1) SimilarityMatrix[i][p] = (float)Math.max(SimilarityMatrix[i][p],SimilarityMatrix[j][p]);
		for (p=j+1; p<i; ++p)
		    if (owner[p] != -1) SimilarityMatrix[i][p] = (float)Math.max(SimilarityMatrix[i][p],SimilarityMatrix[p][j]);
		for (p=i+1; p<n; ++p)
		    if (owner[p] != -1) SimilarityMatrix[p][i] = (float)Math.max(SimilarityMatrix[p][i],SimilarityMatrix[p][j]);
	    } else if (method == 2) {             // average method
		//                int schrott=NumberOfChildren[owner[j]]+NumberOfChildren[owner[i]];
		//            System.out.println(NumberOfChildren[owner[i]]);
		for (p=0; p<j; ++p)
		    if (owner[p] != -1) SimilarityMatrix[i][p] = (float)((SimilarityMatrix[i][p]*NumberOfChildren[owner[i]] +
		    SimilarityMatrix[j][p]*NumberOfChildren[owner[j]])/
		    (2.0*Math.min(NumberOfChildren[owner[i]],NumberOfChildren[owner[j]])));
		for (p=j+1; p<i; ++p)
		    if (owner[p] != -1) SimilarityMatrix[i][p] = (float)((SimilarityMatrix[i][p]*NumberOfChildren[owner[i]] +
		    SimilarityMatrix[p][j]*NumberOfChildren[owner[j]])/
		    (2.0*Math.min(NumberOfChildren[owner[i]],NumberOfChildren[owner[j]])));
		for (p=i+1; p<n; ++p)
		    if (owner[p] != -1) SimilarityMatrix[p][i] = (float)((SimilarityMatrix[p][i]*NumberOfChildren[owner[i]] +
		    SimilarityMatrix[p][j]*NumberOfChildren[owner[j]])/
		    (2.0*Math.min(NumberOfChildren[owner[i]],NumberOfChildren[owner[j]])));
	    } else if (method == 0) {             // average method
		for (p=0; p<j; ++p)
		    if (owner[p] != -1) SimilarityMatrix[i][p] = (float)((SimilarityMatrix[i][p] + SimilarityMatrix[j][p])/2.0);
		for (p=j+1; p<i; ++p)
		    if (owner[p] != -1) SimilarityMatrix[i][p] = (float)((SimilarityMatrix[i][p] + SimilarityMatrix[p][j])/2.0);
		for (p=i+1; p<n; ++p)
		    if (owner[p] != -1) SimilarityMatrix[p][i] = (float)((SimilarityMatrix[p][i] + SimilarityMatrix[p][j])/2.0);
	    }
	    for (p=j; p<n; p++) {
		if (owner[p]!=-1) {
		    if ((MinIndex[p]==j) || (MinIndex[p]==i)) {
			Min[p]=Float.POSITIVE_INFINITY;
			for (int l=0; l<p; l++) {
			    if (owner[l] != -1) {
				if (SimilarityMatrix[p][l]<Min[p]) {
				    Min[p]= SimilarityMatrix[p][l];
				    MinIndex[p]=l;
				}
			    }
			}
		    }
		}
	    }
	}
	if (optimizeOrdering&&(n>1)){
		//optimizes leaf ordering once the tree construction has finished.
	 	for (int leavesInit = n; leavesInit<2*n-1; leavesInit++){
	    	LeavesUnder[leavesInit]= new int[NumberOfChildren[leavesInit]];
	    }
	    for (int leavesInit = 0; leavesInit<n; leavesInit++){
	    	LeavesUnder[leavesInit]= new int[1];
	    	LeavesUnder[leavesInit][0]= leavesInit;
	    }
		MakeLeavesUnderMatrix(2*n-2, Child1, Child2, n, LeavesUnder, NumberOfChildren);
		
		float match;
		float bestMatch = Float.POSITIVE_INFINITY;
		int bestU=0;
		int bestW=0;
		OptProgress = 0;
		AlgorithmEvent optevent = null;
		optevent = new AlgorithmEvent(this, AlgorithmEvent.SET_UNITS, n*n/2, "Optimizing Leaf Order");
		fireValueChanged(optevent);
		optevent.setId(AlgorithmEvent.PROGRESS_VALUE);
		optevent.setIntValue(0);
		fireValueChanged(optevent);
		
		/* Creates a matrix containing the optimal sums of similarities of all leaves between a given
		 * two leaves that have been fixed to be a maximum distance apart.
			 */
	   	for (int reset=0; reset<n; reset++){ //Child arrays are changed so that the Child[i]=i for all leaves
			Child1[reset]=reset;
			Child2[reset]=reset;
		}
		MakeOptimalSumMatrix(Child1, Child2, NumberOfChildren, LeavesUnder, OptimalSum, squareMatrix, n, (2*n-2), optevent);
	   	for (int reset=0; reset<n; reset++){ //Child arrays are reset to their original values for leaves
			Child1[reset]=-1;
			Child2[reset]=-1;
		}
	   	 
		//uses OptimalSum matrix to find leaves generating the best possible sum of similarities
		if (!stop) {
			for (int u = 0; u < NumberOfChildren[Child1[two_n - 2]];u++){
				for (int w = 0; w < NumberOfChildren[Child2[two_n - 2]];w++){
					match = OptimalSum[LeavesUnder[Child1[two_n - 2]][u]][LeavesUnder[Child2[two_n - 2]][w]];
					if (match < bestMatch) 
					{	bestMatch = match;
						bestU = LeavesUnder[Child1[two_n - 2]][u];
						bestW = LeavesUnder[Child2[two_n - 2]][w];
					}
				}
			}
			//recursive method determines optimal ordering and rotates nodes appropriately
			OptimizeLeafOrder(two_n - 2, NumberOfChildren, LeavesUnder, Child1, Child2, OptimalSum, bestU, bestW, n);	
		}
	}

	//========================================
	AlgorithmData result = new AlgorithmData();
	//FloatMatrix similarity_matrix = new FloatMatrix(0, 0);
	//similarity_matrix.A = SimilarityMatrix;
	//result.addMatrix("similarity-matrix", similarity_matrix);
	//result.addIntArray("parent-array", Parent);
	result.addIntArray("child-1-array", Child1);
	result.addIntArray("child-2-array", Child2);
	result.addIntArray("node-order", NodeOrder);
	//result.addIntArray("node-height", NodeHeight);
	result.addMatrix("height", new FloatMatrix(Height, Height.length));
	//result.addIntArray("number-of-children", NumberOfChildren);
      //  if(!genes)
      //      for(int q = 0; q < Height.length; q++){
    //            System.out.println("H"+q+" = "+Height[q]);
                
      //      }
	return result;
    }
    
    public void AssertParentage(int[] Parent, int[] NumberOfChildren, int[] Child1, int[] Child2, int child, int paren) {
	try {
		/* AssertParentage takes an unassigned leaf or node (Parent[child] ==-1) and assigns it to a higher node by fixing the
		 * Parent and Child arrays to reflect the relationship.
		 */
	    if (Parent[child] == -1) {
		Parent[child] = paren;
		--parentless; // global
		Child2[paren]=Child1[paren];
		//         sib[child] = child1[paren];
		Child1[paren] = child;
		NumberOfChildren[paren]+=NumberOfChildren[child];
	    }
	} catch (Exception e) {
	    fireValueChanged(new AlgorithmEvent(this, AlgorithmEvent.WARNING, 0, "Error: "+e.toString()+" - AssertParentage("+String.valueOf(child)+","+String.valueOf(paren)+")"));
	}
    }
    
    public int NewNode(float[] Height, double h) {
    	// NewNode creates a node (Assigned) and assigns it an integer value >=n.   	 
	Height[Assigned] = (float)h;
	if (h > TreeHeight) TreeHeight = h; // global
	++parentless; // global
	return Assigned++; // global
    }
    
    public void MakeLeavesUnderMatrix(int node, int[] Child1, int[] Child2, int n, int[][] LeavesUnder, int[] NumberOfChildren){
    	/* MakeLeavesUnderMatrix is a recursive method that fills the jagged integer matrix LeavesUnder[][] with values of leaves such 
    	 * that Leaves[i][j] is equal to the jth leaf under the node i.
    	 */
    	if (Child1[node]>=n){
    		MakeLeavesUnderMatrix(Child1[node], Child1, Child2, n, LeavesUnder, NumberOfChildren);
    		for (int i = 0; i<NumberOfChildren[Child1[node]]; i++){
    			LeavesUnder[node][i] = LeavesUnder[Child1[node]][i];
    		}
    	}
    	if (Child1[node]<n){
    		LeavesUnder[node][0] = Child1[node]; 
    	}
    	if (Child2[node]>=n){
    		MakeLeavesUnderMatrix(Child2[node], Child1, Child2, n, LeavesUnder, NumberOfChildren);
    		for (int i = 0; i<NumberOfChildren[Child2[node]]; i++){
    			LeavesUnder[node][i + NumberOfChildren[Child1[node]]] = LeavesUnder[Child2[node]][i];
    		}
    	}
    	if (Child2[node]<n){
    		LeavesUnder[node][NumberOfChildren[Child1[node]]] = Child2[node];   		
    	}
    }
	/** MakeOptimalSumMatrix uses the optimal leaf ordering algorithm to generate a FloatMatrix of n x n values.  
	 * OptimalSum[i][j] represents the best possible sum of similarities between all adjacent leaves located 
	 * between i and j in the tree when i and j are set to be their maximum distance apart.
	 * 
	 * The method is recursive, the optimal orderings of a node cannot be determined until the optimal orderings of 
	 * both children of the node are calculated.
	 * @param Child1 the first child of a given node
	 * @param Child2 the second child of a given node
	 * @param NumberOfChildren the number of leaves below a given node
	 * @param LeavesUnder an array of nodes with arrays of leaves under those nodes
	 * @param OptimalSum matrix containing the optimal sum of all similarities between adjacent leaves between two leaves set to be a maximum apart
	 * @param SimilarityMatrix matrix containing similarities between all leaves
	 * @param n the total number of leaves
	 * @param node the node to be optimized
	 * @author dschlauch
	 */
    public void MakeOptimalSumMatrix(int[] Child1, int[]Child2, int[] NumberOfChildren,int[][] LeavesUnder, float[][] OptimalSum, float[][] SimilarityMatrix, int n, int node, AlgorithmEvent optevent){

    	
		//(n*2-2) is the highest node, and starting point
    	if (stop) return;
    	if (node >=n){
    		
    		if (NumberOfChildren[node]==2){  //The OptimalSum between the two leaves of a leaf pair is simply their similarity
    			OptimalSum[LeavesUnder[node][0]][LeavesUnder[node][1]] = SimilarityMatrix[LeavesUnder[node][0]][LeavesUnder[node][1]];
    			OptimalSum[LeavesUnder[node][1]][LeavesUnder[node][0]] = SimilarityMatrix[LeavesUnder[node][0]][LeavesUnder[node][1]];
    		}
    		
    		else{	
		    	MakeOptimalSumMatrix(Child1, Child2, NumberOfChildren,LeavesUnder, OptimalSum, SimilarityMatrix, n, Child1[node], optevent);
		    	MakeOptimalSumMatrix(Child1, Child2, NumberOfChildren,LeavesUnder, OptimalSum, SimilarityMatrix, n, Child2[node], optevent);
		    		
				int unode=-1;
				int wnode=-1;
				int mnode=-1;
				int knode=-1;
				/* for a given node, MakeOptimalSumMatrix finds floats for all 
				 * possible optimum ordering arrangements
				 * given a leaf(u) on the far left grand-child 
				 * and a leaf(w) on the far right grand-child.
				 * m and k represent the intersecting leaves belonging to the
				 * middle grand-children of the node.  The following finds values 
				 * for all 4 possible grand-child arrangements.
				 */
		    	for (int arrangement = 0; arrangement<4; arrangement++){
		    		if (arrangement==0){
		    			unode = Child1[Child1[node]];
		    			wnode = Child2[Child2[node]];
		    			mnode = Child2[Child1[node]];
		    			knode = Child1[Child2[node]];
		    		}
		    		if (arrangement==1){
		    			unode = Child1[Child1[node]];
		    			wnode = Child1[Child2[node]];
		    			mnode = Child2[Child1[node]];
		    			knode = Child2[Child2[node]];
		    		}
		    		if (arrangement==2){
		    			unode = Child2[Child1[node]];
		    			wnode = Child2[Child2[node]];
		    			mnode = Child1[Child1[node]];
		    			knode = Child1[Child2[node]];
		    		}
		    		if (arrangement==3){
		    			unode = Child2[Child1[node]];
		    			wnode = Child1[Child2[node]];
		    			mnode = Child1[Child1[node]];
		    			knode = Child2[Child2[node]];
		    		}
			    	
					/* Fast optimal leaf ordering requires a ranking of optimal orderings between
					 * a leaf and all other leaves of the adjacent grand-child of "node".  The ranking 
					 * allows the search for optimum orderings of (u,w) to be terminated early.  To find the optimal sum of (u,w), 
					 * we start with the best possible orderings for both children of "node". 
					 * If a subsequent ordering (ranked lower), when added to bestC (see above) and
					 * the optimal ordering of the adjacent child, is less than the current best similarity,
					 * we can terminate the search since all subsequent leaves cannot produce the optimal 
					 * ordering.
					 */
					float[][] rankedU = new float[NumberOfChildren[unode]][NumberOfChildren[mnode]];
					float[][] rankedW = new float[NumberOfChildren[wnode]][NumberOfChildren[knode]];
					int[][] leafRankingOrderU =new int[NumberOfChildren[unode]][NumberOfChildren[mnode]]; 
					int[][] leafRankingOrderW =new int[NumberOfChildren[wnode]][NumberOfChildren[knode]]; 
					/* leafRankingOrder holds the ranking order for each leaf of u. 
					 * i.e. leafRankingOrderU[i][j] holds the jth best leaf of mnode for
					 *optimal ordering of the node "node" with the ith leaf of unode at one end.
					 */
					
					for (int u = 0; u<NumberOfChildren[unode]; u++){
						for (int ranker=0; ranker<NumberOfChildren[mnode]; ranker++){
							rankedU[u][ranker]= OptimalSum[LeavesUnder[unode][u]][LeavesUnder[mnode][ranker]];
						}
						Arrays.sort(rankedU[u]);
						for (int align = 0; align<NumberOfChildren[mnode]; align++){
							leafRankingOrderU[u][align] = LeavesUnder[mnode][Arrays.binarySearch(rankedU[u],OptimalSum[LeavesUnder[unode][u]][LeavesUnder[mnode][align]])];
						}
					}
					for (int w = 0; w<NumberOfChildren[wnode]; w++){
						for (int ranker=0; ranker<NumberOfChildren[knode];ranker++){
							rankedW[w][ranker]= OptimalSum[LeavesUnder[wnode][w]][LeavesUnder[knode][ranker]];
						}
						Arrays.sort(rankedW[w]);
						for (int align = 0; align<NumberOfChildren[knode]; align++){
							leafRankingOrderW[w][align] = LeavesUnder[knode][Arrays.binarySearch(rankedW[w],OptimalSum[LeavesUnder[wnode][w]][LeavesUnder[knode][align]])];
						}
					}
					fillOptSumMatrix(NumberOfChildren, rankedU, rankedW, SimilarityMatrix, leafRankingOrderU, leafRankingOrderW, OptimalSum, LeavesUnder, unode, mnode, knode, wnode, optevent);
		    	}
	    	}
	    }
    }
    
    /**
	 * For each leaf (u and w) belonging to the outermost leaves of the outer two grand-children, 
	 * a float is found representing the best possible sum of similarities of all adjacent leaves
	 * between u and w.  The float is stored in the OptimalSum Matrix.
	 * @param NumberOfChildren the number of leaves below a given node
	 * @param rankedU contains an array in ascending order of the optimal sum values for each leaf of U
	 * @param rankedW contains an array in ascending order of the optimal sum values for each leaf of W
	 * @param SimilarityMatrix matrix containing similarities between all leaves 
	 * @param leafRankingOrderU, leafRankingOrderW contains a ranked order of leaves in M by optimal sum versus a leaf in U
	 * @param leafRankingOrderU, leafRankingOrderW contains a ranked order of leaves in K by optimal sum versus a leaf in W
	 * @param OptimalSum matrix containing the optimal sum of all similarities between adjacent leaves between two leaves set to be a maximum apart
	 * @param LeavesUnder an array of nodes with arrays of leaves under those nodes
	 * @param n the total number of leaves
	 * @param unode the grand-child node on the outside, adjacent to mnode
	 * @param mnode the grand-child node on the inside, adjacent to unode
	 * @param knode the grand-child node on the inside, adjacent to wnode
	 * @param wnode the grand-child node on the outside, adjacent to knode
	 * @author dschlauch
	 */
    public void fillOptSumMatrix(int[] NumberOfChildren, float[][] rankedU, float[][] rankedW, float[][]SimilarityMatrix, int[][] leafRankingOrderU, int[][]leafRankingOrderW, float[][] OptimalSum, int[][]LeavesUnder, int unode, int mnode, int knode, int wnode, AlgorithmEvent optevent){
		float currentMin= Float.POSITIVE_INFINITY;
		float currentC = Float.POSITIVE_INFINITY;
		float bestC = Float.POSITIVE_INFINITY;
		
		
		/* for ranking purposes, we find the best possible similarity between possible intersecting leaves
		 * in a given arrangement of grand-children(bestC)
		 */
		for (int m = 0; m < NumberOfChildren[mnode]; m++){
			for (int k = 0; k < NumberOfChildren[knode]; k++){
				currentC = SimilarityMatrix[LeavesUnder[mnode][m]][LeavesUnder[knode][k]];
				if (bestC > currentC) bestC = currentC;	
			}
		}
		for (int u = 0; u<NumberOfChildren[unode]; u++){
	    	for (int w = 0; w<NumberOfChildren[wnode]; w++){
		    		if (stop) return;
		    		for (int m = 0; m<NumberOfChildren[mnode]; m++){
		    			if (rankedU[u][m] + rankedW[w][0] +bestC>=currentMin) break; //halts search when a better arrangement in "m" can no longer be found
		    		
		    			for (int k = 0; k<NumberOfChildren[knode]; k++){	 
		    				if (rankedU[u][m] + rankedW[w][k] + bestC >= currentMin) break; //halts search when a better arrangement in in "k" can no longer be found
		    				if ((rankedU[u][m] + rankedW[w][k] + SimilarityMatrix[leafRankingOrderU[u][m]][leafRankingOrderW[w][k]]) < currentMin){
		    				currentMin = (rankedU[u][m] + rankedW[w][k] + SimilarityMatrix[leafRankingOrderU[u][m]][leafRankingOrderW[w][k]]);
		    				}
			    		}
		    		}
	    	OptimalSum[LeavesUnder[unode][u]][LeavesUnder[wnode][w]] = currentMin;
	    	OptimalSum[LeavesUnder[wnode][w]][LeavesUnder[unode][u]] = currentMin;
	    	currentMin = Float.POSITIVE_INFINITY;
	   		OptProgress++;
	  		optevent.setIntValue(OptProgress);
	  	  	fireValueChanged(optevent);
	    	}	 
		}
    }
    	/** OptimizeLeafOrder uses the complete FloatMatrix OptimalSum[][] to determine optimal ordering.
		 * Given two leaves on opposite ends of a tree, OptimizeLeafOrder can determine the optimal intersecting leaves.
		 * This method checks and adjusts the original Child arrays to make sure the outside leaves(u and w) and 
		 * intersecting leaves (m and k) belong to opposite grand-child nodes of their common node.
		 * The method starts with the complete tree and two optimal outside leaves and uses recursion to order
		 * the new, optimized tree.
		 * @param node the node to be ordered
		 * @param NumberOfChildren the number of leaves below a given node
		 * @param LeavesUnder an array of nodes with arrays of leaves under those nodes
		 * @param Child1 the first child of a given node
		 * @param Child2 the second child of a given node
		 * @param OptimalSum matrix containing the optimal sum of all similarities between adjacent leaves between two leaves set to be a maximum apart
		 * @param bestU the leaf set to be on the outside of the child U
    	 * @param bestW the leaf set to be on the outside of the child W
		 * @param n the total number of leaves
		 * @author dschlauch
		 */
	private void OptimizeLeafOrder(int node, int[]NumberOfChildren,int[][]LeavesUnder, int[]Child1, int[]Child2, float[][]OptimalSum, int bestU, int bestW, int n){
		float match;
		float bestMatch = Float.POSITIVE_INFINITY;
		int bestM = -1;
		int bestK = -1;
		int leftInnerChildren = 1;
		int rightInnerChildren = 1;
		int innerLeftLeaf = bestU;
		int innerRightLeaf = bestW;
		float bestRight = 0.0f;
		float bestLeft = 0.0f;

		rotateNodes(NumberOfChildren, Child1, Child2, bestU, bestW, node, n, LeavesUnder);
		
		if (Child1[node]>=n)		leftInnerChildren = NumberOfChildren[Child2[Child1[node]]];
		if (Child2[node]>=n)		rightInnerChildren = NumberOfChildren[Child1[Child2[node]]];
		
		for (int m = 0; m < leftInnerChildren; m++){
			for (int k = 0; k < rightInnerChildren;k++){
				if (Child1[node]>=n) {
					bestLeft = OptimalSum[bestU][LeavesUnder[Child2[Child1[node]]][m]];
					innerLeftLeaf = LeavesUnder[Child2[Child1[node]]][m];
				}
				
				if (Child2[node]>=n) {
					bestRight = OptimalSum[bestW][LeavesUnder[Child1[Child2[node]]][k]];
					innerRightLeaf = LeavesUnder[Child1[Child2[node]]][k];
				}
				
				match = bestLeft + bestRight + OptimalSum[innerLeftLeaf][innerRightLeaf];
				if (match < bestMatch) 
				{	bestMatch = match;
					bestM = innerLeftLeaf;
					bestK = innerRightLeaf;
				}
			}
		}
		//recursion used for as long as the children of a node are still nodes
		if (Child1[node]>=n){
			OptimizeLeafOrder(Child1[node], NumberOfChildren, LeavesUnder, Child1, Child2, OptimalSum, bestU, bestM, n);
		}
		if (Child2[node]>=n){
			OptimizeLeafOrder(Child2[node], NumberOfChildren, LeavesUnder, Child1, Child2, OptimalSum, bestK, bestW, n);
		}
	}
	
	/**
	 * rotateNodes checks the location of the leaves determined to be on the outermost branches
	 * of each child of "node".  If the branches are not configured for optimal ordering,
	 * the method switches Child1 and Child2 to fix it.
	 * @param NumberOfChildren the number of leaves below a given node
	 * @param Child1 the first child of a given node
	 * @param Child2 the second child of a given node
	 * @param bestU the leaf set to be on the outside of the child U
   	 * @param bestW the leaf set to be on the outside of the child W
   	 * @param node the node that will have its children or grand-children rotated, if necessary
	 * @param n the total number of leaves
	 * @param LeavesUnder an array of nodes with arrays of leaves under those nodes
	 * @author dschlauch
	 */	
	private void rotateNodes(int[]NumberOfChildren, int[]Child1, int[]Child2, int bestU, int bestW, int node, int n, int[][]LeavesUnder){
		int intermediate;
		for (int u = 0; u < NumberOfChildren[Child2[node]];u++){
			if (bestU == LeavesUnder[Child2[node]][u]){
				intermediate = Child1[node];
				Child1[node] = Child2[node];
				Child2[node] = intermediate;
			}
		}
		if (Child1[node]>=n){
			for(int u = 0; u < NumberOfChildren[Child2[Child1[node]]];u++){						
				if (bestU == LeavesUnder[Child2[Child1[node]]][u]){			
				intermediate = Child1[Child1[node]];
				Child1[Child1[node]] = Child2[Child1[node]];
				Child2[Child1[node]] = intermediate;
				}
			}
		}
		if (Child2[node]>=n){
			for(int u = 0; u < NumberOfChildren[Child1[Child2[node]]];u++){						
				if (bestW == LeavesUnder[Child1[Child2[node]]][u]){			
				intermediate = Child2[Child2[node]];
				Child2[Child2[node]] = Child1[Child2[node]];
				Child1[Child2[node]] = intermediate;
				}
			}
	    }
	}
}
