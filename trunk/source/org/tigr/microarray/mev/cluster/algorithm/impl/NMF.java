/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: NMF.java,v $
 * $Revision: 1.8 $
 * $Date: 2008-8-28 17:27:39 $
 * $Author: dschlauch $
 * $State: Exp $
 */


package org.tigr.microarray.mev.cluster.algorithm.impl;


import org.tigr.util.FloatMatrix;
import org.tigr.microarray.mev.cluster.Cluster;
import org.tigr.microarray.mev.cluster.Node;
import org.tigr.microarray.mev.cluster.NodeList;
import org.tigr.microarray.mev.cluster.NodeValue;
import org.tigr.microarray.mev.cluster.NodeValueList;
import org.tigr.microarray.mev.cluster.algorithm.AbortException;
import org.tigr.microarray.mev.cluster.algorithm.AbstractAlgorithm;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmData;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmEvent;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmException;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmParameters;
import org.tigr.microarray.mev.cluster.gui.impl.hcl.HCLTreeData;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
//import javax.swing.JDialog;
import java.util.ArrayList;
import java.util.Random;

public class NMF extends AbstractAlgorithm{
	private static boolean standalone = false;
    private AlgorithmEvent event;
	
	float[][] connectivityMatrix;
	boolean stop = false;
	int r=2;
	int numRuns = 10;
	int maxIterations = 10000;
	int numSamples, numGenes;
	int checkFreq = 40;
	float[] costs = new float[numRuns];
	float[] costsIndex = new float[numRuns];
	float fractionDone;
	float fractionUnit;
	float cutoff = 10f;
	boolean divergence = true;
	boolean doSamples = true;
	boolean expScale = false;
	boolean adjustData = false;
	boolean doMax = false;
	long randomSeed = 33333;
	long startTime;
    FloatMatrix expMatrix;
	FloatMatrix[] W = new FloatMatrix[numRuns];
	FloatMatrix[] H = new FloatMatrix[numRuns];
	int[][] clusters;
	private float tinyNumber = .0001f;
	static long timelast;
	long t1,t2,t3,t4,t5,t6,t7,t8;
    /**
     * This method should interrupt the calculation.
     */
    public void abort() {
    	stop = true;
    }
    /**
     * This method execute calculation and return result,
     * stored in <code>AlgorithmData</code> class.
     *
     * @param data the data to be calculated.
     */
    public AlgorithmData execute(AlgorithmData data) throws AlgorithmException {

    	AlgorithmParameters map = data.getParams();
    	expMatrix = data.getMatrix("experiment");
    	r = map.getInt("r-value");
    	fractionDone = ((float)map.getInt("r-value")-(float)map.getInt("min- r-value"))/(1f+(float)map.getInt("max- r-value")-(float)map.getInt("min- r-value"));
    	fractionUnit = ((float)1)/(1f+(float)map.getInt("max- r-value")-(float)map.getInt("min- r-value"));
    	numRuns = map.getInt("runs");
    	maxIterations = map.getInt("iterations");
    	divergence = map.getBoolean("divergence");
    	doSamples = map.getBoolean("doSamples");
    	expScale = map.getBoolean("expScale");
    	doMax = map.getBoolean("doMax");
    	checkFreq = map.getInt("checkFreq");
    	cutoff = map.getFloat("cutoff");
    	adjustData = map.getBoolean("adjustData");
    	randomSeed = map.getLong("randomSeed");
    	startTime = map.getLong("startTime");
    	if (expMatrix == null) {
    	    throw new AlgorithmException("Input data is absent.");
    	}
    	event = new AlgorithmEvent(this, AlgorithmEvent.SET_UNITS, 100, "Initializing...");
    	fireValueChanged(event);

    	W = new FloatMatrix[numRuns];
    	H = new FloatMatrix[numRuns];
    	costs = new float[numRuns];
    	NMFMultiplicativeUpdate(dataPreProcessing(expMatrix.A));
    	Cluster result_cluster = new Cluster();
    	NodeList nodeList = result_cluster.getNodeList();
	    if (stop) {
	    	throw new AbortException();
	    }
	    Node node = new Node();
        node.setValues(calculateHierarchicalTree());
	    nodeList.addNode(node);
	    clusters = getClusters(node);
	    float cophen = getCopheneticCorrelation();
//	    float cophen2 = getCopheneticCorrelation2();
//	    System.out.println("getCopheneticCorrelation = "+cophen);
//	    System.out.println("getCopheneticCorrelation2 = "+cophen2);
        
        
        // prepare the result
    	AlgorithmData result = new AlgorithmData();
    	result.addCluster("cluster", result_cluster);
    	result.addIntMatrix("clusters", clusters);
        FloatMatrix means = getMeans(clusters);
//        FloatMatrix variances = getVariances(clusters, means);
        FloatMatrix variances = getMeans(clusters);
    	result.addMatrix("connectivity-matrix", new FloatMatrix(this.connectivityMatrix));
    	for (int i=0; i<numRuns; i++)
    		result.addMatrix("W"+i, W[(int)costsIndex[i]]);
    	for (int i=0; i<numRuns; i++)
    		result.addMatrix("H"+i, H[(int)costsIndex[i]]);
    	result.addMatrix("costs", new FloatMatrix(costs, costs.length));
        result.addMatrix("clusters_means", means);
        result.addMatrix("clusters_variances", variances);
        result.addParam("cophen", String.valueOf(cophen));
        result.addParam("adjustData", String.valueOf(adjustData));

//		System.out.println(t1);
//		System.out.println(t2);
//		System.out.println(t3);
//		System.out.println(t4);
//		System.out.println(t5);
//		System.out.println(t6);
    	return result;
    }

//    private float getCopheneticCorrelation2() {
//    	//revert connectivity matrix back to "higher is closer"
//    	for (int i=0; i<connectivityMatrix.length; i++){
//    		for (int j=0; j<connectivityMatrix[i].length; j++){
//    			connectivityMatrix[i][j] = 1-connectivityMatrix[i][j];
//    		}
//    	}
//    	
//    	float[][] hclMatrix = new float[connectivityMatrix.length][connectivityMatrix.length];
//    	for (int i=0; i<hclMatrix.length; i++){
//    		for (int j=0; j<hclMatrix[i].length; j++){
//    			hclMatrix[i][j] = 0f;
//    		}
//    	}
//    	for (int i=0; i<clusters.length; i++){
//    		for (int j=0; j<clusters[i].length; j++){
//        		for (int k=0; k<clusters[i].length; k++){
//        			hclMatrix[clusters[i][j]][clusters[i][k]] = 1f;
//        		}
//    		}
//    	}
//    	float[][] mat = new float[connectivityMatrix.length*connectivityMatrix.length][2];
//    	int index = 0;
//    	for (int i=0; i<connectivityMatrix.length; i++){
//    		for (int j=0; j<connectivityMatrix[i].length; j++){
//    			mat[index][0] = connectivityMatrix[i][j];
//    			mat[index][1] = hclMatrix[i][j];
//    			index++;
//    		}
//		}
//    	FloatMatrix fm = new FloatMatrix(mat);
////    	System.out.println("fm");
////    	printMat(fm.transpose());
//    	return ExperimentUtil.pearsonUncentered(fm, 0, 1, 1);
//
//	}
	private float getCopheneticCorrelation() {
    	//revert connectivity matrix back to "higher is closer"
    	for (int i=0; i<connectivityMatrix.length; i++){
    		for (int j=0; j<connectivityMatrix[i].length; j++){
    			connectivityMatrix[i][j] = 1-connectivityMatrix[i][j];
    		}
    	}
    	
    	
    	float[][] hclMatrix = new float[connectivityMatrix.length][connectivityMatrix.length];
    	for (int i=0; i<hclMatrix.length; i++){
    		for (int j=0; j<hclMatrix[i].length; j++){
    			hclMatrix[i][j] = 0f;
    		}
    	}
    	for (int i=0; i<clusters.length; i++){
    		for (int j=0; j<clusters[i].length; j++){
        		for (int k=0; k<clusters[i].length; k++){
        			hclMatrix[clusters[i][j]][clusters[i][k]] = 1f;
        		}
    		}
    	}
    	//We now have two matrices, the connectivity matrix and the matrix derived from clustering based off the connectivity matrix
    	float x = 0;
    	float t = 0;
    	float cTop = 0;
    	float cBottomL = 0;
    	float cBottomR = 0;
    	for (int i=0; i<hclMatrix.length; i++){
    		for (int j=0; j<hclMatrix[i].length; j++){
    			x = x + connectivityMatrix[i][j];
    			t = t + hclMatrix[i][j];
    		}
    	}
    	x = x/((float)hclMatrix.length*(float)hclMatrix.length);
    	t = t/((float)hclMatrix.length*(float)hclMatrix.length);
    	
		for (int j=0; j<hclMatrix.length; j++){
			for (int i=0; i<j; i++){
    			cTop = cTop + (connectivityMatrix[i][j]-x)*(hclMatrix[i][j]-t);
    			cBottomL = cBottomL + (connectivityMatrix[i][j]-x)*(connectivityMatrix[i][j]-x);
    			cBottomR = cBottomR + (hclMatrix[i][j]-t)*(hclMatrix[i][j]-t);
    		}
		}
//    	System.out.println("cTop = "+cTop);
//    	System.out.println("cBottomL = "+cBottomL);
//    	System.out.println("cBottomR = "+cBottomR);

    	//revert connectivity matrix back to "higher is closer"
    	for (int i=0; i<connectivityMatrix.length; i++){
    		for (int j=0; j<connectivityMatrix[i].length; j++){
    			connectivityMatrix[i][j] = 1-connectivityMatrix[i][j];
    		}
    	}
		return cTop/(float)Math.sqrt(cBottomL*cBottomR);
	}
	protected FloatMatrix getMeans(int[][] clusters) {
        FloatMatrix means = new FloatMatrix(clusters.length, (doSamples ? numSamples : numGenes));
        FloatMatrix mean;
        for (int i=0; i<clusters.length; i++) {
            mean = getMean(clusters[i]);
            means.A[i] = mean.A[0];
        }
        return means;
    }
    
