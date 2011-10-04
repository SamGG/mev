/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/

package org.tigr.microarray.mev.cluster.gui.impl.clvalid;

import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;


import org.tigr.microarray.mev.annotation.AnnotationFieldConstants;
import org.tigr.microarray.mev.cluster.Cluster;
import org.tigr.microarray.mev.cluster.algorithm.AbortException;
import org.tigr.microarray.mev.cluster.algorithm.Algorithm;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmData;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmEvent;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmException;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmListener;
import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.IClusterGUI;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.IViewer;
import org.tigr.microarray.mev.cluster.gui.LeafInfo;
import org.tigr.microarray.mev.cluster.gui.helpers.CentroidUserObject;
import org.tigr.microarray.mev.cluster.gui.helpers.ClusterTableViewer;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.DialogListener;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.Progress;
import org.tigr.microarray.mev.script.scriptGUI.IScriptGUI;
import org.tigr.util.FloatMatrix;

/**
 * @author  dschlauch
 * @version
 */
public class CLVALIDGUI implements IClusterGUI, IScriptGUI {
    
    protected Algorithm algorithm;
    protected Progress progress;
    protected Experiment experiment;
    protected int[][] clusters;
    protected int[][] errorGenesArray = new int[1][];
    protected FloatMatrix means;
    protected FloatMatrix variances;
    
    protected int[][] sigGenesArrays;
    
    
    protected String[] auxTitles;
    protected Object[][] auxData;
    
    protected float[][] geneGroupMeans, geneGroupSDs;
    protected boolean drawSigTreesOnly;
    
    Vector<String> exptNamesVector;
    protected int[] groupAssignments;
    protected double falseProp;
    protected IData data;
    protected int numGroups, dataDesign, numFactorAGroups, numFactorBGroups;
    protected float alpha;
    protected String factorAName, factorBName;
    protected boolean errorGenes;
    protected boolean isHierarchicalTree;
    protected int iterations;
    
    protected ArrayList<String> geneLabels;
    protected ArrayList<String> sampleLabels;
    
    /** Creates new CLVALIDGUI */
    public CLVALIDGUI() {
    }
    
    public DefaultMutableTreeNode execute(IFramework framework, AlgorithmData algData) throws AlgorithmException {
    	return execute(framework);
    }
    
    /**
     * This method should return a tree with calculation results or
     * null, if analysis start was canceled.
     *
     * @param framework the reference to <code>IFramework</code> implementation,
     *       which is used to obtain an initial analysis data and parameters.
     * @throws AlgorithmException if calculation was failed.
     * @throws AbortException if calculation was canceled.
     * @see IFramework
     */
    public DefaultMutableTreeNode execute(IFramework framework) throws AlgorithmException {
		//Before anything check for Mac OS and throw appropriate msg
		if(sysMsg() != JOptionPane.OK_OPTION)
			return null;
		
        this.experiment = framework.getData().getExperiment();        
        this.data = framework.getData();
        exptNamesVector = new Vector<String>();
        int number_of_samples = experiment.getNumberOfSamples();
        
        int [] columnIndices = experiment.getColumnIndicesCopy(); 
        
        sampleLabels = new ArrayList<String>();
        geneLabels = new ArrayList<String>();
        for (int i = 0; i < number_of_samples; i++) {
            exptNamesVector.add(framework.getData().getFullSampleName(columnIndices[i]));
            sampleLabels.add(framework.getData().getFullSampleName(columnIndices[i])); //Raktim
        }
        
        //Use probe index as the gene labels in R
        for (int i = 0; i < experiment.getNumberOfGenes(); i++) {
        	geneLabels.add(framework.getData().getElementAnnotation(i, AnnotationFieldConstants.PROBE_ID)[0]);
        	
        }
        
//        CLVALIDInitBox CLVALIDDialog = new CLVALIDInitBox((JFrame)framework.getFrame(), true, exptNamesVector, framework.getClusterRepository(1), data.getDataType()!=IData.DATA_TYPE_RNASEQ);
//        CLVALIDDialog.setVisible(true);        
//        if (!CLVALIDDialog.isOkPressed()) return null;

        Listener listener = new Listener();
        
        try {
            algorithm = framework.getAlgorithmFactory().getAlgorithm("CLVALID");
            algorithm.addAlgorithmListener(listener);
            
            this.progress = new Progress(framework.getFrame(), "Running CLVALID Analysis", listener);
            this.progress.setIndeterminate(true);
            this.progress.setIndeterminantString("Finding Significant Genes");
            this.progress.show();
            
            AlgorithmData data = new AlgorithmData();
            
            data.addMatrix("experiment", experiment.getMatrix());

            data.addStringArray("geneLabels", geneLabels.toArray(new String[geneLabels.size()]));
            data.addStringArray("sampleLabels", sampleLabels.toArray(new String[sampleLabels.size()]));
           
            
            long start = System.currentTimeMillis();
            AlgorithmData result = algorithm.execute(data);
            long time = System.currentTimeMillis() - start;
            
            Cluster result_cluster = result.getCluster("cluster");

            GeneralInfo info = new GeneralInfo();
            info.time = time;
            return createResultTree(result_cluster, info);
            
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
    	return null;
    }
    
    public DefaultMutableTreeNode executeScript(IFramework framework, AlgorithmData algData, Experiment experiment) throws AlgorithmException {
       return null;
    }
    
    
    public static void main(String[] args){
    	
    }
    protected String getNodeTitle(int ind,int x, int y){
    	return "(node title)";
    }
    /**
     * Creates a result tree to be inserted into the framework analysis node.
     */
    protected DefaultMutableTreeNode createResultTree(Cluster result_cluster, GeneralInfo info) {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("CLVALID");
        addResultNodes(root, result_cluster, info);
        return root;
    }
    
    /**
     * Adds result nodes into the tree root.
     */
    protected void addResultNodes(DefaultMutableTreeNode root, Cluster result_cluster, GeneralInfo info) {
    	if (result_cluster==null||clusters==null)
    		return;
        addExpressionImages(root);
        addCentroidViews(root);
        addTableViews(root);
        addClusterInfo(root);
        addGeneralInfo(root, info);
    }
    
    protected void addTableViews(DefaultMutableTreeNode root) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode("Table Views");
        IViewer tabViewer = new ClusterTableViewer(this.experiment, this.clusters, this.data, this.auxTitles, this.auxData);
        int x=1; int y=2;
        for (int i=0; i<this.clusters.length; i++) {
        	node.add(new DefaultMutableTreeNode(new LeafInfo(this.getNodeTitle(i, x, y), tabViewer, new Integer(i))));
        	if (i%2==1)
        		y++;
            if (y>numGroups){
            	x++;
            	y=x+1;
            }
        }
        root.add(node);
    }
    
