/*
 * CGHCopyNumberCalculator.java
 * Raktim Sinha
 * Created on October 31, 2005
 */

package org.tigr.microarray.mev.cgh.CGHDataGenerator;

import java.util.ArrayList;

import org.tigr.microarray.mev.ISlideData;
import org.tigr.microarray.mev.MultipleArrayData;
import org.tigr.microarray.mev.cluster.gui.ICGHCloneValueMenu;
import org.tigr.microarray.mev.cluster.gui.IFramework;
/**
 * This class is used to make determinations about a CGHMultipleArrayData object
 * that must rely on other information.  For example, this class makes copy number
 * determinations based on data stored in a CGHMultipleArrayData object and parameters
 * stored in an IFramework object
 *
 * @author  Adam Margolin
 * @author Raktim Sinha
 *
 */

public class CGHCopyNumberCalculator {
	public static final int BAD_CLONE = -10;
    public static final int NO_COPY_CHANGE = -11;

    MultipleArrayData data;
    IFramework framework;
    //CGHMultipleArrayDataFcd fcd;

    float ampThresh, delThresh, ampThresh2Copy, delThresh2Copy;
    float cloneDistributionPThresh;

    /** Creates a new instance of CGHMultipleArrayDataFcd */
    //public CGHCopyNumberCalculator(MultipleArrayData data, IFramework framework, CGHMultipleArrayDataFcd fcd) {
    public CGHCopyNumberCalculator(MultipleArrayData data) {
        this.data = data;
        //this.framework = framework;
        //this.fcd = fcd;
    }

    public void onCopyDeterminationChanged(ICGHCloneValueMenu menu){
        this.ampThresh = menu.getAmpThresh();
        this.delThresh = menu.getDelThresh();
        this.ampThresh2Copy = menu.getAmpThresh2Copy();
        this.delThresh2Copy = menu.getDelThresh2Copy();
        this.cloneDistributionPThresh = menu.getClonePThresh();
    }

    public int getCopyNumberDetermination(int experiment, int clone, int chromosome){
        return getCopyNumberDetermination(experiment, data.getCloneIndex(clone, chromosome));
    }

    public int getCopyNumberDetermination(int experiment, int clone){
        ArrayList featuresList = data.getFeaturesList();
        ISlideData sampleData = (ISlideData)featuresList.get(experiment);
        return getCopyNumberDetermination(sampleData, clone);
    }

    public int getCopyNumberDetermination(ISlideData sampleData, int clone, int chromosome){
        return getCopyNumberDetermination(sampleData, data.getCloneIndex(clone, chromosome));
    }

    /**
     * Raktim Oct 31, 2005
     * Chnaged to suit MeV struct
     * @param sampleData
     * @param clone
     * @return
     */
    public int getCopyNumberDetermination(ISlideData sampleData, int clone){
        int copyNumber = determineCopyNumber(sampleData, clone);

        if(copyNumber == CGHCopyNumberCalculator.BAD_CLONE){
            return CGHCopyNumberCalculator.BAD_CLONE;
        }
        if(copyNumber == CGHCopyNumberCalculator.NO_COPY_CHANGE){
            return CGHCopyNumberCalculator.NO_COPY_CHANGE;
        }else{
            if(copyNumber == -2  || copyNumber == -1){
                return -1;
            }else if(copyNumber  == 2 || copyNumber == 1 ){
                return 1;
            }
            return CGHCopyNumberCalculator.NO_COPY_CHANGE;
        }
    }

    /*
    public int getCopyNumberDetermination(ISlideData sampleData, int clone){

        int cy3CopyNumber = determineCopyNumber(sampleData, clone, ISlideData.CY3_SLIDES);
        int cy5CopyNumber = determineCopyNumber(sampleData, clone, ISlideData.CY5_SLIDES);

        if(cy3CopyNumber == CGHCopyNumberCalculator.BAD_CLONE || cy5CopyNumber == CGHCopyNumberCalculator.BAD_CLONE){
            return CGHCopyNumberCalculator.BAD_CLONE;
        }
        if(cy3CopyNumber == CGHCopyNumberCalculator.NO_COPY_CHANGE || cy5CopyNumber == CGHCopyNumberCalculator.NO_COPY_CHANGE){
            return CGHCopyNumberCalculator.NO_COPY_CHANGE;
        }else if(cy3CopyNumber == cy5CopyNumber){
            return cy3CopyNumber;
        }else{
            if((cy3CopyNumber == -2 && cy5CopyNumber == -1) || (cy3CopyNumber == -1 && cy5CopyNumber == -2)){
                return -1;
            }else if((cy3CopyNumber  == 2 && cy5CopyNumber == 1) || (cy3CopyNumber == 1 && cy5CopyNumber == 2)){
                return 1;
            }

            return CGHCopyNumberCalculator.NO_COPY_CHANGE;
        }
    }
    */

