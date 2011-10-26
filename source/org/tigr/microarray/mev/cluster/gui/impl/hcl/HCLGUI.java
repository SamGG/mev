/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: HCLGUI.java,v $
 * $Revision: 1.5 $
 * $Date: 2006/02/23 20:59:51 $
 * $Author: caliente $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.hcl;

import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;

import org.tigr.microarray.mev.annotation.AnnotationFieldConstants;
import org.tigr.microarray.mev.cluster.algorithm.Algorithm;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmData;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmEvent;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmException;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmFactory;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmListener;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmParameters;
import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.IClusterGUI;
import org.tigr.microarray.mev.cluster.gui.IDistanceMenu;
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.IViewer;
import org.tigr.microarray.mev.cluster.gui.LeafInfo;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.DialogListener;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.Progress;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentUtil;
import org.tigr.microarray.mev.script.scriptGUI.IScriptGUI;
import org.tigr.util.FloatMatrix;

public class HCLGUI implements IClusterGUI, IScriptGUI {
    
    private Algorithm algorithm;
    private Progress progress;
    private boolean isSampOrdered = false;
    private boolean isGeneOrdered = false;
	private boolean isClusterValidation = false;
    /**
     * Inits the algorithm parameters, runs calculation and returns
     * a result to be inserted into the framework analysis node.
     */
    public DefaultMutableTreeNode execute(IFramework framework) throws AlgorithmException {
        
        IDistanceMenu menu = framework.getDistanceMenu();
        int function = menu.getDistanceFunction();
        if (function == Algorithm.DEFAULT) {
            function = Algorithm.PEARSON;
        }        
        
        HCLInitDialog dialog = new HCLInitDialog(framework.getFrame(), menu.getFunctionName(function), menu.isAbsoluteDistance(), true, framework.getClusterRepository(0));
        if (dialog.showModal() != JOptionPane.OK_OPTION) {
            return null;
        }
        int method = dialog.getMethod();
        function = dialog.getDistanceMetric();       
        isClusterValidation = dialog.getValidationPanel().isValidate();
        Listener listener = new Listener();
        
        try {
            Experiment experiment = framework.getData().getExperiment();
            AlgorithmFactory factory = framework.getAlgorithmFactory();    
            int number_of_samples = experiment.getNumberOfSamples();
            
            int [] columnIndices = experiment.getColumnIndicesCopy(); 
            
            ArrayList<String> sampleLabels = new ArrayList<String>();
            ArrayList<String> geneLabels = new ArrayList<String>();
            for (int i = 0; i < number_of_samples; i++) {
                sampleLabels.add(framework.getData().getFullSampleName(columnIndices[i])); 
            }
            for (int i = 0; i < experiment.getNumberOfGenes(); i++) {
            	geneLabels.add(framework.getData().getElementAnnotation(i, AnnotationFieldConstants.PROBE_ID)[0]);
            }
            
            
            this.algorithm = factory.getAlgorithm("HCL");
            algorithm.addAlgorithmListener(listener);
            AlgorithmData data = new AlgorithmData();
            data.addStringArray("geneLabels", geneLabels.toArray(new String[geneLabels.size()]));
            data.addStringArray("sampleLabels", sampleLabels.toArray(new String[sampleLabels.size()]));
            data.addMatrix("experiment", experiment.getMatrix());

            data.addParam("hcl-distance-function", String.valueOf(function));
            data.addParam("distance-factor", String.valueOf(1.0f));
            data.addParam("hcl-distance-absolute", String.valueOf(dialog.getAbsoluteSelection()));
            data.addParam("method-linkage", String.valueOf(method));

            
            
            this.progress = new Progress(framework.getFrame(), "", listener);
            this.progress.show();
            
            long start = System.currentTimeMillis();
            AlgorithmData genes_result = null;
            if (dialog.isClusterGenes()) {
                progress.setTitle("Clustering by Genes");
                data.addParam("calculate-genes", String.valueOf(true));
                data.addParam("optimize-gene-ordering", String.valueOf(false));
                if (dialog.isGeneOrdering()){
                	isGeneOrdered = true;
                	data.addParam("optimize-gene-ordering", String.valueOf(true));
                }
                genes_result = algorithm.execute(data);
                validate(genes_result);
            }
            AlgorithmData samples_result = null;
            if (dialog.isClusterExperiments()) {
                progress.setTitle("Clustering by Samples");
                data.addParam("calculate-genes", String.valueOf(false));
                data.addParam("optimize-sample-ordering", String.valueOf(false));
                if (dialog.isSampleOrdering()){
                	isSampOrdered = true;
                	data.addParam("optimize-sample-ordering", String.valueOf(true));
                }
                samples_result = algorithm.execute(data);
                validate(samples_result);
            }

            AlgorithmData validationData = new AlgorithmData();
            if (isClusterValidation){
            	progress.setIndeterminate(true);
            	progress.setIndeterminantString("This process may take awhile...");
            	progress.setDescription("Performing Cluster Validation");
            	progress.setTitle("Hierarchical Clustering");
            	
				validationData.addMatrix("experiment", experiment.getMatrix());
				validationData.addStringArray("geneLabels", geneLabels.toArray(new String[geneLabels.size()]));
				validationData.addStringArray("sampleLabels", sampleLabels.toArray(new String[sampleLabels.size()]));				
				dialog.getValidationPanel().addValidationParameters(validationData);            
				ExperimentUtil.performValidation(validationData);
            }
    		if (validationData.getResultNode("validation-node")==null)
    			System.out.println("result node is null");
    		else
    			System.out.println("result node is not null");
            long time = System.currentTimeMillis() - start;
            
            GeneralInfo info = new GeneralInfo();
            info.time = time;
            info.method = method;
            info.function = menu.getFunctionName(function);
            DefaultMutableTreeNode resultNode = createResultTree(experiment, genes_result, samples_result, validationData, info);
            return resultNode;
            
        } finally {
            if (algorithm != null) {
                algorithm.removeAlgorithmListener(listener);
            }
            if (progress != null) {
                progress.dispose();
            }
        }
    }
    
    
    
