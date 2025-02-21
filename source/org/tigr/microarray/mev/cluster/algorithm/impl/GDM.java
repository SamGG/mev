/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
package org.tigr.microarray.mev.cluster.algorithm.impl;


import javax.swing.JFrame;

import org.tigr.microarray.mev.cluster.algorithm.AbortException;
import org.tigr.microarray.mev.cluster.algorithm.AbstractAlgorithm;
import org.tigr.microarray.mev.cluster.algorithm.Algorithm;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmData;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmEvent;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmException;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmParameters;
import org.tigr.util.FloatMatrix;
import org.tigr.util.awt.ProgressDialog;

public class GDM extends AbstractAlgorithm {
    private boolean stop = false;
    private int function;
    private float factor;
    private boolean absolute;
    private FloatMatrix expMatrix;

    private FloatMatrix geneDistanceMatrix;
    private FloatMatrix rawMatrix;
    
    private int num_samples; // m = number_of_samples
    private int num_genes; //n = number_of_genes
    private boolean pearson=false;

    private long StartTime;
    private long CalculationTime;

    private boolean Stop;
    private int DistanceFunction;

    private ProgressDialog PD;

    private double zeroValue;
    
    private float maxDist;
    private float minDist;
        
    public GDM() {
    }

    public AlgorithmData execute(AlgorithmData data) throws AlgorithmException {

	AlgorithmParameters map = data.getParams();
		
	function = map.getInt("distance-function", EUCLIDEAN);
	factor   = map.getFloat("distance-factor", 1.0f);
        absolute = map.getBoolean("distance-absolute", false);
	this.expMatrix = data.getMatrix("experiment");
        if (expMatrix == null) {
            throw new AlgorithmException("Input data is absent.");
        }
		
	num_genes = this.expMatrix.getRowDimension(); //n = number_of_genes
        num_samples = this.expMatrix.getColumnDimension();//m = number_of_samples
        geneDistanceMatrix = new FloatMatrix(num_genes, num_genes);
        rawMatrix = new FloatMatrix(num_genes, num_genes);

	JFrame dummyFrame = new JFrame();
        PD = new ProgressDialog(dummyFrame, "Gene Distance Matrix Progression", false, 4);

        generateDistanceMatrix();

        AlgorithmData result = new AlgorithmData();
        result.addMatrix("gdMatrix", geneDistanceMatrix);
        result.addMatrix("rawMatrix", rawMatrix);
        
        result.addParam("maxDist", String.valueOf(maxDist));
        result.addParam("minDist", String.valueOf(minDist));
        result.addParam("num_genes", String.valueOf(num_genes));
        return result;

    }


    public void abort() {
        stop = true;
    }

	// Calculate the Actual "Raw" distance between a pair of genes.
    synchronized float getRawDistance(int g1, int g2) {
		float result = Float.NaN;
		switch (function) {
	    	case Algorithm.PEARSON:
				result = ExperimentUtil.genePearson(expMatrix, null, g1, g2, factor);
				break;
	    	case Algorithm.COSINE:
				result = ExperimentUtil.geneCosine(expMatrix, null, g1, g2, factor);
				break;
	    	case Algorithm.COVARIANCE:
				result = ExperimentUtil.geneCovariance(expMatrix, null, g1, g2, factor);
				break;
	    	case Algorithm.EUCLIDEAN:
				result = ExperimentUtil.geneEuclidianDistance(expMatrix, null, g1, g2, factor);
				break;
	    	case Algorithm.DOTPRODUCT:
				result = ExperimentUtil.geneDotProduct(expMatrix, null, g1, g2, factor);
				break;
	    	case Algorithm.PEARSONUNCENTERED:
				result = ExperimentUtil.genePearsonUncentered(expMatrix, null, g1, g2, factor);
				break;
	    	case Algorithm.PEARSONSQARED:
				result = (float)Math.pow(ExperimentUtil.genePearsonUncentered(expMatrix, null, g1, g2, factor), 2)*factor;
				break;
	    	case Algorithm.MANHATTAN:
				result = ExperimentUtil.geneManhattan(expMatrix, null, g1, g2, factor);
				break;
	    	case Algorithm.SPEARMANRANK:
				result = ExperimentUtil.geneSpearmanRank(expMatrix, null, g1, g2, factor);
				break;
	    	case Algorithm.KENDALLSTAU:
				result = ExperimentUtil.geneKendallsTau(expMatrix, null, g1, g2, factor);
				break;
	    	case Algorithm.MUTUALINFORMATION:
				result = ExperimentUtil.geneMutualInformation(expMatrix, null, g1, g2, factor);
				break;
	    	default: {}
		}

		if (absolute) {
	    	result = Math.abs(result);
		}
		return result;
	}

