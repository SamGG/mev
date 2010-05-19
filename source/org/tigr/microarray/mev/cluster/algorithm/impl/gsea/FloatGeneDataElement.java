package org.tigr.microarray.mev.cluster.algorithm.impl.gsea;

import java.util.ArrayList;
/**
 * FloatGeneDataElement is another implementation of IGeneDataElement. The purpose of this implementation is to
 * avoid duplicating information like 
 * 1) Names of probes mapping to a gene
 * 2) Position of these probes
 * Rest of the implementation is same as GeneDataElement
 * 
 * @author Sarita Nair
 *
 */

public class FloatGeneDataElement implements IGeneDataElement{
	
	
	private float currentIntensity;
	private ArrayList trueIntensity;
	private String UID;
	private int row;
	
	public FloatGeneDataElement(int rowNumber, String uniqueID){
		row=rowNumber;
		UID=uniqueID;
		trueIntensity=new ArrayList();
	}
	
	public float getCurrentIntensity() {
		return currentIntensity;
	}

		
	
	public ArrayList getTrueIntensity() {
			return trueIntensity;
	}
	

	public void setTrueIntensity(float cy3) {
		trueIntensity.add(cy3);
		
	}

	
	public void setCurrentIntensity(float cy3) {
		currentIntensity=cy3;
		
	}

	
	public void setGeneIdentifier(String geneID) {
		UID=geneID;
		
	}
	
	
	public String getGeneIdentifier() {
		return UID;
	}
	
	public void setProbeID(String probeID) {
		// TODO Auto-generated method stub
		
	}

	
	public void setProbePosition(int pos) {
		// TODO Auto-generated method stub
		
	}

		
	public ArrayList<String> getProbeID() {
		return null;
	}

	
	public ArrayList getProbePosition() {
		return null;
	}


}
