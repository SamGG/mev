/*
Copyright @ 1999-2004, The Institute for Genomic Research (TIGR).
All rights reserved.
 */
/*
 * ScriptManager.java
 *
 * Created on February 28, 2004, 12:16 AM
 */

package org.tigr.microarray.mev.script;

import java.awt.Frame;
import java.awt.Insets;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextPane;
import javax.swing.tree.DefaultMutableTreeNode;

import org.tigr.microarray.mev.DetectionFilter;
import org.tigr.microarray.mev.FoldFilter;
import org.tigr.microarray.mev.SetDetectionFilterDialog;
import org.tigr.microarray.mev.SetFoldFilterDialog;
import org.tigr.microarray.mev.SetLowerCutoffsDialog;
import org.tigr.microarray.mev.SetPercentageCutoffsDialog;
import org.tigr.microarray.mev.TMEV;
import org.tigr.microarray.mev.action.ActionManager;
import org.tigr.microarray.mev.cluster.algorithm.Algorithm;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmData;
import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.IViewer;
import org.tigr.microarray.mev.cluster.gui.LeafInfo;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.Progress;
import org.tigr.microarray.mev.script.scriptGUI.CentroidEntropyRankingInitDialog;
import org.tigr.microarray.mev.script.scriptGUI.DiversityRankingInitDialog;
import org.tigr.microarray.mev.script.scriptGUI.IScriptGUI;
import org.tigr.microarray.mev.script.scriptGUI.ScriptAlgorithmInitDialog;
import org.tigr.microarray.mev.script.scriptGUI.ScriptAttributeDialog;
import org.tigr.microarray.mev.script.scriptGUI.ScriptTable;
import org.tigr.microarray.mev.script.scriptGUI.ScriptTreeViewer;
import org.tigr.microarray.mev.script.scriptGUI.ScriptXMLViewer;
import org.tigr.microarray.mev.script.util.AlgorithmNode;
import org.tigr.microarray.mev.script.util.ErrorLog;
import org.tigr.microarray.mev.script.util.ParameterAttributes;
import org.tigr.microarray.mev.script.util.ParameterValidator;
import org.tigr.microarray.mev.script.util.ScriptConstants;
import org.tigr.microarray.mev.script.util.ScriptNode;
import org.tigr.microarray.mev.script.util.ScriptRunner;
import org.tigr.microarray.mev.script.util.ScriptTree;


/** The ScriptManager class acts as a conduit to facilitate interaction between
 * the objects below the script package and the rest of MeV including data structures
 * and gui aspects.  The interactions are primarily parameter fetching and script
 * execution via classe that interface with <CODE>IScripGUI</CODE> implementaions
 * which are algorithms within MeV which implement scripting support as specified in
 * the interface.
 * @author braisted
 */
public class ScriptManager implements Serializable {
    public static final long serialVersionUID = 10001020103010001L;
    
    /** Holds the Mev' ResultTree script mount point.
     */
    private DefaultMutableTreeNode scriptManagerNode;
    /** Mev's <CODE>ActionManager</CODE>. Supports algorithm interactions.
     */
    private ActionManager actionManager;
    /** Contains multiple script objects.
     */
    private ScriptTable table;
    /** Count of current scripts.
     */
    private int scriptNum;
    /** MeV's framework communication conduit.
     */
    private IFramework framework;
    /** Validation support class.
     */
    private ParameterValidator validator;
    /** Progress meter.
     */
    private Progress progress;
    /** Vector data structure of current scripts.
     */
    private Vector scripts;
    
    /** Creates a new instance of ScriptManager
     * @param framework MeV's main framework object.  This framework is MeV's
     * analog to scripting's ScriptManager as it serves as a
     * communication conduit.
     * @param scriptNode This is the <CODE>DefaultMutableTreeNode</CODE> associated with script activities that will
     * exist on the <CODE>ResultTree</CODE> object.  The refer.
     *
     * @param manager MeV's <CODE>ActionManager</CODE>.  This permits access to MeV's algorithm GUI
     * implementations which optionally implement <CODE>IScriptGUI</CODE> to support scripting.
     */
    public ScriptManager(IFramework framework, DefaultMutableTreeNode scriptNode, ActionManager manager) {
        this.framework = framework;
        this.actionManager = manager;
        this.scriptManagerNode = scriptNode;
        scripts = new Vector();
        scriptNum = 1;
    }
    
