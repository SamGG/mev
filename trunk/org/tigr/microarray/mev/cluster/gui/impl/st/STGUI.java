/*
Copyright @ 1999-2005, The Institute for Genomic Research (TIGR).
All rights reserved.
 */
/*
 * $RCSfile: STGUI.java,v $
 * $Revision: 1.4 $
 * $Date: 2005-02-24 20:23:51 $
 * $Author: braistedj $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.st;

import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.tree.DefaultMutableTreeNode;

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
import org.tigr.microarray.mev.cluster.gui.impl.hcl.HCLTreeData;
import org.tigr.microarray.mev.script.scriptGUI.IScriptGUI;
import org.tigr.util.FloatMatrix;

public class STGUI implements IClusterGUI, IScriptGUI {
    private Algorithm algorithm;
    private Progress progress;
    boolean drawGeneTree;
    boolean drawExptTree;
    
    /**
     * Inits the algorithm parameters, runs calculation and returns
     * a result to be inserted into the framework analysis node.
     */
    public DefaultMutableTreeNode execute(IFramework framework) throws AlgorithmException {

        IDistanceMenu menu = framework.getDistanceMenu();
        int function = menu.getDistanceFunction();
        if (function == Algorithm.DEFAULT) {
            function = Algorithm.EUCLIDEAN;
        }
        
        ResampleTreeInitDialog dialog = new ResampleTreeInitDialog((JFrame) framework.getFrame(), true, menu.getFunctionName(function), menu.isAbsoluteDistance());
        dialog.setVisible(true);
        
        if (dialog.isCancelled()) {
            return null;
        }
        
        int method = dialog.getMethod();
        drawGeneTree = dialog.drawGeneTreeCheckBox.isSelected();
        drawExptTree = dialog.drawExptTreeCheckBox.isSelected();
        int geneTreeAnalysisOption = dialog.getGeneTreeAnalysisOption();
        int exptTreeAnalysisOption = dialog.getExptTreeAnalysisOption();
        int geneTreeIterations = Integer.parseInt(dialog.geneTreeIterationsTextField.getText());
        int exptTreeIterations = Integer.parseInt(dialog.exptTreeIterationsTextField.getText());
        Listener listener = new Listener();
        
        try {
            Experiment experiment = framework.getData().getExperiment();
            AlgorithmFactory factory = framework.getAlgorithmFactory();
            
            this.algorithm = factory.getAlgorithm("ST");
            algorithm.addAlgorithmListener(listener);
            AlgorithmData data = new AlgorithmData();
            data.addMatrix("experiment", experiment.getMatrix());

            function = dialog.getDistanceMetric();
            
            data.addParam("distance-function", String.valueOf(function));
            data.addParam("distance-factor", String.valueOf(1.0f));
            data.addParam("distance-absolute", String.valueOf(dialog.isAbsoluteDistance()));
            
            data.addParam("method-linkage", String.valueOf(method));
            data.addParam("geneTreeIterations", String.valueOf(geneTreeIterations));
            data.addParam("exptTreeIterations", String.valueOf(exptTreeIterations));
            data.addParam("geneTreeAnalysisOption", String.valueOf(geneTreeAnalysisOption));
            data.addParam("exptTreeAnalysisOption", String.valueOf(exptTreeAnalysisOption));
            
            this.progress = new Progress(framework.getFrame(), "", listener);
            this.progress.show();
            
            long start = System.currentTimeMillis();
            AlgorithmData samples_result = null;
            if (drawExptTree) {
                progress.setTitle("Resampling by Experiments");
                data.addParam("drawGeneTree", String.valueOf(false));
                data.addParam("drawExptTree", String.valueOf(true));
                samples_result = algorithm.execute(data);
                validate(samples_result);
            }
            AlgorithmData genes_result = null;
            if (drawGeneTree) {
                progress.setTitle("Resampling by Genes");
                data.addParam("drawGeneTree", String.valueOf(true));
                data.addParam("drawExptTree", String.valueOf(false));
                genes_result = algorithm.execute(data);
                validate(genes_result);
            }
            long time = System.currentTimeMillis() - start;
            
            GeneralInfo info = new GeneralInfo();
            info.time = time;
            info.method = method;
            info.function = menu.getFunctionName(function);
            if (drawGeneTree) {
                info.drawGeneTree = true;
                info.geneTreeIterations = geneTreeIterations;
                info.geneTreeAnalysisOption = geneTreeAnalysisOption;
            }
            if (drawExptTree) {
                info.drawExptTree = true;
                info.exptTreeIterations = exptTreeIterations;
                info.exptTreeAnalysisOption = exptTreeAnalysisOption;
            }
            
            return createResultTree(experiment, genes_result, samples_result, info);
            
        } finally {
            if (algorithm != null) {
                algorithm.removeAlgorithmListener(listener);
            }
            if (progress != null) {
                progress.dispose();
            }
        }
    }
    
    
    
    public AlgorithmData getScriptParameters(IFramework framework) {
        
        IDistanceMenu menu = framework.getDistanceMenu();
        int function = menu.getDistanceFunction();
        if (function == Algorithm.DEFAULT) {
            function = Algorithm.EUCLIDEAN;
        }
        
        ResampleTreeInitDialog dialog = new ResampleTreeInitDialog((JFrame) framework.getFrame(), true, menu.getFunctionName(function), menu.isAbsoluteDistance());
        dialog.setVisible(true);
        
        if (dialog.isCancelled()) {
            return null;
        }
        
        int method = dialog.getMethod();
        drawGeneTree = dialog.drawGeneTreeCheckBox.isSelected();
        drawExptTree = dialog.drawExptTreeCheckBox.isSelected();
        int geneTreeAnalysisOption = dialog.getGeneTreeAnalysisOption();
        int exptTreeAnalysisOption = dialog.getExptTreeAnalysisOption();
        int geneTreeIterations = Integer.parseInt(dialog.geneTreeIterationsTextField.getText());
        int exptTreeIterations = Integer.parseInt(dialog.exptTreeIterationsTextField.getText());

        AlgorithmData data = new AlgorithmData();  
  
        function = dialog.getDistanceMetric();
  
        data.addParam("distance-function", String.valueOf(function));
        data.addParam("distance-factor", String.valueOf(1.0f));
        data.addParam("distance-absolute", String.valueOf(dialog.isAbsoluteDistance()));
        
        data.addParam("method-linkage", String.valueOf(method));
        data.addParam("geneTreeIterations", String.valueOf(geneTreeIterations));
        data.addParam("exptTreeIterations", String.valueOf(exptTreeIterations));
        data.addParam("geneTreeAnalysisOption", String.valueOf(geneTreeAnalysisOption));
        data.addParam("exptTreeAnalysisOption", String.valueOf(exptTreeAnalysisOption));
        
        data.addParam("drawExptTree", String.valueOf(drawExptTree));
        data.addParam("drawGeneTree", String.valueOf(drawGeneTree));
        
        //script control parameters
        
        // alg name
        data.addParam("name", "ST");
        
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
        AlgorithmParameters params = algData.getParams();
        this.drawExptTree = params.getBoolean("drawExptTree");
        this.drawGeneTree = params.getBoolean("drawGeneTree");
     
        Listener listener = new Listener();
        
        try {
            AlgorithmFactory factory = framework.getAlgorithmFactory();            
            this.algorithm = factory.getAlgorithm("ST");
            algorithm.addAlgorithmListener(listener);

            algData.addMatrix("experiment", experiment.getMatrix());
            
            this.progress = new Progress(framework.getFrame(), "", listener);
            this.progress.show();
            
            long start = System.currentTimeMillis();
            AlgorithmData samples_result = null;
           
            //keep the parameter additions, although they are redundant the flip/flop nature
            //is used to indicate which way to build a tree on the algorithm side.
            if (drawExptTree) {
                progress.setTitle("Resampling by Experiments");
                algData.addParam("drawGeneTree", String.valueOf(false));
                algData.addParam("drawExptTree", String.valueOf(true));
                samples_result = algorithm.execute(algData);
                validate(samples_result);
            }
            AlgorithmData genes_result = null;
            if (drawGeneTree) {
                progress.setTitle("Resampling by Genes");
                algData.addParam("drawGeneTree", String.valueOf(true));
                algData.addParam("drawExptTree", String.valueOf(false));
                genes_result = algorithm.execute(algData);
                validate(genes_result);
            }
            long time = System.currentTimeMillis() - start;
            
            GeneralInfo info = new GeneralInfo();
            info.time = time;
            info.method = params.getInt("method-linkage");

            int function = params.getInt("distance-function");
            info.function = framework.getDistanceMenu().getFunctionName(function);
            
            if (drawGeneTree) {
                info.drawGeneTree = true;
                info.geneTreeIterations = params.getInt("geneTreeIterations");
                info.geneTreeAnalysisOption = params.getInt("geneTreeAnalysisOption");
            }
            if (drawExptTree) {
                info.drawExptTree = true;
                info.exptTreeIterations = params.getInt("exptTreeIterations");
                info.exptTreeAnalysisOption = params.getInt("exptTreeAnalysisOption");
            }
            
            return createResultTree(experiment, genes_result, samples_result, info);
            
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
        if (result.getIntArray("orig-child-1-array") == null) {
            throw new AlgorithmException("parameter 'orig-child-1-array' is null");
        }
        if (result.getIntArray("orig-child-2-array") == null) {
            throw new AlgorithmException("parameter 'orig-child-2-array' is null");
        }
        if (result.getIntArray("orig-node-order") == null) {
            throw new AlgorithmException("parameter 'orig-node-order' is null");
        }
        if (result.getMatrix("orig-height") == null) {
            throw new AlgorithmException("parameter 'orig-height' is null");
        }
    }
    
    /**
     * Creates a result tree.
     */
    private DefaultMutableTreeNode createResultTree(Experiment experiment, AlgorithmData genes_result, AlgorithmData samples_result, GeneralInfo info) {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("ST");
        root.add(new DefaultMutableTreeNode(new LeafInfo("Support Tree - "+info.getMethodName(), createHCLSupportViewer(experiment, genes_result, samples_result, root))));
        root.add(new DefaultMutableTreeNode("Time: "+String.valueOf(info.time)+" ms"));
        root.add(new DefaultMutableTreeNode(info.function));
        if (info.drawGeneTree) {
            DefaultMutableTreeNode geneTreeNode = new DefaultMutableTreeNode("Gene Tree Resampling");
            geneTreeNode.add(new DefaultMutableTreeNode("Resampling method: " + info.getGeneTreeAnalysisOptionName()));
            geneTreeNode.add(new DefaultMutableTreeNode("Iterations: " + info.geneTreeIterations));
            root.add(geneTreeNode);
        }
        
        if (info.drawExptTree) {
            DefaultMutableTreeNode exptTreeNode = new DefaultMutableTreeNode("Expt Tree Resampling");
            exptTreeNode.add(new DefaultMutableTreeNode("Resampling method: " + info.getExptTreeAnalysisOptionName()));
            exptTreeNode.add(new DefaultMutableTreeNode("Iterations: " + info.exptTreeIterations));
            root.add(exptTreeNode);
        }
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
        data.child_1_array = result.getIntArray("orig-child-1-array");
        data.child_2_array = result.getIntArray("orig-child-2-array");
        data.node_order = result.getIntArray("orig-node-order");
        data.height = result.getMatrix("orig-height").getRowPackedCopy();
        return data;
    }
    
    private IViewer createHCLSupportViewer(Experiment experiment, AlgorithmData genes_result, AlgorithmData samples_result, DefaultMutableTreeNode root) {
        return new HCLSupportViewer(experiment, null, getHCLTreeData(genes_result), getHCLTreeData(samples_result), getGeneTreeSupportVector(genes_result), getExptTreeSupportVector(samples_result), root);
    }
    
    
    private Vector getGeneTreeSupportVector(AlgorithmData result) {
        Vector geneTreeSupportVector = new Vector();
        
        if (drawGeneTree) {
            FloatMatrix geneTreeSupportMatrix = result.getMatrix("geneTreeSupportMatrix");
            for (int i = 0; i < geneTreeSupportMatrix.A[0].length; i++) {
                geneTreeSupportVector.add(new Float(geneTreeSupportMatrix.A[0][i]));
            }
        } else {
            geneTreeSupportVector = null;
        }
        
        return geneTreeSupportVector;
    }
    
    private Vector getExptTreeSupportVector(AlgorithmData result) {
        Vector exptTreeSupportVector = new Vector();
        if (drawExptTree) {
            FloatMatrix exptTreeSupportMatrix = result.getMatrix("exptTreeSupportMatrix");
            for (int i = 0; i < exptTreeSupportMatrix.A[0].length; i++) {
                exptTreeSupportVector.add(new Float(exptTreeSupportMatrix.A[0][i]));
            }
        } else {
            exptTreeSupportVector = null;
        }
        
        return exptTreeSupportVector;
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
        public boolean drawGeneTree = false;
        public boolean drawExptTree = false;
        public int geneTreeIterations;
        public int exptTreeIterations;
        public int geneTreeAnalysisOption;
        public int exptTreeAnalysisOption;
        
        public String getGeneTreeAnalysisOptionName() {
            String optionName;
            switch(geneTreeAnalysisOption) {
                case 0: optionName = "No Resampling";
                break;
                case 1: optionName = "Bootstrap Experiments";
                break;
                case 2: optionName = "Bootstrap Genes";
                break;
                case 3: optionName = "Jackknife Experiments";
                break;
                case 4: optionName = "Jackknife Genes";
                break;
                default: optionName = "No Resampling";
                break;
            }
            
            return optionName;
        }
        
        
        public String getExptTreeAnalysisOptionName() {
            
            String optionName;
            switch(exptTreeAnalysisOption) {
                case 0: optionName = "No Resampling";
                break;
                case 1: optionName = "Bootstrap Experiments";
                break;
                case 2: optionName = "Bootstrap Genes";
                break;
                case 3: optionName = "Jackknife Experiments";
                break;
                case 4: optionName = "Jackknife Genes";
                break;
                default: optionName = "No Resampling";
                break;
            }
            
            return optionName;
        }
        
        
        public String getMethodName() {
            return getMethodName(method);
        }
        
        public static String getMethodName(int method) {
            method = method == -1 ? 2 : method;
            return methods[method];
        }
        
        private static String[] methods = {"average linkage", "complete linkage", "single linkage"};
    }
    
}