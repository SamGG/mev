/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: FOMGUI.java,v $
 * $Revision: 1.1.1.2 $
 * $Date: 2004-02-06 21:48:18 $
 * $Author: braisted $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.fom;

import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;

import org.tigr.util.FloatMatrix;
import org.tigr.microarray.mev.cluster.gui.IViewer;
import org.tigr.microarray.mev.cluster.gui.LeafInfo;
import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.IClusterGUI;
import org.tigr.microarray.mev.cluster.gui.IDistanceMenu;

import org.tigr.microarray.mev.cluster.gui.impl.dialogs.Progress;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.DialogListener;

import org.tigr.microarray.mev.cluster.algorithm.Algorithm;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmData;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmEvent;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmListener;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmException;

public class FOMGUI implements IClusterGUI {
    
    private Algorithm algorithm;
    private Progress progress;
    private boolean clusterGenes;
    
    /**
     * This method returns a tree with fom calculation results or
     * null, if analysis start was canceled.
     *
     * @param framework the reference to <code>IFramework</code> implementation,
     *        which is used to obtain an initial analysis data and parameters.
     * @throws AlgorithmException if calculation was failed.
     * @throws AbortException if calculation was canceled.
     * @see IFramework
     */
    public DefaultMutableTreeNode execute(IFramework framework) throws AlgorithmException {
        FOMInitDialog fom_dialog = new FOMInitDialog((JFrame)framework.getFrame(), true);
        fom_dialog.setVisible(true);
        /*
        if (fom_dialog.showModal() != JOptionPane.OK_OPTION) {
            return null;
        }
         */
        if (!fom_dialog.isOkPressed()) {
            return null;
        }
        int method = fom_dialog.getMethod();
        float interval = fom_dialog.getInterval();          //Cast threshold interval
        int numberOfClusters = fom_dialog.getIterations();  //KMC number of clusters (max)
        int iterations = fom_dialog.getKMCIterations();     //KMC max number of iterations/kmc run
        boolean average = fom_dialog.isAverage();
        boolean calculateMeans = fom_dialog.useMeans();
        clusterGenes = fom_dialog.isClusterGenes();
        
        Experiment experiment = framework.getData().getExperiment();
        
        
        Listener listener = new Listener();
        try {
            this.algorithm = framework.getAlgorithmFactory().getAlgorithm("FOM");
            this.algorithm.addAlgorithmListener(listener);
            
            int genes = experiment.getNumberOfGenes();
            
            this.progress = new Progress(framework.getFrame(), "Calculating FOM values", listener);
            this.progress.show();
            
            AlgorithmData data = new AlgorithmData();
            FloatMatrix matrix = experiment.getMatrix();
            if(!clusterGenes)
                matrix = matrix.transpose();
            data.addMatrix("experiment", matrix);
            data.addParam("distance-factor", String.valueOf(1.0f));
            IDistanceMenu menu = framework.getDistanceMenu();
            data.addParam("distance-absolute", String.valueOf(menu.isAbsoluteDistance()));
            int function = menu.getDistanceFunction();
            if (function == Algorithm.DEFAULT) {
                function = Algorithm.EUCLIDEAN;
            }
            data.addParam("distance-function", String.valueOf(function));
            
            data.addParam("method", String.valueOf(method));
            data.addParam("cluster-genes", String.valueOf(clusterGenes));
            data.addParam("number_of_clusters", String.valueOf(numberOfClusters));
            data.addParam("interval", String.valueOf(interval));
            data.addParam("iterations", String.valueOf(iterations));
            data.addParam("average", String.valueOf(average));
            data.addParam("calculate-means", String.valueOf(calculateMeans));
            
            
            long start = System.currentTimeMillis();
            AlgorithmData result = this.algorithm.execute(data);
            long time = System.currentTimeMillis() - start;
            // getting the results
            float[] fom_values = result.getMatrix("fom-values").A[0];
            int[] numOfCastClusters = result.getIntArray("numOfCastClusters");

            GeneralInfo info = new GeneralInfo();
            info.average = average;
            info.function = menu.getFunctionName(function);
            info.interval = interval;
            info.iterations = iterations;
            info.method = method;
            if (method == 2) {
                if(calculateMeans) {
                    info.kMeansOrKMedians = "Calculated Means";
                } else {
                    info.kMeansOrKMedians = "Calculated Medians";
                }
            }
            info.time = time;
            return createResultTree(fom_values, info, interval, numOfCastClusters);
        } finally {
            if (this.algorithm != null) {
                this.algorithm.removeAlgorithmListener(listener);
            }
            if (this.progress != null) {
                this.progress.dispose();
            }
        }
    }
    
