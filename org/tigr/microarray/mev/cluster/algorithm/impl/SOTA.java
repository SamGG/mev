/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: SOTA.java,v $
 * $Revision: 1.1.1.1 $
 * $Date: 2003-08-21 21:04:25 $
 * $Author: braisted $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.algorithm.impl;

import java.io.*;
import java.util.*;
import java.awt.Dimension;

import org.tigr.util.ConfMap;
import org.tigr.util.FloatMatrix;
import org.tigr.microarray.mev.cluster.Node;
import org.tigr.microarray.mev.cluster.Cluster;
import org.tigr.microarray.mev.cluster.NodeList;
import org.tigr.microarray.mev.cluster.NodeValue;
import org.tigr.microarray.mev.cluster.NodeValueList;
import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmData;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmParameters;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmEvent;
import org.tigr.microarray.mev.cluster.algorithm.AbortException;
import org.tigr.microarray.mev.cluster.algorithm.AbstractAlgorithm;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmException;
import org.tigr.microarray.mev.cluster.algorithm.impl.ExperimentUtil;


public class SOTA extends AbstractAlgorithm{
    
    private AlgorithmData inData;
    private FloatMatrix dataMatrix;
    
    private int numberOfGenes;
    private int numberOfSamples;
    public int cycleNum;
    private int numberOfClusters;
    private int utilCounter;
    private float initDivSum;
    
    private SOTACell root;
    private SOTACell head;              //tree has treaded leaf nodes, head is head of this list
    private SOTACell mostDiverseCell;
    private SOTACell mostVariableCell;
    public SOTACell [] myNucleus;      //assosiates an index (gene) to a particular cell
    private double treeDiversity;      //sum of all gene to centroid distances
    private Vector cycleDiversity;     //maintain a record of tree diversity
    Cluster clusters; //result clusters data structure
    
    //Sota parameters
    boolean sotaGenes = true;
    int maxNumEpochs; //max epochs per cycle
    int maxNumCycles;  //max number of training cycles,  equals a number of clusters
    float epochCriteria; //error improvement threshold to continue a cycle
    float endCriteria;  //final hard tree diversity
    float migW;         //migration weights for (w)Winning cell, (p)parental cell, (s)sister cell
    float migP;
    float migS;
    int neighborhoodLevel; //number of leves to ascendt to select assignment subtree
    boolean useClusterVariance;  //use max gene to gene distance as cell division criteria
    float pValue;
    boolean runToMaxCycles;      //unrestricted growth
    boolean stop = false;
    int function;           //distance functin
    float factor;           //factor, based on distance and scaling for a particular algorithm, normally 1.0
    float myFactor;         //keeps track of factor to apply to distance
    boolean absolute;       //absolute value of distance?
    boolean calcClusterHCL;
    boolean calcFullTreeHCL;
    boolean calculate_genes;
    boolean calculate_experiments;
    int method;             //HCL linkage method
    
    
    
    /**
     * Constructs a new SOTA object
     */
    public SOTA(){  }
    
