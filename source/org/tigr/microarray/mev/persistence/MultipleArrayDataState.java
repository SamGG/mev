/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
package org.tigr.microarray.mev.persistence;

import java.util.Properties;

import org.tigr.microarray.mev.sampleannotation.MageIDF;

public class MultipleArrayDataState{
	float maxCY3, maxCY5;
	public MultipleArrayDataState(){}
	public boolean isAnnotationLoaded=false;
	public boolean isSampleAnnotationLoaded=false;
	protected MageIDF mageIDFObject=null;
	protected Properties varianceFilterProps; 
	
	public Properties getVarianceFilterProps() {
		if(varianceFilterProps == null) 
			varianceFilterProps = new Properties();
		return varianceFilterProps;
	}
	public void setVarianceFilterProps(Properties varianceFilterProps) {
		this.varianceFilterProps = varianceFilterProps;
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
	private float maxRatio = 0f;
	
	public boolean isAnnotationLoaded() {
		return isAnnotationLoaded;
	}
	public void setAnnotationLoaded(boolean isAnnotationLoaded) {
		this.isAnnotationLoaded = isAnnotationLoaded;
	}
	
	public boolean isSampleAnnotationLoaded(){
		return isSampleAnnotationLoaded;
	}
	
	public void setSampleAnnotationLoaded(boolean loaded){
		this.isSampleAnnotationLoaded=loaded;
	}
	public MageIDF getMageIDFObject() {
		return mageIDFObject;
	}
	public void setMageIDFObject(MageIDF mageIDFObject) {
		this.mageIDFObject = mageIDFObject;
		
	}
	
	
	
}