    /** Default Constructor.
     */
    public ScriptManager() {
        
    }
    
    /** Loads a script following File selection.
     */
    public void loadScript() {
        JFileChooser chooser = new JFileChooser(TMEV.getFile("data/scripts/"));
        chooser.setMultiSelectionEnabled(false);
        boolean loadState;
        if( chooser.showOpenDialog(framework.getFrame()) == JFileChooser.APPROVE_OPTION ) {
            loadXML(chooser.getSelectedFile());
        }
    }
    
    
    /** Loads the XML script specified by File object.
     * Note that real-time validation occurs on loading the script.
     * @param file XML file object.
     */
    public void loadXML(File file) {
        Thread thread = new Thread(new ThreadHandler(file));
        thread.setPriority(Thread.MIN_PRIORITY);
        this.progress = new Progress(framework.getFrame(), "Script Loading and Validation", null);
        this.progress.show();
        thread.start();
    }
    
    
    
    /** Internal class to handle thread for script loading.
     */
    public class ThreadHandler implements Runnable {
        private final File file;
        public ThreadHandler(File f) {
            file = f;
        }
        public void run() {
            loadScript(file);
        }
    }
    
    
    /** Supports script loading called from a thread.
     * @param inputFile
     * @return  */
    private boolean loadScript(File inputFile) {
        
        ScriptDocument newScriptDoc = new ScriptDocument(scriptNum, inputFile.getPath(), this);
        ErrorLog errorLog = newScriptDoc.getErrorLog();
        boolean validationErrors = false;
        
        try {
            newScriptDoc.loadXMLFile(inputFile, this.progress);
            
            //if loaded with errors get the error log from the document
            if(newScriptDoc.getErrorCount() > 0) {
                if(errorLog.hasErrors() || errorLog.hasFatalErrors()) {
                    validationErrors = true;
                    progress.dispose();
                }
                errorLog.reportAllListings();
            }
        } catch (Exception e) {
            //catch non parse errors such as IO errors
            JOptionPane.showMessageDialog(framework.getFrame(), "Script loading has been aborted due to parse errors.",
            "Script Parse Error", JOptionPane.INFORMATION_MESSAGE);
            
            progress.dispose();
            return false;
        }
        
        //Exit if there were validation or fatal errors.
        if(validationErrors) {
            JOptionPane.showMessageDialog(framework.getFrame(), "Script loading has been aborted.  If the script has been \n"+
            "repaired to correct the errors you can attempt to load it again.", "Script Validation Error", JOptionPane.INFORMATION_MESSAGE);
            return false;
        }
        
        newScriptDoc.setDocumentFileName(inputFile.getPath());
        
        if(validator == null) {
            //construct a parameter validator
            validator = new ParameterValidator();
            validator.loadParameterConstraints();
        }
        
        ScriptTree tree = new ScriptTree(newScriptDoc, this);
        if(!validateParameters(tree, errorLog)) {
            this.progress.dispose();
            JOptionPane.showMessageDialog(framework.getFrame(), "Script loading has been aborted.  If the script has been \n"+
            "repaired to correct the errors you can attempt to load it again.", "Parmameter Validataion Error", JOptionPane.INFORMATION_MESSAGE);
            return false;
        }
        
        //Construct the table if needed
        if(table == null) {
            table = new ScriptTable(this, scripts);
            DefaultMutableTreeNode tableNode = new DefaultMutableTreeNode(new LeafInfo("Script Table", table));
            framework.addNode(scriptManagerNode,tableNode);
        }
        
        ScriptTreeViewer treeViewer = new ScriptTreeViewer(tree, this);
        ScriptXMLViewer xmlViewer = new ScriptXMLViewer(newScriptDoc, this);
        
        DefaultMutableTreeNode scriptNode = new DefaultMutableTreeNode(new LeafInfo("Script ("+scriptNum+")"));
        DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode(new LeafInfo("Script Tree Viewer", treeViewer));
        DefaultMutableTreeNode xmlNode = new DefaultMutableTreeNode(new LeafInfo("Script XML Viewer", xmlViewer));
        
        scriptNode.add(treeNode);
        scriptNode.add(xmlNode);
        
        scripts.add(new Script(newScriptDoc, tree, xmlViewer));
        
        framework.addNode(scriptManagerNode, scriptNode);
        framework.setTreeNode(treeNode);
        scriptNum++;
        
        validator.checkAlgorithmsForDataDependance(tree, this);
        
        return true;
    }
    
