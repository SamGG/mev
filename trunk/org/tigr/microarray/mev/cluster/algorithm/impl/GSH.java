/*
Copyright @ 1999-2004, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $Id: GSH.java,v 1.2 2004-05-21 13:02:32 braisted Exp $
 *
 * Created 11/26/2001
 *
 * Description:
 *
 * Copyright (C) 2002 TIGR
 *
 */
package org.tigr.microarray.mev.cluster.algorithm.impl;

import java.util.Random;
import java.util.ArrayList;
import java.io.*;
import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.util.*;
import java.lang.*;

import org.tigr.microarray.util.Adjustment;
import org.tigr.util.ConfMap;
import org.tigr.util.FloatMatrix;

import org.tigr.util.awt.ProgressDialog;

import org.tigr.microarray.mev.cluster.Node;
import org.tigr.microarray.mev.cluster.Cluster;
import org.tigr.microarray.mev.cluster.NodeList;
import org.tigr.microarray.mev.cluster.NodeValue;
import org.tigr.microarray.mev.cluster.NodeValueList;

import org.tigr.microarray.mev.cluster.algorithm.AbstractAlgorithm;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmData;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmParameters;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmException;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmEvent;
import org.tigr.microarray.mev.cluster.algorithm.AbortException;

import Jama.*;

public class GSH extends AbstractAlgorithm {
    
    private long StartTime;
    private long CalculationTime;
    
    private JLabel StatusLabel;
    private int CurrentCluster, CreatedCluster;
    
    private GSCluster clusters[];
    private GSCluster resultClusters[];
    private boolean gshGenes;
    private int m;
    private int n;
    private int k; // number of expected clusters;
    private int fakedMatrix;
    private int swapTime;
    private int matrixSize;
    private int geneAssaigned=0;
    //private GeneCluster[] GeneClusterPointers;
    
    private int validN;
    
    private Jama.Matrix dataMatrix;
    private Jama.Matrix workingMatrix;
    private Jama.SingularValueDecomposition SVD;
    private Jama.EigenvalueDecomposition EVD;
    private double[][] values;
    private Jama.Matrix eigenVector;
    private double[] prinCom;
    private double[] currentGene;
    private GSCluster unassigned;
    //private boolean drawTrees;
    
    
    private boolean stop = false;
    
    private int function;
    //private int DistanceFunction;
    private float factor;
    private boolean absolute;
    ProgressDialog PD;
    
    private int number_of_genes;
    private int number_of_samples;
    
