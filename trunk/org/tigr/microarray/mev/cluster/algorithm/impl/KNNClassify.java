/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: KNNClassify.java,v $
 * $Revision: 1.2 $
 * $Date: 2003-12-08 19:36:14 $
 * $Author: braisted $
 * $State: Exp $
 */
/*
 * KNNClassify.java
 *
 * Created on September 2, 2003, 4:37 PM
 */

package org.tigr.microarray.mev.cluster.algorithm.impl;

import java.util.Vector;
import java.util.Random;
import org.tigr.util.FloatMatrix;
import org.tigr.util.ConfMap;
import org.tigr.util.QSort;

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

/**
 *
 * @author  nbhagaba
 */
public class KNNClassify extends AbstractAlgorithm {
    
    private boolean stop = false;
    private int function;
    private float factor;
    private boolean absolute;
    private FloatMatrix expMatrix;
    
    private Vector[] clusters;
    private int k; // # of clusters
    
    private int numRows, numCols;  
    private int usedNumNeibs;
    
    private boolean validate, classifyGenes, useVarianceFilter, useCorrelFilter;
    private int numClasses, numVarFilteredVectors, numNeighbors, numPerms, postVarClassSetSize, postVarDataSetSize, postCorrDataSetSize, origDataSetSize, origClassSetSize;
    private double correlPValue;  
    private int[] classIndices, classes;    
    
    private Vector rowsInAnalysis, filteredClassifierSet, filteredClasses;
    
    //AlgorithmEvent event, event2;    
    
