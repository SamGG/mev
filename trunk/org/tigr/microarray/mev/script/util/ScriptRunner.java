/*
Copyright @ 1999-2004, The Institute for Genomic Research (TIGR).
All rights reserved.
 */
/*
 * ScriptRunner.java
 *
 * Created on March 15, 2004, 3:27 PM
 */

package org.tigr.microarray.mev.script.util;

import java.io.File;
import java.awt.Frame;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import org.tigr.util.FloatMatrix;
import org.tigr.microarray.mev.ResultTree;
import org.tigr.microarray.mev.TMEV;

import org.tigr.microarray.mev.action.ActionManager;
import org.tigr.microarray.mev.action.AnalysisAction;

import org.tigr.microarray.mev.cluster.algorithm.AlgorithmData;
import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.IClusterGUI;
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.IViewer;
import org.tigr.microarray.mev.cluster.gui.LeafInfo;
import org.tigr.microarray.mev.cluster.gui.helpers.CentroidUserObject;

import org.tigr.microarray.mev.script.Script;
import org.tigr.microarray.mev.script.scriptGUI.ScriptCentroidViewer;
import org.tigr.microarray.mev.script.scriptGUI.ScriptCentroidsViewer;
import org.tigr.microarray.mev.script.scriptGUI.ScriptClusterSelectionInfoViewer;
import org.tigr.microarray.mev.script.scriptGUI.ScriptExperimentCentroidViewer;
import org.tigr.microarray.mev.script.scriptGUI.ScriptExperimentCentroidsViewer;
import org.tigr.microarray.mev.script.scriptGUI.ScriptExperimentClusterViewer;
import org.tigr.microarray.mev.script.scriptGUI.ScriptExperimentViewer;
import org.tigr.microarray.mev.script.scriptGUI.ScriptTreeViewer;

import org.tigr.microarray.mev.script.scriptGUI.IScriptGUI;


/** ScriptRunner supports script execution activities as directed by the
 * <CODE>ScriptManager</CODE>
 * @author braisted
 */
public class ScriptRunner {
    
    /** Script to run
     */    
    private Script script;
    /** ScriptTree containing objects for execution
     */    
    private ScriptTree scriptTree;
    /** parent's frame
     */    
    private Frame parentFrame;
    /** action manager to provide MeV algorithm access.
     */    
    private ActionManager actionManager;
    /** framework object
     */    
    private IFramework framework;
    /** hashtable of available algorithm classes
     */    
    private Hashtable classHash;
    /** algorithm set collection to execute
     */    
    private AlgorithmSet [] algSets;
    /**
     *  Three output modes: internal (0), file (1), external (2)
     *  see <code>ScriptConstants</code> document for constant names.
     */
    private int mode;
    
    /** Creates a new instance of ScriptRunner
     * @param script Script to execute
     * @param actionManager action manager provides algorithms
     * @param framework
     */
    public ScriptRunner(Script script, ActionManager actionManager, IFramework framework) {
        this.script = script;
        scriptTree = script.getScriptTree();
        this.actionManager = actionManager;
        this.framework = framework;
        mode = ScriptConstants.SCRIPT_OUTPUT_MODE_INTERNAL_OUTPUT;
        parentFrame = framework.getFrame();
        classHash = getClassNames();
    }
    
    /** Sets the output mode (file or internal)
     * @param outputMode  */    
    public void setOutputMode(int outputMode) {
        mode = outputMode;
    }
    
    /** Triggers script execution
     * @param outputMode output mode
     */    
    public void execute(int outputMode) {
        mode = outputMode;
        Thread thread = new Thread(new Runner());
        thread.start();
    }
    
    /** Triggers script execution to interal MeV result tree.
     */    
    public void execute() {
        Thread thread = new Thread(new Runner());
        thread.start();
    }
    
