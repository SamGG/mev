/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
 */
/*
 * $RCSfile: GDMGUI.java,v $
 * $Revision: 1.2 $
 * $Date: 2004-05-10 17:00:22 $
 * $Author: braisted $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.gdm;

import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;

import org.tigr.util.FloatMatrix;
import org.tigr.util.ConfMap;

import org.tigr.microarray.mev.cluster.gui.IViewer;
import org.tigr.microarray.mev.cluster.gui.LeafInfo;
import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.IClusterGUI;
import org.tigr.microarray.mev.cluster.gui.IDistanceMenu;
import org.tigr.microarray.mev.cluster.gui.helpers.CentroidUserObject;

import org.tigr.microarray.mev.cluster.gui.impl.dialogs.Progress;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.DialogListener;

import org.tigr.microarray.mev.cluster.gui.impl.hcl.HCLGUI;
import org.tigr.microarray.mev.cluster.gui.impl.hcl.HCLViewer;
import org.tigr.microarray.mev.cluster.gui.impl.hcl.HCLTreeData;
import org.tigr.microarray.mev.cluster.gui.impl.hcl.HCLInitDialog;

import org.tigr.microarray.mev.cluster.Node;
import org.tigr.microarray.mev.cluster.Cluster;
import org.tigr.microarray.mev.cluster.NodeList;
import org.tigr.microarray.mev.cluster.algorithm.Algorithm;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmData;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmEvent;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmFactory;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmListener;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmException;

import org.tigr.microarray.mev.cluster.NodeValueList;

import org.tigr.microarray.mev.script.scriptGUI.IScriptGUI;

public class GDMGUI implements IClusterGUI, IScriptGUI {
    
    private Experiment experiment;
    private Algorithm algorithm;
    private Progress progress;
    private IFramework framework;
    private FloatMatrix geneDistanceMatrix;
    private float minDist;
    private float maxDist;
    private int num_genes;
    private int num_experiments;
    private boolean useGenes;
    private int displayInterval;
    
    public GDMGUI() {
    }
    
    /**
     * Initialize the algorithm's parameters and execute it.
     */
    public DefaultMutableTreeNode execute(IFramework framework) throws AlgorithmException {
        this.framework = framework;
        this.experiment = framework.getData().getExperiment();
        GeneralInfo info = new GeneralInfo();
        Listener listener = new Listener();
        
        GDMInitDialog dialog = new GDMInitDialog(framework.getFrame());
        if (dialog.showModal() != JOptionPane.OK_OPTION) {
            return null;
        }
        
        useGenes = dialog.isUseGenes();
        if(useGenes)
            displayInterval = dialog.getDisplayInterval();
        else
            displayInterval = 1;
        
        try {
            algorithm = framework.getAlgorithmFactory().getAlgorithm("GDM");
            algorithm.addAlgorithmListener(listener);
            
            this.progress = new Progress(framework.getFrame(), "GDM Training", listener);
            this.progress.show();
            
            AlgorithmData data = new AlgorithmData();
            if (useGenes) {
                data.addMatrix("experiment", framework.getData().getExperiment().getMatrix());
                data.addParam("distance-factor", String.valueOf(1f));
            } else {
                data.addMatrix("experiment", framework.getData().getExperiment().getMatrix().transpose());
                data.addParam("distance-factor", String.valueOf(false));
            }
            
            IDistanceMenu menu = framework.getDistanceMenu();
            
            data.addParam("distance-absolute", String.valueOf(menu.isAbsoluteDistance()));
            int function = menu.getDistanceFunction();
            if (function == Algorithm.DEFAULT) {
                function = Algorithm.EUCLIDEAN;
            }
            
            data.addParam("distance-function", String.valueOf(function));
            
            long startTime = System.currentTimeMillis();
            AlgorithmData result = algorithm.execute(data);
            info.time = System.currentTimeMillis()-startTime;
            info.function = menu.getFunctionName(function);
            
            if (useGenes) {
                int maxGeneNameLength = getMaxGeneNameLength();
                result.addParam("maxGeneNameLength", String.valueOf(maxGeneNameLength));
            } else {
                int maxExpNameLength = getMaxExpNameLength();
                result.addParam("maxExpNameLength", String.valueOf(maxExpNameLength));
            }
            
            return createResultTree(result, info);
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
        this.framework = framework;
        this.experiment = framework.getData().getExperiment();
        
        
        GDMInitDialog dialog = new GDMInitDialog(framework.getFrame());
        if (dialog.showModal() != JOptionPane.OK_OPTION) {
            return null;
        }
        
        useGenes = dialog.isUseGenes();
        if(useGenes)
            displayInterval = dialog.getDisplayInterval();
        else
            displayInterval = 1;
        
        
            AlgorithmData data = new AlgorithmData();
            data.addParam("gdm-genes", String.valueOf(useGenes));
            data.addParam("display-interval", String.valueOf(displayInterval));
            if (useGenes) {
                data.addParam("distance-factor", String.valueOf(1f));
            } else {
             data.addParam("distance-factor", String.valueOf(false));
            }
            
            IDistanceMenu menu = framework.getDistanceMenu();
            
            data.addParam("distance-absolute", String.valueOf(menu.isAbsoluteDistance()));
            int function = menu.getDistanceFunction();
            if (function == Algorithm.DEFAULT)     {
                function = Algorithm.EUCLIDEAN;
            }
            
            data.addParam("distance-function", String.valueOf(function));
            
            //script control parameters
            
            
            // alg name
            data.addParam("name", "GDM");
            
            // alg type
            data.addParam("alg-type", "data-visualization");
            
            // output class
            data.addParam("output-class", "single-output");
            
            //output nodes
            String [] outputNodes = new String[1];
            outputNodes[0] = "Data Visualization";
            data.addStringArray("output-nodes", outputNodes);
            
            return data;
        }
        
        public DefaultMutableTreeNode executeScript(IFramework framework, AlgorithmData algData, Experiment experiment) throws AlgorithmException {
            this.framework = framework;
            Listener listener = new Listener();
            this.experiment = experiment;
            algData.addMatrix("experiment", experiment.getMatrix());
            this.useGenes = algData.getParams().getBoolean("gdm-genes"); 
            this.displayInterval = algData.getParams().getInt("display-interval");
            int function = algData.getParams().getInt("distance-function");   
                
            try {
                algorithm = framework.getAlgorithmFactory().getAlgorithm("GDM");
                algorithm.addAlgorithmListener(listener);
                
                this.progress = new Progress(framework.getFrame(), "GDM Training", listener);
                this.progress.show();
                
                
                long startTime = System.currentTimeMillis();
                AlgorithmData result = algorithm.execute(algData);
                
                GeneralInfo info = new GeneralInfo();                           
                info.time = System.currentTimeMillis()-startTime;          
                info.function = framework.getDistanceMenu().getFunctionName(function);
           
                if (useGenes) {
                    int maxGeneNameLength = getMaxGeneNameLength();
                    algData.addMatrix("experiment", experiment.getMatrix());
                    result.addParam("maxGeneNameLength", String.valueOf(maxGeneNameLength));
                } else {
                    int maxExpNameLength = getMaxExpNameLength();
                    algData.addMatrix("experiment", experiment.getMatrix().transpose());
                    result.addParam("maxExpNameLength", String.valueOf(maxExpNameLength));
                }
                
                return createResultTree(result, info);
                
            } finally {
                if (algorithm != null) {
                    algorithm.removeAlgorithmListener(listener);
                }
                if (progress != null) {
                    progress.dispose();
                }
            }
            
        }
        
        
        private int getMaxGeneNameLength() {
            int i, num_genes, max = 0;
            String geneName;
            
            num_genes = framework.getData().getExperiment().getNumberOfGenes();
            
            for (i=0; i<num_genes; i++) {
                geneName = framework.getData().getGeneName(i);
                max = Math.max(max, geneName.length());
            }
            return max;
        }
        
        private int getMaxExpNameLength() {
            int i, num_experiments, max = 0;
            String expName;
            
            num_experiments = framework.getData().getExperiment().getNumberOfSamples();
            
            for (i=0; i<num_experiments; i++) {
                expName = framework.getData().getSampleName(i);
                max = Math.max(max, expName.length());
            }
            return max;
        }
        
        /**
         * Creates the result tree.
         */
        private DefaultMutableTreeNode createResultTree(AlgorithmData data, GeneralInfo info) {
            
            DefaultMutableTreeNode root;
            
            if(useGenes) {
                root = new DefaultMutableTreeNode("GDM - genes");
                root.add(new DefaultMutableTreeNode(new LeafInfo("Matrix View", new GDMGeneViewer(this.framework, data, info.function, displayInterval, null, 0))));
                root.add(new DefaultMutableTreeNode("Time: "+String.valueOf(info.time)+" ms"));
                root.add(new DefaultMutableTreeNode(info.function));
            }
            else {
                root = new DefaultMutableTreeNode("GDM - experiments");
                root.add(new DefaultMutableTreeNode(new LeafInfo("Matrix View", new GDMExpViewer(this.framework, data, info.function, displayInterval, null, 0))));
                root.add(new DefaultMutableTreeNode("Time: "+String.valueOf(info.time)+" ms"));
                root.add(new DefaultMutableTreeNode(info.function));
            }
            
            return root;
        }
        
        
        
        /**
         * The class to listen to a dialog and algorithm events.
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
         * The general info structure.
         */
        private class GeneralInfo {
            public long time;
            public String function;
            
        }
    }
