/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: KMCSupport.java,v $
 * $Revision: 1.6 $
 * $Date: 2005-03-10 15:45:21 $
 * $Author: braistedj $
 * $State: Exp $
 */

package org.tigr.microarray.mev.cluster.algorithm.impl;

import java.util.HashSet;
import java.util.Vector;

import org.tigr.microarray.mev.cluster.Cluster;
import org.tigr.microarray.mev.cluster.Node;
import org.tigr.microarray.mev.cluster.NodeList;
import org.tigr.microarray.mev.cluster.NodeValue;
import org.tigr.microarray.mev.cluster.NodeValueList;
import org.tigr.microarray.mev.cluster.algorithm.AbortException;
import org.tigr.microarray.mev.cluster.algorithm.AbstractAlgorithm;
import org.tigr.microarray.mev.cluster.algorithm.Algorithm;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmData;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmEvent;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmException;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmParameters;
import org.tigr.util.FloatMatrix;
   
/**
 *
 * @author  nbhagaba
 * @version
 */
public class KMCSupport extends AbstractAlgorithm {
    
    private boolean stop = false;
    
    private int function;
    private float factor;
    private boolean absolute;
    
    private Vector clusterVector = new Vector();
    boolean[] assigned;// = new boolean[numGenes];
    private Vector[] clusters;
    private boolean kmcGenes;
    private int numGenes, numSamples, numReps;
    private int k; // # of clusters
    private float thresholdPercent;
    private short[][] geneMatrix;
    private int userK;
    
    private FloatMatrix expMatrix;
    private int iterations;
    private boolean converged;
    private boolean unassignedExists;
    private boolean calculateMeans;
    HashSet unassignedGeneSet = new HashSet();

    private int hcl_function;
    private boolean hcl_absolute;    
    
    public AlgorithmData execute(AlgorithmData data) throws AlgorithmException {
        
        AlgorithmParameters map = data.getParams();
        
        function = map.getInt("distance-function", EUCLIDEAN);
        factor   = map.getFloat("distance-factor", 1.0f);
        absolute = map.getBoolean("distance-absolute", false);

        hcl_function = map.getInt("hcl-distance-function", EUCLIDEAN);
        hcl_absolute = map.getBoolean("hcl-distance-absolute", false);        
        
        iterations = map.getInt("number-of-iterations", 50);
        kmcGenes = map.getBoolean("kmc-cluster-genes", true);
        //int number_of_clusters = map.getInt("number_of_clusters", 5);
        
        boolean hierarchical_tree = map.getBoolean("hierarchical-tree", false);
        int method_linkage = map.getInt("method-linkage", 0);
        boolean calculate_genes = map.getBoolean("calculate-genes", false);
        boolean calculate_experiments = map.getBoolean("calculate-experiments", false);
        
        this.expMatrix = data.getMatrix("experiment");
        
        numGenes = this.expMatrix.getRowDimension();
        numSamples = this.expMatrix.getColumnDimension();
        numReps = map.getInt("number-of-repetitions", 0);
        thresholdPercent = map.getFloat("threshold-percent", 80);
        userK = map.getInt("number-of-desired-clusters", 5);
        calculateMeans = map.getBoolean("calculate-means", true);
        
        geneMatrix = new short[numGenes][];
        assigned = new boolean[numGenes];
        unassignedExists = false;
        for (int i = 0; i < numGenes; i++) {
            assigned[i] = false;
        }
        
        for(int i = 1; i < numGenes; i++) {
            geneMatrix[i] = new short[i];
            for(int j = 0; j < geneMatrix[i].length; j++) {
                geneMatrix[i][j] = 0;
            }
        }
        
        populateGeneMatrix();
        createClusters();
        
        k = clusterVector.size();
        
        clusters = new Vector[k];
        
        for (int i = 0; i < k; i++) {
            clusters[i] = (Vector)(clusterVector.get(i));
        }
        
        FloatMatrix means = getMeans(clusters);
        FloatMatrix variances = getVariances(clusters, means);
        
        AlgorithmEvent event = null;
        if (hierarchical_tree) {
            event = new AlgorithmEvent(this, AlgorithmEvent.SET_UNITS, clusters.length, "Calculate Hierarchical Trees");
            fireValueChanged(event);
            event.setIntValue(0);
            event.setId(AlgorithmEvent.PROGRESS_VALUE);
            fireValueChanged(event);
        }
        
        Cluster result_cluster = new Cluster();
        NodeList nodeList = result_cluster.getNodeList();
        int[] features;
        for (int i=0; i<clusters.length; i++) {
            if (stop) {
                throw new AbortException();
            }
            features = convert2int(clusters[i]);
            Node node = new Node(features);
            nodeList.addNode(node);
            if (hierarchical_tree) {
                node.setValues(calculateHierarchicalTree(features, method_linkage, calculate_genes, calculate_experiments));
                event.setIntValue(i+1);
                fireValueChanged(event);
            }
        }
        
        // prepare the result
        AlgorithmData result = new AlgorithmData();
        result.addCluster("cluster", result_cluster);
        result.addParam("number-of-clusters", String.valueOf(clusters.length));
        result.addParam("unassigned-genes-exist", String.valueOf(unassignedExists));
        result.addMatrix("clusters_means", means);
        result.addMatrix("clusters_variances", variances);
        return result;
        
    }
    
