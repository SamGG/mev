/*******************************************************************************
 * Copyright (c) 1999-2005 The Institute for Genomic Research (TIGR).
 * Copyright (c) 2005-2008, the Dana-Farber Cancer Institute (DFCI), 
 * J. Craig Venter Institute (JCVI) and the University of Washington.
 * All rights reserved.
 *******************************************************************************/
/*
 * CloneAlterations.java
 *
 * Created on May 19, 2003, 2:08 AM
 */

package org.tigr.microarray.mev.cgh.CGHAlgorithms.NumberOfAlterations.CloneAlterations;


import java.util.Collections;
import java.util.Vector;

import javax.swing.tree.DefaultMutableTreeNode;

import org.tigr.microarray.mev.cgh.CGHAlgorithms.NumberOfAlterations.NumberOfAlterationsCalculator;
import org.tigr.microarray.mev.cgh.CGHDataObj.AlterationRegion;
import org.tigr.microarray.mev.cgh.CGHDataObj.AlterationRegionsComparator;
import org.tigr.microarray.mev.cgh.CGHDataObj.CGHClone;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmException;
import org.tigr.microarray.mev.cluster.gui.IFramework;

/**
 *
 * @author  Adam Margolin
 * @author Raktim Sinha
 */

public abstract class CloneAlterations extends NumberOfAlterationsCalculator{

    /** Creates a new instance of CloneAlterations */
    public CloneAlterations() {
    }

    public DefaultMutableTreeNode execute(IFramework framework) throws AlgorithmException {
        this.framework = framework;
        this.data = framework.getData();
        //SortedSet alterationRegions = new TreeSet(new AlterationRegionsComparator());
        Vector alterationRegions = new Vector();

        for(int i = 0; i < this.framework.getData().getFeaturesSize(); i++){
            CGHClone curClone = this.framework.getData().getCloneAt(i);
            int numAlterations = getNumAlterations(i);

            AlterationRegion curAlterationRegion = new AlterationRegion();
            curAlterationRegion.setDataRegion(curClone);
            curAlterationRegion.setNumAlterations(numAlterations);
            curAlterationRegion.setNumSamples(this.framework.getData().getFeaturesCount());
            //curAlterationRegion.setPercentAltered((float)numAlterations / (float)fcd.getData().getFeaturesCount());
            alterationRegions.add(curAlterationRegion);
        }
        Collections.sort(alterationRegions, new AlterationRegionsComparator());
        return createResultsTree(alterationRegions);
    }

    public int getNumAlterations(int cloneIndex){
        int numAlterations = 0;
        for(int i = 0; i < data.getFeaturesCount(); i++){
            int copyNumber = data.getCopyNumberDetermination(i, cloneIndex);
            if(isAltered(copyNumber)){
                numAlterations++;
            }
        }
        return numAlterations;
    }

    protected abstract boolean isAltered(int copyNumber);

}
