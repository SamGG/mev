/*
 * CGHAnnotationsModel.java
 *
 * Created on May 18, 2003, 8:58 PM
 */

package org.tigr.microarray.mev.cgh.CGHDataModel;

import java.awt.Color;

import org.tigr.microarray.mev.cgh.CGHDataObj.CGHClone;
import org.tigr.microarray.mev.cgh.CGHDataObj.FlankingRegion;
import org.tigr.microarray.mev.cgh.CGHDataObj.ICGHDataRegion;
import org.tigr.microarray.mev.cgh.CGHDataObj.IGeneData;
import org.tigr.microarray.mev.cluster.gui.IData;
import org.tigr.microarray.mev.cluster.gui.IFramework;

/**
 *
 * @author  Adam Margolin
 * @author Raktim Sinha
 */

public class CGHAnnotationsModel {

    int chromosomeIndex;
    //CGHMultipleArrayDataFcd fcd;
    IData data;


    /** Creates a new instance of CGHAnnotationsModel */
    public CGHAnnotationsModel(/*CGHMultipleArrayDataFcd fcd,*/ IFramework framework, int chromosomeIndex) {
        //this.fcd = fcd;
    	this.data = framework.getData();
        this.chromosomeIndex = chromosomeIndex;
    }

    public int getNumAnnotations(){
        try{
            return data.getAnnotations()[chromosomeIndex].length;
        }catch (ArrayIndexOutOfBoundsException e){
            return 0;
        }
    }

    public ICGHDataRegion getAnnotationAt(int index){
        return data.getAnnotations()[chromosomeIndex][index];
    }

    public Color getAnnotationColorAt(int index){
        ICGHDataRegion dataRegion = getAnnotationAt(index);

        if(dataRegion instanceof CGHClone){
            return Color.yellow;
        }else if(dataRegion instanceof FlankingRegion){
            return Color.cyan;
        }else if(dataRegion instanceof IGeneData){
            return Color.white;
        }

        return Color.magenta;

    }

}
