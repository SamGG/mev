/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: Adjustment.java,v $
 * $Revision: 1.1.1.1 $
 * $Date: 2003-08-21 21:04:23 $
 * $Author: braisted $
 * $State: Exp $
 */
package org.tigr.microarray.util;

import java.util.Arrays;

import org.tigr.util.FloatMatrix;

public class Adjustment {
    
    public static void log2Transform(FloatMatrix matrix) {
	float value;
	for (int i=0; i<matrix.getRowDimension(); i++) {
	    for (int j=0; j<matrix.getColumnDimension(); j++) {
		value=matrix.get(i,j);
		if (!Float.isNaN(value)) {
		    if (value>0) {
			matrix.set(i,j,(float)(Math.log(value)/0.69314718));
		    } else {
			matrix.set(i,j,Float.NaN);
		    }
		}
	    }
	}
    }
    
    public static void normalizeSpots(FloatMatrix matrix) {
	for (int i=0; i<matrix.getRowDimension(); i++) {
	    normalizeGene(matrix, i);
	}
    }
    
    public static void divideSpotsRMS(FloatMatrix matrix) {
	for (int i=0; i<matrix.getRowDimension(); i++) {
	    divideGeneByRMS(matrix, i);
	}
    }
    
    public static void divideSpotsSD(FloatMatrix matrix) {
	for (int i=0; i<matrix.getRowDimension(); i++) {
	    divideGeneBySD(matrix, i);
	}
    }
    
    public static void meanCenterSpots(FloatMatrix matrix) {
	for (int i=0; i<matrix.getRowDimension(); i++) {
	    meanCenterGene(matrix, i);
	}
    }
    
    public static void medianCenterSpots(FloatMatrix matrix) {
	for (int i=0; i<matrix.getRowDimension(); i++) {
	    medianCenterGene(matrix, i);
	}
    }
    
    public static void digitalSpots(FloatMatrix matrix) {
	for (int i=0; i<matrix.getRowDimension(); i++) {
	    makeDigitalGene(matrix, i);
	}
    }
    
    public static void normalizeExperiments(FloatMatrix matrix) {
	for (int i=0; i<matrix.getColumnDimension(); i++) {
	    normalizeExperiment(matrix, i);
	}
    }
    
    public static void divideExperimentsRMS(FloatMatrix matrix) {
	for (int i=0; i<matrix.getColumnDimension(); i++) {
	    divideExperimentByRMS(matrix, i);
	}
    }
    
    public static void divideExperimentsSD(FloatMatrix matrix) {
	for (int i=0; i<matrix.getColumnDimension(); i++) {
	    divideExperimentBySD(matrix, i);
	}
    }
    
    public static void meanCenterExperiments(FloatMatrix matrix) {
	for (int i=0; i<matrix.getColumnDimension(); i++) {
	    meanCenterExperiment(matrix, i);
	}
    }
    
    public static void medianCenterExperiments(FloatMatrix matrix) {
	for (int i=0; i<matrix.getColumnDimension(); i++) {
	    medianCenterExperiment(matrix, i);
	}
    }
    
    public static void digitalExperiments(FloatMatrix matrix) {
	for (int i=0; i<matrix.getColumnDimension(); i++) {
	    makeDigitalExperiment(matrix, i);
	}
    }
    
    public static void log10toLog2(FloatMatrix matrix) {
	float value;
	for (int i=0; i<matrix.getRowDimension(); i++) {
	    for (int j=0; j<matrix.getColumnDimension(); j++) {
		value=matrix.get(i,j);
		if (!Float.isNaN(value)) matrix.set(i,j,(float)(value/0.301029995));
	    }
	}
    }
    