    private FloatMatrix expMatrix;
    
    
    public AlgorithmData execute(AlgorithmData data) throws AlgorithmException {
        
        AlgorithmParameters map = data.getParams();
        
        function = map.getInt("distance-function", EUCLIDEAN);
        factor   = map.getFloat("distance-factor", 1.0f);
        absolute = map.getBoolean("distance-absolute", false);
        gshGenes = map.getBoolean("gsh-cluster-genes", true);
        
        int number_of_fakedMatrix = map.getInt("number-of-fakedMatrix", 20);
        fakedMatrix=number_of_fakedMatrix;
        int number_of_swap = map.getInt("number-of-swap", 5);
        swapTime=number_of_swap;
        int number_of_clusters = map.getInt("number-of-clusters", 5);
        k=number_of_clusters;
        
        boolean hierarchical_tree = map.getBoolean("hierarchical-tree", false);
        int method_linkage = map.getInt("method-linkage", 0);
        boolean calculate_genes = map.getBoolean("calculate-genes", false);
        boolean calculate_experiments = map.getBoolean("calculate-experiments", false);
        
        this.expMatrix = data.getMatrix("experiment");
        
        number_of_genes   = this.expMatrix.getRowDimension();
        n=number_of_genes;
        number_of_samples = this.expMatrix.getColumnDimension();
        m=number_of_samples;
        values = new double[n][m];
        prinCom = new double[m];
        
        JFrame dummyFrame = new JFrame();
        if(gshGenes)
            PD = new ProgressDialog(dummyFrame, "Gene Shaving -- Progress", false, 6);
        else
            PD = new ProgressDialog(dummyFrame, "Experiment Shaving -- Progress", false, 6);
        
        JPanel progressPanel = PD.getLabelPanel();
        JPanel superPanel = new JPanel();
        superPanel.setLayout(new BorderLayout());
        JButton abortButton = new JButton(" Cancel ");
        abortButton.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        abortButton.setFocusPainted(false);       
        
        abortButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                stop = true;
                PD.dismiss();
            }
        });
        
        superPanel.add(progressPanel, BorderLayout.CENTER);
        superPanel.add(abortButton, BorderLayout.SOUTH);
        PD.setMainPanel(superPanel);
        PD.setSize(450,200);
        
        calculate();
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
        result.addMatrix("clusters-means", means);
        result.addMatrix("clusters-variances", variances);
        //result.addParam("iterations", String.valueOf(getIterations()));
        //result.addParam("converged", String.valueOf(getConverged()));
        return result;
    }
    
    
    
    public void getMatrix(){
        int i,j;
        
        double[][] temp = new double[n][m];
        
        for(i=0;i<n;i++){
            for(j=0;j<m;j++){
                temp[i][j] = expMatrix.A[i][j];
            }
        }
        
        Adjustment.meanCenterSpots(expMatrix);
        
        for(i=0;i<n;i++){
            
            for(j=0;j<m;j++){
                
                values[i][j]=(double)expMatrix.A[i][j];
                expMatrix.A[i][j] = (float) temp[i][j];
            }
        }
        
        dataMatrix=new Matrix(values);
    }
    
    
    public void calculate(){
        
        PD.setMessage(0, "Distance: " + AbstractAlgorithm.getDistanceName(function));
        PD.setMessage(1, "Clusters to be created: " + k);
        PD.setMessage(2, "0 clusters created.");
        PD.setMessage(3, geneAssaigned+" genes have been assigned to clusters.");
        PD.setMessage(4, (n-geneAssaigned)+" genes left to be assigned to clusters.");
        PD.setTimerLabel(5,"Running for ", " seconds.", 1000);
        PD.setVisible(true);
        
        
        getMatrix();
        CreatedCluster=0;
        
        resultClusters=new GSCluster[k + 1];
        
        //GeneClusterPointers=new GeneCluster[k + 1]; //For 'k' clusters plus the final cluster of unassigned genes
        
        resultClusters[k] = new GSCluster();
        for (int i = 0; i < n; i++) {
            resultClusters[k].add(new Integer(i));
        }
        
        while(CreatedCluster<k){
            
            //System.out.println("CreatedCluster: " + CreatedCluster);
            
            CurrentCluster=0;
            unassigned = new GSCluster();
            
            //get sequence of clusters
            
            for (int i=0;i<n;i++){
                
                unassigned.add(new Integer(i));
            }
            
            int iter=getIteration();
            
            clusters=new GSCluster[iter];
            for (int i = 0; i < clusters.length; i++) {
                clusters[i] = new GSCluster();
            }
            
            for(int i=0; i<iter; i++){
                
                //System.out.println("CurrentCluster: " + CurrentCluster);
                
                matrixSize=unassigned.size();
                
                double[] scores=new double[matrixSize];
                int[] indexs=new int[matrixSize];
                
                for(int r=0; r<matrixSize;r++) {
                    indexs[r]=((Integer)(unassigned.elementAt(r))).intValue();
                }
                
                workingMatrix=dataMatrix.getMatrix(indexs,0,m-1);
                
                getPrincipleComponent();
                
                for(int j=0; j< matrixSize;j++){
                    
                    //currentGene=getGene(((Integer)(unassigned.elementAt(j))).intValue());
                    currentGene=getGene(j);
                    
                    scores[j]=getInnerProduct(prinCom,currentGene);
                }
                
                shaveGene(scores);
                CurrentCluster++;
            }
            
            //get cluster size
            int maxIndex=0;
            double maxGap=0.0d;
            double currentGap;
            
            for(int i=0;i<clusters.length;i++){
                
                workingMatrix=(Matrix)dataMatrix.clone();
                
                currentGap=getDk(clusters[i])-getD_k(clusters[i]);
                if (currentGap>maxGap){
                    
                    maxGap=currentGap;
                    maxIndex=i;
                }
            }
            
            resultClusters[CreatedCluster]=(GSCluster)clusters[maxIndex].clone();
            
            geneAssaigned+=resultClusters[CreatedCluster].size();
            
            PD.setMessage(2, (CreatedCluster + 1) +" clusters created.");
            if(gshGenes){
                PD.setMessage(3, geneAssaigned+" genes have been assigned to clusters.");
                PD.setMessage(4, (n-geneAssaigned)+" genes left to be assigned to clusters.");
            }
            else{
                PD.setMessage(3, geneAssaigned+" experiments have been assigned to clusters.");
                PD.setMessage(4, (n-geneAssaigned)+" experiments left to be assigned to clusters.");
            }
            updateMatrix();
            CreatedCluster++;
        }
        
        //Figure out which genes have not been assigned
        for (int i = 0; i < resultClusters.length - 1; i++) {
            for (int j = 0; j < resultClusters[i].size(); j++) {
                try {
                    resultClusters[k].removeElement(resultClusters[i].elementAt(j));
                } catch (Exception e) {
                    System.out.println("Exception when removing elements");
                }
            }
        }
        
        //Create a final cluster out of unassigned genes
        
        // Output messages
        for (int i = 0; i < resultClusters.length; i++) {
            //System.out.println("Cluster " + i);
            for (int j = 0; j < resultClusters[i].size(); j++) {
                //System.out.println("Gene " + resultClusters[i].elementAt(j));
            }
        }
        
        clusters = resultClusters;
        PD.dismiss();
    }
    
    public void abort() {
        stop=true;
    }
    
    public void getPrincipleComponent(){
        
        double[][] EV= new double[1][matrixSize];
        
        //
        EVD= (workingMatrix.times(workingMatrix.transpose())).eig();
        
        Matrix D=EVD.getD();
        Matrix U=EVD.getV();
        
        
        for(int i=0;i<matrixSize;i++){
            EV[0][i]=U.get(i, 0);
            //	System.out.println("EV[0][" + i + "]: " + EV[0][i]);
        }
        
        eigenVector=new Matrix(EV);
        Matrix PC = eigenVector.times(workingMatrix);
        for(int i=0;i<m;i++){
            
            prinCom[i]=PC.get(0,i);
            //	System.out.println("prinCom[" + i + "]: " + prinCom[i]);
        }
        
        
    }
    
    public int getIteration(){
        int count=0;
        int totalGenes = n;
        while(totalGenes > 1){
            totalGenes-=Math.max((int)(totalGenes*0.1),1);
            //totalGenes-=Math.max((int)(totalGenes*0.2),1); //Try 20% instead of 10% for max cluster size
            count++;
        }
        return count;
        
    }
    
    public double getInnerProduct(double[] x, double[] y){
        int count = 0;
        double add=0.0d;
        
        if(x.length!=y.length){
            System.out.println("Vector has different number of elements!");
        }else{count=x.length;}
        
        for(int i=0;i<count; i++) {
            
            add+=x[i]*y[i];
        }
        return add;
        
    }
    
    public double[] getGene(int row){
        
        double[] aGene=new double[m];
        
        for(int i=0;i<m;i++){
            
            aGene[i]=workingMatrix.get(row,i);
        }
        return aGene;
        
    }
    
    public void shaveGene(double[] scores){
        
        int cutNo=Math.max((int)(unassigned.size()*0.1),1);
        //int cutNo=Math.max((int)(unassigned.size()*0.2),1); //Try 20% instead of 10% for max cluster size
        int[] cutIndex=new int[cutNo];
        double minValue;
        int minIndex;
        Integer spaceHolder=new Integer(-1);
        
        for (int j=0;j<cutNo;j++){
            
            minValue=scores[0];
            minIndex=0;
            
            for(int i=0;i<matrixSize;i++){
                
                if(minValue>scores[i]){
                    
                    minValue=scores[i];
                    minIndex=i;
                }
            }
            cutIndex[j]=minIndex;
            scores[minIndex]=Double.MAX_VALUE;//to set it a big value so that it will not be chosen again.
        }
        
        for (int j=0;j<cutNo;j++){
            clusters[CurrentCluster].add((Integer)(unassigned.elementAt(cutIndex[j])));
            unassigned.setElementAt(spaceHolder, cutIndex[j]);
        }
        
        for (int j=0;j<cutNo;j++){
            
            unassigned.removeElement(spaceHolder);
            
        }
        
    }
    
    public double getDk(GSCluster gs) {
        
        float x_a=0.0f,v_b=0.0f;
        double R2,v_w=0.0d;
        double value;
        
        FloatMatrix Ave=gs.getAveGene();
        
        for (int i=0; i<m; i++) {
            x_a+=Ave.get(0,i);
        }
        x_a/=m;
        
        for (int i=0; i<m; i++) {
            v_b+=Math.pow((Ave.get(0,i)-x_a),2);
        }
        v_b/=m;
        
        for (int i=0; i<m; i++) {
            
            for (int j=0; j<gs.size(); j++) {
                value=workingMatrix.get(((Integer)gs.get(j)).intValue(),i);
                v_w+=Math.pow((value-Ave.get(0,i)),2);
            }
            
        }
        v_w/=(m*gs.size());
        
        R2=(v_b/v_w)/(1+v_b/v_w);
        
        return R2;
    }
    
    public double getD_k(GSCluster gs) {
        
        double R_2=0.0d;
        //iterate for 20 times(default)
        for (int iter=0; iter<fakedMatrix; iter++){
            
            getPermutedMatrix();
            R_2+=getDk(gs);
        }
        R_2/=fakedMatrix;
        return R_2;
        
    }
    
    public void getPermutedMatrix(){
        
        double temp;
        int gene1, gene2;
        for(int i=0;i<n;i++){
            //permute 5 elements for each row
            for (int j=0;j<swapTime;j++) {
                Random MyRandom= new Random();
                gene1=(int)Math.floor(MyRandom.nextFloat()*m);
                gene1=Math.min(gene1,m-1);
                temp=workingMatrix.get(i, gene1);
                gene2=(int)Math.floor(MyRandom.nextFloat()*m);
                gene2=Math.min(gene2,m-1);
                workingMatrix.set(i, gene1, workingMatrix.get(i, gene2));
                workingMatrix.set(i, gene2, temp);
            }
        }
    }
    
    public void updateMatrix(){
        
        double[][] AveGene= new double[1][m];
        
        //workingMatrix=(Matrix)dataMatrix.clone();
        FloatMatrix AG=resultClusters[CreatedCluster].getAveGene();
        for(int i=0; i<m; i++){
            
            AveGene[0][i]=(double)AG.get(0,i);
        }
        Matrix AveG=new Matrix(AveGene);
        Matrix I = Matrix.identity(m,m);
        Matrix Projector= I.minus((AveG.transpose()).times(AveG));
        dataMatrix=dataMatrix.times(Projector);
        
    }
    
    /**********************************************/
    
    private NodeValueList calculateHierarchicalTree(int[] features, int method, boolean genes, boolean experiments) throws AlgorithmException {
        NodeValueList nodeList = new NodeValueList();
        AlgorithmData data = new AlgorithmData();
        FloatMatrix experiment;
        
        if(gshGenes)
            experiment = getSubExperiment(this.expMatrix, features);
        else
            experiment = getSubExperimentReducedCols(this.expMatrix, features);
        
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
            int_matrix[i] = ((Integer) source.get(i)).intValue();
        }
        return int_matrix;
    }
    
    
    
    private FloatMatrix getMeans(GSCluster[] clusters) {
        FloatMatrix means = new FloatMatrix(clusters.length, number_of_samples);
        FloatMatrix mean;
        for (int i=0; i<clusters.length; i++) {
            mean = clusters[i].getMean();
            means.A[i] = mean.A[0];
        }
        return means;
    }
    
    private FloatMatrix getVariances(GSCluster[] clusters, FloatMatrix means) {
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
    
    private float getSampleNormalizedSum(GSCluster cluster, int column, float mean) {
        final int size = cluster.size();
        float sum = 0f;
        float value;
        
        validN = 0;
        
        for (int i=0; i<size; i++) {
            value = expMatrix.get(((Integer) cluster.get(i)).intValue(), column);
            if (!Float.isNaN(value)) {
                sum += Math.pow(value-mean, 2);
                validN++;
            }
        }
        return sum;
    }
    
    private float getSampleVariance(GSCluster cluster, int column, float mean) {
        return(float)Math.sqrt(getSampleNormalizedSum(cluster, column, mean)/(float) (validN - 1));
    }
    
    /***********************************************/
    
    
    private class GSCluster extends Vector {
        
        private FloatMatrix AveGene = new FloatMatrix(1,m);
        private FloatMatrix mean = new FloatMatrix(1,m);
        
        public GSCluster(){}
        
        public FloatMatrix getAveGene() {
            double CurrentMean;
            int n=size();
            float Value;
            for (int i=0; i<m; i++) {
                CurrentMean=0.0d;
                for (int j=0; j<n; j++) {
                    Value= (float) workingMatrix.get(((Integer)get(j)).intValue(),i);
                    if (!Float.isNaN(Value)) CurrentMean += Value;
                }
                AveGene.set(0,i,(float)(CurrentMean/(double)n));
            }
            return AveGene;
        }
        
        public Vector fGenes() {
            Vector floatVector = new Vector();
            
            for (int i = 0; i < this.size(); i++) {
                floatVector.addElement(new Float(((Integer) this.elementAt(i)).intValue()));
            }
            
            return floatVector;
        }
        
        public void calculateMean() {
            float currentMean;
            int n = size();
            int denom;
            float value;
            
            for (int i=0; i<number_of_samples; i++) {
                currentMean = 0f;
                denom = 0;
                for (int j=0; j<n; j++) {
                    value = expMatrix.get(((Integer) get(j)).intValue(), i);
                    if (!Float.isNaN(value)) {
                        currentMean += value;
                        denom++;
                    }
                }
                mean.set(0, i, currentMean/(float) denom);
            }
        }
        
        public FloatMatrix getMean() {
            calculateMean();
            return mean;
        }
    }
    
    
}


