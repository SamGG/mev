/*
Copyright @ 1999-2005, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: DAM.java,v $
 * $Revision: 1.3 $
 * $Date: 2006-03-24 15:49:51 $
 * $Author: eleanorahowe $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.algorithm.impl;

import java.text.DecimalFormat;
import java.util.Vector;

import javax.swing.JOptionPane;

import org.tigr.microarray.mev.cluster.Cluster;
import org.tigr.microarray.mev.cluster.Node;
import org.tigr.microarray.mev.cluster.NodeList;
import org.tigr.microarray.mev.cluster.algorithm.AbortException;
import org.tigr.microarray.mev.cluster.algorithm.AbstractAlgorithm;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmData;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmEvent;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmException;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmParameters;
import org.tigr.util.FloatMatrix;

import JSci.maths.statistics.TDistribution;
import Jama.Matrix;

public class DAM extends AbstractAlgorithm {
    
    private boolean stop = false;

    private Matrix expMatrix;
    private Matrix expMatrixTranspose;
    private Matrix responseMatrix;
    private Matrix testDataMatrix;
    private Matrix trainingMatrix;

    private int numberOfGenes=0;
    private int numberOfSamples=0;

    private int numberOfClasses=0;
    private int kValue=0;
    private int[] trainingIndices; // experiment indices array for classification 
    private int[] testIndices; // test data indices array for classification 
    private int[] classes;    // class number array for classification

    private int whichAlgorithm = 0;
    private boolean isPDA = true;

    private boolean preSelectGenes = true;
    private boolean performLOOCV = true;

    private double alpha = 0.05; // default alpha is set to be 0.05

    private int numberOfSelectedGenes=0;

    private int[] geneRank;
    private int highestGeneRank;
    private Vector[] selectedGeneIndices;

    private int[] usedGeneIndices;
    private int[] unusedGeneIndices;

    final private int used=0;
    final private int unused=1;

    private Vector[][] reducedGeneSetForA2;   // reduced gene set for A2 algorithm only

    private Vector[] reducedGeneSet;
    private Vector[] clusters;

    private Vector[] classified;

    private Matrix classExpSumMatrix; // gene expression sum for each class
    private int[] classSampleArray; // number of samples in each class
 
    private Matrix geneComponentMatrix;

    private Matrix [] beta;
    private Matrix [] A_Matrix;
    private Matrix [] C_Matrix;

    private double [] cValues;

    private boolean [] singularMatrix;

    public AlgorithmData execute(AlgorithmData data) throws AlgorithmException {

	 expMatrix = getJamaMatrix(data.getMatrix("experiment"));
         expMatrixTranspose = expMatrix.transpose();

	 numberOfGenes = expMatrix.getRowDimension();
	 numberOfSamples = expMatrix.getColumnDimension();

	 AlgorithmParameters map = data.getParams();
	 int function = map.getInt("distance-function", COVARIANCE);
	 float factor = map.getFloat("distance-factor", 1.0f);
	 boolean absolute = map.getBoolean("distance-absolute", false);
	 int mode = map.getInt("dam-mode", 0);

         preSelectGenes = map.getBoolean("preSelectionGenes", false);
         whichAlgorithm = map.getInt("algorithmSelection", 0);
         isPDA = map.getBoolean("isPDA", true);
         preSelectGenes = map.getBoolean("preSelectGenes", true);
         numberOfClasses = map.getInt("numberOfClasses", 3);
         kValue = map.getInt("kValue", 3);
         alpha = (double) map.getFloat("alpha", 0.05f);

 /*        System.out.println("");
         System.out.println("DAM.java: numberOfGenes = " + numberOfGenes);
         System.out.println("DAM.java: numberOfSamples = " + numberOfSamples);
	 System.out.println("DAM.java: Algorithm = A" + whichAlgorithm);
	 System.out.println("DAM.java: Perform PDA = " + isPDA);
	 System.out.println("DAM.java: preSelectGenes = " + preSelectGenes);
	 System.out.println("DAM.java: numberOfClasses = " + numberOfClasses);
	 System.out.println("DAM.java: kValue = " + kValue);
	 System.out.println("DAM.java: alpha = " + alpha);
         System.out.println("");
   */     
         singularMatrix = new boolean[numberOfSamples];

         trainingIndices = data.getIntArray("trainingIndices");
         classes = data.getIntArray("classes");
         testIndices = data.getIntArray("testIndices");

     //    System.out.println("DAM.java: trainingIndices: ");
/*	 for(int i=0; i<trainingIndices.length; i++) {
             System.out.print(trainingIndices[i] + ", ");
         }
         System.out.println("");
         System.out.println("");

         System.out.println("DAM.java: classes: ");
	 for(int i=0; i<classes.length; i++) {
             System.out.print(classes[i] + ", ");
         }
         System.out.println("");
         System.out.println("");

         System.out.println("DAM.java: testIndices: ");
	 for(int i=0; i<testIndices.length; i++) {
             System.out.print(testIndices[i] + ", ");
         }

         System.out.println("");
         System.out.println("");


         System.out.print("DAM.java: expMatrix");
         for (int i=0; i<numberOfGenes; i++) {
             System.out.println("");
             for (int j=0; j<numberOfSamples; j++) {
                 System.out.print(expMatrix.get(i,j) + ",   ");
             }
             System.out.println("");
             System.out.println("");
         }
         System.out.println("");
*/


         responseMatrix = new Matrix(numberOfSamples, numberOfClasses);

         for (int row=0; row<numberOfSamples; row++) {
             for (int column=0; column<numberOfClasses; column++) {
	         responseMatrix.set(row, column, 0);
             }
         }

	 for (int i=0; i<trainingIndices.length; i++) {
	     responseMatrix.set(trainingIndices[i], classes[i]-1, 1.0);
	 }

/*
         System.out.println("");
         System.out.println("DAM.java: responseMatrix");
         for(int i=0; i<numberOfSamples; i++) {
             for(int j=0; j<numberOfClasses; j++) {
                 System.out.print(responseMatrix.get(i,j) + ",  ");
             }
             System.out.println("");
         }
         System.out.println("");
*/

         if (trainingIndices.length > 0) {
             trainingMatrix = new Matrix(numberOfGenes, trainingIndices.length);
             trainingMatrix = expMatrix.getMatrix(0, numberOfGenes-1, trainingIndices);

/*
	     System.out.println("");
	     System.out.println("DAM.java: trainingMatrix "+ trainingMatrix.getRowDimension() + "x" + trainingMatrix.getColumnDimension());

	     for (int i=0; i<trainingMatrix.getRowDimension(); i++) {
		 for (int j=0; j<trainingMatrix.getColumnDimension(); j++) {
		     System.out.print(trainingMatrix.get(i,j) + ", ");
		 }
		 System.out.println("");
		 System.out.println("");
	     }
	     System.out.println("");
*/
         }

         if (testIndices.length > 0) {
             testDataMatrix = new Matrix(numberOfGenes, testIndices.length);
             testDataMatrix = expMatrix.getMatrix(0, numberOfGenes-1, testIndices);

/*
	     System.out.println("");
	     System.out.println("DAM.java: testMatrix " + testDataMatrix.getRowDimension() + "x" + testDataMatrix.getColumnDimension());
	     System.out.println("");
*/
         }


         classExpSumMatrix = new Matrix(numberOfGenes,numberOfClasses);
         classExpSumMatrix = expMatrix.times(responseMatrix);

/*
         System.out.print("DAM.java: classExpSumMatrix ");
         for (int i=0; i<25; i++) {
             System.out.println("");
             for (int j=0; j<numberOfClasses; j++) {
                 System.out.print(classExpSumMatrix.get(i,j) + ",  ");
             }
         }
         System.out.println("");
         System.out.println("");
*/

         classSampleArray = new int[numberOfClasses]; 

//         System.out.println("DAM.java: classSampleArray: ");

         for (int classId=0; classId<numberOfClasses; classId++) {
            for (int sample=0; sample<numberOfSamples; sample++) {
                classSampleArray[classId] += responseMatrix.get(sample, classId); 
            }
            
 //           System.out.print(classSampleArray[classId] + ",  ");
         }
//         System.out.println("");
//         System.out.println("");

         geneRank = new int[numberOfGenes];

         reducedGeneSet = new Vector[2];
         reducedGeneSet[used] = new Vector();
         reducedGeneSet[unused] = new Vector();

	 reducedGeneSetForA2 = new Vector[numberOfSamples][2];

         for (int leaveOutSample=0; leaveOutSample<numberOfSamples; leaveOutSample++) {
		 reducedGeneSetForA2[leaveOutSample][used] = new Vector();
		 reducedGeneSetForA2[leaveOutSample][unused] = new Vector();
         }

         Matrix probFunction = new Matrix(numberOfSamples, numberOfClasses);

         Matrix [] beta = new Matrix[numberOfClasses];

	 Vector[] classifiers = new Vector[numberOfClasses + 1];
	 classified = new Vector[numberOfClasses + 1];
	 Vector[] classifierPlusClassified = new Vector[numberOfClasses + 1];

	 for (int i = 0; i < numberOfClasses+1; i++) {
	     classifiers[i] = new Vector();
	     classified[i] = new Vector();
	     classifierPlusClassified[i] = new Vector();
	 }


         //jcb, add in initial classification to set the proper response matrix
         //to verify.  Note that responseMatrix is set to initial result.         
     //    probFunction = InitialClassification(expMatrix);         
     //    if(whichAlgorithm != 3) { //if the algorithm is not just a classification try 
                                    // to adjust response matrix to initial classification result
     //        responseMatrix = extractResult(probFunction);             
      //   }
         
         
         switch (whichAlgorithm) {
            case 0: 
                    probFunction = A0Algorithm(expMatrix);
                    break;
            case 1: 
                    probFunction = A1Algorithm(expMatrix);
                    break;
            case 2: 
                    probFunction = A2Algorithm(expMatrix);
                    break;
            case 3: 
                    probFunction = InitialClassification(expMatrix);
                    break;
            default: 
                    A0Algorithm(expMatrix);
                    break;
         }

         if (probFunction == null) {
            throw new AbortException();
         }

	 for (int i = 0; i < trainingIndices.length; i++) {
	     classifiers[classes[i]].add(new Integer(trainingIndices[i])); 

//             System.out.println("DAM.java - classifiers[" + classes[i] + "] add: " + trainingIndices[i]);
	 }


         if (whichAlgorithm == 3) {  // for InitialClassification only
	     for (int i = 0; i < classifiers.length; i++) {
		 for (int j=0; j<classifiers[i].size(); j++) {
		     classifierPlusClassified[i].add(classifiers[i].get(j));;
		 }              
	     }
	     for (int i = 0; i < classified.length; i++) {
		 for (int j=0; j<classified[i].size(); j++) {
		     classifierPlusClassified[i].add(classified[i].get(j));;
		 }
	     }
         }

  /*       System.out.println(" ");
	 for (int i = 0; i < classifiers.length; i++) {
             System.out.println("DAM.java - classifiers[" + i + "].size() = " + classifiers[i].size());
	 }
 
         System.out.println(" ");
	 for (int i = 0; i < classified.length; i++) {
             System.out.println("DAM.java - classified[" + i + "].size() = " + classified[i].size());
	 }

         System.out.println(" ");
	 for (int i = 0; i < classifierPlusClassified.length; i++) {
             System.out.println("DAM.java - classifierPlusClassified[" + i + "].size() = " + classifierPlusClassified[i].size());
	 }
*/

         clusters = new Vector[numberOfClasses*3]; 
         for (int i = 1; i <= numberOfClasses; i++) {
             clusters[i-1] = classifiers[i];
             clusters[i-1 + numberOfClasses] = classified[i];
             clusters[i-1 + numberOfClasses*2] = classifierPlusClassified[i];
         }

 /*        for (int i = 1; i <= numberOfClasses; i++) {
             System.out.println("DAM.java - clusters 1 size " + clusters[i-1].size());
             System.out.println("DAM.java - clusters 2 size " + clusters[i-1 + numberOfClasses].size());
             System.out.println("DAM.java - clusters 3 size " + clusters[i-1 + numberOfClasses*2].size());
         }
 */
          
         Matrix means = getMeans(clusters);
         Matrix variances = getVariances(clusters, means);

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
         }

         if (whichAlgorithm == 2) {
	     usedGeneIndices = new int[reducedGeneSetForA2[0][used].size()];
	     unusedGeneIndices = new int[reducedGeneSetForA2[0][unused].size()];

	     for (int i = 0; i < usedGeneIndices.length; i++) {
		usedGeneIndices[i] = ((Integer)(reducedGeneSetForA2[0][used].get(i))).intValue();
	     }

	     for (int i = 0; i < unusedGeneIndices.length; i++) {
		unusedGeneIndices[i] = ((Integer)(reducedGeneSetForA2[0][unused].get(i))).intValue();
	     }
         } else {

	     usedGeneIndices = new int[reducedGeneSet[used].size()];
	     unusedGeneIndices = new int[reducedGeneSet[unused].size()];

	     for (int i = 0; i < usedGeneIndices.length; i++) {
		usedGeneIndices[i] = ((Integer)(reducedGeneSet[used].get(i))).intValue();
	     }

	     for (int i = 0; i < unusedGeneIndices.length; i++) {
		unusedGeneIndices[i] = ((Integer)(reducedGeneSet[unused].get(i))).intValue();
	     }
         }

         int rowDim = means.getRowDimension();

         Cluster gene_cluster = new Cluster();
         nodeList = gene_cluster.getNodeList();
         for (int i=0; i<reducedGeneSet.length; i++) {
             if (stop) {
                 throw new AbortException();
             }
             features = convert2int(reducedGeneSet[i]);

             Node node = new Node(features);
             nodeList.addNode(node);
         }

         Matrix means_used = getMeansForGenes(reducedGeneSet);
         Matrix variances_used = getVariancesForGenes(reducedGeneSet, means_used);

         Matrix means_unused = getMeansForGenes(reducedGeneSet);
         Matrix variances_unused = getVariancesForGenes(reducedGeneSet, means_used);


         // construct the result
         AlgorithmData result = new AlgorithmData();

         result.addParam("numberOfGenes", String.valueOf(numberOfGenes));
         result.addMatrix("probFunction", getFloatMatrix(probFunction));

/*
         System.out.println("DAM.java -- geneComponentMatrix ");
         printMatrix(geneComponentMatrix);
*/

         FloatMatrix tempMatrix = getFloatMatrix(geneComponentMatrix).transpose();  
         FloatMatrix matrix3D;  

         if(tempMatrix.getColumnDimension() == 2) {
             matrix3D = new FloatMatrix(tempMatrix.getRowDimension(), 3);
             matrix3D.setMatrix(0, tempMatrix.getRowDimension()-1, 0, tempMatrix.getColumnDimension()-1, tempMatrix);
             for (int i=0; i< tempMatrix.getRowDimension(); i++) {
                 matrix3D.set(i, 2, 0);
             }
         } else {
             matrix3D = tempMatrix;
         }

//         System.out.println("DAM.java -- matrix3D ");
//         printMatrix(matrix3D);
 
         result.addMatrix("matrix3D", matrix3D);

         result.addCluster("cluster", result_cluster);
         result.addCluster("geneCluster", gene_cluster);

         result.addMatrix("clusters_means", getFloatMatrix(means));
         result.addMatrix("clusters_variances", getFloatMatrix(variances));

         result.addMatrix("clusters_means_used", getFloatMatrix(means_used));
         result.addMatrix("clusters_variances_used", getFloatMatrix(variances_used));

         result.addMatrix("clusters_means_unused", getFloatMatrix(means_unused));
         result.addMatrix("clusters_variances_unused", getFloatMatrix(variances_unused));

