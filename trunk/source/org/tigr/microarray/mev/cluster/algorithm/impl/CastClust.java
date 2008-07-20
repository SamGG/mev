/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: CastClust.java,v $
 * $Revision: 1.4 $
 * $Date: 2006-02-23 20:59:45 $
 * $Author: caliente $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.algorithm.impl;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

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
import org.tigr.util.awt.ProgressDialog;

public class CastClust extends AbstractAlgorithm {
    private boolean stop = false;
    public boolean runInside = false;
    private int function;
    private float factor;
    private boolean absolute;
    private boolean castGenes;
    
    private FloatMatrix expMatrix;
    
    private FloatMatrix SimMatrix;
    private CTCluster unassigned;
    private CTCluster openCluster;
    private int CurrentCluster;
    private int m; // m = number_of_samples
    private int n; //n = number_of_genes
    private int maxGene, minGene;
    private float maxA, minA;
    private int maxIndex, minIndex;
    public int clusterCount;
    private boolean changesOccur;
    private boolean pearson=false;
    public float threshold=0.5f;
    
    private long StartTime;
    private long CalculationTime;
    
    private boolean Stop;
    private int DistanceFunction;
    
    private ProgressDialog PD;
    
    private double zeroValue;

    private int hcl_function;
    private boolean hcl_absolute;    
    
    public CastClust() {
    }
    
    public CastClust(boolean runInside) {
        this.runInside = runInside;
    }
    
