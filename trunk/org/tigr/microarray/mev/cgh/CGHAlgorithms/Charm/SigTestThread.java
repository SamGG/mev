package org.tigr.microarray.mev.cgh.CGHAlgorithms.Charm;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

import org.tigr.microarray.mev.cgh.CGHDataModel.CharmDataModel.SegmentInfo;
//import org.tigr.microarray.mev.cgh.CGHDataModel.CharmDataModel.SegmentInfo;
/**
* This class implements the core functionality of ChARM
 * significance testing.
*
 * <p>Title: SigTestThread</p>
 * <p>Description: This class implements the core functionality of ChARM
 * significance testing.</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: Princeton University</p>
 * @author Chad Myers, Xing Chen
 * @version 1.0
 */

public class SigTestThread /*extends Thread*/ {

  /**
   * The number of mean sig. test permutations to perform before using Gaussian approximation.
   */
  private int NUM_PERMUTATIONS=100;


 /**
  * The number of mean sig. test permutations to perform before using Gaussian approximation.
  */
  private int MAX_PERMUTE_VAL=1500;


  /**
   * Deprecated.
   */
  private int PRECISION_THRESH = 10;

  private int curr_permutation;

  private boolean use_median;
  private boolean use_coeff;
  private boolean use_rankvar;
  private boolean use_rankmean;

  private float[] data;

  //private Chromosome chromosome; //Chnaged to CHr Number
  private int ChrNum;
  private String experiment;
  private SegmentInfo segInfo;
  private float orig_mean;

  private boolean stop;

  private boolean threadState_running;
  //private boolean threadState_stopflag;
  private boolean threadState_hasRun;


  /**
   * New Class constructor. Uses the float for genes ratio of onr Chr of any one expr
   * Raktim 9/21
   * @param chromo Chromosome
   * @param exp String
   * @param currSeg SegmentInfo- single segInfo to be tested
   */
  public SigTestThread(float[] geneRatiosOfExprChr, int Chr, String exp, SegmentInfo currSeg) {
    data = geneRatiosOfExprChr;
    //chromosome = chromo;
    ChrNum = Chr;
    experiment = exp;
    segInfo = currSeg;
    orig_mean = 0;
    use_median = use_coeff = use_rankmean = use_rankvar = false;
    stop = false;
    curr_permutation = 0;
    threadState_running = false;
    //threadState_stopflag = false;
    threadState_hasRun = false;
    
    /* Modified Raktim */
    //runPermuteTest();
    //runSignTest();
  }
  
  /**
   * Class constructor.
   * @param chromo Chromosome
   * @param exp String
   * @param currSeg SegmentInfo- single segInfo to be tested
   * Not to be used
   */
  /*
  public SigTestThread(Chromosome chromo, String exp, SegmentInfo currSeg) {
    data = chromo.getRatioArray(exp,false);
    //chromosome = chromo;
    ChrNum = chromo.getNumber();
    experiment = exp;
    segInfo = currSeg;
    orig_mean = 0;
    use_median = use_coeff = use_rankmean = use_rankvar = false;
    stop = false;
    curr_permutation = 0;
    threadState_running = false;
    threadState_stopflag = false;
    threadState_hasRun = false;
  }
  */
  /**
   * Class constructor.
   * @param indata double[]
   * @param chromo Chromosome
   * @param exp String
   * @param currwindow SegmentInfo- single segInfo to be tested
   * Not to be used
   */
  /*
  public SigTestThread(float[] indata, Chromosome chromo, String exp, SegmentInfo currSeg) {
    data = indata;
    //chromosome = chromo;
    ChrNum = chromo.getNumber();
    experiment = exp;
    segInfo = currSeg;
    orig_mean = 0;
    use_median = use_coeff = use_rankmean = use_rankvar = false;
    stop = false;
    curr_permutation = 0;
    threadState_running = false;
    threadState_stopflag = false;
    threadState_hasRun = false;
  }
  */
  /**
   * Stop this thread.
   */
  /*
  public void stopTests(){
    stop = true;
    threadState_stopflag = true;
  }
  */
  /**
   * Return running status.
   * @return boolean
   */
  public boolean isRunning(){
    return threadState_running;
  }

