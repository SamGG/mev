/*
Copyright @ 1999-2004, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: HCL.java,v $
 * $Revision: 1.2 $
 * $Date: 2005-02-24 20:23:46 $
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
	if (expMatrix == null) {
	    throw new AlgorithmException("Input data is absent.");
	}
	AlgorithmParameters map = data.getParams();
	
	int function = map.getInt("hcl-distance-function", EUCLIDEAN);
	float factor; // = map.getFloat("distance-factor", 1.0f);
	boolean absolute = map.getBoolean("hcl-distance-absolute", false);
	boolean genes = map.getBoolean("calculate-genes", true);
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
	    SimilarityMatrix[i] = new float[i];
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
	while (parentless > 1) {
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
	    double close_d = Double.POSITIVE_INFINITY;              // first find the closest pair
	    double test_d = Double.POSITIVE_INFINITY;
	    double TestMin = Double.POSITIVE_INFINITY;
	    int test_i = -2;
	    int test_j = -2;
	    int close_i = -2, close_j = -2;
	    for (i=1; i<n; ++i) {
		if (owner[i] != -1) {
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
	Height[Assigned] = (float)h;
	if (h > TreeHeight) TreeHeight = h; // global
	++parentless; // global
	return Assigned++; // global
    }
    
    /*for (int z=0; z<SimilarityMatrix.length; z++) {
	if (SimilarityMatrix[z] == null) {
	    System.out.println("["+z+"]=null");
	} else {
	    System.out.print("["+z+"]="+SimilarityMatrix[z]+" --> ");
	    for (int y=0; y<SimilarityMatrix[z].length; y++) {
	       System.out.print(" "+SimilarityMatrix[z][y]);
	    }
	    System.out.println("");
	}
    }*/
}
