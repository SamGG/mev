/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: SVM.java,v $
 * $Revision: 1.1.1.2 $
 * $Date: 2004-02-06 21:48:18 $
 * $Author: braisted $
 * $State: Exp $
 */

package org.tigr.microarray.mev.cluster.algorithm.impl;

import java.util.Random;
import java.util.Arrays;
import java.util.ArrayList;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.tigr.util.FloatMatrix;
import org.tigr.util.ConfMap;

import org.tigr.microarray.mev.cluster.Node;
import org.tigr.microarray.mev.cluster.Cluster;
import org.tigr.microarray.mev.cluster.NodeList;
import org.tigr.microarray.mev.cluster.NodeValue;
import org.tigr.microarray.mev.cluster.NodeValueList;

import org.tigr.microarray.mev.cluster.algorithm.AbstractAlgorithm;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmData;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmParameters;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmException;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmEvent;
import org.tigr.microarray.mev.cluster.algorithm.AbortException;

/** The SVM class provides the execution code for running
 * the SVM algorithm and returning results.
 */
public class SVM extends AbstractAlgorithm {
    
    private static final int POSITIVE_DIAGONAL = 0;
    private static final int NEGATIVE_DIAGONAL = 1;
    
    private boolean stop = false;
    
    private int function;
    private float factor;
    private boolean absolute;
    
    private int number_of_genes;
    private int number_of_samples;
    private boolean svmGenes = true;
    
    private FloatMatrix expMatrix;
    private boolean seenUnderflow = false;
    private float prevObjective;
    
    //HCL parameters
    private boolean calcHCL = false;
    private boolean calcGeneHCL = false;
    private boolean calcSampleHCL = false;
    private int method = 0;
    
    //Indicates genes vs experiment clustering
    private boolean classifyGens = true;
    
    /**
     * Executes and returns results of SVM algorithm.
     * @param data holds data and initial parameters
     * @throws AlgorithmException
     * @return SVM result
     */
    public AlgorithmData execute(AlgorithmData data) throws AlgorithmException {
        
        AlgorithmParameters map = data.getParams();
        
        function = map.getInt("distance-function", EUCLIDEAN);  //applies only to hcl trees on final classes, svm uses dot prod. on normalized vectors
        factor   = map.getFloat("distance-factor", 1.0f);
        absolute = map.getBoolean("distance-absolute", false);
        
        this.expMatrix = data.getMatrix("experiment");
        
        number_of_genes   = this.expMatrix.getRowDimension();
        number_of_samples = this.expMatrix.getColumnDimension();
        
        svmGenes = map.getBoolean("classify-genes", true);
        float constant = map.getFloat("constant", 0);
        float coefficient = map.getFloat("coefficient", 0);
        float power = map.getFloat("power", 0);
        
        this.calcHCL = map.getBoolean("calculate-hcl", false);
        this.calcGeneHCL = map.getBoolean("calculate-genes-hcl", false);
        this.calcSampleHCL = map.getBoolean("calculate-samples-hcl", false);
        
        this.method = map.getInt("linkage-method", 0);
        
        AlgorithmData result = new AlgorithmData();
        
        boolean isClassify = map.getBoolean("is-classify", true);
        if (isClassify) {
            FloatMatrix trainingMatrix = data.getMatrix("training");
            FloatMatrix weightsMatrix = data.getMatrix("weights");
            float[] weights = weightsMatrix.getColumnPackedCopy();
            FloatMatrix discriminantMatrix = classify(trainingMatrix, weights, coefficient, constant, power);
            
            int [][] clusters = new int[2][];
            clusters[0] = getPositives(discriminantMatrix);
            clusters[1] = getNegatives(discriminantMatrix);
            
            if(calcHCL){
                //preparation for HCL
                Cluster result_cluster = new Cluster();
                NodeList nodeList = result_cluster.getNodeList();
                int[] features;
                for (int i=0; i<clusters.length; i++) {
                    if (stop) {
                        throw new AbortException();
                    }
                    features = clusters[i];
                    Node node = new Node(features);
                    nodeList.addNode(node);
                    node.setValues(calculateHierarchicalTree(features, method, calcGeneHCL, calcSampleHCL));
                }
                result.addCluster("cluster", result_cluster);
            }
            
            result.addMatrix("discriminant", discriminantMatrix);
            result.addIntArray("positives", getPositives(discriminantMatrix));
            result.addIntArray("negatives", getNegatives(discriminantMatrix));
            FloatMatrix means = getMeans(discriminantMatrix);
            result.addMatrix("means", means);
            result.addMatrix("variances", getVariance(discriminantMatrix, means));
        } else {
            int[] classes = data.getIntArray("classes");
            int seed = map.getInt("seed", 0);
            boolean normalize = map.getBoolean("normalize", false);
            boolean radial = map.getBoolean("radial", false);
            float widthFactor = map.getFloat("width-factor", 1.0f);
            float positiveDiagonal = map.getFloat("positive-diagonal", 0.0f);
            float negativeDiagonal = map.getFloat("negative-diagonal", 0.0f);
            float diagonalFactor = map.getFloat("diagonal-factor", 0.0f);
            float positiveConstraint = map.getFloat("positive-constraint", 1.0f);
            float negativeConstraint = map.getFloat("negative-constraint", 1.0f);
            float convergenceThreshold = map.getFloat("convergence-threshold", 0.00001f);
            boolean constrainWeights = map.getBoolean("constrain-weights", true);
            float[] weights = train(expMatrix, classes, seed, normalize, radial, coefficient, constant, power, widthFactor, positiveDiagonal, negativeDiagonal, diagonalFactor, positiveConstraint, negativeConstraint, convergenceThreshold, constrainWeights);
            result.addMatrix("weights", new FloatMatrix(weights, 1));
        }
        return result;
    }
    
