/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * Created on Aug 10, 2005
 *
 */
package org.tigr.microarray.mev.cluster.gui.impl.tease;

import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;

import org.tigr.microarray.mev.cluster.algorithm.Algorithm;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmData;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmEvent;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmException;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmFactory;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmListener;
import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.IClusterGUI;
import org.tigr.microarray.mev.cluster.gui.IDistanceMenu;
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.IViewer;
import org.tigr.microarray.mev.cluster.gui.LeafInfo;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.DialogListener;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.Logger;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.Progress;
import org.tigr.microarray.mev.cluster.gui.impl.ease.EASEImpliesAndURLDataFile;
import org.tigr.microarray.mev.cluster.gui.impl.ease.EASEInitDialog;
import org.tigr.microarray.mev.cluster.gui.impl.ease.EASESupportDataFile;
import org.tigr.microarray.mev.resources.AvailableAnnotationsFileDefinition;
import org.tigr.microarray.mev.resources.IResourceManager;
import org.tigr.microarray.mev.resources.ISupportFileDefinition;
import org.tigr.microarray.mev.resources.SupportFileAccessError;



/**
 * @author Annie Liu
 * @version Aug 10, 2005
 *
 * TEASEGUI acts as the bridge between viewer and model
 * it recognizes user input and passed in packaged data
 * to TEASE for execution. TEASEGUI receives the executed 
 * data and display the data in viewer.
 */

public class TEASEGUI implements IClusterGUI {

	public static final String LAST_TEASE_FILE_LOCATION = "last-tease-file-location";
	private Algorithm algorithm;   //a reference to TEASE algorithm
	private JFrame frame;
	private GeneralInfo info;      //store parameters of the analysis
	private Experiment experiment;  //data to be analyzed
    private Progress progress;      //progress bar
    private Logger logger;          //log box
    
    private boolean stop;
    private boolean hclOnly;            //variables used specifically for HCL only
    private boolean clusterGeneTree;
    private boolean clusterSampleTree;
    
	/**
	 * Constructor. Create an instance of TEASEGUI
	 *
	 */
	public TEASEGUI() {
		this.info = new GeneralInfo();
	}
	
    /**
     * Inits the algorithm parameters, runs calculation and returns
     * a result to be inserted into the framework analysis node.
     */
    public DefaultMutableTreeNode execute(IFramework framework) throws AlgorithmException {
    	AlgorithmData data = new AlgorithmData();
    	AlgorithmData resultData = new AlgorithmData();
    	this.frame = framework.getJFrame();
        this.experiment = framework.getData().getExperiment();
        AlgorithmFactory factory = framework.getAlgorithmFactory();  
        this.algorithm = factory.getAlgorithm("TEASE");  //store a reference to the algorithm
        
        Listener listener = new Listener();
        this.algorithm.addAlgorithmListener(listener);        //add algorithm listener 
        this.logger = new Logger(framework.getFrame(), "TEASE Analysis", listener);
        this.progress = new Progress(framework.getFrame(), "", listener);
        
        long start = System.currentTimeMillis();
        data = TEASEInterface(framework, data);
    
        if (data == null)
        	return null;
        
        if (this.hclOnly) {
            AlgorithmData genes_result = null;
            if (this.clusterGeneTree) {
                data.addParam("calculate-genes", String.valueOf(true));
                genes_result = algorithm.execute(data);
                validate(genes_result);
            }
            AlgorithmData samples_result = null;
            if (this.clusterSampleTree) {
                data.addParam("calculate-genes", String.valueOf(false));
                samples_result = algorithm.execute(data);
                validate(samples_result);
            }

            logger.append("Creating Result Viewers\n");
          
		    if (algorithm != null) {
            algorithm.removeAlgorithmListener(listener);
            }
            if (logger != null) logger.dispose();
		          
	        long time = System.currentTimeMillis() - start;  
	          
	        this.info.time = time;
		          
	        if (this.algorithm != null) {
	        	this.algorithm.removeAlgorithmListener(listener);
	        }
	        if (this.progress != null) {
	         	this.progress.dispose();
	        }
	        return createResultTree(this.experiment, genes_result, samples_result, this.info);
        }
        
        //include EASE

        resultData = this.algorithm.execute(data);
        validate(resultData);
        
        logger.append("Creating Result Viewers\n");
        
        if (algorithm != null) {
            algorithm.removeAlgorithmListener(listener);
        }
        if (logger != null) logger.dispose();
        
        long time = System.currentTimeMillis() - start;  
        
        this.info.time = time;
        
        if (this.algorithm != null) {
        	this.algorithm.removeAlgorithmListener(listener);
        }
        if (this.progress != null) {
        	this.progress.dispose();
        }
        return createResultTree(this.experiment, resultData, this.info);
    }
    
