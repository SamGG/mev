/*
Copyright @ 1999-2005, The Institute for Genomic Research (TIGR).
All rights reserved.
 */
/*
 * $RCSfile: OWAGUI.java,v $
 * $Revision: 1.10 $
 * $Date: 2006-11-07 17:27:40 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.owa;

import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;

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
import org.tigr.microarray.mev.cluster.gui.impl.hcl.HCLGUI;
import org.tigr.microarray.mev.cluster.gui.impl.hcl.HCLInitDialog;
import org.tigr.microarray.mev.cluster.gui.impl.hcl.HCLTreeData;
import org.tigr.microarray.mev.cluster.gui.impl.hcl.HCLViewer;
import org.tigr.microarray.mev.script.scriptGUI.IScriptGUI;
import org.tigr.util.FloatMatrix;

/**
 *
 * @author  nbhagaba
 * @version
 */
public class OWAGUI implements IClusterGUI, IScriptGUI {
    
    protected Algorithm algorithm;
    protected Progress progress;
    protected Experiment experiment;
    protected int[][] clusters;
    protected FloatMatrix means;
    protected FloatMatrix variances;
    
    protected String[] auxTitles;
    protected Object[][] auxData;
    
    protected Vector fValues, rawPValues, adjPValues, dfNumValues, dfDenomValues, ssGroups, ssError;
    protected float[][] geneGroupMeans, geneGroupSDs;
    protected boolean drawSigTreesOnly;
    
    //protected boolean usePerms;
    
