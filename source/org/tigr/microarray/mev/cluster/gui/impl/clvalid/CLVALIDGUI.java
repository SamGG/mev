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

import javax.swing.tree.DefaultMutableTreeNode;

import org.tigr.microarray.mev.annotation.AnnotationFieldConstants;
import org.tigr.microarray.mev.cluster.algorithm.AbortException;
import org.tigr.microarray.mev.cluster.algorithm.Algorithm;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmData;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmEvent;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmException;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmListener;
import org.tigr.microarray.mev.cluster.algorithm.impl.CLVALID;
import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.IClusterGUI;
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.DialogListener;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.Progress;
import org.tigr.microarray.mev.script.scriptGUI.IScriptGUI;

/**
 * @author  dschlauch
 * @version
 */
public class CLVALIDGUI implements IClusterGUI, IScriptGUI {
    
    private Algorithm algorithm;
    private Progress progress;
    private Experiment experiment;    
    Vector<String> exptNamesVector;
    
    /** Creates new CLVALIDGUI */
    public CLVALIDGUI() {
    }
    /**
     * 
     * @param algData pre-loaded with parameters for CLValid run
     * @return
     * @throws AlgorithmException
     */
    public DefaultMutableTreeNode execute(AlgorithmData algData) throws AlgorithmException {
        algorithm = new CLVALID();
        algData = algorithm.execute(algData);
        return algData.getResultNode("validation-node");
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
        
        CLVALIDInitBox dialog = new CLVALIDInitBox(framework.getFrame());
        dialog.setVisible(true);        
        if (!dialog.isOkPressed()) return null;


        long start = System.currentTimeMillis();
        Listener listener = new Listener();
		try {
            this.progress = new Progress(framework.getFrame(), "", listener);
            this.progress.show();
        	progress.setIndeterminate(true);
        	progress.setIndeterminantString("This process may take awhile...");
        	progress.setDescription("Performing Cluster Validation");
        	progress.setTitle("Cluster Validation");  
        	experiment = framework.getData().getExperiment();
            int number_of_samples = experiment.getNumberOfSamples();
            int [] columnIndices = experiment.getColumnIndicesCopy(); 
            ArrayList<String> sampleLabels = new ArrayList<String>();
            ArrayList<String> geneLabels = new ArrayList<String>();
            for (int i = 0; i < number_of_samples; i++) {
                sampleLabels.add(framework.getData().getFullSampleName(columnIndices[i])); 
            }
            for (int i = 0; i < experiment.getNumberOfGenes(); i++) {
            	geneLabels.add(framework.getData().getElementAnnotation(i, AnnotationFieldConstants.PROBE_ID)[0]);
            }
			AlgorithmData validationData = new AlgorithmData();
			validationData.addMatrix("experiment", experiment.getMatrix());
			validationData.addStringArray("geneLabels", geneLabels.toArray(new String[geneLabels.size()]));
			validationData.addStringArray("sampleLabels", sampleLabels.toArray(new String[sampleLabels.size()]));			
			dialog.getValidationPanel().addValidationParameters(validationData);            
			performValidation(validationData);

            long time = System.currentTimeMillis()-start;
            progress.dispose();
            GeneralInfo info = new GeneralInfo();
            info.time = time;
            info.lowRange = dialog.getValidationPanel().getLowClusterRange();
            info.highRange = dialog.getValidationPanel().getHighClusterRange();
            info.linkageMethod = dialog.getValidationPanel().getValidationLinkageMethod();
            info.distanceMetric = dialog.getValidationPanel().getValidationDistanceMetric();
            info.internal = dialog.getValidationPanel().isInternalV();
            info.stability = dialog.getValidationPanel().isStabilityV();
            info.biological = dialog.getValidationPanel().isBiologicalV();
            info.bioconductorAnnotation = dialog.getValidationPanel().getBioCAnnotationString();
            info.isClusterGenes = dialog.getValidationPanel().isClusterGenes();
			info.methodsArray = dialog.getValidationPanel().getMethodsArray();
            return createResultTree(validationData.getResultNode("validation-node"), info);
		} catch (Exception e){
			e.printStackTrace();
            progress.dispose();
			return null;
		}
    }
	public DefaultMutableTreeNode performValidation(AlgorithmData data) throws AlgorithmException {
		try {
			CLVALIDGUI clv = new CLVALIDGUI();
			return clv.execute(data);
		} catch(Exception e){
			e.printStackTrace();
			System.out.println("Error running clValid");
            throw new AlgorithmException("Error running Cluster Validation");
		}
	}
    /**
     * The class to listen to progress, monitor and algorithms events.
     */
    private class Listener extends DialogListener implements AlgorithmListener {
    	//EH added so AMP could extend this class
        private Listener(){super();}
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
    
    public AlgorithmData getScriptParameters(IFramework framework) {
    	return null;
    }
    
    public DefaultMutableTreeNode executeScript(IFramework framework, AlgorithmData algData, Experiment experiment) throws AlgorithmException {
       return null;
    }
    
    
    /**
     * Creates a result tree to be inserted into the framework analysis node.
     */
    private DefaultMutableTreeNode createResultTree(DefaultMutableTreeNode resultNode, GeneralInfo info) {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("CLVALID");
    	root.add(resultNode);
    	addGeneralInfo(root,info);
        return root;
    }
    
    /**
     * Adds node with general iformation.
     */
    private void addGeneralInfo(DefaultMutableTreeNode root, GeneralInfo info) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode("General Information");
    	node.add(new DefaultMutableTreeNode("Clustering by "+ (info.isClusterGenes ? "genes":"samples")));
        node.add(new DefaultMutableTreeNode("Time: "+String.valueOf(info.time-1)+" ms"));
        if (info.internal)
        	node.add(new DefaultMutableTreeNode("Internal Validation"));
        if (info.stability)
        	node.add(new DefaultMutableTreeNode("Stability Validation"));
        if (info.biological)
        	node.add(new DefaultMutableTreeNode("Biological Validation"));
        node.add(new DefaultMutableTreeNode("Cluster Range: "+info.lowRange+"-"+info.highRange));
        node.add(new DefaultMutableTreeNode("Distance Metric: "+info.distanceMetric));
        if (info.methodsArray[0].equals("hierarchical")||info.methodsArray[info.methodsArray.length-1].equals("agnus"))
        	node.add(new DefaultMutableTreeNode("Linkage Metric: "+info.linkageMethod));
        if (info.biological)
        	node.add(new DefaultMutableTreeNode("Bioconductor Annotation: "+info.bioconductorAnnotation));
        String methods = "";
        for (int i=0; i<info.methodsArray.length; i++)
        	methods = methods + info.methodsArray[i]+", ";
        methods = methods.substring(0, methods.length()-2);
    	node.add(new DefaultMutableTreeNode("Clustering Methods: "+methods));
        	
        root.add(node);
    }
    

    private class GeneralInfo {
		public boolean isClusterGenes;
		public String bioconductorAnnotation;
		public boolean biological;
		public boolean stability;
		public boolean internal;
		public String[] methodsArray;
		public int highRange;
		public int lowRange;
		public String distanceMetric;
		public String linkageMethod;
		public long time;      
    }
    
}