    private AlgorithmData TEASEInterface(IFramework framework, AlgorithmData data) throws AlgorithmException {
    	IDistanceMenu menu = framework.getDistanceMenu();   //get user input from IDistanceMenu
        int function = menu.getDistanceFunction();
        if (function == Algorithm.DEFAULT) {      //default value is Euclidean distance
            function = Algorithm.EUCLIDEAN;
        }        

        String speciesName = null; 
    	Hashtable<String, Vector<String>> speciestoarrays = null;
    	Vector<ISupportFileDefinition> defs = new Vector<ISupportFileDefinition>();
    	EASESupportDataFile edf = null;
    	String chipType = null;
        File defaultTEASEDirectory = null;
        if(framework.getData().isAnnotationLoaded()) {
        	chipType = framework.getData().getChipAnnotation().getChipType();
        	String filename = framework.getData().getChipAnnotation().getAnnFileName();
    		String species = framework.getData().getChipAnnotation().getSpeciesName();
    		edf = new EASESupportDataFile(species, chipType);
    		defs.add(edf);
        	
        	File annotationFile = new File(filename);
	        if(annotationFile.canRead()) {
	        	defaultTEASEDirectory = new File("./data/ease/ease_" + chipType);
	        } else {
	        	annotationFile = new File("./data/Annotation/" + chipType + ".txt");
	        	defaultTEASEDirectory = new File("./data/ease/ease_" + chipType);
	        	if(!annotationFile.canRead()) {
	        		defaultTEASEDirectory = null;
	        	}
	        } 
        }	
        

    	AvailableAnnotationsFileDefinition aafd = new AvailableAnnotationsFileDefinition();
    	defs.add(aafd);
        EASEImpliesAndURLDataFile eiudf = new EASEImpliesAndURLDataFile();
        defs.add(eiudf);        
        if (framework.getData().isAnnotationLoaded()) {
    		chipType = framework.getData().getChipAnnotation().getChipType();
    		speciesName = framework.getData().getChipAnnotation().getSpeciesName();
    		edf = new EASESupportDataFile(speciesName, chipType);
    		defs.add(edf);
    	} 
        try {
        	Hashtable<ISupportFileDefinition, File> supportFiles = framework.getSupportFiles(defs, true);
        	
        	File impliesFile = supportFiles.get(eiudf);
	        data.addParam("implies-location-list", eiudf.getImpliesLocation(impliesFile));
	        data.addParam("tags-location-list", eiudf.getTagsLocation(impliesFile));
	        
	        File speciesarraymapping = supportFiles.get(aafd);
	        try {
	        	speciestoarrays = aafd.parseAnnotationListFile(speciesarraymapping);
	        } catch (IOException ioe) {
	        	speciestoarrays = null;
	        }
	        if(edf != null || framework.getData().isAnnotationLoaded()) {
	        	defaultTEASEDirectory = new File(supportFiles.get(edf).getAbsolutePath());
	        } else {
	        	defaultTEASEDirectory = new File("./data/ease/ease_" + chipType);
	        }
        } catch (SupportFileAccessError sfae) {
        	defaultTEASEDirectory = new File("./data/ease/ease_" + chipType);
        }
//    	TEASEInitDialog dialog = new TEASEInitDialog(framework.getFrame(), framework.getData().getAllFilledAnnotationFields(),
//    			menu.getFunctionName(function),	menu.isAbsoluteDistance(), true, defaultTEASEDirectory);
    	
        TEASEInitDialog dialog = new TEASEInitDialog(
        		framework.getFrame(), 
        		framework.getData().getAllFilledAnnotationFields(), 
        		menu.getFunctionName(function), 
        		menu.isAbsoluteDistance(), 
        		true,
        		defaultTEASEDirectory,
        		framework.getResourceManager(),
        		speciesName, 
        		chipType, 
        		speciestoarrays, framework.getData().isAnnotationLoaded());
        
        if (dialog.showModal() != JOptionPane.OK_OPTION) {
            return null;
        }
        
        int method = dialog.getMethod();
        function = dialog.getDistanceMetric();

        //construct data
        data.addParam("hcl-only", dialog.isHCLOnly());
        data.addMatrix("experiment", this.experiment.getMatrix());
        data.addParam("hcl-distance-function", String.valueOf(function));
        data.addParam("distance-factor", String.valueOf(1.0f));
        data.addParam("hcl-distance-absolute", String.valueOf(dialog.getAbsoluteSelection()));
        data.addParam("method-linkage", String.valueOf(method));
 
        progress.setTitle("Clustering by Genes");

        this.info.method = method;
        this.info.function = menu.getFunctionName(function);
        this.logger.show();
        
        this.hclOnly = Boolean.valueOf(dialog.isHCLOnly()).booleanValue();
        
        if (this.hclOnly) {
        	this.clusterGeneTree = dialog.isGeneTreeSelected();
        	this.clusterSampleTree = dialog.isSampleTreeSelected();
        	data.addParam("hcl-only",String.valueOf(true));
        	return data;
        }

	    data.addParam("maximum-genes", dialog.getMaxNumber());
	    data.addParam("minimum-genes", dialog.getMinNumber());
	    data.addParam("upper-boundary", dialog.getUpperBoundary());
	    data.addParam("lower-boundary", dialog.getLowerBoundary());
	
	    this.info.max = dialog.getMaxNumber();
	    this.info.min = dialog.getMinNumber();
	        
	    String baseFileSystem = dialog.getBaseFileLocation();
	    String converterFileName = dialog.getConverterFileName();
	    String annotationKeyType = dialog.getAnnotationKeyType();
	    String [] annotationFileList = dialog.getAnnToGOFileList();  //import the gene to GO files
	    boolean isClusterAnalysis = dialog.isClusterModeSelected();
	        
	    data.addParam("base-file-system", baseFileSystem);
	    boolean isPvalueCorrectionSelected;
	
	        
	//        for (int i = 0; i < experiment.getRowMappingArrayCopy().length; i++) //***********************************
	//        	System.out.println("Row "+(i+1)+ ": "+experiment.getRowMappingArrayCopy()[i]);
	//        
	//        for (int i = 0; i < experiment.getRowMappingArrayCopy().length; i++) //**********************
	//        	System.out.println("Row " +i +": "+framework.getData().getGeneName(i));
	        
	    if(isClusterAnalysis){
	        data.addParam("report-ease-score", String.valueOf(dialog.isEaseScoreSelected()));
	        isPvalueCorrectionSelected = dialog.isCorrectPvaluesSelected();
	        data.addParam("p-value-corrections", String.valueOf(isPvalueCorrectionSelected));
	        if(isPvalueCorrectionSelected){
	            data.addParam("bonferroni-correction", String.valueOf(dialog.isBonferroniSelected()));
	            data.addParam("bonferroni-step-down-correction", String.valueOf(dialog.isStepDownBonferroniSelected()));
	            data.addParam("sidak-correction", String.valueOf(dialog.isSidakSelected()));
	        }
	          
	        data.addParam("run-permutation-analysis", String.valueOf(dialog.isPermutationAnalysisSelected()));
	        if(dialog.isPermutationAnalysisSelected())
	        	data.addParam("permutation-count", String.valueOf(dialog.getPermutationCount()));
	            
	        	this.logger.append("Extracting Annotation Key Lists\n");
	        }
	        
	    String [] clusterKeys = framework.getData().getAnnotationList(annotationKeyType); 
	    data.addStringArray("name-list", framework.getData().getAnnotationList("Title"));
	    data.addStringArray("annotation-list", clusterKeys);
	        
	    //Population keys can either from an imported file or the present popualation if no file
	   //is selected
	    String [] populationKeys;
	    if(isClusterAnalysis && dialog.isPopFileModeSelected()) {
	        try {
	            populationKeys = getPopulationKeysFromFile(dialog.getPopulationFileName());
	            data.addParam("population-file-name", dialog.getPopulationFileName());
	            if(populationKeys == null) {
	                return null;
	            }
	        } catch (IOException ioe) {
	           //Bad file format
	        	JOptionPane.showMessageDialog(framework.getFrame(), "Error loading population file.", 
	                		"Population File Load Error", JOptionPane.ERROR_MESSAGE);
	            return null;
	        }
	    } else {
	    	populationKeys = framework.getData().getAnnotationList(annotationKeyType, framework.getData().getExperiment().getRowMappingArrayCopy());            
	    }  //select the annotation key to use -->eq. locuslink ID
	
	    data.addParam("perform-cluster-analysis", String.valueOf(isClusterAnalysis));
		data.addStringArray("population-list", populationKeys);  //add population list
        if(converterFileName != null)
        	data.addParam("converter-file-name", converterFileName);
        data.addStringArray("annotation-file-list", annotationFileList);
        
        //Trim options
        String [] trimOptions = dialog.getTrimOptions();
        data.addParam("trim-option", trimOptions[0]);
        data.addParam("trim-value", trimOptions[1]);

    	return data;
    }

