/*
 * RegionAlterations.java
 *
 * Created on May 19, 2003, 6:17 PM
 */

package org.tigr.microarray.mev.cgh.CGHAlgorithms.NumberOfAlterations.RegionAlterations;


import java.util.Collections;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.tree.DefaultMutableTreeNode;

import org.tigr.microarray.mev.ISlideData;
import org.tigr.microarray.mev.MultipleArrayData;
import org.tigr.microarray.mev.cgh.CGHAlgorithms.NumberOfAlterations.NumberOfAlterationsCalculator;
import org.tigr.microarray.mev.cgh.CGHDataObj.AlterationRegion;
import org.tigr.microarray.mev.cgh.CGHDataObj.AlterationRegions;
import org.tigr.microarray.mev.cgh.CGHDataObj.AlterationRegionsComparator;
import org.tigr.microarray.mev.cgh.CGHDataObj.FlankingRegion;
import org.tigr.microarray.mev.cluster.algorithm.AbortException;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmException;
import org.tigr.microarray.mev.cluster.gui.IFramework;

/**
 *
 * @author  Adam Margolin
 * @author Raktim Sinha
 */

public abstract class RegionAlterations extends NumberOfAlterationsCalculator{

    /** Creates a new instance of RegionAlterations */
    public RegionAlterations() {
    }

    /** This method should return a tree with calculation results or
     * null, if analysis start was canceled.
     *
     * @param framework the reference to <code>IFramework</code> implementation,
     *        which is used to obtain an initial analysis data and parameters.
     * @throws AlgorithmException if calculation was failed.
     * @throws AbortException if calculation was canceled.
     * @see IFramework
     */
    public DefaultMutableTreeNode execute(IFramework framework) throws AlgorithmException {
        this.framework = framework;

        MultipleArrayData data = (MultipleArrayData)framework.getData();
        Vector allAlterationRegions = new Vector();

        for(int chromIndex = 0; chromIndex < data.getNumChromosomes(); chromIndex++){

            AlterationRegions curAlterationRegions = new AlterationRegions(chromIndex);
            Vector curFlankingRegions = new Vector(data.getFeaturesCount());

            Iterator featuresIt = data.getFeaturesList().iterator();

            while(featuresIt.hasNext()){
                Vector curFrs = ((ISlideData)featuresIt.next()).getFlankingRegions()[chromIndex];
                curFlankingRegions.add( curFrs.clone() );
            }

            int[] indices = new int[data.getFeaturesCount()];
            for(int i = 0; i < data.getFeaturesCount(); i++){
                indices[i] = 0;
            }

            boolean more = true;

            while(more){

                for(int i = 0; i < indices.length; i++){
                    Vector expRegions = (Vector) curFlankingRegions.get(i);
                    while(indices[i] < expRegions.size() && ((FlankingRegion)expRegions.get(indices[i])).getType() != getFlankingRegionType()  ){
                        indices[i] += 1;
                    }
                }

                int minStartIndex = getMinStartIndex(curFlankingRegions, indices);
                int minEndIndex = getMinEndIndex(curFlankingRegions, indices, minStartIndex);

                for(int i = 0; i < indices.length; i++){
                    Vector expRegions = (Vector) curFlankingRegions.get(i);

                    if(indices[i] < expRegions.size()){
                        FlankingRegion fr = (FlankingRegion) expRegions.get(indices[i]);
                        int start = fr.getStart();
                        int stop = fr.getStop();

                        if(start == minStartIndex){

                            AlterationRegion curAlterationRegion = curAlterationRegions.getAlterationRegion(minStartIndex, minEndIndex, getFlankingRegionType(), data.getFeaturesCount());
                            curAlterationRegion.incrementAlterations();
                            curAlterationRegion.getAlteredExperiments().add(new Integer(i));

                            if(stop == minEndIndex){
                                indices[i] += 1;
                            }else{
                                FlankingRegion tmp = new FlankingRegion(minEndIndex, fr.getStop(),
                                getFlankingRegionType(), chromIndex);
                                expRegions.set(indices[i], tmp);

                            }
                        }
                    }
                }
                more = false;
                for(int i = 0; i < indices.length; i++){
                    Vector expRegions = (Vector) curFlankingRegions.get(i);
                    if(indices[i] < expRegions.size()){
                        more = true;
                    }
                }
            }
            allAlterationRegions.addAll(curAlterationRegions.getAlterationRegions());
        }
        Collections.sort(allAlterationRegions, new AlterationRegionsComparator());
        return createResultsTree(allAlterationRegions);
    }


    private int getMinStartIndex(Vector flankingRegions, int[] indices){
        int min = Integer.MAX_VALUE;

        for(int i = 0; i < indices.length; i++){
            Vector expRegions = (Vector) flankingRegions.get(i);

            if(indices[i] < expRegions.size()){
                min = Math.min(min, ((FlankingRegion) expRegions.get(indices[i])).getStart());
            }
        }

        return min;
    }

    private int getMinEndIndex(Vector flankingRegions, int[] indices, int minStartIndex) {
        int min = Integer.MAX_VALUE;

        for(int i = 0; i < indices.length; i++){
            Vector expRegions = (Vector) flankingRegions.get(i);

            if(indices[i] < expRegions.size()){
                int start = ((FlankingRegion) expRegions.get(indices[i])).getStart();
                if(start == minStartIndex){
                    start = ((FlankingRegion) expRegions.get(indices[i])).getStop();
                }

                min = Math.min(min, start);
            }
        }

        return min;
    }

    public abstract int getFlankingRegionType();

}
