/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * COAGUI.java
 *
 * Created on September 16, 2004, 11:22 AM
 */

package org.tigr.microarray.mev.cluster.gui.impl.coa;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.tree.DefaultMutableTreeNode;

import org.tigr.microarray.mev.cluster.algorithm.AbortException;
import org.tigr.microarray.mev.cluster.algorithm.Algorithm;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmData;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmEvent;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmException;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmListener;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmParameters;
import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.IClusterGUI;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.IViewer;
import org.tigr.microarray.mev.cluster.gui.LeafInfo;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.DialogListener;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.Logger;
import org.tigr.microarray.mev.script.scriptGUI.IScriptGUI;
import org.tigr.util.FloatMatrix;

/**
 *
 * @author  nbhagaba
 */
public class COAGUI  implements IClusterGUI, IScriptGUI {
    
    private static final String ADD_NEW_3D_CMD = "add-new-3d-cmd";
    private static final String ADD_NEW_2D_CMD = "add-new-2d-cmd";    
    
    public static final int GENES = 1;
    public static final int EXPTS = 2;
    public static final int BOTH = 3;
    
    private Algorithm algorithm;
    private Logger logger;    
    //private Progress progress;
    //private Monitor monitor;
    private IData data;
    
    private Experiment experiment;
    private FloatMatrix geneUMatrix, exptUMatrix, lambdaValues;
    
    private IFramework currFramework;
    private DefaultMutableTreeNode projectionNode;    
    
    private double[] lambdaArray, inertiaVals, cumulativeInertiaVals;
    //private int[][] clusters;
    //private FloatMatrix means;
    //private FloatMatrix variances;    
    
    /** Creates a new instance of COAGUI */
    public COAGUI() {
    }
    
    /** This method should return a tree with calculation results or
     * null, if analysis start was canceled.
     *
     * @param framework the reference to <code>IFramework</code> implementation,
     *        which is used to obtain an initial analysis data and parameters.
     * @throws AlgorithmException if calculation was failed.
     * @throws AbortException if calculation was canceled.
     * @see IFramework
     */
    public DefaultMutableTreeNode execute(IFramework framework) throws AlgorithmException {
        this.experiment = framework.getData().getExperiment();
        this.data = framework.getData();        
        int number_of_samples = experiment.getNumberOfSamples();
        int number_of_genes = experiment.getNumberOfGenes(); 
        
        COAInitDialog cDialog = new COAInitDialog((JFrame)framework.getFrame(), true);
        cDialog.setVisible(true);
        
        if (!cDialog.isOkPressed()) return null;  
        
        int numNeibs = cDialog.getNumNeighbors();
        
        Listener listener = new Listener(); 
        try {
            algorithm = framework.getAlgorithmFactory().getAlgorithm("COA");
            algorithm.addAlgorithmListener(listener);
            
            //this.progress = new Progress(framework.getFrame(), "Performing correspondence analysis", listener);
            //this.progress.show();
            logger = new Logger(framework.getFrame(), "COA Log Window", listener);
            logger.show();
            logger.append("Starting SVD calculation\n");  
            
            currFramework = framework;            
            
            AlgorithmData data = new AlgorithmData();
            
            data.addMatrix("experiment", experiment.getMatrix()); 
            data.addParam("numNeighbors", String.valueOf(numNeibs));
            
            long start = System.currentTimeMillis();
            AlgorithmData result = algorithm.execute(data);
            long time = System.currentTimeMillis() - start;  
            
            geneUMatrix = result.getMatrix("gene");
            exptUMatrix = result.getMatrix("expt");
            lambdaValues = result.getMatrix("lambdaValues");
            calculateInertiaVals();
            //System.out.println("geneUMatrix: rowDim = " + geneUMatrix.getRowDimension() + ", colDim = " + geneUMatrix.getColumnDimension());
            //System.out.println("exptUMatrix: rowDim = " + exptUMatrix.getRowDimension() + ", colDim = " + exptUMatrix.getColumnDimension());            
            
            DefaultMutableTreeNode node = new DefaultMutableTreeNode("COA"); 
            addResultNodes(framework.getFrame(), node, time, experiment);
            return node;            
            
        }  finally {
            if (algorithm != null) {
                algorithm.removeAlgorithmListener(listener);
            }
            if (logger != null) {
                logger.dispose();
            }
        }
        
        //return null; // for now
    }
    
    
    private void calculateInertiaVals() {
        lambdaArray = new double[lambdaValues.getRowDimension()];
        for (int i = 0; i < lambdaArray.length; i++) {
            lambdaArray[i] = (double)(lambdaValues.get(i,0));
        }
        
        double totalLambdaSquared = 0d;
        for (int i = 0; i < lambdaArray.length; i++) {
            totalLambdaSquared += Math.pow(lambdaArray[i], 2);
        }
        
        inertiaVals = new double[lambdaArray.length];
        cumulativeInertiaVals = new double[lambdaArray.length];
        for (int i = 0; i < inertiaVals.length; i++) {
            inertiaVals[i] = Math.pow(lambdaArray[i], 2)*100d/totalLambdaSquared;
        }
        
        cumulativeInertiaVals[0] = inertiaVals[0];
        
        for (int i = 1; i < cumulativeInertiaVals.length; i++) {
            double currCumul = 0d;
            for (int j = 0; j <= i; j++){
                currCumul += inertiaVals[j];
            }
            cumulativeInertiaVals[i] = currCumul;
        }
    }
    
