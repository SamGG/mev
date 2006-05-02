package org.tigr.microarray.mev.persistence;

public class MultipleArrayDataState{
	float maxCY3, maxCY5;
	public MultipleArrayDataState(){}
	
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
}