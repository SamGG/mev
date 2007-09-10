package org.tigr.microarray.mev.cgh.CGHAlgorithms.Charm;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import javax.swing.JPanel;
import javax.swing.tree.DefaultMutableTreeNode;

import org.tigr.microarray.mev.ISlideData;
import org.tigr.microarray.mev.cgh.CGHDataModel.CharmDataModel.ResultContainer;
import org.tigr.microarray.mev.cgh.CGHDataModel.CharmDataModel.SegmentInfo;
import org.tigr.microarray.mev.cgh.CGHGuiObj.CharmDataViewer.CharmGUI;
import org.tigr.microarray.mev.cgh.CGHGuiObj.CharmDataViewer.GraphViewPanel;
import org.tigr.microarray.mev.cgh.CGHGuiObj.CharmDialogs.CharmAnalyzeDialog;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmException;
import org.tigr.microarray.mev.cluster.gui.Experiment;
import org.tigr.microarray.mev.cluster.gui.IClusterGUI;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.IFramework;
import org.tigr.microarray.mev.cluster.gui.LeafInfo;
import org.tigr.microarray.mev.cluster.gui.impl.dialogs.DialogListener;

//import edu.umd.cs.piccolox.swing.PScrollPane;
/**
* Main application class for ChARMView- starts both GUI and command-line versions.
*
 * <p>Title: ChARM</p>
 * <p>Description: Main application class for ChARMView- starts both GUI and command-line versions.</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: Princeton University</p>
 * @author Chad Myers, Xing Chen
 * @version 1.0
 */

public class ChARM /*extends Thread*/ implements IClusterGUI{
  boolean packFrame = false;
 
  javax.swing.Timer charmProgressTimer;

  /* Variables from DisplayStateMgr, Raktim */
  private ArrayList selectedExps, displayExps;
  private IData data;
  private IFramework frame;
  private int[][] chrIndices;
  private int exprInidces[];
  private int numEdges = 0;
  
  private DefaultMutableTreeNode resultRoot, viewerNode, paramNode;
  private CharmAnalyzeDialog analyzeData;
  //private ArrayList selectedResultSets;
  //private ArrayList runningResultSets;
  //private ArrayList runningExps;

  private PValue currPvalueCutoff;
  private int pvalTestType;
  private ArrayList exprNames;
  private ArrayList graphSelectedGenes;
  private ArrayList graphSelectedWindows;
  private String graphSelectedExp;
  private int graphSelectedChrom;

  private HashMap stateVariableHash;

  private String graphMode;
  
  /* Variables from DisplayStateMgr, Raktim */
  
  private CharmGUI charmViewer;
  private ResultContainer resultSet;
  
  private RunCharmProgressPanel runProgressPanel;
  private boolean isDone = false;
  private boolean isCanceled = false;
  
  public ChARM(/*IFramework frame*/) { 
	 
	  System.out.println("ChARM constructor");
	  selectedExps = new ArrayList();
	  displayExps = new ArrayList();
	  //selectedResultSets = new ArrayList();
	  //runningResultSets = new ArrayList();
	  //runningExps = new ArrayList();
	  stateVariableHash = new HashMap();
	  currPvalueCutoff = new PValue(.05,.001);
  }
  /**
   * Class constructor
   */
  /*
  public ChARM() {
    MainFrame frame = new MainFrame();
    //Validate frames that have preset sizes
    //Pack frames that have useful preferred size info, e.g. from their layout
    if (packFrame) {
      frame.pack();
    }
    else {
      frame.validate();
    }
    //Center the window
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    Dimension frameSize = frame.getSize();
    if (frameSize.height > screenSize.height) {
      frameSize.height = screenSize.height;
    }
    if (frameSize.width > screenSize.width) {
      frameSize.width = screenSize.width;
    }
    frame.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
    frame.setVisible(true);
  }
*/
 
  /* Start DisplayStateManager functions - Raktim */
  /**
   * Gets the current p-value cutoff selection.
   * @return PValue
   */
  public PValue getPvalueCutoff() {
    return this.currPvalueCutoff;
  }

  /**
   * Updates the state of the current p-value cutoff.
   * @param p PValue
   */
  public void setPValueCutoff(PValue p) {
    this.currPvalueCutoff = p;
  }

  /**
   * Updates the current significance test setting.
   * @param type int
   */
  public void setPValueTestType(int type) {
    this.pvalTestType = type;
  }

  /**
   * Returns the current significance test setting.
   * @return int
   */
  public int getPValueTestType() {
    return this.pvalTestType;
  }

  /**
   * Returns list of current user-selected experiments.
   * @return ArrayList
   */
  public ArrayList getSelectedExperiments() {
    
	  //Raktim Mar 11
	  return displayExps;
	  //return selectedExps;
  }

  public ArrayList getExperimentList() {
	  //return data.getFeaturesList();
	  //Raktim Mar 11
	  return selectedExps;
  }
  /**
   * Returns list of current user-selected result sets.
   * @return ArrayList
   */
  public ResultContainer getSelectedResultSets() {
    //return selectedResultSets;
    return resultSet;
  }

  /**
   * Returns an ArrayList of predicted windows for the specified result set, experiment, and chromosome number.
   * @param resultSet String- result set ID
   * @param exp String- experiment name
   * @param chromosome int- chromosome number
   * @return ArrayList
   */
  public ArrayList getPredictionSegments(/*String resultSet,*/ String exp, int chromosome) {
	  ArrayList segmentArray = new ArrayList();
	  //ResultContainer resSet = (ResultContainer)resultSets.get(resultSet);
	  ArrayList tempList = resultSet.getSegmentsMeetingCriteria(exp,chromosome,currPvalueCutoff,pvalTestType);
	  //System.out.println("getPredictionSegments() segments.size()" + tempList.size());
	  segmentArray.addAll(tempList);
	
	  return segmentArray;
  }
  
