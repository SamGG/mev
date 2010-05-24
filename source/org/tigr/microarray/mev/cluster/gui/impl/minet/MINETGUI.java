/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: MINETGUI.java,v $
 * $Revision: 1.10 $
 * $Date: 2006-11-07 17:27:40 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.minet;

import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;

import org.tigr.microarray.mev.annotation.IAnnotation;
import org.tigr.microarray.mev.annotation.MevAnnotation;
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
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmParameters;
import org.tigr.microarray.mev.cluster.clusterUtil.ClusterRepository;
import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.IClusterGUI;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.IDistanceMenu;
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.IViewer;
import org.tigr.microarray.mev.cluster.gui.LeafInfo;
import org.tigr.microarray.mev.cluster.gui.helpers.CentroidUserObject;
import org.tigr.microarray.mev.cluster.gui.helpers.ClusterTableViewer;
import org.tigr.microarray.mev.cluster.gui.impl.bn.CytoscapeWebstart;
import org.tigr.microarray.mev.cluster.gui.impl.bn.NullArgumentException;
import org.tigr.microarray.mev.cluster.gui.impl.bn.SimpleGeneEdge;
import org.tigr.microarray.mev.cluster.gui.impl.bn.XGMMLGenerator;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.DialogListener;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.Progress;
import org.tigr.microarray.mev.cluster.gui.impl.hcl.HCLGUI;
import org.tigr.microarray.mev.cluster.gui.impl.hcl.HCLInitDialog;
import org.tigr.microarray.mev.cluster.gui.impl.hcl.HCLTreeData;
import org.tigr.microarray.mev.cluster.gui.impl.hcl.HCLViewer;
import org.tigr.microarray.mev.script.scriptGUI.IScriptGUI;
import org.tigr.util.FloatMatrix;

/**
 *
 * @author  raktim
 * @version
 */
public class MINETGUI implements IClusterGUI, IScriptGUI {
    
    protected Algorithm algorithm;
    protected Progress progress;
    protected Experiment experiment;
    
    boolean debug = false;
    protected String methodName = null;
    protected String estimatorName = null;
    protected String discretizationName = null;
    protected int bins = 0;
    
    protected ArrayList<String> geneLabels;
    protected ArrayList<String> sampleLabels;
    Vector<String> exptNamesVector;
    protected int[] groupAssignments;
    
    protected IData data;
    protected int dataDesign;
    
