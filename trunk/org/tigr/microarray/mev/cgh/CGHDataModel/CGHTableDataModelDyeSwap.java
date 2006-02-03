/*
 * CGHTableDataModelDyeSwap.java
 *
 * Created on July 5, 2003, 11:28 PM
 */

package org.tigr.microarray.mev.cgh.CGHDataModel;

import org.tigr.microarray.mev.cluster.gui.IFramework;

/**
 *
 * @author  Adam Margolin
 * @author Raktim Sinha
 */

public class CGHTableDataModelDyeSwap extends CGHTableDataModel {

    /** Creates a new instance of CGHTableDataModelDyeSwap */
    public CGHTableDataModelDyeSwap(/*CGHMultipleArrayDataFcd fcd,*/ IFramework framework, int experimentIndex, int chromosomeIndex){
        super(/*fcd,*/ framework, experimentIndex, chromosomeIndex);
    }

    /**
     * @return the number of columns.  The number of annotations plus the number of experiments
     * if viewing all experiments, the number of annotaitons plus two (one for cy3 experiment, one for
     * cy5 experiment) if viewing only one experiment
     */
    public int getColumnCount() {
        int valueColumns = 0;
        switch(adaptor.cloneValueType){
            case CGHBrowserModelAdaptor.CLONE_VALUES_DYE_SWAP:
                valueColumns = adaptor.experimentIndices.length * 2;
                break;
            case CGHBrowserModelAdaptor.CLONE_VALUES_LOG_AVERAGE_INVERTED:
                valueColumns = adaptor.experimentIndices.length;
                break;
            case CGHBrowserModelAdaptor.CLONE_VALUES_LOG_DYE_SWAP:
                valueColumns = adaptor.experimentIndices.length * 2;
                break;
            case CGHBrowserModelAdaptor.CLONE_VALUES_P_SCORE:
                valueColumns = adaptor.experimentIndices.length * 2;
                break;

        }
        return valueColumns + getNumAnnotationCols();
    }

    public Object getDataValueAt(int row, int col){
        int dataIndex = col - getNumAnnotationCols();
        float val;
//      Raktim
        //System.out.println("adaptor.cloneValueType & adaptor.exprInd: " + adaptor.cloneValueType + " " + dataIndex);
        switch(adaptor.cloneValueType){
            case CGHBrowserModelAdaptor.CLONE_VALUES_DYE_SWAP:
                return getDyeSwapValue(dataIndex, row);
            case CGHBrowserModelAdaptor.CLONE_VALUES_LOG_AVERAGE_INVERTED:
                val = getLogAverageInvertedValue(dataIndex, row);
                if(Float.isInfinite(val)){
                    return null;
                }else{
                    return new Float(val);
                }
            case CGHBrowserModelAdaptor.CLONE_VALUES_LOG_DYE_SWAP:
                val = getLogDyeSwapValue(dataIndex, row);
                if(Float.isInfinite(val)){
                    return new Float(Float.NaN);
                }else{
                    return new Float(val);
                }
            case CGHBrowserModelAdaptor.CLONE_VALUES_P_SCORE:
                return getCloneDistributionPScoreValue(dataIndex, row);
            default:
                return null;
        }
    }


    private Object getDyeSwapValue(int index, int row){

        int dyeSwapIndex = index % 2;
        int experimentIndex = index / 2;

        if(dyeSwapIndex == 0){
            //return new Float(fcd.getRatio(adaptor.experimentIndices[experimentIndex], adaptor.getCloneIndex(row), CGHSampleData.CY3_SLIDES));
        	return new Float(data.getCY3(adaptor.experimentIndices[experimentIndex], adaptor.getCloneIndex(row)));

        }else if(dyeSwapIndex == 1){
            //return new Float(fcd.getRatio(adaptor.experimentIndices[experimentIndex], adaptor.getCloneIndex(row), CGHSampleData.CY5_SLIDES));
        	return new Float(data.getCY5(adaptor.experimentIndices[experimentIndex], adaptor.getCloneIndex(row)));
        }
        return null;
    }

    private float getLogDyeSwapValue(int index, int row){

        int dyeSwapIndex = index % 2;
        int experimentIndex = index / 2;

        if(dyeSwapIndex == 0){
            //return fcd.getRatio(adaptor.experimentIndices[experimentIndex], adaptor.getCloneIndex(row), CGHSampleData.CY3_SLIDES, true);
        	return data.getCY3(adaptor.experimentIndices[experimentIndex], adaptor.getCloneIndex(row));
        }else if(dyeSwapIndex == 1){
            //return fcd.getRatio(adaptor.experimentIndices[experimentIndex], adaptor.getCloneIndex(row), CGHSampleData.CY5_SLIDES, true);
        	return data.getCY5(adaptor.experimentIndices[experimentIndex], adaptor.getCloneIndex(row));
        }
        return Float.NaN;
    }

    private float getLogAverageInvertedValue(int index, int row){
        try{
            float val = data.getLogAverageInvertedValue(adaptor.experimentIndices[index], adaptor.getCloneIndex(row));
            return val;
        }catch (Exception e){
            return Float.NaN;
        }
    }

    private Object getCloneDistributionPScoreValue(int index, int row){

        int dyeSwapIndex = index % 2;
        int experimentIndex = index / 2;
        if(dyeSwapIndex == 0){
            return new Float(data.getPValueByLogCloneDistribution(adaptor.experimentIndices[experimentIndex], adaptor.getCloneIndex(row)));

        }else if(dyeSwapIndex == 1){
            return new Float(data.getPValueByLogCloneDistribution(adaptor.experimentIndices[experimentIndex], adaptor.getCloneIndex(row)));
        }

        return null;
    }

    public String getColumnDataName(int labelIndex){

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
        return label;
    }

}
