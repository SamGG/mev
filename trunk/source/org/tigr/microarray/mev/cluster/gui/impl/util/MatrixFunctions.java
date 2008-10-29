/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/**
 * @author Sarita Nair
 * 
 * 
 * 
 */
package org.tigr.microarray.mev.cluster.gui.impl.util;

import java.util.Vector;

import org.tigr.util.FloatMatrix;

public class MatrixFunctions {

	public MatrixFunctions() {

	}

	/**
	 * getRowSums returns a vector containing the row sums. The size of the
	 * vector would be equal to the number of rows of the matrix.
	 * 
	 * @param matrix
	 * @return
	 */
	public Vector getRowSums(FloatMatrix matrix) {
		Vector rowSums = new Vector();
		for (int index = 0; index < matrix.getRowDimension(); index++) {
			float _tempVal = 0;
			for (int col = 0; col < matrix.getColumnDimension(); col++) {
				if (!Float.isNaN(matrix.get(index, col)))
					_tempVal += matrix.get(index, col);
			}
			rowSums.add(index, _tempVal);
		}
		return rowSums;
	}

}