    /**
     * Aborts current SVM in progress.
     */
    public void abort() {
        stop = true;
    }
    
    /**
     * Sets underflow boolean
     * @param seenUnderflow boolean underflow value
     */
    private final void setSeenUnderflow(boolean seenUnderflow) {
        this.seenUnderflow = seenUnderflow;
    }
    
    /**
     * Returns underflow state.
     */
    private final boolean isSeenUnderflow() {
        return seenUnderflow;
    }
    
    /**
     * Creates a base kernal matrix. (Deprecated)
     * @param trainingMatrix
     * @return
     */
    private FloatMatrix computeBaseKernelMatrix(FloatMatrix trainingMatrix) {
        FloatMatrix kernelMatrix = new FloatMatrix(number_of_genes, number_of_genes);
        
        for (int row1 = 0; row1 < expMatrix.getRowDimension(); row1++) {
            for (int row2 = 0; row2 < trainingMatrix.getRowDimension(); row2++) {
                kernelMatrix.set(row1, row2, ExperimentUtil.geneDistance(expMatrix, trainingMatrix, row1, row2, function, (float)1.0, false));
            }
        }
        return kernelMatrix;
    }
    
    
    /**
     * Creates a base kernal matrix with normalized expression
     * vectors using the dot product metric.
     * @param trainingMatrix
     * @return
     */
    private FloatMatrix computeNormalizedBaseKernelMatrix(FloatMatrix trainingMatrix){
        FloatMatrix normTrainingMatrix = new FloatMatrix(this.number_of_genes, this.number_of_samples);        
        FloatMatrix kernelMatrix = new FloatMatrix(number_of_genes, number_of_genes);
        float value;
        float sumOfSquares = 0;
        for( int row = 0; row < this.number_of_genes; row++){
            sumOfSquares = 0;
            for(int col = 0; col < this.number_of_samples; col++){
                value = trainingMatrix.get(row,col);
                if( !Float.isNaN(value) )
                    sumOfSquares += Math.pow(value, 2);
            }
            if(sumOfSquares != 0.0){
                sumOfSquares = (float) Math.sqrt(sumOfSquares);
                for(int col = 0; col < this.number_of_samples; col++){
                    normTrainingMatrix.set( row, col, trainingMatrix.get(row, col)/sumOfSquares);
                }
            }
        }
        int N1 = expMatrix.getRowDimension();
        int N2 = trainingMatrix.getRowDimension();
        float kernalValue;
        for (int row1 = 0; row1 < N1; row1++) {
            for (int row2 = row1; row2 < N2; row2++) {
                kernalValue = geneDotProduct(normTrainingMatrix, normTrainingMatrix, row1, row2);
                kernelMatrix.set(row1, row2, kernalValue);
                kernelMatrix.set(row2, row1, kernalValue);
            }
        }        
        return kernelMatrix;
    }
    
    
    /**
     * Creates
     * @param kernelMatrix
     * @return
     */
    private float[] createSelfKernelValues(FloatMatrix kernelMatrix) {
        float[] selfKernelValues = new float[kernelMatrix.getRowDimension()];
        extractSelfKernelValues(kernelMatrix, selfKernelValues);
        return selfKernelValues;
    }
    
    /**
     * Extract the diagonal from a given (square) kernel matrix.
     */
    private void extractSelfKernelValues(FloatMatrix kernelMatrix, float[] selfKernelValues) {
        final int size = kernelMatrix.getRowDimension();
        for (int i=0; i<size; i++) {
            selfKernelValues[i] = kernelMatrix.get(i, i);
        }
    }
    
    /**
     * Given three parameters, A, B and C, compute (B(X + C))^A.
     */
    private final float polynomialize(float power, float coefficient, float constant, float value) {
        value += constant;
        value *= coefficient;
        return(float)Math.pow(value, power);
    }
    
    /**
     * Given three parameters, A, B and C, replace each element X in a
     * given matrix by the value (B(X + C))^A.  Also perform the same
     * operation on two given arrays.
     */
    private void polynomializeMatrix(FloatMatrix kernelMatrix, float[] selfKernelValues, float power, float coefficient, float constant) {
        final int rows = kernelMatrix.getRowDimension();
        final int columns = kernelMatrix.getColumnDimension();
        for (int row=0; row < rows; row++) {
            for (int column=0; column < columns; column++) {
                kernelMatrix.set(row, column, polynomialize(power, coefficient, constant, kernelMatrix.get(row, column)));                
            }
        }
        /* Polynomialize the self-kernel values. */
        for (int row=0; row < rows; row++) {
            selfKernelValues[row] = polynomialize(power, coefficient, constant, selfKernelValues[row]);
        }
    }
    
