/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
 */
/*
 * $RCSfile: PCAGUI.java,v $
 * $Revision: 1.3 $
 * $Date: 2004-05-26 13:22:00 $
 * $Author: braisted $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.pca;

import java.awt.Frame;

import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;

import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;

import org.tigr.util.FloatMatrix;
import org.tigr.microarray.mev.cluster.gui.LeafInfo;
import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.IClusterGUI;
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.IDistanceMenu;

import org.tigr.microarray.mev.cluster.gui.impl.dialogs.Logger;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.DialogListener;

import org.tigr.microarray.mev.cluster.algorithm.Algorithm;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmData;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmEvent;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmListener;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmException;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmFactory;

import org.tigr.microarray.mev.script.scriptGUI.IScriptGUI;

public class PCAGUI implements IClusterGUI, IScriptGUI {
    
    private int mode;
    private FloatMatrix T;
    private FloatMatrix V;
    private FloatMatrix S;
    private FloatMatrix U;
    
    private Algorithm algorithm;
    private Logger logger;
    
    /**
     * Runs the calculation algorithm and returns analysis result tree.
     */
    public DefaultMutableTreeNode execute(IFramework framework) throws AlgorithmException {
        Listener listener = new Listener();
        try {
            
            PCASelectionDialog dialog = new PCASelectionDialog(framework.getFrame());
            if(dialog.showModal() == JOptionPane.CANCEL_OPTION)
                return null;
            
            if(dialog.isClusterGenesSelected())
                this.mode = 1;
            else
                this.mode = 3;
            
            algorithm = framework.getAlgorithmFactory().getAlgorithm("PCA");
            algorithm.addAlgorithmListener(listener);
            
            logger = new Logger(framework.getFrame(), "PCA Log Window", listener);
            logger.show();
            logger.append("Starting SVD calculation\n");
            
            FloatMatrix Cov;
            AlgorithmData data = new AlgorithmData();
            Experiment experiment = framework.getData().getExperiment();
            data.addMatrix("experiment", experiment.getMatrix());
            data.addParam("distance-factor", String.valueOf(1.0f));
            IDistanceMenu menu = framework.getDistanceMenu();
            data.addParam("distance-absolute", String.valueOf(menu.isAbsoluteDistance()));
            int function = menu.getDistanceFunction();
            if (function == Algorithm.DEFAULT) {
                function = Algorithm.COVARIANCE;
            }
            data.addParam("distance-function", String.valueOf(function));
            data.addParam("pca-mode", String.valueOf(mode));
            AlgorithmData result = null;
            DefaultMutableTreeNode node = null;
            long start = System.currentTimeMillis();
            switch (mode) {
                case 1: // Spots
                    data.addParam("distance-function", String.valueOf(function));
                    result = algorithm.execute(data);
                    T = result.getMatrix("T");
                    V = result.getMatrix("V");
                    S = result.getMatrix("S");
                    U = result.getMatrix("U");
                    node = new DefaultMutableTreeNode("PCA - genes");
                    break;
                case 3: // Experiments
                    result = algorithm.execute(data);
                    T = result.getMatrix("T");
                    V = result.getMatrix("V");
                    S = result.getMatrix("S");
                    U = result.getMatrix("U");
                    node = new DefaultMutableTreeNode("PCA - experiments");
                    break;
                default:
                    break;
            }
            logger.append("Creation the result viewers");
            long time = System.currentTimeMillis() - start;
            addResultNodes(framework.getFrame(), node, time, menu.getFunctionName(function), experiment);
            return node;
        } finally {
            if (algorithm != null) {
                algorithm.removeAlgorithmListener(listener);
            }
            if (logger != null) {
                logger.dispose();
            }
        }
    }
    
    
    
    public AlgorithmData getScriptParameters(IFramework framework) {
        
        PCASelectionDialog dialog = new PCASelectionDialog(framework.getFrame());
        if(dialog.showModal() == JOptionPane.CANCEL_OPTION)
            return null;
        
        if(dialog.isClusterGenesSelected())
            this.mode = 1;
        else
            this.mode = 3;
        
        FloatMatrix Cov;
        AlgorithmData data = new AlgorithmData();
        
        data.addParam("distance-factor", String.valueOf(1.0f));
        IDistanceMenu menu = framework.getDistanceMenu();
        data.addParam("distance-absolute", String.valueOf(menu.isAbsoluteDistance()));
        int function = menu.getDistanceFunction();
        if (function == Algorithm.DEFAULT) {
            function = Algorithm.COVARIANCE;
        }
        data.addParam("distance-function", String.valueOf(function));
        data.addParam("pca-mode", String.valueOf(mode));
        
        //script control parameters
        
        // alg name
        data.addParam("name", "PCA");
        
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
        Listener listener = new Listener();
        try {
            
            mode = algData.getParams().getInt("pca-mode");
            int function = algData.getParams().getInt("distance-function");
            
                 AlgorithmData result = null;
            DefaultMutableTreeNode node = null;
            algData.addMatrix("experiment", experiment.getMatrix());
            
            algorithm = framework.getAlgorithmFactory().getAlgorithm("PCA");
            algorithm.addAlgorithmListener(listener);
            
            logger = new Logger(framework.getFrame(), "PCA Log Window", listener);
            logger.show();
            logger.append("Starting SVD calculation\n");
            
            long start = System.currentTimeMillis();
            switch (mode) {
                case 1: // Spots
                    result = algorithm.execute(algData);
                    T = result.getMatrix("T");
                    V = result.getMatrix("V");
                    S = result.getMatrix("S");
                    U = result.getMatrix("U");
                    node = new DefaultMutableTreeNode("PCA - genes");
                    break;
                case 3: // Experiments
                    result = algorithm.execute(algData);
                    T = result.getMatrix("T");
                    V = result.getMatrix("V");
                    S = result.getMatrix("S");
                    U = result.getMatrix("U");
                    node = new DefaultMutableTreeNode("PCA - experiments");
                    break;
                default:
                    break;
            }
            logger.append("Creation the result viewers");
            long time = System.currentTimeMillis() - start;
            addResultNodes(framework.getFrame(), node, time, framework.getDistanceMenu().getFunctionName(function), experiment);
            return node;
        } finally {
            if (algorithm != null) {
                algorithm.removeAlgorithmListener(listener);
            }
            if (logger != null) {
                logger.dispose();
            }
        }
    }
    
    
    
    /**
     * Adds nodes into a result tree root.
     */
    private void addResultNodes(Frame frame, DefaultMutableTreeNode node, long time, String function, Experiment experiment) {
        add3DViewNode(frame, node, experiment);
        addPCPlotsNode(node);
        addPCInfoNode(node);
        addEigenNode(node);
        addGeneralInfoNode(node, time, function);
    }
    
    /**
     * Adds node with 3D viewer.
     */
    private void add3DViewNode(Frame frame, DefaultMutableTreeNode node, Experiment experiment) {
        if (U == null || U.getColumnDimension() < 3) {
            return;
        }
        PCA3DViewer pca3DViewer;
        if(mode == 1)
            pca3DViewer = new PCA3DViewer(frame, mode, U, experiment, true);
        else
            pca3DViewer = new PCA3DViewer(frame, mode, U, experiment, false);
        
        node.add(new DefaultMutableTreeNode(new LeafInfo("3D view", pca3DViewer, pca3DViewer.getJPopupMenu())));
    }
    
    /**
     * Adds nodes with plot vector viewers.
     */
    private void addPCPlotsNode(DefaultMutableTreeNode node) {
        if (S == null) {
            return;
        }
        DefaultMutableTreeNode pcNode = new DefaultMutableTreeNode("PC Plots");
        PlotVectorViewer plotVectorViewer = new PlotVectorViewer(T);
        for (int i=0; i<S.getRowDimension(); i++) {
            pcNode.add(new DefaultMutableTreeNode(new LeafInfo("Component "+(i+1), plotVectorViewer, new Integer(i))));
        }
        node.add(pcNode);
    }
    
    /**
     * Adds node with PC information.
     */
    private void addPCInfoNode(DefaultMutableTreeNode node) {
        if (S == null) {
            return;
        }
        DefaultMutableTreeNode infoNode = new DefaultMutableTreeNode("PC Information");
        PCInfoViewer pcInfoViewer = new PCInfoViewer(T);
        for (int i=0; i<S.getRowDimension(); i++) {
            infoNode.add(new DefaultMutableTreeNode(new LeafInfo("Component "+(i+1), pcInfoViewer, new Integer(i))));
        }
        node.add(infoNode);
    }
    
    /**
     * Adds node with eigen values.
     */
    private void addEigenNode(DefaultMutableTreeNode node) {
        DefaultMutableTreeNode vNode = new DefaultMutableTreeNode("Eigenvalues");
        vNode.add(new DefaultMutableTreeNode(new LeafInfo("Plot", new PlotViewer(S))));
        vNode.add(new DefaultMutableTreeNode(new LeafInfo("Values", new ValuesViewer(S))));
        node.add(vNode);
    }
    
    /**
     * Adds node with a general information.
     */
    private void addGeneralInfoNode(DefaultMutableTreeNode node, long time, String function) {
        DefaultMutableTreeNode gNode = new DefaultMutableTreeNode("General Information");
        if (S != null) {
            gNode.add(new DefaultMutableTreeNode("Components: "+S.getColumnDimension()));
        }
        gNode.add(new DefaultMutableTreeNode("Time: "+String.valueOf(time)+" ms"));
        gNode.add(new DefaultMutableTreeNode(function));
        node.add(gNode);
    }
    
    
    /**
     * The class to listen to dialog and algorithm events.
     */
    private class Listener extends DialogListener implements AlgorithmListener {
        
        public void valueChanged(AlgorithmEvent event) {
            logger.append(event.getDescription());
        }
        
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            if (command.equals("cancel-command")) {
                algorithm.abort();
                logger.dispose();
            }
        }
        
        public void windowClosing(WindowEvent e) {
            algorithm.abort();
            logger.dispose();
        }
    }
}
