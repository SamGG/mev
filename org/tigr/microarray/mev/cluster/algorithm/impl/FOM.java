/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: FOM.java,v $
 * $Revision: 1.1.1.2 $
 * $Date: 2004-02-06 21:48:18 $
 * $Author: braisted $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.algorithm.impl;

import org.tigr.util.ConfMap;
import org.tigr.util.FloatMatrix;

import org.tigr.microarray.mev.cluster.algorithm.AlgorithmData;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmParameters;
import org.tigr.microarray.mev.cluster.algorithm.AbortException;
import org.tigr.microarray.mev.cluster.algorithm.AbstractAlgorithm;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmException;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmEvent;
import org.tigr.microarray.mev.cluster.algorithm.Algorithm;
import org.tigr.microarray.mev.cluster.Cluster;
import org.tigr.microarray.mev.cluster.NodeList;

public class FOM extends AbstractAlgorithm {
    
    private boolean stop = false;
    private boolean clusterGenes;
    /**
     * This method execute calculation and return result,
     * stored in <code>AlgorithmData</code> class.
     *
     * @param data the data to be calculated.
     */
    public AlgorithmData execute(AlgorithmData data) throws AlgorithmException {
        
        AlgorithmParameters map = data.getParams();
        
        int function = map.getInt("distance-function", EUCLIDEAN);
        float factor   = map.getFloat("distance-factor", 1.0f);
        boolean absolute = map.getBoolean("distance-absolute", false);
        
        clusterGenes = map.getBoolean("cluster-genes", true);
        int method      = map.getInt("method", 2);
        float interval  = map.getFloat("interval", 0.1f);
        int iterations  = map.getInt("iterations", 50);
        int maxNumClusters = map.getInt("number_of_clusters", 20);
        boolean average = map.getBoolean("average", true);
        boolean calculateMeans = map.getBoolean("calculate-means", true);
        
        FloatMatrix expMatrix = data.getMatrix("experiment");
        
        int number_of_genes   = expMatrix.getRowDimension();
        int number_of_samples = expMatrix.getColumnDimension();
        
        Algorithm sub_algo = null;
        if (method == 2) sub_algo = new KMC();
        else sub_algo = new CastClust(true);
        
        AlgorithmData sub_algo_data = new AlgorithmData();
        sub_algo_data.addMatrix("experiment", expMatrix);
        sub_algo_data.addParam("distance-factor", String.valueOf(factor));
        sub_algo_data.addParam("distance-absolute", String.valueOf(absolute));
        sub_algo_data.addParam("distance-function", String.valueOf(function));
        sub_algo_data.addParam("number_of_iterations", String.valueOf(iterations));
        sub_algo_data.addParam("calculate-means", String.valueOf(calculateMeans));
        
        /*
        AlgorithmEvent event = new AlgorithmEvent(this, AlgorithmEvent.SET_UNITS, iterations);
        fireValueChanged(event);
        event.setId(AlgorithmEvent.PROGRESS_VALUE);
         */
        AlgorithmData sub_algo_result = null;
        Cluster sub_algo_clusters = new Cluster();
        float[] fom_values = null;
        int times = 0;
        int[] numOfCastClusters = new int[1]; // just to initialize
        
        if (method == 2) {	 //FOM FOR K-MEANS
            AlgorithmEvent event = new AlgorithmEvent(this, AlgorithmEvent.SET_UNITS, maxNumClusters);
            fireValueChanged(event);
            event.setId(AlgorithmEvent.PROGRESS_VALUE);
            sub_algo_data.addParam("kmc-cluster-genes", String.valueOf(clusterGenes));
            fom_values = new float[maxNumClusters];
            for (int i=0; i<maxNumClusters; i++) {
                if (stop) {
                    throw new AbortException();
                }
                event.setIntValue(i);
                event.setDescription("calculating for "+String.valueOf(i+1)+" clusters");
                fireValueChanged(event);
                
                sub_algo_data.addParam("number_of_clusters", String.valueOf(i+1));
                sub_algo_result = sub_algo.execute(sub_algo_data);
                
                sub_algo_clusters = sub_algo_result.getCluster("cluster");
                
                fom_values[i] = (float)getFOM(expMatrix, sub_algo_clusters, number_of_genes, number_of_samples);
            }
        }  else { //FOM FOR CAST
            times = (int)(1/interval);
            AlgorithmEvent event = new AlgorithmEvent(this, AlgorithmEvent.SET_UNITS, times);
            fireValueChanged(event);
            event.setId(AlgorithmEvent.PROGRESS_VALUE);
            //sub_algo.runInside = true;
            //System.out.println("times = " + times);
            sub_algo_data.addParam("cast-cluster-genes", String.valueOf(clusterGenes));
            numOfCastClusters = new int[times];
            float threshold = 0;
            fom_values = new float[times];
            for (int i = 0; i < times; i++) {
                threshold = threshold + interval;
                if (threshold > 1.0f) threshold = 1.0f;
                //System.out.println("Threshold at iteration " + i + " = " + threshold);
                if (stop) {
                    throw new AbortException();
                }
                event.setIntValue(i);
                event.setDescription("calculating for threshold of "+String.valueOf(threshold));
                fireValueChanged(event);
                
                sub_algo_data.addParam("threshold", String.valueOf(threshold));
                sub_algo_result = sub_algo.execute(sub_algo_data);
                //System.out.println("Cast iteration [" + i + "] completed" );
                
                sub_algo_clusters = sub_algo_result.getCluster("cluster");
                
                fom_values[i] = (float)getFOM(expMatrix, sub_algo_clusters, number_of_genes, number_of_samples);
                //System.out.println("Inside FOM: fom_values[" + i + "] = " + fom_values[i]);
                
                numOfCastClusters[i] = sub_algo_clusters.getNodeList().getSize();
                //System.out.println ("Inside FOM: numOfCastClusters[" + i + "] = " + numOfCastClusters[i]);
                
                //sub_algo_result = new AlgorithmData();//added for debugging purposes, probably not needed
            }
        }
        
        AlgorithmData result = new AlgorithmData();
        result.addMatrix("fom-values", new FloatMatrix(fom_values, 1));
        result.addIntArray("numOfCastClusters", numOfCastClusters);
        return result;
    }
    
