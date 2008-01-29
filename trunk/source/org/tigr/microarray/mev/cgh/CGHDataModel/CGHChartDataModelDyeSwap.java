/*
 * CGHChartDataModelDyeSwap.java
 *
 * Created on July 5, 2003, 11:15 PM
 */

package org.tigr.microarray.mev.cgh.CGHDataModel;

/*
import com.klg.jclass.chart.ChartDataModel;
import com.klg.jclass.chart.LabelledChartDataModel;
import com.klg.jclass.chart.ChartDataSupport;
import com.klg.jclass.chart.ChartDataEvent;
import com.klg.jclass.chart.ChartDataManageable;
*/
import org.tigr.microarray.mev.ISlideData;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.IFramework;
/*
import java.util.Vector;
import java.util.Iterator;
import java.util.Hashtable;
import javax.swing.event.ChangeListener;

import java.awt.Color;

import org.tigr.microarray.mev.cgh.CGHDataObj.*;
//import org.tigr.microarray.mev.cgh.CGHFcdObj.CGHMultipleArrayDataFcd;
import cern.jet.math.Arithmetic;

import cern.jet.stat.Probability;
*/
/**
 *
 * @author  Adam Margolin
 * @author Raktim Sinha
 */

public class CGHChartDataModelDyeSwap extends CGHChartDataModel {

    /** Creates a new instance of CGHChartDataModelDyeSwap */
    public CGHChartDataModelDyeSwap(/*CGHMultipleArrayDataFcd fcd,*/ IFramework framework, int experimentIndex, int chromosomeIndex){
        super(/*fcd,*/ framework, experimentIndex, chromosomeIndex);
    }

    /*
     * @return the number of experiments if viewing all experiments, 2 otherwise
     * representing the cy3 and cy5 experiments
     */
    public int getNumSeries() {
        switch(adaptor.cloneValueType){
            case CGHBrowserModelAdaptor.CLONE_VALUES_DYE_SWAP:
                return adaptor.experimentIndices.length * 2;
            case CGHBrowserModelAdaptor.CLONE_VALUES_LOG_AVERAGE_INVERTED:
                return adaptor.experimentIndices.length;
            case CGHBrowserModelAdaptor.CLONE_VALUES_LOG_DYE_SWAP:
                return adaptor.experimentIndices.length * 2;
            case CGHBrowserModelAdaptor.CLONE_VALUES_P_SCORE:
                return adaptor.experimentIndices.length * 2;
            default:
                return 0;
        }
    }

    public String[] getSeriesLabels() {
        int numSeries = getNumSeries();
        String[] seriesLabels = new String[numSeries];
        for(int labelIndex = 0; labelIndex < numSeries; labelIndex++){
            String label = "";
            int dyeSwapIndex = labelIndex % 2;
            int experimentIndex = labelIndex / 2;
            String sampleName = data.getSampleName(adaptor.experimentIndices[experimentIndex]);
            switch(adaptor.cloneValueType){
                case CGHBrowserModelAdaptor.CLONE_VALUES_DYE_SWAP:
                    if(dyeSwapIndex == 0){
                        label = sampleName + " Cy3";
                    }else if(dyeSwapIndex == 1){
                        label = sampleName + " Cy5";
                    }
                    break;
                case CGHBrowserModelAdaptor.CLONE_VALUES_LOG_AVERAGE_INVERTED:
                    //label = data.getSampleName(experimentIndices[experimentIndex]);
                    label = sampleName;
                    break;
                case CGHBrowserModelAdaptor.CLONE_VALUES_LOG_DYE_SWAP:
                    if(dyeSwapIndex == 0){
                        label = sampleName + " Cy3";
                    }else if(dyeSwapIndex == 1){
                        label = sampleName + " Cy5";
                    }
                    break;
                case CGHBrowserModelAdaptor.CLONE_VALUES_P_SCORE:
                    if(dyeSwapIndex == 0){
                        label = sampleName + " Cy3";
                    }else if(dyeSwapIndex == 1){
                        label = sampleName + " Cy5";
                    }
                    break;
            }
            seriesLabels[labelIndex] = label;
        }
        return seriesLabels;
    }

    public double[] getYSeries(int index) {
        switch(adaptor.cloneValueType){
            case CGHBrowserModelAdaptor.CLONE_VALUES_DYE_SWAP:
                return generateDyeSwapYSeries(index);
            case CGHBrowserModelAdaptor.CLONE_VALUES_LOG_AVERAGE_INVERTED:
                return generateLogAverageInvertedYSeries(index);
            case CGHBrowserModelAdaptor.CLONE_VALUES_LOG_DYE_SWAP:
                return generateLogDyeSwapYSeries(index);
            case CGHBrowserModelAdaptor.CLONE_VALUES_P_SCORE:
                return generateCloneDistributionPScoreYSeries(index);
            default:
                return null;
        }
    }

