/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: PCA.java,v $
 * $Revision: 1.4 $
 * $Date: 2007-10-22 16:10:00 $
 * $Author: raktim $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.algorithm.impl;

import java.util.Arrays;
import java.util.Vector;

import org.tigr.microarray.mev.cluster.algorithm.AbortException;
import org.tigr.microarray.mev.cluster.algorithm.AbstractAlgorithm;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmData;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmEvent;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmException;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmParameters;
import org.tigr.util.FloatMatrix;
import org.tigr.util.Maths;
import org.tigr.util.QSort;

public class PCA extends AbstractAlgorithm {
    private boolean stop = false;
    private int numNeighbors;    
    private int numGenes, numExps;  
    private float factor;   
    private int mode, center, shortcut;
    private boolean useShortcut;
    public AlgorithmData execute(AlgorithmData data) throws AlgorithmException {
	FloatMatrix expMatrix = data.getMatrix("experiment");
	AlgorithmParameters map = data.getParams();
	int function = map.getInt("distance-function", COVARIANCE);
	factor = map.getFloat("distance-factor", 1.0f);
	boolean absolute = map.getBoolean("distance-absolute", false);
	mode = map.getInt("pca-mode", 0);
	shortcut = map.getInt("use-shortcut");
	useShortcut=false;
	if (shortcut==0)
		useShortcut=true;
	numNeighbors = map.getInt("numNeighbors", 10);  
    
	//Raktim - Indicate centering mode as mean or median or none
	center  = map.getInt("centering", 3);
	
	numGenes = expMatrix.getRowDimension();
	numExps = expMatrix.getColumnDimension();        
	
	final int numberOfGenes = expMatrix.getRowDimension();
	final int numberOfSamples = expMatrix.getColumnDimension();
        /*
	FloatMatrix An = new FloatMatrix(numberOfGenes, numberOfSamples);
	for (int row=0; row<numberOfGenes; row++) {
	    for (int column=0; column<numberOfSamples; column++) {
		if (!Float.isNaN(expMatrix.get(row, column))) {
		    An.set(row, column, expMatrix.get(row, column));
		} else {
		    An.set(row, column, 0);
		}
	    }
	}
        */
	FloatMatrix An = imputeKNearestMatrix(expMatrix, numNeighbors);
	
	//jcb moved this event report after imputation
	AlgorithmEvent event = new AlgorithmEvent(this, AlgorithmEvent.PROGRESS_VALUE, 0);
	int eventValue = 0;
	event.setIntValue(eventValue);
	event.setDescription("Calculate covariance matrix\n");
	fireValueChanged(event);
	
	FloatMatrix matrix = null;
	if (mode==0) {
	    matrix = An;
	}
	if (mode==1||useShortcut){
	    matrix = new FloatMatrix(numberOfSamples, numberOfSamples);
	    for (int column=0; column<numberOfSamples; column++) {
	    	for (int row=0; row<numberOfSamples; row++) {
                    matrix.set(row, column, ExperimentUtil.distance(An, row, column, function, factor, absolute));
	    	}
	    }
	}
	if (mode==3&&(!useShortcut)){
		matrix = new FloatMatrix(numberOfGenes, numberOfGenes);
		FloatMatrix AnTranspose = An.transpose();
		
		for (int column=0; column<numberOfGenes; column++) {
		    for (int row=0; row<numberOfGenes; row++) {
		    	matrix.set(row, column, ExperimentUtil.distance(AnTranspose, row, column, function, factor, absolute));
	        }
		}
	}
	
	
	float[][] A = matrix.getArrayCopy();
	int m = matrix.getRowDimension();
	int n = matrix.getColumnDimension();
	int nu = Math.min(m,n);
	float[] s = new float [Math.min(m+1,n)];
	int[] order = new int [Math.min(m+1,n)];
	float[][] U = new float [m][nu];
	float[][] V = new float [n][n];
	float[] e = new float [n];
	float[] work = new float [m];
	boolean wantu = true;
	boolean wantv = true;
	// Reduce A to bidiagonal form, storing the diagonal elements
	// in s and the super-diagonal elements in e.
	int nct = Math.min(m-1,n);
	int nrt = Math.max(0,Math.min(n-2,m));
	for (int i=0; i<Math.min(m+1,n); i++) {
	    order[i]=i;
	}
	eventValue++;
	event.setIntValue(eventValue);
	event.setDescription("Reducing A to bidiagonal form\n");
	fireValueChanged(event);
	int counter = 0;
	//int factor=(int)Math.round(Math.max(nct,nrt)/50.0);
	for (int k = 0; k < Math.max(nct,nrt); k++) {
	    counter++;
	    if (k < nct) {
		// Compute the transformation for the k-th column and
		// place the k-th diagonal in s[k].
		// Compute 2-norm of k-th column without under/overflow.
		s[k] = 0;
		for (int i = k; i < m; i++) {
		    s[k] = Maths.hypot(s[k],A[i][k]);
		}
		if (s[k] != 0.0) {
		    if (A[k][k] < 0.0) {
			s[k] = -s[k];
		    }
		    for (int i = k; i < m; i++) {
			A[i][k] /= s[k];
		    }
		    A[k][k] += 1.0;
		}
		s[k] = -s[k];
	    }
	    for (int j = k+1; j < n; j++) {
		if ((k < nct) & (s[k] != 0.0)) {
		    // Apply the transformation.
		    float t = 0;
		    for (int i = k; i < m; i++) {
			t += A[i][k]*A[i][j];
		    }
		    t = -t/A[k][k];
		    for (int i = k; i < m; i++) {
			A[i][j] += t*A[i][k];
		    }
		}
		// Place the k-th row of A into e for the
		// subsequent calculation of the row transformation.
		e[j] = A[k][j];
	    }
	    if (wantu & (k < nct)) {
		// Place the transformation in U for subsequent back
		// multiplication.
		for (int i = k; i < m; i++) {
		    U[i][k] = A[i][k];
		}
	    }
	    if (k < nrt) {
		// Compute the k-th row transformation and place the
		// k-th super-diagonal in e[k].
		// Compute 2-norm without under/overflow.
		e[k] = 0;
		for (int i = k+1; i < n; i++) {
		    e[k] = Maths.hypot(e[k],e[i]);
		}
		if (e[k] != 0.0) {
		    if (e[k+1] < 0.0) {
			e[k] = -e[k];
		    }
		    for (int i = k+1; i < n; i++) {
			e[i] /= e[k];
		    }
		    e[k+1] += 1.0;
		}
		e[k] = -e[k];
		if ((k+1 < m) & (e[k] != 0.0)) {
		    // Apply the transformation.
		    for (int i = k+1; i < m; i++) {
			work[i] = 0.0f;
		    }
		    for (int j = k+1; j < n; j++) {
			for (int i = k+1; i < m; i++) {
			    work[i] += e[j]*A[i][j];
			}
		    }
		    for (int j = k+1; j < n; j++) {
			float t = -e[j]/e[k+1];
			for (int i = k+1; i < m; i++) {
			    A[i][j] += t*work[i];
			}
		    }
		}
		if (wantv) {
		    // Place the transformation in V for subsequent
		    // back multiplication.
		    for (int i = k+1; i < n; i++) {
			V[i][k] = e[i];
		    }
		}
	    }
	}
	// Set up the final bidiagonal matrix or order p.
	int p = Math.min(n,m+1);
	if (nct < n) {
	    s[nct] = A[nct][nct];
	}
	if (m < p) {
	    s[p-1] = 0.0f;
	}
	if (nrt+1 < p) {
	    e[nrt] = A[nrt][p-1];
	}
	e[p-1] = 0.0f;
	// If required, generate U.
	if (wantu) {
	    event.setDescription("Generating Matrix U\n");
	    eventValue++;
	    event.setIntValue(eventValue);
	    fireValueChanged(event);
	    for (int j = nct; j < nu; j++) {
		for (int i = 0; i < m; i++) {
		    U[i][j] = 0.0f;
		}
		U[j][j] = 1.0f;
	    }
	    for (int k = nct-1; k >= 0; k--) {
		if (s[k] != 0.0) {
		    for (int j = k+1; j < nu; j++) {
			float t = 0;
			for (int i = k; i < m; i++) {
			    t += U[i][k]*U[i][j];
			}
			t = -t/U[k][k];
			for (int i = k; i < m; i++) {
			    U[i][j] += t*U[i][k];
			}
		    }
		    for (int i = k; i < m; i++) {
			U[i][k] = -U[i][k];
		    }
		    U[k][k] = 1.0f + U[k][k];
		    for (int i = 0; i < k-1; i++) {
			U[i][k] = 0.0f;
		    }
		} else {
		    for (int i = 0; i < m; i++) {
			U[i][k] = 0.0f;
		    }
		    U[k][k] = 1.0f;
		}
	    }
	}
	// If required, generate V.
	if (wantv) {
	    event.setDescription("Generating Matrix V\n");
	    eventValue++;
	    event.setIntValue(eventValue);
	    fireValueChanged(event);
	    for (int k = n-1; k >= 0; k--) {
		if ((k < nrt) & (e[k] != 0.0)) {
		    for (int j = k+1; j < nu; j++) {
			float t = 0;
			for (int i = k+1; i < n; i++) {
			    t += V[i][k]*V[i][j];
			}
			t = -t/V[k+1][k];
			for (int i = k+1; i < n; i++) {
			    V[i][j] += t*V[i][k];
			}
		    }
		}
		for (int i = 0; i < n; i++) {
		    V[i][k] = 0.0f;
		}
		V[k][k] = 1.0f;
	    }
	}
	// Main iteration loop for the singular values.
	int pp = p-1;
	int iter = 0;
	float eps = (float)Math.pow(2.0,-52.0);
	
	event.setDescription("Main iteration loop started...\n");
	eventValue++;
	
	event.setIntValue(eventValue);
	fireValueChanged(event);
	counter=0;
	while (p > 0) {
	    counter++;
	    int k,kase;
	    if (counter==240) {
		if (stop) {
		    throw new AbortException();
		}
		event.setDescription("Main iteration loop.\n");
		eventValue++;
		event.setIntValue(eventValue);
		fireValueChanged(event);
		counter=0;
	    }
	    // Here is where a test for too many iterations would go.
	    // This section of the program inspects for
	    // negligible elements in the s and e arrays.  On
	    // completion the variables kase and k are set as follows.
	    // kase = 1     if s(p) and e[k-1] are negligible and k<p
	    // kase = 2     if s(k) is negligible and k<p
	    // kase = 3     if e[k-1] is negligible, k<p, and
	    //              s(k), ..., s(p) are not negligible (qr step).
	    // kase = 4     if e(p-1) is negligible (convergence).
	    for (k = p-2; k >= -1; k--) {
		if (k == -1) {
		    break;
		}
		if (Math.abs(e[k]) <= eps*(Math.abs(s[k]) + Math.abs(s[k+1]))) {
		    e[k] = 0.0f;
		    break;
		}
	    }
	    if (k == p-2) {
		kase = 4;
	    } else {
		int ks;
		for (ks = p-1; ks >= k; ks--) {
		    if (ks == k) {
			break;
		    }
		    float t = (float)((ks != p ? Math.abs(e[ks]) : 0.) +
		    (ks != k+1 ? Math.abs(e[ks-1]) : 0.));
		    if (Math.abs(s[ks]) <= eps*t) {
			s[ks] = 0.0f;
			break;
		    }
		}
		if (ks == k) {
		    kase = 3;
		} else if (ks == p-1) {
		    kase = 1;
		} else {
		    kase = 2;
		    k = ks;
		}
	    }
	    k++;
	    // Perform the task indicated by kase.
	    switch (kase) {
		// Deflate negligible s(p).
		case 1: {
		    float f = e[p-2];
		    e[p-2] = 0.0f;
		    for (int j = p-2; j >= k; j--) {
			float t = Maths.hypot(s[j],f);
			float cs = s[j]/t;
			float sn = f/t;
			s[j] = t;
			if (j != k) {
			    f = -sn*e[j-1];
			    e[j-1] = cs*e[j-1];
			}
			if (wantv) {
			    for (int i = 0; i < n; i++) {
				t = cs*V[i][j] + sn*V[i][p-1];
				V[i][p-1] = -sn*V[i][j] + cs*V[i][p-1];
				V[i][j] = t;
			    }
			}
		    }
		}
		break;
		// Split at negligible s(k).
		case 2: {
		    float f = e[k-1];
		    e[k-1] = 0.0f;
		    for (int j = k; j < p; j++) {
			float t = Maths.hypot(s[j],f);
			float cs = s[j]/t;
			float sn = f/t;
			s[j] = t;
			f = -sn*e[j];
			e[j] = cs*e[j];
			if (wantu) {
			    for (int i = 0; i < m; i++) {
				t = cs*U[i][j] + sn*U[i][k-1];
				U[i][k-1] = -sn*U[i][j] + cs*U[i][k-1];
				U[i][j] = t;
			    }
			}
		    }
		}
		break;
		// Perform one qr step.
		case 3: {
		    // Calculate the shift.
		    float scale = Math.max(Math.max(Math.max(Math.max(
		    Math.abs(s[p-1]),Math.abs(s[p-2])),Math.abs(e[p-2])),
		    Math.abs(s[k])),Math.abs(e[k]));
		    float sp = s[p-1]/scale;
		    float spm1 = s[p-2]/scale;
		    float epm1 = e[p-2]/scale;
		    float sk = s[k]/scale;
		    float ek = e[k]/scale;
		    float b = ((spm1 + sp)*(spm1 - sp) + epm1*epm1)/2.0f;
		    float c = (sp*epm1)*(sp*epm1);
		    float shift = 0.0f;
		    if ((b != 0.0) | (c != 0.0)) {
			shift = (float)Math.sqrt(b*b + c);
			if (b < 0.0) {
			    shift = -shift;
			}
			shift = c/(b + shift);
		    }
		    float f = (sk + sp)*(sk - sp) + shift;
		    float g = sk*ek;
		    // Chase zeros.
		    for (int j = k; j < p-1; j++) {
			float t = Maths.hypot(f,g);
			float cs = f/t;
			float sn = g/t;
			if (j != k) {
			    e[j-1] = t;
			}
			f = cs*s[j] + sn*e[j];
			e[j] = cs*e[j] - sn*s[j];
			g = sn*s[j+1];
			s[j+1] = cs*s[j+1];
			if (wantv) {
			    for (int i = 0; i < n; i++) {
				t = cs*V[i][j] + sn*V[i][j+1];
				V[i][j+1] = -sn*V[i][j] + cs*V[i][j+1];
				V[i][j] = t;
			    }
			}
			t = Maths.hypot(f,g);
			cs = f/t;
			sn = g/t;
			s[j] = t;
			f = cs*e[j] + sn*s[j+1];
			s[j+1] = -sn*e[j] + cs*s[j+1];
			g = sn*e[j+1];
			e[j+1] = cs*e[j+1];
			if (wantu && (j < m-1)) {
			    for (int i = 0; i < m; i++) {
				t = cs*U[i][j] + sn*U[i][j+1];
				U[i][j+1] = -sn*U[i][j] + cs*U[i][j+1];
				U[i][j] = t;
			    }
			}
		    }
		    e[p-2] = f;
		    iter = iter + 1;
		}
		break;
		// Convergence.
		case 4: {
		    // Make the singular values positive.
		    if (s[k] <= 0.0) {
			s[k] = (s[k] < 0.0 ? -s[k] : 0.0f);
			if (wantv) {
			    for (int i = 0; i <= pp; i++) {
				V[i][k] = -V[i][k];
			    }
			}
		    }
		    // Order the singular values.
		    while (k < pp) {
			if (s[k] >= s[k+1]) {
			    break;
			}
			float t = s[k];
			s[k] = s[k+1];
			s[k+1] = t;
			
			int Dummy = order[k];
			order[k] = order[k+1];
			order[k+1] = Dummy;
			
			if (wantv && (k < n-1)) {
			    for (int i = 0; i < n; i++) {
				t = V[i][k+1]; V[i][k+1] = V[i][k]; V[i][k] = t;
			    }
			}
			if (wantu && (k < m-1)) {
			    for (int i = 0; i < m; i++) {
				t = U[i][k+1]; U[i][k+1] = U[i][k]; U[i][k] = t;
			    }
			}
			k++;
		    }
		    iter = 0;
		    p--;
		}
		break;
	    }
	}
	
	event.setDescription("End SVD calculation.\n");
	eventValue++;
	event.setIntValue(eventValue);
	fireValueChanged(event);
	
	// T-matrix
	FloatMatrix T = new FloatMatrix(U, m, Math.min(m+1, n));
	// V-matrix
	FloatMatrix Vm = new FloatMatrix(V, n, n);
	// S-matrix
	FloatMatrix S = new FloatMatrix(n, n);
	float[][] array = S.getArray();
	for (int i = 0; i < n; i++) {
	    for (int j = 0; j < n; j++) {
		array[i][j] = 0.0f;
	    }
	    array[i][i] = s[i];
	}
	
	
	AlgorithmData result = new AlgorithmData();
	switch (mode) {
	    case 0:
		break;
	    case 1:
		result.addMatrix("U", An.times(T));
		break;
	    case 2:
		result.addMatrix("U", An.transpose().times(T));
		break;
	    case 3:
	    if (!useShortcut){
	    	result.addMatrix("U", An.transpose().times(T));
		    break;
	    }
	    	
		FloatMatrix Q = T.copy();
		FloatMatrix D = S.copy();
		final int dim = D.getRowDimension();
		for (int i=0;i<dim;i++) {
		    D.set(i,i,1.0f/(float)Math.sqrt(D.get(i,i)));
		}
		T = An.times(Q.times(D));
		result.addMatrix("U", An.transpose().times(T));
		break;
	    default:;
	}
	
	result.addMatrix("T", T);
	result.addMatrix("S", S);
	result.addMatrix("V", Vm);
	
	return result;
    }
    
