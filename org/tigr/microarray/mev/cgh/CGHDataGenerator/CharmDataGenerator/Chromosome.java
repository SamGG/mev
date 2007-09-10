package org.tigr.microarray.mev.cgh.CGHDataGenerator.CharmDataGenerator;
import java.util.ArrayList;
import java.util.Collections;

import org.tigr.microarray.mev.cgh.CGHDataObj.CGHClone;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.IFramework;

/**
 * This class is a container class for all data associated with a chromosome.  Each chromosome object
 * contains data for all experiments in a dataset.
 *
 * <p>Title: Chromosome</p>
 * <p>Description: This class is a container class for all data associated with a chromosome.  Each chromosome object
 * contains data for all experiments in a dataset.</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: Princeton University</p>
 * @author Chad Myers, Xing Chen
 * @version 1.0
 */
public class Chromosome implements Comparable{

  //IVARS
  private ArrayList genes;
  private int chromosomeNumber;
  //private int centromereLocation;
  public int numEdges;

  private float[] curr_ratios;
  private int[] curr_geneindex;
  private int[] map_geneindex;

  private String curr_exp;
  private boolean curr_includeNaNs;

  private float size;
  private float maxsize;
  private float maxGeneValue;

  IFramework framework;
  IData data;
  
  /**
   * New conatructor Raktim 9/18
   */
  public Chromosome(int chromosomenum, IFramework framework) {
	  this.framework = framework;
	  this.data = framework.getData();
	  
	  chromosomeNumber = chromosomenum;
	  genes = new ArrayList();
	  maxsize = size = 1;
  }
  
  /**
   * Class constructor.
   * @param chromosomenum int
   */
  public Chromosome(int chromosomenum) {
    chromosomeNumber = chromosomenum;
    genes = new ArrayList();
    maxsize = size = 1;
  }

//setters
  /**
   * Adds a new gene to this chromosome.
   * @param newGene Gene
   */
  public void addGene(Gene newGene){
    genes.add(newGene);
  }

  /**
   * Sets the size (in base pairs) of this chromosome.
   * @param input_size double
   */
  public void setSize(float input_size){
    size = input_size;
  }

  /**
   * Sets the maximum number of bp in any chromosome.
   * @param input_max double
   */
  public void setMaxSize(float input_max){
    maxsize = input_max;
  }

//getter methods

  /**
   * Returns the gene at the specified index (in genes) from the end of the chromosome, for the given experiment.
   * The <code>includeNaNs</code> flag determines whether or not NaN gene values are counted in finding this gene.
   * @param index int
   * @param exp String
   * @param includeNaNs boolean
   * @return Gene
   */
  /**
   * Returns all experiment ratios of a single gene 
   */
  public Gene geneAtRatioIndex(int index,String exp,boolean includeNaNs){
    Gene returnGene=null;

    if(exp.equals(this.curr_exp) && ((includeNaNs && this.curr_includeNaNs) || (!includeNaNs && !this.curr_includeNaNs))) {
      returnGene = (Gene)genes.get( curr_geneindex[index]);
    }
    else {
      float[] temp_ratios = new float[genes.size()];
      int[] temp_gene_indicies = new int[genes.size()];
      int[] temp_map_geneindices = new int[genes.size()];

      int ct = 0;
      int total_ct=0;

      for (int i = 0; i < genes.size(); i++) {
    	float curr = ((CGHClone) genes.get(i)).getRatio(/*exp*/);
        if (!Float.isNaN(curr)|| includeNaNs) {
          temp_ratios[ct] = curr;
          temp_gene_indicies[ct] = i;
          temp_map_geneindices[total_ct]=ct;
          ct++;
        }
        total_ct++;
      }

      curr_ratios = new float[ct];
      curr_geneindex = new int[ct];
      map_geneindex = new int[total_ct];
      System.arraycopy(temp_gene_indicies, 0, curr_geneindex, 0, ct);
      System.arraycopy(temp_map_geneindices, 0, map_geneindex, 0, total_ct);

      System.arraycopy(temp_ratios, 0, curr_ratios, 0, ct);
      returnGene = (Gene)genes.get( curr_geneindex[index]);
    }

    curr_exp = exp;
    curr_includeNaNs =includeNaNs;

    return returnGene;
  }

  /**
   * Returns an ArrayList of genes between the given bounds.  The <code>includeNaNs</code>
   * flag determines whether or not NaN gene values are counted in finding these genes.
   * @param exp String
   * @param ind1 int
   * @param ind2 int
   * @param includeNaNs boolean
   * @return ArrayList
   */
  public ArrayList getGenesBetweenIndices(String exp, int ind1, int ind2, boolean includeNaNs) {
    ArrayList geneList = new ArrayList();
    for (int i=ind1; i<= ind2; i++ ){
      geneList.add(this.geneAtRatioIndex(i,exp,includeNaNs));
    }
    return geneList;
  }