  /**
   * Indicates if this thread has finished.
   * @return boolean
   */
  public boolean hasRun(){
    return threadState_hasRun;
  }

  /**
   * Returns description of the segInfo this thread is testing.
   * @return String
   */
  public String getDescription() {
    //return new String(experiment+", Chrom: "+chromosome.getNumber());
	  return new String(experiment+", Chrom: "+ChrNum);
  }

  /**
   * Deprecated.
   * @param sign boolean
   * @param rank_mean boolean
   * @param rank_var boolean
   * @param coeff boolean
   */
  public void setTests(boolean sign, boolean rank_mean,  boolean rank_var, boolean coeff){
    use_coeff = coeff;
    use_rankmean = rank_mean;
    use_rankvar = rank_var;
  }

  /**
   * Set the number of permutations to be performed.
   * @param number int
   */
  public void setNumberPermutations(int number){
    NUM_PERMUTATIONS = number;
  }
  
  /*
   * TODO
   * Replace Chromosome wiht int Chr
   */
  /*
  public void resetParameters(Chromosome chromo, String exp, SegmentInfo pvals) {
	ChrNum = chromo.getNumber();
    experiment = exp;
    segInfo = pvals;
    data = chromo.getRatioArray(exp,false);
  }
  */
  /**
   * Deprecated.
   * @param rankarray double[]
   * @return double[]
   */
  private double[] getRanks(double[] rankarray) {

    double[] sortdata = new double[data.length];
    System.arraycopy(data, 0, sortdata, 0, data.length);
    Arrays.sort(sortdata);

    int neg = 0;
    while ((neg < sortdata.length) && (sortdata[neg] < 0)){
      neg++;
    }

    int ct = 0;
    if (neg > 0){
      for (int i = neg; i > 0; i--) {
        rankarray[ct] = -1*i;
        ct++;
      }
    }

    for (int i = 1; ct < rankarray.length; i++){
      rankarray[ct] = i;
      ct++;
    }

    double[] orig_ranks = new double[(int)(segInfo.getEnd() - segInfo.getStart())];
    int count = 0;

    for (int i = (int)segInfo.getStart(); i < segInfo.getEnd(); i++) {
      int index = Arrays.binarySearch(sortdata, (data[i]));
      orig_ranks[count] = rankarray[index];
      count++;
    }

    return orig_ranks;
  }
/*
 * UNused .
 */
  /*
  public void run() {
	  System.out.println("In SigTestThread run()");
    runPermuteTest();
    runSignTest();
    threadState_hasRun = true;
    threadState_running= false;

    ///test print
    //   System.out.println("WINDOW:  [" + segInfo.getStart() + " , " + segInfo.getEnd() + "]");
      // System.out.println("MEAN PVAL: " + segInfo.mean_pvalue);
       //System.out.println("MEDIAN PVAL: " + pvalues.median_pvalue);
       //System.out.println("COEFF PVAL: " + segInfo.coeff_pvalue);
       //System.out.println("RANK PVAL: " + segInfo.rank_pvalue);
       //System.out.println("SIGN OVER PVAL: " + segInfo.sign_over_pvalue);
       //System.out.println("SIGN UNDER PVAL: " + segInfo.sign_under_pvalue);
       //test
       //System.out.println("REAL RANK VAR = "+realrankvar);

   //printResults();

  }
*/
  /**
   * Actually runs permutation test- for threaded run, use start().
   */
  public void runPermuteTest() {
	  //System.out.println("In SigTestThread runPermuteTest()");
    float realmean = Statistics.getMean(data, (int)segInfo.getStart(), (int)segInfo.getEnd());
    orig_mean = realmean;
    int windowsize = (int)(segInfo.getEnd() - segInfo.getStart()+1);

    float[] random_data = new float[data.length];
    System.arraycopy(data, 0, random_data, 0, data.length);

    float[] random_maxmeans = new float[this.NUM_PERMUTATIONS];
    boolean precision_reached = false;
    segInfo.setNumPermutations(0);


    int mean_beats = 0;

    permuteloop:
    for (curr_permutation = 0; ( curr_permutation < NUM_PERMUTATIONS && !precision_reached); curr_permutation++) {

      if (stop){
        break permuteloop;
      }

      //shuffle the array of randomly ordered genes, and ranks if needed
      for (int j = random_data.length - 1; j > 0; j--) {
        int random_index = (int) (Math.random() * j);
        float temp = random_data[random_index];
        random_data[random_index] = random_data[j];
        random_data[j] = temp;
      }

      int curr_start = 0;
      float curr_mean = Statistics.getMean(random_data, curr_start,(curr_start + windowsize));
      float rand_max=0;

      while ( (curr_start < Math.min(data.length - windowsize,this.MAX_PERMUTE_VAL))) {

        curr_mean -= random_data[curr_start]/windowsize;
        curr_mean += random_data[curr_start+windowsize]/windowsize;
        if(curr_mean < 0 && curr_mean < rand_max) rand_max = curr_mean;
        else if(curr_mean > 0 && curr_mean > rand_max) rand_max = curr_mean;

        curr_start++;
      }

      random_maxmeans[curr_permutation] = rand_max;

      if (  ( (realmean > 0) && (rand_max > realmean)) ||  ((realmean < 0) && (rand_max < realmean)))
        mean_beats++;

      if(mean_beats > PRECISION_THRESH) precision_reached = true;
    }

    segInfo.setNumPermutations(curr_permutation);
    segInfo.setDataMean(orig_mean);

    float maxMean = Statistics.getMean(random_maxmeans,0,curr_permutation-1);
    float maxVar = Statistics.getVariance(random_maxmeans,0,curr_permutation-1,maxMean);

    float pvalue=1;
    if(realmean > 0)
      pvalue = 1 - Statistics.getNormalCDF((realmean-maxMean)/(float)Math.pow(maxVar,.5));
    else pvalue = Statistics.getNormalCDF((realmean-maxMean)/(float)Math.pow(maxVar,.5));

    segInfo.setStatistic(new String("Mean"),pvalue);
  }