        private NodeValueList calculateHierarchicalTree(int[] features, int method, boolean genes, boolean experiments) throws AlgorithmException {
	NodeValueList nodeList = new NodeValueList();
	AlgorithmData data = new AlgorithmData();
	FloatMatrix experiment;
        if(kmcGenes)
            experiment = getSubExperiment(this.expMatrix, features);
        else
            experiment = getSubExperimentReducedCols(this.expMatrix, features);
        
	data.addMatrix("experiment", experiment);
        data.addParam("hcl-distance-function", String.valueOf(this.hcl_function));
        data.addParam("hcl-distance-absolute", String.valueOf(this.hcl_absolute));
	data.addParam("method-linkage", String.valueOf(method));
	HCL hcl = new HCL();
	AlgorithmData result;
        
	if (genes) {
	    data.addParam("calculate-genes", String.valueOf(true));
	    result = hcl.execute(data);
	    validate(result);
	    addNodeValues(nodeList, result);
	}
	if (experiments) {
	    data.addParam("calculate-genes", String.valueOf(false));
	    result = hcl.execute(data);
	    validate(result);
	    addNodeValues(nodeList, result);
	}
	return nodeList;
    }
    /*
    private NodeValueList calculateHierarchicalTree(int[] features, int method, boolean genes, boolean experiments) throws AlgorithmException {
        NodeValueList nodeList = new NodeValueList();
        AlgorithmData data = new AlgorithmData();
        FloatMatrix experiment = getSubExperiment(this.expMatrix, features);
        data.addMatrix("experiment", experiment);
        data.addParam("distance-function", String.valueOf(this.function));
        data.addParam("distance-absolute", String.valueOf(this.absolute));
        data.addParam("method-linkage", String.valueOf(method));
        HCL hcl = new HCL();
        AlgorithmData result;
        if (genes) {
            data.addParam("calculate-genes", String.valueOf(true));
            result = hcl.execute(data);
            validate(result);
            addNodeValues(nodeList, result);
        }
        if (experiments) {
            data.addParam("calculate-genes", String.valueOf(false));
            result = hcl.execute(data);
            validate(result);
            addNodeValues(nodeList, result);
        }
        return nodeList;
    }
    */
    private void addNodeValues(NodeValueList target_list, AlgorithmData source_result) {
        target_list.addNodeValue(new NodeValue("child-1-array", source_result.getIntArray("child-1-array")));
        target_list.addNodeValue(new NodeValue("child-2-array", source_result.getIntArray("child-2-array")));
        target_list.addNodeValue(new NodeValue("node-order", source_result.getIntArray("node-order")));
        target_list.addNodeValue(new NodeValue("height", source_result.getMatrix("height").getRowPackedCopy()));
    }
    
