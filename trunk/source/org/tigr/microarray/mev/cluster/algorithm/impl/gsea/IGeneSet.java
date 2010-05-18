package org.tigr.microarray.mev.cluster.algorithm.impl.gsea;

import java.util.ArrayList;
import java.util.Vector;

import org.tigr.util.FloatMatrix;

/**
 * 
 *
 * This interface represents all functionality to be implemented by a single geneset.
 * 
 * 1. Return names of all genes in the gene set
 * 2. Return the name of the gene set
 * 3. Check if a gene is present in the gene set
 * 4. SynExpressionGroups found for the geneset (applicable for Attract module)
 * 5. SynExpression profile for the geneset (applicable for Attract module)
 * 
 *  @author Sarita
 */
public interface IGeneSet {
	/**
	 * This identifier is static because the gene identifier used has to be uniform across all the genesets
	 */  
	static String GSEAConstant=new String();
	/**
	 * Returns the gene set name 
	 * @return
	 */
	public String getGeneSetName();
	/**
	 * Sets the gene set name
	 * @param name
	 */
	public void setGeneSetName(String name);
	/**
	 * Returns gene set element (genes) at requested index
	 * @param index
	 * @return
	 */
	public IGeneSetElement getGeneSetElement(int index);
	/**
	 * Sets a gene set element(gene) at the requested position
	 * @param element
	 * @param index
	 */
	public void setGeneSetElement(IGeneSetElement element, int index);
	
	
	//Getters
	/**
	 * The gene identifiers have to be stored according to the category
	 * in which they fall. The reason: To enable comparison of the with the identifiers in
	 * the MeV Annotation model 
	 * 
	 * 
	 */
	public String getGSEAConstant();

	
	//Setters
	public void setGSEAConstant(String temp);
	
	/**
	 * Sets the genes corresponding to syn expression groups found in a gene set.
	 * 
	 * @param groups - Genes corresponding to synexpression groups found in a geneset
	 */
	
	public void setSynExpressionGroup(String name, String[] groups);
	/**
	 * Sets the average gene expression values of syn expression groups found in a gene set.
	 * Each row of the matrix corresponds to average gene expression values of genes in a synexpression group
	 * @param fm - FloatMatrix representing average gene expression profile of genes 
	 */
	public void setSynExpressionProfiles(FloatMatrix fm);
	/**
	 * Returns an array representing the genes  in a synexpression group
	 * @return  array with row corresponding to synexpression groups and columns corresponsing to genes in that group
	 */
	public String[] getSynExpressionGroup(String name);
	/**
	 * 
	 * @return float matrix corresponding to average gene expression profiles of genes in a synexpression group
	 */
	public FloatMatrix getSynExpressionProfiles();
	
	/**
	 * Returns an array containing names of all genes in the expression data which have similar expression profiles
	 * as the requested synExpressionGroup
	 * @param synExpressionGroupName
	 * @return
	 */
	public String[] getSimilarGenes(String synExpressionGroupName);
	
	/**
	 * Returns the average gene expression profiles of genes in the expression data found to be similar to requested
	 * synexpression group
	 * 
	 * @return
	 */
	public FloatMatrix getSimilarGeneExpressionProfile();
	
	/**
	 * sets an array containing names of all genes in the expression data which have similar expression profiles
	 * as the requested synExpressionGroup
	 * @param name
	 * @param groups
	 * @return
	 */
	public void setSimilarGenes(String name, String[] groups);
	
	/**
	 * sets the average gene expression profiles of genes in the expression data found to be similar to requested
	 * synexpression group
	 * 
	 * @return
	 */
	public void setSimilarGeneExpressionProfile(FloatMatrix fm);
	

}