    /** This method should interrupt the calculation.
     */

    
    /** This method execute calculation and return result,
     * stored in <code>AlgorithmData</code> class.
     *
     * @param data the data to be calculated.
     */
    public AlgorithmData execute(AlgorithmData data) throws AlgorithmException {
	AlgorithmParameters map = data.getParams();
	function = map.getInt("distance-function", EUCLIDEAN);
	factor   = map.getFloat("distance-factor", 1.0f);
	absolute = map.getBoolean("distance-absolute", false);
	
	boolean hierarchical_tree = map.getBoolean("hierarchical-tree", false);
	int method_linkage = map.getInt("method-linkage", 0);
	boolean calculate_genes = map.getBoolean("calculate-genes", false);
	boolean calculate_experiments = map.getBoolean("calculate-experiments", false);
	
	this.expMatrix = data.getMatrix("experiment");
	
	numRows = this.expMatrix.getRowDimension();
	numCols = this.expMatrix.getColumnDimension();        
       
        validate = map.getBoolean("validate", false);
        
        if (!validate) {
            classifyGenes = map.getBoolean("classifyGenes", true);
            useVarianceFilter = map.getBoolean("useVarianceFilter", false);
            useCorrelFilter = map.getBoolean("useCorrelFilter", false);
            if (useCorrelFilter) {
                correlPValue = map.getFloat("correlPValue", 0.01f);
                numPerms = map.getInt("numPerms", 1000);
            }
            numClasses = map.getInt("numClasses", 5);
            numNeighbors = map.getInt("numNeighbors", 3);
            classIndices = data.getIntArray("classIndices");
            classes = data.getIntArray("classes");
            numVarFilteredVectors = map.getInt("numVarFilteredVectors", numRows);  
            /*
            if (classifyGenes) {
                if (useVarianceFilter) {
                    numVarFilteredVectors = map.getInt("numVarFilteredVectors", numGenes);
                }
            } else {// if (!classifyGenes)
                if (useVarianceFilter) {
                    numVarFilteredVectors = map.getInt("numVarFilteredVectors", numExps);
                }                
            }
             */
            
            AlgorithmEvent event = new AlgorithmEvent(this, AlgorithmEvent.SET_UNITS, numRows);
            fireValueChanged(event);
            event.setId(AlgorithmEvent.PROGRESS_VALUE);            
            
            rowsInAnalysis = new Vector();
            for (int i = 0; i < numRows; i++) {                
                rowsInAnalysis.add(new Integer(i));
            }    
 
            filteredClassifierSet = new Vector();
            filteredClasses = new Vector();
            for (int i = 0; i < classIndices.length; i++) {
                filteredClassifierSet.add(new Integer(classIndices[i]));
                filteredClasses.add(new Integer(classes[i]));
            }
            
            /*
            System.out.println("Before variance filter:");
            System.out.println("rowsInAnalysis.size() = " + rowsInAnalysis.size() + ", filteredClassifierSet.size() = " + filteredClassifierSet.size());
            */
            
            origDataSetSize = rowsInAnalysis.size();
            origClassSetSize = filteredClassifierSet.size();
            
            if (useVarianceFilter) {
                double[] variances = new double[rowsInAnalysis.size()];
                
                for (int i = 0; i < variances.length; i++) {
                    if (stop) {
                        throw new AbortException();
                    }
                    event.setIntValue(i);
                    event.setDescription("Calculating variance of element = " + (i + 1));
                    fireValueChanged(event); 
                    //System.out.println("Calculating variance of gene = " + (i + 1));
                    variances[i] = getVar(i);
                }
                
                QSort sortVariances = new QSort(variances);
                int[] sortedIndices = sortVariances.getOrigIndx();
                int[] sortDesc = reverse(sortedIndices);
                
                rowsInAnalysis = new Vector();
                for (int i = 0; i < numVarFilteredVectors; i++) {
                    rowsInAnalysis.add(new Integer(sortDesc[i]));
                }
                
                filteredClassifierSet = new Vector();
                filteredClasses = new Vector();
                
                for (int i = 0; i < classIndices.length; i++) {
                    if (isFoundInVector(classIndices[i], rowsInAnalysis)) {
                        filteredClassifierSet.add(new Integer(classIndices[i]));
                        filteredClasses.add(new Integer(classes[i]));
                        //rowsInAnalysis.remove(new Integer(classIndices[i]));
                    }
                }                
                
            }
            
            for (int i = 0; i < classIndices.length; i++) {
                if (isFoundInVector(classIndices[i], rowsInAnalysis)) {
                    rowsInAnalysis.remove(new Integer(classIndices[i]));
                }
            }
            
            /*
            System.out.println("After variance filter: ");
            System.out.println("rowsInAnalysis.size() = " + rowsInAnalysis.size() + ", filteredClassifierSet.size() = " + filteredClassifierSet.size());     
            */
            postVarDataSetSize = rowsInAnalysis.size();
            postVarClassSetSize = filteredClassifierSet.size();

            AlgorithmEvent event2 = new AlgorithmEvent(this, AlgorithmEvent.SET_UNITS, rowsInAnalysis.size());
            fireValueChanged(event2);
            event2.setId(AlgorithmEvent.PROGRESS_VALUE);            
            
            if (useCorrelFilter) {
                Vector correlFilteredRows = new Vector();
                for (int i = 0; i < rowsInAnalysis.size(); i++) {
                    if (stop) {
                        throw new AbortException();
                    }
                    event2.setIntValue(i);
                    event2.setDescription("Applying correlation filter: element " + (i + 1));
                    fireValueChanged(event2);                     
                    int currentRow = ((Integer)(rowsInAnalysis.get(i))).intValue();
                    Random rnd1 = new Random();
                    if (passesCorrelationFilter(currentRow, filteredClassifierSet, correlPValue, numPerms, rnd1.nextLong()) ) {
                        correlFilteredRows.add(new Integer(currentRow));
                    }
                }
                
                rowsInAnalysis = new Vector();
                
                for (int i = 0; i < correlFilteredRows.size(); i++) {
                    rowsInAnalysis.add((Integer)(correlFilteredRows.get(i)));
                }
            }
            
            //System.out.println("After correlation filter: ");
            //System.out.println("rowsInAnalysis.size() = " + rowsInAnalysis.size() + ", filteredClassifierSet.size() = " + filteredClassifierSet.size());            
            
            postCorrDataSetSize = rowsInAnalysis.size();
            
            Vector[] classSets = new Vector[numClasses + 1]; // classSets[0] contains unclassified elements
            for (int i = 0; i < classSets.length; i++) {
                classSets[i] = new Vector();
            }
            
            for (int i = 0; i < rowsInAnalysis.size(); i++) {
                int currRow = ((Integer)(rowsInAnalysis.get(i))).intValue();
                int currClass = getClassification(currRow, numNeighbors);                
                classSets[currClass].add(new Integer(currRow));
                
            }
            
            for (int i = 0; i < numRows; i++) {
                if( !(rowsInAnalysis.contains(new Integer(i))) && !(filteredClassifierSet.contains(new Integer(i))) ) {
                    classSets[0].add(new Integer(i));
                }
            }
            
            Vector[] unusedClassifiers = new Vector[numClasses + 1];
            Vector[] usedClassifiers = new Vector[numClasses + 1];
            for (int i = 1; i < unusedClassifiers.length; i++) {
                unusedClassifiers[i] = new Vector();
                usedClassifiers[i] = new Vector();
            }
            
            //DONE UP TO HERE 10/01/03. NEED TO EXTRACT UNUSED CLASSIFIERS.
            
            for (int i = 0; i < classIndices.length; i++) {
                if (!isFoundInVector(classIndices[i], filteredClassifierSet)) {
                    unusedClassifiers[classes[i]].add(new Integer(classIndices[i]));
                } else {
                   usedClassifiers[classes[i]].add(new Integer(classIndices[i])); 
                }
            }
            
            Vector[] usedPlusClassified = new Vector[numClasses + 1];
            
            for (int i = 1; i < usedPlusClassified.length; i++) {
                usedPlusClassified[i] = new Vector();
                for (int j = 0; j < usedClassifiers[i].size(); j++) {
                    usedPlusClassified[i].add( (Integer)(usedClassifiers[i].get(j)) );
                }
                for (int j = 0; j < classSets[i].size(); j++) {
                    usedPlusClassified[i].add( (Integer)(classSets[i].get(j)) );
                }                
            }
            
            clusters = new Vector[numClasses*4 + 1]; 
            
            for (int i = 1; i <= numClasses; i++) {
                clusters[i - 1] = usedClassifiers[i];
                clusters[i - 1 + numClasses] = unusedClassifiers[i];
                clusters[i - 1 + 2*numClasses] = classSets[i];
                clusters[i - 1 + 3*numClasses] = usedPlusClassified[i];
            }
            
            clusters[numClasses*4] = classSets[0];
            
            /*
            for (int i = 0; i < clusters.length; i++) {
                System.out.println("clusters[" + i + "].size() = " + clusters[i].size());
            }
             */
            
            // for each class, report 1) used classifiers, 2) classified vectors only (no classifiers) 3) classifiers + classified, and 4) unused classisiers.
            // The last cluster is all the unassigned genes
            
            FloatMatrix means = getMeans(clusters);
            FloatMatrix variances = getVariances(clusters, means);
            
            AlgorithmEvent event3 = null;
            if (hierarchical_tree) {
                event3 = new AlgorithmEvent(this, AlgorithmEvent.SET_UNITS, clusters.length, "Calculate Hierarchical Trees");
                fireValueChanged(event3);
                event3.setIntValue(0);
                event3.setId(AlgorithmEvent.PROGRESS_VALUE);
                fireValueChanged(event3);
            }
            
            Cluster result_cluster = new Cluster();
            NodeList nodeList = result_cluster.getNodeList();
            int[] features;
            for (int i=0; i<clusters.length; i++) {
                if (stop) {
                    throw new AbortException();
                }
                features = convert2int(clusters[i]);
                Node node = new Node(features);
                nodeList.addNode(node);
                if (hierarchical_tree) {
                    node.setValues(calculateHierarchicalTree(features, method_linkage, calculate_genes, calculate_experiments));
                    event.setIntValue(i+1);
                    fireValueChanged(event);
                }
            }
            
            // prepare the result
            AlgorithmData result = new AlgorithmData();
            result.addParam("usedNumNeibs", String.valueOf(usedNumNeibs));
            result.addParam("origDataSetSize", String.valueOf(origDataSetSize));
            result.addParam("origClassSetSize", String.valueOf(origClassSetSize));
            if (this.useVarianceFilter) {
                result.addParam("postVarDataSetSize", String.valueOf(postVarDataSetSize));
                result.addParam("postVarClassSetSize", String.valueOf(postVarClassSetSize));
            }
            if (this.useCorrelFilter) {
                result.addParam("postCorrDataSetSize", String.valueOf(postCorrDataSetSize));
            }
            result.addCluster("cluster", result_cluster);
            result.addMatrix("clusters_means", means);
            result.addMatrix("clusters_variances", variances);
            return result;           
            
        } // end of if (!validate)
        
        return null; //for now
    }
    
