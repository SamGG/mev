/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: RN.java,v $
 * $Revision: 1.1.1.2 $
 * $Date: 2004-02-06 21:48:18 $
 * $Author: braisted $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.algorithm.impl;

import java.util.Arrays;

import org.tigr.util.FloatMatrix;

import org.tigr.microarray.mev.cluster.*;
import org.tigr.microarray.mev.cluster.algorithm.*;

import org.tigr.microarray.mev.cluster.algorithm.impl.util.*;


public class RN extends AbstractAlgorithm {

    private static final int c_DecileCount = 10;
    public static final double LOG2 = Math.log(2.0);

    private Algorithm permAlgo;
    private boolean stop = false;
    private int number_of_samples;
    private FloatMatrix expMatrix;
    
    public AlgorithmData execute(AlgorithmData data) throws AlgorithmException {

        expMatrix = data.getMatrix("experiment");
        if (expMatrix == null)
            return null;

        AlgorithmParameters map = data.getParams();
        number_of_samples = expMatrix.getColumnDimension();
        boolean use_permutation = map.getBoolean("use-permutation");
        float perm_threshold = 0.8f;
        if (use_permutation) {
            this.permAlgo = new PermutationTest();
            permAlgo.addAlgorithmListener(new SubAlgoListener());
            AlgorithmData permResult = permAlgo.execute(data);
            perm_threshold = permResult.getParams().getFloat("threshold", perm_threshold);
        }

        int function = map.getInt("distance-function", PEARSON);
        float factor = map.getFloat("distance-factor", 1.0f);
        boolean absolute = map.getBoolean("distance-absolute", true);
        float min_threshold = use_permutation ? perm_threshold : map.getFloat("min-threshold", 0.8f);
        float max_threshold = map.getFloat("max-threshold", 1.0f);
        boolean bFilterByEntropy = map.getBoolean("filter-by-entropy");
        float fltTopNPercent=map.getFloat("top-n-percent", 100f);
        if (fltTopNPercent < 0f || fltTopNPercent>100f) {
            throw new AlgorithmException("Filter value is out of range (0, 100)%");
        }

        int nGenes = expMatrix.getRowDimension();
        int filteredSize = nGenes;

        int[] entropyIndices = new int[nGenes];
        for (int i=0; i<entropyIndices.length; i++) {
            entropyIndices[i] = i;
        }
        if (bFilterByEntropy) {
            double[] entropyValues = new double[nGenes];
            for (int i=0; i<entropyValues.length; i++) {
                entropyValues[i] = getEntropy(expMatrix.A[i]);
            }
            IntSorter.sort(entropyIndices, new RelNetComparator(entropyValues));
            filteredSize = (int)((float)nGenes*fltTopNPercent/100f);
        }

        AlgorithmEvent event = new AlgorithmEvent(this, AlgorithmEvent.SET_UNITS, 100, "Calculation Network");
        fireValueChanged(event);

        event.setId(AlgorithmEvent.PROGRESS_VALUE);

        int progress = 0;
        int links = 0;
        int sum = filteredSize*(filteredSize+1)/2;
        int step = sum/100+1;
        float value;

        IntArray[] indices = new IntArray[nGenes];
        FloatArray[] weights = new FloatArray[nGenes];
        for (int i=0; i<indices.length; i++) {
            indices[i] = new IntArray();
            indices[i].add(i);
            weights[i] = new FloatArray();
            weights[i].add(0f);
        }
        for (int nCurIndOuter=0; nCurIndOuter<filteredSize; nCurIndOuter++) {
            if (this.stop) {
                throw new AbortException();
            }
            progress++;
            // calculation
            for (int nCurIndInner=nCurIndOuter+1; nCurIndInner<filteredSize; nCurIndInner++) {
                value = ExperimentUtil.geneDistance(expMatrix, null, entropyIndices[nCurIndOuter], entropyIndices[nCurIndInner], function, factor, absolute);
                value = value*value; // = abs(r^2)
                if (value >= min_threshold && value <= max_threshold) {
                    links++;
                    indices[entropyIndices[nCurIndOuter]].add(entropyIndices[nCurIndInner]);
                    indices[entropyIndices[nCurIndInner]].add(entropyIndices[nCurIndOuter]);
                    weights[entropyIndices[nCurIndOuter]].add(value);
                    weights[entropyIndices[nCurIndInner]].add(value);
                }
                // progress events handling
                progress++;
                if (progress%step == 0) {
                    event.setIntValue(progress/step);
                    event.setDescription("Calculation Network ("+String.valueOf(links)+" links found)");
                    fireValueChanged(event);
                }
            }
        }
        // return the result
        AlgorithmData result = new AlgorithmData();

        Cluster cluster;
        NodeList clusterNodeList;
        // copy nodes to the cluster structure
        cluster = new Cluster();
        clusterNodeList = cluster.getNodeList();
        clusterNodeList.ensureCapacity(nGenes);
        
        FloatMatrix means = getMeans(indices);        
        result.addMatrix("means", means);
        result.addMatrix("variances", this.getVariances(indices, means));
        
        for (int i=0; i<nGenes; i++) {
            clusterNodeList.addNode(new Node(indices[i].toArray()));
            indices[i] = null; // gc
        }
        result.addCluster("cluster", cluster);
        // copy weights to the cluster structure
        cluster = new Cluster();
        clusterNodeList = cluster.getNodeList();
        clusterNodeList.ensureCapacity(nGenes);
        for (int i=0; i<nGenes; i++) {
            clusterNodeList.addNode(new Node(float2int(weights[i].toArray())));
            weights[i] = null; // gc
        }
        result.addCluster("weights", cluster);

        result.addParam("links", String.valueOf(links));
        result.addParam("min_threshold", String.valueOf(min_threshold));
        return result;
    }

