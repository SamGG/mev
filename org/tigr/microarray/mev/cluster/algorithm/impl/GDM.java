/*
Copyright @ 1999-2002, The Institute for Genomic Research (TIGR).  
All rights reserved.

This software is provided "AS IS".  TIGR makes no warranties, express
or implied, including no representation or warranty with respect to
the performance of the software and derivatives or their safety,
effectiveness, or commercial viability.  TIGR does not warrant the
merchantability or fitness of the software and derivatives for any
particular purpose, or that they may be exploited without infringing
the copyrights, patent rights or property rights of others. TIGR shall
not be liable for any claim, demand or action for any loss, harm,
illness or other damage or injury arising from access to or use of the
software or associated information, including without limitation any
direct, indirect, incidental, exemplary, special or consequential
damages.

This software program may not be sold, leased, transferred, exported
or otherwise disclaimed to anyone, in whole or in part, without the
prior written consent of TIGR.
*/
package org.tigr.microarray.mev.cluster.algorithm.impl;

import java.awt.BorderLayout;
import java.awt.event.*;
import java.util.Random;
import java.util.ArrayList;
import java.util.Vector;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.tigr.util.FloatMatrix;
import org.tigr.util.awt.ProgressDialog;

import org.tigr.microarray.mev.cluster.Node;
import org.tigr.microarray.mev.cluster.Cluster;
import org.tigr.microarray.mev.cluster.NodeList;
import org.tigr.microarray.mev.cluster.NodeValue;
import org.tigr.microarray.mev.cluster.NodeValueList;

import org.tigr.microarray.mev.cluster.algorithm.Algorithm;
import org.tigr.microarray.mev.cluster.algorithm.AbstractAlgorithm;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmData;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmParameters;

import org.tigr.microarray.mev.cluster.algorithm.AlgorithmException;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmEvent;
import org.tigr.microarray.mev.cluster.algorithm.AbortException;

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
            throw new AlgorithmException("Experiment data is absent.");
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