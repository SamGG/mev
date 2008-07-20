/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * http://genomebiology.com/2003/4/12/R83
 * Created on Oct 28, 2004
 */
package org.tigr.microarray.mev.cluster.gui.impl.usc;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SpringLayout;

/**
 * Uncorrelated Shrunken Centroid Algorithm as published in <a
 * href="http://genomebiology.com/2003/4/12/R83">Genome Biology </a> by Kayee
 * Yeung. <br>
 * <br>
 * This code works only for single measurements (no replicate support). It is
 * assumed that the incoming hybs are all sorted identically and that there are
 * no null or NaN values as ratios <br>
 * <br>
 * The main steps are: <br>
 * <br>
 * 1. Split the full set of training hybs into a training subset and a test
 * subset. The cross validation will be run numFold times, wherein each hyb
 * needs to be used as a test hyb once and only once against all the other
 * training hybs. Eg. if there are 10 hybs and numFold is 5, 2 hybs are randomly
 * selected to be used as test hybs for each fold run. During the next fold run,
 * 2 others are randomly selected. This is repeated numFold times such that all
 * hybs were tested once. Remainder hybs are thrown into the last fold's test
 * set <br>
 * <br>
 * 2. Calculate the gene centroid ( the mean ratio for all hybs for each gene )
 * <br>
 * <br>
 * 3. Calculate the class centroids ( the mean ratio for the hybs in each class )
 * <br>
 * <br>
 * 4. Calculate Mk values ( standardizing factor )<br>
 * <br>
 * 5. Calculate S values ( sum of intra-class standard deviations )<br>
 * <br>
 * 6. Calculate s0 ( median S value )<br>
 * <br>
 * 7. Compute Relative Difference ( difference between class centroid and gene
 * centroid standardized by Mk, S, and s0 )<br>
 * <br>
 * 8. Shrink | Relative Difference | by delta ( delta is a random value between
 * 0 and deltaMax incremented by deltaStep )<br>
 * <br>
 * 9. Do soft thresholding on Shrunken Relative Difference ( if subtracting
 * delta from the absolute value of Relative Difference becomes negative, remove
 * that gene from the analysis because it is not significantly different from
 * the gene centroid )<br>
 * <br>
 * 10. Compute Shrunken Class Centroid ( class Centroid + standardized Shrunken
 * Relative Difference )<br>
 * <br>
 * 11. Sort the remaining genes from greatest to least Shrunken Relative
 * Difference <br>
 * <br>
 * 12. Compute pairwise correlation between each gene and the gene with the next
 * greatest Shrunken Relative Difference. <br>
 * <br>
 * 13. Remove from testing if correlation is less than rho ( rho is .5, .6, .7.
 * 8. 9, 1.0 )<br>
 * <br>
 * 14. Compute a new discriminant score for a test hyb against each class <br>
 * <br>
 * 15. Assign the new test hyb to the class with the minimum discriminant score
 * 
 * @author vu
 */
public class USCCrossValidation {
    private int deltaKount;
    private int deltaMax;
    private int foldKount;
    private int xValKount;
    private double rhoMin;
    private double rhoMax;
    private double rhoStep;
    private double deltaStep;


    /**
     * Default and sole constructor
     * 
     * @param hybSetP The training hyb set
     * @param numDeltasP
     * @param deltaMaxP
     * @param rhoMinP
     * @param rhoMaxP
     * @param rhoStepP
     * @param numFoldP
     */
    public USCCrossValidation(int numDeltasP, int deltaMaxP, double rhoMinP,
            double rhoMaxP, double rhoStepP, int numFoldP, int xValKountP) {
        this.deltaKount = numDeltasP;
        this.deltaMax = deltaMaxP;
        this.rhoMin = rhoMinP;
        this.rhoMax = rhoMaxP;
        this.rhoStep = rhoStepP;
        this.foldKount = numFoldP;
        this.xValKount = xValKountP;

        double dDelta = this.deltaMax;
        double dNum = this.deltaKount;
        this.deltaStep = dDelta / dNum;
    }//end constructor


    /**
     * Cross Validate the data.
     * 
     * The number of results is the # of Delta/Rho possibilties ( i.e. numDeltas *
     * 6 )
     * 
     * @param fullSet
     * @return
     */
    public USCDeltaRhoResult[][][] crossValidate(USCHybSet fullSet, Frame frame) {
        frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        
        //[ xVal ][ fold ][ result ]
        USCDeltaRhoResult[][][] xResult = null;

        //display a progress bar
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new SpringLayout());
        JPanel leftPanel = new JPanel();
        leftPanel.add(new JLabel("     "));
        JPanel rightPanel = new JPanel();
        rightPanel.add(new JLabel("     "));

        JPanel midPanel = new JPanel();
        BoxLayout midBox = new BoxLayout(midPanel, BoxLayout.Y_AXIS);
        midPanel.setLayout(midBox);
        JLabel label = new JLabel("Cross Validating... Please Wait");
        JLabel label2 = new JLabel("This will take a few minutes");
        JLabel foldLabel = new JLabel("Fold/CrossVal runs");
        JLabel deltaLabel = new JLabel("Deltas");
        JLabel rhoLabel = new JLabel("Rhos");
        JLabel corrLabel = new JLabel("Pairwise Genes");
        JLabel blankLabel = new JLabel(" ");
        midPanel.add(label);
        midPanel.add(label2);
        midPanel.add(blankLabel);
        JProgressBar foldBar = new JProgressBar(0, ( this.foldKount * this.xValKount ));
        foldBar.setIndeterminate(false);
        foldBar.setStringPainted(true);
        JProgressBar deltaBar = new JProgressBar(0, this.deltaKount);
        deltaBar.setIndeterminate(false);
        deltaBar.setStringPainted(true);
        JProgressBar rhoBar = new JProgressBar(5, 11);
        rhoBar.setIndeterminate(false);
        rhoBar.setStringPainted(true);
        JProgressBar corrBar = new JProgressBar(0, fullSet.getNumGenes());
        corrBar.setIndeterminate(false);
        corrBar.setStringPainted(true);
        midPanel.add(foldLabel);
        midPanel.add(foldBar);
        midPanel.add(deltaLabel);
        midPanel.add(deltaBar);
        midPanel.add(rhoLabel);
        midPanel.add(rhoBar);
        midPanel.add(corrLabel);
        midPanel.add(corrBar);

        mainPanel.add(leftPanel);
        mainPanel.add(midPanel);
        mainPanel.add(rightPanel);
        SpringUtilities.makeCompactGrid(mainPanel, 1, 3, 0, 0, 0, 0);
        JFrame jf = new JFrame("Cross Validating");
        jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jf.getContentPane().add(mainPanel);
        jf.setSize(250, 250);
        jf.show();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        jf.setLocation(( screenSize.width - 200 ) / 2,
                ( screenSize.height - 100 ) / 2);
        int iProgress = 0;
        
        //figure out how many Delta/Rho combos there are
        int iRho = 0;
        double currentRho = this.rhoMin;
        while(currentRho < this.rhoMax) {
            iRho ++;
            currentRho += this.rhoStep;
        }
        int iTrainStep = this.foldKount * this.xValKount;
        int resultKount = this.deltaKount * iRho;
        
    	xResult = new USCDeltaRhoResult[ this.xValKount ][ this.foldKount ][];
    	int xResultKount = 0;
        
