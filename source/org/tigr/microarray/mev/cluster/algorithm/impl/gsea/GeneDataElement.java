package org.tigr.microarray.mev.cluster.algorithm.impl.gsea;

import java.util.ArrayList;
import java.util.Vector;

/**
 * @author Sarita Nair
 * GeneDataElement
 * 
 * 
 * 
 * 
 *
 */

public class GeneDataElement implements IGeneDataElement {

	private int row;
	private String UID;
	private float intensity;
	private ArrayList trueIntensities;
	private ArrayList<String> probeID;
	private ArrayList probePosition;

	public GeneDataElement(int row, String UID) {
		this.row = row;
		this.UID = UID;
		trueIntensities = new ArrayList();
		probeID = new ArrayList<String>();
		probePosition = new ArrayList();

	}

	public String getGeneIdentifier() {
		return UID;
	}

	public ArrayList getProbeID() {
		return probeID;
	}

	public ArrayList getProbePosition() {
		return probePosition;
	}

	public void setGeneIdentifier(String geneID) {
		UID = geneID;

	}

	public void setProbeID(String probeid) {
		probeID.add(probeid);

	}

	public void setProbePosition(int pos) {
		probePosition.add(pos);

	}

	public float getCurrentIntensity() {
		return intensity;
	}

	public ArrayList getTrueIntensity() {
		return trueIntensities;
	}

	public void setCurrentIntensity(float intensityVal) {
		intensity = intensityVal;
	}

	public void setTrueIntensity(float val) {
		trueIntensities.add(val);

	}

}