   private void addResultNodes(Frame frame, DefaultMutableTreeNode node, long time, Experiment experiment) {
       Listener listener = new Listener();
       
       COADummyViewer cdv = new COADummyViewer(geneUMatrix, exptUMatrix); // needed to make the menu on the projectionNode serializable
       
       DefaultMutableTreeNode firstNode = new DefaultMutableTreeNode("Components 1, 2, 3");
       //DefaultMutableTreeNode firstNode = new DefaultMutableTreeNode(new LeafInfo("Comp"));
       DefaultMutableTreeNode threeDNode = new DefaultMutableTreeNode("3D Views");
       DefaultMutableTreeNode twoDNode = new DefaultMutableTreeNode("2D Views");
       add3DViewNode(frame, threeDNode, experiment, 0, 1, 2); 
       add2DViewNode(twoDNode, experiment, 0, 1, 2);
       
       firstNode.add(threeDNode);
       firstNode.add(twoDNode);
       
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
       //DefaultMutableTreeNode subProjNode = new DefaultMutableTreeNode(new LeafInfo("New projections", cdv, cdv.getJPopupMenu()));
        projectionNode = new DefaultMutableTreeNode(new LeafInfo("Projections on COA axes", cdv, cdv.getJPopupMenu()));
       
       //projectionNode.add(subProjNode);
       projectionNode.add(firstNode);
       node.add(projectionNode);
       addCOAInertiaViewer(node);
   }
   
   
    private void addCOAInertiaViewer(DefaultMutableTreeNode root) {
        IViewer inertiaValsViewer = new COAInertiaValsViewer(inertiaVals, cumulativeInertiaVals);
        root.add(new DefaultMutableTreeNode(new LeafInfo("Inertia values", inertiaValsViewer)));
    }   
   
   /**
     * Adds node with 3D viewer.
     */
   private void add3DViewNode(Frame frame, DefaultMutableTreeNode node, Experiment experiment) {
       COA3DViewer coa3DGeneViewer, coa3DExptViewer, coa3DBothViewer;
       coa3DGeneViewer = new COA3DViewer(frame, geneUMatrix, experiment, GENES, 0, 1, 2);
       coa3DExptViewer = new COA3DViewer(frame, exptUMatrix, experiment, EXPTS, 0, 1, 2);
       coa3DBothViewer = new COA3DViewer(frame, geneUMatrix, exptUMatrix, experiment, BOTH, 0, 1, 2);
       
       node.add(new DefaultMutableTreeNode(new LeafInfo("3D view - genes", coa3DGeneViewer, coa3DGeneViewer.getJPopupMenu())));
       node.add(new DefaultMutableTreeNode(new LeafInfo("3D view - expts", coa3DExptViewer, coa3DExptViewer.getJPopupMenu())));
       node.add(new DefaultMutableTreeNode(new LeafInfo("3D view - both", coa3DBothViewer, coa3DBothViewer.getJPopupMenu())));
   } 
   