    protected FloatMatrix getMean(int[] cluster) {
        FloatMatrix mean = new FloatMatrix(1, (!doSamples ? numSamples : numGenes));
        float sum = 0;
        for (int i=0; i<(!doSamples ? numSamples : numGenes); i++){
        	for (int j=0; j<cluster.length; j++){
        		if (doSamples)
        			sum = sum + this.expMatrix.get(i, cluster[j]);
        		else
            		sum = sum + this.expMatrix.get(cluster[j], i);
        	}
        	mean.set(0, i, sum/cluster.length);
        }
        return mean;
    }

    protected FloatMatrix getVariances(int[][] clusters, FloatMatrix means) {
//        FloatMatrix means = new FloatMatrix(clusters.length, this.numSamples);
        FloatMatrix mean;
        for (int i=0; i<clusters.length; i++) {
            mean = getVariance(clusters[i]);
            means.A[i] = mean.A[0];
        }
        return means;
    }
    
    protected FloatMatrix getVariance(int[] cluster) {
        FloatMatrix mean = new FloatMatrix(1, numGenes);
        float sum = 0;
        for (int i=0; i<numGenes; i++){
        	for (int j=0; j<cluster.length; j++){
        		if (doSamples)
        			sum = sum + this.expMatrix.get(i, cluster[j]);
        		else
            		sum = sum + this.expMatrix.get(cluster[j], i);
        	}
        	mean.set(0, i, 0);
        }
        return mean;
    }
    private int[][] getClusters(Node node) {
    	int[][] clusters = new int[r][];
    	HCLTreeData hcltd = getResult(node,0);
    	int[] child1 = hcltd.child_1_array;
    	int[] child2 = hcltd.child_2_array;
    	int index=0;
    	int i = 0;
		int cutoff = child1.length-r;
//		System.out.println("child1.length = "+child1.length);
//		for (int k=0; k<child1.length; k++){
//			System.out.print(child1[k]+"\t");
//		}
    	while (index<r){
    		int node1 = child1[child1.length-2-i];
//    		System.out.print("node "+node1+"\t");
    		if (node1 < (cutoff)){
//    			System.out.print("blaahh");
    			leaves.clear();
    			getLeavesFromNode(hcltd,node1);
//    			System.out.println("Cluster "+(i+1)+" has "+leaves.size()+ " things.");
    			clusters[index] = new int[leaves.size()];
    			for (int j=0; j<clusters[index].length; j++){
    				clusters[index][j] = leaves.get(j);
//        			System.out.print("blarrrrahh");
    			}
    			
    			index++;
    		}
    		int node2 = child2[child1.length-2-i];
    		if (node2 < (cutoff)){
    			leaves.clear();
    			getLeavesFromNode(hcltd,node2);
    			clusters[index] = new int[leaves.size()];
    			for (int j=0; j<clusters[index].length; j++){
    				clusters[index][j] = leaves.get(j);
    			}
    			index++;
    		}
    		i++;
    	}
//    	for (int b=0; b<clusters.length; b++){
//    		for (int j=0; j<clusters[b].length; j++)
//    			System.out.print(clusters[b][j]+"\t");
//    		System.out.println();
//    	}
//    	System.out.println("here1");
		return clusters;
	}
    private float[][] dataPreProcessing(float[][] datav){
    	float[][] v = new float[datav.length][];
    	float expressionAverage = 0;
    	for (int i=0; i<v.length; i++){
    		v[i] = new float[datav[i].length];
    		for (int j=0; j<v[i].length; j++){
    			v[i][j] = datav[i][j];
    			if (v[i][j] < 0 || Float.isNaN(v[i][j]))
    		    	adjustData = true;
    			else
    				expressionAverage = expressionAverage+v[i][j];
    		}
    	}
    	expressionAverage = expressionAverage/((float)v.length*(float)v[0].length);
//    	System.out.println("ad = "+expressionAverage);
    	//handling negative/NaN values...
    	if (!adjustData)
    		return v;
    	if (expScale){
			for (int i=0; i<v.length; i++){
				for (int j=0; j<v[0].length; j++){
					if (Float.isNaN(v[i][j]))
						v[i][j] = 0f;
					v[i][j] = (float)Math.exp(v[i][j]);
				}
			}
    	} else {
    		float minVal = Float.POSITIVE_INFINITY;
    		for (int i=0; i<v.length; i++){
				for (int j=0; j<v[0].length; j++){
					if (v[i][j]<minVal){
						minVal = v[i][j];
					}else if(Float.isNaN(v[i][j])){
						v[i][j] = expressionAverage;
					}
				}
			}
//    		System.out.println("t " + minVal);
			for (int i=0; i<v.length; i++)
				for (int j=0; j<v[0].length; j++)
					v[i][j] = v[i][j] - minVal + tinyNumber;
			
//			System.out.println(v[1592][3]);
    	}
    	return v;
    }
    