    /**
     * This method should interrupt the calculation.
     */
    public void abort() {
        stop = true;
    }
    
    
    /**
     * Performs SOTA tree construction given parameters provided in <code>AlgorithmData</code>.
     * Results are returned in AlgorthmData
     */
    public AlgorithmData execute(AlgorithmData data) throws AlgorithmException{
        
        //Get parameters
        AlgorithmParameters params = data.getParams();
        sotaGenes = params.getBoolean("sota-cluster-genes", true);
        maxNumEpochs = params.getInt("max-epochs-per-cycle", 1000);
        maxNumCycles = params.getInt("max-number-of-cycles", 10);
        epochCriteria = params.getFloat("epoch-improvement-cutoff");
        endCriteria = params.getFloat("end-training-diversity");
        runToMaxCycles = params.getBoolean("run-to-max-cycles");
        useClusterVariance = params.getBoolean("use-cluster-variance", false);
        function = params.getInt("distance-function", EUCLIDEAN);
        absolute = params.getBoolean("distance-absolute", true);
        calcClusterHCL = params.getBoolean("calcClusterHCL",false);
        calculate_genes = params.getBoolean("calculate-genes",false);
        calculate_experiments = params.getBoolean("calculate-experiments",false);
        calcFullTreeHCL = params.getBoolean("calcFullTreeHCL", false);
        method = params.getInt("method-linkage", 0);
        pValue = params.getFloat("pValue", (float)0.05);
        migW = params.getFloat("mig_w", (float)0.01);
        migP = params.getFloat("mig_p", (float)0.005);
        migS = params.getFloat("mig_s", (float)0.001);
        neighborhoodLevel = params.getInt("neighborhood-level", 5);
        
        inData = data;  //keep a handle on AlgorithmData for return
        
        //Set factor based on function
        if ((function==PEARSON)           ||
        (function==PEARSONUNCENTERED) ||
        (function==PEARSONSQARED)     ||
        (function==COSINE)            ||
        (function==COVARIANCE)        ||
        (function==DOTPRODUCT)        ||
        (function==SPEARMANRANK)      ||
        (function==KENDALLSTAU)) {
            myFactor = -1.0f;
        } else {
            myFactor = 1.0f;
        }
        
        factor = (float)1.0; //scaling factor sent to getDistance methods
        inData.addParam("factor", String.valueOf(myFactor));  //return factor
        endCriteria *= myFactor;                      //alter polarity fo endCriteria based on metric
        treeDiversity = Float.POSITIVE_INFINITY;
        dataMatrix = data.getMatrix("experiment"); //point dataMatrix at supplied matrix
        numberOfGenes = dataMatrix.getRowDimension();
        numberOfSamples = dataMatrix.getColumnDimension();
        myNucleus = new SOTACell[numberOfGenes];    //will be shortcut from gene index to a cell
        cycleDiversity = new Vector();
        
        //reset max number of cycles if limited by number of genes
        if(maxNumCycles >= numberOfGenes)
            maxNumCycles = numberOfGenes - 1;
        
        //if using variablility, resample data, select cutoff based on p value supplied
        if(useClusterVariance){
            endCriteria = resampleAndGetNewCutoff(dataMatrix, pValue);
        }
        
        //initialize first cell and two children
        root = new SOTACell(numberOfSamples, dataMatrix);
        root.right = new SOTACell(numberOfSamples, dataMatrix);
        root.left = new SOTACell(numberOfSamples, dataMatrix);
        numberOfClusters = 2;
        root.left.parent = root;
        root.right.parent = root;
        head = root.left;
        root.left.succ = root.right;
        root.right.pred = root.left;
        
        int [] numberOfValidGenesInSample = new int[numberOfSamples];
        //set to zero
        for(int i = 0; i < numberOfSamples ; i++)
            numberOfValidGenesInSample[i] = 0;
        
        //Inialize centroid root centroid to zeros
        for(int i = 0; i < numberOfSamples; i++){
            root.centroidGene.set(0,i,0);
        }
        
        for(int i = 0; i < numberOfGenes ; i++){
            root.members.add(new Integer(i));      //add all gene indices to root
            myNucleus[i] = root;                   //set all gene nuclei to point to root
            for(int j = 0; j < numberOfSamples; j++){
                if(  !(Float.isNaN(dataMatrix.get(i,j)))){
                    numberOfValidGenesInSample[j]++;      //count number of genes with valid data in each sample
                    
                    root.centroidGene.set(0,j,  root.centroidGene.get(0,j) + dataMatrix.get(i,j));  //calcualtes sum
                }
            }
            
        }
        
        mostDiverseCell = root;
        mostVariableCell = root;
        
        for(int j = 0; j < numberOfSamples; j++){
            root.centroidGene.set(0, j,  root.centroidGene.get(0,j)/numberOfValidGenesInSample[j]); //get a mean root centroid
            root.left.centroidGene.set(0,j,  root.centroidGene.get(0,j));       //assign to children
            root.right.centroidGene.set(0,j,  root.centroidGene.get(0,j));
        }
        
        //put first value into diversity vector
        initDivSum = getNodeDiversitySum(root);
        cycleDiversity.add( new Float( initDivSum ));
        root.cellDiversity = initDivSum/numberOfGenes;
        if(useClusterVariance)
            root.cellVariance = getNodeVariance(root);
        
        if(runToMaxCycles)
            growSOTUnrestricted();  //make tree w/o regard to diversity
        else
            growSOT();  // Construct tree
        
        //If performing HCL on samples using all genes
        if(calcFullTreeHCL){
            calcFullTreeHCL();
        }
        
        //Code for HCL clustering
        if(calcClusterHCL){
            calculateClusterHCL(); //calculate HCL trees for SOTA clusters
        }
        
        return inData;  //inData has results incorporated
    }
    
    /**
     * Deterimins variablity cutoff given a p value
     */
    private float resampleAndGetNewCutoff(FloatMatrix origMatrix, float p){
        FloatMatrix randMatrix = randomizeMatrix(origMatrix);
        
        int rows = origMatrix.getRowDimension();
        int cols = origMatrix.getColumnDimension();
        int NUM_BINS = 500;
        
        int numSamplePoints = 0;
        int cumCutoff;
        
        float [][] distances = new float[rows][rows];
        
        for(int i = 0; i < rows - 1; i++){
            for(int j = 0; j < i; j++){
                distances[i][j] = ExperimentUtil.geneDistance(randMatrix, null, i, j, function, factor, absolute);
            }
        }
        
        //now have all gene to gene distances
        float [] minAndMax = getMinAndMax(distances, rows);
        float min = minAndMax[0];
        float max = minAndMax[1];
        
        float [] cumDist = new float[NUM_BINS];
        
        for(int i = 0; i < NUM_BINS; i++){
            cumDist[i] = 0;
        }
        
        for(int i = 0; i < rows - 1; i++){
            for(int j = 0; j < i; j++){
                cumDist[ (int)( (float)(NUM_BINS-1) *  (distances[i][j]-min)/(max - min)) ]++;
                numSamplePoints++;
            }
        }
        
        for(int i  = 0; i < NUM_BINS; i++){
            cumDist[i] /= (float)numSamplePoints;
        }
        
        //now find the bin that has
        float cumCount = 0;
        int bin = 0;
        //cumCutoff = (int)(numSamplePoints * p);
        
        while(cumCount < p){
            cumCount += cumDist[bin];
            bin++;
        }
        
        return  (((float)bin - (float)0.5)/(float)NUM_BINS ) * (max - min)  + min;
    }
    