    /** Executes a delivered <CODE>AlgorithmSet</CODE>
     * @param set set of algorithms and data
     * @return
     */    
    private DefaultMutableTreeNode execute(AlgorithmSet set) {
        Experiment experiment = set.getExperiment();
        
        if(experiment == null) {
            DefaultMutableTreeNode emptyNode = new DefaultMutableTreeNode("No Result (empty input data node)");
            return emptyNode;
        }
        
        int algCount = set.getAlgorithmCount();
        File outputFile;
        AlgorithmNode algNode;
        AlgorithmData data;
        String algName, algType;
        
        DefaultMutableTreeNode currNode = null, outputNode = null;
        
        if(mode == ScriptConstants.SCRIPT_OUTPUT_MODE_FILE_OUTPUT) {
            JFileChooser chooser = new JFileChooser(TMEV.getFile("data/"));
            if(chooser.showOpenDialog(parentFrame) == JFileChooser.APPROVE_OPTION) {
                outputFile = chooser.getSelectedFile();
            } else {
                return null;
            }
        }
        
        if(algCount > 0) {
            //create a node for the alg set and a data info node;
        }
        
        for(int i = 0; i < algCount; i++) {
            algNode = set.getAlgorithmNodeAt(i);
            data = algNode.getAlgorithmData();
            algName = algNode.getAlgorithmName();
            algType = algNode.getAlgorithmType();
            // int actionIndex = getActionIndex(algName);
            
            // Action action = actionManager.getAction(actionManager.ANALYSIS_ACTION+String.valueOf(actionIndex));
            if(algType.equals(ScriptConstants.ALGORITHM_TYPE_CLUSTER) || algType.equals(ScriptConstants.ALGORITHM_TYPE_CLUSTER_GENES)
            || algType.equals(ScriptConstants.ALGORITHM_TYPE_CLUSTER_EXPERIMENTS) || algType.equals(ScriptConstants.ALGORITHM_TYPE_VISUALIZATION)) {
                
                String className = (String)(this.classHash.get(algName));

                try {
                    Class clazz = Class.forName(className);
                    IScriptGUI gui = (IScriptGUI)clazz.newInstance();
                    currNode = gui.executeScript(framework, data, experiment);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(parentFrame, "Can't execute script "+algName+ " algorithm", "Script Parameter Error", JOptionPane.WARNING_MESSAGE);
                    e.printStackTrace();
                }
                
                if(currNode != null) {
                    if(outputNode == null) {
                        outputNode = new DefaultMutableTreeNode("Results");
                    }
                    outputNode.add(currNode);
                    
                    attachResultToChildAlgorithmSets(algNode, experiment, extractClusters(currNode));
                }
            } else if(algType.equals(ScriptConstants.ALGORITHM_TYPE_ADJUSTMENT)){
                //Handle adjustments here
                data.addParam("name", algName);
                
                ScriptDataTransformer adjuster = new ScriptDataTransformer(experiment, framework);
                Experiment resultExperiment = adjuster.transformData(data); 
                
                //Associate result experiment and indices with the output node's result set if it exists.
                int [][] clusters = new int[1][];
                clusters[0] = getDefaultGeneIndices(resultExperiment.getNumberOfGenes());
                attachResultToChildAlgorithmSets(algNode, resultExperiment, clusters);
                
                if(outputNode == null) {
                    outputNode = new DefaultMutableTreeNode("Results");
                }
                
                DefaultMutableTreeNode resultNode = getViewerNodes(resultExperiment);
                resultNode.setUserObject("Data Adjustment: "+algName);
                outputNode.add(resultNode);
            } else if(algType.equals(ScriptConstants.ALGORITHM_TYPE_CLUSTER_SELECTION)) {
                //handle cluster selection
                data.addParam("name", algName);
                
                //DataNode node = (DataNode)(algNode.getParent());
                ScriptDataTransformer selector = new ScriptDataTransformer(experiment, framework);
                int [][] selectedClusters = selector.selectClusters(data, set.getClusters());
                
                attachResultToChildAlgorithmSets(algNode, experiment, selectedClusters);
                
                boolean areGeneClusters =data.getParams().getBoolean("process-gene-clusters");
                DefaultMutableTreeNode node = getSelectedClusterViewers(data, experiment, selectedClusters, areGeneClusters);
                
                if(outputNode == null) {
                    outputNode = new DefaultMutableTreeNode("Results");
                }
                
                outputNode.add(node);
            }
        }
        return outputNode;
    }
    
