package org.tigr.microarray.mev.cluster.algorithm.impl.gsea;

import java.util.ArrayList;
import java.util.Vector;

/**
 * 
 * @author Sarita
 * This interface represents all functionality to be implemented by a single geneset.
 * 
 * 1. Return names of all genes in the gene set
 * 2. Return the name of the gene set
 * 3. Check if a gene is present in the gene set
 *
 */
public interface IGeneSet {
	/**
	 * This identifier is static because the gene identifier used has to be uniform across all the genesets
	 */
	static String GSEAConstant=new String();
	
	public String getGeneSetName();
	
	public void setGeneSetName(String name);
	
	public IGeneSetElement getGeneSetElement(int index);
	
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
	
	
	

}