    /**
     *  Utility to get min and max from a triangular matrix
     */
    private float [] getMinAndMax(float [][] A, int dim){
        
        float [] minAndMax = new float[2];
        float currDist;
        float maxDist = Float.NEGATIVE_INFINITY;
        float minDist = Float.POSITIVE_INFINITY;
        
        for(int i = 0; i < dim - 1; i++){
            for(int j = 0; j < i; j++){
                currDist = A[i][j];
                if(currDist > maxDist)
                    maxDist = currDist;
                if(currDist < minDist)
                    minDist = currDist;
            }
        }
        minAndMax[0] = minDist;
        minAndMax[1] = maxDist;
        return minAndMax;
        
    }
    
    
    /**
     *  Returns a randomized triangular matrix with genes randomized wrt vector element order
     */
    private FloatMatrix randomizeMatrix(FloatMatrix origMatrix){
        FloatMatrix shuffleMatrix = origMatrix.copy();
        int rows = shuffleMatrix.getRowDimension();
        int cols = shuffleMatrix.getColumnDimension();
        float temp;
        int swapIndex;
        int sampleRange = cols - 1;
        
        for(int i = 0; i < rows; i++){
            for(int j = cols-1; j > 0; j--){
                
                swapIndex = (int)(Math.random()*(j));
                temp = shuffleMatrix.get(i,j);
                shuffleMatrix.set(i,j, shuffleMatrix.get(i, swapIndex));
                shuffleMatrix.set(i,swapIndex, temp);
                sampleRange--;
            }
        }
        return shuffleMatrix;
    }
    
    
    /**
     *  Calculates HCL tree over all genes
     */
    private void calcFullTreeHCL() throws AlgorithmException{
        
        AlgorithmEvent event = null;
        event = new AlgorithmEvent(this, AlgorithmEvent.SET_UNITS, 1, "Calculate Hierarchical Tree, Clustering Experiments");
        fireValueChanged(event);
        event.setIntValue(0);
        event.setId(AlgorithmEvent.PROGRESS_VALUE);
        fireValueChanged(event);
        
        Cluster result_cluster = new Cluster();
        NodeList nodeList = result_cluster.getNodeList();
        
        int[] probesIndexes = new int[numberOfGenes];
        for(int i = 0; i < numberOfGenes; i++){
            probesIndexes[i]= i;
        }
        
        int[] featuresIndexes = new int[numberOfSamples];
        for(int i = 0; i < numberOfSamples; i++){
            featuresIndexes[i] = i;
        }
        
        if (stop) {
            throw new AbortException();
        }
        
        Node node = new Node(featuresIndexes);
        nodeList.addNode(node);
        
        node.setValues(calculateHierarchicalTree(probesIndexes, method, false, true));
        event.setIntValue(1);
        fireValueChanged(event);
        
        if(result_cluster != null)
            inData.addCluster("full-tree-sample-HCL", result_cluster);
        
    }
    
    /**
     *  calculates HCL on SOTA cells (clusters)
     */
    private void calculateClusterHCL() throws AlgorithmException{
        
        AlgorithmEvent event = null;
        
        event = new AlgorithmEvent(this, AlgorithmEvent.SET_UNITS, numberOfClusters, "Calculate Hierarchical Trees SOTA Cluster Members");
        fireValueChanged(event);
        event.setIntValue(0);
        event.setId(AlgorithmEvent.PROGRESS_VALUE);
        fireValueChanged(event);
        
        Cluster result_cluster = new Cluster();
        NodeList nodeList = result_cluster.getNodeList();
        
        //clusters are formed after growSOT
        
        NodeList resultList = clusters.getNodeList();
        Node currNode;
        
        int[] probeIndexes;
        
        for (int i=0; i<numberOfClusters; i++) {
            if (stop) {
                throw new AbortException();
            }
            currNode = resultList.getNode(i);
            probeIndexes = currNode.getProbesIndexes();
            
            Node node = new Node(probeIndexes);
            nodeList.addNode(node);
            
            node.setValues(calculateHierarchicalTree(probeIndexes, method, calculate_genes, calculate_experiments));
            event.setIntValue(i+1);
            fireValueChanged(event);
        }
        
        if(result_cluster != null)
            inData.addCluster("hcl-result-clusters", result_cluster);
    }
    
    
    
