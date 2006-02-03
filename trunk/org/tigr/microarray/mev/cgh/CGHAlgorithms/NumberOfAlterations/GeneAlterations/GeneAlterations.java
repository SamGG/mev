/*
 * GeneAlterations.java
 *
 * Created on May 21, 2003, 5:43 PM
 */

package org.tigr.microarray.mev.cgh.CGHAlgorithms.NumberOfAlterations.GeneAlterations;

import java.util.Collections;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.tree.DefaultMutableTreeNode;

import org.tigr.microarray.mev.ISlideData;
import org.tigr.microarray.mev.cgh.CGHAlgorithms.NumberOfAlterations.NumberOfAlterationsCalculator;
import org.tigr.microarray.mev.cgh.CGHDataObj.AlterationRegion;
import org.tigr.microarray.mev.cgh.CGHDataObj.AlterationRegionsComparator;
import org.tigr.microarray.mev.cgh.CGHDataObj.FlankingRegion;
import org.tigr.microarray.mev.cgh.CGHDataObj.GeneDataSet;
import org.tigr.microarray.mev.cgh.CGHDataObj.ICGHDataRegion;
import org.tigr.microarray.mev.cgh.CGHDataObj.RefGeneLinkData;
import org.tigr.microarray.mev.cluster.algorithm.AbortException;
import org.tigr.microarray.mev.cluster.algorithm.AlgorithmException;
import org.tigr.microarray.mev.cluster.gui.IFramework;
/**
 * @author  Adam Margolin
 * @author Raktim Sinha
 */

public abstract class GeneAlterations extends NumberOfAlterationsCalculator{
    private Vector vecGeneData = null;

    /** Creates a new instance of GeneAlterations */
    public GeneAlterations() {
    }

    public GeneAlterations(IFramework framework) {
    	this.framework = framework;
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
        this.data = framework.getData();
        //Vector vecGeneData = null;
        if(vecGeneData == null){
            vecGeneData = getAllGenes();
        }

        if(vecGeneData == null){
            return null;
        }

        Iterator it = vecGeneData.iterator();
        Vector alterationRegions = new Vector();
        while(it.hasNext()){

            RefGeneLinkData curGeneData = (RefGeneLinkData)it.next();

            int numAlterations = getNumAlterations(curGeneData);

            AlterationRegion curAlterationRegion = new AlterationRegion();
            curAlterationRegion.setDataRegion(curGeneData);
            curAlterationRegion.setNumAlterations(numAlterations);
            curAlterationRegion.setNumSamples(framework.getData().getFeaturesCount());

            alterationRegions.add(curAlterationRegion);
        }
        Collections.sort(alterationRegions, new AlterationRegionsComparator());
        return createResultsTree(alterationRegions);
    }

    public int getNumAlterations(ICGHDataRegion geneData){
        int deletions = 0;
        int chromosomeIndex = geneData.getChromosomeIndex();
        int geneStart = geneData.getStart();
        int geneEnd = geneData.getStop();

        if(chromosomeIndex < 0){
            return 0;
        }

        if(framework == null){
            System.out.println("gene alterations null framework");
        }
        Iterator featuresIt = framework.getData().getFeaturesList().iterator();
        while(featuresIt.hasNext()){
            Vector expFrs = ((ISlideData)featuresIt.next()).getFlankingRegions()[chromosomeIndex];
            Iterator itFrs = expFrs.iterator();
            while(itFrs.hasNext()){
                FlankingRegion fr = (FlankingRegion)itFrs.next();
                int frStart = fr.getStart();
                int frStop = fr.getStop();

                if( (geneStart < frStop && geneStart > frStart) || (geneEnd < frStop && geneStart > frStart)  ){
                    if(fr.getType() == getAlterationType()){
                        deletions++;
                        break;
                    }
                }
            }
        }

        return deletions;

    }

    protected abstract int getAlterationType();

    private Vector getAllGenes(){
        GeneDataSet geneDataSet = new GeneDataSet();
        geneDataSet.loadAllGenes(data.getCGHSpecies());
        Vector vecGeneData = geneDataSet.getGeneData();
        return vecGeneData;
    }

    /** Getter for property vecGeneData.
     * @return Value of property vecGeneData.
     */
    public java.util.Vector getVecGeneData() {
        return vecGeneData;
    }

    /** Setter for property vecGeneData.
     * @param vecGeneData New value of property vecGeneData.
     */
    public void setVecGeneData(java.util.Vector vecGeneData) {
        this.vecGeneData = vecGeneData;
    }

}