	private void NMFMultiplicativeUpdate(float[][] v){
		
		//Initiating variables...
    	FloatMatrix V, Wt, Ht;
		V = new FloatMatrix(v);
		numGenes = v.length;
		numSamples = v[0].length;
		float[][] w = new float[numGenes][r];
		float[][] h = new float[r][numSamples];
		int sampsOrGenes = doSamples? numSamples : numGenes;
		connectivityMatrix = new float[sampsOrGenes][sampsOrGenes];
		int totalTries = 0;
//		float costSum = 0;
//		float costBest = Float.POSITIVE_INFINITY;
		Random random = new Random();
		if (randomSeed!=-1)
			random.setSeed(randomSeed);
		for (int runcount=0; runcount<numRuns; runcount++){
            fireValueChanged(event);
    	    if (stop) 
    	    	return;
			totalTries++;
//			if (totalTries%25==0)
//				System.out.println(totalTries);
			//seeding random matrices, creating corresponding transposes for ease of calculation
			for (int i=0; i<numGenes; i++){
				for (int j=0; j<r; j++){
					w[i][j] = random.nextFloat()*16;
				}
			}
			for (int i=0; i<numSamples; i++){
				for (int j=0; j<r; j++){
					h[j][i] = random.nextFloat();
				}
			}
			W[runcount] = new FloatMatrix(w);
			H[runcount] = new FloatMatrix(h);
			Wt = W[runcount].transpose();
			Ht = H[runcount].transpose();
			FloatMatrix WH;
			
			//The number crunching: Uses multiplicative update calculation to find locally optimal factors, W and H
			float previousCost = Float.POSITIVE_INFINITY;
			float cost=0;
			for(int iter = 0; iter<maxIterations; iter++){
				timelast = System.currentTimeMillis();
    			updateProgressBar(iter,runcount);
				t1 += System.currentTimeMillis()-timelast;
				timelast = System.currentTimeMillis();
				if (!divergence){//use euclidean
    				FloatMatrix WtV = Wt.times(V);
    				FloatMatrix WtWH = Wt.times(W[runcount]).times(H[runcount]);
    				float h1[][]=new float[r][numSamples];
    				for (int i=0; i<r; i++){
    					for (int j=0; j<numSamples; j++){
    						h1[i][j] = H[runcount].get(i, j)*WtV.get(i, j)/WtWH.get(i, j);
    					}
    				}
    				H[runcount] = new FloatMatrix(h1);
    				FloatMatrix VHt = V.times(Ht);
    				FloatMatrix WHHt = W[runcount].times(H[runcount]).times(Ht);
    				
    				for (int i=0; i<numGenes; i++){
    					for (int j=0; j<r; j++){
    						w[i][j] = W[runcount].get(i, j)*VHt.get(i, j)/WHHt.get(i, j);
    					}
    				}
    				W[runcount] = new FloatMatrix(w);
    				WH = W[runcount].times(H[runcount]);

    				cost = 0;
    				for (int i=0; i<numGenes; i++){
    					for (int j=0; j<numSamples; j++){
    						cost = cost + (float)Math.pow(V.A[i][j]-WH.A[i][j], 2);
    					}
    				}
				} else { //use divergence
    				WH = W[runcount].times(H[runcount]);
    				t2 += System.currentTimeMillis()-timelast;
    				timelast = System.currentTimeMillis();
    				float h1[][]=new float[r][numSamples];
    				for (int i=0; i<r; i++){
						float sumk = 0;
						for (int k=0; k<numGenes; k++)
							sumk = sumk + W[runcount].get(k, i);
	    				t3 += System.currentTimeMillis()-timelast;
	    				timelast = System.currentTimeMillis();
    					for (int j=0; j<numSamples; j++){
    						float sumi = 0;
    						for (int k=0; k<numGenes; k++)
    							sumi = sumi + W[runcount].get(k, i) * V.get(k, j) / WH.get(k, j);
    						h1[i][j] = H[runcount].get(i, j)*sumi/sumk;
    					}
    				}
    				t4 += System.currentTimeMillis()-timelast;
    				timelast = System.currentTimeMillis();
    				H[runcount] = new FloatMatrix(h1);
    				WH = W[runcount].times(H[runcount]);

    				float w1[][]=new float[numGenes][r];
					for (int j=0; j<r; j++){
						float sumk = 0;
						for (int k=0; k<numSamples; k++)
							sumk = sumk + H[runcount].get(j, k);
						for (int i=0; i<numGenes; i++){
    						float sumi = 0;
    						for (int k=0; k<numSamples; k++)
    							sumi = sumi + H[runcount].get(j, k) * V.get(i, k) / WH.get(i, k);
    						w1[i][j] = W[runcount].get(i, j)*sumi/sumk;
    					}
    				}
    				t5 += System.currentTimeMillis()-timelast;
    				timelast = System.currentTimeMillis();
    				W[runcount] = new FloatMatrix(w1);
    				
    				t6 += System.currentTimeMillis()-timelast;
    				timelast = System.currentTimeMillis();
				}
    			if (!doMax){
    				if (iter%checkFreq==0){
    					WH = W[runcount].times(H[runcount]);   
        				cost = getCost(V,WH);
        				if (cost<0)
        					return;
						if (cost>(previousCost-cutoff)){
							break;
						}
						previousCost = cost;
    				}
    			}
			}
			WH = W[runcount].times(H[runcount]);   
			cost = getCost(V,WH);
			costs[runcount] = cost;
			if (doSamples){
				//Assigns m samples to r classes
				int[] classes = new int[numSamples];
				for (int i=0; i<numSamples; i++){
					float best = 0;
					for (int j=0; j<r; j++){
						if (H[runcount].A[j][i]>best){
							best = H[runcount].A[j][i];
							classes[i]=j;
						}
					}
				}
				//Adds to connectivity matrix
				for (int i=0; i<numSamples; i++){
					for (int j=0; j<numSamples; j++){
						if (classes[i]==classes[j])
							connectivityMatrix[i][j]++;
					}
				}
			}else{
				//Assigns n genes to r classes
				int[] classes = new int[numGenes];
				for (int i=0; i<numGenes; i++){
					float best = 0;
					for (int j=0; j<r; j++){
						if (W[runcount].A[i][j]>best){
							best = W[runcount].A[i][j];
							classes[i]=j;
						}
					}
				}
				//Adds to connectivity matrix
				for (int i=0; i<numGenes; i++){
					for (int j=0; j<numGenes; j++){
						if (classes[i]==classes[j])
							connectivityMatrix[i][j]++;
					}
				}
			}
		}
	    costsIndex = new float[costs.length];
	    for (int i=0; i<costsIndex.length; i++){
	    	costsIndex[i]=i;
	    }
	    ExperimentUtil.sort2(costs, costsIndex);
		for (int i=0; i<connectivityMatrix.length; i++){
			for (int j=0; j<connectivityMatrix.length; j++){
				connectivityMatrix[i][j]=1-connectivityMatrix[i][j]/numRuns;
			}
		}
	}

