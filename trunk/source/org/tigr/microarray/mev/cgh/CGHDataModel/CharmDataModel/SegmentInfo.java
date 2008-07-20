package org.tigr.microarray.mev.cgh.CGHDataModel.CharmDataModel;
/**
 * @author  Raktim Sinha
 */
import java.util.HashMap;

/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/

public class SegmentInfo implements Comparable {

  public static final String[] PVAL_STATISTICS = {new String("Mean"), new String("Sign pos"), new String("Sign neg")};
  /**
   * Specifies that a window was found by hand.
   */
  public static final int TYPE_MANUAL=0;

  /**
   * Specifies that a window was found with automated algorithm.
   */
  public static final int TYPE_AUTO=1;

  private HashMap statisticsHash = null;

  private int start;//start and end indicies: NOT BP!
  private int end;
  private float dataMean;
  private int numPermutations;
  private int type;
  private int bonferroniCorrection;

  /**
   * Class constructor.
   */
  public SegmentInfo() {
    statisticsHash = new HashMap();
    start = end = 0;
    dataMean = 0;
    numPermutations=0;
    type = SegmentInfo.TYPE_AUTO;
    bonferroniCorrection=1;
  }

  /**
   * Compares the start index with supplied window.
   * @param other Object
   * @return int
   */
  public int compareTo(Object other){
    SegmentInfo otherWindow = (SegmentInfo)other;

    if (start < otherWindow.getStart()){
      return -1;
    }
    else if (start > otherWindow.getStart()){
      return 1;
    }
    else {
      return 0;
    }
  }

  /**
   * Sets the start index of this window.
   * @param start int
   */
  public void setStart(int start) {
    this.start = start;
  }

  /**
   * Sets the end index of this window.
   * @param end int
   */
  public void setEnd(int end) {
    this.end = end;
  }

  /**
   * Sets the number of permutations performed in computing p-values for
   * this window.
   * @param num int
   */
  public void setNumPermutations(int num) {
    this.numPermutations = num;
  }

  /**
   * Sets the mean of the data enclosed by this window.
   * @param d double
   */
  public void setDataMean(float d) {
    this.dataMean = d;
  }

  /**
   * Returns the mean of the data enclosed by this window.
   * @return double
   */
  public float getDataMean() {
    return dataMean;
  }

  /**
   * Returns the start index of this window.
   * @return int
   */
  public int getStart() {
    return start;
  }

  /**
   * Returns the end index of this window.
   * @return int
   */
  public int getEnd() {
    return end;
  }

  /**
   * Returns the length of this window (prediction).
   * @return int
   */
  public int getSize() {
    return (end-start+1);
  }

  /**
   * Returns the number of permutations used to compute p-values for this window.
   * @return int
   */
  public int getNumPermutations() {
    return numPermutations;
  }

  /**
   * Sets the specified statistic to the supplied value.
   * @param statName String (options: Mean, Sign pos, Sign neg)
   * @param statValue double
   */
  public void setStatistic(String statName, float statValue) {
    statisticsHash.put(statName,new Float(statValue));
  }

  /**
   * Returns the value of the queried statistic.
   * @param statName String (options: Mean, Sign pos, Sign neg)
   * @return double
   */
  public float getStatistic(String statName) {
    float retValue=Float.NaN;
    if(statisticsHash.containsKey(statName)) {
      retValue = ((Float)statisticsHash.get(statName)).floatValue();
    }
    return Math.min(retValue*this.getBonferroniCorrection(),1.0f);
  }

  /**
   * Resets all values of this window.
   */
  public void resetSegment() {
    statisticsHash = new HashMap();
    dataMean = 0;
    numPermutations=0;
  }

  /**
   * For State Saving
   * @param statsHash
   */
  public void setStatisticsHash(HashMap statsHash){
	  statisticsHash = statsHash;
  }
  
  public HashMap getStatisticsHash(){
	  return statisticsHash;
  }
  /**
   * Sets window type.
   * @param type int (options TYPE_MANUAL, TYPE_AUTO)
   */
  public void setType(int type) {
    this.type = type;
  }

  /**
   * Returns window type.
   * @return int
   */
  public int getType() {
    return type;
  }

  /**
   * Returns number of hypotheses tested with this window (useful in Bonferroni correction,etc.)
   * @return int
   */
  public int getBonferroniCorrection() {
    return this.bonferroniCorrection;
  }

  /**
   * Sets the number of hypotheses tested with this window.
   * @param num int
   */
  public void setBonferroniCorrection(int num) {
    this.bonferroniCorrection = num;
  }
}


