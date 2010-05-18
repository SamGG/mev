package org.tigr.microarray.mev.cluster.algorithm.impl.gsea;

import org.tigr.util.FloatMatrix;

/**
 *	Interface providing accessor methods to components of a Geneset aka individual genes
 *   	
 *  @author Sarita
 *
 */
public interface IGeneSetElement {
	
	/**
	 * Returns gene annotation 
	 * @return
	 */
	public String getGene();
	/**
	 * sets the name of the gene could be any identifier (Gene Symbols, EntrezID etc)
	 * @param gene
	 */
	public void setGene(String gene);
	
	/**
	 * Returns the test statistic computed for a gene using the linear model
	 * @return
	 */
	public float getTestStat();
	/**
	 * Sets the test statistic for a gene 
	 * @param tstat
	 */
	public void setTestStat(float tstat);
	
	
	
	
}