	public void abort() {
		stop = true;
    }
    
    private FloatMatrix imputeKNearestMatrix(FloatMatrix inputMatrix, int k) throws AlgorithmException {
        int numRows = inputMatrix.getRowDimension();
        int numCols = inputMatrix.getColumnDimension();
        FloatMatrix resultMatrix = new FloatMatrix(numRows, numCols);
        
        AlgorithmEvent event = new AlgorithmEvent(this, AlgorithmEvent.SET_UNITS, numGenes);
        event.setDescription("Imputing missing values\n");
        fireValueChanged(event);
        event.setId(AlgorithmEvent.PROGRESS_VALUE);
        
        for (int i = 0; i < numRows; i++) {
            if (stop) {
                throw new AbortException();
            }
            //event.setIntValue(i);
            //event.setDescription("Imputing missing values: Current gene = " + (i+ 1));
            //fireValueChanged(event);
            
            if (isMissingValues(inputMatrix, i)) {
                //System.out.println("gene " + i + " is missing values");
                Vector nonMissingExpts = new Vector();
                for (int j = 0; j < numCols; j++) {
                    if (!Float.isNaN(inputMatrix.A[i][j])) {
                        nonMissingExpts.add(new Integer(j));
                    }
                }
                Vector geneSubset = getValidGenes(i, inputMatrix, nonMissingExpts); //getValidGenes() returns a Vector of genes that have valid values for all the non-missing expts
                
                //System.out.println(" Valid geneSubset.size() = " + geneSubset.size());
                
                /*
                for (int j = 0; j < geneSubset.size(); j++) {
                    System.out.println(((Integer)geneSubset.get(j)).intValue());
                }
                 */
                //System.out.println("imputing KNN: current gene = " + i);
                Vector kNearestGenes = getKNearestGenes(i, k, inputMatrix, geneSubset, nonMissingExpts);
                
                /*
                System.out.println("k nearest genes of gene " + i + " : ");
                 
                for (int j = 0; j < kNearestGenes.size(); j++) {
                    System.out.println("" + ((Integer)kNearestGenes.get(j)).intValue());
                }
                 */
                
                //TESTED UPTO HERE -- 12/18/2002***********
                //
                /*
                System.out.print("Gene " + i + " :\t");
                for (int j = 0; j < numCols; j++) {
                    System.out.print("" +inputMatrix.A[i][j]);
                    System.out.print("\t");
                }
                System.out.println();
                System.out.println("Matrix of k Nearest Genes");
                printSubMatrix(kNearestGenes, inputMatrix);
                 */    //
                for (int j = 0; j < numCols; j++) {
                    if (!Float.isNaN(inputMatrix.A[i][j])) {
                        resultMatrix.A[i][j] = inputMatrix.A[i][j];
                    } else {
                        
                        //System.out.println("just before entering getExptMean(): kNearestGenes.size() = " + kNearestGenes.size());
                        
                        //resultMatrix.A[i][j] = getExptMean(j, kNearestGenes, inputMatrix);
                        resultMatrix.A[i][j] = getExptWeightedMean(i, j, kNearestGenes, inputMatrix);
                    }
                }
                //DONE UPTO HERE
            }
            
            else {
                for (int j = 0; j < numCols; j++) {
                    resultMatrix.A[i][j] = inputMatrix.A[i][j];
                }
            }
        }
        
        //Raktim Center the data here by mean or median
        resultMatrix = centerData(resultMatrix);
    	
        return imputeRowAverageMatrix(resultMatrix);
    }   
    
