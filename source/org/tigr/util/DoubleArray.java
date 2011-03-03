package org.tigr.util;

/**
 * A wrapper for a DoubleArray[], mainly used to allow efficient persistence.
 * @author Eleanor
 *
 */
public class DoubleArray {

	private double[] array;
	private Double[] objarray;
	
	public DoubleArray(double[] _array) {
		this.array = _array;
	}
	public double[] toArray() {
		return this.array;
	}
	public Double[] toObjectArray() {
		if(objarray == null) {
			Double[] temp = new Double[this.array.length];
			for(int i=0; i<this.array.length; i++) {
				temp[i] = new Double(this.array[i]);
			}
			this.objarray = temp;
		}
		return objarray;
	}
}