	private float getCost(FloatMatrix V, FloatMatrix WH) {
		float cost = 0;
		for (int i=0; i<numGenes; i++){
			for (int j=0; j<numSamples; j++){
				float aij = V.A[i][j];
				float bij = WH.A[i][j];
				cost = cost + (float)(aij*Math.log(aij/bij)- aij + bij);
				if (Float.isNaN(cost)){
					System.out.println("cost is NaN");
					System.out.println("i " + i + "  j " + j);
					System.out.println("V.A[i][j] " + V.A[i][j]);
					System.out.println("WH.A[i][j] " + WH.A[i][j]);
					stop = true;
					return -1f;
				}
			}
		}
		return cost;
	}
	public void updateProgressBar(int iter, int run){
		if (standalone)
			return;
		event.setId(AlgorithmEvent.PROGRESS_VALUE);
		float fract = (fractionDone + fractionUnit*((float)run/(float)numRuns) + ((float)fractionUnit/(float)numRuns)*((float)iter)/((float)maxIterations));
		event.setIntValue((int)(100f*fract));
    	int timeremaining = (int)((1-fract)*(((System.currentTimeMillis()- startTime)/fract)/1000));
    	String timeUnits = " seconds";
    	if (timeremaining>7200){
    		timeUnits = " hours";
    		timeremaining = timeremaining/3600+1;
    	} else if (timeremaining>90){
    		timeUnits = " minutes";
    		timeremaining = timeremaining/60+1;
    	}
    	String desc = "Evaluating "+r+" factors; \tRun: "+ (run +1)+" of " + numRuns+"; \tIteration: "+iter + "; \tTime remaining: "+timeremaining + timeUnits;
		event.setDescription(desc);
    	fireValueChanged(event);
	}
	private void printMat(FloatMatrix fm){
		System.out.println("start  " + fm);
		System.out.println("Dimensions: " +fm.getColumnDimension() +" X "+ fm.getRowDimension());
		for (int i=0; i<fm.getRowDimension(); i++){
			for (int j=0; j<fm.getColumnDimension(); j++){
				System.out.print(fm.get(i, j)+"\t");
			}
			System.out.println();
		}
		System.out.println("end \n");
	}
	

    
    /**
     * Checking the result of hcl algorithm calculation.
     * @throws AlgorithmException, if the result is incorrect.
     */
    private void validate(AlgorithmData result) throws AlgorithmException {
		if (result.getIntArray("child-1-array") == null) {
		    throw new AlgorithmException("parameter 'child-1-array' is null");
		}
		if (result.getIntArray("child-2-array") == null) {
		    throw new AlgorithmException("parameter 'child-2-array' is null");
		}
		if (result.getIntArray("node-order") == null) {
		    throw new AlgorithmException("parameter 'node-order' is null");
		}
		if (result.getMatrix("height") == null) {
		    throw new AlgorithmException("parameter 'height' is null");
		}
    }
    private NodeValueList calculateHierarchicalTree() throws AlgorithmException {
		if (standalone)
			return null;
		
		event.setId(AlgorithmEvent.SET_INDETERMINATE);
		event.setDescription("Building Hierarchical Tree");
    	fireValueChanged(event);
    	
    	NodeValueList nodeList = new NodeValueList();
    	AlgorithmData data = new AlgorithmData();
    	HCL hcl = new HCL();
    	AlgorithmData result;
    	float[][] connMatForHCL = new float[connectivityMatrix.length][connectivityMatrix.length];
    	for (int i=0; i<connectivityMatrix.length; i++)
    		for (int j=0; j<connectivityMatrix[i].length; j++)
    			connMatForHCL[i][j] = connectivityMatrix[i][j];

    	data.addMatrix("experiment", new FloatMatrix(connMatForHCL));
	    data.addParam("calculate-genes", String.valueOf(false));
	    data.addParam("optimize-sample-ordering", String.valueOf(true));
	    result = hcl.executeNMF(data, connMatForHCL);
	    validate(result);
	    addNodeValues(nodeList, result);
    	return nodeList;
    }
    