        //do multiple cross validations (added 1.30.05)
        //*** Assuming that there will be a result for each CV Run ***//
        for( int m = 0; m < this.xValKount; m++ ) {
            //loop through the folds, during each fold, a different set of Hybs
            // are left out
            for( int f = 0; f < this.foldKount; f++ ) {
                deltaBar.setValue(0);

                //hold the results from this fold
                xResult[ m ][ f ] = new USCDeltaRhoResult[resultKount];
                int iResult = 0;

                //arrays of USCHyb objects
                USCHyb[] subTestArray = fullSet.getTestArray(f);
                USCHyb[] subTrainArray = fullSet.getTrainArray(f);

                //loop through the deltas
                double delta = 0.0f;
                for( int d = 0; d < this.deltaKount; d++ ) {
                	rhoBar.setValue( 5 );
                	
                    //loop through the rhos
                    double rho;
                    
                    for( int r = 5; r < 11; r++ ) {
                        rho = ( double ) r * 0.1f;

                        USCDeltaRhoResult drResult = this.doDR(subTrainArray,
                                subTestArray, delta, rho, fullSet.getNumGenes(),
                                fullSet.getNumClasses(), fullSet
                                        .getUniqueClasses(), corrBar, r);
                        if( drResult == null ) {
                            //do nothing
                        	xResult[ m ][ f ][ iResult ] = new USCDeltaRhoResult();
                        } else {
                            //store it
                        	xResult[ m ][ f ][ iResult ] = drResult;
                            iResult++;
                        }
                        
                        rhoBar.setValue( r );
                    }//end r (rho)

                    delta += this.deltaStep;
                    deltaBar.setValue(d + 1);
                }//end d (deltas)
                
                xResultKount ++;

                //update progress bar
                foldBar.setIndeterminate(false);
                iProgress++;
                foldBar.setValue(iProgress);
                foldBar.setStringPainted(true);
            }//end f (folds)

            //toReturn.setFoldResult(resultArray, m);
        }//end m (xValKount)