    private FloatMatrix getSubExperiment(FloatMatrix experiment, int[] features) {
        FloatMatrix subExperiment = new FloatMatrix(features.length, experiment.getColumnDimension());
        for (int i=0; i<features.length; i++) {
            subExperiment.A[i] = experiment.A[features[i]];
        }
        return subExperiment;
    }
    
    /**
     *  Creates a matrix with reduced columns (samples) as during experiment clustering
     */
    private FloatMatrix getSubExperimentReducedCols(FloatMatrix experiment, int[] features) {
        FloatMatrix copyMatrix = experiment.copy();
        FloatMatrix subExperiment = new FloatMatrix(features.length, copyMatrix.getColumnDimension());
        for (int i=0; i<features.length; i++) {
            subExperiment.A[i] = copyMatrix.A[features[i]];
        }
        subExperiment = subExperiment.transpose();
        return subExperiment;
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
    
    private int[] convert2int(Vector source) {
        int[] int_matrix = new int[source.size()];
        for (int i=0; i<int_matrix.length; i++) {
            int_matrix[i] = (int)((Integer)source.get(i)).intValue();
        }
        return int_matrix;
    }
    
    private FloatMatrix getMeans(Vector[] clusters) {
        FloatMatrix means = new FloatMatrix(clusters.length, numSamples);
        FloatMatrix mean;
        for (int i=0; i<clusters.length; i++) {
            mean = getMean(clusters[i]);
            means.A[i] = mean.A[0];
        }
        return means;
    }
    
    private FloatMatrix getMean(Vector cluster) {
        FloatMatrix mean = new FloatMatrix(1, numSamples);
        float currentMean;
        int n = cluster.size();
        int denom;
        float value;
        for (int i=0; i<numSamples; i++) {
            currentMean = 0f;
            denom = 0;
            for (int j=0; j<n; j++) {
                value = expMatrix.get(((Integer) cluster.get(j)).intValue(), i);
                if (!Float.isNaN(value)) {
                    currentMean += value;
                    denom++;
                }
            }
            mean.set(0, i, currentMean/(float)denom);
        }
        
        return mean;
    }
    
    private FloatMatrix getVariances(Vector[] clusters, FloatMatrix means) {
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
    
    private float getSampleNormalizedSum(Vector cluster, int column, float mean) {
        final int size = cluster.size();
        float sum = 0f;
        validN = 0;
        float value;
        for (int i=0; i<size; i++) {
            value = expMatrix.get(((Integer) cluster.get(i)).intValue(), column);
            if (!Float.isNaN(value)) {
                sum += Math.pow(value-mean, 2);
                validN++;
            }
        }
        return sum;
    }
    
    private float getSampleVariance(Vector cluster, int column, float mean) {
        return(float)Math.sqrt(getSampleNormalizedSum(cluster, column, mean)/(float)(validN-1));
        
    }
    
    void populateGeneMatrix() throws AlgorithmException {
        
        Algorithm sub_algo = new KMC();
        
        AlgorithmData sub_algo_data = new AlgorithmData();
        sub_algo_data.addMatrix("experiment", expMatrix);
        sub_algo_data.addParam("distance-factor", String.valueOf(factor));
        sub_algo_data.addParam("distance-absolute", String.valueOf(absolute));
        sub_algo_data.addParam("distance-function", String.valueOf(function));
        sub_algo_data.addParam("number-of-iterations", String.valueOf(iterations));
        sub_algo_data.addParam("number-of-clusters", String.valueOf(userK));
        sub_algo_data.addParam("calculate-means", String.valueOf(calculateMeans));
        
        AlgorithmEvent event = new AlgorithmEvent(this, AlgorithmEvent.SET_UNITS, numReps);
        fireValueChanged(event);
        event.setId(AlgorithmEvent.PROGRESS_VALUE);
        
        AlgorithmData sub_algo_result;
        Cluster sub_algo_clusters;
        
        for (int i = 0; i < numReps; i++) {
            //myKMC = new KMC(ParentFrame, ExpMatrix, ResultFolder, Tree, StatusLabel, AntiAliasingMenuItem); //ARE THE PARAMETERS PASSED CORRECTLY?
            //myKMC.k = userK;
            //myKMC.Calculate(true);
            //Vector myKMCClusters = myKMC.getKMClustersVector();
            
                        /*
                        int cLength = myKMC.clusters.length;
                        for (int ii = 0; ii < cLength; ii++) {
                                myKMCClusters.addElement(myKMC.clusters[ii]); //will this work?
                        }
                         */
            
            if (stop) {
                throw new AbortException();
            }
            
            event.setIntValue(i);
            event.setDescription("Current repetition = " + (i + 1));
            fireValueChanged(event);
            
            sub_algo_result = sub_algo.execute(sub_algo_data);
            
            sub_algo_clusters = sub_algo_result.getCluster("cluster");
            
            //Vector myKMCClusters = getClusterVector(sub_algo_clusters);
            int[][] myKMCClusters = getClusterArray(sub_algo_clusters);
            
            //System.out.println("Finished up to getClusterVector(), i = " + i);
            
            for(int j = 1; j < numGenes; j++) {
                for(int k = 0; k < j; k++) {
                    if(occurInSameCluster(j,k, myKMCClusters)) {//write the method "occurInSameCluster(int, int, Vector)"
                        geneMatrix[j][k] = (short)(geneMatrix[j][k] + 1);
                        //System.out.println("genes " + j + " and " + k + " occur in the same cluster");
                    }
                }
            }
            
            //System.out.println("Finished occurInSameCluster(), i = " + i);
            
        }
        
/*        
        for(int j = 1; j < numGenes; j++) {
            for(int k = 0; k < j; k++) {
                geneMatrix[j][k] = (geneMatrix[j][k]/numReps)*100;
            }
        }
 */
        
        /*
        for (int p = 1; p < geneMatrix.length; p++) {
            for (int q = 0; q < geneMatrix[p].length; q++) {
                if (geneMatrix[p][q] > 0.0f) {
                    System.out.println("geneMatrix[" + p + "][" + q + "] = " + geneMatrix[p][q]);
                }
            }
        }
         */
        
        
    }
    
/*    
    private Vector getClusterVector(Cluster clusters) {
        NodeList nodeList = clusters.getNodeList();
        final int number_of_clusters = nodeList.getSize();
        int[] cluster;
        
        Vector cVector = new Vector();
        
        for (int j=0; j<number_of_clusters; j++) {
            cluster = nodeList.getNode(j).getFeaturesIndexes();
            Vector currentCluster = new Vector();
            
            for (int i = 0; i < cluster.length; i++) {
                currentCluster.add(new Integer(cluster[i]));
            }
            
            cVector.add(currentCluster);
        }
        
        return cVector;
    }
 */
    
    private int[][] getClusterArray(Cluster  clusters) {
        NodeList nodeList = clusters.getNodeList();
        final int number_of_clusters = nodeList.getSize();
        int[] cluster;
        
        int[][] cArray = new int[number_of_clusters][];
        
        for (int j = 0; j < number_of_clusters; j++) {
            cluster = nodeList.getNode(j).getFeaturesIndexes();
            
            cArray[j] = new int[cluster.length];
            for (int i = 0; i < cluster.length; i++) {
                cArray[j][i] = cluster[i];
                //currentCluster.add(new Integer(cluster[i]));
            }            
        }
        
        return cArray;
    }
    
/*    
    boolean occurInSameCluster(int gene1, int gene2, Vector clustVect) {
        boolean occurs = false;
        
        for (int i = 0; i < clustVect.size(); i++) {
            Vector currCluster = (Vector)clustVect.get(i);
            if( (isFound(gene1, currCluster)) && (isFound(gene2, currCluster)) ) {
                occurs = true;
                break;
            }
        }
        
        return occurs;
    }
 */
    
    boolean occurInSameCluster(int gene1, int gene2, int[][] clustArr) {
        boolean occurs = false;
        
        for (int i = 0; i < clustArr.length; i++) {
            int[] currCluster = clustArr[i];
            if( (isFound(gene1, currCluster)) && (isFound(gene2, currCluster)) ) {
                //occurs = true;
                //break;
                return true;
            }
        }
        
        return occurs;
    }    
    
    
    void createClusters() throws AlgorithmException {
        
        
        int currentGene, comparisonGene;
        //boolean[] assigned = new boolean[numGenes];
                /*
                for (int i = 0; i < numGenes; i++) {
                        assigned[i] = false;
                }
                 */
        for (currentGene = 1; currentGene < numGenes; currentGene++) {
            
            for (comparisonGene = 0; comparisonGene < currentGene; comparisonGene++) {
                if ( (float)(((float)(geneMatrix[currentGene][comparisonGene])/numReps)*100) >= thresholdPercent) {
                    if (assigned[comparisonGene]) {
                        addToCluster(currentGene, comparisonGene);
                        assigned[currentGene] = true;
                        break;
                    } else {
                        Vector currentCluster = new Vector();
                        currentCluster.add(new Integer(currentGene));
                        currentCluster.add(new Integer(comparisonGene));
                        clusterVector.add(currentCluster);
                        assigned[currentGene] = true;
                        assigned[comparisonGene] = true;
                        break;
                    }
                }
            }
        }
        
        
        //Vector unassignedGeneCluster = new Vector();
        for(int i = 0; i < assigned.length; i++) {
            if (!assigned[i]) {
                //System.out.println("" + i);
                //unassignedExists = true;
                unassignedGeneSet.add(new Integer(i));
            }
        }
        //System.out.println();
        
        Vector workingClusterVector = new Vector();
        
        for (int i = 0; i < clusterVector.size(); i++) {
            Vector currentClust = (Vector)(clusterVector.get(i));
            Vector weededOutCluster = weedOutLowerThanThreshGenes(currentClust);
            if (weededOutCluster.size() > 0) {
                workingClusterVector.add(weededOutCluster);
            }
        }
        
        clusterVector = workingClusterVector;
        
        if (unassignedGeneSet.size() > 0) {
            for (int i = 0; i < clusterVector.size(); i++) {
                Vector currentClust = (Vector)(clusterVector.get(i));
                Vector recheckedCluster = recheckWithUnassigned(currentClust);
                clusterVector.set(i, recheckedCluster);
            }
        }
        
        
        
        if (unassignedGeneSet.size() > 0) {
            //System.out.println("Unassigned Unique ID Indices: ");
            unassignedExists = true;
            Vector unassignedVector = new Vector(unassignedGeneSet);
                    /*
                    for (int i = 0; i < unassignedVector.size(); i++) {
                        int unassignedGene = ((Integer)(unassignedVector.get(i))).intValue();
                        System.out.println(unassignedGene);
                    }*/
            clusterVector.add(unassignedVector);
        }
        
        
               /*
                if (unassignedExists) {
                    clusterVector.add(unassignedGeneCluster);
                }
                */
        
        
                /*
                for(int i = 0; i < clusterVector.size(); i++) {
                        System.out.println("Cluster " + i);
                        Vector currClust = (Vector)clusterVector.get(i);
                        for (int j = 0; j < currClust.size(); j++) {
                                System.out.println("" + ((Integer)currClust.get(j)).intValue());
                        }
                        System.out.println();
                }
                 */
        
        for (int k = 0; k < numGenes; k++) {
            int found = 0;
            for (int l = 0; l < clusterVector.size(); l++) {
                if (isFound(k, (Vector)clusterVector.get(l))) found++;
            }
            
            if (found > 1) System.out.println("Warning: UniqueID[" + k + "] is found in " + found + "clusters");
            
            errorCheck1(k);
            errorCheck2(k);
        }
        
        
    }
    
    
    private Vector weedOutLowerThanThreshGenes(Vector geneCluster) {
        
        HashSet weededOutGenes = new HashSet();
        
        for (int i = 0; i < (geneCluster.size() - 1); i++) {
            int currentGene = ((Integer)(geneCluster.get(i))).intValue();
            for (int j = i+1; j < geneCluster.size(); j++) {
                int gene = ((Integer)(geneCluster.get(j))).intValue();
                if (currentGene > gene) {
                    if ( (float)(((float)(geneMatrix[currentGene][gene])/numReps)*100) < thresholdPercent) {
                        unassignedGeneSet.add(new Integer(currentGene));
                        unassignedGeneSet.add(new Integer(gene));
                        weededOutGenes.add(new Integer(currentGene));
                        weededOutGenes.add(new Integer(gene));
                    }
                } else if (currentGene < gene) {
                    if ( (float)(((float)(geneMatrix[gene][currentGene])/numReps)*100) < thresholdPercent) {
                        unassignedGeneSet.add(new Integer(currentGene));
                        unassignedGeneSet.add(new Integer(gene));
                        weededOutGenes.add(new Integer(currentGene));
                        weededOutGenes.add(new Integer(gene));
                    }
                }
            }
        }
        
        geneCluster.removeAll(weededOutGenes);
        return geneCluster;
        
    }
    
    private Vector recheckWithUnassigned(Vector geneCluster) { // checks the unassigend genes with the current clusters to see if they belong in there
        Vector localUnassignedVect = new Vector(unassignedGeneSet);
        for (int i = 0; i < localUnassignedVect.size(); i++) {
            int unassignedGene = ((Integer)(localUnassignedVect.get(i))).intValue();
            if (belongsInCluster(unassignedGene, geneCluster)) {
                geneCluster.add(new Integer(unassignedGene));
                unassignedGeneSet.remove(new Integer(unassignedGene));
            }
        }
        
        return geneCluster;
    }
    
    private boolean belongsInCluster(int gene, Vector geneCluster) {
        boolean belongs = true;
        
        for (int i = 0; i < geneCluster.size(); i++) {
            int currentGene = ((Integer)(geneCluster.get(i))).intValue();
            if (gene == currentGene) {
                belongs = true;
                break;
            } else if (gene > currentGene) {
                if ( (float)(((float)(geneMatrix[gene][currentGene])/numReps)*100) < thresholdPercent) {
                    belongs = false;
                    break;
                }
            } else if (gene < currentGene) {
                if ( (float)(((float)(geneMatrix[currentGene][gene])/numReps)*100) < thresholdPercent) {
                    belongs = false;
                    break;
                }
            }
            
        }
        return belongs;
    }
        /*
        private float[][] createMatrix(Vector geneCluster) {
            float[][] currentMatrix = new float[geneCluster.size()][];
         
        }
         */
    
    
    void addToCluster(int geneToBeAdded, int geneInTargetCluster) {
        
        for(int i = 0; i < clusterVector.size(); i++) {
            Vector currentCluster = (Vector)clusterVector.get(i);
            if (isFound(geneInTargetCluster, currentCluster)) {
                currentCluster.add(new Integer(geneToBeAdded));
                clusterVector.set(i, currentCluster);
                break;
            }
        }
        
    }
    
    
    boolean isFound(int gene, Vector clustVect) {
        boolean found = false;
        
        for (int i = 0; i < clustVect.size(); i++) {
            //System.out.println(clustVect.get(i));
            if (gene == ((Integer) clustVect.get(i)).intValue()) {
                //found =true;
                //break;
                return true;
            }
        }
        
        return found;
    }

    
    boolean isFound(int gene, int[] clustArr) {
        boolean found = false;
        
        for (int i = 0; i < clustArr.length; i++) {
            //System.out.println(clustVect.get(i));
            if (gene == clustArr[i]) {
                //found =true;
                //break;
                return true;
            }
        }
        
        return found;
    }    
    
    public boolean unassignedGenesExist() {
        return unassignedExists;
    }
    
    
    void errorCheck1(int gene) {//CHECK IF A GENE REMAINS UNASSIGNED EVEN IF IT EQUALS OR EXCEEDS THE THRESHOLD PERCENTAGE FOR AT LEAST ONE ENTRY
        boolean error1 = false;
        if (gene == 0) {
            if(!assigned[gene]) {
                for (int i = 1; i < numGenes; i++) {
                    if ( (float)(((float)(geneMatrix[i][0])/numReps)*100) >= thresholdPercent) {
                        error1 = true;
                        break;
                    }
                }
            }
        } else {
            
            if (!assigned[gene]) {
                for (int i = 0; i < gene; i++) {
                    if ( (float)(((float)(geneMatrix[gene][i])/numReps)*100) >= thresholdPercent){
                        error1 = true;
                        break;
                    }
                }
            }
        }
        
        if (error1) {
            System.out.println("Warning: UniqueID[" + gene + "] not assigned even though it equals or exceeds the threshold % for at least one entry");
        }
    }
    
    
    void errorCheck2(int gene) {//CHECK IF GENE GETS ASSIGNED TO WRONG CLUSTER, I.E., GETS ASSIGNED TO A CLUSTER WITH WHICH ITS "AFFINITY" IS LESS THAN THE THRESHOLD PERCENTAGE
        boolean error2 = false;
        Vector foundInClusters = new Vector();
        
        for (int j = 0; j < clusterVector.size(); j++) {
            if (isFound(gene, (Vector)clusterVector.get(j))) {
                foundInClusters.add(new Integer(j));
            }
        }
        
        out:
            for (int i = 0; i < foundInClusters.size(); i++) {
                int clusterNumber = ((Integer)foundInClusters.get(i)).intValue();
                Vector currClust = (Vector)clusterVector.get(clusterNumber);
                for (int l = 0; l< currClust.size(); l++) {
                    int currGene = ((Integer)currClust.get(l)).intValue();
                    if (currGene == gene) {
                        continue;
                    } else if (currGene > gene) {
                        if ( (float)(((float)(geneMatrix[currGene][gene])/numReps)*100) < thresholdPercent) {
                            error2 = true;
                            if ((unassignedExists)&&(clusterNumber == (clusterVector.size() - 1))) {
                            } else {
                                System.out.println("Warning: UniqueID[" + gene + "] got assigned to cluster" + clusterNumber +" where its 'affinity' to at least one gene is less than the threshold %");
                                System.out.println("geneMatrix[" + currGene + "][" + gene + "] = " + geneMatrix[currGene][gene]);
                            }
                            break out;
                        }
                    } else {
                        if ( (float)(((float)(geneMatrix[gene][currGene])/numReps)*100) < thresholdPercent) {
                            error2 = true;
                            if ((unassignedExists)&&(clusterNumber == (clusterVector.size() - 1))) {
                            } else {
                                System.out.println("Warning: UniqueID[" + gene + "] got assigned to cluster" + clusterNumber +" where its 'affinity' to at least one gene is less than the threshold %");
                                System.out.println("geneMatrix[" + gene + "][" + currGene + "] = " + geneMatrix[gene][currGene]);
                            }
                            break out;
                            
                        }
                    }
                }
            }
            
                /*
                if (error2) {
                        System.out.println("Warning: UniqueID[" + gene + "] got assigned to a cluster where its 'affinity' to at least one gene is less than the threshold %");
                }
                 */
            
    }
    
    
    public Vector itf(Vector integerVector) {
        Vector floatVector = new Vector();
        
        for (int i = 0; i < integerVector.size(); i++) {
            floatVector.addElement(new Float(((Integer) integerVector.elementAt(i)).intValue()));
        }
        
        return floatVector;
    }
    
    public void abort() {
        stop = true;
    }
}
