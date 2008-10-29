package org.tigr.microarray.mev.cluster.algorithm.impl.GSEA;

import org.tigr.microarray.mev.cluster.gui.IGeneSet;

public class GeneSetElement implements IGeneSetElement{

	
	protected String gene;
	protected String uID;
	
	public GeneSetElement(String UID, String gene){
		this.uID=UID;
		this.gene=gene;
	}
	
	
	public String getGene() {
		return gene;
	}

	
	public void setGene(String parameter) {
		this.gene=parameter;
		
	}

}
