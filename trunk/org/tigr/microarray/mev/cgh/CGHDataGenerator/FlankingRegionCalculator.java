/*
 * FlankingRegionCalculator.java
 *
 * Created on December 26, 2002, 9:25 PM
 */

package org.tigr.microarray.mev.cgh.CGHDataGenerator;

import org.tigr.microarray.mev.cgh.CGHDataObj.*;
//import org.abramson.microarray.cgh.CGHFcdObj.CGHMultipleArrayDataFcd;
import org.tigr.microarray.mev.ISlideData;
import org.tigr.microarray.mev.cluster.gui.ICGHCloneValueMenu;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.MultipleArrayData;
import java.util.Iterator;
import java.util.Vector;
import java.util.ArrayList;

/**
 *
 * @author  Adam Margolin
 * @author Raktim Sinha
 */

public class FlankingRegionCalculator {

    ArrayList experiments;
    //CGHMultipleArrayDataFcd fcd;
    IData data;
    int copyDeterminationType = ICGHCloneValueMenu.CLONE_VALUE_DISCRETE_DETERMINATION;
    int experimentIndex;
    /** Creates a new instance of FlankingRegionCalculator */
    public FlankingRegionCalculator() {
    }

    public void calculateFlankingRegions(){
        Iterator it = experiments.iterator();
        int counter = 0;
        while(it.hasNext()){
            this.experimentIndex = counter;
            calculateSampleFlankingRegions(experimentIndex, (/*ICGHFeatureData*/ISlideData)it.next());
            counter++;
        }
        //Raktim
        //System.out.println("Experiments Counter " + counter);
    }

    private void calculateSampleFlankingRegions(int experimentIndex, /*ICGHFeatureData*/ ISlideData flank){
        Vector[] sampleFlankingRegions = new Vector[data.getNumChromosomes()];
        for(int i = 0; i < sampleFlankingRegions.length; i++){
            sampleFlankingRegions[i] = calculateChromosomeSampleFlankingRegions(experimentIndex, i);
        }

        flank.setFlankingRegions(sampleFlankingRegions);
    }

    private Vector calculateChromosomeSampleFlankingRegions(int experimentIndex, int chromosomeIndex){
        Vector flankingRegions = new Vector();
        /**
         * Raktim
         * Need not calculate num Of Data Pts for each experiment for normalized ratios.
         * The assumption there is all samples will have the same num of data points for
         * each chromosome.
         * Do this once in the previous function
         */
        int numDataPts = data.getNumDataPointsInChrom(chromosomeIndex);
        //System.out.println("Chr Samp Flank Regions: " + experimentIndex + ", " + chromosomeIndex + ", " + numDataPts);
        for(int cloneIndex = 0; cloneIndex < numDataPts; cloneIndex++){
        //for(int cloneIndex = 0; cloneIndex < fcd.getData().getNumDataPointsInChrom(chromosomeIndex); cloneIndex++){
            int copyNumber = getCopyNumberDetermination(experimentIndex, cloneIndex, chromosomeIndex);
            //System.out.println("Chr Samp Flank Regions: " + experimentIndex + ", " + cloneIndex + ", " + chromosomeIndex + ", " + copyNumber);
            if(copyNumber != IData.NO_COPY_CHANGE && copyNumber != IData.BAD_CLONE){ //clone is not normal so this begins a flanking region
                int flankingRegionStart = findFlankingRegionStart(experimentIndex, cloneIndex, chromosomeIndex);
                CGHClone flankingRegionStartClone = findFlankingRegionStartClone(cloneIndex, chromosomeIndex);
                FlankingRegion curFlankingRegion = new FlankingRegion();

                if(copyNumber < -1){
                    curFlankingRegion.setType(FlankingRegion.DELETION);
                    curFlankingRegion.setSpecifier(FlankingRegion.DELETION_2_COPY);
                    cloneIndex = calculateFlankingRegionEnd(experimentIndex, cloneIndex, chromosomeIndex, copyNumber);
                //}else if(copyNumber == 1){
                }else if(copyNumber < 0){
                    if(copyNumber != -1){
                        System.out.println("FLCalc copy num < 0 = " + copyNumber);
                    }
                    curFlankingRegion.setType(FlankingRegion.DELETION);
                    curFlankingRegion.setSpecifier(FlankingRegion.DELETION_1_COPY);
                    cloneIndex = calculateFlankingRegionEnd(experimentIndex, cloneIndex, chromosomeIndex, copyNumber);
                }else if(copyNumber > 1){
                    curFlankingRegion.setType(FlankingRegion.AMPLIFICATION);
                    curFlankingRegion.setSpecifier(FlankingRegion.AMPLIFICATION_2_COPY);
                    cloneIndex = calculateFlankingRegionEnd(experimentIndex, cloneIndex, chromosomeIndex, copyNumber);
                }else if(copyNumber > 0){
                    curFlankingRegion.setType(FlankingRegion.AMPLIFICATION);
                    curFlankingRegion.setSpecifier(FlankingRegion.AMPLIFICATION_1_COPY);
                    cloneIndex = calculateFlankingRegionEnd(experimentIndex, cloneIndex, chromosomeIndex, copyNumber);
                }

                int flankingRegionStop = findFlankingRegionStop(cloneIndex, chromosomeIndex);
                CGHClone flankingRegionStopClone = findFlankingRegionStopClone(cloneIndex, chromosomeIndex);

                curFlankingRegion.setStart(flankingRegionStart);
                curFlankingRegion.setStop(flankingRegionStop);
                curFlankingRegion.setChromosome(chromosomeIndex);
                curFlankingRegion.setStartClone(flankingRegionStartClone);
                curFlankingRegion.setStopClone(flankingRegionStopClone);

                flankingRegions.add(curFlankingRegion);
            }
        }

        return flankingRegions;
    }