   private void add3DViewNode(Frame frame, DefaultMutableTreeNode node, Experiment experiment, int xAxis, int yAxis, int zAxis) {
       COA3DViewer coa3DGeneViewer, coa3DExptViewer, coa3DBothViewer;
       coa3DGeneViewer = new COA3DViewer(frame, geneUMatrix, experiment, GENES, xAxis, yAxis, zAxis);
       coa3DExptViewer = new COA3DViewer(frame, exptUMatrix, experiment, EXPTS, xAxis, yAxis, zAxis);
       coa3DBothViewer = new COA3DViewer(frame, geneUMatrix, exptUMatrix, experiment, BOTH, xAxis, yAxis, zAxis);
       
       node.add(new DefaultMutableTreeNode(new LeafInfo("3D view - genes", coa3DGeneViewer, coa3DGeneViewer.getJPopupMenu())));
       node.add(new DefaultMutableTreeNode(new LeafInfo("3D view - expts", coa3DExptViewer, coa3DExptViewer.getJPopupMenu())));
       node.add(new DefaultMutableTreeNode(new LeafInfo("3D view - both", coa3DBothViewer, coa3DBothViewer.getJPopupMenu())));
   }   
   
   
   private void add2DViewNode(DefaultMutableTreeNode node, Experiment experiment) {
       COA2DViewer geneViewer, exptViewer, bothViewer;
       DefaultMutableTreeNode genes = new DefaultMutableTreeNode("2D views - genes");
       DefaultMutableTreeNode expts = new DefaultMutableTreeNode("2D views - expts");
       DefaultMutableTreeNode both = new DefaultMutableTreeNode("2D views - both");
       
       COA2DViewer coa01 = new COA2DViewer(experiment, geneUMatrix, COAGUI.GENES, 0, 1);
       COA2DViewer coa12 = new COA2DViewer(experiment, geneUMatrix, COAGUI.GENES, 1, 2);
       COA2DViewer coa02 = new COA2DViewer(experiment, geneUMatrix, COAGUI.GENES, 0, 2);
       
       genes.add(new DefaultMutableTreeNode(new LeafInfo("1, 2", coa01, coa01.getJPopupMenu())));
       genes.add(new DefaultMutableTreeNode(new LeafInfo("2, 3", coa12, coa12.getJPopupMenu())));
       genes.add(new DefaultMutableTreeNode(new LeafInfo("1, 3", coa02, coa02.getJPopupMenu())));
       
       COA2DViewer coaExpts01 = new COA2DViewer(experiment, exptUMatrix, COAGUI.EXPTS, 0, 1);
       COA2DViewer coaExpts12 = new COA2DViewer(experiment, exptUMatrix, COAGUI.EXPTS, 1, 2);
       COA2DViewer coaExpts02 = new COA2DViewer(experiment, exptUMatrix, COAGUI.EXPTS, 0, 2);
       
       expts.add(new DefaultMutableTreeNode(new LeafInfo("1, 2", coaExpts01, coaExpts01.getJPopupMenu())));    
       expts.add(new DefaultMutableTreeNode(new LeafInfo("2, 3", coaExpts12, coaExpts12.getJPopupMenu()))); 
       expts.add(new DefaultMutableTreeNode(new LeafInfo("1, 3", coaExpts02, coaExpts02.getJPopupMenu()))); 
       
       COA2DViewer both01 = new COA2DViewer(experiment, geneUMatrix, exptUMatrix, COAGUI.BOTH, 0, 1);
       COA2DViewer both12 = new COA2DViewer(experiment, geneUMatrix, exptUMatrix, COAGUI.BOTH, 1, 2);
       COA2DViewer both02 = new COA2DViewer(experiment, geneUMatrix, exptUMatrix, COAGUI.BOTH, 0, 2);
       
       both.add(new DefaultMutableTreeNode(new LeafInfo("1, 2", both01, both01.getJPopupMenu())));
       both.add(new DefaultMutableTreeNode(new LeafInfo("2, 3", both12, both12.getJPopupMenu())));       
       both.add(new DefaultMutableTreeNode(new LeafInfo("1, 3", both02, both02.getJPopupMenu())));    
       
       node.add(genes);
       node.add(expts);
       node.add(both);
   }
   
