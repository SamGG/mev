/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: HCL.java,v $
 * $Revision: 1.1 $
 * $Date: 2006-02-08 18:17:16 $
 * $Author: caliente $
 * $State: Exp $
 */

package org.tigr.microarray.mev.cluster.algorithm.impl.tease;

import org.tigr.microarray.mev.cluster.algorithm.AbortException;
import org.tigr.microarray.mev.cluster.algorithm.AbstractAlgorithm;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmData;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmEvent;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmException;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmParameters;
import org.tigr.microarray.mev.cluster.algorithm.impl.ExperimentUtil;
import org.tigr.util.FloatMatrix;

public class HCL extends AbstractAlgorithm {
    
    private boolean stop = false;
    
    private int parentless;
    private double TreeHeight;
    private int Assigned;
    
    public HCL() {}
    
    public void abort() {
    	stop = true;
    }
    
    public AlgorithmData execute(AlgorithmData data) throws AlgorithmException {
	
		FloatMatrix expMatrix = data.getMatrix("experiment");
		//FloatMatrix matrix = data.getMatrix("expression");

		if (expMatrix == null) {
		    throw new AlgorithmException("Input data is absent.");
		}
//		System.out.println("");
//		String[] names = data.getClusterNames();            //************************************
//		for (int x = 0; x < names.length; x++)
//			System.out.println(names[x]);
//		
//		System.out.println("HCL 50");
//		for (int x = 0; x < expMatrix.getRowDimension(); x ++) {  //************************************
//			for(int y = 0; y < expMatrix.getColumnDimension(); y ++)
//				System.out.print(expMatrix.get(x,y)+" ");
//			System.out.println();
//		}
//		
		AlgorithmParameters map = data.getParams();
		
		int function = map.getInt("hcl-distance-function", EUCLIDEAN);
		float factor; // = map.getFloat("distance-factor", 1.0f);
		boolean absolute = map.getBoolean("hcl-distance-absolute", false);
		boolean genes = map.getBoolean("calculate-genes", true);
		int method = map.getInt("method-linkage", 0);
		
//		System.out.println("HCL 65");
//		System.out.println("function = "+ function);
//		System.out.println("absolute = "+ absolute);
//		System.out.println("genes = "+ genes);
//		System.out.println("method = "+ method);
		
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
		
		int[] Parent = new int[two_n];
		int[] Child1 = new int[two_n];
		int[] Child2 = new int[two_n];
		int[] NodeHeight = new int[two_n];
		int[] NodeOrder  = new int[n];
		int[] NumberOfChildren = new int[two_n];
		
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
		
		factor = (float)1.0;  //factor is used as an optional scaling factor
		
		for (i=1; i<n; ++i) {
		    CurrentProgress=(int)(i*Factor);
		    
		    if (CurrentProgress>OldCurrentProgress) {
				event.setIntValue(CurrentProgress);
				fireValueChanged(event);
				OldCurrentProgress=CurrentProgress;
		    }
		    SimilarityMatrix[i] = new float[i];
		    Min[i] = Float.POSITIVE_INFINITY;
		    
		    for (int j=0; j<i; ++j) {
				if (stop) {
				    throw new AbortException();
				}
				if (genes) {
				    SimilarityMatrix[i][j] = ExperimentUtil.geneDistance(expMatrix, null, i, j, 
				    		function, factor, absolute);//ExpMatrix.GeneDistance(i,j,null);
				} else {
				    SimilarityMatrix[i][j] = ExperimentUtil.distance(expMatrix, i, j, 
				    		function, factor, absolute); //ExpMatrix.ExperimentDistance(i,j);
				}
				
				if (SimilarityMatrix[i][j] < Min[i]) {
				    Min[i] = SimilarityMatrix[i][j];
				    MinIndex[i] = j;
				}
		    }
		}
	//	for (int k = 0; k < n; k++) {
	//		System.out.print(Min[k]+"  ");
	//	}
	//	System.out.println();
	//	System.out.println();	
	//	for (int k = 0; k < n; k++) {
	//		System.out.print(MinIndex[k]+"  ");
	//	}
		
	//    for (int z=0; z<SimilarityMatrix.length; z++) {
	//		if (SimilarityMatrix[z] == null) {
	//		    System.out.println("["+z+"]=null");
	//		} else {
	//		    System.out.print("["+z+"]="+SimilarityMatrix[z]+" --> ");
	//		    for (int y=0; y<SimilarityMatrix[z].length; y++) {
	//		       System.out.print(" "+SimilarityMatrix[z][y]);
	//		    }
	//		    System.out.println("");
	//		}
	//    }
	
		
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
		int NodeCounter = 0;
		double MaxDistance=0;
		double MinDistance=Double.POSITIVE_INFINITY;
		MaxCorrelation=Double.POSITIVE_INFINITY;
		double MinCorrelation=Double.POSITIVE_INFINITY;
		int owner[] = new int[n];
		
		for (i=0; i<n; i++)
		    owner[i] = i;
		
		while (parentless > 1) {  //parentless initialized to be the number of genes
		    if (stop) {
		    	throw new AbortException();
		    }
		    CurrentProgress=(int)(CalculatedNodes*Factor);
		    
		    if (CurrentProgress>OldCurrentProgress) {
				event.setIntValue(CurrentProgress);
				fireValueChanged(event);
				OldCurrentProgress=CurrentProgress;
		    }
		    CalculatedNodes++;
		    
		    double close_d = Double.POSITIVE_INFINITY;        // first find the closest pair
		    double test_d = Double.POSITIVE_INFINITY;
		    int test_i = -2;
		    int test_j = -2;

		    for (i=1; i<n; ++i) {
				if (owner[i] != -1) {
				    if (Min[i] < test_d) {
					test_d = Min[i];
					test_i = i;
					test_j = MinIndex[i];
				    }
				}
		    }
		    
		    i = test_i;
		    j = test_j;
		    close_d = test_d;  
		    double height_k = close_d; 
		    
	            //was close_d/2.0 ????????
		    if ((Math.abs(close_d) > 0) && (Math.abs(close_d) < MinDistance))
		    	MinDistance=Math.abs(close_d);
		    if ((close_d!=1) && (close_d < MaxCorrelation))
		    	MaxCorrelation=close_d;
		    if ((close_d > MaxCorrelation) && (close_d < MinCorrelation))
		    	MinCorrelation=close_d;
		    if (close_d > MaxDistance)
		    	MaxDistance=close_d;
		    
		    try {
				//System.out.println(" owner["+i+"]="+owner[i]);
				if (owner[i]>=n && Height[owner[i]]>height_k) {        // Gene1 already assignd to a node, was >= ?!
				    k = owner[i];
				    AssertParentage(Parent, NumberOfChildren, Child1, Child2, owner[j], k);
				} else if (owner[j]>=n && Height[owner[j]]>height_k) { // Gene2 already assignd to node was >= ?!
				    k = owner[j];
				    AssertParentage(Parent, NumberOfChildren, Child1, Child2, owner[i], k);
				} else {
				    k = NewNode(Height, height_k);
				    AssertParentage(Parent, NumberOfChildren,Child1, Child2,owner[i], k);
				    AssertParentage(Parent, NumberOfChildren,Child1, Child2,owner[j], k);
				}
				NodeOrder[NodeCounter]=k;
				NodeHeight[k]=Math.max(NodeHeight[Child1[k]]+1,NodeHeight[Child2[k]]+1);
				
		    } catch (Exception e) {
				e.printStackTrace();
				fireValueChanged(new AlgorithmEvent(this, AlgorithmEvent.WARNING, 0, "Error: "+e.toString()+" - Height("+String.valueOf(height_k)+","+")"));
				k=0;
		    }
	
		    NodeCounter++;
		    owner[i] = k;
		    owner[j] = -1;
		    
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
				if (owner[p]!=-1 && ((MinIndex[p]==j) || (MinIndex[p]==i))) {
					Min[p]=Float.POSITIVE_INFINITY;
					for (int l=0; l<p; l++) {
						if (owner[l] != -1 && SimilarityMatrix[p][l]<Min[p]) {
							Min[p]= SimilarityMatrix[p][l];
							MinIndex[p]=l;
						}
				    }
				}
		    }
	
		}
	//    for (int z=0; z<SimilarityMatrix.length; z++) {   //************************
	//		if (SimilarityMatrix[z] == null) {
	//		    System.out.println("["+z+"]=null");
	//		} else {
	//		    System.out.print("["+z+"]="+SimilarityMatrix[z]+" --> ");
	//		    for (int y=0; y<SimilarityMatrix[z].length; y++) {
	//		       System.out.print(" "+SimilarityMatrix[z][y]);
	//		    }
	//		    System.out.println("");
	//		}
	//    }
		//========================================
		AlgorithmData result = new AlgorithmData();
		//FloatMatrix similarity_matrix = new FloatMatrix(0, 0);
		//similarity_matrix.A = SimilarityMatrix;
		//result.addMatrix("similarity-matrix", similarity_matrix);
		//result.addIntArray("parent-array", Parent);
	
//		System.out.println("HCL 345");
//		System.out.println("Child1 array");   //**********************************
//		for (int x = 0; x < Child1.length; x++) {
//			System.out.print(Child1[x]+" ");
//		}
//		System.out.println();
//		System.out.println("Child 2 array");
//		for (int x = 0; x < Child2.length; x++) {
//			System.out.print(Child2[x]+" ");
//		}
//		System.out.println();
//		System.out.println("node-order");
//		for (int x = 0; x < NodeOrder.length; x++) {
//			System.out.print(NodeOrder[x]+" ");
//		}
//		System.out.println();
//		System.out.println("child1 of nodeOrder "+ NodeOrder[0]+" = " + Child1[NodeOrder[0]]);
//		System.out.println("child2 of nodeOrder "+ NodeOrder[0]+" = " + Child2[NodeOrder[0]]);
//		System.out.println("Size of child1 array = "+Child1.length);
//		System.out.println("Size of child2 array = "+Child2.length);
//		System.out.println("NodeOrder array = "+NodeOrder.length);
			
		result.addIntArray("child-1-array", Child1);
		result.addIntArray("child-2-array", Child2);
		result.addIntArray("node-order", NodeOrder);
		result.addMatrix("height", new FloatMatrix(Height, Height.length));
	
		return result;
    }
    
    public void AssertParentage(int[] Parent, int[] NumberOfChildren, int[] Child1, 
    		int[] Child2, int child, int paren) {
		try {
		    if (Parent[child] == -1) {
				Parent[child] = paren;
				parentless--; // global
				Child2[paren]=Child1[paren];
				Child1[paren] = child;
				NumberOfChildren[paren]+=NumberOfChildren[child];
		    }
		} catch (Exception e) {
		    fireValueChanged(new AlgorithmEvent(this, AlgorithmEvent.WARNING, 0, "Error: "+e.toString()+" - AssertParentage("+String.valueOf(child)+","+String.valueOf(paren)+")"));
		}
    }
    
    public int NewNode(float[] Height, double h) {
		Height[Assigned] = (float)h;    //assigned is initialized to be the number of genes
		if (h > TreeHeight) 
			TreeHeight = h; // global
		++parentless; // global
		return Assigned++; // global
    }
}
