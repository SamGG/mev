/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: LIMMAGUI.java,v $
 * $Revision: 1.10 $
 * $Date: 2006-11-07 17:27:40 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.attract;

import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;

import org.tigr.microarray.mev.annotation.AnnotationFieldConstants;
import org.tigr.microarray.mev.cluster.Cluster;
import org.tigr.microarray.mev.cluster.Node;
import org.tigr.microarray.mev.cluster.NodeList;
import org.tigr.microarray.mev.cluster.NodeValueList;
import org.tigr.microarray.mev.cluster.algorithm.AbortException;
import org.tigr.microarray.mev.cluster.algorithm.Algorithm;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmData;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmEvent;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmException;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmListener;
import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.IClusterGUI;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.IDistanceMenu;
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.IViewer;
import org.tigr.microarray.mev.cluster.gui.LeafInfo;
import org.tigr.microarray.mev.cluster.gui.helpers.BoxChartViewer;
import org.tigr.microarray.mev.cluster.gui.helpers.CentroidUserObject;
import org.tigr.microarray.mev.cluster.gui.helpers.ClusterTableViewer;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.DialogListener;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.Progress;
import org.tigr.microarray.mev.cluster.gui.impl.hcl.HCLGUI;
import org.tigr.microarray.mev.cluster.gui.impl.hcl.HCLInitDialog;
import org.tigr.microarray.mev.cluster.gui.impl.hcl.HCLTreeData;
import org.tigr.microarray.mev.cluster.gui.impl.hcl.HCLViewer;
import org.tigr.microarray.mev.script.scriptGUI.IScriptGUI;
import org.tigr.util.FloatMatrix;

/**
 * @author  dschlauch
 * @version
 */
public class ATTRACTGUI implements IClusterGUI, IScriptGUI {
    
    protected Algorithm algorithm;
    protected Progress progress;
    protected Experiment experiment;
    protected int[][] keggSynArrays;
    protected int[][] keggCorArrays;
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
    
    //ATTRACT gene & sample names
    protected ArrayList<String> geneLabels;
    protected ArrayList<String> sampleLabels;
	private Object[][] synResultMatrix;
	private Object[][] corResultMatrix;
	private String chipName;
	private IFramework framework;
	private String[] resultColumns;
	private String[] groupNames;
    
