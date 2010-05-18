package org.tigr.microarray.mev.cluster.algorithm.impl.gsea;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import org.tigr.util.FloatMatrix;

/**
 * Geneset class provides implementation for IGeneSet interface
 * @author Sarita Nair
 *
 */

public class Geneset  implements IGeneSet{


	private String geneSetName;
	
	//Gene set has to have more than one gene. Default minimum number of genes in gene set is 5, so smaller gene sets get discarded.
	private ArrayList<IGeneSetElement> geneSetElements;
	//List of all genes in a geneset
	private ArrayList<String> genes_in_geneset;
	//Hashmap containing synExpression groups found in a gene set. Applicable for Attract module 
	private HashMap<String, String[]> synExpressionGroups=new HashMap<String, String[]>();
	//float matrix containing average gene expression values of genes in a synexpression group
	private FloatMatrix synExpressionProfile;
	//Hashmap containing genes in expression data found to be similar to the ones in synexpression groups. Applicable for Attract module 
	private HashMap<String, String[]> similarGeneGroups=new HashMap<String, String[]>();
	//float matrix containing average gene expression values of genes found to have similar profiles to the ones in  synexpression groups
	private FloatMatrix averageExpressionProfile;


	/**
	 * Constructor initializing geneSetElements ArrayList
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

	/**
	 * Adds an element (gene) to the geneSetElement arraylist 
	 */
	public void setGeneSetElement(IGeneSetElement element, int index) {
		geneSetElements.add(index, element);

	}
	/**
	 * Returns arraylist containing genesetelements (aka genes )
	 * @return
	 */
	public ArrayList<IGeneSetElement> getGenesetElements(){
		return geneSetElements;
	}

	/**
	 * Set the name of the geneset
	 * 
	 */
	public void setGeneSetName(String name) {
		geneSetName=name;

	}


	/**
	 *  
	 * @return the name of the geneset
	 */
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


	/**
	 * Returns a string array containing genes in each synexpression group.
	 * There may be multiple synExpressionGroups for a geneset.
	 * @return  string array containing genes in each synexpression group
	 */
	public String[] getSynExpressionGroup(String name) {
		return synExpressionGroups.get(name);
	}

    /**
     * FloatMatrix containing average gene expression profile of genes in synexpression groups.
     * Each row represents a synexpression group
     * @return FloatMatrix containing average gene expression profile of genes in synexpression groups
     */
	
	public FloatMatrix getSynExpressionProfiles() {
		return synExpressionProfile;
	}


	/**
	 * Sets genes present in a synexpression group
	 */
	public void setSynExpressionGroup(String name, String[] groups) {
		
		if(!synExpressionGroups.containsKey(name)) {
			synExpressionGroups.put(name, groups);
		}
		
	}

	/**
	 * Sets the average gene expression profile for genes in synexpression groups 
	 *  
	 */
	public void setSynExpressionProfiles(FloatMatrix fm) {
		synExpressionProfile=new FloatMatrix(fm.getRowDimension(), fm.getColumnDimension());
		synExpressionProfile=fm;
		
	}

    /**
     * Returns genes having same profile as ones in present in requested synexpression group
     * 
     */
	public String[] getSimilarGenes(
			String synExpressionGroupName) {
		
		return similarGeneGroups.get(synExpressionGroupName);
	}


	/**
	 * FloatMatrix containing average gene expression profile of genes similar to ones in synexpression groups.
     * Each row represents expression profile of a group similar to corresponding synexpression group
     * @return FloatMatrix average gene expression profile of genes similar to ones in synexpression groups.
	 *  
	 */
	public FloatMatrix getSimilarGeneExpressionProfile(	) {
		return averageExpressionProfile;
	}
	
	/**
	 * Sets average gene expression profile of genes similar to ones in synexpression groups.
     * Each row represents expression profile of a group similar to corresponding synexpression group
     *   
	 * 
	 */
	public void setSimilarGeneExpressionProfile(FloatMatrix fm) {
		averageExpressionProfile=new FloatMatrix(fm.getRowDimension(), fm.getColumnDimension());
		averageExpressionProfile=fm;
	}


	/**
	 * 
	 *  Sets genes having same profile as ones in present in requested synexpression group
	 * 
	 */
	public void setSimilarGenes(String name, String[] groups) {
		if(!similarGeneGroups.containsKey(name)) {
		similarGeneGroups.put(name, groups);
		}
		
	}

 


}