    private FloatMatrix imputeRowAverageMatrix(FloatMatrix inputMatrix) throws AlgorithmException {
        int numRows = inputMatrix.getRowDimension();
        int numCols = inputMatrix.getColumnDimension();
        FloatMatrix resultMatrix = new FloatMatrix(numRows, numCols);
        
        AlgorithmEvent event = new AlgorithmEvent(this, AlgorithmEvent.SET_UNITS, numGenes);
        fireValueChanged(event);
        event.setId(AlgorithmEvent.PROGRESS_VALUE);
        
        for (int i = 0; i < numRows; i++) {
            
            if (stop) {
                throw new AbortException();
            }
            //event.setIntValue(i);
            //event.setDescription("Imputing missing values: Current gene = " + (i+ 1));
            //fireValueChanged(event);
            
            float[] currentRow = new float[numCols];
            float[] currentOrigRow = new float[numCols];
            for (int j = 0; j < numCols; j++) {
                currentRow[j] = inputMatrix.A[i][j];
                currentOrigRow[j] = inputMatrix.A[i][j];
            }
            for (int k = 0; k < numCols; k++) {
                if (Float.isNaN(inputMatrix.A[i][k])) {
                    currentRow[k] = getMean(currentOrigRow);
                }
            }
            
            for (int l = 0; l < numCols; l++) {
                resultMatrix.A[i][l] = currentRow[l];
            }
        }
        
        return resultMatrix;
    }    
    
