package org.tigr.microarray.mev.cluster.algorithm.impl.gsea;

/**
 * 
 * @author Sarita
 *	This interface represents the components of a geneset
 *  1. Gene
 *  2. test statistic corresponding to that gene	
 *
 *
 */
public interface IGeneSetElement {
		
	public String getGene();
	
	public void setGene(String gene);
	
	public float getTestStat();
	
	public void setTestStat(float tstat);
	
	
}