    /**
     * Classify a single example.
     */
    private final float classify(FloatMatrix kernelMatrix, float[] weights, int test) {
        float returnValue;
        float thisWeight;
        float thisValue;
        
        returnValue = 0.0f;
        for (int i = 0; i<weights.length; i++) {
            /* Get the current weight. */
            thisWeight = weights[i];
            /* If the weight is zero, skip. */
            if (thisWeight == 0.0) {
                continue;
            }
            /* Compute the distance between the two examples. */
            thisValue = kernelMatrix.get(test, i);
            /* Weight the distance appropriately. This assumes that the
               classification of the training set example is encoded in the
               sign of the weight. */
            thisValue *= thisWeight;
            returnValue += thisValue;
        }
        return returnValue;
    }
    
    /**
     * Classify a list of examples.
     */
    private FloatMatrix classifyList(FloatMatrix kernelMatrix, float[] weights) {
        float thisDiscriminant;
        final int rows = kernelMatrix.getRowDimension();
        FloatMatrix discriminantMatrix = new FloatMatrix(rows, 2);
        for (int i=0; i<rows; i++) {
            /* Compute the discriminant. */
            thisDiscriminant = classify(kernelMatrix, weights, i);
            /* Store the classification. */
            if (thisDiscriminant >= 0.0) {
                discriminantMatrix.set(i, 0, 1.0f);
            } else {
                discriminantMatrix.set(i, 0, -1.0f);
            }
            /* Store the discriminant. */
            discriminantMatrix.set(i, 1, thisDiscriminant);
        }
        return discriminantMatrix;
    }
    
    
    /**
     * Normalize a kernel matrix.
     *
     * Let x be a vector of unnormalized features. Let \tilde{x} be the
     * vector of normalized features.
     *
     *         \tilde{x}_i = x_i/||x||
     *
     * where ||x|| is the norm of x. This means that ||\tilde{x}|| = 1 for
     * all x. What is happening when you normalize in this way is that all
     * feature vectors x are getting their lengths scaled so that they lie
     * on the surface of the unit sphere in 79 dimensional space.
     *
     * It turns out that this kind of normalization can be done in general
     * for any kernel. If K(x,y) is any kernel supplied by a user, then
     * you can normalize it by defining
     *
     *     \tilde{K}(x,y) = K(x,y)/\sqrt{K(x,x)}\sqrt{K(y,y)}
     *
     * It turns out that \sqrt{K(x,x)} is the norm of x in the feature
     * space.  Hence, for any x, the norm of x in the feature space
     * defined by \tilde{K} is
     *
     *           \sqrt{\tilde{K}(x,x)}  = 1
     *
     * When you normalize the kernel this way, it ensures that all points
     * are mapped to the surface of the unit ball in some (possibly infinite
     * dimensional) feature space.
     */
    private void normalizeKernelMatrix(FloatMatrix kernelMatrix, float[] selfKernelValues) {
        float rowDiag;
        float columnDiag;
        float cell;
        final int rows = kernelMatrix.getRowDimension();
        final int columns = kernelMatrix.getColumnDimension();
        for (int row=0; row<rows; row++) {
            rowDiag = (float)Math.sqrt(selfKernelValues[row]);
            for (int column=0; column<columns; column++) {
                columnDiag = (float)Math.sqrt(selfKernelValues[column]);
                cell = kernelMatrix.get(row, column);
                cell /= rowDiag*columnDiag*1.0;
                kernelMatrix.set(row, column, cell);
            }
        }
        for (int row=0; row<rows; row++) {
            selfKernelValues[row] = 1.0f;
        }
    }
    
    
    /**
     * Compute the median value in an array.
     *
     * Sorts the array as a side effect.
     */
    private float computeMedian(float[] array) {
        int   numberOfItems;
        float returnValue;
        
        // Sort the array.
        Arrays.sort(array);
        numberOfItems = array.length;
        if (numberOfItems % 2 == 1) {
            // If there are an odd number of elements, return the middle one.
            returnValue = array[numberOfItems/2];
        } else {
            // Otherwise, return the average of the two middle ones.
            returnValue  = array[numberOfItems/2 - 1];
            returnValue += array[numberOfItems/2];
            returnValue /= 2.0;
        }
        return returnValue;
    }
    
    /**
     * Define the squared distance as follows:
     *
     *     d^2(x,y) = K(x,x) - 2 K(x,y) + K(y,y)
     *
     * This is the squared Euclidean distance between points in feature
     * space.
     */
    private final float computeSquaredDistance(float Kxx, float Kxy, float Kyy) {
        return Kxx - 2*Kxy + Kyy;
    }
    