/*
         if (whichAlgorithm == 2) {
	     for (int leaveOutSample=0; leaveOutSample<numberOfSamples; leaveOutSample++) {

   	         System.out.println("DAM.java: Used Gene Index for sample "  + leaveOutSample); 

	         for(int i=0; i< reducedGeneSetForA2[leaveOutSample][used].size(); i++) {
		     System.out.print(((Integer)(reducedGeneSetForA2[leaveOutSample][used].get(i))).intValue() + ", ");
                 }
		 System.out.println(" ");
		 System.out.println(" ");

  	         System.out.println("DAM.java: Unsed Gene Index for sample "  + leaveOutSample); 

	         for(int i=0; i< reducedGeneSetForA2[leaveOutSample][unused].size(); i++) {
		     System.out.print(((Integer)(reducedGeneSetForA2[leaveOutSample][unused].get(i))).intValue() + ", ");
                 }
		 System.out.println(" ");
		 System.out.println(" ");
	     }
	     System.out.println(" ");

         } else {
	     System.out.println("DAM.java: usedGeneIndices size: " + usedGeneIndices.length); 
	     for(int i=0; i< usedGeneIndices.length; i++) {
		 System.out.print(usedGeneIndices[i] + ", "); 
	     }
	     System.out.println(" ");
	     System.out.println(" ");
	     System.out.println("DAM.java: unusedGeneIndices size: " + unusedGeneIndices.length); 
	     for(int i=0; i< unusedGeneIndices.length; i++) {
		 System.out.print(unusedGeneIndices[i] + ", "); 
	     }
	     System.out.println(" ");
	     System.out.println(" ");
         }

*/

         result.addIntArray("usedGeneIndices", usedGeneIndices);
         result.addIntArray("unusedGeneIndices", unusedGeneIndices);

	 return result;
    }


    
    //jcb, get a new responseMatrix from the initial result
    
    private Matrix extractResult(Matrix pMatrix) {
    
         Matrix newMatrix = new Matrix(numberOfSamples, numberOfClasses);
         int rows = pMatrix.getRowDimension();
         int cols = pMatrix.getColumnDimension();
         
        //set to zero
                 for (int row=0; row<rows; row++) {
             for (int column=0; column<cols; column++) {
	         newMatrix.set(row, column, 0);
             }
         }
        
       double [][] array = pMatrix.getArray();
       int classID = 0; 
       
        for(int row = 0; row < rows; row++) {
            classID = getClassID(array[row]);
            newMatrix.set(row, classID, 1.0);
        }
        
        return newMatrix;
    }
    
    //jcb, returns classID for a given row in the prob matrix
    
    private int getClassID(double [] probs) {
        int hasMax = 0;
        double max = 0.0, currMax = 0.0;
        for(int i = 0; i < probs.length; i++) {
            currMax = Math.max(max, probs[i]);
            if(currMax > max) {
                hasMax = i;
                max = currMax;
            }
        }
        return hasMax;
    }

    /**
     *  geneSelection(): 
     *  This method compares all (G+1, 2) pairwise (absolute) mean differences ( G+1 = numberOfClasses )
     *  |mean(Xk)-mean(Xk')| (k!=k') to a critical score
     *     t*sqrt(MSE(1/nk + 1/nk'))
     *  where MSE(mean square error) is the estimate of vairability from ANOVA model
     *  with one factor and G+1 groups, t is t(alpha/2, N-(G+1)) value of t-distribution.
     *  Each gene (j=1, 2, ... p) is ranked according to the number of times the pairwise 
     *  absolute mean difference exceeded the critical score.
     *
     *  Input: none
     *  Output: An array of rank vectors that contains the gene indices of that rank 
     */
    public Vector[] geneSelection(Matrix expMatrix) {

         
//	 System.out.println(" ");
//	 System.out.println("**************** Begin geneSelection() *************** ");
//	 System.out.println(" ");

         Vector[] geneIndices;

	 double tValue=0;
	 double meanSquareError=0;

         int numOfGenes = expMatrix.getRowDimension();
         int numOfSamples = expMatrix.getColumnDimension();

	 double [][] criticalScores = new double[numberOfClasses][numberOfClasses];
         double [][] meanDifferences = new double[numberOfClasses][numberOfClasses];

	 // Obtain the t value for the given alpha value and numberOfClasses

         AlgorithmEvent event = new AlgorithmEvent(this, AlgorithmEvent.SET_UNITS, numberOfGenes, "Gene Screening\n");
         fireValueChanged(event);
         event.setId(AlgorithmEvent.PROGRESS_VALUE);            

 /*        System.out.println("");
         System.out.println("geneSelection() - numOfGenes = " + numOfGenes);
         System.out.println("geneSelection() - numOfSamples = " + numOfSamples);
         System.out.println("geneSelection() - numberOfClasses = " + numberOfClasses);
         System.out.println("geneSelection() - alpha = " + alpha);
*/
         TDistribution testT = new TDistribution(numOfSamples-(numberOfClasses+1));
	 tValue = -testT.inverse(alpha/2.0d);

	 double tValue1 = getTValue(numOfSamples-(numberOfClasses+1), alpha);

     //    System.out.println("geneSelection() - tValue = " + tValue);
     //    System.out.println("geneSelection() - tValue1 = " + tValue1);

         if (tValue < - 1.0) {
             return null;
         }

    //     System.out.println("");
    //     System.out.println("geneSelection() - tValue = " + tValue);

	 // Obtain the Mean Square Error Estimate
	 meanSquareError = getSampleVariance(expMatrix, getSampleMeans(expMatrix));
     //    System.out.println("geneSelection() - meanSquareError = " + meanSquareError);
 
         // For each gene
         for (int gene=0; gene < numOfGenes; gene++) {

	     for(int class1=0; class1<numberOfClasses; class1++) {

		 for(int class2=class1+1; class2<numberOfClasses; class2++) {

	           // Calculate the critical score for each pair of classes among all classes
		   criticalScores[class1][class2] = tValue * 
			     Math.sqrt(meanSquareError*(1.0/classSampleArray[class1] + 1.0/classSampleArray[class2]));

	           // Calculate the pairwise absolute mean differneces for all classes 
		   meanDifferences[class1][class2] = Math.abs(
                          classExpSumMatrix.get(gene, class1)/classSampleArray[class1]-
                          classExpSumMatrix.get(gene, class2)/classSampleArray[class2]); 

                   // rank the genes
                   if (meanDifferences[class1][class2] > criticalScores[class1][class2]) {
                       geneRank[gene] ++ ;
                   }
                }
	     }
         }

         // Find highest rank
         highestGeneRank = 0;

         for (int gene=0; gene<numOfGenes; gene++) {
            if (geneRank[gene] > highestGeneRank) {
               highestGeneRank = geneRank[gene];
            }
         }

    //     System.out.println("");
    //     System.out.println("geneSelection() - highestGeneRank = " + highestGeneRank);

         // Calculate the rank vector for storing gene indices
         geneIndices = new Vector[highestGeneRank+1]; 


         for (int rank=0; rank<highestGeneRank+1; rank++) {
            geneIndices[rank] = new Vector(); 
            for (int gene=0; gene<numOfGenes; gene++) {

                if (geneRank[gene] == rank) {
                    geneIndices[rank].add(new Integer(gene));
                    
                    if (rank > 0) {
                        geneIndices[0].remove(new Integer(gene));
                    }
                }

            }
         }

     /*    for (int rank=0; rank<highestGeneRank+1; rank++) {
             System.out.println("geneSelection() - rank = " + rank + ", geneIndices[rank].size()=" + geneIndices[rank].size());
         }
         System.out.println("");

	 System.out.println(" ");
	 System.out.println("**************** End geneSelection() *************** ");
	 System.out.println(" ");
*/
         return geneIndices;

    } // end of geneSelection



    /**
     * MLE Algorithm:
     *     qualitative response variable: y = k where k = 0, 1, 2, ... G
     *     gene expression levels from one experiment: x = (x1, x2, ... xp) 
     *     probability: P(y = k | x) = exp(gk(x))/(1+exp(g0(x)) + exp(g1(x)) + ... exp(gK(x))) 
     *     where gk(x) = betak0*x0 + betak1*x1 +...+ betakp*xp
     *     beta = (beta1, .... betak) with betak= (betak0, betak1,..., betakp);
     *     bata can be estimated from MLE when numberOfSamples > numberOfClasses * numberOfGenes.
     * 
     *     beta_Matrix: beta Matrix of size [numberOfClasses*(numberOfGenes+1), 1];
     *     Z_Matrix: Indicator Matrix of size [numberOfSamples, (numberOfClasses+1)]
     *     P_Matrix: Probability Matrix of size [numberOfGenes, (numberOfClasses+1)]
     *     I_Matrix: Information Matrix of size 
     *               [numberOfClasses*(numberOfGenes+1), numberOfClasses*(numberOfGenes+1)]
     *     S_Matrix: Score Matrix of size [numberOfClasses*(numberOfGenes+1), 1]
     *     W_Matrices[numberOfClasses*numberOfClasses]: 
     *           An array of diagonal matrix of size [numberOfSamples, numberOfSamples]
     *
     *     S(beta) = (delta(1)(x(1) - X'w(1))) + ... + (delta(N)(x(N) - X'w(N))
     *     Information Matrix:
     *     I(beta) = delta(1)[X'w(1)X - (X'w(1)(w(1)'X)] + ... +
     *     delta(1)[X'w(N)X - (X'w(N)(w(N)'X)] 
     * 
     *     NewTon-Raphson algorithm:
     *       beta(s+1) = beta(s) + inverse(I(beta))*(beta(s)*S(beta(s)))
     * 
     *     If the NewTon-Raphson algorithm converges, the vector of coefficients is 
     *     the maximum partial likelihood estimate of beta.
     */                     
    public Matrix [] mleAlgorithm(Matrix compMatrix, Matrix compResponseMatrix) throws AlgorithmException{

/*	System.out.println(" ");
 	System.out.println("******************** Begin mleAlgorithm() ********************");
	System.out.println(" ");

*/
        int numOfGenes = compMatrix.getRowDimension();
        int numOfSamples = compMatrix.getColumnDimension();
/*
        System.out.println(" ");
        System.out.println("mleAlgorithm() - numOfGenes = " + numOfGenes);
        System.out.println("mleAlgorithm() - numOfSamples = " + numOfSamples);
*/

        AlgorithmEvent event = new AlgorithmEvent(this, AlgorithmEvent.SET_UNITS, numberOfGenes, "MLE Algorithm \n");
        fireValueChanged(event);
        event.setId(AlgorithmEvent.PROGRESS_VALUE);            

        Matrix [] X = new Matrix[numOfSamples]; // an array of sample matrices
        for (int i=0; i<numOfSamples; i++) {
            X[i] = new Matrix(numOfGenes+1, 1);
            X[i].set(0, 0, 1);
            X[i].setMatrix(1, numOfGenes, 0, 0, compMatrix.getMatrix(0, numOfGenes-1, i, i));
        }

        // X_Matrix: obtained from compMatrix by adding 1's in the first row
        Matrix X_Matrix = new Matrix(numOfGenes+1, numOfSamples);
        for (int i=0; i<numOfSamples; i++) {
            X_Matrix.set(0, i, 1);
        }
        X_Matrix.setMatrix(1, numOfGenes, 0, numOfSamples-1, compMatrix);

/*
	System.out.println(" ");
	System.out.println("mleAlogrithm() - X_Matrix : ");
	for (int i=0; i<X_Matrix.getRowDimension(); i++) {
	    for (int j=0; j<X_Matrix.getColumnDimension(); j++) {
	        System.out.print(X_Matrix.get(i,j) + ", ");
	    }
	    System.out.println(" ");
	    System.out.println(" ");
	}
*/

        Matrix beta_Matrix = new Matrix(numberOfClasses*(numOfGenes+1), 1);
        Matrix new_beta_Matrix = new Matrix(numberOfClasses*(numOfGenes+1), 1);

        Matrix [] oldBeta = new Matrix[numberOfClasses];
        Matrix [] newBeta = new Matrix[numberOfClasses];
        for (int k=0; k< numberOfClasses; k++) {
            oldBeta[k] = new Matrix(numOfGenes+1, 1);
            newBeta[k] = new Matrix(numOfGenes+1, 1);
        }

        // Probability Matrix (numOfSamples x numberOfClasses )
        Matrix P_Matrix = new Matrix(numOfSamples, numberOfClasses); 

        // Score Matrix (numOfSamples*(numberOfGenes+1) x 1 )
        Matrix S_Matrix = new Matrix(numberOfClasses*(numOfGenes+1), 1); 

        // A Matrix of Diagonal Matrices
        Matrix [][] W_Matrices = new Matrix[numberOfClasses][numberOfClasses]; 

        for (int i=0; i< numberOfClasses; i++) {
            for (int j=0; j< numberOfClasses; j++) {
                W_Matrices[i][j] = new Matrix(numOfSamples,numOfSamples);
            }
        }

        // Information Matrix (numberOfClasses*(numOfGenes+1), numberOfClasses*(numOfGenes+1));
        Matrix I_Matrix = new Matrix(numberOfClasses*(numOfGenes+1), numberOfClasses*(numOfGenes+1));

        Matrix [][] I = new Matrix[numberOfClasses][numberOfClasses];
        for (int i=0; i<numberOfClasses; i++) {
            for (int j=0; j<numberOfClasses; j++) {
                I[i][j] = new Matrix (numOfGenes+1, numOfGenes+1);
            }
        }


        // Initialize oldBeta and newBeta variables 
        for (int k=0; k<numberOfClasses; k++) {
            for(int row=0; row<numOfGenes+1; row++) {
                oldBeta[k].set(row, 0, 0.1f);
                newBeta[k].set(row, 0, 0.1f);
            }
        }

        // Initialize beta_Matrix
        for (int i=0; i<numberOfClasses; i++) {
            for(int j=0; j<numOfGenes+1; j++) {
                beta_Matrix.set(i*j, 0, oldBeta[i].get(j,0));
                new_beta_Matrix.set(i*j, 0, oldBeta[i].get(j,0));
            }
        }

        // Obtain the Z_matrix: Indicator Matrix ( numOfSamples x numberOfClasses )
        Matrix Z_Matrix = compResponseMatrix.getMatrix(0, numOfSamples-1, 0, numberOfClasses-1); 

/*
	System.out.println(" ");
	System.out.println("mleAlogrithm() - responseMatrix : ");
	for (int i=0; i<responseMatrix.getRowDimension(); i++) {
	    for (int j=0; j<responseMatrix.getColumnDimension(); j++) {
	        System.out.print(responseMatrix.get(i,j) + ", ");
	    }
	    System.out.println(" ");
	    System.out.println(" ");
	}

	System.out.println(" ");
	System.out.println("mleAlogrithm() - Z_Matrix : ");
	for (int i=0; i<Z_Matrix.getRowDimension(); i++) {
	    for (int j=0; j<Z_Matrix.getColumnDimension(); j++) {
	        System.out.print(Z_Matrix.get(i,j) + ", ");
	    }
	    System.out.println(" ");
	    System.out.println(" ");
	}
*/

        double g=0.0, c=0.0, localSum=0.0;
        
        // Main iteration for obtain beta estimator
        for (int iteration = 0; iteration < 40; iteration++) {

            oldBeta = newBeta;

/*
	    System.out.println(" ");
	    System.out.println("mleAlogrithm() - oldBeta[i] : ");
	    for (int i=0; i<numberOfClasses; i++) {
		for (int j=0; j<numOfGenes+1; j++) {
		    System.out.print(oldBeta[i].get(j,0) + ", ");
		}
		System.out.println(" ");
		System.out.println(" ");
	    }
*/

            // Calculate P_Matrix 
	    for (int i=0; i<numOfSamples; i++) {
		for (int j=0; j<numberOfClasses; j++) {
		    localSum += Math.exp(((X[i].transpose()).times(oldBeta[j])).get(0,0));
		}

		c = Math.log(1 + localSum);

		for (int k=0; k<numberOfClasses; k++) {

		    g = ((X[i].transpose()).times(oldBeta[k])).get(0,0);

		    P_Matrix.set(i, k, Math.exp(g-c)); 
		}

                localSum = 0.0;
	    }

/*
	    System.out.println(" ");
	    System.out.println("mleAlogrithm() - P_Matrix : ");
	    System.out.println("mleAlogrithm() - P_Matrix : Row Dimension = " + P_Matrix.getRowDimension());
	    System.out.println("mleAlogrithm() - P_Matrix : Column Dimension = " + P_Matrix.getColumnDimension());
	    for (int i=0; i<P_Matrix.getRowDimension(); i++) {
		for (int j=0; j<P_Matrix.getColumnDimension(); j++) {
		    System.out.print(P_Matrix.get(i,j) + ", ");
		}
		System.out.println(" ");
		System.out.println(" ");
	    }
*/

            // Calculate local sum matrix for sub score matrix 
            Matrix [] sum = new Matrix[numberOfClasses];
            for (int k=0; k<numberOfClasses; k++) {
                sum[k] = new Matrix(numOfGenes+1, 1);

                for (int i=0; i<numOfSamples; i++) {
                    sum[k] = sum[k].plus(X[i].times(Z_Matrix.get(i,k) - P_Matrix.get(i,k)));
                }
            }

            // Calculate S_Matrix
            for (int k=0; k<numberOfClasses; k++) {
                S_Matrix.setMatrix(k*(numOfGenes+1), (k+1)*(numOfGenes+1)-1, 0, 0, sum[k]);
            }

            // Calculate W_Matrix
            for (int i=0; i< numberOfClasses; i++) {
                for (int j=0; j< numberOfClasses; j++) {
                    if (i==j) {
                       for (int k=0; k<numOfSamples; k++) {
                           W_Matrices[i][j].set(k, k, P_Matrix.get(k,i)*(1-P_Matrix.get(k,i)));
                       }
                    } else {
                       for (int k=0; k<numOfSamples; k++) {
                           W_Matrices[i][j].set(k, k, P_Matrix.get(k,i)*P_Matrix.get(k,j));
                       }
                    }
                }
            }

            // Calculate sub I matrices
            for (int k1=0; k1<numberOfClasses; k1++) {
	       for (int k2=0; k2<numberOfClasses; k2++) {
                  if (k1==k2) {
                      I[k1][k2] = X_Matrix.times(W_Matrices[k1][k2]).times(X_Matrix.transpose());
                  } else {
                      I[k1][k2] = (X_Matrix.times(W_Matrices[k1][k2]).times(X_Matrix.transpose())).uminus();
                  }
	       }
            }
             
/*
	    System.out.println("mleAlogrithm() - I[0][0] : ");
	    for (int i=0; i<I[0][0].getRowDimension(); i++) {
		for (int j=0; j<I[0][0].getColumnDimension(); j++) {
		     System.out.print(I[0][0].get(i,j) + ", ");
		}
		System.out.println(" ");
	    }
  	    System.out.println(" ");
*/

            // Calculate I_Matrix
            for (int row=0; row<numberOfClasses; row++) {
		for (int column=0; column<numberOfClasses; column++) {
                    I_Matrix.setMatrix(row*(numOfGenes+1), (row+1)*(numOfGenes+1)-1, column*(numOfGenes+1), (column+1)*(numOfGenes+1)-1, I[row][column]);
		}
            }

/*
            System.out.println("mleAlgorithm() - I_Matrix : ");
            for (int i=0; i<I_Matrix.getRowDimension(); i++) {
                for (int j=0; j<I_Matrix.getColumnDimension(); j++) {
                     System.out.print(I_Matrix.get(i,j) + ", ");
                }
                System.out.println(" ");
            }
            System.out.println(" ");
*/


            // Calculate newBeta
            for (int i=0; i<numberOfClasses; i++) {
                try {
                    Matrix inverseMatrix = I_Matrix.inverse();
                    newBeta[i] = oldBeta[i].plus(((I_Matrix.inverse()).times(S_Matrix)).getMatrix(i*(numOfGenes+1), (i+1)*(numOfGenes+1)-1, 0, 0));
                } catch (RuntimeException ex) {
                    JOptionPane.showMessageDialog(null, "MLE Algorithm: Information Matrix is Singular", "Alert", JOptionPane.WARNING_MESSAGE);
//                    throw new AbortException();
                    throw new AlgorithmException("MLE Algorithm: Singular Matrix");
                }
            }


/*
	    System.out.println(" ");
	    System.out.println("mleAlogrithm() - iteration = " + iteration);
	    System.out.println(" ");
	    System.out.println("mleAlogrithm() - newBeta[i] : ");
	    for (int i=0; i<numberOfClasses; i++) {
		for (int j=0; j<numOfGenes+1; j++) {
		    System.out.print(newBeta[i].get(j,0) + ", ");
		}
		System.out.println(" ");
		System.out.println(" ");
	    }
*/

        }
/*
	System.out.println(" ");
 	System.out.println("******************** End mleAlgorithm() ********************");
	System.out.println(" ");
*/
        return newBeta;

    } // end of mleAlgorithm()



    /**
     * PDA Algorithm:
     * qualitative response variable: y = k where k = 0, 1, 2, ... G
     * gene expression levels from one experiment: x = (x1, x2, ... xp) 
     * probability: P(y = k | x) = exp(gk(x))/(1+exp(g0(x)) + exp(g1(x)) + ... exp(gK(x))) 
     * where gk(x) = betak0*x0 + betak1*x1 +...+ betakp*xp
     * beta = (beta1, .... betak) with betak= (betak0, betak1,..., betakp);
     * bata can be estimated from MLE when 
     * numberOfSamples > numberOfClasses * numberOfGenes.
     * 
     * D matrix: N x N Risk Indicator Matrix
     * u vector: N x 1
     * a vector: N x 1
     * W matrix: N x N matrix 
     * W(i)(j) = (1/a(i))u(j)*D(i)(j)
     * Wdiag[i] array of NxN matrices 
     * delta vector: N x 1, Censoring Indicator
     * delta(i) = I(t(i)= min(y(i), z(i)));
     * 
     * Log Partial likelihood: 
     * L(beta) = beta' * X' * delta - delta'* a
     * Score Vector:
     * S(beta) = (delta(1)(x(1) - X'W(1))) + ... + (delta(N)(x(N) - X'W(N))
     * Information Matrix:
     * I(beta) = delta(1)[X'W(1)X - (X'W(1)(W(1)'X)] + ... +
     * delta(1)[X'W(N)X - (X'W(N)(W(N)'X)] 
     *
     * NewTon-Raphson algorithm:
     *    beta(s+1) = beta(s) + inverse(I(beta))*(beta(s)*S(beta(s)))
     *
     * If the NewTon-Raphson algorithm converges, the vector of coefficients is 
     * the maximum partial likelihood estimate of beta.
     */                 
    public double[] pdaAlgorithm(Matrix testMatrix) {
/*
	System.out.println(" ");
	System.out.println("******************** Begin pdaAlgorithm() ********************");
	System.out.println(" ");
*/
        int numOfGenes = testMatrix.getRowDimension();
        int numOfTestSamples = testMatrix.getColumnDimension();
      
        Matrix X_Matrix = new Matrix(numOfGenes+1, numOfTestSamples);
        
        for (int i=0; i<numOfTestSamples; i++) {
            X_Matrix.set(0, i, 1);
        }
        X_Matrix.setMatrix(1, numOfGenes, 0, numOfTestSamples-1, testMatrix);

	double [] probFunction = new double [numberOfClasses];

/*
	System.out.println("pdaAlgorithm() -- testMatrix: ");
	printMatrix(testMatrix);

	System.out.println(" ");
	System.out.println("pdaAlgorithm() -- X_Matrix");
	for (int row = 0; row < X_Matrix.getRowDimension(); row++) {
	     for (int column = 0; column < testMatrix.getColumnDimension(); column++) {
		 System.out.print(X_Matrix.get(row, column) + ", ");
	     }
	     System.out.println(" "); 
	}

	System.out.println(" ");
        for(int classId=0; classId<numberOfClasses; classId++) {
	    System.out.println("pdaAlgorithm() -- beta[" + classId + "]");
	    for (int row = 0; row < beta[classId].getRowDimension(); row++) {
		 for (int column = 0; column < beta[classId].getColumnDimension(); column++) {
		     System.out.print(beta[classId].get(row, column) + ", ");
		 }
		 System.out.println(" "); 
	     }
   	     System.out.println(" "); 
        }
*/

        for (int classId=0; classId<numberOfClasses; classId++) {
            probFunction[classId] = ((X_Matrix.transpose()).times(beta[classId])).get(0,0);
        }

/*
        System.out.println(" ");
        System.out.println("pdaAlgorithm() -- *******************************");
        System.out.println("pdaAlgorithm() -- probFunction: ");
        for (int classId=0; classId<numberOfClasses; classId++) {
            System.out.print(probFunction[classId] + ", ");
        }
        System.out.println(" ");
        System.out.println("pdaAlgorithm() -- *******************************");
*/
/*
	System.out.println(" ");
	System.out.println("******************** End pdaAlgorithm() ********************");
	System.out.println(" ");
*/
        return probFunction;

    } // end of pdaAlgorithm



    /**
     * Calculate Parameters for QDA Algorithm:
     *   A(i) = -0.5 * covarianceMatrix(i).inverse()    
     *   C(i) = covarianceMatrix(i).inverse() * meanVector(i) and
     *   c(i) = log(P(Y=i)) - 0.5log(covarianceMatrix(i).det()) 
     *   - 0.5 * meanVector'(i) * covarianceMatrix(i).inverse() * meanVector(i);
     */
    public int calculateQDAParameters(Matrix compMatrix, Matrix responseMatrix) throws AlgorithmException {

        int numOfGenes = compMatrix.getRowDimension();
        int numOfSamples = compMatrix.getColumnDimension();

/*
        System.out.println(" ");
        System.out.println("********** Begin calculateQDAParameters() **********");
        System.out.println(" ");
*/

	Matrix [] subCompMatrix = new Matrix[numberOfClasses];

        int [] numOfSamplesInClass = new int[numberOfClasses];

/*
        System.out.println("calculateQDAParameters() - compMatrix: " + compMatrix.getRowDimension() + " X " + compMatrix.getColumnDimension() ); 
        printMatrix(compMatrix);
*/

        for(int classId=0; classId<numberOfClasses; classId++) {

            numOfSamplesInClass[classId] = 0;
            for (int i=0; i<numOfSamples; i++) {
                  numOfSamplesInClass[classId] += responseMatrix.get(i, classId);
            }

//            System.out.println("number of samples in class " + classId + " is " + numOfSamplesInClass[classId]);

            int k = numOfSamplesInClass[classId];
            int [] classIndices = new int[k];

            int j=0;
            for (int i = 0; i<numOfSamples; i++ ) {
		if (responseMatrix.get(i, classId) == 1) {
                    classIndices[j] = i;
                    j++;
                }
            }

            subCompMatrix[classId] = new Matrix(numOfGenes, numOfSamplesInClass[classId]);
	    subCompMatrix[classId] = compMatrix.getMatrix(0, numOfGenes-1, classIndices);
        }


/*
        for (int classId=0; classId <numberOfClasses; classId++) { 
            System.out.println("calculateQDAParameters() -- subCompMatrix for class " + classId);
            System.out.println("calculateQDAParameters() - Dimension: " + subCompMatrix[classId].getRowDimension() + " X " + subCompMatrix[classId].getColumnDimension() ); 
            printMatrix(subCompMatrix[classId]);
        }
*/

        // means for each class
	Matrix [] means = new Matrix[numberOfClasses];
        for (int classId=0; classId < numberOfClasses; classId++) {
             means[classId] = new Matrix(1, numOfGenes);
        }

        // covariance matrix for each class
	Matrix [] covarianceMatrix = new Matrix[numberOfClasses];

        for(int classId=0; classId < numberOfClasses; classId++) {
            covarianceMatrix[classId] = new Matrix(numOfGenes, numOfGenes);
        }

        // inverse of covariance matrix for each class
	Matrix [] covInverseMatrix = new Matrix[numberOfClasses];
        for(int classId=0; classId<numberOfClasses; classId++) {
            covInverseMatrix[classId] = new Matrix(numOfGenes, numOfGenes);
        }

	double [] probability = new double[numberOfClasses]; // probability of a sample in a class

	double[] sumVector = new double[numOfSamples];

        A_Matrix = new Matrix[numberOfClasses];

        C_Matrix = new Matrix[numberOfClasses];
        for (int classId=0; classId < numberOfClasses; classId++) {
            C_Matrix[classId] = new Matrix(numOfGenes,1);
        }

        cValues = new double[numberOfClasses];
 
        // Calculate probability vector
        int responseSum=0;
        for (int classId=0; classId < numberOfClasses; classId++) {
            responseSum=0;
            for (int sample=0; sample < numOfSamples; sample++) {
                responseSum += responseMatrix.get(sample, classId);
            }

            probability[classId] = (double)responseSum/(double)numOfSamples; 

        }

        // Calculate gene mean for all the samples for each class 
        double geneSumInOneClass=0.0;
        for (int classId=0; classId < numberOfClasses; classId++) {
            for (int gene=0; gene < numOfGenes; gene++) {
                geneSumInOneClass=0.0;
                for (int sample=0; sample < subCompMatrix[classId].getColumnDimension(); sample++) {
                    geneSumInOneClass += subCompMatrix[classId].get(gene, sample);
                }
                means[classId].set(0, gene, geneSumInOneClass/subCompMatrix[classId].getColumnDimension()); 
            }
        }

        // Calculate covariance matrix for the given gene expression matrix that's in one class

        double [][]tempDouble = new double [numOfGenes][numOfGenes];
        Matrix tempMatrix = new Matrix(numOfGenes, numOfGenes);

        for (int classId=0; classId < numberOfClasses; classId++) {

            // calculate covariance matrix for this class
            covarianceMatrix[classId] = getCovarianceMatrix(subCompMatrix[classId], means[classId]);

/*
	    System.out.println("calculateQDAParameters() -- covariance matrix for class " + classId);
            printMatrix(covarianceMatrix[classId]);
*/

	    double [][] tempArray = covarianceMatrix[classId].getArray();

	    for (int i=0; i<numOfGenes; i++) {
	       for (int j=0; j<numOfGenes; j++) {
		   tempMatrix.set(i, j, (double) (((int)(tempArray[i][j] * 1E6))/1E6) ); 
	       }
	    }

/*
	    System.out.println("calculateQDAParameters() -- temp matrix: ");
	    System.out.println("calculateQDAParameters() -- det of covariance matrix = " + tempMatrix.det());
            printMatrix(tempMatrix);
*/

            if (Math.abs(tempMatrix.det()) < 1E-10) {

 //               System.out.println("calculateQDAParameters() -- Covariance Matrix is singular for class Id # " + classId);
 // 	        System.out.println("calculateQDAParameters() -- det of covariance matrix for class " + 
 //                   (covarianceMatrix[classId]).det());
 //               System.out.println(" ");

                JOptionPane.showMessageDialog(null, "QDA Algorithm: Covariance Matrix is Singular", "Alert", JOptionPane.WARNING_MESSAGE);

                throw new AlgorithmException("QDA Algorithm: Singular Matrix");
            }

            // calculate inverse of the covariance matrix for this class
//            covInverseMatrix[classId] = covarianceMatrix[classId].inverse();
            covInverseMatrix[classId] = tempMatrix.inverse();

/*
	    System.out.println("calculateQDAParameters() -- Inverse of covariance matrix for class " + classId);
 	    printMatrix(covInverseMatrix[classId]);
*/

        }


/*
        for (int classId=0; classId<numberOfClasses; classId++) {
	    System.out.println("calculateQDAParameters() -- mean for class " + classId);
	    printMatrix(means[classId]);
        }
*/


        // Calculate A matrix for group i
        // A(i) = -0.5 * covarianceMatrix(i).inverse()    
        for (int classId=0; classId<numberOfClasses; classId++) {
            A_Matrix[classId] = new Matrix(numOfGenes, numOfGenes);
            A_Matrix[classId] = covInverseMatrix[classId].times((-0.5));
        }

/*
        for (int classId=0; classId<numberOfClasses; classId++) {
            System.out.println("calculateQDAParameters() -- A_Matrix for class  " + classId);
            printMatrix(A_Matrix[classId]);
        }
*/

        // Calculate C vector for each class i
        // C(i) = covarianceMatrix(i).inverse() * meanVector(i) and
        for (int classId=0; classId<numberOfClasses; classId++) {
            C_Matrix[classId] = covInverseMatrix[classId].times((means[classId]).transpose());
        }


/*
        for (int classId=0; classId<numberOfClasses; classId++) {
            System.out.println("calculateQDAParameters() -- C_Matrix for class  " + classId);
	    printMatrix(C_Matrix[classId]);
        }
*/


        // Calculate c value for group i
        // c(i) = log(P(Y=i)) - 0.5log(covarianceMatrix(i).det()) 
        //        - 0.5 * meanVector'(i) * covarianceMatrix(i).inverse() * meanVector(i);
        for (int classId=0; classId<numberOfClasses; classId++) {
		cValues[classId] = Math.log(probability[classId]);

//		cValues[classId] -= Math.log(covarianceMatrix[classId].det()) * 0.5;

		cValues[classId] -= Math.log(tempMatrix.det()) * 0.5;
		cValues[classId] -= (means[classId].times(C_Matrix[classId])).get(0,0) * 0.5;
        }


   //     System.out.println(" ");
   //     for (int classId=0; classId<numberOfClasses; classId++) {
   //         System.out.println("calculateQDAParameters() -- cValues for class #" + classId + " is " + cValues[classId]);
   //         System.out.println(" ");
    //    }
   //     System.out.println(" ");

   //     System.out.println(" ");
   //     System.out.println("********** End calculateQDAParameters() **********");
   //     System.out.println(" ");

        return 1;

    } // end of calculateQDAParameters



    /**
     * QDA Algorithm:
     *   The probability of Y=k given X is P(Y=k|X) = exp(Q(k,X))/Sum(exp(Q(i,X))),
     *   where Q(i,X)=X'A(i)X + C'X + c(i) with
     *   A(i) = -0.5 * covarianceMatrix(i).inverse()    
     *   C(i) = covarianceMatrix(i).inverse() * meanVector(i) and
     *   c(i) = log(P(Y=i)) - 0.5log(covarianceMatrix(i).det()) 
     *   - 0.5 * meanVector'(i) * covarianceMatrix(i).inverse() * meanVector(i);
     */
    public double[] qdaAlgorithm(Matrix testMatrix) {

//	System.out.println(" ");
//	System.out.println("************* Begin qdaAlgorithm() **************");
//	System.out.println(" ");

        // Calculate Q value for class i
        // Q(i,X)=X'A(i)X + C'X + c(i) 

	Matrix Q_Matrix = new Matrix(numberOfClasses, 1);
        Matrix temp = new Matrix(1,1);

//	System.out.println("qdaAlgorithm() -- testMatrix: ");
//	printMatrix(testMatrix);

        for (int classId=0; classId<numberOfClasses; classId++) {

            temp = (testMatrix.transpose()).times(A_Matrix[classId]).times(testMatrix);
            temp = temp.plus((C_Matrix[classId].transpose()).times(testMatrix));

            Q_Matrix.set(classId, 0, temp.get(0,0) + cValues[classId]);
        }

  //      System.out.println("qdaAlgorithm() -- Q_Matrix: ");
  //      printMatrix(Q_Matrix);
 
        double sum = 0.0d;
 
        for (int classId=0; classId<numberOfClasses; classId++) {
            sum += Math.exp(Q_Matrix.get(classId, 0));
        }

        // Calculate probability P value for Y=k given X
        // The probability of Y=k given X is P(Y=k|X)=exp(Q(k,X))/Sum(exp(Q(i,X)))

	double [] probFunction = new double [numberOfClasses];

        for (int classId=0; classId<numberOfClasses; classId++) {
//            probFunction[classId] = Q_Matrix.get(classId,0);
            probFunction[classId] = Math.exp(Q_Matrix.get(classId,0))/sum;
        }

 //       System.out.println("qdaAlgorithm() -- probFunction: ");
 //       for (int classId=0; classId<numberOfClasses; classId++) {
 //           System.out.print(probFunction[classId] + ", ");
 //       }
 //       System.out.println(" ");

//	System.out.println(" ");
//	System.out.println("************* End qdaAlgorithm() **************");
//	System.out.println(" ");

        return probFunction;

    } // end of qdaAlgorithm




    /**
     * MPLS Algorithm:
     *     Set convergence criterion e = 1E-12;
     *     X1 = X 
     *     Y1 = Y 
     * 
     *     for (int k=1; k<Kp; k++) {
     *       u = first column of Yk
     *       delta = 1.0;
     *       while (delta > e) {
     *         w = Xk'u/u'u
     *         t = Xkw
     *         c = Yk't/t't
     *         scale c to unit length
     *         u = Ykc
     *         delta=(w-w_prep)'(w-w_prep), w_prep is previous value of w
     *       }
     * 
     *       ck = c
     *       pk = X't/(t't)
     *       tk = tcp, cp=(pk'pk)^0.5
     *       wk=wcp
     *       bk=u't/(t't)
     *       Xk+1 = Xk - tkpk'
     *       Yk+1 = Yk - bktkck'
     * 
     *       compute test components based on training information from 5
     *       set X*1=X* and compute t*1=X* and compute t*1=X*1W1. Subsequent test components are computed as T*k=X*kWk
     *       where X*k=X*k-1 - t*k-1PK-1, for k=1,2,...kp-1.
     *     }
     *     End of MPLS Alogorithm
     *
     *  Input: subTrainingMatrix cantainning pre-selected genes obtained from geneSelection() algorithm 
     *  Output: geneCompMatrix tMatrix of size Kp x N (where N = numberOfSamples)
     */
    public Matrix mplsAlgorithm(Matrix trainingMatrix, Matrix trainingResponseMatrix, Matrix testMatrix) {

//	System.out.println(" ");
//	System.out.println("************** Begin mplsAlgorithm() **************");
//	System.out.println(" ");

	Matrix [] matrix_X = new Matrix[kValue+1]; // training matrix for each k iteration
	Matrix [] matrix_Y = new Matrix[kValue+1]; // response matrix for each k iteration
        Matrix [] matrix_T = new Matrix[kValue]; // normalized test data matrix for each k iteration

        double tempMean, tempVariance;

        Matrix training_means_X, training_means_Y;
        Matrix test_means_X;
        Matrix training_variances_X, training_variances_Y;
        Matrix test_variances_X;
        
        int numOfGenes = trainingMatrix.getRowDimension();
        int numOfTrainingSamples = trainingMatrix.getColumnDimension();
        int numOfTestSamples = testMatrix.getColumnDimension();

//        System.out.println("mplsAlgorithm() - numOfGenes = " + numOfGenes);
//        System.out.println("mplsAlgorithm() - numOfTrainingSamples = " + numOfTrainingSamples);
//        System.out.println("mplsAlgorithm() - numOfTestSamples = " + numOfTestSamples);
//        System.out.println(" ");

        AlgorithmEvent event = new AlgorithmEvent(this, AlgorithmEvent.SET_UNITS, kValue, "Dimension Reduction (MPLS)\n");
        fireValueChanged(event);
        event.setId(AlgorithmEvent.PROGRESS_VALUE);            

        for (int k=0; k < kValue+1; k++) {
           matrix_X[k] = new Matrix(numOfGenes, numOfTrainingSamples); 
	   matrix_Y[k] = new Matrix(numOfTrainingSamples, numberOfClasses);
	   if (k < kValue) {
              matrix_T[k] = new Matrix(numOfGenes, numOfTestSamples);
           }
        }
 
        
//        System.out.println("mplsAlgorithm() - Call getSampleMeans for trainingMatrix: ");
        training_means_X = getSampleMeans(trainingMatrix);
        training_variances_X = getSampleVariances(trainingMatrix, training_means_X);

        // Normalize input training data set (matrix of numOfGenes x numOfTrainingSamples)
        for (int column=0; column<numOfTrainingSamples; column++) {

           tempMean = training_means_X.get(0, column);
           tempVariance = training_variances_X.get(0, column);

           for (int row=0; row<numOfGenes; row++) {
              matrix_X[0].set(row, column, (trainingMatrix.get(row,column)-tempMean)/tempVariance); 
           }
        }

//        System.out.println("mplsAlgorithm() - Call getSampleMeans for trainingResponseMatrix: ");
        training_means_Y = getSampleMeans(trainingResponseMatrix);
        training_variances_Y = getSampleVariances(trainingResponseMatrix, training_means_Y);

        // Normalize input response data set
        for (int column=0; column<numberOfClasses; column++) {

           tempMean = training_means_Y.get(0, column);
           tempVariance = training_variances_Y.get(0, column);

           for (int row=0; row<numOfTrainingSamples; row++) {
              matrix_Y[0].set(row, column, (trainingResponseMatrix.get(row,column)-tempMean)/tempVariance); 
           }
        }

        test_means_X = getSampleMeans(testMatrix);
        test_variances_X = getSampleVariances(testMatrix, test_means_X);

        // Normalize input test data set
        for (int column=0; column<numOfTestSamples; column++) {

           tempMean = test_means_X.get(0, column);
           tempVariance = test_variances_X.get(0, column);

           for (int row=0; row<numOfGenes; row++) {
              matrix_T[0].set(row, column, (testMatrix.get(row, column)-tempMean)/tempVariance); 
           }
        }

        double e = 1E-12;
	Matrix  u = new Matrix(numOfTrainingSamples, 1);  // first column of matrix_Y
        double uu = 0.0; // u'u

	Matrix  w = new Matrix(numOfGenes, 1);
        Matrix [] wk = new Matrix [kValue];
        for (int i=0; i<kValue; i++) {
          wk[i] = new Matrix(numOfGenes, 1);
        }

        Matrix w_prep = new Matrix(numOfGenes, 1);
        for (int i=0; i<numOfGenes; i++) {
           w_prep.set(i, 0, 0);
        }

	Matrix  t = new Matrix(numOfTrainingSamples, 1);
        double tt = 0.0; // t't
        double cc = 0.0; // c'c
        double ww = 0.0; // w'w
        double pp = 0.0; // p'p

	Matrix  c = new Matrix(numberOfClasses, 1);

        Matrix [] tk = new Matrix[kValue];
        Matrix [] ck = new Matrix[kValue];
        Matrix [] pk = new Matrix[kValue];
	double [] bk = new double[kValue];
        for (int i=0; i<kValue; i++) {
          tk[i] = new Matrix(numOfTrainingSamples, 1);
          ck[i] = new Matrix(numberOfClasses, 1);
          pk[i] = new Matrix(numOfGenes, 1);
          bk[i] = 0;
        }

        double cp = 1.0;
       

        for (int k=0; k < kValue; k++) {

           // set u = first column of Yk
           for (int row=0; row<numOfTrainingSamples; row++) {
	      u.set(row, 0, matrix_Y[k].get(row, 0));
           }

           // get uu = u'u
           uu = ((u.transpose()).times(u)).get(0,0);

           int loop = 0;
           double delta = 1.0;

           while ((delta>e) && loop < 1000) {
 
              loop++;

              // get w = X[k]'u/u'u (note: here X[k] is genes x samples, so no need to transpose)
              w = matrix_X[k].times(u); // (numOfGenes x 1)

              for (int row = 0; row < numOfGenes; row++) {
                w.set(row, 0, w.get(row, 0)/uu);
              }

              // scale w to unit length
              ww = Math.sqrt(w.transpose().times(w).get(0,0));
              for (int row = 0; row < numOfGenes; row++) {
                w.set(row, 0, (w.get(row, 0)/ww));
              }

              // t = X[k]w (numOfSampes x 1)
              t = (matrix_X[k].transpose()).times(w);

              // c = Yk't/t't (numberOfClasses x 1)
              // scale c to unit length
              tt = ((t.transpose()).times(t)).get(0,0);
              c = (matrix_Y[k].transpose()).times(t);

              for (int row=0; row<numberOfClasses; row++) {
                  c.set(row, 0, c.get(row, 0)/tt);
              }

              // scale c to unit length
              cc = Math.sqrt(c.transpose().times(c).get(0,0));
              for (int row = 0; row < numberOfClasses; row++) {
                c.set(row, 0, (c.get(row, 0)/cc));
              }


              // u = Y[k]c
              u = matrix_Y[k].times(c);


/*
  	      System.out.println(" ");
              if ((loop==6) || (loop == 7) || (loop==8) || (loop==9) || (loop==10) ) {

		  System.out.println(" ");
		  System.out.print("mplsAlgorithm() - w-w_prep: ");
		  for (int i=0; i<numOfGenes; i++ ) {
		      System.out.print((w.minus(w_prep)).get(i,0) + ", ");
		  }
		  System.out.println(" ");
  	          System.out.println(" ");
              }
*/

              // delta=(w-w_prep)'(w-w_prep), w_prep is previous value of w
              if (loop > 1) {
                  delta = (((w.minus(w_prep)).transpose()).times(w.minus(w_prep))).get(0,0);
              }

 //             System.out.println("mplsAlgorithm() - loop = " + loop + "  delta = " + delta);

	      for(int i=0; i<numOfGenes; i++) {
		  w_prep.set(i, 0, w.get(i, 0));
	      }

          }

          // ck[k] = c
             ck[k] = c;

          // pk[k] = Xk't/(t't)
          pk[k] = matrix_X[k].times(t);
          for (int row = 0; row < numOfGenes; row++) {
                pk[k].set(row, 0,  pk[k].get(row, 0)/tt);
          }

          // cp=(pk[k]'pk[k])^0.5, tk[k] = t*cp
          cp = Math.sqrt(pk[k].transpose().times(pk[k]).get(0,0));

	  // scale pk to unit length
	  for (int row = 0; row < numOfGenes; row++) {
	    pk[k].set(row, 0, (pk[k].get(row, 0)/cp));
	  }

//          tk[k] = t.times(cp);
          tk[k] = t;

          // wk[k]=w*cp
//          wk[k] = w.times(cp); 
          wk[k] = w; 

	  // bk[k]=u't/(t't)
          bk[k] = (((u.transpose()).times(t)).get(0,0))/tt; 

          if (k < kValue) {
	      // X[k+1] = X[k] - tk[k]*pk[k]'
	      matrix_X[k+1] = matrix_X[k].minus((tk[k].times(pk[k].transpose())).transpose());

	      // Y[k+1] = Y[k] - bk[k]*tk[k]*ck'[k]
	      matrix_Y[k+1] = matrix_Y[k].minus((tk[k].times(ck[k].transpose())).times(bk[k]));
          }

          event.setIntValue(k);
          event.setDescription("Calculating Component # " + (k + 1) + "\n");
          fireValueChanged(event); 
       }


       // compute test component matrix 
       Matrix [] tMatrix = new Matrix[kValue];
       for (int i=0; i<kValue; i++) {
           tMatrix[i] = new Matrix(numOfTestSamples, 1);
       } 

       // compute test components based on training information.
       // set X*[1]=X* and compute t*[1]=X* and compute t*1=X*1W1. 
       // Subsequent test components are computed as T*k=X*kWk
       // where X*[k]=X*[k-1] - t*[k-1]P[k-1], for k=1,2,...kp-1.

       tMatrix[0] = (matrix_T[0].transpose()).times(wk[0]);

       for (int k=1; k < kValue; k++) {

           matrix_T[k] = matrix_T[k-1].minus((tMatrix[k-1].times(pk[k-1].transpose())).transpose());

           tMatrix[k] = (matrix_T[k].transpose()).times(wk[k]); 

       }

       Matrix geneCompMatrix = new Matrix(kValue, numOfTestSamples);


       for(int gene=0; gene<kValue; gene++) {
           for (int sample=0; sample<numOfTestSamples; sample++) {
               geneCompMatrix.set(gene, sample, tMatrix[gene].get(sample, 0));
           }
       }

//	System.out.println(" ");
//	System.out.println("************** End mplsAlgorithm() **************");
//	System.out.println(" ");

       return geneCompMatrix;

    } // end of mplsAlgorithm



    public Matrix InitialClassification(Matrix expMatrix) throws AlgorithmException {

//	 System.out.println(" ");
//  	 System.out.println("******************** Begin InitialClassification() ********************");
//	 System.out.println(" ");
 
	 Matrix selectedTrainingMatrix, trainingRespMatrix, testMatrix;

	 Matrix geneCompMatrix, trainingCompMatrix;

         Matrix selectedExpMatrix;

         Matrix compMatrix, compResponseMatrix; 
 
         double[][] probFunction = new double[numberOfSamples][numberOfClasses];

         int numOfSelectedGenes=1;
         double [] selectedGenesArray = new double[numberOfGenes];

         int numOfGenes;
         int numOfTrainingSamples = trainingMatrix.getColumnDimension();
         int numOfTestSamples;
         
         AlgorithmEvent event = new AlgorithmEvent(this, AlgorithmEvent.SET_UNITS, numberOfGenes, "Initial Classification\n");
         fireValueChanged(event);
         event.setId(AlgorithmEvent.PROGRESS_VALUE);            

 
         //  1. (InitialClassification) Select Genes - Select a set, geneSet, of p* genes giving an expression 
         //     matrix expMatrix of size p* x N

         // Perform gene selection from the training matrix if required
         if (preSelectGenes && (expMatrix.getRowDimension() > kValue)) {
             
             selectedGeneIndices = geneSelection(trainingMatrix);

             if (selectedGeneIndices == null) return null;
             
             if(highestGeneRank > 1) {
                 numOfSelectedGenes = selectedGeneIndices[highestGeneRank-1].size() + 
                                      selectedGeneIndices[highestGeneRank].size();
             } else if (highestGeneRank == 1){
                 numOfSelectedGenes = selectedGeneIndices[highestGeneRank].size();
             } else if (highestGeneRank == 0) {
                 numOfSelectedGenes = selectedGeneIndices[0].size();
             }

//             usedGeneIndices = new int[selectedGeneIndices[highestGeneRank].size()];
//             unusedGeneIndices = new int[numberOfGenes-selectedGeneIndices[highestGeneRank].size()];
             
//             System.out.println("***********************************");
//             System.out.println("selected gene indices size for highest rank= "+selectedGeneIndices[highestGeneRank].size());
//             System.out.println("highest rank = "+highestGeneRank);
             
             for (int i = 0; i < selectedGeneIndices[highestGeneRank].size(); i++) {
//                  usedGeneIndices[i] = ((Integer)(selectedGeneIndices[highestGeneRank]).get(i)).intValue();
                 reducedGeneSet[used].add(selectedGeneIndices[highestGeneRank].get(i));  
	     }

             if (highestGeneRank > 1) {
		 for (int i = 0; i < selectedGeneIndices[highestGeneRank-1].size(); i++) {
		     reducedGeneSet[used].add(selectedGeneIndices[highestGeneRank-1].get(i));  
		 }
             }
            
 
             int j=0;
  	     for (int i = 0; i < numberOfGenes; i++) {

		 if (!isFoundInVector(i, reducedGeneSet[used])) {
                       reducedGeneSet[unused].add(new Integer(i));
                 }
	     }

/*
             System.out.println("DAM.java: usedGeneIndices size: " + usedGeneIndices.length); 
             for(int i=0; i< usedGeneIndices.length; i++) {
                 System.out.print(usedGeneIndices[i] + ", "); 
             }
             System.out.println(" ");
             System.out.println(" ");
             System.out.println("DAM.java: unusedGeneIndices size: " + unusedGeneIndices.length); 
             for(int i=0; i< unusedGeneIndices.length; i++) {
                 System.out.print(unusedGeneIndices[i] + ", "); 
             }
             System.out.println(" ");
             System.out.println(" ");
*/

	     for (int gene=0; gene<numberOfGenes; gene++) {
		 if (isFoundInVector(gene, selectedGeneIndices[highestGeneRank]) || 
                     isFoundInVector(gene, selectedGeneIndices[highestGeneRank-1])  ) {
		     selectedGenesArray[gene] = 1;
		 } else {
		     selectedGenesArray[gene] = 0;
		 }
	     }

             //  Construct selectedTrainingMatrix for MPLS algorithm
             selectedTrainingMatrix = new Matrix(numOfSelectedGenes, numOfTrainingSamples);

             selectedExpMatrix = new Matrix(numOfSelectedGenes, numberOfSamples);

	     int gene=0;
	     for (int selected=0; selected<numOfSelectedGenes; selected++) {

		 if (selectedGenesArray[gene] == 1) {
		     for (int sample=0; sample<numOfTrainingSamples; sample++) {
			 selectedTrainingMatrix.set(selected, sample, trainingMatrix.get(gene, sample));
		     }

                     for (int sample=0; sample<numberOfSamples; sample++) {
			 selectedExpMatrix.set(selected, sample, expMatrix.get(gene, sample));
		     } 
		 }
		 gene++;
	     }

//	     System.out.println(" "); 
//	     System.out.println("InitialClassification() - numOfSelectedGenes = " + numOfSelectedGenes); 
//	     System.out.println(" "); 

         }
         else {

  	     for (int i = 0; i < numberOfGenes; i++) {
                 reducedGeneSet[used].add(new Integer(i));  
	     }

             //  Construct selectedTrainingMatrix
             selectedTrainingMatrix = new Matrix(numberOfGenes, numOfTrainingSamples);
	     selectedTrainingMatrix = trainingMatrix;

             selectedExpMatrix = new Matrix(numberOfGenes, numberOfSamples);
             selectedExpMatrix = expMatrix;
         }


         // 2. (InitialClassification) Dimension Reduction: Fit PLS (or PCA) to obtain PLS gene components matrix, 
         //    compMatrix, of size NxK

         // Run MPLS for dimension reduction to obtain components matrix: kValue x N
         trainingRespMatrix = new Matrix(numOfTrainingSamples, numberOfClasses);
	 trainingRespMatrix = responseMatrix.getMatrix(trainingIndices, 0, numberOfClasses-1);

         if (selectedExpMatrix.getRowDimension() == kValue) {
             geneCompMatrix = selectedExpMatrix;
         } else {
             geneCompMatrix = mplsAlgorithm(selectedTrainingMatrix, trainingRespMatrix, selectedExpMatrix);
         }

         geneComponentMatrix = geneCompMatrix;

//         System.out.println(" ");
//         System.out.println("InitialClassification() - geneCompMatrix row = " + geneCompMatrix.getRowDimension());
//         System.out.println("InitialClassification() - geneCompMatrix column = " + geneCompMatrix.getColumnDimension());

//         System.out.println("InitialClassification() - geneCompMatrix: "); 
 //        printMatrix(geneCompMatrix);


         // obtain Gene Comp Matrix and Response Matrix only for the training data
         trainingCompMatrix = geneCompMatrix.getMatrix(0, geneCompMatrix.getRowDimension()-1, trainingIndices);

//         System.out.println("InitialClassification() - trainingCompMatrix row = " + trainingCompMatrix.getRowDimension());
//         System.out.println("InitialClassification() - trainingCompMatrix column = " + trainingCompMatrix.getColumnDimension());

 //        System.out.println("InitialClassification() - trainingCompMatrix :");
//         printMatrix(trainingCompMatrix);

 //        System.out.println("InitialClassification() - trainingRespMatrix :");
 //        printMatrix(trainingRespMatrix);

         // Call MLE algorithm to calculate beta value from geneComponentMatrix
	 if (isPDA == true) {
             try { 
                beta = mleAlgorithm(trainingCompMatrix, trainingRespMatrix);
             } catch (AlgorithmException ex) {
                throw new AbortException();
             }
         } else {
             try {
                calculateQDAParameters(trainingCompMatrix, trainingRespMatrix);
             } catch (AlgorithmException ex) {
                throw new AbortException();
             }
         }


	 // 3. (InitialClassification) Classification:  classify all test data

         // Obtain testMatrix to perform classification 
	     numOfGenes = geneCompMatrix.getRowDimension();
	     numOfTestSamples = geneCompMatrix.getColumnDimension();

             int[] columnList = new int[numOfTestSamples-1];

	     testMatrix = new Matrix(numOfGenes, 1);

//	     System.out.println("InitialClassification() - numOfGenes " + numOfGenes);
//	     System.out.println("InitialClassification() - numOfTestSamples  " + numOfTestSamples);

             int testSample;
	     for(int testSampleId=0; testSampleId<testIndices.length; testSampleId++) {

                 testSample = testIndices[testSampleId]; 

 //                System.out.println(" ");
 //                System.out.println("InitialClassification() -- begin testSample = " + testSample);

		 testMatrix = geneCompMatrix.getMatrix(0, numOfGenes-1, testSample, testSample);

//		 System.out.println(" "); 
//		 System.out.println("InitialClassification() - testMatrix: "); 
//		 printMatrix(testMatrix);

		 if (isPDA == true) {
		     probFunction[testSample] = pdaAlgorithm(testMatrix);

		 } else {
		     probFunction[testSample] = qdaAlgorithm(testMatrix);
		 }

	    } 

//	    System.out.println(" ");
//	    System.out.println("InitialClassification() - Probability Function: " );

	    for(int testSampleId=0; testSampleId<testIndices.length; testSampleId++) {

                testSample = testIndices[testSampleId]; 

//	        for (int classId=0; classId<numberOfClasses; classId++) {
//		    System.out.print(probFunction[testSample][classId] + ",  ");
 //               }
//	        System.out.println(" ");
	    }
//	    System.out.println(" ");


            double max=0.0;
            int maxClassId = 0;

	    for(int testSampleId=0; testSampleId<testIndices.length; testSampleId++) {

                testSample = testIndices[testSampleId]; 
		maxClassId = 0;

		if (!Double.isNaN(probFunction[testSample][0])) {

                    max = probFunction[testSample][0]; 
		    maxClassId = 1;

		    for (int classId=0; classId<numberOfClasses; classId++) {
			if (!Double.isNaN(probFunction[testSample][classId])) {
			    if (probFunction[testSample][classId] > max) {
			       max = probFunction[testSample][classId];
			       maxClassId = classId+1;
			    }
                        }
                        else {
                            maxClassId = 0;
                            classId = numberOfClasses;
                        }
		    }
                } 
                else {
                   maxClassId = 0;
                }

         	classified[maxClassId].add(new Integer(testSample));

 // 		System.out.println("InitialClassification() - Sample # " + testSample + " is in class " + maxClassId);

	    }
//	    System.out.println(" ");

//         System.out.println(" "); 
//         System.out.println("******************** End InitialClassification() ********************"); 
//         System.out.println(" ");

         return new Matrix(probFunction);

    }  // end of initialClassification



    /** 
     * A0Algorithm(): 
     * 1. Select Genes - Select a set, geneSet, of p* genes giving an expression matrix expMatrix of size Nxp*
     * 2. Dimension Reduction: Fit PLS (or PCA) to obtain PLS gene components matrix, compMatrix, of size NxK
     * 3. Classification/Prediction: Classification is based on LOOCV:
     *     For i=1 to N DO
     *       Leave out sample (row) i of compMatrix. Fit classifier to the remaining N-1 
     *       samples and use the fitted classifier to predict left out sample i
     *     End         
     *
     */
    public Matrix A0Algorithm(Matrix expMatrix) throws AlgorithmException{
 
//	 System.out.println(" ");
//  	 System.out.println("******************** Begin A0Algorithm() ********************");
//	 System.out.println(" ");

         Matrix selectedExpMatrix, geneCompMatrix;

         Matrix subGeneCompMatrix, subResponseMatrix, testMatrix;
 
         double[][] probFunction = new double[numberOfSamples][numberOfClasses];

         int numOfSelectedGenes=1;

         double [] selectedGenesArray = new double[numberOfGenes];

         int numOfGenes;
         int numOfTrainingSamples = trainingMatrix.getColumnDimension();
         int numOfTestSamples;

         AlgorithmEvent event = new AlgorithmEvent(this, AlgorithmEvent.SET_UNITS, numberOfGenes, "Classification Algorithm : A0\n");
         fireValueChanged(event);
         event.setId(AlgorithmEvent.PROGRESS_VALUE);            

         
         //  1. (A0Algorithm) Select Genes - Select a set, geneSet, of p* genes giving an expression 
         //     matrix expMatrix of size p* x N

         // Perform gene selection from the training matrix if required
         if (preSelectGenes && (expMatrix.getRowDimension() > kValue)) {

             selectedGeneIndices = geneSelection(expMatrix);

             if (selectedGeneIndices == null) return null;
             
             if(highestGeneRank > 1) {
                 numOfSelectedGenes = selectedGeneIndices[highestGeneRank-1].size() + 
                                      selectedGeneIndices[highestGeneRank].size();
             } else if (highestGeneRank == 1){
                 numOfSelectedGenes = selectedGeneIndices[highestGeneRank].size();
             } else if (highestGeneRank == 0) {
                 numOfSelectedGenes = selectedGeneIndices[0].size();
             }

  	     for (int i = 0; i < selectedGeneIndices[highestGeneRank].size(); i++) {
                 reducedGeneSet[used].add(selectedGeneIndices[highestGeneRank].get(i));  
	     }

             if (highestGeneRank > 1) {
		 for (int i = 0; i < selectedGeneIndices[highestGeneRank-1].size(); i++) {
		     reducedGeneSet[used].add(selectedGeneIndices[highestGeneRank-1].get(i));  
		 }
             }
            
             int j=0;
  	     for (int i = 0; i < numberOfGenes; i++) {

		 if (!isFoundInVector(i, reducedGeneSet[used])) {
                       reducedGeneSet[unused].add(new Integer(i));
                 }
	     }

/*

             System.out.println("DAM.java: usedGeneIndices size: " + usedGeneIndices.length); 
             for(int i=0; i< usedGeneIndices.length; i++) {
                 System.out.print(usedGeneIndices[i] + ", "); 
             }
             System.out.println(" ");
             System.out.println(" ");
             System.out.println("DAM.java: unusedGeneIndices size: " + unusedGeneIndices.length); 
             for(int i=0; i< unusedGeneIndices.length; i++) {
                 System.out.print(unusedGeneIndices[i] + ", "); 
             }
             System.out.println(" ");
             System.out.println(" ");
*/

             // Obtain selectedGenesArray
	     for (int gene=0; gene<numberOfGenes; gene++) {
		 if (isFoundInVector(gene, selectedGeneIndices[highestGeneRank]) || 
                     isFoundInVector(gene, selectedGeneIndices[highestGeneRank-1])  ) {
		     selectedGenesArray[gene] = 1;
		 } else {
		     selectedGenesArray[gene] = 0;
		 }
	     }

             //  Construct selectedExpMatrix for MPLS algorithm
             selectedExpMatrix = new Matrix(numOfSelectedGenes, numberOfSamples);

	     int gene=0;
	     for (int selected=0; selected<numOfSelectedGenes; selected++) {

		 if (selectedGenesArray[gene] == 1) {
                     for (int sample=0; sample<numberOfSamples; sample++) {
			 selectedExpMatrix.set(selected, sample, expMatrix.get(gene, sample));
		     } 
		 }
		 gene++;
	     }

//	     System.out.println(" "); 
//	     System.out.println("A0Algorithm() - numOfSelectedGenes = " + numOfSelectedGenes); 
//	     System.out.println(" "); 

         }
         else {

  	     for (int i = 0; i < numberOfGenes; i++) {
                 reducedGeneSet[used].add(new Integer(i));  
	     }

             //  Construct selectedExpMatrix
             selectedExpMatrix = new Matrix(numberOfGenes, numberOfSamples);
             selectedExpMatrix = expMatrix;
         }


         // 2. (A0Algorithm) Dimension Reduction: Fit PLS (or PCA) to obtain PLS gene components matrix, 
         //    geneCompMatrix, of size NxK

         // Run MPLS for dimension reduction to obtain components matrix: kValue x N

         if (selectedExpMatrix.getRowDimension() == kValue) {
             geneCompMatrix = selectedExpMatrix;
         } else {
             geneCompMatrix = mplsAlgorithm(selectedExpMatrix, responseMatrix, selectedExpMatrix);
         }

         geneComponentMatrix = geneCompMatrix;

//         System.out.println(" ");
 //        System.out.println("A0Algorithm() - geneCompMatrix row = " + geneCompMatrix.getRowDimension());
//         System.out.println("A0Algorithm() - geneCompMatrix column = " + geneCompMatrix.getColumnDimension());

 //        System.out.println(" "); 
 //        System.out.println("A0Algorithm() - geneCompMatrix: "); 
   //      for (int row = 0; row < geneCompMatrix.getRowDimension(); row++) {
   //          for (int column = 0; column < geneCompMatrix.getColumnDimension(); column++) {
   //              System.out.print(geneCompMatrix.get(row, column) + ", ");
    //         }
  //           System.out.println(" "); 
  //           System.out.println(" "); 
   //      }


	 // 3. (A0Algorithm) Classification/Prediction: Classification is based on LOOCV:
	 //     For i=1 to N DO
	 //       Leave out sample (column) i of geneCompMatrix. Fit classifier to the remaining N-1 
	 //       samples and use the fitted classifier to predict left out sample i

         // Obtain testMatrix to perform LOOCV classification on componmentsMatrix
         if (performLOOCV == true) {
	     numOfGenes = geneCompMatrix.getRowDimension();
	     numOfTestSamples = geneCompMatrix.getColumnDimension();

             int[] columnList = new int[numOfTestSamples-1];

	     testMatrix = new Matrix(numOfGenes, 1);
	     subGeneCompMatrix = new Matrix(numOfGenes, numOfTestSamples-1);

	     subResponseMatrix = new Matrix(numOfTestSamples-1, numberOfClasses);

//	     System.out.println("A0Algorithm() - numOfGenes " + numOfGenes);
//	     System.out.println("A0Algorithm() - numOfTestSamples  " + numOfTestSamples);

             //3. Leave out one sample from geneCompMatrix
	     for (int leaveOutSample=0; leaveOutSample<numOfTestSamples; leaveOutSample++) {

//                 System.out.println("A0Algorithm() -- begin leaveOutSample = " + leaveOutSample);

                 singularMatrix[leaveOutSample] = false;

/*
		 System.out.println(" "); 
		 System.out.println("A0Algorithm() - geneCompMatrix: "); 
		 for (int row = 0; row < geneCompMatrix.getRowDimension(); row++) {
		     for (int column = 0; column < geneCompMatrix.getColumnDimension(); column++) {
			 System.out.print(geneCompMatrix.get(row, column) + ", ");
		     }
		     System.out.println(" "); 
		     System.out.println(" "); 
		 }
*/

		 for (int sample=0; sample<numOfTestSamples; sample++) {
		     if (sample < leaveOutSample) {
			columnList[sample] = sample; 
 	             } else if ( sample > leaveOutSample ) {
			columnList[sample-1] = sample; 
		     }
		 }

/*		 System.out.println(" "); 
		 System.out.println("A0Algorithm() - columnList: "); 
		 for (int column = 0; column < columnList.length ; column++) {
			 System.out.print(columnList[column] + ", ");
		 }
		 System.out.println(" "); 
		 System.out.println(" "); 
*/
		 subGeneCompMatrix = geneCompMatrix.getMatrix(0, numOfGenes-1, columnList); 
		 subResponseMatrix = responseMatrix.getMatrix(columnList, 0, numberOfClasses-1); 

//		 System.out.println(" "); 
//		 System.out.println("A0Algorithm() - subGeneCompMatrix: " + subGeneCompMatrix.getRowDimension() + " X " + 
 //                      subGeneCompMatrix.getColumnDimension() ); 
 //                printMatrix(subGeneCompMatrix);

  	         testMatrix = geneCompMatrix.getMatrix(0, numOfGenes-1, leaveOutSample, leaveOutSample);

//		 System.out.println(" "); 
//		 System.out.println("A0Algorithm() - testMatrix: " + testMatrix.getRowDimension() + " X " + testMatrix.getColumnDimension() ); 
//		 System.out.println("A0Algorithm() - testMatrix: "); 
//                 printMatrix(testMatrix);

                 // (A0Algorithm) Call PDA or QDA algorithm
		 if (isPDA == true) {
                     try { 
                         beta = mleAlgorithm(subGeneCompMatrix, subResponseMatrix);
                     } catch (AlgorithmException ex) {
                         singularMatrix[leaveOutSample] = true;
                         continue;
                     }
		     probFunction[leaveOutSample] = pdaAlgorithm(testMatrix);

		 } else {
                     try {
                         calculateQDAParameters(subGeneCompMatrix, subResponseMatrix);
                     } catch (AlgorithmException ex) {
                         singularMatrix[leaveOutSample] = true;
                         continue;
                     }
                     
		     probFunction[leaveOutSample] = qdaAlgorithm(testMatrix);
		 }

	    } 

//	    System.out.println(" ");
//	    System.out.println("A0Algorithm() - Probability Function: " );
//	    for (int leaveOutSample=0; leaveOutSample<numOfTestSamples; leaveOutSample++) {
//	        for (int classId=0; classId<numberOfClasses; classId++) {
//		    System.out.print(probFunction[leaveOutSample][classId] + ",  ");
//                }
//	        System.out.println(" ");
//	    }
//	    System.out.println(" ");


            double max=0.0;
            int maxClassId = 0;

	    for (int leaveOutSample=0; leaveOutSample<numOfTestSamples; leaveOutSample++) {

                if (singularMatrix[leaveOutSample] == true) continue;

                max=probFunction[leaveOutSample][0];
                maxClassId = 0;

		if (!Double.isNaN(probFunction[leaveOutSample][0])) {

                    max = probFunction[leaveOutSample][0]; 
		    maxClassId = 1;

		    for (int classId=0; classId<numberOfClasses; classId++) {
			if (!Double.isNaN(probFunction[leaveOutSample][classId])) {
			    if (probFunction[leaveOutSample][classId] > max) {
			       max = probFunction[leaveOutSample][classId];
			       maxClassId = classId+1;
			    }
                        }
                        else {
                            maxClassId = 0;
                            classId = numberOfClasses;
                        }
		    }
                } 
                else {
                   maxClassId = 0;
                }

         	classified[maxClassId].add(new Integer(leaveOutSample));

  //              System.out.println("A0Algorithm() - Sample # " + leaveOutSample + " is in class # " + maxClassId);

	    }
//	    System.out.println(" ");

         }

   //      System.out.println("DAM.java - A0Algorithm() -  classified.length = " + classified.length);

	// for (int i = 0; i < classified.length; i++) {
   //          System.out.println("DAM.java - A0Algorithm() -  classified[" + i + "].size() = " + classified[i].size());
	// }

	// System.out.println(" ");

	// System.out.println(" ");
  	 //System.out.println("******************** End A0Algorithm() ********************");
	 //System.out.println(" ");
         
         return new Matrix(probFunction);
    } // end of A0Algorithm



    /**
     * A1Algorithm(): 
     * 1. Select Genes: Select a set, geneSet, of p* genes giving an expression matrix, expMatrix of size Nxp*
     *    For i=1 to N Do
     *        Leave out sample (row) i of expression matrix expMatrix, say subTrainingMatrix
     * 2. Dimension Reduction: Fit PLS using subTrainingMatrix to obtain PLS gene component matrix, geneCompMatrix
     * 3. Classification/Prediction: Fit classifier to the remaining N-1 samples. 
     *    i.e. using geneCompMatrix. Use the fitted classifier to predict left out sample i.
     */
    public Matrix A1Algorithm(Matrix expMatrix) throws AlgorithmException{

	// System.out.println(" ");
  //	 System.out.println("******************** Begin A1Algorithm() ********************");
	// System.out.println(" ");

         Matrix selectedExpMatrix;

         Matrix subSelectedExpMatrix, subResponseMatrix;

         Matrix geneCompMatrix, subGeneCompMatrix, testMatrix;

         double[][] probFunction = new double[numberOfSamples][numberOfClasses];

         double [] selectedGenesArray = new double[numberOfGenes];

         int numOfSelectedGenes=1;

         AlgorithmEvent event = new AlgorithmEvent(this, AlgorithmEvent.SET_UNITS, numberOfGenes, "Classification Algorithm : A1\n");
         fireValueChanged(event);
         event.setId(AlgorithmEvent.PROGRESS_VALUE);            

         //1. (A1Algorithm) Select Genes: Select a set, geneSet, of p* genes giving a 
         // expression matrix of size p* X N

         // Perform gene selection from the training matrix if required
         if (preSelectGenes && (expMatrix.getRowDimension() > kValue)) {

             selectedGeneIndices = geneSelection(expMatrix);

             if (selectedGeneIndices == null) return null;
             
             if(highestGeneRank > 1) {
                 numOfSelectedGenes = selectedGeneIndices[highestGeneRank-1].size() + 
                                      selectedGeneIndices[highestGeneRank].size();
             } else if (highestGeneRank == 1){
                 numOfSelectedGenes = selectedGeneIndices[highestGeneRank].size();
             } else if (highestGeneRank == 0) {
                 numOfSelectedGenes = selectedGeneIndices[0].size();
             }

  	     for (int i = 0; i < selectedGeneIndices[highestGeneRank].size(); i++) {
                 reducedGeneSet[used].add(selectedGeneIndices[highestGeneRank].get(i));  
	     }

             if (highestGeneRank > 1) {
		 for (int i = 0; i < selectedGeneIndices[highestGeneRank-1].size(); i++) {
		     reducedGeneSet[used].add(selectedGeneIndices[highestGeneRank-1].get(i));  
		 }
             }
            
 
             int j=0;
  	     for (int i = 0; i < numberOfGenes; i++) {

		 if (!isFoundInVector(i, reducedGeneSet[used])) {
                       reducedGeneSet[unused].add(new Integer(i));
                 }
	     }


	     for (int gene=0; gene<numberOfGenes; gene++) {
		 if (isFoundInVector(gene, selectedGeneIndices[highestGeneRank]) || 
                     isFoundInVector(gene, selectedGeneIndices[highestGeneRank-1])  ) {
		     selectedGenesArray[gene] = 1;
		 } else {
		     selectedGenesArray[gene] = 0;
		 }
	     }

             //  Construct selectedExpMatrix for MPLS algorithm
             selectedExpMatrix = new Matrix(numOfSelectedGenes, numberOfSamples);

	     int gene=0;
	     for (int selected=0; selected<numOfSelectedGenes; selected++) {

		 if (selectedGenesArray[gene] == 1) {

                     for (int sample=0; sample<numberOfSamples; sample++) {
			 selectedExpMatrix.set(selected, sample, expMatrix.get(gene, sample));
		     } 
		 }
		 gene++;
	     }

	 //    System.out.println(" "); 
	 //    System.out.println("A1Algorithm() - numOfSelectedGenes = " + numOfSelectedGenes); 
	 //    System.out.println(" "); 

         } else {

  	     for (int i = 0; i < numberOfGenes; i++) {
                 reducedGeneSet[used].add(new Integer(i));  
	     }

             //  Construct selectedExpMatrix
             selectedExpMatrix = new Matrix(numberOfGenes, numberOfSamples);
             selectedExpMatrix = expMatrix;
         }


         // 2&3. (A1Algorithm) For i=1 to N Do
         //    Leave out sample (row) i of selectedExpMatrix

         for (int leaveOutSample = 0; leaveOutSample < numberOfSamples; leaveOutSample++) {

             singularMatrix[leaveOutSample] = false;

	     int gene=0;

        //     System.out.println("A1Algorithm() -- begin leaveOutSample = " + leaveOutSample);

	     int[] columnList = new int[numberOfSamples-1];

	     for (int sample=0; sample<numberOfSamples; sample++) {
		 if (sample < leaveOutSample) {
		    columnList[sample] = sample; 
		 } else if (sample > leaveOutSample) {
		    columnList[sample-1] = sample; 
		 }
	     }

	//     System.out.println(" "); 
	//     System.out.println("A1Algorithm() - columnList: "); 
	//     for (int column = 0; column < columnList.length ; column++) {
	//	     System.out.print(columnList[column] + ", ");
    //         }
	//     System.out.println(" "); 
	 //    System.out.println(" "); 


             //  2. (A1Algorithm) Dimension Reduction: Fit PLS using selectedExpMatrix to obtain PLS gene component matrix, geneCompMatrix

             // obtain subSelectedExpMatrix and subResponseMatrix for MPLS algorithm
   	     subSelectedExpMatrix = selectedExpMatrix.getMatrix(0, numOfSelectedGenes-1, columnList); 
  	     subResponseMatrix = responseMatrix.getMatrix(columnList, 0, numberOfClasses-1); 

	     // Run MPLS for dimension reduction to obtain components matrix: kValue x N
	     if (selectedExpMatrix.getRowDimension() == kValue) {
		 geneCompMatrix = selectedExpMatrix;
	     } else {
		 geneCompMatrix = mplsAlgorithm(subSelectedExpMatrix, subResponseMatrix, selectedExpMatrix);
	     }

//      geneCompMatrix = mplsAlgorithm(subSelectedExpMatrix, subResponseMatrix, selectedExpMatrix);

             geneComponentMatrix = geneCompMatrix;

/*
	     System.out.println(" "); 
	     System.out.println("A1Algorithm() - geneCompMatrix obtained from mplsAlgorithm(): "); 
	     for (int row = 0; row < geneCompMatrix.getRowDimension(); row++) {
		 for (int column = 0; column < geneCompMatrix.getColumnDimension(); column++) {
		     System.out.print(geneCompMatrix.get(row, column) + ", ");
		 }
		 System.out.println(" "); 
		 System.out.println(" "); 
	     }
*/


	     //  3. Classification/Prediction: Fit classifier to the remaining N-1 samples. 
             //     i.e. using geneCompMatrix. Use the fitted classifier to predict left out sample i.

	     // Obtain testMatrix 
 
             int numOfGenes = geneCompMatrix.getRowDimension();

	     testMatrix = new Matrix(numOfGenes, 1);
	     testMatrix = geneCompMatrix.getMatrix(0, numOfGenes-1, leaveOutSample, leaveOutSample);

	     subGeneCompMatrix = new Matrix(numOfGenes, numberOfSamples-1);
	     subGeneCompMatrix = geneCompMatrix.getMatrix(0, numOfGenes-1, columnList); 

/*
	     System.out.println(" "); 
	     System.out.println("A1Algorithm() - testMatrix: "); 
	     for (int row = 0; row < testMatrix.getRowDimension(); row++) {
		 for (int column = 0; column < testMatrix.getColumnDimension(); column++) {
		     System.out.print(testMatrix.get(row, column) + ", ");
		 }
		 System.out.println(" "); 
		 System.out.println(" "); 
	     }

	     System.out.println(" "); 
	     System.out.println("A1Algorithm() - subGeneCompMatrix: "); 
	     for (int row = 0; row < subGeneCompMatrix.getRowDimension(); row++) {
		 for (int column = 0; column < subGeneCompMatrix.getColumnDimension(); column++) {
		     System.out.print(subGeneCompMatrix.get(row, column) + ", ");
		 }
		 System.out.println(" "); 
		 System.out.println(" "); 
	     }
*/

             // (A1Algorithm) Call PDA or QDA algorithm
	     if (isPDA == true) {
                 try { 
                     beta = mleAlgorithm(subGeneCompMatrix, subResponseMatrix);
                 } catch (AlgorithmException ex) {
                     singularMatrix[leaveOutSample] = true;
                     continue;
                 }
  	         probFunction[leaveOutSample] = pdaAlgorithm(testMatrix);

	     } else {
                 try {
                     calculateQDAParameters(subGeneCompMatrix, subResponseMatrix);
                 } catch (AlgorithmException ex) {
                     singularMatrix[leaveOutSample] = true;
                     continue;
                 }
	         probFunction[leaveOutSample] = qdaAlgorithm(testMatrix);
	     }
         } 

 	/* System.out.println(" ");
	// System.out.println("A1Algorithm() - Probablity Function: " );
	 for (int leaveOutSample=0; leaveOutSample<numberOfSamples; leaveOutSample++) {
	    for (int classId=0; classId<numberOfClasses; classId++) {
		System.out.print(probFunction[leaveOutSample][classId] + ",  ");
	    }
	    System.out.println(" ");
	 }
	 System.out.println(" ");
*/

            double max=0.0;
            int maxClassId = 0;

	    for (int leaveOutSample=0; leaveOutSample<numberOfSamples; leaveOutSample++) {

                if (singularMatrix[leaveOutSample] == true) continue;

                max=probFunction[leaveOutSample][0];
                maxClassId = 0;

		if (!Double.isNaN(probFunction[leaveOutSample][0])) {

                    max = probFunction[leaveOutSample][0]; 
		    maxClassId = 1;

		    for (int classId=0; classId<numberOfClasses; classId++) {
			if (!Double.isNaN(probFunction[leaveOutSample][classId])) {
			    if (probFunction[leaveOutSample][classId] > max) {
			       max = probFunction[leaveOutSample][classId];
			       maxClassId = classId+1;
			    }
                        }
                        else {
                            maxClassId = 0;
                            classId = numberOfClasses;
                        }
		    }
                } 
                else {
                   maxClassId = 0;
                }

         	classified[maxClassId].add(new Integer(leaveOutSample));

           //     System.out.println("A1Algorithm() - Sample # " + leaveOutSample + " is in class # " + maxClassId);

	    }
	  //  System.out.println(" ");

      //   System.out.println("DAM.java - A1Algorithm() -  classified.length = " + classified.length);

	// for (int i = 0; i < classified.length; i++) {
    //         System.out.println("DAM.java - A1Algorithm() -  classified[" + i + "].size() = " + classified[i].size());
	// }

	// System.out.println(" ");

	// System.out.println(" ");
  	// System.out.println("******************** End A1Algorithm() ********************");
	// System.out.println(" ");

         return new Matrix(probFunction);
    } // end of A1Algorithm



    /*
     *  A2Algorithm(): 
     *  For i=1 to N Do
     *     Leave out sample (row) i of expression matrix expMatrix to obtain subExpMatrix
     *  1. Select Genes: Select a set, geneSet, of p* genes giving an expression matrix, subTrainingMatrix of size N-1xp*
     *  2. Dimension Reduction: Fit PLS using subTrainingMatrix to obtain PLS gene component matrix, geneCompMatrix
     *  3. Classification/Prediction: Fit classifier to the remaining N-1 samples. 
     *     i.e. using geneCompMatrix. Use the fitted classifier to predict left out sample i.
     */
    public Matrix A2Algorithm(Matrix expMatrix) throws AlgorithmException{

	// System.out.println(" ");
  	// System.out.println("******************** Begin A2Algorithm() ********************");
	// System.out.println(" ");

         Matrix subExpMatrix, subResponseMatrix;

         Matrix selectedExpMatrix, subSelectedExpMatrix;

         Matrix geneCompMatrix, subGeneCompMatrix, testMatrix;

         double[][] probFunction = new double[numberOfSamples][numberOfClasses];

         double [] selectedGenesArray = new double[numberOfGenes];

         int numOfSelectedGenes=1;

         AlgorithmEvent event = new AlgorithmEvent(this, AlgorithmEvent.SET_UNITS, numberOfGenes, "Classification Algorithm : A2\n");
         fireValueChanged(event);
         event.setId(AlgorithmEvent.PROGRESS_VALUE);            

	 // For i=1 to N Do
	 //    Leave out sample (row) i of expMatrix to obtain a subExpMatrix

        // System.out.println("A2Algorithm() Start ---------");


         for (int leaveOutSample=0; leaveOutSample<numberOfSamples; leaveOutSample++) {

             singularMatrix[leaveOutSample] = false;

        //     System.out.println("A2Algorithm() -- For leaveOutSamp = " + leaveOutSample + " Start: ");

	     // 1. Select Genes: Select a set, geneSet, of p* genes giving  expression matrix, 
             //    TrainingsubMatrix of size (N-1) x p*

             int[] columnList = new int[numberOfSamples-1];

             for (int sample=0; sample<numberOfSamples; sample++) { 
		 if (sample < leaveOutSample) {
		    columnList[sample] = sample; 
 	         } else if (sample > leaveOutSample) {
		    columnList[sample-1] = sample; 
		 }
	     }

	/*     System.out.println(" "); 
	     System.out.println("A2Algorithm() - columnList: "); 
	     for (int column = 0; column < columnList.length ; column++) {
		     System.out.print(columnList[column] + ", ");
             }
	     System.out.println(" "); 
	     System.out.println(" "); 
*/
	     subExpMatrix = expMatrix.getMatrix(0, numberOfGenes-1, columnList); 
  	     subResponseMatrix = responseMatrix.getMatrix(columnList, 0, numberOfClasses-1); 
         
	     // Perform gene selection if required
             if (preSelectGenes && (expMatrix.getRowDimension() > kValue)) {

		 selectedGeneIndices = geneSelection(subExpMatrix);

		 if (selectedGeneIndices == null) return null;

		 if(highestGeneRank > 1) {
		     numOfSelectedGenes = selectedGeneIndices[highestGeneRank-1].size() + 
					  selectedGeneIndices[highestGeneRank].size();
		 } else if (highestGeneRank == 1){
		     numOfSelectedGenes = selectedGeneIndices[highestGeneRank].size();
		 } else if (highestGeneRank == 0) {
		     numOfSelectedGenes = selectedGeneIndices[0].size();
		 }


		 for (int i = 0; i < selectedGeneIndices[highestGeneRank].size(); i++) {
		     reducedGeneSetForA2[leaveOutSample][used].add(selectedGeneIndices[highestGeneRank].get(i));  
		 }

		 if (highestGeneRank > 1) {
		     for (int i = 0; i < selectedGeneIndices[highestGeneRank-1].size(); i++) {
			 reducedGeneSetForA2[leaveOutSample][used].add(selectedGeneIndices[highestGeneRank-1].get(i));  
		     }
		 }


		 int j=0;
		 for (int i = 0; i < numberOfGenes; i++) {

		     if (!isFoundInVector(i, reducedGeneSetForA2[leaveOutSample][used])) {
			   reducedGeneSetForA2[leaveOutSample][unused].add(new Integer(i));
		     }
		 }


		 for (int gene=0; gene<numberOfGenes; gene++) {
		     if ( (isFoundInVector(gene, selectedGeneIndices[highestGeneRank])) || 
                          ((highestGeneRank > 0) && isFoundInVector(gene, selectedGeneIndices[highestGeneRank-1])) )  
                     {
			 selectedGenesArray[gene] = 1;
		     } else {
			 selectedGenesArray[gene] = 0;
		     }
		 }

	//	 System.out.println(" "); 
	//	 System.out.println("A2Algorithm() - numOfSelectedGenes = " + numOfSelectedGenes); 
	//	 System.out.println(" "); 

		 //  Construct selectedExpMatrix for MPLS algorithm
		 selectedExpMatrix = new Matrix(numOfSelectedGenes, numberOfSamples);

		 int gene=0;
		 for (int selected=0; selected<numOfSelectedGenes; selected++) {

		     if (selectedGenesArray[gene] == 1) {
			 for (int sample=0; sample<numberOfSamples; sample++) {
			     selectedExpMatrix.set(selected, sample, expMatrix.get(gene, sample));
			 }
		     }
		     gene++;
		 }

	//	 System.out.println(" "); 
	//	 System.out.println("A2Algorithm() - numOfSelectedGenes = " + numOfSelectedGenes); 
	//	 System.out.println(" "); 
                 
  	     } else {

    	         for (int i = 0; i < numberOfGenes; i++) {
                     reducedGeneSetForA2[leaveOutSample][used].add(new Integer(i));  
	         }

		 //  Construct selectedExpMatrix
		 selectedExpMatrix = new Matrix(numberOfGenes, numberOfSamples);
		 selectedExpMatrix = expMatrix;
	     }

	     //  2. (A2Algorithm) Dimension Reduction: Fit PLS using subExpMatrix to obtain PLS gene component matrix, geneCompMatrix

	     //  Construct subSelectedExpMatrix for MPLS algorithm
	     subSelectedExpMatrix = new Matrix(numOfSelectedGenes, numberOfSamples-1);
	     subSelectedExpMatrix = selectedExpMatrix.getMatrix(0, numOfSelectedGenes-1, columnList);

	     // Run MPLS for dimension reduction to obtain components matrix: N x kValue
	     if (selectedExpMatrix.getRowDimension() == kValue) {
		 geneCompMatrix = selectedExpMatrix;
	     } else {
                 geneCompMatrix = mplsAlgorithm(subSelectedExpMatrix, subResponseMatrix, selectedExpMatrix);
	     }
//             geneCompMatrix = mplsAlgorithm(subSelectedExpMatrix, subResponseMatrix, selectedExpMatrix);

             geneComponentMatrix = geneCompMatrix;

/*
	     System.out.println(" "); 
	     System.out.println("A2Algorithm() - geneCompMatrix: "); 
	     for (int row = 0; row < geneCompMatrix.getRowDimension(); row++) {
		 for (int column = 0; column < geneCompMatrix.getColumnDimension(); column++) {
		     System.out.print(geneCompMatrix.get(row, column) + ", ");
		 }
		 System.out.println(" "); 
		 System.out.println(" "); 
	     }
*/


	     //  3. Classification/Prediction: Fit classifier to the remaining N-1 samples. 
  	     //     i.e. using geneCompMatrix. Use the fitted classifier to predict left out sample i.

	     // Obtain testMatrix
             int numOfGenes = geneCompMatrix.getRowDimension();
	     testMatrix = new Matrix(numOfGenes, 1);
	     testMatrix = geneCompMatrix.getMatrix(0, numOfGenes-1, leaveOutSample, leaveOutSample);

   	     subGeneCompMatrix = new Matrix(numOfGenes, numberOfSamples-1);
  	     subGeneCompMatrix = geneCompMatrix.getMatrix(0, numOfGenes-1, columnList); 


/*
	     System.out.println(" "); 
	     System.out.println("A2Algorithm() - testMatrix: "); 
	     for (int row = 0; row < testMatrix.getRowDimension(); row++) {
		 for (int column = 0; column < testMatrix.getColumnDimension(); column++) {
		     System.out.print(testMatrix.get(row, column) + ", ");
		 }
		 System.out.println(" "); 
		 System.out.println(" "); 
	     }

	     System.out.println(" "); 
	     System.out.println("A2Algorithm() - subGeneCompMatrix: "); 
	     for (int row = 0; row < subGeneCompMatrix.getRowDimension(); row++) {
		 for (int column = 0; column < subGeneCompMatrix.getColumnDimension(); column++) {
		     System.out.print(subGeneCompMatrix.get(row, column) + ", ");
		 }
		 System.out.println(" "); 
		 System.out.println(" "); 
	     }
*/


             // (A2Algorithm) Call PDA or QDA algorithm
	     if (isPDA == true) {
                 try { 
                     beta = mleAlgorithm(subGeneCompMatrix, subResponseMatrix);
                 } catch (AlgorithmException ex) {
                     singularMatrix[leaveOutSample] = true;
                     continue;
                 }
  	         probFunction[leaveOutSample] = pdaAlgorithm(testMatrix);

	     } else {
                 try {
                     calculateQDAParameters(subGeneCompMatrix, subResponseMatrix);
                 } catch (AlgorithmException ex) {
                     singularMatrix[leaveOutSample] = true;
                     continue;
                 }
	         probFunction[leaveOutSample] = qdaAlgorithm(testMatrix);
	     }

     //        System.out.println("A2Algorithm() -- For leaveOutSamp = " + leaveOutSample + " End: ");
      //       System.out.println(" ");
      //       System.out.println(" ");

         } 

	/* System.out.println(" ");
	// System.out.println("A2Algorithm() - Probablity Function: " );
	 for (int leaveOutSample=0; leaveOutSample<numberOfSamples; leaveOutSample++) {
	    for (int classId=0; classId<numberOfClasses; classId++) {
		System.out.print(probFunction[leaveOutSample][classId] + ",  ");
	    }
	    System.out.println(" ");
	 }
	 System.out.println(" ");
*/

            double max=0.0;
            int maxClassId = 0;

	    for (int leaveOutSample=0; leaveOutSample<numberOfSamples; leaveOutSample++) {

                if (singularMatrix[leaveOutSample] == true) continue;

                max=probFunction[leaveOutSample][0];
                maxClassId = 0;

		if (!Double.isNaN(probFunction[leaveOutSample][0])) {

                    max = probFunction[leaveOutSample][0]; 
		    maxClassId = 1;

		    for (int classId=0; classId<numberOfClasses; classId++) {
			if (!Double.isNaN(probFunction[leaveOutSample][classId])) {
			    if (probFunction[leaveOutSample][classId] > max) {
			       max = probFunction[leaveOutSample][classId];
			       maxClassId = classId+1;
			    }
                        }
                        else {
                            maxClassId = 0;
                            classId = numberOfClasses;
                        }
		    }
                } 
                else {
                   maxClassId = 0;
                }

         	classified[maxClassId].add(new Integer(leaveOutSample));

          //      System.out.println("A2Algorithm() - Sample # " + leaveOutSample + " is in class # " + maxClassId);

	    }
	//    System.out.println(" ");


   //      System.out.println("DAM.java - A2Algorithm() -  classified.length = " + classified.length);

	// for (int i = 0; i < classified.length; i++) {
  //           System.out.println("DAM.java - A2Algorithm() -  classified[" + i + "].size() = " + classified[i].size());
	// }

	// System.out.println(" ");

	// System.out.println(" ");
  	// System.out.println("******************** End A2Algorithm() ********************");
	// System.out.println(" ");

         return new Matrix(probFunction);
    } // end of A2algorithm


    /**
     * getTValue(): obtain the t value from T table
     *
     */
    public double getTValue(int degreeOfFreedom, double alpha) {

      double ttbl[][] = {

	  { 1 , 6.314,   12.706,  31.821,  63.657, 636.619},
	  { 2 , 2.920 ,  4.303 ,  6.965 ,  9.925 , 31.598 },
	  { 3 , 2.353 ,  3.182 ,  4.541 ,  5.841 , 12.941 },
	  { 4 , 2.132 ,  2.776 ,  3.747 ,  4.604 ,  8.610 },
	  { 5 , 2.015 ,  2.571 ,  3.365 ,  4.032 ,  6.859 },
	  { 6 , 1.943 ,  2.447 ,  3.143 ,  3.707 ,  5.959 },
	  { 7 , 1.895 ,  2.365 ,  2.998 ,  3.499 ,  5.405 },
	  { 8 , 1.860 ,  2.306 ,  2.896 ,  3.355 ,  5.041 },
	  { 9 , 1.833 ,  2.262 ,  2.821 ,  3.250 ,  4.781 },
	  {10 , 1.812 ,  2.228 ,  2.764 ,  3.169 ,  4.587 },
	  {11 , 1.796 ,  2.201 ,  2.718 ,  3.106 ,  4.437 },
	  {12 , 1.782 ,  2.179 ,  2.681 ,  3.055 ,  4.318 },
	  {13 , 1.771 ,  2.160 ,  2.650 ,  3.012 ,  4.221 },
	  {14 , 1.761 ,  2.145 ,  2.624 ,  2.997 ,  4.140 },
	  {15 , 1.753 ,  2.131 ,  2.602 ,  2.947 ,  4.073 },
	  {16 , 1.746 ,  2.120 ,  2.583 ,  2.921 ,  4.015 },
	  {17 , 1.740 ,  2.110 ,  2.567 ,  2.898 ,  3.965 },
	  {18 , 1.734 ,  2.101 ,  2.552 ,  2.878 ,  3.922 },
	  {19 , 1.729 ,  2.093 ,  2.539 ,  2.861 ,  3.883 },
	  {20 , 1.725 ,  2.086 ,  2.528 ,  2.845 ,  3.850 },
	  {21 , 1.721 ,  2.080 ,  2.518 ,  2.831 ,  3.819 },
	  {22 , 1.717 ,  2.074 ,  2.508 ,  2.819 ,  3.792 },
	  {23 , 1.714 ,  2.069 ,  2.500 ,  2.807 ,  3.767 },
	  {24 , 1.711 ,  2.064 ,  2.492 ,  2.797 ,  3.745 },
	  {25 , 1.708 ,  2.060 ,  2.485 ,  2.787 ,  3.725 },
	  {26 , 1.706 ,  2.056 ,  2.479 ,  2.779 ,  3.707 },
	  {27 , 1.703 ,  2.052 ,  2.473 ,  2.771 ,  3.690 },
	  {28 , 1.701 ,  2.048 ,  2.467 ,  2.763 ,  3.674 },
	  {29 , 1.699 ,  2.045 ,  2.462 ,  2.756 ,  3.659 },
	  {30 , 1.697 ,  2.042 ,  2.457 ,  2.750 ,  3.646 },
	  {40 , 1.684 ,  2.021 ,  2.423 ,  2.704 ,  3.551 },
	  {60 , 1.671 ,  2.000 ,  2.390 ,  2.660 ,  3.460 },
	  {120 , 1.658 , 1.980 ,  2.358 ,  2.617 ,  3.373 },
	  {1000 , 1.645 , 1.960 , 2.326 ,  2.576 ,  3.291 } 
      };
 

      int row=0; 
      int column =0;
      double error = 1E-5;

      if (Math.abs(alpha-0.10) < error) {
         column =1;
      } else if (Math.abs(alpha-0.05) < error)  {
         column =2;
      } else if (Math.abs(alpha-0.02) < error)  {
         column =3;
      } else if (Math.abs(alpha-0.01) < error)  {
         column =4;
      } else if (Math.abs(alpha-0.001) < error)  {
         column =5;
      } else {
         return -2.0;
      }

     
      if (degreeOfFreedom <= 0) {
    //     System.out.println("DAM.java: getTValue() - degreeOfFreedom <= 0, return");
         return -2.0;
      } else if (degreeOfFreedom <= 30) {
         row = degreeOfFreedom-1;
      } else if ((degreeOfFreedom>30) && (degreeOfFreedom<40)) {
         row = 29;
      } else if ((degreeOfFreedom>=40) && (degreeOfFreedom<60)) {
         row = 30;
      } else if ((degreeOfFreedom>=60) && (degreeOfFreedom<120)) {
         row = 31;
      } else if ((degreeOfFreedom>=120) && (degreeOfFreedom<1000)) {
         row = 32;
      } else if (degreeOfFreedom>=1000) {
         row = 33;
      } else if (degreeOfFreedom <= 0) {
      }

/*
      System.out.println("");
      System.out.println("getTValue() - row = " + row);
      System.out.println("getTValue() - column = " + column);
*/

      return ttbl[row][column];

    }


    /**
     * getSampleMeans(): obtain the mean value for each sample
     *
     */
    private Matrix getSampleMeans(Matrix expMatrix) {

        int numOfColumns = expMatrix.getColumnDimension();
        int numOfRows = expMatrix.getRowDimension();

	Matrix means = new Matrix(1, numOfColumns);

	double currentSum = 0;
        double currentMean =0;
	int denominator = 0;
	double value;

//        System.out.println("getSampleMeans() - numOfColumns = " + numOfColumns);

	for (int column=0; column<numOfColumns; column++) {
	    currentSum = 0.0;
	    denominator = 0;
	    for (int row=0; row<numOfRows; row++) {
		value = expMatrix.get(row, column);
		if (!Double.isNaN(value)) {
		    currentSum += value;
		    denominator++;
		}
	    }

//            System.out.println("getSampleMeans() - currentSum = " + currentSum + ", denominator = " + denominator);

            currentMean = (currentSum/denominator);
	    means.set(0, column, currentMean);
	}

/*
        for (int i=0; i<numOfColumns; i++) { 
            System.out.println("getSampleMeans() - mean[" + i + "] = " + means.get(0,i));
        }
        System.out.println(" ");
*/

	return means;
    }
   

    private Matrix getSampleVariances(Matrix expMatrix, Matrix means) {
	final int rows = means.getRowDimension();
	final int columns = means.getColumnDimension();
	Matrix variances = new Matrix(rows, columns);
	for (int row=0; row<rows; row++) {
	    for (int column=0; column<columns; column++) {
		variances.set(row, column, getSampleVariance(expMatrix, means));
	    }
	}
	return variances;
    }
  

    int denominator = 0;
    /**
     * getSampleNormalizedSum(): obtain the normalized sum value for all samples
     *
     */
    private double getSampleNormalizedSum(Matrix expMatrix, Matrix means) {

	double sum = 0;
	double value = 0;

        int numOfGenes = expMatrix.getRowDimension();
        int numOfSamples = expMatrix.getColumnDimension();

        for (int sample=0; sample<numOfSamples; sample++) {

            double mean = means.get(0,sample);

	    for (int row=0; row<numOfGenes; row++) {
	        value = expMatrix.get(row, sample);
	        if (!Double.isNaN(value)) {
		    sum += Math.pow(value - mean, 2);
		    denominator++;
	        }
            }
	}
	return sum;
    }

    /**
     * getSampleVariance(): obtain the sample variance for all samples
     *
     */
    private double getSampleVariance(Matrix expMatrix, Matrix means) {

        int denominator = 0;
        double sum = 0;
	double value = 0;

        int numOfSamples = expMatrix.getColumnDimension();
        int numOfGenes = expMatrix.getRowDimension();

        for (int sample=0; sample<numOfSamples; sample++) {

            double mean = means.get(0,sample);

	    for (int row=0; row<numOfGenes; row++) {
	        value = expMatrix.get(row, sample);
	        if (!Double.isNaN(value)) {
		    sum += Math.pow(value - mean, 2);
		    denominator++;
	        }
            }
	}

/*
        System.out.println("getSampleVariance() - sum = " + sum);
        System.out.println("getSampleVariance() - denominator = " + denominator);
        System.out.println("getSampleVariance() - sampleVariance = " + Math.sqrt(sum/(denominator-1)));
        System.out.println(" ");
*/

	return Math.sqrt(sum/(denominator-1));
    }    
 
    /** 
     * getCovarianceMatrix(): obtain the covarianceMatrix
     *
     */
    private Matrix getCovarianceMatrix(Matrix expMatrix, Matrix means) {

	final int rows = expMatrix.getRowDimension();
	final int columns = expMatrix.getColumnDimension();

        Matrix [] X = new Matrix[columns];

        for (int column=0; column<columns; column++) {
            X[column] = new Matrix(1, rows);
            X[column] = (expMatrix.getMatrix(0, rows-1, column, column)).transpose(); 

/*
	    System.out.println("getCovarianceMatrix() -- X[" + column + "] Matrix : ");
	    for (int i=0; i<X[column].getColumnDimension(); i++) {
		System.out.print(X[column].get(0, i) + ", ");
	    } 
	    System.out.println(" ");
	    System.out.println(" ");
*/
        }

        Matrix covariance = new Matrix(rows, rows);

/*
	System.out.println("getCovarianceMatrix() -- mean matrix: ");
	for (int i=0; i<means.getColumnDimension(); i++) {
	    System.out.print(means.get(0, i) + ", ");
	} 
	System.out.println(" ");
	System.out.println(" ");
*/


        for (int column=0; column < columns; column++) {
            covariance = covariance.plus(((X[column].minus(means)).transpose()).times((X[column].minus(means)))); 
        }

        covariance = covariance.times(1.0/(double)(columns-1));

/*
        System.out.println("getCovarianceMatrix() -- covariance matrix: ");
        for (int row=0; row<covariance.getRowDimension(); row++) {
            for (int column=0; column<covariance.getColumnDimension(); column++) {
                System.out.print(covariance.get(row, column) + ", ");
            }
            System.out.println(" ");
        } 
        System.out.println(" ");
*/

	return covariance;
    }


    /**
     * getGroupMean(): obtain the mean value for a given group
     *
     */
    private double getGroupMean(double[] group) {
	double sum = 0;
	int n = 0;
	for (int i = 0; i < group.length; i++) {
	    if (!Double.isNaN(group[i])) {
		sum = sum + group[i];
		n++;
	    }
	}
	
	if (n == 0) {
            return Double.NaN;
        }
	double mean =  sum / n;
        
        if (Double.isInfinite(mean)) {
            return Double.NaN;
        }
        
	return mean;
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


    public Matrix getJamaMatrix(FloatMatrix floatMatrix) {

        int rows = floatMatrix.getRowDimension();
        int columns = floatMatrix.getColumnDimension();

        double[][] values = new double[rows][columns];

        for(int i=0; i<rows; i++) {
            for(int j=0; j<columns; j++) {
                values[i][j] = (double) floatMatrix.A[i][j];
            }
        }

        Matrix jamaMatrix = new Matrix(values);

        return jamaMatrix;
    }


    public FloatMatrix getFloatMatrix(Matrix doubleMatrix) {
        int rows = doubleMatrix.getRowDimension();
        int columns = doubleMatrix.getColumnDimension();

        float[][] values = new float[rows][columns];

        for(int i=0; i<rows; i++) {
            for(int j=0; j<columns; j++) {
                values[i][j] = (float) ((doubleMatrix.getArray())[i][j]);
            }
        }

        FloatMatrix floatMatrix = new FloatMatrix(values);

        return floatMatrix;
    }


    private Matrix getMeansForGenes(Vector[] clusters) {
	Matrix means = new Matrix(clusters.length, numberOfSamples);
	Matrix mean;
	for (int i=0; i<clusters.length; i++) {
	    mean = getMeanForGenes(clusters[i]);
            means.setMatrix(i, i, 0, numberOfSamples-1, mean);
	}
	return means;
    }
    
    private Matrix getMeanForGenes(Vector cluster) {
	Matrix mean = new Matrix(1, numberOfSamples);
	double currentMean;
	int n = cluster.size();
	int denom = 0;
	double value;
	for (int i=0; i<numberOfSamples; i++) {
	    currentMean = 0.0d;
	    denom  = 0;
	    for (int j=0; j<n; j++) {
		value = expMatrix.get(((Integer) cluster.get(j)).intValue(), i);
		if (!Double.isNaN(value)) {
		    currentMean += value;
		    denom++;
		}
	    }
	    mean.set(0, i, currentMean/(double)denom);
	}
	return mean;
    }

    private Matrix getVariancesForGenes(Vector[] clusters, Matrix means) {
	final int rows = means.getRowDimension();
	final int columns = means.getColumnDimension();
	Matrix variances = new Matrix(rows, columns);
	for (int row=0; row<rows; row++) {
	    for (int column=0; column<columns; column++) {
		variances.set(row, column, getClusterVarianceForGenes(clusters[row], column, means.get(row, column)));
	    }
	}
	return variances;
    }

    int validN;
    private double getSampleNormalizedSumForGenes(Vector cluster, int column, double mean) {
	final int size = cluster.size();
	double sum = 0f;
	validN = 0;
	double value;
	for (int i=0; i<size; i++) {
	    value = expMatrix.get(((Integer) cluster.get(i)).intValue(), column);
	    if (!Double.isNaN(value)) {
		sum += Math.pow(value-mean, 2);
		validN++;
	    }
	}
	return sum;
    }
 
    private double getClusterVarianceForGenes(Vector cluster, int column, double mean) {
	return Math.sqrt(getSampleNormalizedSumForGenes(cluster, column, mean)/(validN-1));
    }    


    private Matrix getMeans(Vector[] clusters) {
	Matrix means = new Matrix(clusters.length, numberOfGenes);
	Matrix mean;
	for (int i=0; i<clusters.length; i++) {
	    mean = getMean(clusters[i]);
            means.setMatrix(i, i, 0, numberOfGenes-1, mean);
	}
	return means;
    }
    
    private Matrix getMean(Vector cluster) {
	Matrix mean = new Matrix(1, numberOfGenes);
	double currentMean;
	int n = cluster.size();
	int denom = 0;
	double value;
	for (int i=0; i<numberOfGenes; i++) {
	    currentMean = 0.0d;
	    denom  = 0;
	    for (int j=0; j<n; j++) {
		value = expMatrixTranspose.get(((Integer) cluster.get(j)).intValue(), i);
		if (!Double.isNaN(value)) {
		    currentMean += value;
		    denom++;
		}
	    }
	    mean.set(0, i, currentMean/(double)denom);
	}
	return mean;
    }

    private Matrix getVariances(Vector[] clusters, Matrix means) {
	final int rows = means.getRowDimension();
	final int columns = means.getColumnDimension();
	Matrix variances = new Matrix(rows, columns);
	for (int row=0; row<rows; row++) {
	    for (int column=0; column<columns; column++) {
		variances.set(row, column, getClusterVariance(clusters[row], column, means.get(row, column)));
	    }
	}
	return variances;
    }

 
    private double getSampleNormalizedSum(Vector cluster, int column, double mean) {
	final int size = cluster.size();
	double sum = 0f;
	validN = 0;
	double value;
	for (int i=0; i<size; i++) {
	    value = expMatrixTranspose.get(((Integer) cluster.get(i)).intValue(), column);
	    if (!Double.isNaN(value)) {
		sum += Math.pow(value-mean, 2);
		validN++;
	    }
	}
	return sum;
    }
 
    private double getClusterVariance(Vector cluster, int column, double mean) {
	return Math.sqrt(getSampleNormalizedSum(cluster, column, mean)/(validN-1));
    }    
   

    private int[] convert2int(Vector source) {
	int[] int_matrix = new int[source.size()];
	for (int i=0; i<int_matrix.length; i++) {
	    int_matrix[i] = ((Integer) source.get(i)).intValue();
	}
	return int_matrix;
    }    


    private void printMatrix(Matrix matrix) {

            DecimalFormat df = new DecimalFormat("####.000000");

	    for (int row=0; row<matrix.getRowDimension(); row++) {
		for (int column=0; column<matrix.getColumnDimension(); column++) {
			System.out.print(df.format(matrix.get(row, column)) + "  ");
		}
		System.out.println(" ");
	    } 
	    System.out.println(" ");
	    System.out.println(" ");
    }

    private void printMatrix(FloatMatrix matrix) {

            DecimalFormat df = new DecimalFormat("####.000000");

	    for (int row=0; row<matrix.getRowDimension(); row++) {
		for (int column=0; column<matrix.getColumnDimension(); column++) {
			System.out.print(df.format(matrix.get(row, column)) + "  ");
		}
		System.out.println(" ");
	    } 
	    System.out.println(" ");
	    System.out.println(" ");
    }

    public void abort() {
	stop = true;
    }
}
