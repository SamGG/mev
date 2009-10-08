package org.tigr.microarray.mev.cluster.algorithm.impl.gsea;

import java.util.ArrayList;

/**
 * @author Sarita Nair
 * FloatGeneData is another implementation of IGeneData. It is exactly the same as GeneData
 * except that it stores FloatGeneDataElement instead of GeneDataElement
 * 
 * 
 *
 */
public class FloatGeneData implements IGeneData{

	private ArrayList floatGeneDataElement;
	private String slideName;
	
	public FloatGeneData(){
		floatGeneDataElement=new ArrayList();
	}
	
	
	public float getCurrentIntensity(int index) {
		IGeneDataElement gde=this.getGeneDataElement(index);
		return gde.getCurrentIntensity();
		
	}

	
	public IGeneDataElement getGeneDataElement(int index) {
		if(index <floatGeneDataElement.size()){
			return (IGeneDataElement)floatGeneDataElement.get(index);
			}else
				return null;
	
	}

	public ArrayList getAllGeneDataElement(){
		return floatGeneDataElement;
	}
	
	
	public String getSlideName() {
		return slideName;
	}

	
	public void setCurrentIntensity(float intensity, int index) {
		IGeneDataElement gde=getGeneDataElement(index);
		gde.setCurrentIntensity(intensity);
	
		
	}

	
	public void setGeneDataElement(IGeneDataElement gde, int index) {
		floatGeneDataElement.add(index, gde);

		
	}

	
	public void setSlideName(String name) {
		slideName=name;
		
	}
	
	//Function to return genesetelement, provided gene name
	public IGeneDataElement getGeneDataElement(String Gene){
		for(int i=0; i<this.floatGeneDataElement.size(); i++){
			FloatGeneDataElement gde=(FloatGeneDataElement)this.floatGeneDataElement.get(i);
			
			if(gde.getGeneIdentifier().equalsIgnoreCase(Gene))
				return gde;
		}
		return null;
	}
	

}
