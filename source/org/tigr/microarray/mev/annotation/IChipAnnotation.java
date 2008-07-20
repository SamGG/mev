/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
package org.tigr.microarray.mev.annotation;

/**
 * Encapsulates annotation associated with one experiment, or chip. 
 * 
 * @author eleanora
 *
 */
public interface IChipAnnotation {

	public String getSpeciesName();
	public String getChipName();
	public String getChipType();
	public String getGenomeBuild();
	public String getAnnFileName();
	public String getDataType();
	

	public void setSpeciesName(String _temp);
	public void setChipName(String _temp);
	public void setChipType(String _temp);
	public void setGenomeBuild(String _temp);
	public void setAnnFileName(String _temp);
	public void setDataType(String _temp);
	
}