    /** Creates new ATTRACTGUI */
    public ATTRACTGUI() {
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
		this.framework = framework;
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
        
        //Raktim Use probe index as the gene labels in R
        for (int i = 0; i < experiment.getNumberOfGenes(); i++) {
        	//geneLabels.add(framework.getData().getElementAnnotation(i, AnnotationFieldConstants.PROBE_ID)[0]); //Raktim
        	geneLabels.add(String.valueOf(i));
        }
        String initialChipType = framework.getData().getChipAnnotation().getChipType();
        ATTRACTInitBox ATTRACTDialog = new ATTRACTInitBox((JFrame)framework.getFrame(), true, exptNamesVector, framework.getClusterRepository(1),initialChipType);
        ATTRACTDialog.setVisible(true);
        
        if (!ATTRACTDialog.isOkPressed()) return null;
        
        alpha = ATTRACTDialog.getAlpha();
        dataDesign = ATTRACTDialog.getExperimentalDesign();
        numGroups = ATTRACTDialog.getNumGroups();
        chipName = ATTRACTDialog.getChipName();
        groupNames = ATTRACTDialog.getGroupNames();
        
        groupAssignments=ATTRACTDialog.getGroupAssignments();
        if (groupAssignments == null)
        	return null;
        
        isHierarchicalTree = ATTRACTDialog.drawTrees();
        drawSigTreesOnly = true;
        if (isHierarchicalTree) {
            drawSigTreesOnly = ATTRACTDialog.drawSigTreesOnly();
        }      
        
        IDistanceMenu menu = framework.getDistanceMenu();
        int function = menu.getDistanceFunction();
        if (function == Algorithm.DEFAULT) {
            function = Algorithm.PEARSON;
        }
        
        // hcl init
        int hcl_method = 0;
        boolean hcl_samples = false;
        boolean hcl_genes = false;
        int hcl_function = 4;
        boolean hcl_absolute = false;
        boolean hcl_samples_ordered=false;
        boolean hcl_genes_ordered=false;
        if (isHierarchicalTree) {
            HCLInitDialog hcl_dialog = new HCLInitDialog(framework.getFrame(), menu.getFunctionName(function), menu.isAbsoluteDistance(), true);
            if (hcl_dialog.showModal() != JOptionPane.OK_OPTION) {
                return null;
            }
            hcl_method = hcl_dialog.getMethod();
            hcl_samples = hcl_dialog.isClusterExperiments();
            hcl_genes = hcl_dialog.isClusterGenes();
            hcl_function = hcl_dialog.getDistanceMetric();
            hcl_absolute = hcl_dialog.getAbsoluteSelection();
            hcl_genes_ordered = hcl_dialog.isGeneOrdering();
            hcl_samples_ordered = hcl_dialog.isSampleOrdering();
        }
        
        Listener listener = new Listener();
        
        try {
            algorithm = framework.getAlgorithmFactory().getAlgorithm("ATTRACT");
            algorithm.addAlgorithmListener(listener);
            
            this.progress = new Progress(framework.getFrame(), "ATTRACT", listener);
            this.progress.setIndeterminate(true);
            this.progress.setIndeterminantString("Attract analysis is running...");
            this.progress.show();
            
            AlgorithmData data = new AlgorithmData();
            
            data.addMatrix("experiment", experiment.getMatrix());
            data.addIntArray("group_assignments", groupAssignments);
            data.addParam("dataDesign", String.valueOf(dataDesign));
            data.addParam("numGroups", String.valueOf(numGroups));
            data.addParam("alpha", String.valueOf(alpha));
            data.addParam("chipName", chipName);
            
            data.addStringArray("geneLabels", geneLabels.toArray(new String[geneLabels.size()]));
            data.addStringArray("sampleLabels", sampleLabels.toArray(new String[sampleLabels.size()]));
            data.addStringArray("probeIDs", framework.getData().getAnnotationList(AnnotationFieldConstants.PROBE_ID));
            
            // hcl parameters
            if (isHierarchicalTree) {
                data.addParam("hierarchical-tree", String.valueOf(true));
                data.addParam("draw-sig-trees-only", String.valueOf(drawSigTreesOnly));                
                data.addParam("method-linkage", String.valueOf(hcl_method));
                data.addParam("calculate-genes", String.valueOf(hcl_genes));
                data.addParam("calculate-experiments", String.valueOf(hcl_samples));
                data.addParam("hcl-distance-function", String.valueOf(hcl_function));
                data.addParam("hcl-distance-absolute", String.valueOf(hcl_absolute));
                data.addParam("hcl-genes-ordered", String.valueOf(hcl_genes_ordered));
                data.addParam("hcl-samples-ordered", String.valueOf(hcl_samples_ordered));
            }
            
            long start = System.currentTimeMillis();
            AlgorithmData result = algorithm.execute(data);
            long time = System.currentTimeMillis() - start;
            
            // getting the results
            this.means = result.getMatrix("clusters_means");
            this.variances = result.getMatrix("clusters_variances");
            this.synResultMatrix = result.getObjectMatrix("synResultMatrix");
            this.corResultMatrix = result.getObjectMatrix("corResultMatrix");
            this.resultColumns = result.getStringArray("resultColumns");
            
            this.keggSynArrays = result.getIntMatrix("keggSynArrays");
            this.keggCorArrays = result.getIntMatrix("keggCorArrays");
            
            FloatMatrix geneGroupMeansMatrix = result.getMatrix("geneGroupMeansMatrix");
            
            FloatMatrix geneGroupSDsMatrix = result.getMatrix("geneGroupSDsMatrix");
            
            iterations = result.getParams().getInt("iterations");
            
            geneGroupMeans = new float[geneGroupMeansMatrix.getRowDimension()][geneGroupMeansMatrix.getColumnDimension()];
            geneGroupSDs = new float[geneGroupSDsMatrix.getRowDimension()][geneGroupSDsMatrix.getColumnDimension()];
            for (int i = 0; i < geneGroupMeans.length; i++) {
                for (int j = 0; j < geneGroupMeans[i].length; j++) {
                    geneGroupMeans[i][j] = geneGroupMeansMatrix.A[i][j];
                    geneGroupSDs[i][j] = geneGroupSDsMatrix.A[i][j];
                }
            }
            GeneralInfo info = new GeneralInfo();
            info.time = time;
            //ADD MORE INFO PARAMETERS HERE
            info.alpha = alpha;
            info.function = menu.getFunctionName(function);
            info.hcl = isHierarchicalTree;
            info.hcl_genes = hcl_genes;
            info.hcl_samples = hcl_samples;
            info.hcl_method = hcl_method;
            
            Vector<String> titlesVector = new Vector<String>();
            for (int i = 0; i < geneGroupMeans[0].length; i++) {
                titlesVector.add("Group " + (i+1) + " mean");
                titlesVector.add("Group " + (i+1) + " std.dev");
            }

            titlesVector.add("F-values");
            int x=1; int y=2;
            for (int i=0; i<getTotalInteractions(numGroups); i++) {

                titlesVector.add("significance-values, "+x+"vs."+y);
                titlesVector.add("adj-p-values, "+x+"vs."+y);
                titlesVector.add("log fold change, "+x+"vs."+y);
                titlesVector.add("t-statistic, "+x+"vs."+y);
                titlesVector.add("log-odds, "+x+"vs."+y);
            	
                y++;
                if (y>numGroups){
                	x++;
                	y=x+1;
                }
            }
            
            auxTitles = new String[titlesVector.size()];
            for (int i = 0; i < auxTitles.length; i++) {
                auxTitles[i] = (String)(titlesVector.get(i));
            }
            
            auxData = new Object[experiment.getNumberOfGenes()][auxTitles.length];
            for (int i = 0; i < auxData.length; i++) {
                int counter = 0;
                for (int j = 0; j < geneGroupMeans[i].length; j++) {
                    auxData[i][counter++] = new Float(geneGroupMeans[i][j]);
                    auxData[i][counter++] = new Float(geneGroupSDs[i][j]);
                }
            }
            return createResultTree(info);
            
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
    
    private int getTotalInteractions(int groups){
    	if (groups <= 1)
    		return 0;
    	else
    		return (groups-1 + getTotalInteractions(groups-1));
    }
    
    public static void main(String[] args){
    	
    }
    protected String getNodeTitle(int ind){
    	
        return (String)synResultMatrix[1][ind];
    }
    /**
     * Creates a result tree to be inserted into the framework analysis node.
     */
    protected DefaultMutableTreeNode createResultTree(GeneralInfo info) {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("ATTRACT");
        addResultNodes(root, info);
        return root;
    }
    
    /**
     * Adds result nodes into the tree root.
     */
    protected void addResultNodes(DefaultMutableTreeNode root, GeneralInfo info) {
    	String title = "Synexpression";
    	addGeneSetInfo(root, synResultMatrix, title);    	
    	boolean resultsFound = false;
    	if (keggSynArrays!=null){
	    	for (int i=0; i<keggSynArrays.length; i++){
	    		if (keggSynArrays[i].length>0){
	    			resultsFound = true;
	    			break;
	    		}
	    	}
    	}
    	if (!resultsFound){
    		addNoResultsNode(root);
    	} else {
	        addExpressionImages(root, keggSynArrays, title);
    		addExpressionCharts(root,keggSynArrays, title);
	//      addHierarchicalTrees(root, result_cluster, info);
	        addCentroidViews(root, keggSynArrays, title);
	        addTableViews(root, keggSynArrays, title);        
	//      addNotEnoughGenesFolder(root, keggSynArrays);  //Maybe don't need?
	        addClusterInfo(root, keggSynArrays, title);
    	}
        title = "Correlated Partners";
    	addGeneSetInfo(root, corResultMatrix, title);
    	resultsFound = false;
     	if (keggCorArrays!=null){
 	    	for (int i=0; i<keggCorArrays.length; i++){
 	    		if (keggCorArrays[i].length>0){
 	    			resultsFound = true;
 	    			break;
 	    		}
 	    	}
     	}
    	if (!resultsFound){
    		addNoResultsNode(root);
    	} else {
	        addExpressionImages(root, keggCorArrays, title);
	        addExpressionCharts(root, keggCorArrays, title);
	//      addHierarchicalTrees(root, result_cluster, info);
	        addCentroidViews(root, keggCorArrays, title);
	        addTableViews(root, keggCorArrays, title);  
	        addClusterInfo(root, keggCorArrays, title);
    	}
        
        addNotEnoughGenesFolder(root, keggCorArrays);
        addGeneralInfo(root, info);
    }
	private void addExpressionCharts(DefaultMutableTreeNode root, int[][] keggArrays, String title) {	
		ArrayList<Integer>[] groups = new ArrayList[numGroups];

		for (int i=0; i<groups.length; i++){
			groups[i] = new ArrayList();
		}
		for (int i=0; i<groupAssignments.length; i++){
			groups[groupAssignments[i]-1].add(i);
		}
		int[][] sampleGroups = new int[numGroups][];
		for (int i=0; i<sampleGroups.length; i++){
			sampleGroups[i] = new int[groups[i].size()];
			for (int j=0; j<sampleGroups[i].length; j++){
				sampleGroups[i][j] = groups[i].get(j);
			}
		}
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(title + " Expression Charts");
        for (int i=0; i<keggArrays.length; i++) {
        	if (keggArrays[i].length>0){
        		IViewer echartViewer = new BoxChartViewer(framework, sampleGroups, keggArrays[i], this.getNodeTitle(i), groupNames);
        		node.add(new DefaultMutableTreeNode(new LeafInfo(this.getNodeTitle(i), echartViewer, new Integer(i))));
        	}
        }
        root.add(node);	       
		
	}

    /**
     * Adds nodes to display clusters data.
     */
    protected void addExpressionImages(DefaultMutableTreeNode root, int[][] keggArrays, String title) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(title+ " Expression Images");
        IViewer expViewer = new ATTRACTExperimentViewer(this.experiment, keggArrays, null, null, null, null, null, null, null, null, null);
        for (int i=0; i<keggArrays.length; i++) {
        	if (keggArrays[i].length>0)
        		node.add(new DefaultMutableTreeNode(new LeafInfo(this.getNodeTitle(i), expViewer, new Integer(i))));
        }
        root.add(node);
    }
    
	private void addNoResultsNode(DefaultMutableTreeNode root) {
		root.add(new DefaultMutableTreeNode("No Results Found"));		
	}

	private void addNotEnoughGenesFolder(DefaultMutableTreeNode root, int[][] keggArrays) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode("Not Enough Significant Genes");
        for (int i=0; i<keggArrays.length; i++) {
        	if (keggArrays[i].length==0)
        		node.add(new DefaultMutableTreeNode(new LeafInfo(this.getNodeTitle(i), null, new Integer(i))));
        }
        root.add(node);		
	}

