/*
 * ComparisonFlankingRegionCalculator.java
 *
 * Created on November 22, 2003, 5:54 PM
 */

package org.tigr.microarray.mev.cgh.CGHDataGenerator;

import org.tigr.microarray.mev.cgh.CGHDataObj.*;
//import org.abramson.microarray.cgh.CGHFcdObj.CGHMultipleArrayDataFcd;
//import org.tigr.microarray.mev.cgh.CGHGuiObj.Menus.ICGHCloneValueMenu;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.MultipleArrayData;

//import java.util.Iterator;
import java.util.Vector;
//import java.util.ArrayList;

/**
 *
 * @author  Adam Margolin
 * @author Raktim Sinha
 */
public class ComparisonFlankingRegionCalculator{
    MultipleArrayData data;
    //CGHMultipleArrayDataFcd fcd;
	//IData data;

    int[] cloneIndices;
    int[] comparisonIndices;
    int frCloneIndex = 0;
    int comparisonPointer;

    int[] experimentIndices;

    /** Creates a new instance of ComparisonFlankingRegionCalculator */
    public ComparisonFlankingRegionCalculator() {
    }

    public Vector calculateFlankingRegions(/*CGHMultipleArrayDataFcd fcd*/IData data, int[] cloneIndices, int[] comparisonIndices, int experimentIndices){
        int[] indices = {experimentIndices};
        return calculateFlankingRegions(data, cloneIndices, comparisonIndices, indices);
    }
    public Vector calculateFlankingRegions(/*CGHMultipleArrayDataFcd fcd*/IData data, int[] cloneIndices, int[] comparisonIndices, int[] experimentIndices){
        frCloneIndex = 0;
        this.data = (MultipleArrayData)data;
        this.experimentIndices = experimentIndices;
        this.comparisonIndices = comparisonIndices;
        this.cloneIndices = cloneIndices;
        this.comparisonPointer = 0;
        Vector flankingRegions = new Vector();

        for(int chrom = 0; chrom < data.getNumChromosomes(); chrom++){
            flankingRegions.addAll(calculateChromosomeFlankingRegions(chrom));
        }

        return flankingRegions;
    }

    private Vector calculateChromosomeFlankingRegions(int chromosomeIndex){
        Vector flankingRegions = new Vector();

        for(int cloneIndex = data.getChromosomeStartIndex(chromosomeIndex); cloneIndex <= data.getChromosomeEndIndex(chromosomeIndex); cloneIndex++){
            //if(frCloneIndex >= cloneIndices.length){
            //    return flankingRegions;
            //}

            if(frCloneIndex < cloneIndices.length && cloneIndex == cloneIndices[frCloneIndex]){
                int flankingRegionStart = findFlankingRegionStart(cloneIndex, data.getChromosomeStartIndex(chromosomeIndex));
                CGHClone flankingRegionStartClone = findFlankingRegionStartClone(cloneIndex);
                FlankingRegion curFlankingRegion = new FlankingRegion();

                cloneIndex = calculateFlankingRegionEnd(cloneIndex, data.getChromosomeEndIndex(chromosomeIndex));

                int flankingRegionStop = findFlankingRegionStop(cloneIndex, data.getChromosomeEndIndex(chromosomeIndex));
                CGHClone flankingRegionStopClone = findFlankingRegionStopClone(cloneIndex);

                curFlankingRegion.setStart(flankingRegionStart);
                curFlankingRegion.setStop(flankingRegionStop);
                curFlankingRegion.setChromosome(chromosomeIndex);
                curFlankingRegion.setStartClone(flankingRegionStartClone);
                curFlankingRegion.setStopClone(flankingRegionStopClone);

                flankingRegions.add(curFlankingRegion);
                frCloneIndex++;
            }
        }

        return flankingRegions;
    }



    private int findFlankingRegionStart(int cloneIndex, int chromosomeStartIndex){

        while(cloneIndex >= chromosomeStartIndex && isBad(cloneIndex - 1)){
            cloneIndex--;
        }
        if(cloneIndex <= chromosomeStartIndex){
            return 0;
        }
        return data.getCloneAt(cloneIndex - 1).getStop();
    }


    private CGHClone findFlankingRegionStartClone(int cloneIndex){
        return data.getCloneAt(cloneIndex);
    }

    private int calculateFlankingRegionEnd(int cloneIndex, int chromosomeEndIndex){

        while((cloneIndex + 1) <= chromosomeEndIndex){
            if(((frCloneIndex + 1) < cloneIndices.length) && (cloneIndex + 1) == (cloneIndices[frCloneIndex + 1])){
                cloneIndex++;
                frCloneIndex++;
            }else if(isBad(cloneIndex + 1)){
                cloneIndex++;
            }else{
                break;
            }
        }

        return cloneIndex;

    }

    private int findFlankingRegionStop(int cloneIndex, int chromosomeEndIndex){
        if(cloneIndex + 1 < chromosomeEndIndex){
            return data.getCloneAt(cloneIndex + 1).getStart();
        }else{
            return data.getCloneAt(cloneIndex).getStop();
        }
    }

    private CGHClone findFlankingRegionStopClone(int cloneIndex){
        return data.getCloneAt(cloneIndex);
    }

    private boolean isBad(int cloneIndex){
        for(int i = 0; i < experimentIndices.length; i++){
            if(data.getCopyNumberDetermination(experimentIndices[i], cloneIndex) != IData.BAD_CLONE){
                return false;
            }
        }

        if(!comparatorExists(cloneIndex)){
            return true;
        }else{
            return false;
        }
    }

    private boolean comparatorExists(int cloneIndex){
        if (this.comparisonIndices == null){
            return false;
        }

        //just resetting the pointer for now because is was running past the index
        comparisonPointer = 0;

        while(comparisonPointer < comparisonIndices.length && comparisonIndices[comparisonPointer] < cloneIndex){
            comparisonPointer++;
        }

        if(comparisonPointer < comparisonIndices.length && comparisonIndices[comparisonPointer] == cloneIndex){
            return true;
        }

        return false;
    }
}