    private NodeValueList calculateHierarchicalTree(int[] features, int method, boolean genes, boolean experiments) throws AlgorithmException {
        NodeValueList nodeList = new NodeValueList();
        AlgorithmData data = new AlgorithmData();
        FloatMatrix experiment;
        if(sotaGenes)
            experiment = getSubExperiment(this.dataMatrix, features);
        else
            experiment = getSubExperimentReducedCols(this.dataMatrix, features);
                
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
    
    private void addNodeValues(NodeValueList target_list, AlgorithmData source_result) {
        target_list.addNodeValue(new NodeValue("child-1-array", source_result.getIntArray("child-1-array")));
        target_list.addNodeValue(new NodeValue("child-2-array", source_result.getIntArray("child-2-array")));
        target_list.addNodeValue(new NodeValue("node-order", source_result.getIntArray("node-order")));
        target_list.addNodeValue(new NodeValue("height", source_result.getMatrix("height").getRowPackedCopy()));
    }
    
    /**
     * Returns the Tree root <code>SOTACell</code>
     */
    public SOTACell getRoot(){
        return root;
    }
    
    /**
     *  returns the number of cells (leaf nodes) in a subtree (given root <code>SOTACell</code>)
     */
    public int getPopulation(SOTACell subRoot){
        utilCounter = 0;
        getPop(subRoot);
        return utilCounter;
    }
    
    //gets number of clusters below a node
    private void getPop(SOTACell curr){
        
        if(curr != null){
            if(curr.left == null && curr.right == null)
                utilCounter++;
            
            getPop(curr.left);
            getPop(curr.right);
        }
    }
    
    /**
     *  grows SOT
     */
    private void growSOT(){
        
        AlgorithmEvent event = new AlgorithmEvent(this, AlgorithmEvent.SET_UNITS, maxNumCycles);
        fireValueChanged(event);
        
        
        AlgorithmEvent event1 = new AlgorithmEvent(this, AlgorithmEvent.SET_UNITS, maxNumCycles, "Growing Tree");
        fireValueChanged(event1);
        event1.setIntValue(0);
        event1.setId(AlgorithmEvent.PROGRESS_VALUE);
        fireValueChanged(event1);
        
        
        float currDivFraction;
        boolean stopGrowing = false;
        float lowerLim;
        
        if(!useClusterVariance && mostDiverseCell.cellDiversity < endCriteria)
            stopGrowing = true;
        else if( useClusterVariance &&  mostVariableCell.cellVariance < endCriteria)
            stopGrowing = true;
        
        for( cycleNum = 0; cycleNum < maxNumCycles  && !stopGrowing ; cycleNum++){
            
            runCycle(maxNumEpochs, epochCriteria);
            
            //training has optimized the sub-system centroids, trained node's profiles have been distributed
            //among potentially any cluster (closest chosen)
            
            //now with all profiles assigned to cells, get the cell's and tree's diversity (Resource)
            setDiversities();
            
            cycleDiversity.add(new Float(treeDiversity));
            
            if(!useClusterVariance){
                if(mostDiverseCell.cellDiversity > endCriteria && cycleNum < maxNumCycles-1) //don't split if last cycle in maxNumCycles
                    divideCell(mostDiverseCell);
                else
                    stopGrowing = true;
            }
            
            else{  //using variabliity
                if(mostVariableCell.cellVariance > endCriteria && cycleNum < maxNumCycles-1) //don't split if last cycle in maxNumCycles
                    divideCell(mostVariableCell);
                else
                    stopGrowing = true;
            }
            
            //advance monitor
            event1.setId(AlgorithmEvent.PROGRESS_VALUE);
            event1.setIntValue(cycleNum+1);
            fireValueChanged(event1);
            
            
            //calculate and set monitor values
            //event.setId(AlgorithmEvent.MONITOR_VALUE);
            /*  DISABLED OPTIONAL DIVERSITY MONITOR
            if(myFactor == 1)
                currDivFraction = (float)(treeDiversity/initDivSum);
            else{
                lowerLim = numberOfGenes * myFactor;
                currDivFraction = (float)((treeDiversity + Math.abs(lowerLim))/(initDivSum+Math.abs(lowerLim)));
            }
            if(cycleNum < 245){    //monitor values limit is 245
                event.setIntValue((int)(100*currDivFraction));
                fireValueChanged(event);
            }
             */
        }
        trainLeaves(maxNumEpochs, epochCriteria);  //reached stopping criteria, now just need to train leaves so centroids migrate to center
        getResults();  //add results to AlgorithmData (inData)
        return;
    }
    
    
    
    /** runs sota until maxCycle - 1 */
    private void growSOTUnrestricted(){
        
        boolean stopGrowing = false;
        
        for( cycleNum = 0; cycleNum < maxNumCycles  && !stopGrowing ; cycleNum++){
            
            runCycle(maxNumEpochs, epochCriteria);
            
            //training has optimized the sub-system centroids, trained node's profiles have been distributed
            //among potentially any cluster (closest chosen)
            
            //now with all profiles assigned to cells, get the cell's and tree's diversity (Resource)
            setDiversities();
            
            cycleDiversity.add(new Float(treeDiversity));
            
            if(cycleNum < maxNumCycles-1) //don't split if last cycle in maxNumCycles
                divideCell(mostDiverseCell);
            else
                stopGrowing = true;
        }
        //reached stopping criteria, now just need to train leaves so centroids migrate to center
        trainLeaves(maxNumEpochs, epochCriteria);
        //add results to AlgorithmData (inData)
        getResults();
        
        return;
        
    }
    
    /**
     * Returns the nubmer of samples in the tree
     */
    public int getNumberOfSamples(){ return numberOfSamples; }
    
    /**
     *  Returns the nubmer of leaf nodes (aka cells or clusters)
     */
    public int getNumberOfClusters(){
        return numberOfClusters;
    }
    
    //Utility function to traverse the cells to get populations
    private int [] getGenesPerCluster(){
        
        int [] genesPerCluster;
        SOTACell curr = head;
        int numCells = 0;
        int i = 0 ;
        
        while(curr != null){
            numCells++;
            curr = curr.succ;
        }
        
        genesPerCluster = new int[numCells];
        curr = head;
        
        while(curr != null){
            genesPerCluster[i] = curr.members.size();
            i++;
            curr = curr.succ;
        }
        
        return genesPerCluster;
    }
    
    
    /**
     * Returns the distance function used to construct the Tree
     */
    public int getFunction(){
        return function;
    }
    
    /**
     * Returns whether or not distance values were absolute values
     */
    public boolean getAbsolute(){
        return absolute;
    }
    
    /**
     * Returns the distance metric polarity factor
     */
    public float getFactor(){
        return myFactor;
    }
    
    
    /**
     *  Performs a singe cycle on appropriate cell
     */
    private void runCycle(int maxNumEpochs, double epochCriteria){
        
        int negCounter = 0;
        
        double lastError = Double.POSITIVE_INFINITY;
        double currError = Double.POSITIVE_INFINITY;
        Dimension distribCount;
        
        double initialDiversity = treeDiversity;
        double diversityImprovement = Float.POSITIVE_INFINITY;
        SOTACell trainingNode;
        
        if(!useClusterVariance)
            trainingNode = mostDiverseCell;
        else
            trainingNode = mostVariableCell;
        
        boolean stopEpoch = false;
        
        for(int epochNum = 0; epochNum < maxNumEpochs && !stopEpoch; epochNum++){
            
            initialDiversity = treeDiversity;
            
            distribCount = runNodeEpoch(trainingNode);  //Only distribute left and right, keep counter of left and right.
            //return a dimension(left, right)
            //if left or right == 0 then dont stop epoch
            //Don't redistribute, only alter centroids
            
            lastError = currError;
            //get error associated with the current input set
            currError = getInputError(trainingNode); //This is sum of errors of inputs to the new child cells
            
            if(lastError != 0)
                diversityImprovement = Math.abs( (currError - lastError)/lastError );
            else
                diversityImprovement = 0;
            
            if(diversityImprovement < epochCriteria  && diversityImprovement >= 0
            && distribCount.getHeight() != 0 && distribCount.getWidth() != 0)
                stopEpoch = true;
        }
        //genes in the training node are assigned to closest cell, and inserted
        assignGenesToCells(trainingNode);
    }
    
    
    /**
     *  Following training, assignment of genes to cells
     */
    private void assignGenesToCells(SOTACell trainingNode){
        int geneIndex;
        int n = trainingNode.members.size();
        SOTACell myCell = null;
        
        for(int i = 0; i<n ; i++){
            geneIndex = ((Integer)(trainingNode.members.elementAt(i))).intValue();
            
            myCell = findMyCellInSubTree(trainingNode, geneIndex, neighborhoodLevel);	//finds and adds index to correct cell
        }
    }
    
    
    //Note that leaves are threaded from left to right.
    //This means that if displayed top to bottom, centroids would be reversed
    //Therefore, accumulate in reverse order into AlgorithmData
    private void getResults(){
        
        SOTACell curr = head;
        int numCells = 0;
        FloatMatrix centroidFM = new FloatMatrix(numberOfClusters, numberOfSamples);
        FloatMatrix varianceFM = new FloatMatrix(numberOfClusters, numberOfSamples);
        
        int [] clusterSize = new int[numberOfClusters];
        FloatMatrix clusterDiversity = new FloatMatrix(numberOfClusters,1);
        int numDiv = cycleDiversity.size();
        FloatMatrix cycleDivFM = new FloatMatrix(numDiv, 1);
        
        
        int [] clusterOrder = new int[numberOfClusters];
        
        clusters = new Cluster();
        NodeList nodeList = clusters.getNodeList();
        Node newNode;
        int [] clusterMembership;
        int clusterPop;
        
        //move to tail
        while(curr.succ != null)
            curr = curr.succ;
        
        //now curr is at the tail
        while(numCells <= numberOfClusters && curr != null){
            
            for(int i = 0; i < numberOfSamples; i++){
                centroidFM.set( numCells, i, curr.centroidGene.get(0,i));
                varianceFM.set( numCells, i, curr.getColumnVar(i));
            }
            clusterPop = curr.members.size();
            clusterSize[numCells] = clusterPop;
            clusterDiversity.set(numCells, 0, (float)curr.cellDiversity*(float)myFactor); //alter poloarity by myFactor based on metric
            clusterOrder[numCells] = numCells;
            
            //accumulate cluster probe indicies
            clusterMembership = new int[clusterPop];
            for(int i = 0; i < clusterPop; i++){
                clusterMembership[i] = ((Integer)(curr.members.elementAt(i))).intValue();
            }
            
            newNode = new Node();
            newNode.setProbesIndexes(clusterMembership);
            nodeList.addNode(newNode);
            
            numCells++;
            curr = curr.pred;
        }
        
        //now accumlate cycle divresity information
        if(myFactor == 1){
            float initDiv = ((Float)(cycleDiversity.elementAt(0))).floatValue();
            for(int i = 0; i <  numDiv; i++){
                cycleDivFM.set(i,0, (((Float)(cycleDiversity.elementAt(i))).floatValue())/initDiv );
            }
        }
        else{
            float lowerLim = numberOfGenes * myFactor;
            float initDiv = ((Float)(cycleDiversity.elementAt(0))).floatValue() + Math.abs(lowerLim) ;
            for(int i = 0; i <  numDiv; i++){
                cycleDivFM.set(i,0, (((Float)(cycleDiversity.elementAt(i))).floatValue()+Math.abs(lowerLim))/initDiv );
            }
            
        }
        // put all important information into AlgorithmData
        inData.addParam("cycles", String.valueOf(numberOfClusters));
        inData.addCluster("cluster", clusters);
        inData.addMatrix("centroid-matrix", centroidFM);
        inData.addMatrix("cluster-variances", varianceFM);
        inData.addMatrix("cluster-diversity", clusterDiversity);
        inData.addMatrix("cycle-diversity", cycleDivFM);
        inData.addIntArray("cluster-population", clusterSize);
        
        //Additions to AlgorithmData to allow drawing arrays
        float [] nodeHeight = new float[numberOfClusters*2];
        int [] nodePopulation = new int[numberOfClusters*2];
        int [] leftChild = new int[nodeHeight.length*2];
        int [] rightChild = new int[nodeHeight.length*2];
        
        initializeReturnValues(nodeHeight, nodePopulation, leftChild, rightChild);
        utilCounter = 0;
        loadReturnValues(root, 0, nodeHeight, nodePopulation, leftChild, rightChild);
        inData.addMatrix("node-heights", new FloatMatrix(nodeHeight, nodeHeight.length));
        inData.addIntArray("left-child", leftChild);
        inData.addIntArray("right-child", rightChild);
        inData.addIntArray("node-population", nodePopulation);
        
        
        if(useClusterVariance)
            inData.addParam("computed-var-cutoff", String.valueOf(endCriteria));
        return;
    }
    
    
    private void initializeReturnValues(float [] height, int [] pop, int [] left, int [] right){
        int i = 0;
        for(; i < height.length ; i++){
            height[i] = -1.0f;
            pop[i] = left[i] = right[i] = -1;
        }
        for(int j = i; j < left.length ; j++){
            left[j] = right[j] = -1;
        }
    }
    
    private void loadReturnValues(SOTACell subRoot, int index, float [] h, int [] pop, int [] left, int [] right){
        pop[index] = subRoot.members.size();
        if(subRoot == root)
            h[index] = 0;
        else
            h[index] = ExperimentUtil.geneDistance(subRoot.centroidGene, subRoot.parent.centroidGene, 0, 0, function, factor, absolute);
        
        if(subRoot.left != null){
            left[index] = utilCounter + 1;
            utilCounter++;
            loadReturnValues(subRoot.left, utilCounter, h, pop, left, right);
            right[index] = utilCounter + 1;
            utilCounter++;
            loadReturnValues(subRoot.right, utilCounter, h, pop, left, right);
        }
    }
    
    //gets sum of min distance of genes in parentNode to child centroids
    private double getInputError(SOTACell parentNode){
        int n = parentNode.members.size();
        double cumErr = 0;
        double d1;
        double d2;
        int geneIndex;
        
        for(int i = 0; i < n; i++){
            geneIndex = ((Integer)(parentNode.members.elementAt(i))).intValue();
            
            d1 = ExperimentUtil.geneDistance(dataMatrix, parentNode.left.centroidGene, geneIndex, 0, function, factor, absolute);
            d2 = ExperimentUtil.geneDistance(dataMatrix, parentNode.right.centroidGene, geneIndex, 0, function, factor, absolute);
            
            if(d1<=d2)
                cumErr += d1;
            else
                cumErr += d2;
        }
        return cumErr/n;
    }
    
    //returns the distribution of trainingNode memeber genes among left and right children
    private Dimension runNodeEpoch(SOTACell trainingNode){
        
        SOTACell myCell = null;
        SOTACell sisterCell = null;
        int rightCnt = 0;
        int leftCnt = 0;
        int memberGene = 0;
        
        //for all genes in the training node, find closest child, migrate child
        for(int geneNum = 0; geneNum < trainingNode.members.size() ; geneNum++){
            
            memberGene = ((Integer)trainingNode.members.elementAt(geneNum)).intValue();
            
            myCell = findMyDaughterCell(trainingNode, memberGene); //only look among children
            //dont add to membership
            
            //later make sure that left and right membership set is not null
            if(myCell == trainingNode.left)
                leftCnt++;
            else
                rightCnt++;
            
            myCell.migrateCentroid(memberGene, migW);
            
            sisterCell = findSister(myCell);
            
            //if sister has no offspring then migrate parent and sister
            if(sisterCell.left == null && sisterCell.right == null){
                myCell.parent.migrateCentroid(memberGene, migP);
                sisterCell.migrateCentroid(memberGene, migS);
            }
        }
        return new Dimension(leftCnt, rightCnt);
    }
    
    
    /**
     *  Finds closest daughter cell
     */
    private SOTACell findMyDaughterCell(SOTACell parentNode, int geneIndex){
        
        float dist1 = ExperimentUtil.geneDistance(dataMatrix, parentNode.left.centroidGene, geneIndex, 0, function, factor, absolute);
        float dist2 = ExperimentUtil.geneDistance(dataMatrix, parentNode.right.centroidGene, geneIndex, 0, function, factor, absolute);
        
        if(dist1 <= dist2)
            return parentNode.left;
        else
            return parentNode.right;
    }
    
    
    //sets cell diversities, and variances (if required)
    private void setDiversities(){
        SOTACell curr = head;
        double cellSum = 0;
        double cellVar = 0;
        double treeSum = 0;
        double maxCellDiv = -1;
        double maxCellVar = -1;
        int numberOfCells  = 0;
        double currDist = 0;
        
        mostDiverseCell = head;
        mostVariableCell = head;
        
        while(curr != null){
            
            numberOfCells++;
            cellSum = 0;
            
            //for all members of the node get distance to set cell resource (diversity)
            for(int i = 0; i < curr.members.size() ; i++){
                cellSum += ExperimentUtil.geneDistance(dataMatrix, curr.centroidGene, ((Integer)(curr.members.elementAt(i))).intValue(),
                0, function, factor, absolute);
            }
            
            curr.cellDiversity = (cellSum/curr.members.size());
            
            if(curr.cellDiversity > maxCellDiv && curr.members.size() > 1){
                maxCellDiv = curr.cellDiversity;
                mostDiverseCell = curr;
            }
            
            treeSum += cellSum;
            
            if(useClusterVariance){ //using cell variance, need to find mostVariable cell
                cellVar = 0;
                currDist = 0;
                
                //get cell varience
                //if new members have been added
                if(curr.changedMembership){
                    //use max gene to gene distance
                    for(int i = 0; i < curr.members.size(); i++){
                        for(int j = 0; j < curr.members.size(); j++){
                            
                            currDist = ExperimentUtil.geneDistance(dataMatrix, null, ((Integer)(curr.members.elementAt(i))).intValue(),
                            ((Integer)(curr.members.elementAt(j))).intValue(), function, factor, absolute);
                            
                            //get max dist. to be cellVar
                            if(currDist > cellVar){
                                cellVar = currDist;
                            }
                        }
                    }
                    curr.cellVariance = cellVar;
                }
                else //no change to membership so we dont hve to recalculate variance
                    cellVar = curr.cellVariance;
                
                if(cellVar > maxCellVar && curr.members.size() > 1){
                    maxCellVar = cellVar;
                    mostVariableCell = curr;
                }
            }
            curr.changedMembership = false; //variance already set for current population
            curr = curr.succ;
        }
        treeDiversity = treeSum;
    }
    
    
    private float getNodeDiversitySum(SOTACell curr){
        
        float div = 0;
        int n = curr.members.size();
        int geneIndex;
        float currDist;
        float sum = 0;
        
        for(int i = 0; i < n; i++){
            geneIndex = ( (Integer)(curr.members.elementAt(i)) ).intValue();
            currDist = ExperimentUtil.geneDistance(dataMatrix, curr.centroidGene, geneIndex, 0, function, factor, absolute);
            if(!Float.isNaN(currDist)){
                sum +=  currDist;
            }
        }
        return sum;
    }
    
    
    
    private float getNodeDiversity(SOTACell curr){
        float div = 0;
        int n = curr.members.size();
        int geneIndex;
        float currDist;
        float sum = 0;
        int numGoodRatios  = 0;
        
        for(int i = 0; i < n; i++){
            geneIndex = ( (Integer)(curr.members.elementAt(i)) ).intValue();
            currDist = ExperimentUtil.geneDistance(dataMatrix, curr.centroidGene, geneIndex, 0, function, factor, absolute);
            if(!Float.isNaN(currDist)){
                sum +=  currDist;
                numGoodRatios++;
            }
        }
        return (float)(sum/numGoodRatios);
    }
    
    
    
    private float getNodeVariance(SOTACell curr){
        float div = 0;
        int n = curr.members.size();
        int geneIndex1, geneIndex2;
        float currDist;
        float maxDist = 0;
        int numGoodRatios  = 0;
        
        for(int i = 0; i < n; i++){
            for(int j = i+1; j < n; j++){
                
                geneIndex1 = ( (Integer)(curr.members.elementAt(i)) ).intValue();
                geneIndex2 = ( (Integer)(curr.members.elementAt(j)) ).intValue();
                
                currDist = ExperimentUtil.geneDistance(dataMatrix, null, geneIndex1, geneIndex2, function, factor, absolute);
                
                if(!Float.isNaN(currDist) && currDist > maxDist){
                    maxDist = currDist;
                }
            }
        }
        return maxDist;
    }
    
    
    //Final adjustment of cell centroids
    private void trainLeaves(int maxNumEpochs, double epochCriteria){
        
        double initialDiversity = treeDiversity;
        double diversityImprovement = Double.POSITIVE_INFINITY;
        SOTACell trainingNode = head;
        
        while(trainingNode != null){
            diversityImprovement = Float.POSITIVE_INFINITY;
            
            for(int epochNum = 0; epochNum < maxNumEpochs && diversityImprovement >  epochCriteria; epochNum++){
                
                initialDiversity = getNodeDiversity(trainingNode);
                runLeafEpoch(trainingNode);
                
                diversityImprovement = Math.abs((getNodeDiversity(trainingNode) - initialDiversity)/initialDiversity);
            }
            trainingNode = trainingNode.succ;
        }
        //calculate current cell diversities and tree diversity
        setDiversities();
    }
    
    
    
    private void runLeafEpoch(SOTACell trainingNode){
        
        SOTACell myCell = null;
        SOTACell sisterCell = null;
        
        int memberGene = 0;
        
        for(int geneNum = 0; geneNum < trainingNode.members.size() ; geneNum++){
            memberGene = ((Integer)trainingNode.members.elementAt(geneNum)).intValue();
            //leaf training is just migration of centroids
            (myNucleus[memberGene]).migrateCentroid(memberGene, migW);
        }
    }
    
    
    private SOTACell findMyCellInSubTree(SOTACell trainingCell, int geneNum, int level){
        
        SOTACell currCell = trainingCell;
        SOTACell myCell = trainingCell;
        int levelIndex = 0;
        
        while(currCell.parent != null && levelIndex < level){
            currCell = currCell.parent;
            levelIndex++;
        }
        //now currNode is at root, or 'level' number of nodes above the training node
        
        Vector cellList = new Vector();
        
        getCellsBelow(cellList, currCell);
        
        float minDist = Float.POSITIVE_INFINITY;
        float currDist;
        
        for(int i = 0; i < cellList.size() ; i++){
            currCell = (SOTACell)(cellList.elementAt(i));
            currDist = ExperimentUtil.geneDistance(dataMatrix, currCell.centroidGene, geneNum, 0, function, factor, absolute);
            if(currDist < minDist){
                minDist = currDist;
                myCell = currCell;
            }
        }
        
        if(myNucleus[geneNum] != myCell){
            
            myNucleus[geneNum] = myCell;
            myCell.addMember(geneNum);
        }
        return myCell;
    }
    
    
    private void getCellsBelow(Vector cellList, SOTACell subRoot){
        
        if(subRoot == null) return;
        
        if(subRoot.left == null && subRoot.right == null){
            cellList.add(subRoot);
        }
        
        else{
            getCellsBelow(cellList, subRoot.right);
            getCellsBelow(cellList, subRoot.left);
        }
    }
    
    
    
    
    private SOTACell findMyCell(int geneNum){
        SOTACell curr = head;
        
        SOTACell myClosestCell = head;
        double keyDist = Float.POSITIVE_INFINITY;
        double currDist = 0;
        
        
        while(curr != null){
            
            currDist = ExperimentUtil.geneDistance(dataMatrix, curr.centroidGene, geneNum, 0, function, factor, absolute);
            
            if(currDist <= keyDist){
                keyDist = currDist;
                myClosestCell = curr;
            }
            curr = curr.succ;
        }
        
        if(myNucleus[geneNum] != myClosestCell){
            myNucleus[geneNum] = myClosestCell;
            myClosestCell.addMember(geneNum);
        }
        
        return myClosestCell;
    }
    
    
    private SOTACell findSister(SOTACell curr){
        
        if(curr != null){
            if(curr.parent.left == curr)
                return curr.parent.right;
            else
                return curr.parent.left;
        }
        else
            return null;
    }
    
    
    
    private void divideCell(SOTACell cellToDivide){
        
        float [] parentCentroid;
        
        cellToDivide.left = new SOTACell(numberOfSamples, dataMatrix);
        cellToDivide.right = new SOTACell(numberOfSamples, dataMatrix);
        
        numberOfClusters++;
        
        cellToDivide.left.parent = cellToDivide;
        cellToDivide.right.parent = cellToDivide;
        
        cellToDivide.right.pred = cellToDivide.left;
        cellToDivide.left.succ = cellToDivide.right;
        
        if(cellToDivide.pred != null){
            cellToDivide.left.pred = cellToDivide.pred;
            cellToDivide.left.pred.succ = cellToDivide.left;
        }
        else
            cellToDivide.left.pred = null;
        
        if(cellToDivide.succ != null){
            cellToDivide.right.succ = cellToDivide.succ;
            cellToDivide.right.succ.pred = cellToDivide.right;
        }
        else
            cellToDivide.right.succ = null;
        
        if(cellToDivide == head)
            head = cellToDivide.left;
        
        cellToDivide.succ = null;
        cellToDivide.pred = null;
        
        for(int i = 0; i < numberOfSamples; i++){
            cellToDivide.left.centroidGene.set(0, i, cellToDivide.centroidGene.get(0, i));
            cellToDivide.right.centroidGene.set(0, i, cellToDivide.centroidGene.get(0,i));
        }
    }
    
    
    
    public double getMaxLeafToRootPath(){
        
        SOTACell curr = head;
        SOTACell traveler = curr;
        
        double cumDist;
        double maxDist = -1;
        
        while(curr != null){
            traveler = curr;
            cumDist = 0;
            
            while(traveler != null){
                
                if(traveler.parent != null)
                    cumDist += ExperimentUtil.geneDistance(traveler.parent.centroidGene, traveler.centroidGene, 0, 0, function, factor, absolute);
                
                traveler = traveler.parent;
            }
            
            if(cumDist < maxDist)
                maxDist = cumDist;
            
            curr = curr.succ;
        }
        
        return maxDist;
        
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
    
}





