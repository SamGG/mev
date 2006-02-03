/*
 * CGHCircleViewerModel.java
 *
 * Created on October 10, 2002, 4:56 AM
 */

package org.tigr.microarray.mev.cgh.CGHDataModel;

import java.awt.Color;

import org.tigr.microarray.mev.cgh.CGHDataObj.CGHClone;
import org.tigr.microarray.mev.cluster.gui.IFramework;

/**
 *
 * @author  Adam Margolin
 * @author Raktim Sinha
 */

public class CGHCircleViewerModel  extends CGHViewerDataModel implements ICircleViewerModel{

    /** Creates a new instance of CGHCircleViewerModel */
    //public CGHCircleViewerModel(CGHMultipleArrayDataFcd fcd) {
	public CGHCircleViewerModel(IFramework framework) {
        super(framework);
    }

    public Color getDataPointColor(int chrom, int bac, int experimentIndex){
        float value = data.getValue(experimentIndex, bac, chrom);
        return getColor(value);
    }

    public int getNumChromosomes(){
        return data.getNumChromosomes();
    }

    public int getNumDataPointsInChrom(int chrom){
        return data.getNumDataPointsInChrom(chrom);
    }


    public CGHClone getCloneAt(int cloneIndex, int chromosomeIndex) {
        return data.getCloneAt(cloneIndex, chromosomeIndex);
    }

}