  /**
   * Returns all current prediction windows for the specified experiment and chromosome number.
   * @param exp String
   * @param chromosome int
   * @return ArrayList
   * Temp same as the func above - Raktim
   */
  public ArrayList getAllPredictionSegments(String exp, int chromosome) {
    ArrayList windowArray = new ArrayList();

    //for(int i=0; i<resultSet.size(); i++) {
      ArrayList tempList = this.getPredictionSegments(/*(String)selectedResultSets.get(i),*/exp,chromosome);
      windowArray.addAll(tempList);
    //}

    return windowArray;
  }

  /**
   * Prints all visible prediction windows (windows meeting current UI cutoff) to the specified file.
   * @param filename String
   * @return int
   */
  public int printVisiblePredictionWindows(String filename) {
    int status=0;
    int returnStatus = 0;
    /*  TODO
    ArrayList allResultSets = this.getResultSets();
   for(int j=0; j<this.resultSets.size(); j++) {
       if(j==0) ((ResultContainer)allResultSets.get(j)).printHeaderToFile(filename,false);
       status = ((ResultContainer)allResultSets.get(j)).printToFile(filename,true,this.currPvalueCutoff,this.pvalTestType);
       if(status < 0 ) returnStatus = status;

   }
   */
    resultSet.printHeaderToFile(filename,false);
    status = resultSet.printToFile(filename,true,this.currPvalueCutoff,this.pvalTestType);
    return returnStatus;
  }


  /**
   * Sets graph mode indicator.
   * @param mode String
   */
  public void setGraphMode(String mode) {
	  graphMode = mode;
  }

	/**
	 * Gets current graph mode.
	 * @return String
	 */
	public String getGraphMode() {
	   return graphMode;
	}

	 /**
	  * Sets the gene selection state information (experiment, chromosome, gene list).
	  * @param currExp String
	  * @param chromNum int
	  * @param geneList ArrayList
	  */
	 public void setGraphSelectionGenes(String currExp,int chromNum, ArrayList geneList) {
	  graphSelectedGenes = geneList;
	  graphSelectedExp = currExp;
	  graphSelectedChrom = chromNum;
	  this.setStateVariable("Graph Selection Type","gene");
	  charmViewer.updateViewResults();
	}

	/**
	 * Sets window selection state information (experiment, chromosome number, window list).
	 * @param currExp String
	 * @param chromNum int
	 * @param windowList ArrayList
	 */
	public void setGraphSelectionSegments(String currExp,int chromNum, ArrayList windowList) {
	  graphSelectedWindows = windowList;
	  graphSelectedExp = currExp;
	  graphSelectedChrom = chromNum;
	  this.setStateVariable("Graph Selection Type","window");
	  charmViewer.updateViewResults();
	}

	/**
	 * Clears all graph selection state variables.
	 */
	public void clearGraphSelection() {
	  graphSelectedGenes = null;
	  graphSelectedWindows = null;
	  graphSelectedExp = null;
	  graphSelectedChrom = 0;

	  this.setStateVariable("Graph Selection Type","none");
	  charmViewer.updateViewResults();
	}

	/**
	 * Returns a list of currently selected genes.
	 * @return ArrayList
	 */
	public ArrayList getGraphSelectionGenes() {
	  return graphSelectedGenes;
	}

	/**
	 * Returns a list of currently selected windows.
	 * @return ArrayList
	 */
	public ArrayList getGraphSelectionWindows() {
	  return graphSelectedWindows;
	}

	/**
	 * Returns the experiment name to which the current graph selection belongs
	 * (only one experiment/chromosome may be selected at a time).
	 * @return String
	 */
	public String getGraphSelectionExperiment() {
	  return graphSelectedExp;
	}

	/**
	 * Returns the chromosome number to which the current graph selection belongs
	 * (only one experiment/chromosome may be selected at a time).
	 * @return int
	 */
	public int getGraphSelectionChromosome() {
	  return graphSelectedChrom;
	}

	/**
	 * Returns a reference to the current GraphViewPanel (main display component).
	 * @return GraphViewPanel
	 */
	public GraphViewPanel getGraphPanel() {
	  //return charmMain.graphPanel;
	  return charmViewer.getGraphViewPanel();
	}
	
	public IData getData(){
		return this.data;
	}
	
	public IFramework getFramework() {
		return this.frame;
	}
	 /**
	  * Sets the specified state variable to the supplied value.
	  * @param variable String
	  * @param state String
	  */
	 public void setStateVariable(String variable, String state) {
	   stateVariableHash.put(variable,state);
	 }

	 /**
	  * Returns the value of the specified state variable.
	  * @param variable String
	  * @return String
	  */
	 public String getStateVariable(String variable) {
	   String state=new String(" ");
	   if(stateVariableHash.containsKey(variable)) state = (String)stateVariableHash.get(variable);
	   return state;
	 }
	/* End DisplayStateManager functions - Raktim */
	
