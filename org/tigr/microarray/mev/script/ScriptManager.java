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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import java.util.Enumeration;
import java.util.Vector;

import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;

import org.tigr.microarray.mev.script.scriptGUI.*;
import org.tigr.microarray.mev.script.util.*;
import org.tigr.microarray.mev.SetPercentageCutoffsDialog;
import org.tigr.microarray.mev.SetLowerCutoffsDialog;

import org.tigr.microarray.mev.action.ActionManager;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmData;
import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.IClusterGUI;
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.IViewer;
import org.tigr.microarray.mev.cluster.gui.LeafInfo;

//For testing
import org.tigr.microarray.mev.cluster.gui.impl.kmc.KMCGUI;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.Progress;


/**
 *
 * @author  braisted
 */
public class ScriptManager {
    
    private DefaultMutableTreeNode scriptManagerNode;
    private ActionManager actionManager;
    private ScriptTable table;
    private int scriptNum;
    private IFramework framework;
    private ParameterValidator validator;
    private Progress progress;
    private Vector scripts;
    
    /** Creates a new instance of ScriptManager */
    public ScriptManager(IFramework framework, DefaultMutableTreeNode scriptNode, ActionManager manager) {
        this.framework = framework;
        this.actionManager = manager;
        this.scriptManagerNode = scriptNode;
        scripts = new Vector();
        scriptNum = 1;
    }
    
    public ScriptManager() {
        
    }

    public void loadScript() {
        JFileChooser chooser = new JFileChooser(System.getProperty("user.dir")+System.getProperty("file.separator")+"Data");
        chooser.setMultiSelectionEnabled(false);
        boolean loadState;
        if( chooser.showOpenDialog(framework.getFrame()) == JFileChooser.APPROVE_OPTION ) {
            loadXML(chooser.getSelectedFile());
        }
    }
    
    
    public void loadXML(File file) {
        Thread thread = new Thread(new ThreadHandler(file));
        thread.setPriority(Thread.MIN_PRIORITY);
        this.progress = new Progress(framework.getFrame(), "Script Loading and Validation", null);
        this.progress.show();
        thread.start();
    }
    
    
    
    public class ThreadHandler implements Runnable {
        private final File file;
        public ThreadHandler(File f) {
            file = f;
        }
        public void run() {
            loadScript(file);
        }
    }
    
    
    public boolean loadScript(File inputFile) {
        
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
            return false;
        }
        
        //Exit if there were validation or fatal errors.
        if(validationErrors)
            return false;
        
        newScriptDoc.setDocumentFileName(inputFile.getPath());
        
        //Construct the table if needed
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
        DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode(new LeafInfo("Tree Viewer", treeViewer));
        DefaultMutableTreeNode xmlNode = new DefaultMutableTreeNode(new LeafInfo("XML Viewer", xmlViewer));
        
        scriptNode.add(treeNode);
        scriptNode.add(xmlNode);
        
        scripts.add(new Script(newScriptDoc, tree, xmlViewer));
        
        validateParameters(tree, errorLog);
        
        framework.addNode(scriptManagerNode, scriptNode);
        framework.setTreeNode(treeNode);
        scriptNum++;
        
