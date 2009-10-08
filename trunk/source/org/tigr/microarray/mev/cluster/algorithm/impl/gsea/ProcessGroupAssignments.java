package org.tigr.microarray.mev.cluster.algorithm.impl.gsea;

import java.util.Hashtable;
import java.util.Vector;

import org.tigr.util.FloatMatrix;

/**
 * 
 * @author Sarita Nair ProcessGroupAssignments class reads a factor file and
 *         generates a Matrix of values corresponding to the factor
 *         (phenotype/class)assignments.
 * 
 * 
 * 
 * 
 */

public class ProcessGroupAssignments {

	protected FloatMatrix factor_matrix;
	protected Vector excludedColumns = new Vector();

	protected int nSamples;
	protected boolean removeNA;

	// GUI based grouping
	private String[] factornames = null;
	private int[][] fAssignments = null;
	private int[] factorLevels = null;

	public ProcessGroupAssignments(String[] factorNames, int[] factorLevels,
			int[][] factorAssignments, boolean removeNA, int num_samples) {
		this.factornames = factorNames;
		this.fAssignments = factorAssignments;
		this.removeNA = removeNA;
		this.nSamples = num_samples;
	}

	/**
	 * findUassignedSamples tracks down the samples which are NOT assigned to
	 * any group. These samples are dropped from the downstream analysis.
	 * 
	 * @param factorNames
	 * @param factorLevels
	 * @param factorAssignments
	 */

	public void findUnassignedSamples(String[] factorNames, int[] factorLevels,
			int[][] factorAssignments) {
		
		this.fAssignments = factorAssignments;
		for (int num_factors = 0; num_factors < factorNames.length; num_factors++) {
			// Extract assignments
			int[] tempAssignments = this.fAssignments[num_factors];
			
			for (int col = 0; col < tempAssignments.length; col++) {
				// Add to the exluded columns list any samples that are
				// unassigned
				if (tempAssignments[col] == 0 && !excludedColumns.contains(col)) {
					this.excludedColumns.add(col);
				}
			}
		}

	}

	/**
	 * 
	 * @returns a Vector containing columns which are unassigned to any of the
	 *          factors.
	 */

	public Vector getUnassignedColumns() {
		return this.excludedColumns;
	}

	public int calculateFactorMatrixColumns(String[] names, int[] levels) {
		int cols = 0;
		// Columns is equal to the number of factor levels minus one. The reason
		// is the intercept column that is added

		for (int i = 0; i < names.length; i++) {
			int temp = levels[i] - 1;
			cols = cols + temp;
		}
		// Add a column for intercept
		cols = cols + 1;
		return cols;

	}

	/**
	 * generateFactorMatrix is modeled after the R function "model.matrix". This
	 * generates a matrix with Rows = Number of samples Columns = Intercept +
	 * Levels of factor1 -1 + Levels of factor2 -1 + ...
	 * 
	 * @param factorNames
	 * @param factorLevels
	 * @param factorAssignments
	 * @return
	 */

	public FloatMatrix generateFactorMatrix(String[] factorNames,
			int[] factorLevels, int[][] factorAssignments) {

		int cols = calculateFactorMatrixColumns(factorNames, factorLevels);
		
		int matColStart = 1;
		int matColEnd = 1;
		int unassigned = ((Vector) getUnassignedColumns()).size();
		
		// In the R function model.matrix, after which this is modeled, if there
		// are samples which are unassigned to any factors OR
		// unassigned to one of the factors; The function removes those samples
		// before computing the FloatMatrix.
		// So, the rows of the resulting matrix will always be (number of
		// samples-unassigned).
		factor_matrix = new FloatMatrix(nSamples - unassigned, cols);
		
		// Setting values of the "Intercept Column". This is the first column of
		// the factor matrix and all rows will always be equal to 1
		for (int i = 0; i < factor_matrix.getRowDimension(); i++) {
			factor_matrix.set(i, 0, 1);
		}

		for (int i = 0; i < factorNames.length; i++) {
			int tempcolStart = 0;
			int tempRowStart = 0;
			// MeV assigns numbers to factor levels.
			// Level==0 is for unassigned samples
			// The first level of any factor is always considered to be the
			// intercept, hence level starts with 2
			int factorlevel = 2;
			int[] rowVector = factorAssignments[i];
			// System.out.println("rowVector size:"+rowVector.length);
			// System.out.println("number of samples:"+factor_matrix.getRowDimension());
			int current_factor_level = factorLevels[i];
			FloatMatrix tempMatrix = new FloatMatrix(factor_matrix
					.getRowDimension(), current_factor_level - 1);

			while (factorlevel <= current_factor_level) {
				int samples = 0;
				// System.out.println("factorlevel"+factorlevel);
				while (samples < nSamples - 1) {
					// System.out.println("factor-Assignment"+rowVector[samples]);
					 //System.out.println("level"+factorlevel);
					if (this.excludedColumns.contains(samples)) {
						// System.out.println("excluded column:"+samples);
						if (samples < nSamples - 1)
							samples = samples + 1;

					}
					// System.out.println("rowvector"+samples+":"+rowVector[samples]);
					if (rowVector[samples] == factorlevel) {
						tempMatrix.set(tempRowStart, tempcolStart, 1);
						tempRowStart = tempRowStart + 1;
					} else {
						tempRowStart = tempRowStart + 1;
					}
					if (samples < nSamples - 1)
						samples = samples + 1;
				}
				// Move to the next column
				if (tempcolStart < current_factor_level) {
					tempcolStart = tempcolStart + 1;
				}
				// Reset the row index to 0
				tempRowStart = 0;
				// Go to the next factor level
				factorlevel = factorlevel + 1;
			}// End of factor level for loop
			matColEnd = matColStart + (current_factor_level - 1) - 1;
			// System.out.println("FloatMatrix col start:"+matColStart);
			 //System.out.println("FloatMatrix col end:"+matColEnd);

			factor_matrix.setMatrix(0, factor_matrix.getRowDimension() - 1,
					matColStart, matColEnd, tempMatrix);
			matColStart = matColEnd + 1;
			matColEnd = matColStart;

		}// End of factornames for loop

		return factor_matrix;
	}

	/*public static void main(String[] args) {
		// TODO Auto-generated method stub

		Hashtable fHash = new Hashtable();
		String[] factorNames = { "FactorA", "FactorB" };
		int[] factorLevels = { 3, 2 };
		int[][] factorAssignments = { { 3, 1, 2, 1, 2, 3 },
				{ 1, 2, 1, 2, 2, 0 } };
		
		ProcessGroupAssignments pg = new ProcessGroupAssignments(new String[] {
				"FactorA", "FactorB" }, new int[] { 3, 3 }, factorAssignments,
				true, 6);
		pg.findUnassignedSamples(factorNames, factorLevels, factorAssignments);
		FloatMatrix factor_matrix = pg.generateFactorMatrix(factorNames,
				factorLevels, factorAssignments);

		for (int i = 0; i < factor_matrix.getRowDimension(); i++) {
			for (int j = 0; j < factor_matrix.getColumnDimension(); j++) {
				System.out.print(factor_matrix.get(i, j));
				System.out.print('\t');
			}
			System.out.println();
		}

		
	}*/

}