	private void addGeneSetInfo(DefaultMutableTreeNode root, Object[][] matrix, String title) {
	      Object[][] results = new Object[matrix[0].length][matrix.length];
	      for (int i=0; i<results.length; i++){
	    	  for (int j=0; j<results[0].length; j++){
	    		  results[i][j] = matrix[j][i];
	    	  }
	      }
	      
	      IViewer tabViewer1 = new ATTRACTResultTable(results, root, framework, resultColumns, title);
	      root.add(new DefaultMutableTreeNode(new LeafInfo(title+" Results Table", tabViewer1, new Integer(0))));
	      
			
	}
    protected void addTableViews(DefaultMutableTreeNode root, int[][] keggArrays, String title) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(title + " Table Views");
        IViewer tabViewer = new ClusterTableViewer(this.experiment, keggArrays, this.data, this.auxTitles, this.auxData);
        for (int i=0; i<keggArrays.length; i++) {
        	if (keggArrays[i].length>0)
        		node.add(new DefaultMutableTreeNode(new LeafInfo(this.getNodeTitle(i), tabViewer, new Integer(i))));
        }
        root.add(node);
    }
    
    
    /**
     * Adds nodes to display hierarchical trees.
     */
    protected void addHierarchicalTrees(DefaultMutableTreeNode root, Cluster result_cluster, GeneralInfo info) {
        if (!info.hcl) {
            return;
        }
        DefaultMutableTreeNode node = new DefaultMutableTreeNode("Hierarchical Trees");
        NodeList nodeList = result_cluster.getNodeList();
        if (!drawSigTreesOnly) {        
            for (int i=0; i<nodeList.getSize(); i++) {
                if (i < nodeList.getSize() - 1 ) {
                    node.add(new DefaultMutableTreeNode(new LeafInfo("Significant Genes ", createHCLViewer(nodeList.getNode(i), info))));
                } else if (i == nodeList.getSize() - 1) {
                    node.add(new DefaultMutableTreeNode(new LeafInfo("Non-significant Genes ", createHCLViewer(nodeList.getNode(i), info))));
                }
            }
        } else {
            node.add(new DefaultMutableTreeNode(new LeafInfo("Significant Genes ", createHCLViewer(nodeList.getNode(0), info))));            
        }
        root.add(node);
    }
    
    /**
     * Creates an <code>HCLViewer</code>.
     */
    protected IViewer createHCLViewer(Node clusterNode, GeneralInfo info) {
        HCLTreeData genes_result = info.hcl_genes ? getResult(clusterNode, 0) : null;
        HCLTreeData samples_result = info.hcl_samples ? getResult(clusterNode, info.hcl_genes ? 4 : 0) : null;
        return new HCLViewer(this.experiment, clusterNode.getFeaturesIndexes(), genes_result, samples_result);
    }
    
    /**
     * Returns a hcl tree data from the specified cluster node.
     */
    protected HCLTreeData getResult(Node clusterNode, int pos) {
        HCLTreeData data = new HCLTreeData();
        NodeValueList valueList = clusterNode.getValues();
        data.child_1_array = (int[])valueList.getNodeValue(pos).value;
        data.child_2_array = (int[])valueList.getNodeValue(pos+1).value;
        data.node_order = (int[])valueList.getNodeValue(pos+2).value;
        data.height = (float[])valueList.getNodeValue(pos+3).value;
        return data;
    }
    
    /**
     * Adds node with cluster information.
     */
    protected void addClusterInfo(DefaultMutableTreeNode root, int[][] keggArrays, String title) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(title + " Pathway Information");
        node.add(new DefaultMutableTreeNode(new LeafInfo("Results", new ATTRACTInfoViewer(keggArrays, this.experiment.getNumberOfGenes(), this.dataDesign, this.numGroups, synResultMatrix[1]))));
        root.add(node);
    }
    
    /**
     * Adds nodes to display centroid charts.
     */
    protected void addCentroidViews(DefaultMutableTreeNode root, int[][] keggArrays, String title) {
        DefaultMutableTreeNode centroidNode = new DefaultMutableTreeNode(title + " Centroid Graphs");
        DefaultMutableTreeNode expressionNode = new DefaultMutableTreeNode(title + " Expression Graphs");
        ATTRACTCentroidViewer centroidViewer = new ATTRACTCentroidViewer(this.experiment, keggArrays, null, null, null, null, null, null, null, null, null);
        centroidViewer.setMeans(this.means.A);
        centroidViewer.setVariances(this.variances.A);
        for (int i=0; i<keggArrays.length; i++) {
        	if (keggArrays[i].length>0){
        		centroidNode.add(new DefaultMutableTreeNode(new LeafInfo(this.getNodeTitle(i), centroidViewer, new CentroidUserObject(i, CentroidUserObject.VARIANCES_MODE))));
        		expressionNode.add(new DefaultMutableTreeNode(new LeafInfo(this.getNodeTitle(i), centroidViewer, new CentroidUserObject(i, CentroidUserObject.VALUES_MODE))));
        	}
        }
        root.add(centroidNode);
        root.add(expressionNode);
    }
    
    
    /**
     * Adds node with general iformation.
     */
    protected void addGeneralInfo(DefaultMutableTreeNode root, GeneralInfo info) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode("General Information");
        if (this.isHierarchicalTree)
        	node.add(new DefaultMutableTreeNode("HCL: "+info.getMethodName()));

    	node.add(getGroupAssignmentInfo());
        node.add(new DefaultMutableTreeNode("Number of groups: "+this.numGroups));
        node.add(new DefaultMutableTreeNode("LIMMA cutoff, alpha: "+alpha));
        node.add(new DefaultMutableTreeNode("Bioconductor annotation: "+this.chipName));
        node.add(new DefaultMutableTreeNode("Execution Time: "+String.valueOf(info.time-1)+" ms"));
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
			message += "You need to have 32Bit JVM as default for ATTRACT\n";
			message += "Please contact MeV Support if you need help.\n";
			message += "You also need to have R 2.11.x installed for ATTRACT\n";
			message += "Cancel if either is not installed. Ok to continue.";
			return JOptionPane.showConfirmDialog(null, message, "R Engine Warning", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
		}
		if(arch.toLowerCase().contains("64")) {
			message += "You need to have 32Bit JVM as default for ATTRACT\n";
			message += "Please contact MeV Support if you need help.\n";
			message += "Cancel if 32 Bit JVM is not installed. Ok to continue.";
			return JOptionPane.showConfirmDialog(null, message, "R Engine Warning", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
		}
		if (os.toLowerCase().contains("mac")) {
			message += "You need to have R 2.11.x installed for ATTRACT\n";
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
        public String getMethodName() {
            return hcl ? HCLGUI.GeneralInfo.getMethodName(hcl_method) : "no linkage";
        }
        
    }
    
}