	 /*
	  * IClusterGUI method
	  */
	public DefaultMutableTreeNode execute(IFramework framework) throws AlgorithmException {
		// TODO Auto-generated method stub
		/**
		 * May want to create a Algoritm & AlgorithimData object later for compelete
		 * compatibility with MeV architecture
		 */
		this.data = framework.getData();
		this.frame = framework;
		//TODO Chnage to createExpr for selected exprs
        Experiment experiment = data.getExperiment(); 
        //FloatMatrix fm = experiment.getMatrix(); 
        chrIndices = data.getChromosomeIndices();
        numEdges = 0;
		//ResultContainer resultSet = new ResultContainer(dataset);
		
		System.out.println("ChARM ChARM Analyze Execute Method");
		JPanel dummyPanel = new JPanel();
		
		ArrayList featuresList = data.getFeaturesList();
		exprNames = new ArrayList();
		
		//System.out.println("featuresList Size " + featuresList.size());
		// ArayList for All Experiment Names loaded
		for (int column = 0; column < featuresList.size(); column++){
			String name = (String)((ISlideData)featuresList.get(column)).getSlideDataName();
			//System.out.println("exprNames " + name);
			exprNames.add(name);
		}
		
		analyzeData = new CharmAnalyzeDialog(exprNames);
        analyzeData.setLocationRelativeTo(dummyPanel);
        analyzeData.setVisible(true);
        
        
        if(analyzeData.getExitStatus() != analyzeData.OK_VERIFIED) {
        	return null;
        }
		
        /**
         * TODO
         * ResultContainer needs to be replaced
         */
        
        Thread thread = new Thread(new Runnable(){
        	public void run(){
        		try {
        			CharmListener listener = new CharmListener();
	        		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	        		/*
					runProgressPanel = new RunCharmProgressPanel();
					runProgressPanel.setIndeterminate(true);
					runProgressPanel.setLocation((screenSize.width-screenSize.getSize().width)/2,(screenSize.height-screenSize.getSize().height)/2);
		            runProgressPanel.setVisible(true);
		            */
	        		runProgressPanel = new RunCharmProgressPanel(frame.getFrame(), "ChARM Execution", listener);
	                runProgressPanel.show();

		            runChARM();
		            
        		}catch(Exception ex){
		    	   ex.printStackTrace();
        		}
        		runProgressPanel.dispose();
        	}
        });
        thread.start();
        
        while(!isDone || isCanceled){
		     try{
		        Thread.sleep(1000);	
		     }catch(InterruptedException x){
		     //ignore;
		     }
		}
        
        if(isCanceled) return null;
        
        updateCharmViewer();      
        return createResultNode();
	}

	private void runChARM() {
        
        // ArrayList of selected Experiments for Analysis
        //selectedExps = new ArrayList();
		exprInidces = analyzeData.getSelectedExperimentsIndices();
        for(int m=0; m < exprInidces.length; m++) {
        	String currExpr = (String)exprNames.get(exprInidces[m]);
        	selectedExps.add(currExpr);
        }
        this.displayExps.add(selectedExps.get(0));
        
        initCharmViewer();
        
        resultSet = new ResultContainer(data, selectedExps);
        
        for(int m=0; m < exprInidces.length; m++) {
        	String currExpr = (String)exprNames.get(exprInidces[m]);
        	for(int chr = 0; chr < chrIndices.length; chr++) { //For # of Chrs     	
	        	
	        	float[] geneRatiosOfExpr = getRatioArrayNoNa(exprInidces[m], chr);
	        	//float[] geneRatiosOfExpr = fm.getMatrix(st, end, exprInidces[m], exprInidces[m]).getRowPackedCopy();
	        	//System.out.println("Expr Ind " + exprInidces[m] + " geneRatiosOfExpr Len " + geneRatiosOfExpr.length);
	        	float[] peaks = filterData(geneRatiosOfExpr);
	        	//System.out.println("Expr Ind " + exprInidces[m] + " peaks Len " + peaks.length);
	        	numEdges = Math.max((int)Math.ceil(peaks.length*PERCENT_EDGES) , 10);
	        	//System.out.println("Expr Ind " + exprInidces[m] + " numEdges " + numEdges);
	        	int[] edgeEstimates = estimateEdges(peaks, numEdges);
	        	//System.out.println("Expr Ind " + exprInidces[m] + " edgeEstimates len " + edgeEstimates.length);
	        	ArrayList edges = emEdges(edgeEstimates, geneRatiosOfExpr);
	        	//System.out.println("Expr Ind " + exprInidces[m] + " emEdges edges " + edges.size());
	        	
	            int[] final_edges = new int[edges.size()];

	            for (int i = 0; i < edges.size(); i++){
	              final_edges[i] = ((Edge)edges.get(i)).getPosition();
	            }
	            	            
	            ArrayList segments = getWindowsFromEdges(final_edges, geneRatiosOfExpr.length);
	            //System.out.println("Expr Ind " + exprInidces[m] + " segments Len " + segments.size());
	            mergeWindows(segments,geneRatiosOfExpr);
	            for (int i=0; i < segments.size(); i++) {
	              ((SegmentInfo)segments.get(i)).setBonferroniCorrection(segments.size());
	              resultSet.addSegment((SegmentInfo)segments.get(i),currExpr,chr+1);
	            }
        	}
        	
        }
        runSignificanceTests(resultSet, chrIndices, exprInidces/*, false*/);
        isDone = true;
        // Sleep to give some lead time for Viewer creation
        try { 
        	Thread.sleep(5000);
        	} 
        catch (Exception E) {}
	}
	
	private void initCharmViewer () {
		charmViewer = new CharmGUI(this.frame, this, exprInidces);
        charmViewer.updateExpOptions();
        charmViewer.getJTabbedPane1().setSelectedIndex(1);

        charmViewer.getTogglePredictionsButton().setEnabled(true);
        charmViewer.getTogglePredictionsButton().setSelected(true);
        //displayState.setStateVariable("Data Plot Toggle","on");
        //exportButton.setEnabled(true);
        //charmViewer.getGraphViewPanel().initializeGraph();
        //charmViewer.getGraphViewPanel().updateGraph();
        //charmViewer.getGraphViewPanel().resetZoom();
	}
	private void updateCharmViewer(){
		/*
         * Create Viewer(s)
         */
        //charmViewer = new CharmGUI(this.frame, this, exprInidces);
        //charmViewer.getGraphViewPanel().initializeGraph();
        //charmViewer.getGraphViewPanel().updateGraph();
        System.out.println("In execute() charmViewer constructed");
        charmViewer.getGraphViewPanel().initializePredictionNodes(getSelectedExperiments());

        charmViewer.getTogglePredictionsButton().setEnabled(true);
        charmViewer.getTogglePredictionsButton().setSelected(true);
        setStateVariable("Prediction Plot Toggle","on");
               
        //charmProgressMonitor.hide();
        //CharmGUI.this.updatePredOptions();
        charmViewer.getJTabbedPane1().setSelectedIndex(1);
        charmViewer.getGraphViewPanel().updateGraph();
        charmViewer.validate();
        charmViewer.show();
        //TODO Need TO reset the graphs to fit the screen
        //charmViewer.getGraphViewPanel().resetZoom();
        
	}
	
