package org.tigr.microarray.mev.cgh.CGHAlgorithms.Charm;
import java.util.Arrays;

/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/

public class Edge {

  /**
   * The maximum allowable radius of influence (i.e. the number of genes on either
   * side of an edge that influence edge placement.
   */
  public static final int MAX_ROI = 20;
  /**
   * The radius of influence is determined as follows: min(PERCENT_WINDOW_SIZE*window_size,MAX_ROI).
   */
  public static final float PERCENT_WINDOW_SIZE = 0.5f;
  /**
   * Starting likelihood value for EM iterations.
   */
  public static final float START_LIKELIHOOD = -1000000;
  /**
   * Minimum allowabel window size (windows smaller than this are discarded during EM iterations.
   */
  public static final int MIN_WINDOW = 2;

  private int position;
  private int roi_left;//actual indices of ROI- not size
  private int roi_right;
  private int left_window;//sizes of windows- not indices
  private int right_window;

  private float left_mean;//means and variances for the distribution
  private float left_var;
  private float right_mean;
  private float right_var;

  private float[] left_posterior;//indexed from 0 to roi size/2- does not match data indices
  private float[] right_posterior;
  private float[] left_geneprobs;
  private float[] right_geneprobs;
  private float left_prior;
  private float right_prior;

  private boolean reset;
  private boolean improving;

  private float likelihood;
  /**
   * Class constructor.
   * @param pos int- the position (gene index) of this edge
   */
  public Edge(int pos) {
    position = pos;
    left_mean = left_var = right_mean = right_var = 0;
    roi_left = roi_right = left_window = right_window = 0;
    left_prior = right_prior = 0;
    reset = false;
  }

//Initial setter functions-----------------------------------------------------
  /**
   * Sets the size of the left radius of influence.
   * @param size int
   */
  public void setLeftWindow(int size){
    left_window = size;
  }

  /**
   * Sets the size of the right radius of influence.
   * @param size int
   */
  public void setRightWindow(int size){
    right_window = size;
  }




/**
 * Sets influence window.
 * note- indices are set to match matlab: in matlab the right bound is less by 1,
 * but inclusive. Here the end bound is not inclusive- thus 1 is added to it.
 * This function works based on having the window sizes already set.
 * @param datalength int
 */
public void setInfluenceWindow(int datalength){

    int roi_size = MAX_ROI;
    int half;
    if ((position - left_window) == 0) {
      half = left_window;
    }
    else {
      half = (int)(left_window * PERCENT_WINDOW_SIZE);
    }
    if (half < roi_size) {
      roi_size = half;
    }

    if ((position + right_window) == datalength){
      half = right_window - 1;
    }
    else {
      half = (int)(right_window*PERCENT_WINDOW_SIZE);
    }
    if (half < roi_size) {
      roi_size = half;
    }

    roi_left = position - roi_size;
    roi_right = position + roi_size + 1;
  }

  /**
   * Sets mean and variance of the left distribution associated with this edge.
   * @param mean float
   * @param var float
   */
  public void setLeftDistribution(float mean, float var){
    left_mean = mean;
    left_var = var;
  }

  /**
   * Sets mean and variance of the right distribution associated with this edge.
   * @param mean float
   * @param var float
   */
  public void setRightDistribution(float mean, float var){
    right_mean = mean;
    right_var = var;
  }


  /**
   * Initialize the left and right posterior probabilities.
   * (do after windows are set)
   */
  public void initializePosterior(){
    left_posterior = new float[roi_right - roi_left ];
    right_posterior = new float[roi_right - roi_left ];

    Arrays.fill(left_posterior, 0, position-roi_left, 1.0f);
    Arrays.fill(right_posterior, position-roi_left, roi_right - roi_left, 1.0f);
  }

  /**
   * Sets posterior probabilities of left and right distributions (one for each gene in the ROI).
   * @param left float[]
   * @param right float[]
   */
  public void setPosteriors(float[] left, float[] right){
    left_posterior = left;
    right_posterior = right;
  }