    private static void normalizeGene(FloatMatrix matrix, int GeneNumber) {
	double Mean=0.0;
	double StandardDeviation=0.0;
	int i;
	int n=matrix.getColumnDimension();
	float value;
	for (i=0; i<n; i++) {
	    value=matrix.get(GeneNumber,i);
	    if (!Float.isNaN(value)) Mean += value;
	}
	Mean /= (double)n;
	for (i=0; i<n; i++) {
	    value=matrix.get(GeneNumber,i);
	    if (!Float.isNaN(value)) StandardDeviation+=Math.pow((value-Mean),2);
	}
	StandardDeviation=Math.sqrt(StandardDeviation/(double)(n-1));
	for (i=0; i<n; i++) {
	    value=matrix.get(GeneNumber,i);
	    if (!Float.isNaN(value)) {
		if (StandardDeviation!=0) {
		    matrix.set(GeneNumber,i,(float)((value-Mean)/StandardDeviation));
		} else {
		    matrix.set(GeneNumber,i,(float)((value-Mean)/Float.MIN_VALUE));
		}
	    }
	}
    }
    
    
    private static void divideGeneByRMS(FloatMatrix matrix, int GeneNumber) {
	double Mean=0.0;
	double RMS=0.0;
	float value=0.0f;
	int i;
	int n=matrix.getColumnDimension();
	for (i=0; i<n; i++) {
	    value=matrix.get(GeneNumber,i);
	    if (!Float.isNaN(value)) RMS+=Math.pow((value),2);
	}
	RMS=Math.sqrt(RMS/(double)(n-1));
	for (i=0; i<n; i++) {
	    value=matrix.get(GeneNumber,i);
	    if (!Float.isNaN(value)) {
		if (RMS!=0) {
		    matrix.set(GeneNumber,i,(float)((value)/RMS));
		} else {
		    matrix.set(GeneNumber,i,(float)((value)/Float.MIN_VALUE));
		}
	    }
	}
    }
    
    private static void divideGeneBySD(FloatMatrix matrix, int GeneNumber) {
	double Mean=0.0;
	double StandardDeviation=0.0;
	int i;
	int n=matrix.getColumnDimension();
	float value;
	for (i=0; i<n; i++) {
	    value=matrix.get(GeneNumber,i);
	    if (!Float.isNaN(value)) Mean += value;
	}
	Mean /= (double)n;
	for (i=0; i<n; i++) {
	    value=matrix.get(GeneNumber,i);
	    if (!Float.isNaN(value)) StandardDeviation+=Math.pow((value-Mean),2);
	}
	StandardDeviation=Math.sqrt(StandardDeviation/(double)(n-1));
	for (i=0; i<n; i++) {
	    value=matrix.get(GeneNumber,i);
	    if (!Float.isNaN(value)) {
		if (StandardDeviation!=0) {
		    matrix.set(GeneNumber,i,(float)((value)/StandardDeviation));
		} else {
		    matrix.set(GeneNumber,i,(float)((value)/Float.MIN_VALUE));
		}
	    }
	}
    }
    
    public static void meanCenterGene(FloatMatrix matrix, int GeneNumber) {
	double Mean=0.0;
	int i;
	int n=matrix.getColumnDimension();
	float value;
	for (i=0; i<n; i++) {
	    value=matrix.get(GeneNumber,i);
	    if (!Float.isNaN(value)) Mean += value;
	}
	Mean /= (double)n;
	for (i=0; i<n; i++) {
	    value=matrix.get(GeneNumber,i);
	    if (!Float.isNaN(value)) {
		matrix.set(GeneNumber,i,(float)(value-Mean));
	    }
	}
    }
    
    private static void medianCenterGene(FloatMatrix matrix, int GeneNumber) {
	int i;
	int n=matrix.getColumnDimension();
	float value;
	int k=0;
	for (i=0; i<n; i++) {
	    value=matrix.get(GeneNumber,i);
	    if (!Float.isNaN(value)) k++;
	}
	float[] DummyArray=new float [k];
	k=0;
	for (i=0; i<n; i++) {
	    value=matrix.get(GeneNumber,i);
	    if (!Float.isNaN(value)) {
		DummyArray[k]=value;
		k++;
	    }
	}
	Arrays.sort(DummyArray);
	float Median=0.0f;
	if (k%2==0) {
	    if (k>0) Median=(float)(0.5*(DummyArray[k/2-1]+DummyArray[(k/2)]));
	} else {
	    Median=DummyArray[(k+1)/2-1];
	}
	for (i=0; i<n; i++) {
	    value=matrix.get(GeneNumber,i);
	    if (!Float.isNaN(value)) {
		matrix.set(GeneNumber,i,(float)(value-Median));
	    }
	}
    }
    
