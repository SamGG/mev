/*
 * CGHDataRegion.java
 *
 * Created on March 25, 2003, 3:24 PM
 */

package org.tigr.microarray.mev.cgh.CGHDataObj;

/**
 *
 * @author  Adam Margolin
 * @author Raktim Sinha
 */

public interface ICGHDataRegion {
    public String getName();
    public int getChromosomeIndex();
    public int getStart();
    public int getStop();
}
