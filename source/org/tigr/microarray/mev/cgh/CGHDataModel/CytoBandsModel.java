/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * CytoBandsModel.java
 *
 * Created on January 23, 2003, 6:01 PM
 */

package org.tigr.microarray.mev.cgh.CGHDataModel;

import java.awt.Color;
import java.util.Iterator;
import java.util.Vector;

import org.tigr.microarray.mev.cgh.CGHDataObj.CytoBand;
import org.tigr.microarray.mev.cgh.CGHDataObj.CytoBands;

/**
 *
 * @author  Adam Margolin
 * @author Raktim Sinha
 */

public class CytoBandsModel {

    public Color COLOR_GNEG = Color.lightGray;
    public Color COLOR_GPOS = Color.darkGray;
    public Color COLOR_GVAR = Color.gray;
    public Color COLOR_ACEN = Color.red;

    public Color COLOR_DEFAULT = Color.yellow;

    private CytoBands cytoBands;// = SessionObjects.cytoBands;

    int chromosomeIndex = 0;
    Vector dataElements = new Vector();

    /** Creates a new instance of CytoBandsModel */
    public CytoBandsModel(CytoBands cytoBands) {
        this.cytoBands = cytoBands;

        //this.chromosomeIndex = chromosomeIndex;
        //dataElements = cytoBands.getDataElementsAt();
    }

    public void setChromosomeIndex(int chromosomeIndex){
        this.chromosomeIndex = chromosomeIndex;
        dataElements = cytoBands.getDataElementsAt(chromosomeIndex);
    }

    public int getNumCytoBands(){
        return dataElements.size();
    }


    public int getMaxPosition(){
        int maxPosition = 0;
        Iterator it = dataElements.iterator();

        CytoBand curCytoBand;

        while(it.hasNext()){
            curCytoBand = (CytoBand)it.next();
            if(curCytoBand.getChromEnd() > maxPosition){
                maxPosition = curCytoBand.getChromEnd();
            }
        }
        return maxPosition;
    }

    public CytoBand getCytoBandAt(int index){
        return (CytoBand) dataElements.get(index);
    }

    public Color getDataPointColor(int cytoBandIndex){
        String stain = ((CytoBand)dataElements.get(cytoBandIndex)).getStain();

        if("gpos75".equals(stain) || "gpos50".equals(stain) || "gpos100".equals(stain) || "gpos25".equals(stain) || "gpos33".equals(stain) || "gpos66".equals(stain)){
            return COLOR_GPOS;
        }else if("gneg".equals(stain)){
            return COLOR_GNEG;
        }else if("gvar".equals(stain)){
            return COLOR_GVAR;
        }else if("acen".equals(stain)){
            return COLOR_ACEN;
        }else{
            return COLOR_DEFAULT;
        }
    }
}
