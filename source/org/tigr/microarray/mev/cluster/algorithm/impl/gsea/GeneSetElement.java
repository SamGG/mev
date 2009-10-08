package org.tigr.microarray.mev.cluster.algorithm.impl.gsea;

import org.tigr.microarray.mev.cluster.gui.IGeneSet;

/**
 * @author Sarita Nair
 *
 */

public class GeneSetElement implements IGeneSetElement{

	
	private String gene;
	private String UID;
	private float testStat;
	
	public GeneSetElement(String ID, String geneName){
		UID=ID;
		gene=geneName;
	}
	
	
	public String getGene() {
		return gene;
	}

	
	public void setGene(String parameter) {
		gene=parameter;
		
	}


	
	public float getTestStat() {
		return testStat;
	}


	
	public void setTestStat(float tstat) {
		testStat=tstat;
		
	}

}