  /**
   * Returns an ArrayList of gene names between the given bounds.  The <code>includeNaNs</code>
   * flag determines whether or not NaN gene values are counted in finding these genes.
   * @param exp String
   * @param ind1 int
   * @param ind2 int
   * @param includeNaNs boolean
   * @return ArrayList
   */
  public ArrayList getGeneNamesBetweenIndices(String exp, int ind1, int ind2, boolean includeNaNs) {
    ArrayList geneList = new ArrayList();
    for (int i=ind1; i<= ind2; i++ ){
      geneList.add(this.geneAtRatioIndex(i,exp,includeNaNs).getID());
    }
    return geneList;
  }

  /**
   * Returns the number of genes for a given experiment.  The <code>includeNaNs</code>
   * flag determines whether or not NaN gene values are counted in finding these genes.
   * @param exp String
   * @param includeNaNs boolean
   * @return int
   */
  public int getRatiosLength(String exp,boolean includeNaNs){
    int returnLen;
    if(exp.equals(this.curr_exp) && ((includeNaNs && this.curr_includeNaNs) || (!includeNaNs && !this.curr_includeNaNs))) {
      returnLen = curr_ratios.length;
    }
    else {
      float[] temp  = getRatioArray(exp,includeNaNs);
      returnLen = temp.length;
    }
    return returnLen;
  }

  /**
   * Function for mapping a given gene index that excludes NaN genes to an index
   * that includes all genes for a specific experiment.
   *
   * @param exp String
   * @param index int
   * @return int
   */
  public int mapExcludeNaNIndexToRealIndex(String exp,int index) {
    if(exp.equals(this.curr_exp) && !curr_includeNaNs) {}
    else {
      float[] temp_ratios = new float[genes.size()];
      int[] temp_gene_indicies = new int[genes.size()];
      int[] temp_map_geneindices = new int[genes.size()];

      int ct = 0;
      int total_ct=0;

      for (int i = 0; i < genes.size(); i++) {
    	//float curr = ( (Gene) genes.get(i)).getRatio(exp);
    	  float curr = ((CGHClone) genes.get(i)).getRatio(/*exp*/);
        if (!Double.isNaN(curr)) {
          temp_ratios[ct] = curr;
          temp_gene_indicies[ct] = i;
          temp_map_geneindices[total_ct]=ct;
          ct++;
        }
        total_ct++;
      }

      curr_ratios = new float[ct];
      curr_geneindex = new int[ct];
      map_geneindex = new int[total_ct];

      System.arraycopy(temp_gene_indicies, 0, curr_geneindex, 0, ct);
      System.arraycopy(temp_map_geneindices, 0, map_geneindex, 0, total_ct);

      System.arraycopy(temp_ratios, 0, curr_ratios, 0, ct);
    }

    curr_exp = exp;
    curr_includeNaNs = false;
    return curr_geneindex[index];
  }

  /**
   * Function for mapping a given gene index that includes all genes to an index
   * that excludes all NaN genes for a specific experiment.
   *
   * @param exp String
   * @param index int
   * @return int
   */
  public int mapRealIndexToExcludeNaNIndex(String exp,int index) {
      if(exp.equals(this.curr_exp) && !curr_includeNaNs) {}
      else {
        double[] temp_ratios = new double[genes.size()];
        int[] temp_gene_indicies = new int[genes.size()];
        int[] temp_map_geneindices = new int[genes.size()];

        int ct = 0;
        int total_ct=0;

        for (int i = 0; i < genes.size(); i++) {
          //double curr = ( (Gene) genes.get(i)).getRatio(exp);
        	float curr = ((CGHClone) genes.get(i)).getRatio(/*exp*/);
          if (!Double.isNaN(curr)) {
            temp_ratios[ct] = curr;
            temp_gene_indicies[ct] = i;
            temp_map_geneindices[total_ct]=ct;
            ct++;
          }
          total_ct++;
        }

        curr_ratios = new float[ct];
        curr_geneindex = new int[ct];
        map_geneindex = new int[total_ct];

        System.arraycopy(temp_gene_indicies, 0, curr_geneindex, 0, ct);
        System.arraycopy(temp_map_geneindices, 0, map_geneindex, 0, total_ct);

        System.arraycopy(temp_ratios, 0, curr_ratios, 0, ct);
      }

      curr_exp = exp;
      curr_includeNaNs = false;
      return map_geneindex[index];
    }


