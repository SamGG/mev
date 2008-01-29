/*
 * CGHChartDataModel.java
 *
 * Created on October 30, 2002, 7:34 PM
 */

package org.tigr.microarray.mev.cgh.CGHDataModel;


import javax.swing.event.ChangeListener;

import org.tigr.microarray.mev.cgh.CGHDataObj.CGHClone;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.IFramework;

import com.klg.jclass.chart.ChartDataEvent;
import com.klg.jclass.chart.ChartDataManageable;
import com.klg.jclass.chart.ChartDataModel;
import com.klg.jclass.chart.ChartDataSupport;
import com.klg.jclass.chart.LabelledChartDataModel;

/**
 *
 * @author  Adam Margolin
 * @author Raktim Sinha
 */

public abstract class CGHChartDataModel extends ChartDataSupport implements ChartDataModel, LabelledChartDataModel, ChartDataManageable, ChangeListener {
    public static final int CHROM_LOCATION = 0;
    public static final int LINEAR_ORDER = 1;

    int xAxisPositionType = CHROM_LOCATION;
    boolean smoothUnconfirmed;

    //CGHMultipleArrayDataFcd fcd;
    IData data;

    CGHBrowserModelAdaptor adaptor;

    public CGHChartDataModel(/*CGHMultipleArrayDataFcd fcd,*/ IFramework framework){
        this(/*fcd,*/ framework, 0, 0);
    }

    public CGHChartDataModel(/*CGHMultipleArrayDataFcd fcd,*/ IFramework framework, int experimentIndex, int chromosomeIndex){
        //this.fcd = fcd;
        this.data = framework.getData();
    }

    public com.klg.jclass.chart.ChartDataManager getChartDataManager() {
        return this;
    }

    public String getDataSourceName() {
        return "";
    }

    /*
     * @return the number of experiments if viewing all experiments, 2 otherwise
     * representing the cy3 and cy5 experiments
     */
    public abstract int getNumSeries();

    public String[] getPointLabels() {

        String[] labels = new String[adaptor.getSeriesSize()];
        for(int i = 0; i < labels.length; i++){
            labels[i] = data.getCloneAt(adaptor.getCloneIndex(i)).getName();
        }
        return labels;
    }

    public abstract String[] getSeriesLabels();

    public double[] getXSeries(int index) {

        double[] xSeries = new double[getSeriesSize()];

        if(adaptor.chromosomeIndex == CGHBrowserModelAdaptor.ALL_CHROMOSOMES && xAxisPositionType == CHROM_LOCATION){
            return getXSeriesAllChromosomesChromLocation(xSeries);
        }

        for(int i = 0; i < xSeries.length; i++){
            if(xAxisPositionType == CHROM_LOCATION){
                //xSeries[i] = data.getCloneAt(mapIndexToClone[i]).getStart();
                xSeries[i] = data.getCloneAt(adaptor.getCloneIndex(i)).getStart();
            }else if(xAxisPositionType == LINEAR_ORDER){
                xSeries[i] = i;
            }
        }
        return xSeries;
    }

    private double[] getXSeriesAllChromosomesChromLocation(double[] xSeries){
        double total = 0;

        int curChromIndex = 0;
        for(int index = 0; index < xSeries.length; index++){
            int cloneIndex = adaptor.getCloneIndex(index);
            CGHClone clone = data.getCloneAt(cloneIndex);
            while(clone.getChromosomeIndex() > curChromIndex){
                total += (double)data.getCloneAt(data.getNumDataPointsInChrom(curChromIndex) - 1, curChromIndex).getStart();
                curChromIndex++;
            }

            xSeries[index] = (double)clone.getStart() + total;
        }

        return xSeries;
    }

    public abstract double[] getYSeries(int index);



    public int getSeriesSize(){
        return adaptor.getSeriesSize();
    }




    public void smoothUnconfirmed(boolean smooth){
        this.smoothUnconfirmed = smooth;
        fireChartDataEvent(ChartDataEvent.RESET, ChartDataEvent.ALL_SERIES, ChartDataEvent.ALL_POINTS);
    }