    /**
     * Set the width of a radial basis kernel to be the median of the
     * distances from each positive example to the nearest negative
     * example.
     *
     * This is only called during training, so we know the kernel matrix
     * is square.
     */
    private float computeTwoSquaredWidth(FloatMatrix kernelMatrix, int[] classes, float widthFactor) {
        int     numberOfPositives; // Total number of positive examples.
        float[] nearestNegatives;   // Distances to nearest negative example.
        int     positive;
        float   squaredDistance;    // Squared distance between two examples.
        float   returnValue;
        final int rows = kernelMatrix.getRowDimension();
        // Count the number of positive examples.
        numberOfPositives = 0;
        for (int i=0; i<rows; i++) {
            if (classes[i] == 1) {
                numberOfPositives++;
            }
        }
        // Allocate the array of distances to nearest negatives.
        nearestNegatives = new float[numberOfPositives];
        // Find the nearest negative example for each positive.
        positive = -1;
        for (int i=0; i<rows; i++) {
            // Consider only positive examples.
            if (classes[i] == 1) {
                positive++;
                // Initialize this position to something very large.
                nearestNegatives[positive] = Float.MAX_VALUE;
                for (int j=0; j<rows; j++) {
                    // Consider only negative examples.
                    if (classes[j] != 1) {
                        // Compute the distance between these examples.
                        squaredDistance = computeSquaredDistance(kernelMatrix.get(i, i), kernelMatrix.get(i,j), kernelMatrix.get(j,j));
                        // Store the minimum.
                        if (nearestNegatives[positive] > squaredDistance) {
                            nearestNegatives[positive] = squaredDistance;
                        }
                    }
                }
            }
        }
        // Find the median distance.
        returnValue = computeMedian(nearestNegatives);
        // Multiply in the given width factor and a factor of 2.
        returnValue *= 2.0 * widthFactor;
        // Return the result.
        return returnValue;
    }
    
    
    /**
     * Compute a radial basis function kernel, defined by
     *
     *     K(x,y) = exp{d^2(x,y)/2\sigma^2}
     *
     * for some constant \sigma (the width).
     */
    private float radialKernel(float twoSquaredWidth, float Kxx, float Kxy, float Kyy) {
        float returnValue;
        // Compute the squared distances between the examples.
        returnValue = computeSquaredDistance(Kxx, Kxy, Kyy);
        // Divide by twice sigma squared.
        returnValue /= twoSquaredWidth;
        // Exponentiate the opposite.
        returnValue = (float)Math.exp(-returnValue);
        // Make sure we didn't hit zero.
        if (returnValue == 0.0 && !isSeenUnderflow()) {
            setSeenUnderflow(true);
        }
        return returnValue;
    }
    