    /** Prompts for a new script object to be created.
     * Initialized structures are put in place.
     */
    public void addNewScript() {
        ScriptDocument newScriptDoc;
        ScriptAttributeDialog dialog = new ScriptAttributeDialog();
        if(dialog.showModal() == JOptionPane.OK_OPTION) {
            newScriptDoc = new ScriptDocument(scriptNum, dialog.getName(), dialog.getFormattedDescription(), dialog.getDate(), this);
            
            if(table == null) {
                table = new ScriptTable(this, scripts);
                DefaultMutableTreeNode tableNode = new DefaultMutableTreeNode(new LeafInfo("Script Table", table));
                framework.addNode(scriptManagerNode,tableNode);
                
                //construct a parameter validator
                validator = new ParameterValidator();
                validator.loadParameterConstraints();
            }
            
            ScriptTree tree = new ScriptTree(newScriptDoc, this);
            ScriptTreeViewer treeViewer = new ScriptTreeViewer(tree, this);
            ScriptXMLViewer xmlViewer = new ScriptXMLViewer(newScriptDoc, this);
            
            DefaultMutableTreeNode scriptNode = new DefaultMutableTreeNode(new LeafInfo("Script ("+scriptNum+")"));
            DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode(new LeafInfo("Script Tree Viewer", treeViewer));
            DefaultMutableTreeNode xmlNode = new DefaultMutableTreeNode(new LeafInfo("Script XML Viewer", xmlViewer));
            
            scripts.add(new Script(newScriptDoc, tree, xmlViewer));
            
            scriptNode.add(treeNode);
            scriptNode.add(xmlNode);
            
            framework.addNode(scriptManagerNode, scriptNode);
            framework.setTreeNode(treeNode);
            
            scriptNum++;
        }
    }
    
