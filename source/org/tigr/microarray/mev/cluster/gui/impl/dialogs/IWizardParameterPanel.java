/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * Created on Jan 9, 2007
 * braisted
 */
package org.tigr.microarray.mev.cluster.gui.impl.dialogs;

/**
 * @author braisted
 *
 * Interface to support generic wizard parameter panels for extensions of
 * <code>StatProcessWizard</code> can call methods to populate AlgorithmData
 */

public interface IWizardParameterPanel {
	
	/**
	 * supports capturing values from the parameter panel into shared AlgorithmData
	 */
	public void populateAlgorithmData();
	
	/**
	 * supports clearing values from the wizard parameter panel from AlgorithmData
	 */
	public void clearValuesFromAlgorithmData();

	/**
	 * Adjusts appearance and size attribuites for display
	 */
	public void onDisplayed();
}
