/*
Copyright @ 1999-2007, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
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