    /** Creates new MINETGUI */
    public MINETGUI() {
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
        
        //ClusterRepository repository = ;
        
        MINETInitBox MINETDialog = new MINETInitBox(
        		(JFrame)framework.getFrame(), 
        		true, 
        		exptNamesVector, 
        		framework.getClusterRepository(
        				org.tigr.microarray.mev.cluster.clusterUtil.Cluster.EXPERIMENT_CLUSTER), 
        		framework.getClusterRepository(
        				org.tigr.microarray.mev.cluster.clusterUtil.Cluster.GENE_CLUSTER)
        		);
        MINETDialog.setVisible(true);
        
        if (!MINETDialog.isOkPressed()) return null;
        
        methodName = MINETDialog.methodsPanel.getMethodName();
        estimatorName = MINETDialog.estimatorPanel.getEstimatorName();
        discretizationName = MINETDialog.discretizationPanel.getDiscretizationMethodName();
        
        dataDesign=MINETDialog.getTestDesign();
        if (MINETDialog.getTestDesign()==MINETInitBox.ONE_CLASS){
	        if (MINETDialog.getSelectionDesign()==MINETInitBox.CLUSTER_SELECTION){
	        	groupAssignments=MINETDialog.getClusterOneClassAssignments();
	        }
	        if (MINETDialog.getSelectionDesign()==MINETInitBox.BUTTON_SELECTION){
	        	groupAssignments=MINETDialog.getOneClassAssignments();
	        }
        }
        
        // count # of samples used in analysis
        int samplesUsed = 0;
        for(int i = 0; i < groupAssignments.length; i++) {
        	if(groupAssignments[i] == 1)
        		samplesUsed++;
        }
        System.out.println(samplesUsed + " out of " + groupAssignments.length + " used.");
        if(!discretizationName.equals("none"))
        	bins = (int) Math.round(Math.sqrt(samplesUsed));
        
        int geneIndices[] = MINETDialog.getSelectedCluster().getIndices();
		if(debug) {
	        System.out.println("Selected gene indices from CLuster");
	        for(int i=0; i < geneIndices.length; i++){
	        	System.out.println("Gene " + i + " in Cluster has data index of: " + geneIndices[i]);
	        }
        }
        // Make FloatMAtrix based on genes in cluster - sub matrix
        FloatMatrix fm = new FloatMatrix(geneIndices.length, number_of_samples);
        for(int row=0; row < geneIndices.length; row++) {
        	for(int col=0; col < geneIndices.length; col++) {
        		fm.set(row, col, this.experiment.get(row, col));
        	}
        }
       
        Listener listener = new Listener();
        
        try {
            algorithm = framework.getAlgorithmFactory().getAlgorithm("MINET");
            algorithm.addAlgorithmListener(listener);
            
            this.progress = new Progress(framework.getFrame(), "Running Mutual Information network...", listener);
            this.progress.show();
            
            AlgorithmData data = new AlgorithmData();
            
            //data.addMatrix("experiment", experiment.getMatrix());
            data.addMatrix("experiment", fm);
            
            data.addParam("dataDesign", String.valueOf(dataDesign));
            data.addIntArray("group_assignments", groupAssignments);
            //data.addIntArray("geneIndices", geneIndices);
            data.addParam("methodName", methodName);
            data.addParam("estimatorName", estimatorName);
            data.addParam("discretizationName", discretizationName);
            data.addParam("bins", String.valueOf(bins)); //sqrt(sample size)
            data.addParam("classes", String.valueOf(MINETDialog.getTestDesign()));
            
            data.addStringArray("geneLabels", geneLabels.toArray(new String[geneLabels.size()]));
            data.addStringArray("sampleLabels", sampleLabels.toArray(new String[sampleLabels.size()]));
            
            // run algorithm
            long start = System.currentTimeMillis();
            AlgorithmData result = algorithm.execute(data);
            long time = System.currentTimeMillis() - start;
            
            // getting the results
            FloatMatrix netAdjMatrix = result.getMatrix("network");

        	// 1. Create List of interactions from adjacency matrix
        	ArrayList<String> edgesList = getGeneEdges(netAdjMatrix, geneIndices);
        	
        	// 2. Create XGMML File
        	String netFileDir = System.getProperty("user.dir")+
        							"/data/results"; 
        	// create dir if not there
        	File file = new File(netFileDir);
        	boolean exists = file.exists();
        	if (!exists) {
        		if (!file.mkdir()) {
        			throw new AlgorithmException("Result dir could not be created");
        		}
        	}
        	
        	String netFileName = netFileDir+"/Minet_"+getUniqueFileID()+".xgmml";
        	try {
				fromSimpleEdgeToXgmml(false, edgesList, netFileName);
			} catch (NullArgumentException e) {
				e.printStackTrace();
				throw new AlgorithmException(e);
			} catch (IOException e) {
				e.printStackTrace();
				throw new AlgorithmException(e);
			}
			
        	// 3. Export to Cytoscape
			Vector<String> files = new Vector<String>();
			files.add(netFileName);
			CytoscapeWebstart.onWebstartCytoscapeMINET(files);
           
            GeneralInfo info = new GeneralInfo();
            info.time = time;
            
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
    
    /**
     * 
     * @param netAdjMatrix
     * @return
     */
    private ArrayList<String> getGeneEdges(FloatMatrix netAdjMatrix, int geneIndices[]) {
    	// the interactions on 1 side of the diagonal as it is symmetric
    	int rows = netAdjMatrix.getRowDimension();
    	int cols = netAdjMatrix.getColumnDimension();
    	ArrayList<String> edges = new ArrayList<String>();
    	for(int i=0; i < rows; i++){
    		for(int ii=i; ii < cols; ii++){
    			// create edge with probe index as node id
    			// n pd n where n is probe index
    			// Add if weight of edge in matrix is > 0
    			if (netAdjMatrix.get(i,ii) > 0)
    				// result matrix indices to original data index
    				// use index as edge id
    				edges.add(geneIndices[i] + " pd " + geneIndices[ii]);
    		}
    	}
		return edges;
	}

	public AlgorithmData getScriptParameters(IFramework framework) {
        
        this.experiment = framework.getData().getExperiment();
        exptNamesVector = new Vector<String>();
        int number_of_samples = experiment.getNumberOfSamples();
        //int number_of_genes = experiment.getNumberOfGenes();
        
        for (int i = 0; i < number_of_samples; i++) {
            exptNamesVector.add(framework.getData().getFullSampleName(experiment.getSampleIndex(i)));
        }
        
        MINETInitBox MINETDialog = new MINETInitBox(
        		(JFrame)framework.getFrame(), 
        		true, 
        		exptNamesVector,
        		framework.getClusterRepository(1),
        		framework.getClusterRepository(0)
        		);
        MINETDialog.setVisible(true);
        
        if (!MINETDialog.isOkPressed()) return null;
        
        //float alpha = MINETDialog.getPValue();
        //numTimePoints = MINETDialog.getNumTimePoints();

        if (MINETDialog.getTestDesign()==MINETInitBox.ONE_CLASS){
	        if (MINETDialog.getSelectionDesign()==MINETInitBox.CLUSTER_SELECTION){
	        	groupAssignments=MINETDialog.getClusterOneClassAssignments();
	        }
	        if (MINETDialog.getSelectionDesign()==MINETInitBox.BUTTON_SELECTION){
	        	groupAssignments=MINETDialog.getOneClassAssignments();
	        }
        }
        
        AlgorithmData data = new AlgorithmData();       
        
        // alg name
        data.addParam("name", "MINET");
        
        // alg type
        data.addParam("alg-type", "data-visualization");
        
        // output class
        data.addParam("output-class", "partition-output");
        
        //output nodes
        String [] outputNodes = new String[2];
        outputNodes[0] = "Significant Genes";
        outputNodes[1] = "Non-significant Genes";
        
        data.addStringArray("output-nodes", outputNodes);
        
        return data;
    }
    
	/**
	 * 
	 * @param edgeDir
	 * @param inter
	 * @param fileName
	 * @throws NullArgumentException
	 * @throws IOException
	 */
	public void fromSimpleEdgeToXgmml(boolean edgeDir, ArrayList<String> inter, String fileName) 
		throws NullArgumentException, IOException {
		try {	    
			
			Hashtable<String, MevAnnotation> uniqueNodesWithId = new Hashtable<String, MevAnnotation>();
			
			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(fileName)));
			
			String xgmmlContent = "";
			//System.out.println("Network File Name " + fileName);
			String label = fileName.substring(0, fileName.lastIndexOf("."));
			
			//write header
			xgmmlContent = XGMMLGenerator.createHeader(label, "");
			out.write(xgmmlContent);
			xgmmlContent = "";
			
			// parse the edges for writing and recording the nodes
			for(int i = 0; i < inter.size(); i++){
				String nodeids[] = inter.get(i).split(" pd ");
				int labelFrom = Integer.parseInt(nodeids[0].trim());
				int labelTo = Integer.parseInt(nodeids[1].trim());;
				
				// get node annotations, if not in hash add them
				IAnnotation annoFrom, annoTo;
				// if seeing for first time
				if (!uniqueNodesWithId.containsKey(nodeids[0])) {
					annoFrom = (MevAnnotation) data.getSlideDataElement(0,labelFrom).getElementAnnotation();
					uniqueNodesWithId.put(nodeids[0], (MevAnnotation)annoFrom);
					xgmmlContent = XGMMLGenerator.createNode(
							annoFrom.getGeneSymbol(), nodeids[0], data, labelFrom);
					out.write(xgmmlContent);
				}
				// if seeing for first time
				if (!uniqueNodesWithId.containsKey(nodeids[1])) {
					annoTo = (MevAnnotation) data.getSlideDataElement(0,labelTo).getElementAnnotation();
					uniqueNodesWithId.put(nodeids[1], (MevAnnotation)annoTo);
					xgmmlContent = XGMMLGenerator.createNode(
							annoTo.getGeneSymbol(), nodeids[1], data, labelTo);
					out.write(xgmmlContent);
				}
			}
			
			// traverse list again and write edges
			for(int i = 0; i < inter.size(); i++){
				String nodeids[] = inter.get(i).split(" pd ");
				
				xgmmlContent = XGMMLGenerator.createEdge(
						edgeDir, 
						uniqueNodesWithId.get(nodeids[0]).getGenBankAcc(), 
						uniqueNodesWithId.get(nodeids[1]).getGenBankAcc(),  
						nodeids[0], 
						nodeids[1]);
				
				out.write(xgmmlContent);
			}
			
			// write footer
			xgmmlContent = XGMMLGenerator.getFooter();
			out.write(xgmmlContent);
			
			out.flush();
			out.close();
		}
		catch(IOException ioe){
			throw ioe;
		}
	}
	
	
    public DefaultMutableTreeNode executeScript(IFramework framework, AlgorithmData algData, Experiment experiment) throws AlgorithmException {
        
        Listener listener = new Listener();
        this.experiment = experiment;
        this.data = framework.getData();
        //this.timeAssignments = algData.getIntArray("time_assignments");
        this.groupAssignments = algData.getIntArray("condition_assignments");
        
        exptNamesVector = new Vector<String>();
        int number_of_samples = experiment.getNumberOfSamples();

        for (int i = 0; i < number_of_samples; i++) {
            exptNamesVector.add(this.data.getFullSampleName(i));
        }
 
        try {
            algData.addMatrix("experiment", experiment.getMatrix());
            algorithm = framework.getAlgorithmFactory().getAlgorithm("MINET");
            algorithm.addAlgorithmListener(listener);
            
            this.progress = new Progress(framework.getFrame(), "Running MINET Analysis...", listener);
            this.progress.show();
            
            long start = System.currentTimeMillis();
            AlgorithmData result = algorithm.execute(algData);
            long time = System.currentTimeMillis() - start;
                        
            AlgorithmParameters params = algData.getParams();
            
            GeneralInfo info = new GeneralInfo();            
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
    
    
    protected String getSigMethod(int sigMethod) {
        String methodName = "";
        
        if (sigMethod == MINETInitBox.JUST_ALPHA) {
            methodName = "Just alpha (uncorrected)";
        } else if (sigMethod == MINETInitBox.STD_BONFERRONI) {
            methodName = "Standard Bonferroni correction";
        } else if (sigMethod == MINETInitBox.ADJ_BONFERRONI) {
            methodName = "Adjusted Bonferroni correction";
        } else if (sigMethod == MINETInitBox.MAX_T) {
            methodName = "Westfall Young stepdown - MaxT";
        } else if (sigMethod == MINETInitBox.FALSE_NUM) {
            //methodName = "False significant number: " + falseNum + " or less";
        } else if (sigMethod == MINETInitBox.FALSE_PROP) {
            //methodName = "False significant proportion: " + falseProp + " or less";
        }
        
        return methodName;
    }
    
    /**
     * Creates a result tree to be inserted into the framework analysis node.
     */
    protected DefaultMutableTreeNode createResultTree(GeneralInfo info) {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("MINET");
        addResultNodes(root, info);
        return root;
    }
    
    /**
     * Adds result nodes into the tree root.
     */
    protected void addResultNodes(DefaultMutableTreeNode root, GeneralInfo info) {
        addGeneralInfo(root, info);
    }
    
    /**
     * Adds node with general information.
     */
    protected void addGeneralInfo(DefaultMutableTreeNode root, GeneralInfo info) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode("General Information");
       
        node.add(getConditionAssignmentInfo());
             
        node.add(new DefaultMutableTreeNode("Inference Algorithm: "+methodName));
        node.add(new DefaultMutableTreeNode("Mutual Info Estimator: "+estimatorName));
        node.add(new DefaultMutableTreeNode("Discretization: "+discretizationName));
        if (!discretizationName.equals("none"))
        	node.add(new DefaultMutableTreeNode("Bins: "+bins));
        node.add(new DefaultMutableTreeNode("Time: "+String.valueOf(info.time-1)+" ms"));
        root.add(node);
    }
    
    
    protected DefaultMutableTreeNode getConditionAssignmentInfo() {
        DefaultMutableTreeNode groupAssignmentInfo = new DefaultMutableTreeNode("Group assignments ");
        DefaultMutableTreeNode notInGroups = new DefaultMutableTreeNode("Samples Excluded");
        DefaultMutableTreeNode inGroup;
        inGroup = new DefaultMutableTreeNode("Samples Included");
        groupAssignmentInfo.add(inGroup);
        
        for (int i = 0; i < groupAssignments.length; i++) {
            int currentGroup = groupAssignments[i];
            if (currentGroup == 0)
                notInGroups.add(new DefaultMutableTreeNode((String)(exptNamesVector.get(i))));
            else 
            	inGroup.add(new DefaultMutableTreeNode((String)(exptNamesVector.get(i))));
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
                //monitor.dispose();
            }
        }
        
        public void windowClosing(WindowEvent e) {
            algorithm.abort();
            progress.dispose();
            //monitor.dispose();
        }
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
        protected GeneralInfo(){
    		super();
    	}        
        public String getMethodName() {
            return hcl ? HCLGUI.GeneralInfo.getMethodName(hcl_method) : "no linkage";
        }
        
    }
    
    private static String getUniqueFileID() {
		Date now = new Date();
		String dateString = now.toString();

		SimpleDateFormat formatDt = new SimpleDateFormat("MMM_dd_yy_HHmmssSSS");
		dateString = formatDt.format(now);
		//System.out.println(" 2. " + dateString);
		return dateString;
	}
}
