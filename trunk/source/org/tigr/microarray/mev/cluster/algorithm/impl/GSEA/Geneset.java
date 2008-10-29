package org.tigr.microarray.mev.cluster.algorithm.impl.GSEA;

import java.util.HashMap;
import java.util.Vector;

public class Geneset implements IGeneSet{
	
	
	public String geneSetName;
	public Vector geneSetNames;
	//Unique list of genes across the gene sets
	public Vector genes;
	//Would be useful to have as a Vector because, in the .gmt format files it is difficult to predetermine the
	//number of genes (rows) per gene set
	public Vector geneSetElements;
	
	/**
	 * DO NOT think this constructor is required. The geneSetElements get set in the vector anyway.
	 * no need to have a seperate contsructor.
	 * @param rows
	 * @deprecated use the null argument constructor
	 * @param cols
	 *
	public Geneset(int rows, int cols){
		this.geneSetElements=new Vector();
		this.rows=rows;
		this.cols=cols;
		this.genes=new Vector();
		this.geneSetNames=new Vector();
	}*/
	
	/**
	 * Helpful when you do not have the gene set size in advance. One instance is where 
	 * you are trying to remove the gene sets from the original set which do not have any 
	 * genes OR it happens that all the genes in that set are not present in the expression data.
	 * 
	 */
	public Geneset(){
		this.geneSetElements=new Vector();
		this.genes=new Vector();
		this.geneSetNames=new Vector();
	}
	
	
	/**
	 * getAllGenesets returns a vector containing the names of all gene sets
	 * @return Vector
	 */
	
	public Vector getAllGenesetNames() {
		return geneSetNames;
	}

	/**
	 * getAllGenesinGeneset returns a vector containing the unique genes 
	 * present across all the genesets.
	 *  @return Vector
	 *  
	 */
	public Vector getGenesinGeneset() {
		return genes;
	}
	
	
	public String getGSEAConstant() {
		return GSEAConstant;
	}

	
	public void setGSEAConstant(String temp) {
		GSEAConstant.concat(temp);
		GSEAConstant.trim();
		
	}

	
	public void setAllGenesetNames(Vector genesets) {
		this.geneSetNames=genesets;
		
	}

	/**
	 * setGenesinGeneset function is used to keep a track of 
	 * what genes are present in a gene set. Order does not matter.
	 * 
	 */
	public void setGenesinGeneset(String gene) {
		this.genes.add(gene);
		
	}

	
	public IGeneSetElement getGeneSetElement(int index) {
		return (IGeneSetElement)this.geneSetElements.get(index);
	}

	
	public String getGeneSetName(int index) {
		return (String)this.geneSetNames.get(index);
	}

	
	public void setGeneSetElement(IGeneSetElement element, int index) {
		this.geneSetElements.add(index, element);
		
	}
	
	public Vector getGenesetElements(){
		return this.geneSetElements;
	}
	
	
	
	public void setGeneSetName(String name) {
		this.geneSetName=name;
	
	}
	
	

	
	

	
	

}
