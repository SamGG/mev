package org.tigr.microarray.mev.cluster.algorithm.impl.gsea;

/**
 * @author Sarita Nair
 * 
 * 
 * 
 */
import java.util.Vector;

public interface IGeneDataElement {

	public String getGeneIdentifier();

	public float getCurrentIntensity();

	public Vector getTrueIntensity();

	public Vector getProbePosition();

	public Vector getProbeID();

	//Setters

	public void setGeneIdentifier(String geneID);

	public void setCurrentIntensity(float cy3);

	public void setTrueIntensity(float cy3);

	public void setProbePosition(int pos);

	public void setProbeID(String probeID);

}