   private void add2DViewNode(DefaultMutableTreeNode node, Experiment experiment, int xAxis, int yAxis, int zAxis) {
       COA2DViewer geneViewer, exptViewer, bothViewer;
       DefaultMutableTreeNode genes = new DefaultMutableTreeNode("2D views - genes");
       DefaultMutableTreeNode expts = new DefaultMutableTreeNode("2D views - expts");
       DefaultMutableTreeNode both = new DefaultMutableTreeNode("2D views - both");
       
       COA2DViewer coaxy = new COA2DViewer(experiment, geneUMatrix, COAGUI.GENES, xAxis, yAxis);
       COA2DViewer coayz = new COA2DViewer(experiment, geneUMatrix, COAGUI.GENES, yAxis, zAxis);
       COA2DViewer coaxz = new COA2DViewer(experiment, geneUMatrix, COAGUI.GENES, xAxis, zAxis);
       
       genes.add(new DefaultMutableTreeNode(new LeafInfo("" + (xAxis + 1) + ", " + (yAxis + 1), coaxy, coaxy.getJPopupMenu())));
       genes.add(new DefaultMutableTreeNode(new LeafInfo("" + (yAxis + 1) + ", " + (zAxis + 1), coayz, coayz.getJPopupMenu())));
       genes.add(new DefaultMutableTreeNode(new LeafInfo("" + (xAxis + 1) + ", " + (zAxis + 1), coaxz, coaxz.getJPopupMenu())));
       
       COA2DViewer coaExptsxy = new COA2DViewer(experiment, exptUMatrix, COAGUI.EXPTS, xAxis, yAxis);
       COA2DViewer coaExptsyz = new COA2DViewer(experiment, exptUMatrix, COAGUI.EXPTS, yAxis, zAxis);
       COA2DViewer coaExptsxz = new COA2DViewer(experiment, exptUMatrix, COAGUI.EXPTS, xAxis, zAxis);
       
       expts.add(new DefaultMutableTreeNode(new LeafInfo("" + (xAxis + 1) + ", " + (yAxis + 1), coaExptsxy, coaExptsxy.getJPopupMenu())));    
       expts.add(new DefaultMutableTreeNode(new LeafInfo("" + (yAxis + 1) + ", " + (zAxis + 1), coaExptsyz, coaExptsyz.getJPopupMenu()))); 
       expts.add(new DefaultMutableTreeNode(new LeafInfo("" + (xAxis + 1) + ", " + (zAxis + 1), coaExptsxz, coaExptsxz.getJPopupMenu()))); 
       
       COA2DViewer bothxy = new COA2DViewer(experiment, geneUMatrix, exptUMatrix, COAGUI.BOTH, xAxis, yAxis);
       COA2DViewer bothyz = new COA2DViewer(experiment, geneUMatrix, exptUMatrix, COAGUI.BOTH, yAxis, zAxis);
       COA2DViewer bothxz = new COA2DViewer(experiment, geneUMatrix, exptUMatrix, COAGUI.BOTH, xAxis, zAxis);
       
       both.add(new DefaultMutableTreeNode(new LeafInfo("" + (xAxis + 1) + ", " + (yAxis + 1), bothxy, bothxy.getJPopupMenu())));
       both.add(new DefaultMutableTreeNode(new LeafInfo("" + (yAxis + 1) + ", " + (zAxis + 1), bothyz, bothyz.getJPopupMenu())));       
       both.add(new DefaultMutableTreeNode(new LeafInfo("" + (xAxis + 1) + ", " + (zAxis + 1), bothxz, bothxz.getJPopupMenu())));    
       
       node.add(genes);
       node.add(expts);
       node.add(both);
   }   
   
   private float[] getFloatArray(FloatMatrix matrix, int column) {
       float[] array = new float[matrix.getRowDimension()];
       for (int i = 0; i < array.length; i++) {
           array[i] = matrix.A[i][column];
       }
       return array;
   }
   