	private DefaultMutableTreeNode createResultNode(){
		/**
         * Construct DefaultMutableNode with Result & Viewers
         */
        resultRoot = new DefaultMutableTreeNode("ChARM");
        viewerNode = new DefaultMutableTreeNode(new LeafInfo("ChARM Viewer", charmViewer));
        resultRoot.add(viewerNode);
        
        paramNode = new DefaultMutableTreeNode("General Information");
        for (int i = 0; i < selectedExps.size(); i++){
        	paramNode.add(new DefaultMutableTreeNode("Experiment: " + selectedExps.get(i)));
        }
        resultRoot.add(paramNode);
        
        return resultRoot;
	}
	  /**
	   * Runs significance tests on all windows contained in the supplied ResultContainer
	   * @param results ResultContainer
	   * @param threaded boolean- indicates whether to run as separate thread or serially
	   */
	  private void runSignificanceTests(ResultContainer results, /*FloatMatrix fm,*/ int[][] chrIndices, int[] exprInidces/*, boolean threaded*/) {

	    //ArrayList chromosomes = (ArrayList)dataset.getChromosomes();
	    //ArrayList experiments = (ArrayList)dataset.getExperiments();

	    //runningThreadVect.clear();
	    //waitingThreadVect.clear();
	    //finishedThreadVect.clear();
	    //progressHash.clear();

	    //for(int i=0; i<chromosomes.size(); i++) {
	    for(int i = chrIndices.length; i != 0; i--) {
	    	int curChr = i-1;
	    	//Chromosome currChrom = (Chromosome)chromosomes.get(i);
	      //for (int j = 0; j < experiments.size(); j++) {
	    	for (int j = 0; j < selectedExps.size(); j++) {
	        //String currExp = (String)experiments.get(j);
	        //ArrayList currWindows = results.getWindows(currExp,currChrom.getNumber());
	    	String currExp = (String)selectedExps.get(j);
	    	System.out.println("Cur Chr: " + curChr + ", Cur Expr" + currExp);
	        ArrayList currWindows = results.getSegments(currExp,curChr+1); //Needs Actual Chr# not index
	        
        	float[] geneRatiosOfExpr = this.getRatioArrayNoNa(exprInidces[j], curChr);
	        for (int k = 0; k < currWindows.size(); k++) {
	          //SigTestThread tester = new SigTestThread(currChrom,currExp,(SegmentInfo)currWindows.get(k));
	          // For the curr expr & curr gene, get all ratios
	          SigTestThread tester = new SigTestThread(geneRatiosOfExpr, curChr+1, currExp, (SegmentInfo)currWindows.get(k));
	          tester.setTests(true, true,false,false);
	          tester.runPermuteTest();
	          tester.runSignTest();
	          
	          //tester.setNumberPermutations(numPermutes);
	          //tester.setPriority(Thread.MIN_PRIORITY);
	          //waitingThreadVect.add(tester);
	          //if(progressHash.containsKey(tester.getDescription())) progressHash.put(tester.getDescription(),new Integer(((Integer)progressHash.get(tester.getDescription())).intValue()+1));
	          //else progressHash.put(tester.getDescription(),new Integer(0));
	        }
	      }
	    }

	    //int initialThreads = waitingThreadVect.size();
	    //threadState_totalThreads = initialThreads;

	    //timer = new Timer();
	    //timerListener = new TimerListener();
	    //timer.scheduleAtFixedRate(timerListener, 0, 50);
	    /*
	    if(!threaded) {
	      while(waitingThreadVect.size() > 0 || (finishedThreadVect.size() < initialThreads)) {
	        try {((SigTestThread)runningThreadVect.get(0)).join();}
	        catch (Exception e) {e.printStackTrace();}
	      }
	      timer.cancel();
	    }
	    else {
	      this.threadState_running = true;
	    }
	    */
	  }

	 /**
	  * Converts a set of edges (which separate adjacent predictions) to a set of windows. N edges-->N+1 windows
	  * @param edges int[]
	  * @param datlength int
	  * @return ArrayList
	  */
	 private ArrayList getWindowsFromEdges(int[] edges, int datlength) {
	    int last = 0;
	    int i;

	    ArrayList segments = new ArrayList();

	    for (i = 0; i < edges.length; i++) {
	      SegmentInfo currSeg = new SegmentInfo();
	      currSeg.setStart(last);
	      currSeg.setEnd(edges[i]-1);
	      segments.add(currSeg);
	      last = edges[i];
	    }
	    SegmentInfo currSeg = new SegmentInfo();
	    currSeg.setStart(last);
	    currSeg.setEnd(datlength - 1);
	    segments.add(currSeg);

	    return segments;
	  }
	 /**
	  * Median filter segment size.
	  */
	  public static final int MEDIAN_WINDOW = 5;
	  /**
	   * Smoothing filter window size.
	   */
	  public static final int SMOOTH_WINDOW = 3;
	  /**
	   * The number of initial edge estimates reported from filtering stage: num_estimates = PERCENT_EDGES*chromosome_length
	   */
	  public static final float PERCENT_EDGES = .2f;
	  /**
	   * The minimum index (in genes) between any two initial edge estimates from filtering stage.
	   */
	  public static final int ADJACENT_DIST = 2;
	  /**
	   * Edge removal SNR criteria BEFORE edge convergence.
	   */
	  public static final float INIT_SNR = 0.5f;
	  /**
	   * Edge removal SNR criteria AFTER edge convergence.
	   */
	  public static final float MAX_SNR = 0.75f;
	  