    Vector exptNamesVector;
    protected int[] groupAssignments;
    protected int falseNum, correctionMethod;
    protected double falseProp;
    protected IData data;
    protected int numGroups, numPerms;
    /** Creates new OWAGUI */
    public OWAGUI() {
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
        exptNamesVector = new Vector();
        int number_of_samples = experiment.getNumberOfSamples();
        int number_of_genes = experiment.getNumberOfGenes();
        
        int [] columnIndices = experiment.getColumnIndicesCopy(); 
        
        for (int i = 0; i < number_of_samples; i++) {
            exptNamesVector.add(framework.getData().getFullSampleName(columnIndices[i]));
        }
        
        OneWayANOVAInitBox owaDialog = new OneWayANOVAInitBox((JFrame)framework.getFrame(), true, exptNamesVector);
        owaDialog.setVisible(true);
        
        if (!owaDialog.isOkPressed()) return null;
        
        double alpha = owaDialog.getPValue();
        numGroups = owaDialog.getNumGroups();
        groupAssignments = owaDialog.getGroupAssignments();
        boolean usePerms = owaDialog.usePerms();
        int numPerms = 0;
        if (usePerms) {
            numPerms = owaDialog.getNumPerms();
        }
        correctionMethod = owaDialog.getCorrectionMethod();
        if (correctionMethod == OneWayANOVAInitBox.FALSE_NUM) {
            falseNum = owaDialog.getFalseNum();
        }
        if (correctionMethod == OneWayANOVAInitBox.FALSE_PROP) {
            falseProp = owaDialog.getFalseProp();
        }
        boolean isHierarchicalTree = owaDialog.drawTrees();
        drawSigTreesOnly = true;
        if (isHierarchicalTree) {
            drawSigTreesOnly = owaDialog.drawSigTreesOnly();
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
        
        Listener listener = new Listener();
        
        try {
            algorithm = framework.getAlgorithmFactory().getAlgorithm("OWA");
            algorithm.addAlgorithmListener(listener);
            
            this.progress = new Progress(framework.getFrame(), "Finding significant genes", listener);
            this.progress.show();
            
            AlgorithmData data = new AlgorithmData();
            
            data.addMatrix("experiment", experiment.getMatrix());
            data.addParam("distance-factor", String.valueOf(1.0f));
            data.addParam("distance-absolute", String.valueOf(menu.isAbsoluteDistance()));
            
            data.addParam("distance-function", String.valueOf(function));
            data.addIntArray("group-assignments", groupAssignments);
            data.addParam("usePerms", String.valueOf(usePerms));
            data.addParam("numPerms", String.valueOf(numPerms));
            data.addParam("alpha", String.valueOf(alpha));
            data.addParam("correction-method", String.valueOf(correctionMethod));
            data.addParam("numGroups", String.valueOf(numGroups));
            if (correctionMethod == OneWayANOVAInitBox.FALSE_NUM) {
                data.addParam("falseNum", String.valueOf(falseNum));
            }
            if (correctionMethod == OneWayANOVAInitBox.FALSE_PROP) {
                data.addParam("falseProp", String.valueOf((float)falseProp));
            }
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
            
            long start = System.currentTimeMillis();
            AlgorithmData result = algorithm.execute(data);
            long time = System.currentTimeMillis() - start;
            
            // getting the results
            Cluster result_cluster = result.getCluster("cluster");
            NodeList nodeList = result_cluster.getNodeList();
            AlgorithmParameters resultMap = result.getParams();
            int k = 2; //resultMap.getInt("number-of-clusters"); // NEED THIS TO GET THE VALUE OF NUMBER-OF-CLUSTERS
            
            this.clusters = new int[k][];
            for (int i=0; i<k; i++) {
                clusters[i] = nodeList.getNode(i).getFeaturesIndexes();
            }
            this.means = result.getMatrix("clusters_means");
            this.variances = result.getMatrix("clusters_variances");
            //FloatMatrix pValuesMatrix = result.getMatrix("pValues");
            FloatMatrix rawPValuesMatrix = result.getMatrix("rawPValues");
            FloatMatrix adjPValuesMatrix = result.getMatrix("adjPValues");
            //FloatMatrix pValuesMatrix = result.getMatrix("pValues");
            FloatMatrix fValuesMatrix = result.getMatrix("fValues");
            FloatMatrix dfNumMatrix = result.getMatrix("dfNumMatrix");
            FloatMatrix dfDenomMatrix = result.getMatrix("dfDenomMatrix");
            FloatMatrix ssGroupsMatrix = result.getMatrix("ssGroupsMatrix");
            FloatMatrix ssErrorMatrix = result.getMatrix("ssErrorMatrix");
            FloatMatrix geneGroupMeansMatrix = result.getMatrix("geneGroupMeansMatrix");
            FloatMatrix geneGroupSDsMatrix = result.getMatrix("geneGroupSDsMatrix");
            
            rawPValues = new Vector();
            adjPValues = new Vector();
            fValues = new Vector();
            ssGroups = new Vector();
            ssError = new Vector();
            
            geneGroupMeans = new float[geneGroupMeansMatrix.getRowDimension()][geneGroupMeansMatrix.getColumnDimension()];
            geneGroupSDs = new float[geneGroupSDsMatrix.getRowDimension()][geneGroupSDsMatrix.getColumnDimension()];
            
            for (int i = 0; i < geneGroupMeans.length; i++) {
                for (int j = 0; j < geneGroupMeans[i].length; j++) {
                    geneGroupMeans[i][j] = geneGroupMeansMatrix.A[i][j];
                    geneGroupSDs[i][j] = geneGroupSDsMatrix.A[i][j];
                }
            }
            
            for (int i = 0; i < rawPValuesMatrix.getRowDimension(); i++) {
                rawPValues.add(new Float(rawPValuesMatrix.A[i][0]));
                adjPValues.add(new Float(adjPValuesMatrix.A[i][0]));
            }
            
            for (int i = 0; i < fValuesMatrix.getRowDimension(); i++) {
                fValues.add(new Float(fValuesMatrix.A[i][0]));
            }
            
            dfNumValues = new Vector();
            dfDenomValues = new Vector();
            
            for (int i = 0; i < dfNumMatrix.getRowDimension(); i++) {
                dfNumValues.add(new Float(dfNumMatrix.A[i][0]));
                dfDenomValues.add(new Float(dfDenomMatrix.A[i][0]));
                ssGroups.add(new Float(ssGroupsMatrix.A[i][0]));
                ssError.add(new Float(ssErrorMatrix.A[i][0]));
            }
            
            GeneralInfo info = new GeneralInfo();
            info.time = time;
            //ADD MORE INFO PARAMETERS HERE
            info.alpha = alpha;
            info.usePerms = usePerms;
            info.numPerms = numPerms;
            info.correctionMethod = getSigMethod(correctionMethod);
            /*
            info.pValueBasedOn = getPValueBasedOn(isPermut);
            if (isPermut) {
                info.useAllCombs = useAllCombs;
                info.numCombs = numCombs;
            }
             */
            info.function = menu.getFunctionName(function);
            info.hcl = isHierarchicalTree;
            info.hcl_genes = hcl_genes;
            info.hcl_samples = hcl_samples;
            info.hcl_method = hcl_method;
            
            Vector titlesVector = new Vector();
            for (int i = 0; i < geneGroupMeans[0].length; i++) {
                titlesVector.add("Group" + (i+1) + " mean");
                titlesVector.add("Group" + (i + 1) + " std.dev");
            }
            titlesVector.add("F ratio");
            titlesVector.add("SS(Groups)");
            titlesVector.add("SS(Error)");
            titlesVector.add("df (Groups)");
            titlesVector.add("df (Error)");
            titlesVector.add("Raw p value");
            if (!((correctionMethod == OneWayANOVAInitBox.FALSE_NUM)||(correctionMethod == OneWayANOVAInitBox.FALSE_PROP))) {            
                titlesVector.add("Adj. p value");
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
                
                auxData[i][counter++] = fValues.get(i);
                auxData[i][counter++] = ssGroups.get(i);
                auxData[i][counter++] = ssError.get(i);
                auxData[i][counter++] = dfNumValues.get(i);
                auxData[i][counter++] = dfDenomValues.get(i);
                auxData[i][counter++] = rawPValues.get(i);
                if (!((correctionMethod == OneWayANOVAInitBox.FALSE_NUM)||(correctionMethod == OneWayANOVAInitBox.FALSE_PROP))) {
                    auxData[i][counter++] = adjPValues.get(i);
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
        exptNamesVector = new Vector();
        int number_of_samples = experiment.getNumberOfSamples();
        int number_of_genes = experiment.getNumberOfGenes();
        
        for (int i = 0; i < number_of_samples; i++) {
            exptNamesVector.add(framework.getData().getFullSampleName(experiment.getSampleIndex(i)));
        }
        
        OneWayANOVAInitBox owaDialog = new OneWayANOVAInitBox((JFrame)framework.getFrame(), true, exptNamesVector);
        owaDialog.setVisible(true);
        
        if (!owaDialog.isOkPressed()) return null;
        
        double alpha = owaDialog.getPValue();
        numGroups = owaDialog.getNumGroups();
        groupAssignments = owaDialog.getGroupAssignments();
        boolean usePerms = owaDialog.usePerms();     
        int numPerms = 0;
        if (usePerms) {
            numPerms = owaDialog.getNumPerms();           
        }
        correctionMethod = owaDialog.getCorrectionMethod();
        if (correctionMethod == OneWayANOVAInitBox.FALSE_NUM) {
            falseNum = owaDialog.getFalseNum();
        }
        if (correctionMethod == OneWayANOVAInitBox.FALSE_PROP) {
            falseProp = owaDialog.getFalseProp();
        }        
        boolean isHierarchicalTree = owaDialog.drawTrees();
        drawSigTreesOnly = true;
        if (isHierarchicalTree) {
            drawSigTreesOnly = owaDialog.drawSigTreesOnly();
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
        data.addIntArray("group-assignments", groupAssignments);
        data.addParam("usePerms", String.valueOf(usePerms));   
        data.addParam("numPerms", String.valueOf(numPerms));
        data.addParam("alpha", String.valueOf(alpha));
        data.addParam("correction-method", String.valueOf(correctionMethod));
        data.addParam("numGroups", String.valueOf(numGroups));
        if (correctionMethod == OneWayANOVAInitBox.FALSE_NUM) {
            data.addParam("falseNum", String.valueOf(falseNum));
        }
        if (correctionMethod == OneWayANOVAInitBox.FALSE_PROP) {
            data.addParam("falseProp", String.valueOf((float)falseProp));
        }       
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
        data.addParam("name", "ANOVA");
        
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
        this.groupAssignments = algData.getIntArray("group-assignments");
        this.correctionMethod = algData.getParams().getInt("correction-method");
        if (correctionMethod == OneWayANOVAInitBox.FALSE_NUM) {
            falseNum = algData.getParams().getInt("falseNum");
        }
        if (correctionMethod == OneWayANOVAInitBox.FALSE_PROP) {
            falseProp = algData.getParams().getFloat("falseProp");
        }        
        this.drawSigTreesOnly = algData.getParams().getBoolean("draw-sig-trees-only");        
        this.rawPValues = new Vector();
        this.adjPValues=  new Vector();
        
        exptNamesVector = new Vector();
        int number_of_samples = experiment.getNumberOfSamples();

        for (int i = 0; i < number_of_samples; i++) {
            exptNamesVector.add(this.data.getFullSampleName(i));
        }
 
        try {
            algData.addMatrix("experiment", experiment.getMatrix());
            algorithm = framework.getAlgorithmFactory().getAlgorithm("OWA");
            algorithm.addAlgorithmListener(listener);
            
            this.progress = new Progress(framework.getFrame(), "Finding significant genes", listener);
            this.progress.show();
            
            long start = System.currentTimeMillis();
            AlgorithmData result = algorithm.execute(algData);
            long time = System.currentTimeMillis() - start;
            
            // getting the results
            Cluster result_cluster = result.getCluster("cluster");
            NodeList nodeList = result_cluster.getNodeList();
            AlgorithmParameters resultMap = result.getParams();
            int k = 2; //resultMap.getInt("number-of-clusters"); // NEED THIS TO GET THE VALUE OF NUMBER-OF-CLUSTERS
                       
            this.clusters = new int[k][];
            for (int i=0; i<k; i++) {
                clusters[i] = nodeList.getNode(i).getFeaturesIndexes();
            }
            this.means = result.getMatrix("clusters_means");
            this.variances = result.getMatrix("clusters_variances");
            FloatMatrix rawPValuesMatrix = result.getMatrix("rawPValues");
            FloatMatrix adjPValuesMatrix = result.getMatrix("adjPValues");
            FloatMatrix fValuesMatrix = result.getMatrix("fValues");
            FloatMatrix dfNumMatrix = result.getMatrix("dfNumMatrix");
            FloatMatrix dfDenomMatrix = result.getMatrix("dfDenomMatrix");
            FloatMatrix ssGroupsMatrix = result.getMatrix("ssGroupsMatrix");
            FloatMatrix ssErrorMatrix = result.getMatrix("ssErrorMatrix");
            FloatMatrix geneGroupMeansMatrix = result.getMatrix("geneGroupMeansMatrix");
            FloatMatrix geneGroupSDsMatrix = result.getMatrix("geneGroupSDsMatrix");
            
            //pValues = new Vector();
            fValues = new Vector();
            ssGroups = new Vector();
            ssError = new Vector();
            
            geneGroupMeans = new float[geneGroupMeansMatrix.getRowDimension()][geneGroupMeansMatrix.getColumnDimension()];
            geneGroupSDs = new float[geneGroupSDsMatrix.getRowDimension()][geneGroupSDsMatrix.getColumnDimension()];
            
            for (int i = 0; i < geneGroupMeans.length; i++) {
                for (int j = 0; j < geneGroupMeans[i].length; j++) {
                    geneGroupMeans[i][j] = geneGroupMeansMatrix.A[i][j];
                    geneGroupSDs[i][j] = geneGroupSDsMatrix.A[i][j];
                }
            }
            
            for (int i = 0; i < rawPValuesMatrix.getRowDimension(); i++) {
                rawPValues.add(new Float(rawPValuesMatrix.A[i][0]));
                adjPValues.add(new Float(adjPValuesMatrix.A[i][0]));
            }
            
            for (int i = 0; i < fValuesMatrix.getRowDimension(); i++) {
                fValues.add(new Float(fValuesMatrix.A[i][0]));
            }
            
            dfNumValues = new Vector();
            dfDenomValues = new Vector();
            
            for (int i = 0; i < dfNumMatrix.getRowDimension(); i++) {
                dfNumValues.add(new Float(dfNumMatrix.A[i][0]));
                dfDenomValues.add(new Float(dfDenomMatrix.A[i][0]));
                ssGroups.add(new Float(ssGroupsMatrix.A[i][0]));
                ssError.add(new Float(ssErrorMatrix.A[i][0]));
            }
            
            AlgorithmParameters params = algData.getParams();
            
            GeneralInfo info = new GeneralInfo();
            info.time = time;
            //ADD MORE INFO PARAMETERS HERE
            info.alpha = params.getFloat("alpha");
            numGroups = params.getInt("numGroups");
            info.correctionMethod = getSigMethod(params.getInt("correction-method"));
            info.usePerms = params.getBoolean("usePerms");
            info.numPerms = params.getInt("numPerms");
            /*
            info.pValueBasedOn = getPValueBasedOn(isPermut);
            if (isPermut) {
                info.useAllCombs = useAllCombs;
                info.numCombs = numCombs;
            }
             */
            info.function = framework.getDistanceMenu().getFunctionName(params.getInt("distance-function"));
            info.hcl = params.getBoolean("hierarchical-tree");
            info.hcl_genes = params.getBoolean("calculate-genes");
            info.hcl_samples = params.getBoolean("calculate-experiments");
            if(info.hcl)
                info.hcl_method = params.getInt("method-linkage") ;
            
            Vector titlesVector = new Vector();
            for (int i = 0; i < geneGroupMeans[0].length; i++) {
                titlesVector.add("Group" + (i+1) + " mean");
                titlesVector.add("Group" + (i + 1) + " std.dev");
            }
            titlesVector.add("F ratio");
            titlesVector.add("SS(Groups)");
            titlesVector.add("SS(Error)");
            titlesVector.add("df (Groups)");
            titlesVector.add("df (Error)");
            titlesVector.add("Raw p value");
            if (!((correctionMethod == OneWayANOVAInitBox.FALSE_NUM)||(correctionMethod == OneWayANOVAInitBox.FALSE_PROP))) {            
                titlesVector.add("Adj. p value");
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
                
                auxData[i][counter++] = fValues.get(i);
                auxData[i][counter++] = ssGroups.get(i);
                auxData[i][counter++] = ssError.get(i);
                auxData[i][counter++] = dfNumValues.get(i);
                auxData[i][counter++] = dfDenomValues.get(i);
                auxData[i][counter++] = rawPValues.get(i);
                if (!((correctionMethod == OneWayANOVAInitBox.FALSE_NUM)||(correctionMethod == OneWayANOVAInitBox.FALSE_PROP))) {
                    auxData[i][counter++] = adjPValues.get(i);
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
    
    
    protected String getSigMethod(int sigMethod) {
        String methodName = "";
        
        if (sigMethod == OneWayANOVAInitBox.JUST_ALPHA) {
            methodName = "Just alpha (uncorrected)";
        } else if (sigMethod == OneWayANOVAInitBox.STD_BONFERRONI) {
            methodName = "Standard Bonferroni correction";
        } else if (sigMethod == OneWayANOVAInitBox.ADJ_BONFERRONI) {
            methodName = "Adjusted Bonferroni correction";
        } else if (sigMethod == OneWayANOVAInitBox.MAX_T) {
            methodName = "Westfall Young stepdown - MaxT";
        } else if (sigMethod == OneWayANOVAInitBox.FALSE_NUM) {
            methodName = "False significant number: " + falseNum + " or less";
        } else if (sigMethod == OneWayANOVAInitBox.FALSE_PROP) {
            methodName = "False significant proportion: " + falseProp + " or less";
        }
        
        return methodName;
    }
    
    /**
     * Creates a result tree to be inserted into the framework analysis node.
     */
    protected DefaultMutableTreeNode createResultTree(Cluster result_cluster, GeneralInfo info) {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("One-way ANOVA");
        addResultNodes(root, result_cluster, info);
        return root;
    }
    
    /**
     * Adds result nodes into the tree root.
     */
    protected void addResultNodes(DefaultMutableTreeNode root, Cluster result_cluster, GeneralInfo info) {
        addExpressionImages(root);
        addHierarchicalTrees(root, result_cluster, info);
        addCentroidViews(root);
        addTableViews(root);
        addClusterInfo(root);
        //addFRatioInfoViews(root);
        addGeneralInfo(root, info);
    }
    
    protected void addTableViews(DefaultMutableTreeNode root) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode("Table Views");
        IViewer tabViewer = new ClusterTableViewer(this.experiment, this.clusters, this.data, this.auxTitles, this.auxData);
        for (int i=0; i<this.clusters.length; i++) {
            if (i < this.clusters.length - 1) {
                node.add(new DefaultMutableTreeNode(new LeafInfo("Significant Genes ", tabViewer, new Integer(i))));
            } else if (i == this.clusters.length - 1) {
                node.add(new DefaultMutableTreeNode(new LeafInfo("Non-significant Genes ", tabViewer, new Integer(i))));
                
            }
        }
        root.add(node);
    }
    
    /**
     * Adds nodes to display clusters data.
     */
    protected void addExpressionImages(DefaultMutableTreeNode root) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode("Expression Images");
        IViewer expViewer = new OWAExperimentViewer(this.experiment, this.clusters, geneGroupMeans, geneGroupSDs, rawPValues, adjPValues, fValues, ssGroups, ssError, dfNumValues, dfDenomValues);
        for (int i=0; i<this.clusters.length; i++) {
            if (i < this.clusters.length - 1) {
                node.add(new DefaultMutableTreeNode(new LeafInfo("Significant Genes ", expViewer, new Integer(i))));
            } else if (i == this.clusters.length - 1) {
                node.add(new DefaultMutableTreeNode(new LeafInfo("Non-significant Genes ", expViewer, new Integer(i))));
                
            }
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
    protected void addClusterInfo(DefaultMutableTreeNode root) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode("Cluster Information");
        node.add(new DefaultMutableTreeNode(new LeafInfo("Results (#,%)", new OWAInfoViewer(this.clusters, this.experiment.getNumberOfGenes()))));
        root.add(node);
    }
    
    /**
     * Adds nodes to display centroid charts.
     */
    protected void addCentroidViews(DefaultMutableTreeNode root) {
        DefaultMutableTreeNode centroidNode = new DefaultMutableTreeNode("Centroid Graphs");
        DefaultMutableTreeNode expressionNode = new DefaultMutableTreeNode("Expression Graphs");
        OWACentroidViewer centroidViewer = new OWACentroidViewer(this.experiment, clusters, geneGroupMeans, geneGroupSDs, rawPValues, adjPValues, fValues, ssGroups, ssError, dfNumValues, dfDenomValues);
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
        
        OWACentroidsViewer centroidsViewer = new OWACentroidsViewer(this.experiment, clusters, geneGroupMeans, geneGroupSDs, rawPValues, adjPValues, fValues, ssGroups, ssError, dfNumValues, dfDenomValues);
        centroidsViewer.setMeans(this.means.A);
        centroidsViewer.setVariances(this.variances.A);
        
        centroidNode.add(new DefaultMutableTreeNode(new LeafInfo("All Genes", centroidsViewer, new Integer(CentroidUserObject.VARIANCES_MODE))));
        expressionNode.add(new DefaultMutableTreeNode(new LeafInfo("All Genes", centroidsViewer, new Integer(CentroidUserObject.VALUES_MODE))));
        
        root.add(centroidNode);
        root.add(expressionNode);
    }
    
    /*
    protected void addFRatioInfoViews(DefaultMutableTreeNode root) {
        DefaultMutableTreeNode fRatioInfoNode = new DefaultMutableTreeNode("F-Ratio information");
        IViewer fSigViewer = new FStatsTableViewer(this.experiment, this.clusters, this.data, geneGroupMeans, geneGroupSDs, pValues, fValues, ssGroups, ssError, dfNumValues, dfDenomValues, true);
        IViewer fNonSigViewer = new FStatsTableViewer(this.experiment, this.clusters, this.data, geneGroupMeans, geneGroupSDs, pValues, fValues, ssGroups, ssError, dfNumValues, dfDenomValues, false);
        
        fRatioInfoNode.add(new DefaultMutableTreeNode(new LeafInfo("Significant Genes", fSigViewer)));
        fRatioInfoNode.add(new DefaultMutableTreeNode(new LeafInfo("Non-significant Genes", fNonSigViewer)));
        
        root.add(fRatioInfoNode);
    }
     */
    
    /**
     * Adds node with general iformation.
     */
    protected void addGeneralInfo(DefaultMutableTreeNode root, GeneralInfo info) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode("General Information");
        node.add(getGroupAssignmentInfo());
        //node.add(new DefaultMutableTreeNode("Alpha (overall threshold p-value): "+info.alpha));
        if (info.correctionMethod.startsWith("False")) {
            node.add(new DefaultMutableTreeNode("Confidence (1 - alpha) : "+(1d - info.alpha)*100 + " %"));
        } else {
            node.add(new DefaultMutableTreeNode("Alpha (overall threshold p-value): "+info.alpha));
        }        
        node.add(new DefaultMutableTreeNode("Used permutation test? " + info.usePerms));
        if (info.usePerms) {
            node.add(new DefaultMutableTreeNode("Number of permutations " + info.numPerms));
        }
        /*
        node.add(new DefaultMutableTreeNode("P-values based on: "+info.pValueBasedOn));
        if (isPermutations) {
            node.add(new DefaultMutableTreeNode("All permutations used: " + info.useAllCombs));
            node.add(new DefaultMutableTreeNode("Number of permutations per gene: " + info.numCombs));
        }
         */
        if (info.correctionMethod.startsWith("False")) {
           node.add(new DefaultMutableTreeNode(info.correctionMethod)); 
        } else {
            node.add(new DefaultMutableTreeNode("Significance determined by: "+info.correctionMethod));
        }        
        //node.add(new DefaultMutableTreeNode("Significance determined by: "+info.correctionMethod));
        node.add(new DefaultMutableTreeNode("HCL: "+info.getMethodName()));
        node.add(new DefaultMutableTreeNode("Time: "+String.valueOf(info.time)+" ms"));
        node.add(new DefaultMutableTreeNode(info.function));
        root.add(node);
    }
    
    protected DefaultMutableTreeNode getGroupAssignmentInfo() {
        DefaultMutableTreeNode groupAssignmentInfo = new DefaultMutableTreeNode("Group assignments ");
        DefaultMutableTreeNode notInGroups = new DefaultMutableTreeNode("Not in groups");
        DefaultMutableTreeNode[] groups = new DefaultMutableTreeNode[numGroups];
        for (int i = 0; i < numGroups; i++) {
            groups[i] = new DefaultMutableTreeNode("Group " + (i + 1));
            
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
        //public String pValueBasedOn;
        public double alpha;
        //public int numCombs;
        //public boolean useAllCombs;
        public long time;
        public String function;
        //public int numReps;
        //public double thresholdPercent;
        
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