    /** Thread runner for execution
     */    
    private class Runner implements Runnable {
        
        /** run method to kick of execution
         */        
        public void run() {
            algSets = scriptTree.getAlgorithmSets();
            DefaultMutableTreeNode currNode, setNode, algSetViewerNode, dataNode, resultNode, scriptNode;
            AlgorithmSet set;
            Experiment experiment;
            boolean haveResult = false;
            DataNode inputNode;
            AlgorithmNode inputAlgNode;
            
            DefaultMutableTreeNode scriptResultNode = new DefaultMutableTreeNode("Script Result");
            
            if(algSets.length > 0) {
                
            }
            
            for(int i = 0; i < algSets.length; i++) {
                set = algSets[i];
                if(set.getAlgorithmCount() > 0) {
                    
                    resultNode = execute(set);
                    
                    if(resultNode != null) {
                        experiment = set.getExperiment();
                        
                        haveResult = true;
                        setNode = new DefaultMutableTreeNode("Algorithm Set");
                        dataNode = new DefaultMutableTreeNode("Input Data");
                        ScriptTree copyTree = new ScriptTree(scriptTree);
                        algSetViewerNode = new DefaultMutableTreeNode(new LeafInfo("Script Tree", new ScriptTreeViewer(copyTree, scriptTree.getScriptManager(), set.getDataNode())));
                        
                        inputNode = set.getDataNode();
                        inputAlgNode = (AlgorithmNode)(inputNode.getParent());
                        
                        if(inputAlgNode != null) {
                            currNode = new DefaultMutableTreeNode("Algorithm Source: "+ inputAlgNode.getAlgorithmName() +
                            " ["+inputAlgNode.getDataNodeRef()+","+inputAlgNode.getID()+"] ");
                            dataNode.add(currNode);
                        }
                        
                        currNode = new DefaultMutableTreeNode("Input Data Node: "+inputNode.toString());
                        dataNode.add(currNode);
                        if(experiment != null) {
                            currNode = new DefaultMutableTreeNode("Number of Experiments: "+experiment.getNumberOfSamples());
                            dataNode.add(currNode);
                            currNode = new DefaultMutableTreeNode("Number of Genes: "+experiment.getNumberOfGenes());
                            dataNode.add(currNode);
                            
                            currNode = getViewerNodes(set.getExperiment());
                            dataNode.add(currNode);                            
                        } else {
                            currNode = new DefaultMutableTreeNode("Number of Experiments: 0, null input data");
                            dataNode.add(currNode);
                            currNode = new DefaultMutableTreeNode("Number of Genes: 0, null input data");
                            dataNode.add(currNode);
                        }
                        setNode.add(algSetViewerNode);
                        setNode.add(dataNode);
                        setNode.add(resultNode);                        
                        scriptResultNode.add(setNode);
                    }
                }
            }
            
            ResultTree tree = framework.getResultTree();
            //framework.addNode(tree.getAnalysisNode(), scriptResultNode);
            framework.addAnalysisResult(scriptResultNode);
            tree.scrollPathToVisible(new TreePath(((DefaultTreeModel)(tree.getModel())).getPathToRoot(scriptResultNode)));
        }        
    }
    
    /** Constructs viewer nodes for input display
     * @param experiment
     * @return
     */    
    private DefaultMutableTreeNode getViewerNodes(Experiment experiment) {
        DefaultMutableTreeNode viewerNode = new DefaultMutableTreeNode("Input Data Viewers");
        int [][] cluster = new int [1][];
        
        //since it's a single set, contains all indices in experiment.
        cluster[0] = getDefaultGeneIndices(experiment.getNumberOfGenes());
        
        //Will need to deal with var. exp nums perhaps use exp cluster viewers
        
        DefaultMutableTreeNode currNode = new DefaultMutableTreeNode(new LeafInfo("Expression Image", new ScriptExperimentViewer(experiment, cluster)));
        viewerNode.add(currNode);
        
        FloatMatrix matrix = experiment.getMatrix();
        FloatMatrix means = getMeans(matrix, cluster);
        FloatMatrix vars = getVariances(matrix, means, cluster);
        
        ScriptCentroidViewer viewer = new ScriptCentroidViewer(experiment, cluster);
        viewer.setMeans(means.A);
        viewer.setVariances(vars.A);
        currNode = new DefaultMutableTreeNode(new LeafInfo("Centroid Graph", viewer, new CentroidUserObject(0,CentroidUserObject.VARIANCES_MODE)));
        viewerNode.add(currNode);
        currNode = new DefaultMutableTreeNode(new LeafInfo("Expression Graph", viewer, new CentroidUserObject(0,CentroidUserObject.VALUES_MODE)));
        viewerNode.add(currNode);
        
        return viewerNode;
    }
    
