/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
All rights reserved.
*/
/*
 * $RCSfile: SOTATreeData.java,v $
 * $Revision: 1.1.1.2 $
 * $Date: 2004-02-06 21:48:18 $
 * $Author: braisted $
 * $State: Exp $
 */
package org.tigr.microarray.mev.cluster.gui.impl.sota;

import org.tigr.util.FloatMatrix;

public class SOTATreeData {

    public float[] nodeHeights;    
    public int[] leftChild;    
    public int[] rightChild;    
    public int[] nodePopulation;
    
    public boolean absolute;
    public int function;
    public float factor;
    
    public int [] clusterPopulation;
    public FloatMatrix clusterDiversity;
    public int [] cluster;
    public FloatMatrix centroidMatrix;
    
    /** Creates new SOTATreeData */
    public SOTATreeData() {
    }
}