    private boolean isMissingValues(FloatMatrix mat, int row) {//returns true if the row of the matrix has any NaN values
        
        for (int i = 0; i < mat.getColumnDimension(); i++) {
            if (Float.isNaN(mat.A[row][i])) {
                return true;
            }
        }
        
        return false;
    }   
    
    private Vector getValidGenes(int gene, FloatMatrix mat, Vector validExpts) { //returns the indices of those genes in "mat" that have valid values for all the validExpts
        Vector validGenes = new Vector();
        
        for (int i = 0; i < mat.getRowDimension(); i++) {
            if ((hasAllExpts(i, mat, validExpts)) && (gene != i)){//returns true if gene i in "mat" has valid values for all the validExpts
                validGenes.add(new Integer(i));
            }
        }
        
        if (validGenes.size() < numNeighbors) { // if the number of valid genes is < k, other genes will be added to validGenes in increasing order of Euclidean distance until validGenes.size() = k
            int additionalGenesNeeded = numNeighbors - validGenes.size();
            Vector additionalGenes = getAdditionalGenes(gene, additionalGenesNeeded, validGenes, mat);
            for (int i = 0; i < additionalGenes.size(); i++) {
                validGenes.add(additionalGenes.get(i));
            }
        }
        
        return validGenes;
    } 
    
