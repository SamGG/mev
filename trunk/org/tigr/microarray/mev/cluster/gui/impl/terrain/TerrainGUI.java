/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: TerrainGUI.java,v $
 * $Revision: 1.1.1.1 $
 * $Date: 2003-08-21 21:04:24 $
 * $Author: braisted $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.terrain;

import java.awt.event.*;
import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;

import org.tigr.util.FloatMatrix;

import org.tigr.microarray.mev.cluster.*;
import org.tigr.microarray.mev.cluster.gui.*;
import org.tigr.microarray.mev.cluster.algorithm.*;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.*;

public class TerrainGUI implements IClusterGUI {

    private Algorithm algorithm;
    private Progress progress;

    /**
     * This method returns the terrain calculation result tree or null,
     * if analysis start was canceled.
     *
     * @param framework the reference to <code>IFramework</code> implementation,
     *        which is used to obtain an initial analysis data and parameters.
     * @throws AlgorithmException if calculation was failed.
     * @throws AbortException if calculation was canceled.
     * @see IFramework
     */
    public DefaultMutableTreeNode execute(IFramework framework) throws AlgorithmException {
        TerrainInitDialog dialog = new TerrainInitDialog(framework.getFrame());
        if (dialog.showModal() != JOptionPane.OK_OPTION)
            return null; // canceled

        boolean use_genes = dialog.isGenes();
        int neighbours = dialog.getNeighbours();

        // prepare the algo data
        AlgorithmData data = new AlgorithmData();
        FloatMatrix experiment = framework.getData().getExperiment().getMatrix();
        data.addMatrix("experiment", experiment);
        data.addParam("distance-factor", String.valueOf(1.0f));
        data.addParam("distance-absolute", String.valueOf(framework.getDistanceMenu().isAbsoluteDistance()));
        int function = framework.getDistanceMenu().getDistanceFunction();
        function = (function == Algorithm.DEFAULT) ? Algorithm.PEARSONSQARED : function;
        data.addParam("distance-function", String.valueOf(function));
        data.addParam("neighbours", String.valueOf(neighbours));
        data.addParam("use-genes", String.valueOf(use_genes));
        // the result general info
        GeneralInfo gi = new GeneralInfo();
        gi.function = framework.getDistanceMenu().getFunctionName(function);
        gi.absolute = framework.getDistanceMenu().isAbsoluteDistance();
        gi.neighbours = neighbours;
        gi.isGenes = use_genes;

        Listener listener = new Listener();
        this.progress = new Progress(framework.getFrame(), "Calculating Terrain", listener);
        this.progress.show();
        try {
            this.algorithm = framework.getAlgorithmFactory().getAlgorithm("Terrain");
            this.algorithm.addAlgorithmListener(listener);

            long start = System.currentTimeMillis();
            AlgorithmData result = algorithm.execute(data);
            gi.time = System.currentTimeMillis() - start;

            this.progress.setDescription("Creating 3D View...");
            return createResultTree(result, gi, framework);
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
    private DefaultMutableTreeNode createResultTree(AlgorithmData result, GeneralInfo gi, IFramework framework) throws AlgorithmException {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Terrain");
        addTerrainView(root, result, framework, gi);
        addGeneralInfo(root, gi);
        return root;
    }

    private void addTerrainView(DefaultMutableTreeNode root, AlgorithmData result, IFramework framework, GeneralInfo gi) throws AlgorithmException {
        FloatMatrix locations = result.getMatrix("locations");
        checkLocations(locations.A);
        Cluster cluster = result.getCluster("links");
        int[][] clusters = convert2int(cluster);
        float[][] weights = convert2float(cluster);
        AlgorithmParameters params = result.getParams();
        float sigma = params.getFloat("sigma");
        TerrainViewer viewer = new TerrainViewer(gi.isGenes, framework, clusters, weights, locations.A, sigma);
        root.add(new DefaultMutableTreeNode(new LeafInfo("Map", viewer)));
    }

    private void checkLocations(float[][] locations) throws AlgorithmException {
        if (locations == null)
            throw new AlgorithmException("Locations is null.");
        for (int i=0; i<locations.length; i++)
            for (int j=0; j<locations[i].length; j++)
                if (Float.isNaN(locations[i][j]))
                    throw new AlgorithmException("Location["+i+"]["+j+"] is NaN.");
    }

    /**
     * Adds node with general iformation.
     */
    private void addGeneralInfo(DefaultMutableTreeNode root, GeneralInfo gi) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode("General Information");
        node.add(new DefaultMutableTreeNode("Time: "+String.valueOf(gi.time)+" ms"));
        node.add(new DefaultMutableTreeNode("Distance: "+gi.function));
        node.add(new DefaultMutableTreeNode("Absolute: "+gi.absolute));
        node.add(new DefaultMutableTreeNode("Neighbours: "+gi.neighbours));
        node.add(new DefaultMutableTreeNode("Data Type: "+(gi.isGenes ? "Genes" : "Experiments")));
        root.add(node);
    }

    /**
     * Converts a passed cluster into a two dimensional int array.
     */
    private int[][] convert2int(Cluster cluster) throws AlgorithmException {
        NodeList nodeList = cluster.getNodeList();
        final int nodeListSize = nodeList.getSize();
        int[][] result = new int[nodeListSize][];
        for (int i=0; i<nodeListSize; i++) {
            result[i] = nodeList.getNode(i).getFeaturesIndexes();
            if (result[i] == null) {
                throw new AlgorithmException("Cluster "+i+" does not contain indices.");
            }
        }
        return result;
    }

    private float[][] convert2float(Cluster cluster) throws AlgorithmException {
        NodeList nodeList = cluster.getNodeList();
        final int nodeListSize = nodeList.getSize();
        float[][] result = new float[nodeListSize][];
        int i=0;
        try {
            for (i=0; i<nodeListSize; i++) {
                result[i] = (float[])nodeList.getNode(i).getValues().getNodeValue(0).value;
                if (result[i] == null)
                    throw new Exception();
            }
        } catch (Exception e) {
            throw new AlgorithmException("Cluster "+i+" does not contain weights.");
        }
        return result;
    }

    /**
     * The class to listen to progress, monitor and algorithms events.
     */
    private class Listener extends DialogListener implements AlgorithmListener {

        public void valueChanged(AlgorithmEvent event) {
            switch (event.getId()) {
            case AlgorithmEvent.SET_UNITS:
                TerrainGUI.this.progress.setUnits(event.getIntValue());
                TerrainGUI.this.progress.setDescription(event.getDescription());
                break;
            case AlgorithmEvent.PROGRESS_VALUE:
                TerrainGUI.this.progress.setValue(event.getIntValue());
                TerrainGUI.this.progress.setDescription(event.getDescription());
                break;
            }
        }

        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            if (command.equals("cancel-command")) {
                TerrainGUI.this.algorithm.abort();
                TerrainGUI.this.progress.dispose();
            }
        }

        public void windowClosing(WindowEvent e) {
            TerrainGUI.this.algorithm.abort();
            TerrainGUI.this.progress.dispose();
        }
    }


    private class GeneralInfo {
        public long time;
        public String function;
        public boolean absolute;
        public int neighbours;
        public boolean isGenes;
    }
}

