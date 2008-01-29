package org.tigr.microarray.mev.cgh.CGHAlgorithms.Charm;

/**
* The class is a container for two p-values (sign and mean permutation tests) and can be used
 * for comparison with significance cutoffs.
*
 * <p>Title: PValue</p>
 * <p>Description: The class is a container for two p-values (sign and mean permutation tests) and can be used
 * for comparison with significance cutoffs.</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: Princeton University</p>
 * @author Chad Myers, Xing Chen
 * @author  Raktim Sinha
 */

public class PValue {
  /**
   * Significance test type- both mean and sign test p-values must be deemed significant for a positive result.
   */
  public final static int MEAN_AND_SIGN_TEST = 0;
  /**
   * Significance test type- only mean test p-value must be deemed significant for a positive result.
   */
  public final static int MEAN_TEST = 3;
  /**
   * Significance test type- only sign test p-value must be deemed significant for a positive result.
   */
  public final static int SIGN_TEST = 2;
  /**
   * Significance test type- either mean or sign test p-value must be deemed significant for a positive result.
   */
  public final static int MEAN_OR_SIGN_TEST = 1;


  private double meanPvalue;
  private double signPvalue;

  /**
   * Class constructor.
   * @param pval1 double- mean permutation test p-value
   * @param pval2 double- sign test p-value
   */
  public PValue(double pval1,double pval2) {
    meanPvalue = pval1;
    signPvalue = pval2;
  }

  /**
   * Returns mean permutation test p-value.
   * @return double
   */
  public double getMeanPvalue() {
    return meanPvalue;
  }

  /**
   * Sets mean permutation test p-value cutoff.
   */
  public void setMeanPvalue(double pval) {
    meanPvalue=pval;
  }

  /**
   * Sets sign test p-value cutoff.
   */
  public void setSignPvalue(double pval) {
    signPvalue=pval;
  }


  /**
   * Returns sign test p-value.
   * @return double
   */
  public double getSignPvalue() {
    return signPvalue;
  }

  /**
   * Used for comparing this p-value with specified mean and sign test cutoff.
   * (1: query is more significant than this, 0: equal, -1: query is less significant than this p-value)
   * @param p PValue- p-value cutoff
   * @param type int- test type (options: MEAN_AND_SIGN_TEST, MEAN_OR_SIGN_TEST, MEAN_TEST, SIGN_TEST)
   * @return int- 1: query is more significant than this, 0: equal, -1: query is less significant than this p-value)
   */
  public int compareTo(PValue p,int type) {
    int result= -1;

    if(type == this.MEAN_AND_SIGN_TEST) {
      if(p.getMeanPvalue() < this.meanPvalue && p.getSignPvalue() < this.signPvalue) result =1;
      else if(p.getMeanPvalue() <= this.meanPvalue && p.getSignPvalue() <= this.signPvalue) result=0;
      else result = -1;
    }
    else if(type == this.MEAN_OR_SIGN_TEST) {
      if(p.getMeanPvalue() < this.meanPvalue || p.getSignPvalue() < this.signPvalue) result =1;
      else if(p.getMeanPvalue() == this.meanPvalue && p.getSignPvalue() == this.signPvalue) result=0;
      else result = -1;
    }

    else if(type == this.MEAN_TEST) {
      if(p.getMeanPvalue() < this.meanPvalue) result =1;
      else if(p.getMeanPvalue() == this.meanPvalue) result=0;
      else result = -1;
    }

    else if(type == this.SIGN_TEST) {
      if(p.getSignPvalue() < this.signPvalue) result =1;
      else if(p.getSignPvalue() == this.signPvalue) result=0;
      else result = -1;
    }
    return result;
  }
}
