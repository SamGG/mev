/*
 * NumberOfAlterationsDataModel.java
 *
 * Created on May 19, 2003, 12:45 AM
 */

package org.tigr.microarray.mev.cgh.CGHGuiObj.AlgorithmResultsViewers.NumberOfAlterationsViewers;

import org.tigr.microarray.mev.cgh.CGHDataObj.ICGHDataRegion;

/**
 *
 * @author  Adam Margolin
 * @author Raktim Sinha
 */

public class DataRegionsDataModel extends NumberOfAlterationsDataModel{
    ICGHDataRegion[] dataRegions;

    /** Creates a new instance of NumberOfAlterationsDataModel */
    public DataRegionsDataModel(ICGHDataRegion[] dataRegions){
        this.dataRegions = dataRegions;
    }


    public int getColumnCount() {
        return 4;
    }

    public int getRowCount() {
        return dataRegions.length;
    }

    public Object getValueAt(int row, int col) {
        switch(col){
            case 0:
                return dataRegions[row].getName();
            case 1:
                return new Integer(dataRegions[row].getChromosomeIndex() + 1);
            case 2:
                return new Integer(dataRegions[row].getStart());
            case 3:
                return new Integer(dataRegions[row].getStop());
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
        return dataRegions[index];
    }

}