    /** Returns algorithm class names hash
     * @return
     */    
    private Hashtable getClassNames() {
        Hashtable hash = new Hashtable();
        int algCnt = 0;
        String algName, className;
        
        AnalysisAction action;
        
        while ((action = (AnalysisAction)(actionManager.getAction(ActionManager.ANALYSIS_ACTION+String.valueOf(algCnt))))!=null){
            
            //Name or Short Description??
            
            algName = (String)(action.getValue(Action.NAME));
            className = (String)(action.getValue(ActionManager.PARAMETER));
            
            hash.put(algName, className);
            algCnt++;
        }
        return hash;
    }
    
    
    /**
     *  Calculates means for the clusters
     */
    private FloatMatrix getMeans(FloatMatrix data, int [][] clusters){
        FloatMatrix means = new FloatMatrix(clusters.length, data.getColumnDimension());
        for(int i = 0; i < clusters.length; i++){
            means.A[i] = getMeans(data, clusters[i]);
        }
        return means;
    }
    
    
    /**
     *  Returns a set of means for an element
     */
    private float [] getMeans(FloatMatrix data, int [] indices){
        int nSamples = data.getColumnDimension();
        float [] means = new float[nSamples];
        float sum = 0;
        float n = 0;
        float value;
        for(int i = 0; i < nSamples; i++){
            n = 0;
            sum = 0;
            for(int j = 0; j < indices.length; j++){
                value = data.get(indices[j],i);
                if(!Float.isNaN(value)){
                    sum += value;
                    n++;
                }
            }
            if(n > 0)
                means[i] = sum/n;
            else
                means[i] = Float.NaN;
        }
        return means;
    }
    
    /**
     * Returns a matrix of standard deviations grouped by cluster and element
     */
    private FloatMatrix getVariances(FloatMatrix data, FloatMatrix means, int [][] clusters){
        int nSamples = data.getColumnDimension();
        FloatMatrix variances = new FloatMatrix(clusters.length, nSamples);
        for(int i = 0; i < clusters.length; i++){
            variances.A[i] = getVariances(data, means, clusters[i], i);
        }
        return variances;
    }
    
    /** returns variances
     */    
    private float [] getVariances(FloatMatrix data, FloatMatrix means, int [] indices, int clusterIndex){
        int nSamples = data.getColumnDimension();
        float [] variances = new float[nSamples];
        float sse = 0;
        float mean;
        float value;
        int n = 0;
        for(int i = 0; i < nSamples; i++){
            mean = means.get(clusterIndex, i);
            n = 0;
            sse = 0;
            for(int j = 0; j < indices.length; j++){
                value = data.get(indices[j], i);
                if(!Float.isNaN(value)){
                    sse += (float)Math.pow((value - mean),2);
                    n++;
                }
            }
            if(n > 1)
                variances[i] = (float)Math.sqrt(sse/(n-1));
            else
                variances[i] = 0.0f;
        }
        return variances;
    }
    
    /** Returns a set of dfault indices
     */    
    private int [] getDefaultGeneIndices(int length) {
        int [] indices = new int[length];
        for(int i = 0; i < indices.length; i++)
            indices[i] = i;
        
        return indices;
    }
    