        jf.dispose();
        frame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        
        return xResult;
    }//end crossValidate


    /**
     * Does Discriminant Score Class Assignments to hybs in testArray for one
     * d/r
     * 
     * @param trainArray USCHyb[] of the hybs used for Training
     * @param testArray USCHyb[] of the hybs to be classified
     * @param delta amount by which Dik should be shrunk
     * @param rho Low Correlation threshold
     * @param numGenes Number of Genes in hybs
     * @param numClasses Number of Classes in set of hybs
     * @param uniqueClasses String[] of the labels of each class in set of hybs
     * @return double[ numTestHybs ][ numClasses ]
     */
    public USCDeltaRhoResult doDR(USCHyb[] trainArray, USCHyb[] testArray,
            double delta, double rho, int numGenes, int numClasses,
            String[] uniqueClassLabels, JProgressBar rhoBar, int iRho) {
        //keep track of gene order by creating a USCOrder object for each gene
        USCOrder[] order = new USCOrder[numGenes];
        for( int g = 0; g < numGenes; g++ ) {
            order[g] = new USCOrder(g);
        }

        //each gene has a centroid (mean)
        double[] geneCentroids = this
                .computeGeneCentroids(trainArray, numGenes);

        //each class in each gene has a centroid (class mean) [ classes ][
        // genes ]
        double[][] classCentroids = this.computeClassCentroids(trainArray,
                uniqueClassLabels, numGenes);

        //each class has an mk value [ numClasses ]
        double[] mks = this.computeMks(trainArray, uniqueClassLabels);

        //each gene has an si value [ numGenes ]
        double[] sis = this.computeSis(trainArray, classCentroids,
                uniqueClassLabels, numGenes);

        //s0 is the median si
        double s0 = this.computeMedian(sis);

        //dik is the relative difference of each classCentroid from the
        // geneCentroid (standardized) [ class ][ gene ]
        double[][] dik = this.computeRelativeDifferences(classCentroids,
                geneCentroids, mks, sis, s0);

        //do soft thresholding on diks ( shrunken relative difference )
        double[][] dikShrunk = this.shrinkDiks(delta, dik);

        //compute shrunken class centroids (do for all, deal with removed genes
        // later)
        double[][] shrunkenCentroids = this.computeShrunkenClassCentroid(
                geneCentroids, mks, sis, s0, dikShrunk);

        //find and store Beta (greatest |dikShrunk|)
        for( int g = 0; g < numGenes; g++ ) {
            double maxDik = 0;
            for( int c = 0; c < numClasses; c++ ) {
                //double toTest = dikShrunk[ c ][ g ];
                double toTest = Math.abs(dikShrunk[c][g]);
                if( toTest > maxDik ) {
                    maxDik = toTest;
                }
            }//end c (classes)
            order[g].setBeta(maxDik);
        }//end g (genes)

        //figure out which genes to keep using and store their indices in a
        // BitSet
        //get a BitSet where each gene is marked true when it is relevant
        BitSet bsUse = this.findRelevantGenes(dikShrunk, order);
        int numRelevant = 0;
        for( int g = 0; g < numGenes; g++ ) {
            if( bsUse.get(g) == true ) {
                //order[g].setRelevant(true);
                numRelevant++;
            } else {
                //order[g].setRelevant(false);
            }
        }//end g (genes)

        //sort the genes based on Beta from greatest to least
        Arrays.sort(order, new USCRelevanceComparator());
        //from this point on, order[ numgenes] is sorted by beta

        //store the new order in case we need it later
        for( int g = 0; g < order.length; g++ ) {
            order[g].setIRelevant(g);
        }//end g

        //do pairwise correlation testing if there are any relevant genes
        if( numRelevant > 0 ) {
            this.doCorrelationTesting(order, trainArray, rho, rhoBar, geneCentroids, iRho);
        }//end if( numRelevant > 0 )

        //compute discriminant scores
        double[][] discScores = this.computeDiscriminantScores(trainArray, testArray, 
        		shrunkenCentroids, order, sis, s0, uniqueClassLabels);

        //return a result if there is one
        if( discScores == null ) {
            //System.out.println( "Null Discriminant Scores\tDelta = " + delta
            // + "\tRho = " + rho );
        	return null;
        } else {
            //count the number of genes used
            int iGenes = 0;
            for( int g = 0; g < order.length; g++ ) {
                if( order[g].use() ) {
                    iGenes++;
                }
            }//end g
            
            //tally up scores and return
            int numWrong = 0;
            int numRight = 0;
            for(int h = 0; h < testArray.length; h ++) {
                USCHyb hyb = testArray[ h ];
                String label = hyb.getHybLabel();
                
                double dLow = 9999999;
                int iMin = 0;
                for(int c = 0; c < discScores[ h ].length; c ++) {
                    if( discScores[ h ][ c ] < dLow ) {
                    	dLow = discScores[ h ][ c ];
                    	iMin = c;
                    }
                }
                
                if( uniqueClassLabels[ iMin ].equals(label)) {
                	numRight ++;
                	//System.out.println("Correct:" + uniqueClassLabels[ iMin ] + " = " + label);
                } else {
                	numWrong ++;
                }//c
            }//h
            
            return new USCDeltaRhoResult(delta, rho, numWrong, numRight, iGenes);
        }
    }//end trainTrain()


    /**
     * Does Discriminant Score Class Assignments to hybs in testArray for one
     * d/r
     * 
     * @param trainArray
     *            USCHyb[] of the hybs used for Training
     * @param testArray
     *            USCHyb[] of the hybs to be classified
     * @param delta
     *            amount by which Dik should be shrunk
     * @param rho
     *            Low Correlation threshold
     * @param numGenes
     *            Number of Genes in hybs
     * @param numClasses
     *            Number of Classes in set of hybs
     * @param uniqueClasses
     *            String[] of the labels of each class in set of hybs
     * @return double[ numTestHybs ][ numClasses ]
     */
    public USCResult testTest(USCHyb[] trainArray, USCHyb[] testArray,
            double delta, double rho, int numGenes, int numClasses,
            String[] uniqueClassLabels, JProgressBar corrBar, int iRho) {
        //keep track of gene order by creating a USCOrder object for each gene
        USCOrder[] order = new USCOrder[numGenes];
        for( int g = 0; g < numGenes; g++ ) {
            order[g] = new USCOrder(g);
        }

        //each gene has a centroid (mean)
        double[] geneCentroids = this
                .computeGeneCentroids(trainArray, numGenes);

        //each class in each gene has a centroid (class mean) [ classes ][
        // genes ]
        double[][] classCentroids = this.computeClassCentroids(trainArray,
                uniqueClassLabels, numGenes);

        //each class has an mk value [ numClasses ]
        double[] mks = this.computeMks(trainArray, uniqueClassLabels);

        //each gene has an si value [ numGenes ]
        double[] sis = this.computeSis(trainArray, classCentroids,
                uniqueClassLabels, numGenes);

        //s0 is the median si
        double s0 = this.computeMedian(sis);

        //dik is the relative difference of each classCentroid from the
        // geneCentroid (standardized) [ class ][ gene ]
        double[][] dik = this.computeRelativeDifferences(classCentroids,
                geneCentroids, mks, sis, s0);

        //do soft thresholding on diks ( shrunken relative difference )
        double[][] dikShrunk = this.shrinkDiks(delta, dik);

        //compute shrunken class centroids (do for all, deal with removed genes
        // later)
        double[][] shrunkenCentroids = this.computeShrunkenClassCentroid(
                geneCentroids, mks, sis, s0, dikShrunk);

        //find and store Beta (greatest |dikShrunk|)
        for( int g = 0; g < numGenes; g++ ) {
            double maxDik = 0;
            for( int c = 0; c < numClasses; c++ ) {
                //double toTest = dikShrunk[ c ][ g ];
                double toTest = Math.abs(dikShrunk[c][g]);
                if( toTest > maxDik ) {
                    maxDik = toTest;
                }
            }//end c (classes)
            order[g].setBeta(maxDik);
        }//end g (genes)

        //figure out which genes to keep using and store their indices in a
        // BitSet
        //get a BitSet where each gene is marked true when it is relevant
        BitSet bsUse = this.findRelevantGenes(dikShrunk, order);
        int numRelevant = 0;
        for( int g = 0; g < numGenes; g++ ) {
            if( bsUse.get(g) == true ) {
                //order[g].setRelevant(true);
                numRelevant++;
            } else {
                //order[g].setRelevant(false);
            }
        }//end g (genes)

        //sort the genes based on Beta from greatest to least
        Arrays.sort(order, new USCRelevanceComparator());
        //from this point on, order[ numgenes] is sorted by beta

        //store the new order in case we need it later
        for( int g = 0; g < order.length; g++ ) {
            order[g].setIRelevant(g);
        }//end g

        //do pairwise correlation testing if there are any relevant genes
        if( numRelevant > 0 ) {
            this.doCorrelationTesting(order, trainArray, rho, corrBar,
                    geneCentroids, iRho);
        }//end if( numRelevant > 0 )

        //compute discriminant scores
        double[][] discScores = this.computeDiscriminantScores(trainArray,
                testArray, shrunkenCentroids, order, sis, s0, uniqueClassLabels);

        //return a result if there is one
        if( discScores == null ) {
            //System.out.println( "Null Discriminant Scores\tDelta = " + delta
            // + "\tRho = " + rho );
            return null;
        } else {
            //count the number of genes used
            int iGenes = 0;
            for( int g = 0; g < order.length; g++ ) {
                if( order[g].use() ) {
                    iGenes++;
                }
            }//end g

            return new USCResult(discScores, iGenes, delta, rho, order);
        }
    }//end trainTrain()


    /**
     * Computes the discriminant score for a new hyb compared to a trained class
     * by comparing the ratios of the new hyb to the shrunken class centroids
     * 
     * @param testArray ratios of the new test hyb
     * @param shrkClsCntrds shrunkenClassCentroids
     * @param order USCOrder[ numGenes ] contains sorted & original indices
     * @param sis s values
     * @param s0 s0
     * @param numHybs total # of hybs in training set
     * @return double[ numTestHybs ][ numClasses ]
     */
    private double[][] computeDiscriminantScores(USCHyb[] trainArray,
            USCHyb[] testArray, double[][] shrkClsCntrds, USCOrder[] order,
            double[] sis, double s0, String[] uniqueClasses) {
        double[][] toReturn = new double[testArray.length][uniqueClasses.length];
        
        //loop through the test hybs
        for( int h = 0; h < testArray.length; h++ ) {
            //compute a discriminant score for this hyb against each class

            //loop through classes
            for( int c = 0; c < shrkClsCntrds.length; c++ ) {
                //sum up the standardized square distances between gene &
                // shrkClsCntrds for all genes 
                double classScore = 0.0f;
                double classProb = 0.0f;

                //need the class prior probability for this class
                double fHybsInClassKount = ( double ) this
                        .getNumClassHybsInUSCHybArray(trainArray,
                                uniqueClasses[c]);
                double fHybKount = ( double ) trainArray.length;
                classProb = Math.log(fHybsInClassKount / fHybKount);

                //loop through genes
                for( int g = 0; g < order.length; g++ ) {
                    //only do computation if gene is relevant and uncorrelated
                    if( order[g].use() ) {
                        //compute square diff between gene ratio &
                        // shrkClsCntrds

                        //need to know the index in unsorted array for gene of
                        // interest
                        int iOrig = order[g].getIOriginal();

                        //get the ratio of the gene
                        double ratio = testArray[h].getRatio(iOrig);

                        //calculate diffSquare
                        double diffSquare = ( ratio - shrkClsCntrds[c][iOrig] )
                                * ( ratio - shrkClsCntrds[c][iOrig] );

                        //divide by standardizing factor
                        double denom = ( sis[iOrig] + s0 ) * ( sis[iOrig] + s0 );

                        //calculate the score for this gene
                        double geneScore = ( diffSquare / denom );
                        classScore = classScore + geneScore;
                    } else {
                        //System.out.println( "gene:" + order[ g
                        // ].getIOriginal() + " not used" );
                    }
                }//end g (genes)

                toReturn[h][c] = ( classScore - ( 2 * classProb ) );
            }//end c (classes)
        }//end h (hybs)

        return toReturn;
    }//end computeDiscriminantScore()


    /**
     * Having sorted the genes from Greatest to Least Beta, compare pairwise
     * correlation.
     * 
     * @param order
     * @param trainArray
     * @param rho
     */
    private void doCorrelationTesting(USCOrder[] order, USCHyb[] trainArray,
            double rho, JProgressBar corrBar, double[] geneCentroids, int iRho) {
        //loop through the rho values
        for( int r = 9; r >= iRho; r -- ) {
            //System.out.println("r = " + r);
            double dRho = (double) r / 10.0;
            
	        //loop through the ordered genes
	        for( int o = 0; o < order.length; o++ ) {
	            //don't test if this is irrelevant or has already been deemed correlated
	            if( order[o].use() ) {
	                int iFirstGene = order[o].getIOriginal();
	
	                //now test it against every other gene
	                for( int j = ( o + 1 ); j < order.length; j++ ) {
	                    //only worry about it if it's relevant and hasn't already been tossed
	                    if( order[j].use() ) {
	                        int iSecondGene = order[j].getIOriginal();
	
	                        if( iFirstGene != iSecondGene ) {
	                            double correlation = Math.abs(this
	                                    .computeCorrelation(trainArray, iFirstGene,
	                                            iSecondGene, geneCentroids));
	
	                            if( ( correlation > dRho ) || ( correlation == dRho ) ) {
	                                //System.out.println( "gene:" + order[ j
	                                // ].getIOriginal() + " - " + correlation + " > " + rho);
	                                order[j].setCorrelated(true);
	                            } else {
	                                //System.out.println( "gene" + order[ j ].getIOriginal() 
	                                //        + " - " +correlation + " Less Than " + rho);
	                                order[j].setCorrelated(false);
	                            }
	                        }
	                    }
	                }//end j
	            }//end if
	
	            corrBar.setValue(o);
	            corrBar.setStringPainted(true);
	        }//end o
        }//end r
    }//doCorrelationTesting()


    /**
     * Computes pairwise correlation between geneX and geneY
     * 
     * @param trainArray
     *            USCHyb[] training set
     * @param geneCentroids
     *            Overall centroid for each gene
     * @param iGeneX
     *            Original gene index of 1st gene
     * @param iGeneY
     *            Original gene index of 2nd gene
     * @return
     */
    private double computeCorrelation(USCHyb[] trainArray, int iGeneX,
            int iGeneY, double[] geneCentroids) {
        double toReturn = 0;

        double[] xRatios = new double[trainArray.length];
        double[] yRatios = new double[trainArray.length];

        //loop through hybs (create an array of hyb ratios)
        for( int i = 0; i < trainArray.length; i++ ) {
            USCHyb hyb = trainArray[i];
            xRatios[i] = hyb.getRatio(iGeneX);
            yRatios[i] = hyb.getRatio(iGeneY);
        }//end i (hybs)

        double xMean = geneCentroids[iGeneX];
        double yMean = geneCentroids[iGeneY];

        double numSum = 0;
        double xSum = 0;
        double ySum = 0;

        for( int i = 0; i < trainArray.length; i++ ) {
            USCHyb hyb = trainArray[i];
            numSum += ( ( hyb.getRatio(iGeneX) - xMean ) * ( hyb
                    .getRatio(iGeneY) - yMean ) );
            xSum += ( ( xRatios[i] - xMean ) * ( xRatios[i] - xMean ) );
            ySum += ( ( yRatios[i] - yMean ) * ( yRatios[i] - yMean ) );
        }

        toReturn = numSum / ( double ) Math.sqrt(xSum * ySum);
        return toReturn;
    }//end computeCorrelation()


    /**
     * Calculates the shrunken class centroid = class centroid + mk( si + s0
     * )dikShrunk
     * 
     * @param geneCentroids
     * @param mks
     * @param sis
     * @param s0
     * @param dikShrunk
     * @return
     */
    private double[][] computeShrunkenClassCentroid(double[] geneCentroids,
            double[] mks, double[] sis, double s0, double[][] dikShrunk) {
        double[][] toReturn = new double[mks.length][geneCentroids.length];

        //loop through the classes
        for( int c = 0; c < mks.length; c++ ) {
            //loop through the genes
            for( int g = 0; g < geneCentroids.length; g++ ) {
                toReturn[c][g] = geneCentroids[g]
                        + ( mks[c] * dikShrunk[c][g] * ( sis[g] + s0 ) );
            }//end j (genes)
        }//end i (classes)

        return toReturn;
    }//end computeShrunkenClassCentroid()


    /**
     * Finds the shrunken dik value whose absolute value is greatest
     * 
     * @param dikSig
     * @return
     */
    private double[] findBeta(double[][] dikSig) {
        double[] toReturn = new double[dikSig[0].length];

        //loop through genes
        for( int i = 0; i < toReturn.length; i++ ) {
            double currentHigh = 0;

            //loop through class shrunken diks
            for( int j = 0; j < dikSig.length; j++ ) {
                if( dikSig[j][i] > currentHigh ) {
                    currentHigh = dikSig[j][i];
                }
            }//end j

            toReturn[i] = currentHigh;
        }//end i

        return toReturn;
    }//end findBeta()


    /**
     * Removes the genes identified by index in vRemove
     * 
     * @param dikShrunk
     *            double[ class ][ genes ]
     * @param vRemove
     *            Vector of Integer objects representing gene index of genes to
     *            remove
     * @return new double[ class ][ genes ] with insignificant genes removed
     */
    private double[][] removeInsignificantGenes(double[][] dikShrunk,
            Vector vRemove) {
        int numGenes = dikShrunk[0].length;
        int iSignificant = numGenes - vRemove.size();
        int index = 0;
        double[][] toReturn = new double[dikShrunk.length][iSignificant];

        for( int i = 0; i < numGenes; i++ ) {
            boolean include = true;

            //loop through the remove indices
            for( int j = 0; j < vRemove.size(); j++ ) {
                Integer IRemove = ( Integer ) vRemove.elementAt(j);
                if( i == IRemove.intValue() ) {
                    //this is a gene that should be removed
                    include = false;
                    break;
                }
            }//end j (remove indices)

            if( include ) {
                //loop through classes
                for( int j = 0; j < dikShrunk.length; j++ ) {
                    //System.out.println( "i:" + i + "\tj:" + j + "\tindex:" +
                    // index );
                    toReturn[j][index] = dikShrunk[j][i];
                }//end j
                index++;
            }
        }

        return toReturn;
    }//end removeInsignificantGenes()


    /**
     * Looks through the Shrunken d values (dikStrunk) for those that DO NOT
     * have at least 1 non zero dikShrunk
     * 
     * @param dikShrunk
     * @return BitSet 0 should be removed. 1 should be used. to be removed.
     */
    private BitSet findRelevantGenes(double[][] dikShrunk, USCOrder[] order) {
        BitSet toReturn = new BitSet(dikShrunk[0].length);

        //loop through the genes
        for( int i = 0; i < dikShrunk[0].length; i++ ) {
            //loop through the classes, looking for at least 1 positive
            // absolute value
            for( int j = 0; j < dikShrunk.length; j++ ) {
                if( Math.abs(dikShrunk[j][i]) > 0.000000000000000 ) {
                    //System.out.println("Gene " + i + " is relevant");
                    toReturn.flip(i);
                    order[ i ].setRelevant(true);
                    break;
                }
            }//end j (classes)
        }//end i (genes)

        return toReturn;
    }//end removeCorrelatedGene()


    /**
     * Computes the mean ratio value of all the hybs in each gene
     * 
     * @param trainArray
     *            USCHyb[] consisting of training hybs
     * @param numGenes
     *            The number of genes in these hybs
     * @return double[ numGenes ] of mean ratio
     */
    private double[] computeGeneCentroids(USCHyb[] trainArray, int numGenes) {
        double[] toReturn = new double[numGenes];

        //loop through the genes
        for( int i = 0; i < numGenes; i++ ) {
            double ratioTotal = 0;

            //loop through the hybs
            for( int j = 0; j < trainArray.length; j++ ) {
                ratioTotal = ratioTotal + trainArray[j].getRatio(i);
            }

            toReturn[i] = ( ratioTotal / ( double ) trainArray.length );
            //System.out.println( "geneCentroid[ " + i + " ] = " + toReturn[ i
            // ] );
        }

        return toReturn;
    }//end computeOverallCentroid()


    /**
     * Computes the mean ratio value of the hybs in each class for each gene
     * 
     * @param trainArray
     *            USCHyb[] consisting of training hybs
     * @param classLabels
     *            String[] of unique class labels
     * @param numGenes
     *            Number of genes
     * @return double[ class ][ genes ] of class mean ratios
     */
    private double[][] computeClassCentroids(USCHyb[] trainArray,
            String[] classLabels, int numGenes) {
        double[][] toReturn = new double[classLabels.length][numGenes];

        //loop through the genes
        for( int i = 0; i < numGenes; i++ ) {
            //loop through the classes
            for( int j = 0; j < classLabels.length; j++ ) {
                double total = 0;
                int kount = 0;

                //loop through the hybs in the training array
                for( int k = 0; k < trainArray.length; k++ ) {
                    if( trainArray[k].getHybLabel().equalsIgnoreCase(
                            classLabels[j]) ) {
                        //found one
                        total = total + trainArray[k].getRatio(i);
                        kount++;
                    }
                }//end k(train hybs)

                toReturn[j][i] = ( total / ( double ) kount );
                //System.out.println( "classCentroid[ " + j + " ][ " + i + " ]
                // = " + toReturn[ j ][ i ] );
            }//end j(classes)
        }//end i(genes)

        return toReturn;
    }//end computeClassCentroid()


    /**
     * Computes the Relative Difference between a class a gene centroid
     * 
     * @param classCentroids
     * @param geneCentroids
     * @param mks
     * @param sis
     * @param s0
     * @return
     */
    private double[][] computeRelativeDifferences(double[][] classCentroids,
            double[] geneCentroids, double[] mks, double[] sis, double s0) {
        double[][] toReturn = new double[classCentroids.length][classCentroids[0].length];

        //loop through the classes
        for( int c = 0; c < classCentroids.length; c++ ) {

            //loop through the genes
            for( int g = 0; g < classCentroids[0].length; g++ ) {
                toReturn[c][g] = ( classCentroids[c][g] - geneCentroids[g] )
                        / ( mks[c] * ( sis[g] + s0 ) );
                //System.out.println( "[ " + i + " ][ " + j + " ]( " +
                // classCentroids[ i ][ j ] + " - "
                //+ geneCentroids[ j ] + " ) / ( " + mks[ i ] + " * ( " + sis[
                // j ] + " + " + s0 + " ) ) = " + toReturn[ i ][ j ] );
                //System.out.println( "relDiff[ " + i + " ][ " + j + " ] = " +
                // toReturn[ i ][ j ] );
            }//end j(genes)
        }//end i(classes)

        return toReturn;
    }//end computeRelativeDifferences()


    /**
     * Computes and sums the within class standard deviations of classes of a
     * gene
     * 
     * @param trainArray
     * @param classCentroids
     * @param classLabels
     * @param numGenes
     * @return
     */
    private double[] computeSis(USCHyb[] trainArray, double[][] classCentroids,
            String[] classLabels, int numGenes) {
        double firstTerm = 1.00f / ( ( double ) trainArray.length - ( double ) classLabels.length );
        //System.out.println( "firstTerm:" + firstTerm );

        double[] toReturn = new double[numGenes];

        //loop through genes
        for( int i = 0; i < numGenes; i++ ) {
            double geneSum = 0;

            //loop through the classes
            for( int j = 0; j < classLabels.length; j++ ) {
                double classDiffSquareSum = 0;

                //loop through the hybs in trainArray
                for( int k = 0; k < trainArray.length; k++ ) {
                    //deal with this class's hybs only
                    if( trainArray[k].getHybLabel().equalsIgnoreCase(
                            classLabels[j]) ) {
                        double difference = trainArray[k].getRatio(i)
                                - classCentroids[j][i];
                        double diffSquare = difference * difference;
                        classDiffSquareSum = classDiffSquareSum + diffSquare;
                    }
                }//end k(hybs in class)

                geneSum = geneSum + classDiffSquareSum;
            }//end j(classes)

            toReturn[i] = ( double ) Math.sqrt(firstTerm * geneSum);
            //System.out.println( "si[ " + i + " ] = " + toReturn[ i ] );
        }//end i(genes)

        return toReturn;
    }//end computeSis()


    /**
     * Computes Mk
     * 
     * @param trainArray
     * @param classLabels
     * @return
     */
    private double[] computeMks(USCHyb[] trainArray, String[] classLabels) {
        double[] toReturn = new double[classLabels.length];

        //loop through the classes
        for( int i = 0; i < classLabels.length; i++ ) {
            int kount = 0;

            //find out how many hybs in trainArray are in class[ i ]
            for( int j = 0; j < trainArray.length; j++ ) {
                if( trainArray[j].getHybLabel()
                        .equalsIgnoreCase(classLabels[i]) ) {
                    kount++;
                }
            }//end j

            double firstTerm = 1.00f / ( double ) kount;
            double secondTerm = 1.00f / ( double ) trainArray.length;
            //System.out.println( "kount = " + kount + "\ttrainArray.length = "
            // + trainArray.length );

            toReturn[i] = ( double ) Math.sqrt(firstTerm + secondTerm);
            //System.out.println( "mk[ " + i + " ] = " + toReturn[ i ] );
        }//end i

        return toReturn;
    }//end computeMks


    /**
     * Computes the average value of the doubles in array
     * 
     * @param array
     * @return
     */
    private double computeMean(double[] array) {
        double toReturn = 0;

        for( int i = 0; i < array.length; i++ ) {
            toReturn = toReturn + array[i];
        }

        return toReturn / ( double ) array.length;
    }//end computeMean()


    /**
     * Finds or computes the median value in array
     * 
     * @param array
     * @return
     */
    private double computeMedian(double[] array) {
        //create a new array so we don't mess with the order of the original
        double[] copy = new double[array.length];
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


    /**
     * Does soft thresholding on Dik values by subtracting delta from the
     * absolute value of Dik and then reattaching the sign or replacing with 0
     * if the subtraction is negative
     * 
     * @param delta
     * @param diks
     * @return
     */
    private double[][] shrinkDiks(double delta, double[][] diks) {
        double[][] toReturn = new double[diks.length][diks[0].length];

        //classes
        for( int i = 0; i < diks.length; i++ ) {
            //genes
            for( int j = 0; j < diks[0].length; j++ ) {
                toReturn[i][j] = this.shrinkDik(delta, diks[i][j]);
            }
        }

        return toReturn;
    }//end shrinkDiks()


    /**
     * Shrink dik
     * 
     * @param delta
     * @param dik
     * @return
     */
    private double shrinkDik(double delta, double dik) {
        double toReturn = 0;

        if( dik < 0 ) {
            toReturn = -dik - delta;
        } else {
            toReturn = dik - delta;
        }

        if( toReturn < 0 ) {
            toReturn = 0;
        } else if( dik < 0 ) {
            toReturn = -toReturn;
        }

        return toReturn;
    }//end shrinkCentroid()


    /**
     * Wierd and stupid. Knee deep in my own shit.
     * 
     * @param trainArray
     * @param sClassLabel
     * @return
     */
    public int[] findHybIndicesForClass(USCHyb[] trainArray, int classIndex,
            USCHybSet hybSet) {
        Vector vHybInClass = new Vector();

        for( int i = 0; i < trainArray.length; i++ ) {
            USCHyb hyb = trainArray[i];
            if( hyb.getHybLabel().equals(hybSet.getUniqueClass(classIndex)) ) {
                vHybInClass.add(new Integer(i));
            }
        }

        int[] toReturn = new int[vHybInClass.size()];
        for( int i = 0; i < toReturn.length; i++ ) {
            Integer I = ( Integer ) vHybInClass.elementAt(i);
            toReturn[i] = I.intValue();
        }

        return toReturn;
    }//end findHybIndicesForClass()


    /**
     * Kounts the # of hybs that belong to the a class
     * 
     * @param hybs
     * @param label
     * @return
     */
    private int getNumClassHybsInUSCHybArray(USCHyb[] hybs, String label) {
        int kount = 0;

        for( int h = 0; h < hybs.length; h++ ) {
            USCHyb hyb = hybs[h];

            if( hyb.getHybLabel().equals(label) ) {
                kount++;
            }
        }

        return kount;
    }//end getNumClassHybsInUSCHybArray()


    /**
     * Creates and returns an array of all the USCHybs that belong to a class
     * 
     * @param hybs
     * @param label
     * @return
     */
    private USCHyb[] getClassHybsInUSCHybArray(USCHyb[] hybs, String label) {
        USCHyb[] toReturn = new USCHyb[this.getNumClassHybsInUSCHybArray(hybs,
                label)];
        int kount = 0;

        for( int h = 0; h < toReturn.length; h++ ) {
            USCHyb hyb = hybs[h];

            if( hyb.getHybLabel().equals(label) ) {
                toReturn[kount] = hyb;
                kount++;
            }
        }

        return toReturn;
    }//end getClassHybsInUSCHybArray()


    /**
     * Computes log base 10 of x
     * 
     * @param x
     * @return
     */
    private double computeCommonLog(double x) {
        double toReturn = 0.0f;
        toReturn = ( double ) Math.log(x) / ( double ) Math.log(10);
        return toReturn;
    }//computeCommonLog()
}//end class

