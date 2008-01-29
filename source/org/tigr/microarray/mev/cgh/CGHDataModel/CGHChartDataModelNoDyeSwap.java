/*
 * CGHChartDataModelNoDyeSwap.java
 *
 * Created on July 5, 2003, 11:24 PM
 */

package org.tigr.microarray.mev.cgh.CGHDataModel;
/*
import com.klg.jclass.chart.ChartDataModel;
import com.klg.jclass.chart.LabelledChartDataModel;
import com.klg.jclass.chart.ChartDataSupport;
import com.klg.jclass.chart.ChartDataEvent;
import com.klg.jclass.chart.ChartDataManageable;
*/
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.IFramework;

/**
 *
 * @author  Adam Margolin
 * @author Raktim Sinha
 */

public class CGHChartDataModelNoDyeSwap extends CGHChartDataModel {

    /** Creates a new instance of CGHChartDataModelNoDyeSwap */
    public CGHChartDataModelNoDyeSwap(/*CGHMultipleArrayDataFcd fcd,*/ IFramework framework, int experimentIndex, int chromosomeIndex){
        super(/*fcd,*/ framework, experimentIndex, chromosomeIndex);
    }

    /*
     * @return the number of experiments if viewing all experiments, 2 otherwise
     * representing the cy3 and cy5 experiments
     */
    public int getNumSeries() {
        return adaptor.experimentIndices.length;
    }

    public String[] getSeriesLabels() {
        int numSeries = getNumSeries();
        String[] seriesLabels = new String[numSeries];

        for(int labelIndex = 0; labelIndex < numSeries; labelIndex++){
            String sampleName = data.getSampleName(adaptor.experimentIndices[labelIndex]);
            seriesLabels[labelIndex] = sampleName;
        }
        return seriesLabels;
    }


    public double[] getYSeries(int index) {
        double[] ySeries = new double[getSeriesSize()];
        for(int i = 0; i < ySeries.length; i++){
            if(smoothUnconfirmed){
                int copyNumber = data.getCopyNumberDetermination(adaptor.experimentIndices[index], adaptor.getCloneIndex(i));
                if(copyNumber == IData.BAD_CLONE || copyNumber == IData.NO_COPY_CHANGE){
                    ySeries[i] = 0;
                    continue;
                }
            }

            float val = getDataValue(adaptor.experimentIndices[index], adaptor.getCloneIndex(i), adaptor.getCloneValueType());
            if(Float.isNaN(val) || Float.isInfinite(val)){
                //ySeries[i] = Double.NaN;
            }else{
                ySeries[i] = val;
            }
        }
        return ySeries;
    }

    /**
     * Remember getRatio Glitch
     * @param experimentIndex
     * @param cloneIndex
     * @param cloneValueType
     * @return
     */
    private float getDataValue(int experimentIndex, int cloneIndex, int cloneValueType){
        switch (cloneValueType){
            case CGHBrowserModelAdaptor.CLONE_VALUES_RATIOS:
                return data.getRatio(experimentIndex, cloneIndex, IData.LOG);
            case CGHBrowserModelAdaptor.CLONE_VALUES_LOG_RATIOS:
                return data.getRatio(experimentIndex, cloneIndex, IData.LOG);
            case CGHBrowserModelAdaptor.CLONE_VALUES_P_SCORE:
                return data.getPValueByLogCloneDistribution(experimentIndex, cloneIndex);
        }
        return Float.NaN;
    }
}
