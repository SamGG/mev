/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
package org.tigr.microarray.mev.annotation;

import java.util.Vector;

/**
 * MeV Annotation Interface
 */

/**
 * @author Raktim
 *
 */
public interface IAnnotation {

	/********************************************************************
	 * AnnotationConstants class is used to keep track of the Columns
	 * that are avaialble to query from annotation file. This class is 
	 * used to check at rum time what columns are avaialble, using Java 
	 * Reflection.
	 ********************************************************************/
	public AnnotationFieldConstants fieldConsts = new AnnotationFieldConstants();
	
//	static Vector<String> ChipType = new Vector<String>(5);
//	static Vector<String> ChipName = new Vector<String>(5);
//	static Vector<String> speciesName = new Vector<String>(5);
//	static Vector<String> genomeBuild = new Vector<String>(5);
	
	/****************************
	 * Attribute Getter Functions
	 ****************************/
	public String[] getRefSeqTxAcc();
	public String getGenBankAcc();
	public String getGeneID();
	public String getEntrezGeneID();
	public String getLocusLinkID();
	public String[] getRefSeqProtAcc();
	public String getGeneSymbol();
	public String getGeneTitle();
	public String getProbeCytoband();
	public String getProbeStrand();
	public String getProbeSequence();
//	public String getSpeciesName();
//	public String getChipName();
//	public String getChipType();
//	public String getGenomeBuild();
	public String getCloneID();
	public String getProbeChromosome();
	public int getProbeChromosomeAsInt() throws Exception;
	public int getProbeTxStartBP() throws Exception;
	public int getProbeTxEndBP() throws Exception;
	public int getProbeTxLengthInBP() throws Exception;
	public int getProbeCdsStartBP()throws Exception;
	public int getProbeCdsEndBP() throws Exception;
	public int getProbeCdsLengthInBP() throws Exception;
	public String getUnigeneID ();
	public String[] getGoTerms();
	public String[] getBioCartaPathways();
	public String[] getKeggPathways();
	public String getProbeDesc();
	public String getTgiTC();

	// Setters
	public void setRefSeqTxAcc(String[] _temp);
	public void setGenBankAcc(String _temp);
	public void setGeneID(String _temp);
	public void setEntrezGeneID(String _temp);
	public void setLocusLinkID(String _temp);
	public void setRefSeqProtAcc(String[] _temp);
	public void setGeneSymbol(String _temp);
	public void setGeneTitle(String _temp);
	public void setProbeCytoband(String _temp);
	public void setProbeStrand(String _temp);
	public void setProbeSequence(String _temp);
//	public void setSpeciesName(String _temp);
//	public void setChipName(String _temp);
//	public void setChipType(String _temp);
//	public void setGenomeBuild(String _temp);
	public void setCloneID(String _temp);
	public void setProbeChromosome(String _temp);
	public void setProbeTxStartBP(String _temp) throws Exception;
	public void setProbeTxEndBP(String _temp) throws Exception;
	public void setProbeCdsStartBP(String _temp) throws Exception;
	public void setProbeCdsEndBP(String _temp) throws Exception;
	public void setUnigeneID(String _temp);
	public void setGoTerms(String[] _temp);
	public void setBioCartaPathways(String[] _temp);
	public void setKeggPathways(String[] _temp);
	public void setProbeDesc(String _temp);
	public void setTgiTC(String _temp);
	
	/***********************************************************
	 * Functions to Retreive Attribute value(s)by attribute Name
	 ***********************************************************/
	public String[] getAttribute(final String attr);
	public AnnoAttributeObj getAttributeObj(final String attr);
	public IAnnotation clone();
	
}