	// Scale the calculated "raw" distance value to range between 0 & 1.
    synchronized float getScaledDistance(int g1, int g2, float max, float min) {

		float val = Float.NaN;
		float rawval = rawMatrix.get(g1, g2);
		
		if (Float.isNaN(rawval))
			return rawval;
			
    	switch(function) {
    		case Algorithm.EUCLIDEAN: 	
    		case Algorithm.MANHATTAN:
    			val = rawval / max;
    			break;
    			
    		case Algorithm.PEARSON:
	    	case Algorithm.PEARSONUNCENTERED:
	    	case Algorithm.SPEARMANRANK:
	    	case Algorithm.KENDALLSTAU:
	    	case Algorithm.MUTUALINFORMATION:
	    	case Algorithm.COSINE:
	    		if (!absolute) {
	    			val = (1 - rawval) / 2;
	    		}else {
					val = 1 - Math.abs(rawval);
				}
				break;
    			
    		case Algorithm.PEARSONSQARED:
    			{
    				float pearson;
    				if (!absolute) {
	    				pearson = (1 - rawval) / 2;
	    			} else {
						pearson = 1 - Math.abs(rawval);
					}
					val = 1 - (pearson*pearson);
					break;
				}
	    	case Algorithm.DOTPRODUCT:
	    	case Algorithm.COVARIANCE:
	    		{
	    			float tmp;
					if (!absolute) {
						tmp = (max - rawval) / (max - min);
					} else {
						max = (Math.abs(min) > Math.abs(max)) ? Math.abs(min) : Math.abs(max);
						tmp = Math.abs(rawval) / max;
					}
					val = 1 - tmp;
					break;					
				}
			default: {}
    	}
    	return val;
    }

		
    // Calculate the Gene Distance Matrix
    synchronized void generateDistanceMatrix() throws AbortException {

        AlgorithmEvent event = new AlgorithmEvent(this, AlgorithmEvent.SET_UNITS, 100, "Construct Distance Matrix");
        fireValueChanged(event);

        event.setId(AlgorithmEvent.PROGRESS_VALUE);

        int progress = 0;
        int sum = num_genes * num_genes;
        int step = sum/100+1;

	int i,j,k,nan;
	float dist, rawdist, maxraw, minraw;

	maxraw = minraw = maxDist = minDist = nan = 0;
	for (i=0;i<num_genes;i++){

                progress++;

		for (j=i+1; j<num_genes; j++) {
			if (stop) {
				throw new AbortException();
			}               
			rawdist = getRawDistance(i, j);
			if (!Float.isNaN(rawdist)) {
				maxraw = Math.max(maxraw, rawdist);
				minraw = Math.min(minraw, rawdist);
				if (rawdist == 0 && i != j) {
					int cols = expMatrix.getColumnDimension();
					for (k=0; k<cols; k++) {
    					if (!Float.isNaN(expMatrix.get(i,k)) && !Float.isNaN(expMatrix.get(j,k))) {
    						break;	    						
    					}
				}			
					if (k == cols) {
						rawdist = Float.NaN;
						nan++;
					}			
				}
				rawMatrix.set(i, j, rawdist);
				rawMatrix.set(j, i, rawdist);

			        // progress events handling
                                progress++;
                                if (progress%step == 0) {
                                    event.setIntValue(progress/step);
                                    event.setDescription("Construct Distance Matrix");
                                    fireValueChanged(event);
                                }
			} 
		}
	}

	for (i=0; i<num_genes; i++) {

                progress++;

		for (j=i+1; j<num_genes; j++) {            		
			if (stop) {
				throw new AbortException();
			}               
			dist = getScaledDistance(i, j, maxraw, minraw);
			maxDist = Math.max(maxDist, dist);
			minDist = Math.min(minDist, dist);
			geneDistanceMatrix.set(i, j, dist);      
			geneDistanceMatrix.set(j, i, dist);         	   					   	

                        // progress events handling
                        progress++;
                        if (progress%step == 0) {
                            event.setIntValue(progress/step);
                            event.setDescription("Construct Distance Matrix");
                            fireValueChanged(event);
                        }
		}
	}
	// Since we are displaying "scaled" distance values, therefore set min=0 & max=1
	minDist = 0;
	maxDist = 1;            
    }

}