    private Vector getAdditionalGenes(int currentGene, int numGenesNeeded, Vector alreadyPresentGenes, FloatMatrix mat) {
        Vector additionalGenes = new Vector();
        Vector allGenes = new Vector();
        Vector geneDistances = new Vector();
        
        for (int i = 0; i < mat.getRowDimension(); i++) {
            if (i != currentGene) {
                float currentDistance = ExperimentUtil.geneEuclidianDistance(mat, null, i, currentGene, factor);
                geneDistances.add(new Float(currentDistance));
                allGenes.add(new Integer(i));
            }
        }
        
        float[] geneDistancesArray = new float[geneDistances.size()];
        for (int i = 0; i < geneDistances.size(); i++) {
            float currentDist = ((Float)geneDistances.get(i)).floatValue();
            geneDistancesArray[i] = currentDist;
        }
        
        QSort sortGeneDistances = new QSort(geneDistancesArray);
        float[] sortedDistances = sortGeneDistances.getSorted();
        int[] sortedDistanceIndices = sortGeneDistances.getOrigIndx();
        
        int counter = 0;
        
        for (int i = 0; i < sortedDistanceIndices.length; i++) {
            int currentIndex = sortedDistanceIndices[i];
            int currentNearestGene = ((Integer)allGenes.get(currentIndex)).intValue();
            if (belongsIn(alreadyPresentGenes, currentNearestGene)) {
                continue;
            } else {
                additionalGenes.add(new Integer(currentNearestGene));
                counter++;
                if (counter >= numGenesNeeded) {
                    break;
                }
            }
        }
        
        return additionalGenes;
    }   
    