    /**
     * Remember getRatio Glitch
     * @param index
     * @return
     */
    private double[] generateDyeSwapYSeries(int index){
        double[] ySeries = new double[getSeriesSize()];

        int dyeSwapIndex = index % 2;
        int experimentIndex = index / 2;
        for(int i = 0; i < ySeries.length; i++){
            if(smoothUnconfirmed){
                int copyNumber = data.getCopyNumberDetermination(adaptor.experimentIndices[experimentIndex], adaptor.getCloneIndex(i));
                if(copyNumber == IData.BAD_CLONE || copyNumber == IData.NO_COPY_CHANGE){
                    ySeries[i] = 1;
                    continue;
                }
            }

            if(dyeSwapIndex == 0){
                ySeries[i] = data.getRatio(adaptor.experimentIndices[experimentIndex], adaptor.getCloneIndex(i), ISlideData.CY3_SLIDES);

            }else if(dyeSwapIndex == 1){
                ySeries[i] = data.getRatio(adaptor.experimentIndices[experimentIndex], adaptor.getCloneIndex(i), ISlideData.CY5_SLIDES);
            }
        }
        return ySeries;
    }

    private double[] generateLogDyeSwapYSeries(int index){
        double[] ySeries = new double[getSeriesSize()];

        int dyeSwapIndex = index % 2;
        int experimentIndex = index / 2;
        float val = Float.NaN;
        for(int i = 0; i < ySeries.length; i++){
            if(smoothUnconfirmed){
                int copyNumber = data.getCopyNumberDetermination(adaptor.experimentIndices[experimentIndex], adaptor.getCloneIndex(i));
                if(copyNumber == IData.BAD_CLONE || copyNumber == IData.NO_COPY_CHANGE){
                    ySeries[i] = 0;
                    continue;
                }
            }

            if(dyeSwapIndex == 0){
                //val = data.getRatio(adaptor.experimentIndices[experimentIndex], adaptor.getCloneIndex(i), ISlideData.CY3_SLIDES, true);
            	val = data.getCY3(adaptor.experimentIndices[experimentIndex], adaptor.getCloneIndex(i));
            }else if(dyeSwapIndex == 1){
                //val = data.getRatio(adaptor.experimentIndices[experimentIndex], adaptor.getCloneIndex(i), ISlideData.CY5_SLIDES, true);
            	val = data.getCY5(adaptor.experimentIndices[experimentIndex], adaptor.getCloneIndex(i));
            }
            if(Float.isNaN(val) || Float.isInfinite(val)){

            }else{
                ySeries[i] = val;
            }
        }
        return ySeries;
    }

    private double[] generateLogAverageInvertedYSeries(int index){
        double[] ySeries = new double[getSeriesSize()];
        for(int i = 0; i < ySeries.length; i++){
            if(smoothUnconfirmed){
                int copyNumber = data.getCopyNumberDetermination(adaptor.experimentIndices[index], adaptor.getCloneIndex(i));
                if(copyNumber == IData.BAD_CLONE || copyNumber == IData.NO_COPY_CHANGE){
                    ySeries[i] = 0;
                    continue;
                }
            }

            float val = data.getLogAverageInvertedValue(adaptor.experimentIndices[index], adaptor.getCloneIndex(i));
            if(Float.isNaN(val) || Float.isInfinite(val)){
                //ySeries[i] = Double.NaN;
            }else{
                ySeries[i] = val;
            }
        }
        return ySeries;
    }

    private double[] generateCloneDistributionPScoreYSeries(int index){
        double[] ySeries = new double[getSeriesSize()];

        int dyeSwapIndex = index % 2;
        int experimentIndex = index / 2;
        for(int i = 0; i < ySeries.length; i++){
            if(smoothUnconfirmed){
                int copyNumber = data.getCopyNumberDetermination(adaptor.experimentIndices[experimentIndex], adaptor.getCloneIndex(i));
                if(copyNumber == IData.BAD_CLONE || copyNumber == IData.NO_COPY_CHANGE){
                    ySeries[i] = .5;
                    continue;
                }
            }

            if(dyeSwapIndex == 0){
                ySeries[i] = data.getPValueByLogCloneDistribution(adaptor.experimentIndices[experimentIndex], adaptor.getCloneIndex(i));

            }else if(dyeSwapIndex == 1){
                ySeries[i] = data.getPValueByLogCloneDistribution(adaptor.experimentIndices[experimentIndex], adaptor.getCloneIndex(i));
            }
        }
        return ySeries;
    }
}