  /**
   * Deprecated.
   */
  public void runPermuteTestOld() {

    int mean_beats = 0;

    int windowsize = (int)(segInfo.getEnd() - segInfo.getStart()+1);

    //gets statistics of the actual segInfo
    float realmean = Statistics.getMean(data, (int)segInfo.getStart(), (int)segInfo.getEnd());
    orig_mean = realmean;

    //copies the data into an array that will be randomized
    float[] random_data = new float[data.length];

    System.arraycopy(data, 0, random_data, 0, data.length);

    boolean precision_reached = false;
    segInfo.setNumPermutations(0);

    permuteloop:
    for (curr_permutation = 0; ( curr_permutation < NUM_PERMUTATIONS && !precision_reached); curr_permutation++) {

      if (stop){
        break permuteloop;
      }

      //shuffle the array of randomly ordered genes, and ranks if needed
      for (int j = random_data.length - 1; j > 0; j--) {
        int random_index = (int) (Math.random() * j);
        float temp = random_data[random_index];
        random_data[random_index] = random_data[j];
        random_data[j] = temp;

      }


      boolean meanbeat = false;
      int curr_start = 0;

      while ( (curr_start < data.length - windowsize) && !meanbeat) {

        //sets random statistics ---------------------------
        double random_mean = Statistics.getMean(random_data, curr_start,
                                                (curr_start + windowsize));


        //checks to see if statistics are beat by random--------
        if ( (!meanbeat) && ( ( (realmean > 0) && (random_mean > realmean)) ||
                             ( (realmean < 0) && (random_mean < realmean)))) {
          mean_beats++;
          meanbeat = true;
        }

        curr_start++;
      }

      if(mean_beats > PRECISION_THRESH) {precision_reached = true; curr_permutation = NUM_PERMUTATIONS;}

      segInfo.setNumPermutations(segInfo.getNumPermutations()+1);
    }

    int num_permutations_performed = segInfo.getNumPermutations();



    segInfo.setStatistic(new String("Mean"),(float) mean_beats / (float) num_permutations_performed);

    segInfo.setDataMean(orig_mean);
  }