    private static void makeDigitalGene(FloatMatrix matrix, int Gene) {
	int n=matrix.getColumnDimension();
	int NumberOfBins=(int)Math.floor(Math.log(n)/Math.log(2));
	int Step=1000000/NumberOfBins;
	float Minimum=Float.MAX_VALUE;
	float Maximum=0;
	for (int i=0; i<n; i++) {
	    if (matrix.get(Gene,i)<Minimum) Minimum=matrix.get(Gene,i);
	}
	for (int i=0; i<n; i++) {
	    matrix.set(Gene,i,matrix.get(Gene,i)-Minimum);
	}
	for (int i=0; i<n; i++) {
	    if (matrix.get(Gene,i)>Maximum) Maximum=matrix.get(Gene,i);
	}
	for (int i=0; i<n; i++) {
	    matrix.set(Gene,i,matrix.get(Gene,i)/Maximum);
	}
	for (int i=0; i<n; i++) {
	    if (matrix.get(Gene,i)==1.0) {
		matrix.set(Gene,i,(float)NumberOfBins);
	    } else {
		matrix.set(Gene,i,(float)(Math.floor(matrix.get(Gene,i)*1000000/Step)+1));
	    }
	}
    }
    
    private static void normalizeExperiment(FloatMatrix matrix, int ExperimentNumber) {
	double Mean=0.0;
	double StandardDeviation=0.0;
	int i;
	int n=matrix.getRowDimension();
	float value;
	for (i=0; i<n; i++) {
	    value=matrix.get(i,ExperimentNumber);
	    if (!Float.isNaN(value)) Mean += value;
	}
	Mean /= (double)n;
	for (i=0; i<n; i++) {
	    value=matrix.get(i,ExperimentNumber);
	    if (!Float.isNaN(value)) StandardDeviation+=Math.pow((value-Mean),2);
	}
	StandardDeviation=Math.sqrt(StandardDeviation/(double)(n-1));
	for (i=0; i<n; i++) {
	    value=matrix.get(i,ExperimentNumber);
	    if (!Float.isNaN(value)) {
		if (StandardDeviation!=0) {
		    matrix.set(i,ExperimentNumber,(float)((value-Mean)/StandardDeviation));
		} else {
		    matrix.set(i,ExperimentNumber,(float)((value-Mean)/Float.MIN_VALUE));
		}
	    }
	}
    }
    
    private static void divideExperimentBySD(FloatMatrix matrix, int ExperimentNumber) {
	double Mean=0.0;
	double StandardDeviation=0.0;
	int i;
	int n=matrix.getRowDimension();
	float value;
	for (i=0; i<n; i++) {
	    value=matrix.get(i,ExperimentNumber);
	    if (!Float.isNaN(value)) Mean += value;
	}
	Mean /= (double)n;
	for (i=0; i<n; i++) {
	    value=matrix.get(i,ExperimentNumber);
	    if (!Float.isNaN(value)) StandardDeviation+=Math.pow((value-Mean),2);
	}
	StandardDeviation=Math.sqrt(StandardDeviation/(double)(n-1));
	for (i=0; i<n; i++) {
	    value=matrix.get(i,ExperimentNumber);
	    if (!Float.isNaN(value)) {
		if (StandardDeviation!=0) {
		    matrix.set(i,ExperimentNumber,(float)((value)/StandardDeviation));
		} else {
		    matrix.set(i,ExperimentNumber,(float)((value)/Float.MIN_VALUE));
		}
	    }
	}
    }
    