    /** Provides the next Script ID number.
     */
    public int getNextScriptID() {
        scriptNum++;
        return scriptNum;
    }
    
    
    /** Saves the script to a file to be specified via a prompt.
     * @param doc <CODE>ScriptDocument</CODE> to save.
     */
    public void saveScript(ScriptDocument doc) {
        JFileChooser chooser = new JFileChooser(TMEV.getFile("data/scripts/"));
        if(chooser.showSaveDialog(new JFrame()) == JFileChooser.APPROVE_OPTION) {
            try {
                writeScript(chooser.getSelectedFile(), doc);
            } catch (IOException ioe) {
                JOptionPane.showMessageDialog(new JFrame(), "An error occured while saving the script to file", "Save Error", JOptionPane.WARNING_MESSAGE);
            }
        }
    }
    
    
    /** Supports script output to specified File.
     * @param file Output file
     * @param doc <CODE>ScriptDocument</CODE> to output.
     * @throws IOException
     */
    private void writeScript(File file, ScriptDocument doc) throws IOException {
        BufferedWriter bfr = new BufferedWriter( new FileWriter(file));
        bfr.write(doc.toString());
        bfr.flush();
        bfr.close();
    }
    
    
    /** Prompts the user to select a new algorithm to add during script
     * creation.  Then the method returns the collection of parameters.
     * @param parentNodeOutputClass Parent input data class used to restrict options to
     * appropriate algorithms.
     * @return
     */
    public AlgorithmData getAlgorithm(String parentNodeOutputClass) {
        
        AlgorithmData data = null;
        
        if(this.framework.getData().getFeaturesCount() == 0) {
            JTextPane pane = new JTextPane();
            pane.setContentType("text/html");
            //pane.setBackground(Color.lightGray);
            pane.setMargin(new Insets(5,15,15,15));
            // pane.setBorder(BorderFactory.createLineBorder(Color.black));
            String text = "<html><center><h2>Expression Data Unavailable</h2></center><hr size=3>";
            text += "<center>Expression data has not been loaded.  Some algorithms require information about the <br> " +
            "number and order of loaded experiments in order to set parameters such as group assignments. <br><br>" +
            "<b>Please load data before proceeding with script construction.</b></center></html>";
            pane.setText(text);
            
            JOptionPane.showMessageDialog(framework.getFrame(), pane, "Data Unavailable", JOptionPane.INFORMATION_MESSAGE);
            
            return null;
        }
        int dataType = framework.getData().getDataType();
        boolean isAffy = false;
        if( dataType == IData.DATA_TYPE_AFFY_ABS
        || dataType == IData.DATA_TYPE_AFFY_REF
        || dataType == IData.DATA_TYPE_AFFY_MEAN
        || dataType == IData.DATA_TYPE_AFFY_MEDIAN) {
            isAffy = true;
        }
        
        ScriptAlgorithmInitDialog dialog = new ScriptAlgorithmInitDialog(actionManager, parentNodeOutputClass, isAffy);
        
        if(dialog.showModal() == JOptionPane.OK_OPTION) {
            String algName = dialog.getAlgorithmName();
            String algType = dialog.getAlgorithmType();
            
            
            if(algType.equals(ScriptConstants.ALGORITHM_TYPE_CLUSTER)) {
                int algIndex = dialog.getAlgorithmIndex();
                Action action = actionManager.getAction(ActionManager.ANALYSIS_ACTION+String.valueOf(algIndex));
                if(action == null){
                    return null;
                }
                String className = (String)action.getValue(ActionManager.PARAMETER);
                try {
                	ClassLoader cl = Thread.currentThread().getContextClassLoader();
                    Class clazz = Class.forName(className, true, cl);
                    IScriptGUI gui = (IScriptGUI)clazz.newInstance();
                    data = gui.getScriptParameters(framework);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(framework.getFrame(), "Can't retrieve script parameters for the "+algName+ " algorithm", "Script Parameter Error", JOptionPane.WARNING_MESSAGE);
                    e.printStackTrace();
                }
            } else if(algType.equals(ScriptConstants.ALGORITHM_TYPE_ADJUSTMENT)) {
                
                if(algName.equals("Percentage Cutoff")) {
                    SetPercentageCutoffsDialog percDialog = new SetPercentageCutoffsDialog(new JFrame(), 0.0f);
                    if(percDialog.showModal() == JOptionPane.OK_OPTION) {
                        data = new AlgorithmData();
                        data.addParam("name", algName);
                        float percentage = percDialog.getPercentageCutoff();
                        data.addParam("percent-cutoff", String.valueOf(percentage));
                        setAdjustmentOutput(data);
                    }
                } else if(algName.equals("Lower Cutoffs")) {
                    SetLowerCutoffsDialog lowerDialog = new SetLowerCutoffsDialog(new JFrame(), 0.0f, 0.0f);
                    if(lowerDialog.showModal() == JOptionPane.OK_OPTION) {
                        data = new AlgorithmData();
                        data.addParam("name", algName);
                        data.addParam("cy3-lower-cutoff", String.valueOf(lowerDialog.getLowerCY3Cutoff()));
                        data.addParam("cy5-lower-cutoff", String.valueOf(lowerDialog.getLowerCY5Cutoff()));
                        setAdjustmentOutput(data);
                    }
                } else if(algName.equals("Detection Filter")) {
                    data = new AlgorithmData();
                    
                    IData idata = framework.getData();
                    int featuresCount = idata.getFeaturesCount();
                    String [] expNames = new String[featuresCount];
                    for(int i = 0; i < expNames.length; i++) {
                        expNames[i] = idata.getSampleName(i);
                    }
                    SetDetectionFilterDialog sdfd = new SetDetectionFilterDialog(new JFrame(), expNames, new DetectionFilter(expNames));
                    if(sdfd.showModal() == JOptionPane.OK_OPTION) {
                        DetectionFilter detFilter = sdfd.getDetectionFilter();
                        
                        int [] groupMemb = new int[featuresCount];
                        int [] numReq = new int[featuresCount];
                        
                        for(int i=0; i<groupMemb.length; i++) {
                            groupMemb[i] = detFilter.get_group_membership(i);
                            numReq[i] = detFilter.get_num_required(i);
                        }
                        data.addParam("name", "Affy Detection Filter");
                        data.addIntArray("group-memberships", groupMemb);
                        data.addIntArray("number-required", numReq);
                        data.addParam("is-required-in-both-groups", String.valueOf(detFilter.get_both()));
                        setAdjustmentOutput(data);
                    }
                } else if(algName.equals("Fold Filter")) {
                    data = new AlgorithmData();
                    
                    IData idata = framework.getData();
                    int featuresCount = idata.getFeaturesCount();
                    String [] expNames = new String[featuresCount];
                    for(int i = 0; i < expNames.length; i++) {
                        expNames[i] = idata.getSampleName(i);
                    }
                    SetFoldFilterDialog sffd = new SetFoldFilterDialog(new JFrame(), expNames, new FoldFilter(expNames));
                    if(sffd.showModal() == JOptionPane.OK_OPTION) {
                        FoldFilter foldFilter = sffd.getFoldFilter();
                        
                        int [] groupMemb = new int[featuresCount];
                        int [] numMembers= new int[featuresCount];
                        
                        for(int i=0; i<groupMemb.length; i++) {
                            groupMemb[i] = foldFilter.get_group_membership(i);
                            numMembers[i] = foldFilter.get_num_members(i);
                        }
                        data.addParam("name", "Affy Fold Filter");
                        data.addIntArray("group-memberships", groupMemb);
                        data.addIntArray("number-of-members", numMembers);
                        data.addParam("fold-change", String.valueOf(foldFilter.get_fold_change()));
                        data.addParam("divider-string", foldFilter.get_divider());
                        setAdjustmentOutput(data);
                    }
                    
                } else {
                    data = new AlgorithmData();
                    data.addParam("name", algName);
                    setAdjustmentOutput(data);
                }
            } else if(algType.equals(ScriptConstants.ALGORITHM_TYPE_CLUSTER_SELECTION)) {
                
                if(algName.equals("Diversity Ranking Cluster Selection")) {
                    DiversityRankingInitDialog selectDialog = new DiversityRankingInitDialog(new JFrame());
                    if(selectDialog.showModal() == JOptionPane.OK_OPTION) {
                        data = new AlgorithmData();
                        data.addParam("name", algName);
                        data.addParam("desired-cluster-count", String.valueOf(selectDialog.getClusterNumber()));
                        data.addParam("minimum-cluster-size", String.valueOf(selectDialog.getClusterSize()));
                        data.addParam("use-centroid-variance", String.valueOf(selectDialog.isCentroidBased()));
                        
                        int function = this.framework.getDistanceMenu().getDistanceFunction();
                        if(function == Algorithm.DEFAULT)
                            function = Algorithm.EUCLIDEAN;
                        
                        data.addParam("distance-function", String.valueOf(function));
                        data.addParam("use-absolute", String.valueOf(this.framework.getDistanceMenu().isAbsoluteDistance()));
                        
                        //indicate if input clusters are genes or exps.
                        if(parentNodeOutputClass.equals(ScriptConstants.OUTPUT_DATA_CLASS_GENE_MULTICLUSTER_OUTPUT))
                            data.addParam("process-gene-clusters", String.valueOf(true));
                        else
                            data.addParam("process-gene-clusters", String.valueOf(false));
                        
                        setSelectionOutput(data);
                    }
                } else if(algName.equals("Centroid Entropy/Variance Ranking Cluster Selection")) {
                    CentroidEntropyRankingInitDialog entropyDialog = new CentroidEntropyRankingInitDialog(new JFrame());
                    if(entropyDialog.showModal() == JOptionPane.OK_OPTION) {
                        data = new AlgorithmData();
                        data.addParam("name", algName);
                        data.addParam("desired-cluster-count", String.valueOf(entropyDialog.getClusterNumber()));
                        data.addParam("minimum-cluster-size", String.valueOf(entropyDialog.getClusterSize()));
                        data.addParam("use-centroid-variance", String.valueOf(entropyDialog.isVarianceBased()));
                        
                        int function = this.framework.getDistanceMenu().getDistanceFunction();
                        if(function == Algorithm.DEFAULT)
                            function = Algorithm.EUCLIDEAN;
                        
                        data.addParam("distance-function", String.valueOf(function));
                        data.addParam("use-absolute", String.valueOf(this.framework.getDistanceMenu().isAbsoluteDistance()));
                        
                        //indicate if input clusters are genes or exps.
                        if(parentNodeOutputClass.equals(ScriptConstants.OUTPUT_DATA_CLASS_GENE_MULTICLUSTER_OUTPUT))
                            data.addParam("process-gene-clusters", String.valueOf(true));
                        else
                            data.addParam("process-gene-clusters", String.valueOf(false));
                        
                        setSelectionOutput(data);
                    }
                }
            }
        }
        
        // use for parameter varification dumpParams(data.getParams());
        return data;
    }
    
    
    /** Adds parameters common to all data adjustment algorithms.
     * @param data Parameter container.
     */
    private void setAdjustmentOutput(AlgorithmData data) {
        String [] output_nodes = new String[1];
        output_nodes[0] = "Single Adjusted Ouput";
        data.addStringArray("output-nodes", output_nodes);
        data.addParam("output-class", ScriptConstants.OUTPUT_DATA_CLASS_SINGLE_OUTPUT);
        data.addParam("alg-type", ScriptConstants.ALGORITHM_TYPE_ADJUSTMENT);
    }
    
