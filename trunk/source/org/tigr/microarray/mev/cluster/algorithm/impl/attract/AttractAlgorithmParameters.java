package org.tigr.microarray.mev.cluster.algorithm.impl.attract;

import org.tigr.microarray.mev.cluster.algorithm.AlgorithmParameters;
import org.tigr.microarray.mev.cluster.algorithm.impl.gsea.Geneset;
/**
 * AttractAlgorithParameters provides a container for Attract specific
 * parameters (Geneset[])required for Algorithm execution 
 * 
 * @author sarita
 *
 */
public class AttractAlgorithmParameters extends AlgorithmParameters{

	/**
	 * AlgorithmParameters is Serializable and hence a default serialVersionId
	 */
	private static final long serialVersionUID = 1L;
	private Geneset[]geneSet;
	
	
	public AttractAlgorithmParameters(Geneset[]gset) {
		super();
		geneSet=gset;
		
	}
	
	public Geneset[]getGenesets(){
		return geneSet;
	}
	
	
	public void setGenesets(Geneset[]geneSets) {
		geneSet=geneSets;
	}
	
	
}