    public void abort() {
        this.stop = true;
        if (this.permAlgo != null)
            this.permAlgo.abort();
    }

    private static int[] float2int(float[] floats) {
        if (floats == null)
            return null;
        int[] ints = new int[floats.length];
        for (int i=0; i<ints.length; i++)
            ints[i] = Float.floatToRawIntBits(floats[i]);
        return ints;
    }

    private double getEntropy(float[] pVector) {
        double fltMin = Double.MAX_VALUE;
        double fltMax = -Double.MAX_VALUE;
        int i=0;
        int[] arrDeciles = new int[c_DecileCount]; 

        final int iSize = pVector.length;
        int iValCount = 0;
        for (i=0; i<iSize; i++) {
            if (Double.isNaN(pVector[i]))
                continue;
            fltMin = Math.min(fltMin, pVector[i]);
            fltMax = Math.max(fltMax, pVector[i]);
            iValCount++;
        }

        double fltStep = (fltMax-fltMin)/(c_DecileCount);
        if (fltStep == 0d) {
            return -1.0*Math.log(1.0)/LOG2;
        }

        if (fltMin == Double.MAX_VALUE)
            return 0d;

        Arrays.fill(arrDeciles, 0);
        for (i=0; i<iSize; i++) {
            if (Double.isNaN(pVector[i]))
                continue;
            int iDecileInd = (int)Math.ceil((pVector[i]-fltMin)/fltStep)-1;
            if (iDecileInd < 0) {
                iDecileInd = 0;
            }
            arrDeciles[iDecileInd]++;
        }
        if (iValCount == 0)
            return 0d;

        // finally, calculate entropy
        double dblEntropy=0;

        for (i=0; i<c_DecileCount; i++) {
            if (arrDeciles[i] == 0) {
                continue;
            }
            double dblPx=((double)arrDeciles[i])/iValCount;
            dblEntropy += dblPx*Math.log(dblPx)/LOG2; // log2(x)==log(x)/log(2)
        }
        return -dblEntropy;
    }

    

    
    private FloatMatrix getMeans(IntArray [] clusters) {
	FloatMatrix means = new FloatMatrix(clusters.length, number_of_samples);
	FloatMatrix mean; 
	for (int i=0; i<clusters.length; i++) {
	    mean = getMean(clusters[i]);
	    means.A[i] = mean.A[0];
	}
	return means;
    }
    
    private FloatMatrix getMean(IntArray cluster) {
	FloatMatrix mean = new FloatMatrix(1, number_of_samples);
	float currentMean;
	int n = cluster.getSize();
	int denom = 0;
	float value;
	for (int i=0; i<number_of_samples; i++) {
	    currentMean = 0f;
	    denom  = 0;
	    for (int j=0; j<n; j++) {
		value = expMatrix.get(cluster.get(j), i);
		if (!Float.isNaN(value)) {
		    currentMean += value;
		    denom++;
		}
	    }
	    mean.set(0, i, currentMean/(float)denom);
	}
	
	return mean;
    }
    
        private FloatMatrix getVariances(IntArray [] clusters, FloatMatrix means) {
	final int rows = means.getRowDimension();
	final int columns = means.getColumnDimension();
	FloatMatrix variances = new FloatMatrix(rows, columns);
	for (int row=0; row<rows; row++) {
	    for (int column=0; column<columns; column++) {
		variances.set(row, column, getSampleVariance(clusters[row], column, means.get(row, column)));
	    }
	}
	return variances;
    }
    
    int validN;
    
    private float getSampleNormalizedSum(IntArray cluster, int column, float mean) {
	final int size = cluster.getSize();
	float sum = 0f;
	validN = 0;
	float value;
	for (int i=0; i<size; i++) {
	    value = expMatrix.get(cluster.get(i), column);
	    if (!Float.isNaN(value)) {
		sum += Math.pow(value-mean, 2);
		validN++;
	    }
	}
	return sum;
    }
    
    private float getSampleVariance(IntArray cluster, int column, float mean) {
	return(float)Math.sqrt(getSampleNormalizedSum(cluster, column, mean)/(float)(validN-1));
    }
    
    
    class SubAlgoListener implements AlgorithmListener {
        public void valueChanged(AlgorithmEvent event) {
            fireValueChanged(event);
        }
    }
}