    private void attachResultToChildAlgorithmSets(AlgorithmNode algNode, Experiment experiment, int [][] clusters) {
        //get data ouput nodes
        int outputCount = algNode.getChildCount();
        DataNode dataNode;
        for(int i = 0; i < outputCount; i++) {
            dataNode = ((DataNode)algNode.getChildAt(i));
            for( int j = 0; j < algSets.length; j++) {
                if(dataNode == algSets[j].getDataNode()) {
                    //if it's not multicluster ouput then append the propper experiment
                    
                    if(!dataNode.getDataOutputClass().equals(ScriptConstants.OUTPUT_DATA_CLASS_MULTICLUSTER_OUTPUT)
                    && !dataNode.getDataOutputClass().equals(ScriptConstants.OUTPUT_DATA_CLASS_GENE_MULTICLUSTER_OUTPUT)
                    && !dataNode.getDataOutputClass().equals(ScriptConstants.OUTPUT_DATA_CLASS_EXPERIMENT_MULTICLUSTER_OUTPUT)) {
                        if( i < clusters.length ) {
                            if(algNode.getAlgorithmType().equals(ScriptConstants.ALGORITHM_TYPE_CLUSTER_EXPERIMENTS))
                                setExperiment(algSets[j], experiment, clusters[i], false);
                            else
                                setExperiment(algSets[j], experiment, clusters[i], true);
                        }
                    }

                    //if it IS multicluster output then the next algorithm must be for
                    //cluster selection.  This algorithm will require clusters[][] for selection process
                    else {
                        setExperimentAndClusters(algSets[j], experiment, clusters, algNode);
                    }
                    
                }
            }
        }
    }
    
    /** Sets the Experiment into the output nodes
     */    
    private void setExperiment(AlgorithmSet algSet, Experiment experiment, int [] indices, boolean geneReduction) {
        ScriptDataTransformer transformer = new ScriptDataTransformer(experiment, framework);
        Experiment trimmedExperiment = transformer.getTrimmedExperiment(indices, geneReduction);
        algSet.setExperiment(trimmedExperiment);
    }
    
    
    /** Sets Experiment and cluster indicies.
     */    
    private void setExperimentAndClusters(AlgorithmSet algSet, Experiment experiment, int [][] clusters, AlgorithmNode algNode) {
        algSet.setExperiment(experiment);
        algSet.setClusters(clusters);
        if(algNode.getAlgorithmType().equals(ScriptConstants.ALGORITHM_TYPE_CLUSTER_GENES))
            algSet.setClusterType(ScriptConstants.CLUSTER_TYPE_GENE);
        else
            algSet.setClusterType(ScriptConstants.CLUSTER_TYPE_EXPERIMENT);
    }
    
