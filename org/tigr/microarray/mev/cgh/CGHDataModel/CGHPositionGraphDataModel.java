/*
 * TigrCGHPositionGraphDataModel.java
 *
 * Created on March 16, 2003, 11:46 PM
 */

package org.tigr.microarray.mev.cgh.CGHDataModel;

import java.awt.Color;

import org.tigr.microarray.mev.ISlideData;
import org.tigr.microarray.mev.cgh.CGHDataObj.CGHClone;
import org.tigr.microarray.mev.cgh.CGHDataObj.FlankingRegion;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.IFramework;


/**
 *
 * @author  Adam Margolin
 * @author Raktim Sinha
 */

public class CGHPositionGraphDataModel extends CGHViewerDataModel implements IPositionGraphDataModel{

    int chromosomeIndex;

    /** Creates a new instance of TigrCGHPositionGraphDataModel */
    public CGHPositionGraphDataModel(/*CGHMultipleArrayDataFcd fcd,*/ IFramework framework, int chromosomeIndex) {
        super(framework);

        this.chromosomeIndex = chromosomeIndex;
    }


    public Color getDataPointColor(int cloneIndex, int experimentIndex){
        CGHClone clone = data.getCloneAt(cloneIndex, chromosomeIndex);

        float value = data.getValue(experimentIndex, cloneIndex, chromosomeIndex);
        if(value == IData.NO_COPY_CHANGE){
            //return getColor(0);
            return COLOR_DEFAULT;
        }

        return getColor(value);
    }

    public Color getFlankingRegionColor(int expIndex, int flankingRegionIndex) {
        FlankingRegion fr = getFlankingRegionAt(expIndex, flankingRegionIndex);

        if(fr.getType() == FlankingRegion.DELETION){
            //return COLOR_DEL_ONE_COPY;
            if(fr.getSpecifier() == FlankingRegion.DELETION_1_COPY){
                return COLOR_DEL;
            }else if(fr.getSpecifier() == FlankingRegion.DELETION_2_COPY){
                return COLOR_DEL_2_COPY;
            }
        }else if(fr.getType() == FlankingRegion.AMPLIFICATION){
            if(fr.getSpecifier() == FlankingRegion.AMPLIFICATION_1_COPY){
                return COLOR_AMP;
            }else if(fr.getSpecifier() == FlankingRegion.AMPLIFICATION_2_COPY){
                return COLOR_AMP_2_COPY;
            }
        }

        return COLOR_ERROR;
    }

    public FlankingRegion getFlankingRegionAt(int expIndex, int flankingRegionIndex) {
        return (FlankingRegion) ((ISlideData)data.getFeaturesList().get(expIndex)).getFlankingRegions()[chromosomeIndex].get(flankingRegionIndex);
    }

    public CGHClone getCloneAt(int index){
        return data.getCloneAt(index,chromosomeIndex);
    }

    public int getMaxClonePosition() {
        int maxCloneIndex = data.getChromosomeIndices()[chromosomeIndex][1];

        CGHClone clone = data.getCloneAt(maxCloneIndex);
        int maxPosition = clone.getStop();

        return maxPosition;
    }

    public int getNumElements() {
        return data.getFeaturesSize(chromosomeIndex);
    }

    public int getNumExperiments() {
        return data.getFeaturesCount();
    }

    public int getNumFlankingRegions(int expIndex) {
        return data.getNumFlankingRegions(expIndex,chromosomeIndex);
    }

    public int getStart(int index) {
        return data.getCloneAt(index,chromosomeIndex).getStart();
    }

    public int getStop(int index) {
        return data.getCloneAt(index,chromosomeIndex).getStop();
    }

    public String getExperimentName(int index) {
        return data.getSampleName(index);
    }

    /** Getter for property chromosomeIndex.
     * @return Value of property chromosomeIndex.
     */
    public int getChromosomeIndex() {
        return chromosomeIndex;
    }

    /** Setter for property chromosomeIndex.
     * @param chromosomeIndex New value of property chromosomeIndex.
     */
    public void setChromosomeIndex(int chromosomeIndex) {
        this.chromosomeIndex = chromosomeIndex;
    }

    public int getExperimentIndexAt(int index){
        int[] experimentIndices = data.getSamplesOrder();
        return experimentIndices[index];
    }

}
