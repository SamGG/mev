package org.tigr.microarray.mev.cluster.algorithm.impl.GSEA;

import java.util.Vector;

public class GeneDataElement implements IGeneDataElement {

	protected int row;
	protected String UID;
	//Change the data type to double?--Matrix class requires double, i guess.
	protected float intensity;
	protected Vector trueIntensities;
	protected Vector probeID;
	protected Vector probePosition;
	
	
	public GeneDataElement(int row, String UID){
		this.row=row;
		this.UID=UID;
		this.trueIntensities=new Vector();
		this.probeID=new Vector();
		this.probePosition=new Vector();
		
	}
	

	
	public String getGeneIdentifier() {
		return this.UID;
	}

	
	public Vector getProbeID() {
		return this.probeID;
	}

	
	public Vector getProbePosition() {
		return this.probePosition;
	}

	
	
	public void setGeneIdentifier(String geneID) {
		this.UID=geneID;
		
	}

	
	public void setProbeID(String probeID) {
	this.probeID.add(probeID);	
		
	}

	
	public void setProbePosition(int pos) {
		this.probePosition.add(pos);
		
		
	}

	
	public float getCurrentIntensity() {
		return this.intensity;
	}


	
	public Vector getTrueIntensity() {
		return this.trueIntensities;
	}


	
	public void setCurrentIntensity(float intensity) {
		this.intensity=intensity;
	}



	public void setTrueIntensity(float val) {
		this.trueIntensities.add(val);
		
	}

}