    /**
     * Convert each value in a given kernel matrix to a radial basis
     * version.
     */
    private void radializeMatrix(FloatMatrix kernelMatrix, float[] selfKernelValues, float twoSquaredWidth, float constant) {
        float radialValue;
        // Radialize each row of the matrix.
        final int rows = kernelMatrix.getRowDimension();
        final int columns = kernelMatrix.getColumnDimension();
        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                // Compute the new value.
                radialValue = radialKernel(twoSquaredWidth, selfKernelValues[row], kernelMatrix.get(row, column), selfKernelValues[column]);
                // Add the constant back in.
                radialValue += constant;
                // Store the new value.
                kernelMatrix.set(row, column, radialValue);
            }
        }
        /* Extract the radialized self-kernel values. This is necessary
           because these values are used during the computation of the
           diagonal factor.  This computation only occurs during training
           (not classification).  During training, the self-kernel values
           are the same for the rows and the columns.  Hence, we only need
           to extract values for the rows.  */
        extractSelfKernelValues(kernelMatrix, selfKernelValues);
    }
    
    /*
     * One good way to set the constants to add to the diagonal is
     *
     *   n+ = number of positive examples
     *   n- = number of negative examples
     *   N  = total number of examples
     *   k  = some constant (given by diagonal_factor)
     *
     * Then set
     *
     *   positive_diagonal = (n+/N) * k
     *   negative_diagonal = (n-/N) * k
     *
     */
    /**
     * Returns the diagonal constants
     */
    private float[] getDiagonalConstants(float[] selfKernelValues, int[] classes, float diagonalFactor) {
        // If the diagonal factor is zero, do nothing.
        if (diagonalFactor == 0) {
            return null;
        }
        int numberOfExamples = classes.length;
        int numberOfPositive = 0;
        int numberOfNegative = 0;
        // Find the median self-kernel value.
        float medianDiagonal = computeMedian(selfKernelValues);
        // Count the number of positives and negatives.
        for (int i=0; i<numberOfExamples; i++) {
            if (classes[i]==1) {
                numberOfPositive++;
            } else {
                numberOfNegative++;
            }
        }
        float[] diagonals = new float[2];
        diagonals[POSITIVE_DIAGONAL] = ((float)numberOfPositive/(float)numberOfExamples)*diagonalFactor*medianDiagonal;
        diagonals[NEGATIVE_DIAGONAL] = ((float)numberOfNegative/(float)numberOfExamples)*diagonalFactor*medianDiagonal;
        //diagonals[POSITIVE_DIAGONAL] = ((float)numberOfPositive/(float)numberOfExamples)*diagonalFactor;
        //diagonals[NEGATIVE_DIAGONAL] = ((float)numberOfNegative/(float)numberOfExamples)*diagonalFactor;
        return diagonals;
    }
    
    /*
     * Add a constant to the diagonal of the kernel matrix.
     *
     * This can be used to accomplish two things:
     *
     * (1) If the kernel is not positive definite, then adding a
     *     sufficiently large constant to the diagonal will make it so
     *     (although I don't know how to calculate a priori the proper
     *     value to add).
     *
     * (2) Adding to the diagonal also effectively scales the weights.  A
     *     larger constant makes the weights smaller.  Adding different
     *     constants for the positives and negatives has the same effect
     *     as placing different constraint ceilings.
     *
     */
    private void addToKernelDiagonal(FloatMatrix kernelMatrix, float[] selfKernelValues, int[] classes, float positiveDiagonal, float negativeDiagonal, float diagonalFactor) {
        float[] diagonals = getDiagonalConstants(selfKernelValues, classes, diagonalFactor);
        if (diagonals != null) {
            positiveDiagonal = diagonals[POSITIVE_DIAGONAL];
            negativeDiagonal = diagonals[NEGATIVE_DIAGONAL];
        }
        final int rows = kernelMatrix.getRowDimension();
        for (int row=0; row < rows; row++) {
            if (classes[row] == 1) {
                kernelMatrix.set(row, row, kernelMatrix.get(row, row) + positiveDiagonal);
            } else {
                kernelMatrix.set(row, row, kernelMatrix.get(row, row) + negativeDiagonal);
            }
            
            
        }
    }
    
    /*
     * The discriminant function is used to determine whether a given
     * example is classified positively or negatively.
     *
     * This function implements equation (4) from the paper cited above.
     */
    private float computeDiscriminant(FloatMatrix kernelMatrix, float[] weights, int[] classes, int thisItem) {
        float returnValue = 0.0f;
        for (int i=0; i<classes.length; i++) {
            /* Weight the distance appropriately and
            add or subtract, depending upon whether this is a positive or
            negative example. */
            if(!(Float.isNaN(kernelMatrix.get(thisItem, i) )))
                returnValue += weights[i]*kernelMatrix.get(thisItem, i)*classes[i];
        }
        return returnValue;
    }
    
    /*
     * Keep a local copy of the weights array and signal if they've
     * stopped changing.
     *
     * Convergence is reached when the delta is below the convergence
     * threshold.
     */
    private boolean converged(AlgorithmEvent event, FloatMatrix kernelMatrix, float[] weights, int[] classes, float convergenceThreshold) {
        float objective;       // Current value of the objective.
        float delta = 0.0f;    // Change in objective.
        
        // Compute the new objective.
        objective = computeObjective(kernelMatrix, weights, classes);
        
        // Compute the change in objective.
        delta = objective - prevObjective;
        
        // Store this objective for next time.
        prevObjective = objective;
        
        if(!Float.isNaN(delta) && !Float.isInfinite(delta)){
            event.setFloatValue(Math.abs(delta));
            fireValueChanged(event);
        }
        
        return(Math.abs(delta) < convergenceThreshold);
    }
    
    /*
     * Compute the objective function, equation (7).
     */
    private float computeObjective(FloatMatrix kernelMatrix, float[] weights, int[] classes) {
        float sum = 0.0f;
        for (int i=0; i<classes.length; i++) {
            sum += weights[i]*(2.0-(computeDiscriminant(kernelMatrix, weights, classes, i)*classes[i]));
        }
        return sum;
    }
    
    /*
     * Update one item's weight.  This update rule maximizes the
     * constrained maximization of J(\lambda).  This function implements
     * equations (9) and (10) in Jaakkola et al.
     */
    private float updateWeight(FloatMatrix kernelMatrix, float[] weights, int[] classes, float constraint, boolean constrainWeights, int thisItem) {
        float thisDiscriminant;
        float selfDistance;
        float thisWeight;
        float newWeight;
        float thisClass;
        
        thisDiscriminant = computeDiscriminant(kernelMatrix, weights, classes, thisItem);
        selfDistance = kernelMatrix.get(thisItem, thisItem);
        thisWeight = weights[thisItem];
        // Weight negative examples oppositely.
        thisClass = classes[thisItem];
        // This is equation (8).
        newWeight = 1.0f-(thisClass*thisDiscriminant)+(thisWeight*selfDistance);
        // Divide by k(x,x), checking for divide-by-zero.
        if (selfDistance == 0.0) {
            newWeight /= newWeight;  /* ?????????????????????????????*/
        } else {
            newWeight /= selfDistance;
        }
        if (selfDistance != 0.0) {
            thisWeight = weights[thisItem];
            weights[thisItem] = newWeight;
            weights[thisItem] = newWeight;
            thisDiscriminant  = computeDiscriminant(kernelMatrix, weights, classes, thisItem);
            weights[thisItem] = thisWeight;
        }
        // Constrain the weight.
        if (constrainWeights && (newWeight > constraint)) {
            newWeight = constraint;
        } else if (newWeight < 0.0) {
            newWeight = 0.0f;
        }
        
        
        return newWeight;
    }
    
    /*
     * Optimize the weights so that the discriminant function puts the
     * negatives close to -1 and the positives close to +1.
     */
    private long optimizeWeights(FloatMatrix kernelMatrix, float[] weights, int[] classes, int seed, float positiveConstraint, float negativeConstraint, float convergenceThreshold, boolean constrainWeights) throws AlgorithmException {
        int   randItem;
        long  iter = 0;
        float newWeight = 0.5f;
        float constraint;
        float INITIAL_VALUE = 0.5f; // Initial value for all weights.
        
        // Initialize the weights.
        for (int i=0; i<weights.length; i++) {
            weights[i] = INITIAL_VALUE;
        }
        
        AlgorithmEvent event = new AlgorithmEvent(this, AlgorithmEvent.MONITOR_VALUE);
        fireValueChanged(event); // to show monitor
        Random random = new Random(seed);
        // Iteratively improve the weights until convergence.
        while (!converged(event, kernelMatrix, weights, classes, convergenceThreshold)) {
            isStop();
            for (int i=0; i < classes.length; i++) {
                // Randomly select a weight to update.
                randItem = random.nextInt(classes.length);
                // Set the constraint, based upon the class of this item.
                if (classes[randItem] == 1) {
                    constraint = positiveConstraint;
                } else {
                    constraint = negativeConstraint;
                }
                // Calculate the new weight.
                newWeight = updateWeight(kernelMatrix, weights, classes, constraint, constrainWeights, randItem);
                weights[randItem] = newWeight;
            }
            
            if(iter > 1000){
                if(JOptionPane.showConfirmDialog( null, "                                Warning: 1000 iterations have failed to optimize weights.\n"+
                "Please press OK to continue analysis using current weights OR press CANCEL to abort and try new parameters.\n","Weight Optimization Warning", JOptionPane.WARNING_MESSAGE, JOptionPane.WARNING_MESSAGE)
                == JOptionPane.OK_OPTION)
                    break;
                else
                    this.stop = true;
            }
            iter++;
        }
        return iter+1;
    }
    
    /*
     * Encode the classifications in the weights by multiplying the negative
     * examples by -1.
     */
    private void signWeights(float[] weights, int[] classes) {
        for (int i=0; i<classes.length; i++) {
            weights[i] = weights[i]*classes[i];
        }
    }
    
    /**
     * Creates classification FloatMatrix of class distribution ints (pos == 1, neg == -1) and discriminant values
     */
    private FloatMatrix classify(FloatMatrix trainingMatrix, float[] weights, float coefficient, float constant, float power) {
        
        AlgorithmEvent event = new AlgorithmEvent(this, AlgorithmEvent.PROGRESS_VALUE, 0);
        sendEvent(event, "CLASSIFYING\n");
        event.setDescription("Computing base kernel matrix\n");
        fireValueChanged(event);
        
        FloatMatrix kernelMatrix = computeNormalizedBaseKernelMatrix(trainingMatrix);
        
        float[] selfKernelValues = createSelfKernelValues(kernelMatrix);
        
        event.setDescription("Polynomializing kernel matrix\n");
        fireValueChanged(event);
        
        polynomializeMatrix(kernelMatrix, selfKernelValues, power, coefficient, constant);
        
        FloatMatrix discriminantMatrix = classifyList(kernelMatrix, weights);
        
        return discriminantMatrix;
    }
    
    /**
     * Trains SVM and returns float [] of weights
     */
    private float[] train(FloatMatrix trainingMatrix, int[] classes, int seed, boolean normalize, boolean radial, float coefficient, float constant, float power, float widthFactor, float positiveDiagonal, float negativeDiagonal, float diagonalFactor, float positiveConstraint, float negativeConstraint, float convergenceThreshold, boolean constrainWeights) throws AlgorithmException {
        
        AlgorithmEvent event = new AlgorithmEvent(this, AlgorithmEvent.PROGRESS_VALUE, 0);
        sendEvent(event, "TRAINING SVM\n");
        sendEvent(event, "Computing base kernel matrix\n");
        //FloatMatrix kernelMatrix = computeBaseKernelMatrix(trainingMatrix);
        
        
        FloatMatrix kernelMatrix = computeNormalizedBaseKernelMatrix(trainingMatrix);
        
        
        sendEvent(event, "Extract the diagonal from the kernel matrix.\n");
        float[] selfKernelValues = createSelfKernelValues(kernelMatrix);
        
        isStop();
        
        if (normalize) {
            sendEvent(event, "Normalizing kernel matrix\n");
            normalizeKernelMatrix(kernelMatrix, selfKernelValues);
        }
        
        isStop();
        
        
        if(!radial){
            sendEvent(event, "Polynomializing kernel matrix\n");
            polynomializeMatrix(kernelMatrix, selfKernelValues, power, coefficient, constant);
        }
        
        if (radial) {
            sendEvent(event, "Convert to a radial basis kernel.\n");
            float twoSquaredWidth = computeTwoSquaredWidth(kernelMatrix, classes, widthFactor);
            radializeMatrix(kernelMatrix, selfKernelValues, twoSquaredWidth, constant);
        }
        
        isStop();
        
        // Add constants to the diagonal.
        sendEvent(event, "Adding constants to kernel matrix\n");
        addToKernelDiagonal(kernelMatrix, selfKernelValues, classes, positiveDiagonal, negativeDiagonal, diagonalFactor);
        
        //printKernelDiagonal(kernelMatrix);
        
        isStop();
        
        // Initialize the weights to zeroes.
        float[] weights = new float[number_of_genes];
        // Optimize the weights.
        sendEvent(event, "Optimizing weights\n");
        optimizeWeights(kernelMatrix, weights, classes, seed, positiveConstraint, negativeConstraint, convergenceThreshold, constrainWeights);
        
        // Encode the classifications as the signs of the weights.
        sendEvent(event, "Encoding the classifications as the signs of the weights.\n");
        signWeights(weights, classes);
        
        return weights;
    }
    
    private void isStop() throws AbortException {
        if (stop) {
            throw new AbortException();
        }
    }
    
    private void sendEvent(AlgorithmEvent event, String description) {
        event.setDescription(description);
        fireValueChanged(event);
    }
    
    /**
     * Returns positive element index list
     */
    private int [] getPositives(FloatMatrix matrix){
        int cnt = 0;
        
        for(int i = 0; i < matrix.getRowDimension(); i++){
            if( matrix.get( i, 0 ) == 1.0 )
                cnt++;
        }
        
        int [] pos = new int[cnt];
        cnt = 0;
        
        for(int i = 0; i < matrix.getRowDimension(); i++){
            if( matrix.get( i, 0 ) == 1.0 ){
                pos[cnt] = i;
                cnt++;
            }
        }
        return pos;
    }
    
    /**
     * Returns negative element index list
     */
    private int [] getNegatives(FloatMatrix matrix){
        int cnt = 0;
        for(int i = 0; i < matrix.getRowDimension(); i++){
            if( matrix.get( i, 0 ) <= 0 )
                cnt++;
        }
        
        int [] neg = new int[cnt];
        cnt = 0;
        
        for(int i = 0; i < matrix.getRowDimension(); i++){
            if( matrix.get( i, 0 ) <= 0 ){
                neg[cnt] = i;
                cnt++;
            }
        }
        return neg;
    }
    
    
    /**
     *  Internal gene dot product
     */
    private float geneDotProduct(FloatMatrix matrix, FloatMatrix M, int g1, int g2) {
        if (M == null) {
            M = matrix;
        }
        int k=matrix.getColumnDimension();
        int n=0;
        double sum=0.0;
        for (int i=0; i<k; i++) {
            if ((!Float.isNaN(matrix.get(g1,i))) && (!Float.isNaN(M.get(g2,i)))) {
                sum+=matrix.get(g1,i)*M.get(g2,i);
                n++;
            }
        }
        return(float)(sum);
    }
    
    
    /**
     *  Retuns means values for each column within positives and negatives
     */
    private FloatMatrix getMeans(FloatMatrix discMatrix){
        int numSamples = this.expMatrix.getColumnDimension();
        int numGenes = this.expMatrix.getRowDimension();
        
        FloatMatrix means = new FloatMatrix(2, numSamples);
        float posMean = 0;
        float negMean = 0;
        float value;
        int posCnt = 0;
        int negCnt = 0;
        float c;
        
        for(int j = 0; j < numSamples; j++){
            for(int i = 0; i < numGenes; i++){
                
                c = discMatrix.get(i,0);
                if(c == 1){
                    value = this.expMatrix.get(i,j);
                    if(!Float.isNaN(value)){
                        posCnt++;
                        posMean += value;
                    }
                }
                else{
                    
                    value = this.expMatrix.get(i,j);
                    if(!Float.isNaN(value)){
                        negCnt++;
                        negMean += value;
                    }
                }
            }
            means.set( 0, j, (float)(posCnt != 0 ? posMean/posCnt : 0.0f));
            means.set( 1, j, (float)(negCnt != 0 ? negMean/negCnt : 0.0f));
            posCnt = 0;
            negCnt = 0;
            posMean = 0;
            negMean = 0;
        }
        return means;
    }
    
    /**
     *  Retuns variance values for each column within positives and negatives
     */
    private FloatMatrix getVariance(FloatMatrix discMatrix, FloatMatrix means){
        int numSamples = this.expMatrix.getColumnDimension();
        int numGenes = this.expMatrix.getRowDimension();
        FloatMatrix vars = new FloatMatrix(2, numSamples);
        float value;
        float c;
        float mean;
        float ssePos = 0;
        int posCnt = 0;
        float sseNeg = 0;
        int negCnt = 0;
        for(int i = 0; i < numSamples; i++){
            
            for(int j = 0; j < numGenes; j++){
                c = discMatrix.get(j, 0);
                
                if(c == 1){
                    value = expMatrix.get(j,i);
                    if(!Float.isNaN(value)){
                        ssePos += Math.pow(value - means.get(0, i), 2);
                        posCnt++;
                    }
                }
                else{
                    value = expMatrix.get(j,i);
                    if(!Float.isNaN(value)){                        
                        sseNeg += Math.pow(value - means.get(1, i), 2);
                        negCnt++;
                    }
                }
            }
            vars.set( 0, i, (float)(posCnt > 1 ? Math.sqrt(ssePos/(posCnt - 1)) : 0.0f));
            vars.set( 1, i, (float)(negCnt > 1 ? Math.sqrt(sseNeg/(negCnt - 1)) : 0.0f));
            posCnt = 0;
            negCnt = 0;
            ssePos = 0;
            sseNeg = 0;
        }
        return vars;
    }
    
    /**
     * Creates HCL results
     */
    private NodeValueList calculateHierarchicalTree(int[] features, int method, boolean genes, boolean experiments) throws AlgorithmException {
        NodeValueList nodeList = new NodeValueList();
        AlgorithmData data = new AlgorithmData();
        FloatMatrix experiment;
        if(svmGenes)
            experiment = getSubExperiment(this.expMatrix, features);
        else
            experiment = getSubExperimentReducedCols(this.expMatrix, features);
        
        data.addMatrix("experiment", experiment);
        data.addParam("distance-function", String.valueOf(this.function));
        data.addParam("distance-absolute", String.valueOf(this.absolute));
        data.addParam("method-linkage", String.valueOf(method));
        HCL hcl = new HCL();
        AlgorithmData result;
        
        if (genes) {
            data.addParam("calculate-genes", String.valueOf(true));
            result = hcl.execute(data);
            validate(result);
            addNodeValues(nodeList, result);
        }
        if (experiments) {
            data.addParam("calculate-genes", String.valueOf(false));
            result = hcl.execute(data);
            int [] nodes = result.getIntArray("node-order");
            validate(result);
            addNodeValues(nodeList, result);
        }
        return nodeList;
    }
    
    
    /**
     * Accumulates hcl results
     */
    private void addNodeValues(NodeValueList target_list, AlgorithmData source_result) {
        target_list.addNodeValue(new NodeValue("child-1-array", source_result.getIntArray("child-1-array")));
        target_list.addNodeValue(new NodeValue("child-2-array", source_result.getIntArray("child-2-array")));
        target_list.addNodeValue(new NodeValue("node-order", source_result.getIntArray("node-order")));
        target_list.addNodeValue(new NodeValue("height", source_result.getMatrix("height").getRowPackedCopy()));
    }
    
    /**
     *  Gets sub experiment (cluster membership only, dictated by features)
     */
    private FloatMatrix getSubExperiment(FloatMatrix experiment, int[] features) {
        FloatMatrix subExperiment = new FloatMatrix(features.length, experiment.getColumnDimension());
        for (int i=0; i<features.length; i++) {
            subExperiment.A[i] = experiment.A[features[i]];
        }
        return subExperiment;
    }
    
    /**
     *  Creates a matrix with reduced columns (samples) as during experiment classification
     */
    private FloatMatrix getSubExperimentReducedCols(FloatMatrix experiment, int[] features) {
        FloatMatrix copyMatrix = experiment.copy();
        FloatMatrix subExperiment = new FloatMatrix(features.length, copyMatrix.getColumnDimension());
        for (int i=0; i<features.length; i++) {
            subExperiment.A[i] = copyMatrix.A[features[i]];
        }
        subExperiment = subExperiment.transpose();
        return subExperiment;
    }
    
    /**
     * Checks the result of hcl algorithm calculation.
     * @throws AlgorithmException, if the result is incorrect.
     */
    private void validate(AlgorithmData result) throws AlgorithmException {
        if (result.getIntArray("child-1-array") == null) {
            throw new AlgorithmException("parameter 'child-1-array' is null");
        }
        if (result.getIntArray("child-2-array") == null) {
            throw new AlgorithmException("parameter 'child-2-array' is null");
        }
        if (result.getIntArray("node-order") == null) {
            throw new AlgorithmException("parameter 'node-order' is null");
        }
        if (result.getMatrix("height") == null) {
            throw new AlgorithmException("parameter 'height' is null");
        }
    }
    
    private int[] convert2int(ArrayList source) {
        int[] int_matrix = new int[source.size()];
        for (int i=0; i<int_matrix.length; i++) {
            int_matrix[i] = (int)((Float)source.get(i)).floatValue();
        }
        return int_matrix;
    }
    
    
    
    
    //*************** debug *******************
    private void printMatrix(String title, FloatMatrix matrix) {
        System.out.println("===== "+title+" =====");
        matrix.print(5, 2);
    }
    
    private void printFloatArray(String title, float[] floatArray) {
        System.out.println("===== "+title+" =====");
        for (int i=0; i<floatArray.length; i++) {
            System.out.print(floatArray[i]+" ");
        }
        System.out.println();
    }
    
    private void printKernelDiagonal(FloatMatrix matrix){
        for(int i = 0; i < matrix.getRowDimension(); i++){
            System.out.println("Kernal diagonal " + matrix.get(i,i));
        }
    }
    
    private void printWeights(float [] w){
        for(int i = 0; i < w.length; i++){
            System.out.println("Weight = "+ w[i]);
        }
    }
    //**************end debug methods********************
    
}
