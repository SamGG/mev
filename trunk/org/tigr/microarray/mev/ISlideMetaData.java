/*
Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).  
All rights reserved.
*/
/*
 * $RCSfile: ISlideMetaData.java,v $
 * $Revision: 1.1.1.2 $
 * $Date: 2004-02-06 21:48:17 $
 * $Author: braisted $
 * $State: Exp $
 */
package org.tigr.microarray.mev;

public interface ISlideMetaData {
    /**
     * Returns size of a microarray.
     */
    public int getSize();

    /**
     * Returns a spot base column.
     */
    public int getColumn(int index);

    /**
     * Returns a spot base row.
     */
    public int getRow(int index);

    /**
     * Returns number of a microarray columns.
     */
    public int getColumns();

    /**
     * Returns number of a microarray rows.
     */
    public int getRows();

    /**
     * Returns description of a specified microarray spot.
     */
    public String getValueAt(int index, int valueType);

    /**
     * Returns true if a specified spot has no zero values.
     */
    public boolean hasNoZeros(int index);

    /**
     * Sets a microarray non-zero flag.
     */
    public void setNonZero(boolean state);

    /**
     * Returns reference to an element with specified index.
     */
    public ISlideDataElement toSlideDataElement(int index);
}
