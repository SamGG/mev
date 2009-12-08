package org.tigr.microarray.mev.cluster.algorithm.impl.gsea;
/**
 * @author Sarita Nair
 * IGeneData interface provides a way of representing all information
 * pertaining to a "slide" (microarray) or one sample
 * 
 * A class implementing this interface would provide
 * 1. Means of representing individual gene information via get/set GeneDataElement
 * 2. Name of the sample, captured from existing MeV data structures
 * 3. Getting and setting processed "current" intensity values of genes.
 * 
 *
 */
public interface IGeneData {

	public String getSlideName();
	                                                    
	public IGeneDataElement getGeneDataElement(int index);
	
	public IGeneDataElement getGeneDataElement(String gene);

	public void setSlideName(String name);

	public void setGeneDataElement(IGeneDataElement gde, int index);
	
	public float getCurrentIntensity(int index);

	public void setCurrentIntensity(float intensity, int index);


}
