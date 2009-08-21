package org.tigr.util;

/**
 * A wrapper for a boolean[], mainly used to allow efficient persistence.
 * @author Eleanor
 *
 */
public class BooleanArray {

	private boolean[] array;
	
	public BooleanArray(boolean[] _array) {
		this.array = _array;
	}
	public boolean[] toArray() {
		return this.array;
	}
}