    private String [] getPopulationKeysFromFile(String fileName) throws IOException {
        File file = new File(fileName);
        if(file.exists()) {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            Vector<String> ann = new Vector<String>();
            String key;
            while( (key = reader.readLine()) != null ) {
                ann.add(key);
            }
            String [] annot = new String [ann.size()];
            for(int i = 0; i < annot.length; i++) {
                annot[i] = (String)(ann.elementAt(i));
            }
            return annot;
        }
        return null;
    }
    
    /**
     * Checking the result of hcl algorithm calculation.
     * @throws AlgorithmException, if the result is incorrect.
     */
    private void validate(AlgorithmData result) throws AlgorithmException {
        if (result.getIntArray("child-1-array") == null) {
            throw new AlgorithmException("parameter 'child-1-array' is null");
        }
        if (result.getIntArray("child-2-array") == null) {
            throw new AlgorithmException("parameter 'child-2-array' is null");
        }
        if (result.getIntArray("node-order") == null) {
            throw new AlgorithmException("parameter 'node-order' is null");
        }
        if (result.getMatrix("height") == null) {
            throw new AlgorithmException("parameter 'height' is null");
        }
    }
    
    /**
     * Creates a result tree.
     */
    private DefaultMutableTreeNode createResultTree(Experiment experiment, 
    		AlgorithmData genes_result,GeneralInfo info) {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("TEASE");
        root.add(new DefaultMutableTreeNode(new LeafInfo("TEASE Tree", 
        		createHCLViewer(experiment, genes_result, null, root))));
        if(genes_result != null)
            root.add(new DefaultMutableTreeNode(new LeafInfo("Gene Node Height Plot", 
            		new HCLNodeHeightGraph(getHCLTreeData(genes_result), true))));
        addGeneralInfo(root, info);
        return root;
    }
    
    
    //for HCL only analysis
    private DefaultMutableTreeNode createResultTree(Experiment experiment, 
    		AlgorithmData genes_result, AlgorithmData samples_result, GeneralInfo info) {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("TEASE");
        root.add(new DefaultMutableTreeNode(new LeafInfo("HCL Tree", 
        		createHCLViewer(experiment, genes_result, samples_result, root))));
        if(genes_result != null)
            root.add(new DefaultMutableTreeNode(new LeafInfo("Gene Node Height Plot", 
            		new HCLNodeHeightGraph(getHCLTreeData(genes_result), true))));
        if(samples_result != null)
            root.add(new DefaultMutableTreeNode(new LeafInfo("Sample Node Height Plot", 
            		new HCLNodeHeightGraph(getHCLTreeData(samples_result), false))));
        addGeneralInfo(root, info);
        return root;
    }
    /**
     * Returns a hcl tree data from the specified AlgorithmData structure.
     */
    private HCLTreeData getHCLTreeData(AlgorithmData result) {
        if (result == null) {
            return null;
        }
        HCLTreeData data = new HCLTreeData();
        data.child_1_array = result.getIntArray("child-1-array");
        data.child_2_array = result.getIntArray("child-2-array");
        data.node_order = result.getIntArray("node-order");
        data.node_list = result.getIntArray("node-list");
        data.height = result.getMatrix("height").getRowPackedCopy();

        //System.out.println("HCLGUI");
//        String[] name = result.getMatrixNames(); //***************************************8
//        for (int i = 0; i < name.lenfgth; i++)
//        	System.out.println(name[i]);
        return data;
    }
    