	  private static float MERGE_SNR_THRESH = 1.5f;
	  /**
	   * Max number of ChARM threads to start simultaneously (num_processors + 1 recommended)
	   */
	  //private static int NUM_THREADS=4;	  
	/**
     * Runs the data through a median filter, a mean smoothing filter, and a
     * differentiating filter. SegmentInfo sizes are constants. Returns a float
     * array of the differentiator results- peaks.
     *
     * @param ratios float[]
     * @return float[]
     */
    private float[] filterData(float[] ratios) {

      float[] medians = new float[ratios.length];

      if(ratios.length < 2) {
        float[] newArray=new float[ratios.length];
        System.arraycopy(ratios,0,newArray,0,ratios.length);
        return newArray;
      }

      //test
     // System.out.println("MEDIANS: ");

      for (int i = 0; i < ratios.length; i++){

        medians[i] = Statistics.getMedian(ratios, i-(MEDIAN_WINDOW/2), i+(MEDIAN_WINDOW/2 ));

        //test
        //System.out.println("Index "+i+" = "+medians[i]);
      }

      float[] means = new float[medians.length];

      //test
      //System.out.println("MEANS: ");

      for (int i = 0; i < means.length; i++){ 

        means[i] = Statistics.getMean(medians, i - (SMOOTH_WINDOW/2), i+(SMOOTH_WINDOW/2));

        //test
        //System.out.println("Index "+i+" = "+means[i]);
      }

      float[] diff = new float[means.length];
      //test
      //System.out.println("DIFF: ");
      int i;
      for ( i = 0; i < diff.length-1; i++){

        int lower;
        if (i > 0){
          lower = i-1;
        }
        else {
          lower = 0;
        }

        diff[i] = means[i+1] - means[lower];
      }

      if(i > 0) { diff[i] = means[i] - means[i-1]; }
      else { diff[i] = means[i];}

      return diff;
    }

    /**
     * Estimates edges based on the data from the differentiating filter. Takes the
     * indices of the highest percentile peaks which are non-adjacent to be edges.
     * For adjacent peaks, it will take the middle peak to be the edge. Returns an
     * int array of edge estimates- each value is the index of the edge.
     * @param peaks float[]
     * @return int[]
     */
    private int[] estimateEdges(float[] peaks, int numEdges){

      float thresh = 0.95f;

      //test
      //System.out.println("MAX NUM EDGES: "+ num_edges);
      int num_edges = numEdges;

      int[] edges = new int[num_edges];
      Arrays.fill(edges, -1);
      int curr_edge =0;

      while ((curr_edge < num_edges) && (thresh >= .7)){

        for (int i = 0; i < peaks.length; i++){
          if (Statistics.getPercentile(peaks, peaks[i]) > thresh){

            //if (curr_edge == (num_edges-1)){
            if (curr_edge >= edges.length){
              edges = resizeArray(edges);
            }
            //test
            //System.out.println("CURRENT EDGE #= "+curr_edge + ", @INDEX="+  i +"NUM EDGES=" + num_edges);
            edges[curr_edge] = i;
            curr_edge++;
          }
        }

        curr_edge = cleanEdges(edges);

        thresh -= .02;
      }

      int[] estimates = new int[curr_edge];

      System.arraycopy(edges, 0, estimates, 0, curr_edge);

      return estimates;
    }
    
    /**
     * Doubles the size of the supplied array.
     * @param arr int[]
     * @return int[]
     */
    private int[] resizeArray(int[] arr){
       int[] newArr = new int[arr.length*2+1];

       System.arraycopy(arr, 0, newArr, 0, arr.length);

       Arrays.fill(newArr, arr.length, newArr.length, -1);

       return newArr;
     }
    
    /**
     * Removes adjacent edges, returns total # of edges still in the array.
     * @param edges int[]
     * @return int
     */
    private int cleanEdges(int[] edges){

      Arrays.sort(edges);

      int edge_count=0;
      int count = 0;

      //try #2

      while (count < edges.length-1){
        //test
        //System.out.println("count="+count+" value="+edges[count]);
        if (edges[count] != -1){

          if ((edges[count+1]-edges[count]) <= ADJACENT_DIST) {
            int i;
            for (i = count;
                 ( ( (i + 1) < edges.length ) &&
                   ( (edges[i + 1] - edges[i]) <= ADJACENT_DIST) ); i++) {}

            edges[edge_count] = edges[ (i + count +1 ) / 2];//round up or down?
            //test
            //System.out.println("Incremented to "+i+", edgecount ="+edge_count+", value taken =" +edges[edge_count]);
            count = i;
            edge_count++;
          }

          //if ((edges[count+1]-edges[count]) > 1){
          else {
            edges[edge_count] = edges[count];
            edge_count++;
          }

          //last case
          if ( (count + 1) == (edges.length - 1)) {
            edges[edge_count] = edges[count + 1];
            edge_count++;
          }

        }
        count++;
      }
      Arrays.fill(edges, edge_count, edges.length, -1);
      

      //test, Raktim
      /*
      System.out.println("Printing Edges");
      for (int i = 0; i < edges.length; i++){
       System.out.println(edges[i]);
     }
     */
      return edge_count;
    }
    