/*
 * //loop through the testHybs assignments [ numTestHybs ] USCClassAssignment[]
 * assigns = this.trainTrain( trainArray, testArray, delta, rho,
 * fullSet.getNumGenes(), fullSet.getNumClasses(), fullSet.getUniqueClasses() );
 * 
 * 
 * //make sure there was at least 1 nonzero discriminant score if( assigns ==
 * null ) { //do nothing System.out.println( "Assigns null for delta = " + delta +
 * "\trho = " + rho ); } else { //during cross validation, we need to know the
 * real hyb class for( int h = 0; h < assigns.length; h ++ ) { String hybLabel =
 * testArray[ h ].getHybLabel(); int iLabel; for( int u = 0; u <
 * fullSet.getNumClasses(); u ++ ) { if( hybLabel.equals(
 * fullSet.getUniqueClass( u ) ) ) { iLabel = u; assigns[ h
 * ].setUniqueClassIndex( u ); } }//end u (classes) }//end h (hybs) //sb.append(
 * f + 1 + "\t" + d + "\t" + delta + "\t" + rho);
 * 
 * //keep track of how many genes were used to make this call USCOrder[] order =
 * assigns[ 0 ].getOrder(); int iUsed = 0; for( int o = 0; o < order.length; o ++ ) {
 * if( order[ o ].isRelevant() && ! order[ o ].isCorrelated() ) { iUsed ++; } }
 * //sb.append( "\t" + iUsed );
 * 
 * //loop through assignments to create file for( int a = 0; a < assigns.length;
 * a ++ ) { USCHyb hyb = testArray[ a ];
 * 
 * //sb.append( "\t" + hyb.getHybName() ); //sb.append( "\t" + assigns[ a
 * ].getUniqueClassIndex() ); //sb.append( "\t" + assigns[ a
 * ].getAssignedClassIndex() ); //sb.append( "\t" + hyb.getHybLabel() );
 * //sb.append( "\t" + fullSet.getUniqueClass( assigns[ a
 * ].getAssignedClassIndex() ) );
 * 
 * for( int q = 0; q < assigns[ a ].getDiscriminantScoreArray().length; q ++ ) {
 * //sb.append( "\t" + assigns[ a ].getDiscriminantScore( q ) ); }
 * 
 * //System.out.println( testArray[ a ].getHybName() + " assigned to " +
 * //assigns[ a ].getAssignedClassIndex() ); }//end a (assigns)
 * 
 * //sb.append( "\r\n" ); }//end else
 */