    /**
     * Creates an <code>TEASEViewer</code>.
     */
    private IViewer createHCLViewer(Experiment experiment, AlgorithmData genes_result, 
    		AlgorithmData samples_result, DefaultMutableTreeNode root) {
//    	HCLTreeData sample = getHCLTreeData(samples_result);
//    	System.out.println();
//    	for (int i = 0; i < sample.child_1_array.length; i++)
//    		System.out.print(sample.child_1_array[i] + " ");
//    	System.out.println();
//    	for (int i = 0; i < sample.child_2_array.length; i++)
//    		System.out.print(sample.child_2_array[i] + " ");
//    	System.out.println();
//    	for (int i = 0; i < sample.height.length; i++)
//    		System.out.print(sample.height[i] + " ");
//    	System.out.println();
//    	for (int i = 0; i < sample.node_order.length; i++)
//    		System.out.print(sample.node_order[i] + " ");
        return new TEASEViewer(this.frame, experiment, null, getHCLTreeData(genes_result), 
        		getHCLTreeData(samples_result), root, this.hclOnly, genes_result);
    }
    
    /**
     * Adds node with general iformation.
     */
    private void addGeneralInfo(DefaultMutableTreeNode root, GeneralInfo info) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode("General Information");
        node.add(new DefaultMutableTreeNode("Linkage Method: "+info.getMethodName()));
        node.add(new DefaultMutableTreeNode("Time: "+String.valueOf(info.time)+" ms"));
        node.add(new DefaultMutableTreeNode("Cluster Size: "+ info.getSize()));
        node.add(new DefaultMutableTreeNode(info.function));
        root.add(node);
    }
    
    
    /**
     * The class to listen to algorithm events and update logger
     */
    private class Listener extends DialogListener implements AlgorithmListener{
        String eventDescription;
        
        /** Handles algorithm events.
         * @param actionEvent event object
         */
        public void actionPerformed(java.awt.event.ActionEvent e) {
            String command = e.getActionCommand();
            if (command.equals("cancel-command")) {
                System.out.println("abort execution");
                stop = true;
                algorithm.abort();
                progress.dispose();
                logger.dispose();
            }
        }
        
        /** 
         * Invoked when an algorithm progress value was changed.
         * @param event a <code>AlgorithmEvent</code> object.
         */
        public void valueChanged(AlgorithmEvent event) {
        	
        	switch(event.getId()) {
        	case AlgorithmEvent.MONITOR_VALUE:
        		logger.append(event.getDescription());
        		break;
        	case AlgorithmEvent.SET_UNITS:
	            progress.setDescription("Resampling Analysis Iterations");
	            progress.setValue(0);
	            progress.setUnits(event.getIntValue());
	            progress.show();
	            java.awt.Point p = progress.getLocation();
	            java.awt.Point loggerP = logger.getLocation();
	            progress.setLocation(p.x, loggerP.y-progress.getHeight());
	            break;
            case AlgorithmEvent.PROGRESS_VALUE:
                progress.setValue(event.getIntValue());
                progress.setDescription(event.getDescription());
                break;
            case AlgorithmEvent.SET_VALUE:
            	progress.setValue(event.getIntValue());
            	break;
        	}
			
//            if(event.getId() == AlgorithmEvent.MONITOR_VALUE){
//                logger.append(event.getDescription());
//            } else {  //event to progress
//                
//                eventDescription = event.getDescription();
//                
//                if(eventDescription.equals("SET_VALUE")){
//                    progress.setValue(event.getIntValue());
//                    return;
//                } else if(eventDescription.equals("SET_UNITS")){
//                    progress.setDescription("Resampling Analysis Iterations");
//                    progress.setValue(0);
//                    progress.setUnits(event.getIntValue());
//                    progress.show();
//                    java.awt.Point p = progress.getLocation();
//                    java.awt.Point loggerP = logger.getLocation();
//                    progress.setLocation(p.x, loggerP.y-progress.getHeight());
//                    return;
//                } else {  //default dispose
//                    progress.setVisible(false);
//                    progress.dispose();
//                }
//            }
        }
  
        public void windowClosing(WindowEvent e) {
            algorithm.abort();
            progress.dispose();
        }
    }
    
    /**
     * General info structure.
     */
    public static class GeneralInfo {
        public long time;
        public int method;
        public String function;
        public String max;
        public String min;
        
        public String getMethodName() {
            return getMethodName(method);
        }
        
        public static String getMethodName(int method) {
            method = method == -1 ? 2 : method;
            return methods[method];
        }
        public String getSize() {
        	return min + " - " + max;
        }
        
        private static String[] methods = {"average linkage", "complete linkage", "single linkage"};
    }
    