    /**
     * Creates a result tree to be inserted into the framework analysis node.
     */
    private DefaultMutableTreeNode createResultTree(float[] fom_values, GeneralInfo info, float interval, int[] numOfCastClusters) {
        DefaultMutableTreeNode root;
        if(this.clusterGenes)
            root = new DefaultMutableTreeNode("FOM - genes");
        else
            root = new DefaultMutableTreeNode("FOM - experiments");
        addResultNodes(root, fom_values, info, interval, numOfCastClusters);
        return root;
    }
    
    /**
     * Adds result nodes into the tree root.
     */
    private void addResultNodes(DefaultMutableTreeNode root, float[] fom_values, GeneralInfo info, float interval, int[] numOfCastClusters) {
        addGraphViewer(root, fom_values, info, interval, numOfCastClusters);
        addGeneralInfo(root, info);
    }
    
    private void addGraphViewer(DefaultMutableTreeNode root, float[] fom_values, GeneralInfo info, float interval, int[] numOfCastClusters) {
        IViewer viewer = null;
        if (info.method == 2) {
            viewer = new KFOMViewer(fom_values);
            root.add(new DefaultMutableTreeNode(new LeafInfo("Graph - FOM value vs. # of Clusters", viewer)));
        }
        else {
            viewer = new CastFOMViewerA(fom_values, interval, numOfCastClusters);//null;
            IViewer viewer2 = new CastFOMViewerB(fom_values, interval, numOfCastClusters);
            root.add(new DefaultMutableTreeNode(new LeafInfo("Graph - FOM value vs. Threshold", viewer)));
            root.add(new DefaultMutableTreeNode(new LeafInfo("Graph - FOM value vs. # of Clusters", viewer2)));
        }
        
    }
    
    /**
     * Adds node with general iformation.
     */
    private void addGeneralInfo(DefaultMutableTreeNode root, GeneralInfo info) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode("General Information");
        if (info.method == 1) {
            node.add(new DefaultMutableTreeNode("Method: "+info.getMethod()));
        }
        if (info.method == 2) {
            node.add(new DefaultMutableTreeNode("Method: "+info.getMethod() + " : " + info.kMeansOrKMedians));
            //node.add(new DefaultMutableTreeNode("K-Means or K-Medians: "+ info.kMeansOrKMedians));
            node.add(new DefaultMutableTreeNode("Iterations: "+String.valueOf(info.iterations)));
        } else {
            node.add(new DefaultMutableTreeNode("Interval: "+String.valueOf(info.interval)));
        }
        node.add(new DefaultMutableTreeNode("Average: "+String.valueOf(info.average)));
        node.add(new DefaultMutableTreeNode("Time: "+String.valueOf(info.time)+" ms"));
        node.add(new DefaultMutableTreeNode(info.function));
        root.add(node);
    }
    
    /**
     * The class to listen to progress, monitor and algorithms events.
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
    
    // the general info structure
    private class GeneralInfo {
        public int method;
        public String kMeansOrKMedians;
        public float interval;
        public int iterations;
        public boolean average;
        public long time;
        public String function;
        
        public String getMethod() {
            return method == 1 ? "CAST" : "KMC";
        }
    }
}