    public void setXAxisPositions(int type){
        this.xAxisPositionType = type;
        fireChartDataEvent(ChartDataEvent.RESET, ChartDataEvent.ALL_SERIES, ChartDataEvent.ALL_POINTS);
    }


    public int getRelativeIndexOf(int absoluteIndex){
        if(adaptor.chromosomeIndex == CGHBrowserModelAdaptor.ALL_CHROMOSOMES){
            return absoluteIndex;
        }else{
            return data.getRelativeIndex(absoluteIndex, adaptor.chromosomeIndex);
        }
    }

    public int getCloneIndex(CGHClone clone){
        int absoluteCloneIndex = data.getClones().indexOf(clone);
        int relativeIndex = getRelativeIndexOf(absoluteCloneIndex);
        return adaptor.getIndexOf(relativeIndex);
    }

    public int getStartCloneIndex(CGHClone clone){
        int absoluteCloneIndex = data.getClones().indexOf(clone);
        int relativeIndex = getRelativeIndexOf(absoluteCloneIndex);

        int cloneIndex = adaptor.getIndexOf(relativeIndex--);

        if(cloneIndex == -1){
            while((cloneIndex = adaptor.getIndexOf(relativeIndex--)) == -1){
                if(cloneIndex == 0){
                    return 0;
                }
            }
            return cloneIndex + 1;
        }
        return cloneIndex;
    }

    public int getStopCloneIndex(CGHClone clone){
        //Look into this method
        try{
        int absoluteCloneIndex = data.getClones().indexOf(clone);
        int relativeIndex = getRelativeIndexOf(absoluteCloneIndex);

        int cloneIndex = adaptor.getIndexOf(relativeIndex++);
        if(cloneIndex >= getSeriesSize() - 1){
            return getSeriesSize() - 1;
        }

        if(cloneIndex == -1){
            while((cloneIndex = adaptor.getIndexOf(relativeIndex++)) == -1){
                if(cloneIndex >= getSeriesSize() - 1){
                    return getSeriesSize() - 1;
                }
            }
            return cloneIndex - 1;
        }
        return cloneIndex;
        }catch (ArrayIndexOutOfBoundsException e){
            System.out.println("Chart Model getStopCloneIndex out of bounds... returning last index");
            //return mapCloneToIndex[mapCloneToIndex.length - 1];
            return getSeriesSize() - 1;
        }
    }

    public CGHClone getCloneByPosition(int position){
        for(int i = 0; i < data.getNumDataPointsInChrom(adaptor.chromosomeIndex); i++){
            int startIndex = data.getCloneAt(i, adaptor.chromosomeIndex).getStart();
            int stopIndex = data.getCloneAt(i, adaptor.chromosomeIndex).getStop();
            if(startIndex == position || stopIndex == position){
                return data.getCloneAt(i, adaptor.chromosomeIndex);
            }
        }
        return null;
    }

    public void stateChanged(javax.swing.event.ChangeEvent changeEvent) {
        fireChartDataEvent(ChartDataEvent.RESET, ChartDataEvent.ALL_SERIES, ChartDataEvent.ALL_POINTS);
    }

    public void setExperimentIndex(int experimentIndex){
        adaptor.setExperimentIndex(experimentIndex);
    }

    public void setChromosomeIndex(int chromosomeIndex){
        adaptor.setChromosomeIndex(chromosomeIndex);
    }

    public void setCloneValueType(int cloneValueType){
        adaptor.setCloneValueType(cloneValueType);
    }

    /** Getter for property adaptor.
     * @return Value of property adaptor.
     */
    public CGHBrowserModelAdaptor getAdaptor() {
        return adaptor;
    }

    /** Setter for property adaptor.
     * @param adaptor New value of property adaptor.
     */
    public void setAdaptor(CGHBrowserModelAdaptor adaptor) {
        this.adaptor = adaptor;
    }

}