    /**
     * Runs the EM algorithm to find exact edge estimates. returns arraylist of edges.
     * @param edgeEstimates int[]
     * @param data double[]
     * @return ArrayList
     */
    private ArrayList emEdges(int[] edgeEstimates, float[] data){

      ArrayList edges = new ArrayList();

      for (int i = 0; ((i < edgeEstimates.length)&&(edgeEstimates[i] != -1)); i++){
        edges.add( new Edge(edgeEstimates[i]));
      }

      setWindowSizes(edges, data);

      //initializes edges
      for (int i = 0; i < edges.size(); i++) {
        Edge curredge = ( (Edge) edges.get(i));
        initializeEdge(curredge, data);
        curredge.initializePriors(edges.size());
      }

      //test
      //printEdgeData(edges);
      float snr_thresh = INIT_SNR;//threshold for merging windows on two sides of an edge
      int zerocount = 0;
      int maxcount = 20;
      int count = 0;
      ArrayList edgedata = new ArrayList();

      for (int i = 0; i < edges.size(); i++){
        edgedata.add(new Edgedata());
      }

      while ((zerocount < 5) && (edges.size() > 0) && (count < maxcount)) {

        int iter = 0;
        int max_iterations = 40;
        float improvement = 1;

        for (int i = 0; i < edges.size(); i++){
          Edge curredge = (Edge)edges.get(i);
          curredge.resetLikelihood();
        }

        while ( (iter < max_iterations) && (improvement > 0.000001)) {

          //saves the data from previous iteration if the edge is improving
          for (int i = 0; i < edges.size(); i++) {
            Edgedata curr_edgedata = (Edgedata) edgedata.get(i);
            Edge curr_edge = (Edge) edges.get(i);
            if (curr_edge.isImproving()) {
              curr_edgedata.left_geneprobs = curr_edge.getLeftGeneprobs();
              curr_edgedata.right_geneprobs = curr_edge.getRightGeneprobs();
              curr_edgedata.left_posterior = curr_edge.getLeftPosteriors();
              curr_edgedata.right_posterior = curr_edge.getRightPosteriors();
              curr_edgedata.left_mean = curr_edge.getLeftMean();
              curr_edgedata.left_var = curr_edge.getLeftVar();
              curr_edgedata.right_mean = curr_edge.getRightMean();
              curr_edgedata.right_var = curr_edge.getRightVar();
              curr_edgedata.left_prior = curr_edge.getLeftPrior();
              curr_edgedata.right_prior = curr_edge.getRightPrior();
            }
          }

          updateWindowsParameters(edges, data);
          updateWindowsMemberships(edges, data);

          improvement = updateLikelihood(edges);

          iter++;
        }

        //reloads the data from previous iteration
        for (int i = 0; i < edges.size(); i++) {
          Edgedata curr_edgedata = (Edgedata) edgedata.get(i);
          Edge curr_edge = (Edge) edges.get(i);

          curr_edge.setGeneProbabilities(curr_edgedata.left_geneprobs, curr_edgedata.right_geneprobs);
          curr_edge.setPosteriors(curr_edgedata.left_posterior, curr_edgedata.right_posterior);
          curr_edge.setPriors(curr_edgedata.left_prior, curr_edgedata.right_prior);
          curr_edge.setLeftDistribution(curr_edgedata.left_mean, curr_edgedata.left_var);
          curr_edge.setRightDistribution(curr_edgedata.right_mean, curr_edgedata.right_var);
        }

        //printEdgeData(edges);

        double convergence = adjustEdgePositions(edges, data);

        if (convergence == 0){
          zerocount++;
        }

        mergeWindows(edges, data, snr_thresh);

        count++;

      }

      snr_thresh = MAX_SNR;

      mergeWindows(edges, data, snr_thresh);

      //test-------------------------------------------------------------
      //System.out.println("\nFINAL EDGES after "+(count)+" big loop iterations:\n");
      //printEdgeData(edges);
      //-----------------^^^^^^^^^^^^^^^^^^^^^^^^---------------------------

      return edges;
    }

    /**
     * data holder object- acts as a struct to hold the data for an edge
     */

    private static class Edgedata {

      public float left_mean; //means and variances for the distribution
      public float left_var;
      public float right_mean;
      public float right_var;

      public float[] left_posterior; //indexed from 0 to roi size/2- does not match data indices
      public float[] right_posterior;
      public float[] left_geneprobs;
      public float[] right_geneprobs;
      public float left_prior;
      public float right_prior;

      Edgedata() {
      }

    }
/*
    private class TimerListener extends TimerTask {

        public void run() {
          int currSize = runningThreadVect.size();
          if(currSize > 0) {
            Enumeration runningThreadEnum = runningThreadVect.elements();
            int numDone=0;
            while(runningThreadEnum.hasMoreElements()) {
              SigTestThread currElement = (SigTestThread)runningThreadEnum.nextElement();
              if(currElement.hasRun()) {
                numDone++;
                runningThreadVect.remove(currElement);
                finishedThreadVect.add(currElement);
                if(((Integer)progressHash.get(currElement.getDescription())).intValue() == 1) System.out.println("Finished " + currElement.getDescription());
                progressHash.put(currElement.getDescription(),new Integer(((Integer)progressHash.get(currElement.getDescription())).intValue()-1));
              }
            }
          }

         currSize = runningThreadVect.size();
          while(currSize < NUM_THREADS && waitingThreadVect.size() > 0) {
            SigTestThread currThread = (SigTestThread)waitingThreadVect.get(waitingThreadVect.size()-1);
            waitingThreadVect.remove(currThread);
            currThread.start();
            runningThreadVect.add(currThread);
            currSize++;
          }

          threadState_progress = (double)(finishedThreadVect.size()+0.0)/threadState_totalThreads;
          //System.out.println("Progress: "+threadState_progress);

          if(threadState_stopflag) {
             if(currSize > 0) {
              Enumeration runningThreadEnum = runningThreadVect.elements();
              int numDone=0;
              while(runningThreadEnum.hasMoreElements()) {
                SigTestThread currElement = (SigTestThread)runningThreadEnum.nextElement();
                currElement.stopTests();
              }
            }
            this.cancel();
          }

          if(currSize == 0 && waitingThreadVect.size() == 0) { this.cancel(); threadState_running = false;}
        }
      }
*/
    /**
     * Initializes everything but the window sizes and priors for each edge:
     * sets ROI window, then updates means and variances for left and right ROI
     * distributions, posterior probabilities P(O|G) and gene probabilities P(G|O)
     *
     * @param curredge Edge
     * @param data double[]
     */
    private void initializeEdge(Edge curredge, float[] data){

        curredge.setInfluenceWindow(data.length);

        //set left distr
        float mean = Statistics.getMean(data, curredge.getLeftROI(), curredge.getPosition());
        float var = Statistics.getVariance(data, curredge.getLeftROI(),curredge.getPosition()); //check indices accuracy
        curredge.setLeftDistribution(mean, var);

        //set right distr
        mean = Statistics.getMean(data, curredge.getPosition(),curredge.getRightROI() );
        var = Statistics.getVariance(data, curredge.getPosition(),curredge.getRightROI()); //check indices accuracy
        curredge.setRightDistribution(mean, var);

        //initial posterior P(O|G) and gene/membership P(G|O) probabilities
        curredge.initializePosterior();
        updateGeneProbabilities(curredge, data);
    }
    /**
     * The windows and ROI must be set already. Updates the P(G|O) for each gene based on
     * a gaussian distribution for each side.
     *
     * @param curredge Edge
     * @param data double[]
     */
    private void updateGeneProbabilities(Edge curredge, float[] data) {
      //set gene probs:
    	float[] leftgeneprob = new float[curredge.getRightROI() - curredge.getLeftROI()];
    	float[] rightgeneprob = new float[leftgeneprob.length];

      //left:
      int count = 0;
      for (int j = curredge.getLeftROI(); j < curredge.getRightROI(); j++) {
        leftgeneprob[count] = Statistics.getGaussianDensity(data[j],(float)curredge.getLeftMean(), (float)curredge.getLeftStd());
        count++;
      }
      //right:
      count = 0;
      for (int j = curredge.getLeftROI(); j < curredge.getRightROI(); j++) {
        rightgeneprob[count] = Statistics.getGaussianDensity(data[j],(float)curredge.getRightMean(), (float)curredge.getRightStd());
        count++;
      }

      curredge.setGeneProbabilities(leftgeneprob, rightgeneprob);
    }