    /**
     * Adds nodes to display clusters data.
     */
    protected void addExpressionImages(DefaultMutableTreeNode root) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode("Expression Images");
        IViewer expViewer = new CLVALIDExperimentViewer(this.experiment, clusters, null, null, null, null, null, null, null, null, null);
        int x=1; int y=2;
        for (int i=0; i<this.clusters.length; i++) {
        	node.add(new DefaultMutableTreeNode(new LeafInfo(this.getNodeTitle(i, x, y), expViewer, new Integer(i))));
        	if (i%2==1)
        		y++;
            if (y>numGroups){
            	x++;
            	y=x+1;
            }
        }
        root.add(node);
    }
    

    
    /**
     * Adds node with cluster information.
     */
    protected void addClusterInfo(DefaultMutableTreeNode root) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode("Cluster Information");
        node.add(new DefaultMutableTreeNode(new LeafInfo("Results (#,%)", new CLVALIDInfoViewer(this.clusters, this.experiment.getNumberOfGenes(), this.dataDesign, this.numGroups))));
        root.add(node);
    }
    
    /**
     * Adds nodes to display centroid charts.
     */
    protected void addCentroidViews(DefaultMutableTreeNode root) {
        DefaultMutableTreeNode centroidNode = new DefaultMutableTreeNode("Centroid Graphs");
        DefaultMutableTreeNode expressionNode = new DefaultMutableTreeNode("Expression Graphs");
        //CLVALIDCentroidViewer centroidViewer = new CLVALIDCentroidViewer(this.experiment, clusters, geneGroupMeans, geneGroupSDs, rawPValues, adjPValues, fValues, ssGroups, ssError, dfNumValues, dfDenomValues);
        CLVALIDCentroidViewer centroidViewer = new CLVALIDCentroidViewer(this.experiment, clusters, null, null, null, null, null, null, null, null, null);
        centroidViewer.setMeans(this.means.A);
        centroidViewer.setVariances(this.variances.A);
        for (int i=0; i<this.clusters.length; i++) {
            if (i == 0) {
                centroidNode.add(new DefaultMutableTreeNode(new LeafInfo("Significant Genes ", centroidViewer, new CentroidUserObject(i, CentroidUserObject.VARIANCES_MODE))));
                expressionNode.add(new DefaultMutableTreeNode(new LeafInfo("Significant Genes ", centroidViewer, new CentroidUserObject(i, CentroidUserObject.VALUES_MODE))));
            } else if (i == 1) {
                centroidNode.add(new DefaultMutableTreeNode(new LeafInfo("Non-significant Genes ", centroidViewer, new CentroidUserObject(i, CentroidUserObject.VARIANCES_MODE))));
                expressionNode.add(new DefaultMutableTreeNode(new LeafInfo("Non-significant Genes ", centroidViewer, new CentroidUserObject(i, CentroidUserObject.VALUES_MODE))));
            }
        }
        
        CLVALIDCentroidsViewer centroidsViewer = new CLVALIDCentroidsViewer(this.experiment, clusters, geneGroupMeans, geneGroupSDs, null, null, null, null, null, null, null);

        centroidsViewer.setMeans(this.means.A);
        centroidsViewer.setVariances(this.variances.A);
        
        centroidNode.add(new DefaultMutableTreeNode(new LeafInfo("All Genes", centroidsViewer, new Integer(CentroidUserObject.VARIANCES_MODE))));
        expressionNode.add(new DefaultMutableTreeNode(new LeafInfo("All Genes", centroidsViewer, new Integer(CentroidUserObject.VALUES_MODE))));
        root.add(centroidNode);
        root.add(expressionNode);
    }
    
    
    /**
     * Adds node with general iformation.
     */
    protected void addGeneralInfo(DefaultMutableTreeNode root, GeneralInfo info) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode("General Information");
        node.add(new DefaultMutableTreeNode("Time: "+String.valueOf(info.time-1)+" ms"));
        node.add(new DefaultMutableTreeNode(info.function));
        root.add(node);
    }
    
    protected DefaultMutableTreeNode getGroupAssignmentInfo() {
        DefaultMutableTreeNode groupAssignmentInfo = new DefaultMutableTreeNode("Group assignments ");
        DefaultMutableTreeNode notInGroups = new DefaultMutableTreeNode("Not in groups");
        DefaultMutableTreeNode[] groups = new DefaultMutableTreeNode[numGroups];
        for (int i = 0; i < numGroups; i++) {
            groups[i] = new DefaultMutableTreeNode("Group " + (i+1));
            
        }
        
        for (int i = 0; i < groupAssignments.length; i++) {
            int currentGroup = groupAssignments[i];
            if (currentGroup == 0) {
                notInGroups.add(new DefaultMutableTreeNode((String)(exptNamesVector.get(i))));
            } else {
                groups[currentGroup - 1].add(new DefaultMutableTreeNode((String)(exptNamesVector.get(i))));
            }
        }
        
        for (int i = 0; i < groups.length; i++) {
            groupAssignmentInfo.add(groups[i]);
        }
        if (notInGroups.getChildCount() > 0) {
            groupAssignmentInfo.add(notInGroups);
        }
        return groupAssignmentInfo;
    }
    
    
    /**
     * The class to listen to progress, monitor and algorithms events.
     */
    protected class Listener extends DialogListener implements AlgorithmListener {
    	//EH added so AMP could extend this class
        protected Listener(){super();}
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
                case AlgorithmEvent.MONITOR_VALUE:
                    int value = event.getIntValue();
                    if (value == -1) {
                        //monitor.dispose();
                    } else {
                        //monitor.update(value);
                    }
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
    
	private int sysMsg() {
		String os = System.getProperty("os.name");
		String arch = System.getProperty("os.arch");
		String ver = System.getProperty("os.version");

		String message = "System Config:\n";
		message += "OS: " + os + " | Architecture: " + arch + " | Version: " + ver + "\n";
		message += "Please note:\n";
		if(arch.toLowerCase().contains("64") && os.toLowerCase().contains("mac")) {
			message += "You need to have 32Bit JVM as default for CLVALID\n";
			message += "Please contact MeV Support if you need help.\n";
			message += "You also need to have R 2.11.x installed for CLVALID\n";
			message += "Cancel if either is not installed. Ok to continue.";
			return JOptionPane.showConfirmDialog(null, message, "R Engine Warning", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
		}
		if(arch.toLowerCase().contains("64")) {
			message += "You need to have 32Bit JVM as default for CLVALID\n";
			message += "Please contact MeV Support if you need help.\n";
			message += "Cancel if 32 Bit JVM is not installed. Ok to continue.";
			return JOptionPane.showConfirmDialog(null, message, "R Engine Warning", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
		}
		if (os.toLowerCase().contains("mac")) {
			message += "You need to have R 2.11.x installed for CLVALID\n";
			message += "Cancel if R is not installed. Ok to continue.";
			return JOptionPane.showConfirmDialog(null, message, "R Engine Warning", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
		}
		return JOptionPane.OK_OPTION;
	}

    protected class GeneralInfo {

        public int clusters;
        public String correctionMethod;
        public float alpha;
        public long time;
        public String function;
        
        protected boolean hcl, usePerms;
        protected int hcl_method, numPerms;
        protected boolean hcl_genes;
        protected boolean hcl_samples;
    	//EH constructor added so AMP could extend
        protected GeneralInfo(){
    		super();
    	}        
        
    }
    
}