  /**
   * Set gene probabilities, p(g|O), where O is the current left and right distribution.
   * @param left float[]
   * @param right float[]
   */
  public void setGeneProbabilities(float[] left, float[] right ){
    left_geneprobs = left;
    right_geneprobs = right;
  }

  /**
   * Initializes priors of left and right dist.
   * @param length_edges int
   */
  public void initializePriors(int length_edges){
    left_prior = 0.5f/length_edges;
    right_prior = left_prior;
  }

  /**
   * Sets priors for left and right dist.
   * @param left float
   * @param right float
   */
  public void setPriors(float left, float right){
    left_prior = left;
    right_prior = right;
  }

//EM algorithm functions--------------------------------------------------------

  /**
   * Performs maximum likelihood updates of left and right distributions, given
   * the current posteriors.
   * @param data float[]
   */
  public void updateDistributions(float[] data){

    //numerators
    float left_weighted = 0;
    float right_weighted = 0;
    //denominator
    float post_sum_left = Statistics.getSum(left_posterior);
    float post_sum_right = Statistics.getSum(right_posterior);

    //sets means for each side
    int count = 0;
    for (int i = roi_left; i < roi_right; i++) {
      right_weighted += right_posterior[count] * data[i];
      left_weighted += left_posterior[count] * data[i];

      count++;
    }

    left_mean = (left_weighted / post_sum_left);

    right_mean = (right_weighted / post_sum_right);

    if (Float.isNaN(left_mean)){
      left_mean = Statistics.getMean(data, roi_left, position);
    }
    if (Double.isNaN(right_mean)){
      right_mean = Statistics.getMean(data, position, roi_right);
    }

    //set variances for each side
    int ct = 0;
    float left_weighted_var = 0;
    float right_weighted_var = 0;

    for (int i = roi_left; i < roi_right; i++){

      left_weighted_var += Math.pow(Math.abs(data[i] - left_mean), 2)*left_posterior[ct];
      right_weighted_var += Math.pow(Math.abs(data[i] - right_mean), 2)*right_posterior[ct];
      ct++;
    }

    left_var = left_weighted_var / post_sum_left;

    right_var = right_weighted_var / post_sum_right;

    if (Double.isNaN(left_var)) {
     left_var = Statistics.getVariance(data, roi_left, position);
    }
    if (Double.isNaN(right_var)) {
      right_var = Statistics.getVariance(data, position, roi_right);
    }

    /*System.out.println("EDGE " + position + ": " + " sum of left_posteriors =" +
                       post_sum_left
                       + ", some data =" + data[0] + ", " + data[1] +
                       " left_mean = " + left_mean + ", left_var = " + left_var);
*/
  }

  /**
   * Updates p(g|0) given the left and right distributions.
   * @param data float[]
   */
  //updates posterior and prior probs. for each gene in ROI.
  public void updateProbabilities(float[] data){

    for (int i = 0; i < left_posterior.length; i++){
      //denominator- sum over both distributions
      float gene_prob = left_prior*left_geneprobs[i] + right_prior*right_geneprobs[i];

        left_posterior[i] = (left_geneprobs[i] * left_prior) / gene_prob;
        right_posterior[i] = (right_geneprobs[i] * right_prior) / gene_prob;

        if (Double.isNaN(left_posterior[i])){
          left_posterior[i] = 0;
        }

        if (Double.isNaN(right_posterior[i])){
          right_posterior[i] = 0;
        }

    }

    left_prior = Statistics.getSum(left_posterior)/(data.length);
    right_prior = Statistics.getSum(right_posterior)/(data.length);
  }


