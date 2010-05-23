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
package org.tigr.microarray.mev.cluster.gui.impl.surv;

//TODO
//Implement IScriptGUI interface
//write and use InfoViewer

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;

import org.tigr.microarray.mev.cluster.Cluster;
import org.tigr.microarray.mev.cluster.ClusterWrapper;
import org.tigr.microarray.mev.cluster.Node;
import org.tigr.microarray.mev.cluster.NodeList;
import org.tigr.microarray.mev.cluster.NodeValueList;
import org.tigr.microarray.mev.cluster.algorithm.AbortException;
import org.tigr.microarray.mev.cluster.algorithm.Algorithm;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmData;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmEvent;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmException;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmListener;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmParameters;
import org.tigr.microarray.mev.cluster.algorithm.impl.ExperimentUtil;
import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.IClusterGUI;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.IDistanceMenu;
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.IViewer;
import org.tigr.microarray.mev.cluster.gui.LeafInfo;
import org.tigr.microarray.mev.cluster.gui.helpers.CentroidUserObject;
import org.tigr.microarray.mev.cluster.gui.helpers.ClusterTableViewer;
import org.tigr.microarray.mev.cluster.gui.helpers.ExperimentClusterTableViewer;
import org.tigr.microarray.mev.cluster.gui.helpers.KMGraphViewer;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.DialogListener;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.Progress;
import org.tigr.microarray.mev.cluster.gui.impl.hcl.HCLGUI;
import org.tigr.microarray.mev.cluster.gui.impl.hcl.HCLInitDialog;
import org.tigr.microarray.mev.cluster.gui.impl.hcl.HCLTreeData;
import org.tigr.microarray.mev.cluster.gui.impl.hcl.HCLViewer;
import org.tigr.microarray.mev.script.scriptGUI.IScriptGUI;
import org.tigr.rhook.RConstants;
import org.tigr.rhook.RHook;
import org.tigr.util.FloatMatrix;

/**
 * @author  eleanorahowe
 * @version
 */
public class SURVGUI implements IClusterGUI/*, IScriptGUI*/ {
    
    protected Algorithm algorithm;
    protected Progress progress;
    protected Experiment experiment;
    protected int[][] clusters;

    protected FloatMatrix means;
    protected FloatMatrix variances;
    protected float[][] geneGroupMeans, geneGroupSDs;
    
    protected boolean drawSigTreesOnly;
    
    Vector<String> exptNamesVector;
    protected int[] groupAssignments;
    protected IData data;
    protected int numGroups, numFactorAGroups, numFactorBGroups;
    protected boolean isHierarchicalTree;
    

    protected String[] auxTitles;
    protected Object[][] auxData;

    //Raktim LIMMA gene & sample names
    protected ArrayList<String> geneLabels;
    protected ArrayList<String> sampleLabels;

    Vector<Vector<Float>> timeses = new Vector<Vector<Float>>();
    Vector<Vector<Boolean>> statuses = new Vector<Vector<Boolean>>();
    Vector<Color> colors = new Vector<Color>();
    
