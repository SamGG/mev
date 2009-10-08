package org.tigr.microarray.mev.cluster.algorithm.impl.gsea;

import java.util.ArrayList;
import java.util.Vector;

/**
 * 
 * @author Sarita Nair
 *
 */

public class Geneset  implements IGeneSet{


	private String geneSetName;
	
	//Gene set has to have more than one gene. Default minimum number of genes in gene set is 5, so smaller gene sets get discarded.
	private ArrayList<IGeneSetElement> geneSetElements;
	private ArrayList<String> genes_in_geneset;

	/**
	 * Helpful when you do not have the gene set size in advance. One instance is where 
	 * you are trying to remove the gene sets from the original set which do not have any 
	 * genes OR it happens that all the genes in that set are not present in the expression data.
	 * 
	 */
	public Geneset(){
		this.geneSetElements=new ArrayList();
	
	}


	public String getGSEAConstant() {
		return GSEAConstant;
	}


	public void setGSEAConstant(String temp) {
		GSEAConstant.concat(temp);
		GSEAConstant.trim();

	}


	

	public IGeneSetElement getGeneSetElement(int index) {
		return (IGeneSetElement)this.geneSetElements.get(index);
	}


	public void setGeneSetElement(IGeneSetElement element, int index) {
		geneSetElements.add(index, element);

	}

	public ArrayList<IGeneSetElement> getGenesetElements(){
		return geneSetElements;
	}



	public void setGeneSetName(String name) {
		geneSetName=name;

	}


	
	public String getGeneSetName() {
		return geneSetName;
	}

	
	public ArrayList<String> getGenesinGeneset(){
		genes_in_geneset=new ArrayList<String>();
		for(int index=0; index<this.getGenesetElements().size(); index++){
			if(!genes_in_geneset.contains(((GeneSetElement)this.getGeneSetElement(index)).getGene()));
			genes_in_geneset.add(index, ((GeneSetElement)this.getGeneSetElement(index)).getGene());
		}



		return genes_in_geneset;

	}
	
	





}
