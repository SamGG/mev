/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: PermutationTest.java,v $
 * $Revision: 1.3 $
 * $Date: 2005-03-10 15:45:28 $
 * $Author: braistedj $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.algorithm.impl;

import java.util.Arrays;

import org.tigr.util.FloatMatrix;
import org.tigr.microarray.mev.cluster.algorithm.*;

import org.tigr.microarray.mev.cluster.algorithm.impl.util.*;

public class PermutationTest extends AbstractAlgorithm {

    private static final int c_DecileCount = 10;
    public static final double LOG2 = Math.log(2.0);
    public static final float MINVAL = 0f;
    public static final float MAXVAL = 1f;

    private double getEntropy(float[] pVector) {
        double fltMin = Double.MAX_VALUE;
        double fltMax = -Double.MAX_VALUE;
        int i=0;
        int[] arrDeciles = new int[c_DecileCount];

        final int iSize = pVector.length;
        int iValCount = 0;
        for (i=0; i<iSize; i++) {
            if (Double.isNaN(pVector[i]))
                continue;
            fltMin = Math.min(fltMin, pVector[i]);
            fltMax = Math.max(fltMax, pVector[i]);
            iValCount++;
        }

        double fltStep = (fltMax-fltMin)/(c_DecileCount);
        if (fltStep == 0d) {
            return -1.0*Math.log(1.0)/LOG2;
        }

        if (fltMin == Double.MAX_VALUE)
            return 0d;

        Arrays.fill(arrDeciles, 0);
        for (i=0; i<iSize; i++) {
            if (Double.isNaN(pVector[i]))
                continue;
            int iDecileInd = (int)Math.ceil((pVector[i]-fltMin)/fltStep)-1;
            if (iDecileInd < 0) {
                iDecileInd = 0;
            }
            arrDeciles[iDecileInd]++;
        }
        if (iValCount == 0)
            return 0d;

        // finally, calculate entropy
        double dblEntropy=0;

        for (i=0; i<c_DecileCount; i++) {
            if (arrDeciles[i] == 0) {
                continue;
            }
            double dblPx=((double)arrDeciles[i])/iValCount;
            dblEntropy += dblPx*Math.log(dblPx)/LOG2; // log2(x)==log(x)/log(2)
        }
        return -dblEntropy;
    }

    private boolean stop = false;

    private int[]   m_arrMainHisto = null;  // Real histo stores here
    //private int[][] m_arrPermHistos = null; // for every i-th permutaion m_PermHistos[i] stores appropriate histogram
    private double[] m_arrAvgHisto=null;
    private int     m_iHistoSize = 0;
    private int     m_iPermutationSize = 0;
    private float   m_fltHistoStep = 0.5f;

    private float   m_fltMaxStatSignificant = 0;

    private void Assert(int iFrom, int iTo, float fltVal) {
        m_arrMainHisto[GetIndByVal(fltVal)]++;
    }

    private void AssertPermutted(int iPermutationNum, int iFrom, int iTo, float fltVal) {
        //m_arrPermHistos[iPermutationNum][GetIndByVal(fltVal)]++;
        m_arrAvgHisto[GetIndByVal(fltVal)]++;
        m_fltMaxStatSignificant=Math.max(m_fltMaxStatSignificant, fltVal);
    }

    private int GetIndByVal(float fltVal) {
        int iDecileInd = (int)Math.ceil((fltVal-(MINVAL))/m_fltHistoStep)-1;
        if (iDecileInd < 0)
            return 0;
        return iDecileInd;
    }

    private void RandomPermute(FloatMatrix matr, int iRow) {
        int iFirstInd = 0;
        int iSecondInd =0;
        int iPermCount=(int)(Math.random()*matr.getColumnDimension()+1);
        for(int i=0; i<iPermCount;i++){
          do{
            iFirstInd = (int)(Math.random()*(matr.getColumnDimension()-1));
            iSecondInd = (int)(Math.random()*(matr.getColumnDimension()-1));
          }while(iFirstInd==iSecondInd);

  	  //System.out.println(iFirstInd);
          float temp=matr.A[iRow][iFirstInd];
          matr.A[iRow][iFirstInd]=matr.A[iRow][iSecondInd];
          matr.A[iRow][iSecondInd]=temp;
        }
    }

