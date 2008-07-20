/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
package org.tigr.microarray.mev.persistence;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.beans.Beans;

/**
 * @author eleanora
 *
 * This class stores MultipleArrayData state information.  
 */
public class MEVSessionPrefs extends Beans {
	private Dimension elementSize;
	private float minRatioScale, maxRatioScale, midRatioScale, maxCY3, maxCY5;
	private boolean colorGradientState, autoScale;
	private int colorScheme;
	private BufferedImageWrapper positiveGradientImageWrapper, negativeGradientImageWrapper;
	
	/**
	 * @return Returns the colorScheme.
	 */
	public int getColorScheme() {
		return colorScheme;
	}
	/**
	 * @param colorScheme The colorScheme to set.
	 */
	public void setColorScheme(int colorScheme) {
		this.colorScheme = colorScheme;
	}
	public MEVSessionPrefs(){}
	
	/**
	 * @return Returns the maxRatioScale.
	 */
	public float getMaxRatioScale() {
		return maxRatioScale;
	}
	/**
	 * @param maxRatioScale The maxRatioScale to set.
	 */
	public void setMaxRatioScale(float maxRatioScale) {
		this.maxRatioScale = maxRatioScale;
	}
	/**
	 * @return Returns the minRatioScale.
	 */
	public float getMinRatioScale() {
		return minRatioScale;
	}
	/**
	 * @param minRatioScale The minRatioScale to set.
	 */
	public void setMinRatioScale(float minRatioScale) {
		this.minRatioScale = minRatioScale;
	}

	/**
	 * @param median
	 */
	public void setMidRatioScale(float median) {
		this.midRatioScale = median;
	}
	/**
	 * @return Returns the midRatioScale.
	 */
	public float getMidRatioScale() {
		return midRatioScale;
	}
	/**
	 * @return Returns the maxCY3.
	 */
	public float getMaxCY3() {
		return maxCY3;
	}
	/**
	 * @param maxCY3 The maxCY3 to set.
	 */
	public void setMaxCY3(float maxCY3) {
		this.maxCY3 = maxCY3;
	}
	/**
	 * @return Returns the maxCY5.
	 */
	public float getMaxCY5() {
		return maxCY5;
	}
	/**
	 * @param maxCY5 The maxCY5 to set.
	 */
	public void setMaxCY5(float maxCY5) {
		this.maxCY5 = maxCY5;
	}
	/**
	 * @return Returns the colorGradientState.
	 */
	public boolean isColorGradientState() {
		return colorGradientState;
	}
	/**
	 * @param colorGradientState The colorGradientState to set.
	 */
	public void setColorGradientState(boolean colorGradientState) {
		this.colorGradientState = colorGradientState;
	}
	/**
	 * @return Returns the negativeGradientImage.
	 */
	public BufferedImageWrapper getNegativeGradientImageWrapper() {
		return negativeGradientImageWrapper;
	}
	/**
	 * @param negativeGradientImage The negativeGradientImage to set.
	 */
	public void setNegativeGradientImageWrapper(BufferedImageWrapper negativeGradientImageWrapper) {
		this.negativeGradientImageWrapper = negativeGradientImageWrapper;
	}
	/**
	 * @return Returns the positiveGradientImage.
	 */
	public BufferedImageWrapper getPositiveGradientImageWrapper() {
		return positiveGradientImageWrapper;
	}
	/**
	 * @param positiveGradientImage The positiveGradientImage to set.
	 */
	public void setPositiveGradientImageWrapper(BufferedImageWrapper positiveGradientImageWrapper) {
		this.positiveGradientImageWrapper = positiveGradientImageWrapper;
	}
	/**
	 * @return Returns the autoScale.
	 */
	public boolean isAutoScale() {
		return autoScale;
	}
	/**
	 * @param autoScale The autoScale to set.
	 */
	public void setAutoScale(boolean autoScale) {
		this.autoScale = autoScale;
	}
	/**
	 * @return Returns the elementSize.
	 */
	public Dimension getElementSize() {
		return elementSize;
	}
	/**
	 * @param elementSize The elementSize to set.
	 */
	public void setElementSize(Dimension elementSize) {
		this.elementSize = elementSize;
	}
}