    /** Sets the parameters common to all Cluster Selection algorithms.
     * @param data AlgorithmData parameter container.
     */
    private void setSelectionOutput(AlgorithmData data) {
        int clusterCount = data.getParams().getInt("desired-cluster-count");
        String [] output_nodes = new String[clusterCount];
        for(int i = 0; i < clusterCount; i++)
            output_nodes[i] = "Selected Cluster ("+String.valueOf(i+1)+") ";
        data.addStringArray("output-nodes", output_nodes);
        data.addParam("output-class", ScriptConstants.OUTPUT_DATA_CLASS_CLUSTER_SELECTION_OUTPUT);
        data.addParam("alg-type", ScriptConstants.ALGORITHM_TYPE_CLUSTER_SELECTION);
    }
    
    /** Facilitates the jump from the <CODE>ScriptTreeViewer</CODE> to the
     * <CODE>ScriptXMLViewer</CODE>.
     * @param viewer ScriptTreeViewer object.
     * @param node script node on the tree to highlight.
     */
    public void viewSelectedNodeXML(ScriptTreeViewer viewer, ScriptNode node) {
        IViewer iviewer;
        ScriptXMLViewer xmlViewer;
        DefaultMutableTreeNode viewerNode = getSiblingXMLNode(viewer);
        if(viewerNode != null) {
            iviewer = ((LeafInfo)(viewerNode.getUserObject())).getViewer();
            
            if(!(iviewer instanceof ScriptXMLViewer))
                return;
            xmlViewer = (ScriptXMLViewer)iviewer;
            xmlViewer.update();
            if(node instanceof AlgorithmNode)
                xmlViewer.highlightAlgorithmNode((AlgorithmNode)node);
            framework.setTreeNode(viewerNode);
        }
    }
    
