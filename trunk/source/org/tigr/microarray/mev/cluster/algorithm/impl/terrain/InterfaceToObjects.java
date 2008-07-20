/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * $RCSfile: InterfaceToObjects.java,v $
 * $Revision: 1.4 $
 * $Date: 2006-02-23 20:59:46 $
 * $Author: caliente $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.algorithm.impl.terrain;

import javax.vecmath.Vector2f;

import org.tigr.microarray.mev.cluster.algorithm.impl.util.FloatArray;
import org.tigr.microarray.mev.cluster.algorithm.impl.util.IntArray;

// pure virtual interface to work with real objects in subject area
// it is not nescessary for this interface to work fast because all 
// data should be cached in appropriate place
public interface InterfaceToObjects {
    // Data retrieval methods used for caching the objects in FDGLA	

    // returns the array of Object Identificators(ID)
    int[] GetAllObjectsIds();
    // returns the metrix(x and y coords) for every object with iObjID identity
    void GetObjectGeom(int iObjID, Vector2f rRet);
    int  GetAdjCountFor(int iObjId);
    void GetAdjInfoFor(int iObjId, IntArray rArrAdjIds, FloatArray rArrAdjVals);
    // Uses to return data back to the 'storage'
    void SetObjectGeom(Vector2f[] rRet);
}