    Vector getKNearestGenes(int gene, int k, FloatMatrix mat, Vector geneSubset, Vector nonMissingExpts) {
        Vector allValidGenes = new Vector();
        Vector nearestGenes = new Vector();
        Vector geneDistances = new Vector();
        for (int i = 0; i < geneSubset.size(); i++) {
            int currentGene = ((Integer)geneSubset.get(i)).intValue();
            if (gene != currentGene) {
                float currentDistance = ExperimentUtil.geneEuclidianDistance(mat, null, gene, currentGene, factor);
                //System.out.println("Current distance = " + currentDistance);
                geneDistances.add(new Float(currentDistance));
                allValidGenes.add(new Integer(currentGene));
            }
        }
        
        float[] geneDistancesArray = new float[geneDistances.size()];
        for (int i = 0; i < geneDistances.size(); i++) {
            float currentDist = ((Float)geneDistances.get(i)).floatValue();
            geneDistancesArray[i] = currentDist;
        }
        
        QSort sortGeneDistances = new QSort(geneDistancesArray);
        float[] sortedDistances = sortGeneDistances.getSorted();
        int[] sortedDistanceIndices = sortGeneDistances.getOrigIndx();
        
        for (int i = 0; i < k; i++) {
            int currentGeneIndex = sortedDistanceIndices[i];
            int currentNearestGene = ((Integer)allValidGenes.get(currentGeneIndex)).intValue();
            nearestGenes.add(new Integer(currentNearestGene));
        }
        
        return nearestGenes;
    } 
    