  /**
   * Adjusts edge position using "minimum surprise" metric and returns the edge
   * position change (in gene indices).
   * @return int
   */
  public int adjustPosition(){

    double min_val = 1e9;
    int best_position = position;
    int count = 0;
    int change = 0;

    for (int pos = roi_left; pos < roi_right ; pos++){

      float obj_val = 0;
      double cutoff = 0.00001;
      for (int i = 0; i < count; i++){
        if (left_posterior[i] > cutoff){
          obj_val -= Math.log(left_posterior[i]);
          obj_val += Math.log(right_posterior[i]);
        }
        else {
          obj_val -= Math.log(cutoff);
        }
      }

      for (int i = count; i < right_posterior.length; i++){
        if (right_posterior[i] > cutoff){
          obj_val -= Math.log(right_posterior[i]);
          obj_val += Math.log(left_posterior[i]);
        }
        else {
          obj_val -= Math.log(cutoff);
        }
      }

      if (obj_val < min_val){
        min_val = obj_val;
        best_position = pos;
      }

      count++;
    }

    if ((best_position != position)&&(best_position > 0)){
      change = Math.abs(position - best_position);
      position = best_position;
    }

    return change;
  }

  /**
   * Sets reset flag.
   * @param should_reset boolean
   */
  public void setReset(boolean should_reset){
    reset = should_reset;
  }

  /**
   * Resets likelihood value.
   */
  public void resetLikelihood(){
    likelihood = START_LIKELIHOOD;
    improving = true;
  }

  //-----getters-------------------------------------
  //pass in the data and a threshhold- returns true if the edge should be removed,
  // and false if it should not

  /**
   * Returns flag specifying whether this edge should be removed or not based on SNR metric.
   * @param data float[]- gene data in ROI
   * @param snr_thresh float- SNR threshold
   * @return boolean
   */
  public boolean shouldRemove(float[] data, float snr_thresh){


    if ( (left_window <= MIN_WINDOW) ||
        ( (position + right_window == data.length) &&
         (right_window <= MIN_WINDOW))) {
      return true;
    }

    int leftbound = position - left_window;
    int rightbound = position + right_window;
    float snr_ratio;
    float left_median = Statistics.getMedian(data, leftbound, position);
    float right_median = Statistics.getMedian(data, position, rightbound);

    float deviation = 0f;

    for (int i = leftbound; i < position; i++){
      deviation += Math.abs(data[i] - left_median);
    }

    for (int i = position; i < rightbound; i++){
      deviation += Math.abs(data[i] - right_median);
    }

    snr_ratio = Math.abs(left_median - right_median) / (deviation / (left_window+right_window) );

    //System.out.println("Edge "+position+": SNR RATIO= "+snr_ratio);

    return (snr_ratio < snr_thresh);

  }


  /**
   * Recalculates the likelihood of genes in ROI given the current distribution
   * parameters.
   * @return float
   */
  public float updateLikelihood(){

    float[] maxprobs = new float[left_geneprobs.length];

    for (int i = 0; i < left_geneprobs.length; i++) {
      maxprobs[i] = Math.max(left_geneprobs[i], right_geneprobs[i]);
    }

    float new_likelihood = 0;

    for (int i = 0; i < maxprobs.length; i++) {
      if (maxprobs[i] > 0) {
        new_likelihood += Math.log(maxprobs[i]);
      }
    }
    float improvement = new_likelihood - likelihood;
    if (improvement > 0.0001){
      improving = true;
    }
    else {
      improving = false;
    }

//    System.out.println("Edge "+position+":Old likelihood: "+likelihood+", New Likelihood: "+new_likelihood);
    likelihood = new_likelihood;

    return improvement;
  }

//Data access methods-------------------------------------------------------
  /**
   * Indicates whether the likelihood score is improving or not.
   * @return boolean
   */
  public boolean isImproving(){
    return improving;
  }

  /**
   * Returns value of likelihood reset flag.
   * @return boolean
   */
  public boolean shouldReset(){
    return reset;
  }

  /**
   * Returns value of left posterior at the specified index.
   * @param index int
   * @return float
   */
  public float leftPosteriorAt(int index){
    return left_posterior[index];
  }

  /**
   * Returns the value of the right posterior at the specified index.
   * @param index int
   * @return float
   */
  public float rightPosteriorAt(int index) {
    return right_posterior[index];
  }

