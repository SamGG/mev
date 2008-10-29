package org.tigr.microarray.mev.cluster.algorithm.impl.GSEA;

import java.util.Vector;

/**
 * 
 * @author sarita
 * 
 * This interface should contain functions to --Tentative
 * 1. Return rowsums (number of genes per gene set)
 * 2. Return names of all the gene sets
 * 3. Return the genes in each gene set
 * 4. Remove a gene set, if it does not contain the required number of genes as specified by the user.
 * 5. Tell if a gene is present in a particular gene set 
 * 6. Colsums, number of gene sets that a gene is present in
 * 
 *This interface should be implemented by the classes parsing the gene set files.
 * 
 *
 */
public interface IGeneSet {
	//These two functions can be implemented in the class creating Amat
	//public int getGenesinGeneSet(String gsName);
	//public int getGenesetsperGene(String gName);
	/**
	 * This identifier is static because the gene identifier used has to be uniform across all the genesets
	 */
	static String GSEAConstant=new String();
	
	public String getGeneSetName(int index);
	
	public void setGeneSetName(String name);
	
	//The GMX format files have a line dedicated to description of the gene sets.
	/**
	 * Uncomment if you think people care.
	 *
	public String getGeneSetDesc(int index);
	/**
	 *
	 *
	 *
	public void setGeneSetDesc(String desc);
	*/
	public IGeneSetElement getGeneSetElement(int index);
	
	//Setters
	public void setGeneSetElement(IGeneSetElement element, int index);
	/**
	 * 
	 * @returns a Vector of all the genesets.
	 */
	public Vector getAllGenesetNames();
	/**
	 * @returns a Vector of all the genes (unique) present in the geneset/s.
	 *  
	 * 
	 */
	public Vector getGenesinGeneset();
	
	public void setAllGenesetNames(Vector genesets);
	
	public void setGenesinGeneset(String gene);
	
	
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
	 * @returns a Vector of geneset/s in which a given gene is present
	 *
	public String[] getGenesets(String geneId);
	*/
	
	

}