   public void addNew3DNode() {
        
       COAAdditional3DAxesDialog pd = new COAAdditional3DAxesDialog((JFrame)currFramework.getFrame(), true, geneUMatrix.getColumnDimension());
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
   
   public void addNew2DNode() {
        COAAdditional3DAxesDialog pd = new COAAdditional3DAxesDialog((JFrame)currFramework.getFrame(), true, geneUMatrix.getColumnDimension());
        pd.setZBoxInvisible(true);
        pd.setVisible(true);  
        if (!pd.isOkPressed()) {
            return;
        } else {
            int selectedX = pd.getXAxis();
            int selectedY = pd.getYAxis();  
            
            DefaultMutableTreeNode newNode = new DefaultMutableTreeNode("Components " + (selectedX + 1) + ", " + (selectedY + 1));   
            COA2DViewer coaGenexy = new COA2DViewer(experiment, geneUMatrix, COAGUI.GENES, selectedX, selectedY);
            COA2DViewer coaExptxy = new COA2DViewer(experiment, exptUMatrix, COAGUI.EXPTS, selectedX, selectedY);
            COA2DViewer coaBothxy = new COA2DViewer(experiment, geneUMatrix, exptUMatrix, COAGUI.BOTH, selectedX, selectedY);
            newNode.add(new DefaultMutableTreeNode(new LeafInfo("Genes", coaGenexy, coaGenexy.getJPopupMenu())));
            newNode.add(new DefaultMutableTreeNode(new LeafInfo("Expts", coaExptxy, coaExptxy.getJPopupMenu())));
            newNode.add(new DefaultMutableTreeNode(new LeafInfo("Both", coaBothxy, coaBothxy.getJPopupMenu())));
            currFramework.addNode(projectionNode, newNode);
            //add2DViewNode(newNode, );
        }
   }
   
    /** Excutes algorihtm provided an experiment, parameters, and the framework.
     * @param framework <code>IFramework</code> object.
     * @param algData Holds parameters
     * @param experiment <code>Experiment</code> object wraps <code>FloatMatrix</code>.
     *
     * @throws AlgorithmException
     * @return
     */
    public DefaultMutableTreeNode executeScript(IFramework framework, AlgorithmData algData, Experiment experiment) throws AlgorithmException {
        Listener listener = new Listener();     
        try {
            algorithm = framework.getAlgorithmFactory().getAlgorithm("COA");
            algorithm.addAlgorithmListener(listener);
            
            //this.progress = new Progress(framework.getFrame(), "Performing correspondence analysis", listener);
            //this.progress.show();
            logger = new Logger(framework.getFrame(), "COA Log Window", listener);
            logger.show();
            logger.append("Starting SVD calculation\n");  
            
            currFramework = framework;            
            
            //AlgorithmData data = new AlgorithmData();
            AlgorithmParameters params = algData.getParams();            
            int numNeibs = params.getInt("numNeighbors");           
            
            algData.addMatrix("experiment", experiment.getMatrix()); 
            
            long start = System.currentTimeMillis();
            AlgorithmData result = algorithm.execute(algData);
            long time = System.currentTimeMillis() - start;  
            
            geneUMatrix = result.getMatrix("gene");
            exptUMatrix = result.getMatrix("expt");
            lambdaValues = result.getMatrix("lambdaValues");
            calculateInertiaVals();
            //System.out.println("geneUMatrix: rowDim = " + geneUMatrix.getRowDimension() + ", colDim = " + geneUMatrix.getColumnDimension());
            //System.out.println("exptUMatrix: rowDim = " + exptUMatrix.getRowDimension() + ", colDim = " + exptUMatrix.getColumnDimension());            
            
            DefaultMutableTreeNode node = new DefaultMutableTreeNode("COA"); 
            addResultNodes(framework.getFrame(), node, time, experiment);
            return node;            
            
        }  finally {
            if (algorithm != null) {
                algorithm.removeAlgorithmListener(listener);
            }
            if (logger != null) {
                logger.dispose();
            }   
        }
        //return null; // for now
    }
    
    /** Returns selected parameters for building a script.
     * @param framework Framework object to provide IData object.
     * @return
     */
    public AlgorithmData getScriptParameters(IFramework framework) {
        this.experiment = framework.getData().getExperiment();
        this.data = framework.getData();        
        int number_of_samples = experiment.getNumberOfSamples();
        int number_of_genes = experiment.getNumberOfGenes(); 
        
        //currFramework = framework;
        
        COAInitDialog cDialog = new COAInitDialog((JFrame)framework.getFrame(), true);
        cDialog.setVisible(true);
        
        if (!cDialog.isOkPressed()) return null;  
        
        int numNeibs = cDialog.getNumNeighbors();  
        
        AlgorithmData data = new AlgorithmData();

        data.addParam("numNeighbors", String.valueOf(numNeibs)); 
        
        // alg name
        data.addParam("name", "COA");
        
        // alg type
        data.addParam("alg-type", "data-visualization");
        
        // output class
        data.addParam("output-class", "single-output");
        
        //output nodes
        String [] outputNodes = new String[1];
        outputNodes[0] = "Data Visualization";
        data.addStringArray("output-nodes", outputNodes);
        
        return data;        
        //return null; //for now
    }
    
    /**
     * The class to listen to progress, monitor and algorithms events.
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
                //monitor.dispose();
            } else if (command.equals("add-new-3d-cmd")) {
                addNew3DNode();
            } else if (command.equals("add-new-2d-cmd")) {
                addNew2DNode();
            }
        }
        
        public void windowClosing(WindowEvent e) {
            algorithm.abort();
            logger.dispose();
            //monitor.dispose();
        }
    }    
    
}