/*
 * for( int i = 0; i < testArray.length; i ++ ) { System.out.println( "test\t" +
 * testArray[ i ].getHybName() ); }
 * 
 * //how many genes, how many classes int numGenes = this.fullSet.getNumGenes();
 * int numClasses = this.fullSet.getNumClasses();
 * 
 * //need to keep track of gene sort order USCOrder[] orderArray = new USCOrder[
 * numGenes ]; for( int g = 0; g < numGenes; g ++ ) { orderArray[ g ] = new
 * USCOrder( g ); }
 * 
 * //each gene has a centroid (mean) double[] geneCentroids =
 * this.computeGeneCentroids( trainArray, numGenes );
 * 
 * //each class in each gene has a centroid (class mean) [ classes ][ genes ]
 * double[][] classCentroids = this.computeClassCentroids( trainArray,
 * this.fullSet.getUniqueClasses(), numGenes );
 * 
 * //each class has an mk value [ numClasses ] double[] mks = this.computeMks(
 * trainArray, this.fullSet.getUniqueClasses() );
 * 
 * //each gene has an si value [ numGenes ] double[] sis = this.computeSis(
 * trainArray, classCentroids, this.fullSet.getUniqueClasses(), numGenes );
 * 
 * //there is 1 s0 double s0 = this.computeMedian( sis );
 * 
 * //dik is the relative difference of each classCentroid from the geneCentroid
 * (standardized) [ class ][ gene ] double[][] dik =
 * this.computeRelativeDifferences( classCentroids, geneCentroids, mks, sis, s0 );
 * 
 * //now we have all we need to start
 * testing---------------------------------------- double delta = 0.0; for( int
 * d = 0; d < this.numDeltas; d ++ ) { //for( int d = 0; d < 10; d ++ ) { delta =
 * delta + this.deltaStep; //System.out.println( "delta = " + delta );
 * 
 * //shrink the centroids by delta double[][] dikShrunk = this.shrinkDiks(
 * delta, dik );
 * 
 * //figure out which ones to keep using and store their indices in a BitSet
 * //Beta is the greatest positive dikShrunk value BitSet bsUse =
 * this.findRelavantGenes( dikShrunk ); int kount = 0; for( int g = 0; g <
 * numGenes; g ++ ) { if( bsUse.get( g ) == true ) { kount ++; } }
 * 
 * //sort the genes based on greatest Beta value USCOrder[] significantOrder =
 * new USCOrder[ kount ]; int sigKount = 0; for( int g = 0; g < numGenes; g ++ ) {
 * 
 * orderArray[ g ].setSignificant( bsUse.get( g ) );
 * 
 * double maxDik = 0; for( int c = 0; c < numClasses; c ++ ) { if( dikShrunk[ c ][
 * g ] > maxDik ) { maxDik = dikShrunk[ c ][ g ]; } }//end c (classes)
 * 
 * orderArray[ g ].setBeta( maxDik ); //System.out.println( "maxDik = " + maxDik );
 * 
 * if( bsUse.get( g ) == true ) { significantOrder[ sigKount ] = orderArray[ g ];
 * //System.out.println( significantOrder[ sigKount ].getBeta() ); sigKount ++; }
 * }//end g (genes)
 * 
 * //compute shrunken class centroids (do for all, deal with removed genes
 * later) double[][] shrunkenCentroids = this.computeShrunkenClassCentroid(
 * geneCentroids, mks, sis, s0, dikShrunk );
 * 
 * //sort based on Beta, and set the iSorted variable with new index
 * Arrays.sort( significantOrder, new USCOrderComparator() ); for( int g = 0; g <
 * significantOrder.length; g ++ ) { significantOrder[ g ].setISorted( g ); }
 * 
 * //try uncorrelation for rho = .5 - 1.0 for( double r = 0.5; r <= 1.0; r = r +
 * .1 ) { //System.out.println( "delta = " + delta + "\trho = " + r ); //do
 * pairwise comparison to find correlated Genes for( int g = 0; g < (
 * significantOrder.length - 1 ); g ++ ) { double correlation =
 * this.computeCorrelation( trainArray, geneCentroids, significantOrder[ g
 * ].getIOriginal(), significantOrder[ g + 1 ].getIOriginal() ); if( correlation <
 * r ) { significantOrder[ g ].setCorrelated( true ); } //System.out.println(
 * "correlation ( " + g + ", " + ( g + 1 ) + " ) = " + correlation ); }//end g
 * (genes)
 * 
 * //compute discriminant score double[][] discScores =
 * this.computeDiscriminantScore( testArray, shrunkenCentroids,
 * significantOrder, sis, s0, this.fullSet.getUniqueClasses() );
 * 
 * //assign test hybs to classes for( int h = 0; h < testArray.length; h ++ ) {
 * double min = 999999999; int iAssign = 0;
 * 
 * for( int c = 0; c < numClasses; c ++ ) { if( discScores[ c ][ h ] < min ) {
 * min = discScores[ c ][ h ]; iAssign = c; } }
 * 
 * System.out.println( testArray[ h ].getHybName() + " is in class " + iAssign +
 * "\twhen delta = " + delta + "\trho = " + r ); } }//end r (rho) }//end d
 * (deltas)
 */

