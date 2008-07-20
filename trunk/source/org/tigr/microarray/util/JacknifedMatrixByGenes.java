/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: JacknifedMatrixByGenes.java,v $
 * $Revision: 1.2 $
 * $Date: 2006-02-23 20:59:59 $
 * $Author: caliente $
 * $State: Exp $
 */
package org.tigr.microarray.util;

import java.util.Random;
import java.util.Vector;

import org.tigr.util.FloatMatrix;

public class JacknifedMatrixByGenes {
    
    public Vector resampledIndices = new Vector();
    int jackknifedGene;
    
    
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
    
    
    float[][] resampleMatrix(FloatMatrix expMatrix) {//create a jacknifed 2D array by genes
	
	float[][] aMatrix = get2DArrFromExpMatrix(expMatrix);
	float[][] newMatrix = new float[aMatrix.length - 1][aMatrix[0].length];
	int numGenes = aMatrix.length;
	int numExps = aMatrix[0].length;
	int randGene;
	int k = 0;
	Random generator = new Random();
	
	randGene = generator.nextInt(numGenes);
	
	for (int i = 0; i < numExps; i++) {
	    k = 0;
	    for (int j = 0; j < numGenes; j++) {
		if (j != randGene) {
		    newMatrix[k][i] = aMatrix[j][i];
		    k++;
		}
	    }
	}
	
	jackknifedGene = randGene;
	
	for (int i = 0; i < numGenes; i++) {
	    if (i != jackknifedGene) {
		resampledIndices.add(new Integer(i));
	    }
	}
	
	return newMatrix;
	
    }
    
    
    public FloatMatrix createResampExpMatrixObject(FloatMatrix ExpMatrix) {
	
	int x, y;
	float[][] resampledArray = resampleMatrix(ExpMatrix);
	
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