    private void addNodeValues(NodeValueList target_list, AlgorithmData source_result) {
    	target_list.addNodeValue(new NodeValue("child-1-array", source_result.getIntArray("child-1-array")));
    	target_list.addNodeValue(new NodeValue("child-2-array", source_result.getIntArray("child-2-array")));
    	target_list.addNodeValue(new NodeValue("node-order", source_result.getIntArray("node-order")));
    	target_list.addNodeValue(new NodeValue("height", source_result.getMatrix("height").getRowPackedCopy()));
    }

    protected HCLTreeData getResult(Node clusterNode, int pos) {
        HCLTreeData data = new HCLTreeData();
        NodeValueList valueList = clusterNode.getValues();
        data.child_1_array = (int[])valueList.getNodeValue(pos).value;
        data.child_2_array = (int[])valueList.getNodeValue(pos+1).value;
        data.node_order = (int[])valueList.getNodeValue(pos+2).value;
        data.height = (float[])valueList.getNodeValue(pos+3).value;
        return data;
    }

    ArrayList<Integer> leaves = new ArrayList<Integer>();
    	/** getLeavesFromNode is a recursive method that fills the jagged integer matrix LeavesUnder[][] with values of leaves such 
    	 * that Leaves[i][j] is equal to the jth leaf under the node i.
    	 */
    private void getLeavesFromNode(HCLTreeData hcltd, int node){
    	if (node< (doSamples ? numSamples : numGenes)){
    		leaves.add(node);
    		return;
    	}
    	if (hcltd.child_1_array[node]<(doSamples ? numSamples : numGenes))
    		leaves.add(hcltd.child_1_array[node]);
    	else
    		getLeavesFromNode(hcltd, hcltd.child_1_array[node]);
    	if (hcltd.child_2_array[node]<(doSamples ? numSamples : numGenes))
    		leaves.add(hcltd.child_2_array[node]);
    	else
    		getLeavesFromNode(hcltd, hcltd.child_2_array[node]);
    }

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		timelast = System.currentTimeMillis();
		standalone = true;
		float [][] v1 = 
			{	{	16,	32	,72		,40	},
				{	28,	56	,126 	,70	},
				{	4,	8	,18		,10},
				{	24, 48 	,108	,60}
			};
		

		ArrayList<String> amounts = new ArrayList<String>();
		File file2 = new File("C://Users//Dan//workspace//MeV_4_4//data//BETR_5000_sample.txt");
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(file2));
			String line;
			while( (line = br.readLine()) != null)
				amounts.add(line.trim());
			br.close();
		
		}catch (Exception e){
			
		}
		float[][] v = new float[amounts.size()][];
		for (int i=0; i<v.length; i++){
			String[] str = amounts.get(i).split("\t");
			v[i] = new float[str.length];
			for (int j=0; j<str.length; j++){
				v[i][j] = Float.parseFloat(str[j]);
			}
		}
		NMF nmf = new NMF();
		nmf.NMFMultiplicativeUpdate(nmf.dataPreProcessing(v));
		System.out.println(nmf.t1);
		System.out.println(nmf.t2);
		System.out.println(nmf.t3);
		System.out.println(nmf.t4);
		System.out.println(nmf.t5);
		System.out.println(nmf.t6);
		try{
			nmf.calculateHierarchicalTree();
		}catch (Exception e){
			e.printStackTrace();
		}
	}
}