/*
 * USCHyb[] testArray; USCHyb[] trainArray;
 * 
 * //separate the loaded file into a training set and a test set if( i == (
 * this.numFold - 1 ) ) { //last fold, test remaining hybs } else { //test
 * hybPerFold hybs }//
 * 
 * if( i == ( this.numFold - 1 ) ) { //last run, do remaining test hybs
 * testArray = new USCHyb[ hybPerFold + hybRemainder ]; for( int j = 0; j <
 * testArray.length; j ++ ) { testArray[ j ] = this.fullSet.getHyb(
 * permutation.getIndex( ( i * hybPerFold ) + j ) ); System.out.println(
 * testArray[ j ].getHybName() + " added to testArray" ); } //get the training
 * hybs for this fold trainArray = this.getOtherHybs( this.fullSet, testArray ); }
 * else { //get the test hybs for this fold testArray = new USCHyb[ hybPerFold ];
 * for( int j = 0; j < testArray.length; j ++ ) { testArray[ j ] =
 * this.fullSet.getHyb( permutation.getIndex( ( i * hybPerFold ) + j ) );
 * System.out.println( testArray[ j ].getHybName() + " added to testArray" ); }
 * //get the training hybs for this fold trainArray = this.getOtherHybs(
 * this.fullSet, testArray ); }//end else
 * 
 * USCHybSet testSet = new USCHybSet( testArray, USCHybSet.SUB_TEST ); USCHybSet
 * trainSet = new USCHybSet( trainArray, USCHybSet.SUB_TRAINING );
 */
/*
 * Randomnly permutes a set of Hybs, returning 'fold' number of new USCHybSets
 * <br> USCHybSet[ i ][ j ] is a 2 D array where i = Fold, j = 2. For each fold,
 * 2 subsets of the USCHybSet are created. The first (j=0) is the Subset of hybs
 * to be used to train the algorithm. The 2nd (j=1) is the Subset of hybs to be
 * tested through Cross Validation. @param hybs The complete hyb set read from
 * the training file @param fold The # of times to run Cross Validation @return
 * USCHybSet[ fold ][ 2 ] where j = 0 element is Training Subset and the j = 1
 * element is the Test Subset
 */
