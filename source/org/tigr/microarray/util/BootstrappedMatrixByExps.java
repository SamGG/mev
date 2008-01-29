/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: BootstrappedMatrixByExps.java,v $
 * $Revision: 1.2 $
 * $Date: 2006-02-23 20:59:59 $
 * $Author: caliente $
 * $State: Exp $
 */
package org.tigr.microarray.util;

import java.util.Random;
import java.util.Vector;

import org.tigr.util.FloatMatrix;

public class BootstrappedMatrixByExps {
    
    public Vector resampledIndices = new Vector(); //contains the indices of the experiments from the original matrix in the order they are present in the resampled matrix
    
    //the following method "extracts" the 2D array from a FloatMatrix object to be used by the subsampleMatrix method in creating a resampled matrix.
    
    float[][] get2DArrFromExpMatrix(FloatMatrix expMatrix) {
	int geneDim = expMatrix.getRowDimension();
	int expDim = expMatrix.getColumnDimension();
	float[][] expArray = new float[geneDim][expDim];
	int x, y;
	
	for (x = 0; x < geneDim; x++) {
	    for (y = 0; y < expDim; y++) {
		expArray[x][y] = expMatrix.get(x,y);
	    }
	}
	
	return expArray;
    }
    
    
    
    float[][] resampleMatrix(FloatMatrix expMatrix) {//This creates a resampled array (resampling done on experiments) of the same dimensions as the original array
	
	float[][] aMatrix = get2DArrFromExpMatrix(expMatrix);
	float[][] newMatrix = new float[aMatrix.length][aMatrix[0].length];
	
	int s, t, u;
	int numGenes = aMatrix.length;
	int numExps = aMatrix[0].length;
	int randExp;
	Random generator = new Random();
	
	for (s = 0; s < numExps; s++) {
	    randExp = generator.nextInt(numExps);
	    for (u = 0; u < numGenes; u++) {
		newMatrix[u][s] = aMatrix[u][randExp];
	    }
	    resampledIndices.add(new Integer(randExp));
	}
	
	return newMatrix;
    }
    
    
    public FloatMatrix createResampExpMatrixObject(FloatMatrix expMatrix) {
	int x, y;
	float[][] resampledArray = resampleMatrix(expMatrix);
	FloatMatrix resampMatrixObject = new FloatMatrix(resampledArray);
	return resampMatrixObject;
    }
    
    
    
    void printMatrix(float[][] matrix) {
	int i, j  = 0;
	for (i = 0; i < matrix[0].length; i++) {
	    for (j = 0; j < matrix.length; j++) {
		System.out.print(((int) (matrix[j][i] * 100)) / 100 + " ");
	    }
	    System.out.println();
	}
    }
    
    
}