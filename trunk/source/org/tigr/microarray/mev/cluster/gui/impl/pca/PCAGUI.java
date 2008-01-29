/*
Copyright @ 1999-2005, The Institute for Genomic Research (TIGR).
All rights reserved.
 */
/*
 * $RCSfile: PCAGUI.java,v $
 * $Revision: 1.6 $
 * $Date: 2007-10-22 16:11:56 $
 * $Author: raktim $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.pca;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.tree.DefaultMutableTreeNode;

import org.tigr.microarray.mev.cluster.algorithm.Algorithm;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmData;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmEvent;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmException;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmListener;
import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.IClusterGUI;
import org.tigr.microarray.mev.cluster.gui.IDistanceMenu;
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.LeafInfo;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.DialogListener;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.Logger;
import org.tigr.microarray.mev.script.scriptGUI.IScriptGUI;
import org.tigr.util.FloatMatrix;

public class PCAGUI implements IClusterGUI, IScriptGUI {
    
    private static final String ADD_NEW_3D_CMD = "add-new-3d-cmd";
    private static final String ADD_NEW_2D_CMD = "add-new-2d-cmd";
    
    private int mode, numNeibs, center;
    private FloatMatrix T;
    private FloatMatrix V;
    private FloatMatrix S;
    private FloatMatrix U;
    
    private Algorithm algorithm;
    private Logger logger;
    
    private IFramework currFramework;
    private DefaultMutableTreeNode projectionNode;
    
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
            
            // Raktim - Get Centering mode (median, mean or none) 
            if(dialog.isCenteringMedianSelected())
                this.center = 1;
            else if (dialog.isCenteringNoneSelected())
                this.center = 3;
            else 
            	this.center = 2;
            
            numNeibs = dialog.getNumNeighbors();
            
            algorithm = framework.getAlgorithmFactory().getAlgorithm("PCA");
            algorithm.addAlgorithmListener(listener);
            
            logger = new Logger(framework.getFrame(), "PCA Log Window", listener);
            logger.show();
            logger.append("Starting SVD calculation\n");
            
            currFramework = framework;
            
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
            data.addParam("centering", String.valueOf(center));
            data.addParam("numNeighbors", String.valueOf(numNeibs));
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
                    node = new DefaultMutableTreeNode("PCA - samples");
                    break;
                default:
                    break;
            }
            logger.append("Creating the result viewers");
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
        
        numNeibs = dialog.getNumNeighbors();
        
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
        data.addParam("numNeighbors", String.valueOf(numNeibs));
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
            numNeibs = algData.getParams().getInt("numNeighbors");
            int function = algData.getParams().getInt("distance-function");
            
                 AlgorithmData result = null;
            DefaultMutableTreeNode node = null;
            algData.addMatrix("experiment", experiment.getMatrix());
            
            algorithm = framework.getAlgorithmFactory().getAlgorithm("PCA");
            algorithm.addAlgorithmListener(listener);
            
            logger = new Logger(framework.getFrame(), "PCA Log Window", listener);
            logger.show();
            logger.append("Starting SVD calculation\n");
            
            currFramework = framework;
            
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
                    node = new DefaultMutableTreeNode("PCA - samples");
                    break;
                default:
                    break;
            }
            logger.append("Creating the result viewers");
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
        Listener listener = new Listener();
        
        PCADummyViewer pdv = new PCADummyViewer(U, S, mode); // needed to make the menu on the projectionNode serializable
        
        DefaultMutableTreeNode firstNode = new DefaultMutableTreeNode("Components 1, 2, 3");        
        add3DViewNode(frame, firstNode, experiment, 0, 1, 2);        
        DefaultMutableTreeNode twoDNode = new DefaultMutableTreeNode("2D Views");
        add2DViewNode(twoDNode, experiment, 0, 1, 2); 
        firstNode.add(twoDNode);        
        //projectionNode.add(firstNode);        
        
        JPopupMenu projMenu = new JPopupMenu();
        JMenuItem menuItem;
        menuItem = new JMenuItem("Add new 3-axis projections");
        menuItem.setActionCommand("add-new-3d-cmd");
        menuItem.addActionListener(listener);
        projMenu.add(menuItem);
                
        menuItem = new JMenuItem("Add new 2-axis projections");
        menuItem.setActionCommand("add-new-2d-cmd");
        menuItem.addActionListener(listener);        
        projMenu.add(menuItem);   
        
        //projectionNode = new DefaultMutableTreeNode(new LeafInfo("Projections on PC axes", projMenu));
        projectionNode = new DefaultMutableTreeNode(new LeafInfo("Projections on PC axes", pdv, pdv.getJPopupMenu()));
        
        projectionNode.add(firstNode);
        node.add(projectionNode);
        //node.add(new DefaultMutableTreeNode(new LeafInfo("Projections on PC Axes")));
        addPCPlotsNode(node);
        addPCInfoNode(node);
        addEigenNode(node);
        addGeneralInfoNode(node, time, function);
    }
    
   private void add2DViewNode(DefaultMutableTreeNode node, Experiment experiment) {
       boolean geneViewer = false;
       
       if (mode == 1) 
           geneViewer = true;
       else if (mode == 3)
           geneViewer = false;
       
       PCA2DViewer pca01 = new PCA2DViewer(experiment, U, geneViewer, 0, 1);
       PCA2DViewer pca12 = new PCA2DViewer(experiment, U, geneViewer, 1, 2);
       PCA2DViewer pca02 = new PCA2DViewer(experiment, U, geneViewer, 0, 2);
       
       node.add(new DefaultMutableTreeNode(new LeafInfo("1, 2", pca01, pca01.getJPopupMenu())));
       node.add(new DefaultMutableTreeNode(new LeafInfo("2, 3", pca12, pca12.getJPopupMenu())));
       node.add(new DefaultMutableTreeNode(new LeafInfo("1, 3", pca02, pca02.getJPopupMenu()))); 
   }   
   
   private void add2DViewNode(DefaultMutableTreeNode node, Experiment experiment, int xAxis, int yAxis, int zAxis) {
       boolean geneViewer = false;
       
       if (mode == 1) 
           geneViewer = true;
       else if (mode == 3)
           geneViewer = false;
       
       PCA2DViewer pcaxy = new PCA2DViewer(experiment, U, geneViewer, xAxis, yAxis);
       PCA2DViewer pcayz = new PCA2DViewer(experiment, U, geneViewer, yAxis, zAxis);
       PCA2DViewer pcaxz = new PCA2DViewer(experiment, U, geneViewer, xAxis, zAxis);
       
       node.add(new DefaultMutableTreeNode(new LeafInfo("" + (xAxis + 1) + ", " + (yAxis + 1), pcaxy, pcaxy.getJPopupMenu())));
       node.add(new DefaultMutableTreeNode(new LeafInfo("" + (yAxis + 1) + ", " + (zAxis + 1), pcayz, pcayz.getJPopupMenu())));
       node.add(new DefaultMutableTreeNode(new LeafInfo("" + (xAxis + 1) + ", " + (zAxis + 1), pcaxz, pcaxz.getJPopupMenu()))); 
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
            pca3DViewer = new PCA3DViewer(frame, mode, U, experiment, true, 0, 1, 2);
        else
            pca3DViewer = new PCA3DViewer(frame, mode, U, experiment, false, 0, 1, 2);
        
        node.add(new DefaultMutableTreeNode(new LeafInfo("3D view", pca3DViewer, pca3DViewer.getJPopupMenu())));
    }
    
    private void add3DViewNode(Frame frame, DefaultMutableTreeNode node, Experiment experiment, int xAxis, int yAxis, int zAxis) {
        if (U == null || U.getColumnDimension() < 3) {
            return;
        }
        PCA3DViewer pca3DViewer;
        if(mode == 1)
            pca3DViewer = new PCA3DViewer(frame, mode, U, experiment, true, xAxis, yAxis, zAxis);
        else
            pca3DViewer = new PCA3DViewer(frame, mode, U, experiment, false, xAxis, yAxis, zAxis);
        
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
        gNode.add(new DefaultMutableTreeNode("Number of neighbors for KNN imputation: " + numNeibs));
        node.add(gNode);
    }
    
    private void addNew3DNode() {
        if (S == null) {
            return;
        }
        PCAAdditional3DAxesDialog pd = new PCAAdditional3DAxesDialog((JFrame)currFramework.getFrame(), true, S.getRowDimension());
        pd.setVisible(true);
        if (!pd.isOkPressed()) {
            return;
        } else {
            int selectedX = pd.getXAxis();
            int selectedY = pd.getYAxis();
            int selectedZ = pd.getZAxis();
            
            DefaultMutableTreeNode newNode = new DefaultMutableTreeNode("Components " + (selectedX + 1) + ", " + (selectedY + 1) + ", " + (selectedZ + 1));
            add3DViewNode(currFramework.getFrame(), newNode, currFramework.getData().getExperiment(), selectedX, selectedY, selectedZ);
            DefaultMutableTreeNode twoDNode = new DefaultMutableTreeNode("2D Views");
            add2DViewNode(twoDNode, currFramework.getData().getExperiment(), selectedX, selectedY, selectedZ);
            newNode.add(twoDNode);   
            currFramework.addNode(projectionNode, newNode);
        }
    }
    
    private void addNew2DNode() {
        if (S == null) {
            return;
        }
        PCAAdditional3DAxesDialog pd = new PCAAdditional3DAxesDialog((JFrame)currFramework.getFrame(), true, S.getRowDimension());
        pd.setZBoxInvisible(true);
        pd.setVisible(true);  
        if (!pd.isOkPressed()) {
            return;
        } else {
            int selectedX = pd.getXAxis();
            int selectedY = pd.getYAxis();
            //DefaultMutableTreeNode newNode = new DefaultMutableTreeNode("Components " + (selectedX + 1) + ", " + (selectedY + 1));
            boolean geneViewer = false;            
            if (mode == 1)
                geneViewer = true;
            else if (mode == 3)
                geneViewer = false;
            
            PCA2DViewer pcaxy = new PCA2DViewer(currFramework.getData().getExperiment(), U, geneViewer, selectedX, selectedY); 
            currFramework.addNode(projectionNode, new DefaultMutableTreeNode(new LeafInfo("Components " + (selectedX + 1) + ", " + (selectedY + 1), pcaxy, pcaxy.getJPopupMenu())));
        }
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
            } else if (command.equals("add-new-3d-cmd")) {
                addNew3DNode();
            } else if (command.equals("add-new-2d-cmd")) {
                addNew2DNode();
            }
        }
        
        public void windowClosing(WindowEvent e) {
            algorithm.abort();
            logger.dispose();
        }
    }
}