/*
 * private USCHybSet[][] permuteHybs( USCHybSet hybs, int fold ) { Vector
 * vTested = new Vector();
 * 
 * USCHybSet[][] toReturn = new USCHybSet[ fold ][ 2 ];
 * 
 * int hybPerFold = hybs.getHybKount() / fold; int hybRemainder =
 * hybs.getHybKount() % fold;
 * 
 * for( int i = 0; i < fold; i ++ ) { System.out.println( "Fold:" + i );
 * 
 * USCHyb[] trainArray = null; USCHyb[] testArray = null; int trainIndex = 0;
 * int testIndex = 0;
 * 
 * //the last test set may need to have extra hybs so that all hybs are tested
 * if( i == ( fold - 1 ) ) { //IMPORTANT testSetIndices are 1-based!!! int[]
 * testSetIndices = this.getTestSetIndices( vTested, ( hybPerFold + hybRemainder ),
 * hybs.getHybKount() );
 * 
 * testArray = new USCHyb[ ( hybPerFold + hybRemainder ) ]; trainArray = new
 * USCHyb[ hybs.getHybKount() - ( hybPerFold + hybRemainder ) ];
 * 
 * for( int j = 0; j < testSetIndices.length; j ++ ) { testArray[ j ] =
 * hybs.getHyb( testSetIndices[ j ] - 1 ); vTested.add( new Integer(
 * testSetIndices[ j ] - 1 ) ); //System.out.println( hybs.getHyb(
 * testSetIndices[ j ] ).getHybName() + " added to testArray" ); }//end i
 * 
 * int iTrain = 0; for( int j = 0; j < hybs.getHybKount(); j ++ ) { boolean
 * isTrain = true;
 * 
 * for( int k = 0; k < testSetIndices.length; k ++ ) { if( j == (
 * testSetIndices[ k ] - 1 ) ) { isTrain = false; break; } }//end k
 * 
 * if( isTrain ) { trainArray[ iTrain ] = hybs.getHyb( j );
 * //System.out.println( hybs.getHyb( j ).getHybName() + " added to trainArray" );
 * iTrain ++; } }//end j } else { //IMPORTANT testSetIndices are 1-based!!!
 * int[] testSetIndices = this.getTestSetIndices( vTested, hybPerFold,
 * hybs.getHybKount() );
 * 
 * testArray = new USCHyb[ hybPerFold ]; trainArray = new USCHyb[
 * hybs.getHybKount() - hybPerFold ];
 * 
 * for( int j = 0; j < testSetIndices.length; j ++ ) { testArray[ j ] =
 * hybs.getHyb( testSetIndices[ j ] - 1 ); vTested.add( new Integer(
 * testSetIndices[ j ] - 1 ) ); //System.out.println( hybs.getHyb(
 * testSetIndices[ j ] ).getHybName() + " added to testArray" ); }//end i
 * 
 * int iTrain = 0;
 * 
 * for( int j = 0; j < hybs.getHybKount(); j ++ ) { boolean isTrain = true;
 * 
 * for( int k = 0; k < testSetIndices.length; k ++ ) { if( j == (
 * testSetIndices[ k ] - 1 ) ) { isTrain = false; break; } }//end k
 * 
 * if( isTrain ) { trainArray[ iTrain ] = hybs.getHyb( j );
 * //System.out.println( hybs.getHyb( j ).getHybName() + " added to trainArray" );
 * iTrain ++; } }//end j
 *  }
 * 
 * toReturn[ i ][ 0 ] = new USCHybSet( testArray, USCHybSet.SUB_TEST );
 * toReturn[ i ][ 1 ] = new USCHybSet( trainArray, USCHybSet.SUB_TRAINING );
 * }//end i
 * 
 * return toReturn; }//end permuteHybs() private int[] getTestSetIndices( Vector
 * vTested, int testKount, int total ) { int[] toReturn = new int[ testKount ];
 * System.out.println( toReturn.length ); Random r = new Random(); int iAdded =
 * 0; int iTry = 0;
 * 
 * while( iAdded < testKount ) { iTry = r.nextInt( total ) + 1;
 * 
 * if( ! this.alreadyExist( iTry, vTested ) ) { if( ! this.alreadyAdded( iTry,
 * toReturn ) ) { //System.out.println( iTry + " wasn't added" );
 * 
 * toReturn[ iAdded ] = iTry; iAdded ++; } } }
 * 
 * return toReturn; }//end getTestSetIndices() private boolean alreadyExist( int
 * iTest, Vector v ) { boolean toReturn = false;
 * 
 * for( int i = 0; i < v.size(); i ++ ) { Integer I = ( Integer ) v.elementAt( i );
 * if( iTest == ( I.intValue() + 1 ) ) { toReturn = true; break; } }//end i
 * 
 * return toReturn; } private boolean alreadyAdded( int iTest, int[] test ) {
 * boolean toReturn = false;
 * 
 * for( int i = 0; i < test.length; i ++ ) { if( iTest == test[ i ] ) {
 * System.out.println( iTest + " = " + test[ i ] ); toReturn = true; break; } }
 * 
 * return toReturn; }
 */

/*
 * private Vector generateRandomPermutations( int kount, int iSeed, Vector
 * vTested ) { //first generate a Vector of random numbers of kount size Vector
 * vReturn = new Vector();
 * 
 * Long L = new Long( iSeed ); Random r = new Random( L.longValue() );
 * 
 * for( int i = 0; i < kount; i ++ ) { boolean isAdded = false;
 * 
 * while( ! isAdded ) { Integer IAdd = new Integer( r.nextInt( kount ) );
 * 
 * if( isNovel( IAdd.intValue(), vReturn ) ) { vReturn.add( IAdd ); isAdded =
 * true; //System.out.println( IAdd + " added" ); } } }//end i
 * 
 * //System.out.println( vReturn.size() + " hybs randomnly permuted\r\n" );
 * 
 * return vReturn; }//end generateRandomPermutations() private boolean isNovel(
 * int testInt, Vector vInt ) { boolean toReturn = true;
 * 
 * for( int i = 0; i < vInt.size(); i ++ ) { Integer I = ( Integer )
 * vInt.elementAt( i ); if( I.intValue() == testInt ) { toReturn = false; break; } }
 * 
 * return toReturn; }//end isNovel()
 */

/*
 * int[] testSetIndices = this.getTestSetIndices( vTested, hybPerFold,
 * hybs.getHybKount() );
 * 
 * testArray = new USCHyb[ hybPerFold + hybRemainder ]; trainArray = new USCHyb[
 * hybs.getHybKount() - ( hybPerFold + hybRemainder ) ];
 * 
 * //last time, use the rest of the hybs for( int j = 0; j < vPermuted.size(); j ++ ) {
 * Integer I = ( Integer ) vPermuted.elementAt( j ); int iHyb = I.intValue();
 * 
 * //the first hybPerFold hybs will be used as test if( j < hybPerFold +
 * hybRemainder ) { //add to testSet testArray[ testIndex ] = hybs.getHyb( iHyb );
 * vTested.add( I ); //System.out.println( testArray[ testIndex ].getHybName() + "
 * added to testArray[ " + testIndex + " ]" ); testIndex ++; } else { //add to
 * trainSet trainArray[ trainIndex ] = hybs.getHyb( iHyb );
 * //System.out.println( trainArray[ trainIndex ].getHybName() + " added to
 * trainArray[ " + trainIndex + " ]" ); trainIndex ++; } }//end j
 */
/*
 * for( int j = 0; j < vPermuted.size(); j ++ ) { Integer I = ( Integer )
 * vPermuted.elementAt( j ); int iHyb = I.intValue();
 * 
 * if( j < hybPerFold ) { //add to testSet testArray[ testIndex ] = hybs.getHyb(
 * iHyb ); vTested.add( I ); //System.out.println( testArray[ testIndex
 * ].getHybName() + " added to testArray[ " + testIndex + " ]" ); testIndex ++; }
 * else { //add to trainSet trainArray[ trainIndex ] = hybs.getHyb( iHyb );
 * //System.out.println( trainArray[ trainIndex ].getHybName() + " added to
 * trainArray[ " + trainIndex + " ]" ); trainIndex ++; } }//end j
 */

//--------------------

