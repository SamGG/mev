package org.tigr.microarray.mev.cluster.algorithm.impl.gsea;

/**
 * @author Sarita Nair
 * 
 * IGeneDataElement interface provides a means to represent all
 * information pertaining to a gene 
 * 1. Names of all probes mapping to that gene
 * 2. True intensity values of the probes
 * 3. Processed "current" intensity value of the gene, obtained after collapsing all probe intensities
 * 4. Position of the probes on the chip
 * 5. The gene identifier corresponding to this gene
 * 
 */
import java.util.ArrayList;


public interface IGeneDataElement {

	public String getGeneIdentifier();

	public float getCurrentIntensity();

	public ArrayList getTrueIntensity();

	public ArrayList getProbePosition();

	public ArrayList<String> getProbeID();

	//Setters

	public void setGeneIdentifier(String geneID);

	public void setCurrentIntensity(float cy3);

	public void setTrueIntensity(float cy3);

	public void setProbePosition(int pos);

	public void setProbeID(String probeID);

}