    /** Support retrieval of sibling ScriptXMLViewer given an
     * associated tree.
     * @param treeViewer ScriptTreeViewer associated with xml view to be delivered.
     * @return
     */
    public DefaultMutableTreeNode getSiblingXMLNode(ScriptTreeViewer treeViewer) {
        Enumeration _enum = this.scriptManagerNode.depthFirstEnumeration();
        DefaultMutableTreeNode node, treeNode = null;
        IViewer viewer;
        LeafInfo leaf;
        
        while(_enum.hasMoreElements()) {
            node = (DefaultMutableTreeNode)_enum.nextElement();
            if(node.isLeaf()) {
                leaf = (LeafInfo)(node.getUserObject());
                if(leaf != null && leaf.getViewer() != null) {
                    viewer = leaf.getViewer();
                    if(viewer == treeViewer) {
                        treeNode = node;
                    }
                }
            }
        }
        if(treeNode != null) {
            node = (DefaultMutableTreeNode)treeNode.getParent();
            if(node.getChildCount() > 1) {
                node = (DefaultMutableTreeNode)node.getChildAt(1);
                return node;
            }
        }
        return null;
    }
    
    /** Executes the script specified by the ID.
     * @param index Source script ID.
     */
    public void runScript(int index) {
        if(index >= scripts.size())
            return;
        
        Script script = (Script)scripts.elementAt(index);
        ScriptRunner runner = new ScriptRunner(script, actionManager, framework);
        runner.setOutputMode(ScriptConstants.SCRIPT_OUTPUT_MODE_INTERNAL_OUTPUT);
        runner.execute();
    }
    