/*
 * //significantOrder is a subset of relevantOrder, having tossed out irrelevant
 * genes USCOrder[] significantOrder = new USCOrder[ numRelevant ]; int
 * numSignificant = 0; for( int g = 0; g < numGenes; g ++ ) { double maxDik = 0;
 * for( int c = 0; c < numClasses; c ++ ) { if( dikShrunk[ c ][ g ] > maxDik ) {
 * maxDik = dikShrunk[ c ][ g ]; } }//end c (classes)
 * 
 * relevantOrder[ g ].setBeta( maxDik ); //System.out.println( "maxDik = " +
 * maxDik );
 * 
 * if( bsUse.get( g ) == true ) { significantOrder[ numSignificant ] =
 * relevantOrder[ g ]; //System.out.println( significantOrder[ sigKount
 * ].getBeta() ); numSignificant ++; } }//end g (genes)
 * 
 * //sort based on Beta, and set the iSorted variable with new index
 * Arrays.sort( significantOrder, new USCRelevanceComparator() ); for( int g =
 * 0; g < significantOrder.length; g ++ ) { significantOrder[ g ].setIRelevant(
 * g ); }
 * 
 * //do pairwise comparison to find correlated Genes //be careful here to make
 * sure you only compare relevant genes for( int g = 0; g < (
 * significantOrder.length - 1 ); g ++ ) { double correlation =
 * this.computeCorrelation( trainArray, significantOrder[ g ].getIOriginal(),
 * significantOrder[ g + 1 ].getIOriginal() ); if( correlation < rho ) {
 * significantOrder[ g ].setCorrelated( true ); } //System.out.println(
 * "correlation ( " + g + ", " + ( g + 1 ) + " ) = " + correlation ); }//end g
 * (genes)
 * 
 * //compute discriminant score double[][] discScores =
 * this.computeDiscriminantScores( testArray, shrunkenCentroids,
 * significantOrder, sis, s0, this.fullSet.getUniqueClasses() );
 * 
 * //assign test hybs to classes and store in USCClassAssignment object for( int
 * h = 0; h < testArray.length; h ++ ) { double min = 999999999; int iAssign =
 * 0;
 * 
 * double[] classDiscScoreArray = new double[ numClasses ]; for( int c = 0; c <
 * numClasses; c ++ ) { classDiscScoreArray[ c ] = discScores[ c ][ h ];
 * 
 * if( discScores[ c ][ h ] < min ) { min = discScores[ c ][ h ]; iAssign = c; } }
 * 
 * USCClassAssignment assignment = new USCClassAssignment( min, iAssign,
 * classDiscScoreArray ); assignments[ h ] = assignment;
 * 
 * //System.out.println( testArray[ h ].getHybName() + " is in class " + iAssign );
 * }//end h (testHybs) private int computeCombinations( int n, int c ) { int
 * toReturn = 0;
 * 
 * int nFactorial = n; for( int i = n - 1; i > 0; i -- ) { nFactorial =
 * nFactorial * i; }
 * 
 * int cFactorial = c; for( int i = ( c - 1 ); i > 0; i -- ) { cFactorial =
 * cFactorial * i; }
 * 
 * int diff = ( n - c ); int diffFactorial = diff; for( int i = ( diff - 1 ); i >
 * 0; i -- ) { diffFactorial = diffFactorial * i; }
 * 
 * toReturn = ( nFactorial / ( cFactorial * diffFactorial ) );
 * System.out.println( toReturn + " combinations of " + n );
 * 
 * return toReturn; }//computeCombinations
 */

/*
public USCXValResult crossValidate(USCHybSet fullSet, Frame frame) {
    frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

    USCXValResult toReturn = new USCXValResult(this.xValKount);

    //display a progress bar
    JPanel mainPanel = new JPanel();
    mainPanel.setLayout(new SpringLayout());
    JPanel leftPanel = new JPanel();
    leftPanel.add(new JLabel("     "));
    JPanel rightPanel = new JPanel();
    rightPanel.add(new JLabel("     "));

    JPanel midPanel = new JPanel();
    BoxLayout midBox = new BoxLayout(midPanel, BoxLayout.Y_AXIS);
    midPanel.setLayout(midBox);
    JLabel label = new JLabel("Cross Validating... Please Wait");
    JLabel label2 = new JLabel("This will take a few minutes");
    JLabel foldLabel = new JLabel("Fold/CrossVal runs");
    JLabel deltaLabel = new JLabel("Deltas");
    JLabel corrLabel = new JLabel("Pairwise Genes");
    JLabel blankLabel = new JLabel(" ");
    midPanel.add(label);
    midPanel.add(label2);
    midPanel.add(blankLabel);
    JProgressBar foldBar = new JProgressBar(0, ( this.foldKount * this.xValKount ));
    foldBar.setIndeterminate(false);
    foldBar.setStringPainted(true);
    JProgressBar deltaBar = new JProgressBar(0, this.deltaKount);
    deltaBar.setIndeterminate(false);
    deltaBar.setStringPainted(true);
    JProgressBar corrBar = new JProgressBar(0, fullSet.getNumGenes());
    corrBar.setIndeterminate(false);
    corrBar.setStringPainted(true);
    midPanel.add(foldLabel);
    midPanel.add(foldBar);
    midPanel.add(deltaLabel);
    midPanel.add(deltaBar);
    midPanel.add(corrLabel);
    midPanel.add(corrBar);

    mainPanel.add(leftPanel);
    mainPanel.add(midPanel);
    mainPanel.add(rightPanel);
    SpringUtilities.makeCompactGrid(mainPanel, 1, 3, 0, 0, 0, 0);
    JFrame jf = new JFrame("Cross Validating");
    jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    jf.getContentPane().add(mainPanel);
    jf.setSize(250, 200);
    jf.show();
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    jf.setLocation(( screenSize.width - 200 ) / 2,
            ( screenSize.height - 100 ) / 2);
    int iProgress = 0;

    //do multiple cross validations (added 1.30.05)
    for( int m = 0; m < this.xValKount; m++ ) {
        USCFoldResult[] resultArray = new USCFoldResult[this.foldKount];

        //loop through the folds, during each fold, a different set of Hybs
        // are left out
        for( int f = 0; f < this.foldKount; f++ ) {
            deltaBar.setValue(0);

            //System.out.println(
            // "\r\n\r\n\r\n------------------------------------------------"
            // +
            //"------------------------------------------------------------------------FOLD:"
            // + f );

            //create a USCFoldResult that will hold the results from this
            // fold
            int resultKount = ( this.deltaKount * 6 );
            USCFoldResult foldResults = new USCFoldResult(resultKount);
            int iResult = 0;

            //arrays of USCHyb objects
            USCHyb[] subTestArray = fullSet.getTestArray(f);
            USCHyb[] subTrainArray = fullSet.getTrainArray(f);

            //loop through the deltas
            double delta = 0.0f;
            for( int d = 0; d < this.deltaKount; d++ ) {
                //loop through the rhos
                double rho;
                for( int r = 5; r < 11; r++ ) {
                    rho = ( double ) r * 0.1f;

                    USCResult result = this.testTest(subTrainArray,
                            subTestArray, delta, rho, fullSet.getNumGenes(),
                            fullSet.getNumClasses(), fullSet
                                    .getUniqueClasses(), corrBar, r);
                   
                    if( result == null ) {
                        //do nothing
                    } else {
                        //store it
                        foldResults.setResult(result, iResult);
                        foldResults.setTestArray(subTestArray);
                        foldResults.setUniqueClassArray(fullSet
                                .getUniqueClasses());
                        iResult++;
                    }
                }//end r (rho)

                delta += this.deltaStep;
                deltaBar.setValue(d + 1);
            }//end d (deltas)

            //toReturn[ f ] = foldResults;
            resultArray[f] = foldResults;

            //update progress bar
            foldBar.setIndeterminate(false);
            iProgress++;
            foldBar.setValue(iProgress);
            foldBar.setStringPainted(true);
        }//end f (folds)

        toReturn.setFoldResult(resultArray, m);
    }//end m (xValKount)

    //System.out.println( "Done With Cross Validation" );

    jf.dispose();
    frame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

    return toReturn;
}//end crossValidate
*/