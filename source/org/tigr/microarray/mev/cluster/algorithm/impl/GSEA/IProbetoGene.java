package org.tigr.microarray.mev.cluster.algorithm.impl.GSEA;

public interface IProbetoGene {


	/**
	 * Returns the number of probes which map to a given gene
	 */
	public String[] getProbes(String geneID);
	/**
	 * 
	 * @param geneID
	 * @returns the number of probes associated with a gene 
	 */
	public int numProbes(String geneID);

	/**
	 * 
	 * @returns the description of the gene identifier used.
	 * for e.g. ENTREZ_ID or GENE_SYMBOL
	 */
	public String getDescription();




}