    /*
     * Scripting Methods
     */
    
    public AlgorithmData getScriptParameters(IFramework framework) {
        IDistanceMenu menu = framework.getDistanceMenu();
        int function = menu.getDistanceFunction();
        if (function == Algorithm.DEFAULT) {
            function = Algorithm.PEARSON;
        }        
        
        HCLInitDialog dialog = new HCLInitDialog(framework.getFrame(), menu.getFunctionName(function), menu.isAbsoluteDistance(), true);

        if (dialog.showModal() != JOptionPane.OK_OPTION) {
            return null;
        }
        int method = dialog.getMethod();
        function = dialog.getDistanceMetric();  
        
        AlgorithmData data = new AlgorithmData();
                
        data.addParam("distance-function", String.valueOf(function));
        data.addParam("distance-factor", String.valueOf(1.0f));
        data.addParam("distance-absolute", String.valueOf(dialog.getAbsoluteSelection()));
        data.addParam("method-linkage", String.valueOf(method));
        
        if (dialog.isClusterGenes())
            data.addParam("calculate-genes", String.valueOf(true));
        
        if (dialog.isClusterExperiments())
            data.addParam("calculate-experiments", String.valueOf(true));
        
        if (dialog.isGeneOrdering())
            data.addParam("optimize-gene-ordering", String.valueOf(true));

        if (dialog.isSampleOrdering())
            data.addParam("optimize-sample-ordering", String.valueOf(true));
        
        
        //script control parameters
        
        // alg name
        data.addParam("name", "HCL");
        
        // alg type
        data.addParam("alg-type", "cluster");
        
        // output class
        data.addParam("output-class", "single-output");
        
        //output nodes
        String [] outputNodes = new String[1];
        outputNodes[0] = "Single Ordered Output";
        data.addStringArray("output-nodes", outputNodes);
        
        return data;
    }
    
    
    public DefaultMutableTreeNode executeScript(IFramework framework, AlgorithmData algData, Experiment experiment) throws AlgorithmException {
        Listener listener = new Listener();
        
        try {
            algData.addMatrix("experiment", experiment.getMatrix());
            
            AlgorithmParameters params = algData.getParams();
            boolean clusterGenes = params.getBoolean("calculate-genes");
            boolean clusterExperiments = params.getBoolean("calculate-experiments");
            
            AlgorithmFactory factory = framework.getAlgorithmFactory();
            this.algorithm = factory.getAlgorithm("HCL");
            algorithm.addAlgorithmListener(listener);
            
            this.progress = new Progress(framework.getFrame(), "", listener);
            this.progress.show();
            
            long start = System.currentTimeMillis();
            
            AlgorithmData genes_result = null;
            if (clusterGenes) {
                progress.setTitle("Clustering by Genes");
                algData.addParam("calculate-genes", String.valueOf(true));
                genes_result = algorithm.execute(algData);
                validate(genes_result);
            }
            AlgorithmData samples_result = null;
            if (clusterExperiments) {
                progress.setTitle("Clustering by Samples");
                algData.addParam("calculate-genes", String.valueOf(false));
                samples_result = algorithm.execute(algData);
                validate(samples_result);
            }
            long time = System.currentTimeMillis() - start;
            
            GeneralInfo info = new GeneralInfo();
            info.time = time;
            info.method = params.getInt("method-linkage");
            info.function = framework.getDistanceMenu().getFunctionName(params.getInt("distance-function"));
            return createResultTree(experiment, genes_result, samples_result, null, info);
            
        } finally {
            if (algorithm != null) {
                algorithm.removeAlgorithmListener(listener);
            }
            if (progress != null) {
                progress.dispose();
            }
        }
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
    
    /**
     * Creates a result tree.
     * @param validationData 
     */
    private DefaultMutableTreeNode createResultTree(Experiment experiment, AlgorithmData genes_result, AlgorithmData samples_result, AlgorithmData validationData, GeneralInfo info) {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("HCL");
        root.add(new DefaultMutableTreeNode(new LeafInfo("HCL Tree", createHCLViewer(experiment, genes_result, samples_result, root))));
        if(genes_result != null)
            root.add(new DefaultMutableTreeNode(new LeafInfo("Gene Node Height Plot", new HCLNodeHeightGraph(getHCLTreeData(genes_result), true))));
        if(samples_result != null)
            root.add(new DefaultMutableTreeNode(new LeafInfo("Sample Node Height Plot", new HCLNodeHeightGraph(getHCLTreeData(samples_result), false))));
        addGeneralInfo(root, info);
        if (isClusterValidation)
        	root.add(validationData.getResultNode("validation-node"));
        
        return root;
    }
    
    /**
     * Returns a hcl tree data from the specified AlgorithmData structure.
     */
    private HCLTreeData getHCLTreeData(AlgorithmData result) {
        if (result == null) {
            return null;
        }
        HCLTreeData data = new HCLTreeData();
        data.child_1_array = result.getIntArray("child-1-array");
        data.child_2_array = result.getIntArray("child-2-array");
        data.node_order = result.getIntArray("node-order");
        data.height = result.getMatrix("height").getRowPackedCopy();
        data.setFunction(result.getParams().getInt("function"));
        return data;
    }
    
    /**
     * Creates an <code>HCLViewer</code>.
     */
    private IViewer createHCLViewer(Experiment experiment, AlgorithmData genes_result, AlgorithmData samples_result, DefaultMutableTreeNode root) {
        return new HCLViewer(experiment, null, getHCLTreeData(genes_result), getHCLTreeData(samples_result), root);
    }
    
    /**
     * Adds node with general iformation.
     */
    private void addGeneralInfo(DefaultMutableTreeNode root, GeneralInfo info) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode("General Information");
        node.add(new DefaultMutableTreeNode("Linkage Method: "+info.getMethodName()));
        node.add(new DefaultMutableTreeNode("Leaf Order Optimization: " + isOrdered()));
        node.add(new DefaultMutableTreeNode("Time: "+String.valueOf(info.time)+" ms"));
        node.add(new DefaultMutableTreeNode(info.function));
        root.add(node);
    }
    /**
     * returns String for leaf order optimization
     */
    private String isOrdered(){
    	if (isSampOrdered){
    		if (isGeneOrdered)
    			return "Samples, Genes";
    		return "Samples";
    	}else{
		if (isGeneOrdered)
			return "Genes";
    	}
    	return "None";
    }
    
