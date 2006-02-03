/*
 * NumberOfDeletionsAmpilficationsDataModel.java
 *
 * Created on June 15, 2003, 6:49 AM
 */

package org.tigr.microarray.mev.cgh.CGHGuiObj.AlgorithmResultsViewers.NumberOfAlterationsViewers;

import org.tigr.microarray.mev.cgh.CGHDataObj.AlterationRegion;
import org.tigr.microarray.mev.cgh.CGHDataObj.ICGHDataRegion;

/**
 *
 * @author  Adam Margolin
 * @author Raktim Sinha
 */

public class NumberOfDeletionsAmpilficationsDataModel extends NumberOfAlterationsDataModel{

    /** Creates a new instance of NumberOfAlterationsDataModel */
    public NumberOfDeletionsAmpilficationsDataModel () {
    }

    /** Getter for property alterationRegions.
     * @return Value of property alterationRegions.
     */
    public AlterationRegion[] getAlterationRegions() {
        return this.alterationRegions;
    }

    /** Setter for property alterationRegions.
     * @param alterationRegions New value of property alterationRegions.
     */
    public void setAlterationRegions(AlterationRegion[] alterationRegions) {
        this.alterationRegions = alterationRegions;
    }

    public int getColumnCount() {
        return 8;
    }

    public int getRowCount() {
        return alterationRegions.length;
    }

    public Object getValueAt(int row, int col) {
        switch(col){
            case 0:
                return alterationRegions[row].getDataRegion().getName();
            case 1:
                return new Integer(alterationRegions[row].getDataRegion().getChromosomeIndex() + 1);
            case 2:
                return new Integer(alterationRegions[row].getDataRegion().getStart());
            case 3:
                return new Integer(alterationRegions[row].getDataRegion().getStop());
            case 4:
                return new Integer(alterationRegions[row].getNumDeletions());
            case 5:
                return new Float(alterationRegions[row].getPercentDeleted());
            case 6:
                return new Integer(alterationRegions[row].getNumAmplifications());
            case 7:
                return new Float(alterationRegions[row].getPercentAmplified());
        }
        return null;
    }

    public String getColumnName(int column){
        switch(column){
            case 0:
                return "Name";
            case 1:
                return "Chrom";
            case 2:
                return "Start";
            case 3:
                return "Stop";
            case 4:
                return "# Deletions";
            case 5:
                return "% Deleted";
            case 6:
                return "# Amplfications";
            case 7:
                return "% Amplified";
        }
        return null;
    }
    /*
    public String getColumnName(int col){
        return results.getHeaderAt(col);
    }
     */
    public Class getColumnClass(int c) {
        if(getValueAt(0, c) == null){
          return String.class;
        }else{
            return getValueAt(0, c).getClass();
        }
    }

    public ICGHDataRegion getDataRegionAt(int index){
        return alterationRegions[index].getDataRegion();
    }
}