    /*
     *
     * Old. Not to be Used now. May be used if CY3 & CY5 data are loaded separately
    public int determineCopyNumber(Vector slides, int clone, int testDye){
        return determineCopyNumberByThreshold(slides, clone, testDye);
    }
    */

    /*
     * Conforming to CGH Data Struct
    public int determineCopyNumberByThreshold(Vector slides, int clone, int testDye){
        Iterator it = slides.iterator();
        int copyNumber = CGHCopyNumberCalculator.BAD_CLONE;

        if(it.hasNext()){
            CGHSlideData slideData = (CGHSlideData)it.next();
            copyNumber = determineCopyNumberByThreshold(slideData, clone, testDye);
        }

        while(it.hasNext()){
            CGHSlideData slideData = (CGHSlideData)it.next();
            int slideCopyNumber = determineCopyNumberByThreshold(slideData, clone, testDye);
            if(slideCopyNumber != CGHCopyNumberCalculator.BAD_CLONE){
                if(copyNumber == CGHCopyNumberCalculator.BAD_CLONE){
                    copyNumber = slideCopyNumber;
                }else if (slideCopyNumber != copyNumber){
                    if((copyNumber == -2 && slideCopyNumber == -1) || (copyNumber == -1 && slideCopyNumber == -2)){
                        copyNumber = -1;
                    }else if((copyNumber == 2 && slideCopyNumber == 1) || (copyNumber == 1 && slideCopyNumber == 2)){
                        copyNumber = 1;
                    }else{
                        return CGHCopyNumberCalculator.NO_COPY_CHANGE;
                    }
                }
            }
        }

        return copyNumber;
    }
    */
    //Test
    /*
    public int determineCopyNumberByThreshold(ISlideData slide, int clone, int testDye){
        float ratio = slide.getRatio(clone, 0);
        if(Float.isNaN(ratio) || ratio <= 0){
            return CGHCopyNumberCalculator.BAD_CLONE;
        }

        if(testDye == ISlideData.CY3_SLIDES){
            return determineCy3CopyNumberByThreshold(ratio);
        }else{
            return determineCy5CopyNumberByThreshold(ratio);
        }
    }
    */
    /**
     * Raktim Oct 31, 2005
     * @param slide
     * @param clone
     * @param testDye
     * @return

    public int determineCopyNumber(ISlideData slide, int clone, int testDye){
        return determineCopyNumberByThreshold(slide, clone, testDye);
    }
    */
    /**
     * Raktim Oct 31, 2005
     * @param slides
     * @param clone
     * @return
     */
    public int determineCopyNumber(ISlideData slide, int clone){
        return determineCopyNumberByThreshold(slide, clone);
    }

    /**
     * Remember getRatio Glitch
     * Raktim Oct 31, 2005
     * @param slide
     * @param clone
     * @return
     */
    public int determineCopyNumberByThreshold(ISlideData slide, int clone){
        float ratio = slide.getRatio(clone, 0);
        if(Float.isNaN(ratio) /*|| ratio <= 0*/){
            return CGHCopyNumberCalculator.BAD_CLONE;
        }

        if(ratio < delThresh2Copy){ return -2; }
        if(ratio < delThresh){ return -1; }
        if(ratio > ampThresh2Copy){ return 2; }
        if(ratio > ampThresh){ return 1; }
        return CGHCopyNumberCalculator.NO_COPY_CHANGE;
    }

    /*
    //Old Adama Func
    public int determineCy3CopyNumberByThreshold(float ratio){
        if(ratio < delThresh2Copy){ return -2; }
        if(ratio < delThresh){ return -1; }
        if(ratio > ampThresh2Copy){ return 2; }
        if(ratio > ampThresh){ return 1; }
        return CGHCopyNumberCalculator.NO_COPY_CHANGE;
    }
    //Old Adama Func
    public int determineCy5CopyNumberByThreshold(float ratio){
        if(ratio < delThresh2Copy){ return 2; }
        if(ratio < delThresh){ return 1; }
        if(ratio > ampThresh2Copy){ return -2; }
        if(ratio > ampThresh){ return -1; }
        return CGHCopyNumberCalculator.NO_COPY_CHANGE;
    }
    */