    /**
     * Takes in the edges and data, and sets the window sizes for
     * each edge.
     *
     * @param edges ArrayList
     * @param data float[]
     */
    private void setWindowSizes(ArrayList edges, float[] data){

      int lastedge = 0;
      int i;
      for (i = 0; i < edges.size(); i++){

        Edge curredge = ((Edge)edges.get(i));
        int windowsize = curredge.getPosition() - lastedge;

        curredge.setLeftWindow(windowsize);

        if (i > 0){
          Edge prevedge = (Edge)edges.get(i-1);
          prevedge.setRightWindow(windowsize);
        }

        lastedge = curredge.getPosition();
      }
      if (i > 0) {
        Edge finaledge = (Edge) edges.get(i - 1);
        finaledge.setRightWindow(data.length - finaledge.getPosition());
      }

    }
    
    /**
     * Updates the distribution parameters- mean and variance, for each
     * edges' influence windows.
     *
     * @param edges ArrayList
     * @param data float[]
     */
    private void updateWindowsParameters(ArrayList edges, float[] data){

      for (int i = 0; i < edges.size(); i++){
        Edge curredge = ((Edge)edges.get(i));
        if (curredge.isImproving()){
          curredge.updateDistributions(data);
        }
      }
    }
    
    /**
     * Updates the posterior and prior gene probabilities for each edge.
     *
     * @param edges ArrayList
     * @param data double[]
     */
    private void updateWindowsMemberships(ArrayList edges, float[] data){

      for (int i = 0; i < edges.size(); i++){
        Edge curredge = ((Edge)edges.get(i));
        if (curredge.isImproving()){
          updateGeneProbabilities(curredge, data);
          curredge.updateProbabilities(data);
        }
      }
    }

    /**
     * Updates log likelihood of gene data given edges, distributions.
     * @param edges ArrayList
     * @return double
     */
    private float updateLikelihood(ArrayList edges){

    	float total_improvement = 0;

      for (int i = 0; i < edges.size(); i++){
        total_improvement += ((Edge)edges.get(i)).updateLikelihood();
      }

      return total_improvement;

    }
    /**
     * Takes in the edges and data, and has each edge adjust its own position.
     * Resets the window sizes and ROI based on the edge adjustments, and calculates a
     * measure of convergence which is returned.
     * @param edges ArrayList
     * @param data double[]
     * @return double
     */
    private float adjustEdgePositions(ArrayList edges, float[] data){

      int total_changes = 0;
      //int lastpos = 0;
      int i;
      for (i = 0; i < edges.size(); i++){
        Edge curredge = (Edge)edges.get(i);

        //adjusts window sizes as it goes through the edges
        /*curredge.setLeftWindow(curredge.getPosition() - lastpos);

        if (i < edges.size() - 1){
          Edge nextedge = (Edge)edges.get(i+1);
          curredge.setRightWindow(nextedge.getPosition() - curredge.getPosition());
        }
        else {
          curredge.setRightWindow(data.length - curredge.getPosition());
        }
        lastpos = curredge.getPosition();
        */

        setWindowSizes(edges, data);
        updateRoiParameters(curredge, data);
        total_changes += curredge.adjustPosition();
      }

      setWindowSizes(edges, data);//resets window sizes

      for (int j = 0; j < edges.size(); j++){
        Edge curredge = (Edge)edges.get(j);
        initializeEdge(curredge, data);
        curredge.initializePriors(edges.size());
      }

      float convergence_measure = (float)total_changes / (float)edges.size();

      return convergence_measure;
    }
    
    /**
     * Takes in the edges and data and a threshhold for the SNR cutoff.
     * Goes through each edge and checks if it should be removed based on SNR cutoff.
     * If it should be removed, it flags the edges before and after it to reset,
     * and removes the current edge. After removing all the edges it resets all
     * window sizes, and re- initializes all edges that needed to be reset
     *
     * @param edges ArrayList
     * @param data double[]
     * @param thresh double
     */
    private void mergeWindows(ArrayList edges, float[] data, float thresh){

      int ct = 0;
      //goes through removing edges and flagging the ones next to them as reset
      while (ct < edges.size()){

        Edge curredge = (Edge)edges.get(ct);

        if (curredge.shouldRemove(data, thresh)){

          if (ct > 0){
            Edge prevedge = (Edge)edges.get(ct-1);
            prevedge.setReset(true);
          }
          if (ct < edges.size()-1){
            Edge nextedge = (Edge)edges.get(ct+1);
            nextedge.setReset(true);
          }
          edges.remove(ct);
         }
        else {
         ct++;
        }
      }
      //resets window sizes and edges
      setWindowSizes(edges, data);

      for (int j = 0; j < edges.size(); j++){
        Edge curredge = (Edge)edges.get(j);

        if (curredge.shouldReset()){
          initializeEdge(curredge, data);
          curredge.initializePriors(edges.size());
          curredge.setReset(false);
        }
      }
    }

