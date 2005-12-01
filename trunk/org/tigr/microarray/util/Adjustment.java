/*
Copyright @ 1999-2005, The Institute for Genomic Research (TIGR).
All rights reserved.
 */
/*
 * $RCSfile: Adjustment.java,v $
 * $Revision: 1.5 $
 * $Date: 2005-12-01 19:19:10 $
 * $Author: wwang67 $
 * $State: Exp $
 */
package org.tigr.microarray.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.ArrayList;

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
    
    // pcahan -- affy-abs specific adjustments
    public static void divideGenesMedian(FloatMatrix matrix) {
        float median = 0f;
        float value = 0f;
        int num_samples = matrix.getColumnDimension();
        int num_genes = matrix.getRowDimension();
        
        ArrayList row = new ArrayList(num_genes);
        // foreach gene
        for (int gene = 0; gene < num_genes; gene++) {
            median = 0f;
            
            // get median
            for (int sample = 0; sample < num_samples; sample++) {
                row.add(sample, new Float(matrix.get(gene,sample)));
            }
            
            median = getGeneMedian(row);
            
            // set value = signal/median
            for (int sample = 0; sample < num_samples; sample++) {
                value = matrix.get(gene, sample);
                
                matrix.set(gene, sample, (float) (value / median));
            }
            row.clear();
        }
    }
    
    public static void divideGenesMean(FloatMatrix matrix) {
        
        for (int g=0; g<matrix.getRowDimension(); g++) {
            double Mean = 0.0;
            int i;
            int n = matrix.getColumnDimension();
            int validN = 0;
            
            float value;
            for (i = 0; i < n; i++) {
                value = matrix.get(g, i);
                if (!Float.isNaN(value)) {
                    Mean += value;
                    validN++;
                }
            }
            
            if(validN > 0)
                Mean /= (double)validN;
 
            for (i = 0; i < n; i++) {
                value = matrix.get(g, i);
                if (!Float.isNaN(value)) {
                    matrix.set(g, i, (float) (value / Mean));
                }
            }
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
    public static void log2toLog10(FloatMatrix matrix) {
        float value;
        for (int i=0; i<matrix.getRowDimension(); i++) {
            for (int j=0; j<matrix.getColumnDimension(); j++) {
                value=matrix.get(i,j);
                if (!Float.isNaN(value)) matrix.set(i,j,(float)(value*0.301029995));
            }
        }
    }
    private static void normalizeGene(FloatMatrix matrix, int GeneNumber) {
        double Mean=0.0;
        double StandardDeviation=0.0;
        int i;
        int n=matrix.getColumnDimension();
        int validN = 0;
        float value;
        for (i=0; i<n; i++) {
            value=matrix.get(GeneNumber,i);
            if (!Float.isNaN(value)) {
                Mean += value;
                validN++;
            }
        }
        
        if(validN > 0)
            Mean /= (double)validN;
        
        for (i=0; i<n; i++) {
            value=matrix.get(GeneNumber,i);
            if (!Float.isNaN(value)) StandardDeviation+=Math.pow((value-Mean),2);
        }
        
        if(validN > 1)
            StandardDeviation=Math.sqrt(StandardDeviation/(double)(validN-1));
        else
            StandardDeviation=0.0d;
        
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
        int validN = 0;
        for (i=0; i<n; i++) {
            value=matrix.get(GeneNumber,i);
            if (!Float.isNaN(value)) {
                RMS+=Math.pow((value),2);
                validN++;
            }
        }
        if(validN > 1)
            RMS=Math.sqrt(RMS/(double)(validN-1));
        else if(validN == 0)
            RMS=Math.sqrt(RMS);
        else
            RMS=0.0d;
        
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
        int validN = 0;
        float value;
        for (i=0; i<n; i++) {
            value=matrix.get(GeneNumber,i);
            if (!Float.isNaN(value)) {
                Mean += value;
                validN++;
            }
        }
        
        if(validN > 0)
            Mean /= (double)validN;
        
        for (i=0; i<n; i++) {
            value=matrix.get(GeneNumber,i);
            if (!Float.isNaN(value)) StandardDeviation+=Math.pow((value-Mean),2);
        }
        
        if(validN > 1)
            StandardDeviation=Math.sqrt(StandardDeviation/(double)(validN-1));
        else
            StandardDeviation=0.0d;
        
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
        int validN = 0;
        float value;
        for (i=0; i<n; i++) {
            value=matrix.get(GeneNumber,i);
            if (!Float.isNaN(value)) {
                Mean += value;
                validN++;
            }
        }
        
        if(validN > 0)
            Mean /= (double)validN;
        
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
        int validN = 0;
        float value;
        for (i=0; i<n; i++) {
            value=matrix.get(i,ExperimentNumber);
            if (!Float.isNaN(value)) {
                Mean += value;
                validN++;
            }
        }
        
        if(validN > 0)
            Mean /= (double)validN;
        
        for (i=0; i<n; i++) {
            value=matrix.get(i,ExperimentNumber);
            if (!Float.isNaN(value)) StandardDeviation+=Math.pow((value-Mean),2);
        }
        
        if(validN > 1)
            StandardDeviation=Math.sqrt(StandardDeviation/(double)(validN-1));
        else
            StandardDeviation=0.0d;
        
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
        int validN = 0;
        float value;
        for (i=0; i<n; i++) {
            value=matrix.get(i,ExperimentNumber);
            if (!Float.isNaN(value)) {
                Mean += value;
                validN++;
            }
        }
        
        if(validN > 0)
            Mean /= (double)validN;
        
        for (i=0; i<n; i++) {
            value=matrix.get(i,ExperimentNumber);
            if (!Float.isNaN(value)) StandardDeviation+=Math.pow((value-Mean),2);
        }
        
        if(validN > 1)
            StandardDeviation=Math.sqrt(StandardDeviation/(double)(validN-1));
        else
            StandardDeviation=0.0d;
        
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
        int validN = 0;
        for (i=0; i<n; i++) {
            value=matrix.get(i,ExperimentNumber);
            if (!Float.isNaN(value)) {
                RMS+=Math.pow(value,2);
                validN++;
            }
        }
        if(validN > 0)
            RMS=Math.sqrt(RMS/(double)(validN));
        
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
        int validN = 0;
        float value;
        for (i=0; i<n; i++) {
            value=matrix.get(i,ExperimentNumber);
            if (!Float.isNaN(value)) {
                Mean += value;
                validN++;
            }
        }
        
        if(validN > 0)
            Mean /= (double)validN;
        
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
    
    // pcahan
    public static float getGeneMedian( ArrayList float_array ) {
        
        Collections.sort(float_array);
        
        Float median;
        
        if (float_array.size() == 1){
            return ( (Float) float_array.get(0)).floatValue();
        }
        
        int center = float_array.size() / 2;
        
        if (float_array.size() % 2 == 0) {
            Float a, b;
            a = (Float) float_array.get(center);
            b = (Float) float_array.get(center - 1);
            median = new Float(( a.floatValue() + b.floatValue() )/2);
        }
        else {
            median = (Float)float_array.get( center );
        }
        return median.floatValue();
    }
}