    /**
     * Returns an array of gene values for a given experiment.  The <code>includeNaNs</code>
     * flag determines whether or not NaN gene values are included in these values.
     * @param exp String
     * @param includeNaNs boolean
     * @return double[]
     */
    public float[] getRatioArray(String exp, boolean includeNaNs){
    	if(exp.equals(this.curr_exp) && ((includeNaNs && this.curr_includeNaNs) || (!includeNaNs && !this.curr_includeNaNs))) {
    	}
    	else {
	      float[] temp_ratios = new float[genes.size()];
	      int[] temp_gene_indicies = new int[genes.size()];
	      int[] temp_map_geneindices = new int[genes.size()];
	
	      int ct = 0;
	      int total_ct=0;
	
	      for (int i = 0; i < genes.size(); i++) {
	        //float curr = ( (Gene) genes.get(i)).getRatio(exp);
	    	  float curr = ((CGHClone) genes.get(i)).getRatio(/*exp*/);
	        if (!Float.isNaN(curr)|| includeNaNs) {
	          temp_ratios[ct] = curr;
	          temp_gene_indicies[ct] = i;
	          temp_map_geneindices[total_ct]=ct;
	          ct++;
	        }
	        total_ct++;
	      }
	
	      curr_ratios = new float[ct];
	      curr_geneindex = new int[ct];
	      map_geneindex = new int[total_ct];
	
	      System.arraycopy(temp_gene_indicies, 0, curr_geneindex, 0, ct);
	      System.arraycopy(temp_map_geneindices, 0, map_geneindex, 0, total_ct);
	
	      System.arraycopy(temp_ratios, 0, curr_ratios, 0, ct);
    	}

    	curr_exp = exp;
    	curr_includeNaNs =includeNaNs;

    	return curr_ratios;
  }

    /**
     * Returns an array of gene values for a given experiment.  The <code>includeNaNs</code>
     * flag determines whether or not NaN gene values are included in these values.
     * @param exp String
     * @param includeNaNs boolean
     * @return double[]
     */
/*
    public float[] getRatioArray(String exp, boolean includeNaNs){
    	if(exp.equals(this.curr_exp) && ((includeNaNs && this.curr_includeNaNs) || (!includeNaNs && !this.curr_includeNaNs))) {
    	}
    	else {
	      float[] temp_ratios = new float[genes.size()];
	      int[] temp_gene_indicies = new int[genes.size()];
	      int[] temp_map_geneindices = new int[genes.size()];
	
	      int ct = 0;
	      int total_ct=0;
	
	      for (int i = 0; i < genes.size(); i++) {
	    	  float curr = ( (Gene) genes.get(i)).getRatio(exp);
	        if (!Double.isNaN(curr)|| includeNaNs) {
	          temp_ratios[ct] = curr;
	          temp_gene_indicies[ct] = i;
	          temp_map_geneindices[total_ct]=ct;
	          ct++;
	        }
	        total_ct++;
	      }
	
	      curr_ratios = new float[ct];
	      curr_geneindex = new int[ct];
	      map_geneindex = new int[total_ct];
	
	      System.arraycopy(temp_gene_indicies, 0, curr_geneindex, 0, ct);
	      System.arraycopy(temp_map_geneindices, 0, map_geneindex, 0, total_ct);
	
	      System.arraycopy(temp_ratios, 0, curr_ratios, 0, ct);
    	}

    	curr_exp = exp;
    	curr_includeNaNs =includeNaNs;

    	return curr_ratios;
  }
*/
  /**
   * Returns the number of genes on this chromosome.
   * @return int
   */
  public int numGenes(){
    return genes.size();
  }

  /**
   * Returns the number associated with this chromosome.
   * @return int
   */
  public int getNumber(){
    return chromosomeNumber;
  }

  /**
   * Returns index of specified gene including NaN values (same for all exps).
   * @param g Gene
   * @return int
   */
  public int getGeneIndex(Gene g) {
    return genes.indexOf(g);
  }

  /**
   * Sorts all contained genes by base pair coordinates.
   */
  public void sortGenes(){
    Collections.sort(genes);
  }

  /**
   * Returns size of this chromosome (in bp).
   * @return double
   */
  public float getSize(){
    return size;
  }

  /**
   * Returns max size of any chromosome (in bp)
   * @return double
   */
  public double maxSize(){
    return maxsize;
  }

  /**
   * Returns max. gene value of all genes on this chromosome.
   * @return double
   */
  public float getMaxGeneValue() {
   return maxGeneValue;
 }

 /**
  * Sets max. gene value of all genes on this chromosome.
  * @param max double
  */
 public void setMaxGeneValue(float max) {
   maxGeneValue = max;
 }

 /**
  * Used in comparing chromosome based on number.
  * @param other Object
  * @return int
  */
 public int compareTo(Object other) {
    Chromosome otherchromo = (Chromosome) other;
    if (chromosomeNumber < otherchromo.getNumber())
      return -1;
    else if (chromosomeNumber > otherchromo.getNumber())
      return 1;
    else
      return 0;

  }

}
