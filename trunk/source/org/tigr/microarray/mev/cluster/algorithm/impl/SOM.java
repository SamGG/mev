/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: SOM.java,v $
 * $Revision: 1.3 $
 * $Date: 2005-03-10 15:45:19 $
 * $Author: braistedj $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.algorithm.impl;

import java.util.ArrayList;
import java.util.Random;

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
import org.tigr.util.FloatMatrix;

public class SOM extends AbstractAlgorithm {
    
    private static final int INDEX_X = 0;
    private static final int INDEX_Y = 1;
    private static final int WINNER_INFO_SIZE = 2;
    
    private boolean stop = false;
    
    private int function;
    private int dim_x;
    private int dim_y;
    private float factor;
    private boolean absolute;
    private String topology;
    private boolean somGenes;
    
    private int number_of_genes;
    private int number_of_samples;
    
    private FloatMatrix expMatrix;
    private float[][][] somCodes;
    private int validN;

    private int hcl_function;
    private boolean hcl_absolute;    
    
    public AlgorithmData execute(AlgorithmData data) throws AlgorithmException {
        
        AlgorithmParameters map = data.getParams();
        
        function = map.getInt("distance-function", EUCLIDEAN);
        factor   = map.getFloat("distance-factor", 1.0f);
        absolute = map.getBoolean("distance-absolute", false);
        somGenes = map.getBoolean("som-cluster-genes", true);
        
        dim_x = map.getInt("dimension-x", 0);
        dim_y = map.getInt("dimension-y", 0);
        int iterations = map.getInt("iterations", 0);
        topology = map.getString("topology");
        boolean adoptType = topology != null ? topology.compareTo("rectangular")==0 : true;
        boolean is_neighborhood_bubble = map.getBoolean("is_neighborhood_bubble", true);
        boolean is_random_vector = map.getBoolean("is_random_vector", true);
        float radius = map.getFloat("radius", 0.0f);
        float alpha  = map.getFloat("alpha" , 0.0f);
        
        // hcl parameters
        boolean hierarchical_tree = map.getBoolean("hierarchical-tree", false);
        int method_linkage = map.getInt("method-linkage", 0);
        boolean calculate_genes = map.getBoolean("calculate-genes", false);
        boolean calculate_experiments = map.getBoolean("calculate-experiments", false);
        
        this.expMatrix = data.getMatrix("experiment");
        
        hcl_function = map.getInt("hcl-distance-function", EUCLIDEAN);
        hcl_absolute = map.getBoolean("hcl-distance-absolute", false);
        
        number_of_genes   = this.expMatrix.getRowDimension();
        number_of_samples = this.expMatrix.getColumnDimension();
        
        if (is_random_vector) {
            this.somCodes = randomVectorInit();
        } else {
            this.somCodes = randomGeneInit();
        }
        
        int[] winner_info = new int[WINNER_INFO_SIZE];
        float cRadius = radius;
        float cAlpha = alpha;
        AlgorithmEvent event = new AlgorithmEvent(this, AlgorithmEvent.SET_UNITS, 200, "Training...");
        fireValueChanged(event);
        event.setId(AlgorithmEvent.PROGRESS_VALUE);
        event.setIntValue(0);
        int sample = 0;
        long progress = iterations/200;
        for (int i=0; i<iterations; i++) {
            if (stop) {
                throw new AbortException();
            }
            if (i%progress == 0) {
                event.setIntValue(event.getIntValue()+1);
                fireValueChanged(event);
            }
            /* Radius decreases linearly to one */
            cRadius = 1.0f + (radius - 1.0f)*(float)(iterations-i)/(float)iterations;
            /* Calculate Learning rate */
            cAlpha = linearAlpha(i, iterations, alpha);
            /* Find the best match */
            findWinnerEuclidean(winner_info, sample);
            /* Adapt the units */
            if (is_neighborhood_bubble) {
                bubbleAdapt(sample, winner_info, cRadius, cAlpha, adoptType);
            } else {
                gaussianAdapt(sample, winner_info, cRadius, cAlpha, adoptType);
            }
            sample++;
            if (sample >= number_of_genes) {
                sample = 0;
            }
        }
        
        // clustering...
        SOMMatrix clusters = new SOMMatrix(dim_x, dim_y, 0);
        FloatMatrix u_matrix = new FloatMatrix(dim_x, dim_y);
        calculateClusters(clusters, u_matrix);
        
        AlgorithmData result = new AlgorithmData();
        FloatMatrix matrix = new FloatMatrix(dim_x*dim_y, number_of_samples);
        // copying somCodes
        for (int i=0; i<number_of_samples; i++) {
            for (int x=0; x<dim_x; x++) {
                for (int y=0; y<dim_y; y++) {
                    matrix.set(x*dim_y+y, i, somCodes[x][y][i]);
                }
            }
        }
        result.addMatrix("codes", matrix);
        
        // means, variances...
        FloatMatrix means     = new FloatMatrix(dim_x*dim_y, number_of_samples);
        FloatMatrix variances = new FloatMatrix(dim_x*dim_y, number_of_samples);
        int[] features;
        int dimension;
        
        Cluster result_cluster = new Cluster();
        NodeList nodeList = result_cluster.getNodeList();
        
        for (int x=0; x<dim_x; x++) {
            for (int y=0; y<dim_y; y++) {
                // copying the clusters
                features = getFeatures(clusters.getArrayList(x, y));
                
                dimension = x*dim_y+y;
                fillMean(means.A[dimension], features);
                fillVariance(variances.A[dimension], means.A[dimension], features);
                
                Node node = new Node(features);
                nodeList.addNode(node);
            }
        }
        if (hierarchical_tree) {
            calculateHierarchicalTrees(result_cluster, method_linkage, calculate_genes, calculate_experiments);
        }
        
        result.addCluster("cluster", result_cluster);
        result.addMatrix("clusters_means", means);
        result.addMatrix("clusters_variances", variances);
        // copying the u-matrix
        result.addMatrix("u_matrix", u_matrix);
        
        return result;
    }
    