  /**
   * Returns the value of the p(g|O_l) (left distributions).
   * @param index int
   * @return float
   */
  public float leftGeneprobAt(int index) {
    return left_geneprobs[index];
  }

  /**
   * Returns the value of the p(g|O_r) (right distributions).
   * @param index int
   * @return float
   */
  public float rightGeneprobAt(int index){
    return right_geneprobs[index];
  }

  /**
   * Returns the current position of this edge.
   * @return int
   */
  public int getPosition(){
    return position;
  }

  /**
   * Returns the edge of the left ROI.
   * @return int
   */
  public int getLeftROI(){
    return roi_left;
  }

  /**
   * Returns the edge of the right ROI.
   * @return int
   */
  public int getRightROI(){
    return roi_right;
  }

  /**
   * Returns the left distribution mean.
   * @return float
   */
  public float getLeftMean(){
    return left_mean;
  }

  /**
   * Returns the right distribution mean.
   * @return float
   */
  public float getRightMean(){
    return right_mean;
  }

  /**
   * Returns the left distribution variance.
   * @return float
   */
  public float getLeftVar(){
    return left_var;
  }

  /**
   * Returns the right distribution variance.
   * @return float
   */
  public float getRightVar(){
    return right_var;
  }

  /**
   * Returns the left distribution standard deviation.
   * @return float
   */

  public float getLeftStd(){
    return (float)Math.pow(left_var, 0.5f);
  }

  /**
   * Returns the right distribution standard deviation.
   * @return float
   */
  public float getRightStd(){
    return (float)Math.pow(right_var, 0.5f);
  }


  /**
   * Returns the size of the right ROI.
   * @return int
   */
  public int getRightWindow(){
    return right_window;
  }

  /**
   * Returns the size of the left ROI.
   * @return int
   */
  public int getLeftWindow(){
    return left_window;
  }

  /**
   * Returns left gene probabilities.
   * @return float[]
   */
  public float[] getLeftGeneprobs(){
    return left_geneprobs;
  }

  /**
   * Returns right gene probabilities.
   * @return float[]
   */
  public float[] getRightGeneprobs(){
    return right_geneprobs;
  }
  /**
   * Returns left posteriors.
   * @return float[]
   */
  public float[] getLeftPosteriors(){
    return left_posterior;
  }

  /**
   * Returns right posteriors.
   * @return float[]
   */
  public float[] getRightPosteriors(){
    return right_posterior;
  }

  /**
   * Returns left prior.
   * @return float[]
   */
  public float getLeftPrior(){
    return left_prior;
  }

  /**
   * Returns right prior.
   * @return float[]
   */
  public float getRightPrior(){
    return right_prior;
  }

  //test
  public void printData(){
    System.out.println("Edge: "+position);
/*    System.out.println("POSITION: "+position+ ", ROI BOUNDS: "
                       +roi_left+ " to "+roi_right+", Window sizes: "
                       +left_window+", "+ right_window);

    System.out.println("Distributions:----------------------||---------------------");
    System.out.println(" LEFT: mean = "+left_mean+" var = "+left_var+ "\nRIGHT: mean = "+right_mean+" var = "+right_var);


    System.out.println("PRIORS: left = "+left_prior+ ", right = "+right_prior );

    System.out.println("LEFT Posteriors:");
    for (int i = 0; i < left_posterior.length; i++){
      System.out.println("i = "+i+": (index = "+(i+roi_left)+") "+left_posterior[i]);
    }
    System.out.println("RIGHT Posteriors: ");
    for (int i = 0; i < right_posterior.length; i++){
      System.out.println("i = "+i+": "+right_posterior[i]);
    }

    System.out.println("Left gene probs");
    for (int i = 0; i < left_geneprobs.length; i++){
      System.out.println(left_geneprobs[i]);
    }
    System.out.println("right gene probs");
    for (int i = 0; i < right_geneprobs.length; i++){
      System.out.println(right_geneprobs[i]);
    }
*/
  }
}



