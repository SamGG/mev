package org.tigr.microarray.mev.cluster.algorithm.impl.gsea;

import java.util.Vector;

/**
 * @author Sarita Nair
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
	
	private int row;
	private int col;
	private Vector geneDataElement;
	private String slideName;
	
	//This is useful when trying to display all the probes mapping to a gene using tableviewer.
	private int max_num_of_probes_mappingtoGene=0;
	
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
	
	//Function to return genesetelement, provided gene name
	public IGeneDataElement getGeneDataElement(String Gene){
		for(int i=0; i<this.geneDataElement.size(); i++){
			GeneDataElement gde=(GeneDataElement)this.geneDataElement.get(i);
			
			if(gde.getGeneIdentifier().equalsIgnoreCase(Gene))
				return gde;
		}
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

	/**
	 * getProbetoGeneMapping outputs a 2-d String array
	 * with the first column containing the gene identifier and the subsequent columns containing 
	 * probe identifiers.
	 * @param gData
	 * @return
	 */
	public String[][]getProbetoGeneMapping(GeneData[]gData){
		int maxprobe=0;
		String[][]gDataArray=new String[gData[0].getAllGeneDataElement().size()][];

		for(int gene=0; gene<gData[0].getAllGeneDataElement().size(); gene++){
			GeneDataElement gde = (GeneDataElement) gData[0].getGeneDataElement(gene);
			gDataArray[gene]=new String[gde.getProbeID().size()+1];
			gDataArray[gene][0]=gde.getGeneIdentifier();
			//System.out.print(gDataArray[gene][0]);
			//System.out.print('\t');
			Vector pVector=gde.getProbeID();
			if(pVector.size()>maxprobe){
				maxprobe=pVector.size();
			}

			for(int probes=0; probes<pVector.size(); probes++){
				gDataArray[gene][probes+1]=(String)pVector.get(probes);
				//System.out.print(gDataArray[gene][probes+1]);
				//System.out.print('\t');

			}
			//	System.out.println();
			pVector=null;
		}

		return gDataArray;
	}

	public void set_max_num_probes_mapping_to_gene(int max){
		this.max_num_of_probes_mappingtoGene=max;
	}
	
	public int get_max_num_probes_mapping_to_gene(){
		return this.max_num_of_probes_mappingtoGene;
	}
	
}
