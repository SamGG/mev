package org.tigr.microarray.mev.cluster.algorithm.impl.GSEA;

import java.util.Vector;

/**
 * @author sarita
 * GeneData class is meant to store the "processed" expression data
 * One of the steps in GSEA is to collapse probes to genes. If there
 * are multiple probes mapping to a gene, the expression data corresponding to
 * each of these probes would be "collapsed" using the criteria provided by the user.
 * 
 * This data is stored in GeneData. This class is similar to SlideData
 *  
 *
 */


public class GeneData implements IGeneData{
	
	protected int row;
	protected int col;
	protected Vector geneDataElement;
	protected String slideName;
	
	public GeneData(int rows, int cols){
		this.col=cols;
		this.row=rows;
		this.geneDataElement=new Vector(rows);
		
		
	}
	
	
	public GeneData(){
		this.geneDataElement=new Vector();
	}
	
	
	public String getCollapseMode() {
		return null;
	}

	
	public IGeneDataElement getGeneDataElement(int index) {
		if(index <this.geneDataElement.size()){
		return (IGeneDataElement)this.geneDataElement.get(index);
		}else
			return null;
	}

	
	public String getSlideName() {
		return this.slideName;
	}

	
	public void setGeneDataElement(IGeneDataElement gde, int index) {
		if(index<=this.geneDataElement.size())
		this.geneDataElement.add(index, gde);
	}

	
	public float getCurrentIntensity(int index) {
		IGeneDataElement gde=this.getGeneDataElement(index);
		return gde.getCurrentIntensity(); 
	}

	
	public void setCurrentIntensity(float intensity, int index) {
		IGeneDataElement gde=this.getGeneDataElement(index);
		gde.setCurrentIntensity(intensity);
		
	}


	
	public void setSlideName(String name) {
		this.slideName=name;
		
	}
	
	public Vector getAllGeneDataElement(){
		return this.geneDataElement;
	}

}
