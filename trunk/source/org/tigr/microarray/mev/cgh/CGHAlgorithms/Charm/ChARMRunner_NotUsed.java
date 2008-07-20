package org.tigr.microarray.mev.cgh.CGHAlgorithms.Charm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import org.tigr.microarray.mev.cgh.CGHDataGenerator.CharmDataGenerator.Chromosome;
import org.tigr.microarray.mev.cgh.CGHDataModel.CharmDataModel.DatasetContainer;
import org.tigr.microarray.mev.cgh.CGHDataModel.CharmDataModel.ResultContainer;
import org.tigr.microarray.mev.cgh.CGHDataModel.CharmDataModel.SegmentInfo;

/**
 * TODO
 * NOT needed in MeV. All functions moved to ChARM.java
 */
/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/

public class ChARMRunner_NotUsed extends Thread {
  /**
   * Median filter window size.
   */
  public static final int MEDIAN_WINDOW = 5;
  /**
   * Smoothing filter window size.
   */
  public static final int SMOOTH_WINDOW = 3;
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
  /**
   * The number of initial edge estimates reported from filtering stage: num_estimates = PERCENT_EDGES*chromosome_length
   */
  public static final double PERCENT_EDGES = .2;

  /**
   * Max number of ChARM threads to start simultaneously (num_processors + 1 recommended)
   */
  private static int NUM_THREADS=4;

  /**
   * Number of permutations required for mean permutation test (with Gaussian approximation)
   */
  private static int NUM_PERMUTATIONS=200;

  private static double MERGE_SNR_THRESH=1.5;

  private DatasetContainer dataset;

  private Timer timer;
  private TimerListener timerListener;
  private Vector runningThreadVect;
  private Vector waitingThreadVect;
  private Vector finishedThreadVect;

  private int numPermutes;
  private int numEdges;

  private boolean isThreaded;
  private ArrayList chromExpRunList;

  private ResultContainer resultSet;
  private boolean threadState_running;
  private boolean threadState_stopflag;
  private double threadState_progress;
  private int threadState_totalThreads;

  private HashMap progressHash = new HashMap();

