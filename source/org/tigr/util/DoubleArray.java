package org.tigr.util;

/**
 * A wrapper for a DoubleArray[], mainly used to allow efficient persistence.
 * @author Eleanor
 *
 */
public class DoubleArray {

	private double[] array;
	
	public DoubleArray(double[] _array) {
		this.array = _array;
	}
	public double[] toArray() {
		return this.array;
	}
}
