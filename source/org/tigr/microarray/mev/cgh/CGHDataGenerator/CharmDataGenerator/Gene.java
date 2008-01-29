package org.tigr.microarray.mev.cgh.CGHDataGenerator.CharmDataGenerator;

import java.util.ArrayList;
import java.util.HashMap;


/**
* This class is a container class for all data associated with a gene.  Each gene object
 * contains data for all experiments in a dataset.
*
 * <p>Title: Gene</p>
 * <p>Description: This class is a container class for all data associated with a gene.  Each gene object
 * contains data for all experiments in a dataset. </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: Princeton University</p>
 * @author Chad Myers, Xing Chen
 * @version 1.0
 */
public class Gene implements Comparable{
  // Instance Fields
  private String id;//either yorf or id field for humans
  private String name;
  private String function;
  private float startbp;
  private float endbp;
  private int chromosomeNumber;
  private int chromosomeArm;
  private String strand;//either C or W
  private int index;
  private int currIndex;
  private HashMap otherData;
  private float gWeight;



  //maps experiment names to double objects of ratios
  //To be removed
  //private HashMap ratioMap;

  public boolean isSelected;

  /**
   * Class constructor.
   */
  public Gene() {
    name = "";
    id = "";
    function = "";
    //ratioMap = new HashMap();
    otherData = new HashMap();
    startbp = -1;
    endbp =  -1;
    chromosomeNumber =  -1;
    chromosomeArm = -1;
    strand = "";
    isSelected = false;
  }

  //setter methods follow--------------------------------
  /**
   * Adds an experiment ratio for this gene.
   * @param experiment String
   * @param ratio double
   * Needs to be removed.
   */
  public void addRatio_Dep(String experiment,double ratio){
    //ratioMap.put(experiment.trim(), new Double(ratio));
  }

  /**
   * Sets the starting base pair of this gene.
   * @param inputstart float
   */
  public void setStart(float inputstart){
    startbp = inputstart;
  }

  /**
   * Sets the ending base pair of this gene.
   * @param inputend float
   */
  public void setEnd(float inputend){
    endbp = inputend;
  }

  /**
   * Sets the name (common name) of this gene.
   * @param inputname String
   */
  public void setName(String inputname){
    name = inputname;
  }

  //set yorf and id go to the same variable*
  /**
   * Sets the ORF name (called ID here) for this gene.  setID has the same effect.
   * @param input_yorf String
   */
  public void setYorf(String input_yorf){
    id = input_yorf;
  }

  /**
   * Sets the ID associated with this gene.
   * @param inputID String
   */
  public void setID(String inputID){
    id = inputID;
  }

  /**
   * Sets the functional annotation associated with this gene.
   * @param inputfunction String
   */
  public void setfunction(String inputfunction){
    function = inputfunction;
  }

  /**
   * Sets the chromosome number associated with this gene.
   * @param number int
   */
  public void setChromosome(int number){
    chromosomeNumber = number;
  }

  /**
   * Sets the strand on which this gene is encoded.
   * @param input_strand String
   */
  public void setStrand(String input_strand){
    strand = input_strand;
  }

  /**
   * Sets the GWEIGHT value for this gene.
   * @param gweight float
   */
  public void setGWeight(float gweight) {
    this.gWeight = gweight;
  }

  /**
   * Adds additional annotation fields for this gene.
   * @param header String
   * @param value String
   */
  public void addExtraData(String header, String value){
    otherData.put(header, value);
  }

  /**
   * Used for accessing additional annotation data for this gene.
   * @param key String
   * @return String
   * Needs to be removed
   */
  public String getData_Dep(String key) {
    if(otherData.containsKey(key))  return ((String)otherData.get(key));
    else return new String("");
  }

  /**
   * Adds addition annotation data for the specified field.
   * @param key String- annotation label
   * @param value String- annotation value
   */
  public void setData(String key, String value) {
    otherData.put(key,value);
  }

  /**
   * Gets list of annotation fields.
   * @return ArrayList
   */
  public ArrayList getDataFields() {
    ArrayList keys = new ArrayList(otherData.keySet());
    return keys;
  }


  //getter methods-------------------------------------------

  /**
   * Returns gene's value (log-ratio) for the specified experiment.
   * @param experiment String
   * @return double
   * Needs to be removed
   */
  /*
  public float getRatio_Dep(String experiment){

    if (ratioMap.containsKey(experiment.trim())){
      return (float)((Double) ratioMap.get(experiment.trim())).doubleValue();
    }
    else {
      return (Float.NaN);
    }
  }
*/
  /**
   * Returns array of gene values for all experiments.
   * @return double[]
   * Needs to be replaced
   */
  /*
  public float[] getRatios_Dep() {
   Double[] vals = (Double[])ratioMap.values().toArray(new Double[0]);
   float[] newVals = new float[vals.length];

   for (int i=0; i < vals.length; i++) {
     newVals[i] = (float)vals[i].doubleValue();
   }
   return newVals;
 }
*/
 /**
  * Returns the starting base pair of this gene.
  * @return double
  */
 public double getStart(){
    return startbp;
  }

  /**
   * Returns ending base pair of this gene.
   * @return double
   */
  public double getEnd(){
    return endbp;
  }

  /**
   * Returns ID of this gene.
   * @return String
   */
  public String getID(){
    return id;
  }

  /**
   * Returns chromosome number of the chromosome on which this gene is encoded.
   * @return int
   */
  public int getChromoNumber(){
    return chromosomeNumber;
  }


  /**
   *  Allows comparisons between genes based on starting base pair.
   */
  public int compareTo(Object other){
    Gene othergene = (Gene)other;

    if (startbp < othergene.getStart()){
      return -1;
    }
    else if (startbp > othergene.getStart()){
      return 1;
    }
    else {
      return 0;
    }
  }
}