  /**
   * Class constructor.
   * @param data DatasetContainer- reference to the dataset on which ChARM will be run
   */
  public ChARMRunner_NotUsed(DatasetContainer data) {
    this.dataset = data;
    runningThreadVect = new Vector();
    waitingThreadVect = new Vector();
    finishedThreadVect = new Vector();
    threadState_running=false;
    threadState_stopflag=false;
    numPermutes = this.NUM_PERMUTATIONS;
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

  public void setNumberPermutations(int num){
    numPermutes = num;
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
    return edge_count;

    //test
//    for (int i = 0; i < edges.length; i++){
//     System.out.println(edges[i]);
//   }
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

      float convergence = adjustEdgePositions(edges, data);

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
   * Returns progress (0-1) of ChARM analysis.
   * @return double
   */
  public double getProgress() {
    return threadState_progress;
  }

  /**
   * Stops this thread.
   */
  public void stopRun() {
    threadState_stopflag = true;
  }


  /**
   * Estimates edges based on the data from the differentiating filter. Takes the
   * indices of the highest percentile peaks which are non-adjacent to be edges.
   * For adjacent peaks, it will take the middle peak to be the edge. Returns an
   * int array of edge estimates- each value is the index of the edge.
   * @param peaks double[]
   * @return int[]
   */
  private int[] estimateEdges(float[] peaks){

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
      float mean = Statistics.getMean(data, curredge.getLeftROI(),
                                       curredge.getPosition());
      float var = Statistics.getVariance(data, curredge.getLeftROI(),
                                          curredge.getPosition()); //check indices accuracy
      curredge.setLeftDistribution(mean, var);

      //set right distr
      mean = Statistics.getMean(data, curredge.getPosition(),
                                curredge.getRightROI() );
      var = Statistics.getVariance(data, curredge.getPosition(),
                                   curredge.getRightROI()); //check indices accuracy
      curredge.setRightDistribution(mean, var);

      //initial posterior P(O|G) and gene/membership P(G|O) probabilities
      curredge.initializePosterior();
      updateGeneProbabilities(curredge, data);
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
   * Takes in the ratios of an experiment, and runs the charm algorithm on the data.
   * Loads the final edge predictions into the chromosome when done. The edges
   * are kept in an arraylist of Edge objects which is passed through. Must make
   * sure the ratio array is set for that experiment before running.
   *
   *
   * @param chromExps ArrayList- list of array lists which contain 2 elements - one for the chrom. one of the desired exp
   * @param threaded boolean- indicates whether to run as thread or serially
   * @return ResultContainer
   */
  /*
   * Moved to ChARM.java
   */
  /*
  public ResultContainer runCharm(ArrayList chromExps, boolean threaded){

    ResultContainer newResults = new ResultContainer(dataset);
    resultSet = newResults;

    chromExpRunList = chromExps;
    isThreaded = threaded;
    this.setPriority(Thread.MAX_PRIORITY);
    this.start();

    if(!threaded) {
      try {this.join();} catch(Exception e) {e.printStackTrace();}
    }

    return newResults;
  }
*/
  /*
   * Moved to ChARM.java Raktim 9/21
   */
 public void run() {
   ArrayList chromExps = chromExpRunList;

   for(int m=0; m < chromExps.size(); m++) {
     ArrayList currSet = (ArrayList)chromExps.get(m);
     Chromosome currChrom = (Chromosome)currSet.get(0);
     String currExp = (String)currSet.get(1);
     float[] data = currChrom.getRatioArray(currExp,false);
     float[] peaks = filterData(data);
     numEdges = Math.max((int)Math.ceil(peaks.length*PERCENT_EDGES) , 10);
     int[] edgeEstimates = estimateEdges(peaks);
     ArrayList edges = emEdges(edgeEstimates, data);
     int[] final_edges = new int[edges.size()];

     for (int i = 0; i < edges.size(); i++){
       final_edges[i] = ((Edge)edges.get(i)).getPosition();
       }

     //System.out.println("dataLength: " + data.length);
     ArrayList segments = getWindowsFromEdges(final_edges,data.length);
     this.mergeWindows(segments,data);

     for (int i=0; i < segments.size(); i++) {
       ((SegmentInfo)segments.get(i)).setBonferroniCorrection(segments.size());
       resultSet.addSegment((SegmentInfo)segments.get(i),currExp,currChrom.getNumber());
     }

   }

   runSignificanceTests(resultSet,isThreaded);

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
      SegmentInfo curr_window = new SegmentInfo();
      curr_window.setStart(last);
      curr_window.setEnd(edges[i]-1);
      segments.add(curr_window);
      last = edges[i];
    }
    SegmentInfo curr_window = new SegmentInfo();
    curr_window.setStart(last);
    curr_window.setEnd(datlength - 1);
    segments.add(curr_window);

    return segments;
  }


  /**
   * Returns the result set associated with this ChARMRunner, which is populated upon the thread finishing.
   * @return ResultContainer
   */
  public ResultContainer getResultSet() {
    return resultSet;
  }


//Significance testing----------------------------------------------------------

  /**
   * Runs significance tests on all windows contained in the supplied ResultContainer
   * @param results ResultContainer
   * @param threaded boolean- indicates whether to run as separate thread or serially
   */
  private void runSignificanceTests(ResultContainer results, boolean threaded) {
/*
    ArrayList chromosomes = (ArrayList)dataset.getChromosomes();
    ArrayList experiments = (ArrayList)dataset.getExperiments();

    runningThreadVect.clear();
    waitingThreadVect.clear();
    finishedThreadVect.clear();
    progressHash.clear();

    for(int i=0; i<chromosomes.size(); i++) {
      Chromosome currChrom = (Chromosome)chromosomes.get(i);
      for (int j=0; j<experiments.size(); j++) {
        String currExp = (String)experiments.get(j);
        ArrayList currWindows = results.getSegments(currExp,currChrom.getNumber());
        for (int k=0; k<currWindows.size(); k++) {
          SigTestThread tester = new SigTestThread(currChrom,currExp,(SegmentInfo)currWindows.get(k));
          tester.setTests(true, true,false,false);
          //tester.setNumberPermutations(numPermutes);
          tester.setPriority(Thread.MIN_PRIORITY);
          waitingThreadVect.add(tester);
          if(progressHash.containsKey(tester.getDescription())) progressHash.put(tester.getDescription(),new Integer(((Integer)progressHash.get(tester.getDescription())).intValue()+1));
          else progressHash.put(tester.getDescription(),new Integer(0));
        }
      }
    }

    int initialThreads = waitingThreadVect.size();
    threadState_totalThreads = initialThreads;

    timer = new Timer();
    timerListener = new TimerListener();
    timer.scheduleAtFixedRate(timerListener, 0, 50);

    if(!threaded) {
      while(waitingThreadVect.size() > 0 || (finishedThreadVect.size() < initialThreads)) {
        try {((SigTestThread)runningThreadVect.get(0)).join();}
        catch (Exception e) {}
      }
      timer.cancel();
    }
    else {
      this.threadState_running = true;
    }
    */
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
      leftgeneprob[count] = Statistics.getGaussianDensity(data[j], curredge.getLeftMean(), curredge.getLeftStd());
      count++;
    }
    //right:
    count = 0;
    for (int j = curredge.getLeftROI(); j < curredge.getRightROI(); j++) {
      rightgeneprob[count] = Statistics.getGaussianDensity(data[j], curredge.getRightMean(), curredge.getRightStd());
      count++;
    }

    curredge.setGeneProbabilities(leftgeneprob, rightgeneprob);
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
   * Takes in the edges and data, and sets the window sizes for
   * each edge.
   *
   * @param edges ArrayList
   * @param data double[]
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
   * Updates the distribution parameters- mean and variance, for each
   * edges' influence windows.
   *
   * @param edges ArrayList
   * @param data double[]
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
     * Runs the data through a median filter, a mean smoothing filter, and a
     * differentiating filter. SegmentInfo sizes are constants. Returns a double
     * array of the differentiator results- peaks.
     *
     * @param ratios double[]
     * @return double[]
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


//data holder object- acts as a struct to hold the data for an edge

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

    public Edgedata() {
    }

  }

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
         //currThread.start();
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
             //currElement.stopTests();
           }
         }
         this.cancel();
       }

       if(currSize == 0 && waitingThreadVect.size() == 0) { this.cancel(); threadState_running = false;}
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

         double snr = Math.abs(leftMean-rightMean)/Math.sqrt(leftVar/currLeft.getSize() + rightVar/currRight.getSize());

         if(snr < this.MERGE_SNR_THRESH) {
             currLeft.setEnd(currRight.getEnd());
             currLeft.resetSegment();
             windowArray.remove(i);
             i--;
           }
         }
       }


}