    private void calculateHierarchicalTrees(Cluster cluster, int method, boolean genes, boolean experiments) throws AlgorithmException {
        NodeList nodeList = cluster.getNodeList();
        
        AlgorithmEvent event = new AlgorithmEvent(this, AlgorithmEvent.SET_UNITS, nodeList.getSize(), "Calculate Hierarchical Trees");
        fireValueChanged(event);
        event.setId(AlgorithmEvent.PROGRESS_VALUE);
        
        Node node;
        for (int i=0; i<nodeList.getSize(); i++) {
            if (stop) {
                throw new AbortException();
            }
            event.setIntValue(i);
            fireValueChanged(event);
            
            node = nodeList.getNode(i);
            node.setValues(calculateHierarchicalTree(node.getFeaturesIndexes(), method, genes, experiments));
        }
    }
    
    private NodeValueList calculateHierarchicalTree(int[] features, int method, boolean genes, boolean experiments) throws AlgorithmException {
        NodeValueList nodeList = new NodeValueList();
        AlgorithmData data = new AlgorithmData();
        FloatMatrix experiment;
        if(somGenes)
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
    
    public void abort() {
        stop = true;
    }
    
    private int[] getFeatures(ArrayList source) {
        int[] cluster = new int[source.size()];
        for (int i=0; i<cluster.length; i++) {
            cluster[i] = ((Float)source.get(i)).intValue();
        }
        return cluster;
    }
    
    private float[][][] randomGeneInit() {
        Random random = new Random(System.currentTimeMillis());
        float[][][] somCodes = new float[dim_x][dim_y][number_of_samples];
        int gene;
        for (int y=0; y<dim_y; y++) {
            for (int x=0; x<dim_x; x++) {
                gene = (int)(random.nextFloat()*number_of_genes);
                for (int k=0; k<number_of_samples; k++) {
                    somCodes[x][y][k] = this.expMatrix.get(gene, k);
                }
            }
        }
        return somCodes;
    }
    
    private float[][][] randomVectorInit() {
        float[][][] somCodes = new float[dim_x][dim_y][number_of_samples];
        float[] maxValue = new float[number_of_samples];
        float[] minValue = new float[number_of_samples];
        int i, j, k;
        for (i=0; i<number_of_samples; i++) {
            minValue[i] = Float.MAX_VALUE;
            maxValue[i] = Float.MIN_VALUE;
        }
        float dummy;
        for (i=0; i<number_of_genes; i++) {
            for (j=0; j<number_of_samples; j++) {
                dummy = expMatrix.get(i, j);
                if (!Float.isNaN(dummy)) {
                    if (maxValue[j] < dummy) {
                        maxValue[j] = dummy;
                    }
                    if (minValue[j] > dummy) {
                        minValue[j] = dummy;
                    }
                }
            }
        }
        
        float value;
        Random random = new Random(System.currentTimeMillis());
        for (i=0; i<dim_x; i++) {
            for (j=0; j<dim_y; j++) {
                for (k=0; k<number_of_samples; k++) {
                    value = minValue[k]+(maxValue[k]-minValue[k])*random.nextFloat();
                    somCodes[i][j][k] = value;
                }
            }
        }
        return somCodes;
    }
    
    
    /**
     * find_winner_euc - finds the winning entry (1 nearest neighbour) in
     * codebook using euclidean distance. Information about the winning
     * entry is saved in the winner_info structure. Return 1 (the number
     * of neighbours) when successful and 0 when winner could not be found
     * (for example, all components of data vector have been masked off)
     */
    private final float findWinnerEuclidean(int[] winner_info, int sample) {
        winner_info[INDEX_X] = -1;
        winner_info[INDEX_Y] = -1;
        float winner_distance = -1.0f;
        
        if (number_of_samples == 1) {
            return winner_distance;
        }
        
        int x, y, i;
        double difference;
        double diffsf = Double.MAX_VALUE;
        
        FloatMatrix dummyMatrix = new FloatMatrix(1, number_of_samples);
        
        for (y=0; y<dim_y; y++) {
            for (x=0; x<dim_x; x++) {
                for (i = 0; i < number_of_samples; i++) {
                    dummyMatrix.set(0, i, somCodes[x][y][i]);
                }
                difference = ExperimentUtil.geneDistance(expMatrix, dummyMatrix, sample, 0, function, factor, absolute);
                /* If distance is smaller than previous distances */
                if (difference <= diffsf) {
                    winner_info[INDEX_X] = x;
                    winner_info[INDEX_Y] = y;
                    diffsf = difference;
                    winner_distance = (float)difference;
                }
            }
        }
        return winner_distance;
    }
    
    private void bubbleAdapt(int sample, int[] winner_info, float radius, float alpha, boolean rectangular) {
        long index = 0;
        int tx, ty, xdim, ydim;
        int x, y;
        for (y=0; y<dim_y; y++) {
            for (x=0; x<dim_x; x++) {
                if (rectangular) {
                    if (rectangularDistance(winner_info, x, y) <= radius) {
                        adaptVector(sample, x, y, alpha);
                    }
                } else {
                    if (hexagonalDistance(winner_info, x, y) <= radius) {
                        adaptVector(sample, x, y, alpha);
                    }
                }
            }
        }
    }
    
    private void gaussianAdapt(int sample, int[] winner_info, float radius, float alpha, boolean rectangular) {
        long index = 0;
        int tx, ty, xdim, ydim;
        float dd, alp;
        int x, y;
        for (y=0; y<dim_y; y++) {
            for (x=0; x<dim_x; x++) {
                if (rectangular) {
                    dd = rectangularDistance(winner_info, x, y);
                } else {
                    dd = hexagonalDistance(winner_info, x, y);
                }
                alp = alpha*(float)Math.exp((float)(-dd*dd/(2.0*radius*radius)));
                adaptVector(sample, x, y, alp);
            }
        }
    }
    
    private float rectangularDistance(int[] winner_info, int tx, int ty) {
        float ret, diff;
        diff = winner_info[INDEX_X] - tx;
        ret = diff * diff;
        diff = winner_info[INDEX_Y] - ty;
        ret += diff * diff;
        ret = (float)Math.sqrt((float)ret);
        return(ret);
    }
    
    private float hexagonalDistance(int[] winner_info, int tx, int ty) {
        float ret, diff;
        diff = winner_info[INDEX_X] - tx;
        if (((winner_info[INDEX_Y] - ty) % 2) != 0) {
            if ((winner_info[INDEX_Y] % 2) == 0) {
                diff -= 0.5;
            } else {
                diff += 0.5;
            }
        }
        ret = diff * diff;
        diff = winner_info[INDEX_Y] - ty;
        ret += 0.75 * diff * diff;
        ret = (float)Math.sqrt((float) ret);
        return(ret);
    }
    
    /* adapt_vector - move a codebook vector towards another vector */
    private void adaptVector(int sample, int x, int y, float alpha) {
        int i;
        for (i = 0; i < number_of_samples; i++) {
            if (Float.isNaN(expMatrix.get(sample, i))) {
                continue; // ignore vector components that have 1 in mask
            } else {
                somCodes[x][y][i] = somCodes[x][y][i] + alpha*(expMatrix.get(sample, i)-somCodes[x][y][i]);
            }
        }
    }
    
    private float linearAlpha(long currentIteration, long iterations, float alpha) {
        return(alpha*(float)(iterations-currentIteration)/(float)iterations);
    }
    
    private void calculateClusters(SOMMatrix clusters, FloatMatrix u_matrix) throws AlgorithmException {
        AlgorithmEvent event = new AlgorithmEvent(this, AlgorithmEvent.SET_UNITS, number_of_genes, "Creation of the clusters...");
        fireValueChanged(event);
        event.setId(AlgorithmEvent.PROGRESS_VALUE);
        
        SOMMatrix distances = new SOMMatrix(dim_x, dim_y, 0);
        for (int y=0; y<dim_y; y++) {
            for (int x=0; x<dim_x; x++) {
                u_matrix.set(x, y, 0f);
            }
        }
        float winner_distance;
        float max_winner_distance = 0f;
        int[] winner_info = new int[WINNER_INFO_SIZE];
        int counter;
        for (int i=0; i<number_of_genes; i++) {
            if (stop) {
                throw new AbortException();
            }
            winner_distance = findWinnerEuclidean(winner_info, i);
            if (winner_info[INDEX_X]==-1 || winner_info[INDEX_Y]==-1) {
            } else {
                max_winner_distance = Math.max(winner_distance, max_winner_distance);
                
                if (winner_distance > u_matrix.get(winner_info[INDEX_X], winner_info[INDEX_Y])) {
                    u_matrix.set(winner_info[INDEX_X], winner_info[INDEX_Y], winner_distance);
                }
                counter = 0;
                for (int j=0; j < distances.getDimension(winner_info[INDEX_X], winner_info[INDEX_Y]); j++) {
                    if (winner_distance < distances.getValue(winner_info[INDEX_X], winner_info[INDEX_Y], j)) {
                        break;
                    }
                    counter++;
                }
                distances.insertValue(winner_info[INDEX_X], winner_info[INDEX_Y], counter, winner_distance);
                clusters.insertValue(winner_info[INDEX_X], winner_info[INDEX_Y], counter, i);
            }
            event.setIntValue(i);
            fireValueChanged(event);
        }
        for (int y = 0; y < dim_y; y++) {
            for (int x = 0; x < dim_x; x++) {
                u_matrix.set(x, y, u_matrix.get(x, y)/max_winner_distance);
            }
        }
    }
    
    private void fillMean(float[] means, int[] cluster) {
        float currentMean;
        int n = cluster.length;
        validN = 0;
        float value;
        for (int i=0; i<number_of_samples; i++) {
            currentMean = 0f;
            validN = 0;
            for (int j=0; j<n; j++) {
                value = expMatrix.get(cluster[j], i);
                if (!Float.isNaN(value)) {
                    currentMean += value;
                    validN++;
                }
            }
              means[i] = currentMean/(float)validN;               
        }
    }
    
    private void fillVariance(float[] variances, float[] means, int[] cluster) {
        for (int i=0; i<number_of_samples; i++) {
            variances[i] = getSampleVariance(cluster, i, means[i]);
        }        
    }
    
    private float getSampleNormalizedSum(int[] cluster, int column, float mean) {
        float sum = 0f;
        float value;
        validN = 0;
        for (int i=0; i<cluster.length; i++) {
            value = expMatrix.get(cluster[i], column);
            if (!Float.isNaN(value)) {
                sum += Math.pow(value-mean, 2);
                validN++;
            }
        }
        return sum;
    }
    
    private float getSampleVariance(int[] cluster, int column, float mean) {
        if(validN > 1)
        return(float)Math.sqrt(getSampleNormalizedSum(cluster, column, mean)/(float)(validN-1));
        else
            return 0f;
    }
    
    /*private void print3DArray(String title, float[][][] array) {
        for (int i=0; i<dim_x; i++) {
            for (int j=0; j<dim_y; j++) {
                for (int k=0; k<array[i][j].length; k++) {
                    System.out.println(title+" array["+i+"]["+j+"]["+k+"]="+array[i][j][k]);
                }
            }
        }
    }*/
}
