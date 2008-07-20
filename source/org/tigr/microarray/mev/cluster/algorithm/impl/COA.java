/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * COA.java
 *
 * Created on September 15, 2004, 3:30 PM
 */

package org.tigr.microarray.mev.cluster.algorithm.impl;

import java.util.Vector;

import org.tigr.microarray.mev.cluster.algorithm.AbortException;
import org.tigr.microarray.mev.cluster.algorithm.AbstractAlgorithm;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmData;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmEvent;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmException;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmParameters;
import org.tigr.util.FloatMatrix;
import org.tigr.util.QSort;

import Jama.Matrix;
import Jama.SingularValueDecomposition;

/**
 *
 * @author  nbhagaba
 */
public class COA extends AbstractAlgorithm {
    
    private boolean stop = false;    
    
    public FloatMatrix Org;     // Backup of original input matrix.
    private int numNeighbors;
    private float factor = 1.0f;
    public Matrix N;            // Input matrix as double values.
    public double[] R;          // Row sums.
    public double[] C;          // Column sums.
    public double N_Sum = 0;    // Sum of all values in N.
    public double[][] P;        // Matrix of N[i,j]/N_Sum values.
    public Matrix X;            // Matrix of (P[i,j]-(Ri*Cj))/sqrt(Ri*Cj) values.
    public Matrix Xt;           // Transpose of X.
    public Matrix DcRoot;           // Diagonal matrix of C.
    public Matrix Dr;           // Diagonal matrix of R.
    public Matrix B;            // Intermediate matrix of the algorithm.
    public Matrix geneUMatrix;  // Coordinates of genes.
    public Matrix exptUMatrix;  // Coordinates of experiments.
    public FloatMatrix gene;    // Coordinates of genes as FloatMatrix.
    public FloatMatrix expt;    // Coordinates of experiments as FloatMatrix.
    public double[] G_Sums;
    public Matrix G;
    
    private int numGenes, numExps;    
        
    
    /** Creates a new instance of COA */
    public COA() {
    }
    
    /** This method should interrupt the calculation.
     */
    public void abort() {
        stop = true;        
    }
    
    /** This method execute calculation and return result,
     * stored in <code>AlgorithmData</code> class.
     *
     * @param data the data to be calculated.
     */
    public AlgorithmData execute(AlgorithmData data) throws AlgorithmException {
	AlgorithmParameters map = data.getParams();
	numNeighbors = map.getInt("numNeighbors", 10);        
        
        Org = data.getMatrix("experiment");
	numGenes = this.Org.getRowDimension();
	numExps = this.Org.getColumnDimension(); 
        AlgorithmData result = Analysis(Org);
        return result;        
    }
    
    /* Turns A into a matrix with A values on the diagonal and 0 everywhere else. */
    public Matrix diagonal (double[] A) {
        System.out.println("Entered diagonal method");
        Matrix D = new Matrix(A.length,A.length);
        System.out.println("Created diagonal matrix");
        for (int i = 0; i < D.getRowDimension(); i++) {
            for (int j = 0; j < D.getColumnDimension(); j++) {
                D.set(i, j, 0d);
            }
        }        
        for (int i=0; i<D.getRowDimension(); i++) {
            D.set(i,i,Math.sqrt(A[i]));
        }
        
        return D;
    }    
    
    public Matrix diagonalRoot(double[] A) {
        //System.out.println("Entered Diagonal Root method");
        Matrix D = new Matrix(A.length,A.length);
        //System.out.println("Created Diagonal Root Matrix");
        
        for (int i = 0; i < D.getRowDimension(); i++) {
            for (int j = 0; j < D.getColumnDimension(); j++) {
                D.set(i, j, 0d);
            }
        }          
        
        for (int i=0; i<D.getRowDimension(); i++) {
            D.set(i,i,A[i]);
        }
        
        return D;        
    }
    
    /* Determines the average of all values in p */
    public double mean(double[] p) {
        double sum = 0;  // sum of all the elements
        
        for (int i=0; i<p.length; i++) {
            sum += p[i];
        }
        
        return (sum/p.length);
    }
    
