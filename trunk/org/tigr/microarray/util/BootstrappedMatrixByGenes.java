/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: BootstrappedMatrixByGenes.java,v $
 * $Revision: 1.1.1.1 $
 * $Date: 2003-08-21 21:04:23 $
 * $Author: braisted $
 * $State: Exp $
 */
package org.tigr.microarray.util;

import java.util.*;
import org.tigr.util.FloatMatrix;

public class BootstrappedMatrixByGenes {
    
    
    public Vector resampledIndices = new Vector();
    
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
    
    
    float[][] resampleMatrix(FloatMatrix expMatrix) {//This creates a resampled array (resampling done on genes) of the same dimensions as the original array
	
	float[][] aMatrix = get2DArrFromExpMatrix(expMatrix);
	float[][] newMatrix = new float[aMatrix.length][aMatrix[0].length];
	int s, t, u;
	int numGenes = aMatrix.length;
	int randGene;
	Random generator = new Random();
	
	final int STYLE = 2; //0 = Reshuffled, 1 = Reversed, 2 = Resampled
	
	if (STYLE == 0) {
/*
		Vector reverseIDs = new Vector();
		float[] temp = new float[aMatrix.length];
		String tempID = null;
 
		Vector shuffledUniqueIDs = (Vector) ExpMatrix.UniqueIDs.clone();
 
		for (s = numGenes; s > 0; s--) {
 
			randGene = generator.nextInt(s);
			for (int j = 0; j < aMatrix.length; j++) {
				temp[j] = aMatrix[j][randGene];
				tempID = (String) shuffledUniqueIDs.elementAt(randGene);
			}
			for (int j = 0; j < aMatrix.length; j++) {
				aMatrix[j][randGene] = aMatrix[j][s - 1];
				shuffledUniqueIDs.setElementAt(shuffledUniqueIDs.elementAt(s - 1), randGene);
			}
			for (int j = 0; j < aMatrix.length; j++) {
				aMatrix[j][s - 1] = temp[j];
				shuffledUniqueIDs.setElementAt(tempID, (s - 1));
			}
 
			//reverseIDs.add((String) ExpMatrix.GetUniqueID(randGene));
		}
 
		//for (int i = reverseIDs.size(); i > 0; i--) {
		//	resampledUniqueIDNames.add(reverseIDs.elementAt(i - 1));
		//}
 
		resampledUniqueIDNames = shuffledUniqueIDs;
 */
	    
	} else if (STYLE == 1) {
	    
		/*
		float[] temp = new float[aMatrix.length];
		for (int i = 0; i < numGenes; i++) {
		 
			for (int j = 0; j < aMatrix.length; j++) {
				temp[j] = aMatrix[j][randGene];
				tempID = (String) shuffledUniqueIDs.elementAt(randGene);
			}
			for (int j = 0; j < aMatrix.length; j++) {
				aMatrix[j][randGene] = aMatrix[j][s - 1];
				shuffledUniqueIDs.setElementAt(shuffledUniqueIDs.elementAt(s - 1), randGene);
			}
			for (int j = 0; j < aMatrix.length; j++) {
				aMatrix[j][s - 1] = temp[j];
				shuffledUniqueIDs.setElementAt(tempID, (s - 1));
			}
		}
		Vector reverseIDs = new Vector();
		for (int i = 0; i < numGenes; i++) {
			reverseIDs.add((String) ExpMatrix.GetUniqueID(i));
		}
		for (int i = reverseIDs.size(); i > 0; i--) {
			resampledUniqueIDNames.add(reverseIDs.elementAt(i - 1));
		}
		 
		System.out.println("Reversed");
		printMatrix(aMatrix);
		 */
	    
	} else if (STYLE == 2) {
	    
	    for (s = 0; s < numGenes; s++) {
		randGene = generator.nextInt(numGenes);
		for (u = 0; u < newMatrix[0].length; u++) {
		    newMatrix[s][u] = aMatrix[randGene][u];
		}
		resampledIndices.add(new Integer(randGene));
	    }
	    
	    aMatrix = newMatrix;
	}
	
	return aMatrix;
    }
    
    
    
    public FloatMatrix createResampExpMatrixObject(FloatMatrix expMatrix) {
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