    String statusAnnotationField, eventAnnotationField;
 
    
    public SURVGUI() {
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
		if (RHook.getOS() == RConstants.MAC_OS) {
			try {
				if (RHook.Mac_R_ver_Changed()) {
					if (!RHook.checkRDynLib("survival")) {
						JOptionPane.showMessageDialog(null, "Error updating R library", "REngine", JOptionPane.ERROR_MESSAGE);
						throw new AbortException(); 
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(null, "Error updating R library\n **" + e.getMessage(), "REngine", JOptionPane.ERROR_MESSAGE);
				throw new AbortException();
			}
		}

        this.experiment = framework.getData().getExperiment();   
        
        int number_of_samples = experiment.getNumberOfSamples();
        
        int [] columnIndices = experiment.getColumnIndicesCopy(); 
        
        this.data = framework.getData();
        Vector<String> sampleFieldNames = data.getSampleAnnotationFieldNames();
        exptNamesVector = new Vector<String>();
        
        for (int i = 0; i < number_of_samples; i++) {
            exptNamesVector.add(framework.getData().getFullSampleName(columnIndices[i]));
        }
        
        SURVInitBox survDialog = new SURVInitBox(
        			(JFrame)framework.getFrame(), 
        			true, 
        			exptNamesVector, 
        			framework.getClusterRepository(1),
        			framework.getClusterRepository(0), 
        			data, 
        			sampleFieldNames
        );
        survDialog.setVisible(true);
        
        if (!survDialog.isOkPressed()) return null;
        isHierarchicalTree = survDialog.drawTrees();
        drawSigTreesOnly = true;
        if (isHierarchicalTree) {
            drawSigTreesOnly = survDialog.drawSigTreesOnly();
        }      
        
        IDistanceMenu menu = framework.getDistanceMenu();
        int function = menu.getDistanceFunction();
        if (function == Algorithm.DEFAULT) {
            function = Algorithm.EUCLIDEAN;
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
            algorithm = framework.getAlgorithmFactory().getAlgorithm("SURV");
            algorithm.addAlgorithmListener(listener);
            
            this.progress = new Progress(framework.getFrame(), "Running Survival Analysis", listener);
            this.progress.setIndeterminate(true);
            this.progress.setIndeterminantString("Preparing Data");
            this.progress.show();
            
            SURVAlgorithmData algData = new SURVAlgorithmData();
            
            //TODO 
            //For R, TRUE status == death. So far for MeV, TRUE == censored. 
            statusAnnotationField = survDialog.getCensoredField();
            eventAnnotationField = survDialog.getSurvivalField();
         
            //Fake color selector to use until I figure out how to get a cluster's selected color from InitDialog or cluster manager.
            Color[] colorlist = new Color[]{Color.red, Color.green, Color.blue};

            int designType = survDialog.getExperimentalDesign();
            boolean comparison = true;
            if(designType == 1)
            	comparison = false;
            //TODO
            System.out.println("Comparison: " + comparison);
            algData.setComparison(comparison);
            
            if(algData.isComparison()) {
                this.progress.setIndeterminantString("Comparing Survival Profiles");
            	//Populate SURVAlgorithmData algData with parameters for a 
            	//Kaplan-Meier comparison between two sample groups.
    	        numGroups = survDialog.getNumGroups();
    	        numFactorAGroups = survDialog.getNumFactorAGroups();
    	        numFactorBGroups = survDialog.getNumFactorBGroups();
    	        
    	        groupAssignments=survDialog.getGroupAssignments();
    	        if (groupAssignments == null)
    	        	return null;
    	        Hashtable<Integer, Vector<Integer>> temp = new Hashtable<Integer, Vector<Integer>>();
    	        //use groupAssignments to fill clusters int[][]
    	        for(int i=0; i<groupAssignments.length; i++) {
    	        	if(!temp.containsKey(new Integer(groupAssignments[i]))) {
    	        		temp.put(new Integer(groupAssignments[i]), new Vector<Integer>());
    	        	}
    	        	temp.get(new Integer(groupAssignments[i])).add(new Integer(i));
    	        }
    	        
                Hashtable<Integer, Vector<Float>> timeshash = new Hashtable<Integer, Vector<Float>>();
                Hashtable<Integer, Vector<Boolean>> statushash = new Hashtable<Integer, Vector<Boolean>>();
                Hashtable<Integer, Vector<Integer>> origIndexeshash = new Hashtable<Integer, Vector<Integer>>();
                for(int i=0; i<groupAssignments.length; i++) {
                	//If new group
                	if(!timeshash.containsKey(groupAssignments[i])) {
            			timeshash.put(groupAssignments[i], new Vector<Float>());
            			statushash.put(groupAssignments[i], new Vector<Boolean>());
            			origIndexeshash.put(groupAssignments[i], new Vector<Integer>());
                		colors.add(colorlist[groupAssignments[i]]);
                	}

                	float thistime = parseEventTime(this.data.getSampleAnnotation(i, eventAnnotationField));
                	boolean thisstatus = parseStatus(this.data.getSampleAnnotation(i, statusAnnotationField));
                	timeshash.get(groupAssignments[i]).add(thistime);
                	statushash.get(groupAssignments[i]).add(thisstatus);
                	origIndexeshash.get(groupAssignments[i]).add(new Integer(i));
                }
                timeses = new Vector<Vector<Float>>(timeshash.values());
                statuses = new Vector<Vector<Boolean>>(statushash.values());
                Vector<Vector<Integer>> indexes = new Vector<Vector<Integer>>(origIndexeshash.values());
                
                algData.setGroup1Events(timeses.get(0));
                algData.setGroup2Events(timeses.get(1));
                algData.setGroup1Statuses(statuses.get(0));
                algData.setGroup2Statuses(statuses.get(1));
                algData.setGroup1OriginalIndexes(indexes.get(0));
                algData.setGroup2OriginalIndexes(indexes.get(1));
                clusters = new int[temp.size()][];
                for(int i=0; i<temp.size(); i++) {
                	clusters[i] = new int[temp.get(i).size()];
                	for(int j=0; j<clusters[i].length; j++) {
                		clusters[i][j] = temp.get(i).get(j);
                	}
                }
              
            } else {
                this.progress.setIndeterminantString("Calclating Cox Proportional Hazards Model");
            	//Populate SURVAlgorithmData algData with parameters for a 
            	//Cox Proportional Hazards model with censoring and variable selection.
            	Vector<Float> times = new Vector<Float>();
            	Vector<Boolean> statuses = new Vector<Boolean>();
            	Vector<Integer> indexes = new Vector<Integer>();
                for(int i=0; i<data.getExperiment().getNumberOfSamples(); i++) {
            		times.add(parseEventTime(this.data.getSampleAnnotation(i, eventAnnotationField)));
            		statuses.add(parseStatus(this.data.getSampleAnnotation(i, statusAnnotationField)));
            		indexes.add(new Integer(i));
            	}
                algData.setGroup1Events(times);
                algData.setGroup1Statuses(statuses);
                algData.setGroup1OriginalIndexes(indexes);
                algData.setGroup2Events(new Vector<Float>());
                algData.setGroup2Statuses(new Vector<Boolean>());
                algData.setGroup2OriginalIndexes(new Vector<Integer>());
            
                
                algData.setSampleLabels(exptNamesVector.toArray(new String[exptNamesVector.size()]));
                //Change setExpressionMatrix to only include genes that are in the selected cluster.
                
                org.tigr.microarray.mev.cluster.clusterUtil.Cluster selectedGenes = survDialog.getSelectedGeneCluster();
                //Use whole experiment
                if(selectedGenes == null) {
                	algData.setGeneIndices(experiment.getRows());
                	algData.setExpressionMatrix(experiment.getMatrix());
                } else {
	                algData.setGeneIndices(selectedGenes.getIndices());
	                algData.setExpressionMatrix(experiment.getMatrix().getMatrix(selectedGenes.getIndices(), experiment.getColumnIndicesCopy()));
                }
                System.out.println("Setting lambda " + survDialog.getLambda());
                algData.setLambda(survDialog.getLambda());
            	
            }
            
            // hcl parameters
            if (isHierarchicalTree) {
                algData.addParam("hierarchical-tree", String.valueOf(true));
                algData.addParam("draw-sig-trees-only", String.valueOf(drawSigTreesOnly));                
                algData.addParam("method-linkage", String.valueOf(hcl_method));
                algData.addParam("calculate-genes", String.valueOf(hcl_genes));
                algData.addParam("calculate-experiments", String.valueOf(hcl_samples));
                algData.addParam("hcl-distance-function", String.valueOf(hcl_function));
                algData.addParam("hcl-distance-absolute", String.valueOf(hcl_absolute));
                algData.addParam("hcl-genes-ordered", String.valueOf(hcl_genes_ordered));
                algData.addParam("hcl-samples-ordered", String.valueOf(hcl_samples_ordered));
            }
            
            long start = System.currentTimeMillis();
            SURVAlgorithmData result = (SURVAlgorithmData)algorithm.execute(algData);
            long time = System.currentTimeMillis() - start;

            GeneralInfo info = new GeneralInfo();

            info.result = result;
            if(result.isEmptyResults()) {
            	return createEmptyResultNode(result, info, "No results found.");
            }
            this.progress.setIndeterminantString("Retrieving Data");

            if(result.isComparison()) {
            	//Process result data from SURVAlgorithmData algData with results from a 
            	//Kaplan-Meier survival comparison.
	            this.auxTitles = new String[]{"Linear Predictors", "Residuals"};

            	this.auxData = new Object[experiment.getNumberOfSamples()][auxTitles.length];
	            for(int i=0; i<auxData.length; i++) {
            		int j = result.getResidualsNames().indexOf(new Integer(i).toString());
	            	if(j >= 0) {
	            		auxData[i][0] = result.getLinearPredictors().get(j);
		            	auxData[i][1] = result.getResiduals().get(j);
	            	} else {
		            	auxData[i][0] = "N/A";
		            	auxData[i][1] = "N/A";
	            	}
	            }
	
	            info.chisquare = result.getChiSquare();
	            info.pvalue = result.getPValue();
	            Vector<Double> expected = result.getExpected();//getVector("expected");
	            Vector<Double> observed = result.getObserved();//getVector("observed");
	            Vector<Integer> sizes = result.getSizes();
	            info.cluster1expected = expected.get(0).floatValue();
	            info.cluster2expected = expected.get(1).floatValue();
	            info.cluster1observed = observed.get(0).floatValue();
	            info.cluster2observed = observed.get(1).floatValue();
	            info.cluster1size = sizes.get(0).intValue();
	            info.cluster2size = sizes.get(1).intValue();
	            
	          
	            info.hcl = isHierarchicalTree;
	            info.hcl_genes = hcl_genes;
	            info.hcl_samples = hcl_samples;
	            info.hcl_method = hcl_method;
	            return createResultTree(info);
            } else {
            	//Process a SURVAlgorithmData algData with results from a 
            	//Cox Proportional Hazards model with censoring and variable selection.
            	Vector<Double> penalizedCoefficients = result.getPenalizedCoefficients();
            	clusters = result.getResClusters();
            	int[] penalizedCoefficientsIndexes = result.getPenalizedCoefficientIndexes();
                auxTitles = new String[]{"Coefficient", "Weights"};
                Vector<Double> weights = result.getWeights();
                auxData = new Object[experiment.getNumberOfGenes()][auxTitles.length];
                for(int i=0; i<auxData.length; i++) {
                    auxData[i][0] = new Double(0);
                    auxData[i][1] = "NA";
                }
                for(int i=0; i<penalizedCoefficientsIndexes.length; i++) {
                    auxData[penalizedCoefficientsIndexes[i]][0] = penalizedCoefficients.get(i);
                 	
                	
                }
                //Get gene indexes of selected genes from the cluster. Make sure all the viewers map indices to the whole experiment properly.
                int[] selectedIndices = algData.getGeneIndices();
                for(int i=0; i<selectedIndices.length; i++) {
                	auxData[selectedIndices[i]][1] = weights.get(i);
                }
            	return createResultTree(info);
            	
            }
        } catch (Exception e) {
        	e.printStackTrace();
        	return null;
        } finally {
            if (algorithm != null) {
                algorithm.removeAlgorithmListener(listener);
            }
            if (progress != null) {
                progress.dispose();
            }
        }
    }
    /** creates an empty result with label label if the result is null.
     * @param result
     * @param label the label to apply to the treenode
     * @return 
     */
    protected DefaultMutableTreeNode createEmptyResultNode(AlgorithmData result, GeneralInfo info, String label){
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(label);
        root.add(new DefaultMutableTreeNode("No Results Found"));
        addGeneralInfo(root, info);
        return root;
    }
    //TODO make more flexible
    private boolean parseStatus(String statusString) {
    	if(statusString.equalsIgnoreCase("yes") ||
    			statusString.equalsIgnoreCase("y") ||
    			statusString.equalsIgnoreCase("censored")) {
    		return true;
    	}
    	if(statusString.equalsIgnoreCase("no") ||
    			statusString.equalsIgnoreCase("n") ||
    			statusString.equalsIgnoreCase("dead") ||
    			statusString.equalsIgnoreCase("uncensored")) {
    		return false;
    	}
    	return false;
    }
    //TODO make more flexible? 
    private float parseEventTime(String eventString) {
    	return new Float(eventString).floatValue();
    }
    
    public AlgorithmData getScriptParameters(IFramework framework) {
        
        this.experiment = framework.getData().getExperiment();
        exptNamesVector = new Vector<String>();
        int number_of_samples = experiment.getNumberOfSamples();
        
        for (int i = 0; i < number_of_samples; i++) {
            exptNamesVector.add(framework.getData().getFullSampleName(experiment.getSampleIndex(i)));
        }

        Vector<String> sampleFieldNames = data.getSampleAnnotationFieldNames();
        SURVInitBox SURVDialog = new SURVInitBox((JFrame)framework.getFrame(), true, exptNamesVector,framework.getClusterRepository(1), framework.getClusterRepository(0), data, sampleFieldNames);
        SURVDialog.setVisible(true);
        
        if (!SURVDialog.isOkPressed()) return null;
        
        int dataDesign = SURVDialog.getExperimentalDesign();
        numGroups = SURVDialog.getNumGroups();


       	groupAssignments=SURVDialog.getGroupAssignments();
       	
        if (groupAssignments == null)
        	return null;
        
        boolean isHierarchicalTree = SURVDialog.drawTrees();
        drawSigTreesOnly = true;
        if (isHierarchicalTree) {
            drawSigTreesOnly = SURVDialog.drawSigTreesOnly();
        }         
        
        IDistanceMenu menu = framework.getDistanceMenu();
        int function = menu.getDistanceFunction();
        if (function == Algorithm.DEFAULT) {
            function = Algorithm.EUCLIDEAN;
        }
        
        // hcl init
        int hcl_method = 0;
        boolean hcl_samples = false;
        boolean hcl_genes = false;
        int hcl_function = 4;
        boolean hcl_absolute = false;
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
        }        
        AlgorithmData data = new AlgorithmData();
        
        data.addParam("distance-factor", String.valueOf(1.0f));
        data.addParam("distance-absolute", String.valueOf(menu.isAbsoluteDistance()));
        
        data.addParam("distance-function", String.valueOf(function));
        data.addIntArray("group_assignments", groupAssignments);
        data.addParam("alpha-value", String.valueOf(SURVDialog.mPanel.alpha));
        data.addParam("numGroups", String.valueOf(numGroups));
        // hcl parameters
        if (isHierarchicalTree) {
            data.addParam("hierarchical-tree", String.valueOf(true));
            data.addParam("draw-sig-trees-only", String.valueOf(drawSigTreesOnly));              
            data.addParam("method-linkage", String.valueOf(hcl_method));
            data.addParam("calculate-genes", String.valueOf(hcl_genes));
            data.addParam("calculate-experiments", String.valueOf(hcl_samples));
            data.addParam("hcl-distance-function", String.valueOf(hcl_function));
            data.addParam("hcl-distance-absolute", String.valueOf(hcl_absolute));
        }
        
        
        // alg name
        data.addParam("name", "SURV");
        
        // alg type
        data.addParam("alg-type", "cluster-genes");
        
        // output class
        data.addParam("output-class", "partition-output");
        
        //output nodes
        String [] outputNodes = new String[2];
        outputNodes[0] = "Group 1";
        outputNodes[1] = "Group 2";
        
        data.addStringArray("output-nodes", outputNodes);
        
        
        return data;
    }
    /**
    public DefaultMutableTreeNode executeScript(IFramework framework, AlgorithmData algData, Experiment experiment) throws AlgorithmException {
        
        Listener listener = new Listener();
        this.experiment = experiment;
        this.data = framework.getData();
        this.groupAssignments = algData.getIntArray("group_assignments");
        this.drawSigTreesOnly = algData.getParams().getBoolean("draw-sig-trees-only");        
        
        exptNamesVector = new Vector<String>();
        int number_of_samples = experiment.getNumberOfSamples();

        for (int i = 0; i < number_of_samples; i++) {
            exptNamesVector.add(this.data.getFullSampleName(i));
        }
 
        try {
            algData.addMatrix("experiment", experiment.getMatrix());
            algorithm = framework.getAlgorithmFactory().getAlgorithm("SURV");
            algorithm.addAlgorithmListener(listener);
            
            this.progress = new Progress(framework.getFrame(), "Running Survival Analysis", listener);
            this.progress.setIndeterminate(true);
            this.progress.setIndeterminantString("Finding Significant Genes");
            this.progress.show();
            
            long start = System.currentTimeMillis();
            AlgorithmData result = algorithm.execute(algData);
            long time = System.currentTimeMillis() - start;
            
            // getting the results
            Cluster result_cluster = result.getCluster("cluster");
            NodeList nodeList = result_cluster.getNodeList();
            //AlgorithmParameters resultMap = result.getParams();
            int k = 2; //resultMap.getInt("number-of-clusters"); // NEED THIS TO GET THE VALUE OF NUMBER-OF-CLUSTERS
                       
            this.clusters = new int[k][];
            for (int i=0; i<k; i++) {
                clusters[i] = nodeList.getNode(i).getFeaturesIndexes();
            }
            this.means = result.getMatrix("clusters_means");
            this.variances = result.getMatrix("clusters_variances");
            
            
            AlgorithmParameters params = algData.getParams();
            
            GeneralInfo info = new GeneralInfo();
            
            
//            info.time = time;
            //ADD MORE INFO PARAMETERS HERE
//            info.alpha = params.getFloat("alpha");
            numGroups = params.getInt("numGroups");
            info.usePerms = params.getBoolean("usePerms");
            info.numPerms = params.getInt("numPerms");
//            info.function = framework.getDistanceMenu().getFunctionName(params.getInt("distance-function"));
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
  **/  
    
    public static void main(String[] args){
    	
    }
    protected String getNodeTitle(int ind,int x, int y){
	    if (ind==0) {
        	return "Neither group";
        } else if(ind==1) {
            return "Group 2 Samples";
        } else if(ind==2) {
            return "Group 1 Samples";
        }
    	return "Neither group";
    }
    /**
     * Creates a result tree to be inserted into the framework analysis node.
     */
    protected DefaultMutableTreeNode createResultTree(GeneralInfo info) {
    	if(info.result.isComparison()) {
	        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Survival Comparison");
	        //Kaplan-Meier survival curves
	        addKMPlots(root);
	        addSurvInfo(root, info);
	        //heatmap
	        addExpressionImages(root);
            //centroid graph
	        addExperimentCentroidViews(root);
	        //table
	        addExperimentTableViews(root);
	        addGeneralInfo(root, info);
	        return root;
    	} else {
	        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Survival Model");
	        addSurvInfo(root, info);
            
            DefaultMutableTreeNode node = new DefaultMutableTreeNode("Expression Images");
            IViewer expViewer = new SURVExperimentViewer(this.experiment, clusters, null, null, null, null, null, null, null, null, null);
        	node.add(new DefaultMutableTreeNode(new LeafInfo("Informative Genes", expViewer, new Integer(0))));
        	node.add(new DefaultMutableTreeNode(new LeafInfo("Non-informative Genes", expViewer, new Integer(1))));
            root.add(node);
            
            addCentroidViews(root);
            
            
            addGeneTableViews(root);
            addBaseHazGraph(root, info);
            
            //New inputs accepted for KMPlot viewer (probably)
            addGeneralInfo(root, info);
	        return root;
    	}
    }

    protected void addBaseHazGraph(DefaultMutableTreeNode root, GeneralInfo info) {

        info.result.getBasehazplotx();
        info.result.getBasehazploty();
        
		BasehazGraphViewer centroidViewer = new BasehazGraphViewer(info.result.getBasehazplotx(), info.result.getBasehazploty(), eventAnnotationField);
		root.add(new DefaultMutableTreeNode(new LeafInfo("Baseline Hazard Plot ", centroidViewer, new CentroidUserObject(1,
				CentroidUserObject.VALUES_MODE))));

        Vector<Double> baselineSurvival = info.result.getBasesurvSurvival();
        Vector<Double> baselineSurvivalTime = info.result.getBasesurvTime();
		Vector<Float> times1 = new Vector<Float>();
		Vector<Float> times2 = new Vector<Float>();

        float[][] datapoints = new float[2][baselineSurvival.size()];
		for(int i=0; i<baselineSurvival.size(); i++) {
			datapoints[0][i] = new Float(baselineSurvivalTime.get(i));
			datapoints[1][i] = new Float(baselineSurvival.get(i));
			times1.add(new Float(baselineSurvivalTime.get(i)));
			times2.add(new Float(baselineSurvival.get(i)));
		}
		Vector<Vector<Float>> temp = new Vector<Vector<Float>>();
		temp.add(times1);
		temp.add(times2);
		
		Vector<Color> colors = new Vector<Color>();
		colors.add(Color.black);
        IViewer kmViewer = new KMGraphViewer(temp, colors, eventAnnotationField);
        root.add(new DefaultMutableTreeNode(new LeafInfo("Kaplan-Meier Plot ", kmViewer, null)));
	}
    
    protected void addSurvInfo(DefaultMutableTreeNode root, /*Cluster result_cluster,*/ GeneralInfo info) {
    	Vector<Float> expected = new Vector<Float>(2);
    	expected.add(new Float(info.cluster1expected));
    	expected.add(new Float(info.cluster2expected));
    	Vector<Float> observed = new Vector<Float>(2);
    	observed.add(new Float(info.cluster1observed));
    	observed.add(new Float(info.cluster2observed));
    	Vector<Integer> sizes = new Vector<Integer>(2); 
    	sizes.add(new Integer(info.cluster1size));
    	sizes.add(new Integer(info.cluster2size));

        IViewer survViewer = new SURVInfoViewer(info.result);
        String infonodeTitle;
        if(info.result.isComparison()) {
        	infonodeTitle = "Differential Survival";
        } else {
        	infonodeTitle = "Cox Proportional Hazard Model";
        }
        root.add(new DefaultMutableTreeNode(new LeafInfo(infonodeTitle, survViewer)));
    }

    protected void addExperimentTableViews(DefaultMutableTreeNode root) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode("Table Views");
        
        IViewer tabViewer = new ExperimentClusterTableViewer(this.experiment, this.clusters, this.data, this.auxTitles, this.auxData);
        for (int i=0; i<this.clusters.length; i++) {
        	if(i == 0) {
        		node.add(new DefaultMutableTreeNode(new LeafInfo("Not in Group", tabViewer, new Integer(i))));
        	} else {
            	node.add(new DefaultMutableTreeNode(new LeafInfo("Group " + i, tabViewer, new Integer(i))));
        	}
        }
        root.add(node);
    }