  /**
   * Performs sign significance test.
   */
  public void runSignTest() {
	  //System.out.println("In SigTestThread runSignTest()");
	float thresh = getThreshhold(data);
    int totalbeats = 0;
    int overbeats = 0;
    int underbeats = 0;

    float pval_over = 1;
    float pval_under = 1;

    //System.out.println("thresh: " + thresh + ", data.length: " + data.length + 
    		//", segInfo.getStart():" + segInfo.getStart() + ", segInfo.getEnd(): " + segInfo.getEnd());
    for (int i = (int)segInfo.getStart(); i <= (int)segInfo.getEnd(); i++) {

      if (data[i] > thresh) {
        totalbeats++;
        overbeats++;
      }
      if (data[i] < -1 * thresh) {
        totalbeats++;
        underbeats++;
      }
    }

    if (totalbeats > 15) {
      pval_over = 1 - Statistics.getNormalCDF( (2 * overbeats - totalbeats) / (float)Math.pow(totalbeats, .5));
      pval_under = 1 - Statistics.getNormalCDF( (2 * underbeats - totalbeats) / (float)Math.pow(totalbeats, .5));
    }
    else if (totalbeats > 0) {
      pval_over = (float)(1 - Statistics.getBinomialCDF(overbeats, totalbeats));
      pval_under = (float)(1 - Statistics.getBinomialCDF(underbeats, totalbeats));
    }
    else {
      pval_over = pval_under = 1;
    }

    segInfo.setStatistic(new String("Sign pos"), pval_over);
    segInfo.setStatistic(new String("Sign neg"), pval_under);

//System.out.println("SegmentInfo ["+start+" , "+end+"]: sign p-values: over = "+pval_over+", under = "+pval_under);

//put pvalues into chromosomes for storage here?
  }


  /**
   * Determines threshold to be used in sign test.
   * @param data double[]
   * @return double
   */
  public double getThreshhold_old(double[] data) {

    double[] sortdata = new double[data.length];

    for (int i = 0; i < data.length; i++) {
      sortdata[i] = Math.abs(data[i]);
    }
    Arrays.sort(sortdata);

    double init_percent = .20;
    int currsize = (int) (init_percent * data.length);
    int sizediff = 10;
    double curr_mean = 0;

    while ( (sizediff > 1) && ( (double) currsize / (double) data.length < 0.95)) {

      double curr_stdev = Math.pow(Statistics.getVariance(data, 0, currsize),
                                   0.5);
      curr_mean = Statistics.getMean(sortdata, 0, currsize);
      int newsize = currsize;

      for (newsize = currsize; ((newsize < (int)(sortdata.length*0.95)) &&
           (sortdata[newsize] <= (curr_mean + 2 * curr_stdev) )) ; newsize++) {}

      sizediff = newsize - currsize;
      currsize = newsize;
    }

    return curr_mean;
  }

  /**
   * Determines threshold to be used in sign test.
   * @param float double[]
   * @return float
   */

  public float getThreshhold(float[] data) {

	float[] sortdata = new float[data.length];

    for (int i = 0; i < data.length; i++) {
      sortdata[i] = Math.abs(data[i]);
    }
    Arrays.sort(sortdata);

    float init_percent = .75f;
    int currsize = (int) (init_percent * data.length);

    return sortdata[currsize];
  }


  /**
   * Deprecated.
   */
  public void printResults() {

    try {
      BufferedWriter out = new BufferedWriter(new FileWriter(experiment, true));
      /*
             out.write("WINDOW:  [" + start + " , " + end + "]");
             out.newLine();
             out.write("Start gene: " + chromosome.geneAt(start).getName() );
             out.newLine();
             out.write("End gene: "+ chromosome.geneAt(end).getName());
             out.newLine();
             out.write(" Mean p-value = "+mean_pval);
             out.newLine();
             out.write(" Median p-value = " + median_pval);
             out.newLine();
             out.write(" Coefficient p-value = " + coeff_pval);

      out.write(chromosome.getNumber() + "\t" + segInfo.getStart() + "\t" + segInfo.getEnd() + "\t" +
                chromosome.geneAtRatioIndex((int)segInfo.getStart()).getYorf() + "\t" +
                chromosome.geneAtRatioIndex((int)segInfo.getEnd()).getYorf() + "\t" +
                segInfo.getStatistic(new String("Mean")) + "\t" + orig_mean);
      */out.newLine();
      out.close();
    }
    catch (IOException e) {
      e.printStackTrace();
    }

  }

  /**
   * Returns the current permutation progress.
   * @return int
   */
  public int getCurrentPermutation(){
    return curr_permutation;
  }

}