    /**
     * The class to listen to algorithm events.
     */
    private class Listener extends DialogListener implements AlgorithmListener {
        
        public void valueChanged(AlgorithmEvent event) {
            switch (event.getId()) {
                case AlgorithmEvent.SET_UNITS:
                    progress.setUnits(event.getIntValue());
                    progress.setDescription(event.getDescription());
                    break;
                case AlgorithmEvent.PROGRESS_VALUE:
                    progress.setValue(event.getIntValue());
                    progress.setDescription(event.getDescription());
                    break;
            }
        }
        
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            if (command.equals("cancel-command")) {
                algorithm.abort();
                progress.dispose();
            }
        }
        
        public void windowClosing(WindowEvent e) {
            algorithm.abort();
            progress.dispose();
        }
    }
    
    /**
     * General info structure.
     */
    public static class GeneralInfo {
        public long time;
        public int method;
        public String function;
        
        public String getMethodName() {
            return getMethodName(method);
        }
        
        public static String getMethodName(int method) {
            method = method == -1 ? 2 : method;
            return methods[method];
        }
        
        private static String[] methods = {"average linkage", "complete linkage", "single linkage"};
    }
    
    public static void printResult(AlgorithmData result) {
        FloatMatrix similarity = result.getMatrix("similarity-matrix");
        similarity.print(5, 2);
        int[] parent = result.getIntArray("parent-array");
        int[] child1 = result.getIntArray("child-1-array");
        int[] child2 = result.getIntArray("child-2-array");
        int[] nodeOrder = result.getIntArray("node-order");
        int[] nodeHeight = result.getIntArray("node-height");
        int[] numberOfChildren = result.getIntArray("number-of-children");
        float[] height = result.getMatrix("height").getRowPackedCopy();
    }
}