    public AlgorithmData execute(AlgorithmData data) throws AlgorithmException {
        clusterCount = 0;
        AlgorithmParameters map = data.getParams();
        function = map.getInt("distance-function", EUCLIDEAN);
        factor   = map.getFloat("distance-factor", 1.0f);
        absolute = map.getBoolean("distance-absolute", false);
        threshold = map.getFloat("threshold", 0.5f);
        castGenes = map.getBoolean("cast-cluster-genes", true);

        hcl_function = map.getInt("hcl-distance-function", EUCLIDEAN);
        hcl_absolute = map.getBoolean("hcl-distance-absolute", false);        
        
        boolean hierarchical_tree = map.getBoolean("hierarchical-tree", false);
        int method_linkage = map.getInt("method-linkage", 0);
        boolean calculate_genes = map.getBoolean("calculate-genes", false);
        boolean calculate_experiments = map.getBoolean("calculate-experiments", false);
        this.expMatrix = data.getMatrix("experiment");
        n = this.expMatrix.getRowDimension(); //n = number_of_genes
        m = this.expMatrix.getColumnDimension();//m = number_of_samples
        SimMatrix = new FloatMatrix(n,n);
        
        JFrame dummyFrame = new JFrame();
        PD = new ProgressDialog(dummyFrame, "CAST Progression", false, 4);

        java.awt.Dimension screenDim = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        PD.setSize(450,255);
        PD.setLocation((screenDim.width - PD.getWidth())/2, (screenDim.height - PD.getHeight())/2);
      
        JButton abortButton = new JButton(" Cancel ");
        abortButton.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        abortButton.setFocusPainted(false);               
        abortButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                stop = true;
                PD.dismiss();
            }
        });
       
        JPanel progressPanel = PD.getLabelPanel();
        JPanel superPanel = new JPanel();
        superPanel.setLayout(new BorderLayout());
        superPanel.add(progressPanel, BorderLayout.CENTER);
        superPanel.add(abortButton, BorderLayout.SOUTH);
        PD.setMainPanel(superPanel);
        
        CTCluster[] clusters = calculate(runInside);
        
        for (int i = 0; i < clusters.length; i++) {
            if (clusters[i] == null) System.out.println("Null Cluster: " + i);
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
            features = convert2int(clusters[i].genes);
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
        result.addMatrix("clusters_means", means);
        result.addMatrix("clusters_variances", variances);
        return result;
        
    }
    
    
    public void abort() {
        stop = true;
    }
    
    
    
    CTCluster[] calculate(boolean runInside) {
        
        StartTime = System.currentTimeMillis();
        
        if (! runInside) {
            
            PD.setMessage(0, clusterCount+" clusters created.");
            if(this.castGenes){
                PD.setMessage(1, 0+" genes have been assigned to clusters.");
                PD.setMessage(2, n+" genes left to be assigned to clusters.");
            }
            else{
                PD.setMessage(1, 0+" experiments have been assigned to clusters.");
                PD.setMessage(2, n+" experiments left to be assigned to clusters.");
            }
            PD.setTimerLabel(3,"Running for ", " seconds.", 1000);
            PD.setVisible(true);
        }
        
        getSimMatrix();
        
        CTCluster[] clusters = new CTCluster[n];
        unassigned = new CTCluster();
        for (int i = 0; i < n; i++) {
            unassigned.add(i);
        }
        
        while (unassigned.count != 0) {
            // start a new cluster
            openCluster = new CTCluster();
            
            // reset affinity
            for (int i = 0; i < unassigned.count; i++) {
                (unassigned.affinity).set(i,new Float(0));
            }
            
            do{
                changesOccur = false;
                float theFloat;
                
                // ADD:
                while (getMaxAffinityGene() && (maxA >= threshold * openCluster.count)) {
                    //System.out.println("adding..."+maxA);
                    unassigned.move(maxIndex, openCluster);
                    
                    for (int i = 0; i < unassigned.count; i++) {
                        theFloat = ((Float) unassigned.affinity.elementAt(i)).floatValue();
                        theFloat += SimMatrix.get(((Integer)((unassigned.genes).elementAt(i))).intValue(), maxGene);
                        unassigned.affinity.setElementAt((new Float(theFloat)), i);
                        //System.out.println("Add, unassigned: " + theFloat);
                    }
                    for (int i = 0; i < openCluster.count; i++) {
                        theFloat = ((Float) openCluster.affinity.elementAt(i)).floatValue();
                        theFloat += SimMatrix.get(((Integer)((openCluster.genes).elementAt(i))).intValue(), maxGene);
                        openCluster.affinity.setElementAt((new Float(theFloat)), i);
                        //System.out.println("Add, openCluster: " + theFloat);
                    }
                    changesOccur = true;
                    getMaxAffinityGene();
                    
                    //System.out.println("+ unassigned = " + unassigned.count);
                    //System.out.println("+ openCluster = " + openCluster.count);
                    
                }
                
                // REMOVE:
                if (minA != zeroValue) {
                    while (getMinAffinityGene() && (minA < threshold * openCluster.count)) {
                        
                        //System.out.println("removing..."+minA);
                        openCluster.move(minIndex, unassigned);
                        
                        for (int i = 0; i < unassigned.count; i++) {
                            theFloat = ((Float) unassigned.affinity.elementAt(i)).floatValue();
                            theFloat -= SimMatrix.get(((Integer)((unassigned.genes).elementAt(i))).intValue(), minGene);
                            unassigned.affinity.setElementAt((new Float(theFloat)), i);
                            //System.out.println("Remove, unassigned: " + theFloat);
                        }
                        for (int i = 0; i < openCluster.count; i++) {
                            theFloat = ((Float) openCluster.affinity.elementAt(i)).floatValue();
                            theFloat -= SimMatrix.get(((Integer)((openCluster.genes).elementAt(i))).intValue(), minGene);
                            openCluster.affinity.setElementAt((new Float(theFloat)), i);
                            //System.out.println("Remove, openCluster: " + theFloat);
                        }
                        changesOccur = true;
                        getMinAffinityGene();
                        
                        //System.out.println("- unassigned = " + unassigned.count);
                        //System.out.println("- openCluster = " + openCluster.count);
                        
                    }
                }
                
            } while (changesOccur);
            
            //System.out.println("n = " + n);
            //System.out.println("Currently: " + clusterCount);
            
            if (! runInside) {
                PD.setMessage(0, clusterCount+" clusters created.");
                if(this.castGenes){
                    PD.setMessage(1, (n-unassigned.count)+" genes have been assigned to clusters.");
                    PD.setMessage(2, unassigned.count+" genes left to be assigned to clusters.");
                }
                else{
                    PD.setMessage(1, (n-unassigned.count)+" experiments have been assigned to clusters.");
                    PD.setMessage(2, unassigned.count+" experiments left to be assigned to clusters.");
                }
            }
            
            if (clusterCount < n) clusters[clusterCount++] = openCluster;
            openCluster = null;
        }
        
        if (! runInside) {PD.dismiss();}
        
        //System.out.println(clusterCount + " clusters found");
        //MakeCluster();
        
        CTCluster[] trueClusters = new CTCluster[clusterCount];
        for (int i = 0; i < clusterCount; i++) {
            trueClusters[i] = clusters[i];
        }
        
        //GeneClusterPointers=new GeneCluster[clusterCount];
        CalculationTime=System.currentTimeMillis()-StartTime;
        
        //return clusters;
        return trueClusters;
    }
    
    
    private NodeValueList calculateHierarchicalTree(int[] features, int method, boolean genes, boolean experiments) throws AlgorithmException {
        NodeValueList nodeList = new NodeValueList();
        AlgorithmData data = new AlgorithmData();
        FloatMatrix experiment;
        if(castGenes)
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
    
    private int[] convert2int(Vector source) {
        int[] int_matrix = new int[source.size()];
        for (int i=0; i<int_matrix.length; i++) {
            int_matrix[i] = (int)((Integer)source.get(i)).intValue();
        }
        return int_matrix;
    }
    
    
    synchronized float getDistance(int gene1, int gene2) {
        double distance = 0;
        
        if((function == Algorithm.DEFAULT)||(function == Algorithm.COSINE)
        ||(function == Algorithm.COVARIANCE)||(function == Algorithm.EUCLIDEAN)
        ||(function == Algorithm.DOTPRODUCT)||(function == Algorithm.MANHATTAN)
        ||(function == Algorithm.SPEARMANRANK)||(function == Algorithm.KENDALLSTAU)
        ||(function == Algorithm.MUTUALINFORMATION)){
            // ONLY EUCLIDEAN OR PEARSON IS USED IN THE CALCULATION!!!!
            distance = ExperimentUtil.geneDistance(expMatrix, null, gene1, gene2, Algorithm.EUCLIDEAN, factor, absolute);
            
        }
        else if((function == Algorithm.PEARSON)||(function == Algorithm.PEARSONUNCENTERED)
        ||(function == Algorithm.PEARSONSQARED)) {
            
            if (absolute) {
                distance = Math.abs(ExperimentUtil.geneDistance(expMatrix, null, gene1, gene2, Algorithm.PEARSON, factor, absolute));
            } else {
                distance = ExperimentUtil.geneDistance(expMatrix, null, gene1, gene2, Algorithm.PEARSON, factor, absolute);
            }
        }
        
        //System.out.println(distance);
        
        return (float) distance;
    }
    
    
    synchronized void getSimMatrix(){//calculate the similarity matrix
        int i,j;
        float sim;
        float maxSim = 0;
        
        if((function == Algorithm.PEARSON)||(function == Algorithm.PEARSONUNCENTERED)
        ||(function == Algorithm.PEARSONSQARED)) {
            pearson=true;
        }
        
        if (absolute) {
            zeroValue = 0;
        } else {
            zeroValue = 0.5;
        }
        
        if(pearson){
            for (i=0;i<n;i++){
                for(j=0;j<n;j++){
                    sim=getDistance(i,j);
                    if (!absolute)
                        sim = (float)(1.0 - (sim + 1.0) / 2.0);
                    else
                        sim = (float)((sim + 1.0)/2.0);
                    SimMatrix.set(i,j, sim);
                }
            }
            
        }else{
            
            for (i=0;i<n;i++){
                for(j=0;j<n;j++){
                    sim=getDistance(i,j);
                    if (sim > maxSim) maxSim = sim;
                }
            }
            
            for (i=0;i<n;i++){
                for(j=0;j<n;j++){
                    sim=getDistance(i,j);
                    sim = (float) 1 - (sim / maxSim);
                    SimMatrix.set(i,j, sim);
                }
            }
        }
    }
    
    
    protected boolean getMaxAffinityGene() {
        
        if (unassigned.count == 0)
            return false;
        
        maxIndex = 0;
        maxA = ((Float)((unassigned.affinity).elementAt(0))).floatValue();
        for (int i = 1; i < unassigned.count; i++) {
            if (((Float)((unassigned.affinity).elementAt(i))).floatValue()> maxA) {
                maxA = ((Float)((unassigned.affinity).elementAt(i))).floatValue();
                maxIndex = i;
            }
        }
        maxGene =((Integer)((unassigned.genes).elementAt(maxIndex))).intValue();
        return true;
    }
    
    protected boolean getMinAffinityGene() {
        if (openCluster.count == 0)
            return false;
        
        minIndex = 0;
        minA = ((Float)((openCluster.affinity).elementAt(0))).floatValue();
        for (int i = 1; i < openCluster.count; i++) {
            if (((Float)((openCluster.affinity).elementAt(i))).floatValue() < minA) {
                minA = ((Float)((openCluster.affinity).elementAt(i))).floatValue();
                minIndex = i;
            }
        }
        minGene = ((Integer)((openCluster.genes).elementAt(minIndex))).intValue();
        return true;
    }
    
    //inner class
    private class CTCluster{
        int count=0;
        Vector genes;
        Vector affinity;
        
        public CTCluster() {
            genes=new Vector();
            affinity = new Vector();
            
        }
        
        public Vector fGenes() {
            Vector floatVector = new Vector();
            
            for (int i = 0; i < genes.size(); i++) {
                floatVector.addElement(new Float(((Integer) genes.elementAt(i)).intValue()));
            }
            
            return floatVector;
        }
        
        public void add(int g) {
            add(g, 0);
        }
        
        public void add(int g, float a) {
            genes.add(count,new Integer(g));
            affinity.add(count,new Float(a));
            count++;
            
        }
        
        
        public void remove(int g) {
            int index = -1;
            for (int i = 1; i <= count; i++) {
                if (((Integer)genes.elementAt(i)).intValue() == g) {
                    index = i;
                    break;
                }
            }
            if (index == -1) {
                System.err.println("cluster doesn't contain " + g);
                System.exit(1);
            }
            removeIndex(index);
        }
        
        
        public void removeIndex(int index) {
            genes.remove(index);
            affinity.remove(index);
            count--;
        }
        
        
        public void move(int index, CTCluster c){
            c.add(((Integer)(genes.elementAt(index))).intValue(), ((Float)(affinity.elementAt(index))).floatValue());
            removeIndex(index);
        }
        
        protected FloatMatrix getMean() {
            FloatMatrix mean = new FloatMatrix(1, m);
            float currentMean;
            int k = genes.size();
            int denom;
            float value;
            for (int i=0; i<m; i++) {
                currentMean = 0f;
                denom = 0;
                for (int j=0; j<k; j++) {
                    value = expMatrix.get(((Integer) genes.get(j)).intValue(), i);
                    if (!Float.isNaN(value)) {
                        currentMean += value;
                        denom++;
                    }
                }
                mean.set(0, i, currentMean/(float)denom);
            }
            
            return mean;
        }
    }
    
    
    private FloatMatrix getMeans(CTCluster[] clusters) {
        FloatMatrix means = new FloatMatrix(clusters.length, m);
        FloatMatrix mean;
        for (int i=0; i<clusters.length; i++) {
            mean = clusters[i].getMean();
            means.A[i] = mean.A[0];
        }
        return means;
    }
    
    private FloatMatrix getVariances(CTCluster[] clusters, FloatMatrix means) {
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
    
    private float getSampleNormalizedSum(CTCluster cluster, int column, float mean) {
        final int size = cluster.genes.size();
        float sum = 0f;
        float value;
        validN = 0;
        for (int i=0; i<size; i++) {
            value = expMatrix.get(((Integer)cluster.genes.get(i)).intValue(), column);
            if (!Float.isNaN(value)) {
                sum += Math.pow(value-mean, 2);
                validN++;
            }
        }
        return sum;
    }
    
    private float getSampleVariance(CTCluster cluster, int column, float mean) {
        return(float)Math.sqrt(getSampleNormalizedSum(cluster, column, mean)/(float)(validN-1));
    }
    
    
}