    protected void addGeneTableViews(DefaultMutableTreeNode root) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode("Table Views");
        
        IViewer tabViewer = new ClusterTableViewer(this.experiment, this.clusters, this.data, this.auxTitles, this.auxData);
    	node.add(new DefaultMutableTreeNode(new LeafInfo("Informative Genes", tabViewer, new Integer(0))));
		node.add(new DefaultMutableTreeNode(new LeafInfo("Non-informative Genes", tabViewer, new Integer(1))));

        root.add(node);
    }

    protected void addKMPlots(DefaultMutableTreeNode root) {
        Vector<Vector<Float>> timeses = new Vector<Vector<Float>>();
        timeses.add(this.timeses.get(0));
        timeses.add(this.timeses.get(1));
        Vector<Vector<Boolean>> statuses = new Vector<Vector<Boolean>>();
        statuses.add(this.statuses.get(0));
        statuses.add(this.statuses.get(1));
        IViewer kmViewer = new KMGraphViewer(timeses, statuses, colors, eventAnnotationField);
        root.add(new DefaultMutableTreeNode(new LeafInfo("Kaplan-Meier Plot", kmViewer)));
    }
    
    /**
     * Adds nodes to display clusters data.
     */
    protected void addExpressionImages(DefaultMutableTreeNode root) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode("Expression Images");
        IViewer expViewer = new SURVExperimentClusterViewer(this.experiment, clusters);
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
     * Adds nodes to display hierarchical trees.
     */