    private float getExptWeightedMean(int gene, int expt, Vector geneVector, FloatMatrix mat) {
        float weightedMean = 0.0f;
        int validN = 0;
        float numerator = 0.0f;
        float recipNeighborDistances[] = new float[geneVector.size()];
        for (int i = 0; i < recipNeighborDistances.length; i++) {
            int currentGene = ((Integer)geneVector.get(i)).intValue();
            if (!Float.isNaN(mat.A[currentGene][expt])) {
                float distance = ExperimentUtil.geneEuclidianDistance(mat, null, gene, currentGene, factor);
                if (distance == 0.0f) {
                    distance = Float.MIN_VALUE;
                }
                recipNeighborDistances[i] = (float)(1.0f/distance);
                numerator = numerator + (float)(recipNeighborDistances[i]*mat.A[currentGene][expt]);
                validN++;
            } else {
                recipNeighborDistances[i] = 0.0f;
            }
            
        }
        
        float denominator = 0.0f;
        for (int i = 0; i < recipNeighborDistances.length; i++) {
            denominator = denominator + recipNeighborDistances[i];
        }
        
        weightedMean = (float)(numerator/(float)denominator);
        return weightedMean;
    }   
    
    private float getMean(float[] row) {
        float mean = 0.0f;
        int validN = 0;
        
        for (int i = 0; i < row.length; i++) {
            if (!Float.isNaN(row[i])) {
                mean = mean + row[i];
                validN++;
            }
        }
        
        if (validN == 0) {
            validN = 1; // if the whole row is NaN, it will be set to zero;
        }
        
        mean = (float)(mean / validN);
        
        return mean;
    } 
    