    /** Extracts cluster results from tree nodes
     */    
    private int [][] extractClusters(DefaultMutableTreeNode analysisNode) {
        
        int [][] clusters;
        Enumeration enum = analysisNode.depthFirstEnumeration();
        DefaultMutableTreeNode currentNode;
        IViewer viewer;
        Experiment exp;
        
        while (enum.hasMoreElements()){
            currentNode = (DefaultMutableTreeNode)enum.nextElement();
            if(currentNode.getUserObject() instanceof LeafInfo){
                viewer = ((LeafInfo)currentNode.getUserObject()).getViewer();
                if(viewer != null) {
                    exp = viewer.getExperiment();
                    clusters = viewer.getClusters();
                    if(exp != null && clusters != null) {
                        return clusters;
                    }
                }
            }
        }
        return null;
    }
    /** Constructs cluster viewer for clusters selected
     * by cluster selection algorithms
     */    
    private DefaultMutableTreeNode getSelectedClusterViewers(AlgorithmData data, Experiment experiment, int [][] clusters, boolean areGeneClusters) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode("Cluster Selection Results");
        addExpressionImages(node, experiment, clusters, areGeneClusters);
        addCentroidViews(node, experiment, clusters, areGeneClusters);
        addSelectionInfoViewer(node, data);
        return node;
    }
    
    /**
     * Adds nodes to display clusters data.
     */
    private void addExpressionImages(DefaultMutableTreeNode root, Experiment experiment, int [][] clusters, boolean clusterGenes) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode("Expression Images");
        IViewer expViewer;
        if(clusterGenes)
            expViewer = new ScriptExperimentViewer(experiment, clusters);
        else
            expViewer = new ScriptExperimentClusterViewer(experiment, clusters);
        
        for (int i=0; i<clusters.length; i++) {
            node.add(new DefaultMutableTreeNode(new LeafInfo("Cluster "+String.valueOf(i+1), expViewer, new Integer(i))));
        }
        root.add(node);
    }
    
    /**
     * Adds nodes to display centroid charts.
     */
    private void addCentroidViews(DefaultMutableTreeNode root, Experiment experiment, int [][] clusters, boolean clusterGenes) {
        DefaultMutableTreeNode centroidNode = new DefaultMutableTreeNode("Centroid Graphs");
        DefaultMutableTreeNode expressionNode = new DefaultMutableTreeNode("Expression Graphs");
        
        FloatMatrix matrix = experiment.getMatrix();
        if(!clusterGenes)
            matrix = matrix.transpose();
        
        FloatMatrix means = getMeans(matrix, clusters);
        FloatMatrix variances = getVariances(matrix, means, clusters);
        
        //if ! genes then transpose it back
        if(!clusterGenes)
            matrix = matrix.transpose();
        
        
        ScriptCentroidViewer centroidViewer;
        ScriptExperimentCentroidViewer expCentroidViewer;
        if(clusterGenes){
            centroidViewer = new ScriptCentroidViewer(experiment, clusters);
            centroidViewer.setMeans(means.A);
            centroidViewer.setVariances(variances.A);
            for (int i=0; i<clusters.length; i++) {
                centroidNode.add(new DefaultMutableTreeNode(new LeafInfo("Cluster "+String.valueOf(i+1), centroidViewer, new CentroidUserObject(i, CentroidUserObject.VARIANCES_MODE))));
                expressionNode.add(new DefaultMutableTreeNode(new LeafInfo("Cluster "+String.valueOf(i+1), centroidViewer, new CentroidUserObject(i, CentroidUserObject.VALUES_MODE))));
            }
            
            ScriptCentroidsViewer centroidsViewer = new ScriptCentroidsViewer(experiment, clusters);
            centroidsViewer.setMeans(means.A);
            centroidsViewer.setVariances(variances.A);
            
            centroidNode.add(new DefaultMutableTreeNode(new LeafInfo("All Clusters", centroidsViewer, new Integer(CentroidUserObject.VARIANCES_MODE))));
            expressionNode.add(new DefaultMutableTreeNode(new LeafInfo("All Clusters", centroidsViewer, new Integer(CentroidUserObject.VALUES_MODE))));
            
        }
        else{
            expCentroidViewer = new ScriptExperimentCentroidViewer(experiment, clusters);
            
            expCentroidViewer.setMeans(means.A);
            expCentroidViewer.setVariances(variances.A);
            for (int i=0; i<clusters.length; i++) {
                centroidNode.add(new DefaultMutableTreeNode(new LeafInfo("Cluster "+String.valueOf(i+1), expCentroidViewer, new CentroidUserObject(i, CentroidUserObject.VARIANCES_MODE))));
                expressionNode.add(new DefaultMutableTreeNode(new LeafInfo("Cluster "+String.valueOf(i+1), expCentroidViewer, new CentroidUserObject(i, CentroidUserObject.VALUES_MODE))));
            }
            ScriptExperimentCentroidsViewer expCentroidsViewer = new ScriptExperimentCentroidsViewer(experiment, clusters);
            expCentroidsViewer.setMeans(means.A);
            expCentroidsViewer.setVariances(variances.A);
            
            centroidNode.add(new DefaultMutableTreeNode(new LeafInfo("All Clusters", expCentroidsViewer, new Integer(CentroidUserObject.VARIANCES_MODE))));
            expressionNode.add(new DefaultMutableTreeNode(new LeafInfo("All Clusters", expCentroidsViewer, new Integer(CentroidUserObject.VALUES_MODE))));
        }
        root.add(centroidNode);
        root.add(expressionNode);
    }
    
    /** Creates a viewer for cluster selection results
     */    
    private void addSelectionInfoViewer(DefaultMutableTreeNode root, AlgorithmData data) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(new LeafInfo("Selection Information", new ScriptClusterSelectionInfoViewer(data)));
        root.add(node);
    }
}
