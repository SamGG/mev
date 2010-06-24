/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/

package org.tigr.microarray.mev.cluster.gui.impl.globanc;

import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;

import org.tigr.microarray.mev.annotation.AnnotationFieldConstants;
import org.tigr.microarray.mev.cluster.Cluster;
import org.tigr.microarray.mev.cluster.NodeList;
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
import org.tigr.microarray.mev.cluster.gui.IDistanceMenu;
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
public class GLOBANCGUI implements IClusterGUI, IScriptGUI {
    
    protected Algorithm algorithm;
    protected Progress progress;
    protected Experiment experiment;
    protected int[][] geneLists;
    protected int[][] errorGenesArray = new int[1][];
    protected FloatMatrix means;
    protected FloatMatrix variances;
    
    protected int[][] sigGenesArrays;
    
    
    protected String[] auxTitles;
    protected Object[][] auxData;
    
    protected float[][] geneGroupMeans, geneGroupSDs;
    
    Vector<String> exptNamesVector;
    protected int[] groupAssignments;
    protected double falseProp;
    protected IData data;
    protected int numGroups, dataDesign, numFullGroups, numRedGroups, geneSetOrigin, numPerms;
    protected float alpha;
    protected String factorAName, factorBName;
    protected boolean errorGenes;
    protected int iterations;
    
    protected ArrayList<String> geneLabels;
    protected ArrayList<String> sampleLabels;
	private String[] geneListNames;
	private FloatMatrix resultMatrix;
	private String[] geneSetFilePath;
	private String annotChosen;
	
	
    