    private int findFlankingRegionStart(int experimentIndex, int cloneIndex, int chromosomeIndex){

        if(cloneIndex == 0){
            return 0;
        }

        while(getCopyNumberDetermination(experimentIndex, cloneIndex - 1, chromosomeIndex) == IData.BAD_CLONE){
            cloneIndex--;
            if(cloneIndex == 0){
                break;
            }
        }


        if(cloneIndex != 0){
        	return data.getCloneAt(cloneIndex - 1, chromosomeIndex).getStop();
        }else{
            return data.getCloneAt(cloneIndex, chromosomeIndex).getStart();
        }

    }


    private CGHClone findFlankingRegionStartClone(int cloneIndex, int chromosomeIndex){
        //if(cloneIndex != 0){
        //    return fcd.getData().getCloneAt(cloneIndex - 1, chromosomeIndex);
        //}else{
            return data.getCloneAt(cloneIndex, chromosomeIndex);
        //}
    }

    private int findFlankingRegionStop(int cloneIndex, int chromosomeIndex){
        if(cloneIndex + 1 < data.getNumDataPointsInChrom(chromosomeIndex)){
            return data.getCloneAt(cloneIndex + 1, chromosomeIndex).getStart();
        }else{
            return data.getCloneAt(cloneIndex, chromosomeIndex).getStop();
        }
    }

    private CGHClone findFlankingRegionStopClone(int cloneIndex, int chromosomeIndex){
        //if(cloneIndex + 1 < fcd.getData().getNumDataPointsInChrom(chromosomeIndex)){
        //    return fcd.getData().getCloneAt(cloneIndex + 1, chromosomeIndex);
        //}else{
            return data.getCloneAt(cloneIndex, chromosomeIndex);
        //}
    }


    private int calculateFlankingRegionEnd(int experimentIndex, int cloneIndex, int chromosomeIndex, int flCopyNumber){
        int copyNumber = 0;
        int numDataPts = data.getNumDataPointsInChrom(chromosomeIndex);
        while(cloneIndex + 1 < numDataPts &&
        getCopyNumberDetermination(experimentIndex, cloneIndex + 1, chromosomeIndex) == flCopyNumber){

            cloneIndex++;
        }

        copyNumber = getCopyNumberDetermination(experimentIndex, cloneIndex + 1, chromosomeIndex);

        if(cloneIndex + 1 >= numDataPts){
            return cloneIndex;
        }

        if(copyNumber == IData.BAD_CLONE){
            return calculateFlankingRegionEnd(experimentIndex, cloneIndex + 1, chromosomeIndex, flCopyNumber);
        }else{
            return cloneIndex;
        }
    }


    private int getCopyNumberDetermination(int experimentIndex, int cloneIndex, int chromosomeIndex){
    	//Raktim
    	//System.out.println("copyDeterminationType: " + copyDeterminationType);
        if(copyDeterminationType == ICGHCloneValueMenu.FLANKING_REGIONS_BY_THRESHOLD){
            return ((MultipleArrayData)data).getCopyNumberDetermination(experimentIndex, cloneIndex, chromosomeIndex);
        }else if(copyDeterminationType == ICGHCloneValueMenu.FLANKING_REGIONS_BY_LOG_CLONE_DISTRIBUTION){
        	//Raktim. May be not needed ?
            return ((MultipleArrayData)data).getCopyNumberDeterminationByLogCloneDistribution(experimentIndex, data.getCloneIndex(cloneIndex, chromosomeIndex));
        }else if(copyDeterminationType == ICGHCloneValueMenu.FLANKING_REGIONS_BY_THRESHOLD_OR_CLONE_DISTRIBUTION){
        	//Raktim. May be not needed ?
            return ((MultipleArrayData)data).getCopyNumberDeterminationByThresholdOrCloneDistribution(experimentIndex, data.getCloneIndex(cloneIndex, chromosomeIndex));
        }else{
            return IData.NO_COPY_CHANGE;
        }
    }

    /** Getter for property experiments.
     * @return Value of property experiments.
     */
    public java.util.ArrayList getExperiments() {
        return experiments;
    }

    /** Setter for property experiments.
     * @param experiments New value of property experiments.
     */
    public void setExperiments(java.util.ArrayList experiments) {
        this.experiments = experiments;
    }

    /** Getter for property fcd.
     * @return Value of property fcd.
     */
    //public CGHMultipleArrayDataFcd getFcd() {
    public IData getData() {
        return this.data;
    }

    /** Setter for property fcd.
     * @param fcd New value of property fcd.
     */
    //public void setFcd(CGHMultipleArrayDataFcd fcd) {
    public void setData(IData dat) {
        this.data = dat;
    }

    /** Getter for property copyDeterminationType.
     * @return Value of property copyDeterminationType.
     */
    public int getCopyDeterminationType() {
        return copyDeterminationType;
    }

    /** Setter for property copyDeterminationType.
     * @param copyDeterminationType New value of property copyDeterminationType.
     */
    public void setCopyDeterminationType(int copyDeterminationType) {
        this.copyDeterminationType = copyDeterminationType;
    }

}