    /** Runs the script contained in the ScriptDocument.
     * @param scriptDoc Source script.
     */
    public void runScript(ScriptDocument scriptDoc) {
        Script script = getScriptObjectForDocument(scriptDoc);
        if(script == null)
            return;
        
        ScriptRunner runner = new ScriptRunner(script, actionManager, framework);
        runner.setOutputMode(ScriptConstants.SCRIPT_OUTPUT_MODE_INTERNAL_OUTPUT);
        runner.execute();
    }
    
    /** Delivers a <CODE>Script</CODE> object in the collection that wraps the passed
     * <CODE>ScriptDocument</CODE>
     * @param doc source document.
     * @return
     */
    public Script getScriptObjectForDocument(ScriptDocument doc) {
        Script script;
        for(int i = 0; i < scripts.size(); i++) {
            script = (Script)(scripts.elementAt(i));
            if(script.getScriptDocument() == doc)
                return script;
        }
        return null;
    }
    
    /** Returns MeV's main frame component
     */
    public Frame getFrame() {
        return this.framework.getFrame();
    }
    
    /** Serves the current experiment object in MeV.  This is the primary
     * (initial) data source.
     * @return  */
    public Experiment getCurrentExperiment() {
        return framework.getData().getExperiment();
    }
    
    /** Validates script for parameter rule violations of type or possibly
     * range constraints when appropriate.  Return of true signifies valid
     * parameters based on current supporting parameter information XML.
     * Errors are logged to the passed <CODE>ErrorLog</CODE>.
     * @param tree ScriptTree to validate.
     * @param log <CODE>ErrorLog</CODE> to collect possible violations.
     * @return
     */
    public boolean validateParameters(ScriptTree tree, ErrorLog log) {
        boolean isValid = true;
        if(this.validator != null && this.validator.isEnabled()) {
            if( ! (validator.validate(this, tree, log)) ) {
                //REPORT ERRORS
                log.reportAllListings();
                isValid = false;
            }
        } else {
            //Report validation disabled
            isValid = false;
        }
        return isValid;
    }
    
    /** Returns a set of valid parameters given a passed algorithm name.
     * @param algName Algorithm name.
     * @return
     */
    public String getValidParametersTable(String algName) {
        if(validator == null)
            return null;
        return validator.getValidParameterTable(algName);
    }
    
    /** Returns a Hashtable of valid key value pairs for the algorithm
     * @param algName algorithm name
     * @return
     */
    public Hashtable getParameterHash(String algName) {
        if(validator == null)
            return null;
        return validator.getParameterHash(algName);
    }
    
    /** Returns <CODE>ParmaterAttributes</CODE> object for the specified algorithm.
     * @param algName Algorithm name
     * @param key parameter key
     * @return
     */
    public ParameterAttributes getParameterAttributes(String algName, String key) {
        if(validator == null)
            return null;
        return validator.getParameterAttributes(algName, key);
        
    }
    
    
        
    private void writeObject(java.io.ObjectOutputStream oos) throws java.io.IOException {

   // oos.writeObject(actionManager);
    //oos.writeObject(table);
    oos.writeInt(scriptNum);
//    oos.writeObject(framework);
   // oos.writeObject(validator);
//    oos.writeObject(scripts);
    
    }
     
   
    
    private void readObject(java.io.ObjectInputStream ois) throws java.io.IOException, ClassNotFoundException {

    //this.actionManager = (ActionManager)ois.readObject();
   // this.table = (ScriptTable)ois.readObject();
    this.scriptNum = ois.readInt();
  //  this.framework = (IFramework)ois.readObject();
   // this.scripts = (Vector)ois.readObject();
    }  
    
}