    private void Run(int[] entropyIndices, FloatMatrix expMatrix, int function, float factor, boolean absolute) throws AlgorithmException {
        int filteredSize = entropyIndices.length;
        int progress = 0;
        int links = 0;
        int sum = filteredSize*(filteredSize+1)/2;
        int step = sum/100+1;
        float value;
        AlgorithmEvent event = new AlgorithmEvent(this, AlgorithmEvent.SET_UNITS, 100, "Calculating Histogram");
        fireValueChanged(event);

        event.setId(AlgorithmEvent.PROGRESS_VALUE);

        FloatMatrix temp= new FloatMatrix (1,expMatrix.getColumnDimension());
        for (int nCurIndOuter=0; nCurIndOuter<filteredSize; nCurIndOuter++) {
            if (this.stop) {
                throw new AbortException();
            }
            progress++;
            // calculation
           { float[] arrX = expMatrix.A[entropyIndices[nCurIndOuter]];
                    float[] arrY = temp.A[0];
                    for (int k=0; k<arrY.length; k++)// copy outer vectr
                      arrY[k]=arrX[k];
           }

            for (int nCurIndInner=nCurIndOuter+1; nCurIndInner<filteredSize; nCurIndInner++) {
                //value = AlgorithmUtil.geneDistance(expMatrix, null, entropyIndices[nCurIndOuter], entropyIndices[nCurIndInner], function, factor, absolute);
                //value = value*value; // = abs(r^2)
                 //value = AlgorithmUtil.genePearson(temp, expMatrix, 0, entropyIndices[nCurIndInner],1);
                 //value*=value;

                //Assert(entropyIndices[nCurIndOuter], entropyIndices[nCurIndInner], value);

                for (int i=0; i<m_iPermutationSize; i++) {
                    RandomPermute(temp, 0);
                    //value = AlgorithmUtil.geneDistance(temp, expMatrix, 0, entropyIndices[nCurIndInner], function, factor, absolute);
                    //value = value*value; // = abs(r^2)
                    value = ExperimentUtil.genePearson(temp, expMatrix, 0, entropyIndices[nCurIndInner],1);
                    value*=value;
                    AssertPermutted(i, entropyIndices[nCurIndOuter], entropyIndices[nCurIndInner], value);
                }

                // progress events handling
                progress++;
                if (progress%step == 0) {
                    event.setIntValue(progress/step);
                    event.setDescription("Calculating permutation histogram ");
                    fireValueChanged(event);
                }
            }
        }
    }

    public AlgorithmData execute(AlgorithmData data) throws AlgorithmException {

        FloatMatrix expMatrix = data.getMatrix("experiment");
        if (expMatrix == null)
            return null;

        AlgorithmParameters map = data.getParams();
        m_iHistoSize = map.getInt("decile-count", 1000);
        m_iPermutationSize = map.getInt("permutation-count", 20);
        m_fltHistoStep = (MAXVAL-MINVAL)/(float)m_iHistoSize;
        m_arrMainHisto = new int[m_iHistoSize];
        m_arrAvgHisto=new double[m_iHistoSize];
        //m_arrPermHistos = new int[m_iPermutationSize][];
        //for (int i=0; i<m_iPermutationSize; i++)
        //   m_arrPermHistos[i] = new int[m_iHistoSize];

        int function = map.getInt("distance-function", PEARSON);
        float factor = map.getFloat("distance-factor", 1.0f);
        boolean absolute = map.getBoolean("distance-absolute", true);

        int nGenes = expMatrix.getRowDimension();
        int filteredSize = nGenes;

        // For Entropy Filter
        boolean bFilterByEntropy = map.getBoolean("filter-by-entropy");
        float fltTopNPercent=map.getFloat("top-n-percent", 100f);
        if (fltTopNPercent < 0f || fltTopNPercent>100f) {
            throw new AlgorithmException("Filter value is out of range (0, 100)%");
        }

        int[] entropyIndices = new int[nGenes];
        for (int i=0; i<entropyIndices.length; i++) {
            entropyIndices[i] = i;
        }

        if (bFilterByEntropy) {
            double[] entropyValues = new double[nGenes];
            for (int i=0; i<entropyValues.length; i++) {
                entropyValues[i] = getEntropy(expMatrix.A[i]);
            }
            IntSorter.sort(entropyIndices, new RelNetComparator(entropyValues));
            filteredSize = (int)((float)nGenes*fltTopNPercent/100f);
        }

        Run(entropyIndices, expMatrix, function, factor, absolute);

        // Retrieve threshold from multidimensional histogramms

        // make average histo
        // TODO: add min histo,max histo

        //for (int j=0; j<m_arrAvgHisto.length; j++) {
          //  double fltAvg=0;
            //double fltMin=Double.MAX_VALUE; // never used in the future, but required for stdDev output in case of displayint a histogramm
            //double fltMax=-Double.MAX_VALUE;// never used in the future, but required for stdDev output in case of displayint a histogramm
            //for (int i=0; i<m_arrPermHistos.length; i++) {
             //   fltAvg+=(double)(m_arrPermHistos[i][j])/(double)m_iPermutationSize;
              //  fltMin=Math.min(fltMin, m_arrPermHistos[i][j]);
               // fltMax=Math.max(fltMax, m_arrPermHistos[i][j]);
            //}
            //m_arrAvgHisto[j]=fltAvg;
        //}

        float fltMinStatSignificantThreshold=m_fltMaxStatSignificant;
        if (fltMinStatSignificantThreshold>=1.0){//relaxed method(heuristic)
          for (int i=m_arrAvgHisto.length-1; i>0; i--) {
            //System.out.println("m_arrAvgHisto["+i+"]="+m_arrAvgHisto[i]);
              if (m_arrAvgHisto[i]/m_iPermutationSize>=1f ) {
                  fltMinStatSignificantThreshold=(i+1)*m_fltHistoStep;
                  if (fltMinStatSignificantThreshold>1f)// just in case
                      fltMinStatSignificantThreshold=1f;
                  break;
              }
          }
        }


        // return the result
        AlgorithmData result = new AlgorithmData();
        result.addParam("threshold", String.valueOf(fltMinStatSignificantThreshold));
        return result;
    }

    public void abort() {
        this.stop = true;
    }
}