    private NodeValueList calculateHierarchicalTree(int[] features, int method, boolean genes, boolean experiments) throws AlgorithmException {
	NodeValueList nodeList = new NodeValueList();
	AlgorithmData data = new AlgorithmData();
	FloatMatrix experiment;
        
        if(classifyGenes)
            experiment = getSubExperiment(this.expMatrix, features);
        else
            experiment = this.getSubExperimentReducedCols(this.expMatrix, features);
        
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
	    validate(result);
	    addNodeValues(nodeList, result);
	}
	return nodeList;
    }    
    
    private void addNodeValues(NodeValueList target_list, AlgorithmData source_result) {
	target_list.addNodeValue(new NodeValue("child-1-array", source_result.getIntArray("child-1-array")));
	target_list.addNodeValue(new NodeValue("child-2-array", source_result.getIntArray("child-2-array")));
	target_list.addNodeValue(new NodeValue("node-order", source_result.getIntArray("node-order")));
	target_list.addNodeValue(new NodeValue("height", source_result.getMatrix("height").getRowPackedCopy()));
    }
    
    private FloatMatrix getSubExperiment(FloatMatrix experiment, int[] features) {
	FloatMatrix subExperiment = new FloatMatrix(features.length, experiment.getColumnDimension());
	for (int i=0; i<features.length; i++) {
	    subExperiment.A[i] = experiment.A[features[i]];
	}
	return subExperiment;
    }
    
        /**
     *  Creates a matrix with reduced columns (samples) as during experiment clustering
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
     * Checking the result of hcl algorithm calculation.
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
    
    private int[] convert2int(Vector source) {
	int[] int_matrix = new int[source.size()];
	for (int i=0; i<int_matrix.length; i++) {
	    int_matrix[i] = ((Integer) source.get(i)).intValue();
	}
	return int_matrix;
    }    
    
    public void abort() {
	stop = true;
    }    
    
    private FloatMatrix getMeans(Vector[] clusters) {
	FloatMatrix means = new FloatMatrix(clusters.length, numCols);
	FloatMatrix mean;
	for (int i=0; i<clusters.length; i++) {
	    mean = getMean(clusters[i]);
	    means.A[i] = mean.A[0];
	}
	return means;
    }
    
    private FloatMatrix getMean(Vector cluster) {
	FloatMatrix mean = new FloatMatrix(1, numCols);
	float currentMean;
	int n = cluster.size();
	int denom = 0;
	float value;
	for (int i=0; i<numCols; i++) {
	    currentMean = 0f;
	    denom  = 0;
	    for (int j=0; j<n; j++) {
		value = expMatrix.get(((Integer) cluster.get(j)).intValue(), i);
		if (!Float.isNaN(value)) {
		    currentMean += value;
		    denom++;
		}
	    }
	    mean.set(0, i, currentMean/(float)denom);
	}
	
	return mean;
    }
    
    private FloatMatrix getVariances(Vector[] clusters, FloatMatrix means) {
	final int rows = means.getRowDimension();
	final int columns = means.getColumnDimension();
	FloatMatrix variances = new FloatMatrix(rows, columns);
	for (int row=0; row<rows; row++) {
	    for (int column=0; column<columns; column++) {
		variances.set(row, column, getSampleVariance(clusters[row], column, means.get(row, column)));
	    }
	}
	return variances;
    }
    
    int validN;
    
    private float getSampleNormalizedSum(Vector cluster, int column, float mean) {
	final int size = cluster.size();
	float sum = 0f;
	validN = 0;
	float value;
	for (int i=0; i<size; i++) {
	    value = expMatrix.get(((Integer) cluster.get(i)).intValue(), column);
	    if (!Float.isNaN(value)) {
		sum += Math.pow(value-mean, 2);
		validN++;
	    }
	}
	return sum;
    }
    
    private float getSampleVariance(Vector cluster, int column, float mean) {
	return(float)Math.sqrt(getSampleNormalizedSum(cluster, column, mean)/(float)(validN-1));
    }    
    
    private int getClassification(int row, int numNeibs) { // return zero if unclassified (in case of a tie)
        int[] classCounts = new int[numClasses + 1];
        for (int i = 0; i < classCounts.length; i++) {
            classCounts[i] = 0;
        }
        
        float[] distances = new float[filteredClassifierSet.size()];
        int numNeibsUsed;
        
        if (numNeibs <= filteredClassifierSet.size()) {
            numNeibsUsed = numNeibs;
        } else {
            numNeibsUsed = filteredClassifierSet.size();
        }
        
        usedNumNeibs = numNeibsUsed;
        
        for (int i = 0; i < filteredClassifierSet.size(); i++) {
            int currentClassifier = ((Integer)(filteredClassifierSet.get(i))).intValue();
            float currDist = ExperimentUtil.geneEuclidianDistance(expMatrix, null, row, currentClassifier, factor);
            distances[i] = currDist;
        }
        
        QSort sortDistances = new QSort(distances);
        int[] sortedDistIndices = sortDistances.getOrigIndx();
        
        for (int i = 0; i < numNeibsUsed; i++) {
            int currClassifierIndex = sortedDistIndices[i];
            int currClass = ((Integer)(filteredClasses.get(currClassifierIndex))).intValue();
            classCounts[currClass] = classCounts[currClass] + 1;
        }
        
        int maxCount = 0;
        for (int i = 1; i < classCounts.length; i++) {
            maxCount = Math.max(maxCount, classCounts[i]);
        }
        
        int numMaxCountEncountered = 0;
        int assignedClass = 0;
        for (int i = 1; i < classCounts.length; i++) {
            if (maxCount == classCounts[i]) {
                numMaxCountEncountered++;
                assignedClass = i;
            }
        }
        
        if (numMaxCountEncountered == 1) {
            return assignedClass;
        } else {
            return 0;
        }
    }
    
    private boolean passesCorrelationFilter(int row, Vector classifiers, double thresholdP, int permutations, long seed) {
        boolean passes = false;
        double rMax = getRMax(row, classifiers);
        float[] currentRow = new float[numCols];
        int timesExceeded = 0;
        
        long[] seedsArray = new long[permutations];
        Random rand = new Random(seed);
        for (int i = 0; i < seedsArray.length; i++) {
            seedsArray[i] = rand.nextLong();
        }
        
        for (int i = 0; i < permutations; i++) {            
            for (int j = 0; j < currentRow.length; j++) {
                currentRow[j] = expMatrix.A[row][j];
            }
            
            float[] permutedRow = getPermutedValues(row, seedsArray[i]);
            //DONE UP TO HERE 9_30_03            
            double permRMax = getPermRMax(permutedRow, classifiers);            
            if (permRMax > rMax) {
                timesExceeded++;
            }
            
        }
        
        double permPValue = (double)(timesExceeded)/(double)(permutations);
        if (permPValue <= thresholdP) {
            passes = true;
        } else {
            passes = false;
        }
        
        return passes;
    }
    
    private double getPermRMax(float[] rowValues, Vector classifiers) {
        double permRMax = Double.NEGATIVE_INFINITY;
        
        for (int i = 0; i < classifiers.size(); i++) {
            int currRow = ((Integer)(classifiers.get(i))).intValue();
            float[] currentRowValues = getRowValues(currRow);
            double currentR = getCorr(rowValues, currentRowValues);
            permRMax = Math.max(permRMax, currentR);
        }
        
        return permRMax;
    }
    
    private double getCorr(float[] arrX, float[] arrY) {
        //double corr;
	int nArrSize = arrX.length;
	
	double dblXY = 0f;
	double dblX  = 0f;
	double dblXX = 0f;
	double dblY  = 0f;
	double dblYY = 0f;
	
	double v_1, v_2;
	int iValidValCount = 0;
	for (int i=0; i<nArrSize; i++) {
	    v_1 = arrX[i];
	    v_2 = arrY[i];
	    if (Double.isNaN(v_1) || Double.isNaN(v_2)) {
		continue;
	    }
	    iValidValCount++;
	    dblXY += v_1*v_2;
	    dblXX += v_1*v_1;
	    dblYY += v_2*v_2;
	    dblX  += v_1;
	    dblY  += v_2;
	}
	if (iValidValCount == 0)
	    return 0d;
	
	//Allows for a comparison of two 'flat' genes (genes with no variability in their
	// expression values), ie. 0, 0, 0, 0, 0
	boolean nonFlat = false;
	NON_FLAT_CHECK: for (int j = 1; j < nArrSize; j++) {
	    if ((!Float.isNaN(arrX[j])) && (!Float.isNaN(arrY[j]))) {
		if (arrX[j] != arrX[j-1]) {
		    nonFlat = true;
		    break NON_FLAT_CHECK;
		}
		if (arrY[j] != arrY[j-1]) {
		    nonFlat = true;
		    break NON_FLAT_CHECK;
		}
	    }
	}
	
	if (nonFlat == false) {
	    return 1.0d;
	}
	
	
	double dblAvgX = dblX/iValidValCount;
	double dblAvgY = dblY/iValidValCount;
	double dblUpper = dblXY-dblX*dblAvgY-dblAvgX*dblY+dblAvgX*dblAvgY*((double)iValidValCount);
	double p1 = (dblXX-dblAvgX*dblX*2d+dblAvgX*dblAvgX*((double)iValidValCount));
	double p2 = (dblYY-dblAvgY*dblY*2d+dblAvgY*dblAvgY*((double)iValidValCount));
	double dblLower = p1*p2;
	return(double)(dblUpper/(Math.sqrt(dblLower)+Double.MIN_VALUE)*(double)factor);        
        
        //return corr;
    }
    
    private float[] getRowValues(int row) {
        float[] rowValues = new float[numCols];
        
        for (int i = 0; i < rowValues.length; i++) {
            rowValues[i] = expMatrix.A[row][i];
        }
        
        return rowValues;
    }
    
    private float[] getPermutedValues(int row, long seed) {
        float[] rowValues = new float[numCols];
        float[] permutedRowValues = new float[numCols];
        
        for (int i = 0; i < rowValues.length; i++) {
            rowValues[i] = expMatrix.A[row][i];
        }

        /*
        System.out.print("Original row: ");
        for (int i = 0; i < rowValues.length; i++) {
            System.out.print(rowValues[i] + " ");
        }
        System.out.println();
         */
        Random generator2 = new Random(seed);
        for (int i = rowValues.length; i > 1; i--) {
            //Random generator2 = new Random();
            int randVal = generator2.nextInt(i - 1);
            float temp = rowValues[randVal];
            rowValues[randVal] = rowValues[i - 1];
            rowValues[i - 1] = temp;
        }
        /*
        System.out.print("Permuted row: ");
        for (int i = 0; i < rowValues.length; i++) {
            System.out.print(rowValues[i] + " ");
        }
        System.out.println();
        System.out.println();
         */
        
        /*
        try {
            Thread.sleep(10);
        } catch (Exception exc) {
            exc.printStackTrace();
        }
         */
        
        return rowValues;
        
    }
    
    private double getRMax(int row, Vector classifiers) {        
        double rMax = Double.NEGATIVE_INFINITY;
        
        for (int i = 0; i < classifiers.size(); i++) {
            int currRow = ((Integer)(classifiers.get(i))).intValue();
            double currentR = ExperimentUtil.genePearson(expMatrix, null, currRow, row, factor);
            rMax = Math.max(rMax, currentR);
        }
        
        return rMax;
    }
    
    private boolean isFoundInVector(int element, Vector vect) {
        boolean found = false;
        for (int i = 0; i < vect.size(); i++) {
            if (element == ((Integer)(vect.get(i))).intValue()) {
                found = true;
                break;
            }
        }
        return found;
    }
    
    private int[] reverse(int[] arr) {
        int[] revArr = new int[arr.length];
        int  revCount = 0;
        int count = arr.length - 1;
        for (int i=0; i < arr.length; i++) {
            revArr[revCount] = arr[count];
            revCount++;
            count--;
        }
        return revArr;
    }    
    
    private double getVar(int row) {
        float[] rowValues = new float[numCols];
        for (int i = 0; i < rowValues.length; i++) {
            rowValues[i] = expMatrix.A[row][i];
        }
        return getVar(rowValues);
    }
    
    private double getVar(float[] rowValues) {
	float mean = getMean(rowValues);
	int n = 0;
	
	float sumSquares = 0;
	
	for (int i = 0; i < rowValues.length; i++) {
	    if (!Float.isNaN(rowValues[i])) {
		sumSquares = (float)(sumSquares + Math.pow((rowValues[i] - mean), 2));
		n++;
	    }
	}
        
        if (n < 2) {
            return Float.NaN;
        }
	
	float var = sumSquares / (float)(n - 1);
	if (Float.isInfinite(var)) {
            return Double.NaN;
        }  else {
            return (double)var;
        }
    }
    
    private float getMean(float[] group) {
	float sum = 0;
	int n = 0;
	
	for (int i = 0; i < group.length; i++) {
	    //System.out.println("getMean(): group[" + i + "] = " + group[i]);
	    if (!Float.isNaN(group[i])) {
		sum = sum + group[i];
		n++;
	    }
	}
	
	//System.out.println("getMean(): sum = " +sum);
	if (n == 0) {
            return Float.NaN;
        }
	float mean =  sum / (float)n;
        
        if (Float.isInfinite(mean)) {
            return Float.NaN;
        }
        
	return mean;
    }    
    
}











