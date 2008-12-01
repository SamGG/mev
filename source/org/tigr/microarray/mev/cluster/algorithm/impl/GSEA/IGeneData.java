package org.tigr.microarray.mev.cluster.algorithm.impl.gsea;
/**
 * @author Sarita Nair
 *
 */
public interface IGeneData {

	public String getSlideName();

	public String getCollapseMode();

	public IGeneDataElement getGeneDataElement(int index);

	public void setSlideName(String name);

	public void setGeneDataElement(IGeneDataElement gde, int index);

	//Returns the "current" intensity, which is the SD, max or min of the probes which map to a gene
	public float getCurrentIntensity(int index);

	public void setCurrentIntensity(float intensity, int index);


}