    /** Creates new GLOBALANCGUI */
    public GLOBANCGUI() {
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
		
		// For Mac OS X only --
		// Check for R ver and dyn lib compatibility
		// If mismatched try upgrading to correct version
//		if (RHook.getOS() == RConstants.MAC_OS) {
//			try {
//				if (RHook.Mac_R_ver_Changed()) {
//					if (!RHook.checkRDynLib("globalanc")) {
//						JOptionPane.showMessageDialog(null, "Error updating R library", "REngine", JOptionPane.ERROR_MESSAGE);
//						throw new AbortException(); 
//					}
//				}
//			} catch (Exception e) {
//				e.printStackTrace();
//				JOptionPane.showMessageDialog(null, "Error updating R library\n **" + e.getMessage(), "REngine", JOptionPane.ERROR_MESSAGE);
//				throw new AbortException();
//			}
//		}
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
        	
        
        GLOBANCInitBox GLOBALANCDialog = new GLOBANCInitBox((JFrame)framework.getFrame(), true, exptNamesVector,framework.getData().getAllFilledAnnotationFields(), framework.getClusterRepository(1));
        GLOBALANCDialog.setVisible(true);
        
        annotChosen = GLOBALANCDialog.getSelectedAnnotation();
        for (int i = 0; i < experiment.getNumberOfGenes(); i++) {
        	geneLabels.add(framework.getData().getElementAnnotation(i, annotChosen)[0]);
        }
        
        if (!GLOBALANCDialog.isOkPressed()) return null;
        
        alpha = GLOBALANCDialog.getAlpha();
        dataDesign = 4;
        numGroups = GLOBALANCDialog.getNumGroups();
        numPerms = GLOBALANCDialog.getNumPerms();
        numFullGroups = GLOBALANCDialog.getNumFullGroups();
        numRedGroups = GLOBALANCDialog.getNumRedGroups();
        factorAName = GLOBALANCDialog.getFactorAName();
        factorBName = GLOBALANCDialog.getFactorBName();
        groupAssignments=GLOBALANCDialog.getGroupAssignments();
        geneSetFilePath = GLOBALANCDialog.getGeneSetFilePath();
        geneSetOrigin = GLOBALANCDialog.getGeneSetOrigin();
        if (groupAssignments == null)
        	return null;
        
        IDistanceMenu menu = framework.getDistanceMenu();
        int function = menu.getDistanceFunction();
        if (function == Algorithm.DEFAULT) {
            function = Algorithm.EUCLIDEAN;
        }
        
        
        Listener listener = new Listener();
        
        try {
            algorithm = framework.getAlgorithmFactory().getAlgorithm("GLOBANC");
            algorithm.addAlgorithmListener(listener);
            
            this.progress = new Progress(framework.getFrame(), "Running Global ANCOVA Analysis", listener);
            this.progress.setIndeterminate(true);
            this.progress.setIndeterminantString("Running Analysis...");
            this.progress.show();
            
            AlgorithmData data = new AlgorithmData();
            
            data.addMatrix("experiment", experiment.getMatrix());
            data.addIntArray("group_assignments", groupAssignments);
            data.addParam("dataDesign", String.valueOf(dataDesign));
            data.addParam("numGroups", String.valueOf(numGroups));
            data.addParam("alpha", String.valueOf(alpha));
            data.addParam("numAGroups",String.valueOf(numFullGroups));
            data.addParam("numPerms",String.valueOf(numPerms));
            data.addParam("numBGroups",String.valueOf(numRedGroups));
            data.addParam("nameA",String.valueOf(factorAName));
            data.addParam("nameB",String.valueOf(factorBName));
            data.addParam("geneSetOrigin",String.valueOf(geneSetOrigin));
            data.addStringArray("geneSetFilePaths", geneSetFilePath);
            if (dataDesign==5){
                data.addParam("numAGroups",String.valueOf(2));
                data.addParam("numBGroups",String.valueOf(numGroups));
	            data.addParam("nameA",String.valueOf("Condition"));
	            data.addParam("nameB",String.valueOf("Time"));
            }

            //Raktim
            data.addStringArray("geneLabels", geneLabels.toArray(new String[geneLabels.size()]));
            data.addStringArray("sampleLabels", sampleLabels.toArray(new String[sampleLabels.size()]));
            
            long start = System.currentTimeMillis();
            AlgorithmData result = algorithm.execute(data);
            long time = System.currentTimeMillis() - start;
            
            // getting the results
            Cluster result_cluster = result.getCluster("cluster");
            this.means = result.getMatrix("clusters_means");
            this.variances = result.getMatrix("clusters_variances");
            
            this.geneLists = result.getIntMatrix("geneListsMatrix");
            this.geneListNames = result.getStringArray("gene-list-names");
            this.resultMatrix = result.getMatrix("result-matrix");      
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
        
        this.experiment = framework.getData().getExperiment();
        exptNamesVector = new Vector<String>();
        int number_of_samples = experiment.getNumberOfSamples();
        
        for (int i = 0; i < number_of_samples; i++) {
            exptNamesVector.add(framework.getData().getFullSampleName(experiment.getSampleIndex(i)));
        }
        
        GLOBANCInitBox GLOBANCDialog = new GLOBANCInitBox((JFrame)framework.getFrame(), true, exptNamesVector,framework.getData().getAllFilledAnnotationFields(),framework.getClusterRepository(1));
        GLOBANCDialog.setVisible(true);
        
        if (!GLOBANCDialog.isOkPressed()) return null;
        
        dataDesign = GLOBANCDialog.getExperimentalDesign();
        numGroups = GLOBANCDialog.getNumGroups();

    	groupAssignments=GLOBANCDialog.getGroupAssignments();
        if (groupAssignments == null)
        	return null;
        
        IDistanceMenu menu = framework.getDistanceMenu();
        int function = menu.getDistanceFunction();
        if (function == Algorithm.DEFAULT) {
            function = Algorithm.EUCLIDEAN;
        }
        
        AlgorithmData data = new AlgorithmData();
        
        data.addParam("distance-factor", String.valueOf(1.0f));
        data.addParam("distance-absolute", String.valueOf(menu.isAbsoluteDistance()));
        
        data.addParam("distance-function", String.valueOf(function));
        data.addIntArray("group_assignments", groupAssignments);
        data.addParam("numGroups", String.valueOf(numGroups));        
        
        // alg name
        data.addParam("name", "GLOBANC");
        
        // alg type
        data.addParam("alg-type", "cluster-genes");
        
        // output class
        data.addParam("output-class", "partition-output");
        
        //output nodes
        String [] outputNodes = new String[2];
        outputNodes[0] = "Significant Genes";
        outputNodes[1] = "Non-significant Genes";
        
        data.addStringArray("output-nodes", outputNodes);
        
        
        return data;
    }
    
    public DefaultMutableTreeNode executeScript(IFramework framework, AlgorithmData algData, Experiment experiment) throws AlgorithmException {
        
        Listener listener = new Listener();
        this.experiment = experiment;
        this.data = framework.getData();
        this.groupAssignments = algData.getIntArray("group_assignments");
//        this.drawSigTreesOnly = algData.getParams().getBoolean("draw-sig-trees-only");        
        
        exptNamesVector = new Vector<String>();
        int number_of_samples = experiment.getNumberOfSamples();

        for (int i = 0; i < number_of_samples; i++) {
            exptNamesVector.add(this.data.getFullSampleName(i));
        }
 
        try {
            algData.addMatrix("experiment", experiment.getMatrix());
            algorithm = framework.getAlgorithmFactory().getAlgorithm("GLOBANC");
            algorithm.addAlgorithmListener(listener);
            
            this.progress = new Progress(framework.getFrame(), "Running GLOBANC Analysis", listener);
            this.progress.setIndeterminate(true);
            this.progress.setIndeterminantString("Running Analysis");
            this.progress.show();
            
            long start = System.currentTimeMillis();
            AlgorithmData result = algorithm.execute(algData);
            long time = System.currentTimeMillis() - start;
            
            // getting the results
            Cluster result_cluster = result.getCluster("cluster");
            NodeList nodeList = result_cluster.getNodeList();
            //AlgorithmParameters resultMap = result.getParams();
            int k = 2; //resultMap.getInt("number-of-clusters"); // NEED THIS TO GET THE VALUE OF NUMBER-OF-CLUSTERS
                       
            this.geneLists = new int[k][];
            for (int i=0; i<k; i++) {
                geneLists[i] = nodeList.getNode(i).getFeaturesIndexes();
            }
            this.means = result.getMatrix("clusters_means");
            this.variances = result.getMatrix("clusters_variances");
            
            
            AlgorithmParameters params = algData.getParams();
            
            GeneralInfo info = new GeneralInfo();
            info.time = time;
            //ADD MORE INFO PARAMETERS HERE
            info.alpha = params.getFloat("alpha");
            numGroups = params.getInt("numGroups");
            info.usePerms = params.getBoolean("usePerms");
            info.numPerms = params.getInt("numPerms");
            info.function = framework.getDistanceMenu().getFunctionName(params.getInt("distance-function"));
            info.hcl = params.getBoolean("hierarchical-tree");
            info.hcl_genes = params.getBoolean("calculate-genes");
            info.hcl_samples = params.getBoolean("calculate-experiments");
            if(info.hcl)
                info.hcl_method = params.getInt("method-linkage") ;
            
            Vector<String> titlesVector = new Vector<String>();
            for (int i = 0; i < geneGroupMeans[0].length; i++) {
                titlesVector.add("Group" + (i+1) + " mean");
                titlesVector.add("Group" + (i + 1) + " std.dev");
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
    
    private int getTotalInteractions(int groups){
    	if (dataDesign==4||dataDesign==5)
    		return 3;
    	if (groups <= 1)
    		return 0;
    	else
    		return (groups-1 + getTotalInteractions(groups-1));
    }
    
    public static void main(String[] args){
    	
    }
    protected String getNodeTitle(int ind,int x, int y){
    	return this.geneListNames[ind];
    	
//    	if (dataDesign==1){
//    		if (ind==0)
//                return "Significant Genes ";
//            else 
//                return "Non-significant Genes ";
//    	}
//    	else if (dataDesign==3){
//        	String str = "";
//        	str = (ind%2==0)? "Significant Genes ":"Non-Significant Genes ";
//        	if (ind<getTotalInteractions(numGroups)*2)
//        		str = str+x+" vs. "+y;
//        	else
//        		str = str+"(All Groups)";
//        	return str;
//        }
//    	else if (dataDesign==4){
//    		int index = ind/2;
//        	String str = "";
//        	str = (ind%2==0)? "Significant Genes ":"Non-Significant Genes ";
//        	if (index == 0)
//        		str = str + this.factorAName + "1, " +this.factorBName+ " 1 vs. 2";
//        	if (index == 1)
//        		str = str + this.factorAName + "2, " +this.factorBName+ " 1 vs. 2";
//        	if (index == 2)
//        		str = str + "Difference";
//        	return str;
//        }
//    	else if (dataDesign==5){
//        	String str = "";
//        	str = (ind%2==0)? "Significant Genes, ":"Non-Significant Genes, ";
//        	
//        	if (ind<(numGroups-1)*2)
//        		str = str+"cond. = 1, ";
//        	else if (ind<(numGroups-1)*4)
//        		str = str+"cond. = 2, ";
//        	if (ind<(numGroups-1)*4)
//        		str = str+"t = "+(ind/2%(numGroups-1)+1);
//        	if (ind==(numGroups-1)*4||ind==(numGroups-1)*4+1)
//        		str = str+"cond. = 1 (All) ";
//        	if (ind==(numGroups-1)*4+2||ind==(numGroups-1)*4+3)
//        		str = str+"cond. = 2 (All) ";
//        	if (ind==(clusters.length-2)||ind==(clusters.length-1))
//        		str = str+"(All) ";
//
//        	return str;
//        }else{
//            if (ind%2==0) {
//                return "Significant Genes "+x+" vs. "+y;
//            } else {
//                return "Non-significant Genes "+x+" vs. "+y;
//            }
//    	}
    	
    }
    /**
     * Creates a result tree to be inserted into the framework analysis node.
     */
    protected DefaultMutableTreeNode createResultTree(Cluster result_cluster, GeneralInfo info) {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("GLOBANC");
        addResultNodes(root, result_cluster, info);
        return root;
    }
    
    /**
     * Adds result nodes into the tree root.
     */
    protected void addResultNodes(DefaultMutableTreeNode root, Cluster result_cluster, GeneralInfo info) {
        addExpressionImages(root);
//        addHierarchicalTrees(root, result_cluster, info);
//        addCentroidViews(root);
        addTableViews(root);
        addGeneSetInfo(root);
        addClusterInfo(root);
        
        addGeneralInfo(root, info);
    }
    
    private void addGeneSetInfo(DefaultMutableTreeNode root) {
//        DefaultMutableTreeNode node = new DefaultMutableTreeNode("Results Table");
        Object[][] results = new Object[this.resultMatrix.A.length][this.resultMatrix.A[0].length+1];
        for (int i=0; i<results.length; i++){
        	for (int j=1; j<results[0].length; j++){
        		results[i][j]= resultMatrix.A[i][j-1];
        	}
        }

        for (int i=0; i<results.length; i++){
        	results[i][0] = geneListNames[i];
        }
        String[] columns = {"Gene List","Gene Count", "F-value", "p-value (permutation)", "p-value (approximate)"};
        
        IViewer tabViewer = new GLOBALANCResultTable(results,columns);
    	root.add(new DefaultMutableTreeNode(new LeafInfo("Results Table", tabViewer, new Integer(0))));
		
	}

	protected void addTableViews(DefaultMutableTreeNode root) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode("Table Views");
        IViewer tabViewer = new ClusterTableViewer(this.experiment, this.geneLists, this.data, this.auxTitles, this.auxData);
        int x=1; int y=2;
        for (int i=0; i<this.geneLists.length; i++) {
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
        IViewer expViewer = new GLOBANCExperimentViewer(this.experiment, geneLists, null, null, null, null, null, null, null, null, null);
        int x=1; int y=2;
        for (int i=0; i<this.geneLists.length; i++) {
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
        node.add(new DefaultMutableTreeNode(new LeafInfo("Results (#,%)", new GLOBANCInfoViewer(this.geneLists, this.geneListNames, this.resultMatrix, this.experiment.getNumberOfGenes(), this.dataDesign, this.numGroups))));
        root.add(node);
    }
    
    /**
     * Adds nodes to display centroid charts.
     */
    protected void addCentroidViews(DefaultMutableTreeNode root) {
        DefaultMutableTreeNode centroidNode = new DefaultMutableTreeNode("Centroid Graphs");
        DefaultMutableTreeNode expressionNode = new DefaultMutableTreeNode("Expression Graphs");
        GLOBANCCentroidViewer centroidViewer = new GLOBANCCentroidViewer(this.experiment, geneLists, null, null, null, null, null, null, null, null, null);
        centroidViewer.setMeans(this.means.A);
        centroidViewer.setVariances(this.variances.A);
        for (int i=0; i<this.geneLists.length; i++) {
            if (i == 0) {
                centroidNode.add(new DefaultMutableTreeNode(new LeafInfo("Significant Genes ", centroidViewer, new CentroidUserObject(i, CentroidUserObject.VARIANCES_MODE))));
                expressionNode.add(new DefaultMutableTreeNode(new LeafInfo("Significant Genes ", centroidViewer, new CentroidUserObject(i, CentroidUserObject.VALUES_MODE))));
            } else if (i == 1) {
                centroidNode.add(new DefaultMutableTreeNode(new LeafInfo("Non-significant Genes ", centroidViewer, new CentroidUserObject(i, CentroidUserObject.VARIANCES_MODE))));
                expressionNode.add(new DefaultMutableTreeNode(new LeafInfo("Non-significant Genes ", centroidViewer, new CentroidUserObject(i, CentroidUserObject.VALUES_MODE))));
            }
        }
        
        GLOBANCCentroidsViewer centroidsViewer = new GLOBANCCentroidsViewer(this.experiment, geneLists, geneGroupMeans, geneGroupSDs, null, null, null, null, null, null, null);

        centroidsViewer.setMeans(this.means.A);
        centroidsViewer.setVariances(this.variances.A);
        
        centroidNode.add(new DefaultMutableTreeNode(new LeafInfo("All Genes", centroidsViewer, new Integer(CentroidUserObject.VARIANCES_MODE))));
        expressionNode.add(new DefaultMutableTreeNode(new LeafInfo("All Genes", centroidsViewer, new Integer(CentroidUserObject.VALUES_MODE))));
        root.add(centroidNode);
        root.add(expressionNode);
    }
    
    
    /**
     * Adds node with general information.
     */
    protected void addGeneralInfo(DefaultMutableTreeNode root, GeneralInfo info) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode("General Information");
        
    	node.add(getGroupAssignmentInfo());
        
        node.add(new DefaultMutableTreeNode("Full Model Groups: "+this.numFullGroups));  
        node.add(new DefaultMutableTreeNode("Reduced Model Groups: "+this.numRedGroups));  
        node.add(new DefaultMutableTreeNode("Number of Permutations: "+numPerms)); 
        node.add(new DefaultMutableTreeNode("Gene set: "+(geneSetOrigin==0 ? "local file" :geneSetOrigin==1 ? "MSigDB":"GeneSigDB")));
        node.add(new DefaultMutableTreeNode("Annotation Type: "+annotChosen));  
        for (int i=0; i<geneSetFilePath.length; i++){
        	node.add(new DefaultMutableTreeNode("File location: "+geneSetFilePath[i]));        	
        } 
        node.add(new DefaultMutableTreeNode("Time: "+String.valueOf(info.time-1)+" ms"));
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
			message += "You need to have 32Bit JVM as default for GLOBANC\n";
			message += "Please contact MeV Support if you need help.\n";
			message += "You also need to have R 2.9.x installed for GLOBANC\n";
			message += "Cancel if either is not installed. Ok to continue.";
			return JOptionPane.showConfirmDialog(null, message, "R Engine Warning", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
		}
		if(arch.toLowerCase().contains("64")) {
			message += "You need to have 32Bit JVM as default for GLOBANC\n";
			message += "Please contact MeV Support if you need help.\n";
			message += "Cancel if 32 Bit JVM is not installed. Ok to continue.";
			return JOptionPane.showConfirmDialog(null, message, "R Engine Warning", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
		}
		if (os.toLowerCase().contains("mac")) {
			message += "You need to have R 2.9.x installed for GLOBANC\n";
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