    /**
     * Merges windows (predictions) who do not meet the specified SNR threshold.
     * @param exp Strin
     * @param chromosome int
     */
    private void mergeWindows(ArrayList windowArray, float[] data) {

        for(int i=1; i < windowArray.size(); i++) {
          SegmentInfo currLeft = ((SegmentInfo)windowArray.get(i-1));
          SegmentInfo currRight = ((SegmentInfo)windowArray.get(i));

          float leftMean = Statistics.getMean(data,currLeft.getStart(),currLeft.getEnd()+1);
          float leftVar = Statistics.getVariance(data,currLeft.getStart(),currLeft.getEnd()+1);

          float rightMean = Statistics.getMean(data,currRight.getStart(),currRight.getEnd()+1);
          float rightVar = Statistics.getVariance(data,currRight.getStart(),currRight.getEnd()+1);

          float snr = (float)Math.abs(leftMean-rightMean)/(float)Math.sqrt(leftVar/currLeft.getSize() + rightVar/currRight.getSize());

          if(snr < this.MERGE_SNR_THRESH) {
              currLeft.setEnd(currRight.getEnd());
              currLeft.resetSegment();
              windowArray.remove(i);
              i--;
            }
          }
        }
    /**
     * Helper function for adjust edge positions which updates ROI parameters for an edge
     * assumes window sizes are already set.
     * @param curredge Edge
     * @param data double[]
     */
    private void updateRoiParameters(Edge curredge, float[] data){

      int old_roi_left = curredge.getLeftROI();
      int old_roi_right = curredge.getRightROI();

      curredge.setInfluenceWindow(data.length);//sets new ROI window

      float[] left_newpost = new float[curredge.getRightROI() - curredge.getLeftROI()];
      float[] right_newpost = new float[curredge.getRightROI() - curredge.getLeftROI()];
      float[] left_newgeneprobs = new float[curredge.getRightROI() - curredge.getLeftROI()];
      float[] right_newgeneprobs = new float[curredge.getRightROI() - curredge.getLeftROI()];

      //change to System.arraycopy?
      int oldct = 0;
      int newct = 0;
      //copies all old posteriors and geneprobs into new array, and adds 0's for new indicies in ROI
      for (int j = curredge.getLeftROI(); j < curredge.getRightROI(); j++){
        if ( (j >= old_roi_left) && (j < old_roi_right)){
          left_newpost[newct] = curredge.leftPosteriorAt(oldct);
          right_newpost[newct] = curredge.rightPosteriorAt(oldct);
          left_newgeneprobs[newct] = curredge.leftGeneprobAt(oldct);
          right_newgeneprobs[newct] = curredge.rightGeneprobAt(oldct);
          oldct++;
        }
        else {
          left_newpost[newct] = right_newpost[newct] = left_newgeneprobs[newct] =
              right_newgeneprobs[newct] = 0;
        }
        newct++;
      }

      curredge.setPosteriors(left_newpost, right_newpost);
      curredge.setGeneProbabilities(left_newgeneprobs, right_newgeneprobs);
    }

    /**
     * Adds specified experiment to the list of selected experiments.
     * @param exp String
     */
    public void addSelectedExperiment(String exp) {
        
    	//Raktim Mar 11
    	displayExps.add(exp);
    	//selectedExps.add(exp);
      }

      /**
       * Removes specified experiment from list of selected experiments.
       * @param exp String
       */
      public void removeSelectedExperiment(String exp) {
        
    	  //Raktim Mar 11
    	  if(displayExps.contains(exp)) {displayExps.remove(exp);}
    	  //if(selectedExps.contains(exp)) {selectedExps.remove(exp);}
      }

      /**
       * Adds specified list of experiments to set of selected experiments.
       * @param exps ArrayList
       */
      public void addSelectedExperiments(ArrayList exps) {
    	  
    	 //Raktim Mar 11
      	displayExps.addAll(exps);
    	//selectedExps.addAll(exps);
      }
      
    /**
     * Getting ratios without NAs
     * Rakitm Feb 2, 07
     */
      
      public float [] getRatioArrayNoNa(int exprInd, int chrInd) {
    	
    	int[][] chrIndices = data.getChromosomeIndices();
    	int st = chrIndices[chrInd][0];
      	int end = chrIndices[chrInd][1];
      	
      	ArrayList tempRatios = new ArrayList();
      	
      	for(int ii = st; ii < end; ii++) {
      		float tmp = data.getLogAverageInvertedValue(exprInd, ii);
      		if (!Float.isNaN(tmp))
      			tempRatios.add(new Float(tmp));
      	}
      	  //int[] ratios = new int[tempRatios.size()];
      	  Object[] objs = tempRatios.toArray();
    	  float[] ratios = new float[objs.length];
    	  for(int i = 0; i < objs.length; i++){
    		  ratios[i] = ((Float)objs[i]).floatValue();
    	  }
    	  return ratios;
      }
      
      /**
       * The class to listen to progress, monitor and algorithms events.
       */
      private class CharmListener extends DialogListener {
          
          public void actionPerformed(ActionEvent e) {
              String command = e.getActionCommand();
              if (command.equals("cancel-command")) {
            	  isCanceled = true;
            	  runProgressPanel.dispose();
                  //monitor.dispose();
              }
          }
          
          public void windowClosing(WindowEvent e) {
        	  isCanceled = true;
        	  runProgressPanel.dispose();
              //monitor.dispose();
          }
      }
      
}