    private static void divideExperimentByRMS(FloatMatrix matrix, int ExperimentNumber) {
	double Mean=0.0;
	double RMS=0.0;
	float value;
	int i;
	int n=matrix.getRowDimension();
	for (i=0; i<n; i++) {
	    value=matrix.get(i,ExperimentNumber);
	    if (!Float.isNaN(value)) RMS+=Math.pow(value,2);
	}
	RMS=Math.sqrt(RMS/(double)(n));
	for (i=0; i<n; i++) {
	    value=matrix.get(i,ExperimentNumber);
	    if (!Float.isNaN(value)) {
		if (RMS!=0) {
		    matrix.set(i,ExperimentNumber,(float)((value)/RMS));
		} else {
		    matrix.set(i,ExperimentNumber,(float)((value)/Float.MIN_VALUE));
		}
	    }
	}
    }
    
    private static void meanCenterExperiment(FloatMatrix matrix, int ExperimentNumber) {
	double Mean=0.0;
	int i;
	int n=matrix.getRowDimension();
	float value;
	for (i=0; i<n; i++) {
	    value=matrix.get(i,ExperimentNumber);
	    if (!Float.isNaN(value)) Mean += value;
	}
	Mean /= (double)n;
	for (i=0; i<n; i++) {
	    value=matrix.get(i,ExperimentNumber);
	    if (!Float.isNaN(value)) {
		matrix.set(i,ExperimentNumber,(float)(value-Mean));
	    }
	}
    }
    
    private static void medianCenterExperiment(FloatMatrix matrix, int ExperimentNumber) {
	int i;
	int n=matrix.getRowDimension();
	float value;
	int k=0;
	for (i=0; i<n; i++) {
	    value=matrix.get(i,ExperimentNumber);
	    if (!Float.isNaN(value)) k++;
	}
	float[] DummyArray=new float [k];
	k=0;
	for (i=0; i<n; i++) {
	    value=matrix.get(i,ExperimentNumber);
	    if (!Float.isNaN(value)) {
		DummyArray[k]=value;
		k++;
	    }
	}
	Arrays.sort(DummyArray);
	float Median=0.0f;
	if (k%2==0) {
	    if (k>0) Median=(float)(0.5*(DummyArray[k/2-1]+DummyArray[(k/2)]));
	} else {
	    Median=DummyArray[(k+1)/2-1];
	}
	for (i=0; i<n; i++) {
	    value=matrix.get(i,ExperimentNumber);
	    if (!Float.isNaN(value)) {
		matrix.set(i,ExperimentNumber,(float)(value-Median));
	    }
	}
    }
    
    private static void makeDigitalExperiment(FloatMatrix matrix, int Experiment) {
	int n=matrix.getRowDimension();
	int NumberOfBins=(int)Math.floor(Math.log(n)/Math.log(2));
	int Step=1000000/NumberOfBins;
	float Minimum=Float.MAX_VALUE;
	float Maximum=0;
	for (int i=0; i<n; i++) {
	    if (matrix.get(i,Experiment)<Minimum) Minimum=matrix.get(i,Experiment);
	}
	for (int i=0; i<n; i++) {
	    matrix.set(i,Experiment,matrix.get(i,Experiment)-Minimum);
	}
	for (int i=0; i<n; i++) {
	    if (matrix.get(i,Experiment)>Maximum) Maximum=matrix.get(i,Experiment);
	}
	if (Maximum!=0) {
	    for (int i=0; i<n; i++) {
		matrix.set(i,Experiment,matrix.get(i,Experiment)/Maximum);
	    }
	}
	for (int i=0; i<n; i++) {
	    if (matrix.get(i,Experiment)==1.0) {
		matrix.set(i,Experiment,(float)NumberOfBins);
	    } else {
		matrix.set(i,Experiment,(float)(Math.floor(matrix.get(i,Experiment)*1000000/Step)+1));
	    }
	}
    }
}
