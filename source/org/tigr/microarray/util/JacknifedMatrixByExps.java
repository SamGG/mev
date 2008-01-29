/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: JacknifedMatrixByExps.java,v $
 * $Revision: 1.2 $
 * $Date: 2006-02-23 20:59:59 $
 * $Author: caliente $
 * $State: Exp $
 */
package org.tigr.microarray.util;

import java.util.Random;
import java.util.Vector;

import org.tigr.util.FloatMatrix;

public class JacknifedMatrixByExps {
    public Vector resampledIndices = new Vector();
    int jacknifedExperiment;
    
    //the following method "extracts" the 2D array from an ExpressionMatrix object to be used by the subsampleMatrix method in creating a resampled matrix.
    
    
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
    
    
    float[][] resampleMatrix(FloatMatrix expMatrix) {//create a jacknifed 2D array by experiments
	
	float[][] aMatrix = get2DArrFromExpMatrix(expMatrix);
	float[][] newMatrix = new float[aMatrix.length][aMatrix[0].length - 1];
	int numGenes = aMatrix.length;
	int numExps = aMatrix[0].length;
	int randExp;
	int k = 0;
	Random generator = new Random();
	
	randExp = generator.nextInt(numExps);
	
	for (int i = 0; i < numGenes; i++) {
	    k = 0;
	    for (int j = 0; j < numExps; j++) {
		if (j != randExp) {
		    newMatrix[i][k] = aMatrix[i][j];
		    k++;
		}
	    }
	}
	
	jacknifedExperiment = randExp;
	
	for (int i = 0; i < numExps; i++) {
	    if (i != jacknifedExperiment) {
		resampledIndices.add(new Integer(i));
	    }
	}
	
	return newMatrix;
	
    }
    
    
    public FloatMatrix createResampExpMatrixObject(FloatMatrix expMatrix) {
	int x, y;
	float[][] resampledArray = resampleMatrix(expMatrix);
	int numbExps = resampledArray[0].length;
	int numbGenes = resampledArray.length;
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