//	private static void printDataResult(AlgorithmData data) {
//		int[] nodes = data.getIntArray("node-list");
//		String[] names = data.getStringArray("name-list");
//		String outputFile = "C:/Documents and Settings/hwl2/Desktop/output.txt";
//		AlgorithmData single;
//		try {
//			PrintWriter out = new PrintWriter(new FileOutputStream(outputFile));  //create output writer
//        	out.println("size of data set: " + names.length);
//        	out.println("number of iteration: " + nodes.length);
//        	out.println("\n\n");
//			for (int i = 0; i < nodes.length; i++) {
//				single = data.getResultAlgorithmData(new Integer(nodes[i]));
//				printResult(single, names, out);
//			}
//			out.close();
//		}catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
	
//	private static void printResult(AlgorithmData result, String[] names, PrintWriter out) throws IOException{
//		String[][] re = (String[][])result.getObjectMatrix("result-matrix");
//		//System.out.println(indiData);
//
//		String[] sample = result.getStringArray("sample-list");  //print sample genes
//		int[] indices = result.getIntArray("sample-indices");
//		for (int j = 0; j < sample.length; j++) {
//			out.print(sample[j]+ ": "+ names[indices[j]] + "\t");
//		}
//		out.println();
//		
//		String[] header = result.getStringArray("header-names");  //print header
//		for (int j = 0; j < header.length; j++) {
//			out.print(header[j]+ "\t");
//		}
//		out.println();
//		
//		for (int x = 0;  x< 5; x ++) {   //print categories
//			for (int y = 0; y < re[x].length; y++)
//				out.print(re[x][y]+ "\t");
//			out.println();
//		}
//		out.println();
//		out.println();
//	}
}