    public double getFOM(FloatMatrix expMatrix, Cluster clusters, final int number_of_genes, final int number_of_samples) {
        
        int[] cluster;
        float value;
        int n = 0;
        NodeList nodeList = clusters.getNodeList();
        final int number_of_clusters = nodeList.getSize();
        double[][] means = new double[number_of_samples][number_of_clusters];
        for (int i=0; i<number_of_samples; i++) {
            for (int j=0; j<number_of_clusters; j++) {
                means[i][j] = 0.0;
                cluster = nodeList.getNode(j).getFeaturesIndexes();
                n = 0;
                for (int p=0; p<cluster.length; p++) {
                    value = expMatrix.get(cluster[p], i);
                    if (!Float.isNaN(value)) {
                        means[i][j] += (double)value;
                        n++;
                    }
                }
                if(n > 0)
                    means[i][j] /= (double)n;
                else
                    means[i][j] = 0;
            }
        }
        
        double[] tFOM = new double[number_of_samples];
        double factor;
        //if(this.clusterGenes)
          //  factor = Math.sqrt((double)(number_of_genes-number_of_samples)/(double)number_of_genes);
       // else
         //   factor = Math.sqrt((double)(number_of_samples-number_of_genes)/(double)number_of_samples);
      // factor = Math.sqrt(((double)(number_of_genes-number_of_clusters))/(double)number_of_genes);
 factor = 1;        
for (int i=0; i<number_of_samples; i++) {
            tFOM[i] = 0.0;
            for (int j=0; j<number_of_clusters; j++) {
                cluster = nodeList.getNode(j).getFeaturesIndexes();
                for (int p=0; p<cluster.length; p++) {
                    value = expMatrix.get(cluster[p], i);
                    if (!Float.isNaN(value)) {
                        tFOM[i] += Math.pow(((double)value-means[i][j]), 2);
                    }
                }
         //       if(this.clusterGenes)
                    tFOM[i] = Math.sqrt(tFOM[i]/(double)number_of_genes);
        //        else
        //            tFOM[i] = Math.sqrt(tFOM[i]/(double)number_of_samples);
                
                tFOM[i] /= factor;
            }
        }
        
        double fom_value = 0d;
        for (int i=0; i<number_of_samples; i++) {
            fom_value += tFOM[i];
        }
        return fom_value;
    }
    
    /**
     * This method should interrupt the calculation.
     */
    public void abort() {
        this.stop = true;
    }
}