    private FloatMatrix imputeKNearestMatrix(FloatMatrix inputMatrix, int k) throws AlgorithmException {
        int numRows = inputMatrix.getRowDimension();
        int numCols = inputMatrix.getColumnDimension();
        FloatMatrix resultMatrix = new FloatMatrix(numRows, numCols);
        
        AlgorithmEvent event = new AlgorithmEvent(this, AlgorithmEvent.SET_UNITS, numGenes);
        event.setDescription("Imputing missing values");
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
    
    // New.
    public void groups(int[][] selections, int numGroups) {       
        G = new Matrix(N.getRowDimension(),numGroups);
        G_Sums = new double[numGroups];
        
        for (int i=0; i<numGroups; i++) {
            // Find all values of G.
            double[][] sub = (N.getMatrix(0, N.getRowDimension()-1, selections[i])).getArrayCopy();
            for (int j=0; j<G.getRowDimension(); j++) {
                G.set(j,i,mean(sub[j]));
            }
        
            // Find all values of G_Sums[i].
            for (int j=0; j<selections[i].length; j++) {
                G_Sums[i] += C[selections[i][j]];
            }
        }
        
        System.out.println("G:");
        for (int i=0; i<G.getRowDimension(); i++) {
            for (int j=0; j<G.getColumnDimension(); j++) {
                System.out.println("i: " + i + " , j: " + j + " , " + G.get(i,j));
            }
        }
        System.out.println();
        
        System.out.println("G_Sums:");
        for (int i=0; i<G_Sums.length; i++) {
            System.out.println("i: " + i + " , " + G_Sums[i]);
        }
        System.out.println();
    }
    
    
    public AlgorithmData Analysis(FloatMatrix input) throws AlgorithmException {
        
        input = imputeKNearestMatrix(input, numNeighbors); 
        
        // Prepare N.
        N = new Matrix(input.getRowDimension(),input.getColumnDimension());        
     
        boolean negFound = false;
        
        for (int i=0; i<input.getRowDimension(); i++) {
            if (negFound) break;
            for (int j=0; j<input.getColumnDimension(); j++) {
                if (input.get(i,j) < 0f) {
                    negFound = true;
                    break;
                }
            }
            //if (negFound) break;
        }
        
        if (negFound) {
            float min = Float.POSITIVE_INFINITY;
            for (int i=0; i<input.getRowDimension(); i++) {
                for (int j=0; j<input.getColumnDimension(); j++) {
                    min = Math.min(min,input.get(i,j));
                }
            }   
            
            //System.out.println("min = " + min);
            double additionFactor = (-1d)*min;
            //Make the matrix have all non-negative values.
            for (int i=0; i<input.getRowDimension(); i++) {
                for (int j=0; j<input.getColumnDimension(); j++) {
                    N.set(i,j, (double)(input.get(i,j) + additionFactor));
                }
            } 
            
        } else {
            for (int i=0; i<input.getRowDimension(); i++) {
                for (int j=0; j<input.getColumnDimension(); j++) {
                    N.set(i,j,(double)input.get(i,j));
                }
            }           
        }
        
        //System.out.println("finished making matrix positive");
        
        // Find the minimum value in the input matrix.
        /*
        float min = (float)(0);
        for (int i=0; i<input.getRowDimension(); i++) {
            for (int j=0; j<input.getColumnDimension(); j++) {
                min = Math.min(min,input.get(i,j));
            }
        }
         */
        
        // Subtract the minimum value from all values in the input matrix
        // (Add, instead of subtract, if minimum value is negative) and place
        // the value, as a double, in N.
        /*
        for (int i=0; i<input.getRowDimension(); i++) {
            for (int j=0; j<input.getColumnDimension(); j++) {
                N.set(i,j,(double)input.get(i,j)-min);
            }
        }
         */
        
      
        
        // If N has more columns than rows, transpose it.
        /*
        if (N.getColumnDimension() > N.getRowDimension()) {
            N = N.transpose();
        }
        */
        // Determine N_Sum
        for (int i=0; i<N.getRowDimension(); i++) {
            for (int j=0; j<N.getColumnDimension(); j++) {
                N_Sum += N.get(i,j);
            }
        }
        //System.out.println("N_sum = " + N_Sum);
        // Find values of R
        R = new double[N.getRowDimension()];
        for (int i=0; i<N.getRowDimension(); i++) {
            double N_iSum = 0;
            
            for (int x=0; x<N.getColumnDimension(); x++) {
                N_iSum += N.get(i,x);
            }
            
            R[i] = N_iSum/N_Sum;
        }
        
        // Find values of C
        C = new double[N.getColumnDimension()];
        for (int j=0; j<N.getColumnDimension(); j++) {
            double N_jSum = 0;
            
            for (int x=0; x<N.getRowDimension(); x++) {
                N_jSum += N.get(x,j);
            }
            
            C[j] = N_jSum/N_Sum;
        }
        
        // Find values of P & X
        P = new double[N.getRowDimension()][N.getColumnDimension()];
        X = new Matrix(N.getRowDimension(),N.getColumnDimension());
        for (int i=0; i<N.getRowDimension(); i++) {
            for (int j=0; j<N.getColumnDimension(); j++) {
                P[i][j] = N.get(i,j)/N_Sum;
                double xValue = (P[i][j] - (R[i]*C[j])) / Math.sqrt(R[i]*C[j]);
                X.set(i,j,xValue);
            }
        }
        
        SingularValueDecomposition svd = new SingularValueDecomposition(X);
        
        Matrix U = svd.getU();
        Matrix Lambda = svd.getS();
        Matrix V = svd.getV();
        
        geneUMatrix = U.times(Lambda);
        exptUMatrix = V.times(Lambda);
        
        for (int i = 0; i < geneUMatrix.getRowDimension(); i++) {
            for (int j = 0; j < geneUMatrix.getColumnDimension(); j++) {
                double currVal = geneUMatrix.get(i,j);
                geneUMatrix.set(i, j, currVal/Math.sqrt(R[i]));
            }
        }
        
        for (int i = 0; i < exptUMatrix.getRowDimension(); i++) {
            for (int j = 0; j < exptUMatrix.getColumnDimension(); j++) {
                double currVal = exptUMatrix.get(i,j);
                exptUMatrix.set(i, j, currVal/Math.sqrt(C[i]));
            }
        }
        
        //System.out.println("Finished computing R, C and X");
        
        // Determine Xt; Xt = transpose of X
        /*
        System.out.println("before Xt");
        
        Xt = X.transpose();
        
        System.out.println("Computed Xt");
        
        // Determine Dc; Dc = diagonal of C
        DcRoot = diagonalRoot(C);
        
        System.out.println("Computed DcRoot");
        // Determine Dr; Dr = diagonal of R
        Dr = diagonal(R);
        
        System.out.println("Computed Dr, before computing B");
        
        // Determine B; B = Dc*X*Dr*Xt*Dc
        B = (((DcRoot.times(Xt)).times(Dr)).times(X)).times(DcRoot);
        
        System.out.println("Finished computing B");
        
        // Get the Singular Value Decomposition of B
        SingularValueDecomposition mySVD = new SingularValueDecomposition(B);
        
        System.out.println("Finished SVD");
        
        Matrix T  = mySVD.getU();
        Matrix S = mySVD.getS();
        Matrix V = mySVD.getV();
        geneUMatrix = X.times(T);
        
        Matrix Q = mySVD.getU();
        Matrix D = mySVD.getS();
        //Matrix S= mySVD.getS();
        //Matrix V = mySVD.getV();
        for (int i=0;i<D.getRowDimension();i++) {
            D.set(i,i,1.0f/(float)Math.sqrt(D.get(i,i)));
        }
        T = X.times(Q.times(D));
        exptUMatrix = X.transpose().times(T);
        */
        
        // Fill gene will geneUMatrix's values as floats.
        gene = new FloatMatrix(geneUMatrix.getRowDimension(),geneUMatrix.getColumnDimension());
        for (int i=0; i<gene.getRowDimension(); i++) {
            for (int j=0; j<gene.getColumnDimension(); j++) {
                gene.set(i,j,(float)geneUMatrix.get(i,j));
            }
        }
        
        // Fill expt will exptUMatrix's values as floats.
        expt = new FloatMatrix(exptUMatrix.getRowDimension(),exptUMatrix.getColumnDimension());
        for (int i=0; i<expt.getRowDimension(); i++) {
            for (int j=0; j<expt.getColumnDimension(); j++) {
                expt.set(i,j,(float)exptUMatrix.get(i,j));
            }
        }
        
        FloatMatrix lambdaValues = new FloatMatrix(Lambda.getRowDimension(), 1); 
        
        for (int i = 0; i < Lambda.getRowDimension(); i++) {
            lambdaValues.set(i, 0, (float)(Lambda.get(i, i)));
        }
        
        AlgorithmData result = new AlgorithmData();
        result.addMatrix("gene",gene);
        result.addMatrix("expt",expt);
        result.addMatrix("lambdaValues", lambdaValues);
        
        //System.out.println("gene: rowDim = " + gene.getRowDimension() + ", colDim = " + gene.getColumnDimension());
        //System.out.println("expt: rowDim = " + expt.getRowDimension() + ", colDim = " + expt.getColumnDimension());
        
        return result;
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
    
}