//    protected void addHierarchicalTrees(DefaultMutableTreeNode root, Cluster result_cluster, GeneralInfo info) {
//        if (!info.hcl) {
//            return;
//        }
//        DefaultMutableTreeNode node = new DefaultMutableTreeNode("Hierarchical Trees");
//        NodeList nodeList = result_cluster.getNodeList();
//        if (!drawSigTreesOnly) {        
//            for (int i=0; i<nodeList.getSize(); i++) {
//                if (i < nodeList.getSize() - 1 ) {
//                    node.add(new DefaultMutableTreeNode(new LeafInfo("Significant Genes ", createHCLViewer(nodeList.getNode(i), info))));
//                } else if (i == nodeList.getSize() - 1) {
//                    node.add(new DefaultMutableTreeNode(new LeafInfo("Non-significant Genes ", createHCLViewer(nodeList.getNode(i), info))));
//                }
//            }
//        } else {
//            node.add(new DefaultMutableTreeNode(new LeafInfo("Significant Genes ", createHCLViewer(nodeList.getNode(0), info))));            
//        }
//        root.add(node);
//    }
    
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
     * Adds nodes to display centroid charts.
     */
    protected void addExperimentCentroidViews(DefaultMutableTreeNode root) {
        DefaultMutableTreeNode centroidNode = new DefaultMutableTreeNode("Centroid Graphs");
        DefaultMutableTreeNode expressionNode = new DefaultMutableTreeNode("Expression Graphs");
        SURVExperimentCentroidViewer centroidViewer = new SURVExperimentCentroidViewer(this.experiment, clusters);
        for (int i=0; i<this.clusters.length; i++) {
            if (i == 0) {
                centroidNode.add(new DefaultMutableTreeNode(new LeafInfo("Group 1 Samples ", centroidViewer, new CentroidUserObject(i, CentroidUserObject.VARIANCES_MODE))));
                expressionNode.add(new DefaultMutableTreeNode(new LeafInfo("Group 1 Samples ", centroidViewer, new CentroidUserObject(i, CentroidUserObject.VALUES_MODE))));
            } else if (i == 1) {
                centroidNode.add(new DefaultMutableTreeNode(new LeafInfo("Group 2 Samples ", centroidViewer, new CentroidUserObject(i, CentroidUserObject.VARIANCES_MODE))));
                expressionNode.add(new DefaultMutableTreeNode(new LeafInfo("Group 2 Samples ", centroidViewer, new CentroidUserObject(i, CentroidUserObject.VALUES_MODE))));
            }
        }
        
        SURVExperimentCentroidsViewer centroidsViewer = new SURVExperimentCentroidsViewer(this.experiment, clusters);

        centroidNode.add(new DefaultMutableTreeNode(new LeafInfo("All Samples", centroidsViewer, new Integer(CentroidUserObject.VARIANCES_MODE))));
        expressionNode.add(new DefaultMutableTreeNode(new LeafInfo("All Samples", centroidsViewer, new Integer(CentroidUserObject.VALUES_MODE))));
        root.add(centroidNode);
        root.add(expressionNode);
    }  
    /**
     * Adds nodes to display centroid charts.
     */
    protected void addCentroidViews(DefaultMutableTreeNode root) {
        DefaultMutableTreeNode centroidNode = new DefaultMutableTreeNode("Centroid Graphs");
        DefaultMutableTreeNode expressionNode = new DefaultMutableTreeNode("Expression Graphs");
        SURVCentroidViewer centroidViewer = new SURVCentroidViewer(this.experiment, clusters);
        for (int i=0; i<this.clusters.length; i++) {
            if (i == 0) {
                centroidNode.add(new DefaultMutableTreeNode(new LeafInfo("Informative Genes ", centroidViewer, new CentroidUserObject(i, CentroidUserObject.VARIANCES_MODE))));
                expressionNode.add(new DefaultMutableTreeNode(new LeafInfo("Informative Genes ", centroidViewer, new CentroidUserObject(i, CentroidUserObject.VALUES_MODE))));
            } else if (i == 1) {
                centroidNode.add(new DefaultMutableTreeNode(new LeafInfo("Non-informative Genes ", centroidViewer, new CentroidUserObject(i, CentroidUserObject.VARIANCES_MODE))));
                expressionNode.add(new DefaultMutableTreeNode(new LeafInfo("Non-informative Genes ", centroidViewer, new CentroidUserObject(i, CentroidUserObject.VALUES_MODE))));
            }
        }
        
        SURVCentroidsViewer centroidsViewer = new SURVCentroidsViewer(this.experiment, clusters);

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
    	if (this.isHierarchicalTree)
    		node.add(new DefaultMutableTreeNode("HCL: " + info.getMethodName()));
	    if(info.result.isComparison()) {
        	node.add(new DefaultMutableTreeNode("Kaplan-Meier Survival Comparison"));
        	if(!info.result.isEmptyResults()) {
	    		node.add(new DefaultMutableTreeNode("Chi-square: " + info.chisquare));
	    		node.add(new DefaultMutableTreeNode("p-value: " + info.pvalue));
	    		node.add(new DefaultMutableTreeNode("Coefficient: " + info.result.getCoefficient()));
        	}
    		node.add(getGroupAssignmentInfo());
    	} else {
    		
        	node.add(new DefaultMutableTreeNode("Cox Proportional Hazards Model"));
        	if(!info.result.isEmptyResults()) {
    	    	node.add(new DefaultMutableTreeNode("L1 Penalty: " + info.result.getL1penalty()));
	    		node.add(new DefaultMutableTreeNode("lambda start value: " + info.result.getLambda()));  
        	} else {
        		node.add(new DefaultMutableTreeNode("No non-zero coefficients calculated."));
        	}
    	}
    	root.add(node);
    }
    
    protected DefaultMutableTreeNode getGroupAssignmentInfo() {
        DefaultMutableTreeNode groupAssignmentInfo = new DefaultMutableTreeNode("Group assignments ");
        DefaultMutableTreeNode notInGroups = new DefaultMutableTreeNode("Not in groups");
        DefaultMutableTreeNode[] groups = new DefaultMutableTreeNode[numGroups];
        //System.out.println("ng "+numGroups);
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
			message += "You need to have 32Bit JVM as default for LIMMA\n";
			message += "Please contact MeV Support if you need help.\n";
			message += "You also need to have R 2.9.x installed for LIMMA\n";
			message += "Cancel if either is not installed. Ok to continue.";
			return JOptionPane.showConfirmDialog(null, message, "R Engine Warning", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
		}
		if(arch.toLowerCase().contains("64")) {
			message += "You need to have 32Bit JVM as default for LIMMA\n";
			message += "Please contact MeV Support if you need help.\n";
			message += "Cancel if 32 Bit JVM is not installed. Ok to continue.";
			return JOptionPane.showConfirmDialog(null, message, "R Engine Warning", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
		}
		if (os.toLowerCase().contains("mac")) {
			message += "You need to have R 2.9.x installed for LIMMA\n";
			message += "Cancel if R is not installed. Ok to continue.";
			return JOptionPane.showConfirmDialog(null, message, "R Engine Warning", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
		}
		return JOptionPane.OK_OPTION;
	}

    protected class GeneralInfo {
    	public SURVAlgorithmData result;
    	
		public float cluster1observed;
		public float cluster2observed;
		public float cluster1expected;
		public float cluster2expected;
		public int cluster1size;
		public int cluster2size;
		public float chisquare;
		public float pvalue;
     
        
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