        return true;
    }
    
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
            DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode(new LeafInfo("Tree Viewer", treeViewer));
            DefaultMutableTreeNode xmlNode = new DefaultMutableTreeNode(new LeafInfo("XML Viewer", xmlViewer));
            
            scripts.add(new Script(newScriptDoc, tree, xmlViewer));
            
            scriptNode.add(treeNode);
            scriptNode.add(xmlNode);
            
            framework.addNode(scriptManagerNode, scriptNode);
            framework.setTreeNode(treeNode);
            
            scriptNum++;
        }
    }
    
    public int getNextScriptID() {
        scriptNum++;
        return scriptNum;
    }
    
    public void writeScript(int scriptID) {
        
    }
    
    public void writeScript() {
        
    }
    
    public void saveScript(ScriptDocument doc) {
        JFileChooser chooser = new JFileChooser(System.getProperty("user.dir"));
        
        if(chooser.showSaveDialog(new JFrame()) == JFileChooser.APPROVE_OPTION) {
            try {
                writeScript(chooser.getSelectedFile(), doc);
            } catch (IOException ioe) {
                JOptionPane.showMessageDialog(new JFrame(), "An error occured while saving the script to file", "Save Error", JOptionPane.WARNING_MESSAGE);
            }
        }
    }
    
    
    private void writeScript(File file, ScriptDocument doc) throws IOException {
        BufferedWriter bfr = new BufferedWriter( new FileWriter(file));
        bfr.write(doc.toString());
        bfr.flush();
        bfr.close();
    }
    
    public AlgorithmData getAlgorithm(String parentNodeOutputClass) {
        ScriptAlgorithmInitDialog dialog = new ScriptAlgorithmInitDialog(actionManager, parentNodeOutputClass);
        AlgorithmData data = null;
        if(dialog.showModal() == JOptionPane.OK_OPTION) {
            String algName = dialog.getAlgorithmName();
            String algType = dialog.getAlgorithmType();
            
            
            if(algType.equals(ScriptConstants.ALGORITHM_TYPE_CLUSTER)) {
                int algIndex = dialog.getAlgorithmIndex();
                Action action = actionManager.getAction(actionManager.ANALYSIS_ACTION+String.valueOf(algIndex));
                if(action == null){
                    System.out.println("null action");
                    return null;
                }
                String className = (String)action.getValue(ActionManager.PARAMETER);
                System.out.println("Class name = "+className);
                try {
                    Class clazz = Class.forName(className);
                    IClusterGUI gui = (IClusterGUI)clazz.newInstance();
                    data = ((KMCGUI)gui).getScriptParameters(framework);
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
                        
                        System.out.println("AlgName ="+algName);
                        
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
                } else {
                    data = new AlgorithmData();
                    data.addParam("name", algName);
                    setAdjustmentOutput(data);
                }
            }
        }
        return data;
    }
    
    private void setAdjustmentOutput(AlgorithmData data) {
        String [] output_nodes = new String[1];
        output_nodes[0] = "Single Ouput";
        data.addStringArray("output-nodes", output_nodes);
        data.addParam("output-class", ScriptConstants.OUTPUT_DATA_CLASS_SINGLE_OUTPUT);
        data.addParam("alg-type", ScriptConstants.ALGORITHM_TYPE_ADJUSTMENT);
    }
    
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
    
    public DefaultMutableTreeNode getSiblingXMLNode(ScriptTreeViewer treeViewer) {
        Enumeration enum = this.scriptManagerNode.depthFirstEnumeration();
        DefaultMutableTreeNode node, treeNode = null;
        IViewer viewer;
        LeafInfo leaf;
        
        while(enum.hasMoreElements()) {
            node = (DefaultMutableTreeNode)enum.nextElement();
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
    
    public void runScript(int index) {
        if(index >= scripts.size())
            return;
        
        Script script = (Script)scripts.elementAt(index);
        ScriptRunner runner = new ScriptRunner(script, actionManager, framework);
        runner.setOutputMode(ScriptConstants.SCRIPT_OUTPUT_MODE_INTERNAL_OUTPUT);
        runner.execute();
    }
    
    public Experiment getCurrentExperiment() {
        return framework.getData().getExperiment();
    }
    
    public boolean validateParameters(ScriptTree tree, ErrorLog log) {
        boolean isValid = true;
        if(this.validator != null && this.validator.isEnabled()) {
            System.out.println("VALIDATION ENABLED");
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
    
    public String getValidParametersTable(String algName) {
        if(validator == null)
            return null;        
        return validator.getValidParameterTable(algName);           
    }
    
}