    private boolean hasAllExpts(int gene, FloatMatrix mat, Vector validExpts) {//returns true if "gene" in "mat" has valid values for all the validExpts
        
        for (int i = 0; i < validExpts.size(); i++) {
            int expIndex = ((Integer)validExpts.get(i)).intValue();
            if (Float.isNaN(mat.A[gene][expIndex])) {
                return false;
            }
        }
        
        return true;
    }
    
    private boolean belongsIn(Vector geneVector, int gene) {
        for (int i = 0; i < geneVector.size(); i++) {
            int currentGene = ((Integer)geneVector.get(i)).intValue();
            if (gene == currentGene) {
                return true;
            }
        }
        
        return false;
    }    
    
    /**
     * Raktim to center float matrix
     * @param expMatrix
     * @param center
     * @return
     */
    private FloatMatrix centerData(FloatMatrix expMatrix) {
    	if(center == 3) // No Centering
    		return expMatrix;
    	else {
    		if(mode == 3 ){ // Cluster Samples, Center Genes
    			int rowDim = expMatrix.getRowDimension();
        		int colDim = expMatrix.getColumnDimension();
        		
    			for(int i = 0; i < rowDim; i++){
    				float centerBy;
    	    		if(center == 1) centerBy = computeMedian(expMatrix.A[i]);
    	    		else centerBy = computeMean(expMatrix.A[i]);
    	    		for(int k = 0; k < colDim; k++){
    	    			expMatrix.A[i][k] -= centerBy;
    	    		}
    			}
    		} 
    		
    		if (mode == 1){ // Cluster Genes, Center Samples
    			expMatrix = expMatrix.transpose();
    			int rowDim = expMatrix.getRowDimension();
        		int colDim = expMatrix.getColumnDimension();
        		
    			for(int i = 0; i < rowDim; i++){
    				float centerBy;
    				if(center == 1) centerBy = computeMedian(expMatrix.A[i]);
    				else centerBy = computeMean(expMatrix.A[i]);
    				for(int k = 0; k < colDim; k++){
    	    			expMatrix.A[i][k] -= centerBy;
    	    		}
    			}
    			expMatrix = expMatrix.transpose();
    		}
    	}
		return expMatrix;
	}

    /**
     * Computes the average value of the doubles in array
     * 
     * @param array
     * @return
     */
    private float computeMean(float[] array) {
    	float toReturn = 0;

        for( int i = 0; i < array.length; i++ ) {
            toReturn = toReturn + array[i];
        }

        return toReturn / ( float ) array.length;
    }//end computeMean()


    /**
     * Finds or computes the median value in array
     * 
     * @param array
     * @return
     */
    private float computeMedian(float[] array) {
        //create a new array so we don't mess with the order of the original
    	float[] copy = new float[array.length];
        for( int i = 0; i < array.length; i++ ) {
            copy[i] = array[i];
        }

        //sort the array first
        Arrays.sort(copy);

        //find the middle value
        int half = copy.length / 2;
        int remainder = copy.length % 2;
        if( remainder == 0 ) {
            //even number, use mean of 2 middle values
            return copy[half];
            //return ( copy[half - 1] + copy[half] ) / 2;
        } else {
            //odd number, use middle value
            return copy[half];
        }
    }//end findMedian()

}