    public int getCopyNumberDeterminationByLogCloneDistribution(int experiment, int clone){

        int cy3CopyNumber = determineCopyNumberByLogCloneDistribution(experiment, clone, ISlideData.CY3_SLIDES);
        int cy5CopyNumber = determineCopyNumberByLogCloneDistribution(experiment, clone, ISlideData.CY5_SLIDES);

        if(cy3CopyNumber == CGHCopyNumberCalculator.BAD_CLONE || cy5CopyNumber == CGHCopyNumberCalculator.BAD_CLONE){
            return CGHCopyNumberCalculator.BAD_CLONE;
        }else if(cy3CopyNumber == cy5CopyNumber){
            return cy3CopyNumber;
        }else{
            return CGHCopyNumberCalculator.NO_COPY_CHANGE;
        }
    }

    /**
     * Raktim Oct 31, 2005
     * Modified for MeV data format
     * @param experiment
     * @param clone
     * @return
     */
    public int determineCopyNumberByLogCloneDistribution(int experiment, int clone){
        float pValue = data.getPValueByLogCloneDistribution(experiment, clone);
        if(Float.isNaN(pValue)){
            return CGHCopyNumberCalculator.BAD_CLONE;
        }

        if(pValue > cloneDistributionPThresh){
        	return 1;
        }else if (pValue < 1 - cloneDistributionPThresh){
        	return -1;
        }else
             return CGHCopyNumberCalculator.NO_COPY_CHANGE;
    }

    /**
     * Old Func. May be used later if CY3 & CY5 data are loaded separately
     * @param experiment
     * @param clone
     * @param testDye
     * @return
     */
    public int determineCopyNumberByLogCloneDistribution(int experiment, int clone, int testDye){
        float pValue = data.getPValueByLogCloneDistribution(experiment, clone);
        if(Float.isNaN(pValue)){
            return CGHCopyNumberCalculator.BAD_CLONE;
        }

        if(testDye == ISlideData.CY3_SLIDES){
            if(pValue > cloneDistributionPThresh){
                return 1;
            }else if (pValue < 1 - cloneDistributionPThresh){
                return -1;

            }
        }else{
            if(pValue > cloneDistributionPThresh){
                return -1;
            }else if (pValue < 1 - cloneDistributionPThresh){
                return 1;
            }
        }
        return CGHCopyNumberCalculator.NO_COPY_CHANGE;
    }

    public int getCopyNumberDeterminationByThresholdOrCloneDistribution(int experiment, int clone){
        int threshCopyNumber = getCopyNumberDetermination(experiment, clone);
        int distCopyNumber = getCopyNumberDeterminationByLogCloneDistribution(experiment, clone);

        if(threshCopyNumber == CGHCopyNumberCalculator.BAD_CLONE){
            return distCopyNumber;
        }

        if(distCopyNumber == CGHCopyNumberCalculator.BAD_CLONE){
            return threshCopyNumber;
        }

        if(threshCopyNumber == CGHCopyNumberCalculator.NO_COPY_CHANGE){
            return distCopyNumber;
        }

        if(distCopyNumber == CGHCopyNumberCalculator.NO_COPY_CHANGE){
            return threshCopyNumber;
        }

        if(distCopyNumber < 0 && threshCopyNumber < 0){
            return Math.min(distCopyNumber, threshCopyNumber);
        }

        if(distCopyNumber > 0 && threshCopyNumber > 0){
            return Math.max(distCopyNumber, threshCopyNumber);
        }

        //if we get here, then one method said the clone is amplified and the other said it is deleted.  This would
        // be bizzare (actually impossible unless the thresholds are set incorrectly), so I will just say it's a bad clone
        return CGHCopyNumberCalculator.BAD_CLONE;
    }

    /** Getter for property data.
     * @return Value of property data.
     */
    public MultipleArrayData getData() {
        return data;
    }

    /** Setter for property data.
     * @param data New value of property data.
     */
    public void setData(MultipleArrayData data) {
        this.data = data